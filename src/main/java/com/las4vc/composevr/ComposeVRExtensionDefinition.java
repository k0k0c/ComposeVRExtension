// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package com.las4vc.composevr;

import de.mossgrabers.framework.controller.AbstractControllerExtensionDefinition;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.ControllerHost;

import java.util.UUID;


/**
 * Definition class for the ComposeVR extension
 *
 * @author Lane Spangler
 */
public class ComposeVRExtensionDefinition extends AbstractControllerExtensionDefinition
{
    private static final UUID EXTENSION_ID = UUID.fromString ("E4B6C7E0-AC4E-11E7-8F1A-0800200C9A66");


    /** {@inheritDoc} */
    @Override
    public String getName ()
    {
        return "ComposeVR";
    }

    @Override
    public String getAuthor ()
    {
        return "Lane Spangler";
    }


    /** {@inheritDoc} */
    @Override
    public String getHardwareVendor ()
    {
        return "ComposeVR";
    }


    /** {@inheritDoc} */
    @Override
    public String getHardwareModel ()
    {
        return "ComposeVR";
    }


    /** {@inheritDoc} */
    @Override
    public String getVersion ()
    {
        return "1.0";
    }


    /** {@inheritDoc} */
    @Override
    public UUID getId ()
    {
        return EXTENSION_ID;
    }


    /** {@inheritDoc} */
    @Override
    public int getNumMidiOutPorts ()
    {
        return 0;
    }


    /** {@inheritDoc} */
    @Override
    public ControllerExtension createInstance (final ControllerHost host)
    {
        return new ComposeVRExtension(this, host);
    }


    /** {@inheritDoc} */
    @Override
    public void listAutoDetectionMidiPortNames (final AutoDetectionMidiPortNamesList list, final PlatformType platformType)
    {
        // Intentionally empty
    }
}
