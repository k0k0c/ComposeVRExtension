// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.view;

import de.mossgrabers.framework.ButtonEvent;
import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.configuration.Configuration;
import de.mossgrabers.framework.controller.ControlSurface;
import de.mossgrabers.framework.controller.grid.PadGrid;
import de.mossgrabers.framework.daw.AbstractTrackBankProxy;
import de.mossgrabers.framework.daw.data.TrackData;
import de.mossgrabers.framework.scale.Scales;


/**
 * Abstract implementation for a note sequencer.
 *
 * @param <S> The type of the control surface
 * @param <C> The type of the configuration
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class AbstractNoteSequencerView<S extends ControlSurface<C>, C extends Configuration> extends AbstractSequencerView<S, C> implements TransposeView
{
    protected int   numDisplayRows = 8;
    protected int   numDisplayCols;
    protected int   startKey       = 36;
    protected int   loopPadPressed = -1;

    private boolean useTrackColor;


    /**
     * Constructor.
     *
     * @param name The name of the view
     * @param surface The surface
     * @param model The model
     * @param useTrackColor True to use the color of the current track for coloring the octaves
     */
    public AbstractNoteSequencerView (final String name, final S surface, final Model model, final boolean useTrackColor)
    {
        this (name, surface, model, 8, useTrackColor);
    }


    /**
     * Constructor.
     *
     * @param name The name of the view
     * @param surface The surface
     * @param model The model
     * @param numDisplayCols The number of grid columns
     * @param useTrackColor True to use the color of the current track for coloring the octaves
     */
    public AbstractNoteSequencerView (final String name, final S surface, final Model model, final int numDisplayCols, final boolean useTrackColor)
    {
        super (name, surface, model, 128, numDisplayCols, 7);

        this.useTrackColor = useTrackColor;
        this.numDisplayCols = numDisplayCols;
        this.offsetY = this.startKey;

        this.clip.scrollTo (0, this.startKey);
    }


    /** {@inheritDoc} */
    @Override
    public void onActivate ()
    {
        this.updateScale ();
        super.onActivate ();
    }


    /** {@inheritDoc} */
    @Override
    public void updateNoteMapping ()
    {
        super.updateNoteMapping ();
        this.updateScale ();
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        if (!this.model.canSelectedTrackHoldNotes ())
            return;
        final int index = note - 36;
        final int x = index % 8;
        final int y = index / 8;

        if (y < this.numSequencerRows)
        {
            if (velocity != 0)
                this.clip.toggleStep (x, this.noteMap[y], this.configuration.isAccentActive () ? this.configuration.getFixedAccentValue () : velocity);
            return;
        }

        // Clip length/loop area
        final int pad = x;

        // Button pressed?
        if (velocity > 0)
        {
            // Not yet a button pressed, store it
            if (this.loopPadPressed == -1)
                this.loopPadPressed = pad;
            return;
        }

        if (this.loopPadPressed == -1)
            return;

        if (pad == this.loopPadPressed && pad != this.clip.getEditPage ())
        {
            // Only single pad pressed -> page selection
            this.clip.scrollToPage (pad);
        }
        else
        {
            // Set a new loop between the 2 selected pads
            final int start = this.loopPadPressed < pad ? this.loopPadPressed : pad;
            final int end = (this.loopPadPressed < pad ? pad : this.loopPadPressed) + 1;
            final int lengthOfOnePad = this.getLengthOfOnePage (this.numDisplayCols);
            final double newStart = start * lengthOfOnePad;
            this.clip.setLoopStart (newStart);
            this.clip.setLoopLength ((end - start) * lengthOfOnePad);
            this.clip.setPlayRange (newStart, (double) end * lengthOfOnePad);
        }

        this.loopPadPressed = -1;
    }


    /** {@inheritDoc} */
    @Override
    public void drawGrid ()
    {
        final PadGrid gridPad = this.surface.getPadGrid ();
        if (!this.model.canSelectedTrackHoldNotes ())
        {
            gridPad.turnOff ();
            return;
        }

        final AbstractTrackBankProxy tb = this.model.getCurrentTrackBank ();
        final TrackData selectedTrack = tb.getSelectedTrack ();

        // Steps with notes
        final int step = this.clip.getCurrentStep ();
        final int hiStep = this.isInXRange (step) ? step % this.numDisplayCols : -1;
        for (int x = 0; x < this.numDisplayCols; x++)
        {
            for (int y = 0; y < this.numSequencerRows; y++)
            {
                // 0: not set, 1: note continues playing, 2: start of note
                final int isSet = this.clip.getStep (x, this.noteMap[y]);
                gridPad.lightEx (x, this.numDisplayRows - 1 - y, this.getStepColor (isSet, x == hiStep, y, selectedTrack));
            }
        }

        if (this.numDisplayRows - this.numSequencerRows <= 0)
            return;

        final int lengthOfOnePad = this.getLengthOfOnePage (this.numDisplayCols);
        final double loopStart = this.clip.getLoopStart ();
        final int loopStartPad = (int) Math.ceil (loopStart / lengthOfOnePad);
        final int loopEndPad = (int) Math.ceil ((loopStart + this.clip.getLoopLength ()) / lengthOfOnePad);
        final int currentPage = step / this.numDisplayCols;
        for (int pad = 0; pad < 8; pad++)
            gridPad.lightEx (pad, 0, this.getPageColor (loopStartPad, loopEndPad, currentPage, this.clip.getEditPage (), pad));
    }


    /**
     * Get the color for a step.
     *
     * @param isSet The step has content
     * @param hilite The step should be highlighted
     * @param note The note of the step
     * @param track A track from which to use the color
     * @return The color
     */
    protected String getStepColor (final int isSet, final boolean hilite, final int note, final TrackData track)
    {
        switch (isSet)
        {
            // Note continues
            case 1:
                return hilite ? COLOR_STEP_HILITE_CONTENT : COLOR_CONTENT_CONT;
            // Note starts
            case 2:
                return hilite ? COLOR_STEP_HILITE_CONTENT : COLOR_CONTENT;
            // Empty
            default:
                return hilite ? COLOR_STEP_HILITE_NO_CONTENT : this.getColor (note, this.useTrackColor ? track : null);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void onOctaveDown (final ButtonEvent event)
    {
        if (event == ButtonEvent.DOWN)
            this.updateOctave (Math.max (0, this.offsetY - this.getScrollOffset ()));
    }


    /** {@inheritDoc} */
    @Override
    public void onOctaveUp (final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;
        final int offset = this.getScrollOffset ();
        if (this.offsetY + offset < this.clip.getRowSize ())
            this.updateOctave (this.offsetY + offset);
    }


    protected void updateScale ()
    {
        this.noteMap = this.model.canSelectedTrackHoldNotes () ? this.scales.getSequencerMatrix (8, this.offsetY) : Scales.getEmptyMatrix ();
    }


    protected void updateOctave (final int value)
    {
        this.offsetY = value;
        this.updateScale ();
        this.surface.getDisplay ().notify (Scales.getSequencerRangeText (this.noteMap[0], this.noteMap[this.numSequencerRows - 1]), true, true);
    }
}