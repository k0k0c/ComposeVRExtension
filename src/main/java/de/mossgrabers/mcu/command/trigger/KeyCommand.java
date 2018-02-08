// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.mcu.command.trigger;

import de.mossgrabers.framework.ButtonEvent;
import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.mode.ModeManager;
import de.mossgrabers.mcu.MCUConfiguration;
import de.mossgrabers.mcu.controller.MCUControlSurface;
import de.mossgrabers.mcu.mode.Modes;


/**
 * Remote for keys.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class KeyCommand extends AbstractTriggerCommand<MCUControlSurface, MCUConfiguration>
{
    /** The direction of the cursor. */
    public enum Key
    {
        /** The enter key. */
        ENTER,
        /** The escape key. */
        ESCAPE
    }

    protected Key key;


    /**
     * Constructor.
     *
     * @param key The key to execute
     * @param model The model
     * @param surface The surface
     */
    public KeyCommand (final Key key, final Model model, final MCUControlSurface surface)
    {
        super (model, surface);
        this.key = key;
    }


    /** {@inheritDoc} */
    @Override
    public void execute (final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;

        final ModeManager modeManager = this.surface.getModeManager ();
        switch (this.key)
        {
            case ENTER:
                if (modeManager.isActiveMode (Modes.MODE_BROWSER))
                {
                    this.model.getBrowser ().stopBrowsing (true);
                    modeManager.restoreMode ();
                }
                else
                    this.model.getApplication ().enter ();
                break;

            case ESCAPE:
                if (modeManager.isActiveMode (Modes.MODE_BROWSER))
                {
                    this.model.getBrowser ().stopBrowsing (false);
                    modeManager.restoreMode ();
                }
                else
                    this.model.getApplication ().escape ();
                break;
        }
    }
}
