/**
 *  '$RCSfile: WizardSettings.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-09-25 19:28:22 $'
 * '$Revision: 1.18 $'
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
import edu.ucsb.nceas.utilities.XMLUtilities;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComponent;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.Reader;
import java.io.IOException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;

/**
 *  WizardSettings
 *
 */

public class WizardSettings {

  
  private static String summaryText;
  private static String dataLocation;

  
  private static final String EML_UNIT_DICTIONARY_PATH 
                                                = "/xsl/eml-unitDictionary.xml";

  protected static final int WIZARD_X_COORD = 100;

  protected static final int WIZARD_Y_COORD = 100;

  protected static final int WIZARD_WIDTH   = 800;

  protected static final int WIZARD_HEIGHT  = 600;

  private   static final int DIALOG_SMALLER_THAN_WIZARD_BY = 30;

  protected static final int DIALOG_WIDTH  
                          = WIZARD_WIDTH - DIALOG_SMALLER_THAN_WIZARD_BY;

  protected static final int DIALOG_HEIGHT  
                          = WIZARD_HEIGHT - DIALOG_SMALLER_THAN_WIZARD_BY;

  protected static final String FIRST_PAGE_ID = WizardPageLibrary.INTRODUCTION;
  
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
  // x-dimension is ignored:
  public static final  Dimension WIZARD_CONTENT_TEXTFIELD_DIMS  
                                      = new Dimension(2000,20);

  // x-dimension is ignored:
  public static final  Dimension WIZARD_CONTENT_SINGLE_LINE_DIMS  
                                      = new Dimension(2000,20);

                                      
  public static final  Dimension LIST_BUTTON_DIMS  
                                      = new Dimension(100,30);
                                      
  public static final  Dimension LIST_BUTTON_DIMS_SMALL  
                                      = new Dimension(70,25);
                                      
  public    static final String FINISH_BUTTON_TEXT  = "Finish";
  
  public    static final String PREV_BUTTON_TEXT    = "< Back";
  
  protected static final String NEXT_BUTTON_TEXT    = "Next >";
  
  protected static final String CANCEL_BUTTON_TEXT  = "Cancel";

