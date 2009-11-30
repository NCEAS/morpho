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

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributeSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.TextImportAttribute;
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
public class IncompleteDocumentLoader 
{
  private AbstractDataPackage dataPackage = null;
  private IncompleteDocInfo incompleteDocInfo = null;
  private String incompletionStatus = null;
  private Hashtable wizardPageName = new Hashtable();
  private XPathUIPageMapping[] mappingList = null;
	
	

  /**
   * Constructs a IncompleteDocumentLoader with an AbstractDataPackage object
   * @param dataPackage
  */
  public IncompleteDocumentLoader(AbstractDataPackage dataPackage)
  {
    if(dataPackage == null)
    {
      Log.debug(5, "Morpho couldn't open the incomplete document since the document is null");
      return;
    }
    this.dataPackage = dataPackage;
    this.dataPackage.setLocation(AbstractDataPackage.TEMPLOCATION);
    init();
    readXpathUIMappingInfo();
  }
  
  /*
   * Initializes incompletion status and IncompleteDocInfo object
   */
  private void init()
  {
    this.incompletionStatus = dataPackage.getCompletionStatus();
    WizardPageInfo [] classNameList = null;
    int index =-1;
    Log.debug(30, "The status of incomplete document is "+incompletionStatus+" in DataPackagePlugin.openIncompleteDataPackage");
    if( incompletionStatus != null && incompletionStatus.equals(IncompleteDocSettings.INCOMPLETE_PACKAGE_WIZARD))
    {
      classNameList = dataPackage.getIncompletePacakgeWizardPageInfoList();
    }
    else if( incompletionStatus != null && incompletionStatus.equals(IncompleteDocSettings.INCOMPLETE_ENTITY_WIZARD))
    {
      classNameList = dataPackage.getIncompleteEntityWizardPageInfoList();
      try
      {
        dataPackage.readImportAttributeInfoFromIncompleteDocInEntityWizard();
      }
      catch(Exception e)
      {
        Log.debug(5, "Couldn't read import attribute information in incomplete document "+e.getMessage());
        return;
      }
      index = dataPackage.getEntityIndexInIncompleteDocInfo();
    }
    else
    {
      Log.debug(5, "Morpho couldn't understand the incomplete status "+incompletionStatus);
      return;
    }   
    incompleteDocInfo = new IncompleteDocInfo(incompletionStatus);
    incompleteDocInfo.setWizardPageClassInfoList(classNameList);
    incompleteDocInfo.setEntityIndex(index);
  }

  /**
   * Loads the incomplete AbstractDataPackage into new package wizard or text import wizard
   */
  public void load()
  {
    if(incompletionStatus == null)
    {
      UIController.getInstance().setWizardNotRunning();
      Log.debug(5, "Morpho couldn't open the package since the incompletion status is null");
    }
    else if (incompletionStatus.equals(IncompleteDocSettings.INCOMPLETE_PACKAGE_WIZARD))
    {
      //Log.debug(5, "new package wizard");
      loadNewPackageWizard();
    }
    else if(incompletionStatus.equals(IncompleteDocSettings.INCOMPLETE_ENTITY_WIZARD))
    {
      //Log.debug(5, "In text imorpt wizard");
      loadEntityWizard();
    }
    else
    {
      UIController.getInstance().setWizardNotRunning();
      Log.debug(5, "Morpho couldn't understand the incompletion status of the package "+incompletionStatus);
    }
  }
  
  /*
   * Loads the incomplete AbstractDataPackage into new package wizard
   */
  private void loadNewPackageWizard()
  {
    if(incompleteDocInfo != null)
    {
      boolean showPageCount = true;    
      WizardContainerFrame dpWiz = new WizardContainerFrame();
      dpWiz.initialAutoSaving();
      AbstractUIPage currentPage = loadPagesIntoWizard(dpWiz, dataPackage.getMetadataNode());
      if(currentPage == null)
      {          
          UIController.getInstance().setWizardNotRunning();
          dpWiz.dispose();
          dpWiz = null;
          Log.debug(5, "The new package wizard couldn't load the existing eml document!");
          return;
      }
      Log.debug(25, "The current page id in IncompleteDocument.loadNewPackageWizard is "+currentPage.getPageID());
      PackageWizardListener dataPackageWizardListener = new PackageWizardListener();
      dpWiz.setDataPackageWizardListener(dataPackageWizardListener);
      dpWiz.setBounds(
                      WizardSettings.WIZARD_X_COORD, WizardSettings.WIZARD_Y_COORD,
                      WizardSettings.WIZARD_WIDTH,   WizardSettings.WIZARD_HEIGHT );
      dpWiz.setCurrentPage(currentPage);
      dpWiz.setShowPageCountdown(showPageCount);
      dpWiz.setTitle(DataPackageWizardInterface.NEWPACKAGEWIZARDFRAMETITLE);
      dpWiz.setVisible(true);
    }
  }
  
