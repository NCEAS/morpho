/**
 *  '$RCSfile: MetaDisplayInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-10-30 20:46:13 $'
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

package edu.ucsb.nceas.morpho.plugins;


import java.io.Reader;
import java.awt.Component;
import java.awt.event.ActionListener;

import edu.ucsb.nceas.morpho.framework.EditingCompleteListener;
import edu.ucsb.nceas.morpho.plugins.XMLFactoryInterface;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.exception.NullArgumentException;


/**
 *  This interface provides methods for initializing, displaying, updating and 
 *  registering as a listener for a <code>Component</code> that styles XML and 
 *  displays the results 
 */
public interface MetaDisplayInterface
{

   /**
    *
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *
    *   N O T E: THESE SHOULD BE SUPERSEDED BY THE STATE CHANGE EVENT CONSTANTS 
    *            IN StateChangeEvent.java, AND THE WHOLE LISTENER SETUP SHOULD
    *            BE CHANGED TO USEW THE StateChangeMonitor...
    *
    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    *
    *   descriptive constant used in ActionEvents generated by the display 
    *   Signifies that a navigation event has occurred
    */
    public static final int NAVIGATION_EVENT    = 10;
    /**
     *   descriptive constant used in ActionEvents generated by the display 
     *   Signifies that user has requested the previous document from History
     */
    public static final int HISTORY_BACK_EVENT  = 20;
    /**
     *   descriptive constant used in ActionEvents generated by the display 
     *   Signifies that user has issued a command to close the display
     */
    public static final int CLOSE_EVENT         = 30;
    /**
     *   descriptive constant used in ActionEvents generated by the display 
     *   Signifies that the user has issued a command to edit the metadata
     */
    public static final int EDIT_BEGIN_EVENT    = 40;

    /**
     *  method used to obtain a visual component (a descendent of 
     *  <code>java.awt.Component</code>, which will display the XML resource 
     *  identified by the <code>identifier</code> parameter.
     *
     *  @param identifier   a unique identifier used to determine what resource 
     *                      to return 
     *
     *  @param factory      an instance of a class that implements 
     *                      <code>XMLFactoryInterface</code> to enable this obj
     *                      to obtain the actual XML document to display, given 
     *                      the <code>identifier</code> parameter
     *
     *  @param listener     an <code>ActionListener</code> to be notified of all 
     *                      events generated by this obj
     *
     *  @return             a visual component (a descendent of 
     *                      <code>java.awt.Component</code>, which will display 
     *                      the XML resource identified by the 
     *                      <code>identifier</code> parameter.
     *
     *  @throws DocumentNotFoundException if id does not point to a document, or
     *          if requested document exists but cannot be accessed.
     *  @throws NullArgumentException if XML Factory is null.
     */
    public Component getDisplayComponent(   String identifier,
                                            XMLFactoryInterface factory,
                                            ActionListener listener )
                                            throws  NullArgumentException, 
                                                    DocumentNotFoundException;
                                            
    /**
     *  method used to obtain a visual component (a descendent of 
     *  <code>java.awt.Component</code>, which will subsequently display the XML 
     *  resources identified by unique string IDs.  However, the initial 
     *  instance of the component returned by this method will be blank.
     *
     *  @param factory      an instance of a class that implements 
     *                      <code>XMLFactoryInterface</code> to enable this obj
     *                      to obtain the actual XML document to display, given 
     *                      unique <code>identifier</code> parameters
     *
     *  @param listener     an <code>ActionListener</code> to be notified of all 
     *                      events generated by this obj
     *
     *  @return             a visual component (a descendent of 
     *                      <code>java.awt.Component</code>, which is currently 
     *                      blank, but can subsequently be used to display the
     *                      XML resources identified by unique
     *                      <code>identifier</code> parameters.
     *
     *  @throws NullArgumentException if XML Factory is null.
     */
    public Component getDisplayComponent(   XMLFactoryInterface factory,
                                            ActionListener listener )
                                            throws  NullArgumentException, 
                                                    DocumentNotFoundException;

    /**
     *  method to display metadata in an existing instance of a visual component 
     *  (metadata identified by the <code>identifier</code> parameter).
     *
     *  @param identifier   a unique identifier used to determine what resource 
     *                      to return 
     *
     *  @throws DocumentNotFoundException if id does not point to a document, or
     *          if requested document exists but cannot be accessed.
     */
    public void display(String identifier)  throws  DocumentNotFoundException;
    
    /**
     *  method to display metadata in an existing instance of a visual component 
     *  (metadata is provided as a Reader (the "XMLDocument" parameter), and a 
     *  required corresponding unique <code>identifier</code> parameter) that 
     *  can subsequently be used by this component to get the latest instance of 
     *  the same "XMLDocument" from the XMLFactoryInterface.
     *
     *  @param identifier   a unique identifier that can subsequently be used by 
     *                      this component to get the latest instance of the 
     *                      same "XMLDocument" from the XMLFactoryInterface
     *
     *  @param XMLDocument  a Reader for the character-based XML document
     * 
     *  @throws NullArgumentException if id not provided.
     *  @throws DocumentNotFoundException if Reader cannot be read.
     */
    public void display(String identifier, Reader XMLDocument) 
                                            throws  NullArgumentException, 
                                                    DocumentNotFoundException;
                                            
    /**
     *  method to redisplay the current metadata, by re-obtaining the latest 
     *  instance of the XML document from the XMLFactoryInterface and re-styling 
     *  it
     *
     *  @throws DocumentNotFoundException if id does not point to a document, or
     *          if requested document exists but cannot be accessed.
     */
    public void redisplay() throws DocumentNotFoundException;
    
    /**
     *  Register a <code>java.awt.event.ActionListener</code> to listen for 
     *  events
     *
     *  @param listener  
     */
    public void addActionListener(ActionListener    listener);
    
    /**
     *  Remove this <code>java.awt.event.ActionListener</code> from the list of 
     *  registered listeners
     *
     *  @param listener
     */
    public void removeActionListener(ActionListener listener);
   
    /**
     *  Register an <code>EditingCompleteListener</code> to listen for editor
     *  events
     *
     *  @param listener  
     */
    public void addEditingCompleteListener(EditingCompleteListener listener);
    
    /**
     *  Remove this <code>EditingCompleteListener</code> from the list of 
     *  registered listeners
     *
     *  @param listener
     */
    public void removeEditingCompleteListener(EditingCompleteListener listener);

    /**
     *  method to redisplay the previous metadata document from History, by 
     *  re-obtaining the latest instance of the XML document from the 
     *  XMLFactoryInterface and re-styling it
     *
     *  @throws DocumentNotFoundException   if id for previous metadata document 
     *                                      does not point to an actual document
     *                                      or document cannot be accessed.
     */
    public void displayPrevious() throws DocumentNotFoundException;
    
    /**
     *  method to add a key/value transformer property pair to the properties 
     *  that will be passed onto the XSL Transformation Engine, and will then be 
     *  made available to the actual XSL stylesheets as <xsl:param> values 
     *
     *  @param key  
     *
     *  @param value  
     */
    public void useTransformerProperty(String key, String value);
    
	  /**
	   *  method to get the transformer property value corresponding to the passed 
     *  key 
	   *
	   *  @param key  
	   *
	   *  @return value  <code>String</code> value associated with the passed key
	   */
	  public String getTransformerProperty(String key);

	  /**
	   *  Get the <code>String</code> identifier associated with the 
     *  currently-displayed metadata
	   *
	   *  @return identifier  <code>String</code> identifier associated with the 
     *                      currently-displayed metadata
	   *
	   */
	  public String getIdentifier();
}

