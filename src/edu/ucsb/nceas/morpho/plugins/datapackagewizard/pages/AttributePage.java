/**
 *  '$RCSfile: AttributePage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2003-12-03 02:38:49 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.NominalOrdinalPanel;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributeSettings;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;


import edu.ucsb.nceas.utilities.OrderedMap;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;

import java.util.List;
import java.util.ArrayList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Component;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;


public class AttributePage extends AbstractWizardPage {

  private final String pageID     = DataPackageWizardInterface.ATTRIBUTE_PAGE;
  private final String nextPageID = "";
  private final String pageNumber = "";
  private final String title      = "Attribute Page";
  private final String subtitle   = "";

  public static final int BORDERED_PANEL_TOT_ROWS = 9;
  public static final int DOMAIN_NUM_ROWS = 8;

  private final String CONFIG_KEY_STYLESHEET_LOCATION = "stylesheetLocation";
  private final String CONFIG_KEY_MCONFJAR_LOC   = "morphoConfigJarLocation";

  private JTextField attribNameField;

  private JLabel kwLabel;
  private JLabel attribNameLabel;
  private JLabel attribDefinitionLabel;
  private JLabel measScaleLabel;
  private JPanel currentPanel;

  // to be visible in setData() function call
  private JPanel radioPanel;

  private JPanel nominalPanel;
  private JPanel ordinalPanel;
  private JPanel intervalPanel;
  private JPanel ratioPanel;
  private JPanel dateTimePanel;

  private JPanel middlePanel;

  private String measurementScale;

  private final String xPathRoot = AttributeSettings.Attribute_xPath;

  private JTextArea attribDefinitionField;
  private final String[] buttonsText
      = {
          WizardSettings.HTML_NO_TABLE_OPENING
          +"NOMINAL:&nbsp;&nbsp;&nbsp;numbers have been assigned only for "
          +"categorizing a variable. "
          +WizardSettings.HTML_EXAMPLE_FONT_OPENING
          +"e.g: assigning 1 for male and 2 for female"
          +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
          +WizardSettings.HTML_NO_TABLE_CLOSING,

          WizardSettings.HTML_NO_TABLE_OPENING
          +"ORDINAL:&nbsp;&nbsp;&nbsp;can determine order of categories, "
          +"but not magnitude of their differences. "
          +WizardSettings.HTML_EXAMPLE_FONT_OPENING
          +"e.g: ranking system: 1=good,2=fair,3=poor."
          +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
          +WizardSettings.HTML_NO_TABLE_CLOSING,

          WizardSettings.HTML_NO_TABLE_OPENING
          +"INTERVAL:&nbsp;&nbsp;data consist of equidistant points on a "
          +"scale."+WizardSettings.HTML_EXAMPLE_FONT_OPENING
          +"e.g: Celsius scale (no "
          +"natural zero point; 20C is not twice as hot as 10C)"
          +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
          +WizardSettings.HTML_NO_TABLE_CLOSING,

          WizardSettings.HTML_NO_TABLE_OPENING
          +"RATIO:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;data which "
          +"has equidistant points <b>and</b> a meaningful zero point. "
          +WizardSettings.HTML_EXAMPLE_FONT_OPENING+"e.g: length in meters"
          +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
          +WizardSettings.HTML_NO_TABLE_CLOSING,

          WizardSettings.HTML_NO_TABLE_OPENING
          +"DATE-TIME: values that comply with the Gregorian calendar "
          +"system."+WizardSettings.HTML_EXAMPLE_FONT_OPENING
          +"e.g:  2002-10-14T09:13:45"
          +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
          +WizardSettings.HTML_NO_TABLE_CLOSING
        };


  private final String[] measScaleElemNames = new String[5];

  // these must correspond to indices of measScaleElemNames array
  public static final int MEASUREMENTSCALE_NOMINAL  = 0;
  public static final int MEASUREMENTSCALE_ORDINAL  = 1;
  public static final int MEASUREMENTSCALE_INTERVAL = 2;
  public static final int MEASUREMENTSCALE_RATIO    = 3;
  public static final int MEASUREMENTSCALE_DATETIME = 4;


    //
  /*public AttributePage(JFrame parent) {

    super(parent);

    initNames();
    init();
    this.setVisible(true);
  }

  public AttributePage(JFrame parent, boolean showNow) {

    super(parent);

    initNames();
    init();
    this.setVisible(showNow);
  }*/

  public AttributePage() {

    initNames();
    init();
  }

  private void initNames() {

    measScaleElemNames[MEASUREMENTSCALE_NOMINAL]  = "nominal";
    measScaleElemNames[MEASUREMENTSCALE_ORDINAL]  = "ordinal";
    measScaleElemNames[MEASUREMENTSCALE_INTERVAL] = "interval";
    measScaleElemNames[MEASUREMENTSCALE_RATIO]    = "ratio";
    measScaleElemNames[MEASUREMENTSCALE_DATETIME] = "datetime";
  }


  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    middlePanel = new JPanel();
    this.setLayout( new BorderLayout());
    this.add(middlePanel,BorderLayout.CENTER);
    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));

    middlePanel.add(WidgetFactory.makeHTMLLabel(
              "<font size=\"4\"><b>Define Attribute or Column:</b></font>", 1));

    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    ////
    JPanel attribNamePanel = WidgetFactory.makePanel(1);
    attribNameLabel = WidgetFactory.makeLabel("Attribute name:", true, WizardSettings.WIZARD_REDUCED_CONTENT_LABEL_DIMS);
    attribNamePanel.add(attribNameLabel);
    attribNameField = WidgetFactory.makeOneLineTextField();
    attribNamePanel.add(attribNameField);
    middlePanel.add(attribNamePanel);

    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    ////////////////////////////////////////////////////////////////////////////

    // this embedding and use of html on a label instead of calling
    // WidgetFactory.makeHTMLLabel() is required because the Java HTML rendering
    // on JLabels seems to be buggy - using WidgetFactory.makeHTMLLabel() yields
    // labels that resize themselves depending which radiobutton is chosen :-(
    Dimension infoDim = new Dimension(WizardSettings.DIALOG_WIDTH,40);
    JPanel infoPanel  = new JPanel();
    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));

    JLabel infoLabel = WidgetFactory.makeLabel(

    WizardSettings.HTML_NO_TABLE_OPENING
    +"Define the contents of the attribute (or column) precisely, "
    +"so that a data user could interpret the attribute accurately.<br></br>"
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    +"&nbsp;&nbsp;[Example(s):&nbsp;&nbsp;&nbsp;"
    +"\"spden\" is the number of individuals of all macro invertebrate species "
    +"found in the plot]"+WizardSettings.HTML_EXAMPLE_FONT_CLOSING
    +WizardSettings.HTML_NO_TABLE_OPENING, false, infoDim);

    infoLabel.setAlignmentX(1.0f);

    infoPanel.add(infoLabel);
    infoPanel.add(Box.createGlue());
    middlePanel.add(infoPanel);

    JPanel attribDefinitionPanel = WidgetFactory.makePanel(2);

    attribDefinitionLabel = WidgetFactory.makeLabel("Definition", true, WizardSettings.WIZARD_REDUCED_CONTENT_LABEL_DIMS);
    attribDefinitionLabel.setVerticalAlignment(SwingConstants.TOP);
    attribDefinitionLabel.setAlignmentY(SwingConstants.TOP);
    attribDefinitionPanel.add(attribDefinitionLabel);

    attribDefinitionField = WidgetFactory.makeTextArea("", 3, true);
    JScrollPane jscrl = new JScrollPane(attribDefinitionField);
    attribDefinitionPanel.add(jscrl);
    middlePanel.add(attribDefinitionPanel);

    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    ////
    ActionListener listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "got radiobutton command: "+e.getActionCommand());

        //undo any hilites:

        if (e.getActionCommand().equals(buttonsText[0])) {
          nominalPanel.repaint();
          setMeasurementScaleUI(nominalPanel);
          setMeasurementScale(measScaleElemNames[0]);
	  nominalPanel.repaint();

        } else if (e.getActionCommand().equals(buttonsText[1])) {

          setMeasurementScaleUI(ordinalPanel);
          setMeasurementScale(measScaleElemNames[1]);


        } else if (e.getActionCommand().equals(buttonsText[2])) {

          setMeasurementScaleUI(intervalPanel);
          setMeasurementScale(measScaleElemNames[2]);


        } else if (e.getActionCommand().equals(buttonsText[3])) {

          setMeasurementScaleUI(ratioPanel);
          setMeasurementScale(measScaleElemNames[3]);

        } else if (e.getActionCommand().equals(buttonsText[4])) {

          setMeasurementScaleUI(dateTimePanel);
          setMeasurementScale(measScaleElemNames[4]);

        }
      }
    };

    measScaleLabel = WidgetFactory.makeLabel(
                                "Select and define a Measurement Scale:", true,
                                WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);

    middlePanel.add(measScaleLabel);

    radioPanel = WidgetFactory.makeRadioPanel(buttonsText, -1, listener);

    middlePanel.add(radioPanel);

    currentPanel  = getEmptyPanel();

    middlePanel.add(currentPanel);

    nominalPanel  = getNomOrdPanel(MEASUREMENTSCALE_NOMINAL);
    ordinalPanel  = getNomOrdPanel(MEASUREMENTSCALE_ORDINAL);
    intervalPanel = getIntervalRatioPanel(MEASUREMENTSCALE_INTERVAL);
    ratioPanel    = getIntervalRatioPanel(MEASUREMENTSCALE_RATIO);
    dateTimePanel = getDateTimePanel();

  }

  private void setMeasurementScale(String scale) {

    this.measurementScale = scale;
  }




  private void setMeasurementScaleUI(JPanel panel) {

    middlePanel.remove(currentPanel);
    currentPanel = panel;
    middlePanel.add(currentPanel);
    ((WizardPageSubPanelAPI)currentPanel).onLoadAction();
    currentPanel.validate();
    currentPanel.repaint();
    middlePanel.validate();
    middlePanel.repaint();
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JPanel getEmptyPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(BORDERED_PANEL_TOT_ROWS);

    panel.add(WidgetFactory.makeDefaultSpacer());

    return panel;
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  // nom_ord can be MEASUREMENTSCALE_NOMINAL or MEASUREMENTSCALE_ORDINAL
  private NominalOrdinalPanel getNomOrdPanel(int nom_ord) {

    NominalOrdinalPanel panel = new NominalOrdinalPanel(this);
    WidgetFactory.addTitledBorder(panel, measScaleElemNames[nom_ord]);
    return panel;
  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  private IntervalRatioPanel getIntervalRatioPanel(int intvl_ratio) {

    IntervalRatioPanel panel = new IntervalRatioPanel(this);
    WidgetFactory.addTitledBorder(panel, measScaleElemNames[intvl_ratio]);
    return panel;
  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  private DateTimePanel getDateTimePanel() {

    DateTimePanel panel = new DateTimePanel(this);
    WidgetFactory.addTitledBorder(panel, measScaleElemNames[MEASUREMENTSCALE_DATETIME]);
    return panel;
  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  /**
   *  calls validate() and repaint() on the middle panel
   */
  public void refreshUI() {

    currentPanel.validate();
    currentPanel.repaint();
    middlePanel.validate();
    middlePanel.repaint();
  }

  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *  Here, it does nothing because this is just a Panel and not the outer container
   */

  public void onRewindAction() {
  }

  /**
   *  The action to be executed when the page is loaded
   *  Here, it does nothing because this is just a Panel and not the outer container
   */

  public void onLoadAction() {
  }

  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return this.pageID;}

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
  public String getNextPageID() { return this.nextPageID; }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    if (attribNameField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(attribNameLabel);
      attribNameField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(attribNameLabel);

    if (attribDefinitionField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(attribDefinitionLabel);
      attribDefinitionField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(attribDefinitionLabel);

    if (measurementScale==null) {

      WidgetFactory.hiliteComponent(measScaleLabel);
      return false;
    }
    WidgetFactory.unhiliteComponent(measScaleLabel);

    return ((WizardPageSubPanelAPI)currentPanel).validateUserInput();
  }



  /**
   *  @return a List contaiing 2 String elements - one for each column of the
   *  2-col list in which this surrogate is displayed
   *
   */
  public List getSurrogate() {

    WidgetFactory.unhiliteComponent(attribDefinitionLabel);

    List surrogate = new ArrayList();

    //attribName (first column) surrogate:
    String attribName   = attribNameField.getText().trim();
    if (attribName==null) attribName = "";
    surrogate.add(attribName);


    //attribDefinition (second column) surrogate:
    String attribDefinition   = attribDefinitionField.getText().trim();
    if (attribDefinition==null) attribDefinition = "";
    surrogate.add(attribDefinition);


    //measurementScale (third column) surrogate:
    if (measurementScale==null) measurementScale = "";
    surrogate.add(measurementScale);

    return surrogate;
  }


  /**
   *  gets the Map object that contains all the key/value paired
   *
   *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
   *            appended when making name/value pairs.  For example, in the
   *            following xpath:
   *
   *            /eml:eml/dataset/dataTable/attributeList/attribute[2]
   *            /measurementScale/nominal/nonNumericDomain/textDomain/definition
   *
   *            the root would be:
   *
   *              /eml:eml/dataset/dataTable/attributeList
   *                                /attribute[2]
   *
   *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
   *            SQUARE BRACKETS []
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap   returnMap     = new OrderedMap();
  //////////////////
  public OrderedMap getPageData() {

	  return this.getPageData(xPathRoot);
  }
  public OrderedMap getPageData(String xPathRoot) {

    returnMap.clear();

    String attribName = attribNameField.getText().trim();
    if (attribName!=null && !attribName.equals("")) {
      returnMap.put(xPathRoot + "/attributeName", attribName);
    }

    String attribDef = attribDefinitionField.getText().trim();
    if (attribDef!=null && !attribDef.equals("")) {
      returnMap.put(xPathRoot + "/attributeDefinition", attribDef);
    }

    if (measurementScale!=null && !measurementScale.equals("")) {

      returnMap.putAll(
        ((WizardPageSubPanelAPI)currentPanel).getPanelData(
                            xPathRoot+"/measurementScale/"+measurementScale) );
    }

    return returnMap;
  }



  private String findMeasurementScale(OrderedMap map) {

	Object o1 = map.get(AttributeSettings.Nominal_xPath+"/enumeratedDomain[1]/codeDefinition[1]/code");
	if(o1 != null) return "Nominal";
	o1 = map.get(AttributeSettings.Nominal_xPath+"/textDomain[1]/definition");
	if(o1 != null) return "Nominal";

	o1 = map.get(AttributeSettings.Ordinal_xPath+"/enumeratedDomain[1]/codeDefinition[1]/code");
	if(o1 != null) return "Ordinal";
	o1 = map.get(AttributeSettings.Ordinal_xPath+"/textDomain[1]/definition");
	if(o1 != null) return "Ordinal";

	o1 = map.get(AttributeSettings.Interval_xPath+"/unit/standardUnit");
	if(o1 != null) return "Interval";
	o1 = map.get(AttributeSettings.Interval_xPath+"/numericDomain/numberType");
	if(o1 != null) return "Interval";

	o1 = map.get(AttributeSettings.Ratio_xPath+"/unit/standardUnit");
	if(o1 != null) return "Ratio";
	o1 = map.get(AttributeSettings.Ratio_xPath+"/numericDomain/numberType");
	if(o1 != null) return "Ratio";

	o1 = map.get(AttributeSettings.DateTime_xPath+"/formatString");
	if(o1 != null) return "Datetime";
	o1 = map.get(AttributeSettings.DateTime_xPath+"/dateTimePrecision");
	if(o1 != null) return "Datetime";

	return "";
  }

  /**
   *  sets the Data in the Attribute Dialog fields. This is called from the TextImportWizard
   *  when it wants to set some information it has already guessed from the given data file.
   *
   *  Any data in the AttributeDialog can be set through this method. The TextImportWizard
   *  however sets only the "Attribute Name", "Measurement Scale", "Number Type" and the
   *  "Enumeration Code Definitions"
   *
   *  @param  xPathRoot - this is the relative xPath of the current attribute
   *
   *  @param  map - Data is passed as OrderedMap of xPath-value pairs. xPaths in this map
   *		    are absolute xPath and not the relative xPaths
   *
   *  @param  mScale - The guessed measurement scale. The appropriate radioButton is
   *			selected and that Panel is displayed
   *
   *
   */

  public void setPageData(OrderedMap map) {

	String mScale = findMeasurementScale(map);
	String xPathRoot = AttributeSettings.Attribute_xPath;
	String name = (String)map.get(xPathRoot + "/attributeName");
	if(name != null)
		attribNameField.setText(name);
	String defn = (String)map.get(xPathRoot + "/attributeDefinition");
	if(defn != null)
		attribDefinitionField.setText(defn);

	if(mScale == null || mScale.equals(""))
		return;

	measurementScale = mScale;

	int componentNum = -1;
	if(measurementScale.equalsIgnoreCase("nominal")) {
		  setMeasurementScaleUI(nominalPanel);
		  setMeasurementScale(measScaleElemNames[0]);
		  componentNum = 0;
	}
	else if(measurementScale.equalsIgnoreCase("ordinal")) {
		  setMeasurementScaleUI(ordinalPanel);
		  setMeasurementScale(measScaleElemNames[1]);
		  componentNum = 1;
	}
	if(measurementScale.equalsIgnoreCase("interval")) {
		  setMeasurementScaleUI(intervalPanel);
		  setMeasurementScale(measScaleElemNames[2]);
		  componentNum = 2;
	}
	if(measurementScale.equalsIgnoreCase("ratio")) {
		setMeasurementScaleUI(ratioPanel);
		setMeasurementScale(measScaleElemNames[3]);
		componentNum = 3;
	}
	if(measurementScale.equalsIgnoreCase("datetime")) {
		setMeasurementScaleUI(dateTimePanel);
		setMeasurementScale(measScaleElemNames[4]);
		componentNum = 4;;
	}

	//selects the appropriate radio button

	if(componentNum != -1) {
		JRadioButton jrb = (JRadioButton)(radioPanel.getComponent(componentNum));
		jrb.setSelected(true);
	}
	((NominalOrdinalPanel)nominalPanel).setPanelData(xPathRoot+ "/measurementScale/nominal/nonNumericDomain", map);
	((NominalOrdinalPanel)ordinalPanel).setPanelData(xPathRoot+ "/measurementScale/ordinal/nonNumericDomain", map);
	((IntervalRatioPanel)intervalPanel).setPanelData(xPathRoot+ "/measurementScale/interval", map);
	((IntervalRatioPanel)ratioPanel).setPanelData(xPathRoot+ "/measurementScale/ratio", map);
	((DateTimePanel)dateTimePanel).setPanelData(xPathRoot+ "/measurementScale/datetime", map);
	ordinalPanel.invalidate();
	refreshUI();
	return;
  }

  /**
   *  gets the HTML representation of the attribute values
   *  The HTML text references the entity.css file
   *
   *  @return   the HTML text describes the attribute values
   */

  public String getText() {
	String text = "<html> <head> <link href=\"" + getFullStylePath() + "/entity.css\" type=\"text/css\" rel=\"stylesheet\"> </head> ";
	text += "<body>";
	text += "<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" width=\"100%\" cols = \"5\"> ";


	// First row - Name:
	text += "<tr>";
	text += "<td class=\"highlight\"  width = \"35%\" > Name: </td>";
	text += "<td class=\"secondCol\" width=\"65%\" colspan=\"4\">" + this.attribNameField.getText() + "</td>";
	text += "</tr>";

	// Second row - Definition:
	text += "<tr>";
	text += "<td class=\"highlight\"  width = \"35%\" > Definition: </td>";
	text += "<td class=\"secondCol\" width=\"65%\" colspan=\"4\">" + this.attribDefinitionField.getText() + "</td>";
	text += "</tr>";

	String scale = measurementScale;
	int index = 0;


	OrderedMap map = getPageData(AttributeSettings.Attribute_xPath);

	if(measurementScale.equalsIgnoreCase("Nominal")) {
		Object o1 = map.get(AttributeSettings.Nominal_xPath+"/enumeratedDomain[1]/codeDefinition[1]/code");
		if(o1 != null) { scale += "; Enumerated values"; index = 0; }
		else {
			o1 = map.get(AttributeSettings.Nominal_xPath+"/textDomain[1]/definition");
			if(o1 != null)  {
				scale += "; Text values";
				index = 1;
			}
			// both arent there. so Enumerated is the default -
			// this occurs only at the first time, before the user edits the values
			else {
				scale += "; Enumerated values";
				index = 0;
			}
		}
	}
	else if(measurementScale.equalsIgnoreCase("Ordinal")) {
		Object o1 = map.get(AttributeSettings.Ordinal_xPath+"/enumeratedDomain[1]/codeDefinition[1]/code");
		if(o1 != null) { scale += "; Enumerated values"; index = 0; }
		else {
			o1 = map.get(AttributeSettings.Ordinal_xPath+"/textDomain[1]/definition");
			if(o1 != null)  {
				scale += "; Text values";
				index = 1;
			}
			// both arent there. so Enumerated is the default -
			// this occurs only at the first time, before the user edits the values
			else {
				scale += "; Enumerated values";
				index = 0;
			}
		}
	}

	// Third row - Measurement Scale:
	text += "<tr>";
	text += "<td class = \"highlight\"  width = \"35%\" > Measurement Scale: </td>";
	text += "<td class = \"secondCol\" width=\"65%\" colspan=\"4\"> " + scale + "</td>";
	text += "</tr>";



	// Nominal measurement scale
	if(measurementScale.equalsIgnoreCase("Nominal") || measurementScale.equalsIgnoreCase("Ordinal")) {

		String mainXPath;
		if (measurementScale.equalsIgnoreCase("Nominal")) mainXPath = AttributeSettings.Nominal_xPath;
		else mainXPath = AttributeSettings.Ordinal_xPath;
		//Enumerated values
		if(index == 0) {
			text += "<tr>";
			text += "<td class = \"tablehead\" > Definitions: </td>" ;

			String table="";

			table += "<td class = \"tablehead\" >Code </td>";
			table += "<td class = \"tablehead\" >Definition </td>";
			table += "<td class = \"tablehead\" colspan=\"2\">Source </td>";
			table += "</tr>";

			int i = 1;
			String enumPath = "/enumeratedDomain[1]/codeDefinition[";
			while(true) {
				Object o = map.get(mainXPath + enumPath + i + "]/code" );
				if(o == null) break;
				String e1 = (String) o;
				o = map.get(mainXPath + enumPath + i + "]/definition" );
				String e2;
				if( o == null) e2 = "no definition";
				else e2 = (String)o;

				o = map.get(mainXPath + enumPath + i + "]/source" );
				String e3;
				if( o == null) e3 ="";
				else e3 = (String) o;
				table += "<tr>";
				table += "<td class = \"highlight\"> </td>";
				table += "<td >" + e1 + "</td> <td  >" + e2 + "</td> <td colspan=\"2\">" + e3 + "</td>";
				table += "</tr>";
				i++;
			}

			text += table;


		}
		// Text values
		else if (index == 1) {
			String textPath = "/textDomain[1]/";
			String data  = (String) map.get( mainXPath + textPath + "definition");
			text += "<tr>";
			text += "<td class =\"highlight\"  width = \"35%\"> Definition: </td>";
			text += "<td class =\"secondCol\" width=\"65%\" colspan=\"4\"> " +  data + "</td>";
			text += "</tr>";

			Object o = map.get(mainXPath + textPath + "source");
			if(o != null) {
				text += "<tr>";
				text += "<td class =\"highlight\"  width = \"35%\"> Source: </td>";
				text += "<td class=\"secondCol\" width=\"65%\" colspan=\"4\"> " + (String)o + "</td>";
				text += "</tr>";
			}
			int i = 1;

			while(true) {
				Object o1 = map.get(mainXPath + textPath + "pattern[" + i + "]");
				if(o1 ==null) break;
				text += "<tr>";
				if(i==1) {
					text += "<td class=\"highlight\"  width = \"35%\" valign =\"top\"> Patterns: </td>";
				} else {
					text += "<td class =\"highlight\"></td>";
				}

				text += "<td class = \"secondCol\" width=\"65%\" colspan=\"4\">" + (String)o1 + "</td>";
				text += "</tr>";
				i++;
			}

		} // end of else if

	} // end of Nomimal/Ordinal

	// Interval / Ratio measurement scales

	if(measurementScale.equalsIgnoreCase("Interval") || measurementScale.equalsIgnoreCase("Ratio")) {

		String mainXPath;
		if(measurementScale.equalsIgnoreCase("Interval")) mainXPath = AttributeSettings.Interval_xPath;
		else mainXPath = AttributeSettings.Ratio_xPath;

		String unit = (String) map.get(mainXPath + "/unit/standardUnit");

		text += "<tr>";
		text += "<td class = \"highlight\"  width = \"35%\" > Standard Unit: </td>";
		text += "<td class = \"secondCol\" width=\"65%\" colspan=\"4\"> " + unit + "</td>";
		text += "</tr>";

		String precision = (String) map.get(mainXPath + "/precision");

		text += "<tr>";
		text += "<td class = \"highlight\"  width = \"35%\"> Precision: </td>";
		text += "<td class = \"secondCol\" width=\"65%\" colspan=\"4\">" + precision + "</td>";
		text += "</tr>";

		String numberType = (String) map.get(mainXPath + "/numericDomain/numberType");

		text += "<tr>";
		text += "<td class = \"highlight\"  width = \"35%\"> Number Type: </td>";
		text += "<td class = \"secondCol\" width=\"65%\" colspan=\"4\">" + numberType + "</td>";
		text += "</tr>";

		Object o1 = map.get(mainXPath + "/numericDomain/bounds[1]/minimum");
		Object o2 = map.get(mainXPath + "/numericDomain/bounds[1]/maximum");
		// add Bounds
		if(o1 != null || o2 != null) {

			int pos = 1;
			while(true) {
				Object ob1 = map.get(mainXPath + "/numericDomain/bounds[" + pos + "]/minimum");
				Object ob2 = map.get(mainXPath + "/numericDomain/bounds[" + pos + "]/maximum");
				if(ob1 == null && ob2 == null) break;
				String e1,e2;
				if(ob1 == null) e1 = ""; else e1 = (String)ob1;
				if(ob2 == null) e2 = ""; else e2 = (String)ob2;

				Object ob3 = map.get(mainXPath + "/numericDomain/bounds[" + pos + "]/minimum/@exclusive");
				Object ob4 = map.get(mainXPath + "/numericDomain/bounds[" + pos + "]/maximum/@exclusive");
				String e3,e4;
				if(ob3 == null || ((String)ob3).equalsIgnoreCase("false")) e3 = "(incl)"; else e3 = "(excl)";
				if(ob4 == null || ((String)ob4).equalsIgnoreCase("false")) e4 = "(incl)"; else e4 = "(excl)";

				text += "<tr>";
				if(pos == 1) {
					text += "<td class = \"highlight\"  width = \"35%\"> Bounds: </td>";
				} else {
					text += "<td class= \"highlight\"> </td>";
				}
				text += "<td>min: " + e1 + "</td>";
				text += "<td>" + e3 + "</td>";
				text += "<td>max: " + e2 + "</td>";
				text += "<td>" + e4 + "</td>";
				text += "</tr>";
				pos++;
			}
		} // end of adding bounds

	} // end of Interval/Ratio

	if(measurementScale.equalsIgnoreCase("Datetime")) {

		String mainXPath = AttributeSettings.DateTime_xPath;

		String format = (String) map.get(mainXPath + "/formatString");
		String precision = (String) map.get(mainXPath + "/dateTimePrecision");
		text += "<tr>";
		text += "<td class = \"highlight\"  width = \"35%\" > Format: </td>";
		text += "<td class = \"secondCol\" width=\"65%\" colspan=\"4\"> " + format + "</td>";
		text += "</tr>";

		text += "<tr>";
		text += "<td class = \"highlight\"  width = \"35%\" > Precision: </td>";
		text += "<td class = \"secondCol\" width=\"65%\" colspan=\"4\"> " + precision + "</td>";
		text += "</tr>";

		Object o1 = map.get(mainXPath + "/dateTimeDomain/bounds[1]/minimum");
		Object o2 = map.get(mainXPath + "/dateTimeDomain/bounds[1]/maximum");
		// add Bounds
		if(o1 != null || o2 != null) {

			int pos = 1;
			while(true) {
				Object ob1 = map.get(mainXPath + "/dateTimeDomain/bounds[" + pos + "]/minimum");
				Object ob2 = map.get(mainXPath + "/dateTimeDomain/bounds[" + pos + "]/maximum");
				if(ob1 == null && ob2 == null) break;
				String e1,e2;
				if(ob1 == null) e1 = ""; else e1 = (String)ob1;
				if(ob2 == null) e2 = ""; else e2 = (String)ob2;

				Object ob3 = map.get(mainXPath + "/dateTimeDomain/bounds[" + pos + "]/minimum/@exclusive");
				Object ob4 = map.get(mainXPath + "/dateTimeDomain/bounds[" + pos + "]/maximum/@exclusive");
				String e3,e4;
				if(ob3 == null || ((String)ob3).equalsIgnoreCase("false")) e3 = "(incl)"; else e3 = "(excl)";
				if(ob4 == null || ((String)ob4).equalsIgnoreCase("false")) e4 = "(incl)"; else e4 = "(excl)";

				text += "<tr>";
				if(pos == 1) {
					text += "<td class = \"highlight\" > Bounds: </td>";
				} else {
					text += "<td class=\"highlight\"></td>";
				}
				text += "<td class=\"secondCol\">min: " + e1 + "</td>";
				text += "<td class=\"secondCol\">" + e3 + "</td>";
				text += "<td class=\"secondCol\">max: " + e2 + "</td>";
				text += "<td class=\"secondCol\">" + e4 + "</td>";
				text += "</tr>";
				pos++;
			}

		} // end of adding bounds

	} // end of DateTime

	text += "</table>";
	text += "</body>";
	text += "</html>";

	Log.debug(15,text);
	return text;

  } // end of function - getText()


  private String getFullStylePath()    {
	String FULL_STYLE_PATH = null;
	ConfigXML config = Morpho.getConfiguration();
        if (FULL_STYLE_PATH==null) {
            StringBuffer pathBuff = new StringBuffer();
            pathBuff.append("jar:file:");
            pathBuff.append(new File("").getAbsolutePath());
            pathBuff.append("/");
            pathBuff.append(config.get(CONFIG_KEY_MCONFJAR_LOC, 0));
            pathBuff.append("!/");
            pathBuff.append(config.get(CONFIG_KEY_STYLESHEET_LOCATION, 0));
            FULL_STYLE_PATH = pathBuff.toString();
            pathBuff = null;
        }
        return FULL_STYLE_PATH;
   }






}