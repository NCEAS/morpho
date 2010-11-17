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
import java.awt.event.ItemEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibraryInterface;
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
public class TextImportDelimiters extends AbstractUIPage implements TableModelListener 
{
	   private final String xPathRoot = "/eml:eml/dataset/dataTable/";
	   private static final String EMPTYSTRING = "";
	   private String pageID = DataPackageWizardInterface.TEXT_IMPORT_DELIMITERS; 
	   private String title = "Text Import";
	   private String subTitle = null;
	   private String pageNumber = null;
	   private boolean ignoreConsequtiveDelimiters = false;
	   private WizardContainerFrame frame = null;
	   private ImportedTextFile textFile = null;
	   private String otherDelimiter= EMPTYSTRING;
	   private String delimiter = EMPTYSTRING;
	   private int initDataStartingLineNumber = 1; // store initial dataStaringLineNumber from TextFile
	   private boolean initColumnLabelsInStartingLine = false;
	   private CheckBoxItemListener checkBoxListener = new CheckBoxItemListener();
	   private TextFieldChangeActionListener textChangeListener = new TextFieldChangeActionListener();
	   private TextFieldFocusChangeListener textFocusChangeListener = new TextFieldFocusChangeListener();
	   private boolean hasAdditionalMetadataPart = false;
	   public static final String IGNORECONSECUTIVEDELIMITERS = "ignoreConsecutiveDelimiters";
	   /*
	    * flag used to avoid parsing everytime a checkbox is changed
	    */
	   //private boolean parseOn = true;
	  
	   public static final String TAB = "tab";
	   public static final String COMMA = "comma";
	   public static final String SPACE = "space";
	   public static final String SEMICOLON = "semicolon";
	   public static final String OTHER = "other";
	   public static final String COLON ="colon";
	   private static final String TREATCONSECUTIVE = "Treat consecutive delimiters as one";
	   
	   /**
	    * Construct
	    * @param frame
	    */
	   public TextImportDelimiters(WizardContainerFrame frame)
	   {
		   this.frame = frame;
		   nextPageID = DataPackageWizardInterface.TEXT_IMPORT_FIRST_ATTRIBUTE;
		   Log.debug(32, "=========The next page id is "+nextPageID);
		   if(this.frame == null)
		   {
			   Log.debug(5, "The WizardContainerFrame is null and we can't initialize TexImportDelimiters");
		       return;
			 
		   }
		   else
		   {
			   textFile = frame.getImportDataTextFile();
			   if(textFile == null)
			   {
				 
				   Log.debug(5, "The TextFile object is null and we can't initialize TexImportDelimiters");
				   return;
				   
			   }
			  
		   }
		   initDataStartingLineNumber = textFile.getDataStartingLineNumber();
		   initColumnLabelsInStartingLine = textFile.isColumnLabelsInStartingLine();
		   delimiter = textFile.getGuessedDelimiter();
		   init();
		   
	   }
	   
