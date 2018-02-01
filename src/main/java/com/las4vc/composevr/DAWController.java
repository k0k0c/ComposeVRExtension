package com.las4vc.composevr;

import java.util.ArrayList;

/**
 * DAWController is responsible for executing higher level commands which create other receivers
 *
 * @author Lane Spangler
 */

public class DAWController extends CommandReceiver{


    public DAWController(DAWModel model){
        super(model);
    }

    /**
     * Creates a new sound module with the id specified
     * @param params
     */
    public void createSoundModule(ArrayList<String> params){
        String senderID = params.get(0);
        SoundModule newSoundModule = new SoundModule(model, senderID);
    }
}
