/**
 *  '$RCSfile: InsertColumnCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2003-11-19 01:42:18 $'
 * '$Revision: 1.7 $'
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
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributePage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributeSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.DataPackageWizardPlugin;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;


import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;
import edu.ucsb.nceas.morpho.util.XMLUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.Point;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;


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
  /* Reference to ColumnMetadataEditPanel*/
  ColumnMetadataEditPanel cmep = null;
  /* Control in column meta data edit panel*/
  private JPanel controlPanel;
  private JButton controlOK;
  private JButton controlCancel;
  private JDialog columnDialog;
  
  private DataViewContainerPanel resultPane;
  
  AttributePage attributePage;
  
  /**
   * Constructor of Import data command
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
   */    
  public void execute(ActionEvent event)
  {   
    resultPane = null;
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
         JTable jtable=dataView.getDataTable();
         PersistentTableModel ptmodel=(PersistentTableModel)jtable.getModel();
         PersistentVector vector=dataView.getPV();
         Morpho morph=dataView.getMorpho();
         Document attributeDocoumnet=dataView.getAttributeDoc();
         Vector columnLabels=dataView.getColumnLabels();
//         String fieldDelimiter=dataView.getFieldDelimiter();
         String fieldDelimiter= vector.getFieldDelimiter();
         JPanel tablePanel=dataView.getTablePanel();
         insertEml2Column(jtable, ptmodel, vector, morph, attributeDocoumnet,
                      columnLabels, fieldDelimiter, tablePanel);
       }
       
    }//if
  
  }//execute
  
  

  private void insertEml2Column(JTable table, PersistentTableModel ptm, 
                           PersistentVector pv, Morpho morpho,
                           Document attributeDoc, Vector column_labels,
                           String field_delimiter, JPanel TablePanel)
  {  
    
    int sel = table.getSelectedColumn();
    if (sel>-1) 
    {
       showAttributeDialog();
       if (columnAddFlag) 
       {
         try 
         {
           
           if (beforeFlag)
           {
		insertNewAttributeAt(sel, attributeDoc);
           }
           else
           {
		insertNewAttributeAt((sel+1), attributeDoc);
            }

          }//try
          catch (Exception w) 
          {
              Log.debug(20, "Exception trying to modify attribute DOM");
          }//catch
            
          /*String newHeader = cmep.getColumnName();
          if (newHeader.trim().length()==0) newHeader = "New Column";
          String type = cmep.getDataType();
          String unit = cmep.getUnit();
          newHeader = "<html><font face=\"Courier\"><center><small>"+type+
                                           "<br>"+unit +"<br></small><b>"+
                                           newHeader+"</b></font></center></html>";
          if (beforeFlag)
          {
            column_labels.insertElementAt(newHeader, sel);
            DataViewer dataView = resultPane.getCurrentDataViewer();
            dataView.setColumnLabels(column_labels);
            ptm.insertColumn (sel);
          }
          else
          {
            column_labels.insertElementAt(newHeader, sel+1);
            DataViewer dataView = resultPane.getCurrentDataViewer();
            dataView.setColumnLabels(column_labels);
            ptm.insertColumn(sel+1);
          }
          pv = ptm.getPersistentVector();
          setUpDelimiterEditor(table, field_delimiter, TablePanel);*/
          
          
       }//if
    }//if
  }//insetColumn

  private String findMeasurementScale(OrderedMap map) {
	  
	  Object o1 = map.get(AttributeSettings.Nominal_xPath+"/enumeratedDomain[1]/codeDefinition[1]/code");
	  if(o1 != null) return "Nominal";
	  o1 = map.get(AttributeSettings.Nominal_xPath+"/textDomain[1]/definition");
	  if(o1 != null) return "Nominal";
	  
	  o1 = map.get(AttributeSettings.Ordinal_xPath+"/enumeratedDomain[1]/codeDefinition[1]/code");
	  if(o1 != null) return "Ordinal";
	  o1 = map.get(AttributeSettings.Ordinal_xPath+"/textDomain[1]/definition");
	  if(o1 != null) return "Ordinal";
	  
	  o1 = map.get(AttributeSettings.Interval_xPath+"/unit/standardUnit");
	  if(o1 != null) return "Interval";
	  o1 = map.get(AttributeSettings.Ratio_xPath+"/unit/standardUnit");
	  if(o1 != null) return "Ratio";
	  
	  o1 = map.get(AttributeSettings.DateTime_xPath+"/formatString");
	  if(o1 != null) return "Datetime";
	  
	  return "";
  }
  
  void insertNewAttributeAt(int index, Document doc) 
  {
	  OrderedMap map = attributePage.getPageData(AttributeSettings.Attribute_xPath);
	  Node root = doc.getDocumentElement();
	  Object o1;
	  // create the root of the new attribute subtree
	  Node newAttrRoot = doc.createElement("attribute");
	  Node newText;
	  Node temp = doc.createElement("attributeName");
	  o1 = map.get(AttributeSettings.AttributeName_xPath);
	  if(o1!=null) {
		  String name = (String)o1;
		  newText = doc.createTextNode(XMLUtil.normalize(name));
		  temp.appendChild(newText);
		  newAttrRoot.appendChild(temp);
	  }
	  
	  temp = doc.createElement("attributeLabel");
	  o1 = map.get(AttributeSettings.AttributeLabel_xPath);
	  if( o1 != null) {
		  String label = (String)o1;
		  newText = doc.createTextNode(XMLUtil.normalize(label));
		  temp.appendChild(newText);
		  newAttrRoot.appendChild(temp);
	  }
	  
	  temp = doc.createElement("attributeDefinition");
	  o1 = map.get(AttributeSettings.AttributeDefinition_xPath);
	  if(o1 != null) {
		  String definition = (String)o1;
		  newText = doc.createTextNode(XMLUtil.normalize(definition));
		  temp.appendChild(newText);
		  newAttrRoot.appendChild(temp);
	  }
	  
	  Node mScale = doc.createElement("measurementScale");
	  newAttrRoot.appendChild(mScale);
	  
	  String scale = findMeasurementScale(map);
	  Node type = doc.createElement(scale.toLowerCase());
	  
	  mScale.appendChild(type);
	  
	  if(scale.equals("Nominal") || scale.equals("Ordinal")) {
		
		String path = AttributeSettings.Nominal_xPath;
		if(scale.equals("Ordinal"))
			path = AttributeSettings.Ordinal_xPath;
		
		Node nonNumeric = doc.createElement("nonNumericDomain");
		type.appendChild(nonNumeric);
		Object o2 = map.get(path + "/enumeratedDomain[1]/codeDefinition[1]/code");
		if( o2 != null) { // enumerated Domain
			Node enumdomain = doc.createElement("enumeratedDomain");
			nonNumeric.appendChild(enumdomain);
			int cnt = 1;
			while(true) {
				Object o3 = map.get(path + "/enumeratedDomain[1]/codeDefinition[" + cnt + "]/code");
				if(o3 == null) 
					break;
				Node curr = doc.createElement("codeDefinition");
				enumdomain.appendChild(curr);
				
				String code = (String) o3;
				String defn = (String) map.get(path + "/enumeratedDomain[1]/codeDefinition[" + cnt + "]/definition");
				
				Node cNode = doc.createElement("code");
				newText = doc.createTextNode(XMLUtil.normalize(code));				
				cNode.appendChild(newText);
				curr.appendChild(cNode);
				
				Node dNode = doc.createElement("definition");
				newText = doc.createTextNode(XMLUtil.normalize(defn));				
				dNode.appendChild(newText);
				curr.appendChild(dNode);
				
				Object o4 = map.get(path + "/enumeratedDomain[1]/codeDefinition[" + cnt + "]/source");
				if(o4!= null) {
					Node sNode = doc.createElement("source");
					newText = doc.createTextNode(XMLUtil.normalize((String)o4));				
					sNode.appendChild(newText);
					curr.appendChild(sNode);
				}
				cnt++;
			}
			Object o3 = map.get(path  + "/textDomain[1]/definition");
			if(o3 != null) { // contains free text
				Node textdomain = doc.createElement("textDomain");
				nonNumeric.appendChild(textdomain);
				Node textdefn = doc.createElement("definition"); 
				textdomain.appendChild(textdefn);
				newText = doc.createTextNode(XMLUtil.normalize((String)o3));
				textdefn.appendChild(newText);
				Node textpattern = doc.createElement("pattern"); 
				textdomain.appendChild(textpattern);
				o3 = map.get(path + "/textDomain[1]/pattern[1]");
				newText = doc.createTextNode(XMLUtil.normalize((String)o3));
				textpattern.appendChild(newText);
			}
			
		} else { // text domain
			
			Node domain = doc.createElement("textDomain");
			nonNumeric.appendChild(domain);
			
			String defn = (String)map.get(path  + "/textDomain[1]/definition");
			Node textdefn = doc.createElement("definition"); 
			domain.appendChild(textdefn);
			newText = doc.createTextNode(XMLUtil.normalize(defn));
			textdefn.appendChild(newText);
			int cnt = 1;
			while(true) {
				Object o3 = map.get(path + "/textDomain[1]/pattern[" + cnt +"]");
				if(o3 == null ) break;
				Node textpattern = doc.createElement("pattern"); 
				domain.appendChild(textpattern);
				newText = doc.createTextNode(XMLUtil.normalize((String)o3));
				textpattern.appendChild(newText);
				cnt++;
			}
			Object o4 = map.get(path + "/textDomain[1]/source");
			if(o4 != null) {
				Node textsrc = doc.createElement("source"); 
				domain.appendChild(textsrc);
				newText = doc.createTextNode(XMLUtil.normalize((String)o4));
				textsrc.appendChild(newText);
			}
		}
	  }
	  else if(scale.equals("Interval") || scale.equals("Ratio")) {
		
		String path = AttributeSettings.Interval_xPath;
		if(scale.equals("Ratio"))
			path = AttributeSettings.Ratio_xPath;
		
		String unitStr = (String)map.get(path + "/unit/standardUnit");
		Node unit = doc.createElement("unit");
		Node sdunit = doc.createElement("standardUnit");
		unit.appendChild(sdunit);
		newText = doc.createTextNode(XMLUtil.normalize(unitStr));
		sdunit.appendChild(newText);
		type.appendChild(unit);
		
		Node precision = doc.createElement("precision");
		String precStr  =  (String)map.get(path + "/precision");
		newText = doc.createTextNode(XMLUtil.normalize(precStr));
		precision.appendChild(newText);
		type.appendChild(precision);
		
		Node numDomain = doc.createElement("numericDomain");
		type.appendChild(numDomain);
		Node numType = doc.createElement("numberType");
		numDomain.appendChild(numType);
		String numTypeStr = (String)map.get(path + "/numericDomain/numberType");
		newText = doc.createTextNode(XMLUtil.normalize(numTypeStr));
		numType.appendChild(newText);
		
		int cnt = 1;
		while(true) {
			Object o5 = map.get(path + "/numericDomain/bounds[" + cnt + "]/minimum");
			Object o6 = map.get(path + "/numericDomain/bounds[" + cnt + "]/maximum");
			if(o5 == null && o6 ==null) 
				break;
			Node bounds = doc.createElement("bounds");
			numDomain.appendChild(bounds);
			if(o5 != null) {
				Node min = doc.createElement("minimum");
				bounds.appendChild(min);
				newText = doc.createTextNode(XMLUtil.normalize((String)o5));
				min.appendChild(newText);
				String o7 = (String)map.get(path + "/numericDomain/bounds[" + cnt + "]/minimum/@exclusive");
				((Element)min).setAttribute("exclusive",o7);
			} 
			if(o6 != null) {
				Node max = doc.createElement("maximum");
				bounds.appendChild(max);
				newText = doc.createTextNode(XMLUtil.normalize((String)o6));
				max.appendChild(newText);
				String o8 = (String)map.get(path + "/numericDomain/bounds[" + cnt + "]/maximum/@exclusive");
				((Element)max).setAttribute("exclusive",o8);
			}
			cnt++;
		}
	  }
	  else if(scale.equalsIgnoreCase("Datetime")) {
		String path = AttributeSettings.DateTime_xPath;
		Node formatString = doc.createElement("formatString");
		type.appendChild(formatString);
		String fString = (String)map.get(path + "/formatString");
		newText = doc.createTextNode(XMLUtil.normalize(fString));
		formatString.appendChild(newText);
		
		Node dateTimePrecision = doc.createElement("dateTimePrecision");
		type.appendChild(dateTimePrecision);
		String dString = (String)map.get(path + "/dateTimePrecision");
		newText = doc.createTextNode(XMLUtil.normalize(dString));
		dateTimePrecision.appendChild(newText);
		
		Node domain = doc.createElement("dateTimeDomain");
		type.appendChild(domain);
		
		int cnt = 1;
		while(true) {
			Object o5 = map.get(path + "/dateTimeDomain/bounds[" + cnt + "]/minimum");
			Object o6 = map.get(path + "/dateTimeDomain/bounds[" + cnt + "]/maximum");
			if(o5 == null && o6 ==null) 
				break;
			Node bounds = doc.createElement("bounds");
			domain.appendChild(bounds);
			if(o5 != null) {
				Node min = doc.createElement("minimum");
				bounds.appendChild(min);
				newText = doc.createTextNode(XMLUtil.normalize((String)o5));
				min.appendChild(newText);
				String o7 = (String)map.get(path + "/numericDomain/bounds[" + cnt + "]/minimum/@exclusive");
				((Element)min).setAttribute("exclusive",o7);
			} 
			if(o6 != null) {
				Node max = doc.createElement("maximum");
				bounds.appendChild(max);
				newText = doc.createTextNode(XMLUtil.normalize((String)o6));
				max.appendChild(newText);
				String o8 = (String)map.get(path + "/numericDomain/bounds[" + cnt + "]/maximum/@exclusive");
				((Element)max).setAttribute("exclusive",o8);
			}
			cnt++;
		}
	  }
	  
	  
	  // --------------------------------
	  // now find the 'index'th attribute and insert the new branch there
	  NodeList nl = doc.getElementsByTagName("attribute");
	  // nl should now contain all the 'Attribute' nodes
	  int cnt = index;
	  if (cnt > nl.getLength()) cnt=nl.getLength();
	  Node nextNode = nl.item(cnt);
	  root.insertBefore(newAttrRoot, nextNode);
	  
	  String res = XMLUtilities.getDOMTreeAsString(newAttrRoot);
	  //JOptionPane.showMessageDialog(morphoFrame, "DOM-TREE - \n" + res, "Message",JOptionPane.INFORMATION_MESSAGE, null);
	  Log.debug(1,"DOM-Tree - \n" + res);
	  return;
  }
  
  
  /* Method to insert a new column into table */
  private void insertColumn(JTable table, PersistentTableModel ptm, 
                           PersistentVector pv, Morpho morpho,
                           Document attributeDoc, Vector column_labels,
                           String field_delimiter, JPanel TablePanel)
  {  
    
    int sel = table.getSelectedColumn();
    if (sel>-1) 
    {
       showColumnMetadataEditPanel();
       if (columnAddFlag) 
       {
         try 
         {
           cmep.setMorpho(morpho);
           if (beforeFlag)
           {
              cmep.insertNewAttributeAt(sel, attributeDoc);
           }
           else
           {
              cmep.insertNewAttributeAt((sel+1), attributeDoc);
            }

          }//try
          catch (Exception w) 
          {
              Log.debug(20, "Exception trying to modify attribute DOM");
          }//catch
            
          String newHeader = cmep.getColumnName();
          if (newHeader.trim().length()==0) newHeader = "New Column";
          String type = cmep.getDataType();
          String unit = cmep.getUnit();
          newHeader = "<html><font face=\"Courier\"><center><small>"+type+
                                           "<br>"+unit +"<br></small><b>"+
                                           newHeader+"</b></font></center></html>";
          if (beforeFlag)
          {
            column_labels.insertElementAt(newHeader, sel);
            DataViewer dataView = resultPane.getCurrentDataViewer();
            dataView.setColumnLabels(column_labels);
            ptm.insertColumn(sel);
          }
          else
          {
            column_labels.insertElementAt(newHeader, sel+1);
            DataViewer dataView = resultPane.getCurrentDataViewer();
            dataView.setColumnLabels(column_labels);
            ptm.insertColumn(sel+1);
          }
          pv = ptm.getPersistentVector();
          setUpDelimiterEditor(table, field_delimiter, TablePanel);
          
          
       }//if
    }//if
  }//insetColumn


  private void showAttributeDialog() {
	int newCompWidth = 400;
	int newCompHeight = 650;
	MorphoFrame mf = UIController.getInstance().getCurrentActiveWindow();
	Point curLoc = mf.getLocationOnScreen();
	Dimension dim = mf.getSize();
	int newx = curLoc.x +dim.width/2;
	int newy = curLoc.y+dim.height/2;
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
	attributePage = (AttributePage)dpwPlugin.getPage(DataPackageWizardInterface.ATTRIBUTE_PAGE);
	WizardPopupDialog wpd = new WizardPopupDialog(attributePage, mf, false);
	wpd.setVisible(true);
	
	if (wpd.USER_RESPONSE == WizardPopupDialog.OK_OPTION) {
		columnAddFlag = true;							
	} else {
		columnAddFlag = false;
	}
	return;
  }
  
  /*
   * Method to show column meta data edit panel
   */
  private void showColumnMetadataEditPanel() 
  {
    int newCompWidth = 400;
    int newCompHeight = 650;
    MorphoFrame mf = UIController.getInstance().getCurrentActiveWindow();
    Point curLoc = mf.getLocationOnScreen();
    Dimension dim = mf.getSize();
    int newx = curLoc.x +dim.width/2;
    int newy = curLoc.y+dim.height/2;
    columnDialog = new JDialog(mf,true);
    columnDialog.getContentPane().setLayout(new BorderLayout(0,0));
    columnDialog.setSize(newCompWidth,newCompHeight);
    columnDialog.setLocation(newx-newCompWidth/2, newy-newCompHeight/2);
    cmep = new ColumnMetadataEditPanel();
    columnDialog.getContentPane().add(BorderLayout.CENTER, cmep);
    controlPanel = new JPanel();
    controlCancel = new JButton("Cancel");
    controlOK= new JButton("OK");
    controlPanel.add(controlCancel);
    controlPanel.add(controlOK);
    columnDialog.getContentPane().add(BorderLayout.SOUTH, controlPanel);
    ColAction cAction = new ColAction();
		controlOK.addActionListener(cAction);
    controlCancel.addActionListener(cAction);

    columnDialog.setVisible(true);
  }
  
  /*
   * Method to set a table's interger and string column editor
   */
   private void setUpDelimiterEditor(JTable jtable, String delimiter,
                                                         JPanel pane) 
   {
      //Set up the editor for the integer and string cells.
      int columns = jtable.getColumnCount();
      final DelimiterField delimiterField = 
                              new DelimiterField(pane, delimiter, "", columns);
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
       TableColumnModel columnModel = jtable.getColumnModel();
       for (int j = 0; j< columns; j++)
       {
          columnModel.getColumn(j).setCellEditor(delimiterEditor);
          columnModel.getColumn(j).setPreferredWidth(85);
       }
     
    }
  
  /*
   * Private class to handle the button action in column meta data edit panel
   */
  class ColAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == controlOK) {
        columnAddFlag = true;
				columnDialog.dispose();
      }
      else if (object == controlCancel) {
        columnAddFlag = false;
				columnDialog.dispose();
      }
		}
	}
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
