/**
 *  '$RCSfile: EditColumnMetaDataCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-04-17 02:22:12 $'
 * '$Revision: 1.14 $'
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

import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributePage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CodeImportPage;
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

import org.w3c.dom.Node;

/**
 * Class to handle edit column meta data command
 */
public class EditColumnMetaDataCommand implements Command
{
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  private AbstractDataPackage adp = null;
  private DataViewer dataView = null;
  private int attrIndex = -1;
  private int entityIndex = -1;
  private JTable table = null;
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
    DataViewContainerPanel resultPane = null;

    Node[] attributes = null;

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
          attributes = adp.getAttributeArray(entityIndex);
       }


    }//if

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

    AttributePage attributePage = (AttributePage)dpwPlugin.getPage(DataPackageWizardInterface.ATTRIBUTE_PAGE);
    attributePage.setPageData(map, null);
    String firstKey = (String)map.keySet().iterator().next();

    ModalDialog wpd = new ModalDialog(attributePage,
                                UIController.getInstance().getCurrentActiveWindow(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT
                                , false);
    attributePage.refreshUI();
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

      columnName = getColumnName(map, xPath );
      mScale = getMeasurementScale(map, xPath);

      if(attributePage.isImportNeeded()) {
        CodeImportPage codeImportPage = (CodeImportPage)dpwPlugin.getPage(DataPackageWizardInterface.CODE_IMPORT_PAGE);
        String entityName = adp.getEntityName(entityIndex);

        codeImportPage.addAttributeForImport(entityName, columnName, mScale, map, "/attribute", false);
        DataPackageWizardListener dpwListener = new DataPackageWizardListener () {
          public void wizardComplete(Node newDOM) {

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
        dpwPlugin.startCodeDefImportWizard(dpwListener);

      } else { // if import is not needed

        modifyAttribute();
      }

    } // end of if USER_RESPONSE == OK_OPTION

  } // end of execute

  private void modifyAttribute()
  {

    // get the ID of old attribute and set it for the new one
    map.put("/attribute/@id", adp.getAttributeID(entityIndex, attrIndex));

    Attribute attr = new Attribute(map);
    adp.deleteAttribute(entityIndex, attrIndex);
    adp.insertAttribute(entityIndex, attr, attrIndex);

    String unit = getUnit(map, xPath);

    // modify the
    String newHeader = "<html><font face=\"Courier\"><center><small>"+ mScale +
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

  private String getColumnName(OrderedMap map, String xPath) {

    Object o1 = map.get(xPath + "/attributeName");
    if(o1 == null) return "";
    else return (String) o1;
  }

  private String getMeasurementScale(OrderedMap map, String xPath) {

    Object o1 = map.get(xPath + "/measurementScale/nominal/nonNumericDomain/enumeratedDomain[1]/codeDefinition[1]/code");
    if(o1 != null) return "Nominal";
    boolean b1 = map.containsKey(xPath + "/measurementScale/nominal/nonNumericDomain/enumeratedDomain[1]/entityCodeList/entityReference");
    if(b1) return "Nominal";
    o1 = map.get(xPath + "/measurementScale/nominal/nonNumericDomain/textDomain[1]/definition");
    if(o1 != null) return "Nominal";

    o1 = map.get(xPath + "/measurementScale/ordinal/nonNumericDomain/enumeratedDomain[1]/codeDefinition[1]/code");
    if(o1 != null) return "Ordinal";
    b1 = map.containsKey(xPath + "/measurementScale/ordinal/nonNumericDomain/enumeratedDomain[1]/entityCodeList/entityReference");
    if(b1) return "Ordinal";
    o1 = map.get(xPath + "/measurementScale/ordinal/nonNumericDomain/textDomain[1]/definition");
    if(o1 != null) return "Ordinal";

    o1 = map.get(xPath + "/measurementScale/interval/unit/standardUnit");
    if(o1 != null) return "Interval";
		o1 = map.get(xPath + "/measurementScale/interval/unit/customUnit");
    if(o1 != null) return "Interval";
		
    o1 = map.get(xPath + "/measurementScale/ratio/unit/standardUnit");
    if(o1 != null) return "Ratio";
		o1 = map.get(xPath + "/measurementScale/ratio/unit/customUnit");
    if(o1 != null) return "Ratio";

    o1 = map.get(xPath + "/measurementScale/datetime/formatString");
    if(o1 != null) return "Datetime";

    return "";
  }

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