  /*
   * Loads the incomplete AbstractDataPackage into text import wizard
   */
  private void loadEntityWizard()
  {
    if(incompleteDocInfo != null)
    {
      boolean showPageCount = false;
      boolean isEntity = true;
      int index = incompleteDocInfo.getEntityIndex();
      //remove the entity with the index (this entity is the unfinished one)
      Node entityNode = dataPackage.deleteEntity(index);
      //incomplete information was read in init method and we can delete it now
      dataPackage.removeInfoForIncompleteEntity();
      MorphoFrame frame = openMorphoFrameForDataPackage(dataPackage);
      WizardContainerFrame dpWiz = new WizardContainerFrame(IncompleteDocSettings.ENTITYWIZARD);
      dpWiz.setEntityIndex(index);
      AbstractUIPage currentPage = loadPagesIntoWizard(dpWiz, entityNode);
      if(currentPage == null)
      {
         UIController.getInstance().setWizardNotRunning();
         dpWiz.dispose();
         UIController.getInstance().removeWindow(frame);
         frame.dispose();
         dpWiz = null;
         Log.debug(5, "The new entity wizard couldn't load the existing eml document!");
         return;
      }
      Log.debug(25, "The current page id in IncompleteDocument.loadEntityWizard is "+currentPage.getPageID());
      dpWiz.initialAutoSaving();
      if(frame != null)
      {
        TableWizardListener dataPackageWizardListener = new TableWizardListener(dataPackage, index, frame);
        dpWiz.setDataPackageWizardListener(dataPackageWizardListener);
        dpWiz.setBounds(
                        WizardSettings.WIZARD_X_COORD, WizardSettings.WIZARD_Y_COORD,
                        WizardSettings.WIZARD_WIDTH,   WizardSettings.WIZARD_HEIGHT );
        dpWiz.setCurrentPage(currentPage);
        dpWiz.setTitle(DataPackageWizardInterface.NEWTABLEEWIZARDFRAMETITLE);
        dpWiz.setVisible(true);
      }
    }
  }
  
