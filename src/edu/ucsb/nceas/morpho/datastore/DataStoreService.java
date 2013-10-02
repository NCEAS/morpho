/**
 *  '$RCSfile: DataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2009-02-06 21:26:34 $'
 * '$Revision: 1.10 $'
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

package edu.ucsb.nceas.morpho.datastore;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;


import org.dataone.service.types.v1.Identifier;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ProfileDialog;

/**
 * creates an abstract class for getting files from any dataStore using the same
 * methods.
 */
public abstract class DataStoreService implements DataStoreServiceInterface
{
   
    protected static final String RESOURCEMAPSOLRFIELD = "resourceMap";
	protected static final String RESOURCE_MAP_ID_PREFIX = "resourceMap_";
	protected Morpho morpho;
	protected ConfigXML config;
	
  
  /**
   * create a new FileSystemDataStore for a Morpho
   */
  public DataStoreService(Morpho morpho)
  {
    this.morpho = morpho;
    config = Morpho.getConfiguration();

  }
  
  /**
   * Gets the temp directory
   * @return
   */
  protected String getTempDir() {
		ConfigXML profile = morpho.getProfile();
		String tempdir = getProfileDir(profile) + File.separator + profile.get("tempdir", 0);
		return tempdir;
	}
	
	/**
	 * Get the profile directory for a specified profile
	 * @return the profile directory path string.
	 */
	public static String getProfileDir(ConfigXML profile) {
	  return getProfilesParentDir()+
	      File.separator+profile.get(ProfileDialog.PROFILENAMEELEMENTNAME, 0);
	}
	
	/**
   * Get the currently using profile directory
   * @return the profile directory path string.
   */
  public static String getProfileDir() {
    return getProfilesParentDir() + File.separator+
        Morpho.thisStaticInstance.getProfile().get(ProfileDialog.PROFILENAMEELEMENTNAME, 0);
  }
  
  
  /**
   * Get the the directory which contains all profiles directories
   * @return the path of directory which contains all profiles directories
   */
  public static String getProfilesParentDir() {
    return ConfigXML.getConfigDirectory()+File.separator+Morpho.getConfiguration().get("profile_directory", 0);
  }
  
  
  /*
   * Get the cache directory for the current profile.
   */
  protected String getCacheDir() {
    ConfigXML profile = morpho.getProfile();
    String profileDirName = ConfigXML.getConfigDirectory() + File.separator
        + config.get("profile_directory", 0) + File.separator
        + profile.get("profilename", 0);
    String cachedir = profileDirName + File.separator
        + profile.get("cachedir", 0);
    return cachedir;
  }
  
  /*
   * Get the system metadata directory associated with the object directory.
   * This method doesn't guarantee the existing of the system metadata directory. 
   */
  protected String getSystemMetadataDir(String objectDir) {
    return objectDir+File.separator+ProfileDialog.SYSTEMMETADATADIRNAME;
  }
  
  /**
   * Check if the specified MorphoDataPackage has entity.
   * @param mdp
   * @return true if it has; otherwise false.
   */
  protected boolean hasEntity(MorphoDataPackage mdp) {
      boolean hasEntity = false;
      if(mdp != null) {
          AbstractDataPackage adp = mdp.getAbstractDataPackage();
          if(adp != null) {
              String identifier = adp.getAccessionNumber();
              Map<Identifier, List<Identifier>>map= mdp.getMetadataMap();
              
              if(map != null) {
                Identifier id = new Identifier();
                id.setValue(identifier);
                List<Identifier> list = map.get(id);
                if(list != null && list.size() >0) {
                  hasEntity = true;
                }
              }
          }
          
      }
      return hasEntity;
  }
  
  /**
   * Find an identifier of a resource map which contains the specified identifier.
   * This method will query the solr index and solr index will return a list of resource map id which contains the metadata id.
   * However, we assume the relationship between the resource map id and the metadata is 1 to 1. So we just choose the first one as the resource map id.
   * The default value is to add a resource map prefix.
   * @param identifier
   * @return
   */
   protected String lookupResourceMapId(String identifier) {
          try {
             //The mn supports solr engine. Let's query it to find the resource map.
              String query = "q=id:"+"\""+identifier +"\""+"&"+"fl="+RESOURCEMAPSOLRFIELD;
              InputStream response = Morpho.thisStaticInstance.getDataONEDataStoreService().query(query, DataONEDataStoreService.SOLRQUERYENGINE);
              XPathFactory factory = XPathFactory.newInstance();
              XPath xPath = factory.newXPath();
              NodeList list = (NodeList) xPath.evaluate("//doc/arr[@name='resourceMap']/str/text()", new InputSource(response), XPathConstants.NODESET);
              if(list != null && list.getLength() >0) {
                  Node node = list.item(0);
                  if(node != null) {
                      String resourceMapId = node.getNodeValue();
                      if(resourceMapId != null && !resourceMapId.trim().equals("")) {
                          System.out.println("we got the resource map id from query !!!!");
                          return resourceMapId;
                      }
                  }
                  
              }
              return RESOURCE_MAP_ID_PREFIX + identifier;
          } catch (Exception e) {
              e.printStackTrace();
              return RESOURCE_MAP_ID_PREFIX + identifier;
          }
      
      
  }

}
