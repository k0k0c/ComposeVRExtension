// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.command.trigger;

import de.mossgrabers.framework.ButtonEvent;
import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.command.core.AbstractTriggerCommand;
import de.mossgrabers.framework.configuration.Configuration;
import de.mossgrabers.framework.controller.ControlSurface;
import de.mossgrabers.framework.mode.Mode;


/**
 * Command to delegate the button pushes of a button row to the active mode.
 *
 * @param <S> The type of the control surface
 * @param <C> The type of the configuration
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class ButtonRowModeCommand<S extends ControlSurface<C>, C extends Configuration> extends AbstractTriggerCommand<S, C>
{
    private int index;
    private int row;


    /**
     * Constructor.
     *
     * @param row The number of the button row
     * @param index The index of the button
     * @param model The model
     * @param surface The surface
     */
    public ButtonRowModeCommand (final int row, final int index, final Model model, final S surface)
    {
        super (model, surface);
        this.row = row;
        this.index = index;
    }


    /** {@inheritDoc} */
    @Override
    public void execute (final ButtonEvent event)
    {
        final Mode m = this.surface.getModeManager ().getActiveMode ();
        if (m != null)
            m.onRowButton (this.row, this.index, event);
    }
}
