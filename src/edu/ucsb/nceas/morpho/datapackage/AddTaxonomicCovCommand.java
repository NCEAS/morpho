/**
 *  '$RCSfile: AddTaxonomicCovCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Perumal Sambasivam
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-04-01 17:37:49 $'
 * '$Revision: 1.10 $'
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

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Class to handle addition of taxonomic coverage to the data package
 */
public class AddTaxonomicCovCommand implements Command {

  /* Flag if need to add coverage info*/
  private boolean infoAddFlag = false;

  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  private DataViewContainerPanel resultPane;
  private AbstractUIPage taxonomicPage;
  private DataViewer dataView;
	private OrderedMap map;
	
  public AddTaxonomicCovCommand() {
		map = new OrderedMap();
  }


  /**
   * execute add command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event) {
		
		
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
		
		
    taxonomicPage = dpwPlugin.getPage(
        DataPackageWizardInterface.TAXONOMIC);
				
		AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
		Node taxonomicRoot = adp.getSubtree("taxonomicCoverage", 0);

    if (taxonomicRoot!=null) {
      map = XMLUtilities.getDOMTreeAsXPathMap(taxonomicRoot);
    } else {
			map = new OrderedMap();
		}
		
		taxonomicPage.setPageData(map, "/taxonomicCoverage");
		
    ModalDialog wpd = new ModalDialog(taxonomicPage,
                                UIController.getInstance().getCurrentActiveWindow(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT, false);

    wpd.setSize(UISettings.POPUPDIALOG_WIDTH, UISettings.POPUPDIALOG_HEIGHT);
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {
			
			map = taxonomicPage.getPageData("/coverage/taxonomicCoverage");
			Iterator it = map.keySet().iterator();
			while(it.hasNext()) {
				String k = (String)it.next();
				System.out.println(k + " - " + (String)map.get(k));
			}
			
			Node covRoot = null;
			try {
				DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
				Document doc = impl.createDocument("", "coverage", null);
				
				covRoot = doc.getDocumentElement();
				XMLUtilities.getXPathMapAsDOMTree(map, covRoot);
				// now the covRoot node may have a number of temporalCoverage children
				NodeList kids = covRoot.getChildNodes();
				adp.removeTaxonomicNodes();
				for (int i=0;i<kids.getLength();i++) {
					Node kid = kids.item(i);
					adp.insertCoverage(kid);          
					System.out.println("Insetrting child " + i);
				}
			}
			catch (Exception w) {
				Log.debug(5, "Unable to add OrderMap elements to DOM");
				w.printStackTrace();
			}
    
			//taxonomicPage.setPageData(map, "/coverage/taxonomicCoverage");
      UIController.showNewPackage(adp);
    }
    else {

    }

    return;
  }

}