	   /*
	    * Init the panels
	    */
	   private void init()
	   {
		    
		    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		    JPanel vbox = this;
		    JLabel desc1 = WidgetFactory.makeHTMLLabel(
		    	      "<p>If the columns indicated in the table are incorrect, try changing the assumed delimiter(s)</p>", 1);
		    vbox.add(desc1);
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    
		    JPanel delimitersPanel = WidgetFactory.makePanel(2);	   
		    delimiterLabel = WidgetFactory.makeLabel("Delimiters:", false);
		    delimitersPanel.add(delimiterLabel);
		    tabCheckBox = WidgetFactory.makeCheckBox(TAB, false);
		    tabCheckBox.setActionCommand(TAB);
		    //tabCheckBox.setSelected(true);
		    delimitersPanel.add(tabCheckBox);
		    commaCheckBox = WidgetFactory.makeCheckBox(COMMA, false);
		    commaCheckBox.setActionCommand(COMMA);
		    delimitersPanel.add(commaCheckBox);
		    spaceCheckBox = WidgetFactory.makeCheckBox(SPACE, false);
		    spaceCheckBox.setActionCommand(SPACE);
		    delimitersPanel.add(spaceCheckBox);
		    semicolonCheckBox = WidgetFactory.makeCheckBox(SEMICOLON, false);
		    semicolonCheckBox.setActionCommand(SEMICOLON);
		    delimitersPanel.add(semicolonCheckBox);
		    otherCheckBox = WidgetFactory.makeCheckBox(OTHER, false);
		    otherCheckBox.setActionCommand(OTHER);
		    delimitersPanel.add(otherCheckBox);
		    otherDelimiterTextField = WidgetFactory.makeOneLineShortTextField();
		    otherDelimiterTextField.setText(EMPTYSTRING);
		   
		    delimitersPanel.add(otherDelimiterTextField);
		    delimitersPanel.setBorder(new javax.swing.border.EmptyBorder(WizardSettings.PADDING, 0, 0,
		            WizardSettings.PADDING));
		   
		    vbox.add(delimitersPanel);
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    
		    JPanel consecutivePanel = WidgetFactory.makePanel(1);	
		    consecutiveCheckBox = WidgetFactory.makeCheckBox(TREATCONSECUTIVE, false);
		    consecutiveCheckBox.setActionCommand(TREATCONSECUTIVE);
		    
		    consecutivePanel.add(consecutiveCheckBox);
		    consecutivePanel.setBorder(new javax.swing.border.EmptyBorder(WizardSettings.PADDING, 0, 0,
		            WizardSettings.PADDING));
		    vbox.add(consecutivePanel);
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    vbox.add(WidgetFactory.makeDefaultSpacer());
			registerListener();//it should called before initDelimiterCheckBox
		    
		    
			//intiDelimiterCheckBox only sets up check box correctly, 
            //it wouldn't cause any action since we unregister listener first, the register listener.
			// guessDelimiter method already was called in parseFile in TextImportEntity, so now we know the guessed delimiter
			initDelimiterCheckBox(textFile.getGuessedDelimiter());
		    delimiter = getRealDelimiterString();
		    Log.debug(30, "Parsing the delimitered table!!!!! Init the data panel with parsed data in init method");
		    textFile.parseDelimited(ignoreConsequtiveDelimiters, delimiter);
		    DataScrollPanel.getViewport().removeAll();
		    DataScrollPanel.getViewport().add(textFile.getTable());
		    vbox.add(DataScrollPanel);
		    
		    	   
		    
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
	   *  The action to be executed when the page is displayed. May be empty
	   */
	  public void onLoadAction()
	  {
		  WidgetFactory.unhiliteComponent(delimiterLabel); 
		  //WidgetFactory.unhiliteComponent(otherDelimiterTextField);
		  this.frame = frame;
		   //nextPageID = DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE;
		   if(this.frame == null)
		   {
			   Log.debug(5, "The WizardContainerFrame is null and we can't load TexImportDelimiters");
		       return;
			 
		   }
		   else
		   {
			   ImportedTextFile newTextFile = frame.getImportDataTextFile();
			   if(newTextFile == null)
			   {
				 
				   Log.debug(5, "The TextFile object is null and we can't load TexImportDelimiters");
				   return;
				   
			   }
			   else
			   {
				   //check if some values in previous page (ImportEntity) were changed
				   // the following codes handle click previous button to previous page and come back again(user may did some change in previous page)
				   int newDataStartingLineNumber = newTextFile.getDataStartingLineNumber();
				   Log.debug(30, "The init dataStartingLineNumber is "+initDataStartingLineNumber);
				   Log.debug(30, "The new dataStartingLineNumber is "+newDataStartingLineNumber);
				   boolean newColumnLabelsInStartingLine= newTextFile.isColumnLabelsInStartingLine();
				   Log.debug(30, "The init ColumnLabeIsInStartingLine is "+initColumnLabelsInStartingLine);
				   Log.debug(30, "The new ColumnLabeIsInStartingLine is"+newColumnLabelsInStartingLine);
				   if( !newTextFile.equals(textFile))
				   {
					   Log.debug(30, "Imported text file itself has been changed, we need to re-parse the file");
					   textFile = newTextFile;
					   textFile.addJTableModelChangeListener(this);
					   delimiter = textFile.getGuessedDelimiter();
					   initDelimiterCheckBox(textFile.getGuessedDelimiter());
					   forceUpdateDataPanel(getRealDelimiterString());
					   //force to clear cached TextImportAttribute pages
					   //clearCachedTextImportAttributePageInWizardFrame();					   
				   }
				   else if(newDataStartingLineNumber != initDataStartingLineNumber || newColumnLabelsInStartingLine != initColumnLabelsInStartingLine)
				   {
					   textFile = newTextFile;
					   textFile.addJTableModelChangeListener(this);
  				       Log.debug(30, "Some values in previous page(TextImportEntity) has been changed, we need to re-parse the file");
					   forceUpdateDataPanel(getRealDelimiterString());
				   }
				   else
				   {
					   Log.debug(30, "No values in previous page(TextImportEnity) has been changed, we don't need to parse table, but we need to repaint table.");
					   textFile.addJTableModelChangeListener(this);
					   //if we don't repaint table, we click back button in TextImportAttribute, we will get empty data panel.
					   repaintDataScrollPanel();
				   }
				   
				   //reset init value from previous page
				   initDataStartingLineNumber = newDataStartingLineNumber;
				   Log.debug(30, "Reset init dataStartingLineNumber with value "+newDataStartingLineNumber);
				   initColumnLabelsInStartingLine = newColumnLabelsInStartingLine;
				   Log.debug(30, "Reset init ColunLablesInStartingLine with value "+newColumnLabelsInStartingLine);
				  
			   }
			  
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
		  JTable table = textFile.getTable();
		  if(table == null)
		  {
			  Log.debug(5, "The table is null and we couldn't describe it");
			  return false;
		  }
		  else
		  {
			  boolean success = setImportAttributeSizeToWizardLibarary(table);
			  if(!success)
			  {
				  return false;
			  }
		  }
		  if (!tabCheckBox.isSelected() && !commaCheckBox.isSelected() && !spaceCheckBox.isSelected()
	                 && !semicolonCheckBox.isSelected()&& !otherCheckBox.isSelected()) 
		  {

				 	WidgetFactory.hiliteComponent(delimiterLabel);
				 	return false;
		  }
		  if (otherCheckBox.isSelected()==true) 
		  {

				 String otherTxt = otherDelimiterTextField.getText();
				 if (Util.isBlank(otherTxt)) {

					 WidgetFactory.hiliteComponent(otherDelimiterTextField);
					 otherDelimiterTextField.requestFocus();
					 return false;
				 }
			}
		  
		  return true;
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
		    String delimit = getDelimiterStringAsText();
		    om.put(xPathRoot
		           + "physical/dataFormat/textFormat/simpleDelimited/fieldDelimiter",
		           delimit);


		   if(this.ignoreConsequtiveDelimiters) 
		   {
			    TextImportEntity previousPage = (TextImportEntity) frame.getPreviousPage(this);
			    if(previousPage != null)
			    {
				    String physicalID = previousPage.getPhysicalID();
					// if consecutive delimiters need to be ignored, we have to add this information in the
					// additionalMetadata section.
					String addtHeader = "/eml:eml/additionalMetadata";
					if(!Util.isBlank(physicalID))
					{
						 om.put(addtHeader + "/describes", physicalID);
						 om.put(addtHeader + "/metadata/consecutiveDelimiters", "true");
						 hasAdditionalMetadataPart = true;
					}
					else
					{
						Log.debug(10, "We couldn't add additionalMetadataPart for consecutiveDelimiters since physicalID is unkonw!");
					}
			    }
				
		   }
		   Log.debug(35, "The ordered map in TextImportDelimiter.setPageData is "+om.toString());
		   return om;
	  }
	  
	  private static final String TABTEXT = "#x09";
	  private static final String COMMATEXT = ",";
	  private static final String SPACETEXT = "#x20";
	  private static final String SEMICOLONTEXT = ";";
       /*
        * Gets delimiter string as text
        */
	   private String getDelimiterStringAsText() 
	   {
		    String str = "";
		    if (tabCheckBox.isSelected())str = str + TABTEXT;
		    if (commaCheckBox.isSelected())str = str + COMMATEXT;
		    if (spaceCheckBox.isSelected())str = str + SPACETEXT ;
		    if (semicolonCheckBox.isSelected())str = str +SEMICOLONTEXT;
		    if (otherCheckBox.isSelected()) {
		      String temp = otherDelimiterTextField.getText();
		      if (temp.length() > 0) {
		        temp = temp.substring(0, 1);
		        str = str + temp;
		      }
		    }
		    return str;
		  }
	   
	   /*
	    * When we get a delimiter from eml, we need to select check box base 
	    * on the delimiter. It is for setPageData method
	    */
	   private boolean updateCheckBoxBaseOnEMLDelimiter(String delimiterStr)
	   {		   
		   boolean success = false;
		   if(delimiterStr != null)
		   {
			    unRegisterListener();
			    tabCheckBox.setSelected(false);
			    commaCheckBox.setSelected(false);
			    spaceCheckBox.setSelected(false);
			    semicolonCheckBox.setSelected(false);
			    otherDelimiterTextField.setText(EMPTYSTRING);
			    otherDelimiterTextField.setEnabled(false);
			    otherCheckBox.setSelected(false);
				registerListener();
			   if(delimiterStr.indexOf(TABTEXT)!= -1)
			   {
				   tabCheckBox.doClick();
				   delimiterStr = delimiterStr.replaceAll(TABTEXT, EMPTYSTRING);
			   }
			   
			   if(delimiterStr.indexOf(COMMATEXT) != -1)
			   {
				   commaCheckBox.doClick();
				   delimiterStr = delimiterStr.replaceAll(COMMATEXT, EMPTYSTRING);
			   }
			   
			   if(delimiterStr.indexOf(SPACETEXT) != -1)
			   {
				   spaceCheckBox.doClick();
				   delimiterStr = delimiterStr.replaceAll(SPACETEXT, EMPTYSTRING);
			   }
			   
			   if(delimiterStr.indexOf(SEMICOLONTEXT) != -1)
			   {
				   semicolonCheckBox.doClick();
				   delimiterStr = delimiterStr.replaceAll(SEMICOLONTEXT, EMPTYSTRING);
			   }
			   //now if delimiterStr still has something, it must be other delimiter			   
			   if(!delimiterStr.trim().equals(EMPTYSTRING))
			   {
				   otherCheckBox.doClick();
				   otherDelimiterTextField.setText(delimiterStr);
			   }
			   success = true;
		   }
		   return success;
	   }
       
	   /**
	    * If the metadata generate by this page has additionalMetadata part.
	    * @return true if it has additonal metadata.
	    */
	   public boolean hasAdditionalMetadataPart()
	   {
		   return hasAdditionalMetadataPart;
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
		  if (rootXPath == null )
		 {
		 	 rootXPath = this.xPathRoot;
		 }
		 Log.debug(32,"TexImportDelimiter.setPageData() called with rootXPath = " + rootXPath
		              + "\n Map = \n" +data);
		 String delimiterStr = (String)data.get(rootXPath+"/fieldDelimiter") ;
		 Log.debug(30, "The delimiter from eml is "+delimiterStr+" in TextImportDelimiter.setPage");
		 if(delimiterStr== null)
		 {
			 Log.debug(5, "Couldn't get delimiter from eml in TextImportDelimiter.setPageData");
			 return false;
		 }
		 // update check box base on the delimiter from eml
		 updateCheckBoxBaseOnEMLDelimiter(delimiterStr);
		 //get delimiter based on check box selection
		 delimiter = getRealDelimiterString();
		 String ignoreConsecutiveDelStr = (String)data.get(this.IGNORECONSECUTIVEDELIMITERS);
		 Log.debug(30, "Ignore consecutive delimiter is "+ignoreConsecutiveDelStr+" in TextImportDelimiter.setPageData");
		 try
		 {
			 this.ignoreConsequtiveDelimiters = (new Boolean(ignoreConsecutiveDelStr)).booleanValue();
		 }
		 catch(Exception e )
		 {
			 Log.debug(5, "Couldn't parse the string "+ignoreConsequtiveDelimiters+" into boolean value in TextImportDelimiter.setPageData method");
		     return false;
		 }
		 if(this.ignoreConsequtiveDelimiters && !consecutiveCheckBox.isSelected())
		 {
			 Log.debug(32, "TextImportDelimiter.setPageData the ignoreConsequtiveDelimiters is true, but conescutiveCheckBox is not selected, so we should click on it to select it");
			 consecutiveCheckBox.doClick();
		 }
		 else if (!this.ignoreConsequtiveDelimiters && consecutiveCheckBox.isSelected())
		 {
			 Log.debug(32, "TextImportDelimiter.setPageData the ignoreConsequtiveDelimiters is false, but conescutiveCheckBox is selected, so we should click on it to unselect it");
			 consecutiveCheckBox.doClick();
		 }
		 JTable table = textFile.getTable();
		 setImportAttributeSizeToWizardLibarary(table);
		 
		 return true;
	  }
	  
	  /**
	   * Method inherits from TableModelListener interface.
	   * This interface will be added to the JTable in ImportedTextFile.
	   * So when JTable data model is changed, the method will be called - clear every
	   * TExtImportAttribute page in WizardContainerFrame pageStack. Otherwise,
	   * we will still get old data model in those cached pages.
	   */
	  public void tableChanged(TableModelEvent e)
	  {
		  clearCachedTextImportAttributePageInWizardFrame();
	  }
	  
	  /**
	   * If ignore the consecutive delimiters.
	   * @return true if we want to ignore them
	   */
	  public boolean  ignoreConsequtiveDelimiters()
	  {
		  return this.ignoreConsequtiveDelimiters;
	  }
	  /*
	   * Removes any cached TextImportAttributePage in WizardFrame
	   */
	  private void clearCachedTextImportAttributePageInWizardFrame()
	  {
		  if(frame != null)
		  {
			  Log.debug(30, "Clear the cached TextTimportAttribute pages!!!!!!!!!!!!!!!!!!!!!!");
			  frame.cleanTextImportAttributePagesInCache();
		  }
	  }
	  
	  /*
	   * Set guessed Delimiter. First we will remove check box listener, then add them back.
	   * So in the initDelimiterCheckBox method will not cause any table parsing.
	   *
	   * @return String
	   */
	  private void initDelimiterCheckBox(String delim) {
	    //parseOn = false;
		unRegisterListener();
	    tabCheckBox.setSelected(false);
	    commaCheckBox.setSelected(false);
	    spaceCheckBox.setSelected(false);
	    semicolonCheckBox.setSelected(false);
	    otherDelimiterTextField.setText(EMPTYSTRING);
	    otherDelimiterTextField.setEnabled(false);
	    otherCheckBox.setSelected(false);
	    if (delim != null && delim.equals(TAB)) {
	      tabCheckBox.setSelected(true);
	      //parseOn = true;
	    } else if (delim != null && delim.equals(COMMA)) {
	      commaCheckBox.setSelected(true);
	     // parseOn = true;
	    } else if (delim != null && delim.equals(SPACE)) {
	      spaceCheckBox.setSelected(true);
	     // parseOn = true;
	    } else if (delim != null && delim.equals(SEMICOLON)) {
	      semicolonCheckBox.setSelected(true);
	      //parseOn = true;
	    } else if (delim != null && delim.equals(COLON)) {
	      otherCheckBox.setSelected(true);
	      otherDelimiterTextField.setEnabled(true);
	      otherDelimiterTextField.setText(":");
	      //parseOn = true;
	    } else {
	    	if(delim == null)
	    	{
	    		delim=EMPTYSTRING;
	    	}
	    	otherCheckBox.setSelected(true);
	    	otherDelimiterTextField.setEnabled(true);
		    otherDelimiterTextField.setText(delim);
		    //parseOn = true;
	    }
	    registerListener();
	  }
	  
	  /*
	   * Register listener to check box and text field.
	   */
	  private void registerListener()
	  {
		  if(commaCheckBox!= null)
		  {
		     commaCheckBox.addItemListener(checkBoxListener);
		  }
		  if(spaceCheckBox != null)
		  {
		     spaceCheckBox.addItemListener(checkBoxListener);
		  }
		  if(semicolonCheckBox != null)
		  {
		     semicolonCheckBox.addItemListener(checkBoxListener);
		  }
		  if(consecutiveCheckBox != null)
		  {
		     consecutiveCheckBox.addItemListener(checkBoxListener);
		  }
		  if(otherCheckBox != null)
		  {
			  otherCheckBox.addItemListener(checkBoxListener);
		  }
		  if(tabCheckBox != null)
		  {
		     tabCheckBox.addItemListener(checkBoxListener);
		  }
		  if(otherDelimiterTextField != null)
		  {
		     otherDelimiterTextField.addActionListener(textChangeListener);
		     otherDelimiterTextField.addFocusListener(textFocusChangeListener);
		  }
	  }
	  
	  /*
	   * Unregister listener to check box and text field
	   */
	  private void unRegisterListener()
	  {
		  if(commaCheckBox!= null)
		  {
		     commaCheckBox.removeItemListener(checkBoxListener);
		  }
		  if(spaceCheckBox != null)
		  {
		     spaceCheckBox.removeItemListener(checkBoxListener);
		  }
		  if(semicolonCheckBox != null)
		  {
		     semicolonCheckBox.removeItemListener(checkBoxListener);
		  }
		  if(consecutiveCheckBox != null)
		  {
		     consecutiveCheckBox.removeItemListener(checkBoxListener);
		  }
		  if(otherCheckBox != null)
		  {
			  otherCheckBox.removeItemListener(checkBoxListener);
		  }
		  if(tabCheckBox != null)
		  {
		     tabCheckBox.removeItemListener(checkBoxListener);
		  }
		  if(otherDelimiterTextField != null)
		  {
		     otherDelimiterTextField.removeActionListener(textChangeListener);
		     otherDelimiterTextField.removeFocusListener(textFocusChangeListener);
		  }
	  }
	  
	  /*
	   * Gets the delimiter from the selected check box
	   */
	  private String getRealDelimiterString() {
		    String str = "";
		    if (tabCheckBox.isSelected())str = str + "\t";
		    if (commaCheckBox.isSelected())str = str + ",";
		    if (spaceCheckBox.isSelected())str = str + " ";
		    if (semicolonCheckBox.isSelected())str = str + ";";
		    if (otherCheckBox.isSelected()) {
		      String temp = otherDelimiterTextField.getText();
		      if (temp.length() > 0) {
		        temp = temp.substring(0, 1);
		        str = str + temp;
		      }
		    }
		    return str;
		  }
	  
	  
	  /*
	   * Re-parse the data file and update data panel if it is necessary
	   */
	  private void updateDataPanel()
	  {
		  String newDelimiterFromPanel = getRealDelimiterString();
		  // newDelimiterFromPanel is different to the old one we reparse it
		  Log.debug(32, "The new delimiter from panel is "+newDelimiterFromPanel);
		  Log.debug(32, "The previous delimiter is "+delimiter);
		  if(!newDelimiterFromPanel.equals(delimiter))
		  {
			  Log.debug(32, "Since they are different, we need to update the data panel");
			  delimiter = newDelimiterFromPanel;			  
			  forceUpdateDataPanel(delimiter);
			  
		  }
		  else
		  {
			  Log.debug(32, "Since they are same, we need to nothing");
		  }
	  }
	  
	  /*
	   * force to re-parse the data file and update data panel
	   */
	  private void forceUpdateDataPanel(String delim)
	  {
		      Log.debug(30, "Parsing the delimitered table!!!!! Update the data panel.");
			  textFile.parseDelimited(ignoreConsequtiveDelimiters, delim);
			  repaintDataScrollPanel();
			  //force to clean the cached TextImportAttribute pages in Wizard frame since data model is changed
			   clearCachedTextImportAttributePageInWizardFrame();			  	
	  }
	  
	  /*
	   * Method to repaint DataScrollPanel
	   */
	  private void repaintDataScrollPanel()
	  {
		  DataScrollPanel.getViewport().removeAll();
		  DataScrollPanel.getViewport().add(textFile.getTable());
	  }
	  
	  /*
	   * Listener for check box selection
	   */
	  class CheckBoxItemListener implements java.awt.event.ItemListener {
		    public void itemStateChanged(java.awt.event.ItemEvent event) {
		      Object object = event.getSource();
		       if (object == tabCheckBox)
		        TabCheckBox_itemStateChanged(event);
		      else if (object == commaCheckBox)
		        CommaCheckBox_itemStateChanged(event);
		      else if (object == spaceCheckBox)
		        SpaceCheckBox_itemStateChanged(event);
		      else if (object == semicolonCheckBox)
		        SemicolonCheckBox_itemStateChanged(event);
		      else if (object == otherCheckBox)
		        OtherCheckBox_itemStateChanged(event);
		      else if (object == consecutiveCheckBox)
		        ConsecutiveCheckBox_itemStateChanged(event);
		    }
		  }


	   /*
	    * Set import attribute size to wizard libarary
	    */
       private boolean setImportAttributeSizeToWizardLibarary(JTable table)
       {
    	      boolean success = false;
    	      if(table == null)
    	      {
    	    	  Log.debug(5, "The JTable  is null and we couldn't set attribute size");
    	    	  return success;
    	      }
    	      int columnCount = table.getColumnCount();
			  WizardPageLibraryInterface pageLib = null;
			  if(frame != null)
			  {
				  pageLib= frame.getWizardPageLibrary();
			  }
			  if (pageLib != null)
			  {
				 Log.debug(30, "set attribute size "+columnCount+" into WizardPageLibary");
			     pageLib.setTextImportAttributePagesSize(columnCount);
			     success = true;
			  }
			  else
			  {
				  Log.debug(5, "The WizardPageLibrary is null and we couldn't set attribute size");
				  
			  }
			  return success;
       }

		private void TabCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
		    
			updateDataPanel();
		   
		  }


		private void CommaCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
		 
			updateDataPanel();

		    
		  }


