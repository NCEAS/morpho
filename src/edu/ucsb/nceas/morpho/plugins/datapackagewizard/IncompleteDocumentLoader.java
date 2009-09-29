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
		boolean showPageCount = true;
		  String currentPageId = WizardSettings.PACKAGE_WIZ_FIRST_PAGE_ID;
		  WizardContainerFrame dpWiz = new WizardContainerFrame();
		  Vector classNameFromIncompleteDoc = incompleteDocInfo.getWizardPageClassNameList();
		  Vector parameters = null;
		  //Go through every page from incomplete doc info
		  if(classNameFromIncompleteDoc != null && !classNameFromIncompleteDoc.isEmpty())
		  {
			  int size = classNameFromIncompleteDoc.size();
			  for(int i=0; i<size;  i++)
			  {
				  String className = (String)classNameFromIncompleteDoc.elementAt(i);
				  XPathUIPageMapping map = (XPathUIPageMapping)wizardPageName.get(className);
				  if (map == null)
				  {
					  Log.debug(30, "There is no map for className --------------------"+className+ " so we just initilize an empty page");
					  // those pages are likely introduction ....
					  AbstractUIPage page = WizardUtil.createAbstractUIpageObject(className, dpWiz, parameters);
					  dpWiz.addPageToStack(page);
				  }
				  else
				  {
					  
				  }
			  }
		  }
		  
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
	    wizardPageName = reader.getClassNameMapping();	 
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
      
      
      /*
  	 * Returns a UI page for given xml path. If no page can be found, null will be returned.
  	 * The page also contains existed data.
  	 */
  	private AbstractUIPage getUIPage(String className, XPathUIPageMapping mapping, WizardContainerFrame dpWiz, int position ) throws Exception
  	{
  		AbstractUIPage page = null;
  		boolean mapDataFit = false;
  		
  		
		if(mapping != null)
		{
			className = mapping.getWizardPageClassName();
			Log.debug(45, "get the className from mapping "+className);
			if(className != null)
			{
				OrderedMap xpathMap = null;
				page = WizardUtil.createAbstractUIpageObject(className, dpWiz, mapping.getWizardPageClassParameters());
				page.setXPathUIPageMapping(mapping);
				//load data into the page
				//first we need to check if we should load data from root path.
			    Vector infoList = mapping.getModifyingPageDataInfoList();
				NodeList nodeList = null;
				String settingPageDataPath = "";
				/*if (infoList != null && infoList.size() < -1)
				{	
					ModifyingPageDataInfo info = (ModifyingPageDataInfo)infoList.elementAt(0);
					settingPageDataPath = info.getPathForSettingPageData();
					//page.setXPathRoot(node);
					if (page instanceof AttributePage)
					{
						int entityIndex = getDataTableIndex(path);
						int attributeIndex = getAttributeIndex(path);
						Vector loadExistingDataPathList = info.getLoadExistingDataPath();
						LoadDataPath entityPath = (LoadDataPath)loadExistingDataPathList.elementAt(0);
						entityPath.setPosition(entityIndex);
						LoadDataPath attributePath = (LoadDataPath)loadExistingDataPathList.elementAt(1);
						attributePath.setPosition(attributeIndex);							
						nodeList = XMLUtilities.getNodeListWithXPath(dataPackage.getMetadataNode(),entityPath.getPath());
						Node entityNode = nodeList.item(entityIndex);
						nodeList = XMLUtilities.getNodeListWithXPath(entityNode, attributePath.getPath());
						Node node = nodeList.item(attributeIndex);
						xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node,info.getPathForCreatingOrderedMap());							

					}
					if(page instanceof Entity)
					{
						nodeList = XMLUtilities.getNodeListWithXPath(dataPackage.getMetadataNode(), mapping.getRoot());	
						Node node = nodeList.item(0);
						xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node, "");
						int entityIndex = getDataTableIndex(path);
						//page.addNodeIndex(entityIndex);
					}
					else
					{
						Log.debug(45, "start to process load data process which has info list size 1");
						Vector loadExistingDataPathList = info.getLoadExistingDataPath();
						int position = getLastPredicate(path);
						if (loadExistingDataPathList != null)
						{
							Node node = null;
							for(int k=0; k<loadExistingDataPathList.size(); k++)
							{
							  LoadDataPath loadPath = (LoadDataPath)loadExistingDataPathList.elementAt(k);
							  // store the position information
							  loadPath.setPosition(position);
							  String xpath = loadPath.getPath();
							  nodeList = XMLUtilities.getNodeListWithXPath(dataPackage.getMetadataNode(), xpath);	
							  node = nodeList.item(position);
							}
							Log.debug(45, "before getting ordered map from subtree");
						    xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node, info.getPathForCreatingOrderedMap());
							  
						}
					}
				}*/
				if (infoList != null && infoList.size() > 0)
				{
					//like General page
					Log.debug(45, "start to process load data process which has info list size more than 0");
					Vector list = mapping.getModifyingPageDataInfoList();
					boolean firstTime = true;
					for (int i=0; i<list.size(); i++)
					{
						ModifyingPageDataInfo info =(ModifyingPageDataInfo)list.elementAt(i);
						settingPageDataPath = info.getPathForSettingPageData();
						Vector loadDataPathList = info.getLoadExistingDataPath();
						if (loadDataPathList != null)
						{
							Node node = dataPackage.getMetadataNode();
							//If loadDataPathList has mutiple xpath, the second xpath is kid of the first one.
							//like : <loadExistDataPath>/eml:eml/dataset</loadExistDataPath>
				            //        <loadExistDataPath>./title</loadExistDataPath>
							if(node != null)
							{
								for(int j=0; j<loadDataPathList.size(); j++)
								{
									LoadDataPath pathObj = (LoadDataPath)loadDataPathList.elementAt(j);
									String xPath = pathObj.getPath();
									//String lastElementName = getLastElementName(xPath);
									//int position = getGivenStringIndexAtXPath(lastElementName, path);								
									//System.out.println("==========the xpath is "+xPath);
									Log.debug(46, "Before getting the node list for path"+xPath);
									nodeList = XMLUtilities.getNodeListWithXPath(node, xPath);
									Log.debug(46, "After getting the node list for path"+xPath);
									//reset node
									if(nodeList != null)
									{
										node = nodeList.item(position);
										Log.debug(46, "Getting the node for path"+xPath+" at position "+position+ " "+node);
										pathObj.setPosition(position);
									}
									else
									{
										node = null;
									}
								}
								
							}
							if(firstTime && node != null)
							{
							  Log.debug(46, "Before First time to create xPathMap with path for creating ordered map "+info.getPathForCreatingOrderedMap());
							  xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node, info.getPathForCreatingOrderedMap());
							  Log.debug(46, "After First time to create xPathMap");
							  firstTime = false;
							  //we set first child as the root node for not loading data from root path directly
							  //page.setXPathRoot(node);
							}
							else if (node != null)
							{
								Log.debug(46, "Before second or more time to create xPathMap");
								xpathMap.putAll(XMLUtilities.getDOMTreeAsXPathMap(node, info.getPathForCreatingOrderedMap()));
								Log.debug(46, "After second or more time to create xPathMap");
							}
						}
						
					}
					
				}	
				else
				{
					page = null;
				}
				Log.debug(46, "the xmpath map is "+xpathMap.toString());
				if (page != null)
				{
				   mapDataFit = page.setPageData(xpathMap, settingPageDataPath);
				}
			}
	
		}
  		
  		Log.debug(46, "The map data fit value is "+mapDataFit);
  		//The map has more data that our page can handle. so set page to null and let tree editor to handle it.
  		if(!mapDataFit)
  		{
  			page = null;
  		}
  		return page;
  	}

}
