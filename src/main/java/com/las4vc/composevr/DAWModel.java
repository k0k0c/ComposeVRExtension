package com.las4vc.composevr;
import com.las4vc.composevr.browser.BrowserModel;
import com.las4vc.composevr.browser.TrackSelectionChangeEvent;

import java.util.ArrayList;

import com.bitwig.extension.callback.StringValueChangedCallback;
import com.bitwig.extension.controller.api.*;

import de.mossgrabers.framework.controller.DefaultValueChanger;
import de.mossgrabers.framework.daw.CursorDeviceProxy;



/**
 * The DAWModel is a collection of objects that can be used to manipulate the DAW, access its information, and emit remote events.
 *
 * @author Lane Spangler
 */

public class DAWModel {

    public ControllerHost host;
    public Application app;
    public RemoteEventRouter router;
    public TrackBank mainTrackBank;

    public CursorTrack cursorTrack;
    public CursorDevice cursorDevice;
    public CursorDeviceProxy cursorDeviceProxy;

    public BrowserModel browser;

    private ArrayList<TrackSelectionChangeEvent> trackSelectionChangeListeners;
    private ArrayList<TrackSelectionChangeEvent> toRemove;


    private DefaultValueChanger valueChanger;

    public DAWModel(ControllerHost host, DefaultValueChanger valueChanger){
        this.host = host;
        this.valueChanger = valueChanger;

        trackSelectionChangeListeners = new ArrayList<>();
        toRemove = new ArrayList<>();

        app = host.createApplication();
        router = new RemoteEventRouter(host);
        mainTrackBank = host.createMainTrackBank(8,8,8);

        cursorTrack = host.createCursorTrack("ComposeVRPrimaryTrackCursor","Primary",1,1,false);

        StringValueChangedCallback trackSelectionChangedCallback = new StringValueChangedCallback() {
            @Override
            public void valueChanged(String s) {
                handleTrackSelectionChange();
            }
        };
        cursorTrack.name().addValueObserver(trackSelectionChangedCallback);

        cursorDevice = cursorTrack.createCursorDevice("ComposeVRPrimaryDeviceCursor","Primary",1,CursorDeviceFollowMode.FIRST_DEVICE);
        cursorDeviceProxy = new CursorDeviceProxy(host, cursorDevice, valueChanger, 1, 1, 1,1,1);

        browser = new BrowserModel(this);

    }


    public void addTrackSelectionChangeListener(TrackSelectionChangeEvent listener){
        trackSelectionChangeListeners.add(listener);
    }

    public void removeTrackSelectionChangeListener(TrackSelectionChangeEvent listener){
        toRemove.add(listener);
    }

    /**
     * Notifies all listeners that the track selection has changed
     */
    private void handleTrackSelectionChange(){

        for(TrackSelectionChangeEvent e : trackSelectionChangeListeners) {
            e.onTrackSelectionChanged();
        }

        for(TrackSelectionChangeEvent e : toRemove){
            trackSelectionChangeListeners.remove(e);
        }

        toRemove.clear();
    }




}
