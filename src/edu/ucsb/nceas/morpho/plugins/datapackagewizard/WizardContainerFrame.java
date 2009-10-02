/**
 *  '$RCSfile: WizardContainerFrame.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Matthew Brooke
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-20 18:26:05 $'
 * '$Revision: 1.77 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.util.IncompleteDocSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XMLUtil;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.w3c.dom.Node;

/**
 *  provides a top-level container for AbstractUIPage objects. The top (title) panel
 *  and bottom button panel (including the navigation buttons) are all part of
 *  this class, with the AbstractUIPage content being nested inside a central area
 */
public class WizardContainerFrame
    extends JFrame {

  public static JFrame frame;
  private DataPackageWizardListener listener;

  private Node domToReturn;
  private boolean domPresentToReturn = false;

  private InputMap imap;
  private ActionMap amap;
  private String autoSaveID = null; //the id of the auto saving file
  
  //public static final String TEMP = "temp";
  public final static  String VERSION1 = "1";
  
  



  /**
   * Constructor
   */
  public WizardContainerFrame() {

    super();
    frame = this;
    this.listener = listener;
    pageStack = new Stack();
    pageLib = new WizardPageLibrary(this);
    init();
    ConfigXML profile = Morpho.thisStaticInstance.getProfile();
    if (profile != null)
    {
    	String scope = profile.get("scope", 0);
    	String separator= profile.get("separator", 0);
    	// try to get autoSaveID from adp first.
    	//AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
		//if(adp != null)
		//{
			//autoSaveID = adp.getAutoSavedD();
		//}
		//if we couldn't get the autoSaveID from exist datapackage, get it from random number
    	//if(autoSaveID == null && scope != null)
    	{  	   
    	   autoSaveID = scope+separator+getRandomString()+separator+VERSION1;
    	}
    }

    this.addWindowListener(new WindowAdapter() {

      public void windowClosing(WindowEvent e) {
        cancelAction();
      }
    });

  }

  /**
   *  sets the <code>DataPackageWizardListener</code> to be called  back when
   *  the Wizard has finished
   *
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Wizard has finished
   */
  public void setDataPackageWizardListener(DataPackageWizardListener listener) {

    this.listener = listener;
  }

  /**
   *  sets the wizard content for the center pane
   *
   *  @param pageID  the String pageID of the wizard content to be loaded into
   *                  the center pane
   */
  public void setCurrentPage(String pageID) {

    Log.debug(45, "setCurrentPage called with String ID: " + pageID);
    if (pageID == null) {
      Log.debug(15, "setCurrentPage called with NULL ID");
      return;
    }

    AbstractUIPage pageForID = pageLib.getUIPage(pageID);

    if (pageForID == null) {
      Log.debug(15,
                "setCurrentPage: page library does NOT contain ID: " + pageID);
      return;
    }
    //if this is the first page, remember its ID
    if (pageStack.isEmpty()) {
      firstPageID = pageID;

    }
    setCurrentPage(pageForID);
  }
  
  /**
   * Get the id of the auto-saved file
   */
  public String getAutoSaveID()
  {
	  return autoSaveID;
  }

  /**
   * sets the wizard content for the center pane
   *
   * @param newPage the wizard content for the center pane
   */
  public void setCurrentPage(AbstractUIPage newPage) {

    if (newPage == null) {
      Log.debug(45, "setCurrentPage called with NULL WizardPage");
      return;
    }
    Log.debug(45, "setCurrentPage called with WizardPage ID = "
              + newPage.getPageID());
    //make new page current page
    this.currentPage = newPage;

    setpageTitle(newPage.getTitle());
    setpageSubtitle(getCurrentPage().getSubtitle());
    middlePanel.removeAll();
    setPageCount();
    middlePanel.add(getCurrentPage(), BorderLayout.CENTER);
    getCurrentPage().setOpaque(false);
    //in correction wizard, if the frame displays two attribute page,
    // the second would not be shown up. If we add the validate method, it can.
    middlePanel.validate();
    middlePanel.repaint();
    updateButtonsStatus();

    // update buttons before onLoad so that we cld change button status in onLoad using
    // the setButtonsStatus()
    getCurrentPage().onLoadAction();

  }
  
  /**
   * Sets the WizardPageLibrary to this frame.
   * @param pageLib the specified WizardPageLibrary will be set. 
   */
  public void setWizardPageLibrary(WizardPageLibraryInterface pageLib)
  {
	  this.pageLib = pageLib;
  }

  /**
   *  gets the wizard content from the center pane
   *
   *  @return the wizard content from the center pane
   */
  public AbstractUIPage getCurrentPage() {

    return this.currentPage;
  }

  /**
   *  sets the page count on the Wizard Page if showPageCount is true
   *
   *  @sets the page count on the wizard page
   */
  private void setPageCount() {
    if (showPageCount) {
      stepLabel.setText("Step " + getCurrentPage().getPageNumber()
                        + " of " + WizardSettings.NUMBER_OF_STEPS);
    }
    else {
      stepLabel.setText("");
    }
  }
  
  /*
   * It will generate a random string (number)
   */
  private String getRandomString()
  {
	  String random = "";
	  int size = 3;
	  for(int i=0; i< size; i++)
	  {
		 int number =  (new Double (Math.random()*100)).intValue();
		 random= random+number;
	  }
	  Log.debug(30, "The random number is "+random);
	  return random;
  }

  private void updateButtonsStatus() {

    Log.debug(45, "updateButtonsStatus called");

    if (getCurrentPage() == null) {

      prevButton.setEnabled(false);
      nextButton.setEnabled(false);
      return;

    }
    else {

      prevButton.setEnabled(true);
      nextButton.setEnabled(true);
    }

    // prev button enable/disable:
    if (pageStack.isEmpty() || pageStack.peek() == null) {
      prevButton.setEnabled(false);
    }

    // finish button:
    if (getCurrentPage().getNextPageID() == null) {
      nextButton.setEnabled(false);
      finishButton.setEnabled(true);
      finishButton.grabFocus();
    }
    else {
      nextButton.setEnabled(true);
      nextButton.grabFocus();
      finishButton.setEnabled(false);
    }
  }

  /** Method to set the enabled/disabled state of the three buttons in the
   *		WizardContainerFrame. The Cancel button is always enabled.
   *
   *	@param prevStatus - the state of the 'prev' Button. If true, it enables the button,
   *										else it disables the button
   *	@param nextStatus - the state of the 'next' Button. If true, it enables the button,
   *										else it disables the button
   *	@param finishStatus - the state of the 'finish' Button. If true, it enables the button,
   *										else it disables the button
   *
   */

  public void setButtonsStatus(boolean prevStatus, boolean nextStatus,
                               boolean finishStatus) {

    prevButton.setEnabled(prevStatus);
    nextButton.setEnabled(nextStatus);
    finishButton.setEnabled(finishStatus);
  }

  private void init() {

    pageCache = new HashMap();
    initContentPane();
    initKeyInputMap();
    initTopPanel();
    initMiddlePanel();
    initBottomPanel();
    initButtons();
    updateButtonsStatus();
  }

  private void initKeyInputMap() {
    imap = new InputMap();
    imap.put(javax.swing.KeyStroke.getKeyStroke("ESCAPE"), "cancelKeyAction");
    imap.put(javax.swing.KeyStroke.getKeyStroke("alt C"), "cancelKeyAction");
    imap.put(javax.swing.KeyStroke.getKeyStroke("LEFT"), "previousKeyAction");
    imap.put(javax.swing.KeyStroke.getKeyStroke("alt P"), "previousKeyAction");
    imap.put(javax.swing.KeyStroke.getKeyStroke("RIGHT"), "nextKeyAction");
    imap.put(javax.swing.KeyStroke.getKeyStroke("alt N"), "nextKeyAction");

    amap = new ActionMap();
    amap.put("cancelKeyAction",
             new AbstractAction("cancelKeyAction") {
      public void actionPerformed(ActionEvent evt) {
        Log.debug(45, "cancelKeyAction" + evt.getActionCommand());
        cancelAction();
      }
    });
    amap.put("previousKeyAction",
             new AbstractAction("previousKeyAction") {
      public void actionPerformed(ActionEvent evt) {
        Log.debug(45, "previousKeyAction" + evt.getActionCommand());
        previousAction();
      }
    });
    amap.put("nextKeyAction",
             new AbstractAction("nextKeyAction") {
      public void actionPerformed(ActionEvent evt) {
        Log.debug(45, "nextKeyAction" + evt.getActionCommand());
        nextAction();
      }
    });

  }

  private void initContentPane() {
    this.setIconImage(edu.ucsb.nceas.morpho.util.UISettings.
                      FRAME_AND_TASKBAR_ICON);
    contentPane = this.getContentPane();
    contentPane.setLayout(new BorderLayout());
  }

  private void initTopPanel() {

    Log.debug(45, "WizardContainerFrame starting init()");

    titleLabel = new JLabel("");
    titleLabel.setFont(WizardSettings.TITLE_FONT);
    titleLabel.setForeground(WizardSettings.TITLE_TEXT_COLOR);
    titleLabel.setBorder(new EmptyBorder(PADDING, 0, PADDING, 0));

    subtitleLabel = new JLabel("");
    subtitleLabel.setFont(WizardSettings.SUBTITLE_FONT);
    subtitleLabel.setForeground(WizardSettings.SUBTITLE_TEXT_COLOR);
    subtitleLabel.setBorder(new EmptyBorder(PADDING, 0, PADDING, 0));

    topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.setPreferredSize(WizardSettings.TOP_PANEL_DIMS);
    topPanel.setBorder(new EmptyBorder(0, 3 * PADDING, 0, 3 * PADDING));
    topPanel.setBackground(WizardSettings.TOP_PANEL_BG_COLOR);
    topPanel.setOpaque(true);
    topPanel.add(titleLabel);
    topPanel.add(subtitleLabel);
    contentPane.add(topPanel, BorderLayout.NORTH);

  }

  private void initMiddlePanel() {

    middlePanel = new JPanel();
    middlePanel.setLayout(new BorderLayout());
    middlePanel.setBorder(new EmptyBorder(PADDING, 3 * PADDING, PADDING,
                                          3 * PADDING));
    middlePanel.setInputMap(
        javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, imap);
    middlePanel.setActionMap(amap);
    contentPane.add(middlePanel, BorderLayout.CENTER);
  }

  private void initBottomPanel() {

    bottomBorderPanel = new JPanel();
    bottomBorderPanel.setLayout(new BorderLayout(0, 2));

    bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
    bottomPanel.add(Box.createHorizontalGlue());
    bottomPanel.setOpaque(false);

    bottomPanel.setBorder(new EmptyBorder(PADDING / 2, 0, 0, 0));
    bottomBorderPanel.setBorder(
        BorderFactory.createMatteBorder(2, 0, 0, 0,
                                        WizardSettings.TOP_PANEL_BG_COLOR));
    bottomPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

    stepLabel = new JLabel();
    stepLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 3));
    stepLabel.setText("Step 1 of " + WizardSettings.NUMBER_OF_STEPS);

    bottomBorderPanel.add(stepLabel, BorderLayout.WEST);
    bottomBorderPanel.add(bottomPanel, BorderLayout.CENTER);
    //bottomBorderPanel.add(WidgetFactory.makeHalfSpacer(), BorderLayout.NORTH);

    bottomBorderPanel.setInputMap(
        javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, imap);
    bottomBorderPanel.setActionMap(amap);
    contentPane.add(bottomBorderPanel, BorderLayout.SOUTH);
  }

  private void initButtons() {

    addButton(WizardSettings.CANCEL_BUTTON_TEXT, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelAction();
      }
    });
    prevButton = addButton(WizardSettings.PREV_BUTTON_TEXT, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        previousAction();
      }
    });
    nextButton = addButton(WizardSettings.NEXT_BUTTON_TEXT, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        nextAction();
      }
    });
    finishButton = addButton(WizardSettings.FINISH_BUTTON_TEXT,
                             new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        finishAction();
      }
    });

    this.getRootPane().setDefaultButton(nextButton);
  }

  /**
   * if true is passed, the "page # of ##" counter will be shown in the footer
   *
   * @param show boolean
   */
  public void setShowPageCountdown(boolean show) {
    showPageCount = show;
    setPageCount();
  }

  /**
   * returns the String ID of the first page that was displayed in the current
   * wizard sequence. Used, for example, by Summary page to determine which
   * wizard sequence it is summarizing
   *
   * @return String ID of the first page that was displayed in the current
   * 					wizard sequence (@see DataPackageWizardInterface for values)
   */
  public String getFirstPageID() {

    return firstPageID;
  }

  /**
   * returns the String ID of the previous page that was displayed in the current
   * wizard sequence.
   *
   * @return String ID of the previous page that was displayed in the current
   * 					wizard sequence (@see DataPackageWizardInterface for values)
   */
  public String getPreviousPageID() {

    AbstractUIPage page = getPreviousPage();
    if (page == null) return "";

    return page.getPageID();
  }



  /**
   * returns the AbstractUIPage object representing the previous page that was
   * displayed in the current wizard sequence.
   *
   * @return  AbstractUIPage object representing the previous page that was
   * displayed in the current wizard sequence. DOES NOT REMOVE IT FROM THE STACK,
   * so multiple calls will all return the same value
   */
  public AbstractUIPage getPreviousPage() {

    if (pageStack.isEmpty()) return null;

    return (AbstractUIPage)pageStack.peek();
  }


  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  is pressed. It's up to the content to know whether it's the last page or
   *  not
   */
  public void nextAction() {

    Log.debug(45, "nextFinishAction called");

    // if the page's onAdvanceAction() returns false, don't advance...
    if (! (getCurrentPage().onAdvanceAction())) return;

    if (getCurrentPage().getNextPageID() == null) return;

    // * * * N E X T * * *

    //put current page on stack
    Log.debug(45, "nextFinishAction pushing currentPage to Stack ("
              + getCurrentPage().getPageID() + ")");
    pageStack.push(this.getCurrentPage());
    autoSaveInCompletePackage();
    
    String nextPgID = getCurrentPage().getNextPageID();

    Log.debug(45, "nextFinishAction - next page ID is: " + nextPgID);

    AbstractUIPage nextPage = (AbstractUIPage)pageCache.get(nextPgID);

    if (nextPage==null) nextPage = pageLib.getUIPage(nextPgID);

    setCurrentPage(nextPage);
  }

  /**
   *  The action to be executed when the "Finish" button is pressed.
   */
  public void finishAction() {

    // * * * F I N I S H * * *
    pageStack.push(this.getCurrentPage());

    this.setVisible(false);

    Node rootNode = null;
    if (!domPresentToReturn) {
      rootNode = collectDataFromPages();
    }
    else {
      rootNode = domToReturn;
    }
    listener.wizardComplete(rootNode, autoSaveID);

    // now clean up
    doCleanUp();
  }

  public Node collectDataFromPages() {

    //pages map
    Map pageMap = new HashMap();

    //results Map:
    OrderedMap wizData = new OrderedMap();
    OrderedMap accessData = null;

    //NOTE: the order of pages on the stack is *not* the same as the order
    //of writing data to the DOM. We therefore convert the Stack to a Map
    //(indexed by pageID) and access the pages non-sequentially in a flurry of
    //hard-coded madness:
    //
    
    //in order to reuse pageStack, we give up pageStack.pop method
    //while (!pageStack.isEmpty()) {
     int size = pageStack.size();
     if (size >0)
     {
      for (int i=0; i< size; i++)
      {
	      //AbstractUIPage nextPage = (AbstractUIPage)pageStack.pop();
	      AbstractUIPage nextPage = (AbstractUIPage)pageStack.elementAt(i);
	      String nextPageID = nextPage.getPageID();
	      Log.debug(45, ">> collectDataFromPages() - next pageID = "+nextPageID);
	      if (nextPageID==null || nextPageID.trim().length()<1) {
	
	        Log.debug(15,
	                  "\n*** WARNING - WizardContainerFrame.collectDataFromPages()"
	                  +" has encountered a page with no ID! Object is: "+nextPage);
	        continue;
	      }
	      pageMap.put(nextPageID, nextPage);
      }
    }

    AbstractUIPage GENERAL
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.GENERAL);
    AbstractUIPage KEYWORDS
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.KEYWORDS);
    AbstractUIPage PARTY_CREATOR_PAGE
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.PARTY_CREATOR_PAGE);
    AbstractUIPage PARTY_CONTACT_PAGE
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.PARTY_CONTACT_PAGE);
    AbstractUIPage PARTY_ASSOCIATED_PAGE
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.PARTY_ASSOCIATED_PAGE);
    AbstractUIPage PROJECT
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.PROJECT);
    AbstractUIPage METHODS
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.METHODS);
    AbstractUIPage USAGE_RIGHTS
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.USAGE_RIGHTS);
    AbstractUIPage GEOGRAPHIC
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.GEOGRAPHIC);
    AbstractUIPage TEMPORAL
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.TEMPORAL);
    AbstractUIPage TAXONOMIC
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.TAXONOMIC);
    AbstractUIPage ACCESS
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.ACCESS);
    AbstractUIPage DATA_LOCATION
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.DATA_LOCATION);
    AbstractUIPage TEXT_IMPORT_WIZARD
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.TEXT_IMPORT_WIZARD);
    AbstractUIPage DATA_FORMAT
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.DATA_FORMAT);
    AbstractUIPage ENTITY
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.ENTITY);

    //TITLE:
    OrderedMap generalMap = null;
    
    //ACCESS:
    if (ACCESS != null) {
      accessData = ACCESS.getPageData();
      addPageDataToResultsMap( ACCESS, wizData);
    }
    
    if (GENERAL != null) {

      generalMap = GENERAL.getPageData();
      final String titleXPath = "/eml:eml/dataset/title[1]";
      Object titleObj = generalMap.get(titleXPath);
      if (titleObj != null) {
        wizData.put(titleXPath, titleObj);
//                    XMLUtilities.normalize(titleObj));  //avoid double normalization - DFH
      }
    }

    //CREATOR:
    if (PARTY_CREATOR_PAGE != null) {
      addPageDataToResultsMap( PARTY_CREATOR_PAGE, wizData);
    }

    //ASSOCIATED PARTY:
    if (PARTY_ASSOCIATED_PAGE != null) {
      addPageDataToResultsMap( PARTY_ASSOCIATED_PAGE, wizData);
    }

    //ABSTRACT:
    if (generalMap != null) {

      final String abstractXPath = "/eml:eml/dataset/abstract/para[1]";
      String abstractObj = (String)generalMap.get(abstractXPath);
      Log.debug(45, "abstract data is "+abstractObj);
      //since general page allow the empty string in getPageData. so we have to make
      //sure no empty abstract into document
      if (abstractObj != null && !abstractObj.trim().equals("")) {
        wizData.put(abstractXPath,abstractObj);
//                    XMLUtilities.normalize(abstractObj)); //avoid double normalization - DFH
      }
    }

    //KEYWORDS:
    if (KEYWORDS != null) {
      addPageDataToResultsMap( KEYWORDS, wizData);
    }

    //INTELLECTUAL RIGHTS:
    if (USAGE_RIGHTS != null) {
      addPageDataToResultsMap( USAGE_RIGHTS, wizData);
    }

    //GEOGRAPHIC:
    if (GEOGRAPHIC != null) {
      addPageDataToResultsMap( GEOGRAPHIC, wizData);
    }

    //TEMPORAL:
    if (TEMPORAL != null) {
      addPageDataToResultsMap( TEMPORAL, wizData);
    }

    //TAXONOMIC
    if (TAXONOMIC != null) {
      addPageDataToResultsMap( TAXONOMIC, wizData);
    }

    //CONTACT:
    if (PARTY_CONTACT_PAGE != null) {
      addPageDataToResultsMap( PARTY_CONTACT_PAGE, wizData);
    }

    //METHODS:
    if (METHODS != null) {
      addPageDataToResultsMap( METHODS, wizData);
    }

    //PROJECT:
    if (PROJECT != null) {
      addPageDataToResultsMap( PROJECT, wizData);
    }

   

    if (TEXT_IMPORT_WIZARD != null) {
      addPageDataToResultsMap( TEXT_IMPORT_WIZARD, wizData);
    }

    if (ENTITY != null) {
      addPageDataToResultsMap( ENTITY, wizData);
    }

    if (DATA_FORMAT != null) {
      addPageDataToResultsMap( DATA_FORMAT, wizData);
    }

    if (DATA_LOCATION != null) {
      addPageDataToResultsMap( DATA_LOCATION, wizData);
    }
    // now add unique ID's to all dataTables and attributes
    addIDs(
        new String[] {
        "/eml:eml/dataset/dataTable",
        "/eml:eml/dataset/dataTable/attributeList/attribute"
    }
        , wizData);

    Log.debug(45, "\n\n********** Wizard finished: NVPs:");
    Log.debug(45, wizData.toString());

    ////////////////////////////////////////////////////////////////////////////
    // this is the end of the page processing - wizData OrderedMap should now
    // contain all values in correct order
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////
    // next, create a DOM from the OrderedMap...
    ////////////////////////////////////////////////////////////////////////////

    //create a new empty DOM document to be populated by the wizard values:
    Node rootNode = null;
    if(accessData == null || accessData.isEmpty())
    {
       // for no access subtree
       rootNode = getNewEmptyDataPackageDOM(WizardSettings.NEW_EML210_DOCUMENT_TEXT_WITHOUTACCESS);
    }
    else
    {
    	//for having access subtree
    	rootNode = getNewEmptyDataPackageDOM(WizardSettings.NEW_EML210_DOCUMENT_TEXT_WITHACCESS);
    }

    //now populate it...
    try {

      XMLUtilities.getXPathMapAsDOMTree(wizData, rootNode);

    }
    catch (Exception e) {

      e.printStackTrace();
      Log.debug(5, "unexpected error trying to create new XML document "
                + "after wizard finished\n");
      cancelAction();

      return null;
    }

    Log.debug(45, "\n\n********** Wizard finished: DOM:");
    Log.debug(45, XMLUtilities.getDOMTreeAsString(rootNode));
    return rootNode;
  }


  //create a new empty DOM document to be populated by the wizard values:
  protected Node getNewEmptyDataPackageDOM(String DocText) {

    Node rootNode = null;

    try {
      rootNode = XMLUtilities.getXMLReaderAsDOMTreeRootNode(
          new StringReader(DocText));
    } catch (Exception e) {
      e.printStackTrace();
      Log.debug(5, "unexpected error trying to create new XML document");
      cancelAction();
      return null;
    }
    return rootNode;
  }

  
  /**
   * Manually save incomplete package
   * @param docid
   */
  public void manualSaveInCompletePackage(String docid)
  {
	  
  }
  
  /*
   * save incomplete package automatically
   */
  private void autoSaveInCompletePackage()
  {
	  if(autoSaveID != null)
	  {
		  Node temp = collectDataFromPages(); 
		  String emlDoc = XMLUtilities.getDOMTreeAsString(temp, false);
		  //System.out.println("the original eml "+emlDoc);
		  emlDoc = addPackageWizardIncompleteInfo(emlDoc);
		  Log.debug(40, "The partial eml document is :\n"+emlDoc);
		  //System.out.println("the eml after appending incomplete info  "+emlDoc);
		  saveInCompletePackage(autoSaveID, emlDoc);
	  }
  }
  
  /*
   * Adds some new information into additional part in EML.
   * In New Package Wizard, it is simple. It will replace the </eml> by
   * <additionalMetadata>
   *   <metadata>
   *      <incomplete>
   *         <packagewizard>
   *            <className>edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyIntro</className>
   *         </packagewizard>
   *      </incomplete>
   *   <metadata>
   * <additonalMetacat>
   * </eml>
   *         
   *    
   */
  private String addPackageWizardIncompleteInfo(String originalEML)
  {
	  String emlWithIncompleteInfo = "";
	  if(originalEML != null)
	  {
		  //System.out.println("the original eml is "+originalEML);
		  int index = originalEML.lastIndexOf(IncompleteDocSettings.EMLCLOSINGTAG);
		  if (index != -1)
		  {
			  //it has </eml:eml> closing tag
			  //removes the </packageWizard></metadata></additionalMetadata></eml:eml> from original eml
			  emlWithIncompleteInfo = originalEML.substring(0,index);
			  //appends additionalMetadata par original eml
			  emlWithIncompleteInfo = emlWithIncompleteInfo+IncompleteDocSettings.ADDITIONALMETADATAOPENINGTAG+
              IncompleteDocSettings.METADATAOPENINGTAG+IncompleteDocSettings.PACKAGEWIZARDOPENINGTAG;
			  if(pageStack != null)
			  {
				  int size = pageStack.size();
				  String className = null;
				  for(int i=0; i<size; i++)
				  {
					  AbstractUIPage page = (AbstractUIPage)pageStack.elementAt(i);
					  if(page != null)
					  {
						  className = page.getClass().getName();
						  Log.debug(40, "Class name is "+className);
						  emlWithIncompleteInfo = emlWithIncompleteInfo+IncompleteDocSettings.CLASSNAMEOPENINGTAG+
						                                      className+IncompleteDocSettings.CLASSNAMECLOSINGTAG;
					  }
				  }
			  }
             
              emlWithIncompleteInfo = emlWithIncompleteInfo+IncompleteDocSettings.PACKAGEWIZARDCLOSINGTAG+
              IncompleteDocSettings.METADATACLOSINGTAG+IncompleteDocSettings.ADDITIONALMETADATACLOSINGTAG+
              IncompleteDocSettings.EMLCLOSINGTAG;
			                                       
		  }
		  else
		  {
			  //it doesn't have </eml:eml> closing tag, we use orignal document as modified one.
			  emlWithIncompleteInfo =  originalEML;
			  
		  }
	  }
	  
	  return emlWithIncompleteInfo;
  }
  
  /*
   * Save incompleted package with given id
   */
  private void saveInCompletePackage(String docid, String xml)
  {
	  FileSystemDataStore store = new FileSystemDataStore(Morpho.thisStaticInstance);
	  StringReader reader = new StringReader(xml);
	  store.saveIncompleteDataFile(docid, reader);
  }
  
 

  private final String ID_ATTR_XPATH = "/@id";
  private final StringBuffer tempBuff = new StringBuffer();
  private final OrderedMap idMap = new OrderedMap();

  /**
   * adds unique IDs to the elements identified by the *absolute* XPath strings
   * in the elementsThatNeedIDsArray NOTE - if the xpath points to more than one
   * element, then a unique ID will be assigned to each element
   *
   * @param elementsNeedingIDsArray String[]
   * @param resultsMap OrderedMap
   */
  protected void addIDs(String[] elementsNeedingIDsArray, OrderedMap resultsMap) {

    idMap.clear();
    Set keyset = resultsMap.keySet();
    //
    for (int i = 0; i < elementsNeedingIDsArray.length; i++) {

      String nextXPath = elementsNeedingIDsArray[i];
      //elementsNeedingIDsArray[i];
      //check if resultsMap keys contain the exact xpath with no predicate
      if (keyset.contains(nextXPath)) {

        //if so, add @id to this xpath and append to idMap...
        idMap.put(nextXPath + ID_ATTR_XPATH, WizardSettings.getUniqueID());
      }

      tempBuff.delete(0, tempBuff.length());
      tempBuff.append(nextXPath);
      tempBuff.append("/");

      //check if resultsMap keys contain the substring xpath with no predicate
      if (mapKeysContainSubstring(keyset, tempBuff.toString())) {

        //if so, add @id to this xpath and append to results...
        idMap.put(nextXPath + ID_ATTR_XPATH, WizardSettings.getUniqueID());
      }

      nextXPath += "[";

      int idx = 1;

      tempBuff.delete(0, tempBuff.length());
      tempBuff.append(nextXPath);
      tempBuff.append(idx++);
      tempBuff.append("]");

      //loop while resultsMap keys contain the substring (xpath[+i+])
      while (mapKeysContainSubstring(keyset, tempBuff.toString())) {

        //add @id to xpath[+i+] and append to results
        tempBuff.append(ID_ATTR_XPATH);
        idMap.put(tempBuff.toString(), WizardSettings.getUniqueID());

        tempBuff.delete(0, tempBuff.length());
        tempBuff.append(nextXPath);
        tempBuff.append(idx++);
        tempBuff.append("]");
      }
    }
    resultsMap.putAll(idMap);
  }

  private boolean mapKeysContainSubstring(Set keyset, String xpath) {

    if (keyset == null || xpath == null || xpath.trim().equals("")) {
      return false;
    }

    Iterator it = keyset.iterator();

    if (it == null) {
      return false;
    }

    String nextKey = null;

    while (it.hasNext()) {

      nextKey = (String) it.next();

      if (nextKey == null) {
        continue;
      }
      if (nextKey.indexOf(xpath) < 0) {
        continue;
      }
      else {
        return true;
      }
    }
    return false;
  }

  public void setDOMToReturn(Node dom) {

    this.domToReturn = dom;
    this.domPresentToReturn = true;
  }

  /**
   * given a WizardPage object (nextPage), calls its getPageData() method to get
   * the NVPs from thae page, and adds these NVPs to the OrderedMap provided
   * (resultMap)
   *
   * @param nextPage WizardPage
   * @param resultsMap OrderedMap
   */
  protected void addPageDataToResultsMap(AbstractUIPage nextPage,
                                       OrderedMap resultsMap) {

    String nextKey = null;

    OrderedMap nextPgData = nextPage.getPageData();

    if (nextPgData == null) return;

    Iterator it = nextPgData.keySet().iterator();

    if (it == null) return;

    while (it.hasNext()) {

      nextKey = (String) it.next();

      if (nextKey == null || nextKey.trim().equals("")) continue;

      //now excape all characters that might cause a problem in XML:
//      resultsMap.put(nextKey, XMLUtil.normalize(nextPgData.get(nextKey)));
      resultsMap.put(nextKey, nextPgData.get(nextKey));

    } // end while
  }


  /**
   *  The action to be executed when the "Prev" button is pressed
   */
  public void previousAction() {

    if (pageStack.isEmpty()) return;

    AbstractUIPage previousPage = (AbstractUIPage) pageStack.pop();
    if (previousPage == null) {
      Log.debug(15, "previousAction - popped a NULL page from stack");
      return;
    }
    Log.debug(45, "previousAction - popped page with ID: "
              + previousPage.getPageID() + " from stack");

    //put current page on stack
    AbstractUIPage currentPage = this.getCurrentPage();
    Log.debug(45, "previousAction adding currentPage to pageCache ("
              + currentPage.getPageID() + ")");
    currentPage.onRewindAction();
    pageCache.put(currentPage.getPageID(), currentPage);

    setCurrentPage(previousPage);
  }


  /**
    Function to clear the current page stack
   */
  public void reInitializePageStack() {

    if (pageStack == null) {
      pageStack = new Stack();
      return;
    }
    pageStack.removeAllElements();
    return;
  }


  /**
   *  The action to be executed when the "Cancel" button is pressed
   */
  public void cancelAction() {

    AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
    if(adp != null) {

      edu.ucsb.nceas.morpho.datapackage.Entity[] arr = adp.getOriginalEntityArray();
      if(arr != null) {
        System.out.println("replacing subtree - ");
        adp.deleteAllEntities();
        /*edu.ucsb.nceas.morpho.datapackage.Entity[] newarr = adp.getEntityArray();
        for(int i = 0; i < newarr.length; i++)
          adp.deleteEntity(0);*/
        for(int i = 0; i < arr.length; i++) {
          System.out.println("adding entity - " + i);
          adp.addEntity(arr[i]);
        }
      }

    }

    this.setVisible(false);

    listener.wizardCanceled();
    if(autoSaveID != null)
    {
    	FileSystemDataStore store = new FileSystemDataStore(Morpho.thisStaticInstance);
    	store.deleteInCompleteFile(autoSaveID);
    }
    // now clean up
    doCleanUp();
  }


  private void doCleanUp() {

    UIController.getInstance().setWizardNotRunning();

    //clear out pageStack
    pageStack.clear();
    AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
    if(adp != null) adp.clearAllAttributeImports();

  }

  /**
   *  sets the main title for this page
   *  @param newTitle the page title
   */
  private void setpageTitle(String newTitle) {
    if (newTitle == null) {
      newTitle = " ";
    }
    titleLabel.setText(newTitle);
  }

  /**
   *  sets the main subtitle for this page
   *
   *  @param newSubTitle the main subtitle
   */
  private void setpageSubtitle(String newSubTitle) {
    if (newSubTitle == null) {
      newSubTitle = " ";
    }
    subtitleLabel.setText(newSubTitle);
  }

  /**
   * adds a button with a specified title and action to the bottom panel
   *
   * @param title text to be shown on the button
   * @param actionListener the ActionListener that will respond to the button
   *   press
   * @return JButton
   */
  private JButton addButton(String title, ActionListener actionListener) {

    JButton button = WidgetFactory.makeJButton(title, actionListener,
                                               WizardSettings.NAV_BUTTON_DIMS);
    bottomPanel.add(button);
    bottomPanel.add(Box.createHorizontalStrut(PADDING));
    return button;
  }

  /**
   * gets the JFrame to be used as the owner by a popup dialog. Basically, if
   * the wizard is showing, the wizard container frame is returned. If not, the
   * current morpho frame is returned.
   *
   * @return JFrame
   */
  public static JFrame getDialogParent() {

    if (WizardContainerFrame.frame!=null
        && WizardContainerFrame.frame.isShowing()) {

      dialogParent = WizardContainerFrame.frame;

    } else {

      dialogParent = UIController.getInstance().getCurrentActiveWindow();
    }

    if (dialogParent == null
        || dialogParent.getX() < 0 || dialogParent.getY() < 0) {

      if (dummyFrame == null) {
        dummyFrame = new JFrame();
        dummyFrame.setBounds(UISettings.CLIENT_SCREEN_WIDTH / 2,
                             UISettings.CLIENT_SCREEN_HEIGHT / 2, 1, 1);
        dummyFrame.setVisible(false);
      }
      dialogParent = dummyFrame;
    }
    return dialogParent;
  }
  
  /**
   * Adds an UIPage into the stack
   * @param page
   */
  public void addPageToStack(AbstractUIPage page)
  {
	  pageStack.add(page);
  }




  // * * *  V A R I A B L E S  * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JLabel stepLabel;
  private int PADDING = WizardSettings.PADDING;
  private Container contentPane;
  private JPanel topPanel;
  private JPanel middlePanel;
  private JPanel bottomBorderPanel;
  private JPanel bottomPanel;
  private JLabel titleLabel, subtitleLabel;
  private JButton nextButton;
  private JButton prevButton;
  private JButton finishButton;
  private AbstractUIPage currentPage;
  protected Stack pageStack;
  private WizardPageLibraryInterface pageLib;
  private boolean showPageCount;
  private Map pageCache;

  private String firstPageID;
  private static JFrame dummyFrame;
  private static JFrame dialogParent;
}
