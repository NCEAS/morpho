/**
 *  '$RCSfile: CitationPage.java,v $'
 *    Purpose: A class that handles display of Citation Information
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-04-21 21:32:08 $'
 * '$Revision: 1.18 $'
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.w3c.dom.Node;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.DataPackageWizardPlugin;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

public class CitationPage extends AbstractUIPage {

  private final String pageID     = DataPackageWizardInterface.CITATION_PAGE;
  private final String nextPageID = "";
  private final String pageNumber = "";
  private final String title      = "Citation Page";
  private final String subtitle   = "";

  private String TAXON_CITATION_CREATOR_ROOTXPATH = "creator";
  private String TAXON_CITATION_CREATOR_GENERIC_NAME = "taxon_citation_creator";

  private JLabel titleLabel;
  private JTextField titleField;

  private JLabel authorLabel;
  private CustomList authorList;

  private final String[] authorListNames = {"Party", "Role", "Address"};
  private final Object[] editors = null; //makes non-directly-editable

  private JLabel pubDateLabel;
  private JTextField pubDateField;

  // to be visible in setData() function call
  private JPanel radioPanel;
  private JLabel citationTypeLabel;

  private JPanel bookPanel;
  private JPanel articlePanel;
  private JPanel reportPanel;
  private JPanel currentPanel;

  private JPanel middlePanel;
  private JPanel topMiddlePanel;

  private String xPathRoot = "";

  private final String[] typeElemNames = new String[3];

  // these must correspond to indices of measScaleElemNames array

  private static String citationType = "";
  public static final int CITATIONTYPE_BOOK  = 0;
  public static final int CITATIONTYPE_ARTICLE  = 1;
  public static final int CITATIONTYPE_REPORT = 2;

  // number of author names that are shown in the parent page. Only this many names are
  // retrieved in the getSurrogate method
  private static final int MAX_AUTHOR_NAMES_SHOWN = 2;

  private static final int BORDERED_PANEL_TOT_ROWS = 5;
  private final int PADDING = WizardSettings.PADDING;

  public CitationPage() {

    initNames();
    init();
  }

  private void initNames() {

    typeElemNames[CITATIONTYPE_BOOK]  = "Book";
    typeElemNames[CITATIONTYPE_ARTICLE]  = "Article";
    typeElemNames[CITATIONTYPE_REPORT]  = "Report";

  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    middlePanel = new JPanel();
    topMiddlePanel = new JPanel();

    this.setLayout( new BorderLayout());
    this.add(middlePanel,BorderLayout.CENTER);
    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
    middlePanel.setBorder(BorderFactory.createEmptyBorder(0, 2 * PADDING, PADDING, 3 * PADDING));
    topMiddlePanel.setLayout(new BoxLayout(topMiddlePanel, BoxLayout.Y_AXIS));
    topMiddlePanel.add(WidgetFactory.makeHTMLLabel(
              "<font size=\"4\"><b>Define the Citation Details:</b></font>", 1));

    topMiddlePanel.add(WidgetFactory.makeDefaultSpacer());


    /////////////////////////////////////////////


    // Title
    JPanel titlePanel = WidgetFactory.makePanel(1);
    titleLabel = WidgetFactory.makeLabel("Title:", true);
    titlePanel.add(titleLabel);
    titleField = WidgetFactory.makeOneLineTextField();
    titlePanel.add(titleField);
    titlePanel.setBorder(new EmptyBorder(0,0,0, WizardSettings.PADDING));
    topMiddlePanel.add(titlePanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());

    // Author custom list
    JPanel authorPanel = WidgetFactory.makePanel(-1);
    authorLabel = WidgetFactory.makeLabel("Author(s):", true);
    authorPanel.add(authorLabel);

    authorList = WidgetFactory.makeList(authorListNames, editors, -1,
                                         true, true, false, true, true, true);

//    authorList.setListButtonDimensions(WizardSettings.LIST_BUTTON_DIMS_SMALL);
    authorList.setCustomAddAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        updateDOMFromListOfPages();
        showNewAuthorPartyDialog();
      }
    });

    authorList.setCustomEditAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        updateDOMFromListOfPages();
        showEditAuthorPartyDialog();
      }
    });

    authorPanel.add(authorList);
    authorPanel.setMaximumSize(new Dimension(2000, 165));
    authorPanel.setPreferredSize(new Dimension(2000, 165));
    topMiddlePanel.add(authorPanel);
//    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());

    // Pub Date
    JPanel pubDatePanel = WidgetFactory.makePanel(1);
    pubDateLabel = WidgetFactory.makeLabel("Pubication Date:", false);
    pubDatePanel.add(pubDateLabel);
    pubDateField = WidgetFactory.makeOneLineTextField();
    pubDatePanel.add(pubDateField);
    pubDatePanel.setBorder(new EmptyBorder(0,0,0, WizardSettings.PADDING));
    //salutationPanel.setBorder(new javax.swing.border.EmptyBorder(0,
       // 12 * WizardSettings.PADDING,
        //0, 8 * WizardSettings.PADDING));
    topMiddlePanel.add(pubDatePanel);
    JPanel dataHelpPanel = WidgetFactory.makePanel(1);
    JLabel spacer = WidgetFactory.makeLabel("", false);
    JLabel datehelp = WidgetFactory.makeHTMLLabel("Use the YYYY-MM-DD format - (e.g. 1989-02-24)", 1);
    dataHelpPanel.add(spacer);
    dataHelpPanel.add(datehelp);
    topMiddlePanel.add(dataHelpPanel);

    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());

    ////////////////////////////////////////////

    ActionListener listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "got radiobutton command: "+e.getActionCommand());

        //undo any hilites:

        if (e.getActionCommand().equals(typeElemNames[0])) {

          setCitationTypeUI(bookPanel);
          setCitationType(typeElemNames[0]);

        } else if (e.getActionCommand().equals(typeElemNames[1])) {

          setCitationTypeUI(articlePanel);
          setCitationType(typeElemNames[1]);


        } else if (e.getActionCommand().equals(typeElemNames[2])) {

          setCitationTypeUI(reportPanel);
          setCitationType(typeElemNames[2]);

        }
      }
    };

    citationTypeLabel = WidgetFactory.makeLabel("Category:", true,
                                WizardSettings.WIZARD_CONTENT_LABEL_DIMS);

    radioPanel = WidgetFactory.makeRadioPanel(typeElemNames, -1, listener);
    JPanel outerRadioPanel = new JPanel();
    outerRadioPanel.setLayout(new BoxLayout(outerRadioPanel, BoxLayout.X_AXIS));
    outerRadioPanel.add(citationTypeLabel);
    outerRadioPanel.add(radioPanel);

    topMiddlePanel.add(outerRadioPanel);
    topMiddlePanel.setMaximumSize(topMiddlePanel.getPreferredSize());
    topMiddlePanel.setPreferredSize(topMiddlePanel.getPreferredSize());
    topMiddlePanel.setMinimumSize(topMiddlePanel.getPreferredSize());

    /////////////////////////////////////////////////////

    middlePanel.add(topMiddlePanel);

    currentPanel  = getEmptyPanel();

    middlePanel.add(currentPanel);

    middlePanel.add(Box.createGlue());

    bookPanel  = getBookPanel();
    articlePanel  = getArticlePanel();
    reportPanel = getReportPanel();

    refreshUI();
  }

  private void showNewAuthorPartyDialog() {

    PartyPage partyPage = (PartyPage) WizardPageLibrary.getPage (DataPackageWizardInterface.PARTY_CITATION_AUTHOR);

    ModalDialog wpd = new ModalDialog(partyPage,
                                      WizardContainerFrame.getDialogParent(),
                                      UISettings.POPUPDIALOG_WIDTH - UISettings.DIALOG_SMALLER_THAN_WIZARD_BY,
                                      UISettings.POPUPDIALOG_HEIGHT - UISettings.DIALOG_SMALLER_THAN_WIZARD_BY);

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {

      List newRow = partyPage.getSurrogate();
      newRow.add(partyPage);
      authorList.addRow(newRow);

      if (partyPage.editingOriginalRef) {
        //have been editing an original reference via another party's dialog, so
        //if the original ref is in this current page's list, update its
        //PartyPage object before we write it to DOM...
        updateOriginalRefPartyPage(partyPage);
      }
     //update datapackage...
      updateDOMFromListOfPages();
    }
    WidgetFactory.unhiliteComponent(authorLabel);
  }

  /**
  * A method to edit exsisting Party Page dialog
  */

  private void showEditAuthorPartyDialog() {

    List selRowList = authorList.getSelectedRowList();

    if (selRowList == null || selRowList.size() < 4) return;

    Object dialogObj = selRowList.get(3);

    if (dialogObj == null || ! (dialogObj instanceof PartyPage)) return;

    PartyPage editPartyPage = (PartyPage) dialogObj;

    ModalDialog wpd = new ModalDialog(editPartyPage,
                            WizardContainerFrame.getDialogParent(),
                            UISettings.POPUPDIALOG_WIDTH,
                            UISettings.POPUPDIALOG_HEIGHT, false);
    wpd.resetBounds();
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {
      List newRow = editPartyPage.getSurrogate();
      newRow.add(editPartyPage);
      authorList.replaceSelectedRow(newRow);

      if (editPartyPage.editingOriginalRef) {

        //have been editing an original reference via another party's dialog, so
        //if the original ref is in this current page's list, update its
        //PartyPage object before we write it to DOM...
        updateOriginalRefPartyPage(editPartyPage);
      }
      //update datapackage...
      updateDOMFromListOfPages();
    }
  }



  //have been editing an original reference via another party's dialog, so
  //if the original ref is in this current page's list, update its
  //PartyPage object before we write it to DOM...
  private void updateOriginalRefPartyPage(PartyPage partyPage) {
    String originalRefID = partyPage.getReferencesNodeIDString();
    AbstractDataPackage adp
        = UIController.getInstance().getCurrentAbstractDataPackage();
    if (adp == null) {
      Log.debug(15, "\npackage from UIController is null");
      Log.debug(5, "ERROR: cannot update!");
      return;
    }

    List nextRowList = null;
    PartyPage nextPage = null;

    for (Iterator it = authorList.getListOfRowLists().iterator(); it.hasNext(); ) {

      nextRowList = (List)it.next();
      //column 3 is user object - check it exists and isn't null:
      if (nextRowList.size() < 4)continue;
      nextPage = (PartyPage)nextRowList.get(3);
      if (nextPage == null)continue;
      if (nextPage.getRefID().equals(originalRefID)) {

        Node root = adp.getSubtreeAtReference(originalRefID);

        OrderedMap map = XMLUtilities.getDOMTreeAsXPathMap(root);
        Log.debug(45,
                  "updateOriginalRefPartyPage() got a match with ID: "
                  + originalRefID+"; map = "+map);

        if (map == null || map.isEmpty())return;

        boolean checkParty = nextPage.setPageData(
            map, "/" + TAXON_CITATION_CREATOR_ROOTXPATH);
      }
    }
  }

  private void updateDOMFromListOfPages() {

    //update datapackage...
    List nextRowList = null;
    List pagesList = new ArrayList();
    AbstractUIPage nextPage = null;

    for (Iterator it = authorList.getListOfRowLists().iterator(); it.hasNext(); ) {

      nextRowList = (List)it.next();
      //column 3 is user object - check it exists and isn't null:
      if (nextRowList.size() < 4)continue;
      nextPage = (AbstractUIPage)nextRowList.get(3);
      if (nextPage == null)continue;
      pagesList.add(nextPage);
    }
    DataPackageWizardPlugin.deleteExistingAndAddPageDataToDOM(
        UIController.getInstance().getCurrentAbstractDataPackage(),
        pagesList, TAXON_CITATION_CREATOR_ROOTXPATH,
        TAXON_CITATION_CREATOR_GENERIC_NAME);


        updateListFromDOM();
  }

  private void updateListFromDOM() {

    AbstractDataPackage adp
    = UIController.getInstance().getCurrentAbstractDataPackage();
    if (adp == null) {
      Log.debug(15, "\npackage from UIController is null");
      Log.debug(5, "ERROR: cannot update!");
      return;
    }

    List personnelList = adp.getSubtrees(TAXON_CITATION_CREATOR_GENERIC_NAME);
    Log.debug(45, "updateListFromDOM - personnelList.size() = "
    + personnelList.size());

    List personnelOrderedMapList = new ArrayList();

    for (Iterator it = personnelList.iterator(); it.hasNext(); ) {

      personnelOrderedMapList.add(
      XMLUtilities.getDOMTreeAsXPathMap((Node)it.next()));
    }

    populatePartiesList(personnelOrderedMapList,
    "/"+TAXON_CITATION_CREATOR_ROOTXPATH + "[");
  }


  //personnelXPathRoot looks like:
  //      /contact[
  private boolean populatePartiesList(List personnelOrderedMapList,
  String personnelXPathRoot) {

    Iterator persIt = personnelOrderedMapList.iterator();
    OrderedMap nextPersonnelMap = null;
    int partyPredicate = 1;

    authorList.removeAllRows();
    boolean partyRetVal = true;

    while (persIt.hasNext()) {

      nextPersonnelMap = (OrderedMap)persIt.next();
      if (nextPersonnelMap == null || nextPersonnelMap.isEmpty()) continue;

      PartyPage nextParty = (PartyPage)WizardPageLibrary.getPage(
                 DataPackageWizardInterface.PARTY_CITATION_AUTHOR);

      boolean checkParty = nextParty.setPageData(nextPersonnelMap,
      personnelXPathRoot + (partyPredicate++) + "]");

      if (!checkParty)partyRetVal = false;
      List newRow = nextParty.getSurrogate();
      newRow.add(nextParty);

      authorList.addRow(newRow);
    }
    return partyRetVal;
  }

  private void setCitationType(String type) {

    this.citationType = type;
  }




  private void setCitationTypeUI(JPanel panel) {

    middlePanel.remove(currentPanel);
    //middlePanel.remove(topMiddlePanel);

    currentPanel = panel;
    //middlePanel.add(topMiddlePanel);
    middlePanel.add(currentPanel);
    topMiddlePanel.setMaximumSize(topMiddlePanel.getPreferredSize());
    topMiddlePanel.setMinimumSize(topMiddlePanel.getPreferredSize());

    ((WizardPageSubPanelAPI)currentPanel).onLoadAction();

    currentPanel.invalidate();
    currentPanel.validate();
    currentPanel.repaint();
    topMiddlePanel.validate();
    topMiddlePanel.repaint();
    middlePanel.validate();
    middlePanel.repaint();
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JPanel getEmptyPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(BORDERED_PANEL_TOT_ROWS);

    panel.add(WidgetFactory.makeDefaultSpacer());

    return panel;
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  private BookPanel getBookPanel() {

    BookPanel panel = new BookPanel(this);
    WidgetFactory.addTitledBorder(panel, typeElemNames[0]);
    return panel;
  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private ArticlePanel getArticlePanel() {

    ArticlePanel panel = new ArticlePanel(this);
    WidgetFactory.addTitledBorder(panel, typeElemNames[1]);
    return panel;
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  private ReportPanel getReportPanel() {

    ReportPanel panel = new ReportPanel(this);
    WidgetFactory.addTitledBorder(panel, typeElemNames[2]);
    return panel;
  }

  private JLabel getLabel(String text) {

    if (text==null) text="";
    JLabel label = new JLabel(text);

    label.setAlignmentX(1.0f);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    label.setBorder(BorderFactory.createMatteBorder(1,10,1,3, (Color)null));

    return label;
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  /**
   *  calls validate() and repaint() on the middle panel
   */
  public void refreshUI() {

    currentPanel.validate();
    currentPanel.repaint();
    middlePanel.validate();
    middlePanel.repaint();
  }

  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *  Here, it does nothing because this is just a Panel and not the outer container
   */

  public void onRewindAction() {
  }

  /**
   *  The action to be executed when the page is loaded
   *  Here, it reloads the parties from the DOM, in case any of them are
   *  references that have been edited elsewhere
   */
  public void onLoadAction() {
    updateListFromDOM();
  }

  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return this.pageID;}

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
  public String getNextPageID() { return this.nextPageID; }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    if (titleField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(titleLabel);
      titleField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(titleLabel);

    if (authorList.getRowCount() == 0) {

      WidgetFactory.hiliteComponent(authorLabel);
      return false;
    }
    WidgetFactory.unhiliteComponent(authorLabel);

// COMMENTED BY MB - DATE IS NOT REQD?!
//    String date = this.pubDateField.getText();
// //		if(!date.trim().equals("") && !isDate(date)) {
//    if(!date.trim().equals("")) {
//      WidgetFactory.hiliteComponent(pubDateLabel);
//
//      return false;
//    }

    if (citationType==null || citationType.trim().equals("")) {

      WidgetFactory.hiliteComponent(citationTypeLabel);
      return false;
    }
    WidgetFactory.unhiliteComponent(citationTypeLabel);

    return ((WizardPageSubPanelAPI)currentPanel).validateUserInput();
  }

//  private boolean isDate(String s) {
//    DateFormat dateFormat;
//    Date dt;
//    dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
//    boolean res = true;
//    try {
//      dt = dateFormat.parse(s);
//    } catch (Exception w) {
//      try {
//        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
//        dt = dateFormat.parse(s);
//      } catch (Exception w1) {
//        try {
//          dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
//          dt = dateFormat.parse(s);
//        } catch (Exception w2) {
//          res = false;
//        }
//      }
//    }
//    return res;
//  }

  /**
   *  @return a List contaiing 2 String elements - one for each column of the
   *  2-col list in which this surrogate is displayed
   *
   */
  public List getSurrogate() {

    List surrogate = new ArrayList();
    surrogate.add(this.titleField.getText());

    Iterator it = authorList.getListOfRowLists().iterator();
    String creator = "";
    int cnt = 0;
    while(it.hasNext()) {
      if(cnt == MAX_AUTHOR_NAMES_SHOWN) {
        // show only MAX_AUTHOR_NAMES_SHOWN author names in the creator column in prev page
        creator += " ...";
        break;
      }
      List row = (List)it.next();
      String party = (String)row.get(0);
      int idx = party.indexOf(",");
      if(idx >=0) party = party.substring(0, idx);
      creator += party;
      if(cnt < (MAX_AUTHOR_NAMES_SHOWN - 1) && it.hasNext()) creator += ", ";
      cnt++;
    }

    surrogate.add(creator);
    surrogate.add(this.citationType);
    return surrogate;
  }


  /**
   *  gets the Map object that contains all the key/value paired
   *
   *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
   *            appended when making name/value pairs.  For example, in the
   *            following xpath:
   *
   *            /eml:eml/dataset/dataTable/attributeList/attribute[2]
   *            /measurementScale/nominal/nonNumericDomain/textDomain/definition
   *
   *            the root would be:
   *
   *              /eml:eml/dataset/dataTable/attributeList
   *                                /attribute[2]
   *
   *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
   *            SQUARE BRACKETS []
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap   returnMap;
  //////////////////
  public OrderedMap getPageData() {

    return this.getPageData(xPathRoot);
  }
  public OrderedMap getPageData(String xPath) {

    OrderedMap map = new OrderedMap();

    map.put(xPath + "/title[1]", this.titleField.getText());

    Iterator it = authorList.getListOfRowLists().iterator();
    int idx = 1;
    while(it.hasNext()) {

      List row = (List)it.next();
      if(row.size() < 4) continue;
      PartyPage party = (PartyPage) row.get(3);
      OrderedMap partyMap = party.getPageData(xPath + "/creator[" + idx + "]");
      map.putAll(partyMap);
      idx++;
    }
    String pubn = this.pubDateField.getText();
    if(!pubn.trim().equals(""))
      map.put(xPath + "/pubDate[1]", pubn);

    if(this.citationType.equals("Book")) {

      OrderedMap newMap = ((WizardPageSubPanelAPI)bookPanel).getPanelData(xPath + "/book[1]");
      map.putAll(newMap);

    } else if(citationType.equals("Article")) {

      OrderedMap newMap = ((WizardPageSubPanelAPI)articlePanel).getPanelData(xPath + "/article[1]");
      map.putAll(newMap);

    } else if(citationType.equals("Report")) {

      OrderedMap newMap = ((WizardPageSubPanelAPI)reportPanel).getPanelData(xPath + "/report[1]");
      map.putAll(newMap);
    }
    return map;

  }



  private String findCitationType(OrderedMap map, String xPath) {

    ///// check for Book

    Object o1 = map.get(xPath + "/book[1]/publisher[1]/organizationName[1]");
    if(o1 != null) return "Book";

    o1 = map.get(xPath + "/article[1]/journal[1]");
    if(o1 != null) return "Article";

    o1 = map.get(xPath + "/report[1]/publisher[1]/organizationName[1]");
    if(o1 != null) return "Report";

    return "";
  }

  private boolean mapContainsCreator(OrderedMap map, String xPath, int idx) {

    boolean b = map.containsKey(xPath + "/creator[" + idx + "]/references[1]");
    if(b) return true;
    b = map.containsKey(xPath + "/creator[" + idx + "]/references");
    if(b) return true;
    b = map.containsKey(xPath + "/creator[" +idx+ "]/individualName/surName[1]");
    if(b) return true;
    b = map.containsKey(xPath + "/creator[" +idx+ "]/individualName[1]/surName[1]");
    if(b) return true;
    b = map.containsKey(xPath + "/creator[" +idx+ "]/organizationName[1]");
    if(b) return true;
    b = map.containsKey(xPath + "/creator[" +idx+ "]/positionName[1]");
    if(b) return true;
    return false;

  }


  /**
   * sets the Data in the Attribute Dialog fields. This is called from the
   * TextImportWizard when it wants to set some information it has already
   * guessed from the given data file. Any data in the AttributeDialog can be
   * set through this method. The TextImportWizard however sets only the
   * "Attribute Name", "Measurement Scale", "Number Type" and the "Enumeration
   * Code Definitions"
   *
   * @param map - Data is passed as OrderedMap of xPath-value pairs. xPaths in
   *   this map are relative to the xPath provided
   * @param xPath - the relative xPath
   * @return boolean
   */
  public boolean setPageData(OrderedMap map, String xPath) {

    this.titleField.setText((String)map.get(xPath + "/title[1]"));
    map.remove(xPath + "/title[1]");

    for(int idx = 1; ; idx++) {

      if(!mapContainsCreator(map, xPath, idx)) {
        break;
      }
      OrderedMap copyMap = getNewCreatorMap(map, xPath, idx);
      PartyPage page = (PartyPage)WizardPageLibrary.getPage( DataPackageWizardInterface.PARTY_CITATION_AUTHOR);
      page.setPageData(copyMap, xPath + "/creator[" + idx + "]");
      List row = page.getSurrogate();
      row.add(page);
      authorList.addRow(row);
    }

    String pubn = (String)map.get(xPath + "/pubDate[1]");
    if(pubn != null) {
      this.pubDateField.setText(pubn);
      map.remove(xPath + "/pubDate[1]");
    }

    citationType = findCitationType(map, xPath);
    int componentNum = -1;

    if(this.citationType.equals("Book")) {

      componentNum = 0;
      this.setCitationType("Book");
      this.setCitationTypeUI(bookPanel);
      ((WizardPageSubPanelAPI)bookPanel).setPanelData(xPath + "/book[1]", map);

    } else if(citationType.equals("Article")) {


      componentNum = 1;
      this.setCitationType("Article");
      this.setCitationTypeUI(articlePanel);
      ((WizardPageSubPanelAPI)articlePanel).setPanelData(xPath + "/article[1]", map);

    } else if(citationType.equals("Report")) {


      componentNum = 2;
      this.setCitationType("Report");
      this.setCitationTypeUI(reportPanel);
      ((WizardPageSubPanelAPI)reportPanel).setPanelData(xPath + "/report[1]", map);
    }

    if (componentNum != -1) {

      Container c = (Container)(radioPanel.getComponent(1));
      JRadioButton jrb = (JRadioButton)c.getComponent(componentNum);
      jrb.setSelected(true);

    }

    refreshUI();
    return true;

   }

   private OrderedMap getNewCreatorMap(OrderedMap map, String xPath, int idx) {

     OrderedMap newMap = new OrderedMap();
     String searchString = xPath + "/creator[" + idx + "]";
     Iterator it = map.keySet().iterator();
     while(it.hasNext()) {

       String k = (String)it.next();
       if(k.startsWith(searchString)) {
         newMap.put(k, (String)map.get(k));
       }
     }
     return newMap;
   }
}


class BookPanel extends JPanel implements WizardPageSubPanelAPI{

  CitationPage parent;
  private JLabel editionLabel;
  private JLabel volumeLabel;
  private JLabel publisherLabel;
  private JLabel isbnLabel;

  private JTextField editionField;
  private JTextField volumeField;
  private JTextField publisherField;
  private JTextField isbnField;

  BookPanel(CitationPage page) {

    this.parent = page;
    init();
  }

  private void init() {

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // Publisher (Organization)
    JPanel publisherPanel = WidgetFactory.makePanel(1);
    publisherLabel = WidgetFactory.makeLabel("Publisher:", true);
    publisherPanel.add(publisherLabel);
    publisherField = WidgetFactory.makeOneLineTextField();
    publisherPanel.add(publisherField);
    publisherPanel.setBorder(new EmptyBorder(0,WizardSettings.PADDING,0,2*WizardSettings.PADDING));
    this.add(publisherPanel);
    this.add(WidgetFactory.makeHalfSpacer());
    this.add(Box.createGlue());

    // Edition Name
    JPanel editionPanel = WidgetFactory.makePanel(1);
    editionLabel = WidgetFactory.makeLabel("Edition:", false);
    editionPanel.add(editionLabel);
    editionField = WidgetFactory.makeOneLineTextField();
    editionPanel.add(editionField);
    editionPanel.setBorder(new EmptyBorder(0,WizardSettings.PADDING,0, 2*WizardSettings.PADDING));
    this.add(editionPanel);
    this.add(WidgetFactory.makeHalfSpacer());
    this.add(Box.createGlue());

    // Volume
    JPanel volumePanel = WidgetFactory.makePanel(1);
    volumeLabel = WidgetFactory.makeLabel("Volume:", false);
    volumePanel.add(volumeLabel);
    volumeField = WidgetFactory.makeOneLineTextField();
    volumePanel.add(volumeField);
    volumePanel.setBorder(new EmptyBorder(0,WizardSettings.PADDING,0, 2*WizardSettings.PADDING));
    this.add(volumePanel);
    this.add(WidgetFactory.makeHalfSpacer());
    this.add(Box.createGlue());

    // ISBN
    JPanel isbnPanel = WidgetFactory.makePanel(1);
    isbnLabel = WidgetFactory.makeLabel("ISBN:", false);
    isbnPanel.add(isbnLabel);
    isbnField = WidgetFactory.makeOneLineTextField();
    isbnPanel.add(isbnField);
    isbnPanel.setBorder(new EmptyBorder(0,WizardSettings.PADDING,0, 2*WizardSettings.PADDING));
    this.add(isbnPanel);
    this.add(WidgetFactory.makeHalfSpacer());
    this.add(Box.createGlue());

  }


  /**
   *  checks that the user has filled in required fields - if not, highlights
   *  labels to draw attention to them
   *
   *  @return   boolean true if user data validated OK. false if intervention
   *            required
   */

  public boolean validateUserInput() {

    String text = publisherField.getText();

    if(text.trim().equals("")) {
      WidgetFactory.hiliteComponent(publisherLabel);
      publisherField.requestFocus();
      return false;
    }

    WidgetFactory.unhiliteComponent(publisherLabel);
    return true;
  }


  /**
   *  The action to be executed when the panel is displayed. May be empty
   */
  public void onLoadAction() {}


  /**
   *  gets the Map object that contains all the key/value paired
   *
   *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
   *            appended when making name/value pairs.  For example, in the
   *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the
   *            root would be /eml:eml/dataset/keywordSet[2]
   *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
   *            SQUARE BRACKETS []
   *
   *  @return   data the OrderedMap object that contains all the
   *            key/value paired settings for this particular panel
   */
  public OrderedMap getPanelData(String xPathRoot) {

    OrderedMap map = new OrderedMap();

    String publisher = publisherField.getText().trim();
    map.put(xPathRoot + "/publisher[1]/organizationName[1]", publisher);

    String en = this.editionField.getText();
    if(!en.trim().equals("")) map.put(xPathRoot + "/edition[1]", en);

    String vn = this.volumeField.getText();
    if(!vn.trim().equals("")) map.put(xPathRoot + "/volume[1]", vn);

    String isbn = this.isbnField.getText();
    if(!isbn.trim().equals("")) map.put(xPathRoot + "/ISBN[1]", isbn);

    return map;
  }


  /**
  *	  sets the data in the sub panel using the key/values paired Map object
  *
  *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
  *            appended when making name/value pairs.  For example, in the
  *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the
  *            root would be /eml:eml/dataset/keywordSet[2]
  *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
  *            SQUARE BRACKETS []
  *  @param  map - OrderedMap of xPath-value pairs. xPaths in this map
  *		    		are absolute xPath and not the relative xPaths
  *
  **/

  public void setPanelData(String xPathRoot, OrderedMap map) {

    String pub = (String)map.get(xPathRoot + "/publisher[1]/organizationName[1]");
    if(pub != null) {
      this.publisherField.setText(pub);
      map.remove(xPathRoot + "/publisher[1]/organizationName[1]");
    }

    String en = (String)map.get(xPathRoot + "/edition[1]");
    if(en != null) {
      this.editionField.setText(en);
      map.remove(xPathRoot + "/edition[1]");
    }

    String vn = (String)map.get(xPathRoot + "/volume[1]");
    if(vn != null) {
      this.volumeField.setText(vn);
      map.remove(xPathRoot + "/volume[1]");
    }

    String isbn = (String)map.get(xPathRoot + "/ISBN[1]");
    if(isbn != null) {
      this.isbnField.setText(isbn);
      map.remove(xPathRoot + "/ISBN[1]");
    }

  }

}


class ArticlePanel extends JPanel  implements WizardPageSubPanelAPI{

  CitationPage parent;
  private JLabel journalLabel;
  private JLabel volumeLabel;
  private JLabel rangeLabel;
  private JLabel publisherLabel;
  private JLabel issueLabel;

  private JTextField journalField;
  private JTextField volumeField;
  private JTextField rangeField;
  private JTextField publisherField;
  private JTextField issueField;

  ArticlePanel (CitationPage page) {

    this.parent = page;
    init();
  }

  private void init() {

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // Journal
    JPanel journalPanel = WidgetFactory.makePanel(1);
    journalLabel = WidgetFactory.makeLabel("Journal:", true);
    journalPanel.add(journalLabel);
    journalField = WidgetFactory.makeOneLineTextField();
    journalPanel.add(journalField);
    journalPanel.setBorder(new EmptyBorder(0,WizardSettings.PADDING,0, 2*WizardSettings.PADDING));
    this.add(journalPanel);
    this.add(WidgetFactory.makeHalfSpacer());
    this.add(Box.createGlue());

    // Volume
    JPanel volumePanel = WidgetFactory.makePanel(1);
    volumeLabel = WidgetFactory.makeLabel("Volume:", true);
    volumePanel.add(volumeLabel);
    volumeField = WidgetFactory.makeOneLineTextField();
    volumePanel.add(volumeField);
    volumePanel.setBorder(new EmptyBorder(0,WizardSettings.PADDING,0, 2*WizardSettings.PADDING));
    this.add(Box.createGlue());
    this.add(volumePanel);
    this.add(WidgetFactory.makeHalfSpacer());
    this.add(Box.createGlue());

    // Issue
    JPanel issuePanel = WidgetFactory.makePanel(1);
    issueLabel = WidgetFactory.makeLabel("Issue:", false);
    issuePanel.add(issueLabel);
    issueField = WidgetFactory.makeOneLineTextField();
    issuePanel.add(issueField);
    issuePanel.setBorder(new EmptyBorder(0,WizardSettings.PADDING,0, 2*WizardSettings.PADDING));
    this.add(Box.createGlue());
    this.add(issuePanel);
    this.add(WidgetFactory.makeHalfSpacer());
    this.add(Box.createGlue());

    // Page Range
    JPanel rangePanel = WidgetFactory.makePanel(1);
    rangeLabel = WidgetFactory.makeLabel("Page Range:", true);
    rangePanel.add(rangeLabel);
    rangeField = WidgetFactory.makeOneLineTextField();
    rangePanel.add(rangeField);
    rangePanel.setBorder(new EmptyBorder(0,WizardSettings.PADDING,0, 2*WizardSettings.PADDING));
    this.add(Box.createGlue());
    this.add(rangePanel);
    this.add(WidgetFactory.makeHalfSpacer());
    this.add(Box.createGlue());

    // Publisher (Organization)
    JPanel publisherPanel = WidgetFactory.makePanel(1);
    publisherLabel = WidgetFactory.makeLabel("Publisher:", false);
    publisherPanel.add(publisherLabel);
    publisherField = WidgetFactory.makeOneLineTextField();
    publisherPanel.add(publisherField);
    publisherPanel.setBorder(new EmptyBorder(0,WizardSettings.PADDING,0, 2*WizardSettings.PADDING));
    this.add(publisherPanel);
    this.add(WidgetFactory.makeHalfSpacer());
    this.add(Box.createGlue());

  }


  /**
   *  checks that the user has filled in required fields - if not, highlights
   *  labels to draw attention to them
   *
   *  @return   boolean true if user data validated OK. false if intervention
   *            required
   */


  public boolean validateUserInput() {

    if (journalField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(journalLabel);
      journalField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(journalLabel);

    if (volumeField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(volumeLabel);
      volumeField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(volumeLabel);

    if (rangeField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(rangeLabel);
      rangeField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(rangeLabel);

    return true;
  }


  /**
   *  The action to be executed when the panel is displayed. May be empty
   */
  public void onLoadAction() {}


  /**
   *  gets the Map object that contains all the key/value paired
   *
   *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
   *            appended when making name/value pairs.  For example, in the
   *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the
   *            root would be /eml:eml/dataset/keywordSet[2]
   *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
   *            SQUARE BRACKETS []
   *
   *  @return   data the OrderedMap object that contains all the
   *            key/value paired settings for this particular panel
   */
  public OrderedMap getPanelData(String xPathRoot) {

    OrderedMap map = new OrderedMap();
    map.put(xPathRoot + "/journal[1]", journalField.getText());
    map.put(xPathRoot + "/volume[1]", volumeField.getText());
    String issue = (String) this.issueField.getText();
    if(!issue.trim().equals("")) map.put(xPathRoot + "/issue[1]", issue);

    map.put(xPathRoot + "/pageRange[1]", this.rangeField.getText());
    String pub = (String)this.publisherField.getText();
    if(!pub.trim().equals("")) map.put(xPathRoot + "/publisher[1]/organizationName[1]", pub);

    return map;
  }


  /**
  *	  sets the data in the sub panel using the key/values paired Map object
  *
  *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
  *            appended when making name/value pairs.  For example, in the
  *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the
  *            root would be /eml:eml/dataset/keywordSet[2]
  *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
  *            SQUARE BRACKETS []
  *  @param  map - OrderedMap of xPath-value pairs. xPaths in this map
  *		    		are absolute xPath and not the relative xPaths
  *
  **/

  public void setPanelData(String xPathRoot, OrderedMap map) {

    journalField.setText((String) map.get(xPathRoot + "/journal[1]"));
    volumeField.setText((String)map.get(xPathRoot + "/volume[1]"));
    String pr = (String)map.get(xPathRoot + "/pageRange[1]");
    this.rangeField.setText(pr);

    String issue = (String)map.get(xPathRoot + "/issue[1]");
    if(issue != null) {
      this.issueField.setText(issue);
      map.remove(xPathRoot + "/issue[1]");
    }

    String pub = (String)map.get(xPathRoot + "/publisher[1]/organizationName[1]");
    if(pub != null) {
      this.publisherField.setText(pub);
      map.remove(xPathRoot + "/publisher[1]/organizationName[1]");
    }

    return;
  }

}


class ReportPanel extends JPanel  implements WizardPageSubPanelAPI{

  CitationPage parent;
  private JLabel numberLabel;
  private JLabel pagesLabel;
  private JLabel publisherLabel;

  private JTextField numberField;
  private JTextField pagesField;
  private JTextField publisherField;


  ReportPanel(CitationPage page) {

    this.parent = page;
    init();
  }

  private void init() {

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // Publisher (Organization)
    JPanel publisherPanel = WidgetFactory.makePanel(1);
    publisherLabel = WidgetFactory.makeLabel("Publisher:", false);
    publisherPanel.add(publisherLabel);
    publisherField = WidgetFactory.makeOneLineTextField();
    publisherPanel.add(publisherField);
    publisherPanel.setBorder(new EmptyBorder(0,WizardSettings.PADDING,0, 2*WizardSettings.PADDING));
    this.add(Box.createGlue());
    this.add(publisherPanel);
    this.add(WidgetFactory.makeHalfSpacer());
    this.add(Box.createGlue());

    // Report Number
    JPanel numberPanel = WidgetFactory.makePanel(1);
    numberLabel = WidgetFactory.makeLabel("Report Number:", false);
    numberPanel.add(numberLabel);
    numberField = WidgetFactory.makeOneLineTextField();
    numberPanel.add(numberField);
    numberPanel.setBorder(new EmptyBorder(0,WizardSettings.PADDING,0, 2*WizardSettings.PADDING));
    this.add(numberPanel);
    this.add(WidgetFactory.makeHalfSpacer());
    this.add(Box.createGlue());

    // Report Pages
    JPanel pagesPanel = WidgetFactory.makePanel(1);
    pagesLabel = WidgetFactory.makeLabel("Number of Pages:", false);
    pagesPanel.add(pagesLabel);
    pagesField = WidgetFactory.makeOneLineTextField();
    pagesPanel.add(pagesField);
    pagesPanel.setBorder(new EmptyBorder(0,WizardSettings.PADDING,0, 2*WizardSettings.PADDING));
    this.add(pagesPanel);
    this.add(WidgetFactory.makeHalfSpacer());
    this.add(Box.createGlue());
  }


  /**
   *  checks that the user has filled in required fields - if not, highlights
   *  labels to draw attention to them
   *
   *  @return   boolean true if user data validated OK. false if intervention
   *            required
   */

  public boolean validateUserInput() {

    return true;
  }


  /**
   *  The action to be executed when the panel is displayed. May be empty
   */
  public void onLoadAction() {}


  /**
   *  gets the Map object that contains all the key/value paired
   *
   *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
   *            appended when making name/value pairs.  For example, in the
   *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the
   *            root would be /eml:eml/dataset/keywordSet[2]
   *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
   *            SQUARE BRACKETS []
   *
   *  @return   data the OrderedMap object that contains all the
   *            key/value paired settings for this particular panel
   */
  public OrderedMap getPanelData(String xPathRoot) {

    OrderedMap map = new OrderedMap();

    String rn = this.numberField.getText().trim();
    if(!rn.equals("")) map.put(xPathRoot + "/reportNumber[1]", rn);

    String pub = this.publisherField.getText().trim();
    if(!pub.equals("")) map.put(xPathRoot + "/publisher[1]/organizationName[1]", pub);

    String pn = this.pagesField.getText();
    if(!pn.trim().equals("")) map.put(xPathRoot + "/totalPages[1]", pn);

    return map;
  }

  /**
  *	  sets the data in the sub panel using the key/values paired Map object
  *
  *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
  *            appended when making name/value pairs.  For example, in the
  *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the
  *            root would be /eml:eml/dataset/keywordSet[2]
  *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
  *            SQUARE BRACKETS []
  *  @param  map - OrderedMap of xPath-value pairs. xPaths in this map
  *		    		are absolute xPath and not the relative xPaths
  *
  **/

  public void setPanelData(String xPathRoot, OrderedMap map) {

    String pub = (String)map.get(xPathRoot + "/publisher[1]/organizationName[1]");
    if(pub != null) {
      this.publisherField.setText(pub);
      map.remove(xPathRoot + "/publisher[1]/organizationName[1]");
    }

    String rn = (String)map.get(xPathRoot + "/reportNumber[1]");
    if(rn != null) {
      this.numberField.setText(rn);
      map.remove(xPathRoot + "/reportNumber[1]");
    }

    String pn = (String)map.get(xPathRoot + "/totalPages[1]");
    if(pn != null) {
      this.pagesField.setText(pn);
      map.remove(xPathRoot + "/totalPages[1]");
    }

  }

}


