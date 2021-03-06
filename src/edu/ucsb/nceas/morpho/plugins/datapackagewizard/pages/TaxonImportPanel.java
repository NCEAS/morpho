/**
 *  '$RCSfile: TaxonImportPanel.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Perumal Sambasivam
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2005-05-26 20:12:34 $'
 * '$Revision: 1.9 $'
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

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractCustomTablePopupHandler;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomTable;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3c.dom.Node;

/**
 * class for the panel that is displayed when a user wants to import taxonomic
 * data from his data tables.
 * 
 * It consists of a CustomTable that displays the table names and the column
 * names along with the data defined in the columns. It also contains a radio
 * panel to let the user choose whether all the values from the column(s) need
 * to be imported or only those values that are used in the data set.
 */

public class TaxonImportPanel extends JPanel implements WizardPageSubPanelAPI {

	private static final int LABEL_WIDTH = 85;
	private static final int LABEL_HEIGHT = 17;

	public static final int DIALOG_WIDTH = 600;
	public static final int DIALOG_HEIGHT = 450;
	private DataViewContainerPanel resultPane = null;
	private AbstractDataPackage adp = null;

	private CustomTable table = null;
	private JPanel radioPanel = null;

	private Vector tableNames = null;
	private Vector colNames = null;
	private Vector colData = null;
	private Vector rowData = null;
	private Vector typeNames = null;

	private static final String nominal_xPath = "/attribute/measurementScale[1]/nominal[1]/nonNumericDomain[1]";
	private static final String ordinal_xPath = "/attribute/measurementScale[1]/ordinal[1]/nonNumericDomain[1]";

	private String[] importChoiceText = {/* "Import all values" */
			Language.getInstance().getMessage(
					"TaxonImportPanel.importChoiceText_1"),
			/* "Import only the values used in the dataset" */Language
					.getInstance().getMessage(
							"TaxonImportPanel.importChoiceText_2") };

	private int selectedImportChoice = 0;
	public boolean displayTable = true;

	TaxonImportPanel() {

		super();
		init();
	}

