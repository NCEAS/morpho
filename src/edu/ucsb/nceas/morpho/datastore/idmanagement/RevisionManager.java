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
 * This class represents an object which manage the revision history for a data store service in
 * a profile. The object will be serialized in the file - 
 * .morpho/profiles/profile-name/given-prefix-revision.properties
 * The file is a xml and it looks like:
 * <?xml version="1.0" encoding="UTF-8" ?>
 * <revisions>
 *  <identifier value="pid.1.2">
 *   <obsoletes>pid.1.1</obsoletes>
 *   <obsoletedBy>pid.1.3</obsoletedBy>
 *  </identifier>
 *  <identifier value="pid.2.2">
 *    <obsoletes>pid.2.1</obsoletes>
 *  </identifier>
 * </revisions>
 * @author tao
 *
 */
public class RevisionManager implements RevisionManagerInterface {
  
  public static final String ORIGINALDOC = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
      +"<revisions>\n"+"</revisions>\n";
  public static final String IDENTIFIER = "identifier";
  public static final String VALUE = "value";
  public static final String OBSOLETES = "obsoletes";
  public static final String OBSOLETEDBY = "obsoletedBy";
  public static final String SLASH = "/";
  public static final String AT = "@";
  public static final String SUFFIX = "-revisions.properties";
  private static final boolean AUTOSAVE = true;
  
  private Map<String, String> obsoletes = null;
  private Map<String, String> obsoletedBy = null;
  private String filePrefix = null;
  private XMLConfiguration configuration = null;
  private File profileDir = null;
  
  /**
   * Manage created instances of managers
   */
  private static Map<String, RevisionManager> managers = new HashMap<String, RevisionManager>();
  
  public static RevisionManager getInstance(String profileDir, String prefix) throws IllegalArgumentException, ConfigurationException, IOException {
	  String key = profileDir + prefix;
	  RevisionManager manager = managers.get(key);
	  if (manager == null) {
		  manager = new RevisionManager(profileDir, prefix);
		  managers.put(key, manager);
	  }
	  return manager;
  }
  
  /**
   * Constructor.
   * @param filePrefix - the prefix will be used for the file which stores the revision history.
   */
  private RevisionManager(String profileDir, String filePrefix) throws IllegalArgumentException, IOException, ConfigurationException{
    if(filePrefix == null || filePrefix.trim().equals("")) {
      throw new IllegalArgumentException("RevisionManager.RevisionManager - A null or empty string"+
                                          " can't be used as a prefix for the file which stores the revision information.");
    }
    this.filePrefix = filePrefix;
    this.profileDir = new File(profileDir);
    if(!this.profileDir.exists() || !this.profileDir.isDirectory()) {
      throw new IllegalArgumentException("RevisionManager.RevisionManager - the profile directory in the parameter should point "+
                                        "to an existed directory. However, the "+profileDir+" either doesn't exist or is not a directory");
    }
    init();
  }
  
