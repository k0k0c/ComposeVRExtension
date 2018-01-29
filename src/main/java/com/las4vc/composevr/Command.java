package com.las4vc.composevr;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * A collection of commands which can be sent from Bitwig to the Unity VR app
 *
 * Created to allow command names and parameters to change easily
 *
 * @author Lane Spangler
 */
public class Command {

    public static void TrackCreated(DAWModel model, String receiver, String newTrackID){
        ArrayList<String> params = new ArrayList<>();
        params.add(newTrackID);

        model.host.println("TrackCreated");
        model.router.sendCommand(receiver,"TrackCreated", params);
    }

    public static void BrowserResultsChanged(DAWModel model, String receiver, int scrollPosition, ArrayList<String> results){
        ArrayList<String> params = new ArrayList<>();
        params.add(Integer.toString(scrollPosition));
        params.addAll(results);

        model.router.sendCommand(receiver, "BrowserResultsChanged", params);
    }

    public static void DeviceLoaded(DAWModel model, String receiver){
        model.host.println("DeviceLoaded");
        model.router.sendCommand(receiver, "DeviceLoaded", new ArrayList<String>());
    }

    public static void DeviceNotFound(DAWModel model, String receiver){
        model.host.println("DeviceNotFound");
        model.router.sendCommand(receiver, "DeviceNotFound", new ArrayList<String>());
    }
}
