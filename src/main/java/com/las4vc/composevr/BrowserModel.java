package com.las4vc.composevr;
import com.las4vc.composevr.events.TrackSelectionChangeEvent;

import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.callback.StringValueChangedCallback;
import com.bitwig.extension.controller.api.*;

import com.las4vc.composevr.requests.BrowserRequest;
import de.mossgrabers.framework.daw.BrowserProxy;
import de.mossgrabers.framework.daw.data.BrowserColumnItemData;

import java.util.ArrayList;

/**
 * The BrowserModel is responsible for manipulating Bitwig's PopupBrowser and providing access to PopupBrowser data
 *
 * @author Lane Spangler
 */

public class BrowserModel implements TrackSelectionChangeEvent {

    public BrowserProxy browserProxy;
    public BrowserColumnItemData[] browserResults;
    public int browserPage = 0;

    private DAWModel model;
    private BrowserRequest currentRequest;
    private Track selectedTrack;

    private enum Event {
        TRACK_SELECTION_CHANGE, BROWSER_ACTIVE_CHANGE, BROWSER_RESULTS_CHANGE, NEW_REQUEST
    }

    private enum State {
        CLOSED {
            @Override
            State next(BrowserRequest request, BrowserModel browser, Event e){
                if(e != Event.NEW_REQUEST){
                    return CLOSED;
                }

                browser.browserPage = 0;

                if(browser.model.cursorTrack.position().equals(request.getTrack().position())) {
                    //The cursorTrack is at the desired track already, so open the browser
                    browser.browserProxy.browseToInsertBeforeDevice();
                    return OPENING;
                }else{
                    //Move the cursorTrack to the desired track
                    browser.model.addTrackSelectionChangeListener(browser);
                    browser.model.cursorTrack.selectChannel(request.getTrack());
                    return SELECTING_TRACK;
                }
            }
        },
        SELECTING_TRACK{
            @Override
            State next(BrowserRequest request, BrowserModel browser, Event e){
                if(e != Event.TRACK_SELECTION_CHANGE){
                    return SELECTING_TRACK;
                }

                //Selected track is now the desired track. Begin browsing
                browser.browserProxy.browseToInsertBeforeDevice();
                browser.model.removeTrackSelectionChangeListener(browser);
                return State.OPENING;
            }
        },
        OPENING{
            @Override
            State next(BrowserRequest request, BrowserModel browser, Event e){
                if(e != Event.BROWSER_ACTIVE_CHANGE){
                    return OPENING;
                }

                //Browser has been activated, browse for presets
                if(!browser.browserProxy.isActive()){
                    browser.browserProxy.browseForPresets();
                }

                return LOADING_RESULTS;
            }
        },
        LOADING_RESULTS{
            @Override
            State next(BrowserRequest request, BrowserModel browser, Event e){

                if(!browser.browserProxy.isActive()){
                    return LOADING_RESULTS;
                }

                if(request.isCancel()) {
                    browser.browserProxy.stopBrowsing(false);
                    return CLOSED;
                }


                if(request.getPageChange() != 0){
                    browser.changePage(request);
                }

                //Handle changes to the filter

                //Clamp page index
                browser.browserPage = Math.min(Math.max(0, browser.browserPage), browser.browserProxy.getNumResults() - 1);

                BrowserColumnItemData[] pageResults = browser.browserProxy.getResultColumnItems();

                //Handle request by name
                if(!request.getDeviceName().equals("")){
                    browser.findDevice(request, pageResults);
                }

                //Handle load request
                if(request.getSelectionIndex() != -1){
                    browser.loadDevice(request);

                    return CLOSED;
                }

                //Create list of results on current page
                ArrayList<String> params = new ArrayList<>();

                for(BrowserColumnItemData item : pageResults){
                    params.add(item.getName());
                }

                //Send page results to client
                Command.BrowserResultsChanged(browser.model, request.getSenderID(), params);

                return LOADING_RESULTS;
            }
        };

        abstract  State next(BrowserRequest request, BrowserModel browser, Event e);
    }

    private State currentState;

    public BrowserModel(DAWModel model){

        this.model = model;
        currentState = State.CLOSED;

        browserProxy = new BrowserProxy(model.host, model.cursorTrack, model.cursorDeviceProxy, 15, 15);
        browserProxy.enableObservers(true);

        BooleanValueChangedCallback browserToggleCallback = new BooleanValueChangedCallback() {
            @Override
            public void valueChanged(boolean b) {
                handleBrowserActiveChange();
            }
        };
        browserProxy.addActiveObserver(browserToggleCallback);

        //Set up callback for when browser results are updated
        browserResults = browserProxy.getResultColumnItems();

        StringValueChangedCallback browserChangedCallback = new StringValueChangedCallback() {
            @Override
            public void valueChanged(String s) {
                handleBrowserResultsChange();
            }
        };
        browserResults[0].addNameObserver(browserChangedCallback);
    }

    public void requestBrowser(BrowserRequest request){
        short prevPageChange = 0;
        if(currentRequest != null){
            prevPageChange = currentRequest.getPageChange();
        }

        currentRequest = request;
        request.setPageChange((short)(request.getPageChange() + prevPageChange));

        currentState = currentState.next(currentRequest, this, Event.NEW_REQUEST);
    }

    public void OnTrackSelectionChange(){
        currentState = currentState.next(currentRequest, this, Event.TRACK_SELECTION_CHANGE);
    }

    private void handleBrowserResultsChange(){
        currentState = currentState.next(currentRequest, this, Event.BROWSER_RESULTS_CHANGE);
    }

    private void handleBrowserActiveChange(){
        currentState = currentState.next(currentRequest, this, Event.BROWSER_ACTIVE_CHANGE);
    }


    public void findDevice(BrowserRequest request, BrowserColumnItemData[] pageResults){
        boolean foundDevice = false;

        for (int i = 0; i < pageResults.length; i++) {

            String currentDevice = pageResults[i].getName();

            if (currentDevice.equals("")) {
                if (browserPage > 1) {
                    //Can't find device
                    request.setDeviceName("");

                    //Notify client
                    Command.DeviceNotFound(model, request.getSenderID());
                }
                break;
            }

            if (currentDevice.equals(request.getDeviceName())) {
                request.setSelectionIndex(pageResults[i].getIndex());
                if (browserPage > 0) {
                    request.setSelectionIndex(request.getSelectionIndex() + (browserPage - 1) * browserProxy.getNumResults());
                }
                request.setDeviceName("");
                foundDevice = true;
                break;
            }
        }

        if(!foundDevice && !request.getDeviceName().equals("")){
            browserProxy.nextResultPage();
            browserPage += 1;
        }
    }

    public void loadDevice(BrowserRequest request){
        int currentIndex = browserProxy.getSelectedResultIndex();

        //Navigate to the supplied index
        while (currentIndex != request.getSelectionIndex()) {
            if (currentIndex < request.getSelectionIndex()) {
                currentIndex++;
                browserProxy.selectNextResult();
            } else {
                currentIndex--;
                browserProxy.selectPreviousResult();
            }
        }

        //Load the selected device and stop browsing
        browserProxy.stopBrowsing(true);

        //Send confirmation to client
        Command.DeviceLoaded(model, request.getSenderID());
    }

    public void changePage(BrowserRequest request){
        //Increment the page in the specified direction
        if(request.getPageChange() > 0){
            browserProxy.nextResultPage();
            browserPage += 1;
            request.setPageChange((short)(request.getPageChange() - 1));
        }else{
            browserProxy.previousResultPage();
            browserPage -= 1;
            request.setPageChange((short)(request.getPageChange() + 1));
        }
    }


}
