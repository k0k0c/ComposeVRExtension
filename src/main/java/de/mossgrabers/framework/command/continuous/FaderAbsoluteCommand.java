// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.command.continuous;

import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.command.core.AbstractContinuousCommand;
import de.mossgrabers.framework.configuration.Configuration;
import de.mossgrabers.framework.controller.ControlSurface;
import de.mossgrabers.framework.daw.AbstractTrackBankProxy;
import de.mossgrabers.framework.daw.data.TrackData;


/**
 * Remote to change the volumes of the current track bank.
 *
 * @param <S> The type of the control surface
 * @param <C> The type of the configuration
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class FaderAbsoluteCommand<S extends ControlSurface<C>, C extends Configuration> extends AbstractContinuousCommand<S, C>
{
    private int index;


    /**
     * Constructor.
     *
     * @param index The index of the button
     * @param model The model
     * @param surface The surface
     */
    public FaderAbsoluteCommand (final int index, final Model model, final S surface)
    {
        super (model, surface);
        this.index = index;
    }


    /** {@inheritDoc} */
    @Override
    public void execute (final int value)
    {
        final AbstractTrackBankProxy currentTrackBank = this.model.getCurrentTrackBank ();
        if (this.surface.isShiftPressed ())
        {
            final TrackData track = currentTrackBank.getTrack (this.index);
            if (!track.doesExist ())
                return;
            final int volume = track.getVolume ();
            this.surface.getDisplay ().notify (volume < value ? "Move down" : volume > value ? "Move up" : "Perfect!");
        }
        else
            currentTrackBank.setVolume (this.index, value);
    }
}
