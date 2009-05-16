/**
 *  '$RCSfile: MorphoGuideCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-16 00:05:37 $'
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
package edu.ucsb.nceas.morpho.framework;


import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.swing.JFrame;

//import com.adobe.acrobat.Viewer;
//import com.sun.pdfview.PDFViewer;

import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;

/**
 * This class represents a command to open morpho guide. 
 * This command will be used in help menu. 
 * We will use Jpedal library to open a pdf file.
 * @author tao
 *
 */
public class MorphoGuideCommand implements Command 
{
	
	private JFrame frame = null;
	private int helpCenterX = HelpCommand.caculateCenterX(0, UISettings.CLIENT_SCREEN_WIDTH,
             HelpCommand.helpWidth );
    private int helpCenterY = HelpCommand.caculateCenterY(0, UISettings.CLIENT_SCREEN_HEIGHT,
             HelpCommand.helpHeight);
    private Point location = new Point(helpCenterX, helpCenterY);
	private static final String TITLE = "Morpho Help";
	private static final String GUIDEFILEPATH = "docs"+File.separator+"user"+File.separator+"MorphoUserGuide.pdf";
	private static final String WINDOWS = "rundll32 url.dll,FileProtocolHandler "+GUIDEFILEPATH;
	private static final String MAC = "open "+GUIDEFILEPATH;
	private static final String LINUX = " "+GUIDEFILEPATH;
	private static final String[] pdfApplications = {"evince", "acroread", "okular", "xpdf", "kpdf", "epdfview", "gv"};
	
	/**
	 * Default constructor
	 */
	public MorphoGuideCommand()
	{
	  
	}
	
	
	/**
	   * execute opening Morpho Guide command
	   */
	  public void execute(ActionEvent event) 
	  {
		  
		  String os = System.getProperty("os.name");
		  String command = null;
	      if (os.startsWith("Windows"))
	      {
	          command = WINDOWS;
	      }
	      else if (os.startsWith("Mac OS"))
	      {
	          command = MAC;
	       }
	       else
	       {
	          // os is either unix or linux          
	          for (int i = 0; i < pdfApplications.length ; i++)
	          {
	        	 
	        	try
	        	{
	              if (Runtime.getRuntime().exec(new String[]{"which", pdfApplications[i]}).waitFor() == 0)
	              {
	                command = pdfApplications[i]+LINUX;
	                break;
	              }
	        	}
	        	catch(Exception e)
	        	{
	        		Log.debug(50, "Couldn't find the pdf viewer");
	        	}
	          }
	        }
		    
	        try
	        {
	        	//open the pdf using external application.
	        	Runtime.getRuntime().exec(command);
	        	
	        }
	        catch(Exception e)
	        {
	        	//run PDFRender as backup
		       displayPDFByJavaLib();
	        }
	     
		    
	        
	   
	  }//execute
	  
	  /*
	   * If we couldn't run the default OS pdf viewer, we will run PDFRenderer to display the file.
	   * We only run it as a backup
	   */
	  private void displayPDFByJavaLib()
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
		        helpCenterX = HelpCommand.caculateCenterX(parentX, parentWidth, HelpCommand.helpWidth);
		        helpCenterY = HelpCommand.caculateCenterY(parentY, parentHeight, HelpCommand.helpHeight);
		        location.setLocation(helpCenterX, helpCenterY);		   
		        //frame.setLocation(location);
	       }
	      
	      
	      PDFViewer viewer;
		    boolean useThumbs = true;
         viewer = new PDFViewer(useThumbs);
         //viewer.setSize(UISettings.CLIENT_SCREEN_WIDTH/2, UISettings.CLIENT_SCREEN_HEIGHT/2);
         try
         {
           viewer.openFile(new File(GUIDEFILEPATH));
           viewer.setTitle(TITLE);
           viewer.setLocation(location);
         }
         catch(Exception e)
         {
         	Log.debug(5, "Couldn't open the pdf file"+e.getMessage());
         }
	  }

}
