package com.las4vc.composevr;

import com.bitwig.extension.controller.api.*;
import de.mossgrabers.framework.daw.BrowserProxy;

import java.rmi.Remote;
import java.util.ArrayList;

import java.lang.reflect.Array;

/**
 * ApplicationCommandReceiver is responsible for executing higher level commands which create other receivers
 *
 * @author Lane Spangler
 */

public class ApplicationCommandReceiver extends CommandReceiver{


    public ApplicationCommandReceiver(DAWModel model){
        super(model);
    }

    public void createSoundModule(ArrayList<String> params){
        String senderID = params.get(0);

        //Create a track at the beginning of the root level list of tracks
        model.app.createInstrumentTrack(0);

        //Scroll to the first track
        model.mainTrackBank.scrollToChannel(0);

        //Get the track
        Track newTrack = model.mainTrackBank.getChannel(0);

        //Create a receiver id based on the sender that created the track
        String newTrackID = senderID+"/sound_module";

        if(newTrack != null) {
            newTrack.setName(newTrackID);
        }

        SoundModule newSoundModule = new SoundModule(model, senderID);

        //Add the receiver to the router
        model.router.addReceiver(newSoundModule, newTrackID);

        Command.TrackCreated(model, senderID, newTrackID);
    }
}
