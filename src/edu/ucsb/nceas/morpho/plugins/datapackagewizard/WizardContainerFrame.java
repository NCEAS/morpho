/**
 *  '$RCSfile: WizardContainerFrame.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-09-13 05:40:15 $'
 * '$Revision: 1.9 $'
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

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;

import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener ;

import java.util.Stack;
import java.util.Iterator;

import java.io.StringReader;

import org.w3c.dom.Node;

/**
 *  provides a top-level container for AbstractWizardPage objects. The top (title) panel
 *  and bottom button panel (including the navigation buttons) are all part of 
 *  this class, with the AbstractWizardPage content being nested inside a central area
 */
public class WizardContainerFrame extends JFrame {

  
  public static JFrame frame;
  private DataPackageWizardListener listener;
  
  
  /**
   * Constructor
   */
  public WizardContainerFrame(DataPackageWizardListener listener) {
  
    super();
    frame = this;
    this.listener = listener;
    pageStack   = new Stack();
    pageLib = new WizardPageLibrary();
    init();
    setCurrentPage(WizardSettings.FIRST_PAGE_ID);
  }

  /** 
   *  sets the wizard content for the center pane
   *
   *  @param content  the String pageID of the wizard content to be loaded into 
   *                  the center pane
   */
  public void setCurrentPage(String pageID) {
  
    Log.debug(45,"setCurrentPage called with String ID: "+pageID);
    if (pageID==null) {
      Log.debug(15,"setCurrentPage called with NULL ID");
      return;
    }
    if (!pageLib.containsPageID(pageID)) {
      Log.debug(15,"setCurrentPage: page library does NOT contain ID: "+pageID);
      return;
    }  
    
    AbstractWizardPage pageForID = pageLib.getPage(pageID);
    
    setCurrentPage(pageForID);
  }


  /** 
   *  sets the wizard content for the center pane
   *
   *  @param content the wizard content for the center pane
   */
  public void setCurrentPage(AbstractWizardPage newPage) {
  
    if (newPage==null) {
      Log.debug(45,"setCurrentPage called with NULL WizardPage");
      return;
    }
    Log.debug(45,"setCurrentPage called with WizardPage ID = "
                                                +newPage.getPageID());
    //make new page current page
    this.currentPage = newPage;

    setpageTitle(newPage.getTitle());
    setpageSubtitle(getCurrentPage().getSubtitle());

    middlePanel.removeAll();
    
    middlePanel.add(getCurrentPage(), BorderLayout.CENTER);
    getCurrentPage().setOpaque(false);
    middlePanel.repaint();
    getCurrentPage().onLoadAction();
    updateButtonsStatus();
  }

  /** 
   *  gets the wizard content from the center pane
   *
   *  @return the wizard content from the center pane
   */
  public AbstractWizardPage getCurrentPage() {
  
    return this.currentPage;
  }

  private void updateButtonsStatus() {

    Log.debug(45,"updateButtonsStatus called");
    
    if (getCurrentPage()==null) {
    
      prevButton.setEnabled(false);
      nextFinishButton.setEnabled(false);
      return;
      
    } else {
    
      prevButton.setEnabled(true);
      nextFinishButton.setEnabled(true);
    }
    
    // prev button enable/disable:
    if (pageStack.isEmpty() || pageStack.peek()==null) {
      prevButton.setEnabled(false);
    }
    
    // next/finish button label:
    if (getCurrentPage().getNextPageID()==null) {
      nextFinishButton.setText(WizardSettings.FINISH_BUTTON_TEXT);
    } else {
      nextFinishButton.setText(WizardSettings.NEXT_BUTTON_TEXT);
    }
  }
  
  
  private void init() {
  
    initContentPane();
    initTopPanel();
    initMiddlePanel();
    initBottomPanel();
    initButtons();
  }

  private void initContentPane() {
  
    contentPane = this.getContentPane();
    contentPane.setLayout(new BorderLayout());
  }


