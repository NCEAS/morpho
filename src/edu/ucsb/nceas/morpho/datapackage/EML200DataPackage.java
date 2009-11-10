/**
 *  '$RCSfile: EML200DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-06 01:08:57 $'
 * '$Revision: 1.71 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.DataStoreInterface;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.LocalFileExistingException;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.framework.DocidIncreaseDialog;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;
import edu.ucsb.nceas.morpho.util.IncompleteDocSettings;
import edu.ucsb.nceas.morpho.util.WizardPageInfo;
import edu.ucsb.nceas.morpho.util.XMLUtil;
import edu.ucsb.nceas.morpho.util.XMLErrorHandler;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.apache.xpath.XPathAPI;
import org.apache.xalan.templates.OutputProperties;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * class that represents a data package. This class is abstract. Specific datapackages
 * e.g. eml2, beta6., etc extend this abstact class
 */
public  class EML200DataPackage extends AbstractDataPackage
{

  public static final String LATEST_EML_VER = "eml-2.1.0";
  public static final String EML200NAMESPACE = "eml://ecoinformatics.org/eml-2.0.0";
  public static final String EML201NAMESPACE =  "eml://ecoinformatics.org/eml-2.0.1";
 
  /**
   * Serialize the DOM tree to file system. This method would not overwrite
   * document if docids confict.
   */
   public void serialize(String location)
   {
	   boolean overwrite = false;
	   serialize(location, overwrite);
   }
   
   /**
    * Serialize to local withoverwrite
    *
    */
   public void serializeToLocalWithOverwrite()
   {
	   boolean overwrite = true;
	   serialize(AbstractDataPackage.LOCAL, overwrite);
   }
   
  // serialize to the indicated location
  private void serialize(String location, boolean overWrite)
  {
	 this.setSerializeLocalSuccess(false);
	 this.setSerializeMetacatSuccess(false);
	 //this.setIdentifierChangedInLocalSerialization(false);
	 //this.setIdentifierChangedInMetacatSerialization(false);
	 this.setPackageIDChanged(false);
	 //System.out.println("serialize metadata ===============");
    Morpho morpho = Morpho.thisStaticInstance;
    MetacatDataStore mds  = new MetacatDataStore(morpho);
    FileSystemDataStore fsds = new FileSystemDataStore(morpho);;
    String statusInMetacat = null;
    String  statusInLocal  = null;
    //boolean existFlag = true;
    String conflictLocation = null;
    //String temp = XMLUtilities.getDOMTreeAsString(getMetadataNode(), false);
    String temp = XMLUtil.getDOMTreeAsString(getMetadataNode().getOwnerDocument());
    // To check if this update or insert action
    String identifier = getAccessionNumber();
    String temp2 = identifier;
    String version = null;
    int lastperiod = identifier.lastIndexOf(".");
    if (lastperiod>-1) {
      version = identifier.substring(lastperiod+1, identifier.length());
      temp2 = temp2.substring(0, lastperiod);
      //Log.debug(1, "temp1: "+temp1+"---temp2: "+temp2);
    }
    //boolean existsFlag = mds.exists(temp2+".1");
    
    boolean isRevisionOne = false;
    if (version != null)
    {
    	isRevisionOne = version.equals("1");
    }
    else
    {
    	Log.debug(5,"No revision number assigned to docid. Couldn't save it");
    	return;
    }
    //Check to see if id confilct or not
    if((location.equals(AbstractDataPackage.METACAT))) 
    {
	    statusInMetacat = mds.status(getAccessionNumber());
	    if (statusInMetacat != null && statusInMetacat.equals(DataStoreInterface.CONFLICT))
	    {
	    	conflictLocation = DocidIncreaseDialog.METACAT;
	    	//this.setIdentifierChangedInMetacatSerialization(true);
	    }
    }
    else if((location.equals(AbstractDataPackage.LOCAL))) 
    {
    	statusInLocal = fsds.status(getAccessionNumber());
    	//existFlag = existInLocal;
    	if (statusInLocal != null && statusInLocal.equals(DataStoreInterface.CONFLICT))
	    {
	    	conflictLocation = DocidIncreaseDialog.LOCAL;
	    	//this.setIdentifierChangedInLocalSerialization(true);
	    }
    }
    else if (location.equals(AbstractDataPackage.BOTH))
    {
	    statusInMetacat = mds.status(getAccessionNumber());
    	statusInLocal = fsds.status(getAccessionNumber());
    	//if (existFlag)
    	//{
    		if (statusInMetacat != null && statusInLocal != null && 
    				statusInLocal.equals(DataStoreInterface.CONFLICT) && statusInMetacat.equals(DataStoreInterface.CONFLICT) )
    		{
    			conflictLocation =  DocidIncreaseDialog.LOCAL + " and "+ DocidIncreaseDialog.METACAT;
    		    //this.setIdentifierChangedInLocalSerialization(true);
    		    //this.setIdentifierChangedInMetacatSerialization(true);
    		}
    		else if (statusInMetacat != null && statusInMetacat.equals(DataStoreInterface.CONFLICT))
    		{
    			conflictLocation =  DocidIncreaseDialog.METACAT;
    			//this.setIdentifierChangedInMetacatSerialization(true);
    		}
    		else if (statusInLocal != null && statusInLocal.equals(DataStoreInterface.CONFLICT))
    		{
    			conflictLocation =  DocidIncreaseDialog.LOCAL;
    			//this.setIdentifierChangedInLocalSerialization(true);
    		}
    	//}
    }
    // if we allow local overwrite, we reset confilcLocation. It will skip 
    // the code to handle conflict
    if (overWrite)
    {
    	conflictLocation = null;

    }
    
    //We need to change id to resolve id confilcition
    if (conflictLocation != null && !isRevisionOne)
    {
    	Log.debug(30, "=============In existFlag and update branch");
    	// ToDo - add a frame to give user option to increase docid or revision
    	 DocidIncreaseDialog docidIncreaseDialog = new DocidIncreaseDialog(identifier, conflictLocation);
    	 String choice = docidIncreaseDialog.getUserChoice();
    	 if (choice != null && choice.equals(DocidIncreaseDialog.INCEASEID))
    	 {
    		 // increase to a new id
    	    AccessionNumber an = new AccessionNumber(morpho);
            identifier = an.getNextId();
            setAccessionNumber(identifier);
            setPackageIDChanged(true);
            temp = XMLUtil.getDOMTreeAsString(getMetadataNode().getOwnerDocument());
            //since we changed the revision number, the status of docid will be changed
            statusInMetacat = DataStoreInterface.NONEXIST;
            statusInLocal = DataStoreInterface.NONEXIST;
    	 }
    	 else
    	 {
    		 // increase revision number
    		 int newRevision = this.getNextRevisionNumber(getAccessionNumber());
    		 identifier = temp2+"."+newRevision;
    		 setAccessionNumber(identifier);
    		 setPackageIDChanged(true);
             temp = XMLUtil.getDOMTreeAsString(getMetadataNode().getOwnerDocument());
    		 Log.debug(30, "The new id (after increase revision number) is "+identifier);
    		 statusInMetacat = DataStoreInterface.UPDATE;
             statusInLocal = DataStoreInterface.UPDATE;
    	 }
    }
    else if (conflictLocation != null)
    {
    	Log.debug(30, "==============In existFlag and insert revision 1 branch");
    	//since it is saving a new package, increase docid silently
    	 AccessionNumber an = new AccessionNumber(morpho);
    	 identifier = an.getNextId();
    	 setAccessionNumber(identifier);
    	 setPackageIDChanged(true);
    	 temp = XMLUtil.getDOMTreeAsString(getMetadataNode().getOwnerDocument());
    	 statusInMetacat = DataStoreInterface.NONEXIST;
         statusInLocal = DataStoreInterface.NONEXIST;
    }
     // Log.debug(30, temp);

      // save doc to metacat
      StringReader sr1 = new StringReader(temp);
      if((location.equals(AbstractDataPackage.METACAT))|| (location.equals(AbstractDataPackage.BOTH))) 
      {
        if (statusInMetacat != null && statusInMetacat.equals(DataStoreInterface.UPDATE))
        {
        	try
        	{
        	    mds.saveFile(getAccessionNumber(),sr1);
        	    this.setSerializeMetacatSuccess(true);
        	}
            catch(Exception e) 
            {
            	this.setSerializeMetacatSuccess(false);
            	//this.setIdentifierChangedInMetacatSerialization(false);
            	//System.out.println(" in other exception Exception==========  "+e.getMessage());
                Log.debug(5,"Problem with saving to metacat in EML200DataPackage!\n"+e.getMessage());              
            }
        }
        else if (statusInMetacat != null && statusInMetacat.equals(DataStoreInterface.NONEXIST))
        {
        	try
        	{
	            mds.newFile(getAccessionNumber(),sr1);
	            this.setSerializeMetacatSuccess(true);
	            //setAccessionNumber(temp_an);
        	}
             catch(Exception e) 
             {
            	 this.setSerializeMetacatSuccess(false);
            	 //this.setIdentifierChangedInMetacatSerialization(false);
                 Log.debug(5,"Problem with saving to metacat in EML200DataPackage!\n"+e.getMessage());
             }
        }
        else
        {
        	this.setSerializeMetacatSuccess(false);
       	    //this.setIdentifierChangedInMetacatSerialization(false);
            Log.debug(5,"Problem with saving to metacat in EML200DataPackage since couldn't get the docid status in Metacat");
        }
      }
      
      // save doc to local file system
      StringReader sr = new StringReader(temp);
      if((location.equals(AbstractDataPackage.LOCAL)) || (location.equals(AbstractDataPackage.BOTH))) 
      {
         //Log.debug(10, "XXXXXXXXX: serializing to hardcoded /tmp/emldoc.xml");
         //fsds.saveFile("100.0",sr);
          File newFile = fsds.saveFile(getAccessionNumber(),sr);
          if (newFile != null)
          {
             this.setSerializeLocalSuccess(true);
          }
          else
          {
        	  this.setSerializeLocalSuccess(false);
        	  //this.setIdentifierChangedInLocalSerialization(false);
          }
       }
  }
  
 

