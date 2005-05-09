package edu.tesc.scidb.databank.sms.tdm;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

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
 *@author
 *@created    September 30, 2003
 *@since      November 20, 2001
 *@version    1.0
 */

public class DatabaseImpl implements Database
{
	private String name;
	private Map tables;
	private Map relationships;
	private List inserts;
	
	private Map props;
	
	public void setProperty(String name, Object value)
		{
			props.put(name, value);
		}

	public Object getProperty(String name)
	   {
		   return props.get(name);
	   }

	//private List relationships;

	/**
	 *  Constructor for the DatabaseImpl object
	 *
	 *@param  name  Description of Parameter
	 *@since
	 */
	public DatabaseImpl(String name)
	{
		this.name = name;
		this.tables = new TreeMap();
		this.inserts = new LinkedList();
		//this.relationships = new LinkedList();
		this.relationships = new TreeMap();
		this.props = new TreeMap();
		this.setProperty(Database.PROP_VIZMODULES, new LinkedList());
		this.setProperty(Database.PROP_ACCESSFORMS, new LinkedList());
		
	}


	/**
	 *  Sets the name attribute of the DatabaseImpl object
	 *
	 *@param  name  The new name value
	 *@since
	 */
	public void setName(String name)
	{
		this.name = name;
	}


	/**
	 *  Sets the targetDBMS attribute of the DatabaseImpl object
	 *
	 *@since
	 */
	public void setTargetDBMS()
	{
	}


	/**
	 *  Gets the name attribute of the DatabaseImpl object
	 *
	 *@return    The name value
	 *@since
	 */
	public String getName()
	{
		return name;
	}


	/**
	 *  Gets the tables attribute of the DatabaseImpl object
	 *
	 *@return    The tables value
	 *@since
	 */
	public Collection getTables()
	{
		return tables.values();
	}


	/**
	 *  Gets the table attribute of the DatabaseImpl object
	 *
	 *@param  name  Description of Parameter
	 *@return       The table value
	 *@since
	 */
	public Table getTable(String name)
	{
		return (Table) tables.get(name);
	}


	/**
	 *  Gets the targetDBMS attribute of the DatabaseImpl object
	 *
	 *@return    The targetDBMS value
	 *@since
	 */
	public String getTargetDBMS()
	{
		return null;
	}


	/**
	 *  Gets the relationships attribute of the DatabaseImpl object
	 *
	 *@return    The relationships value
	 *@since
	 */
	public Collection getRelationships()
	{
		return relationships.values();
	}


	/**
	 *  Adds a feature to the Table attribute of the DatabaseImpl object
	 *
	 *@param  table  The feature to be added to the Table attribute
	 *@since
	 */
	public void addTable(Table table)
	{
		tables.put(table.getName(), table);
	}


	/**
	 *  Adds a feature to the Relationship attribute of the DatabaseImpl object
	 *
	 *@param  rel  The feature to be added to the Relationship attribute
	 *@since
	 */
	public void addRelationship(Relationship rel)
	{
		relationships.put(rel.getName(), rel);
	}


	/**
	 *  Gets the inserts attribute of the DatabaseImpl object
	 *
	 *@return    The inserts value
	 */
	public Collection getInserts()
	{
		return inserts;
	}


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
	public void addInsert(Insert insert)
	{
		inserts.add(insert);
		System.err.println("adding insert to db");
	}


	//public void addRelationship(Relationship rel){relationships.add(rel);}

	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 *@since
	 */
	public String toString()
	{
		return "DatabaseImpl name=" + name + "\n" + tables + "\n" + relationships + "\n";
	}


	/**
	 *  Description of the Method
	 *
	 *@param  parent  Description of the Parameter
	 *@return         Description of the Return Value
	 */
	/*
	 *  public String generateEML()
	 *  {
	 *  Collection tables = this.getTables();
	 *  Iterator iter = tables.iterator();
	 *  String eml = new String("");
	 *  / For each table...
	 *  while (iter.hasNext())
	 *  {
	 *  Table table = (Table) iter.next();
	 *  eml = eml.concat("
	 *  table.getName().concat(" "));
	 *  }
	 *  return eml;
	 *  }
	 */
	public String getAsEML()
	{
		org.dom4j.DocumentFactory docfac = new org.dom4j.DocumentFactory();
        org.dom4j.Element emlElement = docfac.createElement("eml");
        
		Document emlDocument = docfac.createDocument();
		
		//		add tags for the vizModules if any
			 List vizModules = (List) this.getProperty(Database.PROP_VIZMODULES);
			 if(vizModules != null)
			 {
				 Iterator iter = vizModules.iterator();
				 while(iter.hasNext())
				 {
					 String moduleName = (String) iter.next();
					 emlDocument.addProcessingInstruction("CanopyView","visualizationModule=\""+moduleName+"\"");
				 }
			 }
		
	        
        emlElement.addAttribute("packageId", "eml.1.1");
		emlElement.addAttribute("system", "knb");

		Namespace nameSpace1 = docfac.createNamespace("eml", "eml://ecoinformatics.org/eml-2.0.0");
        emlElement.add(nameSpace1);
        
		Namespace nameSpace2 = docfac.createNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		emlElement.add(nameSpace2);
				
		Namespace nameSpace3 = docfac.createNamespace("ds", "eml://ecoinformatics.org/dataset-2.0.0");
		emlElement.add(nameSpace3);
		
		QName qName = docfac.createQName("eml", nameSpace1);
		emlElement.setQName(qName);
		
		Element element = this.getAsEML(emlElement);
	
		
		emlDocument.add(emlElement);
		
	// Pretty print the XML
	   OutputFormat format = OutputFormat.createPrettyPrint();
	   StringWriter sw = new StringWriter();
	   XMLWriter writer = new XMLWriter(sw, format );
	   try {
		writer.write( emlDocument );
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}



	return sw.toString();

		//return emlDocument.asXML(); 
		//return emlElement.asXML();
	}

	public Element getAsEML(Element parent)
	{
		Element dataset = parent.addElement("dataset");
		Element title = dataset.addElement("title");
		// TODO - a wizard should get the title and creator info from the user
		title.setText("A Canopy DataBank Generated Dataset");
		Element creator = dataset.addElement("creator");
		creator.addAttribute("id", "18481");
		Element individualName = creator.addElement("individualName");
		Element surName = individualName.addElement("surName");
		surName.setText("Smith");
		
		Element contact = dataset.addElement("contact");
		Element references = contact.addElement("references");
		references.setText("18481");

		Collection tables = this.getTables();
		Iterator iter = tables.iterator();

		while (iter.hasNext())
		{
			Table table = (Table) iter.next();

			table.getAsEML(dataset);
		}
		return dataset;
	}
}

