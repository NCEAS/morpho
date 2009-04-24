/**
 *  '$RCSfile: AddResearchProjectCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 22:03:01 $'
 * '$Revision: 1.19 $'
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
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.DataPackageWizardPlugin;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.awt.event.ActionEvent;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JOptionPane;
/**
 * Class to handle add project command
 */
public class AddResearchProjectCommand implements Command, DataPackageWizardListener {

  //generic name for lookup in eml listings
  private final String DATAPACKAGE_PROJECT_GENERIC_NAME = "project";

  //generic name for lookup in eml listings
  private final String PROJECT_SUBTREE_NODENAME = "/project/";

  public AddResearchProjectCommand() {}


  /**
   * execute add command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event) {

    ServiceController sc;
    DataPackageWizardInterface dpwPlugin = null;
    try {
      sc = ServiceController.getInstance();
      dpwPlugin = (DataPackageWizardInterface) sc.getServiceProvider(
          DataPackageWizardInterface.class);

    } catch (ServiceNotHandledException se) {

        Log.debug(6, "unable to start project editor!");
        se.printStackTrace();
        return;
    }
    if (dpwPlugin == null) return;

    projectPage = dpwPlugin.getPage(DataPackageWizardInterface.PROJECT);

    //Check if the eml document is the current version before editing it.
	  MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
	  EMLTransformToNewestVersionDialog dialog = null;
	  try
	  {
		  dialog = new EMLTransformToNewestVersionDialog(morphoFrame, this);
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
  public void wizardComplete(Node newDOM)
  {
	  adp = UIController.getInstance().getCurrentAbstractDataPackage();

	    if (backupSubtreeAndShowProjectDialog()) {

	      //gets here if user has pressed "OK" on dialog... ////////////////////////

	      final MorphoFrame frame
	          = UIController.getInstance().getCurrentActiveWindow();

	      final SwingWorker worker = new SwingWorker() {

	        public Object construct() {

	          if (frame!=null) {
	            frame.setBusy(true);
	            frame.setEnabled(false);
	          }
	          try {
	            //replace project in datapackage...
	            List pagesList = new ArrayList();
	            pagesList.add(projectPage);

	            DataPackageWizardPlugin.deleteExistingAndAddPageDataToDOM(
	                UIController.getInstance().getCurrentAbstractDataPackage(),
	                pagesList, PROJECT_SUBTREE_NODENAME,
	                DATAPACKAGE_PROJECT_GENERIC_NAME);

	          } catch (Exception w) {
	            Log.debug(15, "Exception trying to modify project DOM: " + w);
	            w.printStackTrace();
	            Log.debug(5, "Unable to add project details!");
	          }
	          return null;
	        }

	        //Runs on the event-dispatching thread.
	        public void finished()
	        {
	          if (frame!=null) {
	            frame.setBusy(false);
	            frame.setEnabled(true);

	            //update package display in main frame...
	            UIController.showNewPackage(adp);
	          }
	        }
	      };
	      worker.start();

	    } else {
	      //gets here if user has pressed "cancel" on dialog... ////////////////////

	      //Restore project subtree to state it was in when we started...
	      if (existingProjectRoot==null) {

	        adp.deleteSubtree(DATAPACKAGE_PROJECT_GENERIC_NAME, 0);

	      } else {

	        adp.replaceSubtree(
	            DATAPACKAGE_PROJECT_GENERIC_NAME, existingProjectRoot, 0);
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


  private boolean backupSubtreeAndShowProjectDialog() {

    //backup subtree so it can be restored if user hits cancel:
    existingProjectRoot = adp.getSubtree(DATAPACKAGE_PROJECT_GENERIC_NAME, 0);

    OrderedMap existingValuesMap = null;

    if (existingProjectRoot==null) {

      //there wasn't a project subtree in the datapackage, so add one
      //(required for writing references) - if user hits cancel, we'll
      //delete it again

      Log.debug(45, "No project subtree in the datapackage, so adding one...");

      DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
      Document doc = impl.createDocument(
          "", DATAPACKAGE_PROJECT_GENERIC_NAME, null);

      Node blankProjectRoot = doc.getDocumentElement();
      Node titleNode = doc.createElement("title");
      blankProjectRoot.appendChild(titleNode);

      Log.debug(45, "\n\nblankProjectRoot: "+XMLUtilities.getDOMTreeAsString(blankProjectRoot));

      Node check = adp.insertSubtree(DATAPACKAGE_PROJECT_GENERIC_NAME,
                                     blankProjectRoot, 0);

      Log.debug(45, "\n\nadp: "+adp);

      if (check == null) {
        Log.debug(45, "** ERROR: AddResearchProjectCommand, "
                  +"trying to add blankProjectRoot");
        Log.debug(5, "** ERROR: Unable to open project editor");
        return false;
      }

    } else {

      //there was already a project subtree in the datapackage, so read it...

      Log.debug(45, "Found project subtree in the datapackage; reading...");

      existingValuesMap = XMLUtilities.getDOMTreeAsXPathMap(existingProjectRoot);
    }

    //show project dialog:
    Log.debug(45, "sending previous data to projectPage -\n\n"
              + existingValuesMap);

    boolean pageCanHandleAllData
        = projectPage.setPageData(existingValuesMap, PROJECT_SUBTREE_NODENAME);

    ModalDialog dialog = null;
    if (pageCanHandleAllData) {

      dialog = new ModalDialog(projectPage,
                               UIController.getInstance().
                               getCurrentActiveWindow(),
                               UISettings.POPUPDIALOG_WIDTH,
                               UISettings.POPUPDIALOG_HEIGHT);
    } else {

      UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
          DATAPACKAGE_PROJECT_GENERIC_NAME, 0);
      return false;
    }

    return (dialog.USER_RESPONSE==ModalDialog.OK_OPTION);
  }


  private Node existingProjectRoot;
  private AbstractDataPackage adp;
  private AbstractUIPage projectPage;
}
