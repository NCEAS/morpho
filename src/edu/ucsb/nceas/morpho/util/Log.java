/**
 *  '$RCSfile: Log.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-16 23:19:50 $'
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

import javax.swing.JOptionPane;

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;

/**
 * The Log is a utility class for logging messages to stdout, stderr,
 * a file, or a dialog box.  By default you can call just the static method
 * Log.debug() to log messages.  If you want to turn debugging off or 
 * change the default severity, use Log.getLog() to get the Log instance, 
 * and then call setDubug() and setDebugLevel(), respectively. 
 */
public class Log
{
  private static Log log = null;
  private static boolean debug = true;
  private static int debugLevel = 9;

  /**
   * Creates a new instance of Log. Private because we don't want it to
   * be called because this is a singleton.
   */
  private Log()
  {
  }

  /**
   * Get the single instance of the Log
   *
   * @return a pointer to the single instance of the Log
   */ 
  public static Log getLog() {
    
    if (log==null) { 
      log = new Log(); 
    }
    return log;
  }
 
  /**
   * Turn debugging on or off
   */
  public static void setDebug(boolean shouldDebug)
  {
      debug = shouldDebug;
      if (debug) {
          debug(20, "Debugging turned on");
      } else {
          debug(20, "Debugging turned off");
      }
  }

  /**
   * Set the threshold severity for debugging output
   */
  public static void setDebugLevel(int level)
  {
        debugLevel = level;
        debug(20, "Debug level set to: " + debugLevel);
  }

  /**
   * Print debugging messages based on severity level, where severity level 1
   * are the most critical and higher numbers are more trivial messages.
   * Messages with severity 1 to 4 will result in an error dialog box for the
   * user to inspect.  Those with severity 5-9 result in a warning dialog
   * box for the user to inspect.  Those with severity greater than 9 are
   * printed only to standard error.
   * Setting the debugLevel to 0 in the configuration file turns all messages
   * off.
   *
   * @param severity the severity of the debug message
   * @param message the message to log
   */
  public static void debug(int severity, String message)
  {
    if (debug) {
      if (debugLevel > 0 && severity <= debugLevel) {
        // Show a dialog for severe errors
    	  MorphoFrame frame = null;
    	  if (severity < 10) {
	    	  try {
	    		  frame = UIController.getInstance().getCurrentActiveWindow();
	    	  } catch (Exception e) {
	    		  System.err.println("No active window for logging severe messages");
			  }
    	  }
        if (severity < 5) {
          JOptionPane.showMessageDialog(frame, message, "Error!",
                                        JOptionPane.ERROR_MESSAGE);
        } else if (severity < 10) {
          JOptionPane.showMessageDialog(frame, message, "Warning!",
                                        JOptionPane.WARNING_MESSAGE);
        }

        // Everything gets printed to standard error
        System.err.println(message);
      }
    }
  } 
}
