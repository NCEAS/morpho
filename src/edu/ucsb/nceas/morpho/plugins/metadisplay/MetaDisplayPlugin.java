/**
 *  '$RCSfile: MetaDisplayPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-16 00:21:09 $'
 * '$Revision: 1.1 $'
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

import edu.ucsb.nceas.morpho.framework.ClientFramework;
import edu.ucsb.nceas.morpho.framework.PluginInterface;
import edu.ucsb.nceas.morpho.framework.ServiceProvider;

import java.awt.Component;

import javax.swing.JLabel;
/**
 * This exception is thrown when a requested document cannot be found
 */
public class MetaDisplayPlugin implements DisplayInterface, 
                                          PluginInterface, ServiceProvider
{
  public MetaDisplayPlugin()
  {
  }
  
  public Component getDisplayComponent()  
  {
    return new JLabel("Not Yet Implemented");
  }
  
  public void initialize(ClientFramework framework) 
  {
  }
}
