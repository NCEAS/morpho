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
import java.util.Vector;

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
	    private static final String LEFTBRACKET = "[";
	    private static final String RIGHTBRACKET = "]";
	    private static final String SLASH = "/";
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
     * This is the method will extract sub-map from original map. (originalMap will be removed those elements too).
     * For example if the original map looks like:
     * /attributeList/attribute[1]/@id	 = 	1258682693040
     * /attributeList/attribute[1]/attributeName[1]	 = 	1
     * /attributeList/attribute[1]/attributeDefinition[1]	 = 	1
     * /attributeList/attribute[1]/measurementScale[1]/dateTime[1]/formatString[1]	 = 	1
     * /attributeList/attribute[2]/@id	 = 	1258682693041
     * /attributeList/attribute[2]/attributeName[1]	 = 	2
     * /attributeList/attribute[2]/attributeDefinition[1]	 = 	2
     * /attributeList/attribute[2]/measurementScale[1]/dateTime[1]/formatString[1]	 = 	2
     * and path like /attributeList/attribute.
     * The result will be:
     * /@id	 = 	1258682693040
     * /attributeName[1]	 = 	1
     * /attributeDefinition[1]	 = 	1
     * /measurementScale[1]/dateTime[1]/formatString[1]	 = 	1
     * @param originalMap
     * @param path
     * @return
     */
    public static OrderedMap getFirstPartialMapForPath(OrderedMap originalMap, String path)
    {
    	OrderedMap newMap = new OrderedMap();   	
    	if(originalMap != null && path != null)
    	{
    		String newPath = path;
    		String newKey = null;
    		Vector deleteKey = new Vector();
    		String count = null;
    		boolean firstTime = true;
    		Iterator it = originalMap.keySet().iterator();
    		boolean hasPredictNumber = true;
    		while(it.hasNext())
    		{
    			String key = (String)it.next();
    			if(key == null)
    			{
    				continue;
    			}
    			Log.debug(35, "The orginal key in map is "+key+" in Util.getFirstPartialMapFroPath");
    			int index = key.indexOf(newPath);
    			Log.debug(35, "The index of "+newPath+" in key "+key+" is "+index);
    			if(firstTime && index != -1)
    			{
    				//since path is /attributeList/attribute, we need to figure out the real path
    				// is /attributeList/attribute[1]. So we need to know the first "/" after path
                    int indexOfSlash = key.indexOf(SLASH, index+newPath.length()-1);
                    //now we can set the newPath to real path
                    if(indexOfSlash != -1)
                    {
                      newPath = key.substring(0, indexOfSlash);
                      newKey = key.substring(indexOfSlash);
                    }
                    else
                    {
                    	//we couldn't find the slash after, so put whole key as new path.
                    	newPath = key;
                    	//newKey will be set to origninal key too.
                        newKey = key;
                    }
                    String value = (String)originalMap.get(key);
                    if(newKey != null && value != null)
                    {
                    	Log.debug(35, "Put the key "+newKey +" value "+value+" into the new map");
                    	newMap.put(newKey, value);
                    }                   
                     deleteKey.add(key);
    				 firstTime = false;
    			}
    			else if(index != -1)
    			{
    				 newKey = key.substring(index+newPath.length());
    				 String value = (String)originalMap.get(key);
                     if(newKey != null && value != null)
                     {
                     	Log.debug(35, "Put the key "+newKey +" value "+value+" into the new map");
                     	newMap.put(newKey, value);
                     }                   
                    deleteKey.add(key);
    			}
    		}
    		//remove the deleted key
        	for(int j=0; j<deleteKey.size(); j++)
        	{
        		String delete = (String)deleteKey.elementAt(j);
        		originalMap.remove(delete);
        	}
        	Log.debug(35, "The original map in Util.getFirstPartialMapForPath now is "+originalMap.toString());
    	}
    	Log.debug(35, "The return map in Util.getFirstPartialMapForPath now is "+newMap.toString());
    	return newMap;
    }
    
    /*
     * Get a count string in an predict. If we couldn't find one, empty string will be returned.
     * [4] will be return 4.
     */
    private static String getCountStringofPedict(String predictStr)
    {
    	String count = "";
    	if(predictStr != null)
    	{
    		boolean open = false;
    		boolean close = false;
    		for(int i=0; i<predictStr.length(); i++)
    		{
    			String str = predictStr.substring(i, i+1);
    			if(open && !close)
    			{
    			   count = count+str;
    			}
    			if(str.equals(LEFTBRACKET))
    			{
    				open = true;
    			}
    			else if(str.equals(RIGHTBRACKET))
    			{
    				close = true;
    			} 			
    		}
    		//if no close, we set it ""
    		if(!close)
    		{
    			Log.debug(35, "There is no close bracket, we set the count to empty");
    			count="";
    		}
    	}
    	Log.debug(30, "The count string in string "+predictStr+" is "+count+" in Util.getCountStringofPedict");
    	return count;
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
	  	        adp.setAutoSavedID(null);
	  	    }
	      }
	}
		  

}
