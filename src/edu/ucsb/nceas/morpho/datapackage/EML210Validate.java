/**
 *  '$RCSfile: EML210Validate.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-06 02:47:24 $'
 * '$Revision: 1.1 $'
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

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;
import java.util.Vector;

import edu.ucsb.nceas.morpho.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class will check validity of an eml 210 doc which was tranformed from 201 by xsl style sheet.
 * In EML 210 specification, most of elements (except  recordDelimiter, physicalLineDelimiter 
 * and fieldDelimiter) couldn't have whitespace value. However, eml 201 and eml200 could. 
 * So this class will find out the pathes of eml 210 document containing whitespace value.
 * The first step is to find out the whitespace path, the second steps is to find out other 
 * potential issues:
 * lat/lon to decimal
 * altitude units -- from dictionary
 * gring tree could be wrong
 * @author tao
 *
 */
public class EML210Validate extends DefaultHandler implements ErrorHandler
{
	 private final static String DEFAULT_PARSER = "org.apache.xerces.parsers.SAXParser";
	 private final static String SLASH ="/";
	// SAX parser
	XMLReader parser = null;
	private String schemaLocation = "xsd/eml-2.1.0";
	private Stack path = new Stack();
	private Vector invalidPathList = new Vector();

	/**
	 * Class constructor.
	 * It will initialize a sax parser.
	 */
    public  EML210Validate() throws SAXException
    {
       initializeParser();
    }
    
    /*
     * Initialize a sax parser
     */
    private void initializeParser() throws SAXException
    {   	    
    	    parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER);
    	    parser.setContentHandler((ContentHandler)this);
    	    parser.setErrorHandler((ErrorHandler)this);
    	    parser.setFeature("http://xml.org/sax/features/namespaces", true);
    	    parser.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
    	    parser.setFeature("http://xml.org/sax/features/validation", true);
    	    //parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error",true);
    	    parser.setFeature("http://apache.org/xml/features/validation/schema", true);
    	    parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
    	      schemaLocation);
    	  
     }
    
    /*
     * Parse the eml document
     */
    public void parse(Reader xml) throws IOException, SAXException
    {
    	parser.parse(new InputSource(xml));
    }
    
    /**
     * Gets the invalid path
     */
    public Vector getInvalidPathList()
    {
    	return invalidPathList;
    }
    
    /**
     * Checks if invalid path is empty after parsing
     */
    public boolean invalidPathIsEmpty()
    {
    	return invalidPathList.isEmpty();
    }
    
    /** SAX Handler that is called at the start of each XML element */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException
    {
        // for element <eml:eml...> qname is "eml:eml", local name is "eml"
        // for element <acl....> both qname and local name is "eml"
        // uri is namespace
        Log.debug(30, "Start ELEMENT(qName) " + qName);
        Log.debug(30, "Start ELEMENT(localName) " + localName);
        Log.debug(30, "Start ELEMENT(uri) " + uri);
        path.add(qName);
    }
    
    /** SAX Handler that is called at the end of each XML element */
    public void endElement(String uri, String localName, String qName)
            throws SAXException
    {
        Log.debug(30, "End ELEMENT " + qName);
    }
    
    /** SAX Handler that is called for each XML text node */
    public void characters(char[] cbuf, int start, int len) throws SAXException
    {
        Log.debug(30, "CHARACTERS");
    }
    
    
    /**
     * SAX Handler that is called for each XML text node that is Ignorable
     * white space
     */
    public void ignorableWhitespace(char[] cbuf, int start, int len)
            throws SAXException
    {
        // When validation is turned "on", white spaces are reported here
        // When validation is turned "off" white spaces are not reported here,
        // but through characters() callback
        Log.debug(30, "IGNORABLEWHITESPACE");

    }

    
    /**
     * Method for handling errors during a parse. Implements ErrorHandler
     *
     * @param exception         The parsing error
     * @exception SAXException  Description of Exception
     */
    public void error(SAXParseException exception) throws SAXException
    {
      //throw exception;
    	Log.debug(30, "Error: " +exception.getMessage());
    }
    
    /**
     * Method for handling fatal errors during a parse. Implements ErrorHandler.
     *
     * @param exception         The parsing error
     * @exception SAXException  Description of Exception
     */
    public void fatalError(SAXParseException exception) throws SAXException
    {
      //throw exception;
    	Log.debug(30, "Fatal Error: " +exception.getMessage());
    }

    /**
     * Method for handling warnings during a parse. Implements ErrorHandler
     *
     * @param exception         The parsing error
     * @exception SAXException  Description of Exception
     */
    public void warning(SAXParseException exception) 
      throws SAXException
    {
      //throw new SAXException("WARNING: " + exception.getMessage());
    	Log.debug(30, "Warning: " +exception.getMessage());
    }
}
