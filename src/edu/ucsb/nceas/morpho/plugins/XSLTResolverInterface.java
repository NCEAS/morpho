/**
 *  '$RCSfile: XSLTResolverInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-12 03:03:32 $'
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
 *  This interface enables access to XSLT stylesheets that are identified by 
 *  unique String DOCTYPE identifiers.  
 */
public interface XSLTResolverInterface
{
    /**
     *  method to return a Reader object, which will provide access to a 
     *  character-based XSLT stylesheet. The stylesheet to be returned is 
     *  determined based on the unique DOCID String identifier passed to this 
     *  method.  If a stylesheet corresponding to the DOCID cannot be found, 
     *  a default or generic stylesheet may be returned.  
     *  If no suitable stylesheet can be returned, a DocumentNotFoundException 
     *  is thrown
     *
     *  @param docID  a unique DOCID used to determine what stylesheet to return 
     *
     *  @return       a Reader for the character-based XSLT stylesheet. If a 
     *                stylesheet corresponding to the DOCID cannot be found, 
     *                a default or generic stylesheet may be returned. 
     *
     *  @throws DocumentNotFoundException if no suitable stylesheet is available
     */
    public Reader getXSLTStylesheetReader(String docID) 
                                              throws DocumentNotFoundException;
}

