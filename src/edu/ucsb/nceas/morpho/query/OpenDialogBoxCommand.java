/**
 *  '$RCSfile: OpenDialogBoxCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-29 06:04:36 $'
 * '$Revision: 1.16 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;

import java.util.Vector;

import java.awt.event.ActionEvent;


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

  }//OpenDialogBoxCommand


  /**
   * execute cancel command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event)
  {
    // create ownerQuery depend on the suiation when it executed
    ownerQuery = new Query(getOwnerQuery(), morpho);
    // Get the current morphoFrame. Maybe change get open dialog parent
    MorphoFrame frame =
                    UIController.getInstance().getCurrentActiveWindow();

    // Open a open dialog
    if ( frame != null)
    {
      doOpenDialog(frame);
    }


  }//execute


  /**
   * Using SwingWorket class to open open dialog
   *
   * @param morphoFrame MorphoFrame
   */
  private void doOpenDialog(final MorphoFrame morphoFrame)
  {

    final SwingWorker worker = new SwingWorker()
    {
        OpenDialogBox open = null;
        public Object construct()
        {
          // set frame butterfly flapping
          morphoFrame.setBusy(true);
          morphoFrame.setEnabled(false);
          open = new OpenDialogBox(morphoFrame, morpho, ownerQuery);
          return null;
        }

        //Runs on the event-dispatching thread.
        public void finished()
        {
          morphoFrame.setEnabled(true);
          morphoFrame.setBusy(false);
          // Set the open dialog box modal true
          if (open!=null) {
              open.setModal(true);
              open.setVisible(true);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
  }//doOpenDialog


  /**
   * Construct a query suitable for getting the owner documents
   *
   * @return String
   */
  protected String getOwnerQuery()
  {
  	ConfigXML config = Morpho.getConfiguration();
  	ConfigXML profile = morpho.getProfile();
    StringBuffer searchtext = new StringBuffer();
    searchtext.append("<?xml version=\"1.0\"?>\n");
    searchtext.append("<pathquery version=\"1.0\">\n");
    String lastname = profile.get("lastname", 0);
    String firstname = profile.get("firstname", 0);
    searchtext.append("<querytitle>My Data (" + firstname + " " + lastname);
    searchtext.append(")</querytitle>\n");
    Vector returnDoctypeList = config.get("returndoc");
    for (int i=0; i < returnDoctypeList.size(); i++) {
      searchtext.append("<returndoctype>");
      searchtext.append((String)returnDoctypeList.elementAt(i));
      searchtext.append("</returndoctype>\n");
    }
    Vector returnFieldList = config.get("returnfield");
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
