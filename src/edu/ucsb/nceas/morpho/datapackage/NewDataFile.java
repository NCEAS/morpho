/**
 *  '$RCSfile: NewDataFile.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-12-15 20:28:31 $'
 * '$Revision: 1.14 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.*;
import java.io.*;
import javax.swing.*;

public class NewDataFile extends javax.swing.JDialog
{
    private DataPackage dataPackage = null;
    ConfigXML config;
    Morpho morpho = null;
    File addedFile = null;
    String entityId;
    Frame parent;
    
    
	public NewDataFile(Frame parent)
	{
		super(parent);
		this.parent = parent;
		
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setTitle("Add Data File");
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(400,225);
		setVisible(false);
		ControlsPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		getContentPane().add(BorderLayout.SOUTH,ControlsPanel);
		AddFileButton.setText("Add File");
		AddFileButton.setActionCommand("Add File");
		ControlsPanel.add(AddFileButton);
		CancelAddButton.setText("Cancel");
		CancelAddButton.setActionCommand("Cancel");
		ControlsPanel.add(CancelAddButton);
		HelpLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		HelpLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		HelpLabel.setForeground(java.awt.Color.black);
		HelpLabel.setText("<html><p><br>Select Data File to be Associated with this Metadata</html>");
		getContentPane().add(BorderLayout.NORTH,HelpLabel);
		CenterPanel.setLayout(new GridLayout(4,1,0,0));
		getContentPane().add(BorderLayout.CENTER,CenterPanel);
		JPanel1.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		CenterPanel.add(JPanel1);
		JPanel2.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		CenterPanel.add(JPanel2);
		JPanel3.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		CenterPanel.add(JPanel3);
		FileNameTextField.setColumns(20);
		JPanel3.add(FileNameTextField);
		FileDialogButton.setText("Browse...");
		FileDialogButton.setActionCommand("jbutton");
		JPanel3.add(FileDialogButton);
		JPanel4.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		CenterPanel.add(JPanel4);
		//}}
    /* Center the Frame */
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle frameDim = getBounds();
    setLocation((screenDim.width - frameDim.width) / 2 ,
            (screenDim.height - frameDim.height) /2);
		
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		FileDialogButton.addActionListener(lSymAction);
		CancelAddButton.addActionListener(lSymAction);
		AddFileButton.addActionListener(lSymAction);
		//}}
	}

	public NewDataFile()
	{
		this((Frame)null);
	}

	public NewDataFile(String sTitle)
	{
		this();
		setTitle(sTitle);
	}
	
	public NewDataFile(Frame parent, DataPackage dp, Morpho morpho, String entityId) {
	    this(parent);
	    this.dataPackage = dp;
	    this.morpho = morpho;
	    this.config = morpho.getConfiguration();
	    this.entityId = entityId;
	}

	static public void main(String args[])
	{
		(new NewDataFile()).setVisible(true);
	}

	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets
		Insets insets = getInsets();
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JPanel ControlsPanel = new javax.swing.JPanel();
	javax.swing.JButton AddFileButton = new javax.swing.JButton();
	javax.swing.JButton CancelAddButton = new javax.swing.JButton();
	javax.swing.JLabel HelpLabel = new javax.swing.JLabel();
	javax.swing.JPanel CenterPanel = new javax.swing.JPanel();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
	javax.swing.JTextField FileNameTextField = new javax.swing.JTextField();
	javax.swing.JButton FileDialogButton = new javax.swing.JButton();
	javax.swing.JPanel JPanel4 = new javax.swing.JPanel();
	//}}


	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == FileDialogButton)
				FileDialogButton_actionPerformed(event);
			else if (object == CancelAddButton)
				CancelAddButton_actionPerformed(event);
			else if (object == AddFileButton)
				AddFileButton_actionPerformed(event);
		}
	}

	void FileDialogButton_actionPerformed(java.awt.event.ActionEvent event)
	{
    {
      JFileChooser filechooser = new JFileChooser();
      File datafile;
      filechooser.showOpenDialog(this);
      datafile = filechooser.getSelectedFile();
      FileNameTextField.setText(datafile.getAbsolutePath());
    }
		
			 
	}

	void CancelAddButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		this.hide();
		this.dispose();
			 
	}

	void AddFileButton_actionPerformed(java.awt.event.ActionEvent event)
	{
	    // first add the file
	    String fileName = FileNameTextField.getText();
	    if (fileName.length()>0) {
	        addedFile = new File(fileName);
	        if (!addedFile.exists()) {
	            addedFile = null;
                JOptionPane.showConfirmDialog(this,
                                   "The file you selected was not found.",
                                   "File Not Found", 
                                   JOptionPane.OK_CANCEL_OPTION,
                                   JOptionPane.WARNING_MESSAGE);
	            return;
	        }
      doAddFile(addedFile);

	    }
  }
  
  public void doAddFile(File addedFile) {
	    String dataPackageId = null;
      
        AccessionNumber a = new AccessionNumber(morpho);
        String newid = "";
        String location = dataPackage.getLocation();
        boolean locMetacat = false;
        boolean locLocal = false;
        String docString;
        FileSystemDataStore fsds = new FileSystemDataStore(morpho);
        File packageFile = dataPackage.getTriplesFile();
    
        if(location.equals(DataPackageInterface.LOCAL) || 
        location.equals(DataPackageInterface.BOTH))
        {
            locLocal = true;
        }
    
        if(location.equals(DataPackageInterface.METACAT) || 
            location.equals(DataPackageInterface.BOTH))
        {
            locMetacat = true;
        }
    
        if(addedFile != null) { 
            newid = a.getNextId();
            dataPackageId = handleAddDataFile(locLocal, locMetacat, newid);
        }
	    // now remove this window
		  this.hide();
		  this.dispose();
        
      if (dataPackageId!=null) {
      //  EntityGUI eu = (EntityGUI)parent;
      //  eu.dispose();
      //  (eu.getDataPackageGui()).dispose();
		
    //refresh the package editor that this wizard came from.
    DataPackage newpackage = new DataPackage(dataPackage.getLocation(),
                                             dataPackageId, null,
                                             morpho, true);
    this.dataPackage = newpackage;
    
    MorphoFrame thisFrame = (UIController.getInstance()).getCurrentActiveWindow();

    // Show the new package
    try 
    {
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider = 
                      services.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackage = (DataPackageInterface)provider;
      dataPackage.openDataPackage(location, newpackage.getID(), null, null, null);
    }
    catch (ServiceNotHandledException snhe) 
    {
       Log.debug(6, snhe.getMessage());
    }
    
    thisFrame.setVisible(false);
    UIController controller = UIController.getInstance();
    controller.removeWindow(thisFrame);
    thisFrame.dispose();

	
      }
	}
