/**
 *       Name: QueryDialog.java
 *    Purpose: Visual display for collecting query info from user
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-31 01:28:02 $'
 * '$Revision: 1.14 $'
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
package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.framework.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * Dialog which collects search information from user
 * to be used to create a Query
 * 
 */
public class QueryDialog extends JDialog
{
  /** A reference to the container framework */
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  /** Vector of textquery panels currently displayed*/
  Vector textPanels;

  /** Vector of taxonquery panels currently displayed*/
  Vector taxonPanels;

  /** flag to set whether searches are case sensitive */
  boolean caseSensitive = true;

  /** default search path for title   */
  String titleSearchPath = "title";

  /** default search path for abstract   */
  String abstractSearchPath = "abstract";

  /** default search path for keyword   */
  String keywordSearchPath = "keyword";

  /** Flag, true if Metacat searches are performed for this query */
  private boolean searchMetacat = true;

  /** Flag, true if network searches are performed for this query */
  private boolean searchLocal = true;

  /** Current query built using this QueryDialog */
  private Query savedQuery;

  /** flag indicating whether the execute button was pressed */
  private boolean searchStarted = false;

  /** A static document counter for new untitled documents */
  private static int untitledCounter = 0;

  //{{DECLARE_CONTROLS
  private JTabbedPane queryTabs = new JTabbedPane();
  private JPanel subjectPanel = new JPanel();
  private JPanel queryChoicesPanel = new JPanel();
  private JRadioButton andRadioButton = new JRadioButton();
  private JRadioButton orRadioButton = new JRadioButton();
  private JButton moreButton = new JButton();
  private JButton lessButton = new JButton();
  private JPanel taxonPanel = new JPanel();
  private JPanel taxonChoicesPanel = new JPanel();
  private JRadioButton taxonAndRadioButton = new JRadioButton();
  private JRadioButton taxonOrRadioButton = new JRadioButton();
  private JButton taxonMoreButton = new JButton();
  private JButton taxonLessButton = new JButton();
  private JPanel spatialPanel = new JPanel();
  private JTextField queryTitleTF = new JTextField(20);
  private JCheckBox otherTabsCheckBox = new JCheckBox();
  private JCheckBox catalogSearchCheckBox = new JCheckBox();
  private JCheckBox localSearchCheckBox = new JCheckBox();
  private JButton executeButton = new JButton();
  private JButton cancelButton = new JButton();
  //}}

