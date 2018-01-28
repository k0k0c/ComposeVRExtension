// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.mcu.mode.track;

import de.mossgrabers.framework.ButtonEvent;
import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.StringUtils;
import de.mossgrabers.framework.controller.display.Display;
import de.mossgrabers.framework.daw.ApplicationProxy;
import de.mossgrabers.framework.daw.MasterTrackProxy;
import de.mossgrabers.mcu.controller.MCUControlSurface;
import de.mossgrabers.mcu.mode.BaseMode;


/**
 * Mode for editing the parameters of the master track.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class MasterMode extends BaseMode
{
    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model The model
     * @param isTemporary If true treat this mode only as temporary
     */
    public MasterMode (final MCUControlSurface surface, final Model model, final boolean isTemporary)
    {
        super (surface, model);
        this.isTemporary = isTemporary;
    }


    /** {@inheritDoc} */
    @Override
    public void onActivate ()
    {
        this.setActive (true);
    }


    /** {@inheritDoc} */
    @Override
    public void onDeactivate ()
    {
        this.setActive (false);
    }


    /** {@inheritDoc} */
    @Override
    public void onValueKnob (final int index, final int value)
    {
        if (index == 0)
            this.model.getMasterTrack ().changeVolume (value);
        else if (index == 1)
            this.model.getMasterTrack ().changePan (value);
    }


    /** {@inheritDoc} */
    @Override
    public void onRowButton (final int row, final int index, final ButtonEvent event)
    {
        if (event != ButtonEvent.UP)
            return;
        final ApplicationProxy application = this.model.getApplication ();
        switch (index)
        {
            case 0:
                this.model.getMasterTrack ().resetVolume ();
                break;

            case 1:
                this.model.getMasterTrack ().resetPan ();
                break;

            case 2:
            case 3:
            case 4:
                application.setEngineActive (!application.isEngineActive ());
                break;

            case 6:
                application.previousProject ();
                break;

            case 7:
                application.nextProject ();
                break;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay ()
    {
        if (!this.surface.getConfiguration ().hasDisplay1 ())
            return;

        this.drawDisplay2 ();

        final Display d = this.surface.getDisplay ().clear ();
        final ApplicationProxy application = this.model.getApplication ();
        final String projectName = StringUtils.fixASCII (application.getProjectName ());
        final MasterTrackProxy master = this.model.getMasterTrack ();

        d.setCell (0, 0, "Volume").setCell (0, 1, "Pan").setBlock (0, 1, "Audio Engine:").setCell (0, 4, application.isEngineActive () ? " On" : " Off");
        d.setCell (0, 5, "Prjct:").setBlock (0, 3, projectName);
        d.setCell (1, 0, master.getVolumeStr (6)).setCell (1, 1, master.getPanStr (6)).setBlock (1, 1, application.isEngineActive () ? "  Turn off" : "  Turn on");
        d.setCell (1, 6, " <<").setCell (1, 7, " >>").allDone ();
    }


    private void setActive (final boolean enable)
    {
        final MasterTrackProxy mt = this.model.getMasterTrack ();
        mt.setVolumeIndication (enable);
        mt.setPanIndication (enable);
    }


    /** {@inheritDoc} */
    @Override
    protected void updateKnobLEDs ()
    {
        final MasterTrackProxy masterTrack = this.model.getMasterTrack ();
        final int upperBound = this.model.getValueChanger ().getUpperBound ();
        this.surface.setKnobLED (0, MCUControlSurface.KNOB_LED_MODE_WRAP, masterTrack.getVolume (), upperBound);
        this.surface.setKnobLED (1, MCUControlSurface.KNOB_LED_MODE_BOOST_CUT, masterTrack.getPan (), upperBound);
        for (int i = 0; i < 6; i++)
            this.surface.setKnobLED (2 + i, MCUControlSurface.KNOB_LED_MODE_WRAP, 0, upperBound);
    }


    /** {@inheritDoc} */
    @Override
    protected void resetParameter (final int index)
    {
        final MasterTrackProxy masterTrack = this.model.getMasterTrack ();
        switch (index)
        {
            case 0:
                masterTrack.resetVolume ();
                break;
            case 1:
                masterTrack.resetPan ();
                break;
            default:
                // Intentionally empty
                break;
        }
    }
}