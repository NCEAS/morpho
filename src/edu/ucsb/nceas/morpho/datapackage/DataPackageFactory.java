/**
 *  '$RCSfile: DataPackageFactory.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-08-21 22:58:46 $'
 * '$Revision: 1.3 $'
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
import java.io.*;

import edu.ucsb.nceas.morpho.util.Log;

/**
 * class (factory) for creating a new DataPackage
 */
public class DataPackageFactory
{
  /**
   *  a uri in string form characterizing the docType of the current document
   */
  static private String docType = null;
  
  /**
   *  Create a new Datapackage given a Reader to a metadata stream
   *  location is given by 2 booleans
   */
  public static AbstractDataPackage getDataPackage(Reader in, boolean metacat, boolean local) {
    // read the stream. figure out the docType(i.e. emlbeta6, eml2, nbii, etc)
    // then create the appropriate subclass of AbstractDataPackage and return it.
    
    // temporary stub!!!
    Log.debug(1,"DocTypeInfo: " + getDocTypeInfo(in));
    AbstractDataPackage dp = new EML200DataPackage();
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
    return temp;
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
      File f = new File("test.xml");
      FileReader in = new FileReader(f);
      DataPackageFactory.getDataPackage(in, false, true);
      in.close();
      System.exit(0);
    } catch (Exception w) {}
  }

}
