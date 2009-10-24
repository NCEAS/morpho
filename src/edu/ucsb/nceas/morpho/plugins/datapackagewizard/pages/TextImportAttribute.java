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
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardUtil;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

/**
 * This page will display some entity information and data table itself for text importing.
 * The metadata information is entityName and entityDescription.
 * @author tao
 *
 */
public class TextImportAttribute extends AbstractUIPage 
{
	   public static final int FIRSTINDEX = 0;
	 //number types for interval/ratio number type
	   private static final String[] numberTypesArray = new String[] {
	                         "NATURAL (non-zero counting numbers: 1, 2, 3..)",
	                         "WHOLE  (counting numbers & zero: 0, 1, 2, 3..)",
	                         "INTEGER (+/- counting nums & zero: -2, -1, 0, 1..)",
	                         "REAL  (+/- fractions & non-fractions: -1/2, 3.14..)"};
	   private String pageID = DataPackageWizardInterface.TEXT_IMPORT_FIRST_ATTRIBUTE;
	   private String title = "Text Import";
	   private String subTitle = null;
	   private String pageNumber = null;
	   private WizardContainerFrame frame = null;
	   private ImportedTextFile textFile = null;
	   private WizardPageLibrary wizardPageLib = null;
	   private int columnIndex = 0;
	   private JTable table = null;
	   // Column Model of the table containting all the columns
	   private TableColumnModel fullColumnModel = null;
	   private AttributePage attributePage = null;
	   
	   
	   /**
	    * Construct
	    * @param frame
	    */
	   public TextImportAttribute(WizardContainerFrame frame,int columnIndex)
	   {
		   this.frame = frame;
		   if(this.frame == null)
		   {
			   Log.debug(5, "The WizardContainerFrame is null and we can't initialize TexImportAttribute");
		       return;
			 
		   }
		   else
		   {
			   textFile = frame.getImportDataTextFile();
			   if(textFile == null)
			   {
				 
				   Log.debug(5, "The TextFile object is null and we can't initialize TexImportAttribute");
				   return;
				   
			   }
			  
		   }
		   this.wizardPageLib = new WizardPageLibrary(frame);
		   this.columnIndex = columnIndex;
		   init();
	   }
	   
	   /*
	    * Sets up table model
	    */
	   private void setupTableModel()
	   {
		   
		   this.table = textFile.getTable();
		   this.fullColumnModel = textFile.getFullColumnModel();
		   if(table != null && fullColumnModel != null)
		   {
			   TableColumnModel model = new DefaultTableColumnModel();
			   model.addColumn(fullColumnModel.getColumn(columnIndex));
			   DefaultListSelectionModel dlsm = new DefaultListSelectionModel();
			   dlsm.setSelectionInterval(0, 0);
			   model.setColumnSelectionAllowed(true);
			   model.setSelectionModel(dlsm);
			   table.setColumnModel(model);
			   table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			   table.doLayout();
		   }
		   
	   }
	   
	   /*
	    * Init the panels
	    */
	   private void init()
	   {
		    this.setLayout(new BorderLayout());
		    JPanel vbox = this;
		    columnDataScrollPanel.setPreferredSize(new Dimension(80, 4000));
		    //updateColumnDataPanel()
		    vbox.add(columnDataScrollPanel, BorderLayout.WEST);
		    attributePage = (AttributePage) wizardPageLib.getPage(DataPackageWizardInterface.ATTRIBUTE_PAGE);
		    vbox.add(attributePage, BorderLayout.CENTER);
	   }
	   
	   /*
	    * Update data model and data panel
	    */
	   private void updateColumnDataPanel()
	   {
		   setupTableModel();
		   repaintColumnDataPanel();
	   }
	   
