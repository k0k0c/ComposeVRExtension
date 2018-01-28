// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.daw;

import de.mossgrabers.framework.controller.ValueChanger;

import com.bitwig.extension.controller.api.Clip;
import com.bitwig.extension.controller.api.ControllerHost;

import java.util.Arrays;


/**
 * Proxy to the Bitwig Cursor clip.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class CursorClipProxy
{
    private int             stepSize;
    private int             rowSize;

    private final int [] [] data;
    private Clip            clip;
    private ValueChanger    valueChanger;
    private int             editPage = 0;


    /**
     * Constructor.
     *
     * @param host The host
     * @param valueChanger The value changer
     * @param stepSize The number of steps of the clip to monitor
     * @param rowSize The number of note rows of the clip to monitor
     */
    public CursorClipProxy (final ControllerHost host, final ValueChanger valueChanger, final int stepSize, final int rowSize)
    {
        this.valueChanger = valueChanger;

        this.stepSize = stepSize;
        this.rowSize = rowSize;

        this.data = new int [this.stepSize] [];

        for (int step = 0; step < this.stepSize; step++)
        {
            this.data[step] = new int [this.rowSize];
            Arrays.fill (this.data[step], 0);
        }

        // TODO We need the old method back to monitor both launcher and arranger - otherwise use
        // both and check which one exists!
        this.clip = host.createLauncherCursorClip (this.stepSize, this.rowSize);

        this.clip.playingStep ().markInterested ();
        this.clip.addStepDataObserver (this::handleStepData);

        this.clip.getPlayStart ().markInterested ();
        this.clip.getPlayStop ().markInterested ();
        this.clip.getLoopStart ().markInterested ();
        this.clip.getLoopLength ().markInterested ();
        this.clip.isLoopEnabled ().markInterested ();
        this.clip.getShuffle ().markInterested ();
        this.clip.getAccent ().markInterested ();
        this.clip.canScrollStepsBackwards ().markInterested ();
        this.clip.canScrollStepsForwards ().markInterested ();
    }


    /**
     * Dis-/Enable all attributes. They are enabled by default. Use this function if values are
     * currently not needed to improve performance.
     *
     * @param enable True to enable
     */
    public void enableObservers (final boolean enable)
    {
        this.clip.playingStep ().setIsSubscribed (enable);
        this.clip.getPlayStart ().setIsSubscribed (enable);
        this.clip.getPlayStop ().setIsSubscribed (enable);
        this.clip.getLoopStart ().setIsSubscribed (enable);
        this.clip.getLoopLength ().setIsSubscribed (enable);
        this.clip.isLoopEnabled ().setIsSubscribed (enable);
        this.clip.getShuffle ().setIsSubscribed (enable);
        this.clip.getAccent ().setIsSubscribed (enable);
    }


    /**
     * Set the color of the clip.
     *
     * @param red The red
     * @param green The green
     * @param blue The blue
     */
    public void setColor (final double red, final double green, final double blue)
    {
        this.clip.color ().set ((float) red, (float) green, (float) blue);
    }


    /**
     * Returns the start of the clip in beat time.
     *
     * @return The clips start time.
     */
    public double getPlayStart ()
    {
        return this.clip.getPlayStart ().get ();
    }


    /**
     * Set the start of the clip in beat time.
     *
     * @param start The clips start time
     */
    public void setPlayStart (final double start)
    {
        this.clip.getPlayStart ().set (start);
    }


    /**
     * Change the start of the clip.
     *
     * @param control The control value
     */
    public void changePlayStart (final int control)
    {
        this.clip.getPlayStart ().inc (this.valueChanger.calcKnobSpeed (control));
    }


    /**
     * Returns the end of the clip in beat time.
     *
     * @return The clips start time.
     */
    public double getPlayEnd ()
    {
        return this.clip.getPlayStop ().get ();
    }


    /**
     * Set the end of the clip in beat time.
     *
     * @param end The clips start time
     */
    public void setPlayEnd (final double end)
    {
        this.clip.getPlayStop ().set (end);
    }


    /**
     * Change the end of the clip.
     *
     * @param control The control value
     */
    public void changePlayEnd (final int control)
    {
        this.clip.getPlayStop ().inc (this.valueChanger.calcKnobSpeed (control));
    }


    /**
     * Sets the start and the end of the clip. Ensure that the start is before the end.
     *
     * @param start The start to set
     * @param end The end to set
     */
    public void setPlayRange (final double start, final double end)
    {
        // Need to distinguish if we move left or right since the start and
        // end cannot be the same value
        if (this.getPlayStart () < start)
        {
            this.setPlayEnd (end);
            this.setPlayStart (start);
        }
        else
        {
            this.setPlayStart (start);
            this.setPlayEnd (end);
        }
    }


    /**
     * Get the start of the loop.
     *
     * @return The start of the loop
     */
    public double getLoopStart ()
    {
        return this.clip.getLoopStart ().get ();
    }


    /**
     * Set the start of the loop.
     *
     * @param start The start of the loop
     */
    public void setLoopStart (final double start)
    {
        this.clip.getLoopStart ().set (start);
    }


    /**
     * Change the start of the loop.
     *
     * @param control The control value
     */
    public void changeLoopStart (final int control)
    {
        this.clip.getLoopStart ().inc (this.valueChanger.calcKnobSpeed (control));
    }


    /**
     * Get the length of the loop.
     *
     * @return The length of the loop
     */
    public double getLoopLength ()
    {
        return this.clip.getLoopLength ().get ();
    }


    /**
     * Set the length of the loop.
     *
     * @param length The length of the loop
     */
    public void setLoopLength (final int length)
    {
        this.clip.getLoopLength ().set (length);
    }


    /**
     * Change the length of the loop.
     *
     * @param control The control value
     */
    public void changeLoopLength (final int control)
    {
        this.clip.getLoopLength ().inc (this.valueChanger.calcKnobSpeed (control));
    }


    /**
     * Is the loop enabled?
     *
     * @return True if enabled
     */
    public boolean isLoopEnabled ()
    {
        return this.clip.isLoopEnabled ().get ();
    }


    /**
     * Set if the loop is enabled.
     *
     * @param enable True if enabled
     */
    public void setLoopEnabled (final boolean enable)
    {
        this.clip.isLoopEnabled ().set (enable);
    }


    /**
     * Is shuffle enabled?
     *
     * @return True if shuffle is enabled
     */
    public boolean isShuffleEnabled ()
    {
        return this.clip.getShuffle ().get ();
    }


    /**
     * Set if shuffle is enabled?
     *
     * @param enable True if shuffle is enabled
     */
    public void setShuffleEnabled (final boolean enable)
    {
        this.clip.getShuffle ().set (enable);
    }


    /**
     * Get the accent value as a formatted string.
     *
     * @return The formatted string
     */
    public String getFormattedAccent ()
    {
        return Math.round (this.getAccent () * 10000) / 100 + "%";
    }


    /**
     * Get the accent value.
     *
     * @return The accent value
     */
    public double getAccent ()
    {
        return this.clip.getAccent ().get ();
    }


    /**
     * Reset the accent value to its default.
     */
    public void resetAccent ()
    {
        this.clip.getAccent ().set (0.5);
    }


    /**
     * Change the accent value.
     *
     * @param control The control value
     */
    public void changeAccent (final int control)
    {
        final double speed = this.valueChanger.calcKnobSpeed (control, this.valueChanger.getFractionValue () / 100.0);
        this.clip.getAccent ().inc (speed);
    }


    /**
     * Get the number of steps.
     *
     * @return The number of steps
     */
    public int getStepSize ()
    {
        return this.stepSize;
    }


    /**
     * Get the row of notes.
     *
     * @return The row of notes
     */
    public int getRowSize ()
    {
        return this.rowSize;
    }


    /**
     * Get the index of the current step
     *
     * @return The index of the current step
     */
    public int getCurrentStep ()
    {
        return this.clip.playingStep ().get ();
    }


    /**
     * Get the value (velocity) of a note.
     *
     * @param step The step
     * @param row The row
     * @return The velocity
     */
    public int getStep (final int step, final int row)
    {
        if (row < 0)
            return 0;
        return this.data[step][row];
    }


    /**
     * Toggle a note at a step.
     *
     * @param step The step
     * @param row The note row
     * @param velocity The velocity of the note
     */
    public void toggleStep (final int step, final int row, final int velocity)
    {
        this.clip.toggleStep (step, row, velocity);
    }


    /**
     * Set a note at a step.
     *
     * @param step The step
     * @param row The note row
     * @param velocity The velocity of the note
     * @param duration The length of the note
     */
    public void setStep (final int step, final int row, final int velocity, final double duration)
    {
        this.clip.setStep (step, row, velocity, duration);
    }


    /**
     * Clear a row (note).
     *
     * @param row The row to clear
     */
    public void clearRow (final int row)
    {
        this.clip.clearSteps (row);
    }


    /**
     * Does the row contain any notes?
     *
     * @param row THe row
     * @return True if it contains at least one note
     */
    public boolean hasRowData (final int row)
    {
        for (int step = 0; step < this.stepSize; step++)
            if (this.data[step][row] > 0)
                return true;
        return false;
    }


    /**
     * Set the length of a step.
     *
     * @param length The length
     */
    public void setStepLength (final double length)
    {
        this.clip.setStepSize (length);
    }


    /**
     * Scroll the clip view to a step and note.
     *
     * @param step The step
     * @param row The row
     */
    public void scrollTo (final int step, final int row)
    {
        this.clip.scrollToKey (row);
        this.clip.scrollToStep (step);
    }


    /**
     * Scroll the clip view to the given page. Depends on the number of the steps of a page.
     *
     * @param page The page to select
     */
    public void scrollToPage (final int page)
    {
        this.clip.scrollToStep (page * this.stepSize);
        this.editPage = page;
    }


    /**
     * Get the edit page.
     *
     * @return The edit page
     */
    public int getEditPage ()
    {
        return this.editPage;
    }


    /**
     * Scroll the steps one page backwards.
     */
    public void scrollStepsPageBackwards ()
    {
        if (this.editPage <= 0)
            return;
        this.clip.scrollStepsPageBackwards ();
        this.editPage--;
    }


    /**
     * Scroll the steps one page forwards.
     */
    public void scrollStepsPageForward ()
    {
        this.clip.scrollStepsPageForward ();
        this.editPage++;
    }


    /**
     * Value that reports if the note grid if the note grid steps can be scrolled backwards.
     *
     * @return True if it can be scrolled
     */
    public boolean canScrollStepsBackwards ()
    {
        // TODO Bugfix required: this.clip.canScrollStepsBackwards ().get ();
        return true;
    }


    /**
     * Value that reports if the note grid if the note grid steps can be scrolled forwards.
     *
     * @return True if it can be scrolled
     */
    public boolean canScrollStepsForwards ()
    {
        // TODO Bugfix required: this.clip.canScrollStepsForwards ().get ();
        return true;
    }


    /**
     * Duplicate the clip.
     */
    public void duplicate ()
    {
        this.clip.duplicate ();
    }


    /**
     * Duplicate the content of a clip (in the clip).
     */
    public void duplicateContent ()
    {
        this.clip.duplicateContent ();
    }


    /**
     * Quantizes the start time of all notes in the clip according to the given amount. The note
     * lengths remain the same as before.
     *
     * @param amount A factor between `0` and `1` that allows to morph between the original note
     *            start and the quantized note start.
     */
    public void quantize (final double amount)
    {
        if (amount < 0.000001 || amount > 1)
            return;
        this.clip.quantize (amount);
    }


    /**
     * Transposes the notes in the clip by the given semitones.
     *
     * @param semitones The number of semitones
     */
    public void transpose (final int semitones)
    {
        this.clip.transpose (semitones);
    }


    private void handleStepData (final int col, final int row, final int state)
    {
        // state: step is empty (0) or a note continues playing (1) or starts playing (2)
        this.data[col][row] = state;
    }
}