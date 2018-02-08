package com.las4vc.composevr;

import com.las4vc.composevr.protocol.*;

/**
 * DAWController is responsible for executing higher level commands which create other receivers
 *
 * @author Lane Spangler
 */

public class DAWController extends RemoteEventHandler {


    public DAWController(DAWModel model){
        super(model);
    }

    /**
     * Creates a new sound module with the id specified
     */
    public void CreateSoundModule(Protocol.Event e){
        SoundModule newSoundModule = new SoundModule(model, e.getModuleEvent().getCreateSoundModuleEvent());
    }
}