	   /*
	    * Force update data panel
	    */
	   private void repaintColumnDataPanel()
	   {
		   Log.debug(32, "The column data panel in TextTimportAttribute class is updating ===========");
		   columnDataScrollPanel.getViewport().removeAll();
		   columnDataScrollPanel.getViewport().add(table);
		   //columnDataScrollPanel.setVisible(true);
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
	   *  Sets the unique ID for this UI page
	   *
	   *  @return   the unique ID String for this UI page
	   */
	  public void setPageID(String pageID)
	  {
		 this.pageID =pageID; 
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
	   * Sets a dynamic id for next pageID
	   *
	   *  @return the String ID of the page that the user will see next, or null if
	   *  this is te last page
	   */
	  public void setNextPageID(String nextPageID)
	  {
		  this.nextPageID = nextPageID;
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
		  Log.debug(30, "The TextImportAttribute page has the index "+columnIndex);
		  updateColumnDataPanel();
		  updateAttributePanel();
	  }


	  /**
	   *  The action to be executed when the "Prev" button is pressed. May be empty
	   *
	   */
	  public  void onRewindAction()
	  {
		  if (table != null && columnIndex == FIRSTINDEX )
		  {
			  table.setColumnModel(fullColumnModel);
			  table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			  table.doLayout();
		  }
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
		  if(attributePage != null)
		  {
		     return attributePage.onAdvanceAction();
		  }
		  else
		  {
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
		  OrderedMap map = null;
		  if(attributePage != null)
		  {
			  //ordered map indext start from 1, but attribute index start 0, so we need to plus 1
			  int index = columnIndex+1;
			  map = attributePage.getPageData(AttributeSettings.Attribute_xPath + "[" + index + "]");
		  }
		  return map;
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
		  OrderedMap map = null;
		  return map;
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
	  
	  /**
	   * Fills some metadata into Attribute page
	   */
	  private void updateAttributePanel()
	  {
		  Vector colTitles = textFile.getColumnTitlesVector();
		  Vector vectorOfData = textFile.getVectorOfData();
		  fillAttributePageData(attributePage, columnIndex, colTitles, vectorOfData);
		  attributePage.validate();
		  attributePage.repaint();
	  }
	  
	  /*
	   * Fills some metadata into the given AttributePage base on column number, column title and data
	   */
	  private void fillAttributePageData(AttributePage ad, int attrNum, Vector colTitles, Vector vectorOfData) {

		    String type = guessColFormat(attrNum, vectorOfData);
		    OrderedMap map = ad.getPageData(AttributeSettings.Attribute_xPath);
		    map.put(AttributeSettings.AttributeName_xPath, colTitles.elementAt(attrNum));

		    // either nominal/ordinal  . We guess as  nominal
		    if (type.equals("text")) {

		      Vector unique = getUniqueColValues(attrNum, vectorOfData);
		      Enumeration en = unique.elements();
		      int pos = 1;
		      while (en.hasMoreElements()) {
		        String elem = (String)en.nextElement();
		        String path = AttributeSettings.Nominal_xPath
		        + "/enumeratedDomain[1]/codeDefinition[" + pos
		        + "]/code";
		        map.put(path, elem);
		        path = AttributeSettings.Ordinal_xPath
		        + "/enumeratedDomain[1]/codeDefinition[" + pos + "]/code";
		        map.put(path, elem);
		        pos++;
		      }
		      ad.setPageData(map, null);
		    }

		    else if (type.equals("float")) {
		      String numberTypePath = AttributeSettings.Interval_xPath
		      + "/numericDomain/numberType";
		      map.put(numberTypePath, numberTypesArray[3]);
		      numberTypePath = AttributeSettings.Ratio_xPath
		      + "/numericDomain/numberType";
		      map.put(numberTypePath, numberTypesArray[3]);
		      ad.setPageData(map, null);
		    }

		    else if (type.equals("integer")) {

		      String numType = guessNumberType(attrNum, vectorOfData);

		      if (numType.equals("Natural")) {
		        String numberTypePath = AttributeSettings.Interval_xPath
		        + "/numericDomain/numberType";
		        map.put(numberTypePath, numberTypesArray[0]);
		        numberTypePath = AttributeSettings.Ratio_xPath
		        + "/numericDomain/numberType";
		        map.put(numberTypePath, numberTypesArray[0]);
		      } else if (numType.equals("Whole")) {
		        String numberTypePath = AttributeSettings.Interval_xPath
		        + "/numericDomain/numberType";
		        map.put(numberTypePath, numberTypesArray[1]);
		        numberTypePath = AttributeSettings.Ratio_xPath
		        + "/numericDomain/numberType";
		        map.put(numberTypePath, numberTypesArray[1]);
		      } else if (numType.equals("Integer")) {
		        String numberTypePath = AttributeSettings.Interval_xPath
		        + "/numericDomain/numberType";
		        map.put(numberTypePath, numberTypesArray[2]);
		        numberTypePath = AttributeSettings.Ratio_xPath
		        + "/numericDomain/numberType";
		        map.put(numberTypePath, numberTypesArray[2]);
		      }
		      ad.setPageData(map, null);
		    } else if (type.equals("date")) {
		      map.put(AttributeSettings.DateTime_xPath + "/dateTimePrecision",
		      new String("0"));
		      ad.setPageData(map, null);
		    }


		  }
	  
	  /*
	   * Guesses the number type in index k at data vector
	   */
	  private String guessNumberType(int k, Vector vec) {
		    Vector v = getUniqueColValues(k, vec);
		    Enumeration e = v.elements();
		    boolean zero = false;
		    boolean neg = false;
		    while (e.hasMoreElements()) {
		      String s = (String)e.nextElement();
		      try {
		        int num = Integer.parseInt(s);
		        if (num == 0)zero = true;
		        if (num < 0)neg = true;
		      } catch (Exception ex) {
		        return "";
		      }
		    }
		    if (neg)return "Integer";
		    if (zero)return "Whole";
		    return "Natural";
		  }


		  /*
		   * creates a Vector containing all the unique items (Strings) in the column
		   *
		   * @param colNum int
		   * @return Vector
		   */
		  Vector getUniqueColValues(int colNum, Vector vec) {
		    Vector res = new Vector();
		    for (int i = 0; i < vec.size(); i++) {
		      Vector v = (Vector)vec.elementAt(i);
		      String str = (String)v.elementAt(colNum);
		      if (!str.equals("")) { // ignore empty strings
		        if (!res.contains(str)) {
		          res.addElement(str);
		        }
		      }
		    }
		    return res;
		  }
		  
		  /*
		   * guesses column type based on frequency of content types include text,
		   * integer, floating point number, and date. Guess is based on frequency of
		   * occurance
		   *
		   * @param colNum column number
		   * @return String
		   */
		  String guessColFormat(int colNum, Vector vectorOfData) {
		    int minInt = 0;
		    int maxInt = 0;
		    double minDouble = Double.POSITIVE_INFINITY;
		    double maxDouble = Double.NEGATIVE_INFINITY;
		    int emptyCount = 0;
		    int dateCount = 0;
		    int numericCount = 0;
		    double numericAverage = 0.0;
		    double numericSum = 0.0;
		    boolean doublePresent = false;
		    for (int i = 0; i < vectorOfData.size(); i++) {
		      Vector v = (Vector)vectorOfData.elementAt(i);
		      String str = (String)v.elementAt(colNum);
		      if (str.trim().length() < 1) {
		        emptyCount++;
		      } else {
		        boolean isInt = WizardUtil.isInteger(str);
		        boolean isDbl = WizardUtil.isDouble(str);
		        if (isInt || isDbl) {
		          numericCount++;
		          if (isDbl)
		            doublePresent = true;
		          double d = Double.parseDouble(str);
		          minDouble = Math.min(minDouble, d);
		          maxDouble = Math.max(maxDouble, d);
		          numericSum += d;
		        }

		        if (WizardUtil.isDate(str)) {
		          dateCount++;
		        }
		      }
		    }
		    numericAverage = numericSum / numericCount;
		    if ((numericCount > 0)
		        && ((100 * (numericCount + emptyCount) / vectorOfData.size())) > 90) {
		      if (doublePresent)return "float";
		      else return "integer";
		    }

		    else if ((dateCount > 0)
		             && ((100 * (dateCount + emptyCount) / vectorOfData.size())) > 90) {
		      return "date";
		    }
		    return "text";
		  }

	  
	  
	  
	 
	  private JScrollPane columnDataScrollPanel = new JScrollPane();

}