	private void init() {

		displayTable = true;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// //////////////

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		JLabel topLabel = WidgetFactory.makeHTMLLabel(
				/* "Select the columns to import " */Language.getInstance()
						.getMessage("TaxonImportPanel.topLabel") + " ", 1,
				false);
		topPanel.add(WidgetFactory.makeDefaultSpacer());
		topPanel.add(topLabel);
		add(topPanel);
		add(WidgetFactory.makeDefaultSpacer());

		// /////////////////
		// height of the labels on the left side
		int ht = 100;

		table = getCustomTable();
		if (table == null || table.getColumnCount() == 0) {

			Log.debug(45, "CustomTable is empty, hence not displaying it");
			displayTable = false;
		} else {

			table.addPopupListener(new PopupHandler());
		}
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		if (table != null) {
			tablePanel.add(table, BorderLayout.CENTER);
			ht = table.getHeaderHeight();
			if (ht < 100)
				ht = 100;
		}
		JPanel sideLabelPanel = new JPanel(new BorderLayout());
		sideLabelPanel.add(tablePanel, BorderLayout.CENTER);

		JPanel subLabelPanel = new JPanel();
		subLabelPanel.setLayout(new BoxLayout(subLabelPanel, BoxLayout.Y_AXIS));
		subLabelPanel.add(getLabel(""));
		subLabelPanel.add(getLabel(""));
		subLabelPanel.add(getLabel(/* "Table Name:" */Language.getInstance()
				.getMessage("TaxonImportPanel.TableName") + ":"));
		subLabelPanel.add(getLabel(/* "Column Name:" */Language.getInstance()
				.getMessage("TaxonImportPanel.ColumnName") + ":"));
		subLabelPanel.add(getLabel(/* "Column Type:" */Language.getInstance()
				.getMessage("TaxonImportPanel.ColumnType") + ":"));
		subLabelPanel.setMaximumSize(new Dimension(LABEL_WIDTH, ht));
		subLabelPanel.setMinimumSize(new Dimension(LABEL_WIDTH, ht));
		subLabelPanel.setPreferredSize(new Dimension(LABEL_WIDTH, ht));

		JPanel sidePanel = new JPanel(new BorderLayout());
		sidePanel.add(subLabelPanel, BorderLayout.NORTH);
		sidePanel.add(new JLabel(""), BorderLayout.CENTER);
		sidePanel.add(Box.createVerticalGlue(), BorderLayout.SOUTH);

		sideLabelPanel.add(sidePanel, BorderLayout.WEST);

		JPanel emptyPanel = new JPanel(new BorderLayout());
		JLabel emptyHeaderLabel = WidgetFactory.makeHTMLLabel(
				"<b>No columns to import</b>", 1, false);
		JLabel emptyExplLabel = WidgetFactory
				.makeHTMLLabel(
						"You can import only columns of type <i>Text</i> or <i>Enumerated</i>, i.e. they should be either Nominal or Ordinal",
						2, false);
		JPanel topEmptyPanel = new JPanel();
		topEmptyPanel.setLayout(new BoxLayout(topEmptyPanel, BoxLayout.Y_AXIS));
		topEmptyPanel.add(emptyHeaderLabel);
		topEmptyPanel.add(emptyExplLabel);
		emptyPanel.add(topEmptyPanel, BorderLayout.NORTH);

		if (displayTable)
			add(sideLabelPanel);
		else
			add(emptyPanel);
		add(WidgetFactory.makeDefaultSpacer());

		// /////////////////
		JLabel choiceLabel = WidgetFactory.makeHTMLLabel(/*
														 * "What data should be imported from these columns? "
														 * ,
														 */
		Language.getInstance().getMessage("TaxonImportPanel.choiceLabel")
				+ "? ", 1, true);
		if (displayTable) {
			add(choiceLabel);
		}

		ActionListener listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				Log.debug(45,
						"got radiobutton command: " + e.getActionCommand());
				if (e.getActionCommand().equals(importChoiceText[0])) {
					selectedImportChoice = 0;
				} else if (e.getActionCommand().equals(importChoiceText[1])) {
					selectedImportChoice = 1;
				}
			}
		};

		radioPanel = WidgetFactory
				.makeRadioPanel(importChoiceText, 0, listener);

		if (displayTable) {
			add(radioPanel);
		}
		add(WidgetFactory.makeDefaultSpacer());

		// //////////////////

		return;
	}

	private JLabel getLabel(String text) {

		if (text == null)
			text = "";
		JLabel label = new JLabel(text);

		label.setAlignmentX(1.0f);
		// label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
		// label.setBorder(BorderFactory.createMatteBorder(1,10,1,3,
		// (Color)null));
		label.setMaximumSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
		label.setMinimumSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
		label.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));
		// label.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
		// WizardSettings.TOP_PANEL_BG_COLOR));

		return label;
	}

	public List<Map<String, String>> getListOfImportedTaxa() {

		List<Map<String, String>> result = new ArrayList<Map<String, String>>();

		if (!displayTable) {
			return result;
		}

		int[] cols = table.getSelectedColumns();

		int ecnt = adp.getEntityCount();
		File entityFiles[] = new File[ecnt];
		for (int i = 0; i < ecnt; i++) {
			entityFiles[i] = null;
		}
		int[] numHeaderLines = new int[ecnt];
		String[] delimiter = new String[ecnt];

		// iterate through the selected columns
		for (int i = 0; i < cols.length; i++) {

			Vector header = table.getColumnHeaderStrings(cols[i]);
			String taxonClass = " ";
			if (header.size() == 4) {
				taxonClass = (String) header.get(3);
			}

			if (isText(header) || selectedImportChoice == 0) { // import all
																// values
				// get the rows of data for that column
				List<String> colData = table.getColumnData(cols[i]);
				for (int j = 0; j < colData.size(); j++) {
					String dataCell = colData.get(j);
					Map<String, String> combo = null;
					// if it's our first column of data, then we need to
					// construct the row first
					if (i == 0) {
						combo = new TreeMap<String, String>();
						combo.put(taxonClass, dataCell);
						result.add(combo);
					} else {
						combo = result.get(j);
						combo.put(taxonClass, dataCell);
						result.set(j, combo);
					}
				}

			} else { // import only values present in dataset

				boolean isDefn = isDefinition(header);
				String tableName = (String) header.get(0);
				String colName = (String) header.get(1);
				int entityIndex = adp.getEntityIndex(tableName);
				if (entityFiles[entityIndex] == null) {

					entityFiles[entityIndex] = DataStoreServiceController
							.getInstance().getEntityFile(adp, entityIndex);

					String numHeaders = adp.getPhysicalNumberHeaderLines(
							entityIndex, 0);

					try {
						if (numHeaders != null) {
							numHeaderLines[entityIndex] = Integer
									.parseInt(numHeaders);
						}
					} catch (Exception e) {
					}

					String field_delimiter = adp.getPhysicalFieldDelimiter(
							entityIndex, 0);
					delimiter[entityIndex] = getDelimiterString(field_delimiter);
				}

				int colIdx = adp.getAttributeIndex(entityIndex, colName);
				List<String> data = CodeDefnPanel
						.getOneColumnValue(entityFiles[entityIndex], colIdx,
								numHeaderLines[entityIndex],
								delimiter[entityIndex], -1);

				if (!isDefn) { // column contains codes

					for (int j = 0; j < data.size(); j++) {
						String dataCell = data.get(j);
						Map<String, String> combo = null;
						if (i == 0) {
							combo = new TreeMap<String, String>();
							combo.put(taxonClass, dataCell);
							result.add(combo);
						} else {
							combo = result.get(j);
							combo.put(taxonClass, dataCell);
							result.set(j, combo);
						}
					}

				} else { // column contains definitions

					List<String> codeColData = table.getColumnData(cols[i] - 1);
					List<String> definitionColData = table
							.getColumnData(cols[i]);

					int cnt = 0;
					Iterator<String> prevIt = codeColData.iterator();
					while (prevIt.hasNext()) {
						String code = prevIt.next();
						String definition = definitionColData.get(cnt);

						if (data.contains(code)) {

							Map<String, String> combo = null;
							if (i == 0) {
								combo = new TreeMap<String, String>();
								combo.put(taxonClass, definition);
								result.add(combo);
							} else {
								combo = result.get(cnt);
								combo.put(taxonClass, definition);
								result.set(cnt, combo);
							}
						}
						cnt++;
					}

				} // end of else contains definitions

			}// end of else import only values in table

		} // end of for loop - for each selected col

		// pare down the result to unique records
		result = removeRedundantData(result);

		return result;
	}

	private List<Map<String, String>> removeRedundantData(
			List<Map<String, String>> data) {

		List<Map<String, String>> newData = new ArrayList<Map<String, String>>();
		Iterator<Map<String, String>> it = data.iterator();
		while (it.hasNext()) {
			Map<String, String> record = it.next();
			if (!newData.contains(record)) {
				newData.add(record);
			}
		}
		return newData;
	}

	private boolean isDefinition(Vector headerVector) {

		if (headerVector.size() < 3)
			return false;
		String type = (String) headerVector.get(2);
		if (type.indexOf("Code") > -1)
			return false;
		return true;
	}

	private boolean isText(Vector headerVector) {

		if (headerVector.size() < 3)
			return false;
		String type = (String) headerVector.get(2);
		if (type.indexOf("Text") > -1)
			return true;
		return false;
	}

	private CustomTable getCustomTable() {

		if (adp == null)
			getADP();

		if (adp == null) {
			Log.debug(
					10,
					"Unable to obtain the Abstract Data Package while building CustomTable in taxonomic page");
			return null;
		}

		tableNames = new Vector();
		colNames = new Vector();
		rowData = new Vector();
		typeNames = new Vector();

		String[] tables = getEntityNames();
		if (tables == null)
			return null;

		Morpho morpho = resultPane.getFramework();

		for (int i = 0; i < tables.length; i++) {

			Vector colsToExtract = new Vector();
			String cols[] = getColumnNames(i);
			for (int j = 0; j < cols.length; j++) {

				String colType = adp.getAttributeDataType(i, j);
				if (colType.trim().equalsIgnoreCase("text")
						|| colType.trim().equalsIgnoreCase("Nominal")
						|| colType.trim().equalsIgnoreCase("Ordinal")
						|| colType.trim().equalsIgnoreCase("String")) {

					colsToExtract.add(new Integer(j));
				}
			}

			if (colsToExtract.size() > 0) {
				/*
				 * File entityFile = CodeDefnPanel.getEntityFile(morpho, adp,
				 * i); String numHeaders = adp.getPhysicalNumberHeaderLines(i,
				 * 0); int numHeaderLines = 0; try { if(numHeaders != null) {
				 * numHeaderLines = Integer.parseInt(numHeaders); } }
				 * catch(Exception e) {}
				 * 
				 * String field_delimiter = adp.getPhysicalFieldDelimiter(i, 0);
				 * String delimiter = getDelimiterString(field_delimiter);
				 * 
				 * List data = CodeDefnPanel.getColumnValues(entityFile,
				 * colsToExtract, numHeaderLines, delimiter, -1);
				 * 
				 * addColumnsToRowData(rowData, data);
				 */

				if (adp == null)
					getADP();
				Node[] attributes = adp.getAttributeArray(i);
				OrderedMap maps[] = new OrderedMap[cols.length];
				// char array to indicate if the attribute is nominal or ordinal
				char measScale[] = new char[cols.length];
				Vector enumCols = new Vector();
				Vector textCols = new Vector();

				// populate information needed for the columns to extract
				for (int k = 0; k < colsToExtract.size(); k++) {
					int col = ((Integer) colsToExtract.get(k)).intValue();
					maps[col] = XMLUtilities.getDOMTreeAsXPathMap(attributes[col]);
					measScale[col] = findMeasurementScale(maps[col], "/attribute");
					if (measScale[col] == 'R' || measScale[col] == ' ') {
						// do nothing
					} else if (measScale[col] == 'n' || measScale[col] == 'o') {
						// text values, not Enums
						textCols.add(new Integer(col));
					} else {
						enumCols.add(new Integer(col));
					}
				}

				List data = getCodeDefinitionsFromAttributes(maps, measScale,
						enumCols);
				if (data.size() > 0) {
					addColumnsToRowData(rowData, data);
					Iterator it = enumCols.iterator();
					while (it.hasNext()) {
						int c = ((Integer) it.next()).intValue();
						tableNames.add(tables[i]);
						tableNames.add(tables[i]);
						colNames.add(cols[c]);
						colNames.add(cols[c]);
						typeNames.add("[ Code ]");
						typeNames.add("[ Definition ]");
					}
				}

				List textData = getTextValuesFromAttributes(i, textCols);
				if (textData.size() > 0) {

					addColumnsToRowData(rowData, textData);
					Iterator it = textCols.iterator();
					while (it.hasNext()) {
						int c = ((Integer) it.next()).intValue();
						tableNames.add(tables[i]);
						colNames.add(cols[c]);
						typeNames.add("[ Text ]");
					}
				}
			}

		}
		Vector header = new Vector();
		header.add(tableNames);
		header.add(colNames);
		header.add(typeNames);
		return new CustomTable(header, rowData);
	}

	List getTextValuesFromAttributes(int entityIndex, Vector textCols) {

		File entityFile = DataStoreServiceController.getInstance()
				.getEntityFile(adp, entityIndex);
		String numHeaders = adp.getPhysicalNumberHeaderLines(entityIndex, 0);
		int numHeaderLines = 0;
		try {
			if (numHeaders != null) {
				numHeaderLines = Integer.parseInt(numHeaders);
			}
		} catch (Exception e) {
		}

		String field_delimiter = adp.getPhysicalFieldDelimiter(entityIndex, 0);
		String delimiter = getDelimiterString(field_delimiter);
		boolean ignoreConsecutiveDelimiters = adp.ignoreConsecutiveDelimiters(
				entityIndex, 0);
		List rowData = CodeDefnPanel.getColumnValues(entityFile, textCols,
				numHeaderLines, delimiter, ignoreConsecutiveDelimiters, -1);
		return rowData;
	}

	private List getCodeDefinitionsFromAttributes(OrderedMap[] maps,
			char[] measScale, Vector cols) {

		List res = new ArrayList();
		int cnt = 1;
		while (true) {

			List row = new ArrayList();
			boolean dataPresent = false;
			for (int i = 0; i < maps.length; i++) {

				if (!cols.contains(new Integer(i)))
					continue;
				if (maps[i] == null)
					continue;
				Object o = null;

				if (measScale[i] == 'N') {
					o = maps[i].get(nominal_xPath
							+ "/enumeratedDomain[1]/codeDefinition[" + cnt
							+ "]/code[1]");
				} else if (measScale[i] == 'O') {
					o = maps[i].get(ordinal_xPath
							+ "/enumeratedDomain[1]/codeDefinition[" + cnt
							+ "]/code[1]");
				}
				if (o == null) {
					row.add("");
					row.add("");
				} else {
					row.add((String) o);
					String defn = "";
					if (measScale[i] == 'N') {
						defn = (String) maps[i].get(nominal_xPath
								+ "/enumeratedDomain[1]/codeDefinition[" + cnt
								+ "]/definition[1]");
					} else if (measScale[i] == 'O') {
						defn = (String) maps[i].get(ordinal_xPath
								+ "/enumeratedDomain[1]/codeDefinition[" + cnt
								+ "]/definition[1]");
					}
					row.add(defn);
					dataPresent = true;
				}

			}
			if (!dataPresent)
				break;
			res.add(row);
			cnt++;
		}

		return res;
	}

	public static void addColumnsToRowData(Vector rowData, List data) {

		if (data.size() == 0)
			return;
		if (rowData.size() == 0) {
			for (int i = 0; i < data.size(); i++)
				rowData.add(new Vector((List) data.get(i)));
			return;
		}
		int cnt = rowData.size();
		int dcnt = data.size();
		int cols = ((Vector) rowData.get(0)).size();
		int dcols = ((List) data.get(0)).size();

		for (int i = 0; i < cnt; i++) {

			Vector row = (Vector) rowData.get(i);
			if (i < dcnt) {
				row.addAll((List) data.get(i));

			} else {
				for (int j = 0; j < dcols; j++)
					row.add("");

			}

		}
		if (cnt < dcnt) {
			for (int l = cnt; l < dcnt; l++) {
				Vector newRow = new Vector();
				for (int k = 0; k < cols; k++) {
					newRow.add("");

				}
				newRow.addAll((List) data.get(l));
				rowData.add(newRow);

			}
		}

	}

	private char findMeasurementScale(OrderedMap map, String xPath) {

		Object o1 = map
				.get(xPath
						+ "/measurementScale[1]/nominal[1]/nonNumericDomain[1]/enumeratedDomain[1]/codeDefinition[1]/code[1]");
		if (o1 != null)
			return 'N';
		o1 = map.get(xPath
				+ "/measurementScale[1]/nominal[1]/nonNumericDomain[1]/textDomain[1]/definition[1]");
		if (o1 != null)
			return 'n';
		boolean b1 = map
				.containsKey(xPath
						+ "/measurementScale[1]/nominal[1]/nonNumericDomain[1]/enumeratedDomain[1]/entityCodeList[1]/entityReference[1]");
		if (b1)
			return 'R';

		o1 = map.get(xPath
				+ "/measurementScale[1]/ordinal[1]/nonNumericDomain[1]/enumeratedDomain[1]/codeDefinition[1]/code[1]");
		if (o1 != null)
			return 'O';
		o1 = map.get(xPath
				+ "/measurementScale[1]/ordinal[1]/nonNumericDomain[1]/textDomain[1]/definition[1]");
		if (o1 != null)
			return 'o';
		b1 = map.containsKey(xPath
				+ "/measurementScale[1]/ordinal[1]/nonNumericDomain[1]/enumeratedDomain[1]/entityCodeList[1]/entityReference[1]");
		if (b1)
			return 'R';

		return ' ';
	}

	private void getADP() {

		MorphoFrame morphoFrame = UIController.getInstance()
				.getCurrentActiveWindow();
		if (morphoFrame != null) {
			resultPane = morphoFrame.getDataViewContainerPanel();
		}// if
			// make sure resulPanel is not null
		if (resultPane != null) {
			MorphoDataPackage mdp = resultPane.getMorphoDataPackage();
			adp = mdp.getAbstractDataPackage();
		}
	}

	private String[] getEntityNames() {

		if (adp == null)
			getADP();

		String[] entNames = null;
		if (adp != null) {
			int cnt = adp.getEntityCount();
			entNames = new String[cnt];
			for (int i = 0; i < cnt; i++) {
				entNames[i] = adp.getEntityName(i);
			}
		} else {
			Log.debug(45,
					"Error - Unable to get the AbstractDataPackage in CodeImportPanel. ");
		}
		return entNames;
	}

	private String[] getColumnNames(int entityIndex) {

		if (adp == null)
			getADP();

		String cols[] = new String[0];
		int num = adp.getAttributeCountForAnEntity(entityIndex);

		if (adp != null) {
			cols = new String[num];
			for (int i = 0; i < num; i++) {
				cols[i] = (String) adp.getAttributeName(entityIndex, i);
			}
		}

		return cols;
	}

	private String getDelimiterString(String field_delimiter) {
		String str = "";
		String temp = field_delimiter.trim();
		if (temp.startsWith("#x")) {
			temp = temp.substring(2);
			if (temp.equals("0A"))
				str = "\n";
			if (temp.equals("09"))
				str = "\t";
			if (temp.equals("20"))
				str = " ";
		} else {
			str = temp;
		}
		return str;
	}

	/**
	 * The action to be executed when the panel is displayed. May be empty
	 */

	public void onLoadAction() {
	}

	/**
	 * The action to be taken when 'OK' is pressed. It checks for and removes
	 * all empty rows (if rank or name or both are empty). It returns true if
	 * the data is valid (contains atleast 1 level). Otherwise, it returns false
	 * 
	 * @return boolean returns true if the data entered is valid. Otherwise
	 *         returns false
	 * 
	 */

	public boolean onAdvanceAction() {

		return true;
	}

	/**
	 * checks that the user has filled in required fields - if not, highlights
	 * labels to draw attention to them
	 * 
	 * @return boolean true if user data validated OK. false if intervention
	 *         required
	 */

	public boolean validateUserInput() {

		return true;
	}

	/**
	 * gets the Map object that contains all the key/value paired
	 * 
	 * @param xPathRoot
	 *            the string xpath to which this dialog's xpaths will be
	 *            appended when making name/value pairs. For example, in the
	 *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the
	 *            root would be /eml:eml/dataset/keywordSet[2] NOTE - MUST NOT
	 *            END WITH A SLASH, BUT MAY END WITH AN INDEX IN SQUARE BRACKETS
	 *            []
	 * 
	 * @return data the OrderedMap object that contains all the key/value paired
	 *         settings for this particular panel
	 */
	public OrderedMap getPanelData(String xPathRoot) {

		return null;
	}

	/**
	 * sets the data in the sub panel using the key/values paired Map object
	 * 
	 * @param xPathRoot
	 *            the string xpath to which this dialog's xpaths will be
	 *            appended when making name/value pairs. For example, in the
	 *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the
	 *            root would be /eml:eml/dataset/keywordSet[2] NOTE - MUST NOT
	 *            END WITH A SLASH, BUT MAY END WITH AN INDEX IN SQUARE BRACKETS
	 *            []
	 * @param map
	 *            - OrderedMap of xPath-value pairs. xPaths in this map are
	 *            absolute xPath and not the relative xPaths
	 * 
	 **/

	public void setPanelData(String xPathRoot, OrderedMap map) {

	}

}

