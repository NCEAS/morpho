/**
 *  '$RCSfile: ObjectCache.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2005-02-22 23:21:51 $'
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

package edu.ucsb.nceas.morpho.util;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.lang.ref.WeakReference;
import edu.ucsb.nceas.morpho.util.Log;


/**
 * Object cache is a caching utility which stores objects as SortReference objects.
 * Thus, garbage collection can clean up out-of-date objects, while objects can still be
 * referenced until removed from memory
 */

public class ObjectCache
{
  /**
    * hash table which contains doctype information about each of
    * the locally stored XML documents;
    * each Object is assumed to have an id by which it is referenced
    */
  private Hashtable object_collection;

  /**
   * constructor
   */
   public ObjectCache() {
     object_collection = new Hashtable();
   }

  public void putObject(String docid, Object obj) {
    if (docid!=null) {
      if (obj!= null) {
        object_collection.put(docid, new WeakReference(obj));
      } else {
        Log.debug(20,"obj in ObjectCache.putObject is null!");
      }
    } else {
      Log.debug(20,"docid in ObjectCache.putObject is null!");
    }
  }

  public boolean isInObjectCollection(String docid) {
    boolean res = false;
    if (object_collection.containsKey(docid)) {
      WeakReference wr = (WeakReference)object_collection.get(docid);
      if (wr.get()!=null) {
        res = true;
      }
      else {
        // has been garbage collected; thus remove
        object_collection.remove(docid);
      }
    }
    return res;
  }

  /**
   *  get an object in the cache;
   *  returns null if object is not in the cache or has been garbage collected
   */
  public Object getObject(String docid) {
    Object res = null;
    if (isInObjectCollection(docid)) {
      WeakReference wr = (WeakReference)object_collection.get(docid);
      res = wr.get();
    } else {
      addNewObjectToCache(docid);
    }
    return res;
  }

  /**
   *  get the current size of the cache
   */
  public int getSize() {
    cleanup();
    return object_collection.size();
  }

  /**
   *  get enumeration of current docids (keys)
   */
  public Enumeration getKeys() {
    cleanup();
    return object_collection.keys();
  }

  /**
   *  create a new object and add it to the cache
   *  Override this to put a new object in the cache if it is currently missing
   */
  protected void addNewObjectToCache(String docid) {
    // do nothing here
  }

  /**
   *  force removal of null WeakRef objects
   */
  private void cleanup() {
    Vector storageVec = new Vector();
    Enumeration enumeration = object_collection.keys();
    // put in a Vector to avoid messing with enumeration while iterating over it
    while (enumeration.hasMoreElements()) {
      storageVec.addElement(enumeration.nextElement());
    }
    for (int i = 0;i<storageVec.size();i++) {
      String docid = (String)storageVec.elementAt(i);
      if (isInObjectCollection(docid)) {
        // don't do anything
      } // if it is not in collection, 'isInObjectCollection' will remove it from hashtable
    }
  }
}
