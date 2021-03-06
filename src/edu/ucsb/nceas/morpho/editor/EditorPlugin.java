/**
 *  '$RCSfile: EditorPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-27 23:08:28 $'
 * '$Revision: 1.33 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.EditingCompleteListener;
import edu.ucsb.nceas.morpho.framework.EditorInterface;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Vector;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.w3c.dom.Document;


public class EditorPlugin implements PluginInterface, ServiceProvider, EditorInterface {

  /** A reference to the container framework */
  protected Morpho morpho = null;

  /** The configuration options object reference from the framework */
  protected ConfigXML config = null;

  protected Vector editingCompleteRegistry = null;
  
  /** list of DocFrame object that have been openeds (key)
   *  with associated EditingCompleteListeners 
   */
  protected Hashtable docframes = null;
  
  /**
   *  the id of a document being edited
   */
  protected String id = null;
  
  /**
   *  the location (local,metacat) of a document being edited
   */
  
  protected String location = null;
  
  /** 
   *  clipboardObject is a DefaultMutableTreeNode used to
   *  pass nodes between different editors; i.e. for
   *  copy and paste between editor windows
   */
  protected Object clipboardObject = null;
  
  
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
   * The plugin must store a reference to Morpho 
   * in order to be able to call the services available through 
   * the framework.  This is also the time to register menus
   * and toolbars with the framework.
   */
  public void initialize(Morpho morpho) {
    this.morpho = morpho;
    this.config = morpho.getConfiguration();
    loadConfigurationParameters();

    editingCompleteRegistry = new Vector();

    // Register Services
    try 
    {
      ServiceController services = ServiceController.getInstance();
      services.addService(EditorInterface.class, this);
      Log.debug(20, "Service added: EditorInterface.");
    } 
    catch (ServiceExistsException see) 
    {
      Log.debug(6, "Service registration failed: EditorInterface.");
      Log.debug(6, see.toString());
    }

  }
  
  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() {
    // Set up the menus for the application
    // MBJ not complete yet -- need to convert to GUIAction
    Action openItemAction = new AbstractAction("Open Editor") {
      public void actionPerformed(ActionEvent e) {
//        EditorController editframe = new EditorController(framework,"Editor Controller");
//        editframe.setVisible(true);
//        framework.addWindow(editframe);
      }
    };
    openItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Edit16.gif")));
    openItemAction.putValue(Action.SHORT_DESCRIPTION, "Open Editor");
    openItemAction.putValue("menuPosition", new Integer(0));
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
      docframes.remove(doc);
      list.editingCompleted(xmlString, this.id, this.location);
    }
  }

  /**
   * Fire off notifications to EditingCanceledListeners
   * when the editing has been completed
   */
  public void fireEditingCanceledEvent(DocFrame doc, String xmlString) 
  {
    if (docframes.containsKey(doc)) {
      location = doc.getLocationString();
      id = doc.getIdString();
      EditingCompleteListener list = (EditingCompleteListener)docframes.get(doc); 
      docframes.remove(doc);
      list.editingCanceled(xmlString, this.id, this.location);
    }
  }
  
  public void openEditor(String xmlText) {
    DocFrame editorframe = new DocFrame(morpho, "Working...", xmlText, false);
    editorframe.setController(this);
//    editorframe.setVisible(true);
    editorframe.initDoc(morpho, xmlText);
    //MBJ framework.addWindow(editorframe);
  }

  public void openEditor(String xmlText, String id, String location, 
                         EditingCompleteListener listener) {
    DocFrame editorframe = new DocFrame(morpho, "Working...", xmlText, id, location);
    editorframe.setController(this);
//    editorframe.setVisible(true);
    editorframe.initDoc(morpho, xmlText);
    this.id = id;
    this.location = location;
    //MBJ if (framework!=null) {
      //MBJ framework.addWindow(editorframe);
    //MBJ }
    docframes.put(editorframe, listener);
  }

  public void openEditor(Document doc, String id, String location, 
                         EditingCompleteListener listener,
                         String nodeName, int cnt) {
	  openEditor(doc, id, location, 
	          listener, nodeName, cnt, true);
    
  }
  
  public void openEditor(Document doc, String id, String location, 
          EditingCompleteListener listener,
          String nodeName, int cnt, boolean returnErrorMessage) {
	  openEditor(doc, id, location, listener, nodeName, cnt, returnErrorMessage, false, "");
	}
  
  public void openEditor(Document doc, String id, String location, 
          EditingCompleteListener listener,
          String nodeName, int cnt, boolean returnErrorMessage, boolean disableUntrimButtonAndPopUpMenu, String title) {	      
	    DocFrame editorframe = new DocFrame();
	    if(title != null && !title.trim().equals(""))
	    {
	    	editorframe.setTitle(title);
	    }
	    editorframe.disableUnTrimButtonAndPopUpMenu(disableUntrimButtonAndPopUpMenu);
		editorframe.setController(this);
		editorframe.setReturnErrorMessageInExistEditing(returnErrorMessage);
		editorframe.setVisible(true);
		editorframe.initDoc(morpho, doc, id, location, nodeName, cnt);      
		this.id = id;
		this.location = location;
		//MBJ if (framework!=null) {
		//MBJ framework.addWindow(editorframe);
		//MBJ }
		docframes.put(editorframe, listener);	      
	}

  public void openEditor(String xmlText, String id, String location, 
                         EditingCompleteListener listener, boolean template) {
    DocFrame editorframe = new DocFrame(morpho, "Working...", xmlText, id, location, template );
    editorframe.setController(this);
 //   editorframe.setVisible(true);
    editorframe.initDoc(morpho, xmlText);
    this.id = id;
    this.location = location;
    //MBJ if (framework!=null) {
      //MBJ framework.addWindow(editorframe);
    //MBJ }
    docframes.put(editorframe, listener);
  }
  
  public void openEditor(String xmlText, EditingCompleteListener listener)
  {
    openEditor(xmlText, null, null, listener);
  }
  
  public void openEditor(String xmlText, String id, String location,
                         String nodeName, String nodeValue,
                         EditingCompleteListener listener) {
    DocFrame editorframe = new DocFrame(morpho, "Working...", xmlText, id, location,
                          nodeName, nodeValue);
    editorframe.setController(this);
  //  editorframe.setVisible(true);
    editorframe.initDoc(morpho, xmlText);
    this.id = id;
    this.location = location;
    //MBJ if (framework!=null) {
      //MBJ framework.addWindow(editorframe);
    //MBJ }
    docframes.put(editorframe, listener);
  }

  
  
  public Object getClipboardObject() {
    return clipboardObject; 
  }
  
  public void setClipboardObject(Object node) {
    this.clipboardObject = node;
  } 

  }
