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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Access;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CodeDefinition;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CodeDefnPanel;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CodeImportPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.DataLocation;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Entity;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.ImportedTextFile;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Methods;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyMainPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Project;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Taxonomic;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.TextImportAttribute;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.TextImportDelimiters;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.TextImportEntity;
import edu.ucsb.nceas.morpho.util.IncompleteDocSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.XMLUtil;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 *  provides a top-level container for AbstractUIPage objects. The top (title) panel
 *  and bottom button panel (including the navigation buttons) are all part of
 *  this class, with the AbstractUIPage content being nested inside a central area
 */
public class WizardContainerFrame
    extends JFrame implements TableModelListener {

  public static JFrame frame;
  private DataPackageWizardListener listener;

  private Node domToReturn;
  private boolean domPresentToReturn = false;

  private InputMap imap;
  private ActionMap amap;
  private String autoSaveID = null; //the id of the auto saving file
  
  //public static final String TEMP = "temp";
  public final static  String VERSION1 = "1";
  private final static String INCOMPLETEDIR ="incompleteDir";
  private final static String DATADIR = "dataDir";
  
  private  boolean disableIncompleteSaving = Morpho.getFlagofDisableSavingIncompleteDoc();
  //private boolean isEntityWizard = false;
  private String status = IncompleteDocSettings.PACKAGEWIZARD;
  private boolean isImportCodeDefinitionTable = false;
  private int entityIndex = 0;
  private  AbstractDataPackage adp = null;
  private final static String ENTITYGENERICNAME = "entities";
  private boolean hasTopPanel = true;
  private ImportedTextFile importedDataTextFile = null;
  protected Stack pageStack;
  private WizardPageLibraryInterface pageLib;
  protected boolean showPageCount;
  private Map pageCache;
  private String firstPageID;
  private String entityName = null;
  private List newImportedAttributeNameList = new ArrayList();
  private Vector neededCancelingEntityList = new Vector();//this is for clean up.
  private OrderedMap editingAttributeMap = null;//stores the attribute map which is edited by editing command
                                                //This is for code-def wizard
  private int editingEntityIndex = -1;
  private int editingAttributeIndex = -1;
  private Boolean beforeSelectionFlag = null; //stores the inserting column is before or after selection
  private MorphoFrame originatingMorphoFrame = null; // the morpho frame which was clicked to initialize the Wizard. For new package wizard, it is not important. It is meaningful on entity/codeDef wizard.
  private String[] entityPageIDList = {DataPackageWizardInterface.DATA_LOCATION, DataPackageWizardInterface.DATA_FORMAT,DataPackageWizardInterface.ENTITY,
		                                             DataPackageWizardInterface.TEXT_IMPORT_ENTITY, DataPackageWizardInterface.TEXT_IMPORT_DELIMITERS, 
		                                             DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE};
  private static final String EMPTYPROJECTTITLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
                                                  "<project><title> </title></project>";
  //private String manuallySaveID = null;
  private static final String CACELSOURCE = "Cancel";
  private static final String FINISHSOURCE = "Finish";
  private static final String SAVELATERSOURCE = "SaveForLater";

  
  /**
   * Constructor with parameter to disable incomplete doc saving feature.
   */
  public WizardContainerFrame(boolean disableIncompleteSaving)
  {
    super();
    this.status = IncompleteDocSettings.PACKAGEWIZARD;
    this.disableIncompleteSaving = disableIncompleteSaving;
    init();
  }
  
  /**
   * Constructor with a flag indicating if this is an entity wizard.
   * In this constructor, disableIncompleteSaving value is determined by configure file.
   */
  public WizardContainerFrame(String status, MorphoFrame orginatingMorphoFrame) {

    super();    
    this.status = status;
    this.originatingMorphoFrame = orginatingMorphoFrame;
    init();

  }
  
  
  private void init()
  {
    frame = this;
    pageStack = new Stack();
    pageLib = new WizardPageLibrary(this);
    setNewDataPackageWizardWindowsActive();
    adp = UIController.getInstance().getCurrentAbstractDataPackage().getAbstractDataPackage(); 
    setDocidToEntityWizardRunningRecorder();//only works for entity wizard or import wizard
    addEmptyProjectTileSubtree();
    initGUI();    
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {

      public void windowActivated(WindowEvent e)
      {
        setNewDataPackageWizardWindowsActive();// for new data package wizard
        setOriginatingMorphoFrameAsCurrentActiveWindow();//for entity/codeDef wizard
      }
      public void windowClosing(WindowEvent e) {
        cancelAction();
      }
    });
  }
  
  /*
   * Set the new data package wizard window to be true.
   */
  private void setNewDataPackageWizardWindowsActive()
  {
    if(status != null && status.equals(IncompleteDocSettings.PACKAGEWIZARD))
    {         
      UIController.getInstance().setNewDataPackageWizardWindowIsActive(true);
    }
  }
  
  /*
   * Set oringinatingMorphoFrame as the currnetActive window on UIController
   */
  private void setOriginatingMorphoFrameAsCurrentActiveWindow()
  {
    if(originatingMorphoFrame != null &&status != null && 
        (status.equals(IncompleteDocSettings.ENTITYWIZARD) ||status.equals(IncompleteDocSettings.CODEDEFINITIONWIZARD)))
    {         
      UIController.getInstance().setCurrentActiveWindow(originatingMorphoFrame);
    }
  }
  
  /*
   * Add the id to entityWziardRunningRecorder if this entity wizard or code import wizard.
   */
  private void setDocidToEntityWizardRunningRecorder()
  {
    if(adp != null && status != null && (status.equals(IncompleteDocSettings.ENTITYWIZARD) ||status.equals(IncompleteDocSettings.CODEDEFINITIONWIZARD)))
    {
      String docid = adp.getAccessionNumber();
      UIController.getInstance().addDocidToEntityWizardRunningRecorder(docid);
    }
  }
  
  /**
   * Initialize auto saving. This mainly initialize the id for auto-saved file.
   * For entity wizard, it will dump the adp to the saved file too. 
   * If we will use an adp rather than the one from UIController.getInstance().getCurrentAbstractDataPackage(),
   * We should call setAbstractDataPackage method prior to call this method.
   */
  public void initialAutoSaving()
  {
    if(!disableIncompleteSaving)
    {
	      
	    if(adp == null)
	    {
	    	Log.debug(5, "The abstract package assoicates with the WizardContainerframe is null and we couldn't initialize the auto save id.");
	    }
	   
	    	// try to get autoSaveID from adp first.
	   		autoSaveID = adp.getAutoSavedD();		
			//if we couldn't get the autoSaveID from exist datapackage, get it from package id.
			//if auto save id is still null or empty, get it for next available id
	    	if(autoSaveID == null )
	    	{ 
	    	   autoSaveID = adp.getAccessionNumber();
	    	   if(autoSaveID == null || autoSaveID.trim().equals(""))
	    	   {
	    	     autoSaveID = DataStoreServiceController.getInstance().generateIdentifier(null, DataPackageInterface.LOCAL);
	    	     //manuallySaveID = NewPackageWizardListener.TEMPORARYID;
	    		    	 
	    	   }
	    	   else
	    	   {
	    	     //manuallySaveID = autoSaveID;
	    	   }
	    	   adp.setAutoSavedID(autoSaveID);	    
	    	}
	    	else
	    	{
	    	 // manuallySaveID = autoSaveID;
	    	}
	    	dumpPackageToAutoSaveFile(autoSaveID);//onlywork for add entity wizard
    }
  }
  
 
  /*
   * When we try to add an entity to a package, we should first dump 
   * the package to an auto-saved file as starting point if the package never has
   * had a auto-saved file 
   */
  private void dumpPackageToAutoSaveFile(String fileID)
  {
    if(!disableIncompleteSaving)
    {
  	  if(fileID != null && adp!= null && status != null && status.equals(IncompleteDocSettings.ENTITYWIZARD))
  	  {
  		  String emlDoc = XMLUtil.getDOMTreeAsString(adp.getMetadataNode());
  		  String emlDocWithIncompleteInfo = addEntityWizardIncompleteInfo(emlDoc);
  		  //System.out.println("the original eml "+emlDoc);
  		  //System.out.println("the eml after appending incomplete info  "+emlDoc);
  		  autoSavingPackageInCompleteDir(fileID, emlDocWithIncompleteInfo);
  	  }
  	  else if(fileID != null && adp!= null && status != null && status.equals(IncompleteDocSettings.CODEDEFINITIONWIZARD))
      {
        String emlDoc = XMLUtil.getDOMTreeAsString(adp.getMetadataNode());
        String emlDocWithIncompleteInfo = addCodeDefinitionWizardIncompleteInfo(emlDoc);
        //System.out.println("the original eml "+emlDoc);
        //System.out.println("the eml after appending incomplete info  "+emlDoc);
        autoSavingPackageInCompleteDir(fileID, emlDocWithIncompleteInfo);
      }
    }
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

    AbstractUIPage pageForID = pageLib.getPage(pageID);

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
   * Gets the imported data txxt file associate with the frame
   * @return
   */
  public ImportedTextFile getImportDataTextFile() 
  {
		return importedDataTextFile;
  }

  /**
   * Sets the imported data text file with the frame
   * @param dataTextFile
   */
  public void setImportedDataTextFile(ImportedTextFile dataTextFile) 
  {
		this.importedDataTextFile = dataTextFile;
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

    if((currentPage instanceof TextImportAttribute) && hasTopPanel)
    {
    	//TextImportAttribute page contains Attribute page. 
    	//The AttributePage can't be displayed completely. So we need to remove top panel
    	// to make sure the Attribute page can be displayed correctly.
    	Log.debug(30, "It is removing the top panel==================");
        this.remove(topPanel);
        this.validate();
        this.repaint();
    	hasTopPanel = false;
    }
    else if(!(currentPage instanceof TextImportAttribute) && !hasTopPanel)
    {
    	//if current page is not TextImportAttribute and deosn't have top panel, we need to add it.
    	Log.debug(30, "It is adding the top panel==================");
    	initTopPanel();
    	topPanel.validate();
    	topPanel.repaint();
    	hasTopPanel = true;
    }
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
   * Gets the WizardPageLibrary to this frame.
   * @return pageLib the specified WizardPageLibrary will be set. 
   */
  public WizardPageLibraryInterface getWizardPageLibrary()
  {
	  return this.pageLib;
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
  protected void setPageCount() {
    setPageCount(getCurrentPage().getPageNumber(), WizardSettings.NUMBER_OF_STEPS);
  }
  
  /*
   * Show page account with given total page number
   */
  protected void setPageCount(String pageNumber, String total)
  {
    if (showPageCount) {
      stepLabel.setText(pageNumber + " / " + total);
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
      if(!disableIncompleteSaving)
      {
        saveLaterButton.setEnabled(false);
      }
      return;

    }
    else {

      prevButton.setEnabled(true);
      nextButton.setEnabled(true);
      if(!disableIncompleteSaving)
      {
        saveLaterButton.setEnabled(true);
      }
    }

    // prev button enable/disable:
    if (pageStack.isEmpty() || pageStack.peek() == null) {
      prevButton.setEnabled(false);
    }

    // finish button:
    if (getCurrentPage().getNextPageID() == null) {
      nextButton.setEnabled(false);
      if(!disableIncompleteSaving)
      {
        saveLaterButton.setEnabled(false);
      }
      finishButton.setEnabled(true);
      finishButton.grabFocus();
    }
    else {
      nextButton.setEnabled(true);
      if(!disableIncompleteSaving)
      {
        saveLaterButton.setEnabled(true);
      }
      nextButton.grabFocus();
      finishButton.setEnabled(false);
    }
  }

  /** Method to set the enabled/disabled state of the three buttons in the
   *		WizardContainerFrame. The Cancel button is always enabled.
   *
   *@param prevStatus - the state of the 'Save for Later' Button. If true, it enables the button,
   *                    else it disables the button
   *	@param prevStatus - the state of the 'prev' Button. If true, it enables the button,
   *										else it disables the button
   *	@param nextStatus - the state of the 'next' Button. If true, it enables the button,
   *										else it disables the button
   *	@param finishStatus - the state of the 'finish' Button. If true, it enables the button,
   *										else it disables the button
   *
   */

  public void setButtonsStatus(boolean saveLaterStatus, boolean prevStatus, boolean nextStatus,
                               boolean finishStatus) {
    if(!disableIncompleteSaving)
    {
      saveLaterButton.setEnabled(saveLaterStatus);
    }
    prevButton.setEnabled(prevStatus);
    nextButton.setEnabled(nextStatus);
    finishButton.setEnabled(finishStatus);
  }

  private void initGUI() {

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

    initLeftBottomPanel();
    
    bottomBorderPanel.add(leftBottomPanel, BorderLayout.WEST);
    bottomBorderPanel.add(bottomPanel, BorderLayout.CENTER);
    //bottomBorderPanel.add(WidgetFactory.makeHalfSpacer(), BorderLayout.NORTH);

    bottomBorderPanel.setInputMap(
        javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, imap);
    bottomBorderPanel.setActionMap(amap);
    contentPane.add(bottomBorderPanel, BorderLayout.SOUTH);
  }
  
  /*
   * Init the left bottom panel. It contains step label and SaveLater button
   */
  private void initLeftBottomPanel()
  {
    leftBottomPanel = new JPanel();
    leftBottomPanel.setLayout(new BoxLayout(leftBottomPanel, BoxLayout.X_AXIS));
    leftBottomPanel.setOpaque(false);
    leftBottomPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
    if(!disableIncompleteSaving)
    {
      ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveForLaterAction();
        }
      };
      saveLaterButton = WidgetFactory.makeJButton(WizardSettings.SAVE_LATER_BUTTON_TEXT, actionListener,
          WizardSettings.LONG_BUTTON_DIMS);
      leftBottomPanel.add(saveLaterButton);
    }
    stepLabel = new JLabel();
    stepLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 3));
    stepLabel.setText("1 / " + WizardSettings.NUMBER_OF_STEPS);
    leftBottomPanel.add(stepLabel);
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

    if(pageStack == null || pageStack.isEmpty())
    {
    	return null;
    }
    else
    {
    	AbstractUIPage page = (AbstractUIPage)pageStack.elementAt(0);
    	if(page != null)
    	{
    		return page.getPageID();
    	}
    	else
    	{
    		return null;
    	}
    }
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
   * Gets the previous page of the given page in the page stack
   * @param page
   * @return
   */
  public AbstractUIPage getPreviousPage(AbstractUIPage page)
  {
	  AbstractUIPage previousPage = null;
	  if(page != null)
	  {
		  for(int i=0; i< pageStack.size(); i++)
		  {
			  AbstractUIPage pageInStack = (AbstractUIPage)pageStack.elementAt(i);
			  if(pageInStack != null && pageInStack.getPageID().equals(page.getPageID()))
			 {
				  if (i>1)
				  {
					  previousPage = (AbstractUIPage)pageStack.elementAt(i-1);
				  }
			 }
			  
		  }
	  }
	  return previousPage;
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

    if (nextPage==null) 
    {
    	Log.debug(30, "!!!We couldn't get page from pageStack, generate new page for the id "+nextPgID);
    	nextPage = pageLib.getPage(nextPgID);
    }
    else
    {
    	Log.debug(30, "***We could get page from pageStack with the id "+nextPgID);
    }

    setCurrentPage(nextPage);
    
  }

  /**
   *  The action to be executed when the "Finish" button is pressed.
   */
  public void finishAction() {

    // * * * F I N I S H * * *
    pageStack.push(this.getCurrentPage());
    //if we add auto save method in finish action, it will store the last page.
    //however, the last page is summary, it has no metadata. Moreover, the collection data
    // will be execute twice ("auto save" is the first one, line 731 is the second one.
    //so I decided not to save automatically here.
    //autoSaveInCompletePackage();
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
    doCleanUp(FINISHSOURCE);
  }
  
  /**
   * Action will be executed when "Save For Later" button is clicked.
   * It will save incomplete document and data file into morpho dir.
   */
  public void saveForLaterAction()
  {
    if(!disableIncompleteSaving)
    {
        if(currentPage != null)
        {
          if(!currentPage.onSaveForLaterAction())
          {
            return;
          }
          boolean success = currentPage.onAdvanceAction();
          if(!success)
          {
            JOptionPane.showMessageDialog(frame, Language.getInstance().getMessage("SaveForLater.warn1"), Language.getInstance().getMessage("Warning"),
                JOptionPane.WARNING_MESSAGE);
            return;
          }
          pageStack.push(currentPage);
          int choice = JOptionPane.showConfirmDialog(frame,
        		  										/*"This incomplete data package will be saved locally and the wizard window will be closed.\n"
        		  										+"Are you sure to continue this action?",*/
        		  										Language.getInstance().getMessage("SaveIncompleteWarning"),
        		  										"Save?",JOptionPane.YES_NO_OPTION);
          if(choice == JOptionPane.YES_OPTION)
          {
            try
            {
              manualSaveInCompletePackage();
            }
            catch(Exception e)
            {
              Log.debug(5, "Couldn't save the incomplete document "+e.getMessage());
              return;
            }
            listener.wizardSavedForLater();
            doCleanUp(SAVELATERSOURCE);
            this.setVisible(false);
            this.dispose();
          
        }   
      }
     
    }
  }

  public Node collectDataFromPages() {

    //pages map
    Map pageMap = new HashMap();

    //results Map:
    OrderedMap wizData = new OrderedMap();
    OrderedMap accessData = null;
    int textImportAttributeSize = 0;

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
	      if(nextPageID.startsWith(DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE))
	      {
	    	  textImportAttributeSize++;
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
    AbstractUIPage REPLICATION_POLICY
    	= (AbstractUIPage)pageMap.get(DataPackageWizardInterface.REPLICATION_POLICY);
    AbstractUIPage DATA_LOCATION
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.DATA_LOCATION);
    /*AbstractUIPage TEXT_IMPORT_WIZARD
        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.TEXT_IMPORT_WIZARD);*/
    TextImportEntity TEXT_IMPORT_ENTITY 
        =  (TextImportEntity)pageMap.get(DataPackageWizardInterface.TEXT_IMPORT_ENTITY);
    AbstractUIPage TEXT_IMPORT_DELIMITER
        =  (AbstractUIPage)pageMap.get(DataPackageWizardInterface.TEXT_IMPORT_DELIMITERS);
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
    
    // REPLICATION:
    if (REPLICATION_POLICY != null) {
      addPageDataToResultsMap(REPLICATION_POLICY, wizData);
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

   

    /*if (TEXT_IMPORT_WIZARD != null) {
      addPageDataToResultsMap( TEXT_IMPORT_WIZARD, wizData);
    }*/

    if (ENTITY != null) {
      addPageDataToResultsMap( ENTITY, wizData);
    }

    if (DATA_FORMAT != null) {
      addPageDataToResultsMap( DATA_FORMAT, wizData);
    }
    
    if(TEXT_IMPORT_ENTITY != null)
    {
    	addPageDataToResultsMap(TEXT_IMPORT_ENTITY, wizData);
    }
    
    if(TEXT_IMPORT_DELIMITER != null)
    {
    	addPageDataToResultsMap(TEXT_IMPORT_DELIMITER, wizData);
    }
    
    //Text_Import_Attribute pages has different numbers in different importing.
    // we got it from textImportAttributeSize. The TextImportAttribute index is dynamic too,
    //it is DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE appending a number
    for (int i=0; i<textImportAttributeSize;i++)
    {
    	AbstractUIPage TEXT_IMPORT_ATTRIBUTE
        =  (AbstractUIPage)pageMap.get(DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE+i);
    	addPageDataToResultsMap(TEXT_IMPORT_ATTRIBUTE, wizData);
    }

    if (DATA_LOCATION != null) {
      addPageDataToResultsMap(DATA_LOCATION, wizData);
    }
    
    //attach the number of records
    if(TEXT_IMPORT_ENTITY != null)
    {
    	addPageDataToResultsMap(TEXT_IMPORT_ENTITY.getNumberOfRecordsData(), wizData);
    }
    // now add unique ID's to all dataTables and attributes
    addIDs(
        new String[] {
        "/eml:eml/dataset/dataTable",
        "/eml:eml/dataset/dataTable/attributeList/attribute"
    }
        , wizData);
    
    if(status != null && status.equals(IncompleteDocSettings.PACKAGEWIZARD))
    {
      addPageDataToResultsMap(generatePackageIDMap(autoSaveID), wizData);
    }

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
       rootNode = getNewEmptyDataPackageDOM(WizardSettings.NEW_EML211_DOCUMENT_TEXT_WITHOUTACCESS);
    }
    else
    {
    	//for having access subtree
    	rootNode = getNewEmptyDataPackageDOM(WizardSettings.NEW_EML211_DOCUMENT_TEXT_WITHACCESS);
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
  
  /*
   * Generate ordered map contains package id info
   */
  private OrderedMap generatePackageIDMap(String docid)
  {
    OrderedMap map = new OrderedMap();
    map.put("/eml:eml/@packageId", docid);
    return map;
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
  public void manualSaveInCompletePackage() throws Exception
  {
    boolean userSaved = true;
    saveInCompletePackage(autoSaveID, userSaved);
  }
  
  /*
   * save incomplete package automatically
   */
  private void autoSaveInCompletePackage()
  {
    try
    {
      boolean userSaved = false;
	    saveInCompletePackage(autoSaveID, userSaved);
    }
    catch(Exception e)
    {
      Log.debug(10, "Auto saving failes since "+e.getMessage());
    }
  }
  
  /*
   * save incomplete package with given id and if it is user saved
   */
  private void saveInCompletePackage(String saveID, boolean userSaved) throws Exception
  {
	  if(saveID != null && !disableIncompleteSaving)
	  {
		  String emlDoc = "";
		  Node temp = null;
		  try
		  {
		      temp = collectDataFromPages();
		  }
		  catch(Exception e)
		  {
			  Log.debug(30, "Some error happens in collecting page data in WizardContainerFrame.savInCompletePackage "+e.getMessage()+
					    " and the saving will be terminated ");
			  throw e;
		  }
		  if(status != null && status.equals(IncompleteDocSettings.PACKAGEWIZARD))
		  {	  
			  try
			  {
			     // for datapackage wizard
		         emlDoc = XMLUtil.getDOMTreeAsString(temp);
		         //System.out.println("the original eml "+emlDoc);
		         emlDoc = addPackageWizardIncompleteInfo(emlDoc);
		      Log.debug(40, "The partial eml document is :\n"+emlDoc);
			  }
			  catch(Exception e)
			  {
				  Log.debug(30, "Some error happens in WizardContainerFrame.savInCompletePackage "+e.getMessage()+
				    " and the saving will be terminated ");
		      throw e;
			  }
		      //System.out.println("the eml after appending incomplete info  "+emlDoc);
		      if(!userSaved)
		      {
		        autoSavingPackageInCompleteDir(saveID, emlDoc);
		      }
		      else
		      {
		        userSavingPackageInCompleteDir(saveID, emlDoc);
		      }
		    
		  }
		  else if(status != null && (status.equals(IncompleteDocSettings.ENTITYWIZARD) ||status.equals(IncompleteDocSettings.CODEDEFINITIONWIZARD)) )
		  {
			  // for entity wizard
			  if(adp != null)
			  {
				  if(isCurrentPageInEntityPageList())
				  {
					 try
					 {
				       adp.replaceEntity(temp, entityIndex);
					 }
					 catch(Exception e)
					 {
						 Log.debug(30, "Some error happens in replaceEntity in WizardContainerFrame.savInCompletePackage "+e.getMessage()+
						    " and the saving will be terminated ");
				      throw e;
					 }
				  }
				  try
				  {
				    emlDoc = XMLUtil.getDOMTreeAsString(adp.getMetadataNode());
				    if(status.equals(IncompleteDocSettings.ENTITYWIZARD))
				    {
				      emlDoc = addEntityWizardIncompleteInfo(emlDoc);
				    }
				    else
				    {
				      emlDoc = addCodeDefinitionWizardIncompleteInfo(emlDoc);
				    }
				    
				    Log.debug(40, "The partial eml document is :\n"+emlDoc);
				  }
				  catch(Exception e)
				  {
					  //remove the entity we needed to adp
				    if(isCurrentPageInEntityPageList())
					  {
					     //Node entityNode = adp.deleteEntity(entityIndex);
					     //if(entityNode == null)
					     //{
					       adp.deleteLastEntity();
					     //}
					  }
					  Log.debug(30, "Some error happens in collecting page data in WizardContainerFrame.savInCompletePackage "+e.getMessage()+
					    " and the saving will be terminated ");
			      throw e;
				  }
			      //System.out.println("the eml after appending incomplete info  "+emlDoc);
				  if(!userSaved)
			    {
				    autoSavingPackageInCompleteDir(saveID, emlDoc);
			    }
			    else
			    {
			      userSavingPackageInCompleteDir(saveID, emlDoc);
			    }
			    //remove the entity we needed to adp
			    if(isCurrentPageInEntityPageList())
				  {
				     //adp.deleteEntity(entityIndex);
			      adp.deleteLastEntity();
				  }
			  }			  
		  }
		 
	  }
  }
  
  /*
   * Save package into incomplete dir with given id
   */
  private void autoSavingPackageInCompleteDir(String docid, String xml)
  {
	  InputStream stringStream = new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")));
	  Morpho.thisStaticInstance.getLocalDataStoreService().saveIncompleteFile(docid, stringStream);
  }
  
  /*
   * Save package into incomplete dir with given id
   */
  private void userSavingPackageInCompleteDir(String docid, String xml) throws Exception
  {
    //FileSystemDataStore store = new FileSystemDataStore(Morpho.thisStaticInstance);
    StringReader reader = new StringReader(xml);
  //store.saveFile(docid, reader);
    try 
    {
     ServiceController services = ServiceController.getInstance();
     ServiceProvider provider =
           services.getServiceProvider(DataPackageInterface.class);
     DataPackageInterface dataPackage = (DataPackageInterface)provider;
     dataPackage.saveIncompleteDocumentForLater(docid, reader);
     JOptionPane.showMessageDialog(frame, Language.getInstance().getMessage("DataPackageWasSavedAsId") + " " + docid, Language.getInstance().getMessage("Information"),
         JOptionPane.PLAIN_MESSAGE);
    } 
    catch (ServiceNotHandledException snhe) 
    {

      Log.debug(6, snhe.getMessage());
      throw snhe;
    }
    catch(Exception e)
    {
      throw e;
    }
    
  }
  
 
  
  public boolean isCurrentPageInEntityPageList()
  {
	  boolean in = false;
	  if(currentPage != null)
	  {
		  String pageID = currentPage.getPageID();
		  if(pageID != null)
		  {
			  for(int i =0; i<entityPageIDList.length; i++)
			  {
				  String entityPageID = entityPageIDList[i];
				  if(pageID.startsWith(entityPageID))
				  {
					  in = true;
					  Log.debug(35, "find current page id "+pageID+" in the entity id ist");
					  break;
				  }
			  }
		  }
	  }
	  return in;
  }
  
  /*
   * Adds some new information into additional part in EML.
   * In New Package Wizard, it is simple. It will replace the </eml> by
   * <additionalMetadata>
   *   <metadata>
   *      <incomplete>
   *         <packagewizard>
   *            <class><name>edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyIntro</name><para>...<para></class>
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
	  StringBuffer emlWithIncompleteInfo = new StringBuffer();
	  if(originalEML != null)
	  {
		  //System.out.println("the original eml is "+originalEML);
		  int index = originalEML.lastIndexOf(IncompleteDocSettings.EMLCLOSINGTAG);
		  if (index != -1)
		  {
			  //it has </eml:eml> closing tag. Note: the orignalEML never has the incompleteMetadata since it from abtractDataPackage
			  //removes the </packageWizard></metadata></additionalMetadata></eml:eml> from original eml
			  emlWithIncompleteInfo.append(originalEML.substring(0,index));
			  //appends additionalMetadata par original eml
			  emlWithIncompleteInfo.append(IncompleteDocSettings.ADDITIONALMETADATAOPENINGTAG+
              IncompleteDocSettings.METADATAOPENINGTAG+IncompleteDocSettings.PACKAGEWIZARDOPENINGTAG);
			  emlWithIncompleteInfo.append(IncompleteDocSettings.TRACINGCHANGEOPENINGTAG+IncompleteDocSettings.TRUE+
			                                             IncompleteDocSettings.TRACINGCHANGECLOSINGTAG);
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
						  emlWithIncompleteInfo.append(IncompleteDocSettings.CLASSOPENINGTAG+IncompleteDocSettings.NAMEOPENINGTAG+
						                                      className+IncompleteDocSettings.NAMECLOSINGTAG);
						  if(page instanceof PartyMainPage)
						  {
							  PartyMainPage partPage = (PartyMainPage)page;
							  String role = partPage.role;
							  emlWithIncompleteInfo.append(IncompleteDocSettings.CLASSPARAMETEROPENINGTAG+
							                                      role+IncompleteDocSettings.CLASSPARAMETERCLOSINGTAG);
						  }
						  else if(page instanceof Project)
						  {
							  emlWithIncompleteInfo.append(IncompleteDocSettings.CLASSPARAMETEROPENINGTAG+
                                      "true"+IncompleteDocSettings.CLASSPARAMETERCLOSINGTAG);
						  }
						  else if(page instanceof Access)
						  {
							  Access accessPage = (Access)page;
							  boolean isEntity = accessPage.isEntity();
							  emlWithIncompleteInfo.append(IncompleteDocSettings.CLASSPARAMETEROPENINGTAG+
                                      isEntity+IncompleteDocSettings.CLASSPARAMETERCLOSINGTAG);
						  }
						  else if(page instanceof Methods)
						  {
							  emlWithIncompleteInfo.append(IncompleteDocSettings.CLASSPARAMETEROPENINGTAG+
                                      "true"+IncompleteDocSettings.CLASSPARAMETERCLOSINGTAG);
						  }
						  else if(page instanceof Taxonomic)
						  {
							  emlWithIncompleteInfo.append(IncompleteDocSettings.CLASSPARAMETEROPENINGTAG+
                                      "true"+IncompleteDocSettings.CLASSPARAMETERCLOSINGTAG);
						  }
						  
						  
						  emlWithIncompleteInfo.append(IncompleteDocSettings.CLASSCLOSINGTAG);
					  }
				  }
			  }
             
              emlWithIncompleteInfo.append(IncompleteDocSettings.PACKAGEWIZARDCLOSINGTAG+
              IncompleteDocSettings.METADATACLOSINGTAG+IncompleteDocSettings.ADDITIONALMETADATACLOSINGTAG+
              IncompleteDocSettings.EMLCLOSINGTAG);
			                                       
		  }
		  else
		  {
			  //it doesn't have </eml:eml> closing tag, we use orignal document as modified one.
			  emlWithIncompleteInfo.append(originalEML);
			  
		  }
	  }
	  
	  return emlWithIncompleteInfo.toString();
  }
  
  
  /*
   * Adds some new information into additional part in EML for entity wizard
   * In Entity Wizard, it is simple. It will replace the </eml> by
   * <additionalMetadata>
   *   <metadata>
   *      <incomplete>
   *         <entityWizard>
   *            <index>1<index>
   *            <class><name>edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyIntro</name><para>...<para></class>
   *         </entityWizard>
   *      </incomplete>
   *   <metadata>
   * <additonalMetacat>
   * </eml>
   *         
   *    
   */
  private String addEntityWizardIncompleteInfo(String originalEML)
  {
	  StringBuffer emlWithIncompleteInfo = new StringBuffer();
	  if(originalEML != null)
	  {
		  //System.out.println("the original eml is "+originalEML);
		  int index = originalEML.lastIndexOf(IncompleteDocSettings.EMLCLOSINGTAG);
		  if (index != -1)
		  {
			  //it has </eml:eml> closing tag. Note: orignalEML never has the incompleteMetadata since it from abtractDataPackage.
			  //removes the </packageWizard></metadata></additionalMetadata></eml:eml> from original eml
			  emlWithIncompleteInfo.append(originalEML.substring(0,index));
			  //appends additionalMetadata par original eml
			  emlWithIncompleteInfo.append(IncompleteDocSettings.ADDITIONALMETADATAOPENINGTAG+
              IncompleteDocSettings.METADATAOPENINGTAG+IncompleteDocSettings.ENTITYWIZARDOPENINGTAG);
			  emlWithIncompleteInfo.append(IncompleteDocSettings.TRACINGCHANGEOPENINGTAG+IncompleteDocSettings.TRUE+
            IncompleteDocSettings.TRACINGCHANGECLOSINGTAG);
        emlWithIncompleteInfo.append(IncompleteDocSettings.INDEXOPENINGTAG+entityIndex+IncompleteDocSettings.INDEXCLOSINGTAG);			  
        emlWithIncompleteInfo.append(getsImportAndPageInfo());   
        emlWithIncompleteInfo.append(IncompleteDocSettings.ENTITYWIZARDCLOSINGTAG+
        IncompleteDocSettings.METADATACLOSINGTAG+IncompleteDocSettings.ADDITIONALMETADATACLOSINGTAG+
        IncompleteDocSettings.EMLCLOSINGTAG);
			                                       
		  }
		  else
		  {
			  //it doesn't have </eml:eml> closing tag, we use orignal document as modified one.
			  emlWithIncompleteInfo.append(originalEML);
			  
		  }
	  }
	  
	  return emlWithIncompleteInfo.toString();
  }

  /*
   * Adds some new information into additional part in EML for entity wizard
   * In Entity Wizard, it is simple. It will replace the </eml> by
   * <additionalMetadata>
   *   <metadata>
   *      <incomplete>
   *         <codeDefinitionWizard>
   *            <index>1<index>
   *            <class><name>edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyIntro</name><para>...<para></class>
   *         </codeDefinitionWizard>
   *      </incomplete>
   *   <metadata>
   * <additonalMetacat>
   * </eml>
   *         
   *    
   */
  private String addCodeDefinitionWizardIncompleteInfo(String originalEML)
  {
    StringBuffer emlWithIncompleteInfo = new StringBuffer();
    if(originalEML != null)
    {
      //System.out.println("the original eml is "+originalEML);
      int index = originalEML.lastIndexOf(IncompleteDocSettings.EMLCLOSINGTAG);
      if (index != -1)
      {
        //it has </eml:eml> closing tag. Note: orignalEML never has the incompleteMetadata since it from abtractDataPackage.
        //removes the </packageWizard></metadata></additionalMetadata></eml:eml> from original eml
        emlWithIncompleteInfo.append(originalEML.substring(0,index));
        //appends additionalMetadata par original eml
        emlWithIncompleteInfo.append(IncompleteDocSettings.ADDITIONALMETADATAOPENINGTAG+
              IncompleteDocSettings.METADATAOPENINGTAG+IncompleteDocSettings.CODEDEFINITIONWIZARDOPENINGTAG);
        emlWithIncompleteInfo.append(IncompleteDocSettings.TRACINGCHANGEOPENINGTAG+IncompleteDocSettings.TRUE+
            IncompleteDocSettings.TRACINGCHANGECLOSINGTAG);
        emlWithIncompleteInfo.append(IncompleteDocSettings.INDEXOPENINGTAG+entityIndex+IncompleteDocSettings.INDEXCLOSINGTAG);
        emlWithIncompleteInfo.append(IncompleteDocSettings.EDITINGATTRIBUTEOPENINGTAG);
        emlWithIncompleteInfo.append(IncompleteDocSettings.EDITINGENTITYINDEXOPENINGTAG);
        emlWithIncompleteInfo.append(editingEntityIndex);
        emlWithIncompleteInfo.append(IncompleteDocSettings.EDITINGENTITYINDEXCLOSINGTAG);
        emlWithIncompleteInfo.append(IncompleteDocSettings.EDITINGATTRIBUTEINDEXOPENINGTAG);
        emlWithIncompleteInfo.append(editingAttributeIndex);
        emlWithIncompleteInfo.append(IncompleteDocSettings.EDITINGATTRIBUTEINDEXCLOSINGTAG);
        if(beforeSelectionFlag != null)
        {
          emlWithIncompleteInfo.append(IncompleteDocSettings.EDITINGATTRIBUTEINSERTIONOPENINGTAG);
          emlWithIncompleteInfo.append(beforeSelectionFlag.booleanValue());
          emlWithIncompleteInfo.append(IncompleteDocSettings.EDITINGATTRIBUTEINSERTIONCLOSINGTAG);
        }
        if(editingAttributeMap != null)
        {
          emlWithIncompleteInfo.append(IncompleteDocSettings.EDITINGATTRIBUTEMAPOPENINGTAG);
          emlWithIncompleteInfo.append(editingAttributeMap.toXML());
          emlWithIncompleteInfo.append(IncompleteDocSettings.EDITINGATTRIBUTEMAPCLOSINGTAG);
        }
        emlWithIncompleteInfo.append(IncompleteDocSettings.EDITINGATTRIBUTECLOSINGNGTAG);
        emlWithIncompleteInfo.append(getsImportAndPageInfo());
        emlWithIncompleteInfo.append(IncompleteDocSettings.CODEDEFINITIONWIZARDCLOSINGTAG+
        IncompleteDocSettings.METADATACLOSINGTAG+IncompleteDocSettings.ADDITIONALMETADATACLOSINGTAG+
        IncompleteDocSettings.EMLCLOSINGTAG);
                                             
      }
      else
      {
        //it doesn't have </eml:eml> closing tag, we use orignal document as modified one.
        emlWithIncompleteInfo.append(originalEML);
        
      }
    }
    return emlWithIncompleteInfo.toString();
  }
  
  /*
   * Gets attributeImport information and Page info
   */
  private String getsImportAndPageInfo()
  {
    StringBuffer emlWithIncompleteInfo = new StringBuffer();
    if(adp != null)
    {
      //put the import attribute information into metadata.
      emlWithIncompleteInfo.append(adp.importAttributesToXML());
    }
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
          emlWithIncompleteInfo.append(IncompleteDocSettings.CLASSOPENINGTAG+IncompleteDocSettings.NAMEOPENINGTAG+
                                              className+IncompleteDocSettings.NAMECLOSINGTAG);
          if(page instanceof DataLocation)
          {
            //add data location into variable pair - key and value
            DataLocation location = (DataLocation)page;
            File dataFile = location.getDataFile();
            if (dataFile != null)
            {
      
              //we need to store the full path of the file
              String dataFilePath = dataFile.getAbsolutePath();
              emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG+IncompleteDocSettings.KEYOPENINGTAG+
                  DataLocation.TEXTFILEPATH+IncompleteDocSettings.KEYCLOSINGTAG+
                  IncompleteDocSettings.VALUEOPENINGTAG+dataFilePath+IncompleteDocSettings.VALUECLOSINGTAG+
                  IncompleteDocSettings.VARIABLECLOSINGTAG);              
              
            }
            short lastEvent = location.getLastEvent();
            Log.debug(35, "last event is"+lastEvent);
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG+IncompleteDocSettings.KEYOPENINGTAG+
                DataLocation.LASTEVENT+IncompleteDocSettings.KEYCLOSINGTAG+
                IncompleteDocSettings.VALUEOPENINGTAG+lastEvent+IncompleteDocSettings.VALUECLOSINGTAG+
                IncompleteDocSettings.VARIABLECLOSINGTAG);
          }
          else if(page instanceof TextImportEntity)
          {
            //add additional info about if ColumnLabelInStartingRow box is checked
            TextImportEntity textEntity = (TextImportEntity)page;
            boolean isSelected = textEntity.isColumnLabelInStartingRowCheckBoxChecked();
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG+IncompleteDocSettings.KEYOPENINGTAG+
                textEntity.COLUMNLABELINSTARTINGROW+IncompleteDocSettings.KEYCLOSINGTAG+
                IncompleteDocSettings.VALUEOPENINGTAG+isSelected+IncompleteDocSettings.VALUECLOSINGTAG+
                IncompleteDocSettings.VARIABLECLOSINGTAG);
          }
          else if(page instanceof TextImportDelimiters)
          {
            //add additional info about if ColumnLabelInStartingRow box is checked
            TextImportDelimiters textDelimiter = (TextImportDelimiters)page;
            boolean ingoreConsecutiveDelimiters = textDelimiter.ignoreConsequtiveDelimiters();
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG+IncompleteDocSettings.KEYOPENINGTAG+
                textDelimiter.IGNORECONSECUTIVEDELIMITERS+IncompleteDocSettings.KEYCLOSINGTAG+
                IncompleteDocSettings.VALUEOPENINGTAG+ingoreConsecutiveDelimiters+IncompleteDocSettings.VALUECLOSINGTAG+
                IncompleteDocSettings.VARIABLECLOSINGTAG);
          }
          else if(page instanceof CodeDefinition)
          {
            CodeDefinition codeDef = (CodeDefinition)page;
            /*emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.KEYOPENINGTAG+
                CodeDefnPanel.SELECTEDENTITYINDEX+IncompleteDocSettings.KEYCLOSINGTAG+
                IncompleteDocSettings.VALUEOPENINGTAG+codeDef.getSelectedEntityIndex()+IncompleteDocSettings.VALUECLOSINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLECLOSINGTAG);*/
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.KEYOPENINGTAG+
                CodeDefnPanel.CODECOLUMNINDEX+IncompleteDocSettings.KEYCLOSINGTAG+
                IncompleteDocSettings.VALUEOPENINGTAG+codeDef.getSelectedCodeColumnIndex()+IncompleteDocSettings.VALUECLOSINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLECLOSINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.KEYOPENINGTAG+
                CodeDefnPanel.DEFINITIONCOLUMNINDEX+IncompleteDocSettings.KEYCLOSINGTAG+
                IncompleteDocSettings.VALUEOPENINGTAG+codeDef.getSelectedDefinitionColumnIndex()+IncompleteDocSettings.VALUECLOSINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLECLOSINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.ADDITIONALINFOOPENINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.REMOVEDIMPORTATTRIBUTEOPENINGTAG);
            emlWithIncompleteInfo.append(AbstractDataPackage.transformOneImportAttributeToXML(codeDef.getRemovedImportAttributeInfo()));
            emlWithIncompleteInfo.append(IncompleteDocSettings.REMOVEDIMPORTATTRIBUTECLOSINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.ADDITIONALINFOCLOSINGTAG);
            
          }
          else if(page instanceof CodeImportPage)
          {
            CodeImportPage codeImport = (CodeImportPage)page;
            short importChoice = codeImport.getImportChoice();
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.KEYOPENINGTAG+
                CodeImportPage.IMMPORTCHOICE+IncompleteDocSettings.KEYCLOSINGTAG+
                IncompleteDocSettings.VALUEOPENINGTAG+importChoice+IncompleteDocSettings.VALUECLOSINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLECLOSINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.KEYOPENINGTAG+
                CodeImportPage.ENTITYINDEXINCREASED+IncompleteDocSettings.KEYCLOSINGTAG+
                IncompleteDocSettings.VALUEOPENINGTAG+codeImport.isEntityIndexIncreased()+IncompleteDocSettings.VALUECLOSINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLECLOSINGTAG);
            
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.KEYOPENINGTAG+
                CodeImportPage.ISENTITYADDEDINPREVIOUSCLYCLE+IncompleteDocSettings.KEYCLOSINGTAG+
                IncompleteDocSettings.VALUEOPENINGTAG+codeImport.isEntityAddedInPreviousCycle()+IncompleteDocSettings.VALUECLOSINGTAG);
            emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLECLOSINGTAG);
            
            if(importChoice == CodeImportPage.IMPORT_DONE)
            {
              /*emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG);
              emlWithIncompleteInfo.append(IncompleteDocSettings.KEYOPENINGTAG+
                  CodeDefnPanel.SELECTEDENTITYINDEX+IncompleteDocSettings.KEYCLOSINGTAG+
                  IncompleteDocSettings.VALUEOPENINGTAG+codeImport.getSelectedEntityIndex()+IncompleteDocSettings.VALUECLOSINGTAG);
              emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLECLOSINGTAG);*/
              emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG);
              emlWithIncompleteInfo.append(IncompleteDocSettings.KEYOPENINGTAG+
                  CodeDefnPanel.CODECOLUMNINDEX+IncompleteDocSettings.KEYCLOSINGTAG+
                  IncompleteDocSettings.VALUEOPENINGTAG+codeImport.getSelectedCodeColumnIndex()+IncompleteDocSettings.VALUECLOSINGTAG);
              emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLECLOSINGTAG);
              emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLEOPENINGTAG);
              emlWithIncompleteInfo.append(IncompleteDocSettings.KEYOPENINGTAG+
                  CodeDefnPanel.DEFINITIONCOLUMNINDEX+IncompleteDocSettings.KEYCLOSINGTAG+
                  IncompleteDocSettings.VALUEOPENINGTAG+codeImport.getSelectedDefinitionColumnIndex()+IncompleteDocSettings.VALUECLOSINGTAG);
              emlWithIncompleteInfo.append(IncompleteDocSettings.VARIABLECLOSINGTAG);
              emlWithIncompleteInfo.append(IncompleteDocSettings.ADDITIONALINFOOPENINGTAG);
              emlWithIncompleteInfo.append(IncompleteDocSettings.REMOVEDIMPORTATTRIBUTEOPENINGTAG);
              emlWithIncompleteInfo.append(AbstractDataPackage.transformOneImportAttributeToXML(codeImport.getRemovedImportAttributeInfo()));
              emlWithIncompleteInfo.append(IncompleteDocSettings.REMOVEDIMPORTATTRIBUTECLOSINGTAG);
              emlWithIncompleteInfo.append(IncompleteDocSettings.ADDITIONALINFOCLOSINGTAG);
            }
          }
          emlWithIncompleteInfo.append(IncompleteDocSettings.CLASSCLOSINGTAG);
        }
      }
    }
    return emlWithIncompleteInfo.toString();
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
  protected void addPageDataToResultsMap(AbstractUIPage page,
                                       OrderedMap resultsMap) {

    
	  if(page != null)
	  {
        OrderedMap pageData = page.getPageData();
        addPageDataToResultsMap(pageData, resultsMap);
	  }
	  else
	  {
		  Log.debug(10, "The page is null in WizardContainerFrame.addPageDataToResultsMap(AbstractUIPage page, OrderedMap resultsMap)");
	  }
    
  }
  
  private void addPageDataToResultsMap(OrderedMap pageData, OrderedMap resultsMap)
  {
	  String nextKey = null;
	  if (pageData == null) return;

	    Iterator it = pageData.keySet().iterator();

	    if (it == null) return;

	    while (it.hasNext()) {

	      nextKey = (String) it.next();

	      if (nextKey == null || nextKey.trim().equals("")) continue;

	      //now excape all characters that might cause a problem in XML:
//	      resultsMap.put(nextKey, XMLUtil.normalize(nextPgData.get(nextKey)));
	      resultsMap.put(nextKey, pageData.get(nextKey));

	    } // end while
  }

 
  /**
   *  The action to be executed when the "Prev" button is pressed
   *  @param storeCurrentPageIntoStack  indicate if store the current page into stack
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

	  int opt = JOptionPane.showConfirmDialog(this,
	            /*"If you cancel, *all* work done in this wizard "+
	            "will be lost.\nIf you want to resume the wizard later, choose \"No\", then click on the \"Save for Later\" button.\nAre you sure you want to cancel? ",*/
			  	Language.getInstance().getMessage("CancelWarning_1")
			  	+"\n"
			  	+Language.getInstance().getMessage("CancelWarning_2")
			  	+"\n"
			  	+ Language.getInstance().getMessage("CancelWarning_3"),
	            "DO YOU WANT TO CONTINUE?",
	            JOptionPane.YES_NO_OPTION);
	   if (opt == JOptionPane.NO_OPTION) 
	   {
	        return;
	    }
    //AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
    if(adp != null) {
      Log.debug(32, "WizardContainerFrame.cancelAction ==========");
      //deletes all newly needed entities. We must delete the entity by the order from big to small
      int size = neededCancelingEntityList.size()-1;
      for(int i=size; i>=0; i--)
      {
    	  int index = -1;
    	  try
    	  {
    		  index = ((Integer)neededCancelingEntityList.elementAt(i)).intValue();
    	  }
    	  catch(Exception e)
    	  {
    		  continue;
    	  }
    	  adp.deleteEntity(index);
      }
      /*adp.deleteAllEntities();
      edu.ucsb.nceas.morpho.datapackage.Entity[] arr = adp.getOriginalEntityArray();
      if(arr != null) 
      {
        Log.debug(30, "replacing subtree in WizardContainerFrame.cancelAction - ");
        for(int i = 0; i < arr.length; i++) {
        	 Log.debug(32, "adding entity - " + i);
             adp.addEntity(arr[i]);
        }
      }*/

    }

    this.setVisible(false);

    listener.wizardCanceled();
    Log.debug(30, "the autosaved id is "+autoSaveID+" in WizardContainerFrame.cancel method");
    // now clean up
    doCleanUp(CACELSOURCE);
    if(autoSaveID != null && status.equals(IncompleteDocSettings.PACKAGEWIZARD))
    {
    	//FileSystemDataStore store = new FileSystemDataStore(Morpho.thisStaticInstance);
    	//store.deleteInCompleteFile(autoSaveID);
    	Morpho.thisStaticInstance.getLocalDataStoreService().deleteAutoSavedFile(adp);
    }
    else if(autoSaveID != null)
    {
    	Log.debug(30, "The entity wizard is canceled and we need to dump the adp to auto-saved file");
    	dumpPackageToAutoSaveFile(autoSaveID);
    }  
    this.dispose();
  }


  private void doCleanUp(String source) {

    UIController.getInstance().setWizardNotRunning();
    String docid = null;
    if(adp != null)
    {
      docid = adp.getAccessionNumber();
    }  
    //remove the docid from the entity wizard running record.
    if(adp != null && status != null && (status.equals(IncompleteDocSettings.ENTITYWIZARD) ||status.equals(IncompleteDocSettings.CODEDEFINITIONWIZARD)))
    {      
      UIController.getInstance().removeDocidFromEntityWizardRunningRecorder(docid);
      if(source != null && source.equals(CACELSOURCE))
      {
        UIController.getInstance().addDocidToIdleWizardRecorder(autoSaveID);
      }
    }
    if(source != null && source.equals(FINISHSOURCE))
    {
      UIController.getInstance().addDocidToIdleWizardRecorder(autoSaveID);
    }
    else if(source != null && source.equals(SAVELATERSOURCE))
    {
      UIController.getInstance().removeDocidFromIdleWizardRecorder(autoSaveID);
    }
    //clear out pageStack
    pageStack.clear();
    //AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
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
    if(titleLabel!= null)
    {
      titleLabel.setText(newTitle);
    }
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
    if(subtitleLabel != null)
    {
      subtitleLabel.setText(newSubTitle);
    }
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
  
  
  /*
   * In new data package wizard, it should have a project/title subtree for inserting project personnel.
   * However, if we open a crashed datapackage and it may not have this subtree. This will cause
   * an error when user tries to insert a new personnel. So we added this method.
   */
  private void addEmptyProjectTileSubtree()
  {
	  if(adp != null && status.equals(IncompleteDocSettings.PACKAGEWIZARD))
	  {
		  
		  String subtreeGenericName = "projectTitle";
	      int index = 0;
	      Node projectTitleNode = adp.getSubtreeNoClone(subtreeGenericName, index);
	      if( projectTitleNode == null)
	      {
	    	    Node rootNode = null;
	    	    try 
	    	    {
	    	      rootNode = XMLUtilities.getXMLReaderAsDOMTreeRootNode(new StringReader(EMPTYPROJECTTITLE));
	    	    } 
	    	    catch (Exception e) 
	    	    {
	    	      e.printStackTrace();
	    	      Log.debug(5, "unexpected error trying to create new XML document");
	    	      
	    	    }
	    	    if(rootNode != null)
	    	    {
	    	    	adp.insertSubtree(subtreeGenericName, rootNode, index);
	    	    }
	            
	      }
	  }
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
  
  /**
   * method inherits from TableModelListener
   */
  public void tableChanged(TableModelEvent e)
  {
	  Log.debug(30, "Attribute list changed and we need to automatically save the change");
	  //when this method is called, the pageStack doesn't contain the current page -Entity
	  // so we couldn't get the data in entity page. We have to add the Entity into page stack
	  //temporarily, then delete it.
	  synchronized(pageStack)
	  {
		  if(currentPage != null && pageStack != null && currentPage instanceof Entity)
		  {
			  pageStack.add(currentPage);
		  }
		  try
		  {
		     autoSaveInCompletePackage();
		  }
		  catch(Exception ee)
		  {
			  Log.debug(30, "couldn't auto save the updating of attriubte list "+ee.getMessage());
		  }
		  finally
		  {
		      if(currentPage != null && pageStack != null && currentPage instanceof Entity)
			  {
				  pageStack.remove(currentPage);
			  }
		  }
	  }
  }
  

  
  /**
   * Sets the index of the entity which is generating. This number will be valid only
   * the wizard is set an entity one. It also will add the index to a list which will
   * be removed in cancel action
   * @param entityIndex
   */
  public void setEntityIndex(int entityIndex)
  {
	  neededCancelingEntityList.add(entityIndex);
	  this.entityIndex =entityIndex;
  }
  
  /**
   * Gets the current working entity index
   * @return
   */
  public int getEnityIndex()
  {
	  return this.entityIndex;
  }
  
  /**
   * Cleans (removes) every TextImportAttribute page in the page stack.
   * This method will be called when data table was changed.
   */
  public void cleanTextImportAttributePagesInCache()
  {
     synchronized(pageCache)
     {
		  if(pageCache != null)
		  {
			  Vector textImportAttributeKeys = new Vector();
			  int size = pageCache.size();
			  Log.debug(32, "pageCache is not null in WizardContainerFrame.cleanTextImportAttributePagesInStack and cache has size "+size);
			  Set keySet = pageCache.keySet();
			  Iterator iterator = keySet.iterator();
			  while(iterator.hasNext())
			  {
				  String key =(String)iterator.next();
				  AbstractUIPage page = (AbstractUIPage)pageCache.get(key);
				  if(page instanceof TextImportAttribute)
				  {
					  Log.debug(34, "add a key "+key +" which points to TextImportAttribute into a vector.");
					  textImportAttributeKeys.add(key);
				  }
			  }
			  for(int i=0; i<textImportAttributeKeys.size(); i++)
			  {
				  String key = (String)textImportAttributeKeys.elementAt(i);
				  pageCache.remove(key);
				  Log.debug(34, "Successfully remove the page with id "+key+"from the pageChage");
			  }
			  Log.debug(32, "after removing TextImportAttribute page in WizardContainerFrame.cleanTextImportAttributePagesInStack and cache now has size "+pageCache.size());
		  }
     } 
  }
  
  /**
   * Clear whole pageCache
   */
  public void clearPageCache()
  {
	  if(pageCache != null)
	  {
	     synchronized(pageCache)
	     {
		    pageCache.clear();	 

	     }
	  }
  }
  
  /**
   * Determines if any TextImportAttribute in page stack needs to import code/definition.
   * Note:the method doesn't count the the last TextImportAttribute page which is not in stack.
   * @return
   */
  public boolean containsAttributeNeedingImportedCode()
  {
	  boolean contain = false;
	  if(pageStack != null)
	  {
		  for(int i=0; i<pageStack.size();i++)
		  {
			  AbstractUIPage page = (AbstractUIPage)pageStack.elementAt(i);
			  {
				  if (page instanceof TextImportAttribute)
				  {
					   TextImportAttribute attribute = (TextImportAttribute)page;
					   if(attribute.isImportAttributeAdded())
					   {
						   Log.debug(32, "Found a attribute in the pageStack need to import code/defintion");
						   contain = true;
						   break;
					   }
				  }
			  }
		  }
	  }
	  return contain;
  }
  
  /**
   * Remove a page from cache
   * @param pageID of page will be removed
   */
  public void removePageFromCache(String pageID)
  {
	  Log.debug(30, "remove the page "+pageID+ " from the cache");
	  pageCache.remove(pageID);
  }
  
  /**
   * Stores the entity name in entity wizard
   * @param entityName
   */
  /*public void setEntityName(String entityName)
  {
	  this.entityName = entityName;
  }*/
  
  /**
   * Gets the entityName in entitywizard
   * @return
   */
  /*public String getEntityName()
  {
	  return this.entityName;
  }*/
  
  
  /**
   * Set if this frame is importing a code/definition table
   * @param isImportCodeDefinitionTable
   */
  public void setImportCodeDefinitionTable(boolean isImportCodeDefinitionTable)
  {
	  this.isImportCodeDefinitionTable = isImportCodeDefinitionTable;
  }

  /**
   * Gets if this frame is importing a code/definition table
   * @return
   */
  public boolean isImportCodeDefinitionTable()
  {
	  return this.isImportCodeDefinitionTable;
  }
  
  /**
   * Gets the order map of the editing attribute
   * @return
   */
  public OrderedMap getEditingAttributeMap()
  {
    return editingAttributeMap;
  }
  
  /**
   * Sets the ordered map for editing attribute.
   * First we need to check if it is for newTable, if it is. We would not store it
   * @param editingAttributeMap
   */
  public void setEditingAttributeMap(OrderedMap map)
  {
    this.editingAttributeMap = map;
  }

  /**
   * Sets the ordered map for editing attribute.
   * First we need to check if it is for newTable, if it is. We would not store it
   * @param editingAttributeMap
   */
  public void setEditingAttributeMapFromRemovedImportAttribute(ArrayList removedImportAttribute)
  {
    if(removedImportAttribute != null)
    {
      Boolean fromNewTable = (Boolean)removedImportAttribute.get(AbstractDataPackage.NEWTABLEINDEX);
      if(!fromNewTable.booleanValue())
      {
        this.editingAttributeMap = (OrderedMap)removedImportAttribute.get(AbstractDataPackage.ORDEREDMAPINDEX);
      }
      
    }
  }

  /**
   * Gets the index of the editing entity
   * @return
   */
  public int getEditingEntityIndex()
  {
    return editingEntityIndex;
  }

  /**
   * Sets the index of the editing entity
   * @param editingEntityIndex
   */
  public void setEditingEntityIndex(int editingEntityIndex)
  {
    this.editingEntityIndex = editingEntityIndex;
  }

  /**
   * Gets the index of the editing attribute
   * @return
   */
  public int getEditingAttributeIndex()
  {
    return editingAttributeIndex;
  }

  /**
   * Sets the index of the editing attribute
   * @param editingAttributeIndex
   */
  public void setEditingAttributeIndex(int editingAttributeIndex)
  {
    this.editingAttributeIndex = editingAttributeIndex;
  }

  /**
   * Gets if inserting is before selection column
   * @return
   */
  public Boolean getBeforeSelectionFlag()
  {
    return beforeSelectionFlag;
  }

  /**
   * Sets if the inserting is before selection column
   * @param beforeSelectionFlag
   */
  public void setBeforeSelectionFlag(Boolean beforeSelectionFlag)
  {
    this.beforeSelectionFlag = beforeSelectionFlag;
  }

  
  /**
   * Gets the abstract data package associated with the frame
   * @return
   */
  public AbstractDataPackage getAbstractDataPackage()
  {
	  return this.adp;
  }
  



  // * * *  V A R I A B L E S  * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JLabel stepLabel;
  private int PADDING = WizardSettings.PADDING;
  private Container contentPane;
  private JPanel topPanel;
  private JPanel middlePanel;
  private JPanel leftBottomPanel;
  private JPanel bottomBorderPanel;
  private JPanel bottomPanel;
  private JLabel titleLabel = new JLabel("");
  private JLabel subtitleLabel = new JLabel("");
  private JButton saveLaterButton = null;
  private JButton nextButton;
  private JButton prevButton;
  private JButton finishButton;
  private AbstractUIPage currentPage;
  private static JFrame dummyFrame;
  private static JFrame dialogParent;


  

}
