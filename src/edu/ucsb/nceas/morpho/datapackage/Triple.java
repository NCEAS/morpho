/**
 *        Name: Triple.java
 *     Purpose: A Class for storing triple info (formerly Relation.java)
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: Triple.java,v 1.1 2001-05-03 18:12:24 berkley Exp $'
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
