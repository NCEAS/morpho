/**
 *  '$RCSfile: EditColumnMetaDataCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2003-12-24 04:24:32 $'
 * '$Revision: 1.4 $'
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
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.DataPackageWizardPlugin;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributePage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.StoreStateChangeEvent;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;
import java.util.Iterator;
import javax.swing.JTable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.ucsb.nceas.utilities.XMLUtilities;
import edu.ucsb.nceas.utilities.OrderedMap;

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
		AbstractDataPackage adp = null;
		DataViewer dataView = null;
		Node[] attributes = null;
		int selectedCol = -1;
		int entityIndex = -1;
		JTable table = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if (morphoFrame != null)
    {
       resultPane = AddDocumentationCommand.
                          getDataViewContainerPanelFromMorphoFrame(morphoFrame);
    }//if
    
		if ( resultPane != null)
    {
       adp = resultPane.getAbstractDataPackage();
		}
		
		if(adp == null) {
			Log.debug(16, " Abstract Data Package is null in the EditColumnMetaDataCommand");
			return;
		}
		
		// make sure resulPanel is not null
    if (resultPane != null)
    {
       dataView = resultPane.getCurrentDataViewer();
       if (dataView != null)
       {
         	
					//DataPackage dataPackage = resultPane.getDataPackage();
					String entityId = dataView.getEntityFileId();
					table = dataView.getDataTable();
					selectedCol = table.getSelectedColumn();
					entityIndex = dataView.getEntityIndex();
					attributes = adp.getAttributeArray(entityIndex);
       }
       
			 
    }//if
		
		if(attributes == null || selectedCol == -1)
		{
			Log.debug(16, " Couldnt get the attributes in	EditColumnMetaDataCommand for selectedCol = " + selectedCol);
			return;
		}
		
		Node currentAttr = attributes[selectedCol];
		OrderedMap map = XMLUtilities.getDOMTreeAsXPathMap(currentAttr, 
										"/eml:eml/dataset/dataTable/attributeList");
		
		ServiceController sc;
		DataPackageWizardPlugin dpwPlugin = null;
		try {
			sc = ServiceController.getInstance();
			dpwPlugin = (DataPackageWizardPlugin)sc.getServiceProvider(DataPackageWizardInterface.class);
		} catch (ServiceNotHandledException se) {
			Log.debug(6, se.getMessage());
		}
		if(dpwPlugin == null) 
			return;
		AttributePage attributePage = (AttributePage)dpwPlugin.getPage(DataPackageWizardInterface.ATTRIBUTE_PAGE);
		attributePage.setPageData(map);
		
		WizardPopupDialog wpd = new WizardPopupDialog(attributePage, morphoFrame, false);
		attributePage.refreshUI();
		wpd.setSize(WizardSettings.DIALOG_WIDTH, WizardSettings.ATTR_DIALOG_HEIGHT);
		wpd.validate();
		wpd.setVisible(true);
		
		if (wpd.USER_RESPONSE == WizardPopupDialog.OK_OPTION) {
			map = attributePage.getPageData("/attribute");
			if(entityIndex == -1) {
				Log.debug(10, "Unable to get the Index of the current Entity, in EditColumnMetaData.");
				return;
			}
				
			Attribute attr = new Attribute(map);
			adp.deleteAttribute(entityIndex, selectedCol);
			adp.insertAttribute(entityIndex, attr, selectedCol);
			
			// modify the 
			String newHeader = getColumnName(map);
			if (newHeader.trim().length()==0) newHeader = "New Column";
			String type = getMeasurementScale(map);
			String unit = getUnit(map);
				newHeader = "<html><font face=\"Courier\"><center><small>"+type+
				"<br>"+unit +"</small><br><b>"+
				newHeader+"</b></center></font></html>";
			if(dataView != null) {
				
				Vector colLabels = dataView.getColumnLabels();
				colLabels.set(selectedCol, newHeader);
				
				PersistentVector pv = dataView.getPV();
				PersistentTableModel ptm = new PersistentTableModel(pv, colLabels);
				table.setModel(ptm);
				//DefaultListSelectionModel dlsm = new DefaultListSelectionModel();
				//dlsm.addSelectionInterval(selectedCol, selectedCol);
				table.setColumnSelectionInterval(selectedCol,	selectedCol);
				StateChangeEvent stateEvent = new 
              StateChangeEvent(table,StateChangeEvent.SELECT_DATATABLE_COLUMN);
        StateChangeMonitor stateMonitor = StateChangeMonitor.getInstance();
        stateMonitor.notifyStateChange(stateEvent);
			}
		} else {
			
		}
  
  }//execute
  
  private String getColumnName(OrderedMap map) {
		
		Object o1 = map.get("/attribute/attributeName");
		if(o1 == null) return "";
		else return (String) o1;                       
	}
	
	private String getMeasurementScale(OrderedMap map) {
		
		Object o1 = map.get("/attribute/measurementScale/nominal/nonNumericDomain/enumeratedDomain[1]/codeDefinition[1]/code");
		if(o1 != null) return "Nominal";
		o1 = map.get("/attribute/measurementScale/nominal/nonNumericDomain/textDomain[1]/definition");
		if(o1 != null) return "Nominal";
		
		o1 = map.get("/attribute/measurementScale/ordinal/nonNumericDomain/enumeratedDomain[1]/codeDefinition[1]/code");
		if(o1 != null) return "Ordinal";
		o1 = map.get("/attribute/measurementScale/ordinal/nonNumericDomain/textDomain[1]/definition");
		if(o1 != null) return "Ordinal";
		
		o1 = map.get("/attribute/measurementScale/interval/unit/standardUnit");
		if(o1 != null) return "Interval";
		o1 = map.get("/attribute/measurementScale/ratio/unit/standardUnit");
		if(o1 != null) return "Ratio";
		
		o1 = map.get("/attribute/measurementScale/datetime/formatString");
		if(o1 != null) return "Datetime";
		
		return "";
	}
	
	private String getUnit(OrderedMap map) {
		
		Object o1 = map.get("/attribute/measurementScale/interval/unit/standardUnit");
		if(o1 != null) return (String)o1;
		o1 = map.get("/attribute/measurementScale/ratio/unit/standardUnit");
		if(o1 != null) return (String)o1;
		return "";
	}
  
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
