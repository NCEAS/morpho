/**
 *  '$RCSfile: XMLFactoryInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-19 18:49:12 $'
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

package edu.ucsb.nceas.morpho.plugins;

import java.io.Reader;

import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;

/**
 *  This Interface enables calling classes to gain access to resources that are 
 *  identified by unique String identifiers.  Implementing classes need to be 
 *  able to map the identifiers to resources in order to return the correct 
 *  instances
 */
public interface XMLFactoryInterface
{
    /**
     *  method to return a Reader object, which will provide access to a 
     *  character-based resource. The resource to be returned is determined 
     *  based on the unique String identifier passed to this method
     *
     *  @param id  a unique identifier used to determine what resource to return 
     *
     *  @return a Reader for the character-based resource
     *
     *  @throws DocumentNotFoundException if id does not point to a document, or
     *          if requested document exists but cannot be accessed.
     */
    public Reader openAsReader(String id) throws DocumentNotFoundException;
}

