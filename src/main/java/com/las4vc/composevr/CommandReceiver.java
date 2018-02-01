package com.las4vc.composevr;

import java.util.ArrayList;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.bitwig.extension.controller.api.*;

/**
 * A CommandReceiver translates incoming messages into method calls using reflection
 *
 * @author Lane Spangler
 */

public class CommandReceiver {

    protected DAWModel model;
    protected RemoteConnection connection;
    private HashMap<String, Method> methodMap;

    public CommandReceiver(DAWModel model){
        this.model = model;
        methodMap = new HashMap<>();
    }


    public void executeCommand(String methodName, ArrayList<String> params){

        Method command = methodMap.get(methodName);

        if(command == null){
            try {
                //Get the method of the given name on this object
                command = this.getClass().getMethod(methodName, ArrayList.class);
                methodMap.put(methodName,command);

            }catch (SecurityException e) {

            }
            catch (NoSuchMethodException e) {

            }
        }

        try{
            //Call the method on this object with the supplied params
            command.invoke(this, params);
        }catch (IllegalArgumentException e) {

        }
        catch (IllegalAccessException e) {

        }
        catch (Exception e) {

        }

    }
}