  /**
   * Construct a new instance of the query dialog
   *
   * @param parent The parent frame for this dialog
   * @param framework A reference to the client framework 
   */
  public QueryDialog(Frame parent, ClientFramework framework)
  {
    super(parent);
    this.framework = framework;
    this.config = framework.getConfiguration();
    String temp = config.get("titleSearchPath", 0);
    if (temp != null) {
      titleSearchPath = temp;
    }
    temp = config.get("abstractSearchPath", 0);
    if (temp != null) {
      abstractSearchPath = temp;
    }
    temp = config.get("keywordSearchPath", 0);
    if (temp != null) {
        keywordSearchPath = temp;
    }
    String searchMetacatString = config.get("searchmetacat", 0);
    searchMetacat = (new Boolean(searchMetacatString)).booleanValue();
    String searchLocalString = config.get("searchlocal", 0);
    searchLocal = (new Boolean(searchLocalString)).booleanValue();

    untitledCounter++;

    // Instantiate the list of subject text panels and taxon panels
    textPanels = new Vector();
    taxonPanels = new Vector();

    // This code is automatically generated by Visual Cafe when you add
    // components to the visual environment. It instantiates and initializes
    // the components. To modify the code, only use code syntax that matches
    // what Visual Cafe can generate, or Visual Cafe may be unable to back
    // parse your Java file into its visual environment.
    //{{INIT_CONTROLS
    setTitle("Search");
    getContentPane().setLayout(new BorderLayout(0, 0));
    setSize(650, 375);
    setVisible(false);
    JPanel queryPanel = new JPanel();
    queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.Y_AXIS));
    getContentPane().add(BorderLayout.CENTER, queryPanel);
    queryPanel.add(Box.createVerticalStrut(8));

    // Configure the title area
    JPanel titlePanel = new JPanel();
    titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
    titlePanel.add(Box.createHorizontalStrut(8));
    JLabel queryTitleLabel = new JLabel();
    queryTitleLabel.setText("Query Title ");
    titlePanel.add(queryTitleLabel);
    titlePanel.add(Box.createHorizontalStrut(8));
    queryTitleTF.setText("Untitled-Search-" + untitledCounter);
    // Make the textfield a fixed height by putting it in a constrained panel
    JPanel queryTitleConstraintPanel = new JPanel() {
      public Dimension getMinimumSize() {
        return getPreferredSize();
      }
      public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 24);
      }
      public Dimension getMaximumSize() {
        return getPreferredSize();
      }
    };
    queryTitleConstraintPanel.add(queryTitleTF);
    titlePanel.add(queryTitleConstraintPanel);
    titlePanel.add(Box.createHorizontalStrut(8));
    titlePanel.add(Box.createHorizontalGlue());
    JPanel searchChoicePanel = new JPanel();
    searchChoicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    catalogSearchCheckBox.setText("Catalog Search");
    catalogSearchCheckBox.setActionCommand("Catalog Search");
    catalogSearchCheckBox.setSelected(searchMetacat);
    searchChoicePanel.add(catalogSearchCheckBox);
    localSearchCheckBox.setText("Local Search");
    localSearchCheckBox.setActionCommand("Local Search");
    localSearchCheckBox.setSelected(searchLocal);
    searchChoicePanel.add(localSearchCheckBox);
    titlePanel.add(searchChoicePanel);
    titlePanel.add(Box.createHorizontalStrut(8));
    queryPanel.add(titlePanel);
    queryPanel.add(Box.createVerticalStrut(8));

    queryPanel.add(queryTabs);

    // Configure the subject search panel
    subjectPanel.setLayout(new BorderLayout(0, 0));
    subjectPanel.setVisible(false);
    JScrollPane queryScrollPanel = new JScrollPane();
    queryScrollPanel.setOpaque(true);
    queryScrollPanel.setPreferredSize(new Dimension(
                     queryScrollPanel.getPreferredSize().width, 500 ));
    subjectPanel.add(BorderLayout.CENTER, queryScrollPanel);
    queryChoicesPanel.setLayout(new BoxLayout(queryChoicesPanel, 
                                              BoxLayout.Y_AXIS));
    queryChoicesPanel.setAlignmentX(0.0F);
    SubjectTermPanel tqt1 = new SubjectTermPanel();
    textPanels.addElement(tqt1);
    queryChoicesPanel.add(tqt1);
    queryScrollPanel.getViewport().add(queryChoicesPanel);
    JPanel subjectMoreLessPanel = new JPanel();
    subjectMoreLessPanel.setLayout(new BoxLayout(subjectMoreLessPanel, 
                                                  BoxLayout.X_AXIS));
    subjectMoreLessPanel.add(Box.createHorizontalStrut(8));
    andRadioButton.setText("And");
    andRadioButton.setActionCommand("And");
    subjectMoreLessPanel.add(andRadioButton);
    orRadioButton.setText("Or");
    orRadioButton.setActionCommand("Or");
    orRadioButton.setSelected(true);
    ButtonGroup subjectRadioGroup = new ButtonGroup();
    subjectRadioGroup.add(andRadioButton);
    subjectRadioGroup.add(orRadioButton);
    subjectMoreLessPanel.add(orRadioButton);
    subjectMoreLessPanel.add(Box.createHorizontalStrut(8));
    moreButton.setText("More");
    moreButton.setActionCommand("More");
    subjectMoreLessPanel.add(moreButton);
    lessButton.setText("Fewer");
    lessButton.setActionCommand("Fewer");
    lessButton.setEnabled(false);
    subjectMoreLessPanel.add(lessButton);
    subjectMoreLessPanel.add(Box.createHorizontalGlue());
    subjectMoreLessPanel.add(Box.createHorizontalStrut(8));
    subjectPanel.add(BorderLayout.SOUTH, subjectMoreLessPanel);
    queryTabs.add(subjectPanel);

    // Configure the taxonomic search panel
    taxonPanel.setLayout(new BorderLayout(0, 0));
    taxonPanel.setVisible(false);
    JScrollPane taxonScrollPanel = new JScrollPane();
    taxonScrollPanel.setOpaque(true);
    taxonScrollPanel.setPreferredSize(new Dimension(
                     taxonScrollPanel.getPreferredSize().width, 500 ));
    taxonPanel.add(BorderLayout.CENTER, taxonScrollPanel);
    taxonChoicesPanel.setLayout(new BoxLayout(taxonChoicesPanel, 
                                              BoxLayout.Y_AXIS));
    taxonChoicesPanel.setAlignmentX(0.0F);
    TaxonTermPanel taxonTerm = new TaxonTermPanel();
    taxonPanels.addElement(taxonTerm);
    taxonChoicesPanel.add(taxonTerm);
    taxonScrollPanel.getViewport().add(taxonChoicesPanel);
    JPanel taxonMoreLessPanel = new JPanel();
    taxonMoreLessPanel.setLayout(new BoxLayout(taxonMoreLessPanel, 
                                                  BoxLayout.X_AXIS));
    taxonMoreLessPanel.add(Box.createHorizontalStrut(8));
    taxonAndRadioButton.setText("And");
    taxonAndRadioButton.setActionCommand("And");
    taxonMoreLessPanel.add(taxonAndRadioButton);
    taxonOrRadioButton.setText("Or");
    taxonOrRadioButton.setActionCommand("Or");
    taxonOrRadioButton.setSelected(true);
    ButtonGroup taxonRadioGroup = new ButtonGroup();
    taxonRadioGroup.add(taxonAndRadioButton);
    taxonRadioGroup.add(taxonOrRadioButton);
    taxonMoreLessPanel.add(taxonOrRadioButton);
    taxonMoreLessPanel.add(Box.createHorizontalStrut(8));
    taxonMoreButton.setText("More");
    taxonMoreButton.setActionCommand("More");
    taxonMoreLessPanel.add(taxonMoreButton);
    taxonLessButton.setText("Fewer");
    taxonLessButton.setActionCommand("Fewer");
    taxonLessButton.setEnabled(false);
    taxonMoreLessPanel.add(taxonLessButton);
    taxonMoreLessPanel.add(Box.createHorizontalGlue());
    taxonMoreLessPanel.add(Box.createHorizontalStrut(8));
    taxonPanel.add(BorderLayout.SOUTH, taxonMoreLessPanel);
    queryTabs.add(taxonPanel);

    // Configure the spatial search panel
    spatialPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    spatialPanel.setVisible(false);
    queryTabs.add(spatialPanel);

    // Set the titles of the tabs
    queryTabs.setSelectedIndex(0);
    queryTabs.setSelectedComponent(subjectPanel);
    queryTabs.setTitleAt(0, "Subject");
    queryTabs.setTitleAt(1, "Taxonomic");
    queryTabs.setTitleAt(2, "Spatial");

    // Configure the control buttons area
    queryPanel.add(Box.createVerticalStrut(8));
    JPanel controlButtonsPanel = new JPanel();
    controlButtonsPanel.setLayout(new BoxLayout(controlButtonsPanel,
                                  BoxLayout.X_AXIS));
    controlButtonsPanel.add(Box.createHorizontalStrut(8));
    otherTabsCheckBox.setText("Combine constraints from all tabs");
    otherTabsCheckBox.setActionCommand("Combine constraints from all tabs");
    controlButtonsPanel.add(otherTabsCheckBox);
    controlButtonsPanel.add(Box.createHorizontalGlue());
    executeButton.setText("Search");
    executeButton.setActionCommand("Search");
    controlButtonsPanel.add(executeButton);
    cancelButton.setText("Cancel");
    cancelButton.setActionCommand("Cancel");
    controlButtonsPanel.add(Box.createHorizontalStrut(8));
    controlButtonsPanel.add(cancelButton);
    controlButtonsPanel.add(Box.createHorizontalStrut(8));
    queryPanel.add(controlButtonsPanel);
    queryPanel.add(Box.createVerticalStrut(8));
    //}}

    //{{REGISTER_LISTENERS
    SymAction lSymAction = new SymAction();
    moreButton.addActionListener(lSymAction);
    lessButton.addActionListener(lSymAction);
    executeButton.addActionListener(lSymAction);
    cancelButton.addActionListener(lSymAction);
    //}}
  }

  /**
   * Construct a new instance of the query dialog
   *
   * @param framework A reference to the client framework 
   */
  public QueryDialog(ClientFramework framework)
  {
    this((Frame)framework, framework);
  }

  /**
   * main method for testing
   */
  static public void main(String args[])
  {
     (new QueryDialog(new ClientFramework(
          new ConfigXML("./lib/config.xml")))).setVisible(true);
  }

  /** Used by visual cafe -- but why? */
  public void addNotify()
  {
    // Record the size of the window prior to calling parents addNotify.
    Dimension size = getSize();

    super.addNotify();

    if (frameSizeAdjusted) {
      return;
    }
    frameSizeAdjusted = true;

    // Adjust size of frame according to the insets
    Insets insets = getInsets();
    setSize(insets.left + insets.right + size.width,
            insets.top + insets.bottom + size.height);
  }

  // Used by addNotify
  private boolean frameSizeAdjusted = false;

  /** Class to listen for ActionEvents */
  private class SymAction implements java.awt.event.ActionListener
  {
    public void actionPerformed(java.awt.event.ActionEvent event)
    {
      Object object = event.getSource();
      if (object == moreButton)
        moreButton_actionPerformed(event);
      else if (object == lessButton)
        lessButton_actionPerformed(event);
      else if (object == executeButton)
        executeButton_actionPerformed(event);
      else if (object == cancelButton)
        cancelButton_actionPerformed(event);
    }
  }

  /**
   * Performs actions associated with pressing the "More" button
   */
  private void moreButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    SubjectTermPanel tq = new SubjectTermPanel();
    queryChoicesPanel.add(tq);
    textPanels.addElement(tq);
    lessButton.setEnabled(true);
    queryChoicesPanel.invalidate();
    subjectPanel.validate();
  }

  /**
   * Performs actions associated with pressing the "Less" button
   */
  private void lessButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    Component comp = (Component) textPanels.lastElement();
      queryChoicesPanel.remove(comp);
      textPanels.removeElementAt(textPanels.size() - 1);
    if (textPanels.size() < 2)
        lessButton.setEnabled(false);
      queryChoicesPanel.invalidate();
      subjectPanel.validate();
  }

  /** 
   * method to constuct a Query from the dialog tabs
   */
  private Query buildQuery()
  {
    // Create the Query object
    Query newQuery = new Query(framework);

    // Set top level query params
    if (queryTitleTF.getText().length() < 1) {
      queryTitleTF.setText(new Date().toString());
    }
    newQuery.setQueryTitle(queryTitleTF.getText());
    newQuery.setSearchMetacat(catalogSearchCheckBox.isSelected());
    newQuery.setSearchLocal(localSearchCheckBox.isSelected());

    // Add a query group that combines the tabs (always INTERSECT)
    QueryGroup rootQG = new QueryGroup("INTERSECT");
    newQuery.setQueryGroup(rootQG);

    // Add a child query group for each panel
    QueryGroup subjectGroup = buildSubjectQueryGroup();
    rootQG.addChild(subjectGroup);

    return newQuery;
  }

  /** 
   * method to constuct a QueryGroup for the subject panel of the dialog
   */
  private QueryGroup buildSubjectQueryGroup()
  {
    String path = "//*";
    String op = "UNION";
    String value = "*";
    String mode = "contains";

    // Add a query group for the overall Subject tab
    if (orRadioButton.isSelected()) {
      op = "UNION";
    } else {
      op = "INTERSECT";
    }
    QueryGroup subjectGroup = new QueryGroup(op);

    // For each subject constraint, add a query group
    Enumeration enum = textPanels.elements();
    while (enum.hasMoreElements())
    {
      SubjectTermPanel tqtp = (SubjectTermPanel) enum.nextElement();
      // Create a separate QG for each textPanel (always INTERSECT)
      QueryGroup termGroup = new QueryGroup("UNION");
      subjectGroup.addChild(termGroup);

      if (tqtp.getAllState())
      { // All button selected; single query term
        path = "//*";
        value = tqtp.getValue();
        mode = tqtp.getSearchMode();
        QueryTerm allTerm = new QueryTerm(caseSensitive, mode, value);
        termGroup.addChild(allTerm);
      }
      else
      { // check other button choices; multiple queries possible
        value = tqtp.getValue();
        mode = tqtp.getSearchMode();
        if (tqtp.getTitleState()) {
          path = titleSearchPath;
          QueryTerm newTerm = new QueryTerm(caseSensitive, mode, value, path);
          termGroup.addChild(newTerm);
        }
        if (tqtp.getAbstractState()) {
          path = abstractSearchPath;
          if (!value.equals("")) {
            QueryTerm newTerm = new QueryTerm(caseSensitive, mode, value, path);
            termGroup.addChild(newTerm);
          }
        }
        if (tqtp.getKeywordsState()) {
          path = keywordSearchPath;
          QueryTerm newTerm = new QueryTerm(caseSensitive, mode, value, path);
          termGroup.addChild(newTerm);
        }
      }
    }

    return subjectGroup;
  }

  /**
   * Save the query when the execute button is set, making it accessible to
   * the getQuery() method
   */
  private void executeButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    savedQuery = buildQuery();
    searchStarted = true;
    setVisible(false);
  }

  /**
   * Close the dialog when the cancel button is pressed
   */
  private void cancelButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    searchStarted = false;
    setVisible(false);
  }

  /**
   * determine whether the query should be executed
   */
  public boolean isSearchStarted() 
  {
    return searchStarted;
  }

  /**
   * Get the Query built using this dialog box
   */
  public Query getQuery()
  {
    return this.savedQuery;
  }

  /**
   * Set the query that should be displayed and edited in the dialog
   */
  public void setQuery(Query query)
  {
    this.savedQuery = query;

    queryTitleTF.setText(query.getQueryTitle());
    // Now refill all of the screen widgets with the query info
    framework.debug(9, "Warning: setQuery implementation not complete!");
    QueryGroup rootGroup = savedQuery.getQueryGroup();
    initializeSubjectSearch(rootGroup);
  }

  /**
   * Fill in the fields in the subject query with the proper values from
   * a QueryGroup
   */
  private void initializeSubjectSearch(QueryGroup rootGroup) 
  {
    // Remove any existing text panels
    for (int i = 0;  i < textPanels.size();  i++) {
      Component comp = (Component) textPanels.lastElement();
      queryChoicesPanel.remove(comp);
      textPanels.removeElementAt(textPanels.size() - 1);
    }

    // Find the QueryGroup containing the subject parameters
    Enumeration rootChildren = rootGroup.getChildren();

    // Find the group with the subject info (this is too simplistic for later
    // when taxon and spatial searches are added)
    QueryGroup subjectGroup = (QueryGroup)rootChildren.nextElement();

    // Set the And/Or button from operator param in the Subject group
    String op = subjectGroup.getOperator();
    if (op.equalsIgnoreCase("INTERSECT")) {
      orRadioButton.setSelected(false);
      andRadioButton.setSelected(true);
    } else {
      orRadioButton.setSelected(true);
      andRadioButton.setSelected(false);
    }

    // Create a textPanel for each group in the subject group
    Enumeration subjectChildren = subjectGroup.getChildren();
    while (subjectChildren.hasMoreElements()) {

      Object obj = subjectChildren.nextElement();

      // Create the panel for this subject term, and set defaults
      SubjectTermPanel tq = new SubjectTermPanel();
      tq.setAllState(true);
      tq.setTitleState(false);
      tq.setAbstractState(false);
      tq.setKeywordsState(false);

      try {
        // Process each subject query group and make a text panel out of it
        // By getting the params out of the contained QueryTerms
        QueryGroup termsGroup = (QueryGroup)obj;
        Enumeration qtList = termsGroup.getChildren();
  
        // Step through the QueryTerms and extract parameters
        while (qtList.hasMoreElements()) {
          Object obj2 = qtList.nextElement();
          QueryTerm qt = (QueryTerm)obj2;
    
          tq.setValue(qt.getValue());
          tq.setSearchMode(qt.getSearchMode());
          String pathExpression = qt.getPathExpression();
          if (pathExpression == null) {
            tq.setAllState(true);
          } else {
            tq.setAllState(false);
            if (pathExpression.equals(titleSearchPath)) {
              tq.setTitleState(true);
            } else if (pathExpression.equals(abstractSearchPath)) {
              tq.setAbstractState(true);
            } else if (pathExpression.equals(keywordSearchPath)) {
              tq.setKeywordsState(true);
            }
          }
        }
      } catch (ClassCastException cce) {
        framework.debug(3, "Query doesn't meet expectations, " +
                        "so couldn't rebuild dialog correctly!");
        tq = new SubjectTermPanel();
        tq.setAllState(true);
      }

      // Add the text panel to the dialog
      queryChoicesPanel.add(tq);
      textPanels.addElement(tq);
    }
    if (textPanels.size() < 2) {
      lessButton.setEnabled(false);
    } else {
      lessButton.setEnabled(true);
    }

    // Force the window to redraw
    queryChoicesPanel.invalidate();
    subjectPanel.validate();
  }
}
