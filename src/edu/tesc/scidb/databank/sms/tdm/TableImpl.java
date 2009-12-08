package edu.tesc.scidb.databank.sms.tdm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.xerces.parsers.DOMParser;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *  Title: Description: Copyright: Copyright (c) 2000 Company: Redistribution
 *  and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met: 1. Redistributions
 *  of source code must retain the above copyright notice, this list of
 *  conditions and the following disclaimer. 2. Redistributions in binary form
 *  must reproduce the above copyright notice, this list of conditions and the
 *  following disclaimer in the documentation and/or other materials provided
 *  with the distribution. 3. All advertising materials mentioning features or
 *  use of this software must display the following acknowledgement: This
 *  product includes software developed by the The Evergreen State College--
 *  Scientific Laboritory and Canopy Research Group and its contributors. With
 *  Funding provived by the NSF Database Activities Program [BIR 9975510] 4.
 *  Neither the name of the College or NSF nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *@author     Erik Ordway ordwayeATevergreen.edu
 *@created    September 30, 2003
 *@since      November 20, 2001
 *@version    1.0
 */

public class TableImpl implements Table
{
	/**
	 *  Description of the Field
	 *
	 *@since
	 */
	public final static Comparator PKFIRST_SORTORDER = new PKFirstColComparator();
	/**
	 *  Description of the Field
	 *
	 *@since
	 */
	public final static Comparator PROP_ORDER_SORTORDER = new PropOrderColComparator();
	/**
	 *  Description of the Field
	 *
	 *@since
	 */
	public final static Comparator DEFAULT_SORTORDER = PROP_ORDER_SORTORDER;
	/**
	 * Fill some element with unknown value
	 */
	public final static String UNKNOW = "unknown";
	private final static String HARDDIRVE = "hard drive";	
	private String name;
	//private List cols;
	private Map cols;
	private File databasePhysicalFile;


	/**
	 *  Constructor for the TableImpl object
	 *
	 *@param  name  Description of Parameter
	 *@param  databasePhysicalFile  the file of the database
	 *@since
	 */
	public TableImpl(String name, File databasePhysicalFile)
	{
		this.name = name;
		this.databasePhysicalFile = databasePhysicalFile;
		this.cols = new TreeMap();
	}


	/**
	 *  Sets the name attribute of the TableImpl object
	 *
	 *@param  name  The new name value
	 *@since
	 */
	public void setName(String name)
	{
		this.name = name;
	}


	/**
	 *  Gets the name attribute of the TableImpl object
	 *
	 *@return    The name value
	 *@since
	 */
	public String getName()
	{
		return name;
	}


	//  public Collection getCols(){return cols;}

	/**
	 *  Gets the cols attribute of the TableImpl object
	 *
	 *@return    The cols value
	 *@since
	 */
	public Collection getCols()
	{
		return getCols(PKFIRST_SORTORDER);
	}


	/**
	 *  Gets the cols attribute of the TableImpl object
	 *
	 *@param  sortOrder  Description of Parameter
	 *@return            The cols value
	 *@since
	 */
	public Collection getCols(Comparator sortOrder)
	{
		List l = new ArrayList(cols.values());
		Collections.sort(l, sortOrder);
		return l;
	}


	/**
	 *  Gets the col attribute of the TableImpl object
	 *
	 *@param  name  Description of Parameter
	 *@return       The col value
	 *@since
	 */
	public Col getCol(String name)
	{
		return (Col) cols.get(name);
	}


	/**
	 *  Gets the primaryKeys attribute of the TableImpl object
	 *
	 *@return    The primaryKeys value
	 *@since
	 */
	public Collection getPrimaryKeys()
	{
		List cols = new LinkedList();
		Iterator itr = getCols().iterator();
		while (itr.hasNext())
		{
			Col col = (Col) itr.next();
			if (col.getProperty(PROP_PRIMARY_KEY) != null)
				cols.add(col);

		}
		///turn list arround and return in the right order
		List cols2 = new LinkedList();
		itr = cols.iterator();
		while (itr.hasNext())
		{
			Col col = (Col) itr.next();
			cols2.add(col);
		}
		return cols2;
	}


