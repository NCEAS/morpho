/**
 *  '$RCSfile: EditorPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-06-26 16:38:58 $'
 * '$Revision: 1.9 $'
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

package edu.ucsb.nceas.morpho.editor;

import edu.ucsb.nceas.morpho.framework.*;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Vector;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

public class EditorPlugin implements PluginInterface, ServiceProvider, EditorInterface {

  /** A reference to the container framework */
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  /** Store our menus and toolbars */
  private Action[] menuActions = null;
  private Action[] toolbarActions = null;

  private Vector editingCompleteRegistry = null;
  
  /** list of DocFrame object that have been openeds (key)
   *  with associated EditingCompleteListeners 
   */
  private Hashtable docframes = null;
  
  private String id = null;
  private String location = null;
  
  /** 
   *  clipboardObject is a DefaultMutableTreeNode used to
   *  pass nodes between different editors; i.e. for
   *  copy and paste between editor windows
   */
   private Object clipboardObject = null;
  
  
  /**
   * Construct the editor plugin.  Initialize our one tab for the 
   * plugin plus any menus and toolbars.
   */
  public EditorPlugin() {
    docframes = new Hashtable();
    // Create the menus and toolbar actions, will register later
    initializeActions();
  }

  /** 
   * The plugin must store a reference to the ClientFramework 
   * in order to be able to call the services available through 
   * the framework.  This is also the time to register menus
   * and toolbars with the framework.
   */
  public void initialize(ClientFramework cf) {
    this.framework = cf;
    this.config = framework.getConfiguration();
    loadConfigurationParameters();
    // Add the menus and toolbars
//    framework.addMenu("Editor", new Integer(4), menuActions);
    framework.addToolbarActions(toolbarActions);

    editingCompleteRegistry = new Vector();

    // Register Services
    try 
    {
      framework.addService(EditorInterface.class, this);
      framework.debug(20, "Service added: EditorInterface.");
    } 
    catch (ServiceExistsException see) 
    {
      framework.debug(6, "Service registration failed: EditorInterface.");
      framework.debug(6, see.toString());
    }

  }
  
  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() {
    // Set up the menus for the application
    menuActions = new Action[1];
    Action openItemAction = new AbstractAction("Open Editor") {
      public void actionPerformed(ActionEvent e) {
        EditorController editframe = new EditorController(framework,"Editor Controller");
        editframe.setVisible(true);
        framework.addWindow(editframe);
      }
    };
    openItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Edit16.gif")));
    openItemAction.putValue(Action.SHORT_DESCRIPTION, "Open Editor");
    openItemAction.putValue("menuPosition", new Integer(0));
    menuActions[0] = openItemAction;
/*    Action searchItemAction = new AbstractAction("Search...") {
      public void actionPerformed(ActionEvent e) {
        handleSearchAction();
      }
    };
    searchItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Search16.gif")));
    searchItemAction.putValue(Action.SHORT_DESCRIPTION, "Search for data");
    searchItemAction.putValue("menuPosition", new Integer(0));
    menuActions[0] = searchItemAction;

    // Set up the toolbar for the application
    toolbarActions = new Action[1];
    toolbarActions[0] = searchItemAction;
 */
  }
 
  /**
   * Load the configuration parameters that we need
   */
  private void loadConfigurationParameters()
  {
    //myparam = config.get("myparam", 0);
  }
 
  
  /**
   * Fire off notifications to EditingCompleteListeners
   * when the editing has been completed
   */
  public void fireEditingCompleteEvent(DocFrame doc, String xmlString) 
  {
    if (docframes.containsKey(doc)) {
      location = doc.getLocationString();
      id = doc.getIdString();
      EditingCompleteListener list = (EditingCompleteListener)docframes.get(doc); 
      list.editingCompleted(xmlString, this.id, this.location);
    }
  }
  
  public void openEditor(String xmlText) {
    DocFrame editorframe = new DocFrame(framework, "Morpho Editor", xmlText);
    editorframe.setController(this);
    editorframe.setVisible(true);
    framework.addWindow(editorframe);
  }

  public void openEditor(String xmlText, String id, String location, 
                         EditingCompleteListener listener) {
    DocFrame editorframe = new DocFrame(framework, "Morpho Editor", xmlText, id, location);
    editorframe.setController(this);
    editorframe.setVisible(true);
    this.id = id;
    this.location = location;
    if (framework!=null) {
      framework.addWindow(editorframe);
    }
    docframes.put(editorframe, listener);
  }
  
  public void openEditor(String xmlText, EditingCompleteListener listener)
  {
    openEditor(xmlText, null, null, listener);
  }
  
  public Object getClipboardObject() {
    return clipboardObject; 
  }
  
  public void setClipboardObject(Object node) {
    this.clipboardObject = node;
  } 

  }
