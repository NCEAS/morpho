/**
 *  '$RCSfile: XMLElement.java,v $'
 *    Purpose: A class that represents an XMLElement for use in the package
 *             wizard.
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-19 21:10:34 $'
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

package edu.ucsb.nceas.morpho.datapackage.wizard;

import edu.ucsb.nceas.morpho.util.Log;

import java.io.*;
import java.util.*;

/**
 * object that represents an Element and any content inside of it.
 */
public class XMLElement
{
  String name = new String();
  Hashtable attributes = new Hashtable();
  Vector content = new Vector(); 

  public XMLElement()
  {
  }

  /**
   * copy constructor
   */
  public XMLElement(XMLElement xmle)
  {
    this.attributes = xmle.attributes;
    this.content = xmle.content;
    this.name = xmle.name;
  }
}
