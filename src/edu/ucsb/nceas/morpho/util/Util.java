/**
 *  '$RCSfile: Util.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-11-11 23:51:05 $'
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
package edu.ucsb.nceas.morpho.util;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * This class presents utility methods which will be repeatly used in morpho code.
 * @author tao
 *
 */
public class Util 
{
		/**
		 * Determine if the specified string is blank or not. If string is null, it will be false.
		 * @param input the specified string
		 * @return true or false if the the string is blank.
		 */
		public static  boolean isBlank(String input)
		{
			boolean isBlank = true;
			if (input != null)
			{
				if (!input.trim().equals(""))
				{
					isBlank = false;
				}
			}
			return isBlank;
		}
		
		/**
		 * Determine if the specified string is number or not. If String is null, it will return false.
		 * @param input the specified string
		 * @return true if it is number. otherwise false.
		 */
		public static boolean isNumber (String input)
		{
			boolean isNumber = true;
			if (input != null)
			{
				try
				{
					Double number = new Double(input);
				}
				catch(NumberFormatException e)
				{
					isNumber = false;
				}
			}
			else
			{
				isNumber = false;
			}
			return isNumber;
		}
		
		/**
		 * Gets an OrderedMap from given nodeList and genericName.
		 * Note: we add order ( /genericName[1]) in the returned map.
		 * @param nodeList
		 * @param genericName
		 * @return
		 */
		public static OrderedMap getOrderedMapFromNodeList(List nodeList, String genericName)
		{
			  OrderedMap existingValuesMap = new OrderedMap();
			  if(nodeList == null || genericName == null || nodeList.isEmpty())
			  {
				  return existingValuesMap;
			  }
		      Iterator listIt =nodeList.iterator();
		      Object nextObj = null;
		      Object nextTempObj = null;
		      String nextTempString = null;
		      int count = 1;

		      while (listIt.hasNext()) {
		        nextObj = listIt.next();
		        OrderedMap tempMap = XMLUtilities.getDOMTreeAsXPathMap( (Node)
		            nextObj);
		        Iterator tempIt = tempMap.keySet().iterator();
		        while (tempIt.hasNext()) {
		          nextTempObj = tempIt.next();
		          nextTempString = (String) nextTempObj;
		          if (nextTempString != null) {
		            existingValuesMap.put("/" + genericName +
		                "["
		                + count + "]" + nextTempString.substring(
		                		genericName.length() + 1,
		                nextTempString.length()),
		                tempMap.get(nextTempObj));
		          }
		        }
		        count++;
		      }
		      return existingValuesMap;
	}
		
	/**
	 * Delete the auto-saved file for given abstract data package.
	 * @param adp
	 */
	public static void deleteAutoSavedFile(AbstractDataPackage adp)
	{
	      if(adp != null)
	      {
	    	 //delete the incomplete file
	  	    String autoSavedID = adp.getAutoSavedD();
	  	    if(autoSavedID != null)
	  	    {
	  		    FileSystemDataStore store = new FileSystemDataStore(Morpho.thisStaticInstance);
	  	        store.deleteInCompleteFile(autoSavedID);
	  	    }
	      }
	}
		  

}
