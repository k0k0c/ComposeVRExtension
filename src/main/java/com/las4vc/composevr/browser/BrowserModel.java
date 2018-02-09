package com.las4vc.composevr.browser;
import com.bitwig.extension.callback.IntegerValueChangedCallback;
import com.las4vc.composevr.RemoteEventHandler;
import com.las4vc.composevr.DAWModel;
import com.las4vc.composevr.RemoteEventEmitter;

import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.callback.StringValueChangedCallback;
import com.bitwig.extension.controller.api.*;

import com.las4vc.composevr.protocol.Browser;
import com.las4vc.composevr.protocol.Protocol;
import de.mossgrabers.framework.daw.BrowserProxy;
import de.mossgrabers.framework.daw.data.BrowserColumnItemData;

import java.util.ArrayList;

/**
 * The BrowserModel is responsible for manipulating Bitwig's PopupBrowser and providing access to PopupBrowser data
 *
 * @author Lane Spangler
 */

public class BrowserModel extends RemoteEventHandler implements TrackSelectionChangeEvent {

    public BrowserProxy browserProxy;
    public BrowserColumnItemData[] browserResults;
    public Track selectedTrack;
    public int browserPage = 0;


    private int selectionIndex = -1;
    private int pageChange = 0;
    private int scrollOffset = 0;
    private String deviceToFind = "";
    public BrowserFilterModel filterModel;

    private enum Event {
        TRACK_SELECTION_CHANGE, BROWSER_ACTIVE_CHANGE, BROWSER_RESULTS_CHANGE, OPEN_REQUEST, CLOSE_REQUEST
    }

    private enum State {
        CLOSED {
            @Override
            State next(BrowserModel browser, Event e){
                if(e != Event.OPEN_REQUEST){
                    browser.model.host.println(e.toString());
                    return CLOSED;
                }

                browser.model.host.println("Selecting track");

                browser.browserPage = 0;

                //Move the cursorTrack to the desired track
                browser.model.addTrackSelectionChangeListener(browser);
                browser.model.cursorTrack.selectChannel(browser.selectedTrack);
                return SELECTING_TRACK;
            }
        },
        SELECTING_TRACK{
            @Override
            State next(BrowserModel browser, Event e){
                if(e == Event.CLOSE_REQUEST){
                    if(browser.browserProxy.isActive()) {
                        browser.browserProxy.stopBrowsing(false);
                    }
                    return CLOSED;
                }

                if(e != Event.TRACK_SELECTION_CHANGE){
                    return SELECTING_TRACK;
                }

                browser.model.host.println("Opening browser");

                //Selected track is now the desired track. Begin browsing
                browser.browserProxy.browseToInsertBeforeDevice();
                browser.model.removeTrackSelectionChangeListener(browser);

                return State.LOADING_INITIAL_RESULTS;
            }
        },
        LOADING_INITIAL_RESULTS{
            @Override
            State next(BrowserModel browser, Event e){

                if(e == Event.CLOSE_REQUEST) {
                    if(browser.browserProxy.isActive()) {
                        browser.browserProxy.stopBrowsing(false);
                    }
                    return CLOSED;
                }

                if(browser.browserProxy.getNumTotalResults() == 0 || !browser.filterModel.getTargetDeviceType().equals(browser.filterModel.targetDeviceType)){
                    return LOADING_INITIAL_RESULTS;
                }

                return LOADING_RESULTS;
            }
        },
        LOADING_RESULTS{
            @Override
            State next(BrowserModel browser, Event e){

                if(e == Event.CLOSE_REQUEST) {
                    if(browser.browserProxy.isActive()) {
                        browser.browserProxy.stopBrowsing(false);
                    }
                    return CLOSED;
                }

                return LOADING_RESULTS;
            }
        };

        abstract  State next(BrowserModel browser, Event e);
    }

    private State currentState;

