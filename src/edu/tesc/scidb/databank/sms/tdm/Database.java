package edu.tesc.scidb.databank.sms.tdm;

import java.util.Collection;
import org.dom4j.Element;

/**
 *  Description of the Interface Redistribution and use in source and binary
 *  forms, with or without modification, are permitted provided that the
 *  following conditions are met: 1. Redistributions of source code must retain
 *  the above copyright notice, this list of conditions and the following
 *  disclaimer. 2. Redistributions in binary form must reproduce the above
 *  copyright notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution. 3.
 *  All advertising materials mentioning features or use of this software must
 *  display the following acknowledgement: This product includes software
 *  developed by the The Evergreen State College-- Scientific Laboritory and
 *  Canopy Research Group and its contributors. With Funding provived by the NSF
 *  Database Activities Program [BIR 9975510] 4. Neither the name of the College
 *  or NSF nor the names of its contributors may be used to endorse or promote
 *  products derived from this software without specific prior written
 *  permission.
 *
 *@author     Erik Ordway ordwayeATevergreen.edu
 *@created    September 30, 2003
 *@since      November 20, 2001
 */
public interface Database
{
	/**
	 *  Gets the name attribute of the Database object
	 *
	 *@return    The name value
	 *@since
	 */
	String getName();
	
	public final static String
		PROP_VIZMODULES = "vizModules",
		PROP_ACCESSFORMS = "accessForms";
	public void setProperty(String name, Object value);
	public Object getProperty(String name);



	/**
	 *  Sets the name attribute of the Database object
	 *
	 *@param  name  The new name value
	 *@since
	 */
	void setName(String name);


	/**
	 *  Gets the tables attribute of the Database object
	 *
	 *@return    The tables value
	 *@since
	 */
	Collection getTables();


	/**
	 *  Gets the table attribute of the Database object
	 *
	 *@param  name  Description of Parameter
	 *@return       The table value
	 *@since
	 */
	Table getTable(String name);


	/**
	 *  Adds a feature to the Table attribute of the Database object
	 *
	 *@param  table  The feature to be added to the Table attribute
	 *@since
	 */
	void addTable(Table table);


	/**
	 *  Gets the targetDBMS attribute of the Database object
	 *
	 *@return    The targetDBMS value
	 *@since
	 */
	String getTargetDBMS();


	/**
	 *  Sets the targetDBMS attribute of the Database object
	 *
	 *@since
	 */
	void setTargetDBMS();


	/**
	 *  Gets the relationships attribute of the Database object
	 *
	 *@return    The relationships value
	 *@since
	 */
	Collection getRelationships();


	/**
	 *  Adds a feature to the Relationship attribute of the Database object
	 *
	 *@param  foo  The feature to be added to the Relationship attribute
	 *@since
	 */
	void addRelationship(Relationship foo);


	/**
	 *  Gets the tables attribute of the Database object
	 *
	 *@return    The tables value
	 *@since
	 */
	Collection getInserts();


	/**
	 *  Gets the table attribute of the Database object
	 *
	 *@param  insert  The feature to be added to the Insert attribute
	 *@since
	 */
	//Table getInsert(String name);


	/**
	 *  Adds a feature to the Table attribute of the Database object
	 *
	 *@param  insert  The feature to be added to the Table attribute
	 *@since
	 */
	void addInsert(Insert insert);


	/**
	 *  Description of the Method
	 *
	 *@param  parent  Description of the Parameter
	 *@return         Description of the Return Value
	 */
	public String getAsEML();
	public Element getAsEML(Element parent);

}

