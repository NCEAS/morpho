/**
 *  '$RCSfile: XMLFactoryInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-04-05 21:58:20 $'
 * '$Revision: 1.3 $'
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

import edu.ucsb.nceas.morpho.util.DocumentNotFoundException;
import org.w3c.dom.Document;

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
		
		/**
		 *  method to return a DOM Document rather than a Reader
		 *  Since the real purppose is to provide the source for an XSLT transform
		 *  a DOM can passed instead of a Reader. This avoids the need to convert
		 *  a Reader to the DOM. Return of a 'null' Document is allowed as an indication
		 *  that one should use the 'openAsReader' method.
		 *
     *  @param id  a unique identifier used to determine what resource to return 
		 */
		public Document openAsDom(String id);
}