	/**
	 *  Gets the primaryKeysString attribute of the TableImpl object
	 *
	 *@return    The primaryKeysString value
	 *@since
	 */
	public Collection getPrimaryKeysString()
	{
		List cols = new LinkedList();
		Iterator itr = getCols().iterator();
		while (itr.hasNext())
		{
			Col col = (Col) itr.next();
			if (col.getProperty(PROP_PRIMARY_KEY) != null)
				cols.add(col.getName());

		}
		///turn list arround and return in the right order
		List cols2 = new LinkedList();
		itr = cols.iterator();
		String name;
		while (itr.hasNext())
		{
			name = (String) itr.next();
			cols2.add(name);
		}
		return cols2;
	}


	/**
	 *  Adds a feature to the Col attribute of the TableImpl object
	 *
	 *@param  col  The feature to be added to the Col attribute
	 *@since
	 */
	public void addCol(Col col)
	{
		cols.put(col.getName(), col);
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 *@since
	 */
	public String toString()
	{
		return "TableImpl name=" + name + "\n" + cols + "\n";
	}


	/**
	 *  Description of the Class
	 *
	 *@author     Erik Ordway
	 *@created    September 30, 2003
	 *@since      November 20, 2001
	 */
	private static class PKFirstColComparator implements Comparator
	{
		/**
		 *  Description of the Method
		 *
		 *@param  o1  Description of Parameter
		 *@param  o2  Description of Parameter
		 *@return     Description of the Returned Value
		 *@since
		 */
		public int compare(Object o1, Object o2)
		{
			int val = ((Col) o1).getName().compareTo(((Col) o2).getName());
			//what is done by default
			if (((Col) o1).getProperty(PROP_PRIMARY_KEY) != null)
			{
				if (((Col) o2).getProperty(PROP_PRIMARY_KEY) != null)
					return val;
				//both primary keys
				else
					return -1;
			}
			else if (((Col) o2).getProperty(PROP_PRIMARY_KEY) != null)
				return 1;
			return val;
		}
	}


	/**
	 *  Description of the Class
	 *
	 *@author     Erik Ordway
	 *@created    September 30, 2003
	 *@since      November 20, 2001
	 */
	private static class PropOrderColComparator implements Comparator
	{
		/**
		 *  Description of the Method
		 *
		 *@param  o1  Description of Parameter
		 *@param  o2  Description of Parameter
		 *@return     Description of the Returned Value
		 *@since
		 */
		public int compare(Object o1, Object o2)
		{
			try
			{
				int val = ((Integer) ((Col) o1).getProperty(PROP_ORDER))
						.compareTo
						((Integer) ((Col) o2).getProperty(PROP_ORDER));
				return val;
			} catch (Exception e)
			{
				return 1;
			}
		}
	}


	/**
	 *  Gets the asEML attribute of the TableImpl object
	 *
	 *@param  parent  Description of the Parameter
	 *@return         The asEML value
	 */
	public Element getAsEML(Element parent)
	{
		Element table = parent.addElement("dataTable");
		table.addAttribute("id", this.getName());
		Element entityName = table.addElement("entityName");
		entityName.setText(this.getName());
		
		// 	TODO - add a better physical section for morpho		
		Element physical = table.addElement("physical");
		Element objectName = physical.addElement("objectName");
		String objectNameString = null;
		if(databasePhysicalFile != null)
		{
		    objectNameString = databasePhysicalFile.getName();
		}
		if(objectNameString != null && !objectNameString.equals(""))
		{
		    objectName.setText(objectNameString);
		}
		else
		{
		    objectName.setText(UNKNOW);
		}
		Element dataFormat = physical.addElement("dataFormat");
		Element externallyDefinedFormat = dataFormat.addElement("externallyDefinedFormat");
		Element formatName = externallyDefinedFormat.addElement("formatName");
		// TODO - This dosn't have to be MS Access here
		formatName.setText("Microsoft Access");
		Element distribution = physical.addElement("distribution");
		Element offline = distribution.addElement("offline");
		Element mediumName = offline.addElement("mediumName");
		mediumName.setText(HARDDIRVE);

		Element attributeList = table.addElement("attributeList");

		// go through all this tables columns
		Collection cols = this.getCols();
		Iterator iter = cols.iterator();
		while (iter.hasNext())
		{
			Col col = (Col) iter.next();
			col.getAsEML(attributeList, this.getName());

		}

		// go through all this tables columns again
		// looking for the primary key(s)
		cols = this.getCols();
		iter = cols.iterator();
		while (iter.hasNext())
		{
			// primary key constraints
			Col col = (Col) iter.next();
			String primaryKeyProp = (String) col.getProperty("primaryKey");
			if (primaryKeyProp != null)
			{
				if (primaryKeyProp.equals("1"))
				{
					Element constraint = table.addElement("constraint");
					Element primaryKey = constraint.addElement("primaryKey");
					Element constraintName = primaryKey.addElement("constraintName");
					constraintName.setText("Primary Key");
					Element key = primaryKey.addElement("key");
					Element attributeReference = key.addElement("attributeReference");
					attributeReference.setText(this.getName() + "." + col.getName());
				}
			}

			// foreignKey constraints
			String referencesProp = (String) col.getProperty("references");
			if (referencesProp != null)
			{
				Element constraint = table.addElement("constraint");
				Element foreignKey = constraint.addElement("foreignKey");
				Element constraintName = foreignKey.addElement("constraintName");
				constraintName.setText("Foreign Key");
				Element key = foreignKey.addElement("key");
				Element attributeReference = key.addElement("attributeReference");
				attributeReference.setText(this.getName() + "." + col.getName());
				Element entityReference = foreignKey.addElement("entityReference");
				entityReference.setText(referencesProp);

			}

			// NOT null constraint
			String notNullProp = (String) col.getProperty("notNull");
			if (notNullProp != null)
			{
				if (notNullProp.equals("1"))
				{
					Element constraint = table.addElement("constraint");
					Element primaryKey = constraint.addElement("notNullConstraint");
					Element constraintName = primaryKey.addElement("constraintName");
					constraintName.setText("Not Nullable");
					Element key = primaryKey.addElement("key");
					Element attributeReference = key.addElement("attributeReference");
					attributeReference.setText(this.getName() + "." + col.getName());
				}
			}
		}

		return table;
	}

	public Node getDataTableNode() {

					 //	In order to properly cast this DOM4J tree into a w3c object 
					 // is to necessary to have DOM4J stream the bytes to a xerces dom parser
					 // DOM4J likes to throw "not yet supported" exceptions otherwise	
				
					 ByteArrayOutputStream output  = new ByteArrayOutputStream();
					 ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
				
					 org.w3c.dom.Document finalDocument = null;					 
					 
					 Element table = this.getAsEML(new DOMElement("dummyElement"));
					 
					 // make a DOM4J Document out of the DOM4J table element
					 DOMDocument domDoc = new DOMDocument();
					 domDoc.setRootElement(table);			 
					 
					
					 try {
						 // create a DOM4J XML writer and send bytes to the output stream
						 XMLWriter writer = new XMLWriter(output);
						 writer.write(domDoc);
						 writer.flush();
										
						 // create the input stream from the DOM4J byte stream
						 input = new ByteArrayInputStream(output.toByteArray());
						 					
						 // create a xerces input source from the input byte stream
						 InputSource inputSource = new InputSource(input);
						 // create a xerces parser and set the input source
						 DOMParser domParser = new DOMParser();
						 domParser.parse(inputSource);
					
						 // the final docuemnt is pure xerces implimented w3c DOM tree
						 finalDocument = domParser.getDocument();
					
					 } catch (UnsupportedEncodingException e) {
						 // TODO Auto-generated catch block
						 e.printStackTrace();
					 } catch (IOException e) {
						 // TODO Auto-generated catch block
						 e.printStackTrace();
					 } catch (SAXException e) {
						 // TODO Auto-generated catch block
						 e.printStackTrace();
					 }
								
					 return finalDocument.getFirstChild();
					 
	}




}