  /*
   *  This method loops over the entities associated witht the package and
   *  finds associated datafile id referenced in distribution url
   */
  private Vector getAssociatedDataFiles() {
    String urlinfo = null;
    Vector res = new Vector();
    Entity[] ents = getEntityArray();
    // if package has no data
    if(ents == null)
      return res;
    for (int i=0;i<ents.length;i++) {
      if (getDistributionUrl(i, 0,0).length()>0) {  // there is a url link
        urlinfo = getDistributionUrl(i, 0,0);
        if (urlinfo.startsWith("ecogrid://")) {
          // assumed that urlinfo is of the form 'ecogrid://systemname/localid/other'
          // system name is 'knb'
          // we just want the local id here
          int indx2 = urlinfo.indexOf("//");
          if (indx2>-1) urlinfo = urlinfo.substring(indx2+2);
          // now start should be just past the '//'
          indx2 = urlinfo.indexOf("/");
          if (indx2>-1) urlinfo = urlinfo.substring(indx2+1);
          //now should be past the system name
          indx2 = urlinfo.indexOf("/");
          if (indx2>-1) urlinfo = urlinfo.substring(0,indx2);
          // should have trimmed 'other'
          if (urlinfo.length()>0) {
            // if we reach here, urlinfo should be the locvalid in a string
            res.addElement(urlinfo);
          }
        }
      }
    }
    return res;
  }

  public void load(String location, String identifier, Morpho morpho) {
    this.location   = location;
    this.id         = identifier;
    this.config     = Morpho.getConfiguration();

    Log.debug(20, "Creating new DataPackage Object");
    Log.debug(20, "id: " + this.id);
    Log.debug(20, "location: " + location);
    morpho = Morpho.thisStaticInstance;

    fileSysDataStore  = new FileSystemDataStore(morpho);
    metacatDataStore  = new MetacatDataStore(morpho);
    File packagefile;
    try {
      packagefile = getFileWithID(this.id, morpho);
     } catch (Throwable t) {
      //already handled in getFileWithID() method,
      //so just abandon this instance:
      return;
    }

    DocumentBuilder parser = Morpho.createDomParser();
    InputSource in;
    FileInputStream fs;
    Document doc = null;

    if (packagefile==null){ Log.debug(1, "packagefile is NULL!"); }
    try {
        fs = new FileInputStream(packagefile);
        load(new InputSource(fs));
        fs.close();
    } catch (java.io.IOException ioe) {
        Log.debug(15, "IOException: " + ioe.getMessage());
    }
  }

  public void load(InputSource in) {
      DocumentBuilder parser = Morpho.createDomParser();
      Document doc = null;
      /*
      Log.debug(15, "Testing input source");
      try {
          java.io.BufferedReader r = new java.io.BufferedReader(in.getCharacterStream());
          String line;
          while ((line=r.readLine()) != null) {
              Log.debug(15, line);
          }
      } catch (Exception ex) {
          Log.debug(1, "Problem with XML input source: " + ex.toString());
      }
*/

      try
      {
        parser.setErrorHandler(new XMLErrorHandler());
        doc = parser.parse(in);
      } catch(Exception e1) {
        Log.debug(4, "Parsing threw: " + e1.toString());
        e1.printStackTrace();
      }
      if (doc==null) { Log.debug(1, "doc is NULL!");}
      setDocument(doc);
      try{
    	String emlVersion = getEMLVersion();
    	Log.debug(30, "eml version===== is "+emlVersion);
    	 if (emlVersion.indexOf("eml-2.0")>-1)
   	  	{
   		  	//System.out.println("the eml 200 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
   		  	metadataPathNode = XMLUtilities.getXMLAsDOMTreeRootNode("/eml200KeymapConfig.xml");
   	  	}
   	  	else
   	  	{
   	  		//System.out.println("the eml 210 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
   	  		metadataPathNode = XMLUtilities.getXMLAsDOMTreeRootNode("/eml210KeymapConfig.xml");
   	  	}
         //metadataPathNode = XMLUtilities.getXMLAsDOMTreeRootNode("/eml200KeymapConfig.xml");
      }
      catch (Exception e2) {
        Log.debug(4, "getting DOM for Paths threw error: " + e2.toString());
        e2.printStackTrace();
      }
  }

