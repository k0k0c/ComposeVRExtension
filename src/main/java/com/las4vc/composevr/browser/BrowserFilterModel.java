package com.las4vc.composevr.browser;

import com.bitwig.extension.callback.IntegerValueChangedCallback;
import com.bitwig.extension.callback.StringValueChangedCallback;
import com.google.protobuf.StringValue;
import com.las4vc.composevr.DAWModel;
import com.las4vc.composevr.RemoteEventHandler;
import com.las4vc.composevr.RemoteEventEmitter;

import java.util.ArrayList;


import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.las4vc.composevr.protocol.Browser;
import com.las4vc.composevr.protocol.Protocol;
import de.mossgrabers.framework.daw.BrowserProxy;
import de.mossgrabers.framework.daw.data.BrowserColumnItemData;
import de.mossgrabers.framework.daw.data.BrowserColumnData;

public class BrowserFilterModel extends RemoteEventHandler {
    public BrowserProxy browserProxy;

    private DAWModel model;
    public String targetDeviceType;

    public BrowserFilterModel(DAWModel model, BrowserProxy browserProxy){
        super(model);
        model.router.addReceiver(this, "browser/filter");

        this.browserProxy = browserProxy;
        this.model = model;
        targetDeviceType = "Any Device";

        for(int i = 0; i < browserProxy.getFilterColumnCount(); i++){
            final int columnIndex = i;

            setUpEntryChangedCallbacks(i);
            setUpNumEntriesChangedCallback(i);
            setUpCanScrollCallbacks(i);
        }
    }

    /**
     * Set device type filter based on BrowserModel targetDeviceType
     */
    public void setDeviceType(){

        int deviceTypeIndex = browserProxy.getFilterColumnIndex("Device Type", model);

        if(deviceTypeIndex != -1) {
            BrowserColumnData deviceTypeColumn = browserProxy.getFilterColumn(deviceTypeIndex);
            deviceTypeColumn.selectFirst();

            int itemIndex = -1;

            for (int i = 0; i < deviceTypeColumn.getItems().length; i++) {

                if (deviceTypeColumn.getItems()[i].getName().equals(targetDeviceType)) {
                    itemIndex = i;
                    break;
                }
            }

            if(itemIndex != -1){
                deviceTypeColumn.getItemAt(itemIndex).isSelected().set(true);
            }
        }
    }

    public String getTargetDeviceType(){
        int deviceTypeIndex = browserProxy.getFilterColumnIndex("Device Type", model);
        BrowserColumnData deviceTypeColumn = browserProxy.getFilterColumn(deviceTypeIndex);
        return deviceTypeColumn.getCursorName();
    }


    private void setUpNumEntriesChangedCallback(final int columnIndex){

        //Num entries callback
        IntegerValueChangedCallback numFilterEntriesChangedCallback = new IntegerValueChangedCallback() {
            @Override
            public void valueChanged(int i) {
                handleNumFilterEntriesChanged(columnIndex, i);
            }
        };

        browserProxy.getFilterColumn(columnIndex).getEntryCount().addValueObserver(numFilterEntriesChangedCallback);
    }

    /**
     * Callback for changes in the number of filter entries
     * @param columnIndex
     * @param numEntries
     */
    public void handleNumFilterEntriesChanged(int columnIndex, int numEntries){
        RemoteEventEmitter.OnBrowserColumnChanged(model,
                browserProxy.getFilterColumn(columnIndex).getName(),
                browserProxy.getNumResultsPerPage(),
                numEntries);
    }


    private void setUpEntryChangedCallbacks(final int columnIndex){

        //Entry changed callbacks
        BrowserColumnItemData[] filterEntries = browserProxy.getFilterColumn(columnIndex).getItems();

        for(int j = 0; j < filterEntries.length; j++){
            final int entryIndex = j;
            StringValueChangedCallback entryChangedCallback = new StringValueChangedCallback() {
                @Override
                public void valueChanged(String s) {
                    handleFilterEntryChanged(columnIndex, entryIndex, s);
                }
            };

            filterEntries[j].addNameObserver(entryChangedCallback);
        }
    }

