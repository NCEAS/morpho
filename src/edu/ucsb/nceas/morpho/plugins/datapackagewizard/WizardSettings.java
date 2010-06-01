/**
 *  '$RCSfile: WizardSettings.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-20 18:26:05 $'
 * '$Revision: 1.77 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.IOUtil;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.StringUtil;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.io.IOException;
import java.io.Reader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.ucsb.nceas.morpho.util.UISettings;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

/**
 *  WizardSettings
 *
 */

public class WizardSettings {

  private static final String ALL_UNIT_TYPES_XPATH
                                      = "/stmml:unitList/stmml:unitType/@name";

  public static final String BASIC_UNIT_TYPES_XPATH
            = "/stmml:unitList/stmml:unitType/@name[string(.)=string(../dimension/@name)]";

  private static String[] basicUnitTypes = {"length", "time", "mass", "charge", "temperature", "amount", "luminosity", "dimensionless", "angle"};
  private static Node udRootNode = null;
  private static String[] unitDictionaryUnitTypesArray = null;
  private static String[] customUnitDictionaryUnitTypesArray = null;
  private static String[] unitDictionaryBasicUnitTypesArray = null;
  private static String[] existingSIUnits = null;

  private static Node[] unitsNodeArray = null;
  private static final String UNITS_XPATH    = "/stmml:unitList/stmml:unit";
  public  static final short ENTITY_DATATABLE = 10;
  private static final OrderedMap mimeTypesMap = new OrderedMap();
  private static List unitsReturnList     = new ArrayList();
  private static List unitsList      = new ArrayList();
  private static List unitsRemainderList  = new ArrayList();
  private static Map  unitDictionaryUnitsCacheMap = new HashMap();
  private static Map  customUnitDictionaryUnitsCacheMap = new HashMap();

  private static Map  unitPreferenceMap = new HashMap();

  //static initialization - happens when classoader loads this class
  static {

    //initialize all unitTypes and Unit values from Unit Dictionary XML file...
    Thread initThread = new Thread() {

      public void run() {
        customUnitDictionaryUnitTypesArray = new String[0];
        String[] temp = getUnitDictionaryOriginalUnitTypes();
        for (int i=0; i<temp.length; i++) {
          getUnitDictionaryUnitsOfType(temp[i]);
        }
        Log.debug(45,  "*** WizardSettings static init thread done");
      }
    };
    initThread.run();
  }
  private static String summaryText;
  private static String dataLocation;


  private static final String EML_UNIT_DICTIONARY_PATH
  = "/xsl/eml-unitDictionary.xml";

  public static final int WIZARD_X_COORD = UISettings.WIZARD_X_COORD;

  public static final int WIZARD_Y_COORD = UISettings.WIZARD_Y_COORD;

  public static final int WIZARD_WIDTH   = UISettings.WIZARD_WIDTH;

  public static final int WIZARD_HEIGHT  = UISettings.WIZARD_HEIGHT;


  public static final int DIALOG_WIDTH = UISettings.POPUPDIALOG_WIDTH;

  public static final int DIALOG_HEIGHT = UISettings.POPUPDIALOG_HEIGHT;

  public static final int ATTR_DIALOG_HEIGHT = UISettings.POPUPDIALOG_FOR_ATTR_HEIGHT;

  public static final String PACKAGE_WIZ_FIRST_PAGE_ID = DataPackageWizardInterface.INTRODUCTION;

  public static final String ENTITY_WIZ_FIRST_PAGE_ID = DataPackageWizardInterface.DATA_LOCATION;

  public static final Color TOP_PANEL_BG_COLOR = new Color(11,85,112);
  
  public static final String UNAVAILABLE = "Unavailable";

  // x-dimension is ignored:
  public static final Dimension TOP_PANEL_DIMS = new Dimension(100,60);

  public static final int PADDING = 5;

  public static final Dimension DEFAULT_SPACER_DIMS = new Dimension(15, 15);

  public static final Font  TITLE_FONT
  = new Font("Sans-Serif", Font.BOLD,  13);

  public static final Color TITLE_TEXT_COLOR
  = new Color(255,255,255);

  public static final Font  SUBTITLE_FONT
  = new Font("Sans-Serif", Font.PLAIN, 11);

  public static final Color SUBTITLE_TEXT_COLOR
  = new Color(255,255,255);

  public static final Font  BUTTON_FONT
  = new Font("Sans-Serif",Font.PLAIN,11);

  public static final Color BUTTON_TEXT_COLOR
  = new Color(51, 51, 51);

  public static final  Font  WIZARD_CONTENT_FONT
  = new Font("Sans-Serif",Font.PLAIN,11);

