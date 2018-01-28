// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.mcu.mode.device;

import de.mossgrabers.framework.Model;
import de.mossgrabers.framework.controller.display.Display;
import de.mossgrabers.framework.daw.BrowserProxy;
import de.mossgrabers.framework.daw.CursorDeviceProxy;
import de.mossgrabers.framework.daw.data.BrowserColumnData;
import de.mossgrabers.framework.daw.data.BrowserColumnItemData;
import de.mossgrabers.mcu.controller.MCUControlSurface;
import de.mossgrabers.mcu.mode.BaseMode;


/**
 * Mode for navigating the browser.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class DeviceBrowserMode extends BaseMode
{
    private static final int SELECTION_OFF    = 0;
    private static final int SELECTION_PRESET = 1;
    private static final int SELECTION_FILTER = 2;

    private int              selectionMode;
    private int              filterColumn;


    /**
     * Constructor.
     *
     * @param surface The control surface
     * @param model The model
     */
    public DeviceBrowserMode (final MCUControlSurface surface, final Model model)
    {
        super (surface, model);

        this.isTemporary = false;

        this.selectionMode = SELECTION_OFF;
        this.filterColumn = -1;
    }


    /** {@inheritDoc} */
    @Override
    public void onDeactivate ()
    {
        this.model.getBrowser ().stopBrowsing (true);
    }


    /** {@inheritDoc} */
    @Override
    public void onValueKnob (final int index, final int value)
    {
        int speed = (int) this.model.getValueChanger ().calcKnobSpeed (value, 1);
        final boolean direction = speed > 0;
        if (this.surface.isShiftPressed ())
            speed = speed * 4;

        speed = Math.abs (speed);
        if (direction)
            this.selectNext (index, speed);
        else
            this.selectPrevious (index, speed);
    }


    /** {@inheritDoc} */
    @Override
    public void updateDisplay ()
    {
        final BrowserProxy browser = this.model.getBrowser ();
        final Display d = this.surface.getDisplay ();
        final boolean isPresetSession = browser.isPresetContentType ();
        final CursorDeviceProxy cd = this.model.getCursorDevice ();
        if (isPresetSession && !(browser.isActive () && cd.hasSelectedDevice ()))
        {
            d.notify ("No active Browsing Session. Select device and press Browser...");
            return;
        }

        d.clear ();

        switch (this.selectionMode)
        {
            case SELECTION_OFF:
                for (int i = 0; i < 7; i++)
                {
                    final BrowserColumnData column = this.getFilterColumn (i);
                    final String value = column != null && column.doesCursorExist () ? column.getCursorName ().equals (column.getWildcard ()) ? "-" : column.getCursorName () : "";
                    final String name = column == null ? "" : this.optimizeName (column.getName (), 6);
                    d.setCell (0, i, name).setCell (1, i, value);
                }
                final String selectedResult = browser.getSelectedResult ();
                d.setCell (0, 7, browser.getSelectedContentType ()).setCell (1, 7, selectedResult == null || selectedResult.length () == 0 ? "-" : selectedResult);
                break;

            case SELECTION_PRESET:
                final BrowserColumnItemData [] results = browser.getResultColumnItems ();
                for (int i = 0; i < browser.getNumFilterColumnEntries (); i++)
                {
                    if (i < results.length)
                        d.setBlock (i / 4, i % 4, (results[i].isSelected () ? ">" : " ") + results[i].getName ());
                    else
                        break;
                }
                break;

            case SELECTION_FILTER:
                final BrowserColumnItemData [] items = browser.getFilterColumn (this.filterColumn).getItems ();
                for (int i = 0; i < browser.getNumResults (); i++)
                    d.setBlock (i / 4, i % 4, (items[i].isSelected () ? ">" : " ") + items[i].getName ());
                break;
        }
        d.allDone ();
    }


    /** {@inheritDoc} */
    @Override
    protected void resetParameter (final int index)
    {
        if (this.selectionMode != SELECTION_OFF)
        {
            this.selectionMode = SELECTION_OFF;
            return;
        }

        if (index == 7)
        {
            this.selectionMode = SELECTION_PRESET;
            this.filterColumn = -1;
        }
        else
        {
            final BrowserColumnData fc = this.getFilterColumn (index);
            if (fc != null && fc.doesExist ())
            {
                this.selectionMode = SELECTION_FILTER;
                this.filterColumn = fc.getIndex ();
            }
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void updateKnobLEDs ()
    {
        for (int i = 0; i < 8; i++)
            this.surface.setKnobLED (i, MCUControlSurface.KNOB_LED_MODE_WRAP, 0, 1);
    }


    private BrowserColumnData getFilterColumn (final int index)
    {
        final BrowserProxy browser = this.model.getBrowser ();
        int column = -1;
        final boolean [] browserDisplayFilter = this.surface.getConfiguration ().getBrowserDisplayFilter ();
        for (int i = 0; i < browser.getFilterColumnCount (); i++)
        {
            if (browserDisplayFilter[i])
            {
                column++;
                if (column == index)
                    return browser.getFilterColumn (i);
            }
        }
        return null;
    }


    private void selectNext (final int index, final int count)
    {
        final BrowserProxy browser = this.model.getBrowser ();
        if (index < 7)
        {
            final BrowserColumnData fc = this.getFilterColumn (index);
            if (fc != null && fc.doesExist ())
            {
                this.filterColumn = fc.getIndex ();
                for (int i = 0; i < count; i++)
                    browser.selectNextFilterItem (this.filterColumn);
                if (browser.getSelectedFilterItemIndex (this.filterColumn) == -1)
                    browser.nextFilterItemPage (this.filterColumn);
            }
        }
        else
        {
            for (int i = 0; i < count; i++)
                browser.selectNextResult ();
        }
    }


    private void selectPrevious (final int index, final int count)
    {
        final BrowserProxy browser = this.model.getBrowser ();
        for (int i = 0; i < count; i++)
        {
            if (index < 7)
            {
                final BrowserColumnData fc = this.getFilterColumn (index);
                if (fc != null && fc.doesExist ())
                {
                    this.filterColumn = fc.getIndex ();
                    for (int j = 0; j < count; j++)
                        browser.selectPreviousFilterItem (this.filterColumn);
                    if (browser.getSelectedFilterItemIndex (this.filterColumn) == -1)
                        browser.previousFilterItemPage (this.filterColumn);
                }
            }
            else
            {
                for (int j = 0; j < count; j++)
                    browser.selectPreviousResult ();
            }
        }
    }
}