  private void setDocument(Document doc) {
      setMetadataNode(doc.getDocumentElement());
      this.doc = doc;  // set the MetadataObject doc
  }

  /**
   * override method in AbstractDataPackage to get all authors and combine name
   * fields
   *
   * @return String
   */
  public String getAuthor() {
    String temp = "";
    String surNameXpath = "/eml:eml/dataset/creator/individualName/surName";
    String givenNameXpath = "/eml:eml/dataset/creator/individualName/givenName";
    String salutationXpath = "/eml:eml/dataset/creator/individualName/salutation";

    NodeList authorNodes = null;
    try{
      authorNodes = XMLUtilities.getNodeListWithXPath(metadataNode, surNameXpath);
    }
    catch (Exception w) {
      Log.debug(4, "Problem with getting Nodelist");
    }
    if (authorNodes==null) return "";  // no authors
    int numAuthors = authorNodes.getLength();

    String surName = "";
    String givenName = "";
    String salutation = "";
    for (int i=1;i<numAuthors+1;i++) {
      surName = getXPathValue("("+surNameXpath +")["+i+"]");
      givenName = getXPathValue("("+givenNameXpath +")["+i+"]");
      salutation = getXPathValue("("+salutationXpath+")["+i+"]");
      if (temp.length()>0) temp = temp + ", ";
      temp = temp + salutation + " " + givenName + " " + surName;
    }
    return temp;
  }

   /**
    * Gets the /eml:eml node.
    * @return Node
    */
   private Node getRootNode() {
       String emlXpath = "/eml:eml";
       NodeList nodes = null;
       try {
           nodes = XMLUtilities.getNodeListWithXPath(metadataNode, emlXpath);
       } catch (Exception w) {
           Log.debug(30, "Problem with getting root node with path " +
                     emlXpath + " -- " + w.toString());
       }

       if (nodes == null) return null;  // no eml:eml
       if (nodes.getLength() > 0) {
           return nodes.item(0);
       }
       return null;
   }


   /**
     * Get the root node's attributes.
     * @return NamedNodeMap
     */
    private NamedNodeMap getRootNodeAttributes() {
        Node n = getRootNode();
        if (n == null)  return null;
        return n.getAttributes();
    }


  /**
   * Get the xmlns:eml attribute of <eml:eml>.
   * @return String
   */
  public String getXMLNamespace() {
      return XMLUtil.getAttributeValue(getRootNodeAttributes(), "xmlns:eml");

//   return getGenericValue("/xpathKeyMap/contextNode[@name='package']/emlVersion");
//   return getXPathValue("/eml:eml/@xmlns:eml");
  }

//method to load custom units that the user had defined and are stored in the
	// 'additionalMetadata' subtree
	public void loadCustomUnits() {

		Document thisDom = getMetadataNode().getOwnerDocument();
		Node rootNode = thisDom.getDocumentElement();
		NodeList nodeList = rootNode.getChildNodes();
		Log.debug(40, "in loadCustom data, initial size = " + this.customUnitDictionaryUnitsCacheMap.keySet().size());
		for(int i = 0; i < nodeList.getLength(); i++) {

			Node child = nodeList.item(i);
			if(child.getNodeName().equals("additionalMetadata")) {
				OrderedMap map = XMLUtilities.getDOMTreeAsXPathMap(child);
				Log.debug(40, "got Map as - " + map.toString());
				if (getXMLNamespace() != null && (getXMLNamespace().equals("EML200NAMESPACE") ||
						getXMLNamespace().equals("EML201NAMESPACE")))
				{
					Log.debug(30,"in eml 201 ..... addtionalMetacat unit");
				    extractUnits(map, "/additionalMetadata");
				}
				else
				{
					Log.debug(30, "in eml 210 and higher version ..... addtionalMetacat unit");
					extractUnits(map, "/additionalMetadata/metadata");
				}
			}
		}
		Log.debug(40, "Extracted units -- \n");
		Iterator it = customUnitDictionaryUnitsCacheMap.keySet().iterator();
		while(it.hasNext()) {
			String key = (String) it.next();
			String[] arr = (String[])customUnitDictionaryUnitsCacheMap.get(key);
			Log.debug(40, key);
			for(int j = 0; j < arr.length; j++)
				Log.debug(40, "\t" + arr[j]);
		}
		Log.debug(40, "End of Extracted units -- \n");
	}


  /**
   * Returns everything after the last / in the xmlns:eml.
   * @return String
   */
  public String getEMLVersion() {
      String xmlns = getXMLNamespace();
      int pos = xmlns.lastIndexOf('/');
      if (pos != -1) {
          return xmlns.substring(pos+1);
      }

      return "";
  }
  
  /**
   * Gets the status of the completion of this package 
   * @return three status - completed, incomplete(new package wizard) or incomplete(text import wizard)
   */
  public String getCompletionStatus()
  {
	  String packageWizardXpath = IncompleteDocSettings.EMLPATH+IncompleteDocSettings.ADDITIONALMETADATA+"/"+IncompleteDocSettings.METADATA+
	                                           "/"+IncompleteDocSettings.PACKAGEWIZARD;
	  String textImportWizardXPath = IncompleteDocSettings.EMLPATH+IncompleteDocSettings.ADDITIONALMETADATA+"/"+IncompleteDocSettings.METADATA+
                                               "/"+IncompleteDocSettings.ENTITYWIZARD;
      NodeList nodes = null;
      try {
          nodes = XMLUtilities.getNodeListWithXPath(metadataNode, packageWizardXpath);
          if (nodes != null && nodes.getLength() > 0) 
          {
        	  // the status is incomplete_new_package_Wizard.
        	  completionStatus = IncompleteDocSettings.INCOMPLETE_PACKAGE_WIZARD;
          }
          else
          {
        	  nodes = XMLUtilities.getNodeListWithXPath(metadataNode, textImportWizardXPath);
        	  if (nodes != null && nodes.getLength() >0)
        	  {
        		  // the status is incomplete_new_package_Wizard.
            	  completionStatus = IncompleteDocSettings.INCOMPLETE_ENTITY_WIZARD;
        	  }
        	  else
        	  {
        		  completionStatus = AbstractDataPackage.COMPLETED;
        	  }
          }
      } catch (Exception w) {
          Log.debug(30, "Problem with getting completion status " + w.toString());
          completionStatus = AbstractDataPackage.COMPLETED;// setting to complete no matter
      }

      
     
	  return completionStatus;
  }
  
  
  /**
   * Gets the UIPage class name list after parsing the incomplete information in additional metacat part.
   * This eml part looks like
   *  <additionalMetadata>
   *      <metadata>
   *         <packagwizard>
   *              <class>
   *                  <name>
   *                   <parameter>
   *              </class>
   *  ............................
   * @return
   */
  public WizardPageInfo [] getIncompletePacakgeWizardPageInfoList()
  {
	  WizardPageInfo[] classInfoList = null;
	  String pageClassNameXpath = IncompleteDocSettings.EMLPATH+IncompleteDocSettings.ADDITIONALMETADATA+"/"+IncompleteDocSettings.METADATA+
      "/"+IncompleteDocSettings.PACKAGEWIZARD+"/"+IncompleteDocSettings.CLASS;
	  classInfoList = getIncompleteWizardPageInfoList(pageClassNameXpath);
	  return classInfoList;
	  
  }
  
