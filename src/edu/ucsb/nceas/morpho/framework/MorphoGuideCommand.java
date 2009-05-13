/**
 *  '$RCSfile: MorphoGuideCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-13 01:00:08 $'
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;

import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.SimpleViewer;

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
		  /*MorphoFrame parent = UIController.getInstance().getCurrentActiveWindow();
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
		        frame.setLocation(location);	      
	        }*/
	       
	        /*Container content = frame.getContentPane();*/
	        /*SimpleViewer viewer = new SimpleViewer();
	        viewer.exitOnClose = false;
	        viewer.setupViewer(GUIDEFILEPATH);*/
	        JPedalFrame frame = new JPedalFrame(GUIDEFILEPATH);
	        
	        
	   
	  }//execute

}
