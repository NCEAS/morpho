/**
 *  '$RCSfile: WizardSettings.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2003-11-26 17:54:19 $'
 * '$Revision: 1.32 $'
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

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.IOUtil;
import edu.ucsb.nceas.utilities.StringUtil;
import edu.ucsb.nceas.utilities.XMLUtilities;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComponent;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.Reader;
import java.io.IOException;

import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;

/**
 *  WizardSettings
 *
 */

public class WizardSettings {

  private static final String UNIT_TYPES_XPATH
                                      = "/stmml:unitList/stmml:unitType/@name";
  private static Node udRootNode = null;
  private static String[] unitDictionaryUnitTypesArray;

  private static Node[] unitsNodeArray = null;
  private static final String UNITS_XPATH    = "/stmml:unitList/stmml:unit";
  public  static final short ENTITY_DATATABLE = 10;
  private static final OrderedMap mimeTypesMap = new OrderedMap();
  private static List unitsReturnList     = new ArrayList();
  private static List unitsList      = new ArrayList();
  private static List unitsRemainderList  = new ArrayList();
  private static Map  unitDictionaryUnitsCacheMap = new HashMap();

  //static initialization - happens when classoader loads this class
  static {

    //initialize all unitTypes and Unit values from Unit Dictionary XML file...
    Thread initThread = new Thread() {

      public void run() {
        String[] temp = getUnitDictionaryUnitTypes();
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

  protected static final int WIZARD_X_COORD = 100;

  protected static final int WIZARD_Y_COORD = 100;

  protected static final int WIZARD_WIDTH   = 800;

  protected static final int WIZARD_HEIGHT  = 600;

  private   static final int DIALOG_SMALLER_THAN_WIZARD_BY = 30;

  public static final int DIALOG_WIDTH
  = WIZARD_WIDTH - DIALOG_SMALLER_THAN_WIZARD_BY;

  public static final int DIALOG_HEIGHT
  = WIZARD_HEIGHT - DIALOG_SMALLER_THAN_WIZARD_BY;

  protected static final String FIRST_PAGE_ID = DataPackageWizardInterface.INTRODUCTION;

  protected static final Color TOP_PANEL_BG_COLOR = new Color(11,85,112);

  // x-dimension is ignored:
  protected static final Dimension TOP_PANEL_DIMS = new Dimension(100,60);

  protected static final int PADDING = 5;

  public static final Dimension DEFAULT_SPACER_DIMS = new Dimension(15, 15);

  protected static final Font  TITLE_FONT
  = new Font("Sans-Serif", Font.BOLD,  12);

  protected static final Color TITLE_TEXT_COLOR
  = new Color(255,255,255);

  protected static final Font  SUBTITLE_FONT
  = new Font("Sans-Serif", Font.PLAIN, 11);

  protected static final Color SUBTITLE_TEXT_COLOR
  = new Color(255,255,255);

  protected static final Font  BUTTON_FONT
  = new Font("Sans-Serif",Font.BOLD,12);

  protected static final Color BUTTON_TEXT_COLOR
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

  // x-dimension is ignored:
  public static final  Dimension WIZARD_CONTENT_SINGLE_LINE_DIMS
  = new Dimension(2000,20);

  public static final String HTML_FONT_OPENING
          = "<font size=\"3\" face=\"verdana\">";

  public static final String HTML_EXAMPLE_FONT_OPENING
                                                  = "<font color=\"#666666\">";

  public static final String HTML_EXAMPLE_FONT_CLOSING = "</font>";

  public static final String HTML_TABLE_LABEL_OPENING
          = "<html><table width=\"100%\"><tr><td valign=\"top\" width=\"100%\">"
                                                        +HTML_FONT_OPENING;

  public static final String HTML_TABLE_LABEL_CLOSING
                                          = "</font></td></tr></table></html>";

  public static final String HTML_NO_TABLE_OPENING = "<html>"+HTML_FONT_OPENING;

  public static final String HTML_NO_TABLE_CLOSING = "</html>";

  public static final  Dimension LIST_BUTTON_DIMS
  = new Dimension(100,30);

  public static final  Dimension LIST_BUTTON_DIMS_SMALL
  = new Dimension(70,25);

  public    static final String FINISH_BUTTON_TEXT  = "Finish";

  public    static final String PREV_BUTTON_TEXT    = "< Back";

  protected static final String NEXT_BUTTON_TEXT    = "Next >";

  protected static final String CANCEL_BUTTON_TEXT  = "Cancel";

  protected static final String OK_BUTTON_TEXT      = "OK";

  protected static final String NUMBER_OF_STEPS     = "11";

  protected static final String NEW_EML200_DOCUMENT_TEXT =
  "<eml:eml "
  +"   packageId=\"\" system=\"knb\" "
  +"   xmlns:eml=\"eml://ecoinformatics.org/eml-2.0.0\" "
  +"   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
  +"   xmlns:ds=\"eml://ecoinformatics.org/dataset-2.0.0\" "
  +"   xsi:schemaLocation=\"eml://ecoinformatics.org/eml-2.0.0 eml.xsd\"> "
  +"   <dataset/> "
  +"</eml:eml>";


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


  private static long previousTimeStamp;
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

    //just use a timestamp, but ensure that any
    //subsequent calls won't get same timestamp (feasible if this method called
    //twice in less than 0.5mS):
    long timeStamp = 0L;

    do {
      timeStamp = System.currentTimeMillis();

    } while (timeStamp==previousTimeStamp);

    //remember value for next time...
    previousTimeStamp = timeStamp;

    String id = String.valueOf(timeStamp);

    return id;
  }



  /**
   *  from the eml unit dictionary, gets all the unitTypes (both fundamental
   *  and derived)
   *
   *  @return String array containing all the unitTypes in the unitdictionary
   */
  public static String[] getUnitDictionaryUnitTypes() {

    if (unitDictionaryUnitTypesArray==null) {

      Reader reader = null;
      try {
        reader = IOUtil.getResourceAsInputStreamReader(EML_UNIT_DICTIONARY_PATH);

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

      unitTypesNodeArray = getNodeArrayWithXPath(udRootNode, UNIT_TYPES_XPATH);

      if (unitTypesNodeArray==null) {
        Log.debug(1,"Fatal error - unitTypesNodeArray == NULL");
        return new String[] {"ERROR!!"};
      }

      final int totUnitTypes      = unitTypesNodeArray.length;
      unitDictionaryUnitTypesArray  = new String[totUnitTypes];

      for (int i=0; i<totUnitTypes; i++) {

        unitDictionaryUnitTypesArray[i] = unitTypesNodeArray[i].getNodeValue();
      }
      Arrays.sort(unitDictionaryUnitTypesArray);
    }
    return unitDictionaryUnitTypesArray;
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

        // if attributes contains an attrib node called unitType {
        if (unitTypeAttrNode!=null) {

          // if unitType value==requested unitType add name to unitsList
          if (((Attr)unitTypeAttrNode).getValue().equals(unitType)) {

            addAttributeNameToList(attribNNMap, unitsList);
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

              addAttributeNameToList(attribNNMap, unitsReturnList);
            }
          }
        }
      }

      //3. finally, add unitsList to unitsReturnList, make into array, sort and return
      unitsReturnList.addAll(unitsList);

      returnArray = new String[unitsReturnList.size()];

      unitsReturnList.toArray(returnArray);

      if (returnArray.length > 1) Arrays.sort(returnArray);

      unitDictionaryUnitsCacheMap.put(unitType, returnArray);
    }
    return returnArray;
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
        mimeTypesMap.put("mathematica",             "application/mathematica");

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
}

