/**
 *  '$RCSfile: MetaDisplayFactoryInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-12-12 18:14:34 $'
 * '$Revision: 1.2 $'
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

package edu.ucsb.nceas.morpho.plugins;


/**
 *  This interface provides methods for initializing, displaying, updating and 
 *  registering as a listener for a <code>Component</code> that styles XML and 
 *  displays the results 
 */
public interface MetaDisplayFactoryInterface 
{
    
    /**
     *  Return a new instance of an object that implements the 
     *  <code>MetaDisplayInterface</code>.  Implementing code should assign this 
     *  instance an integer identifier for internal use - 
     *  @see getMetaDisplay(int displayNum)
     *
     *  @return     new instance of an object that implements the 
     *              <code>MetaDisplayInterface</code>
     */
    public MetaDisplayInterface getInstance();
    
    /**
     *  Return a reference to an existing object that implements the 
     *  <code>MetaDisplayInterface</code>. The object is identified by the int
     *  index assigned to it in the getInstance() method at the time of creation
     *
     *  @param displayNum   int index assigned to the object in the 
     *                      getInstance() method at the time of creation
     *
     *  @return             reference to an existing object that implements the 
     *                      <code>MetaDisplayInterface</code> identified by 
     *                      displayNum.  Returns NULL if displayNum out of range
     */
    public MetaDisplayInterface getMetaDisplay(int displayNum);
    
}

