package edu.tesc.scidb.databank.sms.tdm;

import edu.tesc.scidb.databank.sms.PropertyObject;
import edu.tesc.scidb.databank.sms.TDMConstraints;
import org.dom4j.Element;

/**
 *  Title: Description: uses private Map props; to store at least the following
 *  properties TYPE-->uses the default values for datatypes in TCConvTypes
 *  ORDER-->the order of a column in a Table this may belong in Table
 *  DESCRIPTION-->a string describing the use of the Column LOWERRANGE--> the
 *  valid lower range for the field. The default is that of the DataType.
 *  UPPERRANGE--> the valid upper range for the field. The default is that of
 *  the DataType. Copyright: Copyright (c) 2000 Company: Redistribution and use
 *  in source and binary forms, with or without modification, are permitted
 *  provided that the following conditions are met: 1. Redistributions of source
 *  code must retain the above copyright notice, this list of conditions and the
 *  following disclaimer. 2. Redistributions in binary form must reproduce the
 *  above copyright notice, this list of conditions and the following disclaimer
 *  in the documentation and/or other materials provided with the distribution.
 *  3. All advertising materials mentioning features or use of this software
 *  must display the following acknowledgement: This product includes software
 *  developed by the The Evergreen State College-- Scientific Laboritory and
 *  Canopy Research Group and its contributors. With Funding provived by the NSF
 *  Database Activities Program [BIR 9975510] 4. Neither the name of the College
 *  or NSF nor the names of its contributors may be used to endorse or promote
 *  products derived from this software without specific prior written
 *  permission.
 *
 *@author
 *@created    September 30, 2003
 *@since      November 20, 2001
 *@version    1.0
 */
public interface Col extends PropertyObject, TDMConstraints
{
	/**
	 *  Description of the Field
	 *
	 *@since
	 */
	public final static String
			PROPNAME_NAME = "name",
			PROPNAME_TYPE = "dataType",
			PROPNAME_REFERENCES = "references",
			PROPNAME_REFERENCE_TARGET = "references",
			PROPNAME_PK = "primaryKey",
			PROPNAME_DESCRIPTION = "description",
			PROPNAME_AUTOINCREMENT = "autoIncrement",
			PROPNAME_NOTNULL = "notNull";


	/**
	 *  convenience, 'name' must also bew a property of every COl
	 *
	 *@return    The name value
	 *@since
	 */
	String getName();


	/**
	 *  convenience
	 *
	 *@param  name  The new name value
	 *@since
	 */
	void setName(String name);


	/**
	 *  this order thing may need to be rethought.
	 *
	 *@return    The order value
	 *@since
	 */
	Integer getOrder();


	/**
	 *  Sets the order attribute of the Col object
	 *
	 *@param  order  The new order value
	 *@since
	 */
	void setOrder(Integer order);


	/**
	 *  Gets the asEML attribute of the Col object
	 *
	 *@param  parent  Description of the Parameter
	 *@return         The asEML value
	 */
	public Element getAsEML(Element parent, String parentTableName);

	//----from PropertyObject---
	//set properties in mass for
	//void    setProperties(Map properties);

	//Object  getProperty(String name);

	//void    setProperty(String name,Object value);
}

