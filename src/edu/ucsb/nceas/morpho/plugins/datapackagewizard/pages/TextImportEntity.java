/**
 *  '$RCSfile: CitationPage.java,v $'
 *    Purpose: A class that handles display of Citation Information
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 23:06:55 $'
 * '$Revision: 1.29 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardUtil;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.morpho.util.XMLUtil;
import edu.ucsb.nceas.utilities.OrderedMap;

/**
 * This page will display some entity information and data table itself for text importing.
 * The metadata information is entityName and entityDescription.
 * @author tao
 *
 */
public class TextImportEntity extends AbstractUIPage 
{
	   private final String xPathRoot = "/eml:eml/dataset/dataTable/";
	   private String pageID = DataPackageWizardInterface.TEXT_IMPORT_ENTITY;
	   private String title = "Text Import";
	   private String subTitle = null;
	   private String pageNumber = null;
	   private WizardContainerFrame frame = null;
	   private ImportedTextFile textFile = null;
	   private boolean isTextFile = true;
	   private JTable linesTable = null;
	   private String shortFileName = null;
	   private String physicalID = null;
	   
	   /**
	    * Construct
	    * @param frame
	    */
	   public TextImportEntity(WizardContainerFrame frame)
	   {
		   if(frame == null)
		   {
			   Log.debug(5, "The WizardContainerFrame is null and we can't initialize TexImportEntity");
			   return;
		   }
		   this.frame = frame;	   
		   textFile = frame.getImportDataTextFile();
		   if(textFile == null)
		   {
			   Log.debug(5, "The TextFile object is null and we can't initialize TexImportEntity");
			   return;
		   }
		   buildLinesTable();
		   nextPageID = DataPackageWizardInterface.TEXT_IMPORT_DELIMITERS;
		   init();
	   }
	   
	   /*
	    * Build linesTable which will be displayed on data panel
	    */
	   private boolean buildLinesTable()
	   {
		   boolean success = true;
		   if (textFile == null && textFile.getDataFile() == null )
		   {
			   Log.debug(30, "The imported data file is null and we can't initialize TexImpoortEntity");
			   return false;
		   }
		   Log.debug(35, "The data file from previous page is ============= "+textFile.getDataFile().getAbsolutePath());		   
		   textFile.parsefile();		   
		   textFile.createLinesTable();
		   linesTable = textFile.getLinesTable();
		   return success;
	   }
	   
	   /*
	    * Init the panels
	    */
	   private void init()
	   {
		    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		    JPanel vbox = this;
		    JLabel desc1 = WidgetFactory.makeHTMLLabel(
		    	      "<p><b>This set of screens will create metadata based on the content of the specified data file</b></p>", 1);
		    vbox.add(desc1);
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    
		    JPanel namePanel = WidgetFactory.makePanel(1);	   
		    nameLabel = WidgetFactory.makeLabel("Title:", true);
		    namePanel.add(nameLabel);
		    TableNameTextField = WidgetFactory.makeOneLineTextField();
		    if(textFile != null)
		    {
		       TableNameTextField.setText(textFile.getShortFilename());
		    }
		    namePanel.add(TableNameTextField);
		    namePanel.setBorder(new javax.swing.border.EmptyBorder(WizardSettings.PADDING, 0, 0,
		            WizardSettings.PADDING));
		    vbox.add(namePanel);
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    
		    JPanel tableDescriptionPanel = WidgetFactory.makePanel(1);	   
		    JLabel Step1_TableDescriptionLabel = WidgetFactory.makeLabel("Description:", false);
		    tableDescriptionPanel.add(Step1_TableDescriptionLabel);
		    TableDescriptionTextField = WidgetFactory.makeOneLineTextField();
		    tableDescriptionPanel.add(TableDescriptionTextField);
		    tableDescriptionPanel.setBorder(new javax.swing.border.EmptyBorder(WizardSettings.PADDING, 0, 0,
		            WizardSettings.PADDING));
		    vbox.add(tableDescriptionPanel);
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    
		    JPanel StartingLinePanel=  WidgetFactory.makePanel(1);	
		    JLabel StartingLineLabel = WidgetFactory.makeLabel("Start import at row: ", false, new Dimension(120,20));
		    StartingLinePanel.add(StartingLineLabel);
		    StartingLineTextField = WidgetFactory.makeOneLineShortTextField("1");
		    TextFieldChangeAction textFieldAction = new TextFieldChangeAction();
		    StartingLineTextField.addActionListener(textFieldAction);
		    TextFieldFocusChange textFieldFocus = new TextFieldFocusChange();
		    StartingLineTextField.addFocusListener(textFieldFocus);
		    StartingLinePanel.add(StartingLineTextField);
		    JLabel blank = WidgetFactory.makeLabel("      ",false);
		    StartingLinePanel.add(blank);
		    ColumnLabelsCheckBox = WidgetFactory.makeCheckBox("Column Labels are in starting row", false);
		    ColumnLabelsCheckBox.setActionCommand("Column Labels are in starting row");
		    ColumnLabelsCheckBox.setSelected(false);
		    CheckBoxListener checkBoxListener = new CheckBoxListener();
		    ColumnLabelsCheckBox.addItemListener(checkBoxListener);
		    StartingLinePanel.add(ColumnLabelsCheckBox);	    
		    vbox.add(StartingLinePanel);
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    //vbox.add(WidgetFactory.makeDefaultSpacer());
		    
		    DataScrollPanel.getViewport().removeAll();
		    DataScrollPanel.getViewport().add(linesTable);
		    vbox.add(DataScrollPanel);
	   }
	   