  private void initTopPanel() {
  
    Log.debug(45,"WizardContainerFrame starting init()");
    
    titleLabel = new JLabel("");
    titleLabel.setFont(WizardSettings.TITLE_FONT);
    titleLabel.setForeground(WizardSettings.TITLE_TEXT_COLOR);
    titleLabel.setBorder(new EmptyBorder(PADDING,0,PADDING,0));
    
    subtitleLabel = new JLabel("");
    subtitleLabel.setFont(WizardSettings.SUBTITLE_FONT);
    subtitleLabel.setForeground(WizardSettings.SUBTITLE_TEXT_COLOR);
    subtitleLabel.setBorder(new EmptyBorder(PADDING,0,PADDING,0));
    
    topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.setPreferredSize(WizardSettings.TOP_PANEL_DIMS);
    topPanel.setBorder(new EmptyBorder(0,3*PADDING,0,3*PADDING));
    topPanel.setBackground(WizardSettings.TOP_PANEL_BG_COLOR);
    topPanel.setOpaque(true);
    topPanel.add(titleLabel);
    topPanel.add(subtitleLabel);
    contentPane.add(topPanel, BorderLayout.NORTH);
    
  }
  
  private void initMiddlePanel() {
  
    middlePanel = new JPanel();
    middlePanel.setLayout(new BorderLayout());
    middlePanel.setBorder(new EmptyBorder(PADDING,3*PADDING,PADDING,3*PADDING));
    contentPane.add(middlePanel, BorderLayout.CENTER);
  }
  
  private void initBottomPanel() {
  
    bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
    bottomPanel.add(Box.createHorizontalGlue());
    bottomPanel.setOpaque(false);

    bottomPanel.setBorder(
                  BorderFactory.createMatteBorder(2, 0, 0, 0, WizardSettings.TOP_PANEL_BG_COLOR));
//    bottomPanel.setBorder(new EmptyBorder(PADDING,3*PADDING,3*PADDING,PADDING));
    contentPane.add(bottomPanel, BorderLayout.SOUTH);
  }
  
  private void initButtons()  {
  
    prevButton        = addButton(WizardSettings.PREV_BUTTON_TEXT, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        previousAction();
      }
    });
    nextFinishButton  = addButton(WizardSettings.NEXT_BUTTON_TEXT, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        nextFinishAction();
      }
    });
    cancelButton      = addButton(WizardSettings.CANCEL_BUTTON_TEXT, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelAction();
      }
    });
    this.getRootPane().setDefaultButton(nextFinishButton);
  }

  /** 
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. It's up to the content to know 
   *  whether it's the last page or not
   */
  private void nextFinishAction() {
    
    Log.debug(45,"nextFinishAction called");
    
    // if the page's onAdvanceAction() returns false, don't advance...
    if ( !(getCurrentPage().onAdvanceAction()) ) return;
    
    
    if (getCurrentPage().getNextPageID()!=null) {
    
    // * * * N E X T * * *

      //put current page on stack
      Log.debug(45,"setCurrentPage pushing currentPage to Stack ("
                                              +getCurrentPage().getPageID()+")");
      pageStack.push(this.getCurrentPage());
    
      String nextPgID = getCurrentPage().getNextPageID();
      Log.debug(45,"nextFinishAction - next page ID is: "+nextPgID);
    
      setCurrentPage(pageLib.getPage(nextPgID));
    
    } else {
      
    // * * * F I N I S H * * *
      pageStack.push(this.getCurrentPage());
      doFinish();
      return;
    }
  }
  

  
  // call this when user presses "finish"
  private void doFinish() {
  
    this.setVisible(false);
  
    Iterator pagesIterator = pageStack.iterator();
    
    OrderedMap wizData = new OrderedMap();
    OrderedMap nextPgData  = new OrderedMap();
    String nextKey = null;
    String nextVal = null;
  
    while (pagesIterator.hasNext()) {

      nextPgData = ((WizardPage)(pagesIterator.next())).getPageData();
    
      if (nextPgData==null) continue;
    
      Iterator  it = nextPgData.keySet().iterator();
    
      if (it==null) continue;

      while (it.hasNext()) {

        nextKey = (String)it.next();

        if (nextKey==null || nextKey.trim().equals("")) continue;

        nextVal = (String)nextPgData.get(nextKey);
        wizData.put(nextKey, nextVal);
      
      } // end while
    }
    Log.debug(45, "\n\n********** Wizard finished: NVPs:");
    Log.debug(45, wizData.toString());

    
    Node rootNode = null;
    
    try {
    
      rootNode = XMLUtilities.getXMLReaderAsDOMTreeRootNode(
                    new StringReader(WizardSettings.NEW_EML200_DOCUMENT_TEXT));
    } catch (Exception e) {
      e.printStackTrace();
      Log.debug(5, "unexpected error trying to vreate new XML document "
                    +"at start of wizard\n");
      listener.wizardFinished(null);
      return;
    }
    
    
    try {
    
      XMLUtilities.getXPathMapAsDOMTree(wizData, rootNode);

    } catch (Exception e) {
    
      e.printStackTrace();
      Log.debug(5, "unexpected error trying to create new XML document "
                    +"after wizard finished\n");
      listener.wizardFinished(null);
      return;
    }
    
    listener.wizardFinished(rootNode);
    
    Log.debug(45, "\n\n********** Wizard finished: DOM:");
    Log.debug(45, XMLUtilities.getDOMTreeAsString(rootNode));
  }
  
