/**
 *        Name: QueryBean.java
 *     Purpose: A Class for creating a Query JavaBean for use Desktop Client
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: QueryBean.java,v 1.1 2000-07-12 19:47:44 higgins Exp $'
 */

package edu.ucsb.nceas.querybean;

import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.*;
import com.symantec.itools.javax.swing.JButtonGroupPanel;
import com.symantec.itools.javax.swing.models.StringListModel;
import com.symantec.itools.javax.swing.models.StringComboBoxModel;
import com.symantec.itools.javax.swing.models.StringTreeModel;
import com.symantec.itools.javax.swing.borders.EtchedBorder;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.io.*;
import java.net.URL;

import com.arbortext.catalog.*;

import org.xml.sax.SAXException;
import org.apache.xalan.xslt.XSLTProcessorFactory;
import org.apache.xalan.xslt.XSLTInputSource;
import org.apache.xalan.xslt.XSLTResultTarget;
import org.apache.xalan.xslt.XSLTProcessor;
import org.apache.xalan.xpath.xml.*;

public class QueryBean extends java.awt.Container
{
	LocalQuery lq = null;
	String[] searchmode = {"contains","contains-not","is","is-not","starts-with","ends-with"};
    JTable table;

	public QueryBean() 
	{
	    setLayout(new BorderLayout(0,0));
		//{{INIT_CONTROLS
		setSize(729,492);
		TopQueryPanel.setLayout(new BorderLayout(0,0));
		add(BorderLayout.CENTER,TopQueryPanel);
		TopQueryPanel.setFont(new Font("Dialog", Font.PLAIN, 12));
		TopQueryPanel.setBounds(0,0,729,492);
		TopQueryPanel.add(BorderLayout.CENTER, QueryChoiceTabs);
		QueryChoiceTabs.setFont(new Font("Dialog", Font.PLAIN, 12));
		QueryChoiceTabs.setBounds(0,0,0,0);
//		QueryChoiceTabs.setBounds(0,0,729,492);
		FullTextPanel.setBorder(etchedBorder1);
		FullTextPanel.setLayout(new BorderLayout(0,0));
		QueryChoiceTabs.add(FullTextPanel);
		FullTextPanel.setBounds(2,27,724,462);
		FullTextPanel.setVisible(false);
		Query.setBorder(etchedBorder1);
		Query.setLayout(new BorderLayout(0,0));
		FullTextPanel.add(BorderLayout.NORTH, Query);
		Query.setBounds(0,0,0,0);
//		Query.setBounds(2,2,720,294);
//		Query.setMinimumSize(new Dimension(688,150));
//		Query.setMaximumSize(new Dimension(688,150));
		Query.setPreferredSize(new Dimension(688,160));
		QueryTypePanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		Query.add(BorderLayout.NORTH, QueryTypePanel);
		QueryTypePanel.setBounds(0,0,0,0);
//		QueryTypePanel.setBounds(2,2,716,33);
		TextChoiceButtonPanel.setLayout(new GridLayout(1,0,0,0));
		QueryTypePanel.add(TextChoiceButtonPanel);
		TextChoiceButtonPanel.setBounds(0,0,0,0);
//		TextChoiceButtonPanel.setBounds(5,5,408,23);
		SubjectButton.setSelected(true);
		SubjectButton.setText("Subject Search");
		SubjectButton.setActionCommand("Subject Search");
		TextChoiceButtonPanel.add(SubjectButton);
		SubjectButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		SubjectButton.setBounds(0,0,21,40);
		AllText.setText("Search All Text");
		AllText.setActionCommand("Search All Text");
		TextChoiceButtonPanel.add(AllText);
		AllText.setFont(new Font("Dialog", Font.PLAIN, 12));
		AllText.setBounds(0,0,204,23);
		SelectDocTypeButton.setText("Search Selected Document Type");
		SelectDocTypeButton.setActionCommand("Search Selected Document Types");
		TextChoiceButtonPanel.add(SelectDocTypeButton);
		SelectDocTypeButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		SelectDocTypeButton.setBounds(204,0,204,23);
		RefineQueryPanel.setLayout(new BorderLayout(0,0));
		Query.add(BorderLayout.CENTER, RefineQueryPanel);
		RefineQueryPanel.setBounds(0,0,0,0);
//		RefineQueryPanel.setBounds(2,35,716,257);
		QueryControls.setAlignmentX(0.0F);
		QueryControls.setLayout(new BoxLayout(QueryControls,BoxLayout.Y_AXIS));
		RefineQueryPanel.add(BorderLayout.EAST, QueryControls);
		QueryControls.setBounds(641,0,75,257);
		SearchButton.setText("Search");
		SearchButton.setActionCommand("Search");
		QueryControls.add(SearchButton);
		SearchButton.setBounds(0,0,75,25);
		DocTypePanel.setLayout(new BorderLayout(0,0));
		RefineQueryPanel.add(BorderLayout.WEST, DocTypePanel);
		DocTypePanel.setBounds(0,0,96,257);
		DocTypePanel.setVisible(false);
		DocTypeNameScrollPane.setOpaque(true);
		DocTypePanel.add(BorderLayout.CENTER, DocTypeNameScrollPane);
		DocTypeNameScrollPane.setBounds(0,0,96,242);
		DocTypeList.setModel(DocTypeListModel);
		DocTypeNameScrollPane.getViewport().add(DocTypeList);
		DocTypeList.setBounds(0,0,93,239);
		{
			String[] tempString = new String[8];
			tempString[0] = "eml-access";
			tempString[1] = "eml-content";
			tempString[2] = "eml-dataset";
			tempString[3] = "eml-file";
			tempString[4] = "eml-software";
			tempString[5] = "eml-status";
			tempString[6] = "eml-supplement";
			tempString[7] = "eml-variable";
			DocTypeListModel.setItems(tempString);
		}
		//$$ DocTypeListModel.move(0,493);
		{
			String[] tempString = new String[6];
			tempString[0] = "contains";
			tempString[1] = "doesn't contain";
			tempString[2] = "is";
			tempString[3] = "is not";
			tempString[4] = "starts with";
			tempString[5] = "ends with";
			MatchTypesModel.setItems(tempString);
		}
		//$$ MatchTypesModel.move(24,493);
		{
			String[] tempString = new String[6];
			tempString[0] = "contains";
			tempString[1] = "doesn't contain";
			tempString[2] = "is";
			tempString[3] = "is not";
			tempString[4] = "starts with";
			tempString[5] = "ends with";
			MatchTypesModel2.setItems(tempString);
		}
		//$$ MatchTypesModel2.move(48,493);
		{
			String[] tempString = new String[6];
			tempString[0] = "contains";
			tempString[1] = "doesn't contain";
			tempString[2] = "is";
			tempString[3] = "is not";
			tempString[4] = "starts with";
			tempString[5] = "ends with";
			MatchTypesModel3.setItems(tempString);
		}
		//$$ MatchTypesModel3.move(72,493);
		{
			String[] tempString = new String[6];
			tempString[0] = "contains";
			tempString[1] = "doesn't contain";
			tempString[2] = "is";
			tempString[3] = "is not";
			tempString[4] = "starts with";
			tempString[5] = "ends with";
			MatchTypesModel4.setItems(tempString);
		}
		//$$ MatchTypesModel4.move(96,493);
		{
			String[] tempString = new String[6];
			tempString[0] = "contains";
			tempString[1] = "doesn't contain";
			tempString[2] = "is";
			tempString[3] = "is not";
			tempString[4] = "starts with";
			tempString[5] = "ends with";
			MatchTypesModel5.setItems(tempString);
		}
		//$$ MatchTypesModel5.move(120,493);
		{
			String[] tempString = new String[6];
			tempString[0] = "contains";
			tempString[1] = "doesn't contain";
			tempString[2] = "is";
			tempString[3] = "is not";
			tempString[4] = "starts with";
			tempString[5] = "ends with";
			MatchTypesModel6.setItems(tempString);
		}
		//$$ MatchTypesModel6.move(144,493);
		{
			String[] tempString = new String[13];
			tempString[0] = "eml-dataset";
			tempString[1] = " metafile-id";
			tempString[2] = " dataset-id";
			tempString[3] = " title --- contains NCEAS";
			tempString[4] = " originator";
			tempString[5] = "  party";
			tempString[6] = "   salutation";
			tempString[7] = "   given_name";
			tempString[8] = "   surname";
			tempString[9] = "   job title";
			tempString[10] = " abstract";
			tempString[11] = " keyword";
			tempString[12] = " relations";
			eml_dataset.setItems(tempString);
		}
		//$$ eml_dataset.move(168,493);
		{
			String[] tempString = new String[5];
			tempString[0] = "Full Text";
			tempString[1] = "Document Type";
			tempString[2] = "Taxinomic";
			tempString[3] = "Thematic";
			tempString[4] = "Spatial";
			SearchTypeItems.setItems(tempString);
		}
		//$$ SearchTypeItems.move(216,493);
		//$$ etchedBorder1.move(216,493);
//		TextMatch2.setSelectedIndex(0);
		{
			String[] tempString = new String[10];
			tempString[0] = "eml-accesst";
			tempString[1] = " originator";
			tempString[2] = "  party";
			tempString[3] = "   salutation";
			tempString[4] = "   given_name";
			tempString[5] = "   surname";
			tempString[6] = "   job title";
			tempString[7] = " abstract";
			tempString[8] = " keyword";
			tempString[9] = " relations";
			eml_access.setItems(tempString);
		}
		//$$ eml_access.move(192,493);
		JLabel2.setText("Select Doc Types");
		DocTypePanel.add(BorderLayout.SOUTH, JLabel2);
		JLabel2.setForeground(java.awt.Color.black);
		JLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel2.setBounds(0,242,96,15);
		QueryChoicesPanel1.setLayout(new BorderLayout(0,0));
		RefineQueryPanel.add(BorderLayout.CENTER, QueryChoicesPanel1);
		QueryChoicesPanel1.setBounds(96,0,545,257);
		ChoicesScrollPane.setOpaque(true);
		QueryChoicesPanel1.add(BorderLayout.CENTER, ChoicesScrollPane);
		ChoicesScrollPane.setBounds(0,0,545,222);
		ChoicesPanel2.setAlignmentX(0.496933F);
		ChoicesPanel2.setLayout(new BoxLayout(ChoicesPanel2,BoxLayout.Y_AXIS));
		ChoicesScrollPane.getViewport().add(ChoicesPanel2);
		ChoicesPanel2.setBounds(0,0,542,219);
		TextChoices1.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		ChoicesPanel2.add(TextChoices1);
		TextChoices1.setBounds(0,0,542,36);
		TextLabel1.setText("Subject");
		TextChoices1.add(TextLabel1);
		TextLabel1.setForeground(java.awt.Color.black);
		TextLabel1.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextLabel1.setBounds(5,9,39,15);
		TextMatch1.setModel(MatchTypesModel);
		TextChoices1.add(TextMatch1);
		TextMatch1.setBackground(java.awt.Color.white);
		TextMatch1.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextMatch1.setBounds(49,5,115,24);
		TextMatch1.setVisible(false);
		TextValue1.setColumns(20);
		TextValue1.setText("NCEAS");
		TextChoices1.add(TextValue1);
		TextValue1.setBounds(169,7,330,19);
		TitleCheckBox.setSelected(true);
		TitleCheckBox.setText("Title");
		TitleCheckBox.setActionCommand("Title");
		TextChoices1.add(TitleCheckBox);
		TitleCheckBox.setBounds(0,0,21,40);
		AbstractCheckBox.setSelected(true);
		AbstractCheckBox.setText("Abstract");
		AbstractCheckBox.setActionCommand("Abstract");
		TextChoices1.add(AbstractCheckBox);
		AbstractCheckBox.setBounds(0,0,21,40);
		KeyWordsCheckBox.setSelected(true);
		KeyWordsCheckBox.setText("Key Words");
		KeyWordsCheckBox.setActionCommand("Key Words");
		TextChoices1.add(KeyWordsCheckBox);
		KeyWordsCheckBox.setBounds(0,0,21,40);
		AllCheckBox.setText("All");
		AllCheckBox.setActionCommand("All");
		TextChoices1.add(AllCheckBox);
		AllCheckBox.setBounds(0,0,21,40);
		TextChoices2.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		ChoicesPanel2.add(TextChoices2);
		TextChoices2.setBounds(0,36,542,36);
		TextLabel2.setText("Author (Last Name)");
		TextChoices2.add(TextLabel2);
		TextLabel2.setForeground(java.awt.Color.black);
		TextLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextLabel2.setBounds(5,9,39,15);
		TextMatch2.setModel(MatchTypesModel2);
		TextChoices2.add(TextMatch2);
		TextMatch2.setBackground(java.awt.Color.white);
		TextMatch2.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextMatch2.setBounds(49,5,115,24);
		TextMatch2.setVisible(false);
		TextValue2.setColumns(20);
		TextChoices2.add(TextValue2);
		TextValue2.setBounds(169,7,330,19);
		JLabel3.setText("Checks Indicate Areas to be Searched");
		TextChoices2.add(JLabel3);
		JLabel3.setBounds(0,0,20,40);
		TextChoices3.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		ChoicesPanel2.add(TextChoices3);
		TextChoices3.setBounds(0,72,542,36);
		TextChoices3.setVisible(false);
		JLabel5.setText("Text #3");
		TextChoices3.add(JLabel5);
		JLabel5.setForeground(java.awt.Color.black);
		JLabel5.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel5.setBounds(5,9,39,15);
		TextMatch3.setModel(MatchTypesModel3);
		TextChoices3.add(TextMatch3);
		TextMatch3.setBackground(java.awt.Color.white);
		TextMatch3.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextMatch3.setBounds(49,5,115,24);
		TextValue3.setColumns(30);
		TextChoices3.add(TextValue3);
		TextValue3.setBounds(169,7,330,19);
		TextChoices4.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		ChoicesPanel2.add(TextChoices4);
		TextChoices4.setBounds(0,108,542,36);
		TextChoices4.setVisible(false);
		JLabel1.setText("Text #4");
		TextChoices4.add(JLabel1);
		JLabel1.setForeground(java.awt.Color.black);
		JLabel1.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel1.setBounds(5,9,39,15);
		TextMatch4.setModel(MatchTypesModel4);
		TextChoices4.add(TextMatch4);
		TextMatch4.setBackground(java.awt.Color.white);
		TextMatch4.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextMatch4.setBounds(49,5,115,24);
		TextValue4.setColumns(30);
		TextChoices4.add(TextValue4);
		TextValue4.setBounds(169,7,330,19);
		TextChoices5.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		ChoicesPanel2.add(TextChoices5);
		TextChoices5.setBounds(0,144,542,36);
		TextChoices5.setVisible(false);
		JLabel6.setText("Text #5");
		TextChoices5.add(JLabel6);
		JLabel6.setForeground(java.awt.Color.black);
		JLabel6.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel6.setBounds(5,9,39,15);
		TextMatch5.setModel(MatchTypesModel5);
		TextChoices5.add(TextMatch5);
		TextMatch5.setBackground(java.awt.Color.white);
		TextMatch5.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextMatch5.setBounds(49,5,115,24);
		TextValue5.setColumns(30);
		TextChoices5.add(TextValue5);
		TextValue5.setBounds(169,7,330,19);
		TextChoices6.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		ChoicesPanel2.add(TextChoices6);
		TextChoices6.setBounds(0,180,542,36);
		TextChoices6.setVisible(false);
		JLabel7.setText("Text #6");
		TextChoices6.add(JLabel7);
		JLabel7.setForeground(java.awt.Color.black);
		JLabel7.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel7.setBounds(5,9,39,15);
		TextMatch6.setModel(MatchTypesModel6);
		TextChoices6.add(TextMatch6);
		TextMatch6.setBackground(java.awt.Color.white);
		TextMatch6.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextMatch6.setBounds(49,5,115,24);
		TextValue6.setColumns(30);
		TextChoices6.add(TextValue6);
		TextValue6.setBounds(169,7,330,19);
		More_Less_Buttons_Panel.setLayout(new FlowLayout(FlowLayout.LEFT,20,5));
		QueryChoicesPanel1.add(BorderLayout.SOUTH, More_Less_Buttons_Panel);
		More_Less_Buttons_Panel.setFont(new Font("Dialog", Font.PLAIN, 10));
		More_Less_Buttons_Panel.setBounds(0,222,545,35);
		AndRadioButton.setText("And");
		AndRadioButton.setActionCommand("And");
		More_Less_Buttons_Panel.add(AndRadioButton);
		AndRadioButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		AndRadioButton.setBounds(20,6,46,23);
		OrRadioButton.setSelected(true);
		OrRadioButton.setText("Or");
		OrRadioButton.setActionCommand("Or");
		More_Less_Buttons_Panel.add(OrRadioButton);
		OrRadioButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		OrRadioButton.setBounds(86,6,38,23);
		MoreButton.setText("More");
		MoreButton.setActionCommand("More");
		MoreButton.setEnabled(false);
		More_Less_Buttons_Panel.add(MoreButton);
		MoreButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		MoreButton.setBounds(144,5,61,25);
		LessButton.setText("Fewer");
		LessButton.setActionCommand("Fewer");
		LessButton.setEnabled(false);
		More_Less_Buttons_Panel.add(LessButton);
		LessButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		LessButton.setBounds(225,5,69,25);
		RS_Panel.setLayout(new BorderLayout(0,0));
		FullTextPanel.add(BorderLayout.CENTER, RS_Panel);
		RS_Panel.setBackground(java.awt.Color.white);
		RS_Panel.setBounds(2,296,720,164);
		JPanel22.setLayout(new BorderLayout(0,0));
		RS_Panel.add(BorderLayout.NORTH, JPanel22);
		JPanel22.setBounds(0,0,720,46);
		JPanel23.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel22.add(BorderLayout.CENTER, JPanel23);
		JPanel23.setBounds(233,0,260,46);
		JLabel11.setText("Results of Search");
		JPanel23.add(JLabel11);
		JLabel11.setForeground(java.awt.Color.black);
		JLabel11.setBounds(79,5,101,15);
		JPanel24.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel22.add(BorderLayout.WEST, JPanel24);
		JPanel24.setBounds(0,0,233,46);
		JScrollPane3.setOpaque(true);
		JPanel24.add(JScrollPane3);
		JScrollPane3.setBounds(5,5,223,33);
		QueryStringTextArea.setColumns(20);
		QueryStringTextArea.setRows(2);
		QueryStringTextArea.setText("Query Summary String");
		QueryStringTextArea.setLineWrap(true);
		JScrollPane3.getViewport().add(QueryStringTextArea);
		QueryStringTextArea.setBounds(0,0,220,30);
		JPanel25.setLayout(new GridLayout(2,1,0,0));
		JPanel22.add(BorderLayout.EAST, JPanel25);
		JPanel25.setBounds(493,0,227,46);
		JCheckBox3.setText("Detach (New Window)");
		JCheckBox3.setActionCommand("Detach (New Window)");
		JPanel25.add(JCheckBox3);
		JCheckBox3.setFont(new Font("Dialog", Font.PLAIN, 12));
		JCheckBox3.setBounds(0,0,227,23);
		JCheckBox4.setText("Refine Search (Using these Results)");
		JCheckBox4.setActionCommand("Refine Search (Using these Results)");
		JPanel25.add(JCheckBox4);
		JCheckBox4.setFont(new Font("Dialog", Font.PLAIN, 12));
		JCheckBox4.setBounds(0,23,227,23);
		RS_Panel.add(BorderLayout.CENTER, RSScrollPane);
		RSScrollPane.setBounds(0,46,720,118);
		DocTypeQueryPanel.setLayout(new GridLayout(2,1,0,0));
		QueryChoiceTabs.add(DocTypeQueryPanel);
		DocTypeQueryPanel.setBounds(2,27,724,462);
		DocTypeQueryPanel.setVisible(false);
		JPanel10.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		QueryChoiceTabs.add(JPanel10);
		JPanel10.setBounds(2,27,724,462);
		JPanel10.setVisible(false);
		JPanel11.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		QueryChoiceTabs.add(JPanel11);
		JPanel11.setBounds(2,27,724,462);
		JPanel11.setVisible(false);
		JPanel12.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		QueryChoiceTabs.add(JPanel12);
		JPanel12.setBounds(2,27,724,462);
		JPanel12.setVisible(false);
		QueryChoiceTabs.setSelectedComponent(FullTextPanel);
		QueryChoiceTabs.setSelectedIndex(0);
		QueryChoiceTabs.setTitleAt(0,"Thematic");
		QueryChoiceTabs.setTitleAt(1,"Document Type");
		QueryChoiceTabs.setTitleAt(2,"Taxonomic");
		QueryChoiceTabs.setTitleAt(3,"Spatial");
		QueryChoiceTabs.setTitleAt(4,"Dataset Browser");
		TextMatch6.setSelectedIndex(0);
		TextMatch2.setSelectedIndex(0);
		TextMatch5.setSelectedIndex(0);
		TextMatch3.setSelectedIndex(0);
		TextMatch1.setSelectedIndex(0);
		TextMatch4.setSelectedIndex(0);
		//}}
	
		//{{REGISTER_LISTENERS
		SymItem lSymItem = new SymItem();
		AllText.addItemListener(lSymItem);
		AndRadioButton.addItemListener(lSymItem);
		OrRadioButton.addItemListener(lSymItem);
		SymAction lSymAction = new SymAction();
		MoreButton.addActionListener(lSymAction);
		LessButton.addActionListener(lSymAction);
		SearchButton.addActionListener(lSymAction);
		SubjectButton.addItemListener(lSymItem);
		SelectDocTypeButton.addItemListener(lSymItem);
		AllCheckBox.addItemListener(lSymItem);
		//}}
		invalidate();
		setVisible(true);
	}

