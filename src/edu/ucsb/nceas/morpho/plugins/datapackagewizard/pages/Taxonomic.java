/**
*  '$RCSfile: Taxonomic.java,v $'
*    Purpose: A class that handles xml messages passed by the
*             package wizard
*  Copyright: 2000 Regents of the University of California and the
*             National Center for Ecological Analysis and Synthesis
*    Authors: Saurabh Garg
*    Release: @release@
*
*   '$Author: brooke $'
*     '$Date: 2004-03-17 21:13:01 $'
* '$Revision: 1.8 $'
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


import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;

import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.HyperlinkButton;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Point;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import javax.swing.BorderFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.InputVerifier;

import java.util.List;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;


public class Taxonomic extends AbstractUIPage {

  public final String pageID     = DataPackageWizardInterface.TAXONOMIC;
  public final String nextPageID = null;
  public final String pageNumber = "1";

  //////////////////////////////////////////////////////////

  public final String title      = "Taxonomic Information";
  public final String subtitle   = " ";

  ////////////////////////////////////////////////////////////

  private final String heading = "Enter Taxonomic Information for the Data Package. You can enter the data in place for upto two levels of taxonomic information for a given taxon. To define more levels and/or common names for each level, hit the 'Edit' button";

  // column titles for the customlist in the main-page
  private String colNames[] = {"Higher Level Taxa", "Rank", "Name",
  "Rank", "Name", "Common Name(s)"};

  // CustomList listing the taxons ranks and names
  private CustomList taxonList;
  // Pick list to select the table from which the taxon data has to be imported
  private JComboBox importTableComboBox;
  // Pick list to select the column in the table whose data has to be imported
  private JComboBox importColComboBox;

  private JTextField classSystemTextField;
  private JTextField classNameTextField;
  private JPanel classPanel;
  private JCheckBox classCheckBox;

  private JDialog importTaxaDialog = null;
  private TaxonImportPanel taxonImportPanel = null;
  private JDialog parentTaxaDialog = null;


  public Taxonomic() {

    init();
  }

  /**
  * initialize method does frame-specific design - i.e. adding the widgets that
  are displayed only in this frame (doesn't include prev/next buttons etc)
  */
  private void init() {
    this.setLayout(new BorderLayout());
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
    this.add(centerPanel, BorderLayout.CENTER);

    JTextField colObjects[] = new JTextField[6];
    colObjects[0] = new JTextField();
    ((JTextField)colObjects[0]).setEditable(false);
    ((JTextField)colObjects[0]).setBackground(Color.lightGray);
    ((JTextField)colObjects[0]).setDisabledTextColor(Color.black);
    ((JTextField)colObjects[0]).setForeground(Color.black);
    for(int i = 1;i<6;i++) {
      colObjects[i] = new JTextField();
    }

    colObjects[1].setInputVerifier(new NewTaxonRankVerifier(this, 1));
    colObjects[2].setInputVerifier(new TaxonNameVerifier(this, 2));
    colObjects[3].setInputVerifier(new NewTaxonRankVerifier(this, 3));
    colObjects[4].setInputVerifier(new TaxonNameVerifier(this, 4));
    colObjects[5].setInputVerifier(new CommonNameVerifier(this));
    taxonList = WidgetFactory.makeList(colNames, colObjects, 0, true, true, false,
    true, false, false);
    double[] colPercentages = new double[] {28,14,14,14,14,16};
    taxonList.setColumnWidthPercentages(colPercentages);
    Action addAction = new AbstractAction (){
      public void actionPerformed(ActionEvent e) {

        TaxonListAddAction();
      }
    };
    Action editAction = new AbstractAction (){
      public void actionPerformed(ActionEvent e) {

        TaxonListEditAction();
      }
    };
    taxonList.setCustomAddAction(addAction);
    taxonList.setCustomEditAction(editAction);
    taxonList.fireAddAction();

    JLabel headLabel = WidgetFactory.makeHTMLLabel(heading, 2, false);
    JPanel taxonPanel = new JPanel(new BorderLayout());
    taxonPanel.add(headLabel, BorderLayout.NORTH);
    taxonPanel.add(taxonList, BorderLayout.CENTER);
    centerPanel.add(taxonPanel);
    centerPanel.add(WidgetFactory.makeDefaultSpacer());

    ///////////////

    JPanel importPanel = new JPanel();
    importPanel.setLayout(new BorderLayout());

    GUIAction action = new GUIAction("Import Taxon Information from Data table...",
    null,	new Command() {
      public void execute(ActionEvent ae) {

        Point p = Taxonomic.this.getLocationOnScreen();
        int xc=(int)p.getX()+Taxonomic.this.getWidth()/2 - TaxonImportPanel.DIALOG_WIDTH/2;
        int yc=(int)p.getY()+Taxonomic.this.getHeight()/2 - TaxonImportPanel.DIALOG_HEIGHT/2;

        ActionListener okAction = new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            taxonImportOKAction();
          }
        };
        ActionListener cancelAction = new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            taxonImportCancelAction();
          }
        };

        taxonImportPanel = new TaxonImportPanel();
        importTaxaDialog = WidgetFactory.makeContainerDialog(taxonImportPanel, okAction, cancelAction, "Import", "Cancel");
        importTaxaDialog.setBounds(xc, yc, TaxonImportPanel.DIALOG_WIDTH, TaxonImportPanel.DIALOG_HEIGHT);
        importTaxaDialog.setVisible(true);

      }
    });
    JButton importButton = new HyperlinkButton(action);
    importPanel.add(importButton, BorderLayout.EAST);
    importPanel.setBorder(BorderFactory.createEmptyBorder(2,0,0,0));
    taxonPanel.add(importPanel, BorderLayout.SOUTH);

    //////////////


    JLabel classSystemLabel = WidgetFactory.makeLabel("System Name:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    classSystemTextField = WidgetFactory.makeOneLineTextField();
    JPanel cpanel1 = new JPanel();
    cpanel1.setLayout(new BoxLayout(cpanel1, BoxLayout.X_AXIS));
    cpanel1.add(classSystemLabel);
    cpanel1.add(classSystemTextField);

    JLabel classNameLabel = WidgetFactory.makeLabel("Identifier Name:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    classNameTextField = WidgetFactory.makeOneLineTextField();
    JPanel cpanel2 = new JPanel();
    cpanel2.setLayout(new BoxLayout(cpanel2, BoxLayout.X_AXIS));
    cpanel2.add(classNameLabel);
    cpanel2.add(classNameTextField);


    classPanel = new JPanel();
    classPanel.setLayout(new GridLayout(1,2,4,0));
    classPanel.add(cpanel1);
    classPanel.add(cpanel2);

    JPanel t = new JPanel(new FlowLayout(FlowLayout.LEFT));
    classCheckBox = WidgetFactory.makeCheckBox("The Taxon values belong to a different classification system", false);
    classCheckBox.addActionListener( new ActionListener (){
      public void actionPerformed(ActionEvent ae) {
        if(classCheckBox.isSelected())
          classPanel.setVisible(true);
        else
          classPanel.setVisible(false);
        classPanel.invalidate();
        classPanel.repaint();
      }
    });
    t.add(classCheckBox);
    centerPanel.add(t);
    centerPanel.add(WidgetFactory.makeDefaultSpacer());
    centerPanel.add(classPanel);
    centerPanel.add(WidgetFactory.makeDefaultSpacer());
    classPanel.setVisible(false);
  }

  private void TaxonListAddAction() {

    List rows = taxonList.getListOfRowLists();
    List newRow = new ArrayList();
    ParentTaxaPanel panel = null;
    if(rows.size() == 0) {
      newRow.add("");
      newRow.add("Genus"); newRow.add("");
      newRow.add("Species"); newRow.add("");
      newRow.add("");
      panel = new ParentTaxaPanel();
    } else {
      int selIdx = taxonList.getSelectedRowIndex();
      if(selIdx < 0) selIdx = rows.size() - 1;
      List r = (List)rows.get(selIdx);
      Object o = r.get(6);
      if(o != null  && (o instanceof ParentTaxaPanel)) {
        ParentTaxaPanel p = (ParentTaxaPanel)o;
        if(p.hasBeenEdited())
          panel = (ParentTaxaPanel)p.clonePanel();
        if(panel != null) {
          newRow = panel.getSurrogate();
          newRow.set(2, "");
          newRow.set(4, "");
          newRow.set(5, "");
        }
      }
      if(panel == null) {
        newRow.add("");
        newRow.add((String)r.get(1)); newRow.add("");
        newRow.add((String)r.get(3)); newRow.add("");
        newRow.add("");
        panel = new ParentTaxaPanel();
      }
    }
    newRow.add(panel);
    taxonList.addRow(newRow);

  }


  private void TaxonListEditAction() {

    List row = taxonList.getSelectedRowList();
    if(row.size() < 7 ) return;
    Object o = row.get(6);
    if(o == null || !(o instanceof ParentTaxaPanel)) return;
    final ParentTaxaPanel panel = (ParentTaxaPanel)o;
    panel.setHierarchy(row);

    ActionListener okAction = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        parentTaxaOKAction(panel);
      }
    };
    ActionListener cancelAction = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        parentTaxaDialog.setVisible(false);
      }
    };

    parentTaxaDialog = WidgetFactory.makeContainerDialog(panel, okAction, cancelAction);

    Point p = getLocationOnScreen();
    int xc=(int)p.getX()+ getWidth()/2 - ParentTaxaPanel.DIALOG_WIDTH/2;
    int yc=(int)p.getY()+ getHeight()/2 - ParentTaxaPanel.DIALOG_HEIGHT/2;
    parentTaxaDialog.setBounds(xc, yc,ParentTaxaPanel.DIALOG_WIDTH,ParentTaxaPanel.DIALOG_HEIGHT);
    parentTaxaDialog.setVisible(true);

  }

  private JLabel getLabel(String text) {

    if (text==null) text="";
    JLabel label = new JLabel(text);

    label.setAlignmentX(1.0f);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    label.setBorder(BorderFactory.createMatteBorder(1,10,1,3, (Color)null));

    return label;
  }

  private void parentTaxaOKAction(ParentTaxaPanel panel) {

    if(!panel.onAdvanceAction()) {
      return;
    }
    List newRow = panel.getSurrogate();
    newRow.add(panel);
    taxonList.replaceSelectedRow(newRow);
    parentTaxaDialog.setVisible(false);
  }

  private void taxonImportOKAction() {

    List importedData = taxonImportPanel.getListOfImportedTaxa();
    Iterator it = importedData.iterator();
    while(it.hasNext()) {

      List t = (List) it.next();
      String taxon = (String)t.get(0);
      String rank = (String)t.get(1);
      int rankIdx = WizardSettings.getIndexOfTaxonRank(rank);

      List newRow = new ArrayList();
      newRow.add("");
      if(rankIdx <= 5) { // Genus or higher in hierarchy
        newRow.add(rank); newRow.add(taxon);
        newRow.add("");newRow.add("");
      } else {
        newRow.add("");newRow.add("");
        newRow.add(rank); newRow.add(taxon);
      }
      newRow.add("");
      newRow.add(new ParentTaxaPanel());
      taxonList.addRow(newRow);
    }

    importTaxaDialog.setVisible(false);
  }

  private void taxonImportCancelAction() {

    importTaxaDialog.setVisible(false);
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
  public void onRewindAction() {
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
    return true;
  }


  /**
  *  gets the Map object that contains all the key/value paired
  *  settings for this particular wizard page
  *
  *  @return   data the Map object that contains all the
  *            key/value paired settings for this particular wizard page
  */
  public OrderedMap getPageData() {

    return null;
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
   *  sets the fields in the waird page using the Map object
   *  that contains all the key/value paired
   *
   *  @param   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  public void setPageData(OrderedMap data) { }

  //////////////////////////////////////////////////////
  //InputVerifiers for the TextField in the CustomList
  //////////////////////////////////////////////////////


  private static int validateTaxonCounter = 0;
  private static int commonNameCounter = 0;
  private static int taxonNameCounter = 0;
  class NewTaxonRankVerifier extends InputVerifier {

    Taxonomic parent;
    int pos;

    NewTaxonRankVerifier(Taxonomic panel, int pos) {
      this.parent = panel;
      this.pos = pos;
    }

    public boolean verify(JComponent jc) {

      if(validateTaxonCounter > 0) {
        System.out.println("ret excess in newtaxonrank");
        return true;
      }
      validateTaxonCounter++;

      boolean error = false;
      JTextField textField = (JTextField) jc;
      String newText = textField.getText();
      if(newText == null || newText.trim().equals("")) {
        validateTaxonCounter--;
        return true;
      }
      if(!isValidName(newText)) {
        JOptionPane.showMessageDialog(Taxonomic.this, "Invalid characters in the taxon rank. Only letters and spaces are allowed.", "Error", JOptionPane.ERROR_MESSAGE);
        validateTaxonCounter--;
        int idx = Taxonomic.this.taxonList.getSelectedRowIndex();
        Taxonomic.this.taxonList.editCellAt(idx, pos);
        int[] selRows = new int[]{idx};
        Taxonomic.this.taxonList.setSelectedRows(selRows);
        return false;
      }

      List selRow = taxonList.getSelectedRowList();

      String hier = "";
      String firstTaxon = "";
      String secTaxon = "";
      String comName = "";
      List hierNames = null;

      int size = selRow.size();
      if(size > 0) {
        hier = (String)selRow.get(0);
        hierNames = getHierarchy(hier);
      }
      if(size > 1)
        firstTaxon = (String)selRow.get(1);
      if(size > 3)
        secTaxon = (String)selRow.get(3);
      if(size > 5)
        comName = (String)selRow.get(5);

      // check the hierarchy
      Iterator it = hierNames.iterator();
      while(it.hasNext()) {
        String token = (String)it.next();
        if(token.equals(newText)) {
          error = true;
          break;
        }
      }

      if(pos == 1) {

        if(secTaxon.equals(newText)) {
          error = true;
        }
      } else if (pos == 3) {

        if(firstTaxon.equals(newText)) {
          error = true;
        }
      }

      if(error) {
        JOptionPane.showMessageDialog(Taxonomic.this, "Error in the entry! The entered taxon rank is already present in the taxonomic hierarchy", "Error", JOptionPane.ERROR_MESSAGE);
        int idx = Taxonomic.this.taxonList.getSelectedRowIndex();
        Taxonomic.this.taxonList.editCellAt(idx, pos);
        jc.requestFocus();
        validateTaxonCounter--;
        int[] selRows = new int[]{idx};
        Taxonomic.this.taxonList.setSelectedRows(selRows);
        return false;
      }
      validateTaxonCounter--;
      return true;
    }

    private boolean isValidName(String name) {

      char[] arr = name.toCharArray();
      for(int i = 0; i < arr.length; i++)
        if(!(Character.isLetter(arr[i]) || Character.isSpaceChar(arr[i]) ))
          return false;
      return true;
    }

    private List getHierarchy(String hier) {

      StringTokenizer st = new StringTokenizer(hier, ";");
      List result = new ArrayList();
      if(hier.trim().equals(""))
        return result;

      while(st.hasMoreTokens()) {

        String token = st.nextToken();
        int idx = token.indexOf("=");
        result.add(token.substring(0, idx));
      }

      return result;
    }
  }

  class CommonNameVerifier extends InputVerifier {

    Taxonomic parent;

    CommonNameVerifier(Taxonomic panel) {
      this.parent = panel;
    }

    public boolean verify(JComponent jc) {

      if(commonNameCounter > 0) {
        System.out.println("ret excess listener");
        return true;
      }
      commonNameCounter++;
      if(!validateCommonNames((JTextField)jc)) {
        JOptionPane.showMessageDialog(parent, "Invalid characters in the common name(s). Only letters, digits and spaces are allowed. Common names are seperated by a comma", "Error", JOptionPane.ERROR_MESSAGE);
        int rowIdx = Taxonomic.this.taxonList.getSelectedRowIndex();
        Taxonomic.this.taxonList.editCellAt(rowIdx, 5);
        jc.requestFocus();
        commonNameCounter--;
        int[] selRows = new int[]{rowIdx};
        Taxonomic.this.taxonList.setSelectedRows(selRows);
        return false;
      }
      commonNameCounter--;
      return true;
    }

    private boolean validateCommonNames(JTextField textField) {

      String text = textField.getText();
      char arr[] = text.toCharArray();
      for(int i =0; i < arr.length; i++) {
        if(arr[i] == ',' || Character.isLetterOrDigit(arr[i]) || Character.isSpaceChar(arr[i]))
          continue;
        return false;
      }
      return true;
    }
  }

  class TaxonNameVerifier extends InputVerifier {

    Taxonomic parent;
    int pos;

    TaxonNameVerifier(Taxonomic panel, int pos) {
      this.parent = panel;
      this.pos = pos;
    }

    public boolean verify(JComponent jc) {

      if(taxonNameCounter > 0) {
        System.out.println("ret excess listener");
        return true;
      }
      taxonNameCounter++;

      if(!isValidName( ((JTextField)jc).getText() )) {
        JOptionPane.showMessageDialog(parent, "Invalid characters in the taxon name. Only letters, digits, spaces are allowed.", "Error", JOptionPane.ERROR_MESSAGE);
        int rowIdx = Taxonomic.this.taxonList.getSelectedRowIndex();
        Taxonomic.this.taxonList.editCellAt(rowIdx, pos);
        jc.requestFocus();
        int[] selRows = new int[]{rowIdx};
        Taxonomic.this.taxonList.setSelectedRows(selRows);
        taxonNameCounter--;
        return false;
      }
      taxonNameCounter--;
      return true;
    }

    private boolean isValidName(String text) {

      char arr[] = text.toCharArray();
      for(int i =0; i < arr.length; i++) {
        if(arr[i] == ',' || Character.isLetterOrDigit(arr[i]) || Character.isSpaceChar(arr[i]))
          continue;
        return false;
      }
      return true;
    }
  }

}


class ParentTaxaPanel extends JPanel implements WizardPageSubPanelAPI{

  public static final int DIALOG_WIDTH = 450;
  public static final int DIALOG_HEIGHT = 500;

  private JLabel headLabel;
  private CustomList hierList = null;
  private boolean editedAlready = false;

  ParentTaxaPanel() {
    super();
    init();
  }

  private void init() {

    JPanel sub = WidgetFactory.makePanel();
    sub.setLayout(new BoxLayout(sub, BoxLayout.Y_AXIS));
    JLabel info = getLabel("Enter information about the Taxon");
    //sub.add(info);
    sub.add(WidgetFactory.makeDefaultSpacer());

    headLabel = WidgetFactory.makeLabel("Enter the Taxonomic Hierarychy (in descending order):", false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    JPanel labelPanel = new JPanel(new BorderLayout());
    labelPanel.add(headLabel, BorderLayout.CENTER);

    String colNames2 []= new String[] {"Rank", "Name", "Common Name(s)"};
    JTextField jtf2[] = new JTextField[3];
    for(int i=0;i<3;i++)
      jtf2[i]= new JTextField();

    hierList = WidgetFactory.makeList(colNames2, jtf2, 0, true, false, false, true, true, true);
    JPanel hierPanel = new JPanel(new BorderLayout());
    hierPanel.add(hierList, BorderLayout.CENTER);

    sub.add(labelPanel);
    sub.add(WidgetFactory.makeDefaultSpacer());
    sub.add(hierPanel);
    sub.add(WidgetFactory.makeDefaultSpacer());

    setLayout(new BorderLayout());
    add(sub, BorderLayout.CENTER);
  }

  public void setHierarchy(List row) {

    if(row == null || row.size() < 6) return;

    String ancestor = (String)row.get(0);
    String rank1 = (String)row.get(1);
    String name1 = (String)row.get(2);
    String rank2 = (String)row.get(3);
    String name2 = (String)row.get(4);

    String commonName = (String)row.get(5);
    if(!editedAlready) {

      hierList.removeAllRows();
      int r1 = WizardSettings.getIndexOfTaxonRank(rank1);
      int r2 = WizardSettings.getIndexOfTaxonRank(rank2);
      int max = (r1>=r2)?r1:r2;
      List hier = WizardSettings.getTaxonHierarchyTillIndex(max);
      Iterator it = hier.iterator();
      while(it.hasNext()) {
        String r = (String)it.next();
        String v = "";
        String cn = "";
        if(r.equals(rank1)) v = name1;
        else if(r.equals(rank2)) { v = name2; cn = commonName;}
        List newRow = new ArrayList();
        newRow.add(r);
        newRow.add(v);
        newRow.add(cn);
        hierList.addRow(newRow);
      }

    } else {

      List rows = hierList.getListOfRowLists();
      int size = rows.size();
      if(size < 1) return;
      if(size >= 2) {
        List pr = (List)rows.get(size - 2);
        pr.set(0, rank1);
        pr.set(1, name1);
        hierList.replaceRow(size -2, pr);
      }
      List cr = new ArrayList();
      cr.add(rank2);
      cr.add(name2);
      cr.add(commonName);
      hierList.replaceRow(size - 1, cr);
    }

    return;

  }

  public List getSurrogate() {

    List res = new ArrayList();
    String cn = "";
    List rows = hierList.getListOfRowLists();
    if(rows.size() >= 2) {
      Iterator it = rows.iterator();
      for(int i = 0; i < rows.size() - 2; i++) {

        List r = (List)it.next();
        String rk = (String)r.get(0);
        if(rk == null || rk.trim().equals("")) continue;
        String nm = (String)r.get(1);
        if(nm == null || nm.trim().equals("")) continue;
        cn += (rk + "=" + nm + ";");
      }
      res.add(cn);
      List pl = (List)it.next();
      res.add((String)pl.get(0));
      res.add((String)pl.get(1));
      List cl = (List)it.next();
      res.add((String)cl.get(0));
      res.add((String)cl.get(1));
      res.add((String)cl.get(2));

    } else { // only 1 taxon

      if(rows.size() == 0) return res;
      List cr = (List)rows.get(0);
      String rank = (String) cr.get(0);
      int rankIndex = WizardSettings.getIndexOfTaxonRank(rank);
      if(rankIndex == WizardSettings.NUMBER_OF_TAXON_RANKS - 1) {

        res.add(""); // for ancestor
        res.add(""); // for parent rank
        res.add(""); // for parent name
        res.add(rank);
        res.add((String)cr.get(1));
        res.add((String)cr.get(2));
      } else {

        res.add(""); // for ancestor
        res.add(rank);
        res.add((String)cr.get(1));
        res.add(""); // for lowest rank
        res.add(""); // for lowest name
        res.add(""); // for lowest rank's common names
      }
    }

    this.editedAlready = true;
    return res;

  }

  public boolean hasBeenEdited() {

    return this.editedAlready;
  }

  /**
   *  The action to be executed when the panel is displayed. May be empty
   */

  public void onLoadAction() {
  }

  /** The action to be taken when 'OK' is pressed. It checks for and removes all empty
  *	rows (if rank or name or both are empty). It returns true if the data is valid
  *	(contains atleast 1 level). Otherwise, it returns false
  *
  *	@return boolean returns true if the data entered is valid. Otherwise returns false
  *
  */

  public boolean onAdvanceAction() {

    // remove unnecessary rows
    short[] conditions = new short[] {CustomList.EMPTY_STRING_TRIM, CustomList.EMPTY_STRING_TRIM, CustomList.IGNORE };
    hierList.deleteEmptyRows(CustomList.OR, conditions);

    if (hierList.getRowCount() == 0)
      return false;

    List ranks = new ArrayList();
    List rows = hierList.getListOfRowLists();
    Iterator it = rows.iterator();
    while(it.hasNext()) {

      List row = (List)it.next();
      String t = (String)row.get(0);
      if(ranks.contains(t)) {
        JOptionPane.showMessageDialog(this, "Invalid hierarchy! Two taxonomic levels cannot have the same taxon rank", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      ranks.add(t);
    }

    return true;
  }

  /**
   *  checks that the user has filled in required fields - if not, highlights
   *  labels to draw attention to them
   *
   *  @return   boolean true if user data validated OK. false if intervention
   *            required
   */

  public boolean validateUserInput() {

    boolean ret = onAdvanceAction();
    if(!ret) {
      WidgetFactory.hiliteComponent(headLabel);
    } else {
      WidgetFactory.unhiliteComponent(headLabel);
    }
    return ret;
  }


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

    return null;
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

  }

  private JLabel getLabel(String text) {

    if (text==null) text="";
    JLabel label = new JLabel(text);

    label.setAlignmentX(1.0f);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    label.setBorder(BorderFactory.createMatteBorder(1,10,1,3, (Color)null));

    return label;
  }

  public Object clonePanel() {

    ParentTaxaPanel panel = new ParentTaxaPanel();
    Iterator it = this.hierList.getListOfRowLists().iterator();
    while(it.hasNext()) {
      List row = (List)it.next();
      panel.hierList.addRow(row);
    }
    return panel;
  }


}
