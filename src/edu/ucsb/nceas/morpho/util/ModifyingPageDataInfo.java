/**
 *  '$RCSfile: ModifyingPageDataInfo.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-21 16:33:40 $'
 * '$Revision: 1.4 $'
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

import java.util.Vector;

/**
 * This class represents the object read from lib/xpath-wizard-map.xml file.
 * The information include three parts:
 * 1. the xpath list which is used to load the existing subtree into wizard page.
 * 2. the xpath which is used to call getPageData in wizard page.
 * 3. the document name to create a new Document object (subtree) with the new page data
 * @author tao
 *
 */
public class ModifyingPageDataInfo 
{
	private Vector loadExistingDataPath = new Vector();
	private String pathForgettingPageData = "";
	private String documentName = "";
	private String genericName = null;
	private String pathForCreatingOrderedMap = null;
	private String pathForSettingPageData = "";
	private String key = null;
	
	public String getPathForSettingPageData() {
		return pathForSettingPageData;
	}

	public void setPathForSettingPageData(String pathForSettingPageData) {
		this.pathForSettingPageData = pathForSettingPageData;
	}

	/**
	 * Gets the list which which is used to load the existing subtree into wizard page.
	 * @return
	 */
	public Vector getLoadExistingDataPath() 
	{
		return loadExistingDataPath;
	}
	
	/**
	 * Adds a path into a list which is used to load the existing subtree into wizard page.
	 * @param path
	 */
	public void addLoadExistingDataPath(String path) 
	{
		LoadDataPath newPath = new LoadDataPath(path);
		this.loadExistingDataPath.add(newPath);
	}
	
	/**
	 * Adds a path object into a list which is used to load the existing subtree into wizard page.
	 * @param path
	 */
	public void addLoadExistingDataPath(LoadDataPath newPath) 
	{
		this.loadExistingDataPath.add(newPath);
	}
	
	/**
	 * Remove a path object
	 * @param path
	 */
	public void removeLoadExistingDataPath(LoadDataPath path)
	{
		this.loadExistingDataPath.remove(path);
	}
	/**
	 * Gets the path for getting page data
	 * @return
	 */
	public String getPathForgettingPageData() 
	{
		return pathForgettingPageData;
	}
	
	/**
	 * Sets the path for getting page data
	 * @param pathForgettingPageData
	 */
	public void setPathForgettingPageData(String pathForgettingPageData) 
	{
		this.pathForgettingPageData = pathForgettingPageData;
	}
	
	/**
	 * Gets the document name for new data subtree
	 * @return
	 */
	public String getDocumentName() 
	{
		return documentName;
	}
	
	/**
	 * Sets the document name for new data subtree
	 * @param documentName
	 */
	public void setDocumentName(String documentName) 
	{
		this.documentName = documentName;
	}
	
	public String getGenericName() 
	{
		return genericName;
	}

	public void setGenericName(String genericName) 
	{
		this.genericName = genericName;
	}

	public String getPathForCreatingOrderedMap() 
	{
		return pathForCreatingOrderedMap;
	}

	public void setPathForCreatingOrderedMap(String pathForCreatingOrderedMap) 
	{
		this.pathForCreatingOrderedMap = pathForCreatingOrderedMap;
	}
	
	
	/**
	 * Clone an object
	 * @param info
	 * @return
	 */
	public static ModifyingPageDataInfo copy(ModifyingPageDataInfo info)
	{
		ModifyingPageDataInfo newInfo = new ModifyingPageDataInfo();
		if(info != null)
		{
			newInfo.setPathForgettingPageData(info.getPathForgettingPageData());
			newInfo.setDocumentName(info.getDocumentName());
			newInfo.setGenericName(info.getGenericName());
			newInfo.setPathForCreatingOrderedMap(info.getPathForCreatingOrderedMap());
			newInfo.setPathForSettingPageData(info.getPathForSettingPageData());
			newInfo.setKey(info.getKey());
			Vector dataPathList = info.getLoadExistingDataPath();
			if(dataPathList != null)
			{
				for(int i=0; i<dataPathList.size(); i++)
				{
					LoadDataPath dataPathObj = (LoadDataPath)dataPathList.elementAt(i);
					LoadDataPath newPathObj = LoadDataPath.copy(dataPathObj);
					newInfo.addLoadExistingDataPath(newPathObj);
				}
			}
		}
		return newInfo;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
