/**
*  '$RCSfile: InsertColumnCommand.java,v $'
*  Copyright: 2000 Regents of the University of California and the
*              National Center for Ecological Analysis and Synthesis
*    Authors: @tao@
*    Release: @release@
*
*   '$Author: sambasiv $'
*     '$Date: 2004-04-29 00:55:16 $'
* '$Revision: 1.21 $'
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
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.Vector;

import java.awt.event.ActionEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
* Class to handle insert a cloumn command
*/
public class InsertColumnCommand implements Command
{
  /* Indicate before selected column */
  private boolean beforeFlag = true;

  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  /* Constant string for column position */
  public static final String AFTER = "after";
  public static final String BEFORE = "before";

  /* Flag if need to add a column*/
  private boolean columnAddFlag = false;

  private boolean openNewWindow = false;

  private JPanel controlPanel;
  private JButton controlOK;
  private JButton controlCancel;
  private JDialog columnDialog;

  private DataViewContainerPanel resultPane;

  private AbstractUIPage attributePage;
  private DataViewer dataView;
  private OrderedMap map = null;
  private String mScale;
  private String columnName;
  private AbstractDataPackage adp = null;
  private int entityIndex = -1;
  private int selectedCol = -1;
  private String xPath = "/attribute";

  private JTable table;
  private PersistentTableModel ptm;
  private PersistentVector pv;
  private Vector columnLabels;
  private String fieldDelimiter;
  private JPanel tablePanel;
  private Document attributeDoc;

  /**
   * Constructor of Import data command
   *
   * @param column String
   */
  public InsertColumnCommand(String column)
  {
    if (column.equals(AFTER))
    {
      beforeFlag = false;
    }
  }


