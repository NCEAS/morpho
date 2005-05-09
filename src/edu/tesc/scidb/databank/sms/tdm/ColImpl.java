package edu.tesc.scidb.databank.sms.tdm;

import java.util.Map;
import java.util.TreeMap;
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
public class ColImpl implements Col
{
	private Map props;


	/**
	 *  Constructor for the ColImpl object
	 *
	 *@param  name  Description of Parameter
	 *@param  type  Description of Parameter
	 *@since
	 */
	public ColImpl(String name, String type)
	{
		this.props = new TreeMap();
		setProperty(PROPNAME_TYPE, type);
		setName(name);
	}


	/**
	 *  Constructor for the ColImpl object
	 *
	 *@param  name  Description of Parameter
	 *@since
	 */
	public ColImpl(String name)
	{
		this.props = new TreeMap();
		setName(name);
	}


	/**
	 *  Constructor for the ColImpl object
	 *
	 *@param  props  Description of Parameter
	 *@since
	 */
	public ColImpl(Map props)
	{
		this.props = props;
	}


	/**
	 *  Sets the name attribute of the ColImpl object
	 *
	 *@param  name  The new name value
	 *@since
	 */
	public void setName(String name)
	{
		setProperty(PROPNAME_NAME, name);
	}


	/**
	 *  Sets the property attribute of the ColImpl object
	 *
	 *@param  name   The new property value
	 *@param  value  The new property value
	 *@since
	 */
	public void setProperty(String name, Object value)
	{
		props.put(name, value);
	}


	/**
	 *  Sets the properties attribute of the ColImpl object
	 *
	 *@param  props  The new properties value
	 *@since
	 */
	public void setProperties(Map props)
	{
		this.props = props;
	}


	/**
	 *  Sets the order attribute of the ColImpl object
	 *
	 *@param  order  The new order value
	 *@since
	 */
	public void setOrder(Integer order)
	{
		props.put(PROP_ORDER, order);
	}


	/**
	 *  Gets the name attribute of the ColImpl object
	 *
	 *@return    The name value
	 *@since
	 */
	public String getName()
	{
		return (String) getProperty(PROPNAME_NAME);
	}


	/**
	 *  Gets the property attribute of the ColImpl object
	 *
	 *@param  name  Description of Parameter
	 *@return       The property value
	 *@since
	 */
	public Object getProperty(String name)
	{
		return props.get(name);
	}


	/**
	 *  Gets the properties attribute of the ColImpl object
	 *
	 *@return    The properties value
	 *@since
	 */
	public Map getProperties()
	{
		return props;
	}


