/**
 *  '$RCSfile: DataPackageFactory.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-08-21 17:58:57 $'
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
import java.io.Reader;

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
  private static void getDocTypeInfo(Reader in) {
    
  }

}