  /**
   * execute insert command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event)
  {
    resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if (morphoFrame != null)
    {
      resultPane = morphoFrame.getDataViewContainerPanel();
    }//if

    // make sure resulPanel is not null
    if (resultPane != null)
    {
      dataView = resultPane.getCurrentDataViewer();
      if (dataView != null)
      {
        // Get parameters and run it
        table=dataView.getDataTable();
        ptm = (PersistentTableModel)table.getModel();
        pv = dataView.getPV();
        Morpho morph=dataView.getMorpho();
        attributeDoc = dataView.getAttributeDoc();
        columnLabels=dataView.getColumnLabels();
        //         String fieldDelimiter=dataView.getFieldDelimiter();
        fieldDelimiter= pv.getFieldDelimiter();
        tablePanel=dataView.getTablePanel();
        selectedCol = table.getSelectedColumn();
        if (selectedCol > -1) {
          showAttributeDialog();
        }
      }

    }//if

  }//execute



  private void insertEml2Column() {

    if(selectedCol < 0) {
      Log.debug(10, "Error inserting Column. selected Column is " + selectedCol);
      return;
    }

    try
    {
      if (beforeFlag)
      {
        insertNewAttributeAt(selectedCol, attributeDoc);
      }
      else
      {
        insertNewAttributeAt((selectedCol+1), attributeDoc);
      }

    }//try
    catch (Exception w)
    {
      Log.debug(20, "Exception trying to modify attribute DOM");
    }//catch

    if(attributePage == null) return;


    String newHeader = columnName;
    if (newHeader.trim().length()==0) newHeader = "New Column";
    String unit = getUnit(map, xPath);
		String sType = (String)map.get(xPath + "/storageType");
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
    setUpDelimiterEditor();
    dataView.saveCurrentTable(true);

    if(openNewWindow) {

      try {
        ServiceController services = ServiceController.getInstance();
        ServiceProvider provider =
        services.getServiceProvider(DataPackageInterface.class);
        DataPackageInterface dataPackageInt = (DataPackageInterface)provider;
        dataPackageInt.openNewDataPackage(adp, null);
      } catch (ServiceNotHandledException snhe) {
        Log.debug(6, snhe.getMessage());
      }
      UIController controller = UIController.getInstance();
      morphoFrame.setVisible(false);
      controller.removeWindow(morphoFrame);
      morphoFrame.dispose();
    } // if

    return;

  }//insertEml2Column

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

    Object o1 = map.get(xPath + "/measurementScale/interval" + "/unit/standardUnit");
    if(o1 != null) return (String)o1;
		o1 = map.get(xPath + "/measurementScale/interval" + "/unit/customUnit");
    if(o1 != null) return (String)o1;
    o1 = map.get(xPath + "/measurementScale/ratio" + "/unit/standardUnit");
    if(o1 != null) return (String)o1;
    o1 = map.get(xPath + "/measurementScale/ratio" + "/unit/customUnit");
    if(o1 != null) return (String)o1;
    
		return "";
  }

  void insertNewAttributeAt(int index, Document doc)
  {
    if(map == null) {
      Log.debug(15,"Error retrieving OrderedMap while Inserting Column");
      return;
    }
    map.put("/attribute/@id", UISettings.getUniqueID());
    Attribute attrObject = new Attribute(map);
    if(adp == null)
      adp = dataView.getAbstractDataPackage();
    if(entityIndex == -1)
      entityIndex = dataView.getEntityIndex();
    adp.insertAttribute(entityIndex, attrObject, index);

    return;
  }


  private void showAttributeDialog() {

    ServiceController sc;
    DataPackageWizardInterface dpwPlugin = null;
    try {
      sc = ServiceController.getInstance();
      dpwPlugin = (DataPackageWizardInterface)sc.getServiceProvider(DataPackageWizardInterface.class);
    } catch (ServiceNotHandledException se) {
      Log.debug(6, se.getMessage());
    }
    if(dpwPlugin == null) return;

    attributePage = dpwPlugin.getPage(DataPackageWizardInterface.ATTRIBUTE_PAGE);
    ModalDialog wpd
        = new ModalDialog(attributePage,
                                UIController.getInstance().getCurrentActiveWindow(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT, false);
    wpd.setSize(UISettings.POPUPDIALOG_WIDTH, UISettings.POPUPDIALOG_FOR_ATTR_HEIGHT);
    wpd.setVisible(true);

    entityIndex = dataView.getEntityIndex();
    adp = dataView.getAbstractDataPackage();

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {

      map = attributePage.getPageData(xPath);

      if(entityIndex == -1) {
        Log.debug(10, "Unable to get the Index of the current Entity, in EditColumnMetaData.");
        return;
      }

      columnName = getColumnName(map, xPath );
      mScale = getMeasurementScale(map, xPath);
			boolean toImport = isImportNeeded(map, xPath, mScale);
			
      if(toImport) {
        String entityName = adp.getEntityName(entityIndex);

        adp.addAttributeForImport(entityName, columnName, mScale, map, "/attribute", false);
        DataPackageWizardListener dpwListener = new DataPackageWizardListener () {
          public void wizardComplete(Node newDOM) {
            openNewWindow = true;
            insertEml2Column();

          }
          public void wizardCanceled() {
            return;
          }
        };
        dpwPlugin.startCodeDefImportWizard(dpwListener);

      } else { // if import is not needed
        insertEml2Column();
      }

    }
    return;
  }

	private boolean isImportNeeded(OrderedMap map, String xPath, String mScale) {
		
		mScale = mScale.toLowerCase();
		if(!(mScale.equals("nominal") || mScale.equals("ordinal"))) return false;
		String path = xPath + "/measurementScale/" + mScale + "/nonNumericDomain/enumeratedDomain[1]/entityCodeList/entityReference";
		boolean present = map.containsKey(path);
		if(!present) return false;
		String o = (String)map.get(path);
		if(o == null || o.trim().equals("")) return true;
		return false;
	}
	
  private void setUpDelimiterEditor()
  {
    //Set up the editor for the integer and string cells.
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

}//class InsertColumnCommand
