/**
 *  '$RCSfile: AttributePage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-01-07 02:02:17 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;


public class AttributePage extends AbstractWizardPage {

  private final String pageID     = DataPackageWizardInterface.ATTRIBUTE_PAGE;
  private final String nextPageID = "";
  private final String pageNumber = "";
  private final String title      = "Attribute Page";
  private final String subtitle   = "";

  public static final int BORDERED_PANEL_TOT_ROWS = 7;
  public static final int DOMAIN_NUM_ROWS = 8;

  private final String CONFIG_KEY_STYLESHEET_LOCATION = "stylesheetLocation";
  private final String CONFIG_KEY_MCONFJAR_LOC   = "morphoConfigJarLocation";

  private JTextField attribNameField;
  private JTextField attribLabelField;

  private JLabel attribNameLabel;
  private JLabel attribLabelLabel;
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
  private JPanel topMiddlePanel;

  private String measurementScale;

  private final String xPathRoot = AttributeSettings.Attribute_xPath;

  private JTextArea attribDefinitionField;

  final String ATTRIB_NAME_HELP
    = WizardSettings.HTML_NO_TABLE_OPENING
    +"Name of the attribute as it appears in the data file"
    +WizardSettings.HTML_NO_TABLE_CLOSING;

  final String ATTRIB_LABEL_HELP
    = WizardSettings.HTML_NO_TABLE_OPENING
    +"A more readable label for the attribute"
    +WizardSettings.HTML_NO_TABLE_CLOSING;

  final String ATTRIB_DEFN_HELP
    = WizardSettings.HTML_NO_TABLE_OPENING
    +"Define the contents of the attribute (or column) precisely, "
    +"so that a data user could interpret the attribute accurately.<br></br>"
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    +"e.g:&nbsp;&nbsp;&nbsp;"
    +"\"spden\" is the number of individuals of all macro "
    +"invertebrate species found in the plot"
    +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
    +WizardSettings.HTML_NO_TABLE_OPENING;

  private final String[] buttonsText
      = {
          WizardSettings.HTML_NO_TABLE_OPENING
          +"Unordered:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
          +" unordered categories or text 	(statistically &nbsp;<b>nominal</b>) "
          +WizardSettings.HTML_EXAMPLE_FONT_OPENING
          + "e.g: Male, Female"
          +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
          +WizardSettings.HTML_NO_TABLE_CLOSING,

          WizardSettings.HTML_NO_TABLE_OPENING
          +"Ordered:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
          +"ordered categories (statistically &nbsp;<b>ordinal</b>) "
          +WizardSettings.HTML_EXAMPLE_FONT_OPENING
          +"e.g: Low, High"
          +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
          +WizardSettings.HTML_NO_TABLE_CLOSING,

          WizardSettings.HTML_NO_TABLE_OPENING
          +"Relative:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
          +" values from a scale with equidistant points "
          +"(statistically &nbsp;<b>interval</b>) "
          +WizardSettings.HTML_EXAMPLE_FONT_OPENING
          +"e.g: 12.2 meters"
          +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
          +WizardSettings.HTML_NO_TABLE_CLOSING,

          WizardSettings.HTML_NO_TABLE_OPENING
          +"Absolute:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
          +"measurement scale with a meaningful zero point "
          +"(statistically &nbsp;<b>ratio</b>) "
          +WizardSettings.HTML_EXAMPLE_FONT_OPENING
          +"e.g: 273 Kelvin"
          +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
          +WizardSettings.HTML_NO_TABLE_CLOSING,

          WizardSettings.HTML_NO_TABLE_OPENING
          +"Date-Time:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
          +"date or time values from the Gregorian calendar "
          +WizardSettings.HTML_EXAMPLE_FONT_OPENING
          +"e.g: 2002-10-24"
          +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
          +WizardSettings.HTML_NO_TABLE_CLOSING
        };


  private final String[] measScaleElemNames = new String[5];
  private final String[] measScaleDisplayNames = new String[5];

  // these must correspond to indices of measScaleElemNames array
  public static final int MEASUREMENTSCALE_NOMINAL  = 0;
  public static final int MEASUREMENTSCALE_ORDINAL  = 1;
  public static final int MEASUREMENTSCALE_INTERVAL = 2;
  public static final int MEASUREMENTSCALE_RATIO    = 3;
  public static final int MEASUREMENTSCALE_DATETIME = 4;

  private final Dimension HELP_DIALOG_SIZE = new Dimension(400, 500);

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

    measScaleDisplayNames[MEASUREMENTSCALE_NOMINAL]  = "Unordered";
    measScaleDisplayNames[MEASUREMENTSCALE_ORDINAL]  = "Ordered";
    measScaleDisplayNames[MEASUREMENTSCALE_INTERVAL] = "Relative";
    measScaleDisplayNames[MEASUREMENTSCALE_RATIO]    = "Absolute";
    measScaleDisplayNames[MEASUREMENTSCALE_DATETIME] = "Datetime";
  }


  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    middlePanel = new JPanel();
    topMiddlePanel = new JPanel();

    this.setLayout( new BorderLayout());
    this.add(middlePanel,BorderLayout.CENTER);
    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));

    topMiddlePanel.setLayout(new BoxLayout(topMiddlePanel, BoxLayout.Y_AXIS));
    topMiddlePanel.add(WidgetFactory.makeHTMLLabel(
              "<font size=\"4\"><b>Define Attribute or Column:</b></font>", 1));

    topMiddlePanel.add(WidgetFactory.makeDefaultSpacer());


    /////////////////////////////////////////////

    JPanel namePanel = new JPanel();
    namePanel.setLayout(new GridLayout(1,2));

    JPanel attribNamePanel = WidgetFactory.makePanel(1);
    attribNameLabel = WidgetFactory.makeLabel("Name:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    attribNamePanel.add(attribNameLabel);
    attribNameField = WidgetFactory.makeOneLineTextField();
    attribNamePanel.add(attribNameField);
    JLabel attribNameHelpLabel = getLabel(this.ATTRIB_NAME_HELP);
    namePanel.add(attribNamePanel);
    namePanel.add(attribNameHelpLabel);

    topMiddlePanel.add(namePanel);
    topMiddlePanel.add(WidgetFactory.makeDefaultSpacer());


    /////////////////////////////////////////////

    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new GridLayout(1,2));

    JPanel attribLabelPanel = WidgetFactory.makePanel(1);
    attribLabelLabel = WidgetFactory.makeLabel("Label:", false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    attribLabelPanel.add(attribLabelLabel);
    attribLabelField = WidgetFactory.makeOneLineTextField();
    attribLabelPanel.add(attribLabelField);
    JLabel attribLabelHelpLabel = getLabel(this.ATTRIB_LABEL_HELP);

    labelPanel.add(attribLabelPanel);
    labelPanel.add(attribLabelHelpLabel);

    topMiddlePanel.add(labelPanel);
    topMiddlePanel.add(WidgetFactory.makeDefaultSpacer());


    ////////////////////////////////////////////////////////////////////////////

    JPanel defnPanel = new JPanel();
    defnPanel.setLayout(new GridLayout(1,2));
    JPanel attribDefinitionPanel = WidgetFactory.makePanel(2);

    attribDefinitionLabel = WidgetFactory.makeLabel("Definition:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    attribDefinitionLabel.setVerticalAlignment(SwingConstants.TOP);
    attribDefinitionLabel.setAlignmentY(SwingConstants.TOP);
    attribDefinitionPanel.add(attribDefinitionLabel);

    attribDefinitionField = WidgetFactory.makeTextArea("", 3, true);
    JScrollPane jscrl = new JScrollPane(attribDefinitionField);
    attribDefinitionPanel.add(jscrl);
    JLabel attribDefnHelpLabel = getLabel(this.ATTRIB_DEFN_HELP);

    defnPanel.add(attribDefinitionPanel);
    defnPanel.add(attribDefnHelpLabel);

    topMiddlePanel.add(defnPanel);
    topMiddlePanel.add(WidgetFactory.makeDefaultSpacer());


    ////
    ActionListener listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "got radiobutton command: "+e.getActionCommand());

        //undo any hilites:

        if (e.getActionCommand().equals(buttonsText[0])) {

          setMeasurementScaleUI(nominalPanel);
          setMeasurementScale(measScaleElemNames[0]);

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
                                //"Select and define a Measurement Scale:"
                                "Category:", true,
                                WizardSettings.WIZARD_CONTENT_LABEL_DIMS);

    JButton helpButton = new JButton("Help");
    helpButton.setPreferredSize(new Dimension(35,17));
    helpButton.setMaximumSize(new Dimension(35,15));
    helpButton.setMargin(new Insets(0, 2, 1, 2));
    helpButton.setEnabled(true);
    helpButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    helpButton.setFocusPainted(false);
    helpButton.setToolTipText("More Information about the Catgories");
    helpButton.addActionListener( new ActionListener() {
      private JDialog helpDialog = null;
      public void actionPerformed(ActionEvent ae) {

        if(helpDialog == null) {
          helpDialog = new CategoryHelpDialog();
        }
        if(!helpDialog.isVisible())
          helpDialog.setBounds( (int)AttributePage.this.getX()+100,
                (int)AttributePage.this.getY() + 50,
                HELP_DIALOG_SIZE.width, HELP_DIALOG_SIZE.height);
        helpDialog.setVisible(true);
        helpDialog.toFront();
      }
    });

    JPanel categoryGrid = new JPanel(new GridLayout(1,2, 0, 0));
    JPanel categoryPanel = new JPanel();
    categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.X_AXIS));
    categoryPanel.add(measScaleLabel);
    categoryPanel.add(helpButton);

    categoryGrid.add(categoryPanel);
    categoryGrid.add(new JLabel(""));

    topMiddlePanel.add(categoryGrid);

    radioPanel = WidgetFactory.makeRadioPanel(buttonsText, -1, listener);

    topMiddlePanel.add(radioPanel);

    middlePanel.add(topMiddlePanel);

		currentPanel  = getEmptyPanel();

    middlePanel.add(currentPanel);

    middlePanel.add(Box.createGlue());

		topMiddlePanel.setMaximumSize(topMiddlePanel.getPreferredSize());
    topMiddlePanel.setMinimumSize(topMiddlePanel.getPreferredSize());

    nominalPanel  = getNomOrdPanel(MEASUREMENTSCALE_NOMINAL);
    ordinalPanel  = getNomOrdPanel(MEASUREMENTSCALE_ORDINAL);
    intervalPanel = getIntervalRatioPanel(MEASUREMENTSCALE_INTERVAL);
    ratioPanel    = getIntervalRatioPanel(MEASUREMENTSCALE_RATIO);
    dateTimePanel = getDateTimePanel();


		refreshUI();
  }

  private void setMeasurementScale(String scale) {

    this.measurementScale = scale;
  }




  private void setMeasurementScaleUI(JPanel panel) {

    topMiddlePanel.setMinimumSize(new Dimension(0,0));
		middlePanel.remove(currentPanel);
		//middlePanel.remove(topMiddlePanel);

    currentPanel = panel;
		//middlePanel.add(topMiddlePanel);
    middlePanel.add(currentPanel);
		topMiddlePanel.setMaximumSize(topMiddlePanel.getPreferredSize());
    topMiddlePanel.setMinimumSize(topMiddlePanel.getPreferredSize());

		((WizardPageSubPanelAPI)currentPanel).onLoadAction();

    currentPanel.invalidate();

    currentPanel.repaint();
    topMiddlePanel.validate();
    topMiddlePanel.repaint();
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
    WidgetFactory.addTitledBorder(panel, measScaleDisplayNames[nom_ord]);
    return panel;
  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  private IntervalRatioPanel getIntervalRatioPanel(int intvl_ratio) {

    IntervalRatioPanel panel = new IntervalRatioPanel(this);
    WidgetFactory.addTitledBorder(panel, measScaleDisplayNames[intvl_ratio]);
    return panel;
  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  private DateTimePanel getDateTimePanel() {

    DateTimePanel panel = new DateTimePanel();
    WidgetFactory.addTitledBorder(panel, measScaleDisplayNames[MEASUREMENTSCALE_DATETIME]);
    return panel;
  }

  private JLabel getLabel(String text) {

    if (text==null) text="";
    JLabel label = new JLabel(text);

    label.setAlignmentX(1.0f);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    label.setBorder(BorderFactory.createMatteBorder(1,10,1,3, (Color)null));

    return label;
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

    String attribLabel = attribLabelField.getText().trim();
    if(attribLabel != null && !attribLabel.equals("")) {
      returnMap.put(xPathRoot + "/attributeLabel", attribLabel);
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
	o1 = map.get(AttributeSettings.Nominal_xPath+"/enumeratedDomain/codeDefinition/code");
  if(o1 != null) return "Nominal";
  o1 = map.get(AttributeSettings.Nominal_xPath+"/textDomain/definition");
  if(o1 != null) return "Nominal";

  o1 = map.get(AttributeSettings.Ordinal_xPath+"/enumeratedDomain[1]/codeDefinition[1]/code");
  if(o1 != null) return "Ordinal";
  o1 = map.get(AttributeSettings.Ordinal_xPath+"/textDomain[1]/definition");
  if(o1 != null) return "Ordinal";
	o1 = map.get(AttributeSettings.Ordinal_xPath+"/enumeratedDomain/codeDefinition/code");
  if(o1 != null) return "Ordinal";
  o1 = map.get(AttributeSettings.Ordinal_xPath+"/textDomain/definition");
  if(o1 != null) return "Ordinal";

  o1 = map.get(AttributeSettings.Ratio_xPath+"/unit/standardUnit");
  if(o1 != null) return "Ratio";
  o1 = map.get(AttributeSettings.Ratio_xPath+"/numericDomain/numberType");
  if(o1 != null) return "Ratio";

  o1 = map.get(AttributeSettings.Interval_xPath+"/unit/standardUnit");
  if(o1 != null) return "Interval";
  o1 = map.get(AttributeSettings.Interval_xPath+"/numericDomain/numberType");
  if(o1 != null) return "Interval";

  o1 = map.get(AttributeSettings.DateTime_xPath+"/formatString");
  if(o1 != null) return "Datetime";
  o1 = map.get(AttributeSettings.DateTime_xPath+"/dateTimePrecision");
  if(o1 != null) return "Datetime";

  return "";
  }


  /**
   * sets the Data in the Attribute Dialog fields. This is called from the
   * TextImportWizard when it wants to set some information it has already
   * guessed from the given data file. Any data in the AttributeDialog can be
   * set through this method. The TextImportWizard however sets only the
   * "Attribute Name", "Measurement Scale", "Number Type" and the "Enumeration
   * Code Definitions"
   *
   * @param map - Data is passed as OrderedMap of xPath-value pairs. xPaths in
   *   this map are absolute xPath and not the relative xPaths
   */
  public void setPageData(OrderedMap map) {


		 String name = (String)map.get(xPathRoot + "/attributeName[1]");
		 if(name != null)
			 map = stripIndexOneFromMapKeys(map);

		 String mScale = findMeasurementScale(map);
		 String xPathRoot = AttributeSettings.Attribute_xPath;
     name = (String)map.get(xPathRoot + "/attributeName");
     if(name != null)
       attribNameField.setText(name);

     String label = (String)map.get(xPathRoot + "/attributeLabel");
     if(label != null)
       attribLabelField.setText(label);

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

       Container c = (Container)(radioPanel.getComponent(1));
       JRadioButton jrb = (JRadioButton)c.getComponent(componentNum);
       jrb.setSelected(true);

     }

		 ((NominalOrdinalPanel)nominalPanel).setPanelData(xPathRoot+ "/measurementScale/nominal/nonNumericDomain", map);
     ((NominalOrdinalPanel)ordinalPanel).setPanelData(xPathRoot+ "/measurementScale/ordinal/nonNumericDomain", map);
     ((IntervalRatioPanel)intervalPanel).setPanelData(xPathRoot+ "/measurementScale/interval", map);
     ((IntervalRatioPanel)ratioPanel).setPanelData(xPathRoot+ "/measurementScale/ratio", map);
     ((DateTimePanel)dateTimePanel).setPanelData(xPathRoot+ "/measurementScale/datetime", map);

		 refreshUI();
		 return;
   }

	 private OrderedMap stripIndexOneFromMapKeys(OrderedMap map) {

		 OrderedMap newMap = new OrderedMap();
		 Iterator it = map.keySet().iterator();
		 while(it.hasNext()) {
			 String key = (String) it.next();
			 String val = (String)map.get(key);
			 if(key.indexOf("[1]") < 0) {
				 newMap.put(key, val);
				 continue;
			 }
			 String newKey = key.replaceAll("\\[1\\]","");
			 newMap.put(newKey, val);
		 }
		 return newMap;
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


   class CategoryHelpDialog extends JDialog {

      private final Color TOP_PANEL_BG_COLOR = new Color(11,85,112);

      private final Font  TITLE_FONT	= new Font("Sans-Serif", Font.BOLD,  13);

      private final Color TITLE_TEXT_COLOR	= new Color(255,255,255);

      private final Dimension TOP_PANEL_DIMS = new Dimension(100,40);

      private String helpText = "<html> <body>"
      + "<p>The concept of a measurement scale as defined by Stevens is useful for classifying data despite the weaknesses of the approach that have been pointed out by several practitioners. In particular, the classification allows us to determine some of the mathematical operations that are appropriate for a given set of data, and allows us to determine which types of metadata are needed for a given set of data.  For example, categorical data never have a \"unit\" of measurement. </p>"
      + "<p> Here is a brief overview of the measurement scales we have employed in EML. They are based on Steven's original typology, with the addition of \"Date-Time\"	for purely pragmatic reasons (we need to distinguish date time values in order to collect certain essential metadata about date and time representation).</p>"
      + "<p><b>NOMINAL</b><br></br>	&nbsp;&nbsp;&nbsp;&nbsp;The nominal scale places values into named categories. The different values within a set are unordered.  Some examples of nominal scales include gender (Male/Female) and marital status (single/married/divorced).  Text fields should be classified as nominal.</p>"
      + "<p><b>ORDINAL</b><br></br>&nbsp;&nbsp;&nbsp;&nbsp;The ordinal scale places values in a set order. All ordinal values are also nominal. Ordinal data show a particular value's position relative to other values, such as \"low, medium, high, etc.\" The ordinal scale doesn't indicate the distance between each item.</p>"
      + "<p><b>INTERVAL</b><br></br>&nbsp;&nbsp;&nbsp;&nbsp;The interval scale uses equal-sized units of measurement on a scale between values. It therefore allows the comparison of the differences between two values on the scale. With interval data, the allowable values start from an arbitrary point (not a meaningful zero), and so there is no concept of 'zero' of the measured quantity. Consequently, ratios of interval values are not meaningful. For example, one can not infer that someone with a value of 80 on an ecology test knows twice as much ecology as someone who scores 40 on the test, or that an object at 40 degrees C has twice the kinetic energy as an object at 20 degrees C. All interval values are also ordered and therefore are ordinal scale values as well.</p>"
      + "<p><b>RATIO</b><br></br>&nbsp;&nbsp;&nbsp;&nbsp;The ratio scale is an interval scale with a meaningful zero point. The ratio scale begins at a true zero point that represents an absolute lack of the quality being measured.  Thus, ratios of values are meaningful. For example, an object that is at elevation of 100 meters above sea level is twice as high as an object that is at an elevation of 50 meters above sea level (where sea level is the zero point).  Also, an object at 300 degrees Kelvin has three times the kinetic energy of an object at 100 degrees Kelvin (where absolute zero (no motion) defines the zero point of the Kelvin scale).  Interval values can often be converted to ratio values in order to make ratio comparisons legitimate. For example, an object at 40 degrees C is 313.15 degrees Kelvin, an object at 20 degrees C is 293.15 degrees Kelvin, and so the first object has approximately 1.07 times more kinetic energy (note the wrong answer you would have gotten had you taken the ratio of the values in Celsius).</p>"
      + "<p><b>DATE-TIME</b><br></br>&nbsp;&nbsp;&nbsp;&nbsp;Date and time values in the Gregorian calendar are very strange to use in calculations in that they have properties of both interval and ratio scales.  They also have some properties that do not conform to the interval scale because of the adjustments that are made to time to account for the variations in the period of the Earth around the sun. While the Gregorian calendar has a meaningful zero point, it would be difficult to say that a value taken on midnight January 1, 1000 is twice as old as a value taken on midnight January 1 2000 because the scale has many irregularities in length in practice. However, over short intervals the scale has equidistant points based on the SI second, and so can be considered interval for some purposes, especially with respect to measuring the timing of short-term ecological events.  Date and time values can be represented using several distinct notations, and so we have distinct metadata needs in terms of specifying the format of the value representation.  Because of these pragmatic issues, we separated Date-time into its own measurement scale.  Examples of date-time values are '2003-05-05', '1999/10/10', and '2001-10-10T14:23:20.3'.</p>"
      + "</body> </html>";


      CategoryHelpDialog() {
        super();
        init();
      }

      void init() {

        setTitle("Help");
        setModal(true);

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Help on Choosing a Measurement Scale (Category)");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TITLE_TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(WizardSettings.PADDING,0,WizardSettings.PADDING,0));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setPreferredSize(TOP_PANEL_DIMS);
        topPanel.setBorder(new EmptyBorder(0,2*WizardSettings.PADDING,0,2*WizardSettings.PADDING));
        topPanel.setBackground(TOP_PANEL_BG_COLOR);
        topPanel.setOpaque(true);
        topPanel.add(titleLabel);

        contentPane.add(topPanel, BorderLayout.NORTH);


        JEditorPane editor = new JEditorPane();
        editor.setEditable(false);
        editor.setContentType("text/html");
        editor.setText(helpText);
        editor.setCaretPosition(0);

        contentPane.add(new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);


      }
   }



}
