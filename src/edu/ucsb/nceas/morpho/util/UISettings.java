/**
 *  '$RCSfile: UISettings.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-04-10 02:21:49 $'
 * '$Revision: 1.21 $'
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
import java.awt.Image;
import java.awt.Insets;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.Icon;
import javax.swing.ImageIcon;

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

    private static final Dimension scrDim
                                  = Toolkit.getDefaultToolkit().getScreenSize();

    // * * * * * *  D E F A U L T   D I M E N S I O N S  * * * * * * * * * * * *


    /**
     *  overall screen width (in pixels)
     */
    public static final int CLIENT_SCREEN_WIDTH       = (int)scrDim.getWidth();

    /**
     *  overall screen height (in pixels)
     */
    public static final int CLIENT_SCREEN_HEIGHT      = (int)scrDim.getHeight();

    /**
     *  a guess at how high most taskbars will be (in pixels)
     */
    public static final int TASKBAR_HEIGHT = 100;

    /**
     *  overall width (pixels) of MorphoFrame (after making allowance
     *  for client screen width etc)
     */
    public static final double DEFAULT_WINDOW_WIDTH     = getWindowWidth();

    /**
     *  overall height (pixels) of MorphoFrame (after making allowance
     *  for client screen height, taskbar etc)
     */
    public static final double DEFAULT_WINDOW_HEIGHT    = getWindowHeight();


    public  static final int FOCUS_BORDER_WIDTH         = 4;

    private static final int TITLEBAR_HEIGHT            = 27;
    public static final int TITLEBAR_TOP_PADDING        = 0;
    public static final int TITLEBAR_SIDES_PADDING      = 0;
    public static final int TITLEBAR_BOTTOM_PADDING     = 2;

    public static final int PATHBAR_TOP_PADDING         = 2;
    public static final int PATHBAR_SIDES_PADDING       = 0;
    public static final int PATHBAR_BOTTOM_PADDING      = 2;

    public static final int WIZARD_PADDING      = 5;

    /**
     *  insets for nuttons in metaviewer - order is (TOP, LEFT, BOTTOM, RIGHT)
     */
    public static final Insets METAVIEW_BUTTON_INSETS = new Insets(2,5,2,5);


    //ignored by Borderlayout, but needed to create Dimension:
    private static final int DUMMY_WIDTH                = 5000;

    public static final Dimension TITLEBAR_DIMS
                                  = new Dimension(DUMMY_WIDTH,TITLEBAR_HEIGHT);
    public static final Dimension HEADER_BOTTOMLINE_DIMS
                                  = new Dimension(DUMMY_WIDTH, 2);
    public static final int TITLEBAR_COMPONENT_HEIGHT
                                  = TITLEBAR_HEIGHT - TITLEBAR_TOP_PADDING
                                                    - TITLEBAR_BOTTOM_PADDING;

    public static final int TITLE_CITATION_HEIGHT       = 50;
    public static final Dimension TITLE_CITATION_DIMS
                                  = new Dimension(  DUMMY_WIDTH,
                                                    TITLE_CITATION_HEIGHT);

    public static final int TITLE_LOCATION_HEIGHT       = TITLE_CITATION_HEIGHT;
    public static final int TITLE_LOCATION_WIDTH        = 51;
    public static final Dimension TITLE_LOCATION_DIMS
                                  = new Dimension(  TITLE_LOCATION_WIDTH,
                                                    TITLE_CITATION_HEIGHT);

    public static final int VERT_SPLIT_INIT_LOCATION  = 2+TITLE_CITATION_HEIGHT;


    // dims for MorphoFrame
    public static final int MAX_WINDOW_WIDTH            = 1024;
    public static final int MAX_WINDOW_HEIGHT           = 768;

    /**
     *  How many pixels to leave at each edge of initial, centered screen?
     */
    public static final int WINDOW_CASCADE_X_OFFSET     = 25;
    public static final int WINDOW_CASCADE_Y_OFFSET     = 25;

    /**
     *  # pixels padding around & between the 3 panels on the left side of the
     *  initial screen
     */
    public static final int INIT_SCRN_LEFT_PANELS_PADDING  = 15;


    /**
     *  size of the "Change Profile" and "Password" labels on the left side of
     *  the initial screen
     */
    public static final Dimension INIT_SCRN_LEFT_PANELS_LABELDIMS
                                                    = new Dimension(120, 20);

    /**
     *  size of the "Change Profile" and "Password" picklists (JComboBoxes) on
     *  the left side of the initial screen
     */
    public static final Dimension INIT_SCRN_LEFT_PANELS_PICKLISTDIMS
                                                    = new Dimension(130, 20);


    /**
     *  size of the hyperlink buttons on the left side of the initial screen
     */
    public static final Dimension INIT_SCR_LINKBUTTON_DIMS
                                                    = new Dimension(280, 20);




    /**
     *  Width in pixels for each of 3 panels on the left side of initial screen
     */
    public static final int INIT_SCRN_LEFT_PANELS_WIDTH     = 340;

    public static final int INIT_SCRN_PROFILE_PANEL_HEIGHT  = 110;
    public static final int INIT_SCRN_LOGIN_PANEL_HEIGHT    = 120;
    public static final int INIT_SCRN_DATA_PANEL_HEIGHT     = 115;

    /**
     *  Dims in pixels for each of 3 panels on the left side of initial screen
     */
    public static final Dimension INIT_SCRN_LEFT_PANELS_TITLE_DIMS
                            = new Dimension(INIT_SCRN_LEFT_PANELS_WIDTH,18);




    public static final int WIZARD_X_COORD = 100;

    public static final int WIZARD_Y_COORD = 100;

    public static final int WIZARD_WIDTH   = 800;

    public static final int WIZARD_HEIGHT  = 600;

    public static final int DIALOG_SMALLER_THAN_WIZARD_BY = 30;

    public static final int POPUPDIALOG_WIDTH
        = WIZARD_WIDTH - DIALOG_SMALLER_THAN_WIZARD_BY;

    public static final int POPUPDIALOG_HEIGHT
        = WIZARD_HEIGHT - DIALOG_SMALLER_THAN_WIZARD_BY;

    public static final int POPUPDIALOG_FOR_ATTR_HEIGHT
        = 30 + POPUPDIALOG_HEIGHT;



    // * * * *  D E F A U L T   F O N T S  &  T E X T - C O L O R S   * * * * *

    //                                       "null" means use default font...
    public static final Font SUBPANEL_TITLE_FONT
                                          = new Font("Dialog", Font.BOLD,  12);

    public static final Font BUTTON_FONT  = new Font("Sans-Serif", Font.BOLD,  11);

    public static final Font TITLE_CITATION_FONT
                                          = new Font("Dialog", Font.PLAIN, 12);

    public static final Font TITLE_CITATION_FONT_BOLD
                                          = new Font("Dialog", Font.BOLD,  12);

    public static final Font TITLE_LOCATION_FONT
                                          = new Font("Dialog", Font.PLAIN, 9);

    public static final Font POPUPDIALOG_BUTTON_FONT
                                      = new Font("Sans-Serif", Font.PLAIN,  11);


    /**
     *  html opening tags for font on hyperlink urls
     */
    public static final String HYPERLINK_FONT_HTML_OPENTAGS
                        = "<html><p style=\"color: #0000d2; align: left; "
                            +"font-family: Verdana, Arial, Helvetica, sans-serif; "
                            +"font-size: 9px;\">&nbsp;";
    /**
     *  html closing tags for font on hyperlink urls
     */
    public static final String HYPERLINK_FONT_HTML_CLOSETAGS
                                                    = "</p></html>";


    /**
     *  html opening tags for font on hyperlink urls during mouseover
     */
    public static final String HYPERLINK_FONT_OVER_HTML_OPENTAGS
                        = "<html><a href=\"#\" style=\"color: #ee5500; "
                            +"font-family: Verdana, Arial, Helvetica, sans-serif; "
                            +"font-size: 9px; align: left;\">&nbsp;";

    /**
     *  html closing tags for font on hyperlink urls during mouseover
     */
    public static final String HYPERLINK_FONT_OVER_HTML_CLOSETAGS
                                                    = "</a></html>";


    /**
     *  opening html tags for highlighted text in title bars on panel to the
     *  left of the initial screen
     */
    public static final String INIT_SCR_PANEL_TITLE_HILITE_FONT_OPEN
                    = "<font color=\"#d2ffad\"><b>";



    /**
     *  opening html tags for highlighted text in title bars on panel to the
     *  left of the initial screen
     */
    public static final String INIT_SCR_PANEL_TITLE_HILITE_FONT_CLOSE
                    = "</b></font>";

    /**
     *  opening html tags for light-value text in panels to the
     *  left of the initial screen
     */
    public static final String INIT_SCR_PANEL_LITE_FONT_OPEN
                    = "<html><p style=\"color: #666666; "
                        +"font-family: Verdana, Arial, Helvetica, sans-serif; "
                        +"font-size: 8px; align: left;\">";

    /**
     *  closing html tags for light-value text in panels to the
     *  left of the initial screen
     */
    public static final String INIT_SCR_PANEL_LITE_FONT_CLOSE
                    = "</p></html>";


    /**
     *  Color for text on title bars of sub-windows in datapackage view
     *  (i.e. the "spreadsheet" panel and the two MetaData Viewer panels)
     */
    public static final Color TITLE_TEXT_COLOR        = Color.white;

    /**
     *  Settings for MetaData Viewer panels
     */
    public static final Color BACKBUTTON_TEXT_COLOR
                                = verifyButtonTextColor(new Color(178,238,255));

    /**
     *  Settings for MetaData Viewer panels
     */
    public static final Color CLOSEBUTTON_TEXT_COLOR  = BACKBUTTON_TEXT_COLOR;

    /**
     *  Settings for MetaData Viewer panels
     */
    public static final Color EDITBUTTON_TEXT_COLOR
                                = verifyButtonTextColor(new Color(153,255,153));

    /**
     *  Settings for MetaData Viewer panels
     */
    public static final Color BUTTON_DISABLED_TEXT_COLOR
                                                      = new Color(204,204,204);


    public static final Color POPUPDIALOG_BUTTON_TEXT_COLOR
                                                       = new Color(51, 51, 51);


    /**
     *  General alert text (eg shown in data view if data file not readable)
     */
    public static final Color ALERT_TEXT_COLOR        = Color.red;


    // * * * * *  D E F A U L T   C O M P O N E N T   C O L O R S  * * * * * * *

    public static final Color TITLEBAR_COLOR       = new Color(117,117,117);
    public static final Color BACKGROUND_COLOR     = Color.lightGray;
    public static final Color BOTTOMLINE_COLOR     = Color.darkGray;
    public static final Color BACKBUTTON_COLOR     = TITLEBAR_COLOR;
    public static final Color CLOSEBUTTON_COLOR    = TITLEBAR_COLOR;
    public static final Color EDITBUTTON_COLOR     = TITLEBAR_COLOR;
    public static final Color CUSTOM_GRAY          = new Color(180,180,180);
    public static final Color FOCUSED_BORDER_COLOR = new Color(115,147,196);
    public static final Color UNFOCUSED_BORDER_COLOR = Color.gray;

    public static final Color NONEDITABLE_BACKGROUND_COLOR
                                                  = new Color(237, 237, 237);

    /**
     *  background color of the 3 panels on the left side of the initial screen
     */
    public static final Color INIT_SCRN_LEFT_PANELS_BG_COLOR = Color.white;

    /**
     *  background color of the title bars on the 3 panels to the left of the
     *  initial screen
     */
    public static final Color INIT_SCRN_LEFT_PANELS_TITLE_BG_COLOR
                                                      = new Color(11,85,112);

    /**
     *  background color of the main part of the initial screen
     */
    public static final Color INIT_SCRN_MAIN_BG_COLOR = new Color(0,153,203);



    // * * * * * *  D E F A U L T   I M A G E S   &   I C O N S  * * * * * * * *

    //IMAGES/////////////////
    public static final Image FRAME_AND_TASKBAR_ICON
            = getAsImage("/edu/ucsb/nceas/morpho/framework/Btfly16x16.gif");

    public static final Image INIT_SCR_BACKGROUND
            = getAsImage("/edu/ucsb/nceas/morpho/framework/InitScreenBG.jpg");


    //ICONS//////////////////
    public static final Icon NEW_DATAPACKAGE_ICON
            = getAsImageIcon("/toolbarButtonGraphics/general/New16.gif");

    public static final Icon NEW_DATAPACKAGE_ICON_ROLLOVER
            = getAsImageIcon("/toolbarButtonGraphics/general/New16.gif");

    public static final Icon OPEN_DATAPACKAGE_ICON
            = getAsImageIcon("/toolbarButtonGraphics/general/Open16.gif");

    public static final Icon OPEN_DATAPACKAGE_ICON_ROLLOVER
            = getAsImageIcon("/toolbarButtonGraphics/general/Open16.gif");

    public static final Icon SEARCH_ICON
            = getAsImageIcon("/toolbarButtonGraphics/general/Search16.gif");

    public static final Icon SEARCH_ICON_ROLLOVER
            = getAsImageIcon("/toolbarButtonGraphics/general/Search16.gif");

    public static final Icon REFRESH_ICON
            = getAsImageIcon("/toolbarButtonGraphics/general/Refresh16.gif");

    public static final Icon SAVE_ICON
            = getAsImageIcon("/toolbarButtonGraphics/general/Save16.gif");

    public static final Icon SAVE_QUERY_ICON
            = getAsImageIcon("/toolbarButtonGraphics/general/SaveAs16.gif");


    public static final Icon NEW_PROFILE_ICON
            = getAsImageIcon("/edu/ucsb/nceas/morpho/framework/profile.gif");

    public static final Icon NEW_PROFILE_ICON_ROLLOVER
            = getAsImageIcon("/edu/ucsb/nceas/morpho/framework/profile_OVER.gif");

    public static final Icon LOGOUT_ICON
            = getAsImageIcon("/edu/ucsb/nceas/morpho/framework/login_no.gif");

    public static final Icon LOGOUT_ICON_ROLLOVER
            = getAsImageIcon("/edu/ucsb/nceas/morpho/framework/login_no_OVER.gif");


    // * * * * * * * * * *    T E X T   L A B E L S    * * * * * * * * * * * * *



    /**
     *  html tags and text for title bar on the "Profile" panel to the left of
     *  the initial screen
     */
    public static final String INIT_SCRN_PANELS_PROFILE_TITLE_TEXT_OPEN
                    = "<html><p style=\"color: #ffffff; "
                        +"font-family: Verdana, Arial, Helvetica, sans-serif; "
                        +"font-size: 9px; align: left;\">&nbsp;"
                        +"Current profile:"
                        +"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                        +"&nbsp;&nbsp;";

    /**
     *  html tags and text for title bar on the "Login" panel to the left of
     *  the initial screen
     */
    public static final String INIT_SCRN_PANELS_LOGIN_TITLE_TEXT_OPEN
                    = "<html><p style=\"color: #ffffff; "
                        +"font-family: Verdana, Arial, Helvetica, sans-serif; "
                        +"font-size: 9px; align: left;\">&nbsp;"
                        +"Network Status:"
                        +"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                        +"&nbsp;";

    /**
     *  html tags and text for title bar on the "Data" panel to the left of
     *  the initial screen
     */
    public static final String INIT_SCRN_PANELS_DATA_TITLE_TEXT_OPEN
                    = "<html><p style=\"color: #ffffff; "
                        +"font-family: Verdana, Arial, Helvetica, sans-serif; "
                        +"font-size: 9px; align: left;\">&nbsp;"
                        +"Work with your data...";

    /**
     *  common to the above 3: closing html tags and text for title bar on the
     *  panel to the left of the initial screen
     */
    public static final String INIT_SCRN_PANELS_TITLE_CLOSE
                    = "</p></html>";

    public static final String NEW_DATAPACKAGE_LINK_TEXT
                    = "Create a <b>new</b> data package...";

    public static final String OPEN_DATAPACKAGE_LINK_TEXT
                    = "Open an <b>existing</b> data package...";

    public static final String SEARCH_LINK_TEXT
                    = "<b>Search</b> for an existing data package...";

    public static final String NEW_PROFILE_LINK_TEXT
                                    = "Create a new profile...";

    public static final String LOGOUT_LINK_TEXT
                                 = "<b>Logout</b> from network...";

    public static final String CHANGE_PROFILE_LABEL_TEXT
                    = "<html><p style=\"color: #000000; align: left; "
                        +"font-family: Verdana, Arial, Helvetica, sans-serif; "
                        +"font-size: 9px;\">&nbsp;"
                        +"<b>Change profile:</b></p></html>";


    public static final String PASSWORD_LABEL_TEXT
                    = "<html><p style=\"color: #000000; align: left; "
                        +"font-family: Verdana, Arial, Helvetica, sans-serif; "
                        +"font-size: 9px;\">&nbsp;"
                        +"Password:</p></html>";

    public static final String INIT_SCR_LOGIN_MESSAGE
                    = "If you do not choose to login, you will "
                        +"be able to access only \"public\" network "
                        +"files as a Guest User";

    public static final String INIT_SCR_LOGGED_IN_MESSAGE
                    = "You are logged into the network, and may "
                        +"work with all files for which you have "
                        +"access priviliges ";

    public static final String INIT_SCR_LOGIN_BUTTON_TEXT
                    = "<html><p style=\"color: #000000; "
                        +"font-family: Verdana, Arial, Helvetica, sans-serif; "
                        +"font-size: 9px; align: left;\">"
                        +"login</p></html>";

    public static final String INIT_SCR_LOGIN_HEADER
                    = "<html><p style=\"color: #000000; align: left; "
                        +"font-family: Verdana, Arial, Helvetica, sans-serif; "
                        +"font-size: 9px;font-weight: bold;\">"
                        +"Login to network using current profile:"
                        +"</p></html>";

    public static final String INIT_SCR_LOGGED_IN_STATUS = "Logged In";

    public static final String INIT_SCR_LOGGEDOUT_STATUS = "NOT Logged In";

    public static final String OK_BUTTON_TEXT = "OK";

    public static final String CANCEL_BUTTON_TEXT = "Cancel";



    private static long previousTimeStamp;
    /**
     *  gets a String id that is guaranteed to be unique within the current
     *  document (ie document scope). Note that the ID String is a timestamp in
     *  milliseconds, so all IDs generated by this method running on a given
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



    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    // determine default window size
    private static double getWindowWidth()
    {
        if (CLIENT_SCREEN_WIDTH >= MAX_WINDOW_WIDTH) {
            return MAX_WINDOW_WIDTH;
        } else {
            return CLIENT_SCREEN_WIDTH;
        }
    }

    private static double getWindowHeight()
    {
        if (CLIENT_SCREEN_HEIGHT >= MAX_WINDOW_HEIGHT) {
            return MAX_WINDOW_HEIGHT - TASKBAR_HEIGHT;
        } else {
            return CLIENT_SCREEN_HEIGHT - UISettings.TASKBAR_HEIGHT;
        }
    }

    /////////////////////////////////////////////


    /**
     *   If on mac OSX, changes the value of the color to make it more visible
     *   against the Mac light-grey buttons.
     *   @param proposedColor the proposedColor color
     *   @return either the same proposed color, if not on Mac OSX, or a darker
     *                  value version of the proposed Color for OSX
     */
    private static Color verifyButtonTextColor(Color proposedColor)
    {
        if (((System.getProperty("os.name")).toUpperCase()).indexOf("MAC")<0) {

            return proposedColor;

        } else {

            float[] hsb = new float[3];
            Color.RGBtoHSB(proposedColor.getRed(),proposedColor.getGreen(),
                                                  proposedColor.getBlue(),hsb);
            //increase saturation if less than 50%
            if (hsb[1]<=0.5f) hsb[1] =  1.0f;
            //decrease brightness if greater than 50%
            if (hsb[2]>0.5f) hsb[2] =  0.5f;

            return new Color(Color.HSBtoRGB(hsb[0],hsb[1],hsb[2]));
        }

    }



    /////////////////////////////////////////////

    private static Object cpLocator = null;

    // needs classpath-relative path string (i.e. starts with a "/")
    private static Image getAsImage(String path) {

        if (cpLocator==null) cpLocator = new Object();
        return new ImageIcon(cpLocator.getClass().getResource(path)).getImage();
    }

    // needs classpath-relative path string (i.e. starts with a "/")
    private static Icon getAsImageIcon(String path) {

        return new ImageIcon(getAsImage(path));
    }




    /**
     *  private constructor - no instantiation, since all methods static
     */
    private UISettings() {}

}
