/**
 *  '$RCSfile: ModifyingPageDataInfo.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-14 20:46:09 $'
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
		this.loadExistingDataPath.add(path);
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
	

}
