/**
 *  '$RCSfile: CodeImportPage.java,v $'
 *    Purpose: A class that handles the importing of new tables for taxonomical
 *						lookup for attributes
 *  	Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
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

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AccessionNumber;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This page will let user to choose to import code/definition from 
 * a existing table or from  a new table. If from a new table, it will
 * start DataLocation page to import a new table. The new import code/definition
 * table will be ended at CodeDefintion page.
 * @author tao
 *
 */
public class CodeImportPage extends AbstractUIPage {
	
    /**
     *Import Language into Morpho
     *by pstango 2010/03/15 
     */
    public static Language lan = new Language();	

  public final String pageID     = DataPackageWizardInterface.CODE_IMPORT_PAGE;
  public final String pageNumber = "";

  public final String title      = /*"Code Defintions Import Page"*/ lan.getMessages("CodeDefintionsImportPage");
  public final String subtitle   = /*"Import/Define the codes and definitions"*/ lan.getMessages("CodeImportPage.subtitle");

  private WizardContainerFrame mainWizFrame;
  private OrderedMap resultsMap;

  private String[] importChoiceText = {
		  /*"The table containing the definitions has already been imported into Morpho"*/ lan.getMessages("CodeImportPage.importChoiceText_1"),
  		  /*"The table containing the definitions needs to be imported into Morpho"*/ lan.getMessages("CodeImportPage.importChoiceText_2")
		  };

  private boolean importCompletedOK = false;

  private CodeDefnPanel importPanel = null;
  private JPanel radioPanel;

  private JTextField attrField;
  private JTextField entityField;

  private JLabel choiceLabel;
  private ArrayList removedAttributeInfo = null;
  private AbstractDataPackage adp = null;
  private String handledImportAttributeName = null;

  private short importChoice = 0;
  public static final short IMPORT_DONE = 10;
  public static final short INVOKE_TIW = 20;
  private int selectedCodeColumnIndex = -1;
  private int selectedDefinitionColumnIndex = -1;
  public static String IMMPORTCHOICE = "importChoice";
  public static String ENTITYINDEXINCREASED = "entityIndexIncreased";
  public static String ISENTITYADDEDINPREVIOUSCLYCLE = "isEntityAddedInPreviousCycle";
  private static final int SLEEPINGTIME = 1000;
  private static final int TIME =20;
  private boolean entityIndexIncreased = false;
  private boolean isEntityAddedInPreviousCycle = true;

  public CodeImportPage(WizardContainerFrame mainWizFrame) {

    this.mainWizFrame = mainWizFrame;
    nextPageID = DataPackageWizardInterface.DATA_LOCATION;
    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    setLayout(new BorderLayout());

    JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
    JLabel label = WidgetFactory.makeHTMLLabel(/*"Import Codes and Definitions for the following Attribute - "*/ lan.getMessages("CodeImportPage.Desc") + " - ",
    											1, false);

    JLabel attrLabel = WidgetFactory.makeLabel(/*"Attribute Name:"*/ lan.getMessages("AttributeName") + ":",
    											false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    attrField = WidgetFactory.makeOneLineTextField();
    attrField.setEditable(false);

    JPanel attrPanel = WidgetFactory.makePanel();
    attrPanel.add(attrLabel);
    attrPanel.add(attrField);

    JLabel entityLabel = WidgetFactory.makeLabel(/*"Entity Name:"*/ lan.getMessages("EntityName") + ":",
    											false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    entityField = WidgetFactory.makeOneLineTextField();
    entityField.setEditable(false);

    JPanel entityPanel = WidgetFactory.makePanel();
    entityPanel.add(entityLabel);
    entityPanel.add(entityField);

    infoPanel.add(label);
    infoPanel.add(WidgetFactory.makeDefaultSpacer());
    infoPanel.add(attrPanel);
    infoPanel.add(WidgetFactory.makeDefaultSpacer());
    infoPanel.add(entityPanel);
    infoPanel.add(WidgetFactory.makeDefaultSpacer());

    choiceLabel = WidgetFactory.makeHTMLLabel(/*"Select one of the following"*/ lan.getMessages("CodeImportPage.choiceLabel"),
    											1,true);
    infoPanel.add(choiceLabel);
    infoPanel.add(WidgetFactory.makeDefaultSpacer());

    ActionListener listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "got radiobutton command: "+e.getActionCommand());

        if (e.getActionCommand().equals(importChoiceText[0])) {
          if(importPanel == null) {
            importPanel = new CodeDefnPanel(true);
            CodeImportPage.this.add(importPanel, BorderLayout.CENTER);
            CodeImportPage.this.validate();
            CodeImportPage.this.repaint();
          }
          importPanel.setVisible(true);
          importChoice = IMPORT_DONE;
        } else if (e.getActionCommand().equals(importChoiceText[1])) {
          if(importPanel != null)
            importPanel.setVisible(false);
          importChoice = INVOKE_TIW;
        }
      }
    };

