/**
 *  '$RCSfile: PathElementInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-22 16:46:09 $'
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

package edu.ucsb.nceas.morpho.plugins.metadisplay;

import edu.ucsb.nceas.morpho.util.Command;

/**
 *  This Interface is implemented by objects that may be added to a 
 *  <code>edu.ucsb.nceas.morpho.plugins.metadisplay.Path</code> object
 */
public interface PathElementInterface
{
    /**
     *  sets the text display name for this item.
     *
     *  @param name the display name to be used
     */
    public void setDisplayName(String name);

    /**
     *  gets the text display name for this item.
     *
     *  @return name the display name for this item
     */
    public String getDisplayName();

    /**
     *  sets the <code>edu.ucsb.nceas.morpho.util.Command</code> for this item.
     *
     *  @param name the Command object to be used
     */
    public void setCommand(Command cmd);

    /**
     *  gets the <code>edu.ucsb.nceas.morpho.util.Command</code> for this item.
     *
     *  @return name the Command object for this item
     */
    public Command getCommand();

    /**
     *  sets the previous <code>PathElementInterface</code> in the path
     *
     *  @param element the <code>PathElementInterface</code> to be used
     */
    public void setPreviousElement(PathElementInterface element);

    /**
     *  gets the previous <code>PathElementInterface</code> from the path
     *
     *  @return the previous <code>PathElementInterface</code>
     */
    public PathElementInterface getPreviousElement();

    /**
     *  gets the <code>edu.ucsb.nceas.morpho.plugins.metadisplay.Path</code> 
     *  from this item to the root item.  Includes this item as the last Path 
     *  element.
     *
     *  @return <code>edu.ucsb.nceas.morpho.plugins.metadisplay.Path</code> from 
     *                                              this item to the root item
     */
    public Path getPathToRoot();
}

