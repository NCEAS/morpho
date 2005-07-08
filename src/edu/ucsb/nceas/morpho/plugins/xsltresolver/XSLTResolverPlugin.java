/**
 *  '$RCSfile: XSLTResolverPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2005-07-08 18:58:21 $'
 * '$Revision: 1.13 $'
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

package edu.ucsb.nceas.morpho.plugins.xsltresolver;

import java.io.Reader;
import java.io.InputStreamReader;

import java.util.Vector;
import java.util.Hashtable;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;

import edu.ucsb.nceas.morpho.plugins.XSLTResolverInterface;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.DocumentNotFoundException;

import edu.ucsb.nceas.morpho.util.Log;



/**
 *  Plugin that provides a service to resolve DOCTYPES to XSLT stylesheets
 */
public class XSLTResolverPlugin implements  XSLTResolverInterface,
                                            PluginInterface,
                                            ServiceProvider
{

    private final String CONFIG_KEY_GENERIC_STYLESHEET  = "genericStylesheet";
    private final String CONFIG_KEY_DOCTYPE_TO_XSLT = "doctype_xslt_mappings";
    private final String CONFIG_KEY_DOCTYPE         = "doctype";
    private final String CONFIG_KEY_XSLT            = "xslt";

    private final String CONFIG_KEY_GENERIC_LOCATION = "genericStylesheetLocation";
    private final String CONFIG_KEY_DOCTYPE_TO_LOCATIONS
                                                    = "doctype_xslt_location_mappings";
    private final String CONFIG_KEY_LOCATIONS       = "location";

    private final String CONFIG_KEY_TREE_EDITOR_XML = "tree_editor_xml_location_mappings";

    private final String    GENERIC_STYLESHEET;
    private final ConfigXML config;

    private       Hashtable xslt_mappings;
    private       Hashtable location_mappings;
    private       Hashtable tree_editor_mappings;

    private final ClassLoader classLoader;

    public XSLTResolverPlugin()
    {
        classLoader = Morpho.class.getClassLoader();
        Thread t = Thread.currentThread();
        t.setContextClassLoader(classLoader);

        this.config = Morpho.getConfiguration();
        GENERIC_STYLESHEET = config.get(CONFIG_KEY_GENERIC_STYLESHEET, 0);
        initDoctypeToXSLTMappings();
    }

    /**
     *  Required by PluginInterface; called automatically at runtime
     *
     *  @param morpho    a reference to the <code>Morpho</code>
     */
    public void initialize(Morpho morpho)
    {
        try
        {
          ServiceController services = ServiceController.getInstance();
          services.addService(XSLTResolverInterface.class, this);
          Log.debug(20, "Service added: XSLTResolverInterface.");
        }
        catch (ServiceExistsException see)
        {
          Log.debug(6, "Service registration failed: XSLTResolverInterface");
          Log.debug(6, see.toString());
        }
    }


    /**
     *  Required by XSLTResolverInterface:
     *  method to return a Reader object, which will provide access to a
     *  character-based XSLT stylesheet. The stylesheet to be returned is
     *  determined based on the unique DOCID String identifier passed to this
     *  method.  If a stylesheet corresponding to the DOCID cannot be found,
     *  a default or generic stylesheet may be returned.
     *  If no suitable stylesheet can be returned, a DocumentNotFoundException
     *  is thrown
     *
     *  @param identifier - unique identifier used to determine the stylesheet
     *                to return (e.g. DOCTYPE for DTD-defined XML, or
     *                schemaLocation or rootnode namespace for XSD-defined XML)
     *
     *  @return       a Reader for the character-based XSLT stylesheet. If a
     *                stylesheet corresponding to the DOCID cannot be found,
     *                a default or generic stylesheet may be returned.
     *
     *  @throws DocumentNotFoundException if no suitable stylesheet is available
     */
    public Reader getXSLTStylesheetReader(String identifier)
                                              throws DocumentNotFoundException
    {
        Log.debug(50, "\nXSLTResolver got: "+identifier);
        Reader rdr = null;
        String xslPathString = getFromMappings(xslt_mappings, identifier);
        if (xslPathString==null || xslPathString.trim().equals("")) {

            rdr =  new InputStreamReader(
                            classLoader.getResourceAsStream(GENERIC_STYLESHEET));
            Log.debug(50, "getXSLTStylesheetReader() failed to find valid "
                            +"stylesheet for identifier: "+identifier
                            +"\n returning default: "+GENERIC_STYLESHEET);
        } else {
            rdr =  new InputStreamReader(
                            classLoader.getResourceAsStream(xslPathString));
            Log.debug(50, "getXSLTStylesheetReader() found a value for the "
                            +"stylesheet for identifier: "+identifier
                            +"\n returning: "+xslPathString);
        }
        Log.debug(50, "\nXSLTResolver returning Reader: "+rdr);
        return rdr;
    }

    /**
     *  Required by XSLTResolverInterface:
     *  method to return a String, which will contain the name of the dir
     *  which conatins the XSLT stylesheets. The dir to be returned is
     *  determined based on the unique DOCID String identifier passed to this
     *  method.  If a stylesheet corresponding to the DOCID cannot be found,
     *  a default or generic stylesheet may be returned.
     *
     *  @param identifier - unique identifier used to determine the stylesheet
     *                to return (e.g. DOCTYPE for DTD-defined XML, or
     *                schemaLocation or rootnode namespace for XSD-defined XML)
     *
     *  @return       a String, which will contain the name of the dir
     *                which conatins the XSLT stylesheets. If a
     *                stylesheet corresponding to the DOCID cannot be found,
     *                a default or generic stylesheet may be returned.
     *
     */
     public String getXSLTStylesheetLocation(String identifier)
     {
        Log.debug(50, "\ngetXSLTStylesheetLocation got: "+identifier);
        String xslPathString = getFromMappings(location_mappings, identifier);
        if (xslPathString==null || xslPathString.trim().equals("")) {
          xslPathString = config.get(CONFIG_KEY_GENERIC_LOCATION, 0);
        }
        Log.debug(50, "\ngetXSLTStylesheetLocation returning: "+xslPathString);
        return xslPathString;
    }


    /**
     *  Required by XSLTResolverInterface:
     *  method to return a String, which will contain the name of the xml file
     *  which conatins the structure of schema that can be displayed by the
     *  tree editor. If a xml file corresponding to the DOCID cannot be found,
     *  null is returned.
     *
     *  @param identifier - unique identifier used to determine the stylesheet
     *                to return (e.g. DOCTYPE for DTD-defined XML, or
     *                schemaLocation or rootnode namespace for XSD-defined XML)
     *
     *  @return       a String, which will contain the name of the xml file
     *                which conatins the structure of schema that can be
     *                displayed by the tree editor. If a xml file corresponding
     *                to the DOCID cannot be found, null is returned.
     *
     */
     public String getTreeEditorXMLLocation(String identifier)
     {
        Log.debug(50, "\ngetTreeEditorXMLLocation got: "+identifier);
        String xmlPathString = getFromMappings(tree_editor_mappings, identifier);

        Log.debug(50, "\ngetTreeEditorXMLLocation returning: "+xmlPathString);
        return xmlPathString;
    }

    // gets the doctype-to-xslt mappings from the config file and adds them to
    //the mappings hashtable
    private void initDoctypeToXSLTMappings()
    {
        xslt_mappings = config.getHashtable( CONFIG_KEY_DOCTYPE_TO_XSLT,
                                        CONFIG_KEY_DOCTYPE,
                                        CONFIG_KEY_XSLT );

        location_mappings = config.getHashtable( CONFIG_KEY_DOCTYPE_TO_LOCATIONS,
                                        CONFIG_KEY_DOCTYPE,
                                        CONFIG_KEY_LOCATIONS );

        tree_editor_mappings = config.getHashtable( CONFIG_KEY_TREE_EDITOR_XML,
                                        CONFIG_KEY_DOCTYPE,
                                        CONFIG_KEY_LOCATIONS );

    }

    //trims whitespace, checks for null and empty strings, checks to see if
    //already in HashTable, and if so, returns value for this key
    private String getFromMappings(Hashtable mappings, String key)
    {
        Log.debug(50,"XSLTResolverPlugin.getFromMappings() got key="+key);

        if ( key==null || key.equals("")) {

            Log.debug(12,"ALERT: XSLTResolverPlugin.getFromMappings(): got key="
                                                                          +key);
        } else if (!mappings.containsKey(key)) {

            Log.debug(12,"ALERT: XSLTResolverPlugin.getFromMappings():"
                                                   +" could not find key="+key);
        } else {
            String xslt = (String)mappings.get(key);
            Log.debug(50,"XSLTResolverPlugin.getFromMappings() value = "+xslt);
            return xslt;
        }
        return null;
    }
}
