/**
 *  '$RCSfile: CorrectEML201DocsFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-06-30 21:08:29 $'
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.EML201DocumentCorrector;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * GUI Window notifies users that morpho is correcting invalid eml 201 documents while
 * it is correcting documents. The details of invalid eml 201 documents is on: 
 * http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3239
 * This class will go through every existed profile in morpho data directory and 
 * correct every documents in it. This class will be called in main method of Morpho class.
 * @author tao
 *
 */
public class CorrectEML201DocsFrame extends JFrame 
{
	private Morpho morpho = null;
	private static final String TITLE = "Correcting InValid EML 2.0.1 Document";
	private static final String PROFILENAME = "profilename";
	private static ConfigXML orginalProfile = null;
	private Vector profilesStatusVector = new Vector();
	//private boolean hasCorrectionPath = false;
	
	
	
	/**
	 * Default Constructor
	 *
	 */
	public CorrectEML201DocsFrame(Morpho morpho)
	{
		this.morpho = morpho;
		if (morpho != null)
		{
			orginalProfile = morpho.getProfile();
		}
	    
	}
	
	
	/**
	 * Do the correction job for given doc list.
	 */
	public void doCorrection()
	{
		if (morpho != null)
		{
			ConfigXML profile =morpho.getProfile();
			boolean correctionNeeded = getCorrectionNeededStatusForMorpho();
			if (correctionNeeded)
			{
				  loadGUI();
			      for (int i=0; i<profilesStatusVector.size(); i++)
			      {
			    	   ProfileCorrectionStatus correctionStatus = (ProfileCorrectionStatus)profilesStatusVector.elementAt(i);
				       doCorrectionInOneProfile(correctionStatus);
		               //Dipose the frame
						if (this != null)
						{
							this.dispose();
						}
			      }
			}
			//set back to orginal profile.
			morpho.setProfileDontLogin(orginalProfile);
		}
	}
    
	/*
	 * Go through docid list from one profile and do the correction
	 */
	private void doCorrectionInOneProfile(ProfileCorrectionStatus correctionStatus)
	{   
		   if (correctionStatus != null && !correctionStatus.isEmlCorrected())
		   {
			   //Set morpho to a new profile
			    morpho.setProfileDontLogin(correctionStatus.getProfile());
			    //clean the Cache
			    morpho.cleanCache();
		        Vector docList = getOneProfileDocList(correctionStatus.getProfile());
		        //System.out.println("the size of doclist is "+docList.size());
				if (docList != null && !docList.isEmpty() )
				{
					for (int i=0; i<docList.size(); i++)
					{
						String docid = null;
						try
						{
					        docid = (String)docList.elementAt(i);
					        //System.out.println("before correction docid "+docid);
					        EML201DocumentCorrector corrector = new EML201DocumentCorrector(docid);
					        corrector.correctDocument();
					        Log.debug(30, "finish correcting docid(maybe skip) "+docid);
						}
						catch(Exception e)
						{
							Log.debug(30, "Couldn't correct docid "+ docid+ " since "+e.getMessage());
						}
					}
	     			 // Set the "correctionWasDone" flag to true
		     		 setCorrectionFlagTrue(correctionStatus);
				}
		   }
	}
	
	/*
	 * Load GUI of this frame. The GUI is very simple: a label and a porgress bar.
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
		   loadingLabel.setText("Verifying EML 2.0.1 documents. It may take a while...");
		   //loadingLabel.setForeground(java.awt.Color.red);
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
	 * Goes through every profile to see if we need do correction. If one profile hasn't
	 * been corrected, true will be returned.
	 */
	private boolean getCorrectionNeededStatusForMorpho()
	{
		boolean correctionNeeded = false;
		if (morpho != null)
		{
			//Gets every profile name
			String[] profileList = morpho.getProfilesList();
			if (profileList != null)
			{
				for (int i=0; i<profileList.length; i++)
				{
					String profileName = profileList[i];
					morpho.setProfileDontLogin(profileName);
					ProfileCorrectionStatus status = getCorrectionStatusForProfile(morpho.getProfile());
					profilesStatusVector.add(status);
					if (status.emlCorrected == false)
					{
						correctionNeeded = true;
					}
				}
			}
		}
		return correctionNeeded;
	}
	
	/*
	 * Get the flag from configuration file. This flag indicates if the local eml 201 documents
	 * have been corrected or not.
	 */
	private ProfileCorrectionStatus getCorrectionStatusForProfile(ConfigXML profile)
	{
		ProfileCorrectionStatus profileCorrectionStatus= null;
		boolean flag = true;
		boolean hasCorrectionPath = false;
		if(profile != null)
		{
			String wasCorrected = profile.get(ProfileDialog.CORRECTIONEMLPROFILEPATH, 0);
			if (wasCorrected != null)
			{
				  hasCorrectionPath = true;
			      flag = (new Boolean(wasCorrected)).booleanValue();
			}
			else
			{
				// There is no correction path in the profile. 
				hasCorrectionPath = false;
				//So the eml documents haven't been corrected.
				flag = false;
			}
			//String name = profile.get(PROFILENAME, 0);
			//System.out.println("The profile name is "+ name);
			//System.out.println("Is eml corrected "+ flag);
			//System.out.println("Does the profile has the eml201corrected path "+hasCorrectionPath);
			profileCorrectionStatus = new ProfileCorrectionStatus(profile, flag, hasCorrectionPath);
		}
		return profileCorrectionStatus;
	}
	
