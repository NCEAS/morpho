/**
 *  '$RCSfile: XSLTResolverPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-16 22:35:28 $'
 * '$Revision: 1.3 $'
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

import edu.ucsb.nceas.morpho.Morpho;

import edu.ucsb.nceas.morpho.framework.ConfigXML;

import edu.ucsb.nceas.morpho.plugins.XSLTResolverInterface;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;

import edu.ucsb.nceas.morpho.util.Log;

import java.io.Reader;
import java.io.InputStreamReader;


/**
 *  Plugin that provides a service to resolve DOCTYPES to XSLT stylesheets
 */
public class XSLTResolverPlugin implements  XSLTResolverInterface,
                                            PluginInterface, 
                                            ServiceProvider
{

    private final String CONFIG_KEY_GENERIC_STYLESHEET  = "genericStylesheet";
    private final String GENERIC_STYLESHEET;

    private final ClassLoader classLoader;

    private ConfigXML config = Morpho.getConfiguration();
        
    public XSLTResolverPlugin()
    {
        classLoader = this.getClass().getClassLoader();
        GENERIC_STYLESHEET = config.get(CONFIG_KEY_GENERIC_STYLESHEET, 0);
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
        
        //H A C K ! ! ! ! !
        //needs to be implemented properly. 
        
        if (docType.indexOf("entity")>0) {
          return new InputStreamReader(
          classLoader.getResourceAsStream("style/eml-entity-2.0.0beta6.xsl"));
        } else if (docType.indexOf("dataset")>0) {
          return new InputStreamReader(
          classLoader.getResourceAsStream("style/eml-dataset-2.0.0beta6.xsl"));
        } else if (docType.indexOf("attribute")>0) {
          return new InputStreamReader(
          classLoader.getResourceAsStream("style/eml-attribute-2.0.0beta6.xsl"));
        } else {
          return new InputStreamReader(
          classLoader.getResourceAsStream(GENERIC_STYLESHEET));
        }
    }
}
