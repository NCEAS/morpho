/**
 *  '$RCSfile: EditColumnMetaDataCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 22:03:01 $'
 * '$Revision: 1.24 $'
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

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
//import edu.ucsb.nceas.morpho.framework.EMLTransformToNewestVersionDialog;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.util.Vector;

import java.awt.event.ActionEvent;

import javax.swing.JTable;
import javax.swing.JOptionPane;

import org.w3c.dom.Node;

/**
 * Class to handle edit column meta data command
 */
public class EditColumnMetaDataCommand implements Command, DataPackageWizardListener 
{
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  private AbstractDataPackage adp = null;
  private DataViewer dataView = null;
  private int attrIndex = -1;
  private int entityIndex = -1;
  private JTable table = null;
  private DataViewContainerPanel resultPane = null;
  private OrderedMap map = null;
  private String columnName;
  private String mScale;

  private String xPath = "/attribute";

  /**
   * Constructor of edit column meta data command
   */
  public EditColumnMetaDataCommand()
  {

  }


  /**
   * execute edit column meta data command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event)
  {
    

    

    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    
    if (morphoFrame != null)
    {
       resultPane = morphoFrame.getDataViewContainerPanel();
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
          attrIndex = table.getSelectedColumn();
          entityIndex = dataView.getEntityIndex();
          //attributes = adp.getAttributeArray(entityIndex);
       }


    }//if
    
    //check if we should upgrade eml to the newest version
    EMLTransformToNewestVersionDialog dialog = null;
	try
	{
		  dialog = new EMLTransformToNewestVersionDialog(morphoFrame, this);
	}
	catch(Exception e)
	{
		  return;
	 }
	 if (dialog.getUserChoice() == JOptionPane.NO_OPTION)
	 {
		   // if user choose not transform it, stop the action.
			Log.debug(2,"The current EML document is not the latest version. You should transform it first!");
			return;
	 }
	

  } // end of execute
	
  /**
   * Method from DataPackageWizardListener.
   * When correction wizard finished, it will show the dialog.
   */
  public void wizardComplete(Node newDOM, String autoSavedID)
  {
	    Node[] attributes = null;
	  // since the morphoFrame may be updated to eml210 document, we need to get the adp again.
		morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		if (morphoFrame != null)
		{
		       resultPane = morphoFrame.getDataViewContainerPanel();
		 }//if

		  if ( resultPane != null)
		  {
		       adp = resultPane.getAbstractDataPackage();
		       dataView = resultPane.getCurrentDataViewer();
		       table = dataView.getDataTable();
		  }

		  if(adp == null) {
		      Log.debug(16, " Abstract Data Package is null in the EditColumnMetaDataCommand");
		      return;
		  }
		  
		  attributes = adp.getAttributeArray(entityIndex);

	    if(attributes == null || attrIndex == -1) {

	      Log.debug(16, " Couldnt get the attributes in	EditColumnMetaDataCommand for attrIndex = " + attrIndex);
	      return;
	    }

	    Node currentAttr = attributes[attrIndex];
	    map = XMLUtilities.getDOMTreeAsXPathMap(currentAttr,
	                    "/eml:eml/dataset/dataTable/attributeList");
	    ServiceController sc;
	    DataPackageWizardInterface dpwPlugin = null;
	    try {
	      sc = ServiceController.getInstance();
	      dpwPlugin = (DataPackageWizardInterface)sc.getServiceProvider(DataPackageWizardInterface.class);

	    } catch (ServiceNotHandledException se) {
	      Log.debug(6, se.getMessage());
	    }
	    if(dpwPlugin == null) return;

	    AbstractUIPage attributePage = dpwPlugin.getPage(DataPackageWizardInterface.ATTRIBUTE_PAGE);
	    boolean canHandleAllData = attributePage.setPageData(map, null);
			
			if(canHandleAllData) {
				
				ModalDialog wpd = new ModalDialog(attributePage,
													UIController.getInstance().getCurrentActiveWindow(),
													UISettings.POPUPDIALOG_WIDTH,
													UISettings.POPUPDIALOG_HEIGHT
													, false);
				
				wpd.setSize(UISettings.POPUPDIALOG_WIDTH, UISettings.POPUPDIALOG_FOR_ATTR_HEIGHT);
				wpd.validate();
				wpd.setVisible(true);
				if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {
					adp.setLocation("");
					resultPane.saveDataChanges();  // needed to flag datatable changes
					map = attributePage.getPageData(xPath);
					if(entityIndex == -1) {
						Log.debug(10, "Unable to get the Index of the current Entity, in EditColumnMetaData.");
						return;
					}
					
					columnName = AbstractDataPackage.getAttributeColumnName(map, xPath );
					mScale = AbstractDataPackage.getMeasurementScale(map, xPath);
					boolean toImport = AbstractDataPackage.isImportNeeded(map, xPath, mScale);
					if(toImport) {
						String entityName = adp.getEntityName(entityIndex);
						
						adp.addAttributeForImport(entityName, columnName, mScale, map, "/attribute", false);
						DataPackageWizardListener dpwListener = new DataPackageWizardListener () {
							public void wizardComplete(Node newDOM, String autoSavedID) {
								
								modifyAttribute();
								try
								{
									ServiceController services = ServiceController.getInstance();
									ServiceProvider provider =
									services.getServiceProvider(DataPackageInterface.class);
									DataPackageInterface dataPackageInt = (DataPackageInterface)provider;
									dataPackageInt.openNewDataPackage(adp, null);
								}
								catch (ServiceNotHandledException snhe)
								{
									Log.debug(6, snhe.getMessage());
								}
								UIController controller = UIController.getInstance();
								morphoFrame.setVisible(false);
								controller.removeWindow(morphoFrame);
								morphoFrame.dispose();
							}
							public void wizardCanceled() {
								
								return;
							}
						};
						int nextEntityIndex = adp.getEntityCount();
						Boolean beforeFlag = null;//this variable is for inserting, so we just use null here.
						dpwPlugin.startCodeDefImportWizard(dpwListener, nextEntityIndex, map, entityIndex, attrIndex, beforeFlag);
						
					} else { // if import is not needed
						
						modifyAttribute();
					}
					
				} // end of if USER_RESPONSE == OK_OPTION
				
			} else {
				
				if(entityIndex < 0) entityIndex = 0;
				if(attrIndex < 0) attrIndex = 0;
				
				UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
	          "dataTable["+entityIndex+"]/attribute-", attrIndex);
			}
  }
  
  /**
   * Method from DataPackageWizardListener. Do nothing.
   */
  public void wizardCanceled()
  {
	  Log.debug(45, "Correction wizard cancled");
	  
  }
	
  private void modifyAttribute()
  {

    // get the ID of old attribute and set it for the new one
		String oldID = adp.getAttributeID(entityIndex, attrIndex);
		if(oldID == null || oldID.trim().equals("")) oldID = UISettings.getUniqueID();
    map.put("/attribute/@id", oldID);

    Attribute attr = new Attribute(map);
    adp.insertAttribute(entityIndex, attr, attrIndex);
		adp.deleteAttribute(entityIndex, attrIndex + 1);

    String unit = getUnit(map, xPath);
		String sType = (String)map.get(xPath + "/storageType");
		if(sType == null) sType = mScale;
		
    // modify the
    String newHeader = "<html><font face=\"Courier\"><center><small>"+ sType +
    "</small><br><small>"+unit +"</small><br><b>"+
    columnName +"</b></center></font></html>";
    if(dataView != null) {

      Vector colLabels = dataView.getColumnLabels();
      colLabels.set(attrIndex, newHeader);

      PersistentVector pv = dataView.getPV();
      PersistentTableModel ptm = new PersistentTableModel(pv, colLabels);
      table.setModel(ptm);
      //DefaultListSelectionModel dlsm = new DefaultListSelectionModel();
      //dlsm.addSelectionInterval(attrIndex, attrIndex);
      table.setColumnSelectionInterval(attrIndex,	attrIndex);
      StateChangeEvent stateEvent = new
      StateChangeEvent(table,StateChangeEvent.SELECT_DATATABLE_COLUMN);
      StateChangeMonitor stateMonitor = StateChangeMonitor.getInstance();
      stateMonitor.notifyStateChange(stateEvent);

    }

  }//end of modifyAttribute

 
  private String getUnit(OrderedMap map, String xPath) {

    Object o1 = map.get(xPath + "/measurementScale/interval/unit/standardUnit");
    if(o1 != null) return (String)o1;
		o1 = map.get(xPath + "/measurementScale/interval/unit/customUnit");
    if(o1 != null) return (String)o1;
    o1 = map.get(xPath + "/measurementScale/ratio/unit/standardUnit");
    if(o1 != null) return (String)o1;
		o1 = map.get(xPath + "/measurementScale/ratio/unit/customUnit");
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
