/**
 *  '$RCSfile: CodeImportPage.java,v $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;

import edu.ucsb.nceas.utilities.OrderedMap;
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


public class CodeImportPage extends AbstractWizardPage {

  public final String pageID     = DataPackageWizardInterface.CODE_IMPORT_PAGE;
  public String nextPageID = DataPackageWizardInterface.DATA_LOCATION;
  public final String pageNumber = "";

  public final String title      = "Code Defintions Import Page";
  public final String subtitle   = "Import/Define the codes and definitions";

  private WizardContainerFrame mainWizFrame;
  private OrderedMap resultsMap;
	
	private String[] importChoiceText = {"The table containing the definitions has already been imported into Morpho",
	"The table containing the definitions needs to be imported into Morpho"};

  private boolean importCompletedOK = false;
	
	private CodeImportPanel importPanel = null;
	private JPanel radioPanel;
	
	private JTextField attrField;
	private JTextField entityField;
  
	private JLabel choiceLabel;
	
	private short importChoice = 0;
	private static final short IMPORT_DONE = 10;
	private static final short INVOKE_TIW = 20;
	
	
  public CodeImportPage(WizardContainerFrame mainWizFrame) {

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
		JLabel label = WidgetFactory.makeHTMLLabel("Import Codes and Definitions for the following Attribute - ", 1, false);
		
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
		
		choiceLabel = WidgetFactory.makeHTMLLabel("Select one of the following",1,true);
		infoPanel.add(choiceLabel);
		infoPanel.add(WidgetFactory.makeDefaultSpacer());
		
		ActionListener listener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				Log.debug(45, "got radiobutton command: "+e.getActionCommand());
				
				if (e.getActionCommand().equals(importChoiceText[0])) {
					if(importPanel == null) {
						importPanel = new CodeImportPanel(true);
						CodeImportPage.this.add(importPanel, BorderLayout.CENTER);
						CodeImportPage.this.validate();
						CodeImportPage.this.repaint();
					}
					importPanel.setVisible(true);
					importChoice = IMPORT_DONE;
				} else if (e.getActionCommand().equals(importChoiceText[1])) {
					if(importPanel != null)
						importPanel.setVisible(false);
					importChoice = INVOKE_TIW;
				}
			}
		};
		
		radioPanel = WidgetFactory.makeRadioPanel(importChoiceText, -1, listener);
		
		infoPanel.add(radioPanel);
		
		add(infoPanel, BorderLayout.NORTH);
		
		//add(importPanel, BorderLayout.CENTER);
		//importPanel.setVisible(false);
	}

	public void addAttributeForImport(String entityName, String attributeName, String scale, OrderedMap map, String xPath, boolean newTable) {
		
		System.out.println("To Import, attribute '"+attributeName+"' in Entity '"+ entityName +"'");
		mainWizFrame.addAttributeForImport(entityName, attributeName, scale, map, xPath, newTable);
	}
	
  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
		String attr = mainWizFrame.getCurrentImportAttributeName();
		String entity = mainWizFrame.getCurrentImportEntityName();
		
		attrField.setText(attr);
		entityField.setText(entity);
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
		
		if(importChoice != IMPORT_DONE && importChoice != INVOKE_TIW) {
			WidgetFactory.hiliteComponent(choiceLabel);
			return false;
		}
		WidgetFactory.unhiliteComponent(choiceLabel);
		
		if(importChoice == IMPORT_DONE) {
			if(importPanel.onAdvanceAction()) {
				OrderedMap map = mainWizFrame.getCurrentImportMap();
				String relativeXPath = mainWizFrame.getCurrentImportXPath();
				String scale = mainWizFrame.getCurrentImportScale().toLowerCase();
				String path = relativeXPath + "/measurementScale/" + scale + "/nonNumericDomain/enumeratedDomain[1]/entityCodeList";
				if(!map.containsKey(path + "/entityReference")) {
					System.out.println("ERROR !! map doesnt have the key - "+path);
				} else {
					map.remove(path + "/entityReference");
					map.remove(path + "/valueAttributeReference");
					map.remove(path + "/definitionAttributeReference");
					OrderedMap importMap = importPanel.getPageData(path);
					map.putAll(importMap);
					
				}
				if(mainWizFrame.getAttributeImportCount() > 0)
					nextPageID = pageID;
				else
					nextPageID = DataPackageWizardInterface.CODE_IMPORT_SUMMARY;
				return true;
			} else
				return false;
		} else {
			this.nextPageID = DataPackageWizardInterface.DATA_LOCATION;
			return true;
		}
		
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