    /**
     * Callback for changes in filter column entries. Sends new entries to client.
     * @param columnIndex the index of the column whose entries changed
     * @param entryIndex the index of the entry that changed
     * @param entryName the new name of the entry
     */
    public void handleFilterEntryChanged(int columnIndex, int entryIndex, String entryName){
        if(!browserProxy.isActive()){
            return;
        }

        if(!browserProxy.getFilterColumn(columnIndex).getName().equals("Tag") &&
                !browserProxy.getFilterColumn(columnIndex).getName().equals("Creator") &&
                !browserProxy.getFilterColumn(columnIndex).getName().equals("Category")){
            return;
        }

        RemoteEventEmitter.OnBrowserItemChanged(model,
                browserProxy.getFilterColumn(columnIndex).getName(),
                entryIndex,
                entryName);

    }


    private void setUpCanScrollCallbacks(final int columnIndex){

        BooleanValueChangedCallback filterCanScrollForwardChanged = new BooleanValueChangedCallback() {
            @Override
            public void valueChanged(boolean b) {
                handleFilterCanScrollForwardChanged(columnIndex, b);
            }
        };
        browserProxy.getFilterColumn(columnIndex).hasNextFilterPage().addValueObserver(filterCanScrollForwardChanged);

        BooleanValueChangedCallback filterCanScrollBackwardChanged = new BooleanValueChangedCallback() {
            @Override
            public void valueChanged(boolean b) {
                handleFilterCanScrollBackwardChanged(columnIndex, b);
            }
        };
        browserProxy.getFilterColumn(columnIndex).hasPreviousFilterPage().addValueObserver(filterCanScrollBackwardChanged);
    }

    private void handleFilterCanScrollForwardChanged(int i, boolean val){
        RemoteEventEmitter.OnArrowVisibilityChanged(model, browserProxy.getFilterColumnNames()[i], Browser.OnArrowVisibilityChanged.Arrow.DOWN, val);
    }

    private void handleFilterCanScrollBackwardChanged(int i, boolean val){
        RemoteEventEmitter.OnArrowVisibilityChanged(model, browserProxy.getFilterColumnNames()[i], Browser.OnArrowVisibilityChanged.Arrow.UP, val);
    }

    /**
     * Selects an entry on a filter column
     *
     */
    public void SelectFilterItem(Protocol.Event e){
        Browser.SelectFilterItem params = e.getBrowserEvent().getSelectFilterItemEvent();

        if(!browserProxy.isActive()){
            return;
        }

        int i = browserProxy.getFilterColumnIndex(params.getColumnName(), model);

        if(i == -1){
            return;
        }

        int selection = params.getItemIndex() + browserProxy.getFilterColumn(i).getScrollPosition();

        //Todo: test with multi-page filters to ensure that selection works properly
        browserProxy.getFilterColumn(i).getItemAt(selection).isSelected().set(true);

    }


    /**
     * Changes the page on a filter column
     *
     */
    public void ChangeFilterPage(Protocol.Event e){
        Browser.ChangeFilterPage params = e.getBrowserEvent().getChangeFilterPageEvent();

        if(!browserProxy.isActive()){
            return;
        }

        int i = browserProxy.getFilterColumnIndex(params.getColumnName(), model);

        if(i == -1){
            throw new RuntimeException("Trying to change page on a column that can't be found");
        }

        int filterPageChange = params.getPageChange();

        while(filterPageChange != 0) {
            if (filterPageChange > 0) {
                browserProxy.nextFilterItemPage(i);
                filterPageChange -= 1;
            } else {
                browserProxy.previousFilterItemPage(i);
                filterPageChange += 1;
            }
        }

    }
}
