/**
 *  '$RCSfile: ImportDataCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 22:03:01 $'
 * '$Revision: 1.22 $'
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

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * Class represents a command to import an external EML file to morpho system.
 */
public class ImportEMLFileCommand implements Command
{
  private File lastChosenDir = null;
 

  /**
   * Constructor of Import data command
   */
  public ImportEMLFileCommand()
  {

  }//RefreshCommand


  /**
   * Execute import command. User will choose a eml file from local file system and 
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event)
  {
    JFileChooser fileChooser = new JFileChooser();
    if(lastChosenDir != null)
    {
      fileChooser.setCurrentDirectory(lastChosenDir);
    }
    int returnValue = fileChooser.showOpenDialog(UIController.getInstance().getCurrentActiveWindow());
    if(returnValue == JFileChooser.APPROVE_OPTION)
    {
      File emlFile = fileChooser.getSelectedFile();
      if(emlFile != null)
      {
        lastChosenDir = emlFile.getParentFile();
      }
      Log.debug(30, "The chooser file is "+emlFile.getAbsolutePath());
      try
      {
        AbstractDataPackage dataPackage = DataPackageFactory.getDataPackage(emlFile);
        if(dataPackage == null)
        {
          throw new Exception("Morpho couldn't create a data package from the given file.");
        }
        else
        {
          //serialize local data file into morpho first
          dataPackage.serializeDataInImportExternalEMLFile();
          //given a new id to this dataPackage
          AccessionNumber an = new AccessionNumber(Morpho.thisStaticInstance);
          String identifier = an.getNextId();
          dataPackage.setAccessionNumber(identifier);
          //serialize metadata to local 
          dataPackage.serialize(AbstractDataPackage.LOCAL);
          //dataPackage.setLocation(AbstractDataPackage.LOCAL);
          //open the package.
          DataPackagePlugin dataPackagePlugin = new DataPackagePlugin(Morpho.thisStaticInstance);
          dataPackagePlugin.openDataPackage(AbstractDataPackage.LOCAL, identifier, null, null, null);
        }
      }
      catch(Exception e)
      {
        Log.debug(5, "Couldn't Import the file "+emlFile.getAbsolutePath()+"into morpho.\n"+
                            e.getMessage());
      }
      
    }
    else
    {
      Log.debug(30, "File choosing was canceled in ImportEMLFileCommand.exectue()");
    }
  }//execute
  
  

}

