/**
 *  '$RCSfile: XMLTransformer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-02 16:54:25 $'
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

package edu.ucsb.nceas.morpho.util;

import java.io.Reader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.PipedReader;
import java.io.PipedWriter;

        //import java.net.URL;
        //import java.net.MalformedURLException;
        //import java.sql.*;
        //import java.util.Stack;
        //
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
        //import javax.xml.transform.TransformerException;
        //import javax.xml.transform.TransformerConfigurationException;
        //
        //import org.apache.xerces.parsers.DOMParser;
        //import org.w3c.dom.Attr;
        //import org.w3c.dom.NamedNodeMap;
        //import org.w3c.dom.NodeList;
        //import org.w3c.dom.Document;
        //import org.w3c.dom.Node;
        //import org.w3c.dom.NodeList;
        //import org.w3c.dom.DocumentType;
        //import org.xml.sax.SAXException;
        //import org.xml.sax.InputSource;
        //import org.apache.xerces.dom.DocumentTypeImpl;
        //import org.apache.xpath.XPathAPI;
        //import org.w3c.dom.NamedNodeMap;
        //
        //import org.w3c.dom.Document;
        //import org.w3c.dom.Node;
        //import org.w3c.dom.Element;
        //import org.xml.sax.SAXException;
//
//  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *



/**
 *  XMLTransformer to style XML documents using XSLT
 */
public class XMLTransformer
{
    private static XMLTransformer instance;

    private XMLTransformer() {}
    
    public static XMLTransformer getInstance() 
    {
        if (instance==null) {
            instance=new XMLTransformer();
        }
        return instance;
    }
    
    /**
     *  transforms the passed XML document according to a stylesheet reference 
     *  data contained within it
     *
     *  @param xmlDocument  A <code>java.io.Reader</code> to allow reading of
     *                      the XML document to be styled.  Note XML doc must 
     *                      contain a stylesheet reference so this class can 
     *                      determine which stylesheet to use
     *
     *  @return             A <code>java.io.Reader</code> to allow reading of
     *                      the (character-based) results of styling the XML 
     *                      document
     *
     *  @throws IOException if there are problems reading the Reader
     */
    public Reader transform(Reader xmlDocument) throws IOException
    {
        throw new IOException("XMLTransformer: method not implemented!");
    }
    
    /**
     *  Uses the stylesheet provided, to apply XSLT to the XML document provided
     *
     *  @param xmlDocument  A <code>java.io.Reader</code> to allow reading of
     *                      the XML document to be styled.
     *
     *  @param xslStyleSheet  A <code>java.io.Reader</code> to allow reading of
     *                      the XSL stylesheet to be used
     *
     *  @return             A <code>java.io.Reader</code> to allow reading of
     *                      the results of styling the XML document
     *
     *  @throws IOException if there are problems reading either of the Readers
     */
    public Reader transform(Reader xmlDocument, Reader xslStyleSheet)
                                                            throws IOException
    {
        if (xmlDocument==null) {
            throw new IOException("XMLTransformer.transform():"
                                   +" received NULL Reader for XML document");
        } else if (xslStyleSheet==null) {
            throw new IOException("XMLTransformer.transform():"
                                   +" received NULL Reader for XSL stylesheet");
        }
        PipedWriter pipedWriter = new PipedWriter();
        StringWriter stringWriter = new StringWriter();
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer     
                = tFactory.newTransformer( new StreamSource(xslStyleSheet) );
    //        transformer.setParameter("qformat", qformat);
            transformer.transform(  new StreamSource(xmlDocument), 
                                    new StreamResult(stringWriter));
//                                    new StreamResult(pipedWriter));
        } catch (Exception e) {
            String msg 
                = "XMLTransformer.transform(): Error transforming document"
                                                                +e.getMessage();
            e.printStackTrace();
            pipedWriter.write(msg.toCharArray(),0,msg.length());
        }
//        return new PipedReader(pipedWriter);
        return new StringReader(stringWriter.toString());
    }
    
    /**
     *  Uses the stylesheet provided, to apply XSLT to the XML document provided
     *
     *  @param xmlDocument  A <code>java.io.Reader</code> to allow reading of
     *                      the XML document to be styled.
     *
     *  @param directory    A <code>XSLLookupInterface</code> to allow this
     *                      transformer to get the required XSL document, given
     *                      the DocType in the XML doc to be styled
     *
     *  @return             A <code>java.io.Reader</code> to allow reading of
     *                      the results of styling the XML document
     *
     *  @throws IOException if there are problems reading the Reader
     *  @throws DocumentNotFoundException if XSLLookupInterface does not return 
     *  a valid reference to a suitable XSL stylesheet
     */
//    public Reader transform(Reader xmlDocument, XSLLookupInterface directory)
//                                  throws IOException, DocumentNotFoundException
//    {
//        return new StringReader("XMLTransformer: method not implemented!");
//    }
}