// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.view;

import de.mossgrabers.framework.ButtonEvent;
import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.configuration.Configuration;
import de.mossgrabers.framework.controller.ControlSurface;
import de.mossgrabers.framework.daw.BitwigColors;
import de.mossgrabers.framework.daw.CursorDeviceProxy;
import de.mossgrabers.framework.daw.TrackBankProxy;
import de.mossgrabers.framework.daw.data.ChannelData;
import de.mossgrabers.framework.scale.Scales;

import com.bitwig.extension.controller.api.CursorDevice;
import com.bitwig.extension.controller.api.CursorDeviceFollowMode;


/**
 * Abstract implementation for a 64 drum grid.
 *
 * @param <S> The type of the control surface
 * @param <C> The type of the configuration
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class AbstractDrumView64<S extends ControlSurface<C>, C extends Configuration> extends AbstractView<S, C> implements SceneView, TransposeView
{
    protected static final int    DRUM_START_KEY = 36;
    protected static final int    GRID_COLUMNS   = 8;

    // @formatter:off
    protected static final int [] DRUM_MATRIX    =
    {
        0,  1,  2,  3, 32, 33, 34, 35,      // 1st row
        4,  5,  6,  7, 36, 37, 38, 39,
        8,  9, 10, 11, 40, 41, 42, 43,
       12, 13, 14, 15, 44, 45, 46, 47,
       16, 17, 18, 19, 48, 49, 50, 51,
       20, 21, 22, 23, 52, 53, 54, 55,
       24, 25, 26, 27, 56, 57, 58, 59,
       28, 29, 30, 31, 60, 61, 62, 63      // 8th row
    };
    // @formatter:on

    protected int                 offsetY;
    protected int                 selectedPad    = 0;
    protected int []              pressedKeys    = new int [128];
    protected int                 columns;
    protected int                 rows;
    protected int                 drumOctave;
    protected CursorDeviceProxy   primaryDevice;


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public AbstractDrumView64 (final S surface, final Model model)
    {
        super ("Drum 64", surface, model);

        this.offsetY = DRUM_START_KEY;

        this.canScrollUp = false;
        this.canScrollDown = false;

        this.scales = this.model.getScales ();
        this.noteMap = Scales.getEmptyMatrix ();

        this.columns = 8;
        this.rows = 8;

        this.drumOctave = 0;

        final TrackBankProxy tb = model.getTrackBank ();
        // Light notes send from the sequencer
        tb.addNoteObserver ( (note, velocity) -> this.pressedKeys[note] = velocity);
        tb.addTrackSelectionObserver ( (final int index, final boolean isSelected) -> this.clearPressedKeys ());

        final CursorDevice cd = tb.getCursorTrack ().createCursorDevice ("64_DRUM_PADS", "64 Drum Pads", 0, CursorDeviceFollowMode.FIRST_INSTRUMENT);
        this.primaryDevice = new CursorDeviceProxy (model.getHost (), cd, this.model.getValueChanger (), 0, 0, 0, 64, 64);
    }


    /** {@inheritDoc} */
    @Override
    public void onScene (final int index, final ButtonEvent event)
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    public void onActivate ()
    {
        super.onActivate ();

        this.primaryDevice.enableObservers (true);
        this.primaryDevice.setDrumPadIndication (true);
    }


    /** {@inheritDoc} */
    @Override
    public void onDeactivate ()
    {
        super.onDeactivate ();

        this.primaryDevice.enableObservers (false);
        this.primaryDevice.setDrumPadIndication (false);
    }


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        if (!this.model.canSelectedTrackHoldNotes ())
            return;

        final int index = note - 36;
        final int x = index % this.columns;
        final int y = index / this.columns;
        this.selectedPad = (x >= 4 ? 32 : 0) + y * 4 + x % 4;

        final int playedPad = velocity == 0 ? -1 : this.selectedPad;

        // Mark selected note
        this.pressedKeys[this.offsetY + this.selectedPad] = velocity;

        if (playedPad < 0)
            return;

        this.handleButtonCombinations (playedPad);
    }


    /** {@inheritDoc} */
    @Override
    public void drawGrid ()
    {
        if (!this.model.canSelectedTrackHoldNotes ())
        {
            this.surface.getPadGrid ().turnOff ();
            return;
        }

        // halfColumns x playLines Drum Pad Grid
        final boolean hasDrumPads = this.primaryDevice.hasDrumPads ();
        boolean isSoloed = false;
        final int numPads = this.rows * this.columns;
        if (hasDrumPads)
        {
            for (int i = 0; i < numPads; i++)
            {
                if (this.primaryDevice.getDrumPad (i).isSolo ())
                {
                    isSoloed = true;
                    break;
                }
            }
        }
        final boolean isRecording = this.model.hasRecordingState ();
        for (int index = 0; index < numPads; index++)
        {
            final int x = index / 32 * 4 + index % 4;
            final int y = index / 4 % 8;
            this.surface.getPadGrid ().lightEx (x, 7 - y, this.getPadColor (index, this.primaryDevice, isSoloed, isRecording));
        }
    }


    private String getPadColor (final int index, final CursorDeviceProxy primary, final boolean isSoloed, final boolean isRecording)
    {
        // Playing note?
        if (this.pressedKeys[this.offsetY + index] > 0)
            return isRecording ? AbstractDrumView.COLOR_PAD_RECORD : AbstractDrumView.COLOR_PAD_PLAY;
        // Selected?
        if (this.selectedPad == index)
            return AbstractDrumView.COLOR_PAD_SELECTED;

        // Exists and active?
        final ChannelData drumPad = primary.getDrumPad (index);
        if (!drumPad.doesExist () || !drumPad.isActivated ())
            return this.surface.getConfiguration ().isTurnOffEmptyDrumPads () ? AbstractDrumView.COLOR_PAD_OFF : AbstractDrumView.COLOR_PAD_NO_CONTENT;
        // Muted or soloed?
        if (drumPad.isMute () || isSoloed && !drumPad.isSolo ())
            return AbstractDrumView.COLOR_PAD_MUTED;

        return this.getPadContentColor (drumPad);
    }


    protected String getPadContentColor (final ChannelData drumPad)
    {
        return BitwigColors.getColorIndex (drumPad.getColor ());
    }


    private void clearPressedKeys ()
    {
        for (int i = 0; i < 128; i++)
            this.pressedKeys[i] = 0;
    }


    /** {@inheritDoc} */
    @Override
    public void updateNoteMapping ()
    {
        final boolean turnOn = this.model.canSelectedTrackHoldNotes () && !this.surface.isSelectPressed () && !this.surface.isDeletePressed () && !this.surface.isMutePressed () && !this.surface.isSoloPressed ();
        this.noteMap = turnOn ? this.getDrumMatrix () : Scales.getEmptyMatrix ();
        this.surface.setKeyTranslationTable (this.scales.translateMatrixToGrid (this.noteMap));
    }


    /** {@inheritDoc} */
    @Override
    public void onOctaveDown (final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;

        this.clearPressedKeys ();
        final int oldDrumOctave = this.drumOctave;
        this.drumOctave = Math.max (-2, this.drumOctave - 1);
        this.offsetY = DRUM_START_KEY + this.drumOctave * 16;
        this.updateNoteMapping ();
        this.surface.getDisplay ().notify (this.getDrumRangeText (), true, true);

        if (oldDrumOctave != this.drumOctave)
        {
            // TODO Bugfix required: scrollChannelsUp scrolls the whole bank
            for (int i = 0; i < 16; i++)
                this.primaryDevice.scrollDrumPadsUp ();
        }
    }


    /** {@inheritDoc} */
    @Override
    public void onOctaveUp (final ButtonEvent event)
    {
        if (event != ButtonEvent.DOWN)
            return;

        this.clearPressedKeys ();
        final int oldDrumOctave = this.drumOctave;
        this.drumOctave = Math.min (1, this.drumOctave + 1);
        this.offsetY = DRUM_START_KEY + this.drumOctave * 16;
        this.updateNoteMapping ();
        this.surface.getDisplay ().notify (this.getDrumRangeText (), true, true);
        if (oldDrumOctave != this.drumOctave)
        {
            // TODO Bugfix required: scrollChannelsUp scrolls the whole bank
            for (int i = 0; i < 16; i++)
                this.primaryDevice.scrollDrumPadsDown ();
        }
    }


    private void handleButtonCombinations (final int playedPad)
    {
        if (this.surface.isDeletePressed ())
        {
            // Delete all of the notes on that "pad"
            this.handleDeleteButton (playedPad);
        }
        else if (this.surface.isMutePressed ())
        {
            // Mute that "pad"
            this.handleMuteButton (playedPad);
        }
        else if (this.surface.isSoloPressed ())
        {
            // Solo that "pad"
            this.handleSoloButton (playedPad);
        }
        else if (this.surface.isSelectPressed () || this.surface.getConfiguration ().isAutoSelectDrum ())
        {
            // Also select the matching device layer channel of the pad
            this.handleSelectButton (playedPad);
        }

        this.updateNoteMapping ();
    }


    @SuppressWarnings("unused")
    protected void handleDeleteButton (final int playedPad)
    {
        // Intentionally empty
    }


    protected void handleMuteButton (final int playedPad)
    {
        this.surface.setButtonConsumed (this.surface.getMuteButtonId ());
        this.primaryDevice.toggleLayerOrDrumPadMute (playedPad);
    }


    protected void handleSoloButton (final int playedPad)
    {
        this.surface.setButtonConsumed (this.surface.getSoloButtonId ());
        this.primaryDevice.toggleLayerOrDrumPadSolo (playedPad);
    }


    @SuppressWarnings("unused")
    protected void handleSelectButton (final int playedPad)
    {
        // Intentionally empty
    }


    private int [] getDrumMatrix ()
    {
        final int [] matrix = DRUM_MATRIX;
        this.noteMap = Scales.getEmptyMatrix ();
        for (int i = 0; i < 64; i++)
        {
            final int n = matrix[i] == -1 ? -1 : matrix[i] + DRUM_START_KEY + this.drumOctave * 16;
            this.noteMap[DRUM_START_KEY + i] = n < 0 || n > 127 ? -1 : n;
        }
        return this.noteMap;
    }


    private String getDrumRangeText ()
    {
        final int s = DRUM_START_KEY + this.drumOctave * 64;
        return Scales.formatDrumNote (s) + " to " + Scales.formatDrumNote (s + 63);
    }


    /**
     * Get the drum octave.
     *
     * @return The drum octave
     */
    public int getDrumOctave ()
    {
        return this.drumOctave;
    }
}