  /**
   * Gets the UIPage class name list after parsing the incomplete information in additional metacat part.
   * This eml part looks like
   *  <additionalMetadata>
   *      <metadata>
   *         <entityWizard>
   *              <class>
   *                  <name>
   *                   <parameter>
   *              </class>
   *  ............................
   * @return
   */
  public WizardPageInfo [] getIncompleteEntityWizardPageInfoList()
  {
	  WizardPageInfo[] classInfoList = null;
	  String pageClassNameXpath = IncompleteDocSettings.EMLPATH+IncompleteDocSettings.ADDITIONALMETADATA+"/"+IncompleteDocSettings.METADATA+
      "/"+IncompleteDocSettings.ENTITYWIZARD+"/"+IncompleteDocSettings.CLASS;
	  classInfoList = getIncompleteWizardPageInfoList(pageClassNameXpath);
	  return classInfoList;
	  
  }
  
  /*
   * Gets the page class information in given path
   */
  private  WizardPageInfo [] getIncompleteWizardPageInfoList(String path)
  {
	  WizardPageInfo[] classInfoList = null;
	  NodeList nodeList = null;
      try 
      {
          nodeList = XMLUtilities.getNodeListWithXPath(metadataNode, path);
      } 
      catch (Exception w) 
      {
          Log.debug(30, "Problem with getting page class name list in additional metadata part " + w.toString());
        
      }
      if (nodeList != null && nodeList.getLength() > 0) 
      {
    	    classInfoList = new WizardPageInfo[nodeList.getLength()];
    	  
    	    for(int i=0; i<nodeList.getLength(); i++)
    	    {
    	    	Node targetNode = nodeList.item(i);
    	    	if(targetNode != null)
    	    	{
    	    		NodeList children = targetNode.getChildNodes();
    	    		WizardPageInfo info = null;
    	    		for (int nodeIndex=0; nodeIndex <children.getLength(); nodeIndex++) 
    	    		{
                          Node kidNode = children.item(nodeIndex);
                          if(kidNode.getNodeType() ==Node.ELEMENT_NODE && kidNode.getNodeName().equals(IncompleteDocSettings.NAME))
                          {
                        	  // this handles class name children
                        	  NodeList grandChildren = kidNode.getChildNodes();
                        	  for(int k=0; k<grandChildren.getLength(); k++)
                        	  {
		    	    		      Node textNode = grandChildren.item(k);
		    	    		      if (textNode.getNodeType()==Node.TEXT_NODE
		    	    		                          || textNode.getNodeType()==Node.CDATA_SECTION_NODE) 
		    	    		      {
		    	    		    	  String className = textNode.getNodeValue();
		    	    		    	  Log.debug(25, "The read class name from additional metacat is "+className);
		    	                      info = new WizardPageInfo(className);
		    	    		      }
                        	  }
                          }
                          else if(kidNode.getNodeType() ==Node.ELEMENT_NODE && kidNode.getNodeName().equals(IncompleteDocSettings.PARAMETER))
                          {
                        	 // this handles class parameter children
                        	  NodeList grandChildren = kidNode.getChildNodes();
                        	  for(int k=0; k<grandChildren.getLength(); k++)
                        	  {
		    	    		      Node textNode = grandChildren.item(k);
		    	    		      if (textNode.getNodeType()==Node.TEXT_NODE
		    	    		                          || textNode.getNodeType()==Node.CDATA_SECTION_NODE) 
		    	    		      {
		    	    		    	  String parameter = textNode.getNodeValue();
		    	    		    	  Log.debug(25, "The paramter is "+parameter);
		    	                     if(info != null)
		    	                     {
		    	                    	 info.addParameter(parameter);
		    	                     }
		    	    		      }
                        	  }
                          } 
                          else if(kidNode.getNodeType() ==Node.ELEMENT_NODE && kidNode.getNodeName().equals(IncompleteDocSettings.VARIABLE))
                          {
                        	  //this handle variables with the class. The variable is a pair -key and value.
                        	  NodeList grandChildren = kidNode.getChildNodes();
                        	  String key = null;
                        	  String value = null;
                        	  for(int k=0; k<grandChildren.getLength(); k++)
                        	  {
		    	    		      Node node = grandChildren.item(k);
		    	    		      
		    	    		      if (node.getNodeType()==Node.ELEMENT_NODE && node.getNodeName().equals(IncompleteDocSettings.KEY)) 
		    	    		      {
		    	    		    	  key = node.getFirstChild().getNodeValue();	    		    	  
		    	    		    	  Log.debug(25, "In EML200DataPackage.getIncompleteWizardPageInfoList - the key is "+key);		    	                    
		    	    		      }
		    	    		      else  if (node.getNodeType()==Node.ELEMENT_NODE && node.getNodeName().equals(IncompleteDocSettings.VALUE)) 
		    	    		      {
		    	    		    	  key = node.getFirstChild().getNodeValue();	    		    	  
		    	    		    	  Log.debug(25, "In EML200DataPackage.getIncompleteWizardPageInfoList - the value is "+value);		    	                    
		    	    		      }
                        	  }
                        	  if(info != null)
                        	  {
	    	                    	 info.putVariableValue(key, value);
	    	                  }
                          }
	    		    
    	    		}   	    		
    	    		if (info != null)
    	    		{
    	    			classInfoList[i] = info;
    	    		}
    	    	}
    	    }
      }
      Log.debug(35, "The class info list is "+classInfoList);
	  return classInfoList;
  }
  
  /**
   * Gets the entity index stores in incomplete doc info part
   * @return
   */
  public int getEntityIndexInIncompleteDocInfo()
  {
	  int index = -1;
	  String path = IncompleteDocSettings.EMLPATH+IncompleteDocSettings.ADDITIONALMETADATA+"/"+IncompleteDocSettings.METADATA+
      "/"+IncompleteDocSettings.ENTITYWIZARD+"/"+IncompleteDocSettings.INDEX;
	  NodeList nodeList = null;
      try 
      {
          nodeList = XMLUtilities.getNodeListWithXPath(metadataNode, path);
          Node node = nodeList.item(0);
          if(node != null)
          {
        	  Node textNode = node.getFirstChild();
        	  if(textNode != null && (textNode.getNodeType()==Node.TEXT_NODE
                      || textNode.getNodeType()==Node.CDATA_SECTION_NODE))
        	  {
        		  String indexStr = textNode.getNodeValue();
        		  index = (new Integer(indexStr)).intValue();
        		  Log.debug(30, "The index of entity in incomplete doc inforamtion is "+index);
        	  }
          }
      } 
      catch (Exception w) 
      {
          Log.debug(30, "Problem with getEntityIndexIncompleteDocInfo in additional metadata part " + w.getMessage());
        
      }
	  return index;
  }
  
