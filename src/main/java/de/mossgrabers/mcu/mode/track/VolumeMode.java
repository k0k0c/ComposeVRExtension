// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.mcu.mode.track;

import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.controller.display.Display;
import de.mossgrabers.framework.daw.AbstractTrackBankProxy;
import de.mossgrabers.framework.daw.data.TrackData;
import de.mossgrabers.mcu.controller.MCUControlSurface;


/**
 * Mode for editing a volume parameter of all tracks.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class VolumeMode extends AbstractTrackMode
{
    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model The model
     */
    public VolumeMode (final MCUControlSurface surface, final Model model)
    {
        super (surface, model);
    }


    /** {@inheritDoc} */
    @Override
    public void onValueKnob (final int index, final int value)
    {
        final int channel = this.surface.getExtenderOffset () + index;
        this.model.getCurrentTrackBank ().changeVolume (channel, value);
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay ()
    {
        if (!this.surface.getConfiguration ().hasDisplay1 ())
            return;

        this.drawDisplay2 ();
        if (!this.drawTrackHeader ())
            return;

        final Display d = this.surface.getDisplay ();
        final AbstractTrackBankProxy tb = this.model.getCurrentTrackBank ();
        final int extenderOffset = this.surface.getExtenderOffset ();
        for (int i = 0; i < 8; i++)
        {
            final TrackData t = tb.getTrack (extenderOffset + i);
            d.setCell (1, i, t.getVolumeStr (6));
        }
        d.done (1);
    }


    /** {@inheritDoc} */
    @Override
    protected boolean drawTrackHeader ()
    {
        if (!super.drawTrackHeader ())
            return false;

        if (this.surface.getConfiguration ().isDisplayTrackNames ())
            return true;

        final AbstractTrackBankProxy tb = this.model.getCurrentTrackBank ();
        final Display d = this.surface.getDisplay ();
        final int extenderOffset = this.surface.getExtenderOffset ();
        for (int i = 0; i < 8; i++)
        {
            if (tb.getTrack (extenderOffset + i).doesExist ())
                d.setCell (0, i, "Volume");
            else
                d.clearCell (0, i);
        }
        d.done (0);

        return true;
    }


    /** {@inheritDoc} */
    @Override
    protected void updateKnobLEDs ()
    {
        final AbstractTrackBankProxy tb = this.model.getCurrentTrackBank ();
        final int upperBound = this.model.getValueChanger ().getUpperBound ();
        final int extenderOffset = this.surface.getExtenderOffset ();
        for (int i = 0; i < 8; i++)
        {
            final TrackData t = tb.getTrack (extenderOffset + i);
            this.surface.setKnobLED (i, MCUControlSurface.KNOB_LED_MODE_WRAP, t.getVolume (), upperBound);
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void resetParameter (final int index)
    {
        final int extenderOffset = this.surface.getExtenderOffset ();
        this.model.getCurrentTrackBank ().resetVolume (extenderOffset + index);
    }
}