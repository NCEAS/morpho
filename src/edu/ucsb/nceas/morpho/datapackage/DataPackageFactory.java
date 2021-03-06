/**
 *  '$RCSfile: DataPackageFactory.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-10-15 02:23:40 $'
 * '$Revision: 1.48 $'
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.editor.DocFrame;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * class (factory) for creating a new DataPackage
 */
public class DataPackageFactory
{

  /**
	 * Create a new datapackage given a Reader to a metadata stream
	 */
	public static AbstractDataPackage getDataPackage(Reader originalIn) {
		// read the stream. figure out the docType(i.e. emlbeta6, eml2, nbii,
		// etc)
		// then create the appropriate subclass of AbstractDataPackage and
		// return it.

		// mark so we can get back to the beginning of the stream
		BufferedReader in = new BufferedReader(originalIn);
		try {
			in.mark(2048);
		} catch (IOException ioe) {
			Log.debug(15, "!!ERROR!! Unable mark XML input stream");
		}
		AbstractDataPackage dp = null;
		String type = getDocTypeInfo(in);
		// Log.debug(15,"DocTypeInfo: " + type);
		// if (type.equals("eml:eml")) {
		if (type.indexOf("eml://ecoinformatics.org/eml-2.0") > -1
				|| type.indexOf("eml://ecoinformatics.org/eml-2.1") > -1) {
			//Log.debug(15,"Creating new eml-2.0.x package from metadata stream"
			// );
			dp = new EML200DataPackage();
			// Log.debug(15,"loading new eml-2.0.x DOM");
			try {
				in.reset();
			} catch (IOException ioe) {
				Log.debug(15, "!!ERROR!! Unable reset XML input stream");
			}
			dp.load(new InputSource(in));

		} else if ((type.indexOf("eml-dataset-2.0.0beta6") > -1)
				|| (type.indexOf("eml-dataset-2.0.0beta4") > -1)) {
			dp = new EML2Beta6DataPackage();
		}
		// make sure we have the id set
		dp.setAccessionNumber(dp.getAccessionNumber());
		try {
			dp.initializeSystemMetadata();
		} catch (Exception e) {
			Log.debug(15, "Could not initialize SystemMetadata");
			e.printStackTrace();
		}
		return dp;
	}
  
 
 /**
  * Loads an AbstractDataPackage from incomplete dir with the given docid.
  * @param docid
  * @return
  */
  public static AbstractDataPackage getDataPackageFromIncompeteDir(String docid) {
   
      Reader in = null;  
      InputSource source = null;
     try 
     {
        File file = Morpho.thisStaticInstance.getLocalDataStoreService().openIncompleteFile(docid);
        in = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
        source = new InputSource(in);
      }
      catch (Exception w) 
      {
    	  Log.debug(20,"Problem opening file!");
      }
    
    AbstractDataPackage dp = null;
    dp = new EML200DataPackage();
    dp.load(source);
    dp.setAutoSavedID(docid);
    // make sure we have the id set
	dp.setAccessionNumber(dp.getAccessionNumber());
	try {
		dp.initializeSystemMetadata();
	} catch (Exception e) {
		Log.debug(15, "Could not initialize SystemMetadata");
		e.printStackTrace();
	}
    return dp;
  }



  /**
   *  given the root node in a DOM, create an AbstractDataPackage object
   *  needed for use with DPWizard?
   */
  public static AbstractDataPackage getDataPackage(Node node) {
    AbstractDataPackage dp = null;
    String doctype = getDocType(node);
    //Log.debug(50, "doctype: "+doctype);
    if(doctype.indexOf("eml-2.0")>-1|| doctype.indexOf("eml-2.1")>-1) {
      // Note: assumed that this is ok for any 'eml-2.0.n' mod to eml2.0
      dp = new EML200DataPackage();

      try{
    	  Node metadataPathNode = null;
    	  if (doctype.indexOf("eml-2.0")>-1)
    	  {
    		  //System.out.println("the eml 200 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    		  metadataPathNode = XMLUtilities.getXMLAsDOMTreeRootNode("/eml200KeymapConfig.xml");
    	  }
    	  else
    	  {
    		  //System.out.println("the eml 210 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    		  metadataPathNode = XMLUtilities.getXMLAsDOMTreeRootNode("/eml210KeymapConfig.xml");
    	  }
    	  dp.setMetadataPath(metadataPathNode);
      }
      catch (Exception e2) {
        Log.debug(20, "getting DOM for Paths threw error: " + e2.toString());
        e2.printStackTrace();
      }


      dp.grammar = "eml:eml";
      dp.metadataNode = node;
    }
   // handlers for other types of documents hosuld be inserted here !!!
    if (dp==null) {
      Log.debug(1,"DOM document type is unknown! (DataPackaqeFactory.getDataPackage)");
    }
    
	// make sure we have the id set
    dp.setAccessionNumber(dp.getAccessionNumber());
    try {
		dp.initializeSystemMetadata();
	} catch (Exception e) {
		Log.debug(15, "Could not initialize SystemMetadata");
		e.printStackTrace();
	}
	 
    return dp;
  }
  