  public static final  Font  WIZARD_CONTENT_BOLD_FONT
  = new Font("Sans-Serif",Font.BOLD,11);

  public static final  Color WIZARD_CONTENT_TEXT_COLOR
  = new Color(51, 51, 51);

  public static final  Color WIZARD_CONTENT_REQD_TEXT_COLOR
  = new Color(221, 0, 0);

  public static final  Color WIZARD_CONTENT_HILITE_BG_COLOR
  = new Color(175, 0, 0);

  public static final  Color WIZARD_CONTENT_HILITE_FG_COLOR
  = new Color(255, 255, 255);

  public static final  Dimension WIZARD_CONTENT_LABEL_DIMS
  = new Dimension(100,20);

  public static final  Dimension WIZARD_REDUCED_CONTENT_LABEL_DIMS
  = new Dimension(80,20);

  // x-dimension is ignored:
  public static final  Dimension WIZARD_CONTENT_TEXTFIELD_DIMS
  = new Dimension(2000,20);
  
  public static final  Dimension WIZARD_REDUCED_CONTENT_TEXTFIELD_DIMS
  = new Dimension(80,20);

  // x-dimension is ignored:
  public static final  Dimension WIZARD_CONTENT_SINGLE_LINE_DIMS
  = new Dimension(2000,20);

  private static final String FONT_STYLE = "style=\"font-size: 9px;\"";

  public static final String HTML_FONT_OPENING
          = "<font " + FONT_STYLE + " face=\"Sans-Serif\">";

  public static final String HTML_EXAMPLE_FONT_OPENING
                                                  = "<font " + FONT_STYLE +
                                                  " color=\"#666666\">";



  public static final String HTML_EXAMPLE_FONT_CLOSING = "</font>";

  public static final String HTML_TABLE_LABEL_OPENING
          = "<html><table width=\"100%\"><tr><td valign=\"top\" width=\"100%\">"
                                                        +HTML_FONT_OPENING;

  public static final String HTML_TABLE_LABEL_CLOSING
                                          = "</font></td></tr></table></html>";

  public static final String HTML_NO_TABLE_OPENING = "<html>"+HTML_FONT_OPENING;

  public static final String HTML_NO_TABLE_CLOSING = "</html>";

  public static final Dimension NAV_BUTTON_DIMS
      = new Dimension(80, 25);
  
  public static final Dimension LONG_BUTTON_DIMS
  = new Dimension(120, 25);

  public static final Dimension LIST_BUTTON_DIMS
      = new Dimension(85, 30);

  public static final Dimension LIST_BUTTON_DIMS_SMALL
      = new Dimension(60, 25);

  public    static final String SAVE_LATER_BUTTON_TEXT = /*"Save for Later"*/ Language.getInstance().getMessages("SaveForLater");
 
  public    static final String IMPORT_BUTTON_TEXT  = /*"Import"*/ Language.getInstance().getMessages("Import");

  public    static final String FINISH_BUTTON_TEXT  = /*"Finish"*/ Language.getInstance().getMessages("Finish");

  public    static final String PREV_BUTTON_TEXT    = /*"< Back"*/ Language.getInstance().getMessages("Back");

  protected static final String NEXT_BUTTON_TEXT    = /*"Next >"*/ Language.getInstance().getMessages("Next");

  protected static final String CANCEL_BUTTON_TEXT  = /*"Cancel"*/ Language.getInstance().getMessages("Cancel");

  protected static final String OK_BUTTON_TEXT      = /*"OK"*/ Language.getInstance().getMessages("OK");

  public static String NUMBER_OF_STEPS              = "15";

  /* Three variables defined below are used by AccessPage Screen
   */

  public static final int ACCESS_PAGE_AUTHSYS       = 1;

  public static final int ACCESS_PAGE_GROUP         = 2;

  public static final int ACCESS_PAGE_USER          = 3;

  public static final int MAX_IMPORTED_ROWS_DISPLAYED_IN_CODE_IMPORT = 25;

  protected static final String EML200_SCHEMA_NAMESPACE =
      "eml://ecoinformatics.org/eml-2.0.0";

  /*public static final String EML201_SCHEMA_NAMESPACE =
      "eml://ecoinformatics.org/eml-2.0.1";*/
  
  public static final String EML210_SCHEMA_NAMESPACE =
      "eml://ecoinformatics.org/eml-2.1.0";

  protected static final String NEW_EML200_DOCUMENT_TEXT =
  "<eml:eml "
  +"   packageId=\"\" system=\"knb\" "
  +"   xmlns:eml=\"eml://ecoinformatics.org/eml-2.0.0\" "
  +"   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
//  +"   xmlns:ds=\"eml://ecoinformatics.org/dataset-2.0.0\" "
  +"   xsi:schemaLocation=\"eml://ecoinformatics.org/eml-2.0.0 eml.xsd\"> "
  +"   <dataset> "
  +"   <title> </title> "
  +"   <creator> </creator>"
  +"   </dataset> "
  +"</eml:eml>";

