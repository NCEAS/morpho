/**
 *  '$RCSfile: PrinterPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-04-05 22:00:30 $'
 * '$Revision: 1.4 $'
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
import edu.ucsb.nceas.morpho.util.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;

import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.print.*;
import java.awt.event.ActionEvent;
import java.awt.Dimension;



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
	
	/** Constant to indicate a separator should precede an action */
	private static String SEPARATOR_PRECEDING = "separator_preceding";
	/** Constant to indicate a separator should follow an action */
	private static String SEPARATOR_FOLLOWING = "separator_following";
	
	private Dimension PRINT_FRAME_DIMENSION = new Dimension(800, 600);  
	private static PageFormat pageFormat = null;
	
	public PrinterPlugin() 
	 {
		 	classLoader = Morpho.class.getClassLoader();
      Thread t = Thread.currentThread();
      t.setContextClassLoader(classLoader);
			PrinterJob job = PrinterJob.getPrinterJob();
			pageFormat = job.defaultPage();
			
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
				
				// Action for Page setup
				GUIAction pageSetupAction = new GUIAction("Page setup...",
							null, new PageSetupCommand());
				pageSetupAction.setMenuItemPosition(5);
				pageSetupAction.setSeparatorPosition(SEPARATOR_PRECEDING);
				pageSetupAction.setMenu("File", 0);
				pageSetupAction.setEnabled(false);  //default
				controller.addGuiAction(pageSetupAction);
				pageSetupAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME, 
                            true, GUIAction.EVENT_LOCAL);
				pageSetupAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME, 
                            false, GUIAction.EVENT_LOCAL);
														
				// Action for preview
				GUIAction previewAction = new GUIAction("Print preview...",
							null, new PreviewCommand(this.instanceOfMorpho, this));
				previewAction.setMenuItemPosition(6);
				previewAction.setMenu("File", 0);
				previewAction.setEnabled(false);  //default
				controller.addGuiAction(previewAction);
				previewAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME, 
                            true, GUIAction.EVENT_LOCAL);
				previewAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME, 
                            false, GUIAction.EVENT_LOCAL);
				
				// Action for Print
				GUIAction printAction = new GUIAction("Print...",
							null, new PrintCommand(this.instanceOfMorpho, this));
				printAction.setMenuItemPosition(7);
				
				printAction.setMenu("File", 0);
				printAction.setEnabled(false);  //default
				controller.addGuiAction(printAction);
				printAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME, 
                            true, GUIAction.EVENT_LOCAL);
				printAction.setEnabledOnStateChange(
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
		  displayString = processHTMLString(displayString);
			PrintFrame frame = createFrame(displayString, ctype, PRINT_FRAME_DIMENSION);
			frame.setPageFormat(pageFormat);
		}
		
		
		public void print( String displayString, String contentType) 
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
		  displayString = processHTMLString(displayString);
			PrintFrame frame = createFrame(displayString, ctype, new Dimension(1, 1));
			frame.setPageFormat(pageFormat);
			frame.print();
			frame.dispose();
			
		}
		
		private String processHTMLString(String displayString) {
			displayString = stripHTMLMetaTags(displayString);
			displayString = stripComments(displayString);
			displayString = appendTrailingSpace(displayString);
			return displayString;
		}
		
		private PrintFrame createFrame(String displayString, String ctype, Dimension dims) {
			
			return new PrintFrame(displayString, ctype, dims);
		}
		
		
		public static void setPageFormat(PageFormat pf) {
			
			pageFormat = pf;
		}
		
		public static PageFormat getPageFormat() {
			return pageFormat;
		}
}

class PageSetupCommand implements Command {
	
	PageSetupCommand() {
		
	}
	
	public void execute(ActionEvent event) {
		
		PageFormat format = PrinterPlugin.getPageFormat();
		PrinterJob job = PrinterJob.getPrinterJob();
		format = job.pageDialog(format);
		PrinterPlugin.setPageFormat(format);
		
	}
}

	
