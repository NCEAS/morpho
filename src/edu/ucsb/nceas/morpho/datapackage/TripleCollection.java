/**
 *  '$RCSfile: TripleCollection.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-04 15:23:36 $'
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
import java.io.*;

/**
 * This class implements a collection of triples which make up all of the
 * relationships in a package.
 */
public class TripleCollection
{
  private Vector triples = new Vector();
  /**
   * Default Constructor
   */
  public TripleCollection()
  {
    
  }
  
  /**
   * read an xml file, build the collection from any triples in the xml file.
   */
  public TripleCollection(Reader xml)
  {
    
  }
  
  /**
   * create a collection of triples from a vector of Triple objects.
   */
  public TripleCollection(Vector triples)
  {
    this.triples = new Vector(triples);
  }
  
  /**
   * Add a single triple to this collection
   */
  public void addTriple(Triple triple)
  {
    this.triples.addElement(triple);
  }
  
  /**
   * removes the specified triple from the collection and returns it.  If the
   * triple was not in the collection null is returned.
   */ 
  public Triple removeTriple(Triple triple)
  {
    boolean removed = triples.remove(triple);
    if(removed)
    {
      return triple;
    }
    else
    {
      return null;
    }
  }
  
  /**
   * returns a vector of triples with the given subject
   */
  public Vector getCollectionBySubject(String subject)
  {
    Vector trips = new Vector();
    for(int i=0; i<triples.size(); i++)
    {
      Triple trip = new Triple((Triple)triples.elementAt(i));
      if(trip.getSubject().equals(subject))
      {
        trips.addElement(trip);
      }
    }
    return trips;
  }
  
  /**
   * returns a vector of triples with the given relationship
   */
  public Vector getCollectionByRelationship(String relationship)
  {
    Vector trips = new Vector();
    for(int i=0; i<triples.size(); i++)
    {
      Triple trip = new Triple((Triple)triples.elementAt(i));
      if(trip.getSubject().equals(relationship))
      {
        trips.addElement(trip);
      }
    }
    return trips;
  }
  
  /**
   * returns a vector of triples with the given object
   */
  public Vector getCollectionByObject(String object)
  {
    Vector trips = new Vector();
    for(int i=0; i<triples.size(); i++)
    {
      Triple trip = new Triple((Triple)triples.elementAt(i));
      if(trip.getSubject().equals(object))
      {
        trips.addElement(trip);
      }
    }
    return trips;
  }
  
  /**
   * return this collection as a string
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("[");
    for(int i=0; i<triples.size(); i++)
    {
      Triple t = new Triple((Triple)triples.elementAt(i));
      sb.append(t.toString());
      if(i != triples.size()-1)
      {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }
}