	/**
	 *  Gets the order attribute of the ColImpl object
	 *
	 *@return    The order value
	 *@since
	 */
	public Integer getOrder()
	{
		return (Integer) props.get(PROP_ORDER);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  name  Description of Parameter
	 *@return       Description of the Returned Value
	 *@since
	 */
	public boolean hasProperty(String name)
	{
		return props.containsKey(name);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  other  Description of Parameter
	 *@return        Description of the Returned Value
	 *@since
	 */
	public int compareTo(Object other)
	{
		if (this.getOrder().intValue() > ((ColImpl) other).getOrder().intValue())
			return 1;
		else
				if (this.getOrder().intValue() < ((ColImpl) other).getOrder().intValue())
			return -1;
		else
			return 0;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 *@since
	 */
	public String toString()
	{
		return "ColImpl " + props;
	}


	/**
	 *  Gets the asEML attribute of the ColImpl object
	 *
	 *@param  parent  Description of the Parameter
	 *@return         The asEML value
	 */
	public Element getAsEML(Element parent, String parentTableName)
	{
		Element attribute = parent.addElement("attribute");
		attribute.addAttribute("id", parentTableName + "." + this.getName());
		Element attributeName = attribute.addElement("attributeName");
		attributeName.setText(this.getName());

		String attDef = (String) this.getProperty(PROPNAME_DESCRIPTION);
		if (attDef == null)
			attDef = new String("");
		Element attributeDef = attribute.addElement("attributeDefinition");
		attributeDef.setText(attDef);

		String attType = (String) this.getProperty(PROPNAME_DATATYPE);
		String attUnit = (String) this.getProperty(PROPNAME_UNIT);
		if (attType != null)
		{
			Element storageType = attribute.addElement("storageType");
			storageType.setText(attType);
		}
		
		// The following code is necessary to complete the EML required "measurement scale"
		// since this information is not contained in the templates the following code
		// attempts to make good judgements/defaults so the final product is EML valid.

		// if this attribute's dataType is ID or Reference or Boolean then it is nominal data
		if(attType.equals(TYPE_ID) || attType.equals(TYPE_IDREF) || attType.equals(TYPE_DATAIDREF) || attType.equals(TYPE_BOOL))
			{
				Element measurementScale = attribute.addElement("measurementScale");
				Element nominal = measurementScale.addElement("nominal");
				Element nonNumericDomain = nominal.addElement("nonNumericDomain");
				Element enumeratedDomain = nonNumericDomain.addElement("enumeratedDomain");
				Element codeDefinition = enumeratedDomain.addElement("codeDefinition");
				Element code = codeDefinition.addElement("code");
				code.setText("1");
				Element definition = codeDefinition.addElement("definition");
				if(attType.equals(TYPE_ID))
					definition.setText("Primary Key");
				else if(attType.equals(TYPE_IDREF) || attType.equals(TYPE_DATAIDREF))
					definition.setText("Foreign Key");
				else if(attType.equals(TYPE_BOOL))
					definition.setText("Boolean Value");
				
			}
	    // if this attribute's dataType is some sort of String then it is nominal data text domain
		else if(attType.equals(TYPE_STRING) || attType.equals(TYPE_TEXT) || attType.equals(TYPE_MEMO) ||  attType.equals(TYPE_BIGSTRING))
			{
				Element measurementScale = attribute.addElement("measurementScale");
				Element nominal = measurementScale.addElement("nominal");
				Element nonNumericDomain = nominal.addElement("nonNumericDomain");
				Element textDomain = nonNumericDomain.addElement("textDomain");
				Element definition = textDomain.addElement("definition");
				definition.setText("String Field");
			}
		// if this attribute's dataType is some sort of date/time then it is dateTime
		else if(attType.equals(TYPE_DATE) || attType.equals(TYPE_TIME) || attType.equals(TYPE_DATETIME))
			{
				Element measurementScale = attribute.addElement("measurementScale");
				Element dateTime = measurementScale.addElement("datetime");
				Element formatString = dateTime.addElement("formatString");
				formatString.setText("MM/DD/YYYY HH:MM:SS AM");
				Element dateTimePercision = dateTime.addElement("dateTimePrecision");
				dateTimePercision.setText("1");
				Element dateTimeDomain = dateTime.addElement("dateTimeDomain");
				Element bounds = dateTimeDomain.addElement("bounds");
			}
		// if this attribute is an Int, Float, or Percent
		else if(attType.equals(TYPE_INT) || attType.equals(TYPE_FLOAT) || attType.equals(TYPE_PERCENT))
			{
				if(attUnit == null)
						attUnit = new String("number");
				
				Element measurementScale = attribute.addElement("measurementScale");
				Element ratio = measurementScale.addElement("ratio");
				Element unit = ratio.addElement("unit");
				Element standardUnit = unit.addElement("standardUnit");
				standardUnit.setText(attUnit);
				Element precision = ratio.addElement("precision");
				precision.setText("1");
				Element numericDomain = ratio.addElement("numericDomain");
				Element numberType = numericDomain.addElement("numberType");
				if(attType.equals(TYPE_INT))
					numberType.setText("integer");				
				else
					numberType.setText("real");
				Element bounds = numericDomain.addElement("bounds");
			}
		

		//System.out.println(props);

		return attribute;
	}

}

