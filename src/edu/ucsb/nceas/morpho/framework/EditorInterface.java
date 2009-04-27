/**
 *  '$RCSfile: EditorInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-27 23:08:28 $'
 * '$Revision: 1.12 $'
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

import javax.swing.Action;
import java.awt.Component;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Document;


/**
 * All component plugins that require the editing of XML documents should implement
 * this interface and register themselves as a service provider for the
 * interface with the framework.
 */
public interface EditorInterface
{

  /** 
   * This method is called to open a XML editor that resides either
   * @param xmltext is the xml to be edited in the form of a String
   */
  public void openEditor(String xmltext);
  
  /**
   * This method is called to open a XML editor with the indicated
   * xmltext and an editingCompletedListener to be notified when
   * the editing is completed
   */
 public void openEditor(String xmltext, String id, String location,
                        EditingCompleteListener listener);
 
  /**
   * This method is called to open a XML editor with the indicated
   * xmltext and an editingCompletedListener to be notified when
   * the editing is completed. In this case, only the template
   * based on the DocType is returned (i.e. no merging with existing
   * data) For use in creating new docs.
   */
 public void openEditor(String xmltext, String id, String location,
                        EditingCompleteListener listener, boolean template);
                        
  /**
   * This method is called to open a XML editor with the indicated
   * xmltext and an editingCompletedListener to be notified when
   * the editing is completed, the nodename and nodevalue
   * parameters indicated node to be selected when documement is
   * opened
   */
 public void openEditor(String xmltext, String id, String location,
                        String nodeName, String nodeValue,
                        EditingCompleteListener listener);

  /**
   * This method is called to open a XML editor with the indicated
   * xml structure in a DOM and an editingCompletedListener to be notified when
   * the editing is completed
   */
 public void openEditor(Document doc, String id, String location,
                        EditingCompleteListener listener,
                        String nodeName, int nodeNumber);
 
 /**
  * This method is called to open a XML editor with the indicated
  * xml structure in a DOM and an editingCompletedListener to be notified when
  * the editing is completed
  */
 public void openEditor(Document doc, String id, String location,
         EditingCompleteListener listener,
         String nodeName, int nodeNumber, boolean returnErrorMessage);
   
 /**
  * This method is called to open a XML editor with the indicated
  * xml structure in a DOM and an editingCompletedListener to be notified when
  * the editing is completed
  */
 public void openEditor(Document doc, String id, String location,
         EditingCompleteListener listener,
         String nodeName, int nodeNumber, boolean returnErrorMessage, boolean disableUntrimButtonAndPopUpMenu, String title);
}
