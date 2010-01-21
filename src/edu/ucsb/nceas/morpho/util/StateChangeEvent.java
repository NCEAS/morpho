/**
 *  '$RCSfile: StateChangeEvent.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-01-29 18:43:48 $'
 * '$Revision: 1.12 $'
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ucsb.nceas.morpho.util;

import javax.swing.event.ChangeEvent;

import java.awt.Component;

/**
 * An event that represents a state change in the application, showing
 * both the state that changed and the source of the state change.
 */
public class StateChangeEvent extends ChangeEvent
{
    
//////////////////////////////////////////////////////////////////////////////// 
//////////////////////////////////////////////////////////////////////////////// 
//      PUBLIC STATIC STATE CHANGE EVENT TYPE CONSTANTS
//////////////////////////////////////////////////////////////////////////////// 
//////////////////////////////////////////////////////////////////////////////// 




    /**
     *  Public constant to denote that a data table column has been selected 
     */
    public static String SELECT_DATATABLE_COLUMN 
        = "selectDataTableColumn";

                                      
                                      
    /**
     *  Public constant to denote that a datapackage CONTAINING ONE OR MORE 
     *  ENTITIES has been opened
     */
    public static String CREATE_DATAPACKAGE_FRAME 
        = "createDataPackageFrame";
 
 
    /**
     *  Public constant to denote that a datapackage CONTAINING NO ENTITIES OR 
     *  ASCII DATA has been opened
     */
    public static String CREATE_NOENTITY_DATAPACKAGE_FRAME 
        = "createNoEntityDataPackageFrame";
   
   /**
     *  Public constant to denote that a datapackage CONTAINING ENTITIES 
     *  has been opened
     */
    public static String CREATE_ENTITY_DATAPACKAGE_FRAME 
        = "createEntityDataPackageFrame";

    /**
     *  Public constant to denote that a datapackage CONTAINING ONE OR MORE 
     *  ASCII ENTITIES has been opened
     */
    public static String CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME 
        = "createEditableEntityDataPackageFrame";
     
    /**
     *  Public constant to denote that a datapackage has been opened CONTAINING 
     *  ONE OR MORE ENTITIES THAT ARE NOT EDITABLE (e.g. binary data such as 
     *  images etc, or possibly a package that has entity metadata but the 
     *  corresponding data cannot be read) 
     */
    public static String CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME 
        = "createNonEditableEntityDataPackageFrame";

    /**
     *  Public constant to denote that a datapackage has been opened, but that 
     *  no previous versions of the selected package are available 
     */
    public static String CREATE_DATAPACKAGE_FRAME_NO_VERSIONS
        = "createEntityDataPackageFrameNoPrevVersionAvailable";
                                   
    /**
     *  Public constant to denote that a datapackage has been opened, and that 
     *  previous versions of the selected package *are* available 
     */
    public static String CREATE_DATAPACKAGE_FRAME_VERSIONS
        = "createEntityDataPackageFrameVersionAvailable";
 
    /**
     *  Public constant to denote that a datapackage has been opened, and that 
     *  the local and network versions of the package are *not* in sync 
     */
    public static String CREATE_DATAPACKAGE_FRAME_UNSYNCHRONIZED 
        = "unsynchronizedCreateDataPackageFrame";

    /**
     *  Public constant to denote that a datapackage has been opened, and that 
     *  the local and network versions of the package *are* in sync 
     */
    public static String CREATE_DATAPACKAGE_FRAME_SYNCHRONIZED 
        = "synchronizedCreateDataPackageFrame";


          
          
    /**
     *  Public constant to denote that a search result frame has been opened
     */
    public static String CREATE_SEARCH_RESULT_FRAME 
        = "createRearchResultFrame";  

    /**
     *  Public constant to denote that an entry in a search result frame has 
     *  been selected, 
     */
    public static String SEARCH_RESULT_SELECTED 
        = "searchResultSelected";
        
   /**
     *  Public constant to denote that no entry in a search result frame has 
     *  been selected, 
     */
    public static String SEARCH_RESULT_NONSELECTED 
        = "searchResultNonSelected";

    /**
     *  Public constant to denote that an entry in a search result frame has 
     *  been selected, and that the local and network versions of the selected 
     *  package are *not* in sync 
     */
    public static String SEARCH_RESULT_SELECTED_UNSYNCHRONIZED 
        = "unsynchronizedSearchResultSelected";
 
