/**
 *  '$RCSfile: AddTaxonomicCovCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Perumal Sambasivam
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-23 21:16:46 $'
 * '$Revision: 1.20 $'
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

//import edu.ucsb.nceas.morpho.framework.EMLTransformToNewestVersionDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.event.ActionEvent;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.util.Iterator;
import javax.swing.JOptionPane;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Class to handle addition of taxonomic coverage to the data package
 */
public class AddTaxonomicCovCommand implements Command {

  private AbstractUIPage taxonomicPage;


  private OrderedMap existingValuesMap;

  private final String TAXONOMIC_COVERAGE_SUBTREE_NODENAME =
      "taxonomicCoverage";

  public AddTaxonomicCovCommand() {

    existingValuesMap = null;
  }


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
		  dialog = new EMLTransformToNewestVersionDialog(frame);
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
    showTaxonomicDialog();

  }


  private void showTaxonomicDialog() {

    ServiceController sc;
    DataPackageWizardInterface dpwPlugin = null;

    try {
      sc = ServiceController.getInstance();
      dpwPlugin = (DataPackageWizardInterface) sc.getServiceProvider(
          DataPackageWizardInterface.class);
    }
    catch (ServiceNotHandledException se) {
      Log.debug(6, se.getMessage());
    }

    if (dpwPlugin == null) {
      return;
    }


    taxonomicPage = dpwPlugin.getPage(DataPackageWizardInterface.TAXONOMIC);

     boolean pageCanHandleAllData = backupAndDisplayCurrentData();

    if (pageCanHandleAllData) {

      ModalDialog wpd = new ModalDialog(taxonomicPage,
                                UIController.getInstance().getCurrentActiveWindow(),
                                UISettings.WIZARD_WIDTH,
                                UISettings.WIZARD_HEIGHT, false);

      wpd.setSize(UISettings.WIZARD_WIDTH, UISettings.WIZARD_HEIGHT);
      wpd.setVisible(true);

      if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {

        insertTaxonomicNode(
          taxonomicPage.getPageData("/coverage/taxonomicCoverage[1]"));

      } else {

        //gets here if user has pressed "cancel" on dialog... ////////////////////

        AbstractDataPackage adp = UIController.getInstance().
                                  getCurrentAbstractDataPackage();

        //Restore project subtree to state it was in when we started...

        adp.removeTaxonomicNodes();
				if (existingValuesMap != null) {
					OrderedMap newMap = preprendKeysWithString(existingValuesMap, "/coverage");
					insertTaxonomicNode(newMap);
				}
      }

    } else {

      UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
          "coverage", 0);
    }

    return;
  }

  private void insertTaxonomicNode(OrderedMap map) {

    //OrderedMap map = taxonomicPage.getPageData("/coverage/taxonomicCoverage[1]");
    AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
		Node covRoot = null;
		
		if(adp == null) return;
    try {
      DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
      Document doc = impl.createDocument("", "coverage", null);

      covRoot = doc.getDocumentElement();
      XMLUtilities.getXPathMapAsDOMTree(map, covRoot);
      NodeList kids = covRoot.getChildNodes();
      adp.removeTaxonomicNodes();
      for (int i=0;i<kids.getLength();i++) {
        Node kid = kids.item(i);
        adp.insertCoverage(kid);
      }
    }
    catch (Exception w) {
      Log.debug(5, "Unable to add OrderMap elements to DOM");
      w.printStackTrace();
    }

    UIController.showNewPackage(adp);

  }


//	private boolean insertCurrentDatazz() {
//
//		AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
//
//		if(adp == null) return false;
//    NodeList taxonList = adp.getTaxonomicNodeList();
//
//    if (taxonList==null) {
//      Log.debug(45, "\n no taxon data in datapackage yet");
//
//      return true;
//    }
//    int totTaxa = taxonList.getLength();
//
//    if (totTaxa!=1) Log.debug(45, "More than 1 taxon definition found!!!!");
//
//     OrderedMap taxonMap = XMLUtilities.getDOMTreeAsXPathMap(taxonList.item(0));
//     boolean flag = taxonomicPage.setPageData(taxonMap, "");
//     if (!flag) return false;
//
//    return true;
//  }

	private OrderedMap preprendKeysWithString(OrderedMap map, String prefix) {
		
		if(map == null) return null;
		OrderedMap newMap = new OrderedMap();
		if(map.size() < 1) return newMap;
		Iterator it = map.keySet().iterator();
		while(it.hasNext()) {
			String key = (String)it.next();
			newMap.put(prefix + key, map.get(key));
			
		}
		return newMap;
	}
  private OrderedMap getTaxonSubtreeMap(AbstractDataPackage adp) {

    NodeList taxonList = adp.getTaxonomicNodeList();

    if (taxonList==null) {
      Log.debug(45, "\n no taxon data in datapackage yet");

      return null;
    }
    int totTaxa = taxonList.getLength();

    if (totTaxa!=1) Log.debug(45, "More than 1 taxon definition found!!!!");

    OrderedMap map = XMLUtilities.getDOMTreeAsXPathMap(taxonList.item(0));
		
		return map;
  }



  private boolean backupAndDisplayCurrentData() {

    AbstractDataPackage adp = UIController.getInstance().
                              getCurrentAbstractDataPackage();

    if (adp == null) return false;

    //backup subtree so it can be restored if user hits cancel:
    existingValuesMap = getTaxonSubtreeMap(adp);
		
    if (existingValuesMap == null) {

      //there wasn't a project subtree in the datapackage, so add one
      //(required for writing references) - if user hits cancel, we'll
      //delete it again

      Log.debug(45, "No taxon subtree in the datapackage, so adding one...");

      DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
      Document doc = impl.createDocument(
          "", TAXONOMIC_COVERAGE_SUBTREE_NODENAME, null);

      //need to add in a dummy title node under:
      // taxonomicCoverage/taxonomicSystem/classificationSystem/classificationSystemCitation/
      //so dan's insertion stuff works...
      Node blankTaxonRoot = doc.getDocumentElement();
      Node sysNode = doc.createElement("taxonomicSystem");
      blankTaxonRoot.appendChild(sysNode);
      Node classNode = doc.createElement("classificationSystem");
      sysNode.appendChild(classNode);
      Node citeNode = doc.createElement("classificationSystemCitation");
      classNode.appendChild(citeNode);
      Node titleNode = doc.createElement("title");
      citeNode.appendChild(titleNode);

      Log.debug(45,
                "\n\nblankTaxonRoot: " + XMLUtilities.getDOMTreeAsString(blankTaxonRoot));

      try {
        adp.insertCoverage(blankTaxonRoot);

        Log.debug(45, "\n\nadp after insertion of blank taxon subtree: " + adp);

      } catch (Throwable t) {
        Log.debug(45, "** ERROR: AddTaxonomicCovCommand, "
                  + "trying to add blankTaxonRoot: "+t);
        t.printStackTrace();
        return false;
      }

    } else {

      //there was already a project subtree in the datapackage, so read it...

      Log.debug(45, "Found project subtree in the datapackage; reading...");
		}
		
    Log.debug(45, "sending previous data to projectPage -\n\n"
              + existingValuesMap);
		
		OrderedMap newMap = null;
		if(existingValuesMap != null) {
			newMap = this.preprendKeysWithString(existingValuesMap, "");
		}
		
    boolean pageCanHandleAllData
        = taxonomicPage.setPageData(newMap,
                                    "/" + TAXONOMIC_COVERAGE_SUBTREE_NODENAME);
		
		return pageCanHandleAllData;
  }

}
