package com.las4vc.composevr.browser;

import com.bitwig.extension.callback.IntegerValueChangedCallback;
import com.bitwig.extension.callback.StringValueChangedCallback;
import com.bitwig.extension.controller.api.BrowserItemBank;

public class BrowserItemSelector {
    private BrowserItemBank<?> itemBank;
    private boolean[] itemUpdated;
    private int scrollPosition;

    public String targetItem;
    private boolean attemptingSelection;

    public BrowserItemSelector(BrowserItemBank<?> itemBank){
       this.itemBank = itemBank;
       targetItem = "";
       itemUpdated = new boolean[itemBank.getSizeOfBank()];

       setUpItemChangedCallbacks();
       setUpItemCountCallback();
    }

    public void attemptToSelect(String itemName){
        resetSelector();

        targetItem = itemName;
        attemptingSelection = true;

        itemBank.scrollPosition().set(scrollPosition);
        attemptSelectionOnCurrentPage();
    }

    public void resetSelector(){
        resetUpdateFlags();
        attemptingSelection = false;
        scrollPosition = 0;
    }

    private void setUpItemChangedCallbacks(){
       for(int i = 0; i < itemBank.getSizeOfBank(); i++){
           final int idx = i;
           StringValueChangedCallback onItemChanged = new StringValueChangedCallback() {
               @Override
               public void valueChanged(String s) {
                    handleItemChanged(s, idx);
               }
           };
           itemBank.getItemAt(i).name().addValueObserver(onItemChanged);
       }
    }

    private void setUpItemCountCallback(){
        IntegerValueChangedCallback onItemCountChanged = new IntegerValueChangedCallback() {
            @Override
            public void valueChanged(int i) {
                handleItemCountChanged(i);
            }
        };

        itemBank.itemCount().addValueObserver(onItemCountChanged);
    }


    private void handleItemChanged(String itemName, int itemIndex){
        if(itemName.equals(targetItem) && attemptingSelection){
            itemBank.getItemAt(itemIndex).isSelected().set(true);
            onSelectionAttemptFinished(true);
        }

        itemUpdated[itemIndex] = true;

        scrollBankIfReady();
    }

    private void handleItemCountChanged(int itemCount){
        scrollBankIfReady();
    }

    private void scrollBankIfReady(){
       if(allItemsUpdated() && scrollPosition < itemBank.itemCount().get()){
          scrollPosition += itemBank.getSizeOfBank();
          itemBank.scrollPosition().set(scrollPosition);
          resetUpdateFlags();
       }
    }

    private void resetUpdateFlags(){
        for(int i = 0; i < itemBank.getSizeOfBank(); i++){
            itemUpdated[i] = false;
        }
    }

    private void attemptSelectionOnCurrentPage(){
        for(int i = 0; i < itemBank.getSizeOfBank(); i++){
            if(itemBank.getItemAt(i).name().get().equals(targetItem)){
                itemBank.getItemAt(i).isSelected().set(true);
                onSelectionAttemptFinished(true);
                break;
            }
        }
    }

    private void onSelectionAttemptFinished(boolean success){
        attemptingSelection = false;
        targetItem = "";
        resetSelector();
    }

    public boolean isSelecting(){
        return attemptingSelection;
    }

    private boolean allItemsUpdated(){
        boolean allItemsUpdated = true;
        for(int i = 0; i < itemUpdated.length; i++){
            if(!itemUpdated[i]){
                allItemsUpdated = false;
                break;
            }
        }
        return allItemsUpdated;
    }

}
