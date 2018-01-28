package com.las4vc.composevr.requests;
import java.lang.String;
import java.util.ArrayList;

import com.bitwig.extension.controller.api.*;

public class BrowserRequest {

    private Track track;
    private int selectionIndex;
    private short pageChange;
    private boolean cancel;
    private String deviceName;
    private String senderID;

    public BrowserRequest(ArrayList<String> params, Track track, String id){
        this.track = track;
        this.senderID = id;

        this.selectionIndex = Integer.parseInt(params.get(0));
        this.pageChange = Short.parseShort(params.get(1));
        this.cancel = params.get(2).equals("true");
        this.deviceName = params.get(3);

    }

    public Track getTrack(){
        return track;
    }

    public int getSelectionIndex(){
        return this.selectionIndex;
    }

    public void setSelectionIndex(int index){
        this.selectionIndex = index;
    }

    public void setPageChange(short change){
        this.pageChange = change;
    }

    public short getPageChange(){
        return this.pageChange;
    }

    public boolean isCancel(){
        return this.cancel;
    }

    public String getDeviceName(){
        return this.deviceName;
    }

    public void setDeviceName(String name){
        this.deviceName = name;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }
}
