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
import java.util.Vector;

import javax.swing.JOptionPane;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.IncompleteDocInfo;
import edu.ucsb.nceas.morpho.util.IncompleteDocSettings;
import edu.ucsb.nceas.morpho.util.LoadDataPath;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.ModifyingPageDataInfo;
import edu.ucsb.nceas.morpho.util.WizardPageInfo;
import edu.ucsb.nceas.morpho.util.XPathUIPageMapping;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * This class represents a Loader which will load an incomplete eml document to 
 * either New Package wizard or text import wizard.
 * @author tao
 *
 */
public class IncompleteDocumentLoader implements  DataPackageWizardListener
{
	private AbstractDataPackage dataPackage = null;
	private IncompleteDocInfo incompleteDocInfo = null;
	private String incompletionStatus = null;
	private Hashtable wizardPageName = new Hashtable();
	private XPathUIPageMapping[] mappingList = null;
	
	/**
	 * Default constructor
	 */
	public IncompleteDocumentLoader()
	{
		
	}

	/**
	 * Constructs a IncompleteDocumentLoader with a AbstractDataPackage containing 
	 * meta data information
	 * @param dataPackage
	 * @param incompleteInfo
	 */
	public IncompleteDocumentLoader(AbstractDataPackage dataPackage, IncompleteDocInfo incompleteDocInfo)
	{
		this.dataPackage = dataPackage;
		this.incompleteDocInfo = incompleteDocInfo;
		if(this.incompleteDocInfo != null)
		{
			this.incompletionStatus = incompleteDocInfo.getStatus();
		}
		readXpathUIMappingInfo();
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
		else if (incompletionStatus.equals(IncompleteDocSettings.INCOMPLETE_PACKAGE_WIZARD))
		{
			//Log.debug(5, "new package wizard");
			loadToNewPackageWizard();
		}
		else if(incompletionStatus.equals(IncompleteDocSettings.INCOMPLETE_ENTITY_WIZARD))
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
		boolean showPageCount = true;
		  String currentPageId = WizardSettings.PACKAGE_WIZ_FIRST_PAGE_ID;
		  WizardContainerFrame dpWiz = new WizardContainerFrame();
		  WizardPageInfo [] classNameFromIncompleteDoc = incompleteDocInfo.getWizardPageClassInfoList();
		  //Vector parameters = null;
		  //Go through every page from incomplete doc info
		  if(classNameFromIncompleteDoc != null)
		  {
			  int size = classNameFromIncompleteDoc.length;
			  AbstractUIPage page = null;
			  for(int i=0; i<size;  i++)
			  {
				  WizardPageInfo pageClassInfo = classNameFromIncompleteDoc[i];
				  if(pageClassInfo != null)
				  {
				  String classNamePlusParameter ="";
				  String className = pageClassInfo.getClassName();
				  classNamePlusParameter = classNamePlusParameter+className;
				  Vector parameters = pageClassInfo.getParameters();
				  if(parameters != null && !parameters.isEmpty())
				  {
					  for(int k=0; k<parameters.size(); k++)
					  {
						  String param = (String)parameters.elementAt(k);
						  classNamePlusParameter = classNamePlusParameter +param;
					  }
					  
				  }
				  XPathUIPageMapping map = (XPathUIPageMapping)wizardPageName.get(classNamePlusParameter.trim());
				  if (map != null)
				  {
					  //loading exist data into UIPage
					  String path = null;
					  try
					  {
						  Log.debug(30, "There is map for classNamePlusParamer ~~~~~~~~~~~~~~"+classNamePlusParameter+ " so we create an page with data (if have)");
					      Log.debug(30, "the className from metadata is "+className);
						  page= WizardUtil.getUIPage(className, map, dpWiz, dataPackage, null );
					  }
					  catch(Exception e)
					  {
						  Log.debug(20, "Couldn't create the page with className "+className+" "+e.getMessage());
						  page = null;
					  }
					 
				  }
				  //generate empty UIPage if page isnull or map isnull
                  if (map == null || page ==null)
                  {
                	  
					  // those pages are likely introduction ....
                	  if(map != null)
                	  {
                		  Log.debug(30, "There is no data for className --------------------"+className+ " so we just initilize an empty page");
					     page = WizardUtil.createAbstractUIpageObject(className, dpWiz, map.getWizardPageClassParameters() );
                	  }
                	  else
                	  {
                		  Log.debug(30, "There is no map for classNamePlusParameter --------------------"+classNamePlusParameter+ " so we just initilize an empty page for class "+className);
                		  page = WizardUtil.createAbstractUIpageObject(className, dpWiz, parameters );
                	  }
                  }
				  }
                  
                  //add page into stack of wizard frame
                  if(page != null)
                  {
                	  dpWiz.addPageToStack(page);
                  }
				  // set the current page to the last page
                  if((i == (size-1)) && page != null)
                  {
                	  
                	  currentPageId = page.getNextPageID();
                	  Log.debug(20, "in getting current id!!!!!!!!!!!!!!!"+currentPageId);
                  }
			  }
		  }
		  Log.debug(25, "The current page id is "+currentPageId);
		  IncompleteDocumentLoader dataPackageWizardListener = new IncompleteDocumentLoader();
		  dpWiz.setDataPackageWizardListener(dataPackageWizardListener);
		  dpWiz.setBounds(
		                  WizardSettings.WIZARD_X_COORD, WizardSettings.WIZARD_Y_COORD,
		                  WizardSettings.WIZARD_WIDTH,   WizardSettings.WIZARD_HEIGHT );
		  dpWiz.setCurrentPage(currentPageId);
		  dpWiz.setShowPageCountdown(showPageCount);
		  dpWiz.setTitle(DataPackageWizardPlugin.NEWPACKAGEWIZARDFRAMETITLE);
		  dpWiz.setVisible(true);
	    
	}
	
	/*
	 * Loads the incomplete AbstractDataPackage into text import wizard
	 */
	private void loadToTextImportWizard()
	{
		
	}
	
	/*
	 * Read xpath-UIpage mapping information
	 */
	private void readXpathUIMappingInfo()
	{
		XpathUIPageMappingReader reader = new XpathUIPageMappingReader(CorrectionWizardController.MAPPINGFILEPATH);
	    mappingList   = reader.getXPathUIPageMappingList();
	    wizardPageName = reader.getClassNamePlusParameterMapping();	 
	}
	
	
	/**
	 * Methods inherits from DataPackageWizardListener
	 */
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

	/**
	 * Methods inherits from DataPackageWizardListener
	 */
      public void wizardCanceled() {

        Log.debug(45, "\n\n********** Wizard canceled!");
      }
      
      
  

}