  /**
   * Read the import attribute information from incomplete additionMetadata part.
   */
  public void readImportAttributeInfoFromIncompleteDoc()
  {
	  String path = IncompleteDocSettings.EMLPATH+IncompleteDocSettings.ADDITIONALMETADATA+"/"+IncompleteDocSettings.METADATA+
      "/"+IncompleteDocSettings.ENTITYWIZARD+"/"+this.IMPORTATTRIBUTES+"/"+this.ATTRIBUTE;
	  NodeList nodeList = null;
      try 
      {
          nodeList = XMLUtilities.getNodeListWithXPath(metadataNode, path);
      } 
      catch (Exception w) 
      {
          Log.debug(30, "Problem with getting imported attribute list in additional metadata part " + w.getMessage());
        
      }
      if (nodeList != null && nodeList.getLength() > 0) 
      {
    	for(int i=0; i<nodeList.getLength(); i++)
  	    {
    		List t = new ArrayList();
			String entityName = null;
			String attributeName = null;
			String scale = null;
			OrderedMap omap = new OrderedMap();
			String xPath = null;
			String newTable = null;
  	    	Node targetNode = nodeList.item(i);
  	    	NodeList children = targetNode.getChildNodes();
  	    	for (int nodeIndex=0; nodeIndex <children.getLength(); nodeIndex++) 
    		{
                  Node kidNode = children.item(nodeIndex);
                  if(kidNode.getNodeType() ==Node.ELEMENT_NODE && kidNode.getNodeName().equals(this.ENTITYNAME))
                  {
                	  // this handles class name children
                	  NodeList grandChildren = kidNode.getChildNodes();
                	  for(int k=0; k<grandChildren.getLength(); k++)
                	  {
    	    		      Node textNode = grandChildren.item(k);
    	    		      if (textNode.getNodeType()==Node.TEXT_NODE
    	    		                          || textNode.getNodeType()==Node.CDATA_SECTION_NODE) 
    	    		      {
    	    		    	  entityName = textNode.getNodeValue();
    	    		    	  Log.debug(30, "The read entity name from additional in EML200Package. readImportAttributeInfoFromIncompleteDoc is "+entityName);
    	                      
    	    		      }
                	  }
                  }
                  else if(kidNode.getNodeType() ==Node.ELEMENT_NODE && kidNode.getNodeName().equals(this.ATTRIBUTENAME))
                  {
                	 // this handles class parameter children
                	  NodeList grandChildren = kidNode.getChildNodes();
                	  for(int k=0; k<grandChildren.getLength(); k++)
                	  {
    	    		      Node textNode = grandChildren.item(k);
    	    		      if (textNode.getNodeType()==Node.TEXT_NODE
    	    		                          || textNode.getNodeType()==Node.CDATA_SECTION_NODE) 
    	    		      {
    	    		    	  attributeName = textNode.getNodeValue();
    	    		    	  Log.debug(30, "The read attributeName from additional in EML200Package. readImportAttributeInfoFromIncompleteDoc is "+attributeName);    	                     
    	    		      }
                	  }
                  } 
                  else if(kidNode.getNodeType() ==Node.ELEMENT_NODE && kidNode.getNodeName().equals(this.SCALE))
                  {
                	 // this handles class parameter children
                	  NodeList grandChildren = kidNode.getChildNodes();
                	  for(int k=0; k<grandChildren.getLength(); k++)
                	  {
    	    		      Node textNode = grandChildren.item(k);
    	    		      if (textNode.getNodeType()==Node.TEXT_NODE
    	    		                          || textNode.getNodeType()==Node.CDATA_SECTION_NODE) 
    	    		      {
    	    		    	  scale = textNode.getNodeValue();
    	    		    	  Log.debug(30, "The read scale from additional in EML200Package. readImportAttributeInfoFromIncompleteDoc is "+scale);    	                     
    	    		      }
                	  }
                  }
                  else if(kidNode.getNodeType() ==Node.ELEMENT_NODE && kidNode.getNodeName().equals(this.ORDEREDMAP))
                  {
                	 // this handles class parameter children
                	  NodeList list = null;
                	  try 
                      {
                          list = XMLUtilities.getNodeListWithXPath(kidNode, OrderedMap.PAIR);
                      } 
                      catch (Exception w) 
                      {
                          Log.debug(30, "Problem with getting orderedMp list in additional metadata part " + w.getMessage());
                        
                      }
                      if(list != null)
                      {
                	  
	                	  for(int m=0; m<list.getLength(); m++)
	                	  {
	                		  String key = null;
	    	    		      String value = null;
	    	    		      Node node = list.item(m);
	    	    		      NodeList anotherChildren = node.getChildNodes();
	    	    		      for(int n=0; i<anotherChildren.getLength(); n++)
	    	    		      {
	    	    		    	  Node anotherGrandChild = anotherChildren.item(n);
		    	    		      if (anotherGrandChild.getNodeType()==Node.ELEMENT_NODE && anotherGrandChild.getNodeName().equals(OrderedMap.KEY)) 
		    	    		      {
		    	    		    	  Node firstChild = node.getFirstChild();
		    	    		    	  if(firstChild != null && (firstChild.getNodeType()==Node.TEXT_NODE
		    	    		                          || firstChild.getNodeType()==Node.CDATA_SECTION_NODE))
		    	    		    	  {
		    	    		    	      key = firstChild.getNodeValue();
		    	    		    		  Log.debug(30, "The key of order map from additional in EML200Package. readImportAttributeInfoFromIncompleteDoc is "+key);
		    	    		    	    
		    	    		    	  }
		    	    		      }
		    	    		      else if (anotherGrandChild.getNodeType()==Node.ELEMENT_NODE && anotherGrandChild.getNodeName().equals(OrderedMap.VALUE)) 
		    	    		      {
		    	    		    	  Node firstChild = node.getFirstChild();
		    	    		    	  if(firstChild != null && (firstChild.getNodeType()==Node.TEXT_NODE
		    	    		                          || firstChild.getNodeType()==Node.CDATA_SECTION_NODE))
		    	    		    	  {
		    	    		    	      value = firstChild.getNodeValue();
		    	    		    	     
		    	    		    		  Log.debug(30, "The value of order map from additional in EML200Package. readImportAttributeInfoFromIncompleteDoc is "+value);
		    	    		    	    
		    	    		    	  }    	                     
		    	    		      }
		    	    		      if(key != null )
		    	    		      {
		    	    		    	  omap.put(key, value);
		    	    		    	  Log.debug(30, "Put key "+key+ " and value "+value+"  in ordered map in EML200Package. readImportAttributeInfoFromIncompleteDoc is "+value);
		    	    		    	 	    	    		    	 
		    	    		      }
	                	    }
	                	  }
                     }
                  }                 
                  else if(kidNode.getNodeType() ==Node.ELEMENT_NODE && kidNode.getNodeName().equals(this.XPATH))
                  {
                	 // this handles class parameter children
                	  NodeList grandChildren = kidNode.getChildNodes();
                	  for(int k=0; k<grandChildren.getLength(); k++)
                	  {
    	    		      Node textNode = grandChildren.item(k);
    	    		      if (textNode.getNodeType()==Node.TEXT_NODE
    	    		                          || textNode.getNodeType()==Node.CDATA_SECTION_NODE) 
    	    		      {
    	    		    	  xPath = textNode.getNodeValue();
    	    		    	  Log.debug(30, "The read xPath from additional in EML200Package. readImportAttributeInfoFromIncompleteDoc is "+xPath);    	                     
    	    		      }
                	  }
                  }
                  else if(kidNode.getNodeType() ==Node.ELEMENT_NODE && kidNode.getNodeName().equals(this.NEWTABLE))
                  {
                	 // this handles class parameter children
                	  NodeList grandChildren = kidNode.getChildNodes();
                	  for(int k=0; k<grandChildren.getLength(); k++)
                	  {
    	    		      Node textNode = grandChildren.item(k);
    	    		      if (textNode.getNodeType()==Node.TEXT_NODE
    	    		                          || textNode.getNodeType()==Node.CDATA_SECTION_NODE) 
    	    		      {
    	    		    	  newTable = textNode.getNodeValue();
    	    		    	  Log.debug(30, "The read newTable from additional in EML200Package. readImportAttributeInfoFromIncompleteDoc is "+newTable);    	                     
    	    		      }
                	  }
                  }           
		    
    		}   	    
  	    	t.add(entityName);
			t.add(attributeName);
			t.add(scale);
			t.add(omap);
			t.add(xPath);
			t.add(new Boolean(newTable));
			if (toBeImported == null) 
			{
				toBeImported = new ArrayList();
				toBeImportedCount = 0;
			}
			toBeImported.add(t);
			toBeImportedCount++;
			Log.debug(10,
			"==========Adding Attr to Import - (" + entityName + ", " + attributeName +
			") ; count = " + toBeImportedCount);
			
  	    }
      }
		
  }

  


