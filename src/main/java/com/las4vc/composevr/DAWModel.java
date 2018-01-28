package com.las4vc.composevr;
import com.las4vc.composevr.events.TrackSelectionChangeEvent;

import java.util.ArrayList;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.callback.StringValueChangedCallback;
import com.bitwig.extension.controller.api.*;

import de.mossgrabers.framework.controller.DefaultValueChanger;
import de.mossgrabers.framework.daw.data.BrowserColumnItemData;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.daw.BrowserProxy;
import de.mossgrabers.framework.daw.CursorDeviceProxy;



/**
 * The DAWModel is a collection of objects that every receiver needs.
 *
 * @author Lane Spangler
 */

public class DAWModel {

    public ControllerHost host;
    public Application app;
    public CommandRouter router;
    public TrackBank mainTrackBank;

    public CursorTrack cursorTrack;
    public CursorDevice cursorDevice;
    public CursorDeviceProxy cursorDeviceProxy;

    public BrowserModel browser;

    private ArrayList<TrackSelectionChangeEvent> trackSelectionChangeListeners;


    private DefaultValueChanger valueChanger;

    public DAWModel(ControllerHost host, DefaultValueChanger valueChanger){
        this.host = host;
        this.valueChanger = valueChanger;

        trackSelectionChangeListeners = new ArrayList<>();

        app = host.createApplication();
        router = new CommandRouter(host);
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
        trackSelectionChangeListeners.remove(listener);
    }

    private void handleTrackSelectionChange(){

        for(TrackSelectionChangeEvent e : trackSelectionChangeListeners) {
            e.OnTrackSelectionChange();
        }
    }




}