	   /*
	    * Update data panel
	    */
	   private void updateDataScrollPanel()
	   {
		   DataScrollPanel.getViewport().removeAll();
		   DataScrollPanel.getViewport().add(linesTable);
	   }
	  
	   /**
	   *  gets the unique ID for this UI page
	   *
	   *  @return   the unique ID String for this UI page
	   */
	  public String getPageID()
	  {
		 return pageID; 
	  }


	  /**
	   *  gets the title for this UI page
	   *
	   *  @return   the String title for this UI page
	   */
	  public String getTitle()
	  {
		  return title;
	  }


	  /**
	   *  gets the subtitle for this UI page
	   *
	   *  @return   the String subtitle for this UI page
	   */
	  public String getSubtitle()
	  {
		  return subTitle;
	  }


	  /**
	   *  Returns the ID of the page that the user will see next, after the "Next"
	   *  button is pressed. If this is the last page, return value must be null
	   *
	   *  @return the String ID of the page that the user will see next, or null if
	   *  this is te last page
	   */
	  public String getNextPageID()
	  {
		  return nextPageID;
	  }


	  /**
	   *  Returns the serial number of the page
	   *
	   *  @return the serial number of the page
	   */
	  public String getPageNumber()
	  {
		  return pageNumber;
	  }
	  
	  /**
	   * Gets the unique id of this entity
	   * @return
	   */
	  public String getPhysicalID()
	  {
		  return physicalID;
	  }


	  /**
	   *  The action to be executed when the page is displayed. May be empty
	   */
	  public void onLoadAction()
	  {
		  WidgetFactory.unhiliteComponent(nameLabel);
		  if(frame != null)
		  {
			  Log.debug(35, "The beginning of the onLoadAction");
			  ImportedTextFile newFile = frame.getImportDataTextFile();
			  if(newFile == null)
			  {
				  Log.debug(5, "The ImportedTextFile from WizardContainerFrame is null and we can't load TexImportEntity");
				  return;
			  }
			  boolean isSame = newFile.equals(textFile);
			  if(!isSame)
			  {
				  Log.debug(30, "in they are not same textFile object and they do not have same data file branch on TextImportDelimiter.onLoadAtcion");
				  //File object was changed we need to update both data file and data panel
				  textFile = newFile;
				  String str = StartingLineTextField.getText();
				  handleStartingLineTextChange(str);
				  textFile.setColumnLabelsInStartingLine(ColumnLabelsCheckBox.isSelected());
				  buildLinesTable();
				  updateDataScrollPanel();
				  //set default table name only when data file was changed
				  if(textFile != null)
				  {
					  TableNameTextField.setText(textFile.getShortFilename());
				  }
			  }
			  else
			  {
				  Log.debug(30, "in they are same textFile object or they  have same data file branch on TextImportDelimiter.onLoadAtcion");
			  }
			 
			  
		  }
		  else
		  {
			  Log.debug(5, "The WizardContainerFrame is null and we can't load TexImportEntity");
		  }
	  }


	  /**
	   *  The action to be executed when the "Prev" button is pressed. May be empty
	   *
	   */
	  public  void onRewindAction()
	  {
		  
	  }


