package edu.tesc.scidb.databank.sms;


import java.util.Map;

import edu.tesc.scidb.databank.sms.XMLObject;

/**
 *  Description of the Interface
 *
 *  Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *     must display the following acknowledgement:
 *     This product includes software developed by the The Evergreen State
 *     College-- Scientific Laboritory and Canopy Research Group and its
 *     contributors.  With Funding provived by the NSF Database Activities
 *     Program [BIR 9975510]
 * 4. Neither the name of the College or NSF nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 *@author     Erik Ordway ordwayeATevergreen.edu
 *@since    November 20, 2001
 */
public interface PropertyObject extends  XMLObject
{
    /**
     *  Gets the property attribute of the PropertyObject object
     *
     *@param  name  Description of Parameter
     *@return       The property value
     *@since
     */
    Object getProperty(String name);


    /**
     *  Sets the property attribute of the PropertyObject object
     *
     *@param  name   The new property value
     *@param  value  The new property value
     *@since
     */
    void setProperty(String name, Object value);


    /**
     *  Gets the properties attribute of the PropertyObject object
     *
     *@return    The properties value
     *@since
     */
    Map getProperties();


    /**
     *  Sets the properties attribute of the PropertyObject object
     *
     *@param  props  The new properties value
     *@since
     */
    void setProperties(Map props);


    /**
     *  Description of the Method
     *
     *@param  name  Description of Parameter
     *@return       Description of the Returned Value
     *@since
     */
    boolean hasProperty(String name);


}

