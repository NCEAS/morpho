/**
 *  '$RCSfile: QueryRefreshInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-29 06:04:36 $'
 * '$Revision: 1.4 $'
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

package edu.ucsb.nceas.morpho.framework;

import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.Morpho;
import javax.swing.table.AbstractTableModel;

/**
 * This service allows plugins to request that the query results be
 * refreshed when they have made a change that should affect the resultset.
 * All component plugins that make changes to data packages that should result
 * in the "My Data" result set being updated should utilize this
 * interface and call the refresh method after the change is completed.
 */
public interface QueryRefreshInterface
{


  /**
   * Denotes an instance of the OpenDataPackageCommand object
   */
  public static final int OPEN_DATAPACKAGE_COMMAND = 20;

  /**
   * Denotes an instance of the SearchCommand object
   */
  public static final int SEARCH_COMMAND           = 30;


  /**
   * This method is called to refresh the main query when a change is
   * made that should be propogated to the query result screens.
   */
  public void refresh();

  /**
   * This mehod will be called when Morpho switch to another profile. Old saved
   * queries will be removed from search menu and new saved queries will be
   * adde into search menu.
   * @param newMorpho new Morpho object after switch profile
   */
  public void updateSavedQueryMenuItems(Morpho newMorpho);

  /**
   * return an instance of a Command object, identified by one of the integer
   * constants defined above
   *
   * @param commandIdentifier   integer constant identifying the command
   *                            Options include:<ul>
   *                            <li>OPEN_DATAPACKAGE_COMMAND</li>
   *                            <li>SEARCH_COMMAND</li>
   *                            </ul>
   * @throws ommandNotFoundException if Command not found
   */
  public Command getCommandObject(int commandIdentifier)
                                                throws ClassNotFoundException;

  /**
   * performs a local query to get the documents owned by the user, as
   * identified by the current profile
   * @return AbstractTableModel containing results
   * (@see edu.ucsb.nceas.morpho.query.ResultSet)
   */
  public AbstractTableModel doOwnerQueryForCurrentUser();

}
