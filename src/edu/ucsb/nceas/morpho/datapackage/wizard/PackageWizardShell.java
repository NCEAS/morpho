/**
 *  '$RCSfile: PackageWizardShell.java,v $'
 *    Purpose: A class that creates a custom data entry form that is made
 *             by parsing an xml file.  XML is then produced from the form
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-29 22:50:19 $'
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

package edu.ucsb.nceas.morpho.datapackage.wizard;

import edu.ucsb.nceas.morpho.framework.*;
import javax.swing.*;
import javax.swing.border.*; 
import java.io.*;
import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;

public class PackageWizardShell extends javax.swing.JFrame
                                implements ActionListener
{
  private ClientFramework framework;
  private PackageWizard visiblePackageWizard;
  
  public PackageWizardShell()
  {
    setTitle("Data Package Wizard");
    initComponents();
    pack();
    setSize(600, 500);
  }
  
  public PackageWizardShell(ClientFramework cf)
  {
    framework = cf;
    setTitle("Data Package Wizard");
    initComponents();
    pack();
    setSize(600, 500);
  }
  
  private void initComponents()
  {
    ConfigXML config = framework.getConfiguration();
    Vector saxparserV = config.get("saxparser");
    String saxparser = (String)saxparserV.elementAt(0);
    Vector packageWizardConfigV = config.get("packageWizardConfig");
    String wizardFile = (String)packageWizardConfigV.elementAt(0);
    
    PackageWizardShellParser pwsp = null;
    
    try
    {
      File xmlfile = new File(wizardFile);
      FileReader xml = new FileReader(xmlfile);
      pwsp = new PackageWizardShellParser(xml, saxparser);
      System.out.println("frames: " + pwsp.getFrames().toString());
      System.out.println("mainframe: " + pwsp.getMainFrame());
    }
    catch(Exception e)
    {
      framework.debug(1, "error reading or parsing file in " + 
                      "PackageWizardShell.initComponents().");
      e.printStackTrace();
    }
    
    Container contentPane = getContentPane();
    JPanel mainWizardFrame = createWizardFrame();
    contentPane.add(mainWizardFrame);
    //get the context of the top frame (not the button frame) so that we
    //can add the first wizard frame to it.
    JPanel topWizardFrame = (JPanel)mainWizardFrame.getComponent(0);
    //get the location of the main wizard frame config file, parse it
    //and have it draw itself into the topWizardFrame
    Vector frames = pwsp.getFrames();
    Hashtable firstFrameHash = (Hashtable)frames.elementAt(0);
    String path = (String)firstFrameHash.get("path");
    PackageWizard pw = new PackageWizard(framework, topWizardFrame, path);
    visiblePackageWizard = pw;
  }
  
  /**
   * handles the actions from the menus if this package wizard is 
   * run in stand alone mode.  
   */
  public void actionPerformed(ActionEvent e) 
  {
    String command = e.getActionCommand();
    String paramString = e.paramString();
    Hashtable contentReps = new Hashtable();
    
    System.out.println("action fired: " + command);
    
    if(command.equals("<< Previous"))
    { //go back a frame
      System.out.println("go back a frame");
    }
    else if(command.equals("Next >>"))
    { //go to the next frame
      System.out.println("go forward a frame");
      System.out.println(visiblePackageWizard.getXML());
      
    }
  }
  
  private JPanel createWizardFrame()
  {
    JPanel mainPanel = new JPanel();
    JPanel wizardPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    BoxLayout box = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
    mainPanel.setLayout(box);
    wizardPanel.setMaximumSize(new Dimension(595, 448));
    wizardPanel.setBorder(BorderFactory.createLineBorder(new Color(255,255,255)));
    buttonPanel.setMaximumSize(new Dimension(595, 48));
    //buttonPanel.setBorder(BorderFactory.createLineBorder(new Color(255,255,255)));
    
    JButton previous = new JButton("<< Previous");
    JButton next = new JButton("Next >>");
    previous.addActionListener(this);
    next.addActionListener(this);
    BoxLayout buttonLayout = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
    buttonPanel.setLayout(buttonLayout);
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(previous);
    buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
    buttonPanel.add(next);
    buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
    
    mainPanel.add(wizardPanel);
    mainPanel.add(buttonPanel);
    return mainPanel;
  }
  
  public static void main(String[] args)
  {
    ConfigXML cxml = new ConfigXML("./lib/config.xml");
    ClientFramework cf = new ClientFramework(cxml);
    PackageWizardShell pws = new PackageWizardShell(cf);
    pws.show();
  }
}
