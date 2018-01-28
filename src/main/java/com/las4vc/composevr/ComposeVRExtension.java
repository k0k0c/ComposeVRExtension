// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.las4vc.composevr;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.callback.StringValueChangedCallback;
import com.bitwig.extension.controller.api.*;

import de.mossgrabers.framework.controller.DefaultValueChanger;
import de.mossgrabers.framework.daw.data.BrowserColumnItemData;
import de.mossgrabers.framework.scale.Scales;
import de.mossgrabers.framework.daw.BrowserProxy;
import de.mossgrabers.framework.daw.CursorDeviceProxy;

import java.nio.charset.Charset;


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

        model = new DAWModel(getHost(), valueChanger);

        //Initialize MIDI
        port = host.getMidiInPort(0);
        noteInput = port.createNoteInput("ComposeVR Midi");

        host.println ("Initializing ComposeVR.");
        host.println("Connecting to udp client at: "+ClientIP);

        socket = host.createRemoteConnection("ComposeVR",serverPort);
        host.println("Server listening on port: "+serverPort);
        socket.setClientConnectCallback(this::handleConnection);

        host.addDatagramPacketObserver ("ComposeVR UDP Host", serverPort, this::handleDatagram);

    }

    private void handleConnection(RemoteConnection connection){

        getHost().println("Client connected");
        model.router.setConnection(connection);

        ApplicationCommandReceiver appCommandReceiver = new ApplicationCommandReceiver(model);
        model.router.addReceiver(appCommandReceiver, "app");

        connection.setReceiveCallback(this::handleData);
        connection.setDisconnectCallback(this::handleDisconnect);

    }

    private void handleDisconnect(){
        getHost().println("Disconnected");
    }

    private void handleData(final byte[] data){
        String command = new String(data,Charset.forName("UTF8"));
        //getHost().println(command);
        model.router.routeCommand(command);
    }


    private void handleDatagram (final byte [] data)
    {
        try
        {
            final ControllerHost host = this.getHost();
            noteInput.sendRawMidiEvent(data[0]+256, data[1], data[2]);

            //router.routeMidi(message);

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
