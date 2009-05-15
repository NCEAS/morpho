/**
 *  '$RCSfile: MorphoGuideCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-15 00:42:56 $'
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
package edu.ucsb.nceas.morpho.framework;


import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
	private static final String GUIDEFILEPATH = "docs/user/MorphoUserGuide.pdf";
	
	/**
	 * Default constructor
	 */
	public MorphoGuideCommand()
	{
	    //Create display frame
	    /*frame = new JFrame();
	    frame.setTitle(TITLE);
	    frame.setSize(HelpCommand.size);
	    frame.setLocation(location);*/
	}
	
	
	/**
	   * execute opening Morpho Guide command
	   */
	  public void execute(ActionEvent event) 
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
          
	       /* try
	        {
		        Viewer viewer = new Viewer();
		        frame.add(viewer, BorderLayout.CENTER);
	        	
		        InputStream input =
		            new FileInputStream (new File(GUIDEFILEPATH));
		        viewer.setDocumentInputStream(input);
		        viewer.setProperty("Default_Page_Layout", "SinglePage");
				viewer.setProperty("Default_Zoom_Type", "FitPage");
				viewer.setProperty("Default_Magnification", "100");
				viewer.zoomTo(2.0);
				viewer.activate();
				frame.setSize(UISettings.CLIENT_SCREEN_WIDTH/2, UISettings.CLIENT_SCREEN_HEIGHT/2);
				frame.setVisible(true);

	        }
	        catch (Exception e)
	        {
	        	Log.debug(5, "Couldn't open the pdf file"+e.getMessage());
	        }*/
            
	        // SimpleViewer in Jpedal to display pdf
	        /*Container content = frame.getContentPane();
	        SimpleViewer viewer = new SimpleViewer();
	        viewer.exitOnClose = false;
	        Object[] input = new Object[]{GUIDEFILEPATH};
	        viewer.executeCommand(Commands.OPENFILE, input);*/
	        // JPedalFrame to display pdf
	        /*JPedalFrame PDFframe = new JPedalFrame(GUIDEFILEPATH);
	        PDFframe.setLocation(location);	
	        PDFframe.setSize(UISettings.CLIENT_SCREEN_WIDTH/2, UISettings.CLIENT_SCREEN_HEIGHT/2);*/
	        
		    
	        
	   
	  }//execute

}