	//{{DECLARE_CONTROLS
	javax.swing.JPanel TopQueryPanel = new javax.swing.JPanel();
	javax.swing.JTabbedPane QueryChoiceTabs = new javax.swing.JTabbedPane();
	javax.swing.JPanel FullTextPanel = new javax.swing.JPanel();
	javax.swing.JPanel Query = new javax.swing.JPanel();
	javax.swing.JPanel QueryTypePanel = new javax.swing.JPanel();
	com.symantec.itools.javax.swing.JButtonGroupPanel TextChoiceButtonPanel = new com.symantec.itools.javax.swing.JButtonGroupPanel();
	javax.swing.JRadioButton SubjectButton = new javax.swing.JRadioButton();
	javax.swing.JRadioButton AllText = new javax.swing.JRadioButton();
	javax.swing.JRadioButton SelectDocTypeButton = new javax.swing.JRadioButton();
	javax.swing.JPanel RefineQueryPanel = new javax.swing.JPanel();
	javax.swing.JPanel QueryControls = new javax.swing.JPanel();
	javax.swing.JButton SearchButton = new javax.swing.JButton();
	javax.swing.JPanel DocTypePanel = new javax.swing.JPanel();
	javax.swing.JScrollPane DocTypeNameScrollPane = new javax.swing.JScrollPane();
	javax.swing.JList DocTypeList = new javax.swing.JList();
	javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
	javax.swing.JPanel QueryChoicesPanel1 = new javax.swing.JPanel();
	javax.swing.JScrollPane ChoicesScrollPane = new javax.swing.JScrollPane();
	javax.swing.JPanel ChoicesPanel2 = new javax.swing.JPanel();
	javax.swing.JPanel TextChoices1 = new javax.swing.JPanel();
	javax.swing.JLabel TextLabel1 = new javax.swing.JLabel();
	javax.swing.JComboBox TextMatch1 = new javax.swing.JComboBox();
	javax.swing.JTextField TextValue1 = new javax.swing.JTextField();
	javax.swing.JCheckBox TitleCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox AbstractCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox KeyWordsCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox AllCheckBox = new javax.swing.JCheckBox();
	javax.swing.JPanel TextChoices2 = new javax.swing.JPanel();
	javax.swing.JLabel TextLabel2 = new javax.swing.JLabel();
	javax.swing.JComboBox TextMatch2 = new javax.swing.JComboBox();
	javax.swing.JTextField TextValue2 = new javax.swing.JTextField();
	javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
	javax.swing.JPanel TextChoices3 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel5 = new javax.swing.JLabel();
	javax.swing.JComboBox TextMatch3 = new javax.swing.JComboBox();
	javax.swing.JTextField TextValue3 = new javax.swing.JTextField();
	javax.swing.JPanel TextChoices4 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
	javax.swing.JComboBox TextMatch4 = new javax.swing.JComboBox();
	javax.swing.JTextField TextValue4 = new javax.swing.JTextField();
	javax.swing.JPanel TextChoices5 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel6 = new javax.swing.JLabel();
	javax.swing.JComboBox TextMatch5 = new javax.swing.JComboBox();
	javax.swing.JTextField TextValue5 = new javax.swing.JTextField();
	javax.swing.JPanel TextChoices6 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel7 = new javax.swing.JLabel();
	javax.swing.JComboBox TextMatch6 = new javax.swing.JComboBox();
	javax.swing.JTextField TextValue6 = new javax.swing.JTextField();
	javax.swing.JPanel More_Less_Buttons_Panel = new javax.swing.JPanel();
	javax.swing.JRadioButton AndRadioButton = new javax.swing.JRadioButton();
	javax.swing.JRadioButton OrRadioButton = new javax.swing.JRadioButton();
	javax.swing.JButton MoreButton = new javax.swing.JButton();
	javax.swing.JButton LessButton = new javax.swing.JButton();
	javax.swing.JPanel RS_Panel = new javax.swing.JPanel();
	javax.swing.JPanel JPanel22 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel23 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel11 = new javax.swing.JLabel();
	javax.swing.JPanel JPanel24 = new javax.swing.JPanel();
	javax.swing.JScrollPane JScrollPane3 = new javax.swing.JScrollPane();
	javax.swing.JTextArea QueryStringTextArea = new javax.swing.JTextArea();
	javax.swing.JPanel JPanel25 = new javax.swing.JPanel();
	javax.swing.JCheckBox JCheckBox3 = new javax.swing.JCheckBox();
	javax.swing.JCheckBox JCheckBox4 = new javax.swing.JCheckBox();
	javax.swing.JScrollPane RSScrollPane = new javax.swing.JScrollPane();
	javax.swing.JPanel DocTypeQueryPanel = new javax.swing.JPanel();
	javax.swing.JPanel JPanel10 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel11 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel12 = new javax.swing.JPanel();
	com.symantec.itools.javax.swing.models.StringListModel DocTypeListModel = new com.symantec.itools.javax.swing.models.StringListModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel MatchTypesModel = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel MatchTypesModel2 = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel MatchTypesModel3 = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel MatchTypesModel4 = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel MatchTypesModel5 = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel MatchTypesModel6 = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringTreeModel eml_dataset = new com.symantec.itools.javax.swing.models.StringTreeModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel SearchTypeItems = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringTreeModel eml_access = new com.symantec.itools.javax.swing.models.StringTreeModel();
	com.symantec.itools.javax.swing.borders.EtchedBorder etchedBorder1 = new com.symantec.itools.javax.swing.borders.EtchedBorder();
	//}}

