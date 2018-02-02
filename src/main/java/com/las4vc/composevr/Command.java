package com.las4vc.composevr;

import com.bitwig.extension.callback.BooleanValueChangedCallback;

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

    /**
     * Emitted when a sound module is created by the
     * @param model
     * @param receiver
     */
    public static void SoundModuleCreated(DAWModel model, String receiver){
        model.host.println("SoundModuleCreated");
        model.router.sendCommand(receiver,"SoundModuleCreated", new ArrayList<>());
    }

    public static void BrowserColumnChanged(DAWModel model, String columnName, int resultsPerPage, int totalResults, ArrayList<String> results){
        ArrayList<String> params = new ArrayList<>();
        params.add(Integer.toString(resultsPerPage));
        params.add(Integer.toString(totalResults));
        params.addAll(results);

        model.router.sendCommand("browser/"+columnName, "BrowserColumnChanged", params);
    }

    public static void ArrowVisibilityChanged(DAWModel model, String columnName, boolean upArrow, boolean visible){

        ArrayList<String> params = new ArrayList<>();
        params.add(Boolean.toString(upArrow));
        params.add(Boolean.toString(visible));

        model.router.sendCommand("browser/"+columnName, "ArrowVisibilityChanged", params);
    }



    public static void DeviceLoaded(DAWModel model){
        model.host.println("DeviceLoaded");
        model.router.sendCommand("browser", "DeviceLoaded", new ArrayList<String>());
    }

    public static void DeviceNotFound(DAWModel model){
        model.host.println("DeviceNotFound");
        model.router.sendCommand("browser", "DeviceNotFound", new ArrayList<String>());
    }
}
