/**
 *  '$RCSfile: CitationPage.java,v $'
 *    Purpose: A class that handles display of Citation Information
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-04-05 22:04:31 $'
 * '$Revision: 1.4 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;


public class CitationPage extends AbstractUIPage {

  private final String pageID     = DataPackageWizardInterface.CITATION_PAGE;
  private final String nextPageID = "";
  private final String pageNumber = "";
  private final String title      = "Citation Page";
  private final String subtitle   = "";

	private JLabel titleLabel;
  private JTextField titleField;
	
	private JLabel salutationLabel;
  private JTextField salutationField;
	private JLabel firstNameLabel;
  private JTextField firstNameField;
  private JLabel lastNameLabel;
  private JTextField lastNameField;
  private JLabel organizationLabel;
  private JTextField organizationField;
  private JLabel positionNameLabel;
	private JTextField positionNameField;
  private JPanel warningPanel;
  private JLabel warningLabel;
  
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
  private final String[] typeDisplayNames = new String[3];

  // these must correspond to indices of measScaleElemNames array
	
	private static String citationType;
  public static final int CITATIONTYPE_BOOK  = 0;
  public static final int CITATIONTYPE_ARTICLE  = 1;
  public static final int CITATIONTYPE_REPORT = 2;
	
	private static final int BORDERED_PANEL_TOT_ROWS = 5;
  
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
    //salutationPanel.setBorder(new javax.swing.border.EmptyBorder(0,
       // 12 * WizardSettings.PADDING,
        //0, 8 * WizardSettings.PADDING));
    topMiddlePanel.add(titlePanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());

		// Salutation
    JPanel salutationPanel = WidgetFactory.makePanel(1);
    salutationPanel.add(WidgetFactory.makeLabel("Salutation:", false));
    salutationField = WidgetFactory.makeOneLineTextField();
    salutationPanel.add(salutationField);
    //salutationPanel.setBorder(new javax.swing.border.EmptyBorder(0,
       // 12 * WizardSettings.PADDING,
        //0, 8 * WizardSettings.PADDING));
    topMiddlePanel.add(salutationPanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());

    // First Name
    JPanel firstNamePanel = WidgetFactory.makePanel(1);
    firstNamePanel.add(WidgetFactory.makeLabel("First Name:", false));
    firstNameField = WidgetFactory.makeOneLineTextField();
    firstNamePanel.add(firstNameField);
    //firstNamePanel.setBorder(new javax.swing.border.EmptyBorder(0,
      //  12 * WizardSettings.PADDING,
       // 0, 8 * WizardSettings.PADDING));
    topMiddlePanel.add(firstNamePanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());
		
		// Last Name
    JPanel lastNamePanel = WidgetFactory.makePanel(1);
    lastNameLabel = WidgetFactory.makeLabel("Last Name:", true);
    lastNamePanel.add(lastNameLabel);
    lastNameField = WidgetFactory.makeOneLineTextField();
    lastNamePanel.add(lastNameField);
    //lastNamePanel.setBorder(new javax.swing.border.EmptyBorder(0, 0,
      //  0, 8 * WizardSettings.PADDING));
    topMiddlePanel.add(lastNamePanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());
		
		// Organization
    JPanel organizationPanel = WidgetFactory.makePanel(1);
    organizationLabel = WidgetFactory.makeLabel("Organization:", true);
    organizationPanel.add(organizationLabel);
    organizationField = WidgetFactory.makeOneLineTextField();
    organizationPanel.add(organizationField);
    //organizationPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0,
      //  0, 8 * WizardSettings.PADDING));
    topMiddlePanel.add(organizationPanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());
		
    // Position Name
    JPanel positionNamePanel = WidgetFactory.makePanel(1);
    positionNameLabel = WidgetFactory.makeLabel("Position:", true);
    positionNamePanel.add(positionNameLabel);
    positionNameField = WidgetFactory.makeOneLineTextField();
    positionNamePanel.add(positionNameField);
    topMiddlePanel.add(positionNamePanel);
    topMiddlePanel.add(WidgetFactory.makeHalfSpacer());
		
		// Pub Date
    JPanel pubDatePanel = WidgetFactory.makePanel(1);
    pubDatePanel.add(WidgetFactory.makeLabel("Pubication Date:", false));
    pubDateField = WidgetFactory.makeOneLineTextField();
    pubDatePanel.add(pubDateField);
    //salutationPanel.setBorder(new javax.swing.border.EmptyBorder(0,
       // 12 * WizardSettings.PADDING,
        //0, 8 * WizardSettings.PADDING));
    topMiddlePanel.add(pubDatePanel);
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
		
    citationTypeLabel = WidgetFactory.makeLabel(
                                //"Select and define a Measurement Scale:"
                                "Category:", true,
                                WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		
    radioPanel = WidgetFactory.makeRadioPanel(typeElemNames, -1, listener);
    JPanel outerRadioPanel = new JPanel();
    outerRadioPanel.setLayout(new BoxLayout(outerRadioPanel, BoxLayout.X_AXIS));
    outerRadioPanel.add(citationTypeLabel);
    outerRadioPanel.add(radioPanel);

    topMiddlePanel.add(outerRadioPanel);

    /////////////////////////////////////////////////////

    middlePanel.add(topMiddlePanel);

    currentPanel  = getEmptyPanel();

    middlePanel.add(currentPanel);

    middlePanel.add(Box.createGlue());

    topMiddlePanel.setMaximumSize(topMiddlePanel.getPreferredSize());
    topMiddlePanel.setMinimumSize(topMiddlePanel.getPreferredSize());

    bookPanel  = getBookPanel();
    articlePanel  = getArticlePanel();
    reportPanel = getReportPanel();
    
		refreshUI();
  }

  private void setCitationType(String type) {

		this.citationType = type;
  }




  private void setCitationTypeUI(JPanel panel) {

    topMiddlePanel.setMinimumSize(new Dimension(0,0));
    middlePanel.remove(currentPanel);
    //middlePanel.remove(topMiddlePanel);

    currentPanel = panel;
    //middlePanel.add(topMiddlePanel);
    middlePanel.add(currentPanel);
    topMiddlePanel.setMaximumSize(topMiddlePanel.getPreferredSize());
    topMiddlePanel.setMinimumSize(topMiddlePanel.getPreferredSize());

    ((WizardPageSubPanelAPI)currentPanel).onLoadAction();

    currentPanel.invalidate();

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
   *  Here, it does nothing because this is just a Panel and not the outer container
   */

  public void onLoadAction() {
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

    if (lastNameField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(lastNameLabel);
      lastNameField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(lastNameLabel);

		if (organizationField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(organizationLabel);
      organizationField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(organizationLabel);

		if (positionNameField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(positionNameLabel);
      positionNameField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(positionNameLabel);

    if (citationType==null || citationType.trim().equals("")) {

      WidgetFactory.hiliteComponent(citationTypeLabel);
      return false;
    }
    WidgetFactory.unhiliteComponent(citationTypeLabel);

    return ((WizardPageSubPanelAPI)currentPanel).validateUserInput();
  }



  /**
   *  @return a List contaiing 2 String elements - one for each column of the
   *  2-col list in which this surrogate is displayed
   *
   */
  public List getSurrogate() {

    List surrogate = new ArrayList();
		
		if(this.titleField.getText().trim().length() > 0)
			surrogate.add(this.titleField.getText());
		
		String creatorName = this.salutationField.getText().trim();
		if(creatorName.trim().length() > 0 && creatorName.indexOf(".") < 0) creatorName+=". ";
		if(creatorName.trim().length() > 0) creatorName += " ";
		creatorName += this.firstNameField.getText().trim();
		if(creatorName.trim().length() > 0) creatorName += " ";
		creatorName += " " + this.lastNameField.getText();
		
		if(creatorName.trim().length() > 0)
			surrogate.add(creatorName);
		
		if(citationType.length() > 0)
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
		
		String st = this.salutationField.getText(); 
		if(!st.trim().equals(""))
			map.put(xPath + "/creator[1]/individualName[1]/salutation[1]", st);
		
		String fn = this.firstNameField.getText(); 
		if(!fn.trim().equals(""))
			map.put(xPath + "/creator[1]/individualName[1]/givenName[1]", fn);
		
		String ln = this.lastNameField.getText(); 
		map.put(xPath + "/creator[1]/individualName[1]/surName[1]", ln);
		
		String on = this.organizationField.getText(); 
		map.put(xPath + "/creator[1]/organizationName[1]", on);
		
		String pn = this.positionNameField.getText(); 
		map.put(xPath + "/creator[1]/positionName[1]", pn);
		
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


  /**
   * sets the Data in the Attribute Dialog fields. This is called from the
   * TextImportWizard when it wants to set some information it has already
   * guessed from the given data file. Any data in the AttributeDialog can be
   * set through this method. The TextImportWizard however sets only the
   * "Attribute Name", "Measurement Scale", "Number Type" and the "Enumeration
   * Code Definitions"
   *
   * @param map - Data is passed as OrderedMap of xPath-value pairs. xPaths in
   *   this map are absolute xPath and not the relative xPaths
   */
  public boolean setPageData(OrderedMap map, String xPath) {
		
		this.titleField.setText((String)map.get(xPath + "/title[1]"));
		
		String st = (String)map.get(xPath + "/creator[1]/individualName[1]/salutation[1]");
		if(st != null)
			this.salutationField.setText(st);
		
		String fn = (String)map.get(xPath + "/creator[1]/individualName[1]/givenName[1]");
		if(fn != null)
			this.firstNameField.setText(fn);
		
		this.lastNameField.setText((String)map.get(xPath + "/creator[1]/individualName[1]/surName[1]"));
		this.organizationField.setText((String)map.get(xPath + "/creator[1]/organizationName[1]"));
		this.positionNameField.setText((String)map.get(xPath + "/creator[1]/positionName[1]"));
		
		String pubn = (String)map.get(xPath + "/pubDate[1]");
		if(pubn != null)
			this.pubDateField.setText(pubn);
		
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
		} else 
		
		
		if (componentNum != -1) {
			
			Container c = (Container)(radioPanel.getComponent(1));
			JRadioButton jrb = (JRadioButton)c.getComponent(componentNum);
			jrb.setSelected(true);
			
		}

		refreshUI();
		return true;
		
   }


}


class BookPanel extends JPanel implements WizardPageSubPanelAPI{
	
	CitationPage parent;
	private JLabel editionLabel;
	private JLabel volumeLabel;
	
	private JTextField editionField;
	private JTextField volumeField;
	
	MiniPublisherPanel publisherPanel;
	
	BookPanel(CitationPage page) {
		
		this.parent = page;
		init();
	}
	
	private void init() {
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		publisherPanel = new MiniPublisherPanel("Publisher Name", "Publishing Orgn:");
		
		this.add(publisherPanel);
		this.add(WidgetFactory.makeHalfSpacer());
		this.add(Box.createGlue());
		
		// Edition Name
    JPanel editionPanel = WidgetFactory.makePanel(1);
    editionLabel = WidgetFactory.makeLabel("Edition:", false);
    editionPanel.add(editionLabel);
    editionField = WidgetFactory.makeOneLineTextField();
    editionPanel.add(editionField);
    this.add(editionPanel);
    this.add(WidgetFactory.makeHalfSpacer());
		this.add(Box.createGlue());
		// Organization
    JPanel volumePanel = WidgetFactory.makePanel(1);
    volumeLabel = WidgetFactory.makeLabel("Volume:", false);
    volumePanel.add(volumeLabel);
    volumeField = WidgetFactory.makeOneLineTextField();
    volumePanel.add(volumeField);
    this.add(volumePanel);
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
		
		if(!publisherPanel.validateUserInput())
			return false;

		
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
		map.putAll(publisherPanel.getPanelData(xPathRoot));
		
		String en = this.editionField.getText();
		if(!en.trim().equals(""))
			map.put(xPathRoot + "/edition[1]", en);
		
		String vn = this.volumeField.getText();
		if(!vn.trim().equals(""))
			map.put(xPathRoot + "/volume[1]", vn);
		
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
		
		((WizardPageSubPanelAPI)publisherPanel).setPanelData(xPathRoot, map);
		
		String en = (String)map.get(xPathRoot + "/edition[1]");
		if(en != null)
			this.editionField.setText(en);
		
		String vn = (String)map.get(xPathRoot + "/volume[1]");
		if(vn != null)
			this.volumeField.setText(vn);
		
	}
}


class ArticlePanel extends JPanel  implements WizardPageSubPanelAPI{
	
	CitationPage parent;
	private JLabel journalLabel;
	private JLabel volumeLabel;
	private JLabel rangeLabel;
	
	private JTextField journalField;
	private JTextField volumeField;
	private JTextField rangeField;
	
	MiniPublisherPanel publisherPanel;
	
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
		
		this.add(journalPanel);
    this.add(WidgetFactory.makeHalfSpacer());
		
		// Volume
    JPanel volumePanel = WidgetFactory.makePanel(1);
    volumeLabel = WidgetFactory.makeLabel("Volume:", true);
    volumePanel.add(volumeLabel);
    volumeField = WidgetFactory.makeOneLineTextField();
    volumePanel.add(volumeField);
		this.add(Box.createGlue());
    this.add(volumePanel);
    this.add(WidgetFactory.makeHalfSpacer());
		
		// Page Range
    JPanel rangePanel = WidgetFactory.makePanel(1);
    rangeLabel = WidgetFactory.makeLabel("Page Range:", false);
    rangePanel.add(rangeLabel);
    rangeField = WidgetFactory.makeOneLineTextField();
    rangePanel.add(rangeField);
		this.add(Box.createGlue());
    this.add(rangePanel);
    this.add(WidgetFactory.makeHalfSpacer());
		
		publisherPanel = new MiniPublisherPanel("Publisher Name", "Publishing Orgn:");
		this.add(Box.createGlue());
		this.add(publisherPanel);
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
		
		if(!publisherPanel.validateUserInput())
			return false;

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
		
		String pr = this.rangeField.getText();
		map.put(xPathRoot + "/pageRange[1]", pr);
		
		map.putAll(publisherPanel.getPanelData(xPathRoot));
		
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
		
		((WizardPageSubPanelAPI)publisherPanel).setPanelData(xPathRoot, map);
		
	}
	
}


class ReportPanel extends JPanel  implements WizardPageSubPanelAPI{
	
	CitationPage parent;
	private JLabel numberLabel;
	private JLabel pagesLabel;
	
	private JTextField numberField;
	private JTextField pagesField;
	
	MiniPublisherPanel publisherPanel;
	
	ReportPanel(CitationPage page) {
		
		this.parent = page;
		init();
	}
	
	private void init() {
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		publisherPanel = new MiniPublisherPanel("Publisher Name", "Publishing Orgn:");
		
		this.add(Box.createGlue());
		this.add(publisherPanel);
		
		// Report Number
    JPanel numberPanel = WidgetFactory.makePanel(1);
    numberLabel = WidgetFactory.makeLabel("Report Number:", false);
    numberPanel.add(numberLabel);
    numberField = WidgetFactory.makeOneLineTextField();
    numberPanel.add(numberField);
		this.add(Box.createGlue());
    this.add(numberPanel);
    this.add(WidgetFactory.makeHalfSpacer());
		this.add(Box.createGlue());
		
		// Report Pages
    JPanel pagesPanel = WidgetFactory.makePanel(1);
    pagesLabel = WidgetFactory.makeLabel("Number of Pages:", false);
    pagesPanel.add(pagesLabel);
    pagesField = WidgetFactory.makeOneLineTextField();
    pagesPanel.add(pagesField);
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
		
		if(!publisherPanel.validateUserInput())
			return false;
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
		map.putAll(publisherPanel.getPanelData(xPathRoot));
		
		String rn = this.numberField.getText();
		if(!rn.trim().equals(""))
			map.put(xPathRoot + "/reportNumber[1]", rn);
		
		String pn = this.pagesField.getText();
		if(!pn.trim().equals(""))
			map.put(xPathRoot + "/totalPages[1]", pn);
		
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
	
		((WizardPageSubPanelAPI)publisherPanel).setPanelData(xPathRoot, map);
		
		String rn = (String)map.get(xPathRoot + "/reportNumber[1]");
		
		if(!rn.trim().equals(""))
			this.numberField.setText(rn);
		
		String pn = (String)map.get(xPathRoot + "/totalPages[1]");
		
		if(!pn.trim().equals(""))
			this.pagesField.setText(pn);
		
	}
	
}


class MiniPublisherPanel extends JPanel implements WizardPageSubPanelAPI {
	
	private JLabel lastNameLabel;
	private JLabel organizationLabel;
	
	private JTextField lastNameField;
	private JTextField organizationField;
	
	private String surname;
	private String orgnName;
	
	MiniPublisherPanel(String surname, String orgnName ) {
		
		this.surname = surname;
		this.orgnName = orgnName;
		init();
	}
	
	private void init() {
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Last Name
    JPanel lastNamePanel = WidgetFactory.makePanel(1);
    lastNameLabel = WidgetFactory.makeLabel(surname, true);
    lastNamePanel.add(lastNameLabel);
    lastNameField = WidgetFactory.makeOneLineTextField();
    lastNamePanel.add(lastNameField);
    this.add(lastNamePanel);
    this.add(WidgetFactory.makeHalfSpacer());
		this.add(Box.createGlue());
		
		// Organization
    JPanel organizationPanel = WidgetFactory.makePanel(1);
    organizationLabel = WidgetFactory.makeLabel(orgnName, true, new Dimension(100, 40));
    organizationPanel.add(organizationLabel);
    organizationField = WidgetFactory.makeOneLineTextField();
    organizationPanel.add(organizationField);
    this.add(organizationPanel);
    //this.add(Box.createGlue());
		
		//this.setBorder(BorderFactory.createLineBorder(Color.black));
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
		map.put(xPathRoot + "/publisher/individualName/surname", this.lastNameField.getText());
		
		map.put(xPathRoot + "/publisher/organizationName", this.organizationField.getText());
		
		map.put(xPathRoot + "/publisher/positionName", "Unknown");
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
	
		lastNameField.setText((String)map.get(xPathRoot +"/publisher[1]/individualName[1]/surname[1]")); 
		
		organizationField.setText((String)map.get(xPathRoot + "/publisher[1]/organizationName[1]"));
		
	}
	
}

