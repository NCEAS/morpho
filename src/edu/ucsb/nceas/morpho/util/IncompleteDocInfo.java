/**
 *  '$RCSfile: IOUtil.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-12-16 22:13:22 $'
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

import java.util.Vector;

/**
 * This class represents the information stored in the additional metadata part.
 * @author tao
 *
 */
public class IncompleteDocInfo 
{
	private String status;
	private Vector wizardPageClassNameList = new Vector(); //stores the class name list
	
	/**
	 * Default constructor
	 */
	public IncompleteDocInfo(String status)
	{
		this.status = status;
	}
	
	/**
	 * Sets the list of wizard page name 
	 * @param wizardPageClassNameList
	 */
	public void setWizardPageClassNameList(Vector wizardPageClassNameList)
	{
		this.wizardPageClassNameList = wizardPageClassNameList;
	}
	
	/**
	 * Gets the list of wizard page name
	 * @return
	 */
	public Vector getWizardPageClassNameList()
	{
		return this.wizardPageClassNameList;
	}
	
	/**
	 * Gets the status of the incompletion
	 * @return
	 */
	public String getStatus()
	{
		return status;
	}

}
