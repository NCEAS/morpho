/**
 *  '$RCSfile: CreateNewDataPackageCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-26 20:17:05 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.wizard.PackageWizardShell;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
/**
 * Class to handle create new data package command
 */
public class CreateNewDataPackageCommand implements Command 
{
  
  private Morpho morpho = null;
  /**
   * Constructor of CreateNewDataPackageCommand
   * @param morpho the morpho will apply to this command
   */
  public CreateNewDataPackageCommand(Morpho morpho)
  {
    this.morpho = morpho;
 
  }//RefreshCommand
  
  
  
  
  /**
   * execute create data package  command
   */    
  public void execute(ActionEvent event)
  {   
     Log.debug(20, "Action fired: New Data Package");
     final PackageWizardShell pws = new PackageWizardShell(morpho);
     pws.setName("Package Wizard");
     //MBJ framework.addWindow(pws);
     pws.addWindowListener(new WindowAdapter()
     {
       public void windowClosed(WindowEvent e)
       {
            //MBJ framework.removeWindow(pws);
        }
          
        public void windowClosing(WindowEvent e)
        {
            //MBJ framework.removeWindow(pws);
        }
     });
     pws.show();
        
  }//execute
  
 

 
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
