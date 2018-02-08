package com.las4vc.composevr;

import com.las4vc.composevr.protocol.Protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import com.bitwig.extension.controller.api.RemoteConnection;
import com.bitwig.extension.controller.api.ControllerHost;

/**
 * The RemoteEventRouter is responsible for passing incoming messages to the message's intended receiver
 *
 * @author Lane Spangler
 */

public class RemoteEventRouter {

    private HashMap<String, RemoteEventHandler> handlerDictionary;

    private ControllerHost host;
    private RemoteConnection connection;

    private RemoteEventHandler currentDriver;
    private RemoteEventHandler midiReceiver;

    private ByteArrayOutputStream outputStream;

    public RemoteEventRouter(ControllerHost host){
        handlerDictionary = new HashMap<>();
        outputStream = new ByteArrayOutputStream();
        this.host = host;
    }

    public void setConnection(RemoteConnection connection){
        this.connection = connection;
    }

    public void addReceiver(RemoteEventHandler receiver, String receiverID){
        handlerDictionary.put(receiverID, receiver);
    }

    public void routeEvent(Protocol.Event msg){
        String handlerID = "";

        if(msg.hasModuleEvent()){
            host.println("Handling module event");
            handlerID = msg.getModuleEvent().getHandlerId();
        }else if(msg.hasBrowserEvent()){
            handlerID = "browser";
            handlerID += msg.getBrowserEvent().getPath();
        }else{
            host.println("Unrecognized message type");
            return;
        }

        RemoteEventHandler receiver = handlerDictionary.get(handlerID);

        if(receiver != null){
            receiver.handleEvent(msg);
        }
    }

    public void emitEvent(Protocol.Event msg){
        if(connection == null){
            return;
        }

        try {
            host.println("Emitting event "+msg.getMethodName());
            msg.writeDelimitedTo(outputStream);

            connection.send(outputStream.toByteArray());

            outputStream.reset();

        }catch(IOException e){
            host.println(e.getMessage());
        }
   }

    public void setMidiReceiver(RemoteEventHandler receiver){
        midiReceiver = receiver;
    }

    public void clearMidiReceiever(){
        midiReceiver = null;
    }


}
