/**
 *  '$RCSfile: DataPackageWizardPlugin.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: connolly $'
 *     '$Date: 2005-07-22 16:39:07 $'
 * '$Revision: 1.43 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.editor.DocFrame;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyPage;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import javax.swing.JOptionPane;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


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

    boolean isRunning = UIController.getInstance().isWizardRunning();
    if (isRunning) {
      JOptionPane.showConfirmDialog(null,
        "Sorry, only one instance of the Data Package Wizard can be running at a time!",
                                   "Wizard already running",
                                   JOptionPane.DEFAULT_OPTION,
                                   JOptionPane.WARNING_MESSAGE);
      return;
    }

    AbstractDataPackage tempDataPackage = DataPackageFactory.getDataPackage(
      getNewEmptyDataPackageDOM(WizardSettings.TEMP_REFS_EML201_DOCUMENT_TEXT));
    if(tempDataPackage == null) return;

    UIController.getInstance().setWizardIsRunning(tempDataPackage);

    startWizardAtPage(WizardSettings.PACKAGE_WIZ_FIRST_PAGE_ID, true, listener,
                      "New Data Package Wizard");

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
   *
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Entity Wizard has finished
   */
  public void startEntityWizard(DataPackageWizardListener listener) {

    startWizardAtPage(WizardSettings.ENTITY_WIZ_FIRST_PAGE_ID, false, listener,
                      "New Data Table Wizard");
  }


  /**
   *  method to start the Code Definitions Import wizard
   *
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Wizard has finished
   */
  public void startCodeDefImportWizard(DataPackageWizardListener listener) {

    startWizardAtPage(DataPackageWizardInterface.CODE_IMPORT_PAGE, false,
                      listener, "Import Code Definitions");
  }


  /**
   * method to start the wizard at a given page
   *
   * @param pageID the ID of the page from where the wizard is to be started
   * @param showPageCount boolean
   * @param listener String
   * @param frameTitle String
   */
  private void startWizardAtPage(String pageID, boolean showPageCount,
                        DataPackageWizardListener listener, String frameTitle) {

    WizardContainerFrame dpWiz = new WizardContainerFrame();
    dpWiz.setDataPackageWizardListener(listener);
    dpWiz.setBounds(
                  WizardSettings.WIZARD_X_COORD, WizardSettings.WIZARD_Y_COORD,
                  WizardSettings.WIZARD_WIDTH,   WizardSettings.WIZARD_HEIGHT );
    dpWiz.setCurrentPage(pageID);
    dpWiz.setShowPageCountdown(showPageCount);
    dpWiz.setTitle(frameTitle);
    dpWiz.setVisible(true);
  }


  /**
   *  returns the WizardPage with the corresponding pageID provided
   *
   *  @param pageID the String pageID for the WizardPage to be returned
   *
   *  @return  the corresponding WizardPage with this ID
   */
  public AbstractUIPage getPage(String pageID) {

    return WizardPageLibrary.getPage(pageID);
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

        public void wizardComplete(Node newDOM) {
        Log.debug(1,"Wizard complete - Will now create an AbstractDataPackage..");
          AbstractDataPackage dp = DataPackageFactory.getDataPackage(newDOM);

         Log.debug(1,"AbstractDataPackage complete - Will now show in an XML Editor..");
         Node domnode = dp.getMetadataNode();
          DocFrame df = new DocFrame();
          df.setVisible(true);
          df.initDoc(null, domnode, null, null,
              WizardSettings.EML201_SCHEMA_NAMESPACE);

          Log.debug(45, "\n\n********** Wizard finished: DOM:");
          Log.debug(45, XMLUtilities.getDOMTreeAsString(newDOM, false));

        }

        public void wizardCanceled() {

          Log.debug(45, "\n\n********** Wizard canceled!");
          System.exit(0);
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
        Log.debug(5, "** ERROR: Unable to add new details to package **\n");
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
        UIController.getInstance().getCurrentAbstractDataPackage(),
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
        = UIController.getInstance().getCurrentAbstractDataPackage();
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

      PartyPage nextParty = (PartyPage)WizardPageLibrary.getPage(pageType);

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

}