    /**
     *  Public constant to denote that an entry in a search result frame has 
     *  been selected, and that the local and network versions of the selected 
     *  package *are* in sync 
     */
    public static String SEARCH_RESULT_SELECTED_SYNCHRONIZED 
        = "synchronizedSearchResultSelected";

    /**
     *  Public constant to denote that an entry in a search result frame has 
     *  been selected, and that no previous versions of the selected package 
     *  are available 
     */
    public static String SEARCH_RESULT_SELECTED_NO_VERSIONS 
        = "searchResultSelectedNoPrevVersionAvailable";

    /**
     *  Public constant to denote that an entry in a search result frame has 
     *  been selected, and that previous versions of the selected package *are*
     *  available 
     */
    public static String SEARCH_RESULT_SELECTED_VERSIONS
        = "searchResultSelectedVersionAvailable";
 

    /**
     *  Public constant to denote that a data viewer has changed
     */
    public static String SELECT_DATA_VIEWER
        = "selectDataViewer";
 

    /**
     *  Public constant to denote that the "Hide" button has been pressed in the 
     *  MetaData Viewer
     */
    public static String METAVIEWER_HIDE_BUTTON_PRESSED
        = "metaViewerHideButtonPressed";

    /**
     *  Public constant to denote that a navigation event has been triggered in 
     *  the MetaData Viewer.  This could be any navigation event, such as 
     *  clicking on a URL or traversing the History using the Back button etc.
     */
    public static String METAVIEWER_NAVIGATION
        = "metaViewerNavigation";
                                      
    /**
     *  Public constant to denote that user has requested the previous document 
     *  from History in the MetaData Viewer.  
     */
    public static String METAVIEWER_HISTORY_BACK
        = "metaViewerHistoryBack";

    /**
     *  Public constant to denote that user has issued a command to edit the 
     *  metadata in the MetaData Viewer.  
     */
    public static String METAVIEWER_BEGIN_EDIT
        = "metaViewerBeginEdit";

   /**
     *  Public constant to denote that the Paste command has
     *  data in the System clipboard to 'Paste'. This should
     *  enable the Paste menu item
     */
     public static String CLIPBOARD_HAS_DATA_TO_PASTE
        = "clipboardHasDataToPaste";

    /**
     *  Public constant to denote that the Paste command has
     *  no data in the System clipboard to 'Paste'. This should
     *  disable the Paste menu item
     */
    public static String CLIPBOARD_HAS_NO_DATA_TO_PASTE
        = "clipboardHasNoDataToPaste";
    
    /**
     *  Public constant to denote that the DOM Editor has finished the current
     *  editing task
     */
    public static String DOM_EDITOR_FINISHED_EDITING
        = "domEditorFinishedEditing";
    
    /**
     *  Public constant to denote that the Access List
     *  has been revised
     */
    public static String ACCESS_LIST_MODIFIED
        = "accessListModified";
    
    /**
     *  Public constant to denote that the Save has occurred  
     */
    public static String SAVE_DATAPACKAGE
        = "saveDatapackage";
    
    /**
     * Public constant to denote that an incomplete data package has been chosen
     */
    public static String CHOOSE_INCOMPLETE_DATAPACKAGE = "chooseIncompleteDataPackage";
    
    /**
     * Public constant to denote that a complete data package has been chosen
     */
    public static String CHOOSE_COMPLETE_DATAPACKAGE = "chooseCompleteDataPackage";
        
/////////////////////////////////////////////s/////////////////////////////////// 
//////////////////////////////////////////////////////////////////////////////// 

    private String changedState;

    /**
     * Construct a new StateChange event.
     *
     * @param source  if the source of the state change is a 
     *                <code>java.awt.Component</code>, then a reference to that 
     *                Component should be passed.  However, if the object whose 
     *                state has changed is *not* an instance of 
     *                <code>java.awt.Component</code>, pass a reference to the 
     *                closest <code>java.awt.Component</code> (which could be a
     *                <code>java.awt.Container</code> in which the object is 
     *                embedded.
     * @param changedState the label for the state that has changed
     */
    public StateChangeEvent(Component source, String changedState)
    {
        super(source);
        this.changedState = changedState;
    }

    /**
     * Get the value of the changedState for this event.
     *
     * @return the String value for the changed state
     */
    public String getChangedState()
    {
        return changedState;
    }
}
