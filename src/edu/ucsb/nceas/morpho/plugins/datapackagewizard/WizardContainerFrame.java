/**
 *  '$RCSfile: WizardContainerFrame.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2003-11-26 17:54:19 $'
 * '$Revision: 1.20 $'
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
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.ImportWizard;
import edu.ucsb.nceas.morpho.framework.TextImportListener;

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

import java.util.List;
import java.util.Vector;
import java.util.Stack;
import java.util.Iterator;
import java.util.Set;

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
  public WizardContainerFrame() {

    super();
    frame = this;
    this.listener = listener;
    pageStack   = new Stack();
    pageLib = new WizardPageLibrary();
    init();
    setCurrentPage(WizardSettings.FIRST_PAGE_ID);
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

    initPageLibrary();
    initContentPane();
    initTopPanel();
    initMiddlePanel();
    initBottomPanel();
    initButtons();
  }

  private void initPageLibrary() {


    ImportWizard importPage
        = (ImportWizard)(pageLib.getPage(DataPackageWizardInterface.TEXT_IMPORT_WIZARD));

    importPage.setTextImportListener(

      new TextImportListener() {

        /** TextImportListener interface
         * This method is called when editing is complete
         *
         * @param xmlString is the edited XML in String format
         */
        public void importComplete(OrderedMap om) {

          Log.debug(45, "WizardContainerFrame.importComplete() called");
          nextFinishAction();
        }

        /** TextImportListener interface
         * this method handles canceled editing
         */
        public void importCanceled() {

          Log.debug(45, "WizardContainerFrame.importCanceled() called");
          previousAction();
        }
      });
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

    bottomBorderPanel = new JPanel();
    bottomBorderPanel.setLayout(new BorderLayout());

    bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
    bottomPanel.add(Box.createHorizontalGlue());
    bottomPanel.setOpaque(false);

    bottomBorderPanel.setBorder(
                  BorderFactory.createMatteBorder(2, 0, 0, 0, WizardSettings.TOP_PANEL_BG_COLOR));
//    bottomPanel.setBorder(new EmptyBorder(PADDING,3*PADDING,3*PADDING,PADDING));

    stepLabel = new JLabel();
    stepLabel.setBorder(BorderFactory.createEmptyBorder(3,10,3,3));
    stepLabel.setText("Step " + stepNumber + " of " + WizardSettings.NUMBER_OF_STEPS );

    bottomBorderPanel.add(stepLabel, BorderLayout.WEST);
    bottomBorderPanel.add(bottomPanel, BorderLayout.CENTER);

    contentPane.add(bottomBorderPanel, BorderLayout.SOUTH);
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

      stepNumber++;
      stepLabel.setText("Step " + stepNumber + " of " + WizardSettings.NUMBER_OF_STEPS );

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

    //results Map:
    OrderedMap wizData = new OrderedMap();

    //NOTE: the order of pages on the stack is *not* the same as the order
    //of writing data to the DOM. We therefore convert the Stack to a Vector and
    //access the pages non-sequentially in a feat of hard-coded madness:
    //
    List pagesList = (Vector)pageStack;

    int GENERAL
        = pagesList.indexOf(pageLib.getPage(DataPackageWizardInterface.GENERAL));
    int PROJECT
        = pagesList.indexOf(pageLib.getPage(DataPackageWizardInterface.PROJECT));
    int KEYWORDS
        = pagesList.indexOf(pageLib.getPage(DataPackageWizardInterface.KEYWORDS));
    int PARTY_CREATOR
        = pagesList.indexOf(pageLib.getPage(DataPackageWizardInterface.PARTY_CREATOR));
    int PARTY_CONTACT
        = pagesList.indexOf(pageLib.getPage(DataPackageWizardInterface.PARTY_CONTACT));
    int PARTY_ASSOCIATED
        = pagesList.indexOf(pageLib.getPage(DataPackageWizardInterface.PARTY_ASSOCIATED));
    int USAGE_RIGHTS
        = pagesList.indexOf(pageLib.getPage(DataPackageWizardInterface.USAGE_RIGHTS));
    int DATA_LOCATION
        = pagesList.indexOf(pageLib.getPage(DataPackageWizardInterface.DATA_LOCATION));
    int TEXT_IMPORT_WIZARD
        = pagesList.indexOf(pageLib.getPage(DataPackageWizardInterface.TEXT_IMPORT_WIZARD));
    int DATA_FORMAT
        = pagesList.indexOf(pageLib.getPage(DataPackageWizardInterface.DATA_FORMAT));
    int ENTITY
        = pagesList.indexOf(pageLib.getPage(DataPackageWizardInterface.ENTITY));

