/**
 *  '$RCSfile: History.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-21 03:20:13 $'
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

import java.util.Stack;


/**
 *  A history implementation to wrap the underlying data structure
 */
public class History
{
//  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *

    private Stack historyStack;
    
    /**
     *  constructor
     *
     */
    public History() 
    {
        historyStack = new Stack();
    }

    /**
     *  constructor
     *
     */
    public History(String firstEntry) {
        this();
        add(firstEntry);
    }


	/**
	 *  add the String identifier to the most-recent slot in history
	 *
     *  @param  the identifier to be added
	 */
	public void add(String item)
	{
        if (item!=null) historyStack.push(item);
	}

	/**
	 *  Deletes the String identifier that was most recently added to history
	 */
	public void deletePrevious()
	{
	    getPrevious();  //...but don't return it!
	}

	/**
	 *  Gets the String identifier that was most recently added to history,
	 *  <em>and actually removes it from history</em>
	 *
	 *  @return  the String identifier that was most recently added to history
	 */
	public String getPrevious()
	{
	    if (historyStack.empty()) return null;
	    return (String)(historyStack.pop());
	}

	/**
	 *  Looks at the String identifier that was most recently added to history,
     *  <em>without actually removing it from history</em>
	 *
	 *  @return  the String identifier that was most recently added to history
	 */
  	public String previewPrevious()
	{
	    if (historyStack.empty()) return null;
        return (String)(historyStack.peek());
	}
}