/**
 * class to handle the CustomTable Popup events that are generated when a user
 * clicks on a table header. It extends AbstractCustomTablePopupHandler that
 * implements the CustomTablePopupListener interface.
 * 
 */

class PopupHandler extends AbstractCustomTablePopupHandler {

	private JList list;
	private Vector taxonData = null;
	private String displayString = null;

	private String[] taxa = new String[] { "Kingdom", "Phylum", "Class",
			"Order", "Family", "Genus", "Species" };

	PopupHandler() {

		super();
		setModal(true);
		init();

	}

	private void init() {

		fillTaxonData();
		JLabel headLabel = WidgetFactory.makeHTMLLabel(
				/* "Select the taxon category" */Language.getInstance()
						.getMessage("TaxonImportPanel.headLabel"), 1, false);

		JPanel panel = new JPanel(new BorderLayout());
		list = new JList(taxonData);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent lse) {
				displayString = (String) list.getSelectedValue();
				PopupHandler.this.setVisible(false);
			}
		});
		list.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0,
				WizardSettings.TOP_PANEL_BG_COLOR));
		panel.add(headLabel, BorderLayout.NORTH);
		panel.add(list, BorderLayout.CENTER);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(panel, BorderLayout.CENTER);
		this.setSize(150, 200);

	}

	private void fillTaxonData() {

		taxonData = new Vector();
		for (int i = 0; i < taxa.length; i++)
			taxonData.add(taxa[i]);
	}

	/**
	 * Function to retrieve the string to be displayed in the header of the
	 * CustomTable.
	 * 
	 * @return String the string to be displayed in the header of the table.
	 *         returns null if no value is present
	 */
	public String getDisplayString() {

		return displayString;
	}

	/**
	 * Function to retrieve the dialog that is to be displayed when the user
	 * clicks on the header of the CustomTable. The dialog is responsible of
	 * disposing itself after the input is received.
	 * 
	 * @return JDialog a dialog that is popped up when a user clicks on the
	 *         table header
	 */

	public JDialog getPopupDialog() {

		displayString = null;
		list.setSelectedIndex(-1);
		return this;
	}

}
