package edu.ucsb.nceas.morpho.query;

import java.util.Vector;

/**
 * <p>Title: </p>
 * <p>Description: This is for vector object which is synchronized </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SynchronizeVector
{
  private Vector contentVector;

  /**
   * Constructor
   */
  public SynchronizeVector()
  {
    contentVector = new Vector();
  }

  /**
   * The synchronized method to get Vector and reset contentVector to empty.
   * @return Vector
   */
  public synchronized Vector getVector()
  {
    Vector local = new Vector();
    //copy the cotent vector to local
    for (int i= 0; i<contentVector.size(); i++)
    {
      Vector row = (Vector)contentVector.elementAt(i);
      local.addElement(row);
    }//for
    // reset content vector
    contentVector = null;
    contentVector = new Vector();
    // return the local vector which has the original copy of contentVector
    return local;
  }


  /**
   *  add a new vector to content.
   * @param vector Vector
   */
  public synchronized void addVector(Vector vector)
  {
    contentVector.addElement(vector);
  }
}
