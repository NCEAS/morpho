/**
 *       Name: QueryDialog.java
 *    Purpose: Visual display for collecting query info from user
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-24 18:52:12 $'
 * '$Revision: 1.7 $'
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



import java.awt.*;
import javax.swing.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

import edu.ucsb.nceas.morpho.framework.*;


/**
 * Dialog which collects search information from user
 * to be used to create pathQuery XML document.
 * 
 * @author higgins
 */
public class QueryDialog extends javax.swing.JDialog
{
  /** A reference to the container framework */
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  /** Vector of textquery panels currently displayed*/
  Vector textPanels;

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

    // This code is automatically generated by Visual Cafe when you add
    // components to the visual environment. It instantiates and initializes
    // the components. To modify the code, only use code syntax that matches
    // what Visual Cafe can generate, or Visual Cafe may be unable to back
    // parse your Java file into its visual environment.
    //{{INIT_CONTROLS
    setTitle("Search");
    getContentPane().setLayout(new BorderLayout(0, 0));
    setSize(718, 347);
    setVisible(false);
    QueryPanel.setLayout(new BorderLayout(0, 0));
    getContentPane().add(BorderLayout.CENTER, QueryPanel);
    QueryPanel.add(BorderLayout.CENTER, QueryTabs);
    SubjectTextPanel.setLayout(new BorderLayout(0, 0));
    QueryTabs.add(SubjectTextPanel);
    SubjectTextPanel.setBounds(2, 24, 713, 277);
    SubjectTextPanel.setVisible(false);
    QueryScrollPanel.setOpaque(true);
    SubjectTextPanel.add(BorderLayout.CENTER, QueryScrollPanel);
    Query.setLayout(new BorderLayout(0, 0));
    QueryScrollPanel.getViewport().add(Query);
    QueryChoicesPanel.setLayout(new BoxLayout(QueryChoicesPanel, 
                                              BoxLayout.Y_AXIS));
    Query.add(BorderLayout.CENTER, QueryChoicesPanel);
    Query.setBounds(0, 0, 710, 249);
    QueryChoicesPanel.setAlignmentX(0.0F);
    SubjectTextControlsPanel.setLayout(new BorderLayout(0, 0));
    SubjectTextPanel.add(BorderLayout.SOUTH, SubjectTextControlsPanel);
    MoreLessControlsPanel.setLayout(new BoxLayout(MoreLessControlsPanel, 
                                                  BoxLayout.X_AXIS));
    SubjectTextControlsPanel.add(BorderLayout.CENTER, MoreLessControlsPanel);
    AndRadioButton.setText("And");
    AndRadioButton.setActionCommand("And");
    MoreLessControlsPanel.add(AndRadioButton);
    OrRadioButton.setText("Or");
    OrRadioButton.setActionCommand("Or");
    OrRadioButton.setSelected(true);
    MoreLessControlsPanel.add(OrRadioButton);
    MoreButton.setText("More");
    MoreButton.setActionCommand("More");
    MoreLessControlsPanel.add(MoreButton);
    LessButton.setText("Fewer");
    LessButton.setActionCommand("Fewer");
    LessButton.setEnabled(false);
    MoreLessControlsPanel.add(LessButton);
    MoreLessControlsPanel.add(Box.createHorizontalGlue());
    OtherTabsCheckBox.setText("Include Queries from Other Tabs");
    OtherTabsCheckBox.setActionCommand("Include Queries from Other Tabs");
    MoreLessControlsPanel.add(OtherTabsCheckBox);
    TaxonomicPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    QueryTabs.add(TaxonomicPanel);
    TaxonomicPanel.setBounds(2, 24, 713, 277);
    TaxonomicPanel.setVisible(false);
    Spatial.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    QueryTabs.add(Spatial);
    Spatial.setBounds(2, 24, 713, 277);
    Spatial.setVisible(false);
    QueryTabs.setSelectedIndex(0);
    QueryTabs.setSelectedComponent(SubjectTextPanel);
    QueryTabs.setTitleAt(0, "Subject/Text");
    QueryTabs.setTitleAt(1, "Taxonomic");
    QueryTabs.setTitleAt(2, "Spatial");
    QueryNamePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    QueryPanel.add(BorderLayout.SOUTH, QueryNamePanel);
    QueryTitleLabel.setText("  Query Title ");
    QueryNamePanel.add(QueryTitleLabel);
    QueryTitleTF.setColumns(20);
    QueryNamePanel.add(QueryTitleTF);
    SearchChoicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    QueryNamePanel.add(SearchChoicePanel);
    CatalogSearchCheckBox.setText("Catalog Search");
    CatalogSearchCheckBox.setActionCommand("Catalog Search");
    CatalogSearchCheckBox.setSelected(true);
    SearchChoicePanel.add(CatalogSearchCheckBox);
    LocalSearchCheckBox.setText("Local Search");
    LocalSearchCheckBox.setActionCommand("Local Search");
    LocalSearchCheckBox.setSelected(true);
    SearchChoicePanel.add(LocalSearchCheckBox);
    ExecuteButton.setText("Begin Search");
    ExecuteButton.setActionCommand("Begin Search");
    QueryNamePanel.add(ExecuteButton);
    //}}
    QueryNamePanel.setBorder(BorderFactory.createRaisedBevelBorder());
    textPanels = new Vector();
    TextQueryTermPanel tqt1 = new TextQueryTermPanel();
    QueryChoicesPanel.add(tqt1);
    textPanels.addElement(tqt1);
    CatalogSearchCheckBox.setSelected(searchMetacat);
    LocalSearchCheckBox.setSelected(searchLocal);
    // do we want to set the config file each time?