    public BrowserModel(DAWModel model){
        super(model);
        model.router.addReceiver(this, "browser");

        browserProxy = new BrowserProxy(model.host, model.cursorTrack, model.cursorDeviceProxy, 15, 15);
        browserProxy.enableObservers(true);

        BooleanValueChangedCallback browserToggleCallback = new BooleanValueChangedCallback() {
            @Override
            public void valueChanged(boolean b) {
                handleBrowserActiveChange();
            }
        };
        browserProxy.addActiveObserver(browserToggleCallback);

        //Set up callback for BrowserItemChanged messages
        browserResults = browserProxy.getResultColumnItems();

        for(int i = 0; i < browserResults.length; i++) {
            final int idx = i;
            StringValueChangedCallback resultsChangedCallback = new StringValueChangedCallback() {
                @Override
                public void valueChanged(String s) {
                    handleResultItemChange(idx, s);
                }
            };
            browserResults[i].addNameObserver(resultsChangedCallback);
        }

        //Set up callback when for when total number of results changes
        IntegerValueChangedCallback numResultsChangedCallback = new IntegerValueChangedCallback() {
            @Override
            public void valueChanged(int i) {
                handleNumResultsChange(i);
            }
        };

        browserProxy.getResultEntryCount().addValueObserver(numResultsChangedCallback);


        //Callbacks for scrollability
        BooleanValueChangedCallback canScrollForwardChangedCallback = new BooleanValueChangedCallback() {
            @Override
            public void valueChanged(boolean b) {
                handleResultsCanScrollForwardChanged(b);
            }
        };
        browserProxy.hasNextResultPage().addValueObserver(canScrollForwardChangedCallback);

        BooleanValueChangedCallback canScrollBackwardChangedCallback = new BooleanValueChangedCallback() {
            @Override
            public void valueChanged(boolean b) {
                handleResultsCanScrollBackwardChanged(b);
            }
        };
        browserProxy.hasPreviousResultPage().addValueObserver(canScrollBackwardChangedCallback);

        filterModel = new BrowserFilterModel(model, browserProxy);

        currentState = State.CLOSED;
    }

    /**
     * Opens the browser on a track
     * @param t The track to browse on
     */
    public void openBrowser(Track t, String contentType){
        this.selectedTrack = t;
        filterModel.targetDeviceType = contentType;

        currentState = State.CLOSED;
        currentState = currentState.next(this, Event.OPEN_REQUEST);


    }


    /**
     * Callback when the track selection changes
     */
    public void OnTrackSelectionChange(){
        currentState = currentState.next(this, Event.TRACK_SELECTION_CHANGE);
    }


    /**
     * Callback when the browser's active state is changed
     */
    private void handleBrowserActiveChange(){
        currentState = currentState.next(this, Event.BROWSER_ACTIVE_CHANGE);
    }

    private void handleNumResultsChange(int numResults){
        RemoteEventEmitter.OnBrowserColumnChanged(model, "Results", browserProxy.getNumResultsPerPage(), numResults);
    }

    /**
     * Callback for when the results column changes.
     * Sends results to client
     */
    private void handleResultItemChange(int idx, String itemName){

        State prevState = currentState;
        currentState = currentState.next(this, Event.BROWSER_RESULTS_CHANGE);


        //Wrangling the browser to make the columns display all of their content on the first request
        if(currentState == State.LOADING_INITIAL_RESULTS){
            filterModel.setDeviceType();
        }

        if(pageChange != 0){
            changeResultsPage();
        }

        //Clamp page index
        browserPage = Math.max(0, browserPage);


        //Handle request by name
        if(!deviceToFind.equals("")){
            findDevice();
        }

        //Handle load request
        if(selectionIndex != -1){
            loadDevice();
            currentState = currentState.next(this, Event.CLOSE_REQUEST);
        }

        if(itemName.equals("Results")){
            return;
        }

        RemoteEventEmitter.OnBrowserItemChanged(model, "Results", idx, itemName);
    }

    /**
     * Sets cursor to point at the first item for the results column and every filter column
     */
    private void resetBrowser(){
        browserProxy.setResultsScrollPosition(0);
        browserProxy.selectFirstResult();

        for(int i = 0; i < browserProxy.getFilterColumnCount(); i++){
            if(!browserProxy.getFilterColumn(i).getName().equals("Device Type")) {
                browserProxy.getFilterColumn(i).selectFirst();
            }
        }
    }


