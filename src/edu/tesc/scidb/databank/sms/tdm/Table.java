package edu.tesc.scidb.databank.sms.tdm;

import edu.tesc.scidb.databank.sms.TDMConstraints;

import java.util.Collection;
import java.util.Comparator;
import org.dom4j.Element;
import org.w3c.dom.Node;

/**
 *  Title: Description: The Function getPrimary keys is used when generating a
 *  relationships to an instance of a Table In most cases this will only return
 *  a List of one Item but it could return more than one. Represents a table in
 *  a database. very basic set of meta data, just enough to generate create
 *  statments. we need some way to allow for the proper presentation of the
 *  columns. Copyright: Copyright (c) 2000 Company: Redistribution and use in
 *  source and binary forms, with or without modification, are permitted
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
 *@author     Erik Ordway ordwayeATevergreen.edu
 *@created    September 30, 2003
 *@since      November 20, 2001
 *@version    10
 */

public interface Table extends TDMConstraints
{
	/**
	 *  gets the name of the table.
	 *
	 *@return    The name value
	 *@since
	 */
	String getName();


	/**
	 *  sets the name of the table.
	 *
	 *@param  name  The new name value
	 *@since
	 */
	void setName(String name);


	/**
	 *  returns the cols of this table in no particular order.
	 *
	 *@return    The cols value
	 *@since
	 */
	Collection getCols();


	/**
	 *  returns the cols of this table in the order dictated by sortOrder.
	 *  sortOrder must work on Col objects.
	 *
	 *@param  sortOrder  Description of Parameter
	 *@return            The cols value
	 *@since
	 */
	Collection getCols(Comparator sortOrder);


	/**
	 *  adds a column to this table. If a column of the sname name is allready part
	 *  of this table than the existing column may be droped.
	 *
	 *@param  col  The feature to be added to the Col attribute
	 *@since
	 */
	void addCol(Col col);


	/**
	 *  gets a col given its name. Null if no column of that name exists.
	 *
	 *@param  name  Description of Parameter
	 *@return       The col value
	 *@since
	 */
	Col getCol(String name);


	/**
	 *  the collection of cols that have the "primaryKey" value set.
	 *
	 *@return    The primaryKeys value
	 *@since
	 */
	Collection getPrimaryKeys();


	/**
	 *  the collection of cols that have the "primaryKey" value set by name
	 *
	 *@return    The primaryKeysString value
	 *@since
	 */
	public Collection getPrimaryKeysString();


	/**
	 *  Gets the asEML attribute of the Table object
	 *
	 *@param  parent  Description of the Parameter
	 *@return         The asEML value
	 */
	public Element getAsEML(Element parent);
	public Node getDataTableNode();

}