  /*
   * Initialize the configuration from the file.
   */
  private void init() throws IOException, ConfigurationException {
    //String profileDir = DataStoreService.getProfileDir();
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
    configuration.setDelimiterParsingDisabled(true);
    configuration.setAttributeSplittingDisabled(true);
    
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
            //System.out.println("the identifier is "+identifier);
            //System.out.println("the obsoletes id is "+obsoletesIdentifier);
            setObsoletesRelation(identifier, obsoletesIdentifier);
          }
        }
        
        List<Object> obsoletedByIds = configuration.getList(IDENTIFIER+"["+AT+VALUE+"='"+identifier+"']"+SLASH+OBSOLETEDBY);
        if(obsoletedByIds != null) {
          for(Object obsoletedById : obsoletedByIds) {
            String obsoletedByIdentifier = (String) obsoletedById;
            //System.out.println("the identifier is "+identifier);
            //System.out.println("the obsoletedBy id is "+obsoletedByIdentifier);
            setObsoletedByRelation(identifier, obsoletedByIdentifier);
          }
        }
      }
    }
  }
  
  /**
   * Get the list of all revisions for the specified identifier. The list is in descending order.
   * If the list only includes the given identifier, it means there are no other revisions; doesn't 
   * mean the identifier exists in the data store.
   * @param identifier - the specified identifier.
   * @return the list of all revision which includes the given version.
   */
  public List<String> getAllRevisions(String identifier) {
    List<String> revisions = new Vector<String>();
    revisions.add(identifier);
    String obsoletesId = getObsoletes(identifier);
    //System.out.println("the obsoletes id is "+obsoletesId);
    while(obsoletesId != null) {
      //System.out.println("the obsoletes id is "+obsoletesId);
      revisions.add(obsoletesId);
      //System.out.println("add the id - "+obsoletesId);
      obsoletesId = getObsoletes(obsoletesId);
    }
    
    String obsoletedById = getObsoletedBy(identifier);
    //System.out.println("the obsoletedby id is "+obsoletesId);
    while (obsoletedById != null) {
      //System.out.println("the obsoletedby id is "+obsoletesId);
      revisions.add(0,obsoletedById);
      //System.out.println("add the obsoletedby id "+obsoletesId);
      obsoletedById = getObsoletedBy(obsoletedById);   
    }
    return revisions;
  }
  
  
  /**
   * Get the list of all the revisions which is older than the specified identifier. The list is in descending order.
   * Empty list will be returned if there are no older versions.
   * @param identifier - the specified identifier.
   * @return the list of all revisions which are older than the specified identifier.
   */
  public List<String> getOlderRevisions(String identifier) {
    List<String> revisions = new Vector<String>();
    String obsoletesId = getObsoletes(identifier);
    //System.out.println("the obsoletes id is "+obsoletesId);
    while(obsoletesId != null) {
      //System.out.println("the obsoletes id is "+obsoletesId);
      revisions.add(obsoletesId);
      //System.out.println("add the id - "+obsoletesId);
      obsoletesId = getObsoletes(obsoletesId);
    }
    return revisions;
  }
  
  
  /**
   * Get the list of all the revisions which is newer than the specified identifier. The list is in descending order.
   * Empty list will be returned if there are no newer versions.
   * @param identifier - the specified identifier.
   * @return the list of all revisions which are newer than the specified identifier.
   */
  public List<String> getNewerRevisions(String identifier) {
    List<String> revisions = new Vector<String>();
    String obsoletedById = getObsoletedBy(identifier);
    //System.out.println("the obsoletedby id is "+obsoletesId);
    while (obsoletedById != null) {
      //System.out.println("the obsoletedby id is "+obsoletesId);
      revisions.add(0,obsoletedById);
      //System.out.println("add the obsoletedby id "+obsoletesId);
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
   * Set relationships that a new identifier obsoletes the old identifier. It will
   * handle the both "obsoletes" and "obsoletedBy" relationship.
   * @param newId - the new identifier which obsoletes the old one.
   * @param oldId - the old identifier which will be obsoleted by the new one.
   */
  public void setObsoletes(String newId, String oldId) throws IllegalArgumentException {
    setObsoletedByRelation(oldId, newId);
    setObsoletesRelation(newId, oldId);
  }
  
  
  /**
   * Set relationship that a new identifier obsoletes the old identifier.
   * @param newId - the new identifier which obsoletes the old one.
   * @param oldId - the old identifier which will be obsoleted by the new one.
   */
  private synchronized void setObsoletesRelation(String newId, String oldId) throws IllegalArgumentException {
    if(newId == null || newId.trim().equals("")) {
      throw new IllegalArgumentException("RevisionManager.setObsoletes - the first parameter of this method can't be null or blank.");
    }
    if( oldId == null || oldId.trim().equals("")) {
      throw new IllegalArgumentException("RevisionManager.setObsoletes - the second parameter of this method can't be null or blank.");
    }
    if(!newId.equals(oldId)) {
      obsoletes.put(newId, oldId);
      modifyConfiguration(newId, OBSOLETES, oldId);
    }
    
  }
  
  
  
  
  /**
   * Set a relationship that a old identifier is obsoleted by the new identifier.
   * @param oldId - the old identifier which will be obsoleted.
   * @param newId - the new identifier which obsoletes the old one.
   */
  private synchronized void setObsoletedByRelation(String oldId, String newId) throws IllegalArgumentException {
    if(newId == null || newId.trim().equals("")) {
      throw new IllegalArgumentException("RevisionManager.setObsoletes - the second parameter of this method can't be null or blank.");
    }
    if( oldId == null || oldId.trim().equals("")) {
      throw new IllegalArgumentException("RevisionManager.setObsoletes - the first parameter of this method can't be null or blank.");
    }
    if(!oldId.equals(newId)) {
      obsoletedBy.put(oldId, newId);
      modifyConfiguration(oldId, OBSOLETEDBY, newId);
    }
   
  }
  
  
  /**
   * Modify the configuration file
   * @param targetId - the subject id the action (obsoletes or obsoletedBy)
   * @param action - obsoletes or obsoletedBy
   * @param id - id impose the action
   */
  private synchronized void modifyConfiguration(String targetId, String action, String id ) {
    if(configuration != null) {
      try {
        //The xpath  IDENTIFIER+"["+AT+VALUE+"='"+targetId+"']" doesn't work for getList().
        //we have to give up
        //List<Object> targetIdList = configuration.getList(IDENTIFIER+"["+AT+VALUE+"='"+targetId+"']");
        //System.out.println("the target identifier size is === "+targetIdList.size());
        List<Object> identifierList = configuration.getList(IDENTIFIER+SLASH+AT+VALUE);
        boolean targetIdExisted = identifierList.contains(targetId);
        //System.out.println("the identifier existing in the configuration is "+targetIdExisted);
        if(!targetIdExisted) {
          //no record existing for the id, so add one
          configuration.addProperty(" "+IDENTIFIER+AT+VALUE, targetId);
          configuration.addProperty(IDENTIFIER+"["+AT+VALUE+"='"+targetId+"']"+" "+action, id);
        } else {
          //if more than one target id were found, something is wrong. but we only handle the first one
          List<Object> elementList = configuration.getList("("+IDENTIFIER+"["+AT+VALUE+"='"+targetId+"']"+")[1]"+SLASH+action);
          //System.out.println("the element list size is "+elementList.size());
          if(elementList == null || elementList.size() == 0) {
            //add the new element
            configuration.addProperty("("+IDENTIFIER+"["+AT+VALUE+"='"+targetId+"']"+")[1]"+" "+action, id);
          } else {
            // there is existing element for the action, just reset the value. In future, we may throw this to an exception
            configuration.setProperty("("+IDENTIFIER+"["+AT+VALUE+"='"+targetId+"']"+")[1]"+SLASH+action, id);
          }
        }
        
      } finally {
        //System.out.println("reload is called");
        configuration.reload();
      }

    }
  }
  
  /**
   * Delete the specified identifier from the revision file
   * @param identifier - the identifier will be deleted
   */
  public synchronized void delete(String identifier) {
    
    if(identifier != null && !identifier.trim().equals("")) {
      String cleanXPath = IDENTIFIER+"["+AT+VALUE+"='"+identifier+"']";
      //System.out.println("the identifier "+identifier);
      //System.out.println("obsoleted by "+obsoletedBy);
      //System.out.println("obsoletes "+obsoletes);
      String obsoletesId = getObsoletes(identifier);
      //System.out.println("obsoletes id "+obsoletesId);
      String obsoletedById = getObsoletedBy(identifier);
      //System.out.println("obsoleted by id "+obsoletedById);
      if(obsoletesId == null && obsoletedById != null) {
        obsoletedBy.remove(identifier);
        obsoletes.remove(obsoletedById);
        configuration.clearTree(cleanXPath);
        configuration.clearProperty(IDENTIFIER+"["+AT+VALUE+"='"+obsoletedById+"']"+SLASH+OBSOLETES);
       
      } else if (obsoletesId != null && obsoletedById == null) {
       
        obsoletes.remove(identifier);
        obsoletedBy.remove(obsoletesId);
        configuration.clearTree(cleanXPath);
        configuration.clearProperty(IDENTIFIER+"["+AT+VALUE+"='"+obsoletesId+"']"+SLASH+OBSOLETEDBY);
      } else if ( obsoletesId != null && obsoletedById != null) {
        
        obsoletes.remove(identifier);
        obsoletes.remove(obsoletedById);
        obsoletedBy.remove(identifier);
        obsoletedBy.remove(obsoletesId);
        configuration.clearTree(cleanXPath);
        setObsoletedByRelation(obsoletesId, obsoletedById);
        setObsoletesRelation(obsoletedById, obsoletesId);
        //obsoletes.put(obsoletedById,obsoletesId);
        //obsoletedBy.put(obsoletesId, obsoletedById);
        //System.out.println("obsoleted by "+obsoletedBy);
        //System.out.println("obsoletes "+obsoletes);
      }
    }
  }

}