    //{{REGISTER_LISTENERS
    SymAction lSymAction = new SymAction();
    MoreButton.addActionListener(lSymAction);
    LessButton.addActionListener(lSymAction);
    SymItem lSymItem = new SymItem();
    AndRadioButton.addItemListener(lSymItem);
    OrRadioButton.addItemListener(lSymItem);
    ExecuteButton.addActionListener(lSymAction);
    //}}
  }

  /**
   * Construct a new instance of the query dialog
   *
   * @param framework A reference to the client framework 
   */
  public QueryDialog(ClientFramework framework)
  {
    this((Frame) null, framework);
  }

  /**
   * Construct a new instance of the query dialog
   *
   * @param sTitle the title for the dialog
   * @param framework A reference to the client framework 
   */
  public QueryDialog(String sTitle, ClientFramework framework)
  {
    this(framework);
    setTitle(sTitle);
  }

  /**
   * main method for testing
   */
  static public void main(String args[])
  {
     (new QueryDialog(new ClientFramework(
          new ConfigXML("./lib/config.xml")))).setVisible(true);
  }

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
  boolean frameSizeAdjusted = false;

  //{{DECLARE_CONTROLS
  javax.swing.JPanel QueryPanel = new javax.swing.JPanel();
  javax.swing.JTabbedPane QueryTabs = new javax.swing.JTabbedPane();
  javax.swing.JPanel SubjectTextPanel = new javax.swing.JPanel();
  javax.swing.JScrollPane QueryScrollPanel = new javax.swing.JScrollPane();
  javax.swing.JPanel Query = new javax.swing.JPanel();
  javax.swing.JPanel QueryChoicesPanel = new javax.swing.JPanel();
  javax.swing.JPanel SubjectTextControlsPanel = new javax.swing.JPanel();
  javax.swing.JPanel MoreLessControlsPanel = new javax.swing.JPanel();
  javax.swing.JRadioButton AndRadioButton = new javax.swing.JRadioButton();
  javax.swing.JRadioButton OrRadioButton = new javax.swing.JRadioButton();
  javax.swing.JButton MoreButton = new javax.swing.JButton();
  javax.swing.JButton LessButton = new javax.swing.JButton();
  javax.swing.JCheckBox OtherTabsCheckBox = new javax.swing.JCheckBox();
  javax.swing.JPanel TaxonomicPanel = new javax.swing.JPanel();
  javax.swing.JPanel Spatial = new javax.swing.JPanel();
  javax.swing.JPanel QueryNamePanel = new javax.swing.JPanel();
  javax.swing.JLabel QueryTitleLabel = new javax.swing.JLabel();
  javax.swing.JTextField QueryTitleTF = new javax.swing.JTextField();
  javax.swing.JPanel SearchChoicePanel = new javax.swing.JPanel();
  javax.swing.JCheckBox CatalogSearchCheckBox = new javax.swing.JCheckBox();
  javax.swing.JCheckBox LocalSearchCheckBox = new javax.swing.JCheckBox();
  javax.swing.JButton ExecuteButton = new javax.swing.JButton();
  //}}


  class SymAction implements java.awt.event.ActionListener
  {
    public void actionPerformed(java.awt.event.ActionEvent event)
    {
      Object object = event.getSource();
      if (object == MoreButton)
        MoreButton_actionPerformed(event);
      else if (object == LessButton)
        LessButton_actionPerformed(event);
      else if (object == ExecuteButton)
        ExecuteButton_actionPerformed(event);
    }
  }

  void MoreButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    TextQueryTermPanel tq = new TextQueryTermPanel();
      QueryChoicesPanel.add(tq);
      textPanels.addElement(tq);
      LessButton.setEnabled(true);
      QueryChoicesPanel.invalidate();
      SubjectTextPanel.validate();
  }

  void LessButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    Component comp = (Component) textPanels.lastElement();
      QueryChoicesPanel.remove(comp);
      textPanels.removeElementAt(textPanels.size() - 1);
    if (textPanels.size() < 2)
        LessButton.setEnabled(false);
      QueryChoicesPanel.invalidate();
      SubjectTextPanel.validate();
  }

  class SymItem implements java.awt.event.ItemListener
  {
    public void itemStateChanged(java.awt.event.ItemEvent event)
    {
      Object object = event.getSource();
      if (object == AndRadioButton)
        AndRadioButton_itemStateChanged(event);
      else if (object == OrRadioButton)
        OrRadioButton_itemStateChanged(event);
    }
  }

  void AndRadioButton_itemStateChanged(java.awt.event.ItemEvent event)
  {
    if (AndRadioButton.isSelected())
    {
      OrRadioButton.setSelected(false);
    }
  }

  void OrRadioButton_itemStateChanged(java.awt.event.ItemEvent event)
  {
    if (OrRadioButton.isSelected())
    {
      AndRadioButton.setSelected(false);
    }
  }

  /** method to constuct a pathQuery XML document from
   *  the visual settings of the Subject/Text tab of this
   *  QueryDialog
   */
  private String buildTextPathQuery()
  {
    String ret = "";
    String path = "//*";
    String op = "UNION";
    String value = "*";
    String mode = "contains";

    // loop over the collection of TextQueryTermPanels
    PathQueryXMLDoc pqxml = new PathQueryXMLDoc(config);
    if (OrRadioButton.isSelected()) {
      op = "UNION";
    } else {
      op = "INTERSECT";
    }

    pqxml.add_querygroup(op);
    Enumeration enum = textPanels.elements();
    while (enum.hasMoreElements())
    {
      TextQueryTermPanel tqtp = (TextQueryTermPanel) enum.nextElement();
      // Create a separate QG for each panel
      pqxml.add_querygroup_asChild("UNION");
      //pqxml.add_querygroup("UNION");

      if (tqtp.getAllState())
      { // All button selected; single query term
        path = "//*";
        value = tqtp.getValue();
        mode = tqtp.getSearchMode();
        pqxml.add_queryterm(value, path, mode, caseSensitive);
      }
      else
      { // check other button choices; multiple queries possible
        int bCnt = 0;
        if (tqtp.getTitleState())
          bCnt++;
        if (tqtp.getAbstractState())
          bCnt++;
        if (tqtp.getKeyWordsState())
          bCnt++;
        if (bCnt > 0)
        {
          //if (bCnt > 1)
          //{ // combine using 'Or' 
            //pqxml.add_querygroup_asChild("UNION");
          //}
          if (tqtp.getTitleState())
          {
            path = titleSearchPath;
            value = tqtp.getValue();
            mode = tqtp.getSearchMode();
            pqxml.add_queryterm(value, path, mode, caseSensitive);
          }
          if (tqtp.getAbstractState())
          {
            path = abstractSearchPath;
            value = tqtp.getValue();
            mode = tqtp.getSearchMode();
            if (!value.equals(""))
            {
              pqxml.add_queryterm(value, path, mode, caseSensitive);
            }
          }
          if (tqtp.getKeyWordsState())
          {
            path = keywordSearchPath;
            value = tqtp.getValue();
            mode = tqtp.getSearchMode();
            pqxml.add_queryterm(value, path, mode, caseSensitive);
          }
          //if (bCnt > 1)
          //{ // end group 
            //pqxml.end_querygroup();
          //}

        }
      }

      // End the group associated with each panel
      pqxml.end_querygroup();
    }
    pqxml.end_query();
    ret = pqxml.get_XML();
    return ret;
  }

  /**
   * Save the query when the execute button is set, making it accessible to
   * the getQuery() method
   */
  private void ExecuteButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    String temp = buildTextPathQuery();
    if (QueryTitleTF.getText().length() < 1)
    {
      QueryTitleTF.setText(new Date().toString());
    }

    Query query = new Query(temp, framework);
    query.setQueryTitle(QueryTitleTF.getText());
    query.setSearchMetacat(CatalogSearchCheckBox.isSelected());
    query.setSearchLocal(LocalSearchCheckBox.isSelected());
    savedQuery = query;
    setVisible(false);
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

    // Now refill all of the screen widgets with the query info
    framework.debug(9, "Warning: setQuery implementation not complete!");
  }
}
