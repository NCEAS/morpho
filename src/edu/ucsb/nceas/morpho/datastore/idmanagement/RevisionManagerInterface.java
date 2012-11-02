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

import java.util.List;

/**
 * An interface for the revision manager
 * @author tao
 *
 */
public interface RevisionManagerInterface {
  
  /**
   * Get the list of all revisions for the specified identifier. The list is in descending order.
   * If the list only includes the given identifier, it means there are no other revisions; doesn't 
   * mean the identifier exists in the data store.
   * @param identifier - the specified identifier.
   * @return the list of all revision which includes the given version.
   */
  public List<String> getAllRevisions(String identifier) throws Exception;

  /**
   * Get the identifier of the latest revision for the specified identifier.
   * @param identifier - the specified identifier
   * @return the identifier of the latest revision.
   */
  public String getLatestRevision(String identifier) throws Exception;
  
  /**
   * Get the identifier of the previous version of the specified identifier
   * @param identifier - the specified identifier
   * @return the identifier of the previous version. Null will be returned if
   * no previous version identifier is found.
   */
  public String getObsoletes(String identifier) throws Exception;
  
  /**
   * Get the identifier of the next version of the specified identifier
   * @param identifier - the specified identifier
   * @return the next version of the specified identifier. Null will be returned if
   * no next version identifier is found.
   */
  public String getObsoletedBy(String identifier) throws Exception;
  
  /**
   * Set relationships that a new identifier obsoletes the old identifier. It will
   * handle the both "obsoletes" and "obsoletedBy" relationship.
   * @param newId - the new identifier which obsoletes the old one.
   * @param oldId - the old identifier which will be obsoleted by the new one.
   */
  public void setObsoletes(String newId, String oldId) throws IllegalArgumentException;
  
  /**
   * Set a relationship that a old identifier is obsoleted by the new identifier.
   * @param oldId - the old identifier which will be obsoleted.
   * @param newId - the new identifier which obsoletes the old one.
   */
  //public void setObsoletedBy(String oldId, String newId) throws IllegalArgumentException;
  
  /**
   * Delete the specified identifier from the revision file
   * @param identifier - the identifier will be deleted
   */
  public void delete(String identifier);
}
