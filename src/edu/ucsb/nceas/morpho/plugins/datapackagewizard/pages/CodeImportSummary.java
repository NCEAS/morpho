/**
 *  '$RCSfile: CodeImportSummary.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-02-06 19:46:02 $'
 * '$Revision: 1.2 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;


import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.HyperlinkButton;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AddDocumentationCommand;
import edu.ucsb.nceas.morpho.datapackage.AccessionNumber;
import edu.ucsb.nceas.morpho.datapackage.Entity;
import edu.ucsb.nceas.morpho.datapackage.Attribute;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;



public class CodeImportSummary extends AbstractWizardPage {

  public final String pageID     = DataPackageWizardInterface.CODE_IMPORT_SUMMARY;
  public String nextPageID 			 = DataPackageWizardInterface.CODE_IMPORT_PAGE;
  public final String pageNumber = "13";
  public final String PACKAGE_WIZ_SUMMARY_TITLE = "Data Package Wizard";
  public final String ENTITY_WIZ_SUMMARY_TITLE  = "New DataTable Wizard";
  public final String SUBTITLE                  = "Summary";

  private JLabel desc1;
  private JLabel desc4;
  private WizardContainerFrame mainWizFrame;
  private AbstractDataPackage adp = null;

  public CodeImportSummary(WizardContainerFrame mainWizFrame) {

    this.mainWizFrame = mainWizFrame;
    init();
  }


  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    this.add(WidgetFactory.makeDefaultSpacer());
    this.add(WidgetFactory.makeDefaultSpacer());
    this.add(WidgetFactory.makeDefaultSpacer());

    desc1 = WidgetFactory.makeHTMLLabel("", 2);
    this.add(desc1);

    JLabel desc3 = WidgetFactory.makeHTMLLabel(
        "<p>You can press the \"" + WizardSettings.FINISH_BUTTON_TEXT
        + "\" button, "
        + "or you can use the \"" + WizardSettings.PREV_BUTTON_TEXT
        + "\" button to return to previous pages "
        + "and change the information you have added.</p>", 2);
    this.add(desc3);

    desc4 = WidgetFactory.makeHTMLLabel("", 2);

    this.add(desc4);

    desc1.setAlignmentX(-1f);
    desc3.setAlignmentX(-1f);
    desc4.setAlignmentX(-1f);

    this.add(Box.createVerticalGlue());
  }

  private String getLastParagraph() {

    String ID = mainWizFrame.getFirstPageID();

    if (ID==null) return "";
		int remaining = mainWizFrame.getAttributeImportCount();
		if(remaining > 0) {
			return "<p>Proceed to define or import data tables for the other attributes</p>";
		} else {
			return "<p>All necessary imports have been completed</p>";
		}
	}
  
	
  private String getActionName() {

    String ID = mainWizFrame.getFirstPageID();
    if (ID==null) return "";
		if(ID.equals(DataPackageWizardInterface.CODE_IMPORT_PAGE))
			return "import the codes for the attribute <i>" + mainWizFrame.getCurrentImportAttributeName() + "</i>";
    else if (ID.equals(DataPackageWizardInterface.DATA_LOCATION)) 
			return "create your new data table";
    else return "create your new data package";
  }

  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
		
		String firstPageID = mainWizFrame.getFirstPageID();
		String prevID = mainWizFrame.getPreviousPageID();
		String currentAttrName = "";
		
		if(prevID.equals(DataPackageWizardInterface.CODE_DEFINITION)) {
			
			// need to update attribute in entity(if reqd) and remove attribute
			currentAttrName = mainWizFrame.getCurrentImportAttributeName();
			if(mainWizFrame.isCurrentImportNewTable())
				updateAttributeInNewTable();
			mainWizFrame.removeAttributeForImport();
			desc1.setText(
      WizardSettings.HTML_TABLE_LABEL_OPENING
      +"<p> The new data table has been created and the codes for the attribute " +
			"<i> "+ currentAttrName + "</i> have been imported</p>"
       +WizardSettings.HTML_TABLE_LABEL_CLOSING);
			
		} else if (prevID.equals(DataPackageWizardInterface.CODE_IMPORT_PAGE)) {
			
			// just a summary of import. No further imports
			desc1.setText(
      WizardSettings.HTML_TABLE_LABEL_OPENING
      +"<p>All the information required to import the codes for the attribute " +
			"<i> "+ currentAttrName + "</i> has been collected</p>"
       +WizardSettings.HTML_TABLE_LABEL_CLOSING);
			
		} else if( prevID.equals(DataPackageWizardInterface.TEXT_IMPORT_WIZARD)) {
			
			// this is a new data table creation. Need to store this DOM to return it.
			
			Node newDOM = mainWizFrame.collectDataFromPages();
			mainWizFrame.setDOMToReturn(null);
			if(adp == null)
				adp = getADP();
			
			Node entNode = null;
			String entityXpath = "";
			try{
				entityXpath = (XMLUtilities.getTextNodeWithXPath(adp.getMetadataPath(),
				"/xpathKeyMap/contextNode[@name='package']/entities")).getNodeValue();
				NodeList entityNodes = XMLUtilities.getNodeListWithXPath(newDOM,
				entityXpath);
				entNode = entityNodes.item(0);
			}
			catch (Exception w) {
				Log.debug(5, "Error in trying to get entNode in ImportDataCommand");
			}
			
			//              Entity entity = new Entity(newDOM);
			edu.ucsb.nceas.morpho.datapackage.Entity entityNode = 
			new edu.ucsb.nceas.morpho.datapackage.Entity(entNode);
			
			Log.debug(30,"Adding Entity object to AbstractDataPackage..");
			adp.addEntity(entityNode);
			
			// ---DFH
			Morpho morpho = Morpho.thisStaticInstance;
			AccessionNumber an = new AccessionNumber(morpho);
			String curid = adp.getAccessionNumber();
			String newid = null;
			if (!curid.equals("")) {
				newid = an.incRev(curid);
			} else {
				newid = an.getNextId();
			}
			adp.setAccessionNumber(newid);
			adp.setLocation("");  // we've changed it and not yet saved
			
			
		}
		
		desc4.setText( WizardSettings.HTML_TABLE_LABEL_OPENING
                  +getLastParagraph()+WizardSettings.HTML_TABLE_LABEL_CLOSING);
		
		updateButtonsStatus();
		
		
  }
	
	private AbstractDataPackage getADP() {
		
		DataViewContainerPanel resultPane = null;
		MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		AbstractDataPackage dp = null;
    if (morphoFrame != null) {
       resultPane = AddDocumentationCommand.
                          getDataViewContainerPanelFromMorphoFrame(morphoFrame);
    }
    if ( resultPane != null) {
       dp = resultPane.getAbstractDataPackage();
		}
		
		if(dp == null) {
			Log.debug(16, " Abstract Data Package is null in CodeImportSummary Page");
		}
		return dp;
	}
	
	private void updateAttributeInNewTable() {
		
		OrderedMap map = mainWizFrame.getCurrentImportMap();
		adp = getADP();
		if(adp == null)
			return;
		String eName = mainWizFrame.getCurrentImportEntityName();
		String aName = mainWizFrame.getCurrentImportAttributeName();
		String xPath = mainWizFrame.getCurrentImportXPath();
		
		int entityIndex = adp.getEntityIndex(eName);
		int attrIndex = adp.getAttributeIndex(entityIndex, aName);
		
		
		String firstKey = (String)map.keySet().iterator().next();
		
		if(!firstKey.startsWith("/attribute")) {
			OrderedMap newMap = new OrderedMap();
			Iterator it1 = map.keySet().iterator();
			while(it1.hasNext()) {
				String k = (String)it1.next();
				// get index of first '/' after 'attributeList'
				int idx1 = k.indexOf("/attributeList") + new String("/attributeList").length();
				// get the substring following the '/'. this starts with 'attribute'
				String tk = k.substring(idx1 + 1); //
				int idx2 = tk.indexOf("/");
				// remove the existing 'attribute' and add 'attribute' so that we handle cases
				// like 'attribute[2]'
				String newKey = "/attribute" + tk.substring(idx2); 
				newMap.put(newKey, (String)map.get(k));
			}
			map = newMap;
			xPath = "/attribute";
		}
		
		// get the ID of old attribute and set it for the new one
		String oldID = adp.getAttributeID(entityIndex, attrIndex);
		map.put(xPath + "/@id", oldID);
		
		/*System.out.println("New Keys in CIS page are - ");
		Iterator it = map.keySet().iterator();
		while(it.hasNext()) {
			String kk = (String) it.next();
			System.out.println(kk + " - " + (String)map.get(kk));
		}*/
		
		Attribute attr = new Attribute(map);
		
		adp.deleteAttribute(entityIndex, attrIndex);
		adp.insertAttribute(entityIndex, attr, attrIndex);
		
	}
	
	
	private void updateButtonsStatus() {
		
		if(mainWizFrame.getAttributeImportCount() > 0) {
			mainWizFrame.setButtonsStatus(true, true, false);
		} else {
			mainWizFrame.setButtonsStatus(true, false, true);
		}
	}
	
	
  private String getDataLocation() {

    String summaryText = WizardSettings.getSummaryText();
    if (summaryText!=null
            && (   summaryText.equals(WizardSettings.SUMMARY_TEXT_ONLINE)
                || summaryText.equals(WizardSettings.SUMMARY_TEXT_INLINE)) ) {

      String loc = WizardSettings.getDataLocation();
      if (loc!=null) return "<span style=\"text-decoration: underline;\">"+loc
                                                                    +"</span>";
    }
    return "";
  }


  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {

  }


  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty, but if so, must
   *  return true
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {
		
		if(nextPageID.equals(DataPackageWizardInterface.CODE_IMPORT_PAGE))
			WizardPageLibrary.reInitialize();
		
		return true; 
	}


  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  public OrderedMap getPageData() {

    return null;
  }




  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return pageID; }

  /**
   *  gets the title for this wizard page
   *


  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public String getTitle() {

    if (mainWizFrame.getFirstPageID()
        == DataPackageWizardInterface.DATA_LOCATION) {
      //if we started at DATA_LOCATION we must be in entity wizard
      return ENTITY_WIZ_SUMMARY_TITLE;
    }
    return PACKAGE_WIZ_SUMMARY_TITLE;
  }


  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public String getSubtitle() {

    return SUBTITLE;
  }

  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID() { return nextPageID; }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  public void setPageData(OrderedMap data) { }
}

