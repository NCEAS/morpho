/**
 *  '$RCSfile: DataPackageFactory.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-09-23 23:33:35 $'
 * '$Revision: 1.13 $'
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

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;

import org.xml.sax.InputSource;

import java.util.Vector;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.io.*;
import edu.ucsb.nceas.morpho.Morpho;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.*;


/**
 * class (factory) for creating a new DataPackage
 */
public class DataPackageFactory
{
  /**
   *  a uri in string form characterizing the docType of the current document
   */
  static private String docType = null;
  
  static private Morpho morpho = null;
  
  /**
   *  Create a new Datapackage given a Reader to a metadata stream
   *  location is given by 2 booleans
   */
  public static AbstractDataPackage getDataPackage(Reader in, boolean metacat, boolean local) {
    // read the stream. figure out the docType(i.e. emlbeta6, eml2, nbii, etc)
    // then create the appropriate subclass of AbstractDataPackage and return it.
    
    // temporary stub!!!
    AbstractDataPackage dp = null;
    String type = getDocTypeInfo(in);
    Log.debug(1,"DocTypeInfo: " + type);
    if (type.equals("eml:eml")) {
      Log.debug(1,"Creating new eml2.0.0 package");
      dp = new EML200DataPackage();
      Log.debug(1,"loading new eml2.0.0 DOM");
      dp.load("local","jscientist.7.1",null);
      try{
        Node textNode = XMLUtilities.getTextNodeWithXPath(dp.getMetadataPath(),"/xpathKeyMap/contextNode[@name='package']/title");
        String test = textNode.getNodeValue();
        Log.debug(1,"test:"+test);
        String temp = dp.getGenericValue("/xpathKeyMap/contextNode[@name='package']/title");
        Log.debug(1,"temp:"+temp);
      }
      catch (Exception w) {
        Log.debug(1,"exception");
      }
    }
    else if (type.indexOf("eml-dataset-2.0.0beta6")>-1) {
      dp = new EML2Beta6DataPackage();
    }
    return dp;
  }
  
   /**
   *  Create a new Datapackage given an id of a metadata stream
   *  location is given by 2 booleans
   */
  public static AbstractDataPackage getDataPackage(String docid, boolean metacat, boolean local) {
    // first use datastore package to get a stream for the metadata
    // read the stream. figure out the docType(i.e. emlbeta6, eml2, nbii, etc)
    // then create the appropriate subclass of AbstractDataPackage and return it.
    
    // temporary stub!!!
    AbstractDataPackage dp = new EML200DataPackage();
    
    String location = null;
    if (metacat && !local) location = AbstractDataPackage.METACAT;
    if (!metacat && local) location = AbstractDataPackage.LOCAL;
    if (metacat && local) location = AbstractDataPackage.BOTH;
    dp.load(location, docid, morpho);
/*      try{
        Node textNode = XMLUtilities.getTextNodeWithXPath(dp.getMetadataPath(),"/xpathKeyMap/contextNode[@name='package']/title");
        String test = textNode.getNodeValue();
        Log.debug(1,"test:"+test);
        String temp = dp.getGenericValue("/xpathKeyMap/contextNode[@name='package']/title");
        Log.debug(1,"temp:"+temp);
      }
      catch (Exception w) {
        Log.debug(1,"exception");
      }
*/
   Log.debug(1,"Title: "+dp.getTitle());
   Log.debug(1,"Author: "+dp.getAuthor());
   Log.debug(1,"AccessionNumber: "+dp.getAccessionNumber());
   Log.debug(1,"Keyword: "+dp.getKeywords());
   dp.getEntityArray();
   Log.debug(1, "entity 1 name: "+dp.getEntityName(0));
   Log.debug(1, "entity 2 name: "+dp.getEntityName(1));
   Log.debug(1, "entity 1 numRecords: "+dp.getEntityNumRecords(0));
   Log.debug(1, "entity 1 description: "+dp.getEntityDescription(0));
   Log.debug(1, "entity 0;attribute0 name: "+dp.getAttributeName(0,0));
   Log.debug(1, "entity 0;attribute1 name: "+dp.getAttributeName(0,1));
   Log.debug(1, "entity 0;physical0 name: "+dp.getPhysicalName(0,0));
   Log.debug(1, "entity 0;physical0 name: "+dp.getPhysicalFormat(0,0));
   
    return dp;    
  }


