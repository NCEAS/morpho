/**
 *  '$RCSfile: AddAdditionalPartyCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 22:03:01 $'
 * '$Revision: 1.6 $'
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
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
//import edu.ucsb.nceas.morpho.framework.EMLTransformToNewestVersionDialog;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Class to handle add additionalParty command
 */
public class AddAdditionalPartyCommand
implements Command, DataPackageWizardListener {

  //generic name for lookup in eml listings
  private final String DATAPACKAGE_ASSOCIATED_PARTY_GENERIC_NAME
      = "associatedParty";

  public AddAdditionalPartyCommand() {}

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
			Log.debug(2,"The current EML document is not the latest version. You should transform it first!");
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
	    exsitingAssociatedPartyRoot =
	        adp.getSubtrees(DATAPACKAGE_ASSOCIATED_PARTY_GENERIC_NAME);

	    if (showAdditionalPartyDialog()) {

	      try {
	        insertAdditionalParty();
	        UIController.showNewPackage(adp);
	      }
	      catch (Exception w) {
	        Log.debug(15, "Exception trying to modify additionalParty DOM: " + w);
	        w.printStackTrace();
	        Log.debug(5, "Unable to add additionalParty details!");
	      }
	    } else {
	      //gets here if user has pressed "cancel" on dialog... ////////////////////

	      //Restore project subtree to state it was in when we started...
	      adp.deleteAllSubtrees(DATAPACKAGE_ASSOCIATED_PARTY_GENERIC_NAME);
	      if (!exsitingAssociatedPartyRoot.isEmpty()) {
	        Object nextXPathObj = null;

	        int count = exsitingAssociatedPartyRoot.size();
	        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
	        for (int i = count - 1; i > -1; i--) {
	          associatedPartyRoot = (Node) exsitingAssociatedPartyRoot.get(i);
	          Node check = adp.insertSubtree(
	              DATAPACKAGE_ASSOCIATED_PARTY_GENERIC_NAME,
	              associatedPartyRoot, 0);
	          if (check != null) {
	            Log.debug(45, "added new creator details to package...");
	          } else {
	            Log.debug(5,
	                "** ERROR: Unable to add new creator details to package **");
	          }

	        }
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

  private boolean showAdditionalPartyDialog() {

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

    associatedPartyPage = dpwPlugin.getPage(
        DataPackageWizardInterface.PARTY_ASSOCIATED_PAGE);

    OrderedMap existingValuesMap = new OrderedMap();

    List additionalPartyList = adp.getSubtrees(
        DATAPACKAGE_ASSOCIATED_PARTY_GENERIC_NAME);
    existingValuesMap = Util.getOrderedMapFromNodeList(additionalPartyList, DATAPACKAGE_ASSOCIATED_PARTY_GENERIC_NAME);
    Log.debug(45, "sending previous data to associatedPartyPage -\n\n"
        + existingValuesMap);

    boolean pageCanHandleAllData
        = associatedPartyPage.setPageData(existingValuesMap, null);

    ModalDialog dialog = null;
    if (pageCanHandleAllData) {

      dialog = new ModalDialog(associatedPartyPage,
          UIController.getInstance().
          getCurrentActiveWindow(),
          UISettings.POPUPDIALOG_WIDTH,
          UISettings.POPUPDIALOG_HEIGHT);
    } else {

      UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
          DATAPACKAGE_ASSOCIATED_PARTY_GENERIC_NAME, 0);
      return false;
    }

    return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
  }

  private void insertAdditionalParty() {

    OrderedMap map = associatedPartyPage.getPageData("/associatedParty[");
    Log.debug(45,
        "\n insertAdditionalParty() Got additionalParty details from "
        + "additionalParty page -\n" + map.toString());

    if (map == null) {
      Log.debug(5, "Unable to get additionalParty details from input!");
      return;
    } else if (map.isEmpty()) {
      Log.debug(45, "Deleting all associated party details!");
    }

    DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
    //delete old title from datapackage
    adp.deleteAllSubtrees(DATAPACKAGE_ASSOCIATED_PARTY_GENERIC_NAME);

    Iterator additionalPartyIt = map.keySet().iterator();
    Object nextXPathObj = null;
    String nextXPath = null;
    HashMap additionalPartySetMap = new HashMap();

    while (additionalPartyIt.hasNext()) {
      nextXPathObj = additionalPartyIt.next();
      if (nextXPathObj == null) {
        continue;
      }
      nextXPath = (String) nextXPathObj;

      String temp = nextXPath.substring(
          DATAPACKAGE_ASSOCIATED_PARTY_GENERIC_NAME.length()
          + 2, nextXPath.length());
      temp = temp.substring(0, temp.indexOf("]"));
      nextXPath = nextXPath.replaceFirst(temp, "1");

      if (additionalPartySetMap.containsKey(temp)) {
        OrderedMap additionalPartyMap = (OrderedMap) additionalPartySetMap.get(
            temp);
        additionalPartyMap.put(nextXPath, map.get(nextXPathObj));
      } else {
        OrderedMap additionalPartyMap = new OrderedMap();
        additionalPartyMap.put(nextXPath, map.get(nextXPathObj));
        additionalPartySetMap.put(temp, additionalPartyMap);
      }
    }

    Iterator additionalPartySetIt = additionalPartySetMap.keySet().iterator();
    while (additionalPartySetIt.hasNext()) {
      nextXPathObj = additionalPartySetIt.next();
      OrderedMap additionalPartyMap = (OrderedMap) additionalPartySetMap.get(
          nextXPathObj);
      Document doc = impl.createDocument("", "associatedParty", null);
      associatedPartyRoot = doc.getDocumentElement();

      try {
        XMLUtilities.getXPathMapAsDOMTree(additionalPartyMap,
            associatedPartyRoot);
      }
      catch (TransformerException w) {
        Log.debug(5, "Unable to add additionalParty details to package!");
        Log.debug(15, "TransformerException (" + w + ") calling "
            +
            "XMLUtilities.getXPathMapAsDOMTree(map, associatedPartyRoot) with \n"
            + "map = " + map
            + " and associatedPartyRoot = " + associatedPartyRoot);
        return;
      }

      // add to the datapackage
      Node check = adp.insertSubtree(DATAPACKAGE_ASSOCIATED_PARTY_GENERIC_NAME,
          associatedPartyRoot,
          0);
      if (check != null) {
        Log.debug(45, "added new additionalParty details to package...");
      } else {
        Log.debug(5,
            "** ERROR: Unable to add new additionalParty details to package **");
      }
    }
  }

  private List exsitingAssociatedPartyRoot;
  private Node associatedPartyRoot;
  private AbstractDataPackage adp;
  private AbstractUIPage associatedPartyPage;
}
