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
 *     '$Date: 2003-09-17 00:35:44 $'
 * '$Revision: 1.13 $'
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

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComponent;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;

/**
 *  WizardSettings
 *
 */

public class WizardSettings {

  
  private static String summaryText;
  private static String dataLocation;

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
  
}

