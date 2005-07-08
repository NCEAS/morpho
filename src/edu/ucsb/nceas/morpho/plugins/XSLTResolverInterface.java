/**
 *  '$RCSfile: XSLTResolverInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2005-07-08 18:58:21 $'
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

package edu.ucsb.nceas.morpho.plugins;

import java.io.Reader;

import edu.ucsb.nceas.morpho.util.DocumentNotFoundException;

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

    /**
     *  method to return a String, which will contain the name of the dir
     *  which conatins the XSLT stylesheets. The dir to be returned is
     *  determined based on the unique DOCID String identifier passed to this
     *  method.  If a stylesheet corresponding to the DOCID cannot be found,
     *  a default or generic stylesheet may be returned.
     *
     *  @param identifier - unique identifier used to determine the stylesheet
     *                to return (e.g. DOCTYPE for DTD-defined XML, or
     *                schemaLocation or rootnode namespace for XSD-defined XML)
     *
     *  @return       a String, which will contain the name of the dir
     *                which conatins the XSLT stylesheets. If a
     *                stylesheet corresponding to the DOCID cannot be found,
     *                a default or generic stylesheet may be returned.
     *
     */
    public String getXSLTStylesheetLocation(String identifier);

    /**
    *  method to return a String, which will contain the name of the xml file
    *  which conatins the structure of schema that can be displayed by the
    *  tree editor. If a xml file corresponding to the DOCID cannot be found,
    *  null is returned.
    *
    *  @param identifier - unique identifier used to determine the stylesheet
    *                to return (e.g. DOCTYPE for DTD-defined XML, or
    *                schemaLocation or rootnode namespace for XSD-defined XML)
    *
    *  @return       a String, which will contain the name of the xml file
    *                which conatins the structure of schema that can be
    *                displayed by the tree editor. If a xml file corresponding
    *                to the DOCID cannot be found, null is returned.
    *
    */
    public String getTreeEditorXMLLocation(String identifier);
}

