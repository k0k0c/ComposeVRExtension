package com.las4vc.composevr;

import com.bitwig.extension.api.opensoundcontrol.OscMessage;
import com.bitwig.extension.callback.IntegerValueChangedCallback;
import com.bitwig.extension.controller.api.*;
import com.las4vc.composevr.protocol.*;
import com.google.protobuf.ByteString;
import com.sun.org.apache.bcel.internal.generic.VariableLengthInstruction;
import com.sun.org.apache.xpath.internal.operations.Mod;
import java.util.HashMap;

/**
 * A SoundModule contains a reference to a track, an instrument, and a number of device modules
 *
 * @author Lane Spangler
 */

public class SoundModule extends RemoteEventHandler {

    private Track track;
    private CursorDevice cursorDevice;
    private String id;
    private int trackPosition;
    private HashMap<Integer, Integer> playingNotes;

    public SoundModule(DAWModel model, Module.CreateSoundModule creationEvent){
        super(model);
        this.id = creationEvent.getSenderId();


        int trackPosition = model.createNewInstrumentTrack();

        //Get the track
        this.track = model.mainTrackBank.getChannel(trackPosition);

        //Name the track
        try{
            this.track.setName(id);

            model.router.addReceiver(this, id);

            //Confirm sound module creation with client
            RemoteEventEmitter.OnSoundModuleCreated(model, id);
            model.host.println("Module created with id "+id);
        }catch(NullPointerException e){
            model.host.println("Sound module could not create new track");
        }

        playingNotes = new HashMap<>();
    }

    /**
     * Requests for browser to open on the track for this sound module
     */
    public void OpenBrowser(Protocol.Event e){
        model.host.println("Opening browser");
        model.browser.openBrowser(this.track, e.getModuleEvent().getOpenBrowserEvent().getDeviceType());
        this.track.arm().set(true);
    }

    public void handleOSC(OscMessage msg){
        String[] splitPath = msg.getAddressPattern().split("/");

        int MIDI = msg.getInt(0);
        int Note = MIDI & 0xFF;
        int Velocity = MIDI >> 16;


        if(splitPath[splitPath.length - 1].equals("on")){
            //If the note is already playing, stop it before restarting
            if(playingNotes.containsKey(Note)){
                track.stopNote(Note, Velocity);
                playingNotes.put(Note, playingNotes.get(Note).intValue() + 1);
            }else{
                playingNotes.put(Note, 1);
            }

            track.startNote(Note, Velocity);
        }else{
            if(playingNotes.containsKey(Note)) {
                if (playingNotes.get(Note) <= 1) {
                    track.stopNote(Note, Velocity);
                    playingNotes.remove(Note);
                } else {
                    playingNotes.put(Note, playingNotes.get(Note).intValue() - 1);
                }
            }
        }
    }

}
