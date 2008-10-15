/**
 *  '$RCSfile: AttributeSettings.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-10-15 02:52:57 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

/**
* 
* 	This class contains the absolute xPaths for the various Measurement scales 
*	of an Attribute. This is mainly used to set values in the AttributeDialog as 
*	the data passed to the setData() funciton of the AttributeDialog is a map of 
* 	xPath-value pairs
*
**/

public class AttributeSettings
{
	public static final String Attribute_xPath = "/eml:eml/dataset/dataTable/attributeList/attribute";
	public static final String AttributeName_xPath = "/eml:eml/dataset/dataTable/attributeList/attribute/attributeName";
	public static final String AttributeDefinition_xPath = "/eml:eml/dataset/dataTable/attributeList/attribute/attributeDefinition";
	public static final String AttributeLabel_xPath = "/eml:eml/dataset/dataTable/attributeList/attribute/attributeLabel";
	public static final String StorageType_xPath = "/eml:eml/dataset/dataTable/attributeList/attribute/storageType";
	public static final String MeasurementScale_xPath = "/eml:eml/dataset/dataTable/attributeList/attribute/measurementScale";
	public static final String MissingValueCode_xPath = "/eml:eml/dataset/dataTable/attributeList/attribute/missingValueCode";
	public static final String Nominal_xPath = "/eml:eml/dataset/dataTable/attributeList/attribute/measurementScale/nominal/nonNumericDomain";
	public static final String Nominal_xPath_rel = "/measurementScale/nominal/nonNumericDomain";
	
	public static final String Ordinal_xPath = "/eml:eml/dataset/dataTable/attributeList/attribute/measurementScale/ordinal/nonNumericDomain";
	public static final String Ordinal_xPath_rel = "/measurementScale/ordinal/nonNumericDomain";
	
	public static final String Interval_xPath = "/eml:eml/dataset/dataTable/attributeList/attribute/measurementScale/interval";
	public static final String Interval_xPath_rel = "/measurementScale/interval";
	public static final String Ratio_xPath = "/eml:eml/dataset/dataTable/attributeList/attribute/measurementScale/ratio";
	public static final String Ratio_xPath_rel = "/measurementScale/ratio";
	public static final String DateTime_xPath = "/eml:eml/dataset/dataTable/attributeList/attribute/measurementScale/dateTime";
	public static final String DateTime_xPath_rel = "/measurementScale/dateTime";


}

