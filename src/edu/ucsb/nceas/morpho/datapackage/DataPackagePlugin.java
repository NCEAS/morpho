/**
*  '$RCSfile: DataPackagePlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-07-03 18:30:45 $'
 * '$Revision: 1.17.6.1 $'
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

import edu.ucsb.nceas.morpho.framework.*;
import edu.ucsb.nceas.morpho.datapackage.wizard.*;

import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.*;
import java.awt.event.*;

/**
 * Class that implements the plugin for package editing
 */
public class DataPackagePlugin 
       implements PluginInterface, ServiceProvider, DataPackageInterface
{
  /** A reference to the container framework */
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  /** Store our menus and toolbars */
  private Action[] menuActions = null;
  private Action[] toolbarActions = null;

  /**
   * Construct the plugin.  Initialize our menus and toolbars.
   */
  public DataPackagePlugin()
  {
    // Create the menus and toolbar actions, will register later
    initializeActions();
  }

  /** 
   * The plugin must store a reference to the ClientFramework 
   * in order to be able to call the services available through 
   * the framework.  This is also the time to register menus
   * and toolbars with the framework.
   */
  public void initialize(ClientFramework cf)
  {
    this.framework = cf;
    this.config = framework.getConfiguration();
    loadConfigurationParameters();

    // Add menus, and toolbars
    framework.addMenu("File", new Integer(1), menuActions);
    framework.addToolbarActions(toolbarActions);

    // Register Services
    try 
    {
      framework.addService(DataPackageInterface.class, this);
      framework.debug(20, "Service added: DataPackageInterface.");
    } 
    catch (ServiceExistsException see) 
    {
      framework.debug(6, "Service registration failed: DataPackageInterface.");
      framework.debug(6, see.toString());
    }

    cf.debug(20, "Init DataPackage Plugin"); 
  }

  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() 
  {
    // Set up the menus for the application
    menuActions = new Action[1];
    Action newItemAction = new AbstractAction("New Data Package") 
    {
      public void actionPerformed(ActionEvent e) 
      {
        framework.debug(20, "Action fired: New Data Package");
        final PackageWizardShell pws = new PackageWizardShell(framework);
        pws.setName("Package Wizard");
        framework.addWindow(pws);
        pws.addWindowListener(new WindowAdapter()
        {
          public void windowClosed(WindowEvent e)
          {
            framework.removeWindow(pws);
          }
          
          public void windowClosing(WindowEvent e)
          {
            framework.removeWindow(pws);
          }
        });
        pws.show();
      }
    };
    newItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/New16.gif")));
    newItemAction.putValue(Action.SHORT_DESCRIPTION, "New data package");
    newItemAction.putValue("menuPosition", new Integer(0));
    menuActions[0] = newItemAction;

    // Set up the toolbar for the application
    toolbarActions = new Action[1];
    toolbarActions[0] = newItemAction;
  }

  /**
   * Load the configuration parameters that we need
   */
  private void loadConfigurationParameters()
  {
    //we dont' need any!
  }

  public void openDataPackage(String location, String identifier, 
                              Vector relations)
  {
    framework.debug(11, "DataPackage: Got service request to open: " + 
                    identifier + " from " + location + ".");
    DataPackage dp = new DataPackage(location, identifier, 
                                     relations, framework);
    //framework.debug(11, "location: " + location + " identifier: " + identifier +
    //                " relations: " + relations.toString());
    final DataPackageGUI gui = new DataPackageGUI(framework, dp);
   /*
   gui.addWindowListener(new WindowAdapter()
    {
      public void windowClosed(WindowEvent e)
      {
        framework.removeWindow(gui);
      }
      
      public void windowClosing(WindowEvent e)
      {
        framework.removeWindow(gui);
      }
    });
    gui.setName("Package Editor: " + dp.getID());
    
   // framework.addWindow(gui);
   // gui.show();
   */ 
    JPanel packagePanel = new JPanel();
    packagePanel.setLayout(new BorderLayout(0,0));
    packagePanel.add(BorderLayout.CENTER,gui.basicInfoPanel);
    packagePanel.add(BorderLayout.EAST,gui.listPanel);
    
    DataPackageViewer dpv = new DataPackageViewer("DataPackageViewer", dp);
    dpv.setFramework(framework);
    //dpv.toppanel = gui.basicInfoPanel;
    dpv.toppanel = packagePanel;
    dpv.entityItems = gui.entityitems;
    
    dpv.listValueHash = gui.listValueHash;
    dpv.init();
    System.out.println("DataPackageViewer created !!!");
    dpv.setVisible(true);
  }
  
  /**
   * Uploads the package to metacat.  The location is assumed to be 
   * DataPackage.LOCAL
   * @param docid the id of the package to upload
   */
  public void upload(String docid, boolean updateIds) 
              throws MetacatUploadException
  {
    DataPackage dp = new DataPackage(DataPackage.LOCAL, docid, null, framework);
    dp.upload(updateIds);
  }
  
  /**
   * Downloads the package from metacat.  The location is assumed to be
   * DataPackage.METACAT 
   * @param docid the id of the package to download
   */
  public void download(String docid)
  {
    DataPackage dp = new DataPackage(DataPackage.METACAT, docid, null, framework);
    dp.download();
  }
  
  /**
   * Deletes the package.
   * @param docid the id of the package to download
   */
  public void delete(String docid, String location)
  {
    DataPackage dp = new DataPackage(location, docid, null, framework);
    dp.delete(location);
  }
  
  /**
   * Exports the package.
   * @param docid the id of the package to export
   * @param path the directory to which the package should be exported.
   * @param location the location where the package is now: LOCAL, METACAT or 
   * BOTH
   */
  public void export(String docid, String path, String location)
  {
    DataPackage dp = new DataPackage(location, docid, null, framework);
    dp.export(path);
  }
  
  /**
   * Exports the package into a zip file
   * @param docid the id of the package to export
   * @param path the directory to which the package should be exported.
   * @param location the location where the package is now: LOCAL, METACAT or 
   * BOTH
   */
  public void exportToZip(String docid, String path, String location)
  {
    DataPackage dp = new DataPackage(location, docid, null, framework);
    try
    {
      dp.exportToZip(path);
    }
    catch(Exception e)
    {
      System.out.println("Error in DataPackage.exportToZip(): " + e.getMessage());
      e.printStackTrace();
    }
  }
}