  protected static final String TEMP_REFS_EML200_DOCUMENT_TEXT =
  "<eml:eml "
  +"   packageId=\"\" system=\"knb\" "
  +"   xmlns:eml=\"eml://ecoinformatics.org/eml-2.0.0\" "
  +"   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
  +"   xsi:schemaLocation=\"eml://ecoinformatics.org/eml-2.0.0 eml.xsd\"> "
  +"   <dataset> "
  +"   <title> </title> "
  +"   <creator> </creator>"
  +"   <coverage><taxonomicCoverage><taxonomicSystem><classificationSystem>"
  +"                             <classificationSystemCitation><title> </title>"
  +"            </classificationSystemCitation> </classificationSystem>"
  +"            </taxonomicSystem> </taxonomicCoverage></coverage>"
  +"   <project><title> </title></project>"
  +"   </dataset> "
  +"</eml:eml>";

  /*protected static final String NEW_EML201_DOCUMENT_TEXT =
  "<eml:eml "
  +"   packageId=\"\" system=\"knb\" "
  +"   xmlns:eml=\"eml://ecoinformatics.org/eml-2.0.1\" "
  +"   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
//  +"   xmlns:ds=\"eml://ecoinformatics.org/dataset-2.0.1\" "
  +"   xsi:schemaLocation=\"eml://ecoinformatics.org/eml-2.0.1 eml.xsd\"> "
  +"   <dataset> "
  +"   <title> </title> "
  +"   <creator> </creator>"
  +"   </dataset> "
  +"</eml:eml>";

  public static final String TEMP_REFS_EML201_DOCUMENT_TEXT =
  "<eml:eml "
  +"   packageId=\"\" system=\"knb\" "
  +"   xmlns:eml=\"eml://ecoinformatics.org/eml-2.0.1\" "
  +"   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
  +"   xsi:schemaLocation=\"eml://ecoinformatics.org/eml-2.0.1 eml.xsd\"> "
  +"   <dataset> "
  +"   <title> </title> "
  +"   <creator> </creator>"
  +"   <coverage><taxonomicCoverage><taxonomicSystem><classificationSystem>"
  +"                             <classificationSystemCitation><title> </title>"
  +"            </classificationSystemCitation> </classificationSystem>"
  +"            </taxonomicSystem> </taxonomicCoverage></coverage>"
  +"   <project><title> </title></project>"
  +"   </dataset> "
  +"</eml:eml>";*/
  
  protected static final String NEW_EML210_DOCUMENT_TEXT_WITHOUTACCESS =
	  "<eml:eml "
	  +"   packageId=\"\" system=\"knb\" "
	  +"   xmlns:eml=\"eml://ecoinformatics.org/eml-2.1.0\" "
	  +"   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
	  +"   xsi:schemaLocation=\"eml://ecoinformatics.org/eml-2.1.0 eml.xsd\"> "
	  //+"   <access> "
  	  //+"   </access>"
	  +"   <dataset> "
	  +"   <title> </title> "
	  +"   <creator> </creator>"
	  +"   </dataset> "
	  +"</eml:eml>";
  
  protected static final String NEW_EML210_DOCUMENT_TEXT_WITHACCESS =
	  "<eml:eml "
	  +"   packageId=\"\" system=\"knb\" "
	  +"   xmlns:eml=\"eml://ecoinformatics.org/eml-2.1.0\" "
	  +"   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
	  +"   xsi:schemaLocation=\"eml://ecoinformatics.org/eml-2.1.0 eml.xsd\"> "
	  +"   <access> "
  	  +"   </access>"
	  +"   <dataset> "
	  +"   <title> </title> "
	  +"   <creator> </creator>"
	  +"   </dataset> "
	  +"</eml:eml>";

	  public static final String TEMP_REFS_EML210_DOCUMENT_TEXT =
	  "<eml:eml "
	  +"   packageId=\"\" system=\"knb\" "
	  +"   xmlns:eml=\"eml://ecoinformatics.org/eml-2.1.0\" "
	  +"   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
	  +"   xsi:schemaLocation=\"eml://ecoinformatics.org/eml-2.1.0 eml.xsd\"> "
	  +"   <access> "
  	  +"   </access>"
	  +"   <dataset> "
	  +"   <title> </title> "
	  +"   <creator> </creator>"
	  +"   <coverage><taxonomicCoverage><taxonomicSystem><classificationSystem>"
	  +"                             <classificationSystemCitation><title> </title>"
	  +"            </classificationSystemCitation> </classificationSystem>"
	  +"            </taxonomicSystem> </taxonomicCoverage></coverage>"
	  +"   <project><title> </title></project>"
	  +"   </dataset> "
	  +"</eml:eml>";

//  /////

