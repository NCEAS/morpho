/**
*  '$RCSfile: Taxonomic.java,v $'
*    Purpose: A class that handles xml messages passed by the
*             package wizard
*  Copyright: 2000 Regents of the University of California and the
*             National Center for Ecological Analysis and Synthesis
*    Authors: Saurabh Garg
*    Release: @release@
*
*   '$Author: tao $'
*     '$Date: 2009-04-23 01:40:36 $'
* '$Revision: 1.38 $'
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


import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.HyperlinkButton;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

public class Taxonomic extends AbstractUIPage {
	
    /**
     *Import Language into Morpho
     *by pstango 2010/03/15 
     */
    public static Language lan = new Language();	

  public final String pageID     = DataPackageWizardInterface.TAXONOMIC;
  public final String pageNumber = "12";

  //////////////////////////////////////////////////////////

  public final String title      = /*"Taxonomic Coverage"*/ lan.getMessages("TaxonomicCoverage") ;
  public final String subtitle   = "";
  private final String xPathRoot  = "/eml:eml/dataset/coverage/taxonomicCoverage[1]";

  ////////////////////////////////////////////////////////////

  private final String heading
    = "<p>" 
    + /*"<b>Enter information about the Taxonomic Coverage. </b>"*/ "<b>" + lan.getMessages("Taxonomic.heading_1") + " </b>"
    + /*"By default, you may enter information on Genus and Species.  "*/ lan.getMessages("Taxonomic.heading_2") + " "
    /*+ "If you would like to enter information at another classification rank or "
      + "would like to change the default classification rank, click the edit button."*/
    + lan.getMessages("Taxonomic.heading_3") + " "    
    /*+ "Note that the field 'Higher Level Taxa' is dynamically "
      + "generated from your entries and is not manually editable."*/
    + lan.getMessages("Taxonomic.heading_4")
    + "</p>";

  private final String headingNoImportTable
    = "<p>" 
    + /*"<b>Enter information about the Taxonomic Coverage. </b>"*/ "<b>" + lan.getMessages("Taxonomic.heading_1") + " </b>"
    + /*"By default, you may enter information on Genus and Species.  "*/ lan.getMessages("Taxonomic.heading_2") + " " 
    /*+ "If you would like to enter information at another classification rank or "
      + "would like to change the default classification rank, click the edit button.  "*/
    + lan.getMessages("Taxonomic.heading_3") + " "
    /*+ "Note that the field 'Higher Level Taxa' is dynamically "
      + "generated from your entries and is not manually editable." */
    + lan.getMessages("Taxonomic.heading_4")
    + "</p>"
    + "<br><p>" 
    /*+ "If your information about the taxonomic coverage is extensive "
      + "(e.g., an extensive list of species), you can import this information "
      + "in the form of a table. " */
    + lan.getMessages("Taxonomic.headingNoImportTable_1")
    /*+ "See the Frequently Asked Questions section of the Morpho User Guide to find out how to do this."*/
    + lan.getMessages("Taxonomic.headingNoImportTable_2")
    + "</p>";

  // column titles for the customlist in the main-page
  private String colNames[] = {/*"Higher Level Taxa"*/ lan.getMessages("Taxonomic.HigherLevelTaxa"),
		  					   /*"Rank"*/ lan.getMessages("Taxonomic.Rank"),
		  					   /*"Name"*/ lan.getMessages("Name"),
		  					   /*"Rank"*/ lan.getMessages("Taxonomic.Rank"),
		  					   /*"Name"*/ lan.getMessages("Name"),
                               /*"Common Name(s)"*/ lan.getMessages("Taxonomic.CommonName(s)")
                               };

  // selectedRowIdx is used to store the value of the selected row.
  // this is used because if a row is edited using the edit button, after edit
  // is completed, none of the rows are selected. this results in a
  // null pointer exception. hence to avoid that this variable is used.
  private int selectedRowIdx = 0;

  // column titles for the classification CustomList
  private final String[] classColNames = {/*"Citation Title"*/ lan.getMessages("Taxonomic.CitationTitle"),
		  								  /*"Creator"*/ lan.getMessages("Creator"),
		  								  /*"Citation Type"*/ lan.getMessages("Taxonomic.CitationType")
		  								  };
  private final Object[] classEditors = {null, null, null}; //makes non-directly-editable

  // CustomList listing the taxons ranks and names
  private CustomList taxonList;
  // CustomList listing the classsification citations
  private CustomList classList;

  private JDialog importTaxaDialog = null;
  private TaxonImportPanel taxonImportPanel = null;
  private JDialog parentTaxaDialog = null;
  private boolean checkCitation = false;

  private JTextField colObjects[];
  public Taxonomic() {
	nextPageID = DataPackageWizardInterface.METHODS;
    init();
  }
  
  /**
   * This Constructor will check citation in onAdvanceAction
   * @param checkPersonnel
   */
  public Taxonomic(Boolean checkCitation)
  {
	  this();
	  try
	  {
	    this.checkCitation = checkCitation.booleanValue();
	  }
	  catch(Exception e)
	  {
		  Log.debug(30, "couldn't get the boolean value for "+checkCitation);
	  }
	 	  
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

    Color bgColor = new Color(192, 192, 192);
    colObjects = new JTextField[6];
    colObjects[0] = new JTextField();
    ((JTextField)colObjects[0]).setEditable(false);
    ((JTextField)colObjects[0]).setBackground(bgColor);
    ((JTextField)colObjects[0]).setDisabledTextColor(Color.black);
    ((JTextField)colObjects[0]).setForeground(Color.black);
    for(int i = 1;i<6;i++) {
      colObjects[i] = new JTextField();
    }
    colObjects[1].setInputVerifier(new InputVerifier() {
      public boolean verify(JComponent input) {
				return Taxonomic.this.verifyTaxonRank(Taxonomic.this, ((JTextField)input).getText(), 1);
      }
			public boolean shouldYieldFocus(JComponent input) {
				return true;
			}
    });

		colObjects[2].setInputVerifier(new InputVerifier() {
      public boolean verify(JComponent input) {
				return Taxonomic.this.verifyTaxonName(Taxonomic.this, (JTextField)input, 2);
      }
			public boolean shouldYieldFocus(JComponent input) {
				return true;
			}
    });
		colObjects[3].setInputVerifier(new InputVerifier() {
      public boolean verify(JComponent input) {
				return Taxonomic.this.verifyTaxonRank(Taxonomic.this, ((JTextField)input).getText(), 3);
      }
			public boolean shouldYieldFocus(JComponent input) {
				return true;
			}
    });
		colObjects[4].setInputVerifier(new InputVerifier() {
      public boolean verify(JComponent input) {
				return Taxonomic.this.verifyTaxonName(Taxonomic.this, (JTextField)input, 4);
      }
			public boolean shouldYieldFocus(JComponent input) {
				return true;
			}
    });
		colObjects[5].setInputVerifier(new InputVerifier() {
      public boolean verify(JComponent input) {
				return Taxonomic.this.verifyCommonName(Taxonomic.this, (JTextField)input);
      }
			public boolean shouldYieldFocus(JComponent input) {
				return true;
			}
    });

		//colObjects[2].setInputVerifier(new TaxonNameVerifier(this, 2));
    //colObjects[3].setInputVerifier(new NewTaxonRankVerifier(this, 3));
    //colObjects[4].setInputVerifier(new TaxonNameVerifier(this, 4));
    //colObjects[5].setInputVerifier(new CommonNameVerifier(this));



    /*((JTextField)colObjects[1]).addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent fe){
        ((JTextField)fe.getComponent()).selectAll();
      }
      public void focusLost(FocusEvent fe) {
        boolean res = Taxonomic.this.verifyTaxonRank(Taxonomic.this, (JTextField)colObjects[1], 1);
				if(!res) {
					taxonList.selectAndEditCell(taxonList.getSelectedRowIndex(), 1);
				}
      }
    });

    ((JTextField)colObjects[3]).addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent fe){
        ((JTextField)fe.getComponent()).selectAll();
      }
      public void focusLost(FocusEvent fe) {
        boolean res = Taxonomic.this.verifyTaxonRank(Taxonomic.this, (JTextField)colObjects[3], 3);
				if(!res) {
					taxonList.selectAndEditCell(taxonList.getSelectedRowIndex(), 3);
				}
      }
    });

    ((JTextField)colObjects[2]).addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent fe){
        ((JTextField)fe.getComponent()).selectAll();
      }
      public void focusLost(FocusEvent fe) {
        boolean res = Taxonomic.this.verifyTaxonName(Taxonomic.this, (JTextField)colObjects[2], 2);
				if(!res) {
					taxonList.selectAndEditCell(taxonList.getSelectedRowIndex(), 2);
				}
      }
    });

    ((JTextField)colObjects[4]).addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent fe){
        ((JTextField)fe.getComponent()).selectAll();
      }
      public void focusLost(FocusEvent fe) {
        boolean res = Taxonomic.this.verifyTaxonName(Taxonomic.this, (JTextField)colObjects[4], 4);
				if(!res) {
					taxonList.selectAndEditCell(taxonList.getSelectedRowIndex(), 4);
				}
      }
    });

    ((JTextField)colObjects[5]).addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent fe){
        ((JTextField)fe.getComponent()).selectAll();
      }
      public void focusLost(FocusEvent fe) {
        boolean res = Taxonomic.this.verifyCommonName(Taxonomic.this, (JTextField)colObjects[5]);
				if(!res) {
					taxonList.selectAndEditCell(taxonList.getSelectedRowIndex(), 5);
				}
      }
    });*/

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

    JPanel taxonPanel = new JPanel(new BorderLayout());
    taxonPanel.setLayout(new BoxLayout(taxonPanel, BoxLayout.Y_AXIS));

    GUIAction action = new GUIAction(/*"Import Taxon Information from Data table..."*/ 
    								lan.getMessages("Taxonomic.ImportTaxon") +"...",
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

    taxonImportPanel = new TaxonImportPanel();
    boolean displayTable = taxonImportPanel.displayTable;
    taxonImportPanel = null;

    JButton importButton = new HyperlinkButton(action);
    importButton.setPreferredSize(UISettings.INIT_SCR_LINKBUTTON_DIMS);
    importButton.setMinimumSize(UISettings.INIT_SCR_LINKBUTTON_DIMS);
    importButton.setMaximumSize(UISettings.INIT_SCR_LINKBUTTON_DIMS);
    JLabel headLabel = null;
    if(displayTable == false){
      headLabel = WidgetFactory.makeHTMLLabel(headingNoImportTable, 6, false);
    } else {
      headLabel = WidgetFactory.makeHTMLLabel(heading, 3, false);
    }

    Box headPanel = Box.createVerticalBox();
    headPanel.add(headLabel);
    headPanel.add(Box.createVerticalGlue());

    if(displayTable){
      JPanel importPanel = new JPanel(new BorderLayout());
      importPanel.add(Box.createGlue(), BorderLayout.NORTH);
      importPanel.add(importButton, BorderLayout.WEST);
      importPanel.add(Box.createGlue(), BorderLayout.SOUTH);

      headPanel.add(importPanel);
      headPanel.add(Box.createVerticalGlue());
    }

    taxonPanel.setLayout(new BorderLayout());
    taxonPanel.add(taxonList, BorderLayout.CENTER);//, BorderLayout.CENTER);
    taxonPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));

    centerPanel.add(headPanel);
    centerPanel.add(WidgetFactory.makeDefaultSpacer());

    centerPanel.add(taxonPanel);
    centerPanel.add(WidgetFactory.makeDefaultSpacer());
    //////////////


    JPanel classTablePanel = WidgetFactory.makeVerticalPanel(-1);

    classTablePanel.add(WidgetFactory.makeHTMLLabel("<b>" 
    												+ /*"Classification System"*/ lan.getMessages("Taxonomic.ClassificationSystem") +" : "
    												+ " </b>" 
    												/*+ "If the list of taxa belong to one or more different classification systems, list the citations for those systems.",*/
    												+ lan.getMessages("Taxonomic.ClassificationSystemDesc"),
    												2, false));
    classTablePanel.add(Box.createVerticalGlue());
    JPanel classPanel = new JPanel(new BorderLayout());
    classList = WidgetFactory.makeList(classColNames, classEditors, -1, true, true, false, true, false, false);
    classPanel.add(classList, BorderLayout.CENTER);
    classPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    classTablePanel.add(classPanel);

    Action classAddAction = new AbstractAction (){
      public void actionPerformed(ActionEvent e) {

        classificationCitationAddAction();
      }
    };
    Action classEditAction = new AbstractAction (){
      public void actionPerformed(ActionEvent e) {

        classificationCitationEditAction();
      }
    };
    classList.setCustomAddAction(classAddAction);
    classList.setCustomEditAction(classEditAction);

    classTablePanel.setMaximumSize(new Dimension(2000, 150));
    classTablePanel.setPreferredSize(new Dimension(2000, 150));

    centerPanel.add(classTablePanel);
    centerPanel.add(WidgetFactory.makeDefaultSpacer());

  }

  private void classificationCitationAddAction() {

    CitationPage citationPage = new CitationPage();
    ModalDialog wpd = new ModalDialog(citationPage,
                                WizardContainerFrame.getDialogParent(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT);
    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {
      int rowNum = this.classList.getRowCount();
      List row = citationPage.getSurrogate();
      OrderedMap map = citationPage.getPageData("/classificationSystemCitation[1]");
      row.add(map);
      this.classList.addRow(row);

    }
		this.refreshCitationList();
  }

  private void classificationCitationEditAction() {

    int rowNum = this.classList.getSelectedRowIndex();
    if(rowNum < 0 || rowNum > this.classList.getRowCount())
      return;

    List row = this.classList.getSelectedRowList();
    OrderedMap map = (OrderedMap) row.get(3);
    OrderedMap copyMap = (OrderedMap)map.clone();

    CitationPage citationPage = new CitationPage();
    citationPage.setPageData(copyMap, "/classificationSystemCitation[1]");

    ModalDialog wpd = new ModalDialog(citationPage,
                                WizardContainerFrame.getDialogParent(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT);
    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {

			row = citationPage.getSurrogate();
      row.add(citationPage.getPageData("/classificationSystemCitation[1]"));
      this.classList.replaceSelectedRow(row);

    }
		this.refreshCitationList();
  }

  private void TaxonListAddAction() {

    List rows = taxonList.getListOfRowLists();
    List newRow = new ArrayList();
    TaxonHierarchy hier = null;
    if(rows.size() == 0) {
      newRow.add("");
      newRow.add("Genus"); newRow.add("");
      newRow.add("Species"); newRow.add("");
      newRow.add("");
      hier = new TaxonHierarchy();
    } else {
      int selIdx = taxonList.getSelectedRowIndex();
      if(selIdx < 0) selIdx = rows.size() - 1;
      List r = (List)rows.get(selIdx);
      Object o = r.get(6);
      if(o != null  && (o instanceof TaxonHierarchy)) {
        TaxonHierarchy hr = (TaxonHierarchy)o;
        hier = (TaxonHierarchy)hr.clone();
        int cnt = hier.getLevelCount();
        TaxonLevel parentLevel = hier.getTaxonAtLevel(cnt - 2);
        TaxonLevel currentLevel = hier.getTaxonAtLevel(cnt - 1);
        if(parentLevel != null) {
          parentLevel.setName("");
          parentLevel.setCommonNames(null);
          hier.setTaxonAtLevel(cnt - 2, parentLevel);
        }
        if(currentLevel != null) {
          currentLevel.setName("");
          currentLevel.setCommonNames(null);
          hier.setTaxonAtLevel(cnt - 1, currentLevel);
        }
        newRow.add((String)r.get(0));
        newRow.add((String)r.get(1)); newRow.add("");
        newRow.add((String)r.get(3)); newRow.add("");
        newRow.add("");
      } else {
        newRow.add(""); newRow.add(""); newRow.add("");
        newRow.add(""); newRow.add(""); newRow.add("");
      }
    }
    newRow.add(hier);
    taxonList.addRow(newRow);

  }


  private void TaxonListEditAction() {

    List row = taxonList.getSelectedRowList();
    selectedRowIdx = Taxonomic.this.taxonList.getSelectedRowIndex();
    if(row.size() < 7 ) return;
    Object o = row.get(6);
    if(o == null || !(o instanceof TaxonHierarchy)) return;
    final TaxonHierarchy hier = (TaxonHierarchy)o;

    String parent = ((String)row.get(0)).trim();
    Vector allTaxons = hier.getAllTaxons();
    TaxonHierarchy newHier = new TaxonHierarchy(new Vector());

    for(int i =0; i < allTaxons.size() - 2; i++)
      newHier.addTaxon((TaxonLevel)allTaxons.get(i));

    String rank1 = ((String)row.get(1)).trim();
    String level1 = ((String)row.get(2)).trim();
    String rank2 = ((String)row.get(3)).trim();
    String level2 = ((String)row.get(4)).trim();
    String cn2 = ((String)row.get(5)).trim();

    if(!rank1.equals("")) {
      TaxonLevel origLevel = hier.getTaxonAtRank(rank1);
      String[] commNames = null;
      if(origLevel != null) {
        commNames = origLevel.getCommonNames();
      }
      TaxonLevel tl1 = new TaxonLevel(rank1, level1, commNames);
      newHier.addTaxon(tl1);
    }
    if(!rank2.equals("")) {
      TaxonLevel tl2 = new TaxonLevel(rank2, level2, parseCommonNames(cn2));
      newHier.addTaxon(tl2);
    }

    final ParentTaxaPanel panel = new ParentTaxaPanel();
    panel.setHierarchy(newHier);

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

  private String[] parseCommonNames( String names) {

    List t = new ArrayList();
    StringTokenizer st = new StringTokenizer(names, ",");
    while(st.hasMoreTokens()) {
      t.add(st.nextToken().trim());
    }
    if(t.size() == 0)
      return null;
    String[] ret = new String[t.size()];
    ret = (String[])t.toArray(ret);
    return ret;
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
    newRow.add(panel.getHierarchy());
    taxonList.replaceSelectedRow(newRow);
    parentTaxaDialog.setVisible(false);
  }

   // needs work
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
      TaxonLevel tl = new TaxonLevel(rank, taxon, null);
      TaxonHierarchy hier = new TaxonHierarchy(new Vector());
      hier.addTaxon(tl);
      newRow.add(hier);
      taxonList.addRow(newRow);
    }

    importTaxaDialog.setVisible(false);
  }

  private void taxonImportCancelAction() {

    importTaxaDialog.setVisible(false);
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


  /**
  *  The action to be executed when the page is displayed. May be empty
  */
  public void onLoadAction() {

    // create the first row when the page is loaded, if there are no rows already
    if(this.taxonList.getRowCount() == 0) TaxonListAddAction();
		refreshCitationList();

  }

	private void refreshCitationList() {

		List rows = this.classList.getListOfRowLists();
		for(int i = 0; i < rows.size(); i++) {

			List row = (List)rows.get(i);
			if(row.size() < 4) continue;
			OrderedMap map = (OrderedMap) row.get(3);
			if(map == null || map.size() == 0) continue;
			OrderedMap copyMap = (OrderedMap)map.clone();
			CitationPage citationPage = new CitationPage();
			citationPage.setPageData(copyMap, "/classificationSystemCitation[1]");
			row = citationPage.getSurrogate();
			row.add(citationPage.getPageData("/classificationSystemCitation[1]"));
			this.classList.replaceRow(i, row);
		}
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

		taxonList.fireEditingStopped();
		taxonList.selectAndEditCell(0, 0);
    List rows = this.classList.getListOfRowLists();
    if(rows != null && rows.size() > 0){
    	
       if(checkCitation)
       {
	    	//make sure very classification has all required fields
	       for(int i=0; i<rows.size(); i++)
	       {
	    	    List nextRowList = (List)rows.get(i);
	    	   if (nextRowList.size() < 4)continue;
	    	     Object obj = nextRowList.get(3);
	    	     Log.debug(30, "object at cloumn 3 is "+obj.getClass().getName());
		        OrderedMap map = (OrderedMap)obj;
		         if (map == null)
		         {
		        	 continue;
		         }
		         boolean check = CitationPage.mapContainsRequirePath(map, "/classificationSystemCitation[1]");
		         if(check == false)
		         {
		        	 JOptionPane.showMessageDialog(Taxonomic.this, "The Classification system at row "+(i+1)+" misses some required fields. Please select it and use Edit button to edit it!", "Error", JOptionPane.ERROR_MESSAGE);
		             return false;
		         }
		         
	       }
       }

      // there must be some taxon data, since citations are there.

      List data = this.taxonList.getListOfRowLists();
      int len = data.size();
      if(len > 0) {
        for(int i = 0; i < len; i++) {
          List row = (List)data.get(i);
          if(row.size() < 7) continue;
          TaxonHierarchy th = (TaxonHierarchy)row.get(6);
          Vector taxonLevels = th.getAllTaxons();
          for(int j = 0; j < taxonLevels.size(); j++) {

            TaxonLevel level = (TaxonLevel)taxonLevels.get(j);
            String tRank = level.getRank();
            String tName = level.getName();
            if(tRank.trim().equals("") || tName.trim().equals("")) continue;
            else return true;
          }
        }
      }

      JOptionPane.showMessageDialog(
    		  Taxonomic.this, 
    		  /*"You must enter the Taxonomic information along with the Citation details!"*/ lan.getMessages("Taxonomic.MustEnterTaxonInfo")+"!", 
    		  /*"Error"*/ lan.getMessages("Error"),
    		  JOptionPane.ERROR_MESSAGE);
      return false;
    }
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

    return getPageData(xPathRoot);
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

    OrderedMap result = new OrderedMap();

    // adding the taxonomic System info

    List rows = this.classList.getListOfRowLists();
    if(rows != null && rows.size() > 0){

      for(int i = 0; i < rows.size(); i++) {

        List row = (List) rows.get(i);
        OrderedMap map = (OrderedMap)row.get(3);
        Iterator it = map.keySet().iterator();
        while(it.hasNext()) {
          String k = (String)it.next();
          String newKey = rootXPath + "/taxonomicSystem[1]/classificationSystem[" +(i+1)+ "]" +  k;
          result.put(newKey, (String)map.get(k));
        }

      }
      result.put(rootXPath + "/taxonomicSystem[1]/identifierName[1]/organizationName[1]", "Unknown");
      result.put(rootXPath + "/taxonomicSystem[1]/taxonomicProcedures[1]", "Unknown");

    }

    Iterator it1 = result.keySet().iterator();
    Log.debug(45, "OrderedMap returning from TaxonomicPage");
    while(it1.hasNext()) {
      String k = (String)it1.next();
      Log.debug(45, k + "--" + (String)result.get(k));
    }

    // now adding the taxonomic Classification info
    List data = this.taxonList.getListOfRowLists();
    int len = data.size();

    // Remove all the empty taxon levels...
    for(int i = len - 1; i > -1; i--) {
      List row = (List) data.get(i);
      TaxonHierarchy th = (TaxonHierarchy) row.get(6);
      Vector taxonLevels = th.getAllTaxons();
      boolean removeTaxonLevel = true;
      for (int j = 0; j < taxonLevels.size(); j++) {
        TaxonLevel level = (TaxonLevel) taxonLevels.get(j);
        String tRank = level.getRank();
        String tName = level.getName();
        if (tRank.trim().equals("") || tName.trim().equals(""))continue;
        else {
          removeTaxonLevel = false;
        }
      }
      if (removeTaxonLevel) {
        data.remove(i);
      }
    }

    len = data.size();
    List validHierarchies = new ArrayList();

    for(int i = 0; i < len; i++) {

			List row = (List)data.get(i);
			TaxonHierarchy th = (TaxonHierarchy)row.get(6);

			if(!th.isValidHierarchy()) {
				continue;
			}
			String rank1 = (String)row.get(1);
			if(rank1.trim().length() > 0) {
				if(!this.isValidTaxonName(rank1)) continue;
			}
			String rank2 = (String)row.get(3);
			if(rank2.trim().length() > 0) {
				if(!this.isValidTaxonName(rank2)) continue;
			}

			String name1 = (String)row.get(2);
			if(name1.trim().length() > 0) {
				if(!this.isValidTaxonName(name1)) continue;
			}
			String name2 = (String)row.get(4);
			if(name2.trim().length() > 0) {
				if(!this.isValidTaxonName(name2)) continue;
                                TaxonLevel tempLevel = th.getTaxonAtRank(rank2);
                                tempLevel.setName(name2);
			}
			String cn = (String)row.get(5);
			if(cn.trim().length() > 0) {
				if(!this.validateCommonNames(cn)) continue;
			}
			validHierarchies.add(th);

    }
		len = validHierarchies.size();
		TaxonHierarchy[] hierarchies = new TaxonHierarchy[len];
		for(int i = 0; i < len; i++)
			hierarchies[i] = (TaxonHierarchy)validHierarchies.get(i);

		//hierarchies = (TaxonHierarchy[])validHierarchies.toArray(hierarchies);

    Arrays.sort(hierarchies);

    String lastAddedPrefix = "";
    String prefix = rootXPath + "/taxonomicClassification[1]";
    if(len > 0) {
      Vector taxonLevels = hierarchies[0].getAllTaxons();
			for(int i = 0; i < taxonLevels.size(); i++) {

        TaxonLevel level = (TaxonLevel)taxonLevels.get(i);
        String tRank = level.getRank();
        String tName = level.getName();
        //if(tRank.trim().equals("") || tName.trim().equals("")) continue;
        if(Util.isBlank(tRank)|| Util.isBlank(tName)) continue;

        result.put(prefix + "/taxonRankName",  tRank);
        result.put(prefix + "/taxonRankValue",  tName);
        String[] cn = level.getCommonNames();
        for(int j = 0; cn != null && j < cn.length; j++) {
          if(Util.isBlank(cn[j])) continue;
          result.put(prefix + "/commonName[" + (j + 1) +"]", cn[j]);
        }
        lastAddedPrefix = prefix;
        if(i < (taxonLevels.size() - 1)) {
          prefix += "/taxonomicClassification[1]";
        }
      }

      TaxonHierarchy prevHier = null;

      if(!result.isEmpty()){
        prevHier = hierarchies[0];
      }

      for(int i = 1; i < len; i++) {
        int cmp = -1;
        if(prevHier != null){
          cmp = hierarchies[i].compareTo(prevHier);
          if (cmp == 0)continue;
        }

        if(cmp < 0) {
          Log.debug(10, "Error in getPageData! Sorting not correct");
        } else {

          prevHier = hierarchies[i];
          int start = 0;
          int idx = -1;
        //  while(temp < cmp) {
            idx = lastAddedPrefix.indexOf("taxonomicClassification", start);
         //   if(idx == -1) break;
            start = idx + new String("taxonomicClassification").length();
        //  }

          String currPrefix = lastAddedPrefix.substring(0, start);

          int closingBrace = lastAddedPrefix.indexOf("]", start);
          int currPosition = 0;

          if(closingBrace < (start +1)) {
            Log.debug(25, "Error - cldnt get index");
          } else {
            String pos = lastAddedPrefix.substring(start +1, closingBrace);
            try {
              currPosition = Integer.parseInt(pos);
            } catch(Exception e) {
              Log.debug(45, "not a number - " + pos);
            }
          }
          currPosition++;

          currPrefix += "[" + currPosition + "]";

          Vector newTaxonLevels = hierarchies[i].getAllTaxons();
          for(int k = 0; k < newTaxonLevels.size(); k++) {

            TaxonLevel level = (TaxonLevel)newTaxonLevels.get(k);
            String tRank = level.getRank();
            String tName = level.getName();
            //if(tRank.trim().equals("") || tName.trim().equals("")) continue;
            if(Util.isBlank(tRank)|| Util.isBlank(tName)) continue;
            
            result.put(currPrefix + "/taxonRankName",  tRank);
            result.put(currPrefix + "/taxonRankValue",  tName);
            String[] cn = level.getCommonNames();
            for(int j = 0; cn != null && j < cn.length; j++) {
              if(Util.isBlank(cn[j])) continue;
              result.put(currPrefix + "/commonName[" + (j + 1) +"]", cn[j]);
            }
            lastAddedPrefix = currPrefix;
            if(k < (newTaxonLevels.size() - 1)) {
              currPrefix += "/taxonomicClassification[1]";
            }
          }
        }
      }
    }



    return result;

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
   * sets the fields in the waird page using the Map object that contains all
   * the key/value paired
   *
   * @param data the Map object that contains all the key/value paired settings
   *   for this particular wizard page
   * @param _xPathRoot String
   * @return boolean
   */
	 // _xPathRoot *should* end in "taxonomicCoverage"

  public boolean setPageData(OrderedMap data, String _xPathRoot) {

    //this.taxonList.removeAllRows();
    Node covRoot;
    boolean result = true;

    if (data==null || data.isEmpty()) return true;

    data.remove(_xPathRoot + "/@scope");
    data.remove(_xPathRoot + "/@id");

    try {
      DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
      Document doc = impl.createDocument("", "taxonomicCoverage", null);

      covRoot = doc.getDocumentElement();
      XMLUtilities.getXPathMapAsDOMTree(data, covRoot);
      result = traverseTree(covRoot, new TaxonHierarchy(new Vector()));
      if(!result) return false;
      else {
        removeAllKeysStartingWith(_xPathRoot + "/taxonomicClassification", data);
      }
    } catch(Exception e) {

      Log.debug(10, "Invalid orderedmap - " + e);
      return false;
    }



    int size = data.keySet().size();
    String[] keys = new String[size];
    keys = (String[])data.keySet().toArray(keys);
    int pos = 1;
    for(int cnt = 0; cnt < size; cnt++) {

      String key = keys[cnt];
      int idx = key.indexOf("/taxonomicSystem[1]/classificationSystem[" + pos + "]");
      if( idx > -1) {

        OrderedMap map = new OrderedMap();
        OrderedMap subMap = new OrderedMap();

        int idx2 = key.substring(idx).indexOf("/classificationSystemCitation");
        map.put(key.substring(idx + idx2), data.get(key));
        subMap.put(key, data.get(key));

        cnt++;
        for(;cnt < size; cnt++) {

          key = keys[cnt];
          int newidx = key.indexOf("/taxonomicSystem[1]/classificationSystem[" + pos + "]");
          if( newidx > -1) {
            int citidx = key.substring(newidx).indexOf("/classificationSystemCitation");
            map.put(key.substring(citidx + newidx), data.get(key));
            subMap.put(key, data.get(key));
          } else {
            cnt--;
            break;
          }
        }

        OrderedMap map1 = (OrderedMap)map.clone();
        CitationPage cpage = new CitationPage();
        boolean flag = cpage.setPageData(map, "/classificationSystemCitation[1]");
        if(!flag) {
          return false;
        }
        List row = cpage.getSurrogate();
        if(row.size() == 0)
          break;
        row.add(map1);
        data.removeAll(subMap);
        this.classList.addRow(row);
        pos++;
      } else if (key.indexOf("identifierName") > -1) {
        data.remove(key);
      } else if (key.indexOf("taxonomicProcedures") > -1) {
        data.remove(key);
      }

    }

    this.taxonList.editCellAt(0, 2);

    if(data.keySet().size() > 0) {
			return false;
		}
    else return true;
  }


  private boolean traverseTree(Node root, TaxonHierarchy hier) {

    boolean toReturn = true;
    if (root == null) {
      return true;
    }

    Log.debug(40, "Traversing - " + root.getNodeName());

    NodeList children = root.getChildNodes();
    if (children.getLength() == 0) {
      Log.debug(40, "no children - returning true");
      return true;
    }

    TaxonLevel level = null;
    boolean middleNodePresent = false;
    boolean endNodePresent = false;
    for (int i = 0; i < children.getLength(); i++) {

      Node child = children.item(i);
      String name = child.getNodeName();
      if (name.equals("taxonomicClassification")) {
        traverseTree(child, (TaxonHierarchy)hier.clone());
        middleNodePresent = true;
      }

      else if (name.equals("taxonRankName")) {
        String rankName = child.getFirstChild().getNodeValue();
        String rankVal = "";
        Node value = child.getNextSibling();
        if (value != null && value.getNodeName().equals("taxonRankValue")) {
          rankVal = value.getFirstChild().getNodeValue();
          i++;
        }
        String cns[] = null;
        Node curr = value;
        List cn = new ArrayList();
        int cnt = 0;
        while (true) {
          Node cnNode = curr.getNextSibling();
          if (cnNode != null && cnNode.getNodeName().equals("commonName")) {

            String val = cnNode.getFirstChild().getNodeValue();
            cn.add(val);
            cnt++;
            i++;
          } else {
            break;
          }
          curr = cnNode;
        }
        if (cnt > 0) {
          cns = new String[cnt];
          cns = (String[])cn.toArray(cns);
        }
        level = new TaxonLevel(rankName, rankVal, cns);
        hier.addTaxon(level);
        endNodePresent = true;

      } else if (name.equals("taxonomicSystem")) {

      } else {
        toReturn = false;
      }

    }

    if ((!middleNodePresent) && endNodePresent) {
			/*int hlen = hier.getLevelCount();
			TaxonLevel llevel = hier.getTaxonAtLevel(hlen - 1);
			String lrank = llevel.getRank();
			int rankIndex = WizardSettings.getIndexOfTaxonRank(lrank);
      if(rankIndex != WizardSettings.NUMBER_OF_TAXON_RANKS - 1) {
				hier.addTaxon(new TaxonLevel("", "", new String[0]));
			}*/
      ParentTaxaPanel panel = new ParentTaxaPanel();
      panel.setHierarchy(hier);
      List list = panel.getSurrogate();
      list.add(hier);
      this.taxonList.addRow(list);

    }
    return toReturn;

  }

  private void removeAllKeysStartingWith(String path, OrderedMap map) {

		Iterator it = map.keySet().iterator();
    while(it.hasNext()) {

      String key = (String)it.next();
      if(key.startsWith(path))  {
				it.remove();
			}
    }
    return;
  }

  private int compareStrings(String first, String sec) {

    int len1 = first.length();
    int len2 = sec.length();
    int i;
    for( i = 0; i < len1; i++) {

      if(i >= len2) {

        return i;
      }
      char f = first.charAt(i);
      char s = sec.charAt(i);
      if(f < s) {
        return (-i);
      } else if(f > s) {
        return i;
      }

    }
    if(i < len2) {
      return -i;
    }
    return 0;
  }

  private TaxonLevel getTaxonLevel(OrderedMap map, String currPrefix) {

    String rank = (String)map.get(currPrefix + "/taxonRank");
    if(rank == null || rank.trim().equals(""))
      return null;
    String name = (String)map.get(currPrefix + "/taxonName");
    if(name == null) name = "";
    List commonNames = new ArrayList();
    int cnIdx = 1;
    while(true) {

      String cn = (String)map.get(currPrefix + "/commonName[" + cnIdx + "]");
      if(cn == null) break;
      commonNames.add(cn);
      cnIdx++;
    }
    String cns[] = null;
    if(cnIdx > 1) {

      cns = new String[cnIdx - 1];
      cns = (String[])commonNames.toArray(cns);
    }

    TaxonLevel level = new TaxonLevel(rank, name, cns);
    return level;
  }

  //////////////////////////////////////////////////////
  //InputVerifiers for the TextField in the CustomList
  //////////////////////////////////////////////////////


  private static int validateTaxonCounter = 0;
  private static int commonNameCounter = 0;
  private static int taxonNameCounter = 0;

  public boolean verifyTaxonRank(Taxonomic parent, String newText, int pos) {

    if(validateTaxonCounter > 0) {
      return true;
    }
    validateTaxonCounter++;
    boolean error = false;


    int rowIdx = Taxonomic.this.taxonList.getSelectedRowIndex();

    if(newText == null) {
      validateTaxonCounter--;
      return true;
    }
		if(!isValidTaxonName(newText)) {
      JOptionPane.showMessageDialog(Taxonomic.this, "Invalid characters in the taxon rank. Only letters and spaces are allowed.", "Error", JOptionPane.ERROR_MESSAGE);
      validateTaxonCounter--;
      //Taxonomic.this.taxonList.editCellAt(rowIdx, pos);
      int[] selRows = new int[]{rowIdx};
      Taxonomic.this.taxonList.setSelectedRows(selRows);
      return false;
    }

    List selRow = taxonList.getSelectedRowList();
    TaxonHierarchy hier = null;
    if(selRow.size() > 5)
      hier = (TaxonHierarchy)selRow.get(6);

		if(hier == null) return true;

    int currcnt = hier.getLevelCount();
    if(pos == 1) {

      String nextRank = (String) selRow.get(3);
      TaxonLevel currLevel = null;
      int mypos  = currcnt - 2;
      if(nextRank.trim().equals("")) {
        mypos = currcnt - 1;
      } else {
        mypos = currcnt - 2;
      }
      currLevel = hier.getTaxonAtLevel(mypos);
			if(currLevel == null) {
				currLevel = new TaxonLevel(newText, "", new String[0]);
				hier.insertTaxonAtLevel(0, currLevel);
			} else {
				currLevel.setRank(newText);
			}
      if(newText.trim().equals("")) {
				 //hier.removeTaxon(currLevel);
      }

    } else if(pos == 3) {

			int parentLen = 0;
			String cn[]= parseCommonNames((String)selRow.get(0));
			if(cn != null) parentLen = cn.length;
			String prevRank = (String)selRow.get(1);
			if(prevRank.trim().length() > 0) parentLen++;
			if(currcnt == parentLen) {
				hier.addTaxon(new TaxonLevel("", "", new String[0]));
				currcnt = hier.getLevelCount();
			}
      int mypos = currcnt - 1;
      TaxonLevel currLevel = hier.getTaxonAtLevel(mypos);
      currLevel.setRank(newText);
      if(newText.trim().equals("")) {
				//hier.removeTaxon(currLevel);
			}

    }

    if(! hier.isValidHierarchy() ) {

      JOptionPane.showMessageDialog(Taxonomic.this, "Error in the entry! The entered taxon rank is already present in the taxonomic hierarchy", "Error", JOptionPane.ERROR_MESSAGE);
      int idx = Taxonomic.this.taxonList.getSelectedRowIndex();
      //Taxonomic.this.taxonList.editCellAt(idx, pos);
      //jc.requestFocus();
      validateTaxonCounter--;
      int[] selRows = new int[]{idx};
      //Taxonomic.this.taxonList.setSelectedRows(selRows);
      return false;
    }
    List data = taxonList.getListOfRowLists();
    Iterator it = data.iterator();
    int cnt = -1;
    while(it.hasNext()) {

      List row = (List)it.next();
      cnt++;
      if(cnt == rowIdx)
        continue;
      if(row.size() < 6)
        continue;
      TaxonHierarchy tr = (TaxonHierarchy)row.get(6);
      int res = hier.compareTo(tr);
      if(res == 0) {

        JOptionPane.showMessageDialog(Taxonomic.this, "Error in the entry! The entered taxonomic hierarchy is already present in the list", "Error", JOptionPane.ERROR_MESSAGE);
        int idx = Taxonomic.this.taxonList.getSelectedRowIndex();
        //Taxonomic.this.taxonList.editCellAt(idx, pos);
        //jc.requestFocus();
        validateTaxonCounter--;
        int[] selRows = new int[]{idx};
        Taxonomic.this.taxonList.setSelectedRows(selRows);
        return false;
      }
    }

    validateTaxonCounter--;
    return true;
  }

  public boolean verifyCommonName(Taxonomic parent, JTextField jc) {

    if(commonNameCounter > 0) {
      return true;
    }
    commonNameCounter++;
    String newName = ((JTextField)jc).getText();
    if(!validateCommonNames(newName)) {
      JOptionPane.showMessageDialog(parent, "Invalid characters in the common name(s). Only letters, digits and spaces are allowed. Common names are seperated by a comma", "Error", JOptionPane.ERROR_MESSAGE);
      int rowIdx = Taxonomic.this.taxonList.getSelectedRowIndex();
      //Taxonomic.this.taxonList.editCellAt(rowIdx, 5);
      //jc.requestFocus();
      commonNameCounter--;
      int[] selRows = new int[]{rowIdx};
      Taxonomic.this.taxonList.setSelectedRows(selRows);
      return false;
    }
    List row = (List)Taxonomic.this.taxonList.getSelectedRowList();
    TaxonHierarchy currHier = (TaxonHierarchy) row.get(6);
    int currcount = currHier.getLevelCount();

    TaxonLevel currLevel = currHier.getTaxonAtLevel(currcount - 1);
    String[] cns = this.parseCommonNames(newName);
    currLevel.setCommonNames(cns);
    commonNameCounter--;
    return true;
  }

  private boolean validateCommonNames(String text) {

    char arr[] = text.toCharArray();
    for(int i =0; i < arr.length; i++) {
      if(arr[i] == ',' || Character.isLetterOrDigit(arr[i]) || Character.isSpaceChar(arr[i]))
        continue;
      return false;
    }
    return true;
  }

  public boolean verifyTaxonName(Taxonomic parent, JTextField jc, int pos) {

    if(taxonNameCounter > 0) {
      return true;
    }
    taxonNameCounter++;
    int rowIdx = Taxonomic.this.taxonList.getSelectedRowIndex();
    if(rowIdx < 0){
      rowIdx = selectedRowIdx;
    }

    String newName = ((JTextField)jc).getText();
    if(!isValidTaxonName(newName)) {
      JOptionPane.showMessageDialog(parent, "Invalid characters in the taxon name. Only letters, digits and spaces are allowed.", "Error", JOptionPane.ERROR_MESSAGE);

      //Taxonomic.this.taxonList.editCellAt(rowIdx, pos);
      //jc.requestFocus();
      int[] selRows = new int[]{rowIdx};
      Taxonomic.this.taxonList.setSelectedRows(selRows);
      taxonNameCounter--;
      return false;
    }

    List row = Taxonomic.this.taxonList.getSelectedRowList();
    if(row == null){
      row = (List)Taxonomic.this.taxonList.getListOfRowLists().get(selectedRowIdx);
    }
    TaxonHierarchy currHier = (TaxonHierarchy)row.get(6);
    int currcount = currHier.getLevelCount();

    if(pos == 2) {
      String r2 = (String)row.get(3);
      TaxonLevel currLevel = null;
      if(r2.trim().equals("")) { // next taxon is empty, meaning this is last taxon
        currLevel = currHier.getTaxonAtLevel(currcount - 1);
      } else {
        if(currcount > 1)
          currLevel = currHier.getTaxonAtLevel(currcount - 2);
          else {
            currLevel = new TaxonLevel("", newName, null);
            currHier.insertTaxonAtLevel(0, currLevel);
          }
      }
      if(currLevel != null)
        currLevel.setName(newName);

    } else if(pos == 4) {

      TaxonLevel currLevel = currHier.getTaxonAtLevel(currcount - 1);
      currLevel.setName(newName);
    }


    // check if the current hierarchy is a duplicate
    List data = taxonList.getListOfRowLists();
    Iterator it = data.iterator();
    int cnt = -1;
    while(it.hasNext()) {

      List currRow = (List)it.next();
      cnt++;
      if(cnt == rowIdx)
        continue;
      if(currRow.size() < 6)
        continue;
      TaxonHierarchy tr = (TaxonHierarchy)currRow.get(6);
      int res = currHier.compareTo(tr);
      if(res == 0) {

        JOptionPane.showMessageDialog(Taxonomic.this, "Error in the entry! The entered taxonomic hierarchy is already present in the list", "Error", JOptionPane.ERROR_MESSAGE);
        //Taxonomic.this.taxonList.editCellAt(rowIdx, pos);
        //jc.requestFocus();
        taxonNameCounter--;
        int[] selRows = new int[]{rowIdx};
        Taxonomic.this.taxonList.setSelectedRows(selRows);
        return false;

      }
    }

    List allRows = Taxonomic.this.taxonList.getListOfRowLists();
    for(int i = 0; i < allRows.size(); i++) {

      if(i == rowIdx) continue;
      List currRow = (List)allRows.get(i);
      TaxonHierarchy hier = (TaxonHierarchy)currRow.get(6);
      if(hier == null || hier.getLevelCount() == 0) continue;
      TaxonHierarchy parentHier = hier.getParentsOfName(newName);
      if(parentHier == null) continue;
      if(pos == 2) {


      }
    }


    taxonNameCounter--;
    return true;
  }

  private boolean isValidTaxonName(String text) {

    char arr[] = text.toCharArray();
    for(int i =0; i < arr.length; i++) {
      if(Character.isLetterOrDigit(arr[i]) || Character.isSpaceChar(arr[i]))
        continue;
      return false;
    }
    return true;
  }


}


class ParentTaxaPanel extends JPanel implements WizardPageSubPanelAPI{
	
    /**
     *Import Language into Morpho
     *by pstango 2010/03/15 
     */
    public static Language lan = new Language();		

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

    headLabel = WidgetFactory.makeLabel(
      /*"Enter the Taxonomic Hierarchy (in descending order):"*/ lan.getMessages("Taxonomic.headLabel") +" : ",
         false,
      WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    JPanel labelPanel = new JPanel(new BorderLayout());
    labelPanel.add(headLabel, BorderLayout.CENTER);

    String colNames2 []= new String[] {/*"Rank"*/ lan.getMessages("Taxonomic.Rank"),
			   							/*"Name"*/ lan.getMessages("Name"),
			   							/*"Common Name(s)"*/ lan.getMessages("Taxonomic.CommonName(s)")
    								   };
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

  public void setHierarchy(TaxonHierarchy hier) {

    Iterator it = hier.iterator();
    while(it.hasNext()) {

      TaxonLevel level = (TaxonLevel)it.next();
      String rank = level.getRank();
      if(rank.trim().equals(""))
        continue;
      String name = level.getName();
      String commonName = level.getCommonNamesDisplayString();

      List row = new ArrayList();
      row.add(rank);
      row.add(name);
      row.add(commonName);
      hierList.addRow(row);
    }

  }

  public TaxonHierarchy getHierarchy() {

    Vector vec = new Vector();
    List rows = this.hierList.getListOfRowLists();
    Iterator it = rows.iterator();
    while(it.hasNext()) {

      List row = (List) it.next();
      String rank = (String)row.get(0);
      String name = (String)row.get(1);
      String[] commonNames = null;
      String cn = (String)row.get(2);
      if(cn != null)
        commonNames = parseCommonNames(cn);

      TaxonLevel level = new TaxonLevel(rank, name, commonNames);
      vec.add(level);
    }
    TaxonHierarchy hier = new TaxonHierarchy(vec);
    return hier;
  }

  private String[] parseCommonNames( String names) {

    List t = new ArrayList();
    StringTokenizer st = new StringTokenizer(names, ",");
    while(st.hasMoreTokens()) {
      t.add(st.nextToken().trim());
    }
    if(t.size() == 0)
      return null;
    String[] ret = new String[t.size()];
    ret = (String[])t.toArray(ret);
    return ret;
  }

  public void setHierarchy1(List row) {

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

    boolean dataPresent = false;
    List ranks = new ArrayList();
    List rows = hierList.getListOfRowLists();
    Iterator it = rows.iterator();
    while(it.hasNext()) {

      List row = (List)it.next();
      String t = (String)row.get(0);
      String t1 = (String)row.get(1);
      if(ranks.contains(t)) {
        JOptionPane.showMessageDialog(this, "Invalid hierarchy! Two taxonomic levels cannot have the same taxon rank", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      ranks.add(t);
      if((!t.trim().equals("")) && (!t1.trim().equals("")))
        dataPresent = true;
    }

    if(!dataPresent) {
      JOptionPane.showMessageDialog(this, /*"Atleast one taxonomic level must be defined"*/ lan.getMessages("Taxonomic.Error_1") + "!",
    		  							  /*"Error"*/ lan.getMessages("Error"),
    		  							  JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // remove unnecessary rows
    short[] conditions = new short[] {CustomList.EMPTY_STRING_TRIM, CustomList.EMPTY_STRING_TRIM, CustomList.IGNORE };
    hierList.deleteEmptyRows(CustomList.OR, conditions);

    if (hierList.getRowCount() == 0)
      return false;

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


class TaxonLevel implements Comparable {

  private String name = "";
  private String rank = "";
  private String[] commonNames = null;

  TaxonLevel(String rank, String name, String[] commonNames) {

    this.name = name;
    this.rank = rank;
    this.commonNames = commonNames;
  }

  public int compareTo(Object o) {

    if(!(o instanceof TaxonLevel))
      return -1;
    TaxonLevel t = (TaxonLevel)o;
    String s1 = rank + "=" + name;
    String s2 = t.rank + "=" + t.name;
    int res = s1.compareTo(s2);
    return res;
  }

  public boolean equals(Object o) {

    // compare only ranks
    if(!(o instanceof TaxonLevel))
      return false;
    TaxonLevel t = (TaxonLevel)o;
    if(t.rank.equals(this.rank))
      return true;
    else
      return false;
  }

  public String getRank() {
    return this.rank;
  }

  public String getName() {
    return this.name;
  }

  public String[] getCommonNames() {
    return this.commonNames;
  }

  public void setRank(String rank) {
    this.rank = rank;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCommonNames(String[] cn) {
    this.commonNames = cn;
  }

  public String getCommonNamesDisplayString() {

    if(this.commonNames == null || commonNames.length < 1)
      return "";

    String ret = commonNames[0];
    for(int i = 1; i < commonNames.length; i++)
      ret += (", " + commonNames[i]);

    return ret;
  }

  public String toString() {

    String ret = "";
    ret += (rank + "=" + name);

    if(commonNames != null && commonNames.length > 0)
      ret += ( " (" + commonNames[0] + ")" );

    return ret;
  }

  public Object clone() {

    String newArr[] = null;
    if(commonNames != null) {
      newArr = new String[commonNames.length];
      for(int i = 0; i < commonNames.length; i++)
        newArr[i] = commonNames[i];
    }
    return new TaxonLevel(rank, name, newArr);
  }

}

class TaxonHierarchy implements Comparable {

  private Vector taxonLevels;

  TaxonHierarchy() {

    this.taxonLevels = new Vector();
    for(int i = 0; i < WizardSettings.commonTaxonRanks.length; i++) {
      TaxonLevel level = new TaxonLevel(WizardSettings.commonTaxonRanks[i], "", null);
      taxonLevels.add(level);
    }
  }

  TaxonHierarchy(TaxonLevel[] levels) {

    this.taxonLevels = new Vector();
    for(int i = 0; i < levels.length; i++)
      taxonLevels.add(levels[i]);
  }

  TaxonHierarchy(Vector levels) {

    this.taxonLevels = levels;
  }

  public void addTaxon(TaxonLevel level) {

    if(level == null)
      return;
    if(taxonLevels == null)
      taxonLevels = new Vector();
    taxonLevels.add(level);
  }

  public void insertTaxonAtLevel(int pos, TaxonLevel level) {

    if(level == null)
      return;
    if(taxonLevels == null)
      taxonLevels = new Vector();
    if(pos < 0)
      return;
    if(pos >= taxonLevels.size())
      taxonLevels.add(level);
    else
      taxonLevels.add(pos, level);
  }

  public TaxonLevel getTaxonAtLevel(int level) {

    if(level < 0 || level >= this.taxonLevels.size())
      return null;
    return (TaxonLevel)taxonLevels.get(level);
  }

  public void setTaxonAtLevel(int level, TaxonLevel taxon) {

    if(level < 0 || level >= this.taxonLevels.size() || taxon == null)
      return;
    this.taxonLevels.set(level, taxon);
    return;
  }

  public int getLevelCount() {

    return taxonLevels.size();
  }

  public void removeTaxon(TaxonLevel level) {

    taxonLevels.remove(level);
  }

  public TaxonLevel getTaxonAtRank(String rank) {

    for(int i = 0; i < taxonLevels.size(); i++) {

      TaxonLevel level = (TaxonLevel)taxonLevels.get(i);
      String cr = level.getRank();
      if(cr != null && cr.equals(rank))
        return level;
    }
    return null;
  }

  public TaxonHierarchy getParentsOfName(String name) {

    for(int i = 0; i < taxonLevels.size(); i++) {

      TaxonLevel level = (TaxonLevel)taxonLevels.get(i);
      String cn = level.getName();
      if(cn != null && cn.equals(name)) {

        TaxonHierarchy result = new TaxonHierarchy(new Vector());
        for(int j = 0; j <= i; j++)
          result.addTaxon((TaxonLevel)taxonLevels.get(j));
        return result;
      }
    }
    return null;
  }

  public Vector getAllTaxons() {

    return this.taxonLevels;
  }

  public int compareTo(Object o) {

    if(!(o instanceof TaxonHierarchy))
      return -1;
    TaxonHierarchy th = (TaxonHierarchy)o;
    int i;
    for(i = 0; i < taxonLevels.size(); i++) {

      if (i >= th.taxonLevels.size())
        return (i+1);
      TaxonLevel t1 = (TaxonLevel)taxonLevels.get(i);
      TaxonLevel t2 = (TaxonLevel)th.taxonLevels.get(i);
      int c = t1.compareTo(t2);
      if(c > 0)
        return (i+1);
      else if (c < 0)
        return (-(i+1));

    }
    if (taxonLevels.size() == th.taxonLevels.size())
      return 0;
    return (-(i+1));
  }

  public boolean isValidHierarchy() {

    if(taxonLevels.size() < 2)
      return true;
    Vector temp = new Vector();
		for(int i = 0; i < taxonLevels.size(); i++) {
      TaxonLevel level = (TaxonLevel)taxonLevels.get(i);
			if(temp.contains(level))
        return false;
      temp.add(level);
    }
    return true;
  }


  public String toString() {

    String ret = "";
    for(int i = 0; i < this.taxonLevels.size(); i++) {

      TaxonLevel l = (TaxonLevel)this.taxonLevels.get(i);
      for(int j = 0; j < i;j++)
        ret += "\t";
      ret += "--";
      ret += l.toString();
      ret += '\n';
    }
    return ret;
  }

  public Iterator iterator() {

    return this.taxonLevels.iterator();
  }


  public Object clone() {

    Vector newV = new Vector();
    for(int i = 0;i < taxonLevels.size(); i++)
      newV.add(((TaxonLevel)taxonLevels.get(i)).clone());
    return new TaxonHierarchy(newV);
  }
}



