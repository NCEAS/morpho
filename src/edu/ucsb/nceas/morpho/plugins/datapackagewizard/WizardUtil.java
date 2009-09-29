/**
 *  '$RCSfile: WizardSettings.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-20 18:26:05 $'
 * '$Revision: 1.77 $'
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
package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.LoadDataPath;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.ModifyingPageDataInfo;
import edu.ucsb.nceas.morpho.util.XPathUIPageMapping;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * Represents methods can be repeatedly used in this plugin
 * @author tao
 *
 */
public class WizardUtil 
{
	private static final String MAPPINGFILEPATH = "lib/xpath-wizard-map.xml";
	// element name used in mapping properties file (xml format)
	private static final String MAPPING = "mapping";
	private static final String XPATH = "xpath";
	private static final String WIZARDAGECLASS = "wizardPageClass";
	private static final String ROOT = "root";
	private static final String INFORORMODIFYINGDATA = "infoForModifyingData";
	private static final String LOADEXISTDATAPATH = "loadExistDataPath";
	private static final String XPATHFORGETTINGPAGEDATA = "xpathForGettingPageData";
	private static final String NEWDATADOCUMENTNAME = "newDataDocumentName";
	private static final String GENERICNAME = "genericName";
	private static final String XPAHTFORCREATINGORDEREDMAP = "xpathForCreatingOrderedMap";
	private static final String WIZARDAGECLASSPARAMETER ="wizardPageClassParameter";
	private static final String XPATHFORSETTINGPAGEDATA = "xpathForSettingPageData";
	private static final String WIZARDCONTAINERFRAME = "edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame";
	private static final String CORRECTIONSUMMARY = "edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CorrectionSummary";
	private static final String TITLE = "Correction Wizard for Data Package ";
	private static final String STARTPAGEID = "0";
	private static final String PARA = "para";
	private static final String SLASH = "/";
	private final static String DATATABLE = "dataTable";
	private final static String ATTRIBUTE = "attribute";
	private final static String RIGHTBRACKET = "]";
	private final static String LEFTBRACKET = "[";
	private final static char RIGHTBRACKETCHAR = ']';
	private final static char LEFTBRACKETCHAR = '[';
	private final static String KEY = "key";
	private final static String PREVNODE = "prevNode";
	private final static String NEXTNODE = "nextNode";
	public static Hashtable fullPathMapping = new Hashtable();
	public static Hashtable shortPathMapping = new Hashtable();
	
	
	
	/**
	 * Create an object for given class name. This is only for AbractUIPage object
	 * @param className
	 * @param frame
	 * @param parameters
	 * @return
	 */
	public static AbstractUIPage createAbstractUIpageObject(String className, WizardContainerFrame frame, Vector parameters)  {

		Object object = null;
		Class classDefinition = null;
		try {
			classDefinition = Class.forName(className);
			//we only consider String and boolean
			if(parameters != null && !parameters.isEmpty())
			{
				Class[] parameterList = new Class[parameters.size()];
				Object[] objectList = new Object[parameters.size()];
				for(int i=0; i<parameters.size(); i++)
				{
					String para = (String)parameters.elementAt(i);
					if(para.equalsIgnoreCase("false") || para.equalsIgnoreCase("true"))
					{
						Class parameter= Class.forName("java.lang.Boolean");
						parameterList[i] = parameter;	
						objectList[i]= new Boolean(para);
					}
					else
					{
						Class parameter = Class.forName("java.lang.String");
						parameterList[i] = parameter;
						objectList[i] = para;
					}
				}
				Constructor constructor = classDefinition.getDeclaredConstructor(parameterList);
				object =constructor.newInstance(objectList);
			} 
			else
			{
			    object = classDefinition.newInstance();
			}
		} catch (InstantiationException e) {
			Log.debug(30, "InstantiationException "+e.getMessage());
			// couldn't get default constructor. The contructor has a parameter WizardContainerFrame
			try
			{
				if (classDefinition != null)
				{
					Class parameter= Class.forName(WIZARDCONTAINERFRAME);
					Class[] parameterList = new Class[1];
					parameterList[0] = parameter;
					Constructor constructor = classDefinition.getDeclaredConstructor(parameterList);
					object = constructor.newInstance(frame);
				}
			}
			catch(Exception ee)
			{
				Log.debug(30, "Exception "+e.getMessage());
			}
		} catch (IllegalAccessException e) {
			Log.debug(30, "IllegalAccessException "+e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.debug(30, "ClassNotFoundException "+e.getMessage());
		} 
		catch(Exception e)
		{
			Log.debug(30, "Exception in creating an UI page object through the class name "+className + " is " +e.getMessage());
		}
		Log.debug(30, "successfully create the instance of this class "+className);
		return (AbstractUIPage)object;
	}
	
	
}