  /*
   * these values denote the location of the data:
   */
  public static final short INLINE  = 0;
  public static final short ONLINE  = 10;
  public static final short OFFLINE = 20;
  public static final short NODATA  = 30;

  public static final String SUMMARY_TEXT_INLINE
  = "In addition to describing your data, you have chosen to include it "
  + "within the data package.";

  public static final String SUMMARY_TEXT_ONLINE
  = "You have chosen to describe data that is available online at: ";

  public static final String SUMMARY_TEXT_OFFLINE
  = "You have chosen to describe data, but not make the data itself "
  + "available at this time.";

  public static final String SUMMARY_TEXT_NODATA
  = "You have chosen not to include or describe any data in your data "
  + "package at this time. Data may be added later";

  public static final String HEX_VALUE_TAB    = "#x09";
  public static final String HEX_VALUE_SPACE  = "#x20";


  /**
   *  sets summary text that will be shown on the final page of the wizard.
   *  <em>NOTE that this method makes an internal call to setDataLocation() and
   *  sets the dataLocation to null; therefore, any calls to setDataLocation()
   *  shoudl be made *AFTER* calling this function!</em>
   *
   *  @param  text the String to be displayed. Must be one of the final static
   *          Strings defined elsewhere in this class, named SUMMARY_TEXT_***,
   *          otherwise text will be unchanged
   */
  public static void setSummaryText(String text) {

    if (text==null) return;
    if (text.equals(SUMMARY_TEXT_INLINE) || text.equals(SUMMARY_TEXT_ONLINE)
    || text.equals(SUMMARY_TEXT_NODATA) || text.equals(SUMMARY_TEXT_OFFLINE)) {

      summaryText = text;
      setDataLocation(null);
    }
  }

  /**
   *  gets summary text that will be shown on the final page of the wizard
   *
   *  @return text the summary String to be displayed.
   */
  public static String getSummaryText() {

    return summaryText;
  }


  /**
   *  sets data location to be used in summary text that will be shown on the
   *  page of the wizard. For Online data, this would be a URL, and for inline
   *  data, it could be a file:// url or the filename or something similar.
   *
   *  @param  loc the location to be displayed. May be null or empty, or may
   *          contain only whitespace characters.
   */
  public static void setDataLocation(String loc) { dataLocation = loc; }


  /**
   *  gets data location to be used in summary text that will be shown on the
   *  page of the wizard. For Online data, this would be a URL, and for inline
   *  data, it could be a file:// url or the filename or something similar.
   *  Note that text should be displayed only if the summary text is set to
   *  SUMMARY_TEXT_INLINE or SUMMARY_TEXT_ONLINE. NOTE that this method may
   *  return a null value or an empty value for the location string, if that's
   *  what the user has set, so the summary should default gracefully and not
   *  show a location in such cases.
   *
   *  @return the String location to be displayed. May be null or empty, or may
   *          contain only whitespace characters.
   */
  public static String getDataLocation() { return dataLocation; }


  /**
   *  gets a String id that is guaranteed to be unique within the current
   *  document (ie document scope). Note that the ID String is a timestamp in
   *  milliseconds, so all IDs generated by this method runnign on a given
   *  machine will always be unique with respect to all other IDs generated by
   *  this method, provided the system clock is not reset. Absolute "global"
   *  uniqueness is not guaranteed, and cannot be assumed
   *
   *  @return a String id that is guaranteed to be unique within the current
   *          document (ie document scope)
   */
  public static String getUniqueID() {

    return UISettings.getUniqueID();
  }

  /**
   *  from the eml unit dictionary, gets all the units that are the SI units of some
   *	unit tpye
   *
   *  @return String array containing the SI units in the unitdictionary
   */
  public static String[] getSIUnits() {

    if(existingSIUnits != null) return existingSIUnits;
    String[] unitTypes = getUnitDictionaryUnitTypes();
    existingSIUnits = new String[unitTypes.length];
    for(int i = 0; i < unitTypes.length; i++) {
      getUnitDictionaryUnitsOfType(unitTypes[i]);
      existingSIUnits[i] = getPreferredType(unitTypes[i]);
    }
    Arrays.sort(existingSIUnits);
    return existingSIUnits;
  }

