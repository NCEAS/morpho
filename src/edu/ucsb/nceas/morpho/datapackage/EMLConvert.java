/**
 *  '$RCSfile: EMLConvert.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-09-23 23:28:37 $'
 * '$Revision: 1.8 $'
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

/**
 * The purpose of this class is to transform an emlbeta6 package to an eml2 document.
 * This process is done using two succesive xslt transformations.
 *
 * The 'main' class runs the transformation from the command line. The path to the
 * top level beta6 dataset module must be passed on the command line. All the associated
 * modules in the package are assumed to be in the same directory and the file names are
 * the ids used in the beta6 triples (e.g. "higgins.232.3")
 */

package edu.ucsb.nceas.morpho.datapackage;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xalan.templates.OutputProperties;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.ucsb.nceas.morpho.util.Log;

public class EMLConvert
{

  public static String outputfileName = "eml2out.xml";
  static String path = "";
  static String fname = "";

  public static String indentAmount = "2";

  public static void setIndentAmount(String indentAmount) {
    EMLConvert.indentAmount = indentAmount;
  }

  /**
   * Method main
   */
  public static void main(String argv[])
            throws TransformerException, TransformerConfigurationException, Exception {
    if (argv.length<1) {
      Log.debug(20, "Must have an argument with name/path of dataset module");
      System.exit(0);
    }
    doTransform(argv[0], "");
  }

  public static void doTransform(String datasetID, String metacatURL)
                 throws TransformerException, TransformerConfigurationException, Exception{
  // Instantiate  a TransformerFactory.
  	TransformerFactory tFactory = TransformerFactory.newInstance();
    URIResolver res = new MyURIResolver();


    tFactory.setURIResolver(res);


    // Determine whether the TransformerFactory supports The use uf SAXSource
    // and SAXResult
    if (tFactory.getFeature(SAXSource.FEATURE) && tFactory.getFeature(SAXResult.FEATURE))
    {
      // Cast the TransformerFactory to SAXTransformerFactory.
      SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) tFactory);
      // Create a TransformerHandler for each stylesheet.

      // files created since using a string for the new StreamSourece objects
      // does not work correctly if there is a space in the path!
      File f1 = new File("./xsl/triple_info.xsl");
      java.io.FileInputStream fis = new FileInputStream(f1);

      File f2 = new File("./xsl/emlb6toeml2.xsl");
      TransformerHandler tHandler1 = saxTFactory.newTransformerHandler(new StreamSource(f1));
      TransformerHandler tHandler2 = saxTFactory.newTransformerHandler(new StreamSource(f2));

      Transformer tr = tHandler1.getTransformer();
      Transformer tr1 = tHandler2.getTransformer();

      getPathInfo(datasetID);
      if (path.length()>0) {
         if (!path.startsWith("file://")) {
            path = "file:///" + path;
         }
         MyURIResolver.setDataDefault(path);
      }
      tr.setParameter("packageName", fname);
      tr1.setParameter("metacatURL", metacatURL);

      Transformer lastTransformer = tHandler2.getTransformer();
      lastTransformer.setOutputProperty(OutputProperties.S_KEY_INDENT_AMOUNT, EMLConvert.indentAmount);


      // Create an XMLReader.
	    XMLReader reader = XMLReaderFactory.createXMLReader();
      reader.setContentHandler(tHandler1);
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", tHandler1);

      tHandler1.setResult(new SAXResult(tHandler2));

//      StringWriter writer = new StringWriter();
      File outfile = new File(outputfileName);
      Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), Charset.forName("UTF-8")));
      Result result = new StreamResult(writer);

      tHandler2.setResult(result);

	    // Parse the XML input document. The input ContentHandler and output ContentHandler
      // work in separate threads to optimize performance.

      reader.parse(new InputSource(fis));
//      String resultString = writer.toString();
//      System.out.println(resultString);
    }
  }

  private static void getPathInfo1(String str) {
    int pos = -1;
    if ((str.indexOf("/")>-1)||(str.indexOf("\\")>-1)) {
      int pos1 = str.lastIndexOf("/");
      int pos2 = str.lastIndexOf("\\");
      if (pos1>pos2) {
        pos = pos1;
      }
      else { pos = pos2; }
      path = str.substring(0,pos+1);
      fname = str.substring(pos+1,str.length());
    }
    else {
      fname = str;
    }
//   System.out.println("path: "+path+"  --fname: "+fname);
  }

  private static void getPathInfo(String str) {
    File nf = new File(str);
    if (nf.exists()) {
      fname = nf.getName();
      path = nf.getAbsolutePath();
      Log.debug(20, "path: "+path+"  --fname: "+fname);
    }
  }

}
