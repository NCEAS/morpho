/**
 *  '$RCSfile: HelpCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-11-21 01:30:52 $'
 * '$Revision: 1.1 $'
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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;

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
  static final String helpsetName = "HolidayHistory";
  private String errorMessage = null;
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
    helpListener = new CSH.DisplayHelpFromSource(mainHB);
  }//CancelCommand
  
  
  /**
   * execute help command
   */    
  public void execute(ActionEvent event)
  {
    if (helpListener != null)
    {
      helpListener.actionPerformed(event);
    }
    else
    {
      Log.debug(5, errorMessage);
    }
  }//execute

  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
