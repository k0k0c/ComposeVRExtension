package com.las4vc.composevr;

import java.io.IOException;
import java.lang.reflect.Array;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.bitwig.extension.controller.api.RemoteConnection;
import com.bitwig.extension.controller.api.ControllerHost;
import org.json.*;

import javax.naming.ldap.Control;

/**
 * The CommandRouter is responsible for passing incoming messages to the message's intended receiver
 *
 * @author Lane Spangler
 */

public class CommandRouter {

    private HashMap<String, CommandReceiver> receiverDictionary;

    private ControllerHost host;
    private RemoteConnection connection;

    private CommandReceiver currentDriver;
    private CommandReceiver midiReceiver;

    public CommandRouter(ControllerHost host){
        receiverDictionary = new HashMap<>();
        this.host = host;
    }

    public void setConnection(RemoteConnection connection){
        this.connection = connection;
    }

    public void routeCommand(String command){
        JSONObject commandObject = new JSONObject(command);

        String receiverID = commandObject.getString("receiverID");
        CommandReceiver receiver = receiverDictionary.get(receiverID);

        if(receiver != null){
            JSONArray jsonParams = commandObject.getJSONArray("methodParams");
            List<Object> paramList = jsonParams.toList();

            ArrayList<String> params = new ArrayList<String>();

            for(int i = 0; i < paramList.size(); i++){
                params.add(paramList.get(i).toString());
            }

            receiver.executeCommand(commandObject.getString("methodName"), params);
        }
    }

    public void addReceiver(CommandReceiver receiver, String receiverID){
        receiverDictionary.put(receiverID, receiver);
    }

    public void sendCommand(String receiverID, String methodName, ArrayList<String> params){
        JSONObject command = new JSONObject();

        command.put("receiverID",receiverID);
        command.put("methodName",methodName);
        command.put("methodParams",params);


        String commandString = command.toString();

        try {
            connection.send(commandString.getBytes(Charset.forName("UTF8")));
        }catch(IOException e){
            host.println(e.getMessage());
        }
    }

    public void setMidiReceiver(CommandReceiver receiver){
        midiReceiver = receiver;
    }

    public void clearMidiReceiever(){
        midiReceiver = null;
    }

    public void routeMidi(String midiMessage){
        if(midiReceiver != null){
            String[] midi = midiMessage.split("\\|");
            ArrayList<String> params = new ArrayList<String>();
            params.add(midi[0]);
            params.add(midi[1]);
            midiReceiver.executeCommand("PlayMidiNote",params);
        }
    }



}