  /**
   *  from the eml unit dictionary, gets only the fundamental unitTypes
   *
   *  @return String array containing the fundamental unitTypes in the unitdictionary
   */
  public static String[] getUnitDictionaryBasicUnitTypes() {

    return basicUnitTypes;
    /*if (unitDictionaryBasicUnitTypesArray == null) {
      unitDictionaryBasicUnitTypesArray = getUnitTypesWithXPath(BASIC_UNIT_TYPES_XPATH);
    }
    return unitDictionaryBasicUnitTypesArray;
    */
  }

  /**
   *  from the eml unit dictionary, gets all the unitTypes (both fundamental
   *  and derived)
   *
   *  @return String array containing all the unitTypes in the unitdictionary
   */
  public static String[] getUnitDictionaryUnitTypes() {

    if (unitDictionaryUnitTypesArray==null) {
      unitDictionaryUnitTypesArray = getUnitTypesWithXPath(ALL_UNIT_TYPES_XPATH);
    }
    String newArr[] = new String[unitDictionaryUnitTypesArray.length + customUnitDictionaryUnitTypesArray.length];
    int i;
    for(i = 0; i<unitDictionaryUnitTypesArray.length; i++)
      newArr[i] = unitDictionaryUnitTypesArray[i];
    for(int j = 0; j < customUnitDictionaryUnitTypesArray.length; j++)
      newArr[i++] = customUnitDictionaryUnitTypesArray[j];
		Arrays.sort(newArr);
    return newArr;
  }

  private static String[] getUnitDictionaryOriginalUnitTypes() {

    if (unitDictionaryUnitTypesArray==null) {
      unitDictionaryUnitTypesArray = getUnitTypesWithXPath(ALL_UNIT_TYPES_XPATH);
    }
    return unitDictionaryUnitTypesArray;
  }

  private static String[] getUnitTypesWithXPath(String unitXPath) {

    Reader reader = null;
    try {
      File unitDict = new File("./xsl/eml-unitDictionary.xml");
      reader = new FileReader(unitDict);
      //        reader = IOUtil.getResourceAsInputStreamReader(EML_UNIT_DICTIONARY_PATH);
      // above change to use a FileReader was made to get this code to work on a Mac;
      // Use of getResourceAsInputStreamReader(EML_UNIT_DICTIONARY_PATH) seems to work fine on Windows!
      // DFH - Mar 2004
    } catch (Exception e) {

      e.printStackTrace();
      Log.debug(12,"Exception: <"+e+"> trying to open unit dictionary file.\n"
      +"\nClasspath was: \n"+System.getProperty("java.class.path")+"\n");
      reader = null;
    }

    if (reader==null) {

      Log.debug(1,"Can't find unit dictionary file at: "+EML_UNIT_DICTIONARY_PATH);
      return new String[]{"ERROR"};
    }

    try {
      udRootNode = XMLUtilities.getXMLReaderAsDOMTreeRootNode(reader);

    } catch (IOException ioe) {
      Log.debug(12,"Exception getting unit dictionary RootNode: "+ioe);
      ioe.printStackTrace();
      return new String[] {"IOException!"};
    }

    Node[] unitTypesNodeArray = null;

    unitTypesNodeArray = getNodeArrayWithXPath(udRootNode, unitXPath);

    if (unitTypesNodeArray==null) {
      Log.debug(1,"Fatal error - unitTypesNodeArray == NULL");
      return new String[] {"ERROR!!"};
    }

    final int totUnitTypes      = unitTypesNodeArray.length;
    String[] unitTypesArray  = new String[totUnitTypes];

    for (int i=0; i<totUnitTypes; i++) {

      unitTypesArray[i] = unitTypesNodeArray[i].getNodeValue();
    }
    Arrays.sort(unitTypesArray);

    return unitTypesArray;
  }


