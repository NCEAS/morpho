/**
 *        Name: ApplyXPath.java
 *     Purpose: A Class for creating a Query JavaBean for use Desktop Client
 *    source code essentially unchanged from Apache version
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: ApplyXPath.java,v 1.1 2000-07-12 19:47:44 higgins Exp $'
 */


/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
// This file uses 4 space indents, no tabs.


package edu.ucsb.nceas.querybean;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xalan.xpath.xml.FormatterToXML;
import org.apache.xalan.xpath.xml.TreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

/**
 *  Very basic utility for applying an XPath epxression to an xml file and printing information
 /  about the execution of the XPath object and the nodes it finds.
 *  Takes 2 arguments:
 *     (1) an xml filename
 *     (2) an XPath expression to apply to the file
 *  Examples:
 *     java ApplyXPath foo.xml /
 *     java ApplyXPath foo.xml /doc/name[1]/@last
 * @see XPathAPI
 */
public class ApplyXPath
{
  protected String filename = null;
  protected String xpath = null;

  /** Process input args and execute the XPath.  */
  public void doMain(String[] args)
  {
    filename = args[0];
    xpath = args[1];

    if ((filename != null) && (filename.length() > 0)
        && (xpath != null) && (xpath.length() > 0))
    {
      InputSource in;
      try
      {
        in = new InputSource(new FileInputStream(filename));
      }
      catch (FileNotFoundException fnf)
      {
        System.err.println("FileInputStream of " + filename + " threw: " + fnf.toString());
        fnf.printStackTrace();
        return;
      }

      // Use a DOMParser from Xerces so we get a complete DOM from the document
      DOMParser parser = new DOMParser();
      try
      {
        parser.parse(in);
      }
      catch(Exception e1)
      {
        System.err.println("Parsing " + filename + " threw: " + e1.toString());
        e1.printStackTrace();
        return;
      }

      // Get the documentElement from the parser, which is what the selectNodeList method expects
      Node root = parser.getDocument().getDocumentElement();
      NodeList nl = null;
      try
      {
        // Use the simple XPath API to select a node.
        nl = XPathAPI.selectNodeList(root, xpath);
        
        // Use the FormatterToXML class right not instead of 
        // the Xerces Serializer classes, because I'm not sure 
        // yet how to make them handle arbitrary nodes.
        FormatterToXML fl = new FormatterToXML(System.out);
        TreeWalker tw = new TreeWalker(fl);
        int n = nl.getLength();
        for(int i = 0; i < n; i++)
        {
          tw.traverse(nl.item(i));
          // We have to do both a flush and a flushWriter here, 
          // because the FormatterToXML rightly does not flush 
          // until it get's an endDocument, which usually will 
          // not happen here.
          fl.flush();
          fl.flushWriter();
        }
      }
      catch (Exception e2)
      {
        System.err.println("selectNodeList threw: " + e2.toString() + " perhaps your xpath didn't select any nodes");
        e2.printStackTrace();
        return;
      }
      

    }
    else
    {
      System.out.println("Bad input args: " + filename + ", " + xpath);
    }
  }

  /** Main method to run from the command line.    */
  public static void main (String[] args)
  {
    if (args.length != 2)
    {
      System.out.println("java ApplyXPath filename.xml xpath\n"
                         + "Reads filename.xml and applies the xpath; prints the nodelist found.");
      return;
    }
    ApplyXPath app = new ApplyXPath();
    System.out.println("<output>");
    app.doMain(args);
    System.out.println("</output>");
  }
  
} // end of class ApplyXPath