	public static void main(String argv[])
	{
		class DriverFrame extends java.awt.Frame
		{
			public DriverFrame()
			{
				addWindowListener(new java.awt.event.WindowAdapter()
				{
					public void windowClosing(java.awt.event.WindowEvent event)
					{
						dispose();	  // free the system resources
						System.exit(0); // close the application
					}
				});
		//		setLayout(null);
				setSize(400,300);
				add(new QueryBean());
			}
		}

		new DriverFrame().show();
	}




	class SymItem implements java.awt.event.ItemListener
	{
		public void itemStateChanged(java.awt.event.ItemEvent event)
		{
			Object object = event.getSource();
			if (object == AllText)
				AllText_itemStateChanged(event);
			else if (object == AndRadioButton)
				AndRadioButton_itemStateChanged(event);
			else if (object == OrRadioButton)
				OrRadioButton_itemStateChanged(event);
			else if (object == SubjectButton)
				SubjectButton_itemStateChanged(event);
			else if (object == SelectDocTypeButton)
				SelectDocTypeButton_itemStateChanged(event);
			else if (object == AllCheckBox)
				AllCheckBox_itemStateChanged(event);
		}
	}


	void AndRadioButton_itemStateChanged(java.awt.event.ItemEvent event)
	{
		if(AndRadioButton.isSelected()) OrRadioButton.setSelected(false);
		
			 
	}

