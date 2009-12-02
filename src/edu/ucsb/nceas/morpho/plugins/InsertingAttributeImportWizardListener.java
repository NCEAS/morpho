package edu.ucsb.nceas.morpho.plugins;

import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.Attribute;
import edu.ucsb.nceas.morpho.datapackage.DataViewer;
import edu.ucsb.nceas.morpho.datapackage.DelimiterField;
import edu.ucsb.nceas.morpho.datapackage.PersistentTableModel;
import edu.ucsb.nceas.morpho.datapackage.PersistentVector;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;

/**
 * This class represent a listener which will be passed to CodeDefintionWizard when
 * user inserts a new attribute.
 * @author tao
 *
 */
public class InsertingAttributeImportWizardListener implements DataPackageWizardListener 
{
  
  private MorphoFrame morphoFrame = null;
  private AbstractDataPackage adp = null;
  private WizardContainerFrame wizardFrame = null;
  private int entityIndex = -1;
  private int selectedAttributeIndex = -1;
  private String xPath = "/attribute";
  private boolean beforeFlag = true;
  

  /**
   * Constructor
   * @param morphoFrame the frame which user starts to edit an attribute
   * @param adp the abstract data package which contains the attribute
   * @param map the ordered map will contains the edited data
   * @param entityIndex the index of entity which contains the editing attribute
   * @param attributeIndex the index of editing attribute
   * @param beforeFalg the new column is before the selection of not
   */
  public InsertingAttributeImportWizardListener(MorphoFrame morphoFrame, AbstractDataPackage adp, 
                                                                WizardContainerFrame wizardFrame,  int entityIndex, int attributeIndex, 
                                                                Boolean beforeFlag) throws Exception
  {
    this.morphoFrame = morphoFrame;
    this.adp = adp;
    if(morphoFrame == null || adp == null || wizardFrame == null)
    {
      throw new Exception("The morpho frame or dataPackage is null in InsertingAttributeImportWizardListener");
    }
    this.wizardFrame = wizardFrame;
    this.entityIndex = entityIndex;
    this.selectedAttributeIndex = attributeIndex;
    if(beforeFlag != null)
    {
      this.beforeFlag = beforeFlag.booleanValue();
    }
    //Log.debug(5, "in constructor the entity index and attribute index are "+this.entityIndex+ " and "+this.attributeIndex);
  }
  
 
  /**
   * Inheritance method from  DataPackageWizardListener.
   * It will be called when wizard is done.
   */
  public void wizardComplete(Node newDOM, String autoSavedID) 
  {
    
    DataViewer dataView = morphoFrame.getDataViewContainerPanel().getCurrentDataViewer();
    OrderedMap map = wizardFrame.getEditingAttributeMap(); 
    insertEml2Column(adp, dataView , entityIndex, selectedAttributeIndex, map, xPath, beforeFlag);
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
   * Methods inherits from DataPackageWizardListener.
   */
  public void wizardSavedForLater()
  {
    if(morphoFrame != null)
    {
      morphoFrame.setVisible(false);
      UIController controller = UIController.getInstance();
      controller.removeWindow(morphoFrame);
      morphoFrame.dispose();
    }
    Log.debug(45, "\n\n********** Wizard saved for later!");
  }
  
  
  /*
   * Inserts a column in both data package and gui
   */
  private void insertEml2Column(AbstractDataPackage adp, DataViewer dataView ,
      int enIndex, int selectedCol, OrderedMap map, String xPath, boolean beforeFlag)
  {

    if(selectedCol < 0) {
      Log.debug(10, "Error inserting Column. selected Column is " + selectedCol);
      return;
    }
    
    JTable table = null;
    PersistentTableModel ptm = null;
    String fieldDelimiter = null;
    PersistentVector pv = null;
    Vector columnLabels = null;    
    if (dataView != null)
    {
       // Get parameters and run it
       table=dataView.getDataTable();
       ptm = (PersistentTableModel)table.getModel();
       pv = dataView.getPV();
       Morpho morph=dataView.getMorpho();
       columnLabels=dataView.getColumnLabels();
       fieldDelimiter= pv.getFieldDelimiter();
    }
    else
    {
      Log.debug(5, "The data view from Morpho frame is null");
      return;
    }

    try
    {
      if (beforeFlag)
      {
        insertNewAttributeAt(adp, map, selectedCol, dataView, enIndex);
      }
      else
      {
        insertNewAttributeAt(adp, map, (selectedCol+1), dataView, enIndex);
      }

    }//try
    catch (Exception w)
    {
      Log.debug(20, "Exception trying to modify attribute DOM");
      return;
    }//catch


    String columnName = getColumnName(map, xPath );
    String newHeader = columnName;
    if (newHeader.trim().length()==0) newHeader = "New Column";
    String unit = EditingAttributeImportWizardListener.getUnit(map, xPath);
    String sType = (String)map.get(xPath + "/storageType");
    String mScale = AbstractDataPackage.getMeasurementScale(map, xPath);
    if(sType == null) sType = mScale;
    
    newHeader = "<html><font face=\"Courier\"><center><small>"+ sType+
    "<br>"+unit +"</small><br><b>"+
    newHeader+"</b></center></font></html>";
    if (beforeFlag)
    {
      columnLabels.insertElementAt(newHeader, selectedCol);
      //dataView = resultPane.getCurrentDataViewer();
      //dataView.setColumnLabels(columnLabels);
      ptm.insertColumn(selectedCol);
    }
    else
    {
      columnLabels.insertElementAt(newHeader, selectedCol+1);
      //dataView = resultPane.getCurrentDataViewer();

      ptm.insertColumn(selectedCol+1);
    }
    pv = ptm.getPersistentVector();
    dataView.setPV(pv);
    dataView.setColumnLabels(columnLabels);
    table.setModel(ptm);
    setUpDelimiterEditor(table, fieldDelimiter, dataView.getTablePanel());
    dataView.saveCurrentTable(true);
  }//insertEml2Column
  
  /*
   * Inserts a new attribute into data package
   */
  private void insertNewAttributeAt(AbstractDataPackage dataPackage, OrderedMap map, int index, DataViewer dataView, int enIndex)
  {
    if(map == null) {
      Log.debug(15,"Error retrieving OrderedMap while Inserting Column");
      return;
    }
    map.put("/attribute/@id", UISettings.getUniqueID());
    Attribute attrObject = new Attribute(map);
    if(dataPackage == null)
      dataPackage = dataView.getAbstractDataPackage();
    if(enIndex == -1)
      enIndex = dataView.getEntityIndex();
    dataPackage.insertAttribute(enIndex, attrObject, index);

  }
  
  /*
   * Set up the editor for the integer and string cells.
   */
  private void setUpDelimiterEditor(JTable table, String fieldDelimiter, JPanel tablePanel)
  {  
    int columns = table.getColumnCount();
    final DelimiterField delimiterField =
    new DelimiterField(tablePanel, fieldDelimiter, "", columns);
    delimiterField.setHorizontalAlignment(DelimiterField.RIGHT);

    DefaultCellEditor delimiterEditor =
    new DefaultCellEditor(delimiterField)
    {
      //Override DefaultCellEditor's getCellEditorValue method
      public Object getCellEditorValue()
      {
        return new String(delimiterField.getValue());
      }
    };
    TableColumnModel columnModel = table.getColumnModel();
    for (int j = 0; j< columns; j++)
    {
      columnModel.getColumn(j).setCellEditor(delimiterEditor);
      columnModel.getColumn(j).setPreferredWidth(85);
    }

  }
  
  /*
   * Gets the column name from map
   */
  private String getColumnName(OrderedMap map, String xPath) {

    Object o1 = map.get(xPath + "/attributeName");
    if(o1 == null) return "";
    else return (String) o1;
  }
  
}

