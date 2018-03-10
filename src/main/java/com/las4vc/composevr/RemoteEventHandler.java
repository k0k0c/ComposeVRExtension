package com.las4vc.composevr;
import com.bitwig.extension.api.opensoundcontrol.OscMessage;
import com.las4vc.composevr.protocol.Protocol;

import java.lang.reflect.Method;
import java.util.HashMap;

import com.bitwig.extension.controller.api.*;

/**
 * A RemoteEventHandler translates incoming remote events into method calls using reflection
 *
 * @author Lane Spangler
 */

public class RemoteEventHandler {

    protected DAWModel model;
    protected RemoteConnection connection;
    private HashMap<String, Method> methodMap;

    public RemoteEventHandler(DAWModel model){
        this.model = model;
        methodMap = new HashMap<>();
    }

    /**
     * Invokes the method referenced by a remote event on this event handler
     * @param event
     */
    public void handleEvent(Protocol.Event event){
        Method handlerMethod = getMethodByName(event.getMethodName());

        try{
            //Call the method on this object with the supplied params
            handlerMethod.invoke(this, event);
        }catch (IllegalArgumentException e) {
            this.model.host.println("Method invoked via reflection with illegal arguments");
            this.model.host.println(e.getMessage());
        }
        catch (IllegalAccessException e) {
            this.model.host.println(e.getMessage());
        }
        catch (Exception e) {
            this.model.host.println(e.getMessage());
        }
    }

    public void handleOSC(OscMessage msg){
        String methodName = "handleOSC";
        Method handlerMethod = getMethodByName(methodName);

        try{
            //Call the method on this object with the supplied params
            handlerMethod.invoke(this, msg);
        }catch (IllegalArgumentException e) {
            this.model.host.println("Method invoked via reflection with illegal arguments");
            this.model.host.println(e.getMessage());
        }
        catch (IllegalAccessException e) {
            this.model.host.println(e.getMessage());
        }
        catch (Exception e) {
            this.model.host.println(e.getMessage());
        }
    }

    public Method getMethodByName(String methodName){
        Method handlerMethod = methodMap.get(methodName);

        //model.host.println("Invoking "+event.getMethodName()+" on "+this.getClass().getName());

        if(handlerMethod == null){
            try {
                //Get the method of the given name on this object
                handlerMethod = this.getClass().getMethod(methodName, Protocol.Event.class);
                methodMap.put(methodName, handlerMethod);

            }catch (SecurityException e) {
                this.model.host.println(e.getMessage());
            }
            catch (NoSuchMethodException e) {
                this.model.host.println("Method can't be found on object");
                this.model.host.println(e.getMessage());
            }
        }

        return handlerMethod;
    }

}
