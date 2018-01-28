// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.las4vc.composevr;

import de.mossgrabers.framework.configuration.AbstractConfiguration;
import de.mossgrabers.framework.controller.ValueChanger;

import com.bitwig.extension.controller.api.Preferences;
import com.bitwig.extension.controller.api.SettableRangedValue;
import com.bitwig.extension.controller.api.SettableStringValue;


/**
 * The configuration settings for ComposeVR
 *
 * @author Lane Spangler
 */
public class ComposeVRConfiguration extends AbstractConfiguration
{
    /** ID for receive host setting. */
    public static final Integer UDP_CLIENT = Integer.valueOf (30);
    /** ID for receive port setting. */
    public static final Integer RECEIVE_PORT   = Integer.valueOf (31);
    /** ID for send port setting. */
    public static final Integer UDP_PORT = Integer.valueOf (32);

    private static final String DEFAULT_CLIENT = "192.168.0.29";

    private String              UDPClientIP    = DEFAULT_CLIENT;
    private int                 TCPPort    = 8888;
    private int                 UDPPort       = 8889;


    /**
     * Constructor.
     *
     * @param valueChanger The value changer
     */
    public ComposeVRConfiguration(final ValueChanger valueChanger)
    {
        super (valueChanger);
    }


    /** {@inheritDoc} */
    @Override
    public void init (final Preferences prefs)
    {
        ///////////////////////////
        // Network

        final SettableStringValue receiveHostSetting = prefs.getStringSetting ("UDP Client IP", "UDP Client (Script restart required)", 15, DEFAULT_CLIENT);
        receiveHostSetting.addValueObserver (value -> {
            this.UDPClientIP = value;
            this.notifyObservers (ComposeVRConfiguration.UDP_CLIENT);
        });

        final SettableRangedValue receivePortSetting = prefs.getNumberSetting ("TCP Port", "Server port (script restart required)", 0, 65535, 1, "", 8000);
        receivePortSetting.addValueObserver (65535, value -> {
            this.TCPPort = value;
            this.notifyObservers (ComposeVRConfiguration.RECEIVE_PORT);
        });


        final SettableRangedValue sendPortSetting = prefs.getNumberSetting ("UDP Port", "(script restart required)", 0, 65535, 1, "", 9000);
        sendPortSetting.addValueObserver (65535, value -> {
            this.UDPPort = value;
            this.notifyObservers (UDP_PORT);
        });

    }


    /**
     * Get the host on which the extension receives OSC messages.
     *
     * @return The receive host
     */
    public String getUDPClientIP()
    {
        return this.UDPClientIP;
    }


    /**
     * Get the port of the host on which the extension receives OSC messages.
     *
     * @return The port
     */
    public int getServerPort ()
    {
        return this.TCPPort;
    }


    /**
     * Get the port of the host on which the extension sends OSC messages.
     *
     * @return The port
     */
    public int getUDPPort ()
    {
        return this.UDPPort;
    }
}
