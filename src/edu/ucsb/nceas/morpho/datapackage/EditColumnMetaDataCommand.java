/**
 *  '$RCSfile: EditColumnMetaDataCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-12-15 21:03:04 $'
 * '$Revision: 1.2 $'
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
import edu.ucsb.nceas.morpho.framework.EditorInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;
import javax.swing.JTable;
import org.w3c.dom.Document;


/**
 * Class to handle edit column meta data command
 */
public class EditColumnMetaDataCommand implements Command 
{
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;
  
 
  /**
   * Constructor of edit column meta data command
   */
  public EditColumnMetaDataCommand()
  {
  
  }

  /**
   * execute edit column meta data command
   */    
  public void execute(ActionEvent event)
  {   
    DataViewContainerPanel resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if (morphoFrame != null)
    {
       resultPane = AddDocumentationCommand.
                          getDataViewContainerPanelFromMorphoFrame(morphoFrame);
    }//if
    
    // make sure resulPanel is not null
    if (resultPane != null)
    {
       DataViewer dataView = resultPane.getCurrentDataViewer();
       if (dataView != null)
       {
         // Get parameters and run it
//         DataPackage dataPackage = resultPane.getDataPackage();
//         String entityId = dataView.getEntityFileId();
//         edit(dataPackage, dataView, entityId);
       }
       
    }//if
  
  }//execute
  
  
  
  /* Method to run edit cloumn meta data */
/*DFH need to rewrite
  private void edit(DataPackage dp, DataViewer thisRef, String entityFileId)
  {  
        EditorInterface editor = null;
        String id = dp.getAttributeFileId(entityFileId);
        try
        {
          ServiceController services = ServiceController.getInstance();
          ServiceProvider provider = 
                        services.getServiceProvider(EditorInterface.class);
          editor = (EditorInterface)provider;
        }
        catch(Exception ee)
        {
          Log.debug(0, "Error acquiring editor plugin: " + ee.getMessage());
          ee.printStackTrace();
          return;
        }
        
        StringBuffer sb = new StringBuffer();
        Reader reader = null;
        try 
        {
          reader = dp.openAsReader(id);
          char[] buff = new char[4096];
          int numCharsRead;
      
          while ((numCharsRead = reader.read( buff, 0, buff.length ))!=-1) 
          {
            sb.append(buff, 0, numCharsRead);
          }
        } 
        catch (DocumentNotFoundException dnfe) 
        {
          Log.debug(0, "Error finding file : "+id+" "+dnfe.getMessage());
          return;
        } 
        catch (IOException ioe) 
        {
          Log.debug(0, "Error reading file : "+id+" "+ioe.getMessage());
        } 
        finally 
        {
          try 
          { 
            reader.close();
          } 
          catch (IOException ce) 
          {  
            Log.debug(12, "Error closing Reader : "+id+" "+ce.getMessage());
          }
        }
        
        editor.openEditor(sb.toString(), id, dp.getLocation(), thisRef);
  }//edit
*/
 
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