  /**
   * Sets the EML version xmlns:eml and xsi:schemaLocation.
   *
   * xmlns:eml="eml://ecoinformatics.org/eml-2.0.1"
   * xsi:schemaLocation="eml://ecoinformatics.org/eml-2.0.1 eml.xsd"
   */
  public void setEMLVersion(String newVersion) {

      NamedNodeMap rootNodeAtts = getRootNodeAttributes();
      String oldXmlns = XMLUtil.getAttributeValue(rootNodeAtts, "xmlns:eml");

      // check if current version differs from given version
      int pos = oldXmlns.lastIndexOf('/');
      if (pos != -1) {
          String oldVersion = oldXmlns.substring(pos+1);
          if (!oldVersion.equals(newVersion)) {
              // different versions
              String newXmlns = oldXmlns.substring(0, pos+1) + newVersion;
              Log.debug(30, "XXXXX setting new xmlns: " + newXmlns);

              Node xmlnsNode = rootNodeAtts.getNamedItem("xmlns:eml");
              xmlnsNode.setNodeValue(newXmlns);

              // todo: str_replace old version with eml-2.0.1
              // todo: go back to SaveDialog line 315 and make a new DP
              String oldSchemaLocation = XMLUtil.getAttributeValue(rootNodeAtts,
                      "xsi:schemaLocation");
              int spacePos = oldSchemaLocation.indexOf(' ');
              String oldSchemaHome = oldSchemaLocation.substring(
                      oldSchemaLocation.lastIndexOf('/', spacePos)+1, spacePos);

              String newSchemaLocation = oldSchemaLocation.replaceAll(oldSchemaHome, newVersion);

              //Log.debug(10, "newSchemaLocation = " + newSchemaLocation);
              Node schemaLocationNode = rootNodeAtts.getNamedItem("xsi:schemaLocation");
              schemaLocationNode.setNodeValue(newSchemaLocation);

              /*
              // reload the DOM with the new namespace
              Node origMetadataNode = getMetadataNode();
              org.w3c.dom.Document oldDoc = origMetadataNode.getOwnerDocument();
              DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
              org.w3c.dom.Document newDoc = impl.createDocument(
                      origMetadataNode.getNamespaceURI(),
                      origMetadataNode.getNodeName(),
                      oldDoc.getDoctype());

              // swap out the old doc's root node
              Node importedNode = newDoc.importNode(origMetadataNode, true);
              Node tempRoot = newDoc.getDocumentElement();
              newDoc.replaceChild(importedNode, tempRoot);

              // now reset the metadataNode
              setDocument(newDoc);
              */

              // TODO: somehow confirm that the new doc is set right
              //Log.debug(30, XMLUtil.getDOMTreeAsString(metadataNode));
              //Log.debug(30, "new namespace: " + getEMLVersion());

              Log.debug(30, "----DONE setting new EML version");
          }
      } else {
          Log.debug(15, "invalid xmlns while setting EML version: " + oldXmlns);
      }
  }

  public AbstractDataPackage upload(String id, boolean updatePackageId)
                                                throws MetacatUploadException {
    Morpho morpho = Morpho.thisStaticInstance;
    load(AbstractDataPackage.LOCAL, id, morpho);
    String nextid = id;
    if (updatePackageId) {
      AccessionNumber an = new AccessionNumber(morpho);
      nextid = an.getNextId();
      this.setAccessionNumber(nextid);
          // serialize locally with the new id
      serialize(AbstractDataPackage.LOCAL);
    }

    try {
    	serializeData(AbstractDataPackage.METACAT);
        serialize(AbstractDataPackage.METACAT);
      // if serialize data successfully, we will serialize data. 
      if(getSerializeMetacatSuccess() == true)
      {
    	  //System.out.println("=================== serialzie metacat is true");
    	  this.setLocation(AbstractDataPackage.METACAT);
    	  
      }
      else
      {
    	  //System.out.println("=================== serialzie metacat is false");
    	  this.setLocation(AbstractDataPackage.LOCAL);
    	  throw new MetacatUploadException("Couldn't upload the package to metacat");
      }
    }
    catch (Exception w) {
      Log.debug(5, "error in uploading! "+w.getMessage());
      throw new MetacatUploadException(w.getMessage());
    }
    return this;
  }

