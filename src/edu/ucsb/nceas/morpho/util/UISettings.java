/**
 *  '$RCSfile: UISettings.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-28 06:14:11 $'
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

package edu.ucsb.nceas.morpho.util;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;

/**
 *  This is a class containing static methods and attributes that define global 
 *  display settings in Morpho, which can't be represented directly by string 
 *  values from config.xml (such as Fonts, colors etc)
 *
 *  N O T E :
 *
 *    currently, this class also contains values that *could* be represented by 
 *    strings in a config file - this is an interim step - one step better than 
 *    hard-coding them in the UI classes, but should still be replaced by calls 
 *    to a config file, ultimately.  Similarly, colors and fonts could be  
 *    represented in String form in a config file (eg colors as red/green/blue 
 *    values etc).  When that has been established, this class will serve as a 
 *    resource for pulling the RGB values and creating the java.awt.Color 
 *    objects, instead of needing to do that in the various UI classes.
 */
public class UISettings
{
    // * * * *  D E F A U L T   F O N T S  &  T E X T - C O L O R S   * * * * * 

    //                                       "null" means use default font...
    public static final Font SUBPANEL_TITLE_FONT   
                                          = new Font("Dialog", Font.BOLD, 12);
    public static final Font BUTTON_FONT  = new Font("Dialog", Font.BOLD, 11);
    public static final Font TITLE_CITATION_FONT 
                                          = new Font("Dialog", Font.PLAIN, 12);
    public static final Font TITLE_LOCATION_FONT 
                                          = new Font("Dialog", Font.PLAIN, 9);
                                          
    public static final Color TITLE_TEXT_COLOR        = Color.white;
    public static final Color BACKBUTTON_TEXT_COLOR   = new Color(0, 198, 255);
    public static final Color CLOSEBUTTON_TEXT_COLOR  = BACKBUTTON_TEXT_COLOR;
    public static final Color EDITBUTTON_TEXT_COLOR   = new Color(0, 255, 0);
    public static final Color ALERT_TEXT_COLOR        = Color.red;
    
    // * * * * * *  D E F A U L T   D I M E N S I O N S  * * * * * * * * * * * * 
    
    public  static final int FOCUS_BORDER_WIDTH       = 4;

    private static final int TITLEBAR_HEIGHT          = 27;
    public static final int TITLEBAR_TOP_PADDING      = 0;
    public static final int TITLEBAR_SIDES_PADDING    = 0;
    public static final int TITLEBAR_BOTTOM_PADDING   = 2;

    public static final int PATHBAR_TOP_PADDING       = 2;
    public static final int PATHBAR_SIDES_PADDING     = 0;
    public static final int PATHBAR_BOTTOM_PADDING    = 2;
    
                                           
    //ignored by Borderlayout, but needed to create Dimension:
    private static final int DUMMY_WIDTH              = 100;

    public static final Dimension TITLEBAR_DIMS 
                                  = new Dimension(DUMMY_WIDTH,TITLEBAR_HEIGHT);
    public static final Dimension HEADER_BOTTOMLINE_DIMS 
                                  = new Dimension(DUMMY_WIDTH, 2);
    public static final int TITLEBAR_COMPONENT_HEIGHT 
                                  = TITLEBAR_HEIGHT - TITLEBAR_TOP_PADDING 
                                                    - TITLEBAR_BOTTOM_PADDING;
        
    // * * * * *  D E F A U L T   C O M P O N E N T   C O L O R S  * * * * * * *
    
    public static final Color TITLEBAR_COLOR       = Color.gray;
    public static final Color BACKGROUND_COLOR     = Color.lightGray;
    public static final Color BOTTOMLINE_COLOR     = Color.darkGray;
    public static final Color BACKBUTTON_COLOR     = TITLEBAR_COLOR;
    public static final Color CLOSEBUTTON_COLOR    = TITLEBAR_COLOR;
    public static final Color EDITBUTTON_COLOR     = TITLEBAR_COLOR;

    public static final Color FOCUSED_BORDER_COLOR = new Color(115,147,196);
    public static final Color UNFOCUSED_BORDER_COLOR = Color.gray;
    
    public static final Color NONEDITABLE_BACKGROUND_COLOR  
                                                  = new Color(237, 237, 237);
    
    /**
     *  private constructor - no instantiation, since all methods static
     */
    private UISettings() {}

}