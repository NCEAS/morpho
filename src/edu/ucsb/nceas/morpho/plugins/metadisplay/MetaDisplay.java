/**
 *  '$RCSfile: MetaDisplay.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-13 23:04:57 $'
 * '$Revision: 1.19 $'
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

package edu.ucsb.nceas.morpho.plugins.metadisplay;

import java.io.Reader;
import java.io.IOException;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Vector;
import java.util.Enumeration;

import javax.swing.JLabel;

import edu.ucsb.nceas.morpho.util.XMLTransformer;

import edu.ucsb.nceas.morpho.util.IOUtil;

import edu.ucsb.nceas.morpho.framework.EditingCompleteListener;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayFactoryInterface;
import edu.ucsb.nceas.morpho.plugins.XMLFactoryInterface;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.exception.NullArgumentException;

import edu.ucsb.nceas.morpho.util.Log;

/**
 *  Top-level controller/Mediator class for an instance of a metadata display 
 *  panel.
 */
public class MetaDisplay implements MetaDisplayInterface, 
                                    EditingCompleteListener                                   
{
//  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *

    public static final String BLANK_HTML_PAGE =
          "<html><head></head><body bgcolor=\"#eeeeee\">&nbsp;</body></html>";
                            
    private final   MetaDisplayUI           ui;
    private final   XMLTransformer          transformer;
    private final   History                 history;
    private final   Vector                  editingCompletelistenerList;
    private final   Vector                  listenerList;
    private         XMLFactoryInterface     factory;
    private         String                  identifier;

    
    /**
     *  constructor
     */
    public MetaDisplay()
    {
        listenerList                = new Vector();
        editingCompletelistenerList = new Vector();
        ui                          = new MetaDisplayUI(this);
        transformer                 = XMLTransformer.getInstance();
        
        //maybe we can detect the base path automatically??:
        //definitely need to put in config file
        transformer.addTransformerProperty("stylePath", 
        "jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/morpho-config.jar!/style");
        
        history                     = new History();
    }

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
                                                    DocumentNotFoundException
    {
        Log.debug(50, "getDisplayComponent() called; id = "+identifier);
        //set XML factory
        setFactory(factory);
        
        //add ActionListener to list
        addActionListener(listener);
        
        display(identifier);

        return ui;
    }

    
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
    public Component getDisplayComponent(XMLFactoryInterface factory,
                        ActionListener listener) throws NullArgumentException
    {
        Log.debug(50, "getDisplayComponent() called");
        //set XML factory
        setFactory(factory);
        
        //add ActionListener to list
        addActionListener(listener);
        
        ui.setHTML(BLANK_HTML_PAGE);
        updateBackButtonStatus();
        
        return ui;
    }
    
    
    
    //returns String contents read from Reader. NOTE - closes Reader when done
    private String getAsString(Reader reader) throws DocumentNotFoundException
    {
        StringBuffer docBuff = null;
        try {
            docBuff = IOUtil.getAsStringBuffer(reader, true);
        } catch (IOException ioe) {
            Log.debug(12, "Error reading reader "+ioe.getMessage());
            DocumentNotFoundException dnfe =  new DocumentNotFoundException(
             "MetaDisplay.getAsString() - Nested IOException " + ioe);
            dnfe.fillInStackTrace();
            throw dnfe;
        }
        if (docBuff==null) {
            Log.debug(12, 
            "getAsString() got NULL buffer from IOUtil.getAsStringBuffer()");
            return "";
        }
        return docBuff.toString();
    }
    
    
    
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
    public void display(String identifier) throws DocumentNotFoundException
    {
        Log.debug(50, "display(String identifier) called; id = "+identifier);

        //keep a temp backup of current (outgoing) ID:
        String oldID = this.identifier; //the global one, not the local one
        
        //display mew ID, and in the process, set it to be the current ID:
        displayThisID(identifier);  //the local one
        
        //If new ID wasn't valid, we wouldn't have got this far, so we're OK...
        //add outgoing (i.e. older) ID to hisory:
        history.add(oldID);
    }
    

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
     *  @throws DocumentNotFoundException if Reader isn't null but can't be read
     */
    public void display(String identifier, Reader XMLDocument) 
                                            throws  NullArgumentException, 
                                                    DocumentNotFoundException
    {
        Log.debug(50, 
                  "display(String identifier, Reader XMLDocument) called; id = "
                                                                  +identifier);
        //keep a temp backup of current (outgoing) ID:
        String oldID = this.identifier; //the global one, not the local one
        
        //set ID
        setIdentifier(identifier);
        
        if (XMLDocument==null) {
            Log.debug(12,"MetaDisplay.display() received NULL XML Factory - "
                                                  +"displaying blank document");
            ui.setHTML(getAsString(XMLDocument));
        } else {
            ui.setHTML(getAsString(XMLDocument));
        }
        fireActionEvent(MetaDisplayInterface.NAVIGATION_EVENT,getIdentifier());
        
        //If new ID wasn't valid, we wouldn't have got this far, so we're OK...
        //add ID to hisory:
        history.add(oldID);
    }
                                          
    /**
     *  method to redisplay the previous metadata document from History, by 
     *  re-obtaining the latest instance of the XML document from the 
     *  XMLFactoryInterface and re-styling it
     *
     *  @throws DocumentNotFoundException   if id for previous metadata document 
     *                                      does not point to an actual document
     *                                      or document cannot be accessed.
     */
    public void displayPrevious() throws DocumentNotFoundException 
    {
        //display mew ID, and in the process, set it to be the current ID, but
        //DO NOT add it to the History!
        displayThisID(history.getPrevious()); 
    }
    

    /**
     *  method to redisplay the current metadata, by re-obtaining the latest 
     *  instance of the XML document from the XMLFactoryInterface and re-styling 
     *  it
     *
     *  @throws DocumentNotFoundException if id does not point to a document, or
     *          if requested document exists but cannot be accessed.
     */
    public void redisplay() throws DocumentNotFoundException
    {
        Log.debug(50, "redisplay() called");
        display(getIdentifier());
    }
  
    /**
     *  Register a <code>java.awt.event.ActionListener</code> to listen for 
     *  events
     *
     *  @param listener  
     */
    public void addActionListener(ActionListener    listener)
    {
        Log.debug(50, "addActionListener() called");
        
        if (listener==null) return;
        
        if (!listenerList.contains(listener)) listenerList.add(listener);
    }
  
    /**
     *  Remove this <code>java.awt.event.ActionListener</code> from the list of 
     *  registered listeners
     *
     *  @param listener
     */
    public void removeActionListener(ActionListener listener)
    {
        Log.debug(50, "removeActionListener() called");
        if (listenerList.contains(listener)) listenerList.remove(listener);
    }

    /**
     *  Does callback to <code>actionPerformed()</code> method of each 
     *  <code>java.awt.event.ActionListener</code> in the list of  registered 
     *  listeners.  
     *
     *  @param descriptionInt   integer describing the type of event; eg:
     *                          <ul>
     *                            <li>MetaDisplayInterface.NAVIGATION_EVENT</li>
     *                            <li>MetaDisplayInterface.CLOSE_EVENT</li>
     *                            <li>MetaDisplayInterface.EDIT_BEGIN_EVENT</li>
     *                            <li>...etc</li></ul>
     *                            @see also <code>MetaDisplayInterface</code>
     *                            
     *  @param descriptionInt   String description of the event that will allow 
     *                          listeners to determine specific information 
     *                          about the action and act accordingly.  Could be 
     *                          any unique string identifier 
     */
    protected void fireActionEvent(int descriptionInt, String commandStr)
    {
        Log.debug(50, "fireActionEvent(); description int: "+descriptionInt
                                       +" command string : "+commandStr);
                                       
        ActionEvent ae = new ActionEvent(this,descriptionInt,commandStr);
        Enumeration listeners = listenerList.elements();
        while (listeners.hasMoreElements()) {
            ((ActionListener)listeners.nextElement()).actionPerformed(ae);
        }
    }
    
    /**
     *  Register an <code>EditingCompleteListener</code> to listen for editor
     *  events
     *
     *  @param listener  
     */
    public void addEditingCompleteListener(EditingCompleteListener listener) 
    {
        Log.debug(50, "MetaDisplay: addEditingCompleteListener() called");
    
        if (listener==null) return;
    
        if (!editingCompletelistenerList.contains(listener)) {
            editingCompletelistenerList.add(listener);
        }
    }
    
    /**
     *  Remove this <code>EditingCompleteListener</code> from the list of 
     *  registered listeners
     *
     *  @param listener
     */
    public void removeEditingCompleteListener(EditingCompleteListener listener) 
    {
        Log.debug(50, "MetaDisplay: removeActionListener() called");
        if (editingCompletelistenerList.contains(listener)) {
            editingCompletelistenerList.remove(listener);
        }
    }
 

    /**
     *    Required by <code>EditingCompleteListener</code>  Interface:
     *    This method is called when editing is complete
     *
     *    @param xmlString is the edited XML in String format
     */
    public void editingCompleted(String xmlString, String id, String location) 
    {
        Log.debug(50, "MetaDisplay: editingCompleted() callback received.");
                                       
        Enumeration listeners = editingCompletelistenerList.elements();
        while (listeners.hasMoreElements()) {
            ((EditingCompleteListener)listeners.nextElement())
                                    .editingCompleted(xmlString, id, location);
        }
    }

    /**
     *    Required by <code>EditingCompleteListener</code>  Interface:
     *    this method handles canceled editing
     */
    public void editingCanceled(String xmlString, String id, String location) 
    {
        Log.debug(50, "MetaDisplay: editingCanceled() callback received.");
                                       
        Enumeration listeners = editingCompletelistenerList.elements();
        while (listeners.hasMoreElements()) {
            ((EditingCompleteListener)listeners.nextElement())
                                    .editingCompleted(xmlString, id, location);
        }
    }
    
	
	/**
	 *  Get the current XML factory, used to resolve IDs into XML documents 
	 *
	 *  @return factory     an instance of a class that implements 
	 *                      <code>XMLFactoryInterface</code> to enable this obj
	 *                      to obtain the actual XML document to display, given 
	 *                      the <code>identifier</code> parameter
	 *
	 */
	public XMLFactoryInterface getFactory()
	{
		return this.factory;
	}

    
	/**
	 *  Set the current XML factory, used to resolve IDs into XML documents 
	 *
	 *  @param factory      an instance of a class that implements 
	 *                      <code>XMLFactoryInterface</code> to enable this obj
	 *                      to obtain the actual XML document to display, given 
	 *                      the <code>identifier</code> parameter
	 *
	 *  @throws NullArgumentException if factory not provided.
	 */
	public void setFactory(XMLFactoryInterface factory)
                                                    throws NullArgumentException
	{
        if (factory!=null)  {
		    this.factory = factory;
        } else  {
            NullArgumentException iae
                    = new NullArgumentException("XML Factory may not be null");
            iae.fillInStackTrace();
            throw iae;
        }
	}

	protected String getIdentifier()
	{
		return this.identifier;
	}


	protected History getHistory()
	{
		return this.history;
	}

	
	//displays the passed ID, but DOES NOT add the previous one to the history -
	//that must be done separately, since it's not always required (eg when this 
	//is called by displayPrevious() )
	private void displayThisID(String ID) throws DocumentNotFoundException
	{
	    Reader xmlReader = null;
	    String oldID = this.identifier; //the global one, not the local one
	    try  {
	        setIdentifier(ID);
	    } catch (NullArgumentException nae) {
	        Log.debug(12, "NullArgumentException setting identifier: "
	                                        +ID+"; "+nae.getMessage());
	        DocumentNotFoundException dnfe 
	            =  new DocumentNotFoundException("Nested NullArgumentException:"
	                                                        +nae.getMessage());
	        dnfe.fillInStackTrace();
	        throw dnfe;
	    }
	    try  {
	        xmlReader = factory.openAsReader(ID);
	    } catch (DocumentNotFoundException dnfe) {
	    
	        //reset ID to it's original value before exception occurred:
	        setIDBackTo(oldID);
	        
	        Log.debug(12, "DocumentNotFoundException getting Reader for ID: "
	                                        +ID+"; "+dnfe.getMessage());
	        dnfe.fillInStackTrace();
	        throw dnfe;
	    }
	    fireActionEvent(MetaDisplayInterface.NAVIGATION_EVENT,getIdentifier());

	    Reader resultReader = doTransform(xmlReader);
	    String htmlDoc = getAsString(resultReader);
        ui.setHTML(htmlDoc);
        updateBackButtonStatus();
	}

    //sends xml Reader to morpho.util.XMLTransformer to be styled
	private Reader doTransform(Reader xml) throws DocumentNotFoundException
	{
	    Reader result = null;
	    try {
	        result = transformer.transform(xml);
	    } catch (IOException ioe) {
	        String errMsg   = "MetaDisplay.doTransform(): \n"
	                        + "throwing DocumentNotFoundException. \n"
	                        + "Nested IOException is:\n"+ioe.getMessage()
	                        + "\nXML document received was:\n"+getAsString(xml);
	        Log.debug(12, errMsg);
	        DocumentNotFoundException d = new DocumentNotFoundException(errMsg);
	        d.fillInStackTrace();
	        throw d;
	    }
	    Log.debug(50, "doTransform returning Reader: " + result
                                                +" for ID: " + this.identifier);
	    return result;
//        return xml;
	}    

    
    //sets ID
	private void setIdentifier(String identifier) throws NullArgumentException
	{
        if (identifier != null && !identifier.trim().equals("")) {
		    this.identifier = identifier;
		} else  {
		    NullArgumentException nae 
		        = new NullArgumentException("identifier must have a value");
		    nae.fillInStackTrace();
		    throw nae;
		}
	}
    
    //reset ID to it's original value before exception occurred:
    private void setIDBackTo(String oldID) 
    {
        try {
            setIdentifier(oldID);
        } catch (NullArgumentException nae) {
            Log.debug(12, "NullArgumentException setIDBackTo("+oldID+"); "
                                                            +nae.getMessage());
        }
    }
    
    private void updateBackButtonStatus() 
    {
        if (getHistory().previewPrevious()==null) {
            ui.getHeader().setBackButtonEnabled(false);
        } else {
            ui.getHeader().setBackButtonEnabled(true);
        }
    }
}