//TITLE:
    OrderedMap generalMap = null;
    if (GENERAL>=0)             {

      generalMap = ((WizardPage)(pagesList.get(GENERAL))).getPageData();
      final String titleXPath = "/eml:eml/dataset/title[1]";
      Object titleObj = generalMap.get(titleXPath);
      if (titleObj!=null) wizData.put(titleXPath,
                                      XMLUtilities.normalize(titleObj));
    }
//PROJECT

//CREATOR:
    if (PARTY_CREATOR>=0)       {
      addPageDataToResultsMap((WizardPage)(pagesList.get(PARTY_CREATOR)),wizData);
    }

//ASSOCIATED PARTY:
    if (PARTY_ASSOCIATED>=0)    {
      addPageDataToResultsMap((WizardPage)(pagesList.get(PARTY_ASSOCIATED)),wizData);
    }

//ABSTRACT:
    if (generalMap!=null)       {

      final String abstractXPath = "/eml:eml/dataset/abstract/section/para[1]";
      Object abstractObj = generalMap.get(abstractXPath);
      if (abstractObj!=null) wizData.put( abstractXPath,
                                          XMLUtilities.normalize(abstractObj));
    }

//KEYWORDS:
    if (KEYWORDS>=0)            {
      addPageDataToResultsMap((WizardPage)(pagesList.get(KEYWORDS)),wizData);
    }

//INTELLECTUAL RIGHTS:
    if (USAGE_RIGHTS>=0)        {
      addPageDataToResultsMap((WizardPage)(pagesList.get(USAGE_RIGHTS)),wizData);
    }

