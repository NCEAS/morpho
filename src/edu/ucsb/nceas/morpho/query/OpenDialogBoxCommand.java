/**
 *  '$RCSfile: OpenDialogBoxCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-21 18:35:34 $'
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
package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import java.util.Vector;
import javax.swing.JDialog;


/**
 * Class to handle Open a dialog box command
 */
public class OpenDialogBoxCommand implements Command 
{
  
  /** A reference to Morpho application */
  private Morpho morpho = null;
  
  /** A reference to the owner query*/
  private Query ownerQuery = null;
  
  /**
   * Constructor of SearchCommand
   *
   * @param morpho the Morpho app to which the cancel command will apply
   */
  public OpenDialogBoxCommand(Morpho morpho)
  {
    this.morpho = morpho;
    ownerQuery = new Query(getOwnerQuery(), morpho);
    
  }//OpenDialogBoxCommand
  
  
  /**
   * execute cancel command
   */    
  public void execute()
  {
    OpenDialogBox open = null;
    open = new OpenDialogBox(morpho, ownerQuery);
    // Set the open dialog box modal true
    open.setModal(true);
    open.setVisible(true);
   
  }//execute

  /**
   * Construct a query suitable for getting the owner documents
   */
  private String getOwnerQuery()
  {
    ConfigXML profile = morpho.getProfile();
    StringBuffer searchtext = new StringBuffer();
    searchtext.append("<?xml version=\"1.0\"?>\n");
    searchtext.append("<pathquery version=\"1.0\">\n");
    String lastname = profile.get("lastname", 0);
    String firstname = profile.get("firstname", 0);
    searchtext.append("<querytitle>My Data (" + firstname + " " + lastname);
    searchtext.append(")</querytitle>\n");
    Vector returnDoctypeList = profile.get("returndoc");
    for (int i=0; i < returnDoctypeList.size(); i++) {
      searchtext.append("<returndoctype>");
      searchtext.append((String)returnDoctypeList.elementAt(i));
      searchtext.append("</returndoctype>\n");
    }
    Vector returnFieldList = profile.get("returnfield");
    for (int i=0; i < returnFieldList.size(); i++) {
      searchtext.append("<returnfield>");
      searchtext.append((String)returnFieldList.elementAt(i));
      searchtext.append("</returnfield>\n");
    }
    searchtext.append("<owner>" + morpho.getUserName() + "</owner>\n");
    searchtext.append("<querygroup operator=\"UNION\">\n");
    searchtext.append("<queryterm casesensitive=\"true\" ");
    searchtext.append("searchmode=\"contains\">\n");
    searchtext.append("<value>%</value>\n");
    searchtext.append("</queryterm></querygroup></pathquery>");

    return searchtext.toString();
  } 
  
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class OpenDialogBoxCommand
