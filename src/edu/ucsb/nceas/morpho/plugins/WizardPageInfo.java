/**
 *  '$RCSfile: Util.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-11-11 23:51:05 $'
 * '$Revision: 1.2 $'
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
package edu.ucsb.nceas.morpho.plugins;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

/**
 * It represents a wizard page information in incomplete-document medata part.
 * It include the wizard page class name and an array of parameter (string)
 * @author tao
 *
 */
public class WizardPageInfo 
{
    private String className = null;
    private Vector parameters = new Vector();//parameters is for the class' constructor.
    private OrderedMap variablesValues = new OrderedMap();//stores variable as key and value pair.
                                                                                 // or some other map value.
                                                                                //if eml itself doesn't have enough info for 
                                                                              //for loading a page. We store extral info here.
    /**
     * Constructor for given className
     * @param className
     */
    public WizardPageInfo(String className)
    {
    	this.className = className;   	
    }

    /**
     * Get class name of the page
     * @return
     */
	public String getClassName() 
	{
		return className;
	}

	/**
	 * Gets the parameter list of the class. Parameters is for UIPage's constructor
	 * @return
	 */
	public Vector getParameters() 
	{
		return parameters;
	}

	/**
	 * Adds a parameter into this object. Parameters is for UIPage's constructor.
	 * @param parameters
	 */
	public void addParameter(String parameter) 
	{
		this.parameters.add(parameter);
	}
	
	/**
	 * Gets variables value as pairs of key and values.
	 * This stores extra info if eml itself is not enough to load an page.
	 */
	public OrderedMap getVariablesValuesMap()
	{
		return this.variablesValues;
	}
	
	/**
	 * Puts the key-value pair into the variablesValues hashtable.
	 * This stores extra info if eml itself is not enough to load an page.
	 * @param key
	 * @param value
	 */
	public void putVariableValueMap(String key, String value)
	{
		if(key != null && value != null)
		{
			Log.debug(35, "In IncompleteDocInfo.putVariableValue put key "+key+" and value "+value+ " into variablesValue OrderedMap");
			variablesValues.put(key, value);
		}
	}
  


  /**
   * Adds some additional information to the class
   * @param additionalInfo
   */
  public void addAdditionalInfo(OrderedMap additionalInformation)
  {
    this.variablesValues.putAll(additionalInformation);
  }
	
	
}
