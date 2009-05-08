/**
 *  '$RCSfile: BackupMorphoDataFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-08 21:50:34 $'
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
package edu.ucsb.nceas.morpho.framework;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.ice.tar.TarArchive;
import com.ice.tar.TarEntry;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.EML201DocumentCorrector;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * GUI Window notifies users that morpho is backing up .morpho directory.
 * This class will backup the .morpho directory in case user want to go back.
 * First, morpho will check the if .morpho/profile dir exists. If not, do nothing.
 * The reason why we need to check the .morpho/profile, rather than .morpho is
 * morpho backup GUI will show up during user first time install morpho. And this
 * backup will only handle a configuration file.
 * Second, morpho will check if the value of variable back_dot_morpho_dir in configure
 * file. If the value != false, we will started to backup.
 * Third, then morpho will check up the DOTMORPHOBACKUP+VERSION.tar.gz file exist or
 * not. If exist, morpho wouldn't backup (it backuped before).
 * During the backup, morpho will skip any files starting DOTMORPHOBACKUP in .morpho dir.
 * Those file are previous backup, there is no reason to backup again.
 * @author tao
 *
 */
public class BackupMorphoDataFrame extends JFrame 
{
	private Morpho morpho = null;
	private static final String TITLE = "Backup Morpho Data Directory";
	private final static String DOTMORPHOBACKUP = "dotMorphoBackup-";
	private final static String GZIP = "gz";
	private final static String TAR   = "tar";
	private final static String DOT  = ".";

	/**
	 * Default Constructor
	 *
	 */
	public BackupMorphoDataFrame(Morpho morpho)
	{
		this.morpho = morpho;
	    
	}
	
	
	/**
	 * Do the backup job for given doc list.
	 */
	public void doBackup()
	{
		if (morpho != null)
		{
			backupDotMorphoDir();
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
		   loadingLabel.setText("Backuping Morpho user data. This may take a while...");
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
     * This method will backup the .morpho directory in case user want to go back.
     * First, morpho will check the if .morpho dir(configdir) exists. If not, do nothing.
     * Second, morpho will check if the value of variable back_dot_morpho_dir in configure
     * file. If the value != false, we will started to backup.
     * Third, then morpho will check up the DOTMORPHOBACKUP+VERSION.tar.gz file exist or
     * not. If exist, morpho wouldn't backup (it backuped before).
     * During the backup, morpho will skip any files starting DOTMORPHOBACKUP in .morpho dir.
     * Those file are previous backup, there is no reason to backup again.
     */
    private  void backupDotMorphoDir()
    {
    	try
    	{
	    	//check if the .morpho/profiles directory exists
    		File configDir = new File(ConfigXML.getConfigDirectory());
	    	File profileDir = new File(ConfigXML.getConfigDirectory() + File.separator + morpho.getConfiguration().get("profile_directory", 0));
	        if (configDir.exists() && profileDir.exists()) 
	        {
		        // get  the configuration value of backup .morpho from configure file
		        boolean backup = true;//true is the default value
		        try
		        {
		        	backup = (new Boolean(morpho.getConfiguration().get("back_dot_morpho_dir", 0))).booleanValue();
		        }
		        catch(Exception ee)
		        {
		        	Log.debug(30, "We will set up backup to true even we couldn't the configuration value from config file "+ee.getMessage());
		        }
		        
		        // only backup when the back_dot_morpho_dir is not false
		        if (backup)
		        {
		        	//check the bakup file of this version eixisting or not
		        	File targetFile = new File(ConfigXML.getConfigDirectory()+File.separator+DOTMORPHOBACKUP+Morpho.VERSION+DOT+TAR+DOT+GZIP);
		        	if(!targetFile.exists())
		        	{
		        		//load the gui
		        		loadGUI();
		        		// generte a tar file under the parent dir of .morpho - home dir
		        		File tarFile = new File(System.getProperty("user.home")+File.separator+DOTMORPHOBACKUP+Morpho.VERSION+DOT+TAR);
		        		FileOutputStream tarOutput = new FileOutputStream(tarFile);
		        		TarArchive tarArchive = new TarArchive(tarOutput);
		        		//list the .morpho dir (configure dir) and put everything exception previous backup file into tar
		        		if(configDir.isDirectory())
		        		{
		        			File[] children = configDir.listFiles();
		        			boolean recurse= true;
		        			if(children != null)
		        			{
		        				for (int i=0; i<children.length; i++)
		        				{
		        					File kid = children[i];
		        					if (kid != null && kid.getName() != null && !kid.getName().startsWith(DOTMORPHOBACKUP))
		        					{
		        					   TarEntry tarEntry = new TarEntry(kid);
		        					   tarArchive.writeEntry(tarEntry, recurse);
		        					}
		        				}
		        			}
		        		}
		        		tarArchive.closeArchive();
		        		//gzip the tar file, the gzip file will located under the .morpho
		        		GZIPOutputStream gzipOutput = new GZIPOutputStream (new FileOutputStream(targetFile));
		        		FileInputStream input = new FileInputStream(tarFile);
		        		 int size = 20000000;
		                 byte buf[] = new byte[size];
		                 int len = 0;
		                 while ((len = input.read(buf, 0, size)) != -1) {
		                     gzipOutput.write(buf, 0, len);
		                 }
		                 input.close();
		                 gzipOutput.close();
		        		//delete the tar file.
		                 tarFile.delete();
		        	}
		        	
		        }
		        else
		        {
		        	Log.debug(30, "since morpho is configured not to backup .morph dir, we will skip backup");
		        }
	        }
    	}
    	catch(Exception e)
    	{
    		Log.debug(30, "Backup .morpho directory failed since "+e.getMessage());
    	}
    	finally
		{
			 //Dipose the frame
			if (this != null)
			{
				 this.dispose();
			 }
		}
    	
    }


}

