/**
 *  '$RCSfile: HelpCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-13 01:09:53 $'
 * '$Revision: 1.7 $'
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

package edu.ucsb.nceas.morpho.framework;

import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Point;
import java.awt.Window;
import java.net.URL;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.JFrame;

/**
 * Class to handle help command
 */
public class HelpCommand implements Command
{
  // Main HelpSet & Broker
  private HelpSet mainHS = null;
  private HelpBroker mainHB = null;
  private ActionListener helpListener = null;
  // Defaults for Main Help
  static final String helpsetName = "morpho";
  private String id = null;
  private String errorMessage = null;
  // Size of help window
  public static int helpWidth = 800;
  public static int helpHeight= 520;
  public static Dimension size = new Dimension(helpWidth, helpHeight);
  // Location of help window
  private int helpCenterX = caculateCenterX(0, UISettings.CLIENT_SCREEN_WIDTH,
                                            helpWidth );
  private int helpCenterY = caculateCenterY(0, UISettings.CLIENT_SCREEN_HEIGHT,
                                            helpHeight);
  private Point location = new Point(helpCenterX, helpCenterY);

  /**
   * Constructor of HelpCommand
   */
  public HelpCommand()
  {
    try
    {
	    ClassLoader cl = HelpCommand.class.getClassLoader();
	    URL url = HelpSet.findHelpSet(cl, helpsetName);
	    mainHS = new HelpSet(cl, url);
	  }
    catch (Exception ee)
    {
	    errorMessage = "Help Set "+helpsetName+" not found!";
      Log.debug(30, errorMessage);
	    return;
	  }
    catch (ExceptionInInitializerError ex)
    {
	    Log.debug(30, "initialization error:");
	    ex.getException().printStackTrace();
	  }

    mainHB = mainHS.createHelpBroker();
    mainHB.setSize(size);
    mainHB.setLocation(location);
    helpListener = new CSH.DisplayHelpFromSource(mainHB);
  }//Constructor

  /**
   * Constructor of HelpCommand with string id
   */
  public HelpCommand(String id){
    this();
    this.id = id;
  }//Constructor

  /**
   * execute help command
   */
  public void execute(ActionEvent event)
  {
    if (helpListener != null)
    {
       MorphoFrame parent = UIController.getInstance().getCurrentActiveWindow();
       // make sure the morphoFrame is not null
      if ( parent != null )
      {
        // Make the help window is in the center of parent frame
        int parentWidth = parent.getWidth();
        int parentHeight = parent.getHeight();
        double parentX = parent.getLocation().getX();
        double parentY = parent.getLocation().getY();
        helpCenterX = caculateCenterX(parentX, parentWidth, helpWidth);
        helpCenterY = caculateCenterY(parentY, parentHeight, helpHeight);
        location.setLocation(helpCenterX, helpCenterY);
        mainHB.setLocation(location);
        if(id != null) { mainHB.setCurrentID(id); }
      }//if
      helpListener.actionPerformed(event);
    }//if
    else
    {
      Log.debug(5, errorMessage);
    }
  }//execute

  /* Method to child frame X value if the child frame is in the center of
     parent frame */
  public static int caculateCenterX(double parentX, int parentWidth,
                                     int childWidth)
  {
    double parentCenterX = parentX + 0.5 * parentWidth;
    int childCenterX =(new Double(parentCenterX - 0.5 * childWidth)).intValue();
    return childCenterX;
  }

  /* Method to child frame Y value if the child frame is in the center of
     parent frame */
  public static int caculateCenterY(double parentY, int parentHeight,
                                     int childHeight)
  {
    double parentCenterY = parentY + 0.5 * parentHeight;
    int childCenterY=(new Double(parentCenterY - 0.5 * childHeight)).intValue();
    return childCenterY;
  }

  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class CancelCommand