  /**
   *  from the eml unit dictionary, gets all the units of the given unitType
   *
   *  @param  unitType the String representation of the unitType to look for
   *
   *  @return String array containing all the units in the unitdictionary that
   *          have the given unitType
   */
  public static String[] getUnitDictionaryUnitsOfType(String unitType) {

    if (unitType==null) return null;
    String[] returnArray = null;

    if (unitDictionaryUnitsCacheMap.containsKey(unitType)) {

      returnArray = (String[])(unitDictionaryUnitsCacheMap.get(unitType));

    } else if (customUnitDictionaryUnitsCacheMap.containsKey(unitType)) {

      returnArray = (String[])(customUnitDictionaryUnitsCacheMap.get(unitType));

    } else {

      // ensure xml DOM has already been created...
      if (udRootNode==null || unitDictionaryUnitTypesArray==null) {
        getUnitDictionaryUnitTypes();
      }
      unitsReturnList.clear();
      unitsList.clear();
      unitsRemainderList.clear();

      // init - get node array containing all <unit> elements - do only once!
      if (unitsNodeArray==null) {

        unitsNodeArray = getNodeArrayWithXPath(udRootNode, UNITS_XPATH);

        if (unitsNodeArray==null) {
          Log.debug(1,"Fatal error - unitsNodeArray == NULL");
          return new String[] {"ERROR!!"};
        }
      }

      // for each unit element node, get list of attribute nodes;
      for (int i=0; i<unitsNodeArray.length; i++) {

        NamedNodeMap attribNNMap = unitsNodeArray[i].getAttributes();

        if (attribNNMap==null || attribNNMap.getLength()<1) continue;

        Node unitTypeAttrNode = attribNNMap.getNamedItem("unitType");
        Node parentSIAttrNode2 = attribNNMap.getNamedItem("parentSI");


        // if attributes contains an attrib node called unitType {
        if (unitTypeAttrNode!=null) {

          // if unitType value==requested unitType add name to unitsList
          if (((Attr)unitTypeAttrNode).getValue().equals(unitType)) {

            addAttributeNameToList(attribNNMap, unitsList);

            if (parentSIAttrNode2!=null) {
              String parentSIVal2 = ((Attr)parentSIAttrNode2).getValue();
              unitPreferenceMap.put(unitType.toLowerCase(), parentSIVal2);
            } else {
              Attr nameAttr = (Attr)attribNNMap.getNamedItem("name");
              if(nameAttr != null) {
                String val = nameAttr.getValue().trim();
                if(val.length() > 0) unitPreferenceMap.put(unitType.toLowerCase(), val);
              }
            }

          }
        } else {  //  add unit node to unitsRemainderList

          unitsRemainderList.add(unitsNodeArray[i]);
        }
      }

      //2. for each unit element in unitsRemainderList,
      for (Iterator it = unitsRemainderList.iterator(); it.hasNext(); ) {

        Object nextObj = it.next();

        if (nextObj!=null) {

          NamedNodeMap attribNNMap = ((Node)nextObj).getAttributes();

          if (attribNNMap==null || attribNNMap.getLength()<1) continue;

          Node parentSIAttrNode = attribNNMap.getNamedItem("parentSI");

          // if attributes contains an attrib node called parentSI
          if (parentSIAttrNode!=null) {

            // if unitsList.contains(parentSI value), add name attr to unitsReturnList
            if (unitsList.contains( StringUtil.stripTabsNewLines(
            ((Attr)parentSIAttrNode).getValue()))) {

              String parentSIVal = ((Attr)parentSIAttrNode).getValue();
              unitPreferenceMap.put(unitType.toLowerCase(), parentSIVal);
              addAttributeNameToList(attribNNMap, unitsReturnList);
            }
          } else {
            Attr nameAttr = (Attr)attribNNMap.getNamedItem("name");
            if(nameAttr != null) {
              String val = nameAttr.getValue().trim();
              if(val.length() > 0) unitPreferenceMap.put(unitType.toLowerCase(), val);
            }
          }
        }
      }

      //3. finally, add unitsList to unitsReturnList, make into array, sort and return
      unitsReturnList.addAll(unitsList);

      if (unitType.equalsIgnoreCase("amount")) {  //DFH - patch so that 'moles' is not the only choice
        returnArray = new String[unitsReturnList.size()+1];
        unitsReturnList.add("dimensionless");
      } else {
        returnArray = new String[unitsReturnList.size()];
      }

      unitsReturnList.toArray(returnArray);

      if (returnArray.length > 1) Arrays.sort(returnArray);

      unitDictionaryUnitsCacheMap.put(unitType, returnArray);
    }
    return returnArray;
  }

  /**
   *  returns the display string for a given unit Type. Display string has
   *	words with the first letter capitalized. For example, for the unit type
   * 	currentDensity, it would return the string "Current Density"
   *
   *  @param  unitType the String representation of the unitType to look for
   *
   *  @return display String for the given unit type
   */

  public static String getDisplayFormOfUnitType(String unitType) {

    if (unitType==null || unitType.trim().equals("")) return "";

    StringBuffer buff = new StringBuffer();

    final char SPACE = ' ';

    int length = unitType.length();

    char[] originalUnitTypeChars      = new char[length];
    unitType.getChars(0, length, originalUnitTypeChars, 0);

    char[] upperCaseUnitTypeChars = new char[length];
    unitType.toUpperCase().getChars(0, length, upperCaseUnitTypeChars, 0);

    //make first char uppercase:
    buff.append(upperCaseUnitTypeChars[0]);

    for (int i=1; i<length; i++) {

      //if it's an uppercase letter, add a space before it:
      if (originalUnitTypeChars[i]==upperCaseUnitTypeChars[i]) {

        buff.append(SPACE);
      }
      buff.append(originalUnitTypeChars[i]);
    }
    return buff.toString();
  }

