/**
 *  '$RCSfile: CitationPage.java,v $'
 *    Purpose: A class that handles display of Citation Information
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 23:06:55 $'
 * '$Revision: 1.29 $'
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.utilities.OrderedMap;

/**
 * This page will display some entity information and data table itself for text importing.
 * The metadata information is entityName and entityDescription.
 * @author tao
 *
 */
public class TextImportEntity extends AbstractUIPage 
{
	   private String pageID = null;
	   private String title = null;
	   private String subTitle = null;
	   private String pageNumber = null;
	   private WizardContainerFrame frame = null;
	   
	   /**
	    * Construct
	    * @param frame
	    */
	   public TextImportEntity(WizardContainerFrame frame)
	   {
		   this.frame = frame;
		   init();
	   }
	   
	   /*
	    * Init the panels
	    */
	   private void init()
	   {
		    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		    JPanel vbox = this;
		    
		    JLabel Step1_titleLabel = new JLabel("Text Import Wizard");
		    Step1_titleLabel.setFont(WizardSettings.TITLE_FONT);
		    Step1_titleLabel.setForeground(WizardSettings.TITLE_TEXT_COLOR);
		    Step1_titleLabel.setBorder(new EmptyBorder(WizardSettings.PADDING,0,WizardSettings.PADDING,0));
		    JPanel Step1_topPanel = new JPanel();
		    Step1_topPanel.setLayout(new BorderLayout());
		    Step1_topPanel.setPreferredSize(WizardSettings.TOP_PANEL_DIMS);
		    Step1_topPanel.setMaximumSize(WizardSettings.TOP_PANEL_DIMS);
		    Step1_topPanel.setBorder(new EmptyBorder(0,3*WizardSettings.PADDING,0,3*WizardSettings.PADDING));
		    Step1_topPanel.setBackground(WizardSettings.TOP_PANEL_BG_COLOR);
		    Step1_topPanel.setOpaque(true);
		    Step1_topPanel.add(Step1_titleLabel, BorderLayout.CENTER);
		    
		    vbox.add(Step1_topPanel);

		    JPanel Step1ControlsPanel = new JPanel();
		    Step1ControlsPanel.setLayout(new BoxLayout(Step1ControlsPanel, BoxLayout.Y_AXIS));
		    Step1_TopTitlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		    Step1ControlsPanel.add(Step1_TopTitlePanel);
		    Step1_TopTitleLabel.setText("This set of screens will create metadata based on the content of the specified data file");
		    Step1_TopTitlePanel.add(Step1_TopTitleLabel);
		    Step1_TopTitleLabel.setForeground(java.awt.Color.black);
		    Step1_TopTitleLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		    Step1_TableNamePanel.setLayout(new BoxLayout(Step1_TableNamePanel,
		                                                 BoxLayout.X_AXIS));
		    Step1ControlsPanel.add(Step1_TableNamePanel);
		    Step1ControlsPanel.add(Box.createGlue());
		    /*Step1_NameLabel.setText(" Table Name: ");
		    Step1_NameLabel.setPreferredSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		    Step1_NameLabel.setMinimumSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		    Step1_NameLabel.setMaximumSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		    Step1_TableNamePanel.add(Step1_NameLabel);
		    Step1_NameLabel.setForeground(java.awt.Color.black);
		    Step1_NameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));*/
		    Step1_NameLabel = WidgetFactory.makeLabel(" Title:", true);
		    Step1_TableNamePanel.add(Step1_NameLabel);
		    Step1_TableDescriptionLabel.setMaximumSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		    Step1_TableDescriptionLabel.setMinimumSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		    Step1_TableDescriptionLabel.setPreferredSize(WizardSettings.
		                                                 WIZARD_CONTENT_LABEL_DIMS);
		    TableNameTextField.setPreferredSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
		    TableNameTextField.setMaximumSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
		    Step1_TableNamePanel.add(TableNameTextField);
		    Step1_TableDescriptionPanel.setLayout(new BoxLayout(
		        Step1_TableDescriptionPanel, BoxLayout.X_AXIS));
		    Step1ControlsPanel.add(Step1_TableDescriptionPanel);
		    Step1_TableDescriptionLabel.setText(" Description: ");
		    Step1ControlsPanel.add(Box.createGlue());
		    Step1_TableDescriptionPanel.add(Step1_TableDescriptionLabel);
		    Step1_TableDescriptionLabel.setForeground(java.awt.Color.black);
		    Step1_TableDescriptionLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		    TableDescriptionTextField.setPreferredSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
		    TableDescriptionTextField.setMaximumSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
		    Step1_TableDescriptionPanel.add(TableDescriptionTextField);
		    StartingLinePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		    Step1ControlsPanel.add(StartingLinePanel);
		    StartingLineLabel.setText("Start import at row: ");
		    StartingLinePanel.add(StartingLineLabel);
		    StartingLineLabel.setForeground(java.awt.Color.black);
		    StartingLineLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		    StartingLineTextField.setText("1");
		    StartingLineTextField.setColumns(4);
		    StartingLinePanel.add(StartingLineTextField);
		    ColumnLabelsLabel.setText("     ");
		    StartingLinePanel.add(ColumnLabelsLabel);
		    ColumnLabelsCheckBox.setText("Column Labels are in starting row");
		    ColumnLabelsCheckBox.setActionCommand("Column Labels are in starting row");
		    ColumnLabelsCheckBox.setSelected(false);
		    StartingLinePanel.add(ColumnLabelsCheckBox);
		    ColumnLabelsCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
		    
		    vbox.add(Step1ControlsPanel, BorderLayout.CENTER);
		    
		    vbox.add(DataScrollPanel);
	   }
	  
