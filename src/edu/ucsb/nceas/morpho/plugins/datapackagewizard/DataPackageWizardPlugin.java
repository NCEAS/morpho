/**
 *  '$RCSfile: DataPackageWizardPlugin.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 20:32:17 $'
 * '$Revision: 1.47 $'
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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.xml.transform.TransformerException;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.editor.DocFrame;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyPage;
import edu.ucsb.nceas.morpho.util.IncompleteDocSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XMLUtil;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;


/**
 *  Main controller class for creating and starting a Data Package Wizard Plugin
 */

public class DataPackageWizardPlugin implements PluginInterface,
                                                ServiceProvider,
                                                DataPackageWizardInterface {
	
	

  /**
   *  Constructor
   */
  public DataPackageWizardPlugin() { }


  /**
   *  Required by PluginInterface; called automatically at runtime
   *
   *  @param morpho    a reference to the <code>Morpho</code>
   */
  public void initialize(Morpho morpho) {

    try {
      ServiceController services = ServiceController.getInstance();
      services.addService(DataPackageWizardInterface.class, this);
      Log.debug(20, "Service added: DataPackageWizardInterface.");

    } catch (ServiceExistsException see) {
      Log.debug(6, "Service registration failed: DataPackageWizardInterface");
      Log.debug(6, see.toString());
    }
  }


  /**
   *  Required by DataPackageWizardInterface:
   *  method to start the Package wizard
   *
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Package Wizard has finished
   */
  public void startPackageWizard(DataPackageWizardListener listener) {

   boolean ableStart = ableStartNewPackageWizard();
   if(!ableStart)
   {
     return;
   }

    AbstractDataPackage tempDataPackage = DataPackageFactory.getDataPackage(
      getNewEmptyDataPackageDOM(WizardSettings.TEMP_REFS_EML211_DOCUMENT_TEXT));
    if(tempDataPackage == null) return;
    String tempID = DataStoreServiceController.getInstance().generateIdentifier(null, DataPackageInterface.LOCAL);
    tempDataPackage.setAccessionNumber(tempID);
    UIController.getInstance().setWizardIsRunning(tempDataPackage);
    int entityIndex = -1;
    MorphoFrame originatingMorphoFrame = null;
    startWizardAtPage(originatingMorphoFrame, WizardSettings.PACKAGE_WIZ_FIRST_PAGE_ID, true, listener,
    		NEWPACKAGEWIZARDFRAMETITLE, entityIndex);

  }

  private Node getNewEmptyDataPackageDOM(String DocText) {

    Node rootNode = null;

    try {
      rootNode = XMLUtilities.getXMLReaderAsDOMTreeRootNode(
          new StringReader(DocText));
    } catch (Exception e) {
      e.printStackTrace();
      Log.debug(5, "unexpected error trying to create new XML document");
      return null;
    }
    return rootNode;
  }

  /**
   *  Required by DataPackageWizardInterface:
   *  method to start the Entity wizard
   *  @param originatingMorphoFrame the frame which started the wizard.
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Entity Wizard has finished
   *  @param entityIndex the index of the new entity in this package
   */
  public void startEntityWizard(MorphoFrame originatingMorphoFrame,DataPackageWizardListener listener, int entityIndex) {
    boolean ableStart = ableStartEntityPackageWizard();
    if(!ableStart)
    {
      return;
    }
    startWizardAtPage(originatingMorphoFrame, WizardSettings.ENTITY_WIZ_FIRST_PAGE_ID, false, listener,
    		NEWTABLEEWIZARDFRAMETITLE, entityIndex);
  }


  /**
   *  method to start the Code Definitions Import wizard
   *
   *  @param originatingMorphoFrame the frame which started the wizard.
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Wizard has finished
   *  @param entityIndex the index of the entity which wizard will use (next entity index)
   *  @param editingEntityIndex  the index of the entity which is editing
   *  @param editingAttributeIndex the index of the attribute which is editing
   *  @param beforeFlag if the new column is before the select column. If it is null, it means editing rather than inserting
   */
  public void startCodeDefImportWizard(MorphoFrame originatingMorphoFrame, DataPackageWizardListener listener, int entityIndex,Boolean beforeFlag, 
		  int editingEntityIndex, int editingAttributeIndex) {
    boolean ableStart = ableStartEntityPackageWizard();
    if(!ableStart)
    {
      return;
    }
	  WizardContainerFrame wizard = startWizardAtPage(originatingMorphoFrame,DataPackageWizardInterface.CODE_IMPORT_SUMMARY, false,
                      listener, NEWCODEDEFINITIONWIZARDFRAMETITLE, entityIndex);
	  if(wizard != null)
	  {
	    wizard.setEditingEntityIndex(editingEntityIndex);
	    wizard.setEditingAttributeIndex(editingAttributeIndex);
	    //wizard.setEditingAttributeMap(attributeMap);
	    wizard.setBeforeSelectionFlag(beforeFlag);
	  }
  }
  
  /**
   * Check if a new package wizard can be run.
   * 1. No any new package wizard is running.
   * 2. No any entity package wizard is running
   * @return true if morpho can start new package wizard
   */
  public static boolean ableStartNewPackageWizard()
  {
     boolean ableStart = false;
     boolean existingRunningWizard = checkIsNewPackageWizardRunning();
     if(!existingRunningWizard)
     {
       Log.debug(30, "DataPackageWizardPlugin.ableStartNewPackageWizard() - There is no existing new package wizard");
       ableStart = true;
     }
     else
     {
       ableStart = false;
       return ableStart;
     }
     existingRunningWizard = UIController.getInstance().isAnyEntityWizardRunning();
     if(!existingRunningWizard)
     {
       Log.debug(30, "DataPackageWizardPlugin.ableStartNewPackageWizard() - There is no existing enity package wizard");
       ableStart = true;
     }
     else
     {
       Log.debug(5, "Sorry, there is another entity wizard running on a package."+ 
       "\nPlease finish that entity wizard first.");
       ableStart = false;
     }
     return ableStart;
  }
  
  
  /**
   * Check if an entity package wizard can be run.
   * 1. No any new package wizard is running.
   * 2. No any other entity package wizard is running on the same morpho frame (data package)
   * @return true if morpho can start an entity package wizard
   */
  public static boolean ableStartEntityPackageWizard()
  {
    boolean ableStart = false;
    boolean existingRunningWizard = checkIsNewPackageWizardRunning();
    if(!existingRunningWizard)
    {
      Log.debug(30, "DataPackageWizardPlugin.ableStartEntityPackageWizard() - There is no existing new package wizard");
      ableStart = true;
    }
    else
    {
      ableStart = false;
      return ableStart;
    }
    existingRunningWizard = checkIsEntityWizardRunningOnSameMorphoFrame();
    if(!existingRunningWizard)
    {
      Log.debug(30, "DataPackageWizardPlugin.ableStartEntityPackageWizard() - There is no existing enity package wizard on the frame");
      ableStart = true;
    }
    else
    {
      ableStart = false;
    }
    return ableStart;
  }
  
  /*
   * Check if any new package wizard is running on the morpho
   */
  private static boolean checkIsNewPackageWizardRunning()
  {
    boolean isRunning = UIController.getInstance().isWizardRunning();
    if (isRunning) {
      JOptionPane.showConfirmDialog(UIController.getInstance().getCurrentActiveWindow(),
        "Sorry, a Data Package Wizard is running. Please finish the wizard first!",
                                   "Wizard already running",
                                   JOptionPane.DEFAULT_OPTION,
                                   JOptionPane.WARNING_MESSAGE);
    }
    return isRunning;
  }
  
  /*
   * Check if an entity wizard is running on the current active window.
   */
  private static boolean checkIsEntityWizardRunningOnSameMorphoFrame()
  {
    boolean running  = false;
    String docid  = null;
    AbstractDataPackage adp = null;
    MorphoDataPackage morphoPackage = UIController.getInstance().getCurrentAbstractDataPackage();
    if(morphoPackage != null) {
        adp = morphoPackage.getAbstractDataPackage();
    }
   
    if(adp != null)
    {
      docid = adp.getAccessionNumber();
      running = UIController.getInstance().isEntityWizardRunning(docid);
    }
    if(running)
    {
      Log.debug(5, "Sorry, there is another Entity Wizard running on data package "+docid+
          ". \nPlease finish that entity wizard first.");
    }
    return running;
  }
  
  /**
   * 
   * start a correction invalid eml document wizard. This wizard always be used to
   * correct in valid eml document which was transformed from old eml version.
   *
   * @param dataPackage  the datapackage will be corrected
   * @param errorPathes    the list of path which has valid value
   * @param frame            the old frame which need be disposed after correction is done
   * @param listener         the listener will handle some another action after the wizard is done, e.g .AddAccessCommand
   */
  public void startCorrectionWizard(MorphoDataPackage mdp, Vector errorPathes, MorphoFrame frame, DataPackageWizardListener listener)
  {
      boolean isSaveProcess = false;
      startCorrectionWizard(mdp, errorPathes, frame, listener, isSaveProcess);
  }
  /**
   * 
   * start a correction invalid eml document wizard. This wizard always be used to
   * correct in valid eml document which was transformed from old eml version.
   *
   * @param dataPackage  the datapackage will be corrected
   * @param errorPathes    the list of path which has valid value
   * @param frame            the old frame which need be disposed after correction is done
   * @param listener         the listener will handle some another action after the wizard is done, e.g .AddAccessCommand
   * @param isSaveProcess    if the correction happens during the save process.  
   */
  public void startCorrectionWizard(MorphoDataPackage mdp, Vector errorPathes, MorphoFrame frame, DataPackageWizardListener listener, boolean isSaveProcess)
  {
      CorrectionWizardController controller = new CorrectionWizardController(errorPathes, mdp, frame);  
      controller.setIsSavingProcess(isSaveProcess);
      controller.setExternalListener(listener);
      controller.startWizard();
  }


  /**
   * method to start the wizard at a given page
   *
   * @param originatingMorphoFrame the frame which started the wizard.
   * @param pageID the ID of the page from where the wizard is to be started
   * @param showPageCount boolean
   * @param listener String
   * @param frameTitle String
   * @param entityIndex the index of the new entity in this package
   */
  protected WizardContainerFrame startWizardAtPage(MorphoFrame originatingMorphoFrame, String pageID, boolean showPageCount,
                        DataPackageWizardListener listener, String frameTitle, int entityIndex) {

    WizardContainerFrame dpWiz = null;
    if(pageID != null && pageID.equals(WizardSettings.ENTITY_WIZ_FIRST_PAGE_ID))
    {
    	//boolean isEnity = true;
    	dpWiz = new WizardContainerFrame(IncompleteDocSettings.ENTITYWIZARD, originatingMorphoFrame);
    	dpWiz.setEntityIndex(entityIndex);
    }
    else if(pageID != null && pageID.equals(DataPackageWizardInterface.CODE_IMPORT_SUMMARY))
    {
      dpWiz = new WizardContainerFrame(IncompleteDocSettings.CODEDEFINITIONWIZARD, originatingMorphoFrame);
      dpWiz.setEntityIndex(entityIndex);
    }
    else
    {
    	dpWiz = new WizardContainerFrame(IncompleteDocSettings.PACKAGEWIZARD, originatingMorphoFrame);
    }
    dpWiz.initialAutoSaving();
    dpWiz.setDataPackageWizardListener(listener);
    dpWiz.setBounds(
                  WizardSettings.WIZARD_X_COORD, WizardSettings.WIZARD_Y_COORD,
                  WizardSettings.WIZARD_WIDTH,   WizardSettings.WIZARD_HEIGHT );
    dpWiz.setCurrentPage(pageID);
    dpWiz.setShowPageCountdown(showPageCount);
    dpWiz.setTitle(frameTitle);
    dpWiz.setVisible(true);
    return dpWiz;
  }


  /**
   *  returns the WizardPage with the corresponding pageID provided
   *
   *  @param pageID the String pageID for the WizardPage to be returned
   *
   *  @return  the corresponding WizardPage with this ID
   */
  public AbstractUIPage getPage(String pageID) {

	WizardPageLibrary library = new WizardPageLibrary(null);
    return library.getPage(pageID);
  }

  // for testing/development
  public static void main(String[] args) {

  // TEXT IMPORT WIZARD NEEDS MORPHO TO GET CONFIG
    Morpho.main(null);
    ///////////////////////

    Log.setDebugLevel(55);
    DataPackageWizardPlugin plugin = new DataPackageWizardPlugin();
    //plugin.initialize(Morpho.thisStaticInstance);
    plugin.startPackageWizard(
      new DataPackageWizardListener() {

        public void wizardComplete(Node newDOM, String autoSavedID) {
        Log.debug(1,"Wizard complete - Will now create an AbstractDataPackage..");
          AbstractDataPackage dp = DataPackageFactory.getDataPackage(newDOM);

         Log.debug(1,"AbstractDataPackage complete - Will now show in an XML Editor..");
         Node domnode = dp.getMetadataNode();
          DocFrame df = new DocFrame();
          df.setVisible(true);
          df.initDoc(null, domnode, null, null,
              WizardSettings.EML211_SCHEMA_NAMESPACE);

          Log.debug(45, "\n\n********** Wizard finished: DOM:");
          Log.debug(45, XMLUtil.getDOMTreeAsString(newDOM));

        }

        public void wizardCanceled() {

          Log.debug(45, "\n\n********** Wizard canceled!");
          System.exit(0);
        }
        
        /**
         *  Method from DataPackageWizardListener. Do nothing.
         *
         */
        public void wizardSavedForLater()
        {
          Log.debug(45, "Correction wizard was saved for later usage");
        }

      }
    );
  }



  /**
   * deletes <em>all</em> existing subtrees of name subtreeGenericName, then
   * inserts data for each AbstractUIPage in pageList into the passed
   * AbstractDataPackage
   *
   * @param adp the AbstractDataPackage where the data will be inserted
   * @param pageList List of AbstractUIPages that are the source of the data, in
   * the order that they should be added to the DOM
   * @param rootXPath the String that represents the "root" of the XPath to the
   * content of each AbstractUIPage, NOT INCLUDING PREDICATES. example - if the
   * list contains "Party" widgets, being used for "creator" entries, then
   *   xPathRoot = "creator"
   * @param subtreeGenericName String - eg "contact", "project" etc
   * (@see lib/eml200KeymapConfig.xml)
   * @return boolean true if this page data successfully added to the datapkg,
   *   false if not.
   */
  public static boolean deleteExistingAndAddPageDataToDOM(
                                                    AbstractDataPackage adp,
                                                    List pageList,
                                                    String rootXPath,
                                                    String subtreeGenericName) {

    if (adp==null) {
      Log.debug(15, "** ERROR - deleteExistingAndAddPageDataToDOM() Got NULL AbstractDataPackage");
      return false;
    }
    if (pageList==null) {
      Log.debug(15, "** ERROR - deleteExistingAndAddPageDataToDOM() Got NULL pageList");
      return false;
    }
    if (subtreeGenericName==null || subtreeGenericName.trim().length()==0) {
      Log.debug(15, "** ERROR - deleteExistingAndAddPageDataToDOM() Got subtreeGenericName: "
                + subtreeGenericName);
      return false;
    }
    if (rootXPath==null) {
      Log.debug(15, "** ERROR - deleteExistingAndAddPageDataToDOM() Got NULL rootXPath");
      return false;
    }
    // if pageList is empty, don't need to do anything
    //if (pageList.size() < 1) {
    //  Log.debug(15, "deleteExistingAndAddPageDataToDOM() Got empty pageList - returning true");
    //  return true;
    //}

    //ensure root xpath starts with a slash...
    rootXPath = rootXPath.trim();
    if (!rootXPath.startsWith("/")) rootXPath = "/" + rootXPath;

    // given a rootXPath like: /eml/dataset/whatever[2]/aName[3], we want to end
    //up with rootNodeName="aName"...
    boolean errorOccurred = false;
    String rootNodeName = rootXPath;
    //strip trailing slashes...
    while (rootNodeName.endsWith("/")) {
      rootNodeName = rootNodeName.substring(0, rootNodeName.length() - 1);
    }
    int lastSlashIdx = 1 + rootNodeName.lastIndexOf("/");
    int lastPredicateIdx = rootNodeName.lastIndexOf("[");
    if (lastPredicateIdx < 0)lastPredicateIdx = rootNodeName.length();
    rootNodeName = rootNodeName.substring(lastSlashIdx, lastPredicateIdx);
    Log.debug(45, "rootNodeName=" + rootNodeName);
    DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();

    List deletedOriginalsList = adp.deleteAllSubtrees(subtreeGenericName);

    int  index = 0;
    for (Iterator it = pageList.iterator(); it.hasNext();) {

      AbstractUIPage nextPage = (AbstractUIPage)it.next();
      if (nextPage==null) continue;

      OrderedMap map = nextPage.getPageData(rootXPath);

      Log.debug(45,
          "\n deleteExistingAndAddPageDataToDOM() Got rootXPath: "+rootXPath);
      Log.debug(45,
          "\n deleteExistingAndAddPageDataToDOM() Got details from page "
          +index+" - " + map);

      if (map == null || map.isEmpty()) {
        Log.debug(15, "ERROR - Unable to get details from page"
          +index+"!");
        errorOccurred = true;
      }

      Document doc = impl.createDocument("", rootNodeName, null);

      Node subtreeRoot = doc.getDocumentElement();

      try {
        XMLUtilities.getXPathMapAsDOMTree(map, subtreeRoot);

      } catch (TransformerException w) {
        Log.debug(15, "TransformerException (" + w + ") calling "
                  +
                  "XMLUtilities.getXPathMapAsDOMTree(map, subtreeRoot) with \n"
                  + "map = " + map
                  + " and subtreeRoot = " + subtreeRoot);
        w.printStackTrace();
        errorOccurred = true;
        Log.debug(5, "Unable to add to package!");
      }
      Node check = null;

      //not existing, so add to the datapackage
      Log.debug(45,
                "deleteExistingAndAddPageDataToDOM() adding subtree to package...");
      Log.debug(45, "subtreeGenericName=" + subtreeGenericName);
      Log.debug(45,
                "subtreeRoot=" + XMLUtilities.getDOMTreeAsString(subtreeRoot));
      Log.debug(45, "index=" + index);

      check = adp.insertSubtree(subtreeGenericName, subtreeRoot, index);

      if (check == null) {

        Log.debug(15, "deleteExistingAndAddPageDataToDOM(): "
                  + "ADP.insertSubtree() returned NULL");
        errorOccurred = true;
        Log.debug(5, "**" + Language.getInstance().getMessage("UnableToAddNewDetails") + "**\n");
      }
      index++;
    }
    if (errorOccurred) {

      //delete the ones we've added:
      adp.deleteAllSubtrees(subtreeGenericName);

      //now add back the original ones
      int idx = 0;
      for (Iterator it = deletedOriginalsList.iterator(); it.hasNext(); ) {

        Node nextNode = (Node)it.next();
        adp.replaceSubtree(subtreeGenericName, nextNode, idx++);
      }
    }
    Log.debug(45, ((errorOccurred)?
           "\n** ERROR - data NOT added - datapackage reset to original values"
         : "\n>> data added successfully ") );
    return true;
  }



  /**
   * Given a CustomList containing Party listings, updates the DOM to contain
   * those listings
   *
   * @param partiesCustomList CustomList the UI CustomList containing the party objects
   * to be added to the DOM
   * @param rootXPath the String that represents the "root" of the XPath to the
   * content of each AbstractUIPage, NOT INCLUDING PREDICATES. example - if the
   * list contains "Party" widgets, being used for "creator" entries, then
   *   xPathRoot = "creator"
   * @param subtreeGenericName String - eg "contact", "project" etc
   * (@see lib/eml200KeymapConfig.xml)
   * @param pageType the type of page object to use, as defined in the
   * DataPackageWizardInterface class - eg:
   * DataPackageWizardInterface.PARTY_CREATOR
   * DataPackageWizardInterface.PARTY_PERSONNEL
   * etc...
   */
  public static void updateDOMFromPartiesList(CustomList partiesCustomList,
                                              String rootXPath,
                                              String subtreeGenericName,
                                              String pageType) {

    //update datapackage...
    List nextRowList = null;
    List pagesList = new ArrayList();
    Object nextPageObj = null;

    for (Iterator it = partiesCustomList.getListOfRowLists().iterator(); it.hasNext(); ) {

      nextRowList = (List)it.next();
      //column 3 is user object - check it exists and isn't null:
      if (nextRowList.size() < 4) continue;
      nextPageObj = nextRowList.get(3);
      if (nextPageObj == null)continue;
      if (!(nextPageObj instanceof AbstractUIPage)) continue;
      pagesList.add((AbstractUIPage)nextPageObj);
    }

    deleteExistingAndAddPageDataToDOM(
        UIController.getInstance().getCurrentAbstractDataPackage().getAbstractDataPackage(),
        pagesList, rootXPath, subtreeGenericName);

    updatePartiesListFromDOM(partiesCustomList, rootXPath, subtreeGenericName, pageType);
  }


  /**
   * Given a CustomList containing Party listings, updates that list to contain
   * the parties in the DOM
   *
   * @param partiesCustomList CustomList the UI CustomList containing the party objects
   * to be added to the DOM
   * @param rootXPath the String that represents the "root" of the XPath to the
   * content of each AbstractUIPage, NOT INCLUDING PREDICATES. example - if the
   * list contains "Party" widgets, being used for "creator" entries, then
   *   xPathRoot = "creator"
   * @param subtreeGenericName String - eg "contact", "project" etc
   * (@see lib/eml200KeymapConfig.xml)
   */

  public static void updatePartiesListFromDOM(CustomList partiesCustomList,
                                       String rootXPath,
                                       String subtreeGenericName,
                                       String pageType) {

    AbstractDataPackage adp
        = UIController.getInstance().getCurrentAbstractDataPackage().getAbstractDataPackage();
    if (adp == null) {
      Log.debug(15, "\npackage from UIController is null");
      Log.debug(5, "ERROR: cannot update!");
      return;
    }

    List partySubtreesList = adp.getSubtrees(subtreeGenericName);
    Log.debug(45, "updatePartiesListFromDOM - partySubtreesList.size() = "
              + partySubtreesList.size());

    List partiesOrderedMapList = new ArrayList();

    for (Iterator it = partySubtreesList.iterator(); it.hasNext(); ) {

      partiesOrderedMapList.add(
          XMLUtilities.getDOMTreeAsXPathMap((Node)it.next()));
    }

    populatePartiesList(partiesCustomList, partiesOrderedMapList, rootXPath, pageType);
  }


  /**
   * Given a UI CustomList, populates it from the nvps in the List of OrderedMap
   * objects passed as partiesOrderedMapList
   *
   * @param partiesCustomList CustomList the CustomList of parties to be updated
   * @param partiesOrderedMapList List the list OrderedMaps containing party
   * nvps to be made into entries on the parties list
   * @param partyXPathRoot String - the xpath relative to the subtree root we're
   * dealing with - so for example, if we're in dataset, contact's path would be:
   *   /contact
   * or if we're dealing with project, the path would be
   *   /project/personnel etc
   * @return boolean
   */
  public static boolean populatePartiesList(CustomList partiesCustomList,
                                            List partiesOrderedMapList,
                                            String partyXPathRoot,
                                            String pageType) {

    Iterator persIt = partiesOrderedMapList.iterator();
    OrderedMap nextPersonnelMap = null;
    int partyPredicate = 1;

    partiesCustomList.removeAllRows();
    boolean partyRetVal = true;

    if (!partyXPathRoot.startsWith("/")) partyXPathRoot = "/" + partyXPathRoot;
    if (!partyXPathRoot.endsWith("["))   partyXPathRoot = partyXPathRoot + "[";

    while (persIt.hasNext()) {

      nextPersonnelMap = (OrderedMap)persIt.next();
      if (nextPersonnelMap == null || nextPersonnelMap.isEmpty()) continue;

      WizardPageLibrary library = new WizardPageLibrary(null);
      PartyPage nextParty = (PartyPage)library.getPage(pageType);

      boolean checkParty = nextParty.setPageData(nextPersonnelMap,
                                                 partyXPathRoot
                                                 + (partyPredicate++) + "]");

      if (!checkParty)partyRetVal = false;
      List newRow = nextParty.getSurrogate();
      newRow.add(nextParty);

      partiesCustomList.addRow(newRow);
    }
    return partyRetVal;
  }
  
  /**
   * Load (open) an incomplete document into new package wizard /text import wizard
   * @param dataPackage the incomplete data package
   */
  public void loadIncompleteDocument(MorphoDataPackage mdp)
  {
	  
	  if(mdp == null) return;
	  IncompleteDocumentLoader loader = new IncompleteDocumentLoader(mdp);
	  loader.load();
  }

}
