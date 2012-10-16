/**
 *        Name: AttributeEditDialog.java
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2005-02-22 23:21:51 $'
 * '$Revision: 1.9 $'
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
package edu.ucsb.nceas.morpho.datastore.idmanagement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

import edu.ucsb.nceas.morpho.datastore.DataStoreService;

/**
 * This class represents an object which manage the revision history for a data store in
 * a profile. 
 * @author tao
 *
 */
public class RevisionManager {
  
  public static final String ORIGINALDOC = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
      +"<revisions>\n"+"</revisions>\n";
  public static final String IDENTIFIER = "identifier";
  public static final String VALUE = "value";
  public static final String OBSOLETES = "obsoletes";
  public static final String OBSOLETEDBY = "obsoletedBy";
  public static final String SLASH = "/";
  public static final String AT = "@";
  
  private static final String SUFFIX = ".revision.properties";
  private static final boolean AUTOSAVE = true;
  
  private Map<String, String> obsoletes = null;
  private Map<String, String> obsoletedBy = null;
  private String filePrefix = null;
  private XMLConfiguration configuration = null;
  
  
  
  /**
   * Constructor.
   * @param filePrefix - the prefix will be used for the file which stores the revision history.
   */
  public RevisionManager(String filePrefix) throws IllegalArgumentException, IOException, ConfigurationException{
    if(filePrefix == null || filePrefix.trim().equals("")) {
      throw new IllegalArgumentException("RevisionManager.RevisionManager - A null or empty string"+
                                          " can't be used as a prefix for the file which stores the revision information.");
    }
    this.filePrefix = filePrefix;
    init();
  }
  
  /*
   * Initialize the configuration from the file.
   */
  private void init() throws IOException, ConfigurationException {
    String profileDir = DataStoreService.getProfileDir();
    File configurationFile = new File(profileDir, File.separator+filePrefix+SUFFIX);
    //initial a xml properties file
    if(!configurationFile.exists()) {
      configurationFile.createNewFile();
      OutputStreamWriter writer = null;
      FileOutputStream output = null;
      try {
        output = new FileOutputStream(configurationFile);
        writer = new OutputStreamWriter(output, IdentifierFileMap.UTF8);
        writer.write(ORIGINALDOC);
      } finally {
        writer.close();
        output.close();
      }
    }
    configuration = new XMLConfiguration(configurationFile);
    configuration.setExpressionEngine(new XPathExpressionEngine());
    configuration.setAutoSave(AUTOSAVE);
    
    obsoletes = new HashMap<String, String>();
    obsoletedBy = new HashMap<String, String> ();
    List<Object> identifiers = configuration.getList(IDENTIFIER+SLASH+AT+VALUE);
    if(identifiers != null) {
      for(Object id : identifiers) {
        String identifier = (String) id;
        List<Object> obsoletesIds = configuration.getList(IDENTIFIER+"["+AT+VALUE+"='"+identifier+"']"+SLASH+OBSOLETES);
        if(obsoletesIds != null) {
          for(Object obsoletesId : obsoletesIds) {
            String obsoletesIdentifier = (String) obsoletesId;
            System.out.println("the identifier is "+identifier);
            System.out.println("the obsoletes id is "+obsoletesIdentifier);
            setObsoletes(identifier, obsoletesIdentifier);
          }
        }
        
        List<Object> obsoletedByIds = configuration.getList(IDENTIFIER+"["+AT+VALUE+"='"+identifier+"']"+SLASH+OBSOLETEDBY);
        if(obsoletedByIds != null) {
          for(Object obsoletedById : obsoletedByIds) {
            String obsoletedByIdentifier = (String) obsoletedById;
            System.out.println("the identifier is "+identifier);
            System.out.println("the obsoletedBy id is "+obsoletedByIdentifier);
            setObsoletedBy(identifier, obsoletedByIdentifier);
          }
        }
      }
    }
  }
  
  /**
   * Get the list of all revisions for the specified identifier. The list is in descending order.
   * @param identifier - the specified identifier.
   * @return the list of all revision which includes the given version.
   */
  public List<String> getAllRevisions(String identifier) {
    List<String> revisions = new Vector<String>();
    revisions.add(identifier);
    String obsoletesId = getObsoletes(identifier);
    while(obsoletesId != null) {
      revisions.add(obsoletesId);
      obsoletesId = getObsoletes(obsoletesId);
    }
    
    String obsoletedById = getObsoletedBy(identifier);
    while (obsoletedById != null) {
      revisions.add(0,obsoletedById);
      obsoletedById = getObsoletedBy(obsoletedById);   
    }
    return revisions;
  }
  
  
  /**
   * Get the identifier of the latest revision for the specified identifier.
   * @param identifier - the specified identifier
   * @return the identifier of the latest revision.
   */
  public String getLatestRevision(String identifier) {
    String last = getObsoletedBy(identifier);
    if(last == null) {
      return identifier;
    } else {
      return getLatestRevision(last);
    }
  }
  
  /**
   * Get the identifier of the previous version of the specified identifier
   * @param identifier - the specified identifier
   * @return the identifier of the previous version. Null will be returned if
   * no previous version identifier is found.
   */
  public String getObsoletes(String identifier) {
    String obsoleteId = obsoletes.get(identifier);
    return obsoleteId;
  }
  
  /**
   * Get the identifier of the next version of the specified identifier
   * @param identifier - the specified identifier
   * @return the next version of the specified identifier. Null will be returned if
   * no next version identifier is found.
   */
  public String getObsoletedBy(String identifier) {
    String obsoletedById = obsoletedBy.get(identifier);
    return obsoletedById;
  }
  
  /**
   * Set relationship that a new identifier obsoletes the old identifier.
   * @param newId - the new identifier which obsoletes the old one.
   * @param oldId - the old identifier which will be obsoleted by the new one.
   */
  public void setObsoletes(String newId, String oldId) {
    obsoletes.put(newId, oldId);
  }
  
  
  /**
   * Set a relationship that a old identifier is obsoleted by the new identifier.
   * @param oldId - the old identifier which will be obsoleted.
   * @param newId - the new identifier which obsoletes the old one.
   */
  public void setObsoletedBy(String oldId, String newId) {
    obsoletedBy.put(oldId, newId);
  }

}