  /*
   * Load Wizard pages into a given Wizard. Return currentPage 
   */
  private AbstractUIPage loadPagesIntoWizard(WizardContainerFrame dpWiz, Node node)
  {
    AbstractUIPage currentPage = null;
    int textImportAttributePageIndex = 0;
    if(dpWiz != null && incompleteDocInfo != null)
    {
        WizardPageInfo [] classNameFromIncompleteDoc = incompleteDocInfo.getWizardPageClassInfoList();
        //Vector parameters = null;
        //Go through every page from incomplete doc info
        if(classNameFromIncompleteDoc != null)
        {
          int size = classNameFromIncompleteDoc.length;
          AbstractUIPage page = null;
          for(int i=0; i<size;  i++)
          {
            page = null;
            WizardPageInfo pageClassInfo = classNameFromIncompleteDoc[i];
            if(pageClassInfo != null)
            {
              String classNamePlusParameter ="";
              String className = pageClassInfo.getClassName();
              if(className !=null && className.equals(TextImportAttribute.CLASSFULLNAME))
              {
                //load TextImportAttribute from a special method
                int entityIndex = incompleteDocInfo.getEntityIndex();          
                page = generateTextImportAttributePage(dpWiz, node, textImportAttributePageIndex);
                textImportAttributePageIndex++;
              }
              else
              {
                classNamePlusParameter = classNamePlusParameter+className;
                Vector parameters = pageClassInfo.getParameters();//this one is for constructor
                OrderedMap variables = pageClassInfo.getVariablesValuesMap();//some additional info, which is not in eml itself
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
                    page= WizardUtil.getUIPage(map, dpWiz,  variables, node, null );
                     
                  }
                  catch(Exception e)
                  {
                    Log.debug(20, "Couldn't create the page with className "+className+" "+e.getMessage());
                    page = null;
                  }
                 
                }
                //generate empty UIPage if page isnull and map isnull
                if (map == null && page ==null)
                {
                          
                     Log.debug(30, "There is no map for classNamePlusParameter --------------------"+classNamePlusParameter+ " so we just initilize an empty page for class "+className);
                    page = WizardUtil.createAbstractUIpageObject(className, dpWiz, parameters );
                 
                }
              }
            }
            
            //if we can't get any page, we shouldn't load it. Otherwise, the result will be unpredicable.
            if(page == null)
            {
              break;
            }
    
            if(i == (size-1))
            {
              // set the current page to the last page
              //since we will use setCurrent page in loadNewPackageWizard 
              // and loadEntityWizard method, so we should NOT add it the stack
              currentPage = page;                                         
            }
            else
            {
              //add other page into stack of wizard frame
              dpWiz.addPageToStack(page);
            }                    
           
          }
        }
    }
    return currentPage;
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
   * Listener class for New Package Wizard
   * @author tao
   *
   */
  class PackageWizardListener implements  DataPackageWizardListener
  {
    /**
     * Methods inherits from DataPackageWizardListener
     */
    public void wizardComplete(Node newDOM, String autoSavedID) 
    {
    
      Log.debug(30,
          "Wizard complete - Will now create an AbstractDataPackage..");
  
      AbstractDataPackage adp = DataPackageFactory.getDataPackage(newDOM);
      Log.debug(30, "AbstractDataPackage complete");
      adp.setAccessionNumber("temporary.1.1");
      adp.setAutoSavedID(autoSavedID);
      openMorphoFrameForDataPackage(adp);
      Log.debug(45, "\n\n********** Wizard finished: DOM:");
      Log.debug(45, XMLUtilities.getDOMTreeAsString(newDOM, false));
    }
    
    /**
     * Methods inherits from DataPackageWizardListener
     */
    public void wizardCanceled() 
    {
    
       Log.debug(45, "\n\n********** Wizard canceled!");
     }
      
  } 
  
  
  /**
   * Listener class for New Package Wizard
   * @author tao
   *
   */
  class TableWizardListener implements  DataPackageWizardListener
  {
    private AbstractDataPackage adp = null;
    private int nextEntityIndex = 0;
    private MorphoFrame oldMorphoFrame = null;
    
    public TableWizardListener(AbstractDataPackage adp, int nextEntityIndex, MorphoFrame oldMorphoFrame)
    {
      this.adp = adp;
      this.nextEntityIndex = nextEntityIndex;
      this.oldMorphoFrame = oldMorphoFrame;
    }
    
    public void wizardComplete(Node newDOM, String autoSavedID) 
    {

      if(newDOM != null) 
      {

        Log.debug(30,"Entity Wizard complete - creating Entity object..");
        Log.debug(35, "Add/replace entity in incompleteDocumentloader.TableWizardListener with entity index "+nextEntityIndex);
        adp.replaceEntity(newDOM, nextEntityIndex);//we use replace method here because the auto-save file already adding the entity into datapackage.
        adp.setLocation("");  // we've changed it and not yet saved

      }
      MorphoFrame frame = openMorphoFrameForDataPackage(adp);
      if(frame != null)
      {
        oldMorphoFrame.setVisible(false);
        UIController controller = UIController.getInstance();
        controller.removeWindow(oldMorphoFrame);
        oldMorphoFrame.dispose();
      }

    }

     public void wizardCanceled() 
     {

        Log.debug(45, "\n\n********** Wizard canceled!");
     }
      
  } 
  
  /*
   * Open a morpho frame for given abstractDataPacakge
   */
  private MorphoFrame openMorphoFrameForDataPackage(AbstractDataPackage adp)
  {
     MorphoFrame frame = null;
     try 
     {
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider =
            services.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackage = (DataPackageInterface)provider;
      frame = dataPackage.openNewDataPackage(adp, null);
            
     } 
     catch (ServiceNotHandledException snhe) 
     {

       Log.debug(6, snhe.getMessage());
     }
     return frame;
  }
  
  /*
   * Generates a TextImportAttribute page. This page is kind of special, so we don't
   * generates from configuration.
   */
  private AbstractUIPage generateTextImportAttributePage(WizardContainerFrame dpWiz, Node dataTableNode, int index)
  {
    TextImportAttribute page = null;
    boolean success = false;
    if(dpWiz != null && index >= 0 && dataTableNode != null)
    {
      NodeList attributeList = null;
      Node attributeNode = null;
      try
      {
        attributeList = XPathAPI.selectNodeList(dataTableNode, TextImportAttribute.ATTRIBUTELISTPATH+"/"+TextImportAttribute.ATTRIBUTEPATH);
        attributeNode = attributeList.item(index);
      }
      catch(Exception e)
      {
        Log.debug(30, "Couldn't get attribute node from entity node in IncompleteDocumentLoader.generateTextImportAttributePage since "+e.getMessage());
        return page;
      }
      if(attributeNode != null)
      {
        WizardPageLibraryInterface lib = dpWiz.getWizardPageLibrary();
        if(lib != null)
        {
          page =(TextImportAttribute)lib.getPage(DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE+index);
        }
        if(page != null)
        {
          OrderedMap xpathMap = XMLUtilities.getDOMTreeAsXPathMap(attributeNode);
          success = page.setPageData(xpathMap, TextImportAttribute.ATTRIBUTEPAGEORDEREDMAPPATH, index);
          if(!success)
          {
            page = null;
          }
        }
      }
     }
     return page;
  }
}
