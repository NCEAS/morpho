/**
 *  '$RCSfile: AddContactCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2005-01-27 16:27:15 $'
 * '$Revision: 1.1 $'
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

import javax.xml.transform.TransformerException;

import java.awt.event.ActionEvent;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Class to handle add contact command
 */
public class AddContactCommand
    implements Command {

  //generic name for lookup in eml listings
  private final String DATAPACKAGE_CONTACT_GENERIC_NAME = "contact";

  public AddContactCommand() {}

  /**
   * execute add command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event) {

    adp = UIController.getInstance().getCurrentAbstractDataPackage();

    if (showContactDialog()) {

      try {
        insertContact();
        UIController.showNewPackage(adp);
      }
      catch (Exception w) {
        Log.debug(15, "Exception trying to modify contact DOM: " + w);
        w.printStackTrace();
        Log.debug(5, "Unable to add contact details!");
      }
    }
  }

  private boolean showContactDialog() {

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

    contactPage = dpwPlugin.getPage(
        DataPackageWizardInterface.PARTY_CONTACT_PAGE);

    OrderedMap existingValuesMap = new OrderedMap();

    List contactList = adp.getSubtrees(DATAPACKAGE_CONTACT_GENERIC_NAME);

    if (!contactList.isEmpty()) {
      Iterator listIt = contactList.iterator();
      Object nextObj = null;
      Object nextTempObj = null;
      String nextTempString = null;
      int count = 1;

      while (listIt.hasNext()) {
        nextObj = listIt.next();
        OrderedMap tempMap = XMLUtilities.getDOMTreeAsXPathMap( (Node) nextObj);
        Iterator tempIt = tempMap.keySet().iterator();
        while (tempIt.hasNext()) {
          nextTempObj = tempIt.next();
          nextTempString = (String) nextTempObj;
          if (nextTempString != null) {
            existingValuesMap.put("/" + DATAPACKAGE_CONTACT_GENERIC_NAME + "["
                + count + "]" + nextTempString.substring(
                DATAPACKAGE_CONTACT_GENERIC_NAME.length() + 1,
                nextTempString.length()),
                tempMap.get(nextTempObj));
          }
        }
        count++;
      }
    }

    Log.debug(45, "sending previous data to contactPage -\n\n"
        + existingValuesMap);

    boolean pageCanHandleAllData
        = contactPage.setPageData(existingValuesMap, null);

    ModalDialog dialog = null;
    if (pageCanHandleAllData) {

      dialog = new ModalDialog(contactPage,
          UIController.getInstance().
          getCurrentActiveWindow(),
          UISettings.POPUPDIALOG_WIDTH,
          UISettings.POPUPDIALOG_HEIGHT);
    } else {

      UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
          DATAPACKAGE_CONTACT_GENERIC_NAME, 0);
      return false;
    }

    return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
  }

  private void insertContact() {

    OrderedMap map = contactPage.getPageData("/contact[");
    Log.debug(45, "\n insertContact() Got contact details from "
        + "contact page -\n" + map.toString());

    if (map == null || map.isEmpty()) {
      Log.debug(5, "Unable to get contact details from input!");
      return;
    }

    DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
    //delete old title from datapackage
    adp.deleteAllSubtrees(DATAPACKAGE_CONTACT_GENERIC_NAME);

    Iterator contactIt = map.keySet().iterator();
    Object nextXPathObj = null;
    String nextXPath = null;
    HashMap contactSetMap = new HashMap();

    while (contactIt.hasNext()) {
      nextXPathObj = contactIt.next();
      if (nextXPathObj == null) {
        continue;
      }
      nextXPath = (String) nextXPathObj;

      String temp = nextXPath.substring(
          DATAPACKAGE_CONTACT_GENERIC_NAME.length() + 2, nextXPath.length());
      temp = temp.substring(0, temp.indexOf("]"));
      nextXPath = nextXPath.replaceFirst(temp, "1");

      if (contactSetMap.containsKey(temp)) {
        OrderedMap contactMap = (OrderedMap) contactSetMap.get(temp);
        contactMap.put(nextXPath, map.get(nextXPathObj));
      } else {
        OrderedMap contactMap = new OrderedMap();
        contactMap.put(nextXPath, map.get(nextXPathObj));
        contactSetMap.put(temp, contactMap);
      }
    }

    Iterator contactSetIt = contactSetMap.keySet().iterator();
    while (contactSetIt.hasNext()) {
      nextXPathObj = contactSetIt.next();
      OrderedMap contactMap = (OrderedMap) contactSetMap.get(nextXPathObj);
      Document doc = impl.createDocument("", "contact", null);
      contactRoot = doc.getDocumentElement();

      try {
        XMLUtilities.getXPathMapAsDOMTree(contactMap, contactRoot);
      }
      catch (TransformerException w) {
        Log.debug(5, "Unable to add contact details to package!");
        Log.debug(15, "TransformerException (" + w + ") calling "
            + "XMLUtilities.getXPathMapAsDOMTree(map, contactRoot) with \n"
            + "map = " + map
            + " and contactRoot = " + contactRoot);
        return;
      }

      // add to the datapackage
      Node check = adp.insertSubtree(DATAPACKAGE_CONTACT_GENERIC_NAME,
          contactRoot,
          0);
      if (check != null) {
        Log.debug(45, "added new contact details to package...");
      } else {
        Log.debug(5,
            "** ERROR: Unable to add new contact details to package **");
      }
    }
  }

  private Node contactRoot;
  private AbstractDataPackage adp;
  private AbstractUIPage contactPage;
}