  protected static final String OK_BUTTON_TEXT      = "OK";

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
   *  from the eml unit dictionary, gets all the unitTypes (both fundamental 
   *  and derived) 
   *
   *  @return String array containing all the unitTypes in the unitdictionary
   */
  private static final String UNIT_TYPES_XPATH    = "/stmml:unitList/unitType/@id";
  private static final String UNIT_TYPES_NAME_ATT = "@name";
  private static final String UNIT_TYPES_EXCLUDE  = "dimension";
  private static Map          unitDictionaryNVPs;
  /////////////
  public static String[] getUnitDictionaryUnitTypes() { 

    Reader reader = null;
    try {
      
      reader = IOUtil.getResourceAsInputStreamReader(EML_UNIT_DICTIONARY_PATH);
      
    } catch (Exception e) {
    
      e.printStackTrace();
      Log.debug(12,"Exception: <"+e+"> trying to open unit dictionary file.\n"
           +"\nClasspath was: ---------------------------------------------\n"
                                   +System.getProperty("java.class.path")+"\n");
      reader = null;
    }
    
    if (reader==null) {
    
      Log.debug(1, "ATTENTION! Cannot find unit dictionary file at: "
                                                    +EML_UNIT_DICTIONARY_PATH);
      return new String[]{" "};
    } 
    
    Node udRootNode = null;
    try {
    
      udRootNode = XMLUtilities.getXMLReaderAsDOMTreeRootNode(reader);
      
    } catch (IOException ioe) {
    
      ioe.printStackTrace();
      return new String[] {"IOException!"};
    }
    
    
    List returnList = new ArrayList();
    
    
    NodeList unitTypesNodeList = null;
    try {
    
      unitTypesNodeList = XMLUtilities.getNodeListWithXPath(udRootNode, UNIT_TYPES_XPATH);
      System.err.println("IS IT A NODESET? unitTypesNodeList = "+unitTypesNodeList);
      
    } catch (Exception ioe) {
    
      ioe.printStackTrace();
      return new String[] {"Exception!"};
    }
    
    
//    unitDictionaryNVPs = XMLUtilities.getDOMTreeAsXPathMap(udRootNode);
//    
//    if (unitDictionaryNVPs==null) {
//      Log.debug(1,"Fatal error - can't find unit dictionary!");
//      return new String[] {"ERROR!"};
//    }    
//    Object nextObj  = null;
//    String nextStr  = null;
//    
//    for (Iterator it = unitDictionaryNVPs.keySet().iterator(); it.hasNext(); ) {
//    
//      nextObj = it.next();
//      if (nextObj==null) continue;
//      nextStr = (String)nextObj;
//      
//      if ( (nextStr.indexOf(UNIT_TYPES_XPATH) == 0) 
//                  && (nextStr.indexOf(UNIT_TYPES_NAME_ATT) > 0) 
//                              && (nextStr.indexOf(UNIT_TYPES_EXCLUDE) < 0) ) {
//
//        returnList.add((String)(unitDictionaryNVPs.get(nextStr)));
//      }
//    }


    String[] returnArray = new String[returnList.size()];

    returnArray = (String[])(returnList.toArray(returnArray));
    Arrays.sort(returnArray);
    return returnArray;
  }

  
  /**
   *  from the eml unit dictionary, gets all the units of the given unitType 
   *
   *  @param  UnitType the String representation of the unitType to look for
   *
   *  @return String array containing all the units in the unitdictionary that 
   *          have the given unitType
   */
  private static final String UNITS_XPATH    = "/stmml:unitList/unit";
  private static final String UNITS_TYPE_ATT = "@unitType";
  private static final String UNITS_PRNT_ATT = "@parentSI";
  private static final String UNITS_EXCLUDE  = "description";
  private static final String UNITS_NAME_ATT = "@name";
  //////// 
  public static String[] getUnitDictionaryUnitsOfType(String UnitType) { 
  

    List returnList     = new ArrayList();
    Object nextKeyObj   = null;
    String nextKeyStr   = null;
    Object nextValObj   = null;
    String nextValueStr = null;
    
    
    if (unitDictionaryNVPs==null) return new String[] {"ERROR!"};
    
    for (Iterator it = unitDictionaryNVPs.keySet().iterator(); it.hasNext(); ) {
    
      nextKeyObj = it.next();
      if (nextKeyObj==null) continue;
      nextKeyStr = (String)nextKeyObj;
      
      int unitTypeIndex = nextKeyStr.indexOf(UNITS_TYPE_ATT);
      int parentSIIndex = nextKeyStr.indexOf(UNITS_PRNT_ATT);
      int trimIndex = -1;
      
      if (unitTypeIndex>0) {
      
        trimIndex = unitTypeIndex;
        
      } else if (parentSIIndex>0) {
      
        trimIndex = parentSIIndex;
      }
      
      if ( (nextKeyStr.indexOf(UNITS_XPATH) == 0) && (trimIndex > 0)
                              && (nextKeyStr.indexOf(UNITS_EXCLUDE) < 0) ) {

        nextValObj = unitDictionaryNVPs.get(nextKeyStr);
        
        if (nextValObj==null) continue;
        
        nextValueStr = (String)nextValObj;
        
        if (nextValueStr.equals(UnitType)) {
        
          nextKeyStr = nextKeyStr.substring(0, trimIndex);
          returnList.add((String)(unitDictionaryNVPs.get(
                                                nextKeyStr + UNITS_NAME_ATT)));
        }
      }
    }
    String[] returnArray = new String[returnList.size()];

    returnArray = (String[])(returnList.toArray(returnArray));
    Arrays.sort(returnArray);
    return returnArray;
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

