package com.las4vc.composevr.browser;
import com.bitwig.extension.callback.IntegerValueChangedCallback;
import com.las4vc.composevr.RemoteEventHandler;
import com.las4vc.composevr.DAWModel;
import com.las4vc.composevr.RemoteEventEmitter;

import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.callback.StringValueChangedCallback;
import com.bitwig.extension.controller.api.*;

import com.las4vc.composevr.protocol.Browser;
import com.las4vc.composevr.protocol.Module;
import com.las4vc.composevr.protocol.Protocol;
import de.mossgrabers.framework.daw.BrowserProxy;
import de.mossgrabers.framework.daw.data.BrowserColumnItemData;

/**
 * The BrowserModel is responsible for manipulating Bitwig's PopupBrowser and providing access to PopupBrowser data
 *
 * @author Lane Spangler
 */

public class BrowserModel extends RemoteEventHandler implements TrackSelectionChangeEvent {

    public BrowserProxy browserProxy;
    public BrowserFilterModel filterModel;
    public BrowserColumnItemData[] browserResults;

    public Track selectedTrack;
    public CursorDevice cursorDevice;
    public int browserPage = 0;
    public boolean browseToReplace = false;

    private int selectionIndex = -1;
    private int pageChange = 0;

    private enum Event {
        TRACK_SELECTION_CHANGE, BROWSER_ACTIVE_CHANGE, BROWSER_RESULTS_CHANGE, OPEN_REQUEST, CLOSE_REQUEST
    }

