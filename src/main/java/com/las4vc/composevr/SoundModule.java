package com.las4vc.composevr;

import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.callback.StringValueChangedCallback;
import com.bitwig.extension.controller.api.*;
import com.las4vc.composevr.requests.BrowserRequest;
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
    }

    public void requestBrowser(ArrayList<String> params){
        BrowserRequest request = new BrowserRequest(params, track, id);
        model.browser.requestBrowser(request);
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