  public AbstractDataPackage download(String id) {
    Morpho morpho = Morpho.thisStaticInstance;
    //load(AbstractDataPackage.METACAT, id, Morpho.thisStaticInstance);
    try {
      serialize(AbstractDataPackage.LOCAL);
    } catch (Exception w) {
        Log.debug(5,"Exception serializing local package in 'download'");
    }
    // now download the associated data files
    Vector idlist = getAssociatedDataFiles();
    Enumeration e = idlist.elements();
    MetacatDataStore mds = new MetacatDataStore(morpho);
    FileSystemDataStore fds = new FileSystemDataStore(morpho);
    while (e.hasMoreElements()) {
      String curid = (String)e.nextElement();
      try{
        File datafile = mds.openDataFile(curid);
        FileReader fr = new FileReader(datafile);
        fds.saveFile(curid, fr);
      }
      catch (Exception q3) {
        Log.debug(5,"Exception opening datafile from metacat and saving locally");
      }
    }
    return this;
  }


  /**
   * This method follows the pointer stored in 'references' node to return the
   * DOM node referred to by 'references'
   *
   * @param node Node
   * @return Node
   */
  public Node getReferencedNode(Node node) {
    Node referencedNode = node;
    // does the node have a child named 'references'?
    // if so, resolve
    try {
      String refpath = "references";
      NodeList refs = XMLUtilities.getNodeListWithXPath(node, refpath);
      while ((refs!=null)&&(refs.getLength()>0)) {
        // get id
        String id = (XMLUtilities.getTextNodeWithXPath(node, "references")).getNodeValue();
        // get node under rootNode with the id
        Node rootNode = node.getOwnerDocument().getDocumentElement();
        NodeList refs2 = XMLUtilities.getNodeListWithXPath(rootNode, "//*[@id='"+id+"']");
        // there should be a single node with the id
        referencedNode = refs2.item(0);
        // check for another reference!
        refs = XMLUtilities.getNodeListWithXPath(referencedNode, refpath);
             // if refs is non-zero in length, we repeat
      } // end while
    } catch (Exception w) {
      Log.debug(25, "Exception trying to follow references!");
    }
    return referencedNode;
  }



  /**
   * returns cloned root Node of subtree identified by the passed unique String
   * refID; returns null if not found
   *
   * @param refID unique String refID
   * @return  cloned root Node of subtree, or null if refID not found
   */
  public Node getSubtreeAtReference(String refID) {
    Node refdNode = null;
    try{
      refdNode = getSubtreeAtReferenceNoClone(refID);
      if (refdNode==null) return null;
      Node deepClone = refdNode.cloneNode(true);
      DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
      Document doc = impl.createDocument("", "tempRoot", null);
      Node importedClone = doc.importNode(deepClone, true);
      Node tempRoot = doc.getDocumentElement();
      doc.replaceChild(importedClone, tempRoot);
      return importedClone;
    } catch (Throwable w) {
      Log.debug(25,
        "Exception trying to follow references (in getSubtreeAtReference)! "+w);
      w.printStackTrace();
    }
    return null;
  }


  /**
   * returns a List of pointers to subtrees that reference (the subtree
   * identified by) the passed refID.
   * More formally, returns a List of pointers to subtree root Nodes, where each
   * subtree root Node contains a "references" child-node, and the content
   * String of the references child-node matches the unique String refID passed
   * to this method; returns an empty List if none found. Should never return
   * null;
   *
   * @param refID unique String refID
   * @return List of pointers to subtrees that reference the subtree identified
   * by the passed refID. Returns an empty List if none found. Should never
   * return null;
   */
  public List getSubtreesThatReference(String refID) {
    List returnList = new ArrayList();
    try {
      String refpath = "//*/references";
      NodeList refs = XMLUtilities.getNodeListWithXPath(metadataNode, refpath);
      if (refs == null || refs.getLength() < 1) {
        Log.debug(12,
       "getSubtreesThatReference() found no subtrees that reference " + refID);
        return returnList;
      }
      for (int i = 0; i < refs.getLength(); i++) {
        Node nd = refs.item(i);
        String val = (nd.getFirstChild()).getNodeValue();
        val = val.trim();
        if (val.equals(refID.trim())) {
          returnList.add(nd.getParentNode());
        }
      }
    } catch (Exception w) {
      w.printStackTrace();
      Log.debug(12, "Problem in 'getSubtreesThatReference. "+w);
    }
    return returnList;
  }


  /**
   * replaces subtree identified by the passed unique String refID; returns null
   * if not found. Note that the new subtree will be given the same refID as the
   * subtree it replaces, even if the newSubtreeRoot node has a different id set
   *
   * @param refID unique String refID. Note that the new subtree will be given
   *   the same refID as the subtree it replaces, even if the newSubtreeRoot
   *   node has a different id set
   * @param newSubtreeRoot Node
   * @return root Node of new subtree, or null if refID not found
   */
  public Node replaceSubtreeAtReference(String refID, Node newSubtreeRoot) {

    Document doc = getMetadataNode().getOwnerDocument();
    try {
      Node oldSubtreeRoot = getSubtreeAtReferenceNoClone(refID);
      if (oldSubtreeRoot == null) return null;

      Node newIDAttr = newSubtreeRoot.getAttributes().getNamedItem("id");
      if (newIDAttr!=null && newIDAttr.getNodeType() == Node.ATTRIBUTE_NODE) {

        Log.debug(45, "replaceSubtreeAtReference() - newID="
                  + newIDAttr.getNodeValue());

        if (!newIDAttr.getNodeValue().equals(refID)) {
          Log.debug(25, "new Subtree ID (" + newIDAttr.getNodeValue()
                    + ") will be changed to agree with original subtree ID ("
                    + refID + ")");
          newIDAttr.setNodeValue(refID);
        }
      }
      Node importedSubtree = doc.importNode(newSubtreeRoot, true);
      oldSubtreeRoot.getParentNode().replaceChild(importedSubtree,
                                                  oldSubtreeRoot);
      return importedSubtree;

    } catch (Throwable w) {
      Log.debug(25, "Exception in replaceSubtreeAtReference)! " + w);
      w.printStackTrace();
    }
    return null;
  }



  /**
   * returns pointer to root Node of subtree identified by the passed unique
   * String refID; returns null if not found
   *
   * @param refID unique String refID
   * @return  pointer to root Node of subtree, or null if refID not found
   */
  public Node getSubtreeAtReferenceNoClone(String refID) {

    try{
      Node rootNode = getMetadataNode();
      NodeList refs2 = XMLUtilities.getNodeListWithXPath(rootNode, "//*[@id='"+refID+"']");
      // there should be a single node with the id (otherwise doc is eml invalid)
      if (refs2==null) return null;
      Node referencedNode = (refs2.item(0));
      // 'referencedNode' is the first order reference
      // next line calls to see if further references occur
      return getReferencedNode(referencedNode);

    } catch (TransformerException w) {
      Log.debug(25, "TransformerException trying to follow references (in getSubtreeAtReference)! "+w);
      w.printStackTrace();
    }
    return null;
  }

