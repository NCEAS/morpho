package edu.ucsb.nceas.morpho.plugins;

import java.util.Vector;

import javax.swing.JTable;

import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.Attribute;
import edu.ucsb.nceas.morpho.datapackage.DataViewer;
import edu.ucsb.nceas.morpho.datapackage.PersistentTableModel;
import edu.ucsb.nceas.morpho.datapackage.PersistentVector;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;

/**
 * This class represent a listener which will be passed to CodeDefintionWizard when
 * user edits an attribute.
 * @author tao
 *
 */
public class EditingAttributeImportWizardListener implements DataPackageWizardListener 
{
  
  private MorphoFrame morphoFrame = null;
  private AbstractDataPackage adp = null;
  

  public void wizardComplete(Node newDOM, String autoSavedID) 
  {
    
     //modifyAttribute();
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
  
  
  public void wizardCanceled() 
  {
    return;
  }
  
  public static void modifyAttribute(AbstractDataPackage adp, DataViewer dataView ,
                                        int enIndex, int attrIndex, OrderedMap map, String xPath)
  {
    JTable table = dataView.getDataTable();
    // get the ID of old attribute and set it for the new one
    String oldID = adp.getAttributeID(enIndex, attrIndex);
    if(oldID == null || oldID.trim().equals("")) oldID = UISettings.getUniqueID();
    map.put("/attribute/@id", oldID);

    Attribute attr = new Attribute(map);
    adp.insertAttribute(enIndex, attr, attrIndex);
    adp.deleteAttribute(enIndex, attrIndex + 1);

    String unit = getUnit(map, xPath);
    String sType = (String)map.get(xPath + "/storageType");
    if(sType == null) sType = "";
    String columnName = AbstractDataPackage.getAttributeColumnName(map, xPath );
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
      table.setColumnSelectionInterval(attrIndex, attrIndex);
      StateChangeEvent stateEvent = new
      StateChangeEvent(table,StateChangeEvent.SELECT_DATATABLE_COLUMN);
      StateChangeMonitor stateMonitor = StateChangeMonitor.getInstance();
      stateMonitor.notifyStateChange(stateEvent);

    }

  }//end of modifyAttribute
  
  private static String getUnit(OrderedMap map, String xPath) {

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
}
