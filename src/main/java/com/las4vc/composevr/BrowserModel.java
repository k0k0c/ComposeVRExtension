package com.las4vc.composevr;
import com.las4vc.composevr.events.TrackSelectionChangeEvent;

import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.callback.StringValueChangedCallback;
import com.bitwig.extension.controller.api.*;

import de.mossgrabers.framework.daw.BrowserProxy;
import de.mossgrabers.framework.daw.data.BrowserColumnItemData;
import de.mossgrabers.framework.daw.data.BrowserColumnData;

import java.util.ArrayList;

/**
 * The BrowserModel is responsible for manipulating Bitwig's PopupBrowser and providing access to PopupBrowser data
 *
 * @author Lane Spangler
 */

public class BrowserModel extends CommandReceiver implements TrackSelectionChangeEvent {

    public BrowserProxy browserProxy;
    public BrowserColumnItemData[] browserResults;
    public Track selectedTrack;
    public int browserPage = 0;


    private int selectionIndex = -1;
    private int pageChange = 0;
    private int scrollOffset = 0;
    private String deviceToFind = "";
    private String deviceType = "Any Device Type";

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

                if(browser.browserProxy.getNumTotalResults() == 0 || !browser.getDeviceType().equals(browser.deviceType)){
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

        //Set up callback for when browser results are updated
        browserResults = browserProxy.getResultColumnItems();

        for(int i = 0; i < browserResults.length; i++) {
            final int idx = i;
            StringValueChangedCallback resultsChangedCallback = new StringValueChangedCallback() {
                @Override
                public void valueChanged(String s) {
                    handleBrowserResultsChange(idx);
                }
            };
            browserResults[i].addNameObserver(resultsChangedCallback);
        }

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



        //Set up callbacks for when filter entries are updated
        for(int i = 0; i < browserProxy.getFilterColumnCount(); i++){
            final int idx = i;
            BooleanValueChangedCallback filterCanScrollForwardChanged = new BooleanValueChangedCallback() {
                @Override
                public void valueChanged(boolean b) {
                    handleFilterCanScrollForwardChanged(idx, b);
                }
            };
            browserProxy.getFilterColumn(i).hasNextFilterPage().addValueObserver(filterCanScrollForwardChanged);

            BooleanValueChangedCallback filterCanScrollBackwardChanged = new BooleanValueChangedCallback() {
                @Override
                public void valueChanged(boolean b) {
                    handleFilterCanScrollBackwardChanged(idx, b);
                }
            };
            browserProxy.getFilterColumn(i).hasPreviousFilterPage().addValueObserver(filterCanScrollBackwardChanged);
        }

        currentState = State.CLOSED;
    }