  public static String getStandardFormOfUnitType(String unitType) {

    StringTokenizer st = new StringTokenizer(unitType, " ");
    String result = "";
    int cnt = 0;
    while(st.hasMoreTokens()) {
      if(cnt == 0)
        result += st.nextToken().toLowerCase();
      else
        result+= st.nextToken();
      cnt++;
    }
    return result;
  }

  public static void insertObjectIntoArray( Object[] arr, Object value, Object[] newArr) {

    int idx = Arrays.binarySearch(arr, value);
		int pos = 0;
		if(idx >= 0) pos = idx;
    else pos = -(idx + 1);
    int i = 0;
    for(i = 0 ;i < pos; i++)
      newArr[i] = arr[i];
    newArr[i] = value;
    for(int j = pos; j < arr.length;j++)
      newArr[j+1] = arr[j];

    return;
  }

  /**
   *  returns the definition for a given unit type as a list of terms. Each
   *	term is a 2-element list consisting of a Fundamental unit type and the
   *	power that it is raised to
   *
   *  @param  unitType the String representation of the unitType to look for
   *
   *  @return List of lists of the basic units and powers, that define this unit
   */

  public static List getDefinitionsForUnitType(String unitType) {

    Reader reader = null;
    try {
      File unitDict = new File("./xsl/eml-unitDictionary.xml");
      reader = new FileReader(unitDict);
      //        reader = IOUtil.getResourceAsInputStreamReader(EML_UNIT_DICTIONARY_PATH);
      // above change to use a FileReader was made to get this code to work on a Mac;
      // Use of getResourceAsInputStreamReader(EML_UNIT_DICTIONARY_PATH) seems to work fine on Windows!
      // DFH - Mar 2004
    } catch (Exception e) {

      e.printStackTrace();
      Log.debug(12,"Exception: <"+e+"> trying to open unit dictionary file.\n"
      +"\nClasspath was: \n"+System.getProperty("java.class.path")+"\n");
      reader = null;
    }

    if (reader==null) {

      Log.debug(1,"Can't find unit dictionary file at: "+EML_UNIT_DICTIONARY_PATH);
      return null;
    }

    Node rootNode;
    try {
      rootNode = XMLUtilities.getXMLReaderAsDOMTreeRootNode(reader);

    } catch (IOException ioe) {
      Log.debug(12,"Exception getting unit dictionary RootNode: "+ioe);
      ioe.printStackTrace();
      return null;
    }

    unitType = getStandardFormOfUnitType(unitType);
    Node unitTypeNode = null;
    String unitTypeXPath = "/stmml:unitList/stmml:unitType[@name='" + unitType + "']";
    try {
      unitTypeNode = XMLUtilities.getNodeWithXPath(rootNode, unitTypeXPath);
    } catch (Exception te) {
      Log.debug(12, "Exception retreving the given unitType node");
      return null;
    }

    if (unitTypeNode==null) {
      Log.debug(12, "Got no nodes for the given unit type");
      return null;
    }

    List result = new ArrayList();
    NodeList children = unitTypeNode.getChildNodes();
    Log.debug(12, "children got = " + children.getLength());
    for(int i = 0; i < children.getLength(); i++) {

      String name = "";
      String power = "";
      Node child = children.item(i);
      NamedNodeMap attrs = child.getAttributes();
      if(attrs == null)
        continue;
      Node nameAttr = attrs.getNamedItem("name");
      if(nameAttr != null)
        name = nameAttr.getNodeValue();
      else
        continue;
      Node powerAttr = attrs.getNamedItem("power");
      if(powerAttr != null)
        power = powerAttr.getNodeValue();
      else
        power = "1";

      List row = new ArrayList();
      row.add(getDisplayFormOfUnitType(name));
      row.add(power);
      result.add(row);
    }

    try {
      if(reader != null)
        reader.close();
    }
    catch(Exception e) {}
    return result;
  }


  /**
   *  returns the preferred unit for each unitType; to be used for
   *  setting the default unit type
   */
  public static String getPreferredType(String unitType) {

    if(unitType == null) return null;
    String res = (String)(unitPreferenceMap.get(unitType.toLowerCase()));
    return res;
  }


  public static boolean isCustomUnit(String type, String unit) {

		type = getStandardFormOfUnitType(type);
    boolean newT = unitDictionaryUnitsCacheMap.containsKey(type);
    if(newT) {
      String[] units = (String[])unitDictionaryUnitsCacheMap.get(type);
      if(Arrays.binarySearch(units, unit) >= 0) return false;
      else return true;
    } else {
			return true;
		}
	}