  /**
   *  no tag in eml2.0 for this information
   *  it is being put in additionalMetadata until a new version of eml is released
   */
  public boolean ignoreConsecutiveDelimiters(int entityIndex, int physicalIndex) {
    boolean ret = false;
    String temp = "";
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      Log.debug(20, "No such entity!");
      return ret;
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ( (physicals == null) || (physicals.length < 1)) {
      Log.debug(20, "no physicals!");
      return ret;
    }
    if (physicalIndex > (physicals.length - 1)) {
      Log.debug(20, "physical index too large!!");
      return ret;
    }
    Node physical = physicals[physicalIndex];
    try{
      NodeList aNodes = XPathAPI.selectNodeList(physical, "@id");
      if (aNodes == null) {
        Log.debug(30, "aNodes is null!");
        return ret;
      }
      if (aNodes.getLength()<1) {
        Log.debug(30, "aNodes is <1");
        return ret;
      }
      Node attr = aNodes.item(0);
      String id = attr.getNodeValue();
      Log.debug(40, "id: "+id);
      // now know the id of the physical element; look for additionalMetadata
      // since the additionaMetadata is only added when we want to ignore consecutive delimiter
      // we don't need to check the actual flag; just the presence of a describes tag with
      // the physical id
      NodeList amd = XPathAPI.selectNodeList(this.getMetadataNode(),"additionalMetadata/describes");
      if ((amd == null)||(amd.getLength()<1)) return ret;
      for (int j=0;j<amd.getLength();j++) {
        Node desNode = amd.item(j);
        String txt = desNode.getFirstChild().getNodeValue();
        txt = txt.trim();
        if (txt.equals(id)) return true;
      }
    }
    catch (Exception eee) {
      Log.debug(1, "exception in EML200");
    }
    return ret;
  }
  
  /**
   * Determines if the package is the latest eml version
   * @return
   */
  public boolean isLatestEMLVersion()
  {
	  String emlVersion = getEMLVersion();
      Log.debug(10, "\n\n**********Got the EML version: " + emlVersion);
      boolean isLatestEMLVersion = (emlVersion.toLowerCase()
                           .indexOf(EML200DataPackage.LATEST_EML_VER) != -1);
      return isLatestEMLVersion;
  }
   
  /**
   * Transform to the latest eml version. Null will be return if it couldn't be transformed.
   * This is a utility method
   * @return the eml document in string format
   */
  public String transformToLastestEML() throws EMLVersionTransformationException
  {
	  String result = transformToEML210();
	  return result;
  }
  
  /*
   * Transform to eml 210 version. Null will be return if it couldn't be transformed.
   */
  private String transformToEML210() throws EMLVersionTransformationException
  {
	  String result = null;
	  try
	  {
		  result= doTransform("./xsl/eml201to210.xsl", XMLUtil.getDOMTreeAsString(
                            getMetadataNode().getOwnerDocument())) ;
	  }
	  catch(EMLVersionTransformationException e)
	  {
		  throw e;
	  }
	  catch(Exception e)
	  {
		  Log.debug(5, "Couldn't transform the eml document to the latest version "+e.getMessage());
	  }
	  return result;
  }
  
  /*
   * Transform an eml document to another version of eml2 document by specifying the
   * style sheet. The output will be string of the new doc. If  transform couldn't happen,
   * null will be returned.
   */
  private String doTransform(String styleSheetPath, String XMLinput)
  throws TransformerException, TransformerConfigurationException, EMLVersionTransformationException, Exception{
	 String output = null;
	// Instantiate  a TransformerFactory.
	TransformerFactory tFactory = TransformerFactory.newInstance();
	//MyURIResolver res = new MyURIResolver();		
	//tFactory.setURIResolver(res);
	
	//System.out.println("==================1 "+XMLinput);
	// Determine whether the TransformerFactory supports The use uf SAXSource
	// and SAXResult
	if (tFactory.getFeature(SAXSource.FEATURE) &&  tFactory.getFeature(StreamResult.FEATURE))
	{
		// Cast the TransformerFactory to SAXTransformerFactory.
		SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) tFactory);
		// Create a TransformerHandler for  stylesheet.
	    File f2 = new File(styleSheetPath);
		TransformerHandler tHandler2 = saxTFactory.newTransformerHandler(new StreamSource(f2));
		Transformer lastTransformer = tHandler2.getTransformer();
		lastTransformer.setOutputProperty(OutputProperties.S_KEY_INDENT_AMOUNT, EMLConvert.indentAmount);
		
		//add code to handle the error message from xslt style sheet
		XSLTErrorListener xslErrorListener = new XSLTErrorListener();
		lastTransformer.setErrorListener(xslErrorListener);
		
		// Create an XMLReader.
		XMLReader reader = XMLReaderFactory.createXMLReader();
	   	reader.setContentHandler(tHandler2);
		reader.setProperty("http://xml.org/sax/properties/lexical-handler", tHandler2);
		/*File outfile = new File("example");
		FileWriter writer = new FileWriter(outfile);
		Result result = new StreamResult(writer);*/
		CharArrayWriter outputWriter  = new CharArrayWriter();
		Result result = new StreamResult(outputWriter);
		//DOMResult result = new DOMResult();
		tHandler2.setResult(result);
		// Parse the XML input document. The input ContentHandler and output ContentHandler
		// work in separate threads to optimize performance.	
		reader.parse(new InputSource(new StringReader(XMLinput)));
		//outputNode = result.getNode();
		output = outputWriter.toString();
		//System.out.println("==================6 "+output);
		if (xslErrorListener.getWarningMessage() != null)
		{
		   /*Log.debug(5, "Transforming the eml document to the latest version was done, however it got those warning(s) "+
				   xslErrorListener.getWarningMessage());*/
			EMLVersionTransformationException exception = new EMLVersionTransformationException(xslErrorListener.getWarningMessage(), output);
		    throw exception;
		}
	
	}
	else
	{
		Log.debug(3, "The parser doesn't support SAXsource or SAXresult or DOMresult featurs. \n"+
				 "It couldn't be save to newest EML document ");
	}
	return output;
  }
  
  /*
   * A class to catch the error from xslt style sheet
   */
    private class XSLTErrorListener implements ErrorListener{
    	
      private String warningMessage = null;
	  
	  public void error(TransformerException e) throws TransformerException
	  {
		  Log.debug(30, "error method "+e.getMessage());
		  throw e;
	  }
	  
	  public void fatalError(TransformerException e) throws TransformerException
	  {
		  Log.debug(30, "fatal error method "+e.getMessage());
		  // Gets the warning message from xslt
		  if (warningMessage != null)
		  {
			  throw(new TransformerException(warningMessage));
		  }
		  else
		  {
		      throw e;
		  }
	  }
	  
	  public void warning(TransformerException e) throws TransformerException
	  {
		  Log.debug(30, "warning method "+e.getMessage());
		  if (warningMessage == null)
		  {
		     warningMessage = e.getMessage();
		  }
		  else
		  {
			  warningMessage = warningMessage+"\n"+e.getMessage();
		  }
		  //throw(new TransformerException(warningMessage));
	  }
	  
	  /**
	   * Get the warning message from transformation style sheet
	   * @return
	   */
	  public String getWarningMessage()
	  {
		  return warningMessage;
	  }
	  
  }

}