    radioPanel = WidgetFactory.makeRadioPanel(importChoiceText, -1, listener);

    infoPanel.add(radioPanel);

    add(infoPanel, BorderLayout.NORTH);

    //add(importPanel, BorderLayout.CENTER);
    //importPanel.setVisible(false);
  }

  /*public void addAttributeForImport(String entityName, String attributeName, String scale, OrderedMap map, String xPath, boolean newTable) {
		
		AbstractDataPackage	adp = getADP();
		if(adp == null) {
			Log.debug(15, "Unable to obtain the ADP in CodeImportPage");
			return;
		}
    adp.addAttributeForImport(entityName, attributeName, scale, map, xPath, newTable);
  }*/

  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
	    //removedAttributeInfo = null;
		adp = getADP();
		if(adp == null) {
			Log.debug(15, "Unable to obtain the ADP in CodeImportPage");
			return;
		}
		if(removedAttributeInfo != null)
		{
			adp.addFirstAttributeForImport(removedAttributeInfo);
		}
		
		  //since we increase entity in advanceAction in CodeImportPage
      // and this page couldn't go back (back button was disabled.
		  //so we can decrease it when it loads again (click backup in the next package)
      if(entityIndexIncreased)
      {      
         int entityIndex = mainWizFrame.getEnityIndex();
          entityIndex = entityIndex-1;
          Log.debug(30, "On CodeImportPage.onLoad method, we need to decrease entity index to "+entityIndex);
          mainWizFrame.setEntityIndex(entityIndex);
      }
		
	  handledImportAttributeName = adp.getCurrentImportAttributeName();
    String entity = adp.getCurrentImportEntityName();

    attrField.setText(handledImportAttributeName);
    entityField.setText(entity);
    mainWizFrame.setButtonsStatus(true,false, true, false);//couldn't back again.
    

  }

  private AbstractDataPackage getADP() {
		
    return mainWizFrame.getAbstractDataPackage();
  }

  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   */
  public void onRewindAction() {

	 //adp.addFirstAttributeForImport(removedAttributeInfo);
	  removedAttributeInfo = null;
 
  }

  /**
   *  The action to be executed when the "Next" button is pressed.
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    if(importChoice != IMPORT_DONE && importChoice != INVOKE_TIW) {
      WidgetFactory.hiliteComponent(choiceLabel);
      return false;
    }
    WidgetFactory.unhiliteComponent(choiceLabel);
    mainWizFrame.setImportCodeDefinitionTable(true);
    Log.debug(40, "Set the importwizard to be true in CodeImportPage.setPage");
    //figure out if it need to increase id here.
    AbstractUIPage previousPage = mainWizFrame.getPreviousPage();
    //boolean needIncreaseEntityID = true;
    if(previousPage != null && previousPage instanceof CodeImportSummary)
    {
      CodeImportSummary codeSummary = (CodeImportSummary )previousPage;
      isEntityAddedInPreviousCycle = codeSummary.isEntityAddedInCyle();
      Log.debug(30, "In CodeImportPage.onAdvance method, the isEntityAddedInCycle value from previous ImportSummary page is "+isEntityAddedInPreviousCycle);
    
    }
    //since we will start a new entity, we need to increase the index.
    int entityIndex = mainWizFrame.getEnityIndex();
    if(isEntityAddedInPreviousCycle)
    {
      entityIndex = entityIndex+1;
      mainWizFrame.setEntityIndex(entityIndex);
      entityIndexIncreased = true;
      Log.debug(30, "In CodeImportPage.onAdvance to increase entity id to "+entityIndex);
    }
    else
    {
      Log.debug(30, "In CodeImportPage.onAdvance to keep (NOT increase) entity id to "+entityIndex);
    }
    
    if(importChoice == IMPORT_DONE) {
      if(importPanel.validateUserInput()) {
		//AbstractDataPackage	adp = getADP();
		if(adp == null) {
			Log.debug(15, "Unable to obtain the ADP in CodeImportPage");
			return false;
		}
        OrderedMap map = adp.getCurrentImportMap();
        CodeDefinition.replaceEmptyReference(map,importPanel, "CodeImportPage");
        if(adp.isCurrentImportNewTable())
        {
      	  Log.debug(30, "====it is in current import new table and update attribute reference in CodeImportPage"); 
      	  CodeDefinition.updateImportAttributeInNewTable(adp, map);
  
        }
        removedAttributeInfo = adp.removeFirstAttributeForImport();
        mainWizFrame.setEditingAttributeMapFromRemovedImportAttribute(removedAttributeInfo);
        //selectedEntityIndex = importPanel.getSelectedEntityIndex();
        selectedCodeColumnIndex = importPanel.getSelectedCodeColumnIndexInTable();
        selectedDefinitionColumnIndex = importPanel.getSelectedDefColumnIndexInTable();
        nextPageID = DataPackageWizardInterface.CODE_IMPORT_SUMMARY;     
        
        mainWizFrame.clearPageCache();
        mainWizFrame.reInitializePageStack();
        Log.debug(35, "In CodeImportPage.onAdvance the value of imported attribute  "+adp.getAttributeImportCount());
        return true;
      } else
        return false;
    } else {
      this.nextPageID = DataPackageWizardInterface.DATA_LOCATION;  
      removedAttributeInfo = null;
      mainWizFrame.clearPageCache();
      mainWizFrame.reInitializePageStack(); 
      Log.debug(35, "In CodeImportPage.onAdvance the value of imported attribute  "+adp.getAttributeImportCount());
      return true;
    }

  }

  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  public OrderedMap getPageData() { return resultsMap; }

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

    public boolean setPageData(OrderedMap data, String xPathRoot) 
    {
      boolean success = false;
      
      if(data == null)
      {
        Log.debug(30, "The map in CodeImportPage.setPageData and return false");
        return success;
      }    
      Log.debug(35, "In CodeImportPage.setPageData, the xpathRoot is "+xPathRoot+" and map is "+data.toString() );
      String importChoiceStr = (String)data.get(IMMPORTCHOICE);
      Log.debug(35, "The importChoice in CodeImportPage.setPageData method is "+importChoiceStr);
      data.remove(IMMPORTCHOICE);
      String entityIndexIncreasedStr = (String)data.get(ENTITYINDEXINCREASED);
      Log.debug(35, "The entityIndexIncreased value in CodeImportPage.setPageData method is "+entityIndexIncreasedStr);
      data.remove(ENTITYINDEXINCREASED);
      String isEntityAddedInPreviousCycleStr = (String)data.get(ISENTITYADDEDINPREVIOUSCLYCLE);     
      try
      {
        importChoice =(new Short(importChoiceStr)).shortValue();
        entityIndexIncreased = (new Boolean(entityIndexIncreasedStr)).booleanValue();
        isEntityAddedInPreviousCycle = (new Boolean(isEntityAddedInPreviousCycleStr)).booleanValue();
      }
      catch(Exception e)
      {
        Log.debug(30, "Couldn't get importChocie or entityIndexIncreased or isEntityAddedInPreviousCycle value in CodeImportPage.setPageData since "+e.getMessage());
        return success;
      }
      if(importChoice == INVOKE_TIW)
      {
        Log.debug(30, "In create another new table wizard branch at CodeImportPage.setPageData method");
        try
        {
          clickRadionButton(radioPanel, 1);
        }
        catch(Exception e)
        {
          Log.debug(30, "Morpho couldn't click the radion button for the choice -Import Done or - Import a New Table");
          return success;
        }      
        success = true;
        mainWizFrame.setImportCodeDefinitionTable(true);
        return success;
      }
      else if(importChoice == IMPORT_DONE)
      {
       
        Log.debug(30, "In import is done branch at CodeImportPage.setPageData method");
        //String selectedEntityIndexStr = (String)data.get(CodeDefnPanel.SELECTEDENTITYINDEX);
        //data.remove(CodeDefnPanel.SELECTEDENTITYINDEX);
        String codeColumnStr = (String)data.get(CodeDefnPanel.CODECOLUMNINDEX);
        data.remove(CodeDefnPanel.CODECOLUMNINDEX);
        Log.debug(30, "The code column index is "+codeColumnStr+" int CodeImportPage.setPage method");
        String definitionColumnStr = (String)data.get(CodeDefnPanel.DEFINITIONCOLUMNINDEX);
        data.remove(CodeDefnPanel.DEFINITIONCOLUMNINDEX);
        Log.debug(30, "The definition column index is "+definitionColumnStr+" int CodeImportPage.setPage method");
        try
        {
          clickRadionButton(radioPanel, 0);
          selectedCodeColumnIndex = (new Integer(codeColumnStr)).intValue();         
          selectedDefinitionColumnIndex = (new Integer(definitionColumnStr)).intValue();
        }
        catch(Exception e)
        {
          Log.debug(30, "Couldn't get importChocie entity in CodeImportPage.setPageData since "+e.getMessage());
          return success;
        }
        Log.debug(35, "Before generating removedAttributeInfo in CodeImportPage.setPageData");
        removedAttributeInfo = extactRemovedImportAttribute(data);
        Log.debug(35, "AFter generating removedAttributeInfo in CodeImportPage.setPageData");
        if(removedAttributeInfo == null)
        {
          Log.debug(30, "In CodeImportPage.setPageData, morpho couldn't get the removed the import attribute info.");
          return success;
        }
        try
        {
          int index = 0;
          while(importPanel == null && index <TIME)
          {
             try
             {
               Thread.sleep(1000);
               index++;
             }
             catch(Exception e)
             {
                Log.debug(30, "Main thread couldn't sleep in CodeImportPage.setPageData");
             }
          }
          if(importPanel != null)
          {
            
            Log.debug(35, "Before selecting the code and defintion columns in CodeImportPage.setPageData "+selectedCodeColumnIndex);
            int[] columnsIndex = {selectedCodeColumnIndex, selectedDefinitionColumnIndex};//the first is code, the second is definition
            importPanel.setSelectedCodeDefColumnInTable(columnsIndex);
            Log.debug(35, "After selecting the code and defintion columns in CodeImportPage.setPageData "+selectedCodeColumnIndex);
          }
          else
          {
            Log.debug(30, "Couldn't get import panel in CodeImmportPage.setPageData");
            return success;
          }
        }
        catch(Exception e)
        {
          Log.debug(30, "Morpho couldn't  select code-definition table");
          return success;
        }
        
        success = true;
        mainWizFrame.setImportCodeDefinitionTable(true);//when this page shows up, it is definitely a importCodeDefinitionTable
        return success;
      }
      else
      {
        Log.debug(30, "In CodeImportPage.setPageData, morpho couldn't understand the import choice "+importChoice);
        return success;
      }
      
       
    }
    
    /**
     * Extract import attribute from a map like:
     * /additionInfo/removedImortAttribute[1]/attribute[1]/entityName[1]   =  test-2
     * /additionInfo/removedImortAttribute[1]/attribute[1]/attributeName[1]   =  Column 1
     * /additionInfo/removedImortAttribute[1]/attribute[1]/scale[1]   =  Nominal
     * /additionInfo/removedImortAttribute[1]/attribute[1]/orderedMap[1]/pair[1]/key[1]   =  /eml:eml/dataset/dataTable/attributeList/attribute[1]/attributeName
     * /additionInfo/removedImortAttribute[1]/attribute[1]/orderedMap[1]/pair[1]/value[1]   =  Column 1
     * /additionInfo/removedImortAttribute[1]/attribute[1]/orderedMap[1]/pair[2]/key[1]   =  /eml:eml/dataset/dataTable/attributeList/attribute[1]/attributeDefinition
     * /additionInfo/removedImortAttribute[1]/attribute[1]/orderedMap[1]/pair[2]/value[1]   =  1
     * /additionInfo/removedImortAttribute[1]/attribute[1]/orderedMap[1]/pair[3]/key[1]   =  /eml:eml/dataset/dataTable/attributeList/attribute[1]/measurementScale/nominal/nonNumericDomain/enumeratedDomain[1]/entityCodeList/entityReference
     * /additionInfo/removedImortAttribute[1]/attribute[1]/orderedMap[1]/pair[3]/value[1]   =  1258843292741
     * /additionInfo/removedImortAttribute[1]/attribute[1]/orderedMap[1]/pair[4]/key[1]   =  /eml:eml/dataset/dataTable/attributeList/attribute[1]/measurementScale/nominal/nonNumericDomain/enumeratedDomain[1]/entityCodeList/valueAttributeReference
     * /additionInfo/removedImortAttribute[1]/attribute[1]/orderedMap[1]/pair[4]/value[1]   =  1258843292742
     * /additionInfo/removedImortAttribute[1]/attribute[1]/orderedMap[1]/pair[5]/key[1]   =  /eml:eml/dataset/dataTable/attributeList/attribute[1]/measurementScale/nominal/nonNumericDomain/enumeratedDomain[1]/entityCodeList/definitionAttributeReference
     * /additionInfo/removedImortAttribute[1]/attribute[1]/orderedMap[1]/pair[5]/value[1]   =  1258843292743
     * /additionInfo/removedImortAttribute[1]/attribute[1]/xPath[1]   =  /eml:eml/dataset/dataTable/attributeList/attribute[0]
     * /additionInfo/removedImortAttribute[1]/attribute[1]/newTable[1]  =  true
     * @param map
     * @return null will be returned if we couldn't extract the list
     */
    public static ArrayList extactRemovedImportAttribute(OrderedMap map) 
    {
       ArrayList list = null;
       if(map != null)
       {
         list = new ArrayList();
         Iterator it = map.keySet().iterator();
         String keyInMap = null;
         String valueInMap = null;
         OrderedMap mapForImportAttribute = new OrderedMap();
         boolean getKey = false;
         boolean getValue = false;
         while (it.hasNext())
         {
            String key = (String)it.next();
            if(key == null)
            {
              continue;
            }
            
           
            if(key.startsWith("/additionInfo/removedImortAttribute[1]/attribute[1]/entityName"))
            {
               String entityName= (String)map.get(key);
               Log.debug(40, "In CodeImportPage.extactRemovedImportAttribute, the entityName from order map is "+entityName);
               if(entityName != null)
               {
                 list.add(AbstractDataPackage.ENTITYNAMEINDEX, entityName);
               }
               else
               {
                 list = null;
                 Log.debug(30, "couldn't get the entityName for importAttributeImport info CodeImportPage.inextactRemovedImportAttribute ");
                 break;
               }
               
            }
            else if(key.startsWith("/additionInfo/removedImortAttribute[1]/attribute[1]/attributeName"))
            {
               String attributeName= (String)map.get(key);
               Log.debug(40, "In CodeImportPage.extactRemovedImportAttribute, the attribureName from order map is "+attributeName);
               if(attributeName != null)
               {
                 list.add(AbstractDataPackage.ATTRIBUTENAMEINDEX, attributeName);
               }
               else
               {
                 list = null;
                 Log.debug(30, "couldn't get the attributeName for importAttributeImport info CodeImportPage.inextactRemovedImportAttribute ");
                 break;
               }
            }
            else if(key.startsWith("/additionInfo/removedImortAttribute[1]/attribute[1]/scale"))
            {
              String scale= (String)map.get(key);
              Log.debug(40, "In CodeImportPage.extactRemovedImportAttribute, the scale from order map is "+scale);
              list.add(AbstractDataPackage.SCALEINDEX, scale);
              
            }
            else if(key.startsWith("/additionInfo/removedImortAttribute[1]/attribute[1]/xPath"))
            {
              //order map is before xpath. So we should add it first.
              list.add(AbstractDataPackage.ORDEREDMAPINDEX, mapForImportAttribute);
              String xpath = (String)map.get(key);
              Log.debug(40, "In CodeImportPage.extactRemovedImportAttribute, the xpath from order map is "+xpath);
              list.add(AbstractDataPackage.XPATHINDEX, xpath);
            }
            else if(key.startsWith("/additionInfo/removedImortAttribute[1]/attribute[1]/newTable"))
            {
              String newTable = (String)map.get(key);
              Log.debug(40, "In CodeImportPage.extactRemovedImportAttribute, the newTable from order map is "+newTable);
              Boolean newTableBoolean = null;
              try
              {
                newTableBoolean = new Boolean(newTable);
              }
              catch(Exception e)
              {
                list = null;
                Log.debug(30,"Couldn't transform string "+newTable+" to a boolean value in CodeImportPage.extactRemovedImportAttribute");
                return list;
              }
              list.add(AbstractDataPackage.NEWTABLEINDEX, newTableBoolean);
            }
            else if(key.startsWith("/additionInfo/removedImortAttribute[1]/attribute[1]/orderedMap[1]/pair"))
            {
               if(key.indexOf("key") != -1)
               {
                  keyInMap = (String)map.get(key);
                  getKey= true;
               }
               else if(key.indexOf("value") != -1)
               {
                  valueInMap = (String)map.get(key);
                  getValue = true;
               }
               if(getKey && getValue)
               {
                 mapForImportAttribute.put(keyInMap, valueInMap);
                 getKey = false;
                 getValue = false;
                 Log.debug(40, "In CodeImportPage.extactRemovedImportAttribute, put key "+keyInMap+" and value "+valueInMap+" into a map");
               }
            }            
            
         }
         
       }     
       return list;
    }
   
   /**
    * Gets the attribute name be handle (imported in this page)
    * @return
    */
   public String getHandledImportAttributeName()
   {
	   return this.handledImportAttributeName;
   }
   
   /**
    * Gets the import attribute info which was removed in this page's advanceAction.
    * @return
    */
   public ArrayList getRemovedImportAttributeInfo()
   {
 	  return this.removedAttributeInfo;
   }
   

   /**
    * Gets the selected code column index (not id) from CodeDefnPanel
    * @return
    */
	public int getSelectedCodeColumnIndex() 
	{
		return selectedCodeColumnIndex;
	}

	 /**
	    * Gets the selected definition column index (not id) from CodeDefnPanel
	    * @return
	    */
	public int getSelectedDefinitionColumnIndex() 
	{
		return selectedDefinitionColumnIndex;
	}
	
	/**
	 * Gets the import choice
	 * @return
	 */
	public short getImportChoice()
	{
		return this.importChoice;
	}
	
	/**
	 * Gets if entity index was increased.
	 * @return true if index was increased
	 */
	public boolean isEntityIndexIncreased()
	{
	  return this.entityIndexIncreased;
	}
	
  /**
   * Determines if entity was added in previous cycle
   * @return
   */
	public boolean isEntityAddedInPreviousCycle()
  {
    return isEntityAddedInPreviousCycle;
  }
	
	/*
   * Click button in radio panel
   */
  private void clickRadionButton(JPanel container, int index) throws Exception
  {
    if(container != null)
    {
     try
     {
         Container radio = (Container)container.getComponent(1);
         JRadioButton button = (JRadioButton)radio.getComponent(index);
         button.doClick();
     }
     catch(Exception e)
     {
       throw e;
     }
    }
    else
    {
     Log.debug(10, "The Radion button container is null and we couldn't click it in DataFormat.clickRadioButton");
       throw new Exception("The Radion button container is null and we couldn't click it in DataFormat.clickRadioButton");
    }
  }

   
}
