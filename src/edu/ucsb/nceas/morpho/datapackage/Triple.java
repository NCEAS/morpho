/**
 *  '$RCSfile: Triple.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-07 17:36:28 $'
 * '$Revision: 1.6 $'
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
  
  /**
   * create an initialized, blank Triple object
   */
  public Triple()
  {
    this.subject = new String();
    this.relationship = new String();
    this.object = new String();
  }
  
  /**
   * create a new triple with the specified subject relationship and object
   */
  public Triple(String subject, String relationship, String object) 
  {
    this.subject = subject;
    this.relationship = relationship;
    this.object = object;
  }
  
  /**
   * Copy constructor.  creates a new triple object that is a copy of the given
   * triple
   */
  public Triple(Triple t)
  {
    this.subject = t.getSubject();
    this.relationship = t.getRelationship();
    this.object = t.getObject();
  }

  /**
   * Set the subject of this triple
   */
	public void setSubject(String subject)
	{
	  this.subject = subject;
	}
  
  /**
   * Set the relationship of this triple
   */
	public void setRelationship(String relationship)
	{
	  this.relationship = relationship;
	}

  /**
   * Set the object of this triple
   */
	public void setObject(String object)
	{
    this.object = object;
	}
  
  /**
   * get the subject of this triple
   */
	public String getSubject()
	{
		return subject;
	}

  /**
   * get the relationship of this triple
   */
	public String getRelationship()
	{
		return relationship;
	}
  
  /**
   * get the object of this triple
   */
	public String getObject()
	{
		return object;
	}
	
  /**
   * return this triple as a vector of strings
   */
  public Vector getTriple()
  {
    Vector triple = new Vector();
    triple.addElement(this.subject);
    triple.addElement(this.relationship);
    triple.addElement(this.object);
    return triple;
  }
  
  /**
   * return this triple in xml format.  The xml looks like: 
   * <pre>
   * &lt;triple&gt;&lt;subject&gt;some content 1&lt;/subject&gt;
   * &lt;relationship&gt;some content 2&lt;/relationship&gt;
   * &lt;object&gt;some content 3&lt;/object&gt;&lt;/triple&gt;
   * </pre>
   */
   public String toXML()
   {
     StringBuffer sb = new StringBuffer();
     sb.append("<triple>");
     sb.append("<subject>").append(this.subject).append("</subject>");
     sb.append("<relationship>").append(this.relationship).append("</relationship>");
     sb.append("<object>").append(this.object).append("</object>");
     sb.append("</triple>");
     return sb.toString();
   }
   
   /**
   * return this triple in xml format with easy to read formatting.  
   * The xml looks like: 
   * <pre>
   * &lt;triple&gt;
   *   &lt;subject&gt;some content 1&lt;/subject&gt;
   *   &lt;relationship&gt;some content 2&lt;/relationship&gt;
   *   &lt;object&gt;some content 3&lt;/object&gt;
   * &lt;/triple&gt;
   * </pre>
   */
   public String toFormatedXML()
   {
     StringBuffer sb = new StringBuffer();
     sb.append("<triple>\n");
     sb.append("  <subject>").append(this.subject).append("</subject>\n");
     sb.append("  <relationship>").append(this.relationship);
     sb.append("</relationship>\n");
     sb.append("  <object>").append(this.object).append("</object>\n");
     sb.append("</triple>\n");
     return sb.toString();
   }

  /**
   * return this triple as a string
   */
  public String toString() 
  {
    return "[" + this.subject + "," + 
                 this.relationship + "," + 
                 this.object + "]";
  }
}
