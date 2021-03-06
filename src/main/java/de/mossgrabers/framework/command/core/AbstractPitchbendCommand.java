// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.command.core;

import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.configuration.Configuration;
import de.mossgrabers.framework.controller.ControlSurface;


/**
 * Abstract base class for aftertouch commands.
 *
 * @param <S> The type of the control surface
 * @param <C> The type of the configuration
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class AbstractPitchbendCommand<S extends ControlSurface<C>, C extends Configuration> implements PitchbendCommand
{
    protected final Model model;
    protected final S     surface;


    /**
     * Constructor.
     *
     * @param model The model
     * @param surface The surface
     */
    public AbstractPitchbendCommand (final Model model, final S surface)
    {
        this.model = model;
        this.surface = surface;
    }


    /** {@inheritDoc} */
    @Override
    public void updateValue ()
    {
        // Intentionally empty
    }
}
