/**
 *  '$RCSfile: Triple.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-03 18:13:34 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import java.util.*;

/**
 * This class implements one triple which defines one relationship inside
 * of a package.  This class will probably be used most in a collection of
 * triples that represents all of the relationships in package
 */
public class Triple
{
  private String subject;
  private String relationship;
  private String object;
  
  public Triple(String sub, String rel, String obj) 
  {
    this.subject = sub;
    this.relationship = rel;
    this.object = obj;
  }
  
  public Triple(String sub) 
  {
    this.subject = sub;
    this.relationship = null;
    this.object = null;
  }
    
	public String getObject()
	{
		return object;
	}

	public void setObject(String object)
	{
    this.object = object;
	}
	
	public String getRelationship()
	{
		return relationship;
	}

	public void setRelationship(String relationship)
	{
	  this.relationship = relationship;
	}
	public String getSubject()
	{
		return subject;
	}

	public void setSubject(String subject)
	{
	  this.subject = subject;
	}
  
  public Vector getTriple()
  {
    Vector triple = new Vector();
    triple.addElement(this.subject);
    triple.addElement(this.relationship);
    triple.addElement(this.object);
    return triple;
  }

  public String toString() 
  {
    return "[" + this.subject + "," + 
                 this.relationship + "," + 
                 this.object + "]";
  }
}
