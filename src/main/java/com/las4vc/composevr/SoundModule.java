package com.las4vc.composevr;

import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.callback.StringValueChangedCallback;
import com.bitwig.extension.controller.api.*;
import de.mossgrabers.framework.daw.BrowserProxy;
import de.mossgrabers.framework.daw.data.BrowserColumnItemData;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * A SoundModule contains a reference to a track, an instrument, and a number of device modules
 *
 * @author Lane Spangler
 */

public class SoundModule extends CommandReceiver{

    private Track track;
    private String id;

    public SoundModule(DAWModel model, String id){
        super(model);
        this.id = id;

        //Create a track at the beginning of the root level list of tracks
        model.app.createInstrumentTrack(0);

        //Scroll to the first track
        model.mainTrackBank.scrollToChannel(0);

        //Get the track
        this.track = model.mainTrackBank.getChannel(0);

        //Name the track
        try{
            this.track.setName(id);

            model.router.addReceiver(this, id);

            //Confirm sound module creation with client
            Command.SoundModuleCreated(model, id);

        }catch(NullPointerException e){
            model.host.println("Sound module could not create new track");
        }
    }

    /**
     * Requests for browser to open on the track for this sound module
     * @param params
     */
    public void openBrowser(ArrayList<String> params){
        model.host.println("Opening browser");
        model.browser.openBrowser(this.track, params.get(0));
    }


    public void PlayMidiNote(ArrayList<String> params){
        int note = Integer.parseInt(params.get(0));
        int velocity = Integer.parseInt(params.get(1));
        this.track.playNote(note, velocity);
    }

    public void OnAreaEntered(ArrayList<String> params){
        model.router.setMidiReceiver(this);
        model.host.println("SoundModule area entered");
        track.getArm().set(true);
    }

    public void OnAreaExited(ArrayList<String> params){
        model.router.clearMidiReceiever();
        model.host.println("SoundModule area left");
        track.getArm().set(false);
    }


}