  /**
   *  given a node in a DOM (the root?), create an AbstractDataPackage object
   *  needed for use with DPWizard?
   */
  public static AbstractDataPackage getDataPackage(Node node, String doctype) {
    AbstractDataPackage dp = null;
    // no 'load' operation is required
    
//    this.doctype = doctype;
    dp.grammar = doctype;
    dp.metadataNode = node;
    return dp;
  }
  
  /**
   *  reads the stream and tries to determine the docType. If there is a read docType,
   *  (i.e. a DocType element in the xml) then the publicID is stored, if available. If not
   *  then the systemId. If no docType, then look for the nameSpace of the root element; otherwise
   *  record the root element name
   */
  private static String getDocTypeInfo(Reader in) {
    String temp = getSchemaLine(in,2);
    Log.debug(1,"line is:"+temp);
    // this should return a line of text which is either the DOCTYPE declaraton or the root node
    if (temp.indexOf("DOCTYPE")>-1) {
      // get PUBLIC and/or SYSRWM values
      if(temp.indexOf("PUBLIC")>-1) {
        temp = temp.substring(temp.indexOf("PUBLIC"));
        StringTokenizer st = new StringTokenizer(temp," ");
        if(st.countTokens()>1) {
          String temp1 = st.nextToken(); // should be 'PUBLIC'
          temp1 = st.nextToken();
          docType = temp1;
        }
      }
      else if(temp.indexOf("SYSTEM")>-1){
        temp = temp.substring(temp.indexOf("SYSTEM"));
        StringTokenizer st = new StringTokenizer(temp," ");
        if(st.countTokens()>1) {
          String temp1 = st.nextToken(); // should be 'SYSTEM'
          temp1 = st.nextToken();
          docType = temp1;
        }
      }
    }
    else {
      // assume that this is the root node and look for NS information
      StringTokenizer st = new StringTokenizer(temp," ");
      String temp1 = st.nextToken();
      
      // collect all the NS declarations
      Vector ns_vec = new Vector();
      int start = -1;
      // should correlate NS declarations with NS abbreviation in root node element name
      // for now, just return the root node name
      docType = temp1;
      Log.debug(1,"temp1: "+temp1);
    }
    return docType;
  }

  
  // 'borrowed' from MetaCatServlet class of metacat
  // this method should return everything inside the linenum set of angle brackets
    private static String getSchemaLine(Reader xml, int linenum)   {
    // find the line
    String secondLine = null;
    int count =0;
    int endIndex = 0;
    int startIndex = 0;
    StringBuffer buffer = new StringBuffer();
    boolean comment =false;
    char thirdPreviousCharacter = '?';
    char secondPreviousCharacter ='?';
    char previousCharacter = '?';
    char currentCharacter = '?';
    
    try {
    
      while ( (currentCharacter = (char) xml.read()) != -1)
      {
        //in a comment
        if (currentCharacter =='-' && previousCharacter == '-'  && 
          secondPreviousCharacter =='!' && thirdPreviousCharacter == '<')
        {
          comment = true;
        }
        //out of comment
        if (comment && currentCharacter == '>' && previousCharacter == '-' && 
          secondPreviousCharacter =='-')
        {
           comment = false;
        }
      
        //this is not comment
        if (previousCharacter =='<'  && !comment)
        {
          count ++;
        }
        // get target line
        if (count == linenum && currentCharacter !='>')
        {
          buffer.append(currentCharacter);
        }
        if (count == linenum && currentCharacter == '>')
        {
            break;
        }
        thirdPreviousCharacter = secondPreviousCharacter;
        secondPreviousCharacter = previousCharacter;
        previousCharacter = currentCharacter;
      
      }
      secondLine = buffer.toString();
      Log.debug(25, "the second line string is: "+secondLine);
//      xml.reset();
      xml.close();
    } catch (Exception e) {Log.debug(6, "Error in getSchemaLine!");}
    return secondLine;
  }
  
  static public void main(String args[]) {
    try{
      Morpho.createMorphoInstance();
      DataPackageFactory.getDataPackage("jscientist.7.1", false, true);
      System.exit(0);
    } catch (Exception w) {}
  }

}