	  /**
	   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
	   *  or "Finish" button(last page) is pressed. May be empty
	   *
	   *  @return boolean true if wizard should advance, false if not
	   *          (e.g. if a required field hasn't been filled in)
	   */
	  public boolean onAdvanceAction()
	  {
		  if(textFile == null)
		  {
			 Log.debug(5, "Text File objext is null, couldn't continue!");	 
		  }
		  if(Util.isBlank(TableNameTextField.getText()))
		  {
			 WidgetFactory.hiliteComponent(nameLabel);
			 TableNameTextField.requestFocus();
			 return false;  
		  }
		  else
		  {
			  //make sure textFile contains current panel setting
			  Log.debug(30, "TextImporEntity.onAdvanceAction, the final coumnLableInStartingLine is "+ColumnLabelsCheckBox.isSelected());
			  textFile.setColumnLabelsInStartingLine(ColumnLabelsCheckBox.isSelected());
			  String str = StartingLineTextField.getText();
			  handleStartingLineTextChange(str);			  			
			  WidgetFactory.unhiliteComponent(nameLabel);
		     return true;
		  }
	  }


	  /**
	   *  gets the Map object that contains all the key/value paired
	   *  settings for this particular UI page
	   *
	   *  @return   data the Map object that contains all the
	   *            key/value paired settings for this particular UI page
	   */
	  public OrderedMap getPageData()
	  {
		  OrderedMap om = new OrderedMap();
		  om.put(xPathRoot + "entityName", XMLUtil.normalize(TableNameTextField.getText()));
		  if(!Util.isBlank(TableDescriptionTextField.getText()))
		  {
		       om.put(xPathRoot + "entityDescription",
		       XMLUtil.normalize(TableDescriptionTextField.getText()));
		  }
		    // physical NV pairs are inserted here
		    if(physicalID == null)
		    {
			   physicalID = WizardSettings.getUniqueID();
		    }
			om.put(xPathRoot + "physical/@id", physicalID);
			String shortFileName = null;
			File dataFile = null;
			int nlines_actual = 0;
			if(textFile != null)
			{
				shortFileName = textFile.getShortFilename();
				dataFile = textFile.getDataFile();
				nlines_actual = textFile.getNlines_actual();
			}
			if(!Util.isBlank(shortFileName))
			{
		        om.put(xPathRoot + "physical/objectName", shortFileName);
			}
			else
			{
				om.put(xPathRoot + "physical/objectName", WizardSettings.UNAVAILABLE);
			}
		    long filesize = 0;
		    if(dataFile != null)
		    {
		       filesize = dataFile.length();
		    }
		    String filesizeString = (new Long(filesize)).toString();
		    om.put(xPathRoot + "physical/size", filesizeString);
		    om.put(xPathRoot + "physical/size/@unit", "byte");
		    String numHeaderLinesStr = StartingLineTextField.getText();
		    int numHeaderLines = 1;
		    int startingLine =1;
		    try
		    {
		    	numHeaderLines = (new Integer(numHeaderLinesStr)).intValue();
		    	startingLine = numHeaderLines;
		    }
		    catch(Exception e)
		    {
		    	numHeaderLines = 1;
		    }
		    if (!ColumnLabelsCheckBox.isSelected())
		    {
		    	numHeaderLines = numHeaderLines - 1;
		    }
		    om.put(xPathRoot + "physical/dataFormat/textFormat/numHeaderLines",
		           "" + numHeaderLines);
		    om.put(xPathRoot + "physical/dataFormat/textFormat/recordDelimiter",
		           "#x0A");
		    om.put(xPathRoot + "physical/dataFormat/textFormat/attributeOrientation",
		           "column");
		   
		    return om;
	  }
	  
	  /**
	   * Gets the order map contains the number of records. Since the numberOfRecords is the last
	   * subtree of dataTable in eml/dataTable, we have to append it to dataTable at the last step
	   * @return
	   */
	  public OrderedMap getNumberOfRecordsData()
	  {
		    int nlines_actual = 0;
			if(textFile != null)
			{
				File dataFile = textFile.getDataFile();
				nlines_actual = textFile.getNlines_actual();
			}
			 int startingLine =1;
			 try
			 {
			    	startingLine = (new Integer(StartingLineTextField.getText())).intValue();
			    	
			 }
			 catch(Exception e)
			 {
			    	startingLine = 1;
			 }
		    OrderedMap  om = new OrderedMap();
		    int temp = 0;
		    if (ColumnLabelsCheckBox.isSelected())temp = 1;
		    int numrecs = nlines_actual - (startingLine - 1) - temp;
		    String numRecords = (new Integer(numrecs)).toString();
		    if(!Util.isBlank(numRecords))
		    {
		    	om.put(xPathRoot + "numberOfRecords", XMLUtil.normalize(numRecords));
		    }
		    return om;
	  }


