/**
 *  '$RCSfile: MetaDisplayPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-12-12 18:14:34 $'
 * '$Revision: 1.6 $'
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

package edu.ucsb.nceas.morpho.plugins.metadisplay;

import java.util.List;
import java.util.ArrayList;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayFactoryInterface;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Log;


/**
 *  Plugin that builds a display panel to display metadata.  Given a String ID, 
 *  does a lookup using a factory that must also be provided (and which 
 *  implements the ContentFactoryInterface) to get the XML document to display.  
 *  Then styles this document accordingly using XSLT, before displaying it in an 
 *  embedded HTML display.
 */
public class MetaDisplayPlugin implements   PluginInterface, 
                                            ServiceProvider,
                                            MetaDisplayFactoryInterface
{

    private static List displayList;
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
          services.addService(MetaDisplayFactoryInterface.class, this);
          Log.debug(20, "Service added: MetaDisplayFactoryInterface.");
        } 
        catch (ServiceExistsException see)
        {
          Log.debug(6, 
                    "Service registration failed: MetaDisplayFactoryInterface");
          Log.debug(6, see.toString());
        }
        displayList = new ArrayList();
    }
     
    
    /**
     *  Required by MetaDisplayFactoryInterface:
     *  Returns a new instance of an object that implements the 
     *  <code>MetaDisplayInterface</code>
     *
     *  @return     new instance of an object that implements the 
     *              <code>MetaDisplayInterface</code>
     */
    public MetaDisplayInterface getInstance() 
    {
        MetaDisplay display = new MetaDisplay();
        displayList.add(display);
        return display;
    }
    
    
    /**
     *  Required by MetaDisplayFactoryInterface:
     *  Returns a reference to an existing object that implements the 
     *  <code>MetaDisplayInterface</code>. The object is identified by the int
     *  index assigned to it in the getInstance() method at the time of creation
     *
     *  @param displayNum   int index assigned to the object in the 
     *                      getInstance() method at the time of creation
     *
     *  @return             reference to an existing object that implements the 
     *                      <code>MetaDisplayInterface</code> identified by 
     *                      displayNum.  Returns NULL if displayNum out of range
     */
    public MetaDisplayInterface getMetaDisplay(int displayNum)
    {
        if (displayList.size() < displayNum+1) return null;
        return (MetaDisplayInterface)(displayList.get(displayNum));
    }
    
}
