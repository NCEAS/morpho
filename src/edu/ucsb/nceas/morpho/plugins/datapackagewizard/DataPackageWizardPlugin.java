/**
 *  '$RCSfile: DataPackageWizardPlugin.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-04-02 07:31:19 $'
 * '$Revision: 1.31 $'
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
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import javax.xml.transform.TransformerException;

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

    startWizardAtPage(WizardSettings.PACKAGE_WIZ_FIRST_PAGE_ID, true, listener,
                      "New Data Package Wizard");
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
                      "New Datatable Wizard");
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
          df.initDoc(null, domnode, null, null);

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
   * inserts data from an AbstractUIPage into the AbstractDataPackage
   *
   * @param adp the AbstractDataPackage where the data will be inserted
   * @param page AbstractUIPage that is the source of the data
   * @param rootXPath the String that represents the "root" of the XPath to the
   *   content of the AbstractUIPage, INCLUDING PREDICATES. example - if this is
   *   a "Party" widget, being used for the second "Creator" entry in a list,
   *   then xPathRoot = "/eml:eml/dataset[1]/creator[2]
   * @param subtreeGenericName String (@see lib/eml200KeymapConfig.xml)
   * @param predicate int the predicate of this entry's subtree root node
   * @return boolean true if this page data successfully added to the datapkg,
   * false if not.
   */
  public static boolean addPageDataToDOM(AbstractDataPackage adp,
                                         AbstractUIPage page,
                                         String rootXPath,
                                         String subtreeGenericName,
                                         int predicate) {

    if (adp==null) {
      Log.debug(15, "** ERROR - addPageDataToDOM() Got NULL AbstractDataPackage");
      return false;
    }
    if (page==null) {
      Log.debug(15, "** ERROR - addPageDataToDOM() Got NULL AbstractUIPage");
      return false;
    }
    if (subtreeGenericName==null || subtreeGenericName.trim().length()==0) {
      Log.debug(15, "** ERROR - addPageDataToDOM() Got subtreeGenericName: "
                + subtreeGenericName);
      return false;
    }
    if (predicate < 1) {
      Log.debug(15, "** ERROR - addPageDataToDOM() Got invalid predicate: "
                + predicate);
      return false;
    }

    OrderedMap map = page.getPageData(rootXPath);

    Log.debug(45, "\n addPageDataToDOM() Got details from page -\n" + map);

    if (map == null || map.isEmpty()) {
      Log.debug(5, "Unable to get details from input!");
      return false;
    }
    String rootNodeName = rootXPath;
    //strip trailing slashes...
    while (rootNodeName.endsWith("/")) {
      rootNodeName = rootNodeName.substring(0, rootNodeName.length() - 1);
    }
    int lastSlashIdx = 1 + rootNodeName.lastIndexOf("/");
    int lastPredicateIdx = rootNodeName.lastIndexOf("[");
    if (lastPredicateIdx < 0) lastPredicateIdx = rootNodeName.length();
    rootNodeName = rootNodeName.substring(lastSlashIdx, lastPredicateIdx);
    Log.debug(5, "rootNodeName=" + rootNodeName);

    DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
    Document doc = impl.createDocument("", rootNodeName, null);

    Node subtreeRoot = doc.getDocumentElement();

    try {
      XMLUtilities.getXPathMapAsDOMTree(map, subtreeRoot);

    } catch (TransformerException w) {
      Log.debug(5, "Unable to add to package!");
      Log.debug(15, "TransformerException (" + w + ") calling "
                + "XMLUtilities.getXPathMapAsDOMTree(map, subtreeRoot) with \n"
                + "map = " + map
                + " and subtreeRoot = " + subtreeRoot);
      w.printStackTrace();
      return false;
    }
    Node check = null;

    if (adp.getSubtree(subtreeGenericName, predicate - 1) == null) {
      //not existing, so add to the datapackage
      Log.debug(45, "addPageDataToDOM() adding subtree to package...");
      Log.debug(45, "subtreeGenericName="+subtreeGenericName);
      Log.debug(45, "subtreeRoot="+XMLUtilities.getDOMTreeAsString(subtreeRoot));
      Log.debug(45, "predicate - 1="+(predicate - 1));
      check = adp.insertSubtree(subtreeGenericName, subtreeRoot, predicate - 1);
    } else {
      //existing, so replace...
      Log.debug(45,
                "addPageDataToDOM() replacing existing subtree in package...");
      check = adp.replaceSubtree(subtreeGenericName, subtreeRoot, predicate - 1);
    }
    if (check == null) {

      Log.debug(5, "** ERROR: Unable to add new details to package **\n");
      Log.debug(15, "addPageDataToDOM(): ADP.replaceSubtree() returned NULL");
      return false;
    }

    Log.debug(45, "...new project details successfully added/replaced");
    return true;
  }

}