		  private void SpaceCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
		  
			  updateDataPanel();
		    
		  }


		  private void SemicolonCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
		   
			  updateDataPanel();
		    
		  }


		  private void OtherCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
			 int stateChange = event.getStateChange();
			 boolean delim_other = (stateChange==ItemEvent.SELECTED);
			 Log.debug(32, "The selection status of other box is "+delim_other);
			 if(!delim_other)
			 {
				 //if unselect other check box, we need clear the other delimiter text field
				 otherDelimiterTextField.setText(EMPTYSTRING);
			 }
			 otherDelimiterTextField.setEnabled(delim_other);
		     updateDataPanel();		    
		  }

		  /*
		   * This method will force to update data panel even delimiter wasn't changed.
		   */
		  private void ConsecutiveCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
			  int stateChange = event.getStateChange();
			  ignoreConsequtiveDelimiters = (stateChange==ItemEvent.SELECTED);
			  Log.debug(30, "ignoreConsecutive delmiters value is "+ignoreConsequtiveDelimiters);
			  forceUpdateDataPanel(getRealDelimiterString());

		    
		  }
	  
		   /*
		    * Action listener for text field value change
		    * @author tao
		    *
		    */
		   class TextFieldChangeActionListener implements java.awt.event.ActionListener 
		   {

			    public void actionPerformed(java.awt.event.ActionEvent event) 
			    {
			      Object object = event.getSource();
			      if (object == otherDelimiterTextField)
			    	  otherDelimiterTextField_actionPerformed(event);
			    }
			  }

		  private void otherDelimiterTextField_actionPerformed(java.awt.event.ActionEvent event) 
		  {
			    
			    String str = otherDelimiterTextField.getText();
			    handleotherDelimiterTextChange(str);
	     }
		  
		 /*
		  * Action to handle starting line text change
		  */
		 private void handleotherDelimiterTextChange(String str)
		 {
			 if(str == null)
			 {
				 str = EMPTYSTRING;
			 }
			 otherDelimiterTextField.setText(str);
			 Log.debug(30, "The other delimiter text field value is "+otherDelimiterTextField.getText());
			 updateDataPanel();
		 }


	 /*
	  * Listener for text field focus change event
	  */
	 class TextFieldFocusChangeListener extends java.awt.event.FocusAdapter 
	 {
		    public void focusLost(java.awt.event.FocusEvent event) 
		    {
		      Object object = event.getSource();
		      if (object == otherDelimiterTextField)
		      {
		    	  otherDelimiterTextField_focusLost(event);
		      }
		    }
	 }

	 private void otherDelimiterTextField_focusLost(java.awt.event.FocusEvent event) 
	 {
		    String str =otherDelimiterTextField.getText();
		    handleotherDelimiterTextChange(str);
	  }
		  	  
		  
	  private JCheckBox tabCheckBox = null;
	  private JCheckBox commaCheckBox = null;
	  private JCheckBox spaceCheckBox = null;
	  private JCheckBox semicolonCheckBox = null;
	  private JCheckBox otherCheckBox = null;
	  private JTextField otherDelimiterTextField = null;
	  private JCheckBox consecutiveCheckBox = null;
	  private JScrollPane DataScrollPanel = new JScrollPane();
	  private JLabel delimiterLabel = null;

}
