package com.las4vc.composevr.browser;

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
                for(int i = 0; i < itemIndex; i++){
                    deviceTypeColumn.selectNextItem();
                }
            }
        }
    }

    public String getTargetDeviceType(){
        int deviceTypeIndex = browserProxy.getFilterColumnIndex("Device Type", model);
        BrowserColumnData deviceTypeColumn = browserProxy.getFilterColumn(deviceTypeIndex);
        return deviceTypeColumn.getCursorName();
    }

    /**
     * Callback for changes in filter column entries. Sends new entries to client.
     * @param i the index of the column whose entries changed
     */
    public void handleFilterEntriesChanged(int i){
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
            RemoteEventEmitter.OnBrowserColumnChanged(model,
                    browserProxy.getFilterColumn(i).getName(),
                    browserProxy.getNumFilterColumnEntries(),
                    browserProxy.getFilterColumn(i).getTotalEntries(),
                    columnEntries);
        }
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

        handleFilterEntriesChanged(i);

    }
}