	void OrRadioButton_itemStateChanged(java.awt.event.ItemEvent event)
	{
		if(OrRadioButton.isSelected()) AndRadioButton.setSelected(false);
		
			 
	}

	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == MoreButton)
				MoreButton_actionPerformed(event);
			else if (object == LessButton)
				LessButton_actionPerformed(event);
			else if (object == SearchButton)
				SearchButton_actionPerformed(event);
			
		}
	}

	void MoreButton_actionPerformed(java.awt.event.ActionEvent event)
	{
        if(TextChoices6.isVisible()) {}  // do nothing
        else if (TextChoices5.isVisible()) 
            {TextChoices6.setVisible(true);
              MoreButton.setEnabled(false);}
        else if (TextChoices4.isVisible()) TextChoices5.setVisible(true);
        else if (TextChoices3.isVisible()) TextChoices4.setVisible(true);
        else if (TextChoices2.isVisible()) TextChoices3.setVisible(true);
        else if (TextChoices1.isVisible()) 
          { TextChoices2.setVisible(true);
            LessButton.setEnabled(true); }
		
			 
	}

	void LessButton_actionPerformed(java.awt.event.ActionEvent event)
	{
        if(TextChoices6.isVisible()) 
            {TextChoices6.setVisible(false);
             MoreButton.setEnabled(true);}  
        else if (TextChoices5.isVisible()) TextChoices5.setVisible(false);
        else if (TextChoices4.isVisible()) TextChoices4.setVisible(false);
        else if (TextChoices3.isVisible()) TextChoices3.setVisible(false);
        else if (TextChoices2.isVisible()) 
            {TextChoices2.setVisible(false);
             LessButton.setEnabled(false);}
		
			 
	}

	void SearchButton_actionPerformed(java.awt.event.ActionEvent event)
	{
	    create_XMLQuery();
	    if (SearchButton.getText().equalsIgnoreCase("Halt")) {
	        if (lq!=null) {
	            lq.setStopFlag();
	            lq = null;
	        }
	        SearchButton.setText("Search");
	    }
	    else {
	    String path;
	    String root = "";
	    String[] paths = {"", "", "", "", "", ""};
	    String op = "and";
	    
	 // eventually will have to check for state here
	 if (SubjectButton.isSelected()) {
	    if (OrRadioButton.isSelected()) op = "or";
	    if (TitleCheckBox.isSelected()) {
	        paths[0] = "/eml-dataset/title[contains(text(),\""+TextValue1.getText()+"\")]";
	    }
	    if (AbstractCheckBox.isSelected()) {
	        paths[1] = "/eml-dataset/abstract/paragraph[contains(text(),\""+TextValue1.getText()+"\")]";
	    }
	    if (KeyWordsCheckBox.isSelected()) {
	        paths[2] = "/eml-dataset/keyword_info/keyword[contains(text(),\""+TextValue1.getText()+"\")]";
	    }
	    
	    if (AllCheckBox.isSelected()) {
	        paths[0] = "//*[(contains(text(),\""+TextValue1.getText()+"\"))]";
	        paths[1] = "";
	        paths[2] = "";
	    }
	    if (TextValue2.getText().length()>0) {
            paths[2] = "/eml-dataset/originator/party/party_individual/surname[(contains(text(),\""+TextValue2.getText()+"\"))]";
	    }
        
	    boolean op1 = true;
	    if (op.equalsIgnoreCase("or")) op1 = false;
	    lq = new LocalQuery(paths,op1, SearchButton);
	 }
	 else {
	 if (SelectDocTypeButton.isSelected()) root="/"+DocTypeList.getSelectedValue().toString();
	 
	 if(TextValue1.getText().length()>0) {
	    String mode, match;
	    if (OrRadioButton.isSelected()) op = "or";
	    
		if ((TextChoices1.isVisible())&&(TextValue1.getText().length()>0)) {
		    mode = TextMatch1.getSelectedItem().toString();
		    match = TextValue1.getText();
		    paths[0] = getPath(mode,match);
		}
		if ((TextChoices2.isVisible())&&(TextValue2.getText().length()>0)) {
		    mode = TextMatch2.getSelectedItem().toString();
		    match = TextValue2.getText();
		    paths[1] = getPath(mode,match);
		}
		if ((TextChoices3.isVisible())&&(TextValue3.getText().length()>0)) {
		    mode = TextMatch3.getSelectedItem().toString();
		    match = TextValue3.getText();
		    paths[2] = getPath(mode,match);
		}
		if ((TextChoices4.isVisible())&&(TextValue4.getText().length()>0)) {
		    mode = TextMatch4.getSelectedItem().toString();
		    match = TextValue4.getText();
		    paths[3] = getPath(mode,match);
		}
		if ((TextChoices5.isVisible())&&(TextValue5.getText().length()>0)) {
		    mode = TextMatch5.getSelectedItem().toString();
		    match = TextValue5.getText();
		    paths[4] = getPath(mode,match);
		}
		if ((TextChoices6.isVisible())&&(TextValue6.getText().length()>0)) {
		    mode = TextMatch6.getSelectedItem().toString();
		    match = TextValue6.getText();
		    paths[5] = getPath(mode,match);
		}
      }   
            if (paths[0].length()>0) paths[0] = root+"//*["+paths[0]+ "]";
            if (paths[1].length()>0) paths[1] = root+"//*["+paths[1]+ "]";
            if (paths[2].length()>0) paths[2] = root+"//*["+paths[2]+ "]";
            if (paths[3].length()>0) paths[3] = root+"//*["+paths[3]+ "]";
            if (paths[4].length()>0) paths[4] = root+"//*["+paths[4]+ "]";
            if (paths[5].length()>0) paths[5] = root+"//*["+paths[5]+ "]";
            QueryStringTextArea.setText(paths[0]+"\n"+paths[1]+"\n"+paths[2]+"\n"+paths[3]+"\n"+paths[4]+"\n"+paths[5]);
	        boolean op1 = true;
	        if (op.equalsIgnoreCase("or")) op1 = false;
		     lq = new LocalQuery(paths, op1, SearchButton);
	 }   
		     table = lq.getRSTable();
		     
		     table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            ListSelectionModel rowSM = table.getSelectionModel();
            rowSM.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting()) return;
                    
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    if (lsm.isSelectionEmpty()) {
                        System.out.println("No rows are selected.");
                    } else {
                        int selectedRow = lsm.getMinSelectionIndex();
//                        System.out.println("Row " + selectedRow
//                                           + " is now selected.");
                        String filename = (String)table.getModel().getValueAt(selectedRow, 0);
                        File file = new File("xmlfiles/"+filename);
                        DocFrame df = new DocFrame(file);
                        df.setVisible(true);
                        df.writeInfo();
                        
     /*                   try{
                            File file = new File("./xmlfiles/"+filename);
                            FileReader in = new FileReader(file);
                            StringWriter out = new StringWriter();
                            int c;
                            while ((c = in.read()) != -1) {
                                out.write(c);
                            }
                            in.close();
                            out.close();
                        DocFrame df = new DocFrame();
                        df.setVisible(true);
                        df.;
                        }
                        catch (Exception w) {;}
       */
                    }
                }
            });
        
		     
		     
		     RSScrollPane.getViewport().add(table);
