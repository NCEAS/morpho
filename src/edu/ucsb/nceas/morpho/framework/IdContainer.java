/**
 *  '$RCSfile: IdContainer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-07 20:32:47 $'
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

package edu.ucsb.nceas.morpho.framework;

import javax.swing.*;
import javax.swing.border.*; 
import java.io.*;
import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
/**
 * This class implements accession number containment for Morpho.
 */
public class IdContainer
{
  private ConfigXML config;
  private String scope = new String();
  private int accNum;
  
  public IdContainer(ClientFramework framework)
  {
    config = framework.getConfiguration();
    Vector scopeV = config.get("scope");
    Vector accNumV = config.get("lastId");
    this.scope = (String)scopeV.elementAt(0);
    this.accNum = new Integer((String)accNumV.elementAt(0)).intValue();
    framework.debug(9, "scope: " + this.scope);
    framework.debug(9, "Accnum: " + this.accNum);
  }
  
  public String getScope()
  {
    return this.scope;
  }
  
  public int getAccNum()
  {
    return this.accNum;
  }
  
}