	  /**
	   * gets the Map object that contains all the key/value paired settings for
	   * this particular UI page
	   *
	   * @param rootXPath the root xpath to prepend to all the xpaths returned by
	   *   this method
	   * @return data the Map object that contains all the key/value paired
	   *   settings for this particular UI page
	   */
	  public OrderedMap getPageData(String rootXPath)
	  {
		  throw new UnsupportedOperationException(
	      "getPageData(String rootXPath) Method Not Implemented");
	  }


	  /**
	   * sets the fields in the UI page using the Map object that contains all
	   * the key/value paired
	   *
	   * @param data the Map object that contains all the key/value paired settings
	   *   for this particular UI page
	   * @param rootXPath the String that represents the "root" of the XPath to the
	   *   content of this widget, INCLUDING PREDICATES. example - if this is a
	   *   "Party" widget, being used for the second "Creator" entry in a list,
	   *   then xPathRoot = "/eml:eml/dataset[1]/creator[2]
	   * @return boolean true if this page can handle all the data passed in the
	   * OrderedMap, false if not. <em>NOTE that the setPageData() method should
	   * still complete its work and fill out all the UI values, even if it is
	   * returning false</em>
	   */
	  public boolean setPageData(OrderedMap data, String rootXPath)
	  {
		  return true;
	  }
	  
	  
	  /*
	   * Listener for check box selection
	   */
	  class CheckBoxListener implements java.awt.event.ItemListener 
	  {
		    public void itemStateChanged(java.awt.event.ItemEvent event) 
		    {
		      Object object = event.getSource();
		      if (object == ColumnLabelsCheckBox)
		        columnLabelsCheckBox_itemStateChanged(event);
		    }
	  }

	   private void columnLabelsCheckBox_itemStateChanged(java.awt.event.ItemEvent event) 
	   {
		    boolean labelsInStartingLine = ColumnLabelsCheckBox.isSelected();
		    Log.debug(30, "Set Column Label check box is selected "+labelsInStartingLine);
		    textFile.setColumnLabelsInStartingLine(labelsInStartingLine);
		    
	   }
	   
	  
	   /*
	    * Action listener for text field value change
	    * @author tao
	    *
	    */
	   class TextFieldChangeAction implements java.awt.event.ActionListener 
	   {

		    public void actionPerformed(java.awt.event.ActionEvent event) 
		    {
		      Object object = event.getSource();
		      if (object == StartingLineTextField)
		        startingLineTextField_actionPerformed(event);
		    }
		  }

	  private void startingLineTextField_actionPerformed(java.awt.event.ActionEvent event) 
	  {
		    
		    String str = StartingLineTextField.getText();
		    handleStartingLineTextChange(str);
     }
	  
	 /*
	  * Action to handle starting line text change
	  */
	 private void handleStartingLineTextChange(String str)
	 {
		 int startingLine =1;
		 if (WizardUtil.isInteger(str)) 
		    {
		      startingLine = (Integer.valueOf(str)).intValue();
		      // startingLine is assumed to be 1-based
		      if (startingLine < 1) 
		      {
		        startingLine = 1;
		        StartingLineTextField.setText("1");
		      }
		    } 
		    else 
		    {
		      StartingLineTextField.setText(String.valueOf(startingLine));
		    }
		    Log.debug(30, "Set the starting line number to "+startingLine);
		    textFile.setDataStartingLineNumber(startingLine);
	 }


	 /*
	  * Listener for text field focus change event
	  */
	 class TextFieldFocusChange extends java.awt.event.FocusAdapter 
	 {
		    public void focusLost(java.awt.event.FocusEvent event) 
		    {
		      Object object = event.getSource();
		      if (object == StartingLineTextField)
		      {
		        startingLineTextField_focusLost(event);
		      }
		    }
	 }

	 private void startingLineTextField_focusLost(java.awt.event.FocusEvent event) 
	 {
		    String str = StartingLineTextField.getText();
		    handleStartingLineTextChange(str);
	  }
		  	  
	  
	  private JLabel nameLabel = null;
	  private JTextField TableNameTextField = new JTextField();
	  private JTextField TableDescriptionTextField = new JTextField();
	  private JTextField StartingLineTextField = new JTextField();
	  private JCheckBox ColumnLabelsCheckBox = new JCheckBox();
	  private JScrollPane DataScrollPanel = new JScrollPane();

}
