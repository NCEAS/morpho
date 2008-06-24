/**
 *  '$RCSfile: EML201DocumentCorrector.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-06-24 00:23:47 $'
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

import java.util.List;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.EML200DataPackage;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
/**
 * Before Metacat 1.8.1 release, Metacat uses the eml201 schema with the tag
 * RELEASE_EML_2_0_1_UPDATE_5. Unfortunately, this tag points at wrong version
 * of eml-resource.xsd. In this schema, the element "references" has an attribute named
 * "system" and the attribute has a default value "document". Metacat will add 
 * the attribute system="document" to "references" element even the orginal eml didn't have it
 * (this is another bug and see bug 1601), so this causes metacat generated some invalid eml 2.0.1
 * documents. If user uses Morpho to generate an eml 2.0.1 document, the local copy wouldn't 
 * have the extral attribute.  However, morpho will have an invalid eml 2.0.1document when  morpho
 * download a new document from metacat 1.8.0 or previous verion. This class provides a path to fix the 
 * existed invalid eml 2.0.1 documents in morpho local system. It will DOM parser remove the attribute 
 * system="document" of the element "references" in local eml 2.0.1 documents.  The docid will keep
 * as same as before.
 * @author tao
 *
 */
public class EML201DocumentCorrector {
	
	private String docid;
	private static final String EML201NAMESPACE = "eml://ecoinformatics.org/eml-2.0.1";
	private static final String REFERENCEPATH = "references";
	private static final String SYSTEM = "system";
	
	/**
	 * Constructor of this class
	 * @param docid  the given docid
	 */
     public EML201DocumentCorrector(String docid)
     {
    	  this.docid = docid;
     }
     
     /**
      * Correct the eml 2.0.1 document - remove "system" attribute from
      * "references" element from a local eml document, then save it.
      * @throws Exception
      */
     public void correctDocument() throws Exception
     {
    	 AbstractDataPackage dataPackage = getDataPackage();
    	 if (dataPackage != null && dataPackage instanceof EML200DataPackage)
    	 {
    		  EML200DataPackage eml2Package = (EML200DataPackage)dataPackage;
    		  String namespace = eml2Package.getXMLNamespace();
    		  Log.debug(30,  "DocTypeInfo: " + namespace);
    		  if (namespace.equals(EML201NAMESPACE))
    		  {
    			  // remove the extral attribute
    			  removeExtralAttributes(eml2Package);
    			  // save the new  package to old id.
    			  eml2Package.serialize(AbstractDataPackage.LOCAL);
    		  }
    			  
    	 }
     }
     
     /*
      * Get the local eml document package with the given id
      */
     private AbstractDataPackage getDataPackage()
     {
    	 AbstractDataPackage dataPackage = null;
    	 boolean metacat = false;
    	 boolean local       = true;
    	 dataPackage = DataPackageFactory.getDataPackage(docid, metacat, local);
    	 return dataPackage;
     }
     
     /*
      * Remove the extral attributes from reference element
      */
     private void removeExtralAttributes(AbstractDataPackage dataPackage)
     {
    	  if (dataPackage != null)
    	  {
    		  List list = dataPackage.getSubtrees(REFERENCEPATH);
    		  if (list != null)
    		  {
    			  // Go through every reference element
    			  for (int i=0;i<list.size();i++)
    			  {
    				  Node node = (Node)list.get(i);
    				  if (node != null )
    				  {
    					   NamedNodeMap attributeList= node.getAttributes();
    					   if (attributeList != null)
    					   {
    						   for (int j=0; j<attributeList.getLength(); j++)
    						   {
    							     Node attribute = attributeList.item(j);
    							     // find the attribute and remove it
    							     if (attribute != null && attribute.getNodeName().equals(SYSTEM))
    							     {
    							    	 node.removeChild(attribute);
    							     }
    						   }
    					   }
    				  }
    			  }
    		  }
    	  }
     }
	

}
