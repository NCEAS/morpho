/**
 *  '$RCSfile: StateChangeEvent.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-27 23:08:03 $'
 * '$Revision: 1.5 $'
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
    public static String CREATE_ENTITY_DATAPACKAGE_FRAME 
        = "createEntityDataPackageFrame";
 
    /**
     *  Public constant to denote that a datapackage CONTAINING NO ENTITIES OR 
     *  DATA has been opened
     */
    public static String CREATE_NOENTITY_DATAPACKAGE_FRAME 
        = "createNoEntityDataPackageFrame";

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
    public static String CREATE_DATAPACKAGE_FRAME_UNSYNCRONIZED 
        = "unsynchronizedCreateDataPackageFrame";

    /**
     *  Public constant to denote that a datapackage has been opened, and that 
     *  the local and network versions of the package *are* in sync 
     */
    public static String CREATE_DATAPACKAGE_FRAME_SYNCRONIZED 
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
     *  Public constant to denote that an entry in a search result frame has 
     *  been selected, and that the local and network versions of the selected 
     *  package are *not* in sync 
     */
    public static String SEARCH_RESULT_SELECTED_UNSYNCRONIZED 
        = "unsynchronizedSearchResultSelected";
 
    /**
     *  Public constant to denote that an entry in a search result frame has 
     *  been selected, and that the local and network versions of the selected 
     *  package *are* in sync 
     */
    public static String SEARCH_RESULT_SELECTED_SYNCRONIZED 
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
 
    
//////////////////////////////////////////////////////////////////////////////// 
//////////////////////////////////////////////////////////////////////////////// 

    private String changedState;

    /**
     * Construct a new StateChange event.
     *
     * @param source the object whose state has changed
     * @param changedState the label for the state that has changed
     */
    public StateChangeEvent(Object source, String changedState)
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