	  /**
   *  given an entityType, returns an <code>OrderedMap<code> whose keys contain
   *  the human-readable display names for all the allowable MIME types (for the
   *  given entity type), and whose corresponding values are the actual MIME
   *  types themselves.
   *
   *  To add more MIME types,
   *  @see  http://www.iana.org/assignments/media-types/
   *        or /etc/mime.types on linux
   *
   *  @param  entityType the constant representing the entity type whose
   *          allowable MIME types are sought. Currently, only possible values
   *          are:
   *          WizardSettings.ENTITY_DATATABLE
   *
   *  @return Map whose keys contain the human-readable display names for all
   *          the allowable MIME types (for the given entity type), and whose
   *          corresponding values are the actual MIME types themselves.
   */
  public static OrderedMap getSupportedMIMETypesForEntity(short entityType) {

    mimeTypesMap.clear();

    switch (entityType) {

      case ENTITY_DATATABLE:

        mimeTypesMap.put("Microsoft Excel",         "application/vnd.ms-excel");
        mimeTypesMap.put("XML text",                "application/xml");
        mimeTypesMap.put("HTML text",               "Text/html");
        mimeTypesMap.put("XHTML text",              "application/xhtml+xml");
        mimeTypesMap.put("Mathematica",             "application/mathematica");

//  GRASS
//  matlab
//  maple
//  sas
//  splus
//  R
//  all of the ESRI binary formats
//  text formats like ESRI's ArcInfo export format


        break;

      default:
        //leave map empty
    }
    return mimeTypesMap;
  }



  //
  //  To add more MIME types,
  //  @see  http://www.iana.org/assignments/media-types/
  //        or /etc/mime.types on linux
  //
  //  SPARES - JUST IN CASE...
  //
  //  mimeTypesMap.put("Plain Text",                "Text/plain");
  //  mimeTypesMap.put("Formatted/Rich Text",       "Text/enriched");
  //  mimeTypesMap.put("Microsoft Word",            "application/msword");
  //  mimeTypesMap.put("Tar Archive",               "application/x-tar");
  //  mimeTypesMap.put("Zip-Compressed Archive",    "Application/x-compressed");
  //  mimeTypesMap.put("Macintosh Stuffit Archive", "application/x-stuffit");
  //  mimeTypesMap.put("Adobe PDF File",            "Application/pdf");
  //  mimeTypesMap.put("PostScript File",           "Application/postscript");

  //////////////////////////////////////////////////////////////////////////////




  private static void addAttributeNameToList(NamedNodeMap map, List list) {

    Node nameAttrNode = map.getNamedItem("name");
    if (nameAttrNode!=null) {
      list.add(StringUtil.stripTabsNewLines(((Attr)nameAttrNode).getValue()));
    }
  }

  private static Node[] getNodeArrayWithXPath(Node rootNode, String xpath) {

    NodeList nodeList = null;
    try {
      nodeList = XMLUtilities.getNodeListWithXPath(rootNode, xpath);
    } catch (Exception ioe) {
      Log.debug(12,"Exception getting nodeList: "+ioe);
      ioe.printStackTrace();
      return null;
    }
    if (nodeList==null) {
      Log.debug(1,"Fatal error - nodeList == NULL");
      return null;
    }
    return XMLUtilities.getNodeListAsNodeArray(nodeList);
  }



  /**
   * returns true if the String can be parsed as a float
   */
  private static float floatNum = 0f;
  //
  public static boolean isFloat(String numberString) {

    try {
      floatNum = Float.parseFloat(numberString);
    } catch (Exception e) {
      return false;
    }
    return !(Float.isNaN(floatNum));
  }

  // Taxonomic Information

  public static final String[] stdTaxonRanks = {"Kingdom", "Sub-Kingdom", "Phylum", "Sub-Phylum", "Class", "Sub-Class", "Order", "Sub-Order", "Family", "Sub-Family", "Genus", "Sub-Genus", "Species", "Sub-Species"};

  public static final String[] commonTaxonRanks = {"Kingdom", "Phylum", "Class", "Order", "Family", "Genus", "Species"};

  public static final int NUMBER_OF_TAXON_RANKS = 7;

  public static int getIndexOfTaxonRank(String rank) {

    for(int i = 0;i < commonTaxonRanks.length; i++)
      if(commonTaxonRanks[i].equals(rank))
        return i;
    return -1;
  }

  public static List getTaxonHierarchyTillIndex(int index) {

    List ret = new ArrayList();
    for(int i =0; i <= index; i++)
      ret.add(commonTaxonRanks[i]);
    return ret;
  }


}

