/**
 *  '$RCSfile: AddTitleAbstractCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 22:03:01 $'
 * '$Revision: 1.7 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import javax.swing.JOptionPane;
import javax.xml.transform.TransformerException;

import java.awt.event.ActionEvent;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
//import edu.ucsb.nceas.morpho.framework.EMLTransformToNewestVersionDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

/**
 * Class to handle add title and abstract command
 */
public class AddTitleAbstractCommand
implements Command, DataPackageWizardListener {

  //generic name for lookup in eml listings
  private final String DATAPACKAGE_TITLE_GENERIC_NAME = "title";
  private final String DATAPACKAGE_ABSTRACT_GENERIC_NAME = "abstract";

  public AddTitleAbstractCommand() {}

  /**
   * execute add command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event) {


	  //Check if the eml document is the current version before editing it.
	  MorphoFrame frame = UIController.getInstance().getCurrentActiveWindow();
	  EMLTransformToNewestVersionDialog dialog = null;
	  try
	  {
		  dialog = new EMLTransformToNewestVersionDialog(frame, this);
	  }
	  catch(Exception e)
	  {
		  return;
	  }
	  if (dialog.getUserChoice() == JOptionPane.NO_OPTION)
	 {
		   // if user choose not transform it, stop the action.
			Log.debug(2,
					/*"The current EML document is not the latest version."*/ Language.getInstance().getMessages("EMLDocumentIsNotTheLatestVersion_1") + " "
					+/*" You should transform it first!"*/ Language.getInstance().getMessages("EMLDocumentIsNotTheLatestVersion_2") + "!"
					);
			return;
	 }
    
  }
  
  /**
   * Method from DataPackageWizardListener.
   * When correction wizard finished, it will show the dialog.
   */
  public void wizardComplete(Node newDOM, String autoSavedID)
  {
	  adp = UIController.getInstance().getCurrentAbstractDataPackage();

	    if (showTitleAbstractDialog()) {

	      try {
	        insertTitleAbstract();
	        UIController.showNewPackage(adp);
	      }
	      catch (Exception w) {
	        Log.debug(15,"Exception trying to modify title and abstract DOM: " + w);
	        w.printStackTrace();
	        Log.debug(5, "Unable to add title and abstract details!");
	      }
	    }
  }
  
  /**
   * Method from DataPackageWizardListener. Do nothing.
   */
  public void wizardCanceled()
  {
	  Log.debug(45, "Correction wizard cancled");
	  
  }
  
  /**
   *  Method from DataPackageWizardListener. Do nothing.
   *
   */
  public void wizardSavedForLater()
  {
    Log.debug(45, "Correction wizard was saved for later usage");
  }


  private boolean showTitleAbstractDialog() {

    ServiceController sc;
    DataPackageWizardInterface dpwPlugin = null;
    try {
      sc = ServiceController.getInstance();
      dpwPlugin = (DataPackageWizardInterface) sc.getServiceProvider(
          DataPackageWizardInterface.class);

    }
    catch (ServiceNotHandledException se) {

      Log.debug(6, se.getMessage());
      se.printStackTrace();
    }
    if (dpwPlugin == null) {
      return false;
    }

    titleAbstractPage = dpwPlugin.getPage(DataPackageWizardInterface.GENERAL);

    OrderedMap existingValuesMap = null;

    titleRoot = adp.getSubtree(DATAPACKAGE_TITLE_GENERIC_NAME, 0);
    abstractRoot = adp.getSubtree(DATAPACKAGE_ABSTRACT_GENERIC_NAME, 0);

    if (titleRoot != null) {
      existingValuesMap = XMLUtilities.getDOMTreeAsXPathMap(titleRoot);
    }
    if (abstractRoot != null) {
      existingValuesMap.putAll(XMLUtilities.getDOMTreeAsXPathMap(abstractRoot));
    }

    Log.debug(45,
              "sending previous data to titleAbstractPage -\n\n"
              + existingValuesMap);

    boolean pageCanHandleAllData
        = titleAbstractPage.setPageData(existingValuesMap, null);

    ModalDialog dialog = null;
    if (pageCanHandleAllData) {

      dialog = new ModalDialog(titleAbstractPage,
                               UIController.getInstance().
                               getCurrentActiveWindow(),
                               UISettings.POPUPDIALOG_WIDTH,
                               UISettings.POPUPDIALOG_HEIGHT);
    }
    else {

      UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
          DATAPACKAGE_TITLE_GENERIC_NAME, 0);
      return false;
    }

    return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
  }

  private void insertTitleAbstract() {

    OrderedMap map = titleAbstractPage.getPageData("");
    OrderedMap titleMap = new OrderedMap();
    OrderedMap abstractMap = new OrderedMap();
    boolean insertAbstract = false;

    Log.debug(45, "\n insertTitleAbstract() Got title & abstract details from "
         + "Title and Abstract page -\n" + map.toString());

    if (map == null || map.isEmpty()) {
      Log.debug(5, "Unable to get title & abstract details from input!");
      return;
    }
    titleMap.put("/title[1]", map.get("/title[1]"));
    map.remove("/title[1]");
    if(!map.isEmpty()) {
       String abstractKey = "/abstract/para[1]";
       String abstractValue = (String) map.get(abstractKey);

       if (abstractValue == null || abstractValue.equals("")){
           // make sure the abstract is not present in the map
           abstractMap.remove(abstractKey);
           adp.deleteSubtree(DATAPACKAGE_ABSTRACT_GENERIC_NAME, 0);

       } else {
           abstractMap.put(abstractKey, map.get(abstractKey));
           insertAbstract = true;
       }
    }

    DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
    Document doc = impl.createDocument("", "title", null);

    titleRoot = doc.getDocumentElement();

    try {
      XMLUtilities.getXPathMapAsDOMTree(titleMap, titleRoot);
    }
    catch (TransformerException w) {
      Log.debug(5, "Unable to add title details to package!");
      Log.debug(15, "TransformerException (" + w + ") calling "
                + "XMLUtilities.getXPathMapAsDOMTree(titleMap, titleRoot) with \n"
                + "map = " + titleMap
                + " and titleRoot = " + titleRoot);
      return;
    }
    //delete old title from datapackage
    adp.deleteSubtree(DATAPACKAGE_TITLE_GENERIC_NAME, 0);

    // add to the datapackage
    Node check = adp.insertSubtree(DATAPACKAGE_TITLE_GENERIC_NAME, titleRoot,
                                   0);
    if (check != null) {
      Log.debug(45, "added new title details to package...");
    }else {
      Log.debug(5, "** ERROR: Unable to add new title details to package **");
    }

    if(insertAbstract == true){
      DOMImplementation abstractImpl =
          DOMImplementationImpl.getDOMImplementation();
      Document abstractDoc = abstractImpl.createDocument("", "abstract", null);

      abstractRoot = abstractDoc.getDocumentElement();

      try {
        XMLUtilities.getXPathMapAsDOMTree(abstractMap, abstractRoot);
      }
      catch (TransformerException w) {
        Log.debug(5, "Unable to add abstract details to package!");
        Log.debug(15, "TransformerException (" + w + ") calling "
            +
            "XMLUtilities.getXPathMapAsDOMTree(abstractMap, abstractRoot) with \n"
            + "map = " + abstractMap
            + " and abstractRoot = " + abstractRoot);
        return;
      }
      //delete old abstract from datapackage
      adp.deleteSubtree(DATAPACKAGE_ABSTRACT_GENERIC_NAME, 0);

      // add to the datapackage
      check = adp.insertSubtree(DATAPACKAGE_ABSTRACT_GENERIC_NAME, abstractRoot,
          0);

      if (check != null) {
        Log.debug(45, "added new abstract details to package...");
      } else {
        Log.debug(5, "** ERROR: Unable to add new abstract details to package **");
      }
    }
  }

  private Node titleRoot;
  private Node abstractRoot;
  private AbstractDataPackage adp;
  private AbstractUIPage titleAbstractPage;
}
