/**
 *  '$RCSfile: CustomTablePopupListener.java,v $'
 *    Purpose: A widget that displays data of multiple columns from multiple tables
 *						 in a columnar fashion and allows the user to select multiple columns
 * 						 using checkboxes
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import javax.swing.JDialog;

/** 
	Interface CustomTablePopupListener
	
	This interface is used to define listeners for popup events for the CustomTable.
	Popup events are triggered by mouse clicks on the table headers. The listeners are 
	responsible to define a dialog that is to presented as a popup and a possible
	String that should be displayed in the table header.
	
*/

public interface CustomTablePopupListener {
	
	/**
		gets the dialog that is be popped up when the user clicks on a table header. The
		implementor is responsible to ensure that the dialog is removed after the 
		necessary operations are performed.
		
		@return  the dialog window to be popped up
	*/
	
	public JDialog getPopupDialog();
	
	/**
		gets an optional String to be displayed in the table header. This method is invoked 
		by the CustomTable after the dialog is popped up. If no String needs to be displayed, 
		a null should be returned.
		
		@return  the String to be displayed in the table header. null is returned if no string
							is to be displayed.
	*/
	
	public String getDisplayString();
	
	
}
