/**
 *     '$RCSfile: XMLErrorHandler.java,v $'
 *     Copyright: 1997-2002 Regents of the University of California,
 *                          University of New Mexico, and
 *                          Arizona State University
 *      Sponsors: National Center for Ecological Analysis and Synthesis and
 *                Partnership for Interdisciplinary Studies of Coastal Oceans,
 *                   University of California Santa Barbara
 *                Long-Term Ecological Research Network Office,
 *                   University of New Mexico
 *                Center for Environmental Studies, Arizona State University
 * Other funding: National Science Foundation (see README for details)
 *                The David and Lucile Packard Foundation
 *   For Details: http://knb.ecoinformatics.org/
 *
 *      '$Author: anderson $'
 *        '$Date: 2006-02-06 19:44:02 $'
 *    '$Revision: 1.1 $'
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ucsb.nceas.morpho.util;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Report XML parsing errors.
 */
public class XMLErrorHandler implements ErrorHandler
{

  /**
   * Method for handling errors during a parse.
   *
   * @param exception         The parsing error
   * @exception SAXException  Description of Exception
   */
  public void error(SAXParseException exception) throws SAXException
  {
    Log.debug(15, "ERROR while SAX parsing: " + exception.toString());
    throw exception;
  }

  /**
   * Method for handling warnings during a parse.
   *
   * @param exception         The parsing error
   * @exception SAXException  Description of Exception
   */
  public void warning(SAXParseException exception)
    throws SAXException
  {
      Log.debug(50, "WARNING while SAX parsing: " + exception.toString());
    throw new SAXException("WARNING: " + exception.getMessage());
  }

  /**
   * Method for handling fatal errors during a parse.
   *
   * @param exception         The parsing error
   * @exception SAXException  Description of Exception
   */
  public void fatalError(SAXParseException exception)
    throws SAXException
  {
    Log.debug(15, "FATAL ERROR while SAX parsing: " + exception.toString());
    throw new SAXException("WARNING: " + exception.getMessage());
  }


}