	/*
	 * Set the flag to true when the correction is done.
	 */
	private void setCorrectionFlagTrue(ProfileCorrectionStatus correctionStatus)
	{
		if (correctionStatus != null)
		{
			ConfigXML profile = correctionStatus.getProfile();
			if (profile != null)
			{
				if (correctionStatus.hasCorrectionPath())
				{
					//need to remove the path from the file first
		            profile.removeNode(ProfileDialog.CORRECTIONEMLPROFILEPATH, 0);
		            
				}
		         //append the correction path and value to the end.
				profile.insert(ProfileDialog.CORRECTIONEMLPROFILEPATH, "true");
				profile.save();
			}
	   }
	}
	
	/*
	 * Get the local file list. The list may include non-xml file - data file.
	 * If nothing be found, emply vector will be returned.
	 */
	private Vector getOneProfileDocList(ConfigXML profile)
	{		
		Vector list = new Vector();
		if (profile != null)
		{
		    String currentProfile = profile.get("profilename", 0);
		    String separator = profile.get("separator", 0);
		    ConfigXML config = morpho.getConfiguration();
		    String profileDir = config.getConfigDirectory() + File.separator +
		                       config.get("profile_directory", 0) + File.separator +
		                       currentProfile;
		    String datadir = profileDir + File.separator + profile.get("datadir", 0);
		    datadir = datadir.trim();
		    //System.out.println("the data dir is "+datadir);
		    File xmldir = new File(datadir);
		    
		    // get a list of all docids	    
		    getDocIds(xmldir, list, separator );
		}
		return list;
	}
	
	  /*
	    * given a directory, return a vector of docid it contains
	    * including subdirectories
	    */
	   private void getDocIds(File directoryFile, Vector vec, String separator)
	   {
		   DocumentBuilder parser = morpho.createDomParser();
	      String[] files = directoryFile.list();
	      for (int i=0;i<files.length;i++)
	        {
	            String filename = files[i];
	            File currentfile = new File(directoryFile, filename);
	            if (currentfile.isDirectory()) {
	            	getDocIds(currentfile,vec, separator);  // recursive call to subdirecctories
	            }
	            if (currentfile.isFile()) 
	            {
	                try 
	                {
	                	 InputSource in;
	                	 String filePath = currentfile.getPath();
	                     try 
	                     {
	                       in = new InputSource(new FileInputStream(filePath));
	                     } 
	                     catch (FileNotFoundException fnf)
	                     {
	                       Log.debug(20,"FileInputStream of " + filePath +
	                                          " threw: " + fnf.toString());
	                       continue;
	                     }
	                     Document current_doc = null;
	                     try 
	                     {
	                       Log.debug(30, "(3.2) Starting parse...");
	                       current_doc = parser.parse(in);
	                       Log.debug(30, "(3.3) Ended parse...");
	                     } 
	                     catch(Exception e1) 
	                     {
	                       // Either this isn't an XML doc, or its broken, so skip it
	                       Log.debug(20,"Parsing error: " + filePath);
	                       Log.debug(20,e1.toString());
	                      continue;
	                     }
	                    Double d = Double.valueOf(filename);
	                    File parentFile = new File(currentfile.getParent());
	                    String docid = parentFile.getName() + separator + currentfile.getName();
	                    Log.debug(30, "the docid from data file is ==== "+docid);
	                    vec.addElement(docid);
	                } 
	                catch (NumberFormatException nfe) 
	                {
	                    Log.debug(30, "Not loading file with invalid name: " + filename);
	                }
	            }
	        }

	   }
	  
	   /*
	    * This class reprents a EML201DocumentCorrection status for a profile.
	    */
	  class ProfileCorrectionStatus
	  {
		     private ConfigXML profile = null;
		     private boolean emlCorrected = false;
		     private boolean hasCorrectionPath = false;
		     /**
		      * Constructor
		      * @param profile   this profile itself
		      * @param emlCorrected   if eml documents were corrected in this profile
		      * @param hasCorrectionPath  if the profile already has the new property eml201corrected in profile.xml
		      */
		    public ProfileCorrectionStatus(ConfigXML profile, boolean emlCorrected, boolean hasCorrectionPath )
		    {
		    	this.profile = profile;
		    	this.emlCorrected = emlCorrected;
		    	this.hasCorrectionPath = hasCorrectionPath;
		    }
		    
		    /**
		     *  Gets the profile 
		     * @return
		     */
		    public ConfigXML getProfile()
		    {
		    	return this.profile;
		    }
		    
		    /**
		     *  Gets the status if eml documents were corrected in this profile
		     * @return
		     */
		    public boolean isEmlCorrected()
		    {
		    	return this.emlCorrected;
		    }
		    
		    /**
		     * Gets the status if profile.xml has the new path eml201corrected
		     * @return
		     */
		    public boolean hasCorrectionPath()
		    {
		    	return this.hasCorrectionPath;
		    }
		    
	  }


}
