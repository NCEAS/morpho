package edu.ucsb.nceas.morpho.util;

import java.util.Vector;

/**
 * This class represents a unit in mapping property file:
 * 
 * <mapping>
 *   <root>/eml:eml/dataset/</root>
 *    <xpath>title</xpath>
 *    <xpath>abract/para</xpath>
 *    <infoForModifyingData>
 *            <!-- xpath for loading existing data into page-->
 *            <loadExistDataPath></loadExistDataPath>
 *            <!-- xpath to get page data -->
 *            <xpathForGettingPageData></xpathForGettingPageData>
 *            <!-- the document name which contains the new data-->
 *            <newDataDocumentName></newDataDocumentName>
 *    </infoForModifyingData>
 *    <infoForModifyingData>
 *           <loadExistDataPath></loadExistDataPath>
 *            <newDataDocumentName></newDataDocumentName>
 *            <xpathForGettingPageData></xpathFo
 *    <wizardPageClass>General</wizardPageClass>
 *  </mapping>
 *  If loadDataFromRootPath is false, we need to load data from /root/xpath1 /root/xpath2. 
 *  General is this kind of page.
 */
public class XPathUIPageMapping
{
	private String root = null;
	private Vector xpathList = new Vector();
	private Vector modifyingPageDataInfo = new Vector();
	private String wizardPageClassName = null;
	//private String genericName = null;
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
	 * Gets the list of modifying page data info
	 * @return
	 */
	public Vector getModifyingPageDataInfoList()
	{
		return this.modifyingPageDataInfo;
	}
	
	/**
	 * Adds a ModifyingPageDataInfo object into the list
	 * @param info
	 */
	public void addModifyingPageDataInfo(ModifyingPageDataInfo info)
	{
		this.modifyingPageDataInfo.add(info);
	}

	

}