//		     lq.queryAll();
            lq.start();
		} 
	}
	
	
	void create_XMLQuery() {
	 	if(TextValue1.getText().length()>0) {
	    String op = "INTERSECT";
	    if (OrRadioButton.isSelected()) op = "UNION";
	    
		pathqueryXML pqx = new pathqueryXML();
	 if (SubjectButton.isSelected()) {
		if ((TextChoices2.isVisible())&&(TextValue2.getText().length()>0)) {
		    pqx.add_querygroup(op);
		    pqx.add_querygroup_asChild("UNION");
		}
		else {
		    pqx.add_querygroup("UNION");
		}
		if ((TextChoices1.isVisible())&&(TextValue1.getText().length()>0)) {
		    if (TitleCheckBox.isSelected()) {
		        pqx.add_queryterm(TextValue1.getText(),"/eml-dataset/title","contains",true);
		    }
		    if (AbstractCheckBox.isSelected()) {
		        pqx.add_queryterm(TextValue1.getText(),"/eml-dataset/abstract/paragraph","contains",true);
		    }
		    if (KeyWordsCheckBox.isSelected()) {
		        pqx.add_queryterm(TextValue1.getText(),"/eml-dataset/keyword_info/keyword","contains",true);
		    }
		    if (AllCheckBox.isSelected()) {
		        pqx.add_queryterm(TextValue1.getText(),"//*","contains",true);		        
		    }
		}
//		pqx.end_querygroup();
		if ((TextChoices2.isVisible())&&(TextValue2.getText().length()>0)) {
		    pqx.add_querygroup(op);
		    pqx.add_queryterm(TextValue2.getText(),"/eml-dataset/originator/party/party_individual/surname","contains",true);
//		    pqx.end_querygroup();
		}
	 }
	 if (AllText.isSelected()) {
		pqx.add_querygroup(op);
		if ((TextChoices1.isVisible())&&(TextValue1.getText().length()>0)) {
		    pqx.add_queryterm(TextValue1.getText(),"//*",searchmode[TextMatch1.getSelectedIndex()],true);
		}
		if ((TextChoices2.isVisible())&&(TextValue2.getText().length()>0)) {
		    pqx.add_queryterm(TextValue2.getText(),"//*",searchmode[TextMatch2.getSelectedIndex()],true);
		}
		if ((TextChoices3.isVisible())&&(TextValue3.getText().length()>0)) {
		    pqx.add_queryterm(TextValue3.getText(),"//*",searchmode[TextMatch3.getSelectedIndex()],true);
		}
		if ((TextChoices4.isVisible())&&(TextValue4.getText().length()>0)) {
		    pqx.add_queryterm(TextValue4.getText(),"//*",searchmode[TextMatch4.getSelectedIndex()],true);
		}
		if ((TextChoices5.isVisible())&&(TextValue5.getText().length()>0)) {
		    pqx.add_queryterm(TextValue5.getText(),"//*",searchmode[TextMatch5.getSelectedIndex()],true);
		}
		if ((TextChoices6.isVisible())&&(TextValue6.getText().length()>0)) {
		    pqx.add_queryterm(TextValue6.getText(),"//*",searchmode[TextMatch6.getSelectedIndex()],true);
		}
//		pqx.end_querygroup();
	 }
	 if (SelectDocTypeButton.isSelected()) {
	    String root = "/"+DocTypeList.getSelectedValue().toString();
		pqx.add_querygroup(op);
		if ((TextChoices1.isVisible())&&(TextValue1.getText().length()>0)) {
		    pqx.add_queryterm(TextValue1.getText(),root+"//*",searchmode[TextMatch1.getSelectedIndex()],true);
		}
		if ((TextChoices2.isVisible())&&(TextValue2.getText().length()>0)) {
		    pqx.add_queryterm(TextValue2.getText(),"root+//*",searchmode[TextMatch2.getSelectedIndex()],true);
		}
		if ((TextChoices3.isVisible())&&(TextValue3.getText().length()>0)) {
		    pqx.add_queryterm(TextValue3.getText(),"root+//*",searchmode[TextMatch3.getSelectedIndex()],true);
		}
		if ((TextChoices4.isVisible())&&(TextValue4.getText().length()>0)) {
		    pqx.add_queryterm(TextValue4.getText(),"root+//*",searchmode[TextMatch4.getSelectedIndex()],true);
		}
		if ((TextChoices5.isVisible())&&(TextValue5.getText().length()>0)) {
		    pqx.add_queryterm(TextValue5.getText(),"root+//*",searchmode[TextMatch5.getSelectedIndex()],true);
		}
		if ((TextChoices6.isVisible())&&(TextValue6.getText().length()>0)) {
		    pqx.add_queryterm(TextValue6.getText(),"root+//*",searchmode[TextMatch6.getSelectedIndex()],true);
		}
//		pqx.end_querygroup();
	 }
//	    if ((TextChoices2.isVisible())&&(TextValue2.getText().length()>0)&&(SubjectButton.isSelected()))
//	    {
//	        pqx.end_query_plus(op);
//	    }
//	    else {
		    pqx.end_query();
//		}
	//	System.out.println(pqx.get_XML());
		try{
		    StringReader sr = new StringReader(pqx.get_XML());
		    File pathFile = new File("pathFile.xml");
		    FileWriter fw = new FileWriter(pathFile);
            int c;
            while ((c = sr.read()) != -1) {
                fw.write(c);
            }
            sr.close();
            fw.close();
        }
        catch (Exception z) {}
		
      }   
	}
	
	
