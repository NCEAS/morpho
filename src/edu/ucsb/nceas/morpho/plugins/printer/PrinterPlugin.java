/**
 *  '$RCSfile: PrinterPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2003-12-19 01:44:02 $'
 * '$Revision: 1.3 $'
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

package edu.ucsb.nceas.morpho.plugins.printer;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;


import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.PrinterInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;

import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Log;




/**
 *  Plugin that provides a display panel to display HTML or plain text and
 *	allows the user to print the document 
 */
 
public class PrinterPlugin implements       PrinterInterface,
																						PluginInterface,
                                            ServiceProvider
{
	 
	 private Morpho instanceOfMorpho;
	 private final ClassLoader classLoader;
	 
	 public PrinterPlugin() 
	 {
		 	classLoader = Morpho.class.getClassLoader();
      Thread t = Thread.currentThread();
      t.setContextClassLoader(classLoader);
			
	 }
				
	 /**
     *  Required by PluginInterface; called automatically at runtime
     *
     *  @param morpho    a reference to the <code>Morpho</code>
     */
    public void initialize(Morpho morpho)
    {
				this.instanceOfMorpho = morpho;
				initializeActions();
        try 
        {
          ServiceController services = ServiceController.getInstance();
          services.addService(PrinterInterface.class, this);
          Log.debug(50, "Service added: PrinterInterface.");
        } 
        catch (ServiceExistsException see)
        {
          Log.debug(1, "PrinterService registration failed: PrinterInterface");
          Log.debug(6, see.toString());
        }
		}


		private void initializeActions() 
		{		
				
				UIController controller = UIController.getInstance();
				
				// Action for search
				GUIAction displayItemAction = new GUIAction("View/Print Metadata",
							null, new PrintCommand(this.instanceOfMorpho, this));
				displayItemAction.setToolTipText("Display and/or Print MetaData");
				displayItemAction.setMenuItemPosition(4);
				displayItemAction.setMenu("File", 0);
				displayItemAction.setEnabled(false);  //default
				controller.addGuiAction(displayItemAction);
				displayItemAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME, 
                            true, GUIAction.EVENT_LOCAL);
				displayItemAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME, 
                            false, GUIAction.EVENT_LOCAL);
				
		}
		
		private static String stripHTMLMetaTags(String html)
		{
			
			int META_END = 0;
			final int META_START = html.indexOf("<META");
			
			if (META_START>=0)  {
				final char[] htmlChars = html.toCharArray();
				int charIndex = META_START;
				char nextChar = ' ';
			
				do {
					nextChar = htmlChars[charIndex];
					htmlChars[charIndex] = ' ';
					charIndex++;
				} while ((nextChar!='>') && (charIndex < htmlChars.length));
				
				html = String.valueOf(htmlChars);
				return stripHTMLMetaTags(html);
				
			}
			return html;
			
		}
		
		private static String stripComments(String html) 
		{
			
			int prev = 0;
			String res = ""; 
			int pos = html.indexOf("<!--");
			if(pos < 0) return html;
			
			while(pos>=0) {
				 
				res += html.substring(prev, pos);
				html = html.substring(pos);
				int next = html.indexOf("-->");
				prev = 0;
				html = html.substring(next+3);
				pos = html.indexOf("<!--");
				
			}
			res += html;
			
			return res;
		}
		
		private String appendTrailingSpace(String html)
		{
			int pos;
			pos = html.indexOf("<tail>");
			if(pos != -1)
				return html;
			pos = html.indexOf("</html>");
			if(pos == -1)
				return html;
			String init = html.substring(0, pos);
			init += "\n<tail>\n</tail>\n </html>\n";
			return init;
			
		}
		
		public void display( String displayString, String contentType) 
		{
			
			String ctype;
			
			if(displayString == null || displayString.trim().equals("")) {
				return;
			}
			
			if(contentType == null || contentType.trim().equals("")) {
				ctype = DEFAULT_CONTENT_TYPE;
			} else {
				ctype = contentType;
			}
		  
			displayString = stripHTMLMetaTags(displayString);
			displayString = stripComments(displayString);
			
			displayString = appendTrailingSpace(displayString);
			
			new PrintFrame(displayString, ctype);
			
		}
}
		
		