	   /**
	   *  gets the unique ID for this UI page
	   *
	   *  @return   the unique ID String for this UI page
	   */
	  public String getPageID()
	  {
		 return pageID; 
	  }


	  /**
	   *  gets the title for this UI page
	   *
	   *  @return   the String title for this UI page
	   */
	  public String getTitle()
	  {
		  return title;
	  }


	  /**
	   *  gets the subtitle for this UI page
	   *
	   *  @return   the String subtitle for this UI page
	   */
	  public String getSubtitle()
	  {
		  return subTitle;
	  }


	  /**
	   *  Returns the ID of the page that the user will see next, after the "Next"
	   *  button is pressed. If this is the last page, return value must be null
	   *
	   *  @return the String ID of the page that the user will see next, or null if
	   *  this is te last page
	   */
	  public String getNextPageID()
	  {
		  return nextPageID;
	  }


	  /**
	   *  Returns the serial number of the page
	   *
	   *  @return the serial number of the page
	   */
	  public String getPageNumber()
	  {
		  return pageNumber;
	  }


	  /**
	   *  The action to be executed when the page is displayed. May be empty
	   */
	  public void onLoadAction()
	  {
		  
	  }


	  /**
	   *  The action to be executed when the "Prev" button is pressed. May be empty
	   *
	   */
	  public  void onRewindAction()
	  {
		  
	  }


	  /**
	   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
	   *  or "Finish" button(last page) is pressed. May be empty
	   *
	   *  @return boolean true if wizard should advance, false if not
	   *          (e.g. if a required field hasn't been filled in)
	   */
	  public boolean onAdvanceAction()
	  {
		  return true;
	  }


	  /**
	   *  gets the Map object that contains all the key/value paired
	   *  settings for this particular UI page
	   *
	   *  @return   data the Map object that contains all the
	   *            key/value paired settings for this particular UI page
	   */
	  public OrderedMap getPageData()
	  {
		  OrderedMap map = null;
		  return map;
	  }


	  /**
	   * gets the Map object that contains all the key/value paired settings for
	   * this particular UI page
	   *
	   * @param rootXPath the root xpath to prepend to all the xpaths returned by
	   *   this method
	   * @return data the Map object that contains all the key/value paired
	   *   settings for this particular UI page
	   */
	  public OrderedMap getPageData(String rootXPath)
	  {
		  OrderedMap map = null;
		  return map;
	  }


	  /**
	   * sets the fields in the UI page using the Map object that contains all
	   * the key/value paired
	   *
	   * @param data the Map object that contains all the key/value paired settings
	   *   for this particular UI page
	   * @param rootXPath the String that represents the "root" of the XPath to the
	   *   content of this widget, INCLUDING PREDICATES. example - if this is a
	   *   "Party" widget, being used for the second "Creator" entry in a list,
	   *   then xPathRoot = "/eml:eml/dataset[1]/creator[2]
	   * @return boolean true if this page can handle all the data passed in the
	   * OrderedMap, false if not. <em>NOTE that the setPageData() method should
	   * still complete its work and fill out all the UI values, even if it is
	   * returning false</em>
	   */
	  public boolean setPageData(OrderedMap data, String rootXPath)
	  {
		  return true;
	  }
	  
	  
	  private JPanel Step1_TopTitlePanel = new JPanel();
	  private JLabel Step1_TopTitleLabel = new JLabel();
	  private JPanel Step1_TableNamePanel = new JPanel();
	  private JLabel Step1_NameLabel = new JLabel();
	  private JTextField TableNameTextField = new JTextField();
	  private JPanel Step1_TableDescriptionPanel = new JPanel();
	  private JLabel Step1_TableDescriptionLabel = new JLabel();
	  private JTextField TableDescriptionTextField = new JTextField();
	  private JPanel StartingLinePanel = new JPanel();
	  private JLabel StartingLineLabel = new JLabel();
	  private JTextField StartingLineTextField = new JTextField();
	  private JLabel ColumnLabelsLabel = new JLabel();
	  private JCheckBox ColumnLabelsCheckBox = new JCheckBox();
	  private JScrollPane DataScrollPanel = new JScrollPane();

}
