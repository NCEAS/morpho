/*  '$Id$'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author$'
 *     '$Date$'
 * '$Revision$'
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
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

public class ReplaceDataPage extends AbstractUIPage {

  private final String pageID       = DataPackageWizardInterface.REPLACE_DATA_LOCATION;
  private final String pageNumber   = "1";

  private final String title      = "Replace Data Table Wizard";
  private final String subtitle   = "Data Location";

  private FileChooserWidget  fileChooserWidget;
  private JTextField entityName;

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  /**
   * Default constructor. Do nothing
   */
  
  public ReplaceDataPage() {
	  
    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JLabel desc = WidgetFactory.makeHTMLLabel(
    		"<b>Choose a new data file.</b> " +
    		"For data tables, attribute metadata will remain unchanged. " +
    		"Please make sure the attribute number and order are correct. " +
    		"Basic file metadata will be updated automatically (file size, type)", 4);
    this.add(desc);
    
    JPanel panel =  WidgetFactory.makePanel(5);
    panel.setLayout(new BorderLayout());
    WidgetFactory.addTitledBorder(panel, DataLocation.FILECHOOSER_PANEL_TITLE);
    fileChooserWidget = new FileChooserWidget(
    		DataLocation.FILE_LOCATOR_FIELD_FILENAME_LABEL,
    		DataLocation.FILE_LOCATOR_IMPORT_DESC_INLINE,
    		DataLocation.INIT_FILE_LOCATOR_TEXT);
    panel.add(fileChooserWidget);
    this.add(panel);
    
    this.add(WidgetFactory.makeDefaultSpacer());
    
    JPanel entityPanel = WidgetFactory.makePanel();
    entityPanel.add(WidgetFactory.makeLabel(Language.getInstance().getMessage("EntityName") + ":", false));
    entityName = WidgetFactory.makeOneLineTextField();
    entityPanel.add(entityName);
    entityPanel.add(WidgetFactory.makeLabel("(file name will be used if left blank)", false, null));
    
    this.add(entityPanel);

  }

  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

  }


  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() 
  {
	   
  }

  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty, but if so, must
   *  return true
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

     this.dataFileObj = validateDataFileSelection();
     return (dataFileObj != null);
  }

  /**
   * returns data file object
   *
   * @return data file object
   */
  public File getDataFile() { return this.dataFileObj; }

  private File validateDataFileSelection() {

    //ensure file URL is set
    String fileURL = fileChooserWidget.getImportFileURL();
    if (fileURL == null) {
      WidgetFactory.hiliteComponent(fileChooserWidget.getLabel());
      fileChooserWidget.getButton().requestFocus();
      return null;
    }
    WidgetFactory.unhiliteComponent(fileChooserWidget.getLabel());

    //ensure file URL is valid

  //UNFINISHED - ultimately need to handle remote urls (http, ftp etc?)
    File fileObj = null;
    try {
      fileObj = new File(fileURL.trim());
    } catch (Exception ex) {
      Log.debug(1, "error - cannot read your data file - "+fileURL+"!");
      ex.printStackTrace();
      WidgetFactory.hiliteComponent(fileChooserWidget.getLabel());
      fileChooserWidget.getButton().requestFocus();
      return null;
    }

    if (!fileObj.exists()) {
      Log.debug(1, "Error -  the importing data file "+fileObj.getAbsolutePath()+ " appears to have been moved or deleted!");
      WidgetFactory.hiliteComponent(fileChooserWidget.getLabel());
      fileChooserWidget.getButton().requestFocus();
      return null;
    }

    if (!fileObj.isFile()) {
      Log.debug(1, "error - selected location is a directory, not a file!");
      WidgetFactory.hiliteComponent(fileChooserWidget.getLabel());
      fileChooserWidget.getButton().requestFocus();
      return null;
    }

    if (!fileObj.canRead()) {
      Log.debug(1, "error - cannot read your data file (permissions problem?)");
      WidgetFactory.hiliteComponent(fileChooserWidget.getLabel());
      fileChooserWidget.getButton().requestFocus();
      return null;
    }
    return fileObj;
  }

  /**
   *  gets the OrderedMap object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the OrderedMap object that contains all the
   *            key/value paired settings for this particular wizard page   *
   * -----------------
   */
  protected OrderedMap returnMap = new OrderedMap();

  public OrderedMap getPageData() {

		returnMap.clear();

		File dataFile = getDataFile();
		if (dataFile != null) {
			// if datafile exists, it's a local file referenced by a URN
			returnMap.put(DataLocation.ONLINE_URL_XPATH, dataFile.getAbsolutePath());
			returnMap.put(DataLocation.OBJECTNAME_XPATH, dataFile.getName());
			returnMap.put(TextImportEntity.xPathRoot + "entityName", entityName.getText());
			
			long fileSize = dataFile.length();
			returnMap.put(TextImportEntity.xPathRoot + "physical/size", String.valueOf(fileSize));
			returnMap.put(TextImportEntity.xPathRoot + "physical/size/@unit", "byte");

			// is it a text file (get more metadata)
			ImportedTextFile importedTextFile = new ImportedTextFile(dataFile);
			if (importedTextFile.isTextFile()) {
				importedTextFile.parsefile();
				returnMap.put(TextImportEntity.xPathRoot + "numberOfRecords",
						String.valueOf(importedTextFile.getNlines_actual()));
			}

		}

		return returnMap;
	}


  /**
   * gets the Map object that contains all the key/value paired settings for
   * this particular wizard page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular wizard page
   */
  public OrderedMap getPageData(String rootXPath) {

    throw new UnsupportedOperationException(
      "getPageData(String rootXPath) Method Not Implemented");
  }

  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return pageID; }

  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public String getTitle() { return title; }

  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public String getSubtitle() { return subtitle; }

  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID() { return nextPageID; }


  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  /**
   * Populate the metadata from eml to this page.
   * However, this page needs some additional metadata such as last event,
   * text data file full path and et al.
   */
  public boolean setPageData(OrderedMap map, String _xPathRoot)
  {
	 boolean empty = false;
	 if(map == null)
	  {
		  Log.debug(30, "The map in DataLocation.setPageData and return false");
		  return empty;
	  }

     String objectName = null;
     objectName = (String)map.get(_xPathRoot+"/objectName");
     Log.debug(32, "The object name in the map is "+objectName+ " in DataLocation.setPageData");
     String onlineUrl = null;
     String offlineMediumName = null;
     onlineUrl = (String)map.get(_xPathRoot+"/distribution/online/url");
     Log.debug(32, "The oneline url is "+onlineUrl+" in DataLocation.setPageData");
     offlineMediumName = (String)map.get(_xPathRoot+"/distribution/offline/mediumName");
     Log.debug(32, "The offline mediu name is "+offlineMediumName+" in DataLocation.setPageData");
 
     empty = true;
     
	 return empty;
  }

////////////////////////////////////////////////////////////////////////////////
// variables and non-editable constants
////////////////////////////////////////////////////////////////////////////////

  protected JPanel onlinePanel;
  protected JPanel offlinePanel;
  protected JPanel nodataPanel;
  protected JPanel blankPanel;
  private File   dataFileObj = null;

  protected final short DESCRIBE_MAN_NODATA   = 30;
  protected final short DESCRIBE_MAN_ONLINE   = 32;
  protected final short DESCRIBE_MAN_OFFLINE  = 34;

  protected short distribution;

 
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////



  protected String getFileName() {
    String ret = "";
    File file = getDataFile();
    if (file!=null) {
      ret = file.getName();
    }
    return ret;
  }


}

