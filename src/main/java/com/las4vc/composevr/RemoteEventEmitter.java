package com.las4vc.composevr;

import java.util.ArrayList;

import com.las4vc.composevr.protocol.Browser.*;
import com.las4vc.composevr.protocol.Protocol.*;
import com.las4vc.composevr.protocol.Module.*;
/**
 * This class acts as a wrapper around protocol buffers to provide a concise way to emit remote events
 *
 * @author Lane Spangler
 */
public class RemoteEventEmitter {

    /**
     * Emitted when a sound module is created
     * @param model
     * @param receiver
     */
    public static void OnSoundModuleCreated(DAWModel model, String receiver){

        OnSoundModuleCreated.Builder event = OnSoundModuleCreated.newBuilder();

        ModuleEvent.Builder moduleEvent = ModuleEvent.newBuilder();
        moduleEvent.setHandlerId(receiver);
        moduleEvent.setOnSoundModuleCreatedEvent(event.build());

        Event.Builder remoteEvent = Event.newBuilder();
        remoteEvent.setModuleEvent(moduleEvent);
        remoteEvent.setMethodName("OnSoundModuleCreated");

        model.router.emitEvent(remoteEvent.build());
    }


    /**
     * Emitted when any entries change in a browser column
     * @param model
     * @param columnName
     * @param resultsPerPage
     * @param totalResults
     * @param results
     */
    public static void OnBrowserColumnChanged(DAWModel model, String columnName, int resultsPerPage, int totalResults, ArrayList<String> results){
        OnBrowserColumnChanged.Builder event = OnBrowserColumnChanged.newBuilder();
        event.setResultsPerPage(resultsPerPage);
        event.setTotalResults(totalResults);
        event.addAllResults(results);

        BrowserEvent.Builder browserEvent = BrowserEvent.newBuilder();
        browserEvent.setPath("/"+columnName);
        browserEvent.setOnBrowserColumnChangedEvent(event.build());

        Event.Builder remoteEvent = Event.newBuilder();
        remoteEvent.setBrowserEvent(browserEvent.build());
        remoteEvent.setMethodName("OnBrowserColumnChanged");

        model.router.emitEvent(remoteEvent.build());
    }

    /**
     * Emitted when the scrollability of a browser column changes.
     * Example: if a column cannot scroll up, then its up arrow should not be visible.
     *
     * @param model
     * @param columnName
     * @param upArrow Is the up arrow the arrow whose visibility changed?
     * @param visible
     */
    public static void OnArrowVisibilityChanged(DAWModel model, String columnName, boolean upArrow, boolean visible){
        OnArrowVisibilityChanged.Builder event = OnArrowVisibilityChanged.newBuilder();
        event.setUpArrow(upArrow);
        event.setVisible(visible);

        BrowserEvent.Builder browserEvent = BrowserEvent.newBuilder();
        browserEvent.setPath("/"+columnName);
        browserEvent.setOnArrowVisibilityChangedEvent(event.build());

        Event.Builder remoteEvent = Event.newBuilder();
        remoteEvent.setBrowserEvent(browserEvent.build());
        remoteEvent.setMethodName("OnArrowVisibilityChanged");

        model.router.emitEvent(remoteEvent.build());
    }

    /**
     * Emitted when a device is loaded by the browser
     * @param model
     */
    public static void OnDeviceLoaded(DAWModel model){
        OnDeviceLoaded.Builder event = OnDeviceLoaded.newBuilder();

        BrowserEvent.Builder browserEvent = BrowserEvent.newBuilder();
        browserEvent.setOnDeviceLoadedEvent(event.build());

        Event.Builder remoteEvent = Event.newBuilder();
        remoteEvent.setBrowserEvent(browserEvent);
        remoteEvent.setMethodName("OnDeviceLoaded");

        model.router.emitEvent(remoteEvent.build());
    }

    /**
     * Emitted when a device is requested by name but it cannot be found
     * @param model
     */
    public static void OnDeviceNotFound(DAWModel model){
        OnDeviceNotFound.Builder event = OnDeviceNotFound.newBuilder();

        BrowserEvent.Builder browserEvent = BrowserEvent.newBuilder();
        browserEvent.setOnDeviceNotFoundEvent(event.build());

        Event.Builder remoteEvent = Event.newBuilder();
        remoteEvent.setBrowserEvent(browserEvent);
        remoteEvent.setMethodName("OnDeviceNotFound");

        model.router.emitEvent(remoteEvent.build());
    }
}
