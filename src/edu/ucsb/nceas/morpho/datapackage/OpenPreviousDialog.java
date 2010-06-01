/**
 *  '$RCSfile: OpenPreviousDialog.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-02-27 19:06:52 $'
 * '$Revision: 1.6 $'
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

import java.awt.*;
import javax.swing.*;
import java.util.Vector;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

public class OpenPreviousDialog extends javax.swing.JDialog
{

  String packageName = "";
  Morpho morpho;
  boolean localLoc;
  
	public OpenPreviousDialog(Frame parent)
	{
		super(parent);
		
		setTitle("Select Version");
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(216,252);
		setVisible(false);
		ControlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
		getContentPane().add(BorderLayout.SOUTH,ControlPanel);
		CancelButton.setText(/*"Cancel"*/ Language.getInstance().getMessage("Cancel"));
		ControlPanel.add(CancelButton);
		OpenButton.setText(/*"Open"*/ Language.getInstance().getMessage("Open"));
		ControlPanel.add(OpenButton);
		CenterPanel.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER,CenterPanel);
		CenterPanel.add(BorderLayout.CENTER,ListScrollPane);
		ListScrollPane.getViewport().add(PrevPackageList);
		PrevPackageList.setBounds(0,0,213,214);
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		CancelButton.addActionListener(lSymAction);
		OpenButton.addActionListener(lSymAction);
		//}}
	}

	public OpenPreviousDialog()
	{
		this((Frame)null);
	}

	public OpenPreviousDialog(String sTitle, int numVersions, Morpho morpho, boolean local)
	{
		this();
		setTitle(sTitle);
		this.packageName = sTitle;
		Vector data = new Vector();
		for (int num=0;num<numVersions;num++) {
		  data.addElement("Revision "+(num+1));    
		}
		PrevPackageList.setListData(data);
		this.morpho = morpho;
		this.localLoc = local;
	}

	static public void main(String args[])
	{
		(new OpenPreviousDialog()).setVisible(true);
	}


	//{{DECLARE_CONTROLS
	javax.swing.JPanel ControlPanel = new javax.swing.JPanel();
	javax.swing.JButton CancelButton = new javax.swing.JButton();
	javax.swing.JButton OpenButton = new javax.swing.JButton();
	javax.swing.JPanel CenterPanel = new javax.swing.JPanel();
	javax.swing.JScrollPane ListScrollPane = new javax.swing.JScrollPane();
	javax.swing.JList PrevPackageList = new javax.swing.JList();
	//}}


	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == CancelButton)
				CancelButton_actionPerformed(event);
			else if (object == OpenButton)
				OpenButton_actionPerformed(event);
		}
	}

	void CancelButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		this.setVisible(false);
	  this.dispose();		 
	}

	void OpenButton_actionPerformed(java.awt.event.ActionEvent event)
	{
      int selnum = PrevPackageList.getSelectedIndex();
      if (selnum<0) return;
      String temp = packageName + "." + (selnum + 1);

      DataPackageInterface dataPackage;
      try 
      {
        ServiceController services = ServiceController.getInstance();
        ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
        dataPackage = (DataPackageInterface)provider;
      } 
      catch (ServiceNotHandledException snhe) 
      {
        Log.debug(6, snhe.getMessage());
        return;
      }
      String location = "";
      if (localLoc) location = "local";
      else {
        location = "metacat";
      }
      dataPackage.openDataPackage(location, temp, null, null, null);
		this.setVisible(false);
	  this.dispose();		 
	}
}
