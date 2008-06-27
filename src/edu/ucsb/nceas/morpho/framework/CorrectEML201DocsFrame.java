/**
 *  '$RCSfile: CorrectEML201DocsFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-06-27 00:03:03 $'
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import edu.ucsb.nceas.morpho.util.Log;

/**
 * GUI Window notifies users that morpho is correcting invalid eml 201 documents while
 * it is correcting documents. The details of invalid eml 201 documents is on: 
 * http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3239
 * @author tao
 *
 */
public class CorrectEML201DocsFrame extends JFrame 
{
	private static boolean correctionWasDone = false;
	private String[] docList = null;
	private static final String TITLE = "Correcting InValid EML 2.0.1 Document";
	
	/**
	 * Default Constructor
	 *
	 */
	public CorrectEML201DocsFrame()
	{
	    this.correctionWasDone = getCorrectionFlag();	
	    if (!correctionWasDone)
	    {
	        loadGUI();
	    }
	}
	
	/*
	 * Load GUI of this frame. The GUI is very simple: A label and a porgress bar.
	 */
	private void loadGUI()
	{
		   this.setTitle(TITLE);
		   setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
	        getContentPane().setLayout(
                 new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		   getContentPane().setBackground(java.awt.Color.white);		  
		
		   getContentPane().add(Box.createVerticalStrut(8));
		   javax.swing.JLabel loadingLabel = new javax.swing.JLabel();
		   loadingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		   loadingLabel.setText("Morpho is correcting invalid EML 2.0.1 documents...");
		   loadingLabel.setForeground(java.awt.Color.red);
		   loadingLabel.setFont(new Font("Dialog", Font.BOLD, 14));
		   getContentPane().add(loadingLabel);
		   getContentPane().add(Box.createVerticalStrut(8));
		   JProgressBar progBar = new JProgressBar();
		   progBar.setIndeterminate(true);
		    getContentPane().add(progBar);		    
		    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		    Rectangle frameDim = getBounds();
		    setLocation((screenDim.width - frameDim.width) / 2,
		                (screenDim.height - frameDim.height) / 2);
		    pack();
		    setVisible(true);
	}
	
	/*
	 * Get the flag from configuration file. This flag indicates if the local eml 201 documents
	 * have been corrected or not.
	 */
	private boolean getCorrectionFlag()
	{
		boolean flag = false;
		return flag;
	}
	
	/*
	 * Set the flag to true when the correction is done.
	 */
	private void setCorrectionFlagTrue()
	{
		
	}
	
	/*
	 * Get the local file list. The list may include non-xml file - data file.
	 * If nothing be found, null will be returned.
	 */
	private String[] getLocalDocList()
	{
		String[] list = {"hello"};
		return list;
	}
	
	/**
	 * Do the correction job for given doc list.
	 */
	public void doCorrect()	
	{
        this.docList = getLocalDocList();
		if (!correctionWasDone && docList != null )
		{
		
				long i = 1000000000;
				 while(i !=-1000000000)
			     {
			      	  i--;
			      	  //System.out.println("here");
			     }
				 // Set the "correctionWasDone" flag to true
				 setCorrectionFlagTrue();
			
		}
		
		//Dipose the frame
		if (this != null)
		{
			this.dispose();
		}
	}


}
