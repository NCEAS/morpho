/**
 *  '$RCSfile: XMLTransformer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-19 19:16:00 $'
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

package edu.ucsb.nceas.morpho.util;

import java.io.Reader;
import java.io.IOException;
import java.io.StringReader;
import java.io.PipedReader;
import java.io.PipedWriter;

        //import java.net.URL;
        //import java.net.MalformedURLException;
        //import java.sql.*;
        //import java.util.Stack;
        //
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.stream.StreamSource;
//import javax.xml.transform.stream.StreamResult;
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
 *  XMLTransformer that builds a display panel to display metadata.  Given a String ID, 
 *  does a lookup using a factory that must also be provided (and which 
 *  implements the ContentFactoryInterface) to get the XML document to display.  
 *  Then styles this document accordingly using XSLT, before displaying it in an 
 *  embedded HTML display.
 */
public class XMLTransformer
{
    /**
     *  Checks the passed XML document for a stylesheet ref, and uses that 
     *  stylesheet to apply XSLT to the XML, returning the result as a 
     *  <code>java.io.Reader</code>.
     *
     *  @param xmlDocument  A <code>java.io.Reader</code> to allow reading of
     *                      the XML document to be styled.  Note XML doc must 
     *                      contain a stylesheet so this class can determine 
     *                      which stylesheet to use
     *
     *  @return             A <code>java.io.Reader</code> to allow reading of
     *                      the results of styling the XML document
     *
     *  @throws IOException if there are problems reading the Reader
     */
    public Reader transform(Reader xmlDocument) throws IOException
    {
        return new StringReader("XMLTransformer: method not implemented!");
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
//        try {
//            TransformerFactory tFactory = TransformerFactory.newInstance();
//            Transformer transformer     
//                = tFactory.newTransformer( new StreamSource(xslStyleSheet) );
//    //        transformer.setParameter("qformat", qformat);
//            transformer.transform(  new StreamSource(xmlDocument), 
//                                    new StreamResult(pipedWriter));
//        } catch (Exception e) {
//            String msg = "XMLTransformer.transform(): Error transforming document"
//                                                              +e.getMessage();
//            pipedWriter.write(msg.getBytes(),0,msg.length());
//        }

        String msg = "XMLTransformer not implemented!";
        pipedWriter.write(msg.toCharArray(),0,msg.length());
            
        return new PipedReader(pipedWriter);
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

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//
//    
//
//    /**
//    * Transform an XML document using the stylesheet reference from the db
//    *
//    * @param doc the document to be transformed
//    * @param sourcetype the document type of the source
//    * @param targettype the target document type
//    * @param qformat the name of the style set to use
//    * @param pw the PrintWriter to which output is printed
//    */
//    public void transformXMLDocument(String doc, String sourceType, 
//        String targetType, String qformat, PrintWriter pw)
//        {
//
//        // Look up the stylesheet for this type combination
//        String xslSystemId = getStyleSystemId(qformat, sourceType, targetType);
//
//        if (xslSystemId != null)
//            {
//            // Create a stylesheet from the system id that was found
//            try
//                {
//                TransformerFactory tFactory = TransformerFactory.newInstance();
//                Transformer transformer = tFactory.newTransformer(
//                    new StreamSource(xslSystemId));
//                transformer.setParameter("qformat", qformat);
//                transformer.transform(new StreamSource(new StringReader(doc)), 
//                    new StreamResult(pw));
//                } catch (Exception e)
//                {
//                pw.println(xslSystemId + "Error transforming document in " +
//                    "DBTransform.transformXMLDocument: " +
//                    e.getMessage());
//
//                }
//            } else
//        {
//            // No stylesheet registered form this document type, so just return the 
//            // XML stream we were passed
//            pw.print(doc);
//        }
//        }
//
//    /**
//    * Transform an XML document to StringWriter using the stylesheet reference 
//    * from the db
//    * @param doc the document to be transformed
//    * @param sourceType the document type of the source
//    * @param targetType the target document type
//    * @param qFormat the name of the style set to use
//    * @param pw the StringWriter to which output will be stored
//    */
//    public void transformXMLDocument(String doc, String sourceType,
//        String targetType, String qFormat, StringWriter pw)
//        {
//
//        // Look up the stylesheet for this type combination
//        String xslSystemId = getStyleSystemId(qFormat, sourceType, targetType);
//
//        if (xslSystemId != null)
//            {
//            // Create a stylesheet from the system id that was found
//            try
//                {
//                TransformerFactory tFactory = TransformerFactory.newInstance();
//                Transformer transformer = tFactory.newTransformer(
//                    new StreamSource(xslSystemId));
//                transformer.setParameter("qFormat", qFormat);
//                transformer.transform(new StreamSource(new StringReader(doc)),
//                    new StreamResult(pw));
//                } catch (Exception e)
//                {
//                util.debugMessage(xslSystemId + "Error transforming document in " +
//                    "DBTransform.transformXMLDocument: " +
//                    e.getMessage());
//
//                }
//            } else
//        {
//            // No stylesheet registered form this document type, so just return the 
//            // XML stream we were passed
//            pw.write(doc);
//        }
//        }
//
//    /**
//    * gets the content of a tag in a given xml file with the given path
//    * @param f the file to parse
//    * @param path the path to get the content from
//    */
//    public static NodeList getPathContent(File f, String path)
//        {
//        if(f == null)
//            {
//            return null;
//            }
//
//        DOMParser parser = new DOMParser();
//        InputSource in;
//        FileInputStream fs;
//
//        try
//            { 
//            fs = new FileInputStream(f);
//            in = new InputSource(fs);
//            }
//        catch(FileNotFoundException fnf)
//            {
//            fnf.printStackTrace();
//            return null;
//            }
//
//        try
//            {
//            parser.parse(in);
//            fs.close();
//            }
//        catch(Exception e1)
//            {
//            System.err.println("File: " + f.getPath() + " : parse threw: " + 
//                e1.toString());
//            return null;
//            }
//
//        Document doc = parser.getDocument();
//
//        try
//            {
//            NodeList docNodeList = XPathAPI.selectNodeList(doc, path);
//            return docNodeList;
//            }
//        catch(Exception se)
//            {
//            System.err.println("file: " + f.getPath() + " : parse threw: " + 
//                se.toString());
//            return null;
//            }
//        }
//
//    /**
//    * Lookup a stylesheet reference from the db catalog
//    *
//    * @param qformat    the named style-set format
//    * @param sourcetype the document type of the source
//    * @param targettype the document type of the target
//    */
//    public String getStyleSystemId(String qformat, String sourcetype, 
//        String targettype)
//        {
//        String systemId = null;
//
//        if ((qformat == null) || (qformat.equals("html")))
//            {
//            qformat = defaultStyle;
//            }
//
//        // Load the style-set map for this qformat into a DOM
//        try
//            {
//            boolean breakflag = false;
//            String filename = configDir + "/" + qformat + ".xml";       
//            util.debugMessage("Trying style-set file: " + filename);
//            File f = new File(filename);
//            NodeList nlDoctype = getPathContent(f, "/style-set/doctype");
//            NodeList nlDefault = getPathContent(f, "/style-set/default-style");
//            Node nDefault = nlDefault.item(0);
//            systemId = nDefault.getFirstChild().getNodeValue(); //set the default
//
//            for(int i=0; i<nlDoctype.getLength(); i++)
//                { //look for the right sourcetype
//                Node nDoctype = nlDoctype.item(i);
//                NamedNodeMap atts = nDoctype.getAttributes();
//                Node nAtt = atts.getNamedItem("publicid");
//                String doctype = nAtt.getFirstChild().getNodeValue();
//                if(doctype.equals(sourcetype))
//                    { //found the right sourcetype now we need to get the target type
//                    NodeList nlChildren = nDoctype.getChildNodes();
//                    for(int j=0; j<nlChildren.getLength(); j++)
//                        {
//                        Node nChild = nlChildren.item(j);
//                        String childName = nChild.getNodeName();
//                        if(childName.equals("target"))
//                            {
//                            NamedNodeMap childAtts = nChild.getAttributes();
//                            Node nTargetPublicId = childAtts.getNamedItem("publicid");
//                            String target = nTargetPublicId.getFirstChild().getNodeValue();
//                            if(target.equals(targettype))
//                                { //we found the right target type
//                                NodeList nlTarget = nChild.getChildNodes();
//                                for(int k=0; k<nlTarget.getLength(); k++)
//                                    {
//                                    Node nChildText = nlTarget.item(k);
//                                    if(nChildText.getNodeType() == Node.TEXT_NODE)
//                                        { //get the text from the target node
//                                        systemId = nChildText.getNodeValue();
//                                        breakflag = true;
//                                        break;
//                                        }
//                                    }
//                                }
//                            }
//
//                        if(breakflag)
//                            {
//                            break;
//                            }
//                        }
//                    }
//
//                if(breakflag)
//                    {
//                    break;
//                    }
//                }
//            }
//        catch(Exception e)
//            {
//            System.out.println("Error parsing style-set file: " + e.getMessage());
//            e.printStackTrace();
//            }
//
//        // Return the system ID for this particular source document type
//        return systemId;
//        }
//
//    /**
//    * Lookup a stylesheet reference from the db catalog
//    *
//    * @param objecttype the type of the object we want to retrieve
//    * @param sourcetype the document type of the source
//    * @param targettype the document type of the target
//    */
//    public String getSystemId(String objecttype, String sourcetype, 
//        String targettype)
//        {
//
//        // Look up the System ID of a particular object
//        PreparedStatement pstmt = null;
//        String the_system_id = null;
//        DBConnection dbConn = null;
//        int serialNumber = -1;
//        try
//            {
//            dbConn=DBConnectionPool.
//                getDBConnection("DBTransform.getSystemId");
//            serialNumber=dbConn.getCheckOutSerialNumber();
//            pstmt =
//                dbConn.prepareStatement("SELECT system_id " +
//                "FROM xml_catalog " +
//                "WHERE entry_type = ? " +
//                "AND source_doctype = ? " +
//                "AND target_doctype = ? ");
//            // Bind the values to the query
//            pstmt.setString(1, objecttype);
//            pstmt.setString(2, sourcetype);
//            pstmt.setString(3, targettype);
//            pstmt.execute();
//            try
//                {
//                ResultSet rs = pstmt.getResultSet();
//                try
//                    {
//                    boolean tableHasRows = rs.next();
//                    if (tableHasRows)
//                        {
//                        try
//                            {
//                            the_system_id = rs.getString(1);
//                            } catch (SQLException e)
//                            {
//                            System.out.println("Error with getString in " + 
//                                "DBTransform.getSystemId: " + e.getMessage());                
//                            }
//                        } else
//                    {
//                        the_system_id = null; 
//                    }
//                    } catch (SQLException e)
//                    {
//                    System.err.println("Error with next in DBTransform.getSystemId: " + 
//                        e.getMessage());
//                    return ("Error with next: " + e.getMessage());
//                    }
//                } catch (SQLException e)
//                {
//                System.err.println("Error with getrset in DBTransform.getSystemId: " + 
//                    e.getMessage());
//                return ("Error with getrset: " + e.getMessage());
//                }
//            pstmt.close();
//            } catch (SQLException e)
//            {
//            System.err.println("Error getting id in DBTransform.getSystemId: " + 
//                e.getMessage());
//            return ("Error getting id in DBTransform.getSystemId:: " + 
//                e.getMessage());
//            }
//        finally
//            {
//            try
//                {
//                pstmt.close();
//                }//try
//            catch (SQLException sqlE)
//                {
//                MetaCatUtil.debugMessage("Error in DBTransform.getSystemId: "
//                    +sqlE.getMessage(), 30);
//                }//catch
//            finally
//                {
//                DBConnectionPool.returnDBConnection(dbConn, serialNumber);
//                }//finally
//            }//finally
//        return the_system_id;
//        }
//
//    /**
//    * the main routine used to test the transform utility.
//    *
//    * Usage: java DBTransform
//    */
//    static public void main(String[] args)
//        {
//
//        if (args.length > 0)
//            {
//            System.err.println("Wrong number of arguments!!!");
//            System.err.println("USAGE: java DBTransform");
//            return;
//            } else
//        {
//            try
//                {
//
//                // Open a connection to the database
//                /*MetaCatUtil   util = new MetaCatUtil();
//                Connection dbconn = util.openDBConnection();*/
//
//                // Create a test document
//                StringBuffer testdoc = new StringBuffer();
//                testdoc.append("<?xml version=\"1.0\"?>");
//                testdoc.append("<eml-dataset><metafile_id>NCEAS-0001</metafile_id>");
//                testdoc.append("<dataset_id>DS001</dataset_id>");
//                testdoc.append("<title>My test doc</title></eml-dataset>");
//
//                // Transform the document to the new doctype
//                DBTransform dbt = new DBTransform();
//                dbt.transformXMLDocument(testdoc.toString(), 
//                    "-//NCEAS//eml-dataset//EN", 
//                    "-//W3C//HTML//EN", 
//                    "knb",
//                    new PrintWriter(System.out));
//
//                } catch (Exception e)
//                {
//                System.err.println("EXCEPTION HANDLING REQUIRED");
//                System.err.println(e.getMessage());
//                e.printStackTrace(System.err);
//                }
//        }
//        }
//
//    private void dbg(int position)
//        {
//        System.err.println("Debug flag: " + position);
//        }
//}
