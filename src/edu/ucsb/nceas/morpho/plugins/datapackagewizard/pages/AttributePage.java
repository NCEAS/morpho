/**
*  '$RCSfile: AttributePage.java,v $'
*    Purpose: A class that handles xml messages passed by the
*             package wizard
*  Copyright: 2000 Regents of the University of California and the
*             National Center for Ecological Analysis and Synthesis
*    Release: @release@
*
*   '$Author: tao $'
*     '$Date: 2009-04-10 21:49:16 $'
* '$Revision: 1.43 $'
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.utilities.OrderedMap;


import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

public class AttributePage extends AbstractUIPage {

  private final String pageID     = DataPackageWizardInterface.ATTRIBUTE_PAGE;
  private final String pageNumber = "";
  private final String title      = "Attribute Page";
  private final String subtitle   = "";

  public static final int BORDERED_PANEL_TOT_ROWS = 7;
  public static final int DOMAIN_NUM_ROWS = 8;

  private final String CONFIG_KEY_CSS_LOCATION = "emlCSSLocation";
  private final String CONFIG_KEY_MCONFJAR_LOC   = "morphoConfigJarLocation";

  // optional xml attributes to the <attribute> tag
  // should be changed to widgets in future revisions
  private String attribIDField = "";
  private String attribScopeField = "";
  private String attribSystemField = "";

  //required name and definition, optional label and storage type
  private JTextField attribNameField;
  private JTextField attribLabelField;
  private JTextArea attribDefinitionField;
  private JTextField attribStorageField;
  private JTextField attribStorageSystemField;

  // ID, scope, and system are unused, so no labels here
  private JLabel attribNameLabel;
  private JLabel attribLabelLabel;
  private JLabel attribDefinitionLabel;
  private JLabel attribStorageLabel;
  private JLabel attribStorageSystemLabel;
  // storage type is not presented, so no label here
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
  // name/value pairs for missing value code/explanation
  private CustomList missingValueCodes;

  private String xPathRoot = AttributeSettings.Attribute_xPath;
  
  private int attributeIndex = 0;
  
  private final String ATTRIBUTE = "attribute";
  
  private final String[] genericPathNameList = {ATTRIBUTE};

 
  final String ATTRIB_NAME_HELP
  = WizardSettings.HTML_NO_TABLE_OPENING
  +/*"Name of the attribute as it appears in the data file"*/ Language.getInstance().getMessage("AttributePage.ATTRIB_NAME_HELP")
  +WizardSettings.HTML_NO_TABLE_CLOSING;

  final String ATTRIB_LABEL_HELP
  = WizardSettings.HTML_NO_TABLE_OPENING
  +/*"A more readable label for the attribute"*/ Language.getInstance().getMessage("AttributePage.ATTRIB_LABEL_HELP")
  +WizardSettings.HTML_NO_TABLE_CLOSING;

  final String ATTRIB_STORAGE_TYPE_HELP
  = WizardSettings.HTML_NO_TABLE_OPENING
  +/*"Storage type for this field"*/ Language.getInstance().getMessage("AttributePage.ATTRIB_STORAGE_TYPE_HELP") + " "
  +WizardSettings.HTML_EXAMPLE_FONT_OPENING
  +/*" e.g"*/ Language.getInstance().getMessage("e.g") 
  +":&nbsp;  " 
  + /*"integer, float"*/ Language.getInstance().getMessage("integer") + ", " + Language.getInstance().getMessage("float")
  +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
  +WizardSettings.HTML_NO_TABLE_CLOSING;

  final String ATTRIB_STORAGE_SYSTEM_HELP
  = WizardSettings.HTML_NO_TABLE_OPENING
  +/*"The system used to define the storage types"*/ Language.getInstance().getMessage("AttributePage.ATTRIB_STORAGE_SYSTEM_HELP") + " "
  +WizardSettings.HTML_EXAMPLE_FONT_OPENING
  +/*" e.g"*/ Language.getInstance().getMessage("e.g") 
  +":&nbsp; C, Java, Oracle"
  +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
  +WizardSettings.HTML_NO_TABLE_CLOSING;

  final String MISSING_VALUE_CODE_HELP
  = WizardSettings.HTML_NO_TABLE_OPENING
  + Language.getInstance().getMessage("AttributePage.MISSING_VALUE_CODE_HELP")
  + WizardSettings.HTML_NO_TABLE_CLOSING;
  
  final String MISSING_VALUE_EXPLN_HELP
  = WizardSettings.HTML_NO_TABLE_OPENING
  + Language.getInstance().getMessage("AttributePage.MISSING_VALUE_EXPLN_HELP")
  + WizardSettings.HTML_NO_TABLE_CLOSING;
  
  final String ATTRIB_DEFN_HELP
  = WizardSettings.HTML_NO_TABLE_OPENING
  /*
  +"Define the contents of the attribute (or column) precisely, "
  +"so that a data user could interpret the attribute accurately." 
  */
  + Language.getInstance().getMessage("AttributePage.ATTRIB_DEFN_HELP_1")
  +"<br></br>"
  +WizardSettings.HTML_EXAMPLE_FONT_OPENING
  +/*"e.g"*/ Language.getInstance().getMessage("e.g") + ":&nbsp;&nbsp;&nbsp;"
  /*
  +"\"spden\" is the number of individuals of all macro "
  +"invertebrate species found in the plot"
  */
  + Language.getInstance().getMessage("AttributePage.ATTRIB_DEFN_HELP_2")
  +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
  +WizardSettings.HTML_NO_TABLE_OPENING;

  private final String[] buttonsText
  = {
    WizardSettings.HTML_NO_TABLE_OPENING
    +/*"Unordered"*/ Language.getInstance().getMessage("Unordered") + " :&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    +/*" unordered categories or text   "*/ Language.getInstance().getMessage("Unordered.desc")
    +" (" + /*"statistically"*/ Language.getInstance().getMessage("statistically") + "&nbsp;<b>" + /*"nominal"*/ Language.getInstance().getMessage("nominal") + "</b>) "
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    + /*"e.g"*/ Language.getInstance().getMessage("e.g") +": " + /*"Male"*/ Language.getInstance().getMessage("Male") +", " + /*"Female"*/ Language.getInstance().getMessage("Female")
    +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
    +WizardSettings.HTML_NO_TABLE_CLOSING,

    WizardSettings.HTML_NO_TABLE_OPENING
    +/*"Ordered"*/ Language.getInstance().getMessage("Ordered") + " :&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    +/*" ordered categories  "*/ Language.getInstance().getMessage("Ordered.desc")
    +" (" + /*"statistically"*/ Language.getInstance().getMessage("statistically") + "&nbsp;<b>" + /*"ordinal"*/ Language.getInstance().getMessage("ordinal") + "</b>) "
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    + /*"e.g"*/ Language.getInstance().getMessage("e.g") +" : " + /*"Low"*/ Language.getInstance().getMessage("Low") +", " + /*"High"*/ Language.getInstance().getMessage("High") 
    +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
    +WizardSettings.HTML_NO_TABLE_CLOSING,

    WizardSettings.HTML_NO_TABLE_OPENING
    +/*"Relative*/ Language.getInstance().getMessage("Relative") + " :&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    +/*" values from a scale with equidistant points "*/ Language.getInstance().getMessage("Relative.desc") + " "
    +" (" + /*"statistically"*/ Language.getInstance().getMessage("statistically") + "&nbsp;<b>" + /*"interval"*/ Language.getInstance().getMessage("interval") + "</b>) "
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    + /*"e.g"*/ Language.getInstance().getMessage("e.g") +" : 12.2 " + /*"degrees Celsius"*/ Language.getInstance().getMessage("DegreesCelsius") 
    +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
    +WizardSettings.HTML_NO_TABLE_CLOSING,

    WizardSettings.HTML_NO_TABLE_OPENING
    +/*"Absolute"*/ Language.getInstance().getMessage("Absolute") + " :&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    +/*"measurement scale with a meaningful zero point "*/ Language.getInstance().getMessage("Absolute.desc") + " "
    +" (" + /*"statistically"*/ Language.getInstance().getMessage("statistically") + "&nbsp;<b>" + /*"ratio"*/ Language.getInstance().getMessage("ratio") + "</b>) "
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    + /*"e.g"*/ Language.getInstance().getMessage("e.g") +" : 273 " + /*"degrees Celsius"*/ Language.getInstance().getMessage("Kelvin")
    +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
    +WizardSettings.HTML_NO_TABLE_CLOSING,

    WizardSettings.HTML_NO_TABLE_OPENING
    +/*"Date-Time*/ Language.getInstance().getMessage("Date-Time") + " :&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    +/*"date or time values from the Gregorian calendar "*/ Language.getInstance().getMessage("Date-Time.desc") + " "
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    + /*"e.g"*/ Language.getInstance().getMessage("e.g") +" : 2002-10-24" 
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

  public AttributePage() {
	nextPageID = "";
    initNames();
    init();
  }

  private void initNames() {

    measScaleElemNames[MEASUREMENTSCALE_NOMINAL]  = "nominal";
    measScaleElemNames[MEASUREMENTSCALE_ORDINAL]  = "ordinal";
    measScaleElemNames[MEASUREMENTSCALE_INTERVAL] = "interval";
    measScaleElemNames[MEASUREMENTSCALE_RATIO]    = "ratio";
    measScaleElemNames[MEASUREMENTSCALE_DATETIME] = "dateTime";

    measScaleDisplayNames[MEASUREMENTSCALE_NOMINAL]  = /*"Unordered"*/ Language.getInstance().getMessage("Unordered");
    measScaleDisplayNames[MEASUREMENTSCALE_ORDINAL]  = /*"Ordered"*/ Language.getInstance().getMessage("Ordered");
    measScaleDisplayNames[MEASUREMENTSCALE_INTERVAL] = /*"Relative"*/ Language.getInstance().getMessage("Relative");
    measScaleDisplayNames[MEASUREMENTSCALE_RATIO]    = /*"Absolute"*/ Language.getInstance().getMessage("Absolute");
    measScaleDisplayNames[MEASUREMENTSCALE_DATETIME] = /*"Datetime"*/ Language.getInstance().getMessage("Date-Time");
  }


  public boolean isImportNeeded() {
    if(measurementScale.equalsIgnoreCase("nominal")) {
      return ((NominalOrdinalPanel)nominalPanel).isImportNeeded();
    }

    if(measurementScale.equalsIgnoreCase("ordinal")) {
      return ((NominalOrdinalPanel)ordinalPanel).isImportNeeded();
    }

    return false;
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
    "<font size=\"4\"><b>" 
    +/*"DefineAttribute/Column"*/ Language.getInstance().getMessage("DefineAttribute/Column")
    +":</b></font>", 1));

    topMiddlePanel.add(WidgetFactory.makeDefaultSpacer());


    /////////////////////////////////////////////

    JPanel namePanel = new JPanel();
    namePanel.setLayout(new GridLayout(1,2));

    JPanel attribNamePanel = WidgetFactory.makePanel(1);
    attribNameLabel = WidgetFactory.makeLabel(/*"Name:"*/ Language.getInstance().getMessage("Name") + ":", 
    										  true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    attribNamePanel.add(attribNameLabel);
    attribNameField = WidgetFactory.makeOneLineTextField();
    attribNamePanel.add(attribNameField);
    JLabel attribNameHelpLabel = WidgetFactory.makeHelpLabel(this.ATTRIB_NAME_HELP);
    namePanel.add(attribNamePanel);
    namePanel.add(attribNameHelpLabel);

    topMiddlePanel.add(namePanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());
    
    /////////////////////////////////////////////

    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new GridLayout(1,2));

    JPanel attribLabelPanel = WidgetFactory.makePanel(1);
    attribLabelLabel = WidgetFactory.makeLabel(/*"Label:"*/ Language.getInstance().getMessage("Label") + ":",
    											false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    attribLabelPanel.add(attribLabelLabel);
    attribLabelField = WidgetFactory.makeOneLineTextField();
    attribLabelPanel.add(attribLabelField);
    JLabel attribLabelHelpLabel = WidgetFactory.makeHelpLabel(this.ATTRIB_LABEL_HELP);

    labelPanel.add(attribLabelPanel);
    labelPanel.add(attribLabelHelpLabel);

    topMiddlePanel.add(labelPanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());


    ////////////////////////////////////////////////////////////////////////////

    JPanel defnPanel = new JPanel();
    defnPanel.setLayout(new GridLayout(1,2));
    JPanel attribDefinitionPanel = WidgetFactory.makePanel(2);

    attribDefinitionLabel = WidgetFactory.makeLabel(/*"Definition:"*/ Language.getInstance().getMessage("Definition") + ":",
    												true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    attribDefinitionLabel.setVerticalAlignment(SwingConstants.TOP);
    attribDefinitionLabel.setAlignmentY(SwingConstants.TOP);
    attribDefinitionPanel.add(attribDefinitionLabel);

    attribDefinitionField = WidgetFactory.makeTextArea("", 3, true);
    JScrollPane jscrl = new JScrollPane(attribDefinitionField);
    attribDefinitionPanel.add(jscrl);
    JLabel attribDefnHelpLabel = WidgetFactory.makeHelpLabel(this.ATTRIB_DEFN_HELP);

    defnPanel.add(attribDefinitionPanel);
    defnPanel.add(attribDefnHelpLabel);

    topMiddlePanel.add(defnPanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());


    ////////////////////////////////////////////

    JPanel storagePanel = new JPanel();
    storagePanel.setLayout(new GridLayout(1,2));
    JPanel attribStoragePanel = WidgetFactory.makePanel(1);

    attribStorageLabel = WidgetFactory.makeLabel(/*"Storage:"*/ Language.getInstance().getMessage("Storage") + ":",
    											false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    attribStoragePanel.add(attribStorageLabel);

    attribStorageField = WidgetFactory.makeOneLineTextField();
    attribStoragePanel.add(attribStorageField);
    JLabel attribStorageHelpLabel = WidgetFactory.makeHelpLabel(this.ATTRIB_STORAGE_TYPE_HELP);

    storagePanel.add(attribStoragePanel);
    storagePanel.add(attribStorageHelpLabel);

    topMiddlePanel.add(storagePanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());

    ////////////////////////////////////////////

    JPanel storageSystemPanel = new JPanel();
    storageSystemPanel.setLayout(new GridLayout(1,2));
    JPanel attribStorageSystemPanel = WidgetFactory.makePanel(1);

    attribStorageSystemLabel = WidgetFactory.makeLabel(/*"Storage System:"*/ Language.getInstance().getMessage("StorageSystem") + ":",
    													false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    attribStorageSystemPanel.add(attribStorageSystemLabel);

    attribStorageSystemField = WidgetFactory.makeOneLineTextField();
    attribStorageSystemPanel.add(attribStorageSystemField);
    JLabel attribStorageSystemHelpLabel = WidgetFactory.makeHelpLabel(this.ATTRIB_STORAGE_SYSTEM_HELP);

    storageSystemPanel.add(attribStorageSystemPanel);
    storageSystemPanel.add(attribStorageSystemHelpLabel);

    topMiddlePanel.add(storageSystemPanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());

    ////////////////////////////////////////////
        
    // multiple missing value codes
    JPanel missingValueCodesPanel = WidgetFactory.makePanel(4);
    missingValueCodesPanel.add(WidgetFactory.makeLabel(Language.getInstance().getMessage("AttributePage.MissingValues") + ":", false));
    String[] colNames = 
    	new String[] {
    		Language.getInstance().getMessage("AttributePage.MissingValueCode"),
    		Language.getInstance().getMessage("AttributePage.MissingValueExpln")
    };
    Object[] columnEditors = 
    	new Object[] {
    		new JTextField(),
    		new JTextField()
    	};
    missingValueCodes = WidgetFactory.makeList(colNames, columnEditors, 2, true, false, false, true, false, false);
    missingValueCodesPanel.add(missingValueCodes);
    topMiddlePanel.add(missingValueCodesPanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());

    ////////
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
    /*"Category:"*/ Language.getInstance().getMessage("Category")+ ":",
    true,
    WizardSettings.WIZARD_CONTENT_LABEL_DIMS);

    measScaleLabel.setAlignmentY(measScaleLabel.CENTER_ALIGNMENT);

    JButton helpButton = new JButton(/*"Help"*/ Language.getInstance().getMessage("Help"));
    helpButton.setMinimumSize(new Dimension(35,15));
    helpButton.setMaximumSize(new Dimension(35,15));
    helpButton.setMargin(new Insets(0, 2, 1, 2));
    helpButton.setEnabled(true);
    helpButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    helpButton.setFocusPainted(false);
    helpButton.setToolTipText("More Information about the Categories");
    Point loc1 = getLocation();

    final AttributePage pageRef = this;
    helpButton.addActionListener( new ActionListener() {
      private JDialog helpDialog = null;
      public void actionPerformed(ActionEvent ae) {

        if(helpDialog == null) {
        	String title = "Help on Choosing a Measurement Scale (Category)";
        	
        	 String helpText = "<html> <body>"
    			+ "<p>The concept of a measurement scale as defined by Stevens is useful for classifying data despite the weaknesses of the approach that have been pointed out by several practitioners. In particular, the classification allows us to determine some of the mathematical operations that are appropriate for a given set of data, and allows us to determine which types of metadata are needed for a given set of data.  For example, categorical data never have a \"unit\" of measurement. </p>"
    			+ "<p> Here is a brief overview of the measurement scales we have employed in EML. They are based on Steven's original typology, with the addition of \"Date-Time\"  for purely pragmatic reasons (we need to distinguish date time values in order to collect certain essential metadata about date and time representation).</p>"
    			+ "<p><b>NOMINAL</b><br></br>  &nbsp;&nbsp;&nbsp;&nbsp;The nominal scale places values into named categories. The different values within a set are unordered.  Some examples of nominal scales include gender (Male/Female) and marital status (single/married/divorced).  Text fields should be classified as nominal.</p>"
    			+ "<p><b>ORDINAL</b><br></br>&nbsp;&nbsp;&nbsp;&nbsp;The ordinal scale places values in a set order. All ordinal values are also nominal. Ordinal data show a particular value's position relative to other values, such as \"low, medium, high, etc.\" The ordinal scale doesn't indicate the distance between each item.</p>"
    			+ "<p><b>INTERVAL</b><br></br>&nbsp;&nbsp;&nbsp;&nbsp;The interval scale uses equal-sized units of measurement on a scale between values. It therefore allows the comparison of the differences between two values on the scale. With interval data, the allowable values start from an arbitrary point (not a meaningful zero), and so there is no concept of 'zero' of the measured quantity. Consequently, ratios of interval values are not meaningful. For example, one can not infer that someone with a value of 80 on an ecology test knows twice as much ecology as someone who scores 40 on the test, or that an object at 40 degrees C has twice the kinetic energy as an object at 20 degrees C. All interval values are also ordered and therefore are ordinal scale values as well.</p>"
    			+ "<p><b>RATIO</b><br></br>&nbsp;&nbsp;&nbsp;&nbsp;The ratio scale is an interval scale with a meaningful zero point. The ratio scale begins at a true zero point that represents an absolute lack of the quality being measured.  Thus, ratios of values are meaningful. For example, an object that is at elevation of 100 meters above sea level is twice as high as an object that is at an elevation of 50 meters above sea level (where sea level is the zero point).  Also, an object at 300 degrees Kelvin has three times the kinetic energy of an object at 100 degrees Kelvin (where absolute zero (no motion) defines the zero point of the Kelvin scale).  Interval values can often be converted to ratio values in order to make ratio comparisons legitimate. For example, an object at 40 degrees C is 313.15 degrees Kelvin, an object at 20 degrees C is 293.15 degrees Kelvin, and so the first object has approximately 1.07 times more kinetic energy (note the wrong answer you would have gotten had you taken the ratio of the values in Celsius).</p>"
    			+ "<p><b>DATE-TIME</b><br></br>&nbsp;&nbsp;&nbsp;&nbsp;Date and time values in the Gregorian calendar are very strange to use in calculations in that they have properties of both interval and ratio scales.  They also have some properties that do not conform to the interval scale because of the adjustments that are made to time to account for the variations in the period of the Earth around the sun. While the Gregorian calendar has a meaningful zero point, it would be difficult to say that a value taken on midnight January 1, 1000 is twice as old as a value taken on midnight January 1 2000 because the scale has many irregularities in length in practice. However, over short intervals the scale has equidistant points based on the SI second, and so can be considered interval for some purposes, especially with respect to measuring the timing of short-term ecological events.  Date and time values can be represented using several distinct notations, and so we have distinct metadata needs in terms of specifying the format of the value representation.  Because of these pragmatic issues, we separated Date-time into its own measurement scale.  Examples of date-time values are '2003-05-05', '1999/10/10', and '2001-10-10T14:23:20.3'.</p>"
    			+ "</body> </html>";
        	 Window owner = SwingUtilities.getWindowAncestor(pageRef);
			if (owner instanceof Frame) {
				helpDialog = new HelpDialog((Frame)owner, title, helpText);
			}
			if (owner instanceof Dialog) {
				helpDialog = new HelpDialog((Dialog)owner, title, helpText);
			}
        }
        Point loc = getLocationOnScreen();
        int wd = getWidth();
        int ht = getHeight();
        int dwd = HelpDialog.HELP_DIALOG_SIZE.width;
        int dht = HelpDialog.HELP_DIALOG_SIZE.height;
        helpDialog.setLocation( (int)loc.getX() + wd/2 - dwd/2, (int)loc.getY() + ht/2 - dht/2);
        helpDialog.setSize(HelpDialog.HELP_DIALOG_SIZE);
        helpDialog.setVisible(true);
        helpDialog.toFront();

      }
    });

    JPanel categoryPanel = new JPanel();
    categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));

    JPanel helpButtonPanel = new JPanel();
    helpButtonPanel.setLayout(new BoxLayout(helpButtonPanel, BoxLayout.X_AXIS));
    helpButtonPanel.add(helpButton);
    helpButtonPanel.add(Box.createHorizontalGlue());
    helpButtonPanel.setBorder(BorderFactory.createEmptyBorder(0,3,0,0));

    categoryPanel.add(measScaleLabel);
    categoryPanel.add(WidgetFactory.makeHalfSpacer());
    categoryPanel.add(helpButtonPanel);

    categoryPanel.setMinimumSize(new Dimension(90, 45));
    categoryPanel.setMaximumSize(new Dimension(90, 45));

    JPanel outerCategoryPanel = new JPanel(new BorderLayout());
    outerCategoryPanel.add(categoryPanel, BorderLayout.CENTER);
    outerCategoryPanel.add(Box.createGlue(), BorderLayout.SOUTH);
    outerCategoryPanel.add(Box.createGlue(), BorderLayout.NORTH);

    radioPanel = WidgetFactory.makeRadioPanel(buttonsText, -1, listener);
    JPanel outerRadioPanel = new JPanel();
    outerRadioPanel.setLayout(new BoxLayout(outerRadioPanel, BoxLayout.X_AXIS));
    outerRadioPanel.add(categoryPanel);
    outerRadioPanel.add(radioPanel);

    topMiddlePanel.add(outerRadioPanel);

    /////////////////////////////////////////////////////

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
   * Get the index of this attribute
   * @return
   */
  public int getAttributeIndex()
  {
	  return attributeIndex;
  }
  
  /**
   * Set the index (under the same entity) to this attribute
   * @param attributeIndex
   */
  public void setAttributeIndex(int attributeIndex)
  {
	  this.attributeIndex = attributeIndex;
  }
  
  /**
   * Gets a list of generic name of path of this page
   * The order of the list should be as same as the order of subtrees in the page
   */
  public String[] getGenericPathName()
  {
	  return genericPathNameList;
  }
  
  public String getPageDataXPathForCorrection()
  {
      return "/"+ATTRIBUTE;
  }
  
  /**
  *  The action to be executed when the "OK" button is pressed. If no onAdvance
  *  processing is required, implementation must return boolean true.
  *
  *  @return boolean true if dialog should close and return to wizard, false
  *          if not (e.g. if a required field hasn't been filled in)
  */
  public boolean onAdvanceAction() {

    //if (attribNameField.getText().trim().equals("")) {
    if (Util.isBlank(attribNameField.getText())) {

      WidgetFactory.hiliteComponent(attribNameLabel);
      attribNameField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(attribNameLabel);

    //if (attribDefinitionField.getText().trim().equals("")) {
    if (Util.isBlank(attribDefinitionField.getText())) {

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

    boolean valid = ((WizardPageSubPanelAPI)currentPanel).validateUserInput();
    if(!valid) return false;
    return true;

    

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
   * Gets attribute name from this page
   * @return
   */
  public String getAttributeName()
  {
    String attribName   = attribNameField.getText().trim();
    if (attribName==null) attribName = "";
    return attribName;
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
  public OrderedMap getPageData(String xPath) {

    returnMap.clear();

    // handle <attribute id="" scope="" system="" >
    String attribID = attribIDField.trim();
    if (attribID!=null && !attribID.equals("")) {
      returnMap.put(xPath + "/@id", attribID);
    }

    String attribScope = attribScopeField.trim();
    if (attribScope!=null && !attribScope.equals("")) {
      returnMap.put(xPath + "/@scope", attribScope);
    }

    String attribSystem = attribSystemField.trim();
    if (attribSystem!=null && !attribSystem.equals("")) {
      returnMap.put(xPath + "/@system", attribSystem);
    }


    // then handle the elements
    String attribName = attribNameField.getText().trim();
    if (attribName!=null && !attribName.equals("")) {
      returnMap.put(xPath + "/attributeName", attribName);
    }

    String attribLabel = attribLabelField.getText().trim();
    //if(attribLabel != null && !attribLabel.equals("")) {
    if(!Util.isBlank(attribLabel)) {
      returnMap.put(xPath + "/attributeLabel", attribLabel);
    }

    String attribDef = attribDefinitionField.getText().trim();
    if (attribDef!=null && !attribDef.equals("")) {
      returnMap.put(xPath + "/attributeDefinition", attribDef);
    }

    String storageType = attribStorageField.getText().trim();
    if(storageType !=null && !storageType.equals("")) {
      returnMap.put(xPath + "/storageType", storageType);
    }

    String storageTypeTypeSystem = attribStorageSystemField.getText().trim();
    if (storageTypeTypeSystem!=null && !storageTypeTypeSystem.equals("")) {
      returnMap.put(xPath + "/storageType/@typeSystem", storageTypeTypeSystem);
    }

    if (measurementScale!=null && !measurementScale.equals("")) {

      returnMap.putAll(
      ((WizardPageSubPanelAPI)currentPanel).getPanelData(
      xPath+"/measurementScale/"+measurementScale) );
    }
    /*if( measurementScale != null && (measurementScale.equalsIgnoreCase("Interval") || measurementScale.equalsIgnoreCase("Ratio")) ) {

      // look for several additionalMetadata subtrees in the map.
      for(int i = 1; ; i++) {

        String prefix = xPath+"/measurementScale/"+measurementScale + "/additionalMetadata[" + i + "]";
        boolean p = returnMap.containsKey(prefix + "/unitList/unit[1]/@name");
        if(p) {
          OrderedMap map = this.extractKeysContaining(returnMap, "/additionalMetadata["+i+"]");
          insertIntoDOMTree(map);
        } else {
          break;
        }
      }

    }*/

    // missing value codes
    List<List<String>> missingCodes = missingValueCodes.getListOfRowLists();
    int index = 1;
    String codeXpath = xPath + "/missingValueCode/code";
	String explnXpath = xPath + "/missingValueCode/codeExplanation";
    for (List<String> row: missingCodes) {
    	// use the correct xPath
    	if (missingCodes.size() > 1) {
    		codeXpath = xPath + "/missingValueCode[" + index + "]/code";
    		explnXpath = xPath + "/missingValueCode[" + index + "]/codeExplanation";
    	}
    	// put the values in the map
    	String missingValueCode = row.get(0);
    	if (!Util.isBlank(missingValueCode)) {
		  returnMap.put(codeXpath, missingValueCode);
		}
    	String missingValueExpln = row.get(1);
    	if(!Util.isBlank(missingValueExpln)) {
	      returnMap.put(explnXpath, missingValueExpln);
	    }
    	index++;
    }

    return returnMap;
  }

  

  

  private String findMeasurementScale(OrderedMap map) {

    ///// check for Nominal

    Object o1 = map.get( xPathRoot+AttributeSettings.Nominal_xPath_rel+"/enumeratedDomain[1]/codeDefinition[1]/code");
    if(o1 != null) return "Nominal";
    boolean b1 = map.containsKey( xPathRoot+AttributeSettings.Nominal_xPath_rel+"/enumeratedDomain[1]/entityCodeList/entityReference");
    if(b1) return "Nominal";
    o1 = map.get(xPathRoot+AttributeSettings.Nominal_xPath_rel+"/textDomain[1]/definition");
    if(o1 != null) return "Nominal";
    o1 = map.get( xPathRoot+AttributeSettings.Nominal_xPath_rel+"/enumeratedDomain/codeDefinition/code");
    if(o1 != null) return "Nominal";
    b1 = map.containsKey( xPathRoot+AttributeSettings.Nominal_xPath_rel+"/enumeratedDomain/entityCodeList/entityReference");

    if(b1) return "Nominal";
    o1 = map.get(xPathRoot+AttributeSettings.Nominal_xPath_rel+"/textDomain/definition");
    if(o1 != null) return "Nominal";

    ///// check for Ordinal

    o1 = map.get( xPathRoot+AttributeSettings.Ordinal_xPath_rel+"/enumeratedDomain[1]/codeDefinition[1]/code");
    if(o1 != null) return "Ordinal";
    b1 = map.containsKey( xPathRoot+AttributeSettings.Ordinal_xPath_rel+"/enumeratedDomain[1]/entityCodeList/entityReference");
    if(b1) return "Ordinal";
    o1 = map.get(xPathRoot+AttributeSettings.Ordinal_xPath_rel+"/textDomain[1]/definition");
    if(o1 != null) return "Ordinal";
    o1 = map.get( xPathRoot+AttributeSettings.Ordinal_xPath_rel+"/enumeratedDomain/codeDefinition/code");
    if(o1 != null) return "Ordinal";
    b1 = map.containsKey( xPathRoot+AttributeSettings.Ordinal_xPath_rel+"/enumeratedDomain/entityCodeList/entityReference");
    if(b1) return "Ordinal";
    o1 = map.get(xPathRoot+AttributeSettings.Ordinal_xPath_rel+"/textDomain/definition");
    if(o1 != null) return "Ordinal";

    ///// check for Ratio

    o1 = map.get(xPathRoot+AttributeSettings.Ratio_xPath_rel+"/unit/standardUnit");
    if(o1 != null) return "Ratio";
    o1 = map.get(xPathRoot+AttributeSettings.Ratio_xPath_rel+"/unit/customUnit");
    if(o1 != null) return "Ratio";
    o1 = map.get(xPathRoot+AttributeSettings.Ratio_xPath_rel+"/numericDomain/numberType");
    if(o1 != null) return "Ratio";

    ///// check for Interval

    o1 = map.get(xPathRoot+AttributeSettings.Interval_xPath_rel+"/unit/standardUnit");
    if(o1 != null) return "Interval";
    o1 = map.get(xPathRoot+AttributeSettings.Interval_xPath_rel+"/unit/customUnit");
    if(o1 != null) return "Interval";
    o1 = map.get(xPathRoot+AttributeSettings.Interval_xPath_rel+"/numericDomain/numberType");
    if(o1 != null) return "Interval";

    ///// check for DateTime

    o1 = map.get(xPathRoot+AttributeSettings.DateTime_xPath_rel+"/formatString");
    if(o1 != null) return "Datetime";
    o1 = map.get(xPathRoot+AttributeSettings.DateTime_xPath_rel+"/dateTimePrecision");
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
  public boolean setPageData(OrderedMap map, String _xPathRoot) {

    if (_xPathRoot!=null) this.xPathRoot = _xPathRoot;
     
    Log.debug(32,"AttributePage.setPageData() called with rootXPath = " + xPathRoot
            + "\n Map = \n" +map);
    // future enhancements to this AttributePage dialog should map
    // the following 3 xml attributes to appropriate widgets
    String id = (String)map.get(xPathRoot + "/@id");
    if ( id != null ) {
      attribIDField = id.toString();
      map.remove(xPathRoot + "/@id");
    }
    String scope = (String)map.get(xPathRoot + "/@scope");
    if ( scope != null ) {
      attribScopeField = scope.toString();
      map.remove(xPathRoot + "/@scope");
    }
    String system = (String)map.get(xPathRoot + "/@system");
    if ( system != null ) {
      attribSystemField = system.toString();
      map.remove(xPathRoot + "/@system");
    }

    String name = (String)map.get(xPathRoot + "/attributeName[1]");
    //if(name != null)
      map = stripIndexOneFromMapKeys(map);
    String mScale = findMeasurementScale(map);

    //String xPathRoot = AttributeSettings.Attribute_xPath;
    name = (String)map.get(xPathRoot + "/attributeName");
    if(name != null) {
      attribNameField.setText(name);
      map.remove(xPathRoot + "/attributeName");
    }
    String label = (String)map.get(xPathRoot + "/attributeLabel");
    if(label != null){
      attribLabelField.setText(label);
      map.remove(xPathRoot + "/attributeLabel");
    }

    String defn = (String)map.get(xPathRoot + "/attributeDefinition");
    if(defn != null){
      attribDefinitionField.setText(defn);
      map.remove(xPathRoot + "/attributeDefinition");
    }

    // in future versions of this Attribute Page dialog, storageType
    // should be handled with an appropriate widget.
    String storageType = (String)map.get(xPathRoot + "/storageType");
    if(storageType != null) {
      attribStorageField.setText(storageType);
      map.remove(xPathRoot + "/storageType");
    }

    // in future versions of this Attribute Page dialog, the
    // typeSystem attribute to the storageType element
    // should be handled with an appropriate widget.
    String storageTypeTypeSystem = (String)map.get(xPathRoot + "/storageType/@typeSystem");
    if(storageTypeTypeSystem != null) {
      attribStorageSystemField.setText(storageTypeTypeSystem.toString());
      map.remove(xPathRoot + "/storageType/@typeSystem");
    }

    if(mScale == null || mScale.equals("")) return false;

    measurementScale = mScale;

    //depending on the type of measurement scale, populate the
    //appropriate panel with the data, ensuring that each panel can
    //handle each map member passed to it
    int componentNum = -1;
    if(measurementScale.equalsIgnoreCase("nominal")) {
      setMeasurementScaleUI(nominalPanel);
      setMeasurementScale(measScaleElemNames[0]);
      componentNum = 0;
      ((NominalOrdinalPanel)nominalPanel).setPanelData(
      xPathRoot + "/measurementScale/nominal/nonNumericDomain", map);
    }
    else if(measurementScale.equalsIgnoreCase("ordinal")) {
      setMeasurementScaleUI(ordinalPanel);
      setMeasurementScale(measScaleElemNames[1]);
      componentNum = 1;
      ((NominalOrdinalPanel)ordinalPanel).setPanelData(
      xPathRoot + "/measurementScale/ordinal/nonNumericDomain", map);
    }
    if(measurementScale.equalsIgnoreCase("interval")) {
      setMeasurementScaleUI(intervalPanel);
      setMeasurementScale(measScaleElemNames[2]);
      componentNum = 2;
      ((IntervalRatioPanel)intervalPanel).setPanelData(
      xPathRoot + "/measurementScale/interval", map);
    }
    if(measurementScale.equalsIgnoreCase("ratio")) {
      setMeasurementScaleUI(ratioPanel);
      setMeasurementScale(measScaleElemNames[3]);
      componentNum = 3;
      ((IntervalRatioPanel)ratioPanel).setPanelData(
      xPathRoot + "/measurementScale/ratio", map);
    }
    if(measurementScale.equalsIgnoreCase("dateTime")) {
      setMeasurementScaleUI(dateTimePanel);
      setMeasurementScale(measScaleElemNames[4]);
      componentNum = 4;;
      ((DateTimePanel)dateTimePanel).setPanelData(
      xPathRoot + "/measurementScale/dateTime", map);
    }

    //selects the appropriate radio button
    if (componentNum != -1) {
      Container c = (Container)(radioPanel.getComponent(1));
      JRadioButton jrb = (JRadioButton)c.getComponent(componentNum);
      jrb.setSelected(true);
    }

    // handle missing value code definitions
    int index = 1;
    while (true) {
    	// use the correct xpath for the content
    	String codeXpath = xPathRoot + "/missingValueCode/code";
    	if (!map.containsKey(codeXpath)) {
    		codeXpath = xPathRoot + "/missingValueCode[" + index + "]/code";
    	}
    	String explnXpath = xPathRoot + "/missingValueCode/codeExplanation";
    	if (!map.containsKey(explnXpath)) {
    		explnXpath = xPathRoot + "/missingValueCode[" + index + "]/codeExplanation";
    	}
    	
	    // look up the code and explanation
	    String missingValueCode = (String) map.get(codeXpath);
	    if (missingValueCode != null) {
	      map.remove(codeXpath);
	    }
	    String missingValueExpln = (String) map.get(explnXpath);
	    if (missingValueExpln != null) {
	      map.remove(explnXpath);
	    }
	    
	    // check if we are done
	    if (missingValueCode == null && missingValueExpln == null) {
	    	break;
	    }
	   
	    // set the values
	    List<String> rowList = new ArrayList<String>();
	    rowList.add(missingValueCode);
	    rowList.add(missingValueExpln);
	    missingValueCodes.addRow(rowList);
	    
	    // continue
	    index++;

    }

    // handle attribute level coverage elements

    // handle attribute level method elements

    refreshUI();

    //if anything left in map, then it included stuff we can't handle...
    boolean returnVal = map.isEmpty();

    if (!returnVal) {

      Log.debug(20,
      "AttributePage.setPageData returning FALSE! Map still contains:"
      + map);
    }
    return returnVal;
  }

  private OrderedMap stripIndexOneFromMapKeys(OrderedMap map) {

    OrderedMap newMap = new OrderedMap();
    Iterator it = map.keySet().iterator();
    while(it.hasNext()) {
      String key = (String) it.next();
      String val = (String)map.get(key);
      int pos;
      if((pos = key.indexOf("[1]")) < 0) {
        newMap.put(key, val);
        continue;
      }
      String newKey = "";
      for(;pos != -1; pos = key.indexOf("[1]")){
        newKey += key.substring(0,pos);
        key = key.substring(pos + 3);
      }
      newKey += key;
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
      if(unit != null) {
        text += "<tr>";
        text += "<td class = \"highlight\"  width = \"35%\" > Standard Unit: </td>";
        text += "<td class = \"secondCol\" width=\"65%\" colspan=\"4\"> " + unit + "</td>";
        text += "</tr>";

      } else {
        unit = (String) map.get(mainXPath + "/unit/customUnit");
        if(unit != null) {
          text += "<tr>";
          text += "<td class = \"highlight\"  width = \"35%\" > Custom Unit: </td>";
          text += "<td class = \"secondCol\" width=\"65%\" colspan=\"4\"> " + unit + "</td>";
          text += "</tr>";
        }
      }

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
      pathBuff.append(config.get(CONFIG_KEY_CSS_LOCATION, 0));
      FULL_STYLE_PATH = pathBuff.toString();
      pathBuff = null;
    }
    return FULL_STYLE_PATH;
  }

}