    /**
     * Opens the browser on a track
     * @param t The track to browse on
     */
    public void openBrowser(Track t, String contentType){
        this.selectedTrack = t;
        this.deviceType = contentType;

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

    /**
     * Callback for when the results column changes.
     * Sends results to client
     */
    private void handleBrowserResultsChange(int idx){

        //setDeviceType();

        State prevState = currentState;
        currentState = currentState.next(this, Event.BROWSER_RESULTS_CHANGE);

        //Wrangling the browser to make the columns display all of their content on the first request
        if(currentState == State.LOADING_INITIAL_RESULTS){
            setDeviceType();
        }

        if(currentState != State.LOADING_RESULTS || !browserProxy.isActive()){
            return;
        }

        //Reset the browser to its initial state now that we have the
        if(prevState == State.LOADING_INITIAL_RESULTS){
            resetBrowser();
            setDeviceType();
            return;
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

        BrowserColumnItemData[] pageResults = browserProxy.getResultColumnItems();

        //Create list of results on current page
        ArrayList<String> results = new ArrayList<>();

        for(BrowserColumnItemData item : pageResults){
            results.add(item.getName());
        }

        //Send page results to client
        Command.BrowserColumnChanged(model,
                "Results",
                browserProxy.getNumResultsPerPage(),
                browserProxy.getNumTotalResults(),
                results);

        //Send filter data
        for(int i = 0; i < browserProxy.getFilterColumnCount(); i++){
            handleFilterEntriesChanged(i);
        }

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


    private String getDeviceType(){
        int deviceTypeIndex = browserProxy.getFilterColumnIndex("Device Type", model);
        BrowserColumnData deviceTypeColumn = browserProxy.getFilterColumn(deviceTypeIndex);
        return deviceTypeColumn.getCursorName();
    }
    /**
     * Set device type filter based on BrowserModel deviceType
     */
    private void setDeviceType(){

        int deviceTypeIndex = browserProxy.getFilterColumnIndex("Device Type", model);

        if(deviceTypeIndex != -1) {
            BrowserColumnData deviceTypeColumn = browserProxy.getFilterColumn(deviceTypeIndex);
            deviceTypeColumn.selectFirst();

            int itemIndex = -1;

            for (int i = 0; i < deviceTypeColumn.getItems().length; i++) {

                if (deviceTypeColumn.getItems()[i].getName().equals(deviceType)) {
                    itemIndex = i;
                    break;
                }
            }

            if(itemIndex != -1){
                for(int i = 0; i < itemIndex; i++){
                    deviceTypeColumn.selectNextItem();
                }
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
                    Command.DeviceNotFound(model);
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
        Command.DeviceLoaded(model);
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


    /**
     * Increment or decrement the results page by the specified amount
     *
     * @param params [0] Should contain an integer representing the desired change in pages
     */
    public void changeResultsPage(ArrayList<String> params){
        //Just check if the state is LOADING RESULT when the request is received
        //Open browser, browser active change, browser results change-> state transition
        if(currentState == State.LOADING_RESULTS){
            pageChange += Integer.parseInt(params.get(0));
        }

        changeResultsPage();
    }

    private void handleResultsCanScrollForwardChanged(boolean val){
        Command.ArrowVisibilityChanged(model, "Results", false, val);
    }

    private void handleResultsCanScrollBackwardChanged(boolean val){
        Command.ArrowVisibilityChanged(model, "Results", true, val);
    }

    /**
     * Load the device at the given index OR try to find and load the device with the given name
     *
     * @param params [0] The index of the device to load
     * @param params [1] The name of the device to find and load
     */
    public void loadDevice(ArrayList<String> params){
        int deviceIndex = Integer.parseInt(params.get(0));

        if(deviceIndex != -1){
            selectionIndex = deviceIndex + browserProxy.getResultsScrollPosition() + scrollOffset;
            loadDevice();
        }else if(params.size() > 1){
            deviceToFind = params.get(1);
        }
    }


    /**
     * Close the browser
     * @param params
     */
    public void closeBrowser(ArrayList<String> params){
        currentState = currentState.next(this, Event.CLOSE_REQUEST);
    }


    /**
     * Callback for changes in filter column entries. Sends new entries to client.
     * @param i the index of the column whose entries changed
     */
    private void handleFilterEntriesChanged(int i){
        if(!browserProxy.isActive()){
            return;
        }

        if(!browserProxy.getFilterColumn(i).getName().equals("Tag") &&
                !browserProxy.getFilterColumn(i).getName().equals("Creator") &&
                !browserProxy.getFilterColumn(i).getName().equals("Category")){
            return;
        }

        ArrayList<String> columnEntries = new ArrayList<>();
        for(BrowserColumnItemData entry : browserProxy.getFilterColumn(i).getItems()){
            if(entry.getName().length() > 0) {
                columnEntries.add(entry.getName());
            }
        }

        if(columnEntries.size() > 0) {
            Command.BrowserColumnChanged(model,
                    browserProxy.getFilterColumn(i).getName(),
                    browserProxy.getNumFilterColumnEntries(),
                    browserProxy.getFilterColumn(i).getTotalEntries(),
                    columnEntries);
        }
    }


    private void handleFilterCanScrollForwardChanged(int i, boolean val){
        Command.ArrowVisibilityChanged(model, browserProxy.getFilterColumnNames()[i], false, val);
    }

    private void handleFilterCanScrollBackwardChanged(int i, boolean val){
        Command.ArrowVisibilityChanged(model, browserProxy.getFilterColumnNames()[i], true, val);
    }



    /**
     * Selects an entry on a filter column
     *
     * @param params [0] the name of the filter column
     * @param params [1] the index of the entry to select
     */
    public void selectFilterEntry(ArrayList<String> params){
        if(!browserProxy.isActive()){
            return;
        }

        int i = browserProxy.getFilterColumnIndex(params.get(0), model);

        if(i == -1){
            return;
        }

        int selection = Integer.parseInt(params.get(1)) + browserProxy.getFilterColumn(i).getScrollPosition();
        browserProxy.getFilterColumn(i).selectFirst();

        int currentIndex = 0;

        //Navigate to the supplied index
        while (currentIndex < selection) {
            browserProxy.getFilterColumn(i).selectNextItem();
            currentIndex++;
        }

    }


    /**
     * Changes the page on a filter column
     *
     * @param params [0] the name of the filter column
     * @param params [1] the amount to change the page by
     */
    public void changeFilterPage(ArrayList<String> params){
        if(!browserProxy.isActive()){
            return;
        }

        int i = browserProxy.getFilterColumnIndex(params.get(0), model);

        if(i == -1){
            throw new RuntimeException("Trying to change page on a column that can't be found");
        }

        int filterPageChange = Integer.parseInt(params.get(1));

        while(filterPageChange != 0) {
            if (filterPageChange > 0) {
                browserProxy.nextFilterItemPage(i);
                filterPageChange -= 1;
            } else {
                browserProxy.previousFilterItemPage(i);
                filterPageChange += 1;
            }
        }

        handleFilterEntriesChanged(i);

    }

}
