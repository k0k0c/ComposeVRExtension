package com.las4vc.composevr;

import com.bitwig.extension.api.opensoundcontrol.OscMessage;
import com.bitwig.extension.callback.IntegerValueChangedCallback;
import com.bitwig.extension.controller.api.*;
import com.las4vc.composevr.protocol.*;
import com.google.protobuf.ByteString;
import com.sun.org.apache.bcel.internal.generic.VariableLengthInstruction;
import com.sun.org.apache.xpath.internal.operations.Mod;
import java.util.HashSet;

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
    private HashSet<Integer> playingNotes;

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

        playingNotes = new HashSet<>();
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

        if(splitPath.length > 2){
            String localDest = splitPath[2];

            if(localDest.equals("note")){
                handleNoteMessage(msg, splitPath[3]);
            }else if(localDest.equals("trackParam")){
                handleTrackParamChange(msg, splitPath[3]);
            }
        }

    }

    private void handleNoteMessage(OscMessage msg, String type){
        int MIDI = msg.getInt(0);
        int Note = MIDI & 0xFF;
        int Velocity = MIDI >> 16;

        if(type.equals("on")){
            //If the note is already playing, stop it before restarting
            if(playingNotes.contains(Note)){
                track.stopNote(Note, Velocity);
            }else{
                playingNotes.add(Note);
            }

            track.startNote(Note, Velocity);
        }else if(type.equals("off")){
            track.stopNote(Note, Velocity);
            if(playingNotes.contains(Note)) {
                playingNotes.remove(Note);
            }
        }
    }

    private void handleTrackParamChange(OscMessage msg, String paramName){
       if(paramName.equals("volume")){
          track.volume().set(msg.getFloat(0));
       }
    }

}
