/**
 *  '$RCSfile: StateChangeEvent.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: cjones $'
 *     '$Date: 2002-09-26 01:30:07 $'
 * '$Revision: 1.1.4.1 $'
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
    // An event name for selecting a data table column
    public static String SELECTDATATABLECOLUMN = "selectDataTableColumn";
    
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