    /**
     * Finds the device with the same name as deviceToFind
     */
    private void findDevice(){
        BrowserColumnItemData[] pageResults = browserProxy.getResultColumnItems();

        boolean foundDevice = false;

        for (int i = 0; i < pageResults.length; i++) {

            String currentDevice = pageResults[i].getName();

            if (currentDevice.equals("")) {
                if (browserPage > 1) {
                    //Can't find device
                    deviceToFind = "";

                    //Notify client
                    RemoteEventEmitter.OnDeviceNotFound(model);
                }
                break;
            }

            if (currentDevice.equals(deviceToFind)) {
                selectionIndex = pageResults[i].getIndex();

                if (browserPage > 0) {
                    selectionIndex += browserProxy.getResultsScrollPosition();
                }

                deviceToFind = "";
                foundDevice = true;
                break;
            }
        }

        if(!foundDevice && !deviceToFind.equals("")){
            browserPage += 1;
            browserProxy.nextResultPage();
        }
    }


    /**
     * Loads the device at selectionIndex
     */
    private void loadDevice(){
        int currentIndex = browserProxy.getSelectedResultIndex();

        //Navigate to the supplied index
        while (currentIndex != selectionIndex) {
            if (currentIndex < selectionIndex) {
                currentIndex++;
                browserProxy.selectNextResult();
            } else {
                currentIndex--;
                browserProxy.selectPreviousResult();
            }
        }

        selectionIndex = -1;

        //Load the selected device and stop browsing
        browserProxy.stopBrowsing(true);

        //Send confirmation to client
        RemoteEventEmitter.OnDeviceLoaded(model);
    }


    /**
     * Changes the results page according to the value of pageChange
     */
    private void changeResultsPage(){
        //Increment the page by the specified amount
        while(pageChange != 0) {
            if (pageChange > 0) {
                browserProxy.nextResultPage();
                browserPage += 1;
                pageChange -= 1;
                //scrollOffset = -browserProxy.getNumResultsPerPage();
            } else {
                browserProxy.previousResultPage();
                browserPage -= 1;
                pageChange += 1;
                //scrollOffset = browserProxy.getNumResultsPerPage();
            }
        }
    }

    private void handleResultsCanScrollForwardChanged(boolean val){
        RemoteEventEmitter.OnArrowVisibilityChanged(model, "Results", Browser.OnArrowVisibilityChanged.Arrow.DOWN, val);
    }

    private void handleResultsCanScrollBackwardChanged(boolean val){
        RemoteEventEmitter.OnArrowVisibilityChanged(model, "Results", Browser.OnArrowVisibilityChanged.Arrow.UP, val);
    }

    /**
     * Increment or decrement the results page by the specified amount
     */
    public void ChangeResultsPage(Protocol.Event e){
        Browser.ChangeResultsPage params = e.getBrowserEvent().getChangeResultsPageEvent();

        //Just check if the state is LOADING RESULT when the request is received
        //Open browser, browser active change, browser results change-> state transition
        if(currentState == State.LOADING_RESULTS){
            pageChange += params.getPageChange();
        }

        model.host.println("Trying to change page");
        changeResultsPage();
    }

    /**
     * Load the device at the given index
     * @param e
     */
    public void LoadDeviceAtIndex(Protocol.Event e){
        Browser.LoadDeviceAtIndex params = e.getBrowserEvent().getLoadDeviceAtIndexEvent();

        int deviceIndex = params.getIndex();

        if(deviceIndex != -1){
            selectionIndex = deviceIndex + browserProxy.getResultsScrollPosition() + scrollOffset;
            loadDevice();
        }
    }

    /**
     * Try to load the device with the supplied name
     * @param e
     */
    public void LoadDeviceWithName(Protocol.Event e){
        Browser.LoadDeviceWithName params = e.getBrowserEvent().getLoadDeviceWithNameEvent();
        deviceToFind = params.getName();
    }


    /**
     * Close the browser
     */
    public void CloseBrowser(Protocol.Event e){
        currentState = currentState.next(this, Event.CLOSE_REQUEST);
    }



}
