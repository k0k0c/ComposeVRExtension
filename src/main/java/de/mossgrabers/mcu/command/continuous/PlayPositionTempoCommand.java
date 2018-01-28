// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.mcu.command.continuous;

import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.command.continuous.PlayPositionCommand;
import de.mossgrabers.mcu.MCUConfiguration;
import de.mossgrabers.mcu.controller.MCUControlSurface;


/**
 * Command to change the time (play position).
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class PlayPositionTempoCommand extends PlayPositionCommand<MCUControlSurface, MCUConfiguration>
{
    /**
     * Constructor.
     *
     * @param model The model
     * @param surface The surface
     */
    public PlayPositionTempoCommand (final Model model, final MCUControlSurface surface)
    {
        super (model, surface);
    }


    /** {@inheritDoc} */
    @Override
    public void execute (final int value)
    {
        if (this.surface.isPressed (MCUControlSurface.MCU_OPTION))
            this.model.getTransport ().changeTempo (value <= 61);
        else
            super.execute (value);
    }
}