// returns new ID of dataPackage
  private String handleAddDataFile(boolean locLocal, boolean locMetacat, 
                                 String newid)
  { //add a data file here
    String dataPackageId = null;
    String relationship = "isRelatedTo";
    AccessionNumber a = new AccessionNumber(morpho);
    FileSystemDataStore fsds = new FileSystemDataStore(morpho);
    //relate the new data file to the package itself
    if (addedFile!=null) {
      relationship = FileNameTextField.getText();
      if(relationship.indexOf("/") != -1 || 
        relationship.indexOf("\\") != -1)
      { //strip out the path info
        int slashindex = relationship.lastIndexOf("/") + 1;
        if(slashindex == 0)
        {
          slashindex = relationship.lastIndexOf("\\") + 1;
        }
      
        relationship = relationship.substring(slashindex, 
                                            relationship.length());
        relationship = "isDataFileFor(" + relationship + ")";
      }
    }
    Triple t = new Triple(newid, relationship, dataPackage.getID());
    TripleCollection triples = new TripleCollection();
    triples.addTriple(t);
    
    // add an access triple for the new datafile
    String accessId = dataPackage.getAccessFileIdForDataPackage();
    Triple tacc = new Triple(accessId,"provides access control rules for", newid);
    triples.addTriple(tacc);
    
    // connect this entity to the new datafile
    Triple t1 = new Triple(entityId, 
                       "provides table-entity information for DATAFILE",
                       newid);
    triples.addTriple(t1);
    
    File packageFile = dataPackage.getTriplesFile();
    //add the triple to the triple file
    String docString = PackageUtil.addTriplesToTriplesFile(triples, 
                                                           dataPackage, 
                                                           morpho);
    //write out the files
    File newDPTempFile;
    //get a new id for the package file
    dataPackageId = a.incRev(dataPackage.getID());
    Log.debug(20, "datapackageid: " + dataPackage.getID() + " newid: " + dataPackageId);
    try
    { //this handles the package file
      //save a temp file with the new id
      newDPTempFile = fsds.saveTempFile(dataPackageId,
                                        new StringReader(docString));
      //inc the revision of the new Package file in the triples
      docString = a.incRevInTriples(newDPTempFile, dataPackage.getID(), 
                                    dataPackageId);
      //save new temp file that has the right id and the id inced in the triples
      newDPTempFile = fsds.saveTempFile(dataPackageId, 
                                        new StringReader(docString));
    }
    catch(Exception e)
    {
      Log.debug(0, "Error saving file: " + e.getMessage());
      e.printStackTrace();
      return dataPackageId;
    }
    
    if(locLocal)
    {
      File newPackageMember;
      try
      { //save the new package member
        if (addedFile!=null) 
        {
          newPackageMember = fsds.newDataFile(newid, new FileInputStream(addedFile));
        }
      }
      catch(Exception e)
      {
        Log.debug(0, "Error saving file: " + e.getMessage());
        e.printStackTrace();
        e.printStackTrace();
        return dataPackageId;
      }
      
      try
      { //save the new package file
        fsds.saveFile(dataPackageId, new FileReader(newDPTempFile));
      }
      catch(Exception e)
      {
        Log.debug(0, "Error saving file: " + e.getMessage());
        e.printStackTrace();
        return dataPackageId;
      }
    }
    
    if(locMetacat)
    { //send the package file and the data file to metacat
      Log.debug(20, "Sending file(s) to metacat.");
      MetacatDataStore mds = new MetacatDataStore(morpho);
      try
      { //send the new data file to the server
        mds.newDataFile(newid, addedFile);
      }
      catch(Exception mue)
      {
        Log.debug(0, "Error saving data file to metacat: " + 
                              mue.getMessage());
        mue.printStackTrace();
        return dataPackageId;
      }
      
      try
      { //save the new package file
        mds.saveFile(dataPackageId, new FileReader(newDPTempFile), 
                     dataPackage);
      }
      catch(MetacatUploadException mue)
      {
        Log.debug(0, "Error saving package file to metacat: " + 
                              mue.getMessage());
        mue.printStackTrace();
        return dataPackageId;
      }
      catch(FileNotFoundException fnfe)
      {
        Log.debug(0, "Error saving package file to metacat(2): " + 
                              fnfe.getMessage());
        fnfe.printStackTrace();
        return dataPackageId;
      }
      catch(Exception e)
      {
        Log.debug(0, "Error saving package file to metacat(3): " + 
                              e.getMessage());
        e.printStackTrace();
        return dataPackageId;
      }
    }
      return dataPackageId;
    
  }


}
