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

import java.util.Hashtable;

import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.IncompleteDocSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XPathUIPageMapping;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * This class represents a Loader which will load an incomplete eml document to 
 * either New Package wizard or text import wizard.
 * @author tao
 *
 */
public class IncompleteDocumentLoader 
{
	private AbstractDataPackage dataPackage = null;
	private String incompletionStatus = null;
	private Hashtable wizardPageName = new Hashtable();

	/**
	 * Constructs a IncompleteDocumentLoader with a AbstractDataPackage containing 
	 * meta data information
	 * @param dataPackage
	 * @param incompletionStatus 
	 */
	public IncompleteDocumentLoader(AbstractDataPackage dataPackage, String incompletionStatus)
	{
		this.dataPackage = dataPackage;
		this.incompletionStatus = incompletionStatus;
	}
	
	/**
	 * Loads the incomplete AbstractDataPackage into new package wizard or text import wizard
	 */
	public void load()
	{
		if(incompletionStatus == null)
		{
			Log.debug(5, "Morpho couldn't open the package since the incompletion status is null");
		}
		else if (incompletionStatus.equals(AbstractDataPackage.INCOMPLETE_NEWPACKAGEWIZARD))
		{
			//Log.debug(5, "new package wizard");
			loadToNewPackageWizard();
		}
		else if(incompletionStatus.equals(AbstractDataPackage.INCOMPLETE_TEXTIMPORTWIZARD))
		{
			//Log.debug(5, "In text imorpt wizard");
			loadToTextImportWizard();
		}
		else
		{
			Log.debug(5, "Morpho couldn't understand the incompletion status of the package "+incompletionStatus);
		}
	}
	
	/*
	 * Loads the incomplete AbstractDataPackage into new package wizard
	 */
	private void loadToNewPackageWizard()
	{
		try
		{
			DataPackageWizardPlugin plugin = new DataPackageWizardPlugin();
		    plugin.startPackageWizard(
		          new DataPackageWizardListener() {

		        public void wizardComplete(Node newDOM, String autoSavedID) {

		          Log.debug(30,
		              "Wizard complete - Will now create an AbstractDataPackage..");

		          AbstractDataPackage adp = DataPackageFactory.getDataPackage(newDOM);
		          Log.debug(30, "AbstractDataPackage complete");
		          adp.setAccessionNumber("temporary.1.1");
		          adp.setAutoSavedID(autoSavedID);

		          try {
		            ServiceController services = ServiceController.getInstance();
		            ServiceProvider provider =
		                services.getServiceProvider(DataPackageInterface.class);
		            DataPackageInterface dataPackage = (DataPackageInterface)provider;
		            dataPackage.openNewDataPackage(adp, null);

		          } catch (ServiceNotHandledException snhe) {

		            Log.debug(6, snhe.getMessage());
		          }
		          Log.debug(45, "\n\n********** Wizard finished: DOM:");
		          Log.debug(45, XMLUtilities.getDOMTreeAsString(newDOM, false));
		        }


		        public void wizardCanceled() {

		          Log.debug(45, "\n\n********** Wizard canceled!");
		        }
		      });

		    } catch (Throwable t) {

		      Log.debug(5, "** ERROR: Unable to start wizard!");
		      t.printStackTrace();
		    }
	}
	
	/*
	 * Loads the incomplete AbstractDataPackage into text import wizard
	 */
	private void loadToTextImportWizard()
	{
		
	}
	
	/*
	 * Transform the array to a hashtable. This is for efficiency reason.
	 * The key of this hastable is the page class name and value is XPathUIPageMapping object.
	 */
	private void transformMappingToHashtable()
	{
		XPathUIPageMapping[] mappingList = readXpathUIMappingInfo();
		if(mappingList != null)
		{
			int size = mappingList.length;
			for(int i=0; i<size; i++)
			{
				XPathUIPageMapping map = mappingList[i];
				String className = map.getWizardPageClassName();
				wizardPageName.put(className, map);
			}
		}
	}
	
	/*
	 * Read xpath-UIpage mapping information
	 */
	private XPathUIPageMapping[] readXpathUIMappingInfo()
	{
		XPathUIPageMapping[] mappingList =null;
		XpathUIPageMappingReader reader = new XpathUIPageMappingReader(CorrectionWizardController.MAPPINGFILEPATH);
	    mappingList   = reader.getXPathUIPageMappingList();
	    return mappingList;
	}

}
