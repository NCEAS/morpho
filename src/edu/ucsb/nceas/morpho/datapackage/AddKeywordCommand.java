/**
 *  '$RCSfile: AddKeywordCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-10-14 01:00:44 $'
 * '$Revision: 1.4 $'
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
import javax.swing.JOptionPane;

import java.awt.event.ActionEvent;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.EMLTransformToNewestVersionDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
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
import java.util.Iterator;
import java.util.Stack;
import java.util.HashMap;
import java.util.List;

/**
 * Class to handle add keyword command
 */
public class AddKeywordCommand
    implements Command {

  //generic name for lookup in eml listings
  private final String DATAPACKAGE_KEYWORD_GENERIC_NAME = "keywordSet";

  public AddKeywordCommand() {}

  /**
   * execute add command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event) {

	MorphoFrame frame = UIController.getInstance().getCurrentActiveWindow();
	EMLTransformToNewestVersionDialog dialog = new EMLTransformToNewestVersionDialog(frame);
	if (dialog.getUserChoice() == JOptionPane.NO_OPTION)
	{
		Log.debug(2,"The current EML document is not the latest version. You should transform it first!");
		return;
	}
    adp = UIController.getInstance().getCurrentAbstractDataPackage();
	

    if (showKeywordDialog()) {

      try {
        insertKeyword();
        UIController.showNewPackage(adp);
      }
      catch (Exception w) {
        Log.debug(15,"Exception trying to modify keyword DOM: " + w);
        w.printStackTrace();
        Log.debug(5, "Unable to add keyword details!");
      }
    }
  }

  private boolean showKeywordDialog() {

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

    keywordPage = dpwPlugin.getPage(DataPackageWizardInterface.KEYWORDS);

    OrderedMap existingValuesMap = new OrderedMap();

    List keywordList = adp.getSubtrees(DATAPACKAGE_KEYWORD_GENERIC_NAME);

    if(!keywordList.isEmpty()){
      Iterator listIt = keywordList.iterator();
      Object nextObj = null;
      Object nextTempObj = null;
      String nextTempString = null;
      int count = 1;

      while (listIt.hasNext()) {
        nextObj = listIt.next();
        OrderedMap tempMap = XMLUtilities.getDOMTreeAsXPathMap((Node)nextObj);
        Iterator tempIt = tempMap.keySet().iterator();
        while(tempIt.hasNext()){
          nextTempObj = tempIt.next();
          nextTempString = (String)nextTempObj;
          if(nextTempString != null){
            existingValuesMap.put("/" + DATAPACKAGE_KEYWORD_GENERIC_NAME + "["
                + count + "]" + nextTempString.substring(
                DATAPACKAGE_KEYWORD_GENERIC_NAME.length() + 1,
                nextTempString.length()),
                tempMap.get(nextTempObj));
          }
        }
        count++;
      }
    }

    Log.debug(45, "sending previous data to keywordPage -\n\n"
              + existingValuesMap);

    boolean pageCanHandleAllData
        = keywordPage.setPageData(existingValuesMap, null);

    ModalDialog dialog = null;
    if (pageCanHandleAllData) {

      dialog = new ModalDialog(keywordPage,
                               UIController.getInstance().
                               getCurrentActiveWindow(),
                               UISettings.POPUPDIALOG_WIDTH,
                               UISettings.POPUPDIALOG_HEIGHT);
    }
    else {

      UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
          DATAPACKAGE_KEYWORD_GENERIC_NAME, 0);
      return false;
    }

    return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
  }

  private void insertKeyword() {

    OrderedMap map = keywordPage.getPageData("/keywordSet[");

    if (map == null) {
      Log.debug(5, "Unable to get keyword details from input!");
      return;
    } else if (map.isEmpty()){
      Log.debug(45, "Empty map returned. Deleting all previous keywords!");
    }

    Log.debug(45, "\n insertKeyword() Got keyword details from "
         + "Keyword page -\n" + map.toString());

    DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
    //delete old title from datapackage
    adp.deleteAllSubtrees(DATAPACKAGE_KEYWORD_GENERIC_NAME);

    Iterator keyIt = map.keySet().iterator();
    Object nextXPathObj = null;
    String nextXPath = null;
    HashMap keySetMap = new HashMap();

    while(keyIt.hasNext()){
      nextXPathObj = keyIt.next();
      if (nextXPathObj == null) {
        continue;
      }
      nextXPath = (String) nextXPathObj;
      String temp = nextXPath.substring(12, nextXPath.length());
      temp = temp.substring(0, temp.indexOf("]"));
      nextXPath = nextXPath.replaceFirst(temp, "1");
      if(keySetMap.containsKey(temp)){
        OrderedMap keyMap = (OrderedMap)keySetMap.get(temp);
        keyMap.put(nextXPath, map.get(nextXPathObj));
      } else {
        OrderedMap keyMap = new OrderedMap();
        keyMap.put(nextXPath, map.get(nextXPathObj));
        keySetMap.put(temp, keyMap);
      }
    }

    Iterator keySetIt = keySetMap.keySet().iterator();
    while(keySetIt.hasNext()){
      nextXPathObj = keySetIt.next();
      OrderedMap keyMap = (OrderedMap)keySetMap.get(nextXPathObj);
      Document doc = impl.createDocument("", "keywordSet", null);
      keywordRoot = doc.getDocumentElement();

      try {
        XMLUtilities.getXPathMapAsDOMTree(keyMap, keywordRoot);
      }
      catch (TransformerException w) {
        Log.debug(5, "Unable to add keyword details to package!");
        Log.debug(15, "TransformerException (" + w + ") calling "
            + "XMLUtilities.getXPathMapAsDOMTree(keywordMap, keywordRoot) with \n"
            + "map = " + map + " and keywordRoot = " + keywordRoot);
        return;
      }
      // add to the datapackage
      Node check = adp.insertSubtree(DATAPACKAGE_KEYWORD_GENERIC_NAME,
          keywordRoot, 0);

      if (check != null) {
        Log.debug(45, "added new title details to package...");
      }else {
        Log.debug(5, "** ERROR: Unable to add new title details to package **");
      }

    }
  }

  private Node keywordRoot;
  private AbstractDataPackage adp;
  private AbstractUIPage keywordPage;
}