private String getPath(String type, String match){
    if(type.equalsIgnoreCase("contains")) {
        return ("(contains(text(),\""+match+"\"))");
    }
    else if (type.equalsIgnoreCase("doesn't contain")) {
        return ("(not(contains(text(),\""+match+"\")))");
    }
    else if (type.equalsIgnoreCase("is")) {
        return ("(text() = \""+match+"\")");
    }
    else if (type.equalsIgnoreCase("is not")) {
        return ("(text() != \""+match+"\")");
    }
    else if(type.equalsIgnoreCase("starts with")) {
        return ("(starts-with(text(),\""+match+"\"))");
    }
    
    else return ("(contains(text(),\""+match+"\"))");
}
	
	
	void AllText_itemStateChanged(java.awt.event.ItemEvent event)
	{
		if(AllText.isSelected()) {
		    DocTypePanel.setVisible(false);
		    RefineQueryPanel.validate();
		    MoreButton.setEnabled(true);
		    TextLabel1.setText("Text #1");
		    TextLabel2.setText("Text #2");
		    JLabel3.setVisible(false);
		    TextMatch1.setVisible(true);
		    TextMatch2.setVisible(true);
		    TitleCheckBox.setVisible(false);
		    AbstractCheckBox.setVisible(false);
		    KeyWordsCheckBox.setVisible(false);
		    AllCheckBox.setVisible(false);
		    TextValue1.setColumns(30);
		    TextValue2.setColumns(30);
		    TextChoices2.setVisible(false);
		    TextChoices3.setVisible(false);
		    TextChoices4.setVisible(false);
		    TextChoices5.setVisible(false);
		    TextChoices6.setVisible(false);
		    RefineQueryPanel.validate();
		}
	}
	

	void SubjectButton_itemStateChanged(java.awt.event.ItemEvent event)
	{
		if(SubjectButton.isSelected()) {
		    DocTypePanel.setVisible(false);
		    DocTypePanel.invalidate();
		    MoreButton.setEnabled(false);
		    LessButton.setEnabled(false);
		    TextLabel1.setText("Subject");
		    TextLabel2.setText("Author (Last Name)");
		    JLabel3.setVisible(true);
		    TextMatch1.setVisible(false);
		    TextMatch2.setVisible(false);
		    TitleCheckBox.setVisible(true);
		    AbstractCheckBox.setVisible(true);
		    KeyWordsCheckBox.setVisible(true);
		    AllCheckBox.setVisible(true);
		    TextValue1.setColumns(20);
		    TextValue2.setColumns(20);
		    TextChoices2.setVisible(true);
		    TextChoices3.setVisible(false);
		    TextChoices4.setVisible(false);
		    TextChoices5.setVisible(false);
		    TextChoices6.setVisible(false);
		    RefineQueryPanel.validate();
		    
		}
			 
	}

	void SelectDocTypeButton_itemStateChanged(java.awt.event.ItemEvent event)
	{
		if(SelectDocTypeButton.isSelected()) {
		    DocTypePanel.setVisible(true);
		    DocTypePanel.invalidate();
		    MoreButton.setEnabled(true);
		    TextLabel1.setText("Text #1");
		    TextLabel2.setText("Text #2");
		    JLabel3.setVisible(false);
		    TextMatch1.setVisible(true);
		    TextMatch2.setVisible(true);
		    TitleCheckBox.setVisible(false);
		    AbstractCheckBox.setVisible(false);
		    KeyWordsCheckBox.setVisible(false);
		    AllCheckBox.setVisible(false);
		    TextValue1.setColumns(30);
		    TextValue2.setColumns(30);
		    TextChoices2.setVisible(false);
		    TextChoices3.setVisible(false);
		    TextChoices4.setVisible(false);
		    TextChoices5.setVisible(false);
		    TextChoices6.setVisible(false);
		    RefineQueryPanel.validate();
		    
		}
			 
	}

	void AllCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
        if(AllCheckBox.isSelected()) {
            TitleCheckBox.setSelected(false);
            AbstractCheckBox.setSelected(false);
            KeyWordsCheckBox.setSelected(false);
            TitleCheckBox.setEnabled(false);
            AbstractCheckBox.setEnabled(false);
            KeyWordsCheckBox.setEnabled(false);
            
        }
        else {
            TitleCheckBox.setSelected(true);
            AbstractCheckBox.setSelected(true);
            KeyWordsCheckBox.setSelected(true);
            TitleCheckBox.setEnabled(true);
            AbstractCheckBox.setEnabled(true);
            KeyWordsCheckBox.setEnabled(true);
        }
	}
// this method is called by external full text search routines	
public void searchFor(String searchText) {
    AllText.setSelected(true);
    TextMatch1.setSelectedIndex(0);
    TextValue1.setText(searchText);
    SearchButton_actionPerformed(null);
}
	
	
	
}