  /**
   *  reads the stream and tries to determine the docType. If there is a read docType,
   *  (i.e. a DocType element in the xml) then the publicID is stored, if available. If not
   *  then the systemId. If no docType, then look for the nameSpace of the root element; otherwise
   *  record the root element name
   *
   *  This method avoids creating a DOM in case the XML doc is very large (i.e. contains inline
   *  data)
   */
  private static String getDocTypeInfo(Reader in) {
    String temp = getSchemaLine(in);
    
    String docType = null;
	//Log.debug(1,"line is:"+temp);
    // this should return a line of text which is either the DOCTYPE declaraton or the root node
    if (temp.indexOf("DOCTYPE")>-1) {
      // get PUBLIC and/or SYSRWM values
      if(temp.indexOf("PUBLIC")>-1) {
        temp = temp.substring(temp.indexOf("PUBLIC"));
        StringTokenizer st = new StringTokenizer(temp," ");
        if(st.countTokens()>1) {
          String temp1 = st.nextToken(); // should be 'PUBLIC'
          temp1 = st.nextToken();
          docType  = temp1;
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
      //Log.debug(1,"first line is root node");
      StringTokenizer st = new StringTokenizer(temp," ");
      String temp1 = st.nextToken();

      // if node name has a : then it has a namespace declaration
      int colon_pos = temp1.indexOf(":");
      if (colon_pos>-1) {
        String ns_abrev = temp1.substring(0,colon_pos);
        int ns_dec_pos = temp.indexOf("xmlns:"+ns_abrev+"=\"");
        if (ns_dec_pos>-1) {
          int len = ("xmlns:"+ns_abrev+"=\"").length();
          int end = temp.indexOf("\"", ns_dec_pos+len+1);
          String ns = temp.substring(ns_dec_pos, end+1);
          String ns1 = ns.substring(ns.indexOf("\"")+1,ns.length()-1);
          Log.debug(40, "namespace: "+ ns1);
          docType = ns1;
        }
      } else {
        docType = temp1;
      }
    }
    return docType;
  }


  // 'borrowed' from MetaCatServlet class of metacat
  // this method should return everything inside the linenum set of angle brackets
    private static String getSchemaLine(Reader xml)   {
    int linenum = 1;
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
	int currentInt;

    try {
    	currentInt = xml.read();
    	currentCharacter = (char) currentInt;
      while (currentInt != -1)
      {
        //in a comment
        if (currentCharacter =='-' && previousCharacter == '-'  &&
          secondPreviousCharacter =='!' && thirdPreviousCharacter == '<')
        {
          count --;
          buffer = new StringBuffer();;
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
        
        //Log.debug(25, currentInt + ":" + currentCharacter);
        currentInt = xml.read();
    	currentCharacter = (char) currentInt;

      }
      secondLine = buffer.toString();
      Log.debug(25, "possible schema/doctype string is: " + secondLine);
      //skip over processing instructions and other lines starting with ?
      if (secondLine.startsWith("?")) {
    	  Log.debug(25, "recursing...");
    	  secondLine = getSchemaLine(xml);
      }
      //xml.reset();
     //xml.close();
    } catch (Exception e) {
    Log.debug(6, "Sorry - Unable to Open the Requested Data Package!");
    Log.debug(20, "Error in getSchemaLine!");
    }
    
    return secondLine;
  }

  /**
   *  This method is designed to try and determine the type of document
   *  the dom indicated by the rootNode 'rNode' represents
   */
  private static String getDocType(Node rNode) {
    Element rootNode = (Element)rNode;
    Document domDoc = rootNode.getOwnerDocument();
    Log.debug(50,"domDoc is: "+XMLUtilities.getDOMTreeAsString(rootNode));

    String identifier = null;

    //first try to get public DOCTYPE:
    if (domDoc.getDoctype()!=null) {
      identifier = domDoc.getDoctype().getPublicId();
      Log.debug(50,"getPublicId() gives: "+identifier);
    }
    //if this is null, then try to get schemaLocation:
    if (identifier==null || identifier.trim().equals("")) {
      identifier = rootNode.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                                               "schemaLocation");
      // since schema location string may contain multiple substrings
      // separated by spaces, we take only the first of these substrings:
      if ((identifier!=null) && ( !identifier.trim().equals(""))) {
        identifier = identifier.trim().substring(0, identifier.indexOf(" "));
          Log.debug(50,"getAttributeNS schemaLocation is: "+identifier);
      }
    }

    //if this is null, then try to get namespace of root node:
    if (identifier==null || identifier.trim().equals("")) {

      identifier = rootNode.getNamespaceURI();
      Log.debug(50,"rootNode.getNamespaceURI() gives: "+identifier);
    }

    //finally, if this is null, give up!
    if (identifier==null || identifier.trim().equals("")) {
      identifier = "no identifier";
      Log.debug(50,"no identifier - requesting generic stylesheet");
    }
    return identifier;
  }

  /**
   *  This is a static main method configured to test the class by
   *  creating a datapackage from a 'test' file with the id
   *  "jscientist.7.1".
   */
  static public void main(String args[]) {
    Node attrRoot = null;
    Node entRoot = null;
    OrderedMap om = new OrderedMap();
    OrderedMap om1 = new OrderedMap();
    AbstractDataPackage adp = null;
    Entity entityObject = null;
    Attribute attributeObject = null;
    try{
      Morpho.createMorphoTestInstance();
      MorphoDataPackage mdp = DataStoreServiceController.getInstance().read("jscientist.7.1", DataPackageInterface.LOCAL);

      // create a simple subtree to use to test coverage insertion
      Document doc = adp.getMetadataNode().getOwnerDocument();
      Node elem = (Node)(doc.createElement("temporalCoverage"));
      Node txt = (Node)doc.createTextNode("when");
      elem.appendChild(txt);

      adp.insertCoverage(elem);

      adp.showPackageSummary();

      Node node = adp.getSubtree("intellectualRights",0);
      String val = node.getFirstChild().getNodeValue();
      Log.debug(1, "node: "+val);
      node.getFirstChild().setNodeValue("No Rights for YOU!");
      Node delNode = adp.deleteSubtree("intellectualRights",0);
      Log.debug(1, "delNode: "+delNode);
      Node insNode = adp.insertSubtree("intellectualRights", node, 1);
      Log.debug(1,"insNode: "+insNode);
/*
      // now let us test the add attribute
      om.put("/attribute/"+"attributeName","TestAttributeName");
      om.put("/attribute/"+"attributeLabel","TestAttibuteLabel");
      om.put("/attribute/"+"attributeDefinition","Test Attribute Definition");
      // set measurementScale
      om.put("/attribute/"+"measurementScale/interval/"
              +"unit/standardUnit","meters");
      om.put("/attribute/"+"measurementScale/interval/numericDomain/"
              +"numberType","floating point");
      om.put("/attribute/"+"measurementScale/interval/numericDomain/"
              +"bounds/minimum","0.0");
      om.put("/attribute/"+"measurementScale/interval/numericDomain/"
              +"bounds/maximum","1.0");
			om.put("/attribute/references","123456");
      //  create ordermap elements for a new entity
      om1.put("/dataTable/entityName", "TestEntityName");
      om1.put("/dataTable/attributeList/attribute/attributeName", "attributeOne");
*/
    } catch (Exception w) {Log.debug(5, "problem creating ordered map!");}

/*    try{

      entityObject = new Entity("dataTable", om1);
      attributeObject = new Attribute(om);

      }
      catch (Exception e) {Log.debug(5, "problem creating DOM tree!"+ e);}

      adp.insertEntity(entityObject, 3);
      adp.insertAttribute(0, attributeObject,1);

			Node[] ndarry = adp.getAttributeArray(0);
			Node nd1 = ndarry[1];
      Node attrnode = adp.getReferencedNode(ndarry[1]);

Log.debug(1, "referenced node: "+attrnode);
    Node attribute = attrnode;
    String temp = "";
    try {
      NodeList aNodes = XPathAPI.selectNodeList(attribute, "./attributeName");
      if (aNodes == null) {
        Log.debug(5, "aNodes is null !");
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting entity description" + w.toString());
    }
Log.debug(1, "AttrName: "+temp);

*/
      Log.debug(1,"AbstractDataPackage complete - Will now show in an XML Editor..");
      Node domnode = adp.getMetadataNode();
      DocFrame df = new DocFrame();
      df.setVisible(true);
      df.initDoc(null, domnode, null, null, null);
  }

}
