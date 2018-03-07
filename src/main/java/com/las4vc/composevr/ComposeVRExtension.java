// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.las4vc.composevr;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.*;

import com.las4vc.composevr.protocol.Protocol;
import de.mossgrabers.framework.controller.DefaultValueChanger;
import de.mossgrabers.framework.scale.Scales;

import java.io.ByteArrayInputStream;

import java.io.IOException;


/**
 * Bitwig Studio extension to support ComposeVR
 *
 * @author Lane Spangler
 */
public class ComposeVRExtension extends ControllerExtension
{
    private ComposeVRConfiguration configuration;
    private DefaultValueChanger valueChanger;
    private RemoteSocket socket;

    private DAWModel model;

    private MidiIn port;
    private NoteInput noteInput;
    private int midiChannel = 1;

    /**
     * Constructor.
     *
     * @param extensionDefinition The extension definition
     * @param host The Bitwig host
     */
    protected ComposeVRExtension(final ComposeVRExtensionDefinition extensionDefinition, final ControllerHost host)
    {
        super (extensionDefinition, host);

        this.valueChanger = new DefaultValueChanger (128, 1, 0.5);
        this.configuration = new ComposeVRConfiguration(this.valueChanger);
    }


    /** {@inheritDoc} */
    @Override
    public void init ()
    {
        this.configuration.init (this.getHost ().getPreferences ());

        final Scales scales = new Scales (this.valueChanger, 0, 128, 128, 1);
        scales.setChromatic (true);

        final ControllerHost host = this.getHost ();

        final String ClientIP = this.configuration.getUDPClientIP();
        final int serverPort = this.configuration.getServerPort();
        final int udpPort = this.configuration.getUDPPort();

        model = new DAWModel(getHost(), valueChanger);


        host.println ("Initializing ComposeVR.");
        host.println("Connecting to udp client at: "+ClientIP);

        socket = host.createRemoteConnection("ComposeVR", serverPort);
        host.println("Server listening on port: "+ serverPort);
        socket.setClientConnectCallback(this::handleConnection);

        host.addDatagramPacketObserver ("ComposeVR UDP Host", udpPort, this::handleDatagram);
        host.println("Listening on UDP port "+udpPort);

    }

    private void handleConnection(RemoteConnection connection){

        getHost().println("Client connected");
        model.router.setConnection(connection);

        DAWController appCommandReceiver = new DAWController(model);
        model.router.addReceiver(appCommandReceiver, "app");

        connection.setReceiveCallback(this::handleData);
        connection.setDisconnectCallback(this::handleDisconnect);

    }

    private void handleDisconnect(){
        getHost().println("Disconnected");
    }

    private void handleData(final byte[] data){

        getHost().println("Received "+data.length+" bytes of data");

       ByteArrayInputStream messageStream = new ByteArrayInputStream(data);
        try {
            Protocol.Event msg = Protocol.Event.parseDelimitedFrom(messageStream);
            getHost().println("Incoming event: "+msg.getMethodName());
            model.router.routeEvent(msg);
        }catch(IOException e){
            getHost().println(e.getMessage());
        }

    }


    private void handleDatagram (final byte [] data)
    {
        try
        {
            ByteArrayInputStream messageStream = new ByteArrayInputStream(data);

            try{
                Protocol.Event msg = Protocol.Event.parseDelimitedFrom(messageStream);
                model.router.routeEvent(msg);
            }catch(IOException e){
                getHost().println(e.getMessage());
            }

            /*if(message.equals("Hello Bitwig")){
                String reply = new String("Hello Unity");
                host.sendDatagramPacket(this.configuration.getUDPClientIP(),this.configuration.getUDPPort(),reply.getBytes(Charset.forName("UTF-8")));
            }*/
        }
        catch (final IllegalArgumentException ex)
        {
            this.getHost ().errorln (ex.getLocalizedMessage ());
        }
    }



    /** {@inheritDoc} */
    @Override
    public void exit ()
    {
        this.getHost ().println ("Exited.");
    }


    /** {@inheritDoc} */
    @Override
    public void flush ()
    {
    }
}
