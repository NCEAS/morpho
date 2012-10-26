package edu.ucsb.nceas.morpho.datastore.idmanagement;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.exception.ConfigurationException;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.Log;

/**
 *  '$RCSfile: CacheAccessException.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-06 21:10:39 $'
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

/**
 * This class represents an object which can generate the identifiers for the metadata
 * and data objects. The local id looks like urn:id:scope.X. The scope is the string user input in
 * the profile creating process. The X is a number which is kept in the profile xml file. 
 * Once a new identifier is generated, the X will be increased by 1. This is a singleton class.
 * @author tao
 *
 */
public class LocalIdentifierGenerator
{
  public static final String LASTID = "lastId";
  public static final String URNID = "urn:id:";
  public static final String DOT = ".";
  private ConfigXML profile = null;
  String scope  = null; 
  long serialNumber = 1;
  private static LocalIdentifierGenerator manager= null;
  
  /*
   * Private constructor
   */
  private LocalIdentifierGenerator(ConfigXML profile) throws ConfigurationException
  {
    this.profile = profile;
    getScopeFromProfile();
    getSerialnumberFromProfile();
  }
  
  /**
   * Get an instance of the Identifier manager 
   * @param profile
   * @return
   */
  public static LocalIdentifierGenerator getInstance(ConfigXML profile) throws ConfigurationException
  {
    if(manager == null)
    {
      manager = new LocalIdentifierGenerator(profile);
    }
    return manager;
  }
  
  /*
   * Get the Scope from the profile
   */
  private synchronized void getScopeFromProfile() throws ConfigurationException
  {
    if(profile != null)
    {
      scope = profile.get("scope", 0);
      if(scope == null || scope.trim().equals(""))
      {
        throw new ConfigurationException("IdentifierManager.getScopeFromProfile - There is no \"scope\" element in the profile ");
      }
    }
    else
    {
      throw new ConfigurationException("IdentifierManager.getScopeFromProfile - Couldn't get the scope from the profile since the profile object is null.");
    }
   
    
  }
  /*
   * Get the serial number (last id) from the profile
   */
  private synchronized void getSerialnumberFromProfile() throws ConfigurationException
  {
    if(profile != null)
    {
      String lastidS = profile.get(LASTID, 0);
      if(lastidS == null || lastidS.trim().equals(""))
      {
        throw new ConfigurationException("IdentifierManager.getSerialNumberFromProfile - Couldn't get the serial number from the profile since there is no element named \"lastid\" in the profilel.");
      }
      try
      {
          serialNumber = (new Long(lastidS)).longValue();
      }
      catch(NumberFormatException e)
      {
        throw new ConfigurationException("IdentifierManager.getSerialNumberFromProfile - Couldn't get the serial number from the profile since the value of the element \"lastid\" "+
         lastidS+" is not a number.");
      }
    }
    else
    {
      throw new ConfigurationException("IdentifierManager.getSerialNumberFromProfile - Couldn't get the serial number from the profile since the profile object is null.");
    }
   
  }
  
 
  /*
   * Update the value of the  "lastId " element in the profile.
   */
  private synchronized void updateSerialNumber()
  {
    if(profile != null)
    {
      try
      {
        String id = (new Long(serialNumber)).toString();
        profile.set(LASTID, 0, id);
        profile.save();
      }
      catch(Exception e)
      {
        Log.debug(20, "IdentifierManager.updateSerialNumber - Couldn't update the value of the \"lastId\" since "+e.getMessage());
      }
    }
    else
    {
      Log.debug(20, "IdentifierManager.updateSerialNumber - Couldn't update the value of the \"lastId\" since the proifle object is null");
    }
           
      
    
  }
  
  /**
   * Generate the local id. 
   * @return the generated local id. Null will be returned if id can't be generated correctly  
   */
  public String generatLocalId() 
  {
    String newId = null;
    newId = URNID+scope+DOT+serialNumber;
    serialNumber++;
    updateSerialNumber();
    return newId;
  }
  
}
