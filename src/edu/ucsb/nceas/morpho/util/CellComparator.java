/**
 *  '$RCSfile: CellComparator.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-02 23:03:38 $'
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

package edu.ucsb.nceas.morpho.util;
import java.util.*;

/**
 * This class to compare two vecotors which is two dimentions
 */

public class CellComparator implements Comparator
{
  protected int index;
  protected boolean order;
  
  /**
   * Constructor of cellComparator 
   * @param index the index in vectors need to compare
   * @param ascending the order which need to compare
   */
  public CellComparator(int index, boolean ascending)
  {
    this.index = index;
    this.order = ascending;
  }
  
  /**
   * The method to compare twso vectors
   * @param one, the first vector
   * @param two, the second vector
   */
  public int compare(Object one, Object two) 
  {
    if (one instanceof Vector &&
        two instanceof Vector)
    {
      Vector vOne = (Vector)one;
      Vector vTwo = (Vector)two;
      Object oOne = vOne.elementAt(index);
      Object oTwo = vTwo.elementAt(index);
      if (oOne instanceof Comparable &&
          oTwo instanceof Comparable)
      {
        Comparable cOne = (Comparable)oOne;
        Comparable cTwo = (Comparable)oTwo;
        if (order)
        {
          return cOne.compareTo(cTwo);
        }
        else
        {
          return cTwo.compareTo(cOne);
        }
      }
    }
    return 1;
  }
}