/*******************************************************************************
********************************************************************************
********************************************************************************
********************************************************************************
********************************************************************************
********************************************************************************


function finish() {

  var nvpArray = new Array();

  //dataset.general ////////////////////////////////////////////////////////////
  var generalPage = document.getElementsByAttribute("pageid",
                                            "dataset.general")[0];
  var generalArray = lib.getNVPArrayForSubElementsWithTagName(generalPage,
                                                             "textbox", false);
  // pattern match textbox ID's for titles, and add them to the array.  The
  // abstract from this page will be added to the array after creator, etc.
  for ( var i=0; i < generalArray.length; i++ ) {
    lib.debug("generalArray["+i+"] = "+generalArray[i]+"\n");
    var titleIDString = /\eml:eml\/dataset\/title/;
    if ( generalArray[i].match(titleIDString) ) {
      nvpArray = nvpArray.concat(generalArray[i]);
    }
  }

  // dataset.party.creator ////////////////////////////////////////////////////

  lib.debug("building creator NVP array *************************\n");
  // Get a reference to the list object
  var creatorListXBL =
      document.getElementById("dataset.party.creator.creator_list");
  if (!creatorListXBL) {
    lib.debug("finish(): creatorListXBL is NULL!!! - returning false");
    return false;
  }

  // get an array of rows from the list object
  var creatorListRowsArray = creatorListXBL.getRowObjectsAsArray();

  // iterate through each card in the deck based on the card's ID
  // and append to the NVP array
  if (creatorListRowsArray) {
    lib.debug("creatorListRowsArray=" +creatorListRowsArray+ "\n");
    var nextID;
    var nextCard;
    var nextNVPArray;

    for (var setCount = 0; setCount < creatorListRowsArray.length; setCount++) {
      nextID = creatorListRowsArray[setCount].getAttribute("value");
      nextCard = document.getElementById(nextID);
      nextNVPArray =
      nextCard.get_as_NVP_array("/eml:eml/dataset/creator["+(setCount+1)+"]/");

      nvpArray = nvpArray.concat(nextNVPArray);
    }
  }


  // dataset.party.associatedparty ////////////////////////////////////////////

  var associatedpartyListXBL =
      document.getElementById("dataset.party.associatedparty.associatedparty_list");
  if (!associatedpartyListXBL) {
    lib.debug("finish(): associatedpartyListXBL is NULL!!! - returning false");
    return false;
  }

  // get an array of rows from the list object
  var associatedpartyListRowsArray = associatedpartyListXBL.getRowObjectsAsArray();

  // iterate through each card in the deck based on the card's ID
  // and append to the NVP array
  if (associatedpartyListRowsArray) {
    lib.debug("associatedpartyListRowsArray=" +associatedpartyListRowsArray+ "\n");
    var nextID;
    var nextCard;
    var nextNVPArray;

    for (var setCount = 0; setCount < associatedpartyListRowsArray.length; setCount++) {
      nextID = associatedpartyListRowsArray[setCount].getAttribute("value");
      nextCard = document.getElementById(nextID);
      nextNVPArray =
      nextCard.get_as_NVP_array("/eml:eml/dataset/associatedParty["+(setCount+1)+"]/");
      nvpArray = nvpArray.concat(nextNVPArray);
    }
  }

  // abstract //////////////////////////////////////////////////////////////////
  // pattern match textbox ID's for abstract, and add them to the array.
  for ( var i=0; i < generalArray.length; i++ ) {
    lib.debug("generalArray["+i+"] = "+generalArray[i]+"\n");
    var abstractIDString = /\eml:eml\/dataset\/abstract/;
    if ( generalArray[i].match(abstractIDString) ) {
      nvpArray = nvpArray.concat(generalArray[i]);
    }
  }
  // keywords //////////////////////////////////////////////////////////////////
  var keywordsetListXBL
              = document.getElementById('dataset.keywords.keywordset_list');
  if (!keywordsetListXBL) {
    lib.debug("finish(): keywordsetListXBL is NULL!!! - returning false");
    return false;
  }
  var kwlistRowsArray = keywordsetListXBL.getRowObjectsAsArray();

  if (kwlistRowsArray) {
    var nextID;
    var nextCard;
    var nextNVPArray;

    for (var setCount = 0; setCount < kwlistRowsArray.length; setCount++) {
      nextID = kwlistRowsArray[setCount].getAttribute("value");
      nextCard = document.getElementById(nextID);
      nextNVPArray = nextCard.get_as_NVP_array(
                              "/eml:eml/dataset/keywordSet["+(setCount+1)+"]/",
                              "keywordThesaurus", "keyword");
      nvpArray = nvpArray.concat(nextNVPArray);
    }
  }

  //dataset.rights /////////////////////////////////////////////////////////////
  var rightsPage = document.getElementsByAttribute("pageid",
                                            "dataset.rights")[0];
  var rightsArray = lib.getNVPArrayForSubElementsWithTagName(rightsPage,
                                                             "textbox", false);
  nvpArray = nvpArray.concat(rightsArray);

  // dataset.party.contact ////////////////////////////////////////////////////
  var contactListXBL =
      document.getElementById("dataset.party.contact.contact_list");
  if (!contactListXBL) {
    lib.debug("finish(): contactListXBL is NULL!!! - returning false");
    return false;
  }

  // get an array of rows from the list object
  var contactListRowsArray = contactListXBL.getRowObjectsAsArray();

  // iterate through each card in the deck based on the card's ID
  // and append to the NVP array
  if (contactListRowsArray) {
    lib.debug("contactListRowsArray=" +contactListRowsArray+ "\n");
    var nextID;
    var nextCard;
    var nextNVPArray;

    for (var setCount = 0; setCount < contactListRowsArray.length; setCount++) {
      nextID = contactListRowsArray[setCount].getAttribute("value");
      nextCard = document.getElementById(nextID);
      nextNVPArray =
      nextCard.get_as_NVP_array("/eml:eml/dataset/contact["+(setCount+1)+"]/");
      lib.debug("nextNVPArray = "+nextNVPArray+"******************\n");

      nvpArray = nvpArray.concat(nextNVPArray);
    }
  }

  // dataset.entity ////////////////////////////////////////////////////////////
  // NOTE - even though there are no /eml:eml?.. fields in XUL document, ///////
  // hidden textfields get written to these pages by javascript...       ///////
  var entityPage = document.getElementsByAttribute("pageid",
                                            "dataset.entity")[0];
  var entityArray = lib.getNVPArrayForSubElementsWithTagName(entityPage,
                                                             "textbox", false);
  nvpArray = nvpArray.concat(entityArray);


  // dataset.physical //////////////////////////////////////////////////////////
  // NOTE - even though there are no /eml:eml?.. fields in XUL document, ///////
  // hidden textfields get written to these pages by javascript...       ///////
  var physicalPage = document.getElementsByAttribute("pageid",
                                            "dataset.physical")[0];
  var physicalArray = lib.getNVPArrayForSubElementsWithTagName(physicalPage,
                                                             "textbox", false);
  nvpArray = nvpArray.concat(physicalArray);

  // dataset.physical.distribution /////////////////////////////////////////////
  var distribPage = document.getElementsByAttribute("pageid",
                                            "dataset.physical.distribution")[0];
  var distribArray = lib.getNVPArrayForSubElementsWithTagName(distribPage,
                                                             "textbox", false);
  nvpArray = nvpArray.concat(distribArray);


  // Attributes:
  var attributeListXBL
              = document.getElementById('dataset.dataTable.attributeList');

  if (!attributeListXBL) {
    lib.debug("finish(): attributeListXBL is NULL!!! - returning false");
    return false;
  }

  lib.debug("finish(): attributeListXBL is "+attributeListXBL);

  var attribListRowsArray = attributeListXBL.getRowObjectsAsArray();

  lib.debug("finish(): attribListRowsArray is "+attribListRowsArray);
  lib.debug("finish(): attribListRowsArray.length is "+attribListRowsArray.length);

  lib.debug("finish(): DOING test: if (attribListRowsArray)...");
  if (attribListRowsArray) {
    lib.debug("finish(): DONE test: if (attribListRowsArray)... TRUE");

    var nextID;
    var nextCard;
    var nextNVPArray = new Array();
    var nextNVPArrayIdx = 0;

    lib.debug("finish(): DOING LOOP FOR attribListRowsArray");

    for (var setCount = 0; setCount < attribListRowsArray.length; setCount++) {

      nextID = attribListRowsArray[setCount].getAttribute("value");
      nextCard = document.getElementById(nextID);
      var nextXPathRoot
        = "/eml:eml/dataset/dataTable[1]/attributeList/attribute["
                                                          +(setCount+1) + "]/";

      var attribName
        = nextCard.getElementsByAttribute(
                "id", "dataset.popup.attributes.attributeName")[0].value;

      nextNVPArray[nextNVPArrayIdx++]
                = nextXPathRoot + "attributeName=" + attribName;

      lib.debug("nextCard attribName NVP = "+nextNVPArray[nextNVPArrayIdx-1]);


      var attribDef
        = nextCard.getElementsByAttribute(
                "id", "dataset.popup.attributes.attributeDefinition")[0].value;

      nextNVPArray[nextNVPArrayIdx++]
                = nextXPathRoot + "attributeDefinition="+attribDef;

      lib.debug("nextCard attribDef NVP = "+nextNVPArray[nextNVPArrayIdx-1]);

      var attribMScale
        = nextCard.getElementsByAttribute(
                "id", "attribute.measurementScale.radiogroup")[0].value;

      var meaScaleRoot = nextXPathRoot + "measurementScale/"+attribMScale;

      switch (attribMScale) {

        case "nominal":
        case "ordinal":
          lib.debug("attribMScale is "+attribMScale);
          var measScale_hiddentextfields = nextCard.getElementsByAttribute(
                  "name","dataset.measurementScale.nominal_ordinal.hiddenfield");
          if (measScale_hiddentextfields) {
            var nextHF;
            for (var tfIndex = 0;
                      tfIndex < measScale_hiddentextfields.length; tfIndex++) {

              nextHF = measScale_hiddentextfields[tfIndex];

              nextNVPArray[nextNVPArrayIdx++] = meaScaleRoot
                                              + nextHF.getAttribute("id")
                                              + "="
                                              + nextHF.getAttribute("value");
              lib.debug("added NVP = "+nextNVPArray[nextNVPArrayIdx-1]);
            }
          }
          break;

        case "interval":
          lib.debug("attribMScale is INTERVAL");
          // NOT YET IMPLEMENTED * * *
          break;

        case "ratio":
          lib.debug("attribMScale is RATIO");
          // NOT YET IMPLEMENTED * * *
          break;

        case "dateTime":
          lib.debug("attribMScale is DATETIME");
          // NOT YET IMPLEMENTED * * *
          break;
      }

      nvpArray = nvpArray.concat(nextNVPArray);
    }
  }

  ////////////


  // ignore any NVPs that have an XPATH not beginning with "/eml:eml/":
  var nextIdx = 0;
  var trimmedNVPArray = new Array();

  for (var index = 0; index < nvpArray.length; index++) {

    if (nvpArray[index].indexOf("/eml:eml/") > -1) {
      trimmedNVPArray[nextIdx++] = nvpArray[index];
    }
  }


  ////////////


  lib.add_NVP_array_to_XML_DOM(trimmedNVPArray);

  lib.getCommandProxy().doPost("finalize",null);
  return true;
}

********************************************************************************
********************************************************************************
********************************************************************************
********************************************************************************
********************************************************************************
*******************************************************************************/  
  
  
  
  
  
  
  /** 
   *  The action to be executed when the "Prev" button is pressed
   */
  private void previousAction() {
    
    if (pageStack.isEmpty()) return;
    
    AbstractWizardPage previousPage = (AbstractWizardPage)pageStack.pop();
    if (previousPage==null) {
      Log.debug(15,"previousAction - popped a NULL page from stack");
      return;
    }
    Log.debug(45,"previousAction - popped page with ID: "
                                      +previousPage.getPageID()+" from stack");
    
    getCurrentPage().onRewindAction();
    
    setCurrentPage(previousPage);
  }
    
    
  /** 
   *  The action to be executed when the "Cancel" button is pressed
   */
  private void cancelAction() {
    this.setVisible(false);
  }

  /** 
   *  sets the main title for this page
   *  @param newTitle the page title
   */
  private void setpageTitle(String newTitle) {
    if (newTitle==null) newTitle=" ";
    titleLabel.setText(newTitle);
  }

  /** 
   *  sets the main subtitle for this page
   *
   *  @param newSubTitle the main subtitle
   */
  private void setpageSubtitle(String newSubTitle) {
    if (newSubTitle==null) newSubTitle=" ";
    subtitleLabel.setText(newSubTitle);
  }

  /** 
   *  adds a button with a specified title and action to the bottom panel
   *  @param title text to be shown on the button
   *  @param actionListener the ActionListener that will respond 
   *                       to the button press
   */
  private JButton addButton(String title, ActionListener actionListener) {
  
    JButton button = new JButton(title);
    button.setForeground(WizardSettings.BUTTON_TEXT_COLOR);
    button.setFont(WizardSettings.BUTTON_FONT);
    if (actionListener!=null) button.addActionListener(actionListener);
    bottomPanel.add(button);
    bottomPanel.add(Box.createHorizontalStrut(PADDING));
    return button;
  }


  // * * *  V A R I A B L E S  * * * * * * * * * * * * * * * * * * * * * * * * * *

  private int PADDING = WizardSettings.PADDING;
  private Container contentPane;
  private JPanel topPanel;
  private JPanel middlePanel;
  private JPanel bottomPanel;
  private JLabel titleLabel, subtitleLabel;
  private JButton nextFinishButton;
  private JButton prevButton;
  private JButton cancelButton;
  private AbstractWizardPage currentPage;
  private Stack pageStack;
  private WizardPageLibrary pageLib;
}
