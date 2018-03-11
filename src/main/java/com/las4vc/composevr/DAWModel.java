package com.las4vc.composevr;
import com.las4vc.composevr.browser.BrowserModel;
import com.las4vc.composevr.browser.TrackSelectionChangeEvent;

import java.util.ArrayList;

import com.bitwig.extension.callback.StringValueChangedCallback;
import com.bitwig.extension.controller.api.*;

import de.mossgrabers.framework.controller.DefaultValueChanger;
import de.mossgrabers.framework.daw.CursorDeviceProxy;

class TrackReference{
    public CursorTrack cursorTrack;
    public boolean used = false;
}

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

    public CursorTrack mainCursorTrack;

    public ArrayList<TrackReference> tracks;
    public final int numCursorTracks = 64;

    public CursorDevice cursorDevice;
    public CursorDeviceProxy cursorDeviceProxy;

    public BrowserModel browser;
    public MidiIn midiInput;

    private ArrayList<TrackSelectionChangeEvent> trackSelectionChangeListeners;
    private ArrayList<TrackSelectionChangeEvent> toRemove;


    private DefaultValueChanger valueChanger;
    private int numTracks;

    public DAWModel(ControllerHost host, DefaultValueChanger valueChanger) {
        this.host = host;
        this.valueChanger = valueChanger;

        trackSelectionChangeListeners = new ArrayList<>();
        toRemove = new ArrayList<>();

        app = host.createApplication();
        router = new RemoteEventRouter(host);
        mainTrackBank = host.createMainTrackBank(64, 8, 64);
        mainTrackBank.scrollToChannel(0);

            mainCursorTrack = host.createCursorTrack("ComposeVRPrimaryTrackCursor", "Primary", 1, 1, false);

        StringValueChangedCallback trackSelectionChangedCallback = new StringValueChangedCallback() {
            @Override
            public void valueChanged(String s) {
                handleTrackSelectionChange();
            }
        };
            mainCursorTrack.name().addValueObserver(trackSelectionChangedCallback);

        cursorDevice =
            mainCursorTrack.createCursorDevice("ComposeVRPrimaryDeviceCursor", "Primary", 1, CursorDeviceFollowMode.FIRST_DEVICE);
        cursorDeviceProxy = new CursorDeviceProxy(host, cursorDevice, valueChanger, 1, 1, 1, 1, 1);

        browser = new BrowserModel(this);

        //Initialize MIDI
        midiInput = host.getMidiInPort(0);

        //Create cursor tracks to store references to tracks
        tracks = new ArrayList<TrackReference>();
        for(int i = 0; i < numCursorTracks; i++){
            TrackReference ref = new TrackReference();
            ref.used = false;
            ref.cursorTrack = host.createCursorTrack(0, 0);
            ref.cursorTrack.isPinned().markInterested();
            tracks.add(ref);
        }

    }


    /**
     * Notifies all listeners that the track selection has changed
     */
    private void handleTrackSelectionChange() {

        for (TrackSelectionChangeEvent e : trackSelectionChangeListeners) {
            e.onTrackSelectionChanged();
        }

        for (TrackSelectionChangeEvent e : toRemove) {
            trackSelectionChangeListeners.remove(e);
        }

        toRemove.clear();
    }


    public void addTrackSelectionChangeListener(TrackSelectionChangeEvent listener) {
        trackSelectionChangeListeners.add(listener);
    }

    public void removeTrackSelectionChangeListener(TrackSelectionChangeEvent listener) {
        toRemove.add(listener);
    }

    /**
     * Create a new instrument track and return its position
     *
     * @return
     */
    public int createNewInstrumentTrack() {
        app.createInstrumentTrack(-1)
        ;
        int trackPosition = numTracks;
        numTracks += 1;

        return trackPosition;
    }

    public TrackReference getTrackReference(){
        for(int i = 0; i < numCursorTracks; i++){
            if(!tracks.get(i).used){
                host.println("Getting track ref "+i);
                return tracks.get(i);
            }
        }
        return null;
    }

}
