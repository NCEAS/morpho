/**
 *  '$RCSfile: CodeDefnPanel.java,v $'
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


/*
This a page used for importing codes from another table. It gives the user
the option of importing it later or identifing the table and columns of an
already present table.
If the user chooses to import it later, the returned OrdereMap contains
null values for the entityCodeList/entityReference. The presence of this
key in the OrderedMap with a null value indicates that we need to import the
data table at the end of the current operation. Thus, whenever an
AttributePage is used anywhere in Morpho, we need to check if some data
table has to be imported after that.
*/


package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.util.StringTokenizer;

import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.Base64;

import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AddDocumentationCommand;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;

import edu.ucsb.nceas.morpho.Morpho;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.DefaultComboBoxModel;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class CodeDefnPanel extends JPanel implements WizardPageSubPanelAPI {

	private final String title      = "Import Codes and Definitions";
	private final String subtitle   = "";

	public short USER_RESPONSE;
	public static final short OK_OPTION      = 10;
	public static final short CANCEL_OPTION  = 20;


	private String[] importChoiceText = {"Import the definitions table into Morpho later",
	"The definitions table has already been included in this package"};

	private JLabel choiceLabel;
	private JPanel radioPanel;
	private JPanel definitionsPanel;


	private JPanel tableNamePanel;
	private JPanel namePanel;
	private JLabel nameLabel;
	private JComboBox namePickList;

	private JPanel tableCodePanel;
	private JPanel codePanel;
	private JLabel codeLabel;
	private JComboBox codePickList;

	private JPanel tableDefnPanel;
	private JPanel defnPanel;
	private JLabel defnLabel;
	private JComboBox defnPickList;

	private JPanel buttonsPanel;


	private String[] entityNames = null;
	private String[] attrNames = null;

	private short selectedImportChoice = 0;

	private static final int IMPORT_LATER = 1;
	private static final int IMPORT_DONE  = 2;
	private static final String ID_XPATH = "attribute/@id";
	private static final int MAX_IMPORTED_ROWS_DISPLAYED = 10;
	private static final String TRUNCATE_STRING = "--other codes not displayed--";

	private int currentEntityIndexSelected = -1;

	private int entityIdx = -1;
	private String currentEntityID = null;
	private String codeAttributeID = null;
	private String defnAttributeID = null;

	private static Node[] attributeArray = null;

	// AbstractDataPackage of the current package
	private AbstractDataPackage adp = null;
	// DataViewContainerPanel of current package
	private DataViewContainerPanel resultPane = null;

	private ItemListener namePickListListener;

	// flag to indicate if the panel is to only allow the user to define codes,
	// without giving the option of importing it later.

	private boolean onlyDefnPanel = false;

	public CodeDefnPanel(){

		onlyDefnPanel = false;
		init();
	}

	public CodeDefnPanel(boolean onlyDefinitionsPanel) {

		onlyDefnPanel = onlyDefinitionsPanel;
		if(onlyDefnPanel)
			selectedImportChoice = IMPORT_DONE;
		init();
	}

	private void init() {

		// gets the Abstract Data Package and sets it to the member variable 'adp'
		getADP();

		setLayout(new BorderLayout());

		add(WidgetFactory.makeDefaultSpacer());

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		choiceLabel = WidgetFactory.makeHTMLLabel("Select one of the following",1,true);
		topPanel.add(choiceLabel);
		add(WidgetFactory.makeDefaultSpacer());

		ActionListener listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				Log.debug(45, "got radiobutton command: "+e.getActionCommand());

				if (e.getActionCommand().equals(importChoiceText[0])) {
					selectedImportChoice = IMPORT_LATER;
					definitionsPanel.setVisible(false);
				} else if (e.getActionCommand().equals(importChoiceText[1])) {
					selectedImportChoice = IMPORT_DONE;
					definitionsPanel.setVisible(true);
				}
			}
		};

		radioPanel = WidgetFactory.makeRadioPanel(importChoiceText, -1, listener);

		topPanel.add(radioPanel);

		if(onlyDefnPanel == false)
			add(topPanel, BorderLayout.NORTH);


		definitionsPanel = getDefinitionsPanel();
		add(definitionsPanel, BorderLayout.CENTER);
		if(onlyDefnPanel == false)
			definitionsPanel.setVisible(false);

	}



	private JPanel getDefinitionsPanel() {

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3,1,0, 5));

		entityNames = getEntityNames();
		attrNames = new String[] {};

		JPanel tableNamePanel = new JPanel(new GridLayout(1,2, 3, 0));

		namePickListListener = new ItemListener() {

			public void itemStateChanged(ItemEvent e) {

				int index = ((JComboBox)e.getSource()).getSelectedIndex();
				String name = (String)((JComboBox)e.getSource()).getSelectedItem();
				if(index == currentEntityIndexSelected)
					return;
				currentEntityIndexSelected = index;
				Log.debug(45, "PickList state changed: " +
				(String)((JComboBox)e.getSource()).getItemAt(index));


				if (index == 0) {
					codePickList.setEnabled(false);
					defnPickList.setEnabled(false);
				} else {
					if(adp == null)
						getADP();
					if(adp == null) {

						Log.debug(15, "Unable to obtain the AbstractDataPackage in the CodeImportPanel");
						return;
					}
					entityIdx = adp.getEntityIndex(name);
					attrNames = getAttributeNames(entityIdx);
					currentEntityID = getEntityID(entityIdx);
					if(currentEntityID == null || currentEntityID.trim().length() ==0) {
						String newId = WizardSettings.getUniqueID();
						adp.setEntityID(entityIdx, newId);
						currentEntityID = newId;
						Log.debug(15, "Entity does not have an ID ! Assigning it a new ID of " + newId);
					}
					codePickList.setEnabled(true);
					defnPickList.setEnabled(true);
					codePickList.setModel(new DefaultComboBoxModel(attrNames));
					defnPickList.setModel(new DefaultComboBoxModel(attrNames));

				}
			}
		};

		namePanel = WidgetFactory.makePanel();
		nameLabel = WidgetFactory.makeLabel("Data table:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		namePanel.add(nameLabel);
		namePickList = WidgetFactory.makePickList( entityNames, false, 0, namePickListListener);
		namePanel.add(namePickList);

		tableNamePanel.add(namePanel);
		tableNamePanel.add(getLabel(
		WizardSettings.HTML_NO_TABLE_OPENING
		+"Choose the data table that contains the codes and their definition"
		+WizardSettings.HTML_NO_TABLE_CLOSING));
		panel.add(tableNamePanel);

		JPanel tableCodePanel = new JPanel(new GridLayout(1,2, 3, 0));

		codePanel = WidgetFactory.makePanel();
		codeLabel = WidgetFactory.makeLabel("Codes:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		codePanel.add(codeLabel);
		codePickList = WidgetFactory.makePickList( attrNames, false, 0, null);
		codePanel.add(codePickList);

		tableCodePanel.add(codePanel);
		tableCodePanel.add(getLabel(
		WizardSettings.HTML_NO_TABLE_OPENING
		+"Choose the column in the data table that contains the codes"
		+WizardSettings.HTML_NO_TABLE_CLOSING));
		panel.add(tableCodePanel);

		JPanel tableDefnPanel = new JPanel(new GridLayout(1,2, 3, 0));

		defnPanel = WidgetFactory.makePanel();
		defnLabel = WidgetFactory.makeLabel("Definitions:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		defnPanel.add(defnLabel);
		defnPickList = WidgetFactory.makePickList( attrNames, false, 0, null);
		defnPanel.add(defnPickList);

		tableDefnPanel.add(defnPanel);
		tableDefnPanel.add(getLabel(
		WizardSettings.HTML_NO_TABLE_OPENING
		+"Choose the column in the data table that contains the definitions for the codes"
		+WizardSettings.HTML_NO_TABLE_CLOSING));
		panel.add(tableDefnPanel);

		codePickList.setEnabled(false);
		defnPickList.setEnabled(false);

		return panel;
	}

	private void getADP() {

		MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		if (morphoFrame != null) {
			resultPane = AddDocumentationCommand.
			getDataViewContainerPanelFromMorphoFrame(morphoFrame);
		}//if
		// make sure resulPanel is not null
		if ( resultPane != null) {
			adp = resultPane.getAbstractDataPackage();
		}
	}

	private String[] getEntityNames() {

		if(entityNames != null)
			return entityNames;

		ArrayList names = new ArrayList();

		if(adp == null)
			getADP();

		if(adp != null) {
			for(int i = 0; i < adp.getEntityCount(); i++) {
				names.add(adp.getEntityName(i));
			}
		} else {
			Log.debug(45, "Error - Unable to get the AbstractDataPackage in CodeImportPanel. ");
		}

		int cnt = names.size();
		String[] entNames = new String[cnt + 1];
		entNames[0] = "--Select data table--";
		int i = 1;
		for(Iterator it = names.iterator(); it.hasNext();)
			entNames[i++] = (String)it.next();

		return entNames;
	}

	/**
	*	Function to retrieve the selected table name from where the enumerated
	*	codes are imported. If the user chooses the option of importing the data
	*	table later, this function returns null.
	*/

	public String getTableName() {
		if(selectedImportChoice == IMPORT_LATER || entityNames == null)
			return null;
		if(namePickList.getSelectedIndex() == 0)
			return null;
		return (String)namePickList.getSelectedItem();
	}

	public void setTableName(String name) {
		entityNames = new String[2];
		entityNames[0] = "--Select data table--";
		entityNames[1] = name;
		namePickList.setModel(new DefaultComboBoxModel(entityNames));

	}

	public void setAttributes(List attr) {

		int cnt = attr.size();
		attrNames = new String[cnt + 1];
		attrNames[0] = "--Select A Column--";
		int i = 1;
		for(Iterator it = attr.iterator(); it.hasNext() && i < (cnt+1) ; )
			attrNames[i++] =  (String) it.next();

		codePickList.setModel(new DefaultComboBoxModel(attrNames));
		defnPickList.setModel(new DefaultComboBoxModel(attrNames));

	}

	/*
	*	Function to retrieve the ID of the selected table from where the enumerated
	*	codes are imported.
	*/

	private String getEntityID(int entityIndex) {
		String id = "";
		id = adp.getEntityID(entityIndex);
		Log.debug(45, "Entity ID for entityIndex = " + entityIndex + " is " + id);
		return id;
	}

	/**
	*	Function to retrieve the selected entity Index
	*	@return int the entity index of the selected data table
	*/

	public int getSelectedEntityIndex() {
		return entityIdx;
	}


	private String[] getAttributeNames(int entityIndex) {
		ArrayList names = new ArrayList();
		if(adp != null) {
			int num = adp.getAttributeCountForAnEntity(entityIndex);
			for(int i = 0; i < num; i++) {
				names.add(adp.getAttributeName(entityIndex, i));
			}
		}
		int cnt = names.size();
		String[] attrs = new String[cnt+1];
		attrs[0] = "--Select A Column--";
		int i = 1;
		for(Iterator it = names.iterator(); it.hasNext();)
			attrs[i++] = (String)it.next();

		return attrs;
	}


	/**
	*  The action to be executed when the page is displayed. May be empty
	*/

	public void onLoadAction() {}

	/**
   *  checks that the user has filled in required fields - if not, highlights
   *  labels to draw attention to them
   *
   *  @return   boolean true if user data validated OK. false if intervention
   *            required
   */

	public boolean validateUserInput() {

		if(selectedImportChoice != IMPORT_LATER && selectedImportChoice != IMPORT_DONE) {
			WidgetFactory.hiliteComponent(choiceLabel);
			return false;
		}
		WidgetFactory.unhiliteComponent(choiceLabel);

		if(selectedImportChoice == IMPORT_LATER)
			return true;

		if(namePickList.getSelectedIndex() < 1) {
			WidgetFactory.hiliteComponent(nameLabel);
			return false;
		}
		WidgetFactory.unhiliteComponent(nameLabel);

		if(codePickList.getSelectedIndex() < 1) {
			WidgetFactory.hiliteComponent(codeLabel);
			return false;
		}
		WidgetFactory.unhiliteComponent(codeLabel);

		if(defnPickList.getSelectedIndex() < 1) {
			WidgetFactory.hiliteComponent(defnLabel);
			return false;
		}
		WidgetFactory.unhiliteComponent(defnLabel);

		int codeIndex = codePickList.getSelectedIndex() - 1;
		int defnIndex = defnPickList.getSelectedIndex() - 1;

		if(adp != null) {

			codeAttributeID = adp.getAttributeID(entityIdx, codeIndex);

			// the attribute has no ID !! This should never happen for data tables
			// created using the new DPW. But if we encounter such a situation, a
			// new ID is assigned and added to the attribute.

			if(codeAttributeID.trim() == "") {
				Log.debug(15, "Attribute " +
				adp.getAttributeName(entityIdx,	codeIndex) + "has no ID; assigning one now");

				codeAttributeID = WizardSettings.getUniqueID();
				assignIDToAttribute(codeIndex, codeAttributeID);
			}
			Log.debug(45, "Code AttributeID = " + codeAttributeID);

			defnAttributeID = adp.getAttributeID(entityIdx,	defnPickList.getSelectedIndex() - 1);

			// the attribute has no ID !! This should never happen for data tables
			// created using the new DPW. But if we encounter such a situation, a
			// new ID is assigned and added to the attribute.
			if(defnAttributeID.trim() == "") {
				Log.debug(15, "Attribute " +
				adp.getAttributeName(entityIdx,	defnIndex) + " has no ID; assigning one now");
				defnAttributeID = WizardSettings.getUniqueID();
				assignIDToAttribute(defnIndex, defnAttributeID);
			}
			Log.debug(45, "Defn AttributeID = " + defnAttributeID);

		} else {
			Log.debug(15, "No AbstractDataPackage found! Hence IDs could not be retrieved!");
		}
		return true;
	}

	/* function to assign a new unique ID to an attribute if that attribute doesnt
		have an ID already assigned to it. */

	private void assignIDToAttribute(int attrIndex, String value) {

		if(adp == null)
			Log.debug(15, "Abstract Data Package is null ! Cant assign ID to attribute");
		if(attributeArray == null)
			attributeArray = adp.getAttributeArray(entityIdx);

		if(attributeArray[attrIndex] == null) {
			Log.debug(15, " attribute node itself is null; Cant assign ID to it");
			return;
		} else {
			Log.debug(45, "attribute node =" + attributeArray[attrIndex].getNodeName() + ";"
			+ attributeArray[attrIndex].getNodeValue());
		}

		NamedNodeMap map = attributeArray[attrIndex].getAttributes();
		Node oldIdNode = map.getNamedItem("id");

		if(oldIdNode != null) {
			attributeArray[attrIndex].removeChild(oldIdNode);
			oldIdNode.setNodeValue(value);
			attributeArray[attrIndex].appendChild(oldIdNode);
		} else {
			Log.debug(45, "Attribute element has no 'id' attribute. Adding it.");
			((Element)attributeArray[attrIndex]).setAttribute("id", value);
		}
		adp.setLocation("");
		return;
	}


	/**
	*  gets the Map object that contains all the key/value paired
	*  settings for this particular wizard panel
	*
	*  @return   data the Map object that contains all the
	*            key/value paired settings for this particular wizard page
	*/
	public OrderedMap getPanelData() {

		return getPanelData("");
	}

	/**
	*  gets the Map object that contains all the key/value paired
	*  settings for this particular wizard panel, given a prefix xPath
	*
	*	@param xPath the xPath that needs to be prepended to all keys that are
	*								inserted in the map
	*  @return   data the Map object that contains all the
	*            key/value paired settings for this particular wizard page
	*/

	public OrderedMap getPanelData(String xPath) {

		OrderedMap map = new OrderedMap();
		map.put(xPath + "/entityReference", this.currentEntityID);
		map.put(xPath + "/valueAttributeReference", this.codeAttributeID);
		map.put(xPath + "/definitionAttributeReference", this.defnAttributeID);
		return map;
	}


	/**
	*  sets the fields in the wizard panel using the Map object
	*  that contains all the key/value paired
	*
	*  @param   data the Map object that contains all the
	*            key/value paired settings for this particular wizard page
	*/
	public void setPanelData(OrderedMap data) {
		boolean b1 = data.containsKey(AttributeSettings.Nominal_xPath + "/enumeratedDomain/entityCodeList/entityReference");

		if(b1) { // check if its Nominal

			setPanelData(AttributeSettings.Nominal_xPath + "/enumeratedDomain/entityCodeList", data);

		} else { // check for Ordinal

			b1 = data.containsKey(AttributeSettings.Ordinal_xPath + "/enumeratedDomain/entityCodeList/entityReference");

			if(b1) {
				setPanelData(AttributeSettings.Ordinal_xPath + "/enumeratedDomain/entityCodeList", data);
			}

		}
		return;
	}

	/**
	*  sets the fields in the wizard panel using the Map object
	*  that contains all the key/value paired and the relative xPath to be used
	*
	*	@param 	xPath	the relative xPath of the keys
	*  @param   data the Map object that contains all the
	*            key/value paired settings for this particular wizard page
	*/

	public void setPanelData(String xPath, OrderedMap data) {

		Object o1 = data.get(xPath + "/entityReference");
		Container c = (Container)(radioPanel.getComponent(1));

		if( o1 == null) {
			JRadioButton jrb = (JRadioButton)c.getComponent(0);
			jrb.setSelected(true);
			selectedImportChoice = IMPORT_LATER;
			definitionsPanel.setVisible(false);
			return;

		} else {

			JRadioButton jrb = (JRadioButton)c.getComponent(1);
			jrb.setSelected(true);
			selectedImportChoice = IMPORT_DONE;
			definitionsPanel.setVisible(true);
		}

		currentEntityID = (String)o1;

		entityNames = getEntityNames();
		for(int i = 0; i < entityNames.length; i++) {
			String ID = getEntityID(i);
			if(ID.equals(currentEntityID)) {
				namePickList.setSelectedIndex(i + 1);

				break;
			}
		}

		codeAttributeID = (String)data.get(xPath + "/valueAttributeReference");
		defnAttributeID = (String)data.get(xPath + "/definitionAttributeReference");

		boolean codeSelected = false;
		boolean defnSelected = false;

		for(int j = 1; j < codePickList.getItemCount(); j++) {
			String attrID = adp.getAttributeID(entityIdx, j -1);
			if(attrID.equals(codeAttributeID)) {
				codePickList.setSelectedIndex(j);
				codeSelected = true;
			}
			if(attrID.equals(defnAttributeID)) {
				defnPickList.setSelectedIndex(j);
				defnSelected = true;
			}
			if(codeSelected  && defnSelected)
				break;
		}
		return;
	}



	private JLabel getLabel(String text) {

		if (text==null) text="";
		JLabel label = new JLabel(text);

		label.setAlignmentX(1.0f);
		label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
		label.setBorder(BorderFactory.createMatteBorder(1,10,1,3, (Color)null));

		return label;
	}

	/**
	*	Function to retrieve the imported data from the columns selected by the
	*	user. The selected table is read and the necessary two columns are
	*	extracted and returned as a List of rows. Each row is a list of two elements
	*
	* @return list the list of rows containing only the selected two columns
	*/

	public List getColumnData() {

		File entityFile = null;

		if(adp == null) {
			Log.debug(15, "Abstract Data Package is null. Cannot fill customlist with the imported codes");
			return null;
		}

		int entityIndex = entityIdx;
		String inline = adp.getDistributionInlineData(entityIndex, 0,0);
		Morpho morpho = resultPane.getFramework();

		if (inline.length()>0) {  // there is inline data

			String encMethod = adp.getEncodingMethod(entityIndex, 0);
			if ((encMethod.indexOf("Base64")>-1)||(encMethod.indexOf("base64")>-1)||
			(encMethod.indexOf("Base 64")>-1)||(encMethod.indexOf("base 64")>-1)) {
				// is Base64
				byte[] decodedData = Base64.decode(inline);
				ByteArrayInputStream bais = new ByteArrayInputStream(decodedData);
				InputStreamReader isr = new InputStreamReader(bais);
				FileSystemDataStore fds3 = new FileSystemDataStore(morpho);
				entityFile = fds3.saveTempDataFile(adp.getAccessionNumber(), isr);
			}
			else {
				// is assumed to be text
				FileSystemDataStore fds2 = new FileSystemDataStore(morpho);
				StringReader sr2 = new StringReader(inline);
				entityFile = fds2.saveTempDataFile(adp.getAccessionNumber(), sr2);
			}
		} else if (adp.getDistributionUrl(entityIndex, 0,0).length()>0) {

			String urlinfo = adp.getDistributionUrl(entityIndex, 0,0);
			// assumed that urlinfo is of the form 'protocol://systemname/localid/other'
			// protocol is probably 'ecogrid'; system name is 'knb'
			// we just want the local id here
			int indx2 = urlinfo.lastIndexOf("/");
			if(indx2 == -1) {
				Log.debug(15, "Distribution URL is not in the right format! So data couldnt be retrieved");
				return null;
			}
			urlinfo = urlinfo.substring(indx2 +1);
			if (urlinfo.length()==0) {
				Log.debug(15, "Distribution URL is not in the right format! So data couldnt be retrieved");
				return null;
			}
			// we now have the id
			try{
				String loc = adp.getLocation();
				if ((loc.equals(adp.LOCAL))||(loc.equals(adp.BOTH))) {
					FileSystemDataStore fds = new FileSystemDataStore(morpho);
					entityFile = fds.openFile(urlinfo);
				}
				else if (loc.equals(adp.METACAT)) {
					MetacatDataStore mds = new MetacatDataStore(morpho);
					entityFile = mds.openFile(urlinfo);
				}
				else if (loc.equals("")) {  // just created the package; not yet saved!!!
					try{
						// first try looking in the profile temp dir
						ConfigXML profile = morpho.getProfile();
						String separator = profile.get("separator", 0);
						separator = separator.trim();
						FileSystemDataStore fds = new FileSystemDataStore(morpho);
						String temp = new String();
						temp = urlinfo.substring(0, urlinfo.indexOf(separator));
						temp += "/" + urlinfo.substring(urlinfo.indexOf(separator) + 1, urlinfo.length());
						entityFile = fds.openTempFile(temp);
					}
					catch (Exception q1) {
						// oops - now try locally
						try{
							FileSystemDataStore fds = new FileSystemDataStore(morpho);
							entityFile = fds.openFile(urlinfo);
						}
						catch (Exception q2) {
							// now try metacat
							try{
								MetacatDataStore mds = new MetacatDataStore(morpho);
								entityFile = mds.openFile(urlinfo);
							}
							catch (Exception q3) {
								// give up!
								Log.debug(15,"Exception opening datafile after trying all sources!");
								return null;
							}
						}
					}
				}
			}
			catch (Exception q) {
				Log.debug(15,"Exception opening file!");
				q.printStackTrace();
			}
		}
		else if (adp.getDistributionArray(entityIndex, 0)==null) {
			// case where there is no distribution data in the package

			Log.debug(5, "The selected entity has NO distribution information!");
			return null;
		}

		if(entityFile == null) {
			Log.debug(15, "Unable to get the selected entity's data file!");
			return null;
		}

		int codeIndex = codePickList.getSelectedIndex() - 1;
		int defnIndex = defnPickList.getSelectedIndex() - 1;
		String numHeaders = adp.getPhysicalNumberHeaderLines(entityIndex, 0);
		int numHeaderLines = 0;
		try {
			if(numHeaders != null)
				numHeaderLines = Integer.parseInt(numHeaders);
		} catch(Exception e) {
		}

		List data = readTwoColumnsFromFile(entityFile, codeIndex, defnIndex, numHeaderLines);

		return data;

	} // end of getColumns

	private List readTwoColumnsFromFile(File file, int firstCol, int secondCol, int numHeaderLines) {

		List result = new ArrayList();
		String line;
		int entityIndex = entityIdx;
		String field_delimiter = adp.getPhysicalFieldDelimiter(entityIndex, 0);
		String delimiter = getDelimiterString(field_delimiter);
		boolean ignoreConsequtiveDelimiters = false;
		boolean orderReversed = false;
		if(firstCol > secondCol)
			orderReversed = true;
		String token, oldToken = "";
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			int linecnt = 0;
			while( (line = br.readLine()) != null) {
				linecnt++;
				if(linecnt <= numHeaderLines)
					continue;
				if(line.trim().equals(""))
					continue;
				List row = new ArrayList();

				if(result.size() >= MAX_IMPORTED_ROWS_DISPLAYED) {
					int space = TRUNCATE_STRING.indexOf(" ");
					row.add(TRUNCATE_STRING.substring(0, space));
					row.add(TRUNCATE_STRING.substring(space + 1));
					result.add(row);
					break;
				}

				if (ignoreConsequtiveDelimiters) {
					StringTokenizer st = new StringTokenizer(line, delimiter, false);
					int cnt = -1;
					while( st.hasMoreTokens() ) {
						token = st.nextToken().trim();
						cnt++;
						if(cnt == firstCol) {
							if(orderReversed) {
								row.add(0, token);
								break;
							} else {
								row.add(token);
							}
						}
						if (cnt == secondCol) {
							row.add(token);
							if(!orderReversed) break;
						}
					} // end of while
					result.add(row);
					continue;
				}
				else { // not consecutive delimiters
					int cnt = -1;
					StringTokenizer st = new StringTokenizer(line, delimiter, true);
					while( st.hasMoreTokens() ) {
						token = st.nextToken().trim();
						if (! (delimiter.indexOf(token) > -1) ) {
							cnt++;
							if(cnt == firstCol) {
								if(orderReversed) {
									row.add(0, token);
									break;
								} else {
									row.add(token);
								}
							}
							if (cnt == secondCol) {
								row.add(token);
								if(!orderReversed) break;
							}

						}
						else {
							if ((delimiter.indexOf(oldToken) > -1) && (delimiter.indexOf(token) > -1)) {
								cnt++;
								if(cnt == firstCol) {
									if(orderReversed) {
										row.add(0, "");
										break;
									} else {
										row.add("");
									}
								}
								if (cnt == secondCol) {
									row.add("");
									if(!orderReversed) break;
								}
							}
						}
						oldToken = token;
					}
				} // end of else

				result.add(row);
			} // end of while

		} // end of try bolck
		catch(Exception e) {
			Log.debug(15, "Exception in reading the data File: " + e);

		}
		return result;

	}// end of function readTwoColumnsFromFile

	private String getDelimiterString(String field_delimiter) {
		String str = "";
		String temp = field_delimiter.trim();
		if (temp.startsWith("#x")) {
			temp = temp.substring(2);
			if (temp.equals("0A")) str = "\n";
			if (temp.equals("09")) str = "\t";
			if (temp.equals("20")) str = " ";
		}
		else {
			str = temp;
		}
		return str;
	}

}

