/**
 *  '$RCSfile: CellComparator.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-05 22:01:47 $'
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

package edu.ucsb.nceas.morpho.query;

import java.util.Comparator;
import java.util.Vector;
import javax.swing.ImageIcon;

/**
 * This class to compare two vecotors which is two dimentions
 */

public class CellComparator implements Comparator
{
  protected int indexOfRow;
  protected boolean ascending;
  
  /**
   * Constructor of cellComparator 
   * @param myIndex the row index in vectors need to compare
   * @param myAscending the order which need to compare
   */
  public CellComparator(int myIndexOfRow, boolean myAscending)
  {
    indexOfRow = myIndexOfRow;
    ascending = myAscending;
  }
  
  /**
   * The method to compare twso vectors
   * @param vector1, the first vector
   * @param vector2, the second vector
   */
  public int compare(Object object1, Object object2) 
  {
      // Return value
      int returnValue = 1;
      Vector vector1 = (Vector)object1;
      Vector vector2 = (Vector)object2;
      // Get the objects need be compared from vectors
      Object obj1 = vector1.elementAt(indexOfRow);
      Object obj2 = vector2.elementAt(indexOfRow);
      // for element is String
      if (obj1 instanceof String && obj2 instanceof String )
      {
        // Casting
        String str1 = (String)obj1;
        String str2 = (String)obj2;
        if (ascending)
        {
          // for ascending
          returnValue = str1.compareTo(str2);
        }//if
        else
        {
          returnValue = str2.compareTo(str1);
        }//else
      }//if
      // for element is image icon
      else if (obj1 instanceof ImageIcon && obj2 instanceof ImageIcon)
      {
        // casting
        ImageIcon icon1 = (ImageIcon) obj1;
        ImageIcon icon2 = (ImageIcon) obj2;
        // get description for imageicon
        String description1 = icon1.getDescription();
        String description2 = icon2.getDescription();
        // compare description
        if (ascending)
        {
          // for ascending
          returnValue = description1.compareTo(description2);
        }//if
        else
        {
          returnValue = description2.compareTo(description1);
        }//else
      }
      // for element is Boolean
      else if (obj1 instanceof Boolean && obj2 instanceof Boolean)
      {
        // castiong
        Boolean boolean1 = (Boolean) obj1;
        Boolean boolean2 = (Boolean) obj2;
        returnValue = booleanCompare(boolean1, boolean2, ascending);
      }
    
    return returnValue;
  }//Compare
  
  /**
   * A method to compare two boolean object
   */
   private int booleanCompare(Boolean bool1, Boolean bool2, boolean ascending)
   {
     // return value
     int returnValue = 1;
     boolean boolean1 = bool1.booleanValue();
     boolean boolean2 = bool2.booleanValue();
     // if boolean 1 and boolean are tures or falses. They are equal
     if (!(boolean1 ^ boolean2))
     {
       returnValue = 0;
     }//if
     else if (ascending)
     {
       // boolean1 = true and boolean2 = false;
       if (boolean1)
       {
         returnValue =-1;
       }
       else// boolean1 = false and boolean2 = true
       {
         returnValue = 1;
       }
     }//else if
     else if (!ascending)
     {
       if (boolean1)
       {
         returnValue = 1;
       }
       else
       {
         returnValue = -1;
       }
     }//else if
     
     return returnValue;
     
       
   }//booleanCompare
}

