/**
 *  '$RCSfile: XSLTResolverPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-18 18:21:06 $'
 * '$Revision: 1.7 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;

import edu.ucsb.nceas.morpho.plugins.XSLTResolverInterface;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;

import edu.ucsb.nceas.morpho.util.Log;



/**
 *  Plugin that provides a service to resolve DOCTYPES to XSLT stylesheets
 */
public class XSLTResolverPlugin implements  XSLTResolverInterface,
                                            PluginInterface, 
                                            ServiceProvider
{

    private final String CONFIG_KEY_GENERIC_STYLESHEET  = "genericStylesheet";
    private final String GENERIC_STYLESHEET;
    private final ConfigXML config;
    private final Hashtable mappings;

    private final ClassLoader classLoader;

    public XSLTResolverPlugin()
    {
        Log.debug(30, "XSLTResolverPlugin: ClassLoader *would* have been: " 
                            + this.getClass().getClassLoader().getClass().getName());
        
        classLoader = Morpho.class.getClassLoader();
        Log.debug(30, "XSLTResolverPlugin: ...but from Morpho, setting ClassLoader to: " 
                                                + classLoader.getClass().getName());
        Thread t = Thread.currentThread();
        t.setContextClassLoader(classLoader);        

        this.config = Morpho.getConfiguration();
        GENERIC_STYLESHEET = config.get(CONFIG_KEY_GENERIC_STYLESHEET, 0);
        mappings = new Hashtable();
        initMappings();
    }
    
    private void initMappings() 
    {
        Vector doctypes = config.get("****");
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
     *  @param docType unique DOCTYPE used to determine the stylesheet to return 
     *
     *  @return       a Reader for the character-based XSLT stylesheet. If a 
     *                stylesheet corresponding to the DOCID cannot be found, 
     *                a default or generic stylesheet may be returned. 
     *
     *  @throws DocumentNotFoundException if no suitable stylesheet is available
     */
    public Reader getXSLTStylesheetReader(String docType)
                                              throws DocumentNotFoundException
    {
        Log.debug(50, "\nXSLTResolver got: "+docType);
        Reader rdr = null;
        String xslPathString = (String)mappings.get(docType);
        if (xslPathString==null || xslPathString.trim().equals("")) {
        
            rdr =  new InputStreamReader(
                            classLoader.getResourceAsStream(GENERIC_STYLESHEET));
            Log.debug(50, "getXSLTStylesheetReader() failed to find valid "
                            +"stylesheet for docType: "+docType
                            +"\n returning default: "+GENERIC_STYLESHEET);
        } else {
            rdr =  new InputStreamReader(
                            classLoader.getResourceAsStream(xslPathString));
            Log.debug(50, "getXSLTStylesheetReader() found a value for the "
                            +"stylesheet for docType: "+docType
                            +"\n returning: "+xslPathString);
        }
        Log.debug(50, "\nXSLTResolver returning Reader: "+rdr);
        return rdr;
    }
}
//        if (docType.indexOf("entity")>0) {
//          rdr = new InputStreamReader(
//          classLoader.getResourceAsStream("style/eml-entity-2.0.0beta6.xsl"));
//        } else if (docType.indexOf("dataset")>0) {
//          rdr = new InputStreamReader(
//          classLoader.getResourceAsStream("style/eml-dataset-2.0.0beta6.xsl"));
//        } else if (docType.indexOf("attribute")>0) {
//          rdr = new InputStreamReader(
//          classLoader.getResourceAsStream("style/eml-attribute-2.0.0beta6.xsl"));
