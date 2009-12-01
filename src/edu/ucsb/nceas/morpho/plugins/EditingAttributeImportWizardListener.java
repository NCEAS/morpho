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
  private OrderedMap map = null;
  private int entityIndex = -1;
  private int attributeIndex = -1;
  private String xPath = "/attribute";
  

  /**
   * Constructor
   * @param morphoFrame the frame which user starts to edit an attribute
   * @param adp the abstract data package which contains the attribute
   * @param map the ordered map will contains the edited data
   * @param entityIndex the index of entity which contains the editing attribute
   * @param attributeIndex the index of editing attribute
   */
  public EditingAttributeImportWizardListener(MorphoFrame morphoFrame, AbstractDataPackage adp, 
                                                                OrderedMap map,  int entityIndex, int attributeIndex) throws Exception
  {
    this.morphoFrame = morphoFrame;
    this.adp = adp;
    if(morphoFrame == null || adp == null)
    {
      throw new Exception("The morpho frame or dataPackage is null in EditingAttributeImportWizardListener");
    }
    this.map = map;
    this.entityIndex = entityIndex;
    this.attributeIndex = attributeIndex;
    //Log.debug(5, "in constructor the entity index and attribute index are "+this.entityIndex+ " and "+this.attributeIndex);
  }
  
 
  /**
   * Inheritance method from  DataPackageWizardListener.
   * It will be called when wizard is done.
   */
  public void wizardComplete(Node newDOM, String autoSavedID) 
  {
    
    DataViewer dataView = morphoFrame.getDataViewContainerPanel().getCurrentDataViewer();
     modifyAttribute(adp, dataView , entityIndex, attributeIndex, map, xPath);
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
  
  
  /**
   * Inheritance method from  DataPackageWizardListener.
   * It will be called when wizard is canceled.
   */
  public void wizardCanceled() 
  {
    return;
  }
  
  
  /**
   * Modify the attribute with new data
   * @param adp the abstract data package contains the attribute
   * @param dataView dataView of the entity
   * @param enIndex index of the entity
   * @param attrIndex index of the attribute
   * @param map new attribute data
   * @param xPath path to get data from ordered map
   */
  public static void modifyAttribute(AbstractDataPackage adp, DataViewer dataView ,
                                        int enIndex, int attrIndex, OrderedMap map, String xPath) 
  {
    if(adp != null && map != null)
    {
      JTable table = dataView.getDataTable();
      // get the ID of old attribute and set it for the new one
      String oldID = adp.getAttributeID(enIndex, attrIndex);
      if(oldID == null || oldID.trim().equals("")) oldID = UISettings.getUniqueID();
      map.put("/attribute/@id", oldID);
    
      Attribute attr = new Attribute(map);
      //Log.debug(5, "The entity index is "+enIndex+" and attribute index is "+attrIndex);
      adp.insertAttribute(enIndex, attr, attrIndex);
      adp.deleteAttribute(enIndex, attrIndex + 1);
    
      String unit = getUnit(map, xPath);
      String sType = (String)map.get(xPath + "/storageType");
      String mScale = AbstractDataPackage.getMeasurementScale(map, xPath);
      if(sType == null) sType = mScale;
      String columnName = AbstractDataPackage.getAttributeColumnName(map, xPath );
      // modify the
      String newHeader = "<html><font face=\"Courier\"><center><small>"+ sType +
      "</small><br><small>"+unit +"</small><br><b>"+
      columnName +"</b></center></font></html>";
      if(dataView != null) 
      {
    
        Vector colLabels = dataView.getColumnLabels();
        colLabels.set(attrIndex, newHeader);
        if(table != null)
        {
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
      }
    }
    else
    {
       Log.debug(5, "The data map or abstract data package is null in EditingAttributeImportListener.modifyAttribute");
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
