/**
 *  '$RCSfile: Path.java,v $'
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

import java.util.Vector;


/**
 *  A path implementation to wrap the underlying data structure
 */
public class Path
{
//  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *

    private Vector pathVector;
    
    /**
     *  constructor
     *
     */
    public Path() 
    {
        pathVector = new Vector();
    }

    /**
     *  constructor
     *
     */
    public Path(PathElementInterface firstEntry) {
        this();
        append(firstEntry);
    }


	/**
	 *  add the PathElement to the end of the path
	 *
     *  @param  element the PathElementInterface to be added
	 */
	public void append(PathElementInterface element)
	{
        if (element!=null) pathVector.add(element);
	}

	/**
	 *  Deletes the PathElement that is currently at the end of the path
	 */
	public void removeLast()
	{
	    getLast();  //...but don't return it!
	}

	/**
	 *  Gets the PathElement that is currently at the end of the path
	 *
	 *  @return  the PathElementInterface that was most recently added to path
	 */
	public PathElementInterface getLast()
	{
	    if (pathVector.isEmpty()) return null;
	    return (PathElementInterface)(pathVector.lastElement());
	}

	/**
	 *  Clears all PathElements, resulting in an empty path,
	 */
  	public void clear()
	{
	    if (pathVector.isEmpty()) return;
        pathVector.clear();
	}
    
	/**
	 *  returns a String representation of all the elements in the Path,
	 *
	 *  @return  the String representation of all the elements in the Path
	 */
	public String toString()
	{
	    if (pathVector.isEmpty()) return "";

	    return pathVector.toString();
	}
}