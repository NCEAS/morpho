/**
 *  '$RCSfile: TreeEditorCorrectionController.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Jing Tao
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-05 23:26:37 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;

/**
 * This class represents a Loader which will load an incomplete eml document to 
 * either New Package wizard or text import wizard.
 * @author tao
 *
 */
public class IncompleteDocumentLoader 
{
	private AbstractDataPackage dataPackage = null;
	
	/**
	 * Constructs a IncompleteDocumentLoader with a AbstractDataPackage containing 
	 * meta data information
	 * @param dataPackage
	 */
	public IncompleteDocumentLoader(AbstractDataPackage dataPackage)
	{
		this.dataPackage = dataPackage;
	}
	
	/**
	 * Loads the incomplete AbstractDataPackage into new package wizard or text import wizard
	 */
	public void load()
	{
		
	}
	
	/*
	 * Loads the incomplete AbstractDataPackage into new package wizard
	 */
	private void loadToNewPackageWizard()
	{
		
	}
	
	/*
	 * Loads the incomplete AbstractDataPackage into text import wizard
	 */
	private void loadToTextImportWizard()
	{
		
	}

}
