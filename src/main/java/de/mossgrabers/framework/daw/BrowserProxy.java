// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.framework.daw;

import com.bitwig.extension.callback.BooleanValueChangedCallback;
import com.bitwig.extension.controller.api.*;
import com.las4vc.composevr.DAWModel;
import de.mossgrabers.framework.daw.data.BrowserColumnData;
import de.mossgrabers.framework.daw.data.BrowserColumnItemData;


/**
 * Provides access to the device, preset, sample, ... browser.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class BrowserProxy
{
    private CursorTrack              cursorTrack;
    private CursorDeviceProxy        cursorDevice;
    public PopupBrowser             browser;

    final BrowserFilterColumn []     filterColumns;
    final BrowserColumnData []       columnData;

    private int                      numResults;
    private BrowserResultsColumn     resultsColumn;
    private CursorBrowserResultItem  cursorResult;
    private BrowserResultsItem result;

    private BrowserResultsItemBank   resultsItemBank;
    private BrowserColumnItemData [] resultData;
    private int                      numFilterColumnEntries;


    /**
     * Constructor.
     *
     * @param host The host
     * @param cursorTrack The cursor track
     * @param cursorDevice The cursor device
     * @param numFilterColumnEntries The number of entries in a filter column page
     * @param numResults The number of entries in a results column page
     */
    public BrowserProxy (final ControllerHost host, final CursorTrack cursorTrack, final CursorDeviceProxy cursorDevice, final int numFilterColumnEntries, final int numResults)
    {
        this.cursorTrack = cursorTrack;
        this.cursorDevice = cursorDevice;
        this.numFilterColumnEntries = numFilterColumnEntries;
        this.numResults = numResults;

        this.browser = host.createPopupBrowser ();
        this.browser.exists ().markInterested ();
        this.browser.selectedContentTypeIndex ().markInterested ();
        this.browser.selectedContentTypeName ().markInterested ();
        this.browser.contentTypeNames ().markInterested ();

        this.filterColumns = new BrowserFilterColumn []{
            this.browser.smartCollectionColumn (),
            this.browser.locationColumn (),
            this.browser.fileTypeColumn (),
            this.browser.categoryColumn (),
            this.browser.tagColumn (),
            this.browser.creatorColumn (),
            this.browser.deviceTypeColumn (),
            this.browser.deviceColumn ()
        };

        this.columnData = this.createFilterColumns (this.filterColumns.length, numFilterColumnEntries);

        this.resultsColumn = this.browser.resultsColumn ();
        this.resultsColumn.entryCount().markInterested();


        //this.result = this.resultsColumn.createCursorItem();
        //this.result.

        this.cursorResult = (CursorBrowserResultItem) this.resultsColumn.createCursorItem ();
        this.cursorResult.name ().markInterested ();

        this.resultsItemBank = (BrowserResultsItemBank) this.cursorResult.createSiblingsBank (this.numResults);
        this.resultsItemBank.scrollPosition().markInterested();
        this.resultsItemBank.canScrollBackwards().markInterested();
        this.resultsItemBank.canScrollForwards().markInterested();

        this.resultData = this.createResultData (this.numResults);
    }


    /**
     * Dis-/Enable all attributes. They are enabled by default. Use this function if values are
     * currently not needed to improve performance.
     *
     * @param enable True to enable
     */
    public void enableObservers (final boolean enable)
    {
        this.browser.exists ().setIsSubscribed (enable);
        this.browser.selectedContentTypeIndex ().setIsSubscribed (enable);
        this.browser.selectedContentTypeName ().setIsSubscribed (enable);
        this.browser.contentTypeNames ().setIsSubscribed (enable);

        this.resultsItemBank.setIsSubscribed(true);
        this.resultsItemBank.scrollPosition().setIsSubscribed(true);

        for (final BrowserColumnData column: this.columnData)
            column.enableObservers (enable);

        this.cursorResult.name ().setIsSubscribed (enable);

        for (final BrowserColumnItemData item: this.resultData)
            item.enableObservers (enable);
    }


    /**
     * Returns true of the browser displays presets.
     *
     * @return True of the browser displays presets.
     */
    public boolean isPresetContentType ()
    {
        return this.getSelectedContentTypeIndex () == 1;
    }


    /**
     * Get the index of the content type (selection tab).
     *
     * @return The index
     */
    public int getSelectedContentTypeIndex ()
    {
        return this.browser.selectedContentTypeIndex ().get ();
    }


    /**
     * Select the previous selection tab, if any.
     */
    public void previousContentType ()
    {
        this.browser.selectedContentTypeIndex ().inc (-1);
    }


    /**
     * Select the next selection tab, if any.
     */
    public void nextContentType ()
    {
        this.browser.selectedContentTypeIndex ().inc (1);
    }


    /**
     * Get the selected content type.
     *
     * @return The selected content type.
     */
    public String getSelectedContentType ()
    {
        return this.browser.selectedContentTypeName ().get ();
    }


    /**
     * Get the names of all content types (panes).
     *
     * @return The names
     */
    public String [] getContentTypeNames ()
    {
        return this.browser.contentTypeNames ().get ();
    }

    public void setContentType(int i){
        this.browser.selectedContentTypeIndex().set(i);
    }


    /**
     * Open the browser to browse for presets.
     */
    public void browseForPresets ()
    {
        this.stopBrowsing (false);
        this.cursorDevice.browseToReplaceDevice ();
    }


    /**
     * Open the browser to browse for a device which will be inserted before the current one.
     */
    public void browseToInsertBeforeDevice ()
    {
        this.stopBrowsing (false);
        if (this.cursorDevice.hasSelectedDevice ())
            this.cursorDevice.browseToInsertBeforeDevice ();
        else
            this.cursorTrack.browseToInsertAtStartOfChain ();
    }


    /**
     * Open the browser to browse for a device which will be inserted after the current one.
     */
    public void browseToInsertAfterDevice ()
    {
        this.stopBrowsing (false);

        if (this.cursorDevice.hasSelectedDevice ())
            this.cursorDevice.browseToInsertAfterDevice ();
        else
            this.cursorTrack.browseToInsertAtEndOfChain ();
    }


    /**
     * Stop browsing.
     *
     * @param commitSelection Commits the selection if true otherwise it is discarded.
     */
    public void stopBrowsing (final boolean commitSelection)
    {
        if (commitSelection)
            this.browser.commit ();
        else
            this.browser.cancel ();
    }
    /**
     * Check if the browser is active.
     *
     * @return True if active
     */
    public boolean isActive ()
    {
        return this.browser.exists ().get ();
    }

    /**
     * Reset a filter to the default (all) value.
     *
     * @param column The index of the column to resetSelector
     */
    public void resetFilterColumn (final int column)
    {
        this.columnData[column].resetFilter ();
    }

    public void selectFilterItem(final int column, int pos){
        this.columnData[column].setScrollPosition(pos);
    }

    /**
     * Get a filter column.
     *
     * @param column The index of the column to get
     * @return The column
     */
    public BrowserColumnData getFilterColumn (final int column)
    {
        return this.columnData[column];
    }

    public int getFilterColumnIndex(String name, DAWModel model){

        for(int i = 0; i < getFilterColumnNames().length; i++){
            if(getFilterColumnNames()[i].equals(name)){
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the number of filter columns.
     *
     * @return The number of filter columns
     */
    public int getFilterColumnCount ()
    {
        return this.columnData.length;
    }


    /**
     * Get the names of the filter columns.
     *
     * @return The names of the filter columns
     */
    public String [] getFilterColumnNames ()
    {
        final String [] names = new String [this.columnData.length];
        for (int i = 0; i < this.columnData.length; i++)
            names[i] = this.columnData[i].getName ();
        return names;
    }


    /**
     * Get the result columns items.
     *
     * @return The item data
     */
    public BrowserColumnItemData [] getResultColumnItems ()
    {
        return this.resultData;
    }

    /**
     * Select the previous item of a filter column.
     *
     * @param columnIndex The index of the column
     */
    public void selectPreviousFilterItem (final int columnIndex)
    {
        this.columnData[columnIndex].selectPreviousItem ();
    }


    /**
     * Select the next item of a filter column.
     *
     * @param columnIndex The index of the column
     */
    public void selectNextFilterItem (final int columnIndex)
    {
        this.columnData[columnIndex].selectNextItem ();
    }


    /**
     * Select the previous item page of a filter column.
     *
     * @param columnIndex The index of the column
     */
    public void previousFilterItemPage (final int columnIndex)
    {
        this.columnData[columnIndex].scrollItemPageUp ();

    }


    /**
     * Select the next item page of a filter column.
     *
     * @param columnIndex The index of the column
     */
    public void nextFilterItemPage (final int columnIndex)
    {
        this.columnData[columnIndex].scrollItemPageDown ();
    }


    /**
     * Get the index of the select filter item of a column.
     *
     * @param columnIndex The index of the column
     * @return The index of the item
     */
    public int getSelectedFilterItemIndex (final int columnIndex)
    {
        return this.columnData[columnIndex].getCursorIndex ();
    }

    /**
     * Set the index of the select filter item of a column.
     *
     * @param columnIndex The index of the column
     * @param index The index of the item
     */
    public void setSelectedFilterItemIndex (final int columnIndex, final int index)
    {
        this.columnData[columnIndex].setCursorIndex (index);
    }

    /**
     * Select the previous results item.
     */
    public void selectPreviousResult ()
    {
        this.cursorResult.selectPrevious ();
    }

    /**
     * Select the next results item.
     */
    public void selectNextResult ()
    {
        this.cursorResult.selectNext ();
    }

    public void selectFirstResult() {
        this.cursorResult.selectFirst();
    }

    public void selectCurrentResult(){
        this.cursorResult.isSelected().set(true);
    }

    /**
     * Get the selected result item.
     *
     * @return The result
     */
    public String getSelectedResult ()
    {
        return this.cursorResult.name ().get ();
    }

    /**
     * Get the index of the selected result item.
     *
     * @return The index of the result
     */
    public int getSelectedResultIndex ()
    {
        for (int i = 0; i < this.numResults; i++)
        {
            if (this.resultData[i].isSelected ())
                return i;
        }
        return -1;
    }

    /**
     * Select the previous result page.
     */
    public void previousResultPage ()
    {
        this.resultsItemBank.scrollPageBackwards ();

    }

    public void setResultsScrollPosition(int i){
        this.resultsItemBank.scrollPosition().set(i);
    }

    public void selectResultAt(int i){
        this.resultsItemBank.getItemAt(i).isSelected().set(true);
    }

    /**
     * Select the next result page.
     */
    public void nextResultPage ()
    {
        this.resultsItemBank.scrollPageForwards ();
    }

    public BooleanValue hasNextResultPage(){
        return this.resultsItemBank.canScrollForwards();
    }

    public BooleanValue hasPreviousResultPage(){
        return this.resultsItemBank.canScrollBackwards();
    }

    public int getResultsScrollPosition(){
        return this.resultsItemBank.scrollPosition().get();
    }


    private BrowserColumnData [] createFilterColumns (final int count, final int numFilterColumnEntries)
    {
        final BrowserColumnData [] columns = new BrowserColumnData [count];
        for (int i = 0; i < count; i++)
            columns[i] = new BrowserColumnData (this.filterColumns[i], i, numFilterColumnEntries);
        return columns;
    }


    private BrowserColumnItemData [] createResultData (final int count)
    {
        final BrowserColumnItemData [] items = new BrowserColumnItemData [count];
        for (int i = 0; i < count; i++)
            items[i] = new BrowserColumnItemData (this.resultsItemBank.getItemAt (i), i);
        return items;
    }


    /**
     * Get the number of results to display on a page.
     *
     * @return The number oif results.
     */
    public int getNumResultsPerPage()
    {
        return this.numResults;
    }

    public int getNumTotalResults(){
        return this.resultsColumn.entryCount().get();
    }

    public IntegerValue getResultEntryCount() { return this.resultsColumn.entryCount(); }

    /**
     * Get the number of filter items to display on a page.
     *
     * @return The number oif results.
     */
    public int getNumFilterColumnEntries ()
    {
        return this.numFilterColumnEntries;
    }

    public void addActiveObserver(BooleanValueChangedCallback callback){
        this.browser.exists().addValueObserver(callback);
    }

}