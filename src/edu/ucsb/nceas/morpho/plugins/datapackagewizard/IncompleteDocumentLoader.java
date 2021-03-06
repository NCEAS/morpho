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

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.EditingAttributeImportWizardListener;
import edu.ucsb.nceas.morpho.plugins.EditingAttributeInfo;
import edu.ucsb.nceas.morpho.plugins.EntityWizardListener;
import edu.ucsb.nceas.morpho.plugins.IncompleteDocInfo;
import edu.ucsb.nceas.morpho.plugins.InsertingAttributeImportWizardListener;
import edu.ucsb.nceas.morpho.plugins.NewPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.WizardPageInfo;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.TextImportAttribute;
import edu.ucsb.nceas.morpho.util.IncompleteDocSettings;
import edu.ucsb.nceas.morpho.util.Log;
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
  private MorphoDataPackage mdp = null;
  private IncompleteDocInfo incompleteDocInfo = null;
  private String incompletionStatus = null;
  private Hashtable wizardPageName = new Hashtable();
  private XPathUIPageMapping[] mappingList = null;
	
	

  /**
   * Constructs a IncompleteDocumentLoader with an AbstractDataPackage object
   * @param dataPackage
  */
  public IncompleteDocumentLoader(MorphoDataPackage mdp)
  {
    if(mdp == null)
    {
      Log.debug(5, "Morpho couldn't open the incomplete document since the document is null");
      return;
    }
    this.mdp = mdp;
    AbstractDataPackage adp = mdp.getAbstractDataPackage();
    adp.setLocation(DataPackageInterface.TEMPLOCATION);
    init();
    readXpathUIMappingInfo();
  }
  
  /*
   * Initializes incompletion status and IncompleteDocInfo object
   */
  private void init()
  {
	  AbstractDataPackage dataPackage = mdp.getAbstractDataPackage();
    try
    {
      incompleteDocInfo = dataPackage.readIncompleteDocInformation();
    }
    catch(Exception e)
    {
      Log.debug(5, "Couldn't read incomplete information in incomplete document "+e.getMessage());
      return;
    }   
    incompletionStatus = incompleteDocInfo.getStatus();
    WizardPageInfo [] classNameList = null;
    if( incompletionStatus != null && incompletionStatus.equals(IncompleteDocSettings.INCOMPLETE_ENTITY_WIZARD))
    {
      try
      {
        dataPackage.readImportAttributeInfoFromIncompleteDocInEntityWizard();
      }
      catch(Exception e)
      {
        Log.debug(5, "Couldn't read import attribute information in incomplete document "+e.getMessage());
        return;
      }
    }
    else if (incompletionStatus != null && incompletionStatus.equals(IncompleteDocSettings.INCOMPLETE_CODE_DEFINITION_WIZARD))
    {
      try
      {
        dataPackage.readImportAttributeInfoFromIncompleteDocInCodeDefWizard();
      }
      catch(Exception e)
      {
        Log.debug(5, "Couldn't read import attribute information in incomplete document "+e.getMessage());
        return;
      }
    }
 
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
    else if(incompletionStatus.equals(IncompleteDocSettings.INCOMPLETE_CODE_DEFINITION_WIZARD))
    {
      loadCodeDefWizard();
    }
    else if(incompletionStatus.equals(AbstractDataPackage.COMPLETED))
    {
      //UIController.getInstance().setWizardNotRunning();
      MorphoFrame frame = openMorphoFrameForDataPackage(mdp);
      frame.setVisible(true);
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
      boolean ableStart = DataPackageWizardPlugin.ableStartNewPackageWizard();
      if(!ableStart)
      {
        return;
      }
      AbstractDataPackage dataPackage = mdp.getAbstractDataPackage();
      //Log.debug(5, "The datatpackage is "+dataPackage);
      UIController.getInstance().setWizardIsRunning(dataPackage);
      boolean showPageCount = true;    
      MorphoFrame originatingMorphoFrame = null;
      WizardContainerFrame dpWiz = new WizardContainerFrame(IncompleteDocSettings.PACKAGEWIZARD, originatingMorphoFrame);
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
      //NewPackageWizardListener dataPackageWizardListener = new NewPackageWizardListener(dataPackage.getAccessionNumber());
      NewPackageWizardListener dataPackageWizardListener = new NewPackageWizardListener();
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
   * Loads the incomplete AbstractDataPackage into an entity wizard
   */
  private void loadEntityWizard()
  {
    if(incompleteDocInfo != null)
    {
      boolean ableStart = DataPackageWizardPlugin.ableStartEntityPackageWizard();
      if(!ableStart)
      {
        return;
      }
      //handle there is no page info - just open a frame.
      if(incompleteDocInfo.getWizardPageClassInfoList() == null)
      {
        openVisibleMorphoFrame(mdp);
        return;
      }   
      int index = incompleteDocInfo.getEntityIndex();
      AbstractDataPackage dataPackage = mdp.getAbstractDataPackage();
      //remove the entity with the index (this entity is the unfinished one)
      Node entityNode = dataPackage.deleteEntity(index);
      //incomplete information was read in init method and we can delete it now
      dataPackage.removeInfoForIncompleteEntity();
      MorphoFrame frame = openMorphoFrameForDataPackage(mdp);
      if(frame != null)
      {
        EntityWizardListener dataPackageWizardListener = new EntityWizardListener(mdp, index, frame);
        OrderedMap editingAttributeMap = null;
        loadEntityWizard(frame, IncompleteDocSettings.ENTITYWIZARD, 
                                dataPackageWizardListener, index, entityNode,editingAttributeMap);
      }
     
    }
  }
  
  /*
   * Loads CodeDefinitionWziard from an incomplete document
   */
  private void loadCodeDefWizard()
  {
    if(incompleteDocInfo != null)
    {
      boolean ableStart = DataPackageWizardPlugin.ableStartEntityPackageWizard();
      if(!ableStart)
      {
        return;
      }
      //handle there is no page info - just open a frame.
      if(incompleteDocInfo.getWizardPageClassInfoList() == null)
      {
        openVisibleMorphoFrame(mdp);
        return;
      }
      int index = incompleteDocInfo.getEntityIndex();
      EditingAttributeInfo editingAttributeInfo = incompleteDocInfo.getEditingAttributeInfo();
      if(editingAttributeInfo == null)
      {
        UIController.getInstance().setWizardNotRunning();
        Log.debug(5, "Morpho couldn't get the information for editing attribute in order to load the wizard");
        return;
      }
      int editingEntityIndex = editingAttributeInfo.getEntityIndex();
      int editingAttributeIndex = editingAttributeInfo.getAttributeIndex();
      Boolean insertBeforeSelection = editingAttributeInfo.getInsertionBeforeSelection();
      OrderedMap map = editingAttributeInfo.getData();
      AbstractDataPackage dataPackage = mdp.getAbstractDataPackage();
      //remove the entity with the index (this entity is the unfinished one)
      Node entityNode = dataPackage.deleteEntity(index);
      //incomplete information was read in init method and we can delete it now
      dataPackage.removeInfoForIncompleteCodeDef();
      MorphoFrame frame = openMorphoFrameForDataPackage(mdp);
      if(frame != null)
      {
        DataPackageWizardListener  listener = null;
        WizardContainerFrame wizard =loadEntityWizard(frame, IncompleteDocSettings.CODEDEFINITIONWIZARD, 
            listener, index, entityNode, map);
        if(wizard == null)
        {
          UIController.getInstance().setWizardNotRunning();
          if(frame != null)
          {
            frame.setVisible(false);
            frame.dispose();
          }
          Log.debug(5, "Morpho couldn't get wizard container frame!");
          return;
        }
        try
        {
          if(insertBeforeSelection == null)
          {
            //this is for editing an attribute
            listener = new EditingAttributeImportWizardListener(frame, mdp, 
                                                          wizard,  editingEntityIndex, editingAttributeIndex);   
            wizard.setDataPackageWizardListener(listener);          
          }
          else
          {
            //for inserting a new column
            listener = new InsertingAttributeImportWizardListener(frame, mdp, 
                wizard,  editingEntityIndex, editingAttributeIndex, insertBeforeSelection);   
            wizard.setDataPackageWizardListener(listener); 
          }
        }
        catch(Exception e)
        {
          UIController.getInstance().setWizardNotRunning();
          if(frame != null)
          {
            frame.setVisible(false);
            frame.dispose();
          }
          Log.debug(5, "Morpho couldn't get the listener for editing attribute in order to load the wizard");
          return;
        }
             
      }
    }
  }
  
  /*
   * Loads an entity wizard with specified morpho frame, wizard type, a listener, entity index, entity node and an attribute orderedMap
   */
  private WizardContainerFrame loadEntityWizard(MorphoFrame frame, String wizardType, DataPackageWizardListener listener,
        int index, Node entityNode, OrderedMap  attributeMap)
  {
    WizardContainerFrame dpWiz = null;
    if(frame != null)
    {
      dpWiz = new WizardContainerFrame(wizardType, frame);
      dpWiz.setEditingAttributeMap(attributeMap);
      dpWiz.setEntityIndex(index);
      AbstractUIPage currentPage = loadPagesIntoWizard(dpWiz, entityNode);
      if(currentPage == null)
      {
         //UIController.getInstance().setWizardNotRunning();
    	  AbstractDataPackage dataPackage = mdp.getAbstractDataPackage();
         String docid = dataPackage.getAccessionNumber();
         UIController.getInstance().removeDocidFromEntityWizardRunningRecorder(docid);   
         dpWiz.dispose();
         UIController.getInstance().removeWindow(frame);
         frame.dispose();
         dpWiz.dispose();
         dpWiz = null;
         Log.debug(5, "The new entity wizard couldn't load the existing eml document!");
         return dpWiz;
      }
      Log.debug(25, "The current page id in IncompleteDocument.loadEntityWizard is "+currentPage.getPageID());
      dpWiz.initialAutoSaving();   
      dpWiz.setDataPackageWizardListener(listener);
      dpWiz.setBounds(
                      WizardSettings.WIZARD_X_COORD, WizardSettings.WIZARD_Y_COORD,
                      WizardSettings.WIZARD_WIDTH,   WizardSettings.WIZARD_HEIGHT );
      dpWiz.setCurrentPage(currentPage);
      boolean showPageCount = false;
      dpWiz.setShowPageCountdown(showPageCount);
      dpWiz.setTitle(DataPackageWizardInterface.NEWTABLEEWIZARDFRAMETITLE);
      frame.setVisible(true);
      dpWiz.setVisible(true);
    }
    return dpWiz;
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
  
  
  /*
   * Open a morpho frame for given abstractDataPacakge
   */
  private MorphoFrame openMorphoFrameForDataPackage(MorphoDataPackage mdp)
  {
     MorphoFrame frame = null;
     try 
     {
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider =
            services.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackage = (DataPackageInterface)provider;
      // make it visible
      // see http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5245
      boolean visible = true;
      frame = dataPackage.openNewDataPackage(mdp, null, visible);
            
     } 
     catch (ServiceNotHandledException snhe) 
     {

       Log.debug(6, snhe.getMessage());
     }
     return frame;
  }
  
  /*
   * Opens a visible morpho frame
   */
   private void openVisibleMorphoFrame(MorphoDataPackage mdp)
   {
     MorphoFrame frame = openMorphoFrameForDataPackage(mdp);
     if(frame != null)
     {
       frame.setVisible(true);
     }
     else
     {
       Log.debug(5, "Morpho frame couldn't open a frame for the package!");
     }
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
          //page =(TextImportAttribute)lib.getPage(DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE+index);
            page =(TextImportAttribute)lib.getPage(DataPackageWizardInterface.LOAD_INCOMPLETED_ATTRIBUTE+DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE+index);
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
