/**
 *  '$RCSfile: CodeDefinition.java,v $'
 *    Purpose: A class that handles the importing of new tables for taxonomical
 *						lookup for attributes
 *  	Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
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
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;

import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AddDocumentationCommand;
import edu.ucsb.nceas.morpho.datapackage.AccessionNumber;
import edu.ucsb.nceas.morpho.datapackage.Entity;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;
import edu.ucsb.nceas.morpho.util.Log;

import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;



public class CodeDefinition extends AbstractWizardPage {

  public final String pageID = DataPackageWizardInterface.CODE_DEFINITION;
  public String nextPageID = DataPackageWizardInterface.CODE_IMPORT_SUMMARY;
  public final String pageNumber = "";

  public final String title      = "Code Defintions Import Page";
  public final String subtitle   = "Define the columns for the codes and definitions";

  private WizardContainerFrame mainWizFrame;
  private OrderedMap resultsMap;
	
	private CodeDefnPanel importPanel = null;
	
	
	private JTextField attrField;
	private JTextField entityField;
	
	private AbstractDataPackage adp = null;
  
	public CodeDefinition(WizardContainerFrame mainWizFrame) {

    this.mainWizFrame = mainWizFrame;
		
    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
		
		setLayout(new BorderLayout());
		
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		JLabel label = WidgetFactory.makeHTMLLabel("Identify the columns of the new data table that contain the Codes and Definitions for the following Attribute - ", 1, false);
		
		JLabel attrLabel = WidgetFactory.makeLabel("Attribute Name:", false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		attrField = WidgetFactory.makeOneLineTextField();
		attrField.setEditable(false);
		
		JPanel attrPanel = WidgetFactory.makePanel();
		attrPanel.add(attrLabel);
		attrPanel.add(attrField);
		
		JLabel entityLabel = WidgetFactory.makeLabel("Entity Name:", false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		entityField = WidgetFactory.makeOneLineTextField();
		entityField.setEditable(false);
		
		JPanel entityPanel = WidgetFactory.makePanel();
		entityPanel.add(entityLabel);
		entityPanel.add(entityField);
		
		infoPanel.add(label);
		infoPanel.add(WidgetFactory.makeDefaultSpacer());
		infoPanel.add(attrPanel);
		infoPanel.add(WidgetFactory.makeDefaultSpacer());
		infoPanel.add(entityPanel);
		infoPanel.add(WidgetFactory.makeDefaultSpacer());
		
		
		add(infoPanel, BorderLayout.NORTH);
		
		importPanel = new CodeDefnPanel(true, false); // CodeDefnPanel with only the definition part but without the data tables being created	
		add(importPanel, BorderLayout.CENTER);
		
	}

	
  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
		String attr = mainWizFrame.getCurrentImportAttributeName();
		String entity = mainWizFrame.getCurrentImportEntityName();
		
		attrField.setText(attr);
		entityField.setText(entity);
		
		String tableName = mainWizFrame.getLastImportedEntity();
		List attrs =  mainWizFrame.getLastImportedAttributes();
		Vector rowData = mainWizFrame.getLastImportedDataSet();
		importPanel.setTable(tableName, attrs, rowData);
		importPanel.invalidate();
		
		mainWizFrame.setLastImportedAttributes(null);
		mainWizFrame.setLastImportedEntity(null);
		mainWizFrame.setLastImportedDataSet(null);
		
		String prevPageID = mainWizFrame.getPreviousPageID();
		if(prevPageID.equals(DataPackageWizardInterface.TEXT_IMPORT_WIZARD)) {
				
				Node newDOM = mainWizFrame.collectDataFromPages();
				
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
		
		
	}

  
  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   */
  public void onRewindAction() {

    //never used
  }

  /**
   *  The action to be executed when the "Next" button is pressed. 
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {
		
		if(importPanel.validateUserInput()) {
			OrderedMap map = mainWizFrame.getCurrentImportMap();
			String relativeXPath = mainWizFrame.getCurrentImportXPath();
			String scale = mainWizFrame.getCurrentImportScale().toLowerCase();
			String path = relativeXPath + "/measurementScale/" + scale + "/nonNumericDomain/enumeratedDomain[1]/entityCodeList";
			
			Iterator it = map.keySet().iterator();
			String prefix = "";
			int pos = -1;
			while(it.hasNext()) {
				String k1 = (String) it.next();
				
				if((pos = k1.indexOf("/entityReference")) > -1) {
					prefix = k1.substring(0, pos);
					break;
				}
			}
				
			if(pos == -1) {
				Log.debug(15, "Error in CodeDefintion!! map doesnt have the entityReference key");
			} else {
				map.remove(prefix + "/entityReference");
				map.remove(prefix + "/valueAttributeReference");
				map.remove(prefix + "/definitionAttributeReference");
				OrderedMap importMap = importPanel.getPanelData(prefix);
				map.putAll(importMap);
				
			}
			return true;
		} else
			return false;
		
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
			Log.debug(16, " Abstract Data Package is null in CodeDefinition Page");
		}
		return dp;
	}
	
	
  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  public OrderedMap getPageData() { return resultsMap; }

  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return pageID; }

  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public String getTitle() { return title; }

  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public String getSubtitle() { return subtitle; }

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
