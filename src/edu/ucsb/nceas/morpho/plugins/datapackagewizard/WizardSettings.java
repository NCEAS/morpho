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
 *     '$Date: 2003-08-06 05:44:56 $'
 * '$Revision: 1.6 $'
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


  protected static final int WIZARD_X_COORD = 100;

  protected static final int WIZARD_Y_COORD = 100;

  protected static final int WIZARD_WIDTH   = 800;

  protected static final int WIZARD_HEIGHT  = 600;
  

  protected static final String FIRST_PAGE_ID = WizardPageLibrary.INTRODUCTION;
  
  protected static final Color TOP_PANEL_BG_COLOR = new Color(11,85,112);
  
  // x-dimension is ignored:
  protected static final Dimension TOP_PANEL_DIMS = new Dimension(100,60);
  
  protected static final int PADDING = 5;

  protected static final Dimension DEFAULT_SPACER_DIMS
                                      = new Dimension(15, 15);

  
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
  protected static final  Dimension WIZARD_CONTENT_SINGLE_LINE_DIMS  
                                      = new Dimension(2000,20);

                                      
  public static final  Dimension LIST_BUTTON_DIMS  
                                      = new Dimension(100,30);
                                      
  protected static final String FINISH_BUTTON_TEXT  = "Finish";
  
  protected static final String NEXT_BUTTON_TEXT    = "Next >";
  
  protected static final String PREV_BUTTON_TEXT    = "< Back";
  
  protected static final String CANCEL_BUTTON_TEXT  = "Cancel";

}