//CONTACT:
    if (PARTY_CONTACT>=0)       {
      addPageDataToResultsMap((WizardPage)(pagesList.get(PARTY_CONTACT)),wizData);
    }

    if (TEXT_IMPORT_WIZARD>=0)  {
      addPageDataToResultsMap((WizardPage)(pagesList.get(TEXT_IMPORT_WIZARD)),wizData);
    }

    if (ENTITY>=0)              {
      addPageDataToResultsMap((WizardPage)(pagesList.get(ENTITY)),wizData);
    }

    if (DATA_FORMAT>=0)         {
      addPageDataToResultsMap((WizardPage)(pagesList.get(DATA_FORMAT)),wizData);
    }

    if (DATA_LOCATION>=0)       {
      addPageDataToResultsMap((WizardPage)(pagesList.get(DATA_LOCATION)),wizData);
    }

    addIDs(
      new String[]  {
                      "/eml:eml/dataset/dataTable",
                      "/eml:eml/dataset/dataTable/attributeList/attribute"
                    }, wizData);


    Log.debug(45, "\n\n********** Wizard finished: NVPs:");
    Log.debug(45, wizData.toString());


    Node rootNode = null;

    //create a new empty DOM document to be populated by the wizard values:
    try {

      rootNode = XMLUtilities.getXMLReaderAsDOMTreeRootNode(
                    new StringReader(WizardSettings.NEW_EML200_DOCUMENT_TEXT));
    } catch (Exception e) {
      e.printStackTrace();
      Log.debug(5, "unexpected error trying to create new XML document "
                    +"at start of wizard\n");
      listener.wizardCanceled();
      return;
    }

    //now populate it...
    try {

      XMLUtilities.getXPathMapAsDOMTree(wizData, rootNode);

    } catch (Exception e) {

      e.printStackTrace();
      Log.debug(5, "unexpected error trying to create new XML document "
                    +"after wizard finished\n");
      listener.wizardCanceled();
      return;
    }

    listener.wizardComplete(rootNode);
  }


  private final String ID_ATTR_XPATH  = "/@id";
  private final StringBuffer tempBuff = new StringBuffer();
  private final OrderedMap idMap      = new OrderedMap();
  /**
   *  adds unique IDs to the elements identified by the *absolute* XPath strings
   *  in the elementsThatNeedIDsArray
   *  NOTE - if the xpath points to more than one element, then a unique ID
   *  will be assigned to each element
   */
  private void addIDs(String[] elementsNeedingIDsArray, OrderedMap resultsMap) {

    idMap.clear();
    Set keyset = resultsMap.keySet();
    //
    for (int i=0; i<elementsNeedingIDsArray.length; i++) {

      String nextXPath = elementsNeedingIDsArray[i];
      //elementsNeedingIDsArray[i];
      //check if resultsMap keys contain the exact xpath with no predicate
      if (keyset.contains(nextXPath)) {

        //if so, add @id to this xpath and append to idMap...
        idMap.put(nextXPath + ID_ATTR_XPATH, WizardSettings.getUniqueID());
      }


      tempBuff.delete(0,tempBuff.length());
      tempBuff.append(nextXPath);
      tempBuff.append("/");

      //check if resultsMap keys contain the substring xpath with no predicate
      if (mapKeysContainSubstring(keyset, tempBuff.toString())) {

        //if so, add @id to this xpath and append to results...
        idMap.put(nextXPath + ID_ATTR_XPATH, WizardSettings.getUniqueID());
      }

      nextXPath += "[";

      int idx = 1;

      tempBuff.delete(0,tempBuff.length());
      tempBuff.append(nextXPath);
      tempBuff.append(idx++);
      tempBuff.append("]");

      //loop while resultsMap keys contain the substring (xpath[+i+])
      while (mapKeysContainSubstring(keyset, tempBuff.toString())) {

        //add @id to xpath[+i+] and append to results
        tempBuff.append(ID_ATTR_XPATH);
        idMap.put(tempBuff.toString(), WizardSettings.getUniqueID());

        tempBuff.delete(0,tempBuff.length());
        tempBuff.append(nextXPath);
        tempBuff.append(idx++);
        tempBuff.append("]");
      }
    }
    resultsMap.putAll(idMap);
  }

  private boolean mapKeysContainSubstring(Set keyset, String xpath) {

    if (keyset==null || xpath==null || xpath.trim().equals("")) return false;

    Iterator  it = keyset.iterator();

    if (it==null) return false;

    String nextKey = null;

    while (it.hasNext()) {

      nextKey = (String)it.next();

      if (nextKey==null) continue;
      if (nextKey.indexOf(xpath)<0) continue;
      else return true;
    }
    return false;
  }

  /**
   *  given a WizardPage object (nextPage), calls its getPageData() method to
   *  get the NVPs from thae page, and adds these NVPs to the OrderedMap
   *  provided (resultMap)
   */
  private void addPageDataToResultsMap( WizardPage nextPage,
                                        OrderedMap resultsMap) {

    String nextKey = null;

    OrderedMap nextPgData = nextPage.getPageData();

    if (nextPgData==null) return;

    Iterator  it = nextPgData.keySet().iterator();

    if (it==null) return;

    while (it.hasNext()) {

      nextKey = (String)it.next();

      if (nextKey==null || nextKey.trim().equals("")) continue;

      //now excape all characters that might cause a problem in XML:
      resultsMap.put(nextKey, XMLUtilities.normalize(nextPgData.get(nextKey)));

    } // end while
  }



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

    stepNumber--;
    stepLabel.setText("Step " + stepNumber + " of " + WizardSettings.NUMBER_OF_STEPS );

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

    JButton button = WidgetFactory.makeJButton(title, actionListener);
    bottomPanel.add(button);
    bottomPanel.add(Box.createHorizontalStrut(PADDING));
    return button;
  }


  // * * *  V A R I A B L E S  * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JLabel stepLabel;
  private int stepNumber = 1;
  private int PADDING = WizardSettings.PADDING;
  private Container contentPane;
  private JPanel topPanel;
  private JPanel middlePanel;
  private JPanel bottomBorderPanel;
  private JPanel bottomPanel;
  private JLabel titleLabel, subtitleLabel;
  private JButton nextFinishButton;
  private JButton prevButton;
  private JButton cancelButton;
  private AbstractWizardPage currentPage;
  private Stack pageStack;
  private WizardPageLibrary pageLib;
}
