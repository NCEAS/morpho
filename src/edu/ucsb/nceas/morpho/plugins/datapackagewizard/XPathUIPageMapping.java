package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import java.util.Vector;

/**
 * This class represents a unit in mapping property file:
 * 
 * <mapping>
 *   <root>/eml:eml/dataset/</root>
 *    <xpath>title</xpath>
 *    <xpath>abract/para</xpath>
 *    <loadDataFromRootPath>false</loadDataFromRootPath> 
 *    <wizardPageClass>General</wizardPageClass>
 *  </mapping>
 *  If loadDataFromRootPath is false, we need to load data from /root/xpath1 /root/xpath2. 
 *  General is this kind of page.
 */
public class XPathUIPageMapping
{
	private String root = null;
	private Vector xpathList = new Vector();
	private boolean loadDataFromRootPath = true;
	private String wizardPageClassName = null;
	
	/**
	 * Constructor
	 */
	public XPathUIPageMapping()
	{
		
	}
	
	/**
	 * Gets the root of xpath
	 * @return
	 */
	public String getRoot() 
	{
		return root;
	}
	
	/**
	 * Sets the root of xpath
	 * @param root
	 */
	public void setRoot(String root) 
	{
		this.root = root;
	}
	
	/**
	 * Gets the list of relative xpath
	 * @return
	 */
	public Vector getXpath() 
	{
		return xpathList;
	}
	
	/**
	 * Adds relative xpath to list
	 * @param xpath
	 */
	public void addXpath(String xpath) 
	{
		this.xpathList.add(xpath);
	}
	
	/**
	 * Gets the class name mapping to the xpath
	 * @return
	 */
	public String getWizardPageClassName() 
	{
		return wizardPageClassName;
	}
	
	/**
	 * Sets the class name mapping to the xpath
	 * @param wizardPageClassName
	 */
	public void setWizardPageClassName(String wizardPageClassName) 
	{
		this.wizardPageClassName = wizardPageClassName;
	}
	
	
	/**
	 * Sets if the UI page need to load data from the given root path.
	 * @param loadDataFromRootPath
	 */
	public void setLoadDataFromRootPath(boolean loadDataFromRootPath)
	{
		this.loadDataFromRootPath = loadDataFromRootPath;
	}
	
	/**
	 * Determine if the UI Page need load data from the given root path
	 * @return true if we need load page from root path
	 */
	public boolean isLoadDataFromRootPath()
	{
		return this.loadDataFromRootPath;
	}
}