    private enum State {
        CLOSED {
            @Override
            State next(BrowserModel browser, Event e){
                if(e != Event.OPEN_REQUEST){
                    return CLOSED;
                }

                browser.model.host.println("Selecting track");

                browser.browserPage = 0;
                browser.browserProxy.stopBrowsing(false);

                if(browser.browseToReplace){
                    if(browser.cursorDevice.exists().get()){
                        browser.cursorDevice.browseToReplaceDevice();
                    }else{
                        browser.selectedTrack.browseToInsertAtStartOfChain();
                    }
                }else{
                    if(browser.cursorDevice.exists().get()){
                        browser.cursorDevice.browseToInsertBeforeDevice();
                    }else{
                        browser.selectedTrack.browseToInsertAtStartOfChain();
                    }
                }

                browser.model.host.println("Loading initial");
                return LOADING_INITIAL_RESULTS;
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


                if(browser.browserProxy.getNumTotalResults() == 0 || !browser.filterModel.autoFiltersSet()){
                    browser.filterModel.setTargetContentType();
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

        browserProxy = new BrowserProxy(model.host, model.mainCursorTrack, model.cursorDeviceProxy, 15, 15);
        browserProxy.enableObservers(true);

        setUpBrowserToggleCallback();
        setUpResultsChangedCallbacks();
        setUpCanScrollCallbacks();

        filterModel = new BrowserFilterModel(model, browserProxy);

        currentState = State.CLOSED;
    }

    private void setUpBrowserToggleCallback(){
        BooleanValueChangedCallback browserToggleCallback = new BooleanValueChangedCallback() {
            @Override
            public void valueChanged(boolean b) {
                onBrowserActiveChanged();
            }
        };
        browserProxy.addActiveObserver(browserToggleCallback);
    }

    private void setUpResultsChangedCallbacks(){
        //Set up callback for BrowserItemChanged messages
        browserResults = browserProxy.getResultColumnItems();

        for(int i = 0; i < browserResults.length; i++) {
            final int idx = i;
            StringValueChangedCallback resultsChangedCallback = new StringValueChangedCallback() {
                @Override
                public void valueChanged(String s) {
                    onResultItemChanged(idx, s);
                }
            };
            browserResults[i].addNameObserver(resultsChangedCallback);
        }

        //Set up callback when for when total number of results changes
        IntegerValueChangedCallback numResultsChangedCallback = new IntegerValueChangedCallback() {
            @Override
            public void valueChanged(int i) {
                onNumResultsChanged(i);
            }
        };

        browserProxy.getResultEntryCount().addValueObserver(numResultsChangedCallback);
    }

    private void setUpCanScrollCallbacks(){
        //Callbacks for scrollability
        BooleanValueChangedCallback canScrollForwardChangedCallback = new BooleanValueChangedCallback() {
            @Override
            public void valueChanged(boolean b) {
                onResultsCanScrollForwardChanged(b);
            }
        };
        browserProxy.hasNextResultPage().addValueObserver(canScrollForwardChangedCallback);

        BooleanValueChangedCallback canScrollBackwardChangedCallback = new BooleanValueChangedCallback() {
            @Override
            public void valueChanged(boolean b) {
                onResultsCanScrollBackwardChanged(b);
            }
        };
        browserProxy.hasPreviousResultPage().addValueObserver(canScrollBackwardChangedCallback);
    }

    /**
     * Opens the browser on a track
     */
    public void openBrowser(int trackPosition, Module.OpenBrowser event){
        selectedTrack = model.mainTrackBank.getChannel(trackPosition);

        cursorDevice = model.mainCursorDevices[trackPosition];
        cursorDevice.selectFirstInChannel(selectedTrack);
        for(int i = 0; i < event.getDeviceIndex(); i++){
            cursorDevice.selectNext();
        }

        filterModel.targetContentType = event.getContentType();

        browseToReplace = event.getReplaceDevice();
        if(browseToReplace){
            filterModel.selectDevice(cursorDevice.name().get());
        }

        filterModel.selectDeviceType(event.getDeviceType());

        currentState = State.CLOSED;
        currentState = currentState.next(this, Event.OPEN_REQUEST);
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

        browserProxy.selectCurrentResult();

        //Load the selected device and stop browsing
        browserProxy.stopBrowsing(true);

    }

    private void selectResult(){
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

        browserProxy.selectCurrentResult();
    }

    /* Local event handlers */

    /**
     * Callback when the track selection changes
     */
    public void onTrackSelectionChanged(){
        currentState = currentState.next(this, Event.TRACK_SELECTION_CHANGE);
    }

    /**
     * Callback when the browser's active state is changed
     */
    private void onBrowserActiveChanged(){
        currentState = currentState.next(this, Event.BROWSER_ACTIVE_CHANGE);
    }

    private void onNumResultsChanged(int numResults){
        RemoteEventEmitter.OnBrowserColumnChanged(model, "Results", browserProxy.getNumResultsPerPage(), numResults, filterModel.getSelectedFilter("Device Type"));
    }

    /**
     * Callback for when the results column changes.
     * Sends results to client
     */
    private void onResultItemChanged(int idx, String itemName){

        currentState = currentState.next(this, Event.BROWSER_RESULTS_CHANGE);

        if(pageChange != 0){
            changeResultsPage();
        }

        //Clamp page index
        browserPage = Math.max(0, browserPage);

        //Handle load request
        if(selectionIndex != -1){
            loadDevice();
            currentState = currentState.next(this, Event.CLOSE_REQUEST);
        }

        RemoteEventEmitter.OnBrowserItemChanged(model, "Results", idx, itemName);
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

    private void onResultsCanScrollForwardChanged(boolean val){
        RemoteEventEmitter.OnArrowVisibilityChanged(model, "Results", Browser.OnArrowVisibilityChanged.Arrow.DOWN, val);
    }

    private void onResultsCanScrollBackwardChanged(boolean val){
        RemoteEventEmitter.OnArrowVisibilityChanged(model, "Results", Browser.OnArrowVisibilityChanged.Arrow.UP, val);
    }

    /* Remote Event Handlers */
    /**
     * Increment or decrement the results page by the specified amount
     */
    public void ChangeResultsPage(Protocol.Event e){
        Browser.ChangeResultsPage params = e.getBrowserEvent().getChangeResultsPageEvent();
        pageChange += params.getPageChange();

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
            selectionIndex = deviceIndex;
            loadDevice();
        }
    }

    public void SelectResult(Protocol.Event e){
        Browser.SelectResult params = e.getBrowserEvent().getSelectResultEvent();

        int deviceIndex = params.getIndex();

        if(deviceIndex != -1){
            browserProxy.selectResultAt(deviceIndex);
        }
    }

    public void CommitSelection(Protocol.Event e){
        if(e.getBrowserEvent().getCommitSelectionEvent().getCommit()) {
            browserProxy.stopBrowsing(true);

            //Send confirmation to client
            RemoteEventEmitter.OnDeviceLoaded(model);
        }else{
            browserProxy.stopBrowsing(false);
        }
    }

    /**
     * Try to load the device with the supplied name
     * @param e
     */
    public void LoadDeviceWithName(Protocol.Event e){
        Browser.LoadDeviceWithName params = e.getBrowserEvent().getLoadDeviceWithNameEvent();
    }

    /**
     * Close the browser
     */
    public void CloseBrowser(Protocol.Event e){
        currentState = currentState.next(this, Event.CLOSE_REQUEST);
    }



}
