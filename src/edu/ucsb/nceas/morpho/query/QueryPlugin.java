/**
 *        Name: QueryBean.java
 *     Purpose: A Class for creating a Query JavaBean for use Desktop Client
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: QueryPlugin.java,v 1.12 2000-08-23 17:31:50 higgins Exp $'
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
import javax.swing.table.*;

import com.symantec.itools.javax.swing.JButtonGroupPanel;
import com.symantec.itools.javax.swing.models.StringListModel;
import com.symantec.itools.javax.swing.models.StringComboBoxModel;
import com.symantec.itools.javax.swing.models.StringTreeModel;
import com.symantec.itools.javax.swing.borders.EtchedBorder;

import edu.ucsb.nceas.dtclient.*;


import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.io.*;
import java.net.URL;
import java.util.*;

import com.arbortext.catalog.*;

import org.xml.sax.SAXException;
import org.apache.xalan.xslt.XSLTProcessorFactory;
import org.apache.xalan.xslt.XSLTInputSource;
import org.apache.xalan.xslt.XSLTResultTarget;
import org.apache.xalan.xslt.XSLTProcessor;
import org.apache.xalan.xpath.xml.*;
import edu.ucsb.nceas.querybean.DataGuideBean;

//public class QueryBean extends java.awt.Container 
public class QueryBean extends AbstractQueryBean 
{
    String userName = "anonymous";
    String passWord = "none";
    boolean localonly = false;
    String 	xmlcatalogfile = null;
    String MetaCatServletURL = null;
    PropertyResourceBundle options;
    
	LocalQuery lq = null;
	String[] searchmode = {"contains","contains-not","is","is-not","starts-with","ends-with"};
    JTable table;

	public QueryBean() 
	{
	    setLayout(new BorderLayout(0,0));
		//{{INIT_CONTROLS
		setLayout(new BorderLayout(0,0));
		setSize(0,0);
//		setLayout(null);
//		setSize(729,492);
		TopQueryPanel.setLayout(new BorderLayout(0,0));
		TopQueryPanel.setFont(new Font("Dialog", Font.PLAIN, 12));
		TopQueryPanel.setBounds(0,0,0,0);
//		TopQueryPanel.setBounds(0,0,729,492);
		add(BorderLayout.CENTER,TopQueryPanel);
		TopQueryPanel.add(BorderLayout.CENTER, QueryChoiceTabs);
		QueryChoiceTabs.setFont(new Font("Dialog", Font.PLAIN, 12));
		QueryChoiceTabs.setBounds(0,0,0,0);
//		QueryChoiceTabs.setBounds(0,0,729,492);
		ThematicPanel.setBorder(etchedBorder1);
		ThematicPanel.setLayout(new BorderLayout(0,0));
		QueryChoiceTabs.add(ThematicPanel);
		ThematicPanel.setBounds(2,27,724,462);
		ThematicPanel.setVisible(false);
		Query.setBorder(etchedBorder1);
		Query.setLayout(new BorderLayout(0,0));
		ThematicPanel.add(BorderLayout.NORTH, Query);
		Query.setBounds(0,0,0,0);
//		Query.setBounds(2,2,720,294);
//		Query.setMinimumSize(new Dimension(688,150));
//		Query.setMaximumSize(new Dimension(688,150));
//		Query.setPreferredSize(new Dimension(688,160));
		Query1.setPreferredSize(new Dimension(688,160));
		Query2.setPreferredSize(new Dimension(688,240));
//		QueryTypePanel.setBounds(2,2,716,33);
//		TextChoiceButtonPanel.setBounds(5,5,408,23);
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
		LocalCheckBox.setText("Local Only?");
		LocalCheckBox.setActionCommand("Local Only?");
		QueryControls.add(LocalCheckBox);
		LocalCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
		LocalCheckBox.setBounds(0,0,21,40);
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
		QueryChoicesPanel1.setLayout(new BorderLayout(0,0));
		RefineQueryPanel.add(BorderLayout.CENTER, QueryChoicesPanel1);
		QueryChoicesPanel1.setBounds(96,0,545,257);
		ChoicesPanel2.setAlignmentX(0.496933F);
		ChoicesPanel2.setLayout(new BoxLayout(ChoicesPanel2,BoxLayout.Y_AXIS));
		QueryChoicesPanel1.add(BorderLayout.CENTER,ChoicesPanel2);
		ChoicesPanel2.setBounds(0,0,542,219);
		TextChoices11.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		ChoicesPanel2.add(TextChoices11);
		TextChoices11.setBounds(0,0,542,36);
		TextLabel11.setText("Subject");
		TextChoices11.add(TextLabel11);
		TextLabel11.setForeground(java.awt.Color.black);
		TextLabel11.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextLabel11.setBounds(5,9,39,15);
		TextValue11.setColumns(20);
		TextValue11.setText("NCEAS");
		TextChoices11.add(TextValue11);
		TextValue11.setBounds(169,7,330,19);
		TitleCheckBox.setSelected(true);
		TitleCheckBox.setText("Title");
		TitleCheckBox.setActionCommand("Title");
		TextChoices11.add(TitleCheckBox);
		TitleCheckBox.setBounds(0,0,21,40);
		AbstractCheckBox.setSelected(true);
		AbstractCheckBox.setText("Abstract");
		AbstractCheckBox.setActionCommand("Abstract");
		TextChoices11.add(AbstractCheckBox);
		AbstractCheckBox.setBounds(0,0,21,40);
		KeyWordsCheckBox.setSelected(true);
		KeyWordsCheckBox.setText("Key Words");
		KeyWordsCheckBox.setActionCommand("Key Words");
		TextChoices11.add(KeyWordsCheckBox);
		KeyWordsCheckBox.setBounds(0,0,21,40);
		AllCheckBox.setText("All");
		AllCheckBox.setActionCommand("All");
		TextChoices11.add(AllCheckBox);
		AllCheckBox.setBounds(0,0,21,40);
		TextChoices22.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		ChoicesPanel2.add(TextChoices22);
		TextChoices22.setBounds(0,0,542,36);
		JLabel4.setText("Author (Last Name)");
		TextChoices22.add(JLabel4);
		JLabel4.setForeground(java.awt.Color.black);
		JLabel4.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel4.setBounds(5,9,39,15);
		TextValue22.setColumns(20);
		TextChoices22.add(TextValue22);
		TextValue22.setBounds(169,7,330,19);
		JLabel9.setText("Checks Indicate Areas to be Searched");
		TextChoices22.add(JLabel9);
		JLabel9.setBounds(0,0,20,40);
		JPanel2.setLayout(new FlowLayout(FlowLayout.LEFT,20,5));
		QueryChoicesPanel1.add(BorderLayout.SOUTH, JPanel2);
		JPanel2.setFont(new Font("Dialog", Font.PLAIN, 10));
		JPanel2.setBounds(0,0,545,35);
		AndButton2.setText("And");
		AndButton2.setActionCommand("And");
		JPanel2.add(AndButton2);
		AndButton2.setFont(new Font("Dialog", Font.PLAIN, 12));
		AndButton2.setBounds(20,6,46,23);
		OrButton2.setSelected(true);
		OrButton2.setText("Or");
		OrButton2.setActionCommand("Or");
		JPanel2.add(OrButton2);
		OrButton2.setFont(new Font("Dialog", Font.PLAIN, 12));
		OrButton2.setBounds(86,6,38,23);
		RS_Panel.setLayout(new BorderLayout(0,0));
		ThematicPanel.add(BorderLayout.CENTER, RS_Panel);
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
		DetachCheckBox.setText("Detach (New Window)");
		DetachCheckBox.setActionCommand("Detach (New Window)");
		JPanel25.add(DetachCheckBox);
		DetachCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
		DetachCheckBox.setBounds(0,0,227,23);
		JCheckBox4.setText("Refine Search (Using these Results)");
		JCheckBox4.setActionCommand("Refine Search (Using these Results)");
		JPanel25.add(JCheckBox4);
		JCheckBox4.setFont(new Font("Dialog", Font.PLAIN, 12));
		JCheckBox4.setBounds(0,23,227,23);
		RS_Panel.add(BorderLayout.CENTER, RSScrollPane);
		RSScrollPane.setBounds(0,46,720,118);
		AllTextPanel.setLayout(new BorderLayout(0,0));
		QueryChoiceTabs.add(AllTextPanel);
		AllTextPanel.setBounds(2,27,724,462);
		AllTextPanel.setVisible(false);
		Query1.setBorder(etchedBorder1);
		Query1.setLayout(new BorderLayout(0,0));
		AllTextPanel.add(BorderLayout.NORTH,Query1);
		Query1.setBounds(0,0,0,0);
		RefineQueryPanel1.setLayout(new BorderLayout(0,0));
		Query1.add(BorderLayout.CENTER, RefineQueryPanel1);
		RefineQueryPanel1.setBounds(0,0,0,0);
		QueryControls1.setAlignmentX(0.0F);
		QueryControls1.setLayout(new BoxLayout(QueryControls1,BoxLayout.Y_AXIS));
		RefineQueryPanel1.add(BorderLayout.EAST, QueryControls1);
		QueryControls1.setBounds(641,0,75,257);
		SearchButton1.setText("Search");
		SearchButton1.setActionCommand("Search");
		QueryControls1.add(SearchButton1);
		SearchButton1.setBounds(0,0,75,25);
		LocalCheckBox1.setText("Local Only?");
		LocalCheckBox1.setActionCommand("Local Only?");
		QueryControls1.add(LocalCheckBox1);
		LocalCheckBox1.setFont(new Font("Dialog", Font.PLAIN, 12));
		LocalCheckBox1.setBounds(0,0,21,40);
		QueryChoicesPanel11.setLayout(new BorderLayout(0,0));
		RefineQueryPanel1.add(BorderLayout.CENTER, QueryChoicesPanel11);
		QueryChoicesPanel11.setBounds(96,0,545,257);
		ChoicesScrollPane1.setOpaque(true);
		QueryChoicesPanel11.add(BorderLayout.CENTER, ChoicesScrollPane1);
		ChoicesScrollPane1.setBounds(0,0,545,222);
		JPanel7.setAlignmentX(0.496933F);
		JPanel7.setLayout(new BoxLayout(JPanel7,BoxLayout.Y_AXIS));
		ChoicesScrollPane1.getViewport().add(JPanel7);
		JPanel7.setBounds(0,0,542,219);
		TextChoices1.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		JPanel7.add(TextChoices1);
		TextChoices1.setBounds(0,0,542,36);
		JLabel8.setText("Text #1");
		TextChoices1.add(JLabel8);
		JLabel8.setForeground(java.awt.Color.black);
		JLabel8.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel8.setBounds(5,9,39,15);
		TextMatch1.setModel(MatchTypesModel);
		TextChoices1.add(TextMatch1);
		TextMatch1.setBackground(java.awt.Color.white);
		TextMatch1.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextMatch1.setBounds(49,5,115,24);
		TextValue1.setColumns(30);
		TextValue1.setText("NCEAS");
		TextChoices1.add(TextValue1);
		TextValue1.setBounds(169,7,330,19);
		TextChoices2.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		JPanel7.add(TextChoices2);
		TextChoices2.setBounds(0,36,542,36);
		TextChoices2.setVisible(false);
		TextLabel2.setText("Text #2");
		TextChoices2.add(TextLabel2);
		TextLabel2.setForeground(java.awt.Color.black);
		TextLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextLabel2.setBounds(5,9,39,15);
		TextMatch2.setModel(MatchTypesModel2);
		TextChoices2.add(TextMatch2);
		TextMatch2.setBackground(java.awt.Color.white);
		TextMatch2.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextMatch2.setBounds(49,5,115,24);
		TextValue2.setColumns(30);
		TextChoices2.add(TextValue2);
		TextValue2.setBounds(169,7,330,19);
		TextChoices3.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		JPanel7.add(TextChoices3);
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
		JPanel7.add(TextChoices4);
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
		JPanel7.add(TextChoices5);
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
		JPanel7.add(TextChoices6);
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
		QueryChoicesPanel11.add(BorderLayout.SOUTH, More_Less_Buttons_Panel);
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
		More_Less_Buttons_Panel.add(MoreButton);
		MoreButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		MoreButton.setBounds(144,5,61,25);
		LessButton.setText("Fewer");
		LessButton.setActionCommand("Fewer");
		LessButton.setEnabled(false);
		More_Less_Buttons_Panel.add(LessButton);
		LessButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		LessButton.setBounds(225,5,69,25);
		RS_Panel1.setLayout(new BorderLayout(0,0));
		AllTextPanel.add(BorderLayout.CENTER,RS_Panel1);
		RS_Panel1.setBackground(java.awt.Color.white);
		RS_Panel1.setBounds(0,0,720,164);
		JPanel30.setLayout(new BorderLayout(0,0));
		RS_Panel1.add(BorderLayout.NORTH, JPanel30);
		JPanel30.setBounds(0,0,720,46);
		JPanel31.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel30.add(BorderLayout.CENTER, JPanel31);
		JPanel31.setBounds(233,0,260,46);
		JLabel24.setText("Results of Search");
		JPanel31.add(JLabel24);
		JLabel24.setForeground(java.awt.Color.black);
		JLabel24.setBounds(79,5,101,15);
		JPanel32.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel30.add(BorderLayout.WEST, JPanel32);
		JPanel32.setBounds(0,0,233,46);
		JScrollPane6.setOpaque(true);
		JPanel32.add(JScrollPane6);
		JScrollPane6.setBounds(5,5,223,33);
		QueryStringTextArea1.setColumns(20);
		QueryStringTextArea1.setRows(2);
		QueryStringTextArea1.setText("Query Summary String");
		QueryStringTextArea1.setLineWrap(true);
		JScrollPane6.getViewport().add(QueryStringTextArea1);
		QueryStringTextArea1.setBounds(0,0,220,30);
		JPanel33.setLayout(new GridLayout(2,1,0,0));
		JPanel30.add(BorderLayout.EAST, JPanel33);
		JPanel33.setBounds(493,0,227,46);
		DetachCheckBox1.setText("Detach (New Window)");
		DetachCheckBox1.setActionCommand("Detach (New Window)");
		JPanel33.add(DetachCheckBox1);
		DetachCheckBox1.setFont(new Font("Dialog", Font.PLAIN, 12));
		DetachCheckBox1.setBounds(0,0,227,23);
		JPanel33.add(DetachCheckBox1);
		JCheckBox11.setText("Refine Search (Using these Results)");
		JCheckBox11.setActionCommand("Refine Search (Using these Results)");
		JPanel33.add(JCheckBox11);
		JCheckBox11.setFont(new Font("Dialog", Font.PLAIN, 12));
		JCheckBox11.setBounds(0,23,227,23);
		RS_Panel1.add(BorderLayout.CENTER, RSScrollPane1);
		RSScrollPane1.setBounds(0,46,720,118);
		DocumentTypePanel.setLayout(new BorderLayout(0,0));
		QueryChoiceTabs.add(DocumentTypePanel);
		DocumentTypePanel.setBounds(2,27,724,462);
		DocumentTypePanel.setVisible(false);
		Query2.setBorder(etchedBorder1);
		Query2.setLayout(new BorderLayout(0,0));
		DocumentTypePanel.add(BorderLayout.NORTH,Query2);
		Query2.setBounds(0,0,0,0);
		dataGuideBean1.setLayout(new GridLayout(1,2,2,2));
		Query2.add(BorderLayout.CENTER, dataGuideBean1);
		dataGuideBean1.setBackground(java.awt.Color.lightGray);
		dataGuideBean1.setBounds(0,0,635,453);
		RS_Panel2.setLayout(new BorderLayout(0,0));
		DocumentTypePanel.add(BorderLayout.CENTER,RS_Panel2);
		RS_Panel2.setBackground(java.awt.Color.white);
		RS_Panel2.setBounds(0,0,720,164);
		JPanel34.setLayout(new BorderLayout(0,0));
		RS_Panel2.add(BorderLayout.NORTH, JPanel34);
		JPanel34.setBounds(0,0,720,46);
		JPanel35.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel34.add(BorderLayout.CENTER, JPanel35);
		JPanel35.setBounds(233,0,260,46);
		JLabel25.setText("Results of Search");
		JPanel35.add(JLabel25);
		JLabel25.setForeground(java.awt.Color.black);
		JLabel25.setBounds(79,5,101,15);
		JPanel36.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel34.add(BorderLayout.WEST, JPanel36);
		JPanel36.setBounds(0,0,233,46);
		JScrollPane8.setOpaque(true);
		JPanel36.add(JScrollPane8);
		JScrollPane8.setBounds(5,5,223,33);
		QueryStringTextArea2.setColumns(20);
		QueryStringTextArea2.setRows(2);
		QueryStringTextArea2.setText("Query Summary String");
		QueryStringTextArea2.setLineWrap(true);
		JScrollPane8.getViewport().add(QueryStringTextArea2);
		QueryStringTextArea2.setBounds(0,0,220,30);
		JPanel37.setLayout(new GridLayout(2,1,0,0));
		JPanel34.add(BorderLayout.EAST, JPanel37);
		JPanel37.setBounds(493,0,227,46);
		JCheckBox12.setText("Detach (New Window)");
		JCheckBox12.setActionCommand("Detach (New Window)");
		JPanel37.add(JCheckBox12);
		JCheckBox12.setFont(new Font("Dialog", Font.PLAIN, 12));
		JCheckBox12.setBounds(0,0,227,23);
		JCheckBox13.setText("Refine Search (Using these Results)");
		JCheckBox13.setActionCommand("Refine Search (Using these Results)");
		JPanel37.add(JCheckBox13);
		JCheckBox13.setFont(new Font("Dialog", Font.PLAIN, 12));
		JCheckBox13.setBounds(0,23,227,23);
		RS_Panel2.add(BorderLayout.CENTER, RSScrollPane2);
		RSScrollPane2.setBounds(0,46,720,118);
		JPanel11.setLayout(new BorderLayout(0,0));
		QueryChoiceTabs.add(JPanel11);
		JPanel11.setBounds(2,27,724,462);
		JPanel11.setVisible(false);
		JPanel12.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		QueryChoiceTabs.add(JPanel12);
		JPanel12.setBounds(2,27,724,462);
		JPanel12.setVisible(false);
		JPanel1.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		QueryChoiceTabs.add(JPanel1);
		JPanel1.setBounds(0,0,20,40);
		JPanel1.setVisible(false);
		QueryChoiceTabs.setSelectedComponent(ThematicPanel);
		QueryChoiceTabs.setSelectedIndex(0);
		QueryChoiceTabs.setTitleAt(0,"Thematic");
		QueryChoiceTabs.setTitleAt(1,"All Text");
		QueryChoiceTabs.setTitleAt(2,"Document Type");
		QueryChoiceTabs.setTitleAt(3,"Taxonomic");
		QueryChoiceTabs.setTitleAt(4,"Spatial");
		QueryChoiceTabs.setTitleAt(5,"Data");
		TextMatch5.setSelectedIndex(0);
		TextMatch4.setSelectedIndex(0);
		TextMatch2.setSelectedIndex(0);
		TextMatch3.setSelectedIndex(0);
		TextMatch1.setSelectedIndex(0);
		TextMatch6.setSelectedIndex(0);
		//}}
	
		//{{REGISTER_LISTENERS
		SymItem lSymItem = new SymItem();
		AndRadioButton.addItemListener(lSymItem);
		OrRadioButton.addItemListener(lSymItem);
		SymAction lSymAction = new SymAction();
		MoreButton.addActionListener(lSymAction);
		LessButton.addActionListener(lSymAction);
		SearchButton.addActionListener(lSymAction);
		AllCheckBox.addItemListener(lSymItem);
		DetachCheckBox.addItemListener(lSymItem);
		AndButton2.addItemListener(lSymItem);
		OrButton2.addItemListener(lSymItem);
		SearchButton1.addActionListener(lSymAction);
		LocalCheckBox.addItemListener(lSymItem);
		LocalCheckBox1.addItemListener(lSymItem);
		//}}
		invalidate();
		setVisible(true);
    try {
      options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");
      xmlcatalogfile = (String)options.handleGetObject("xmlcatalogfile");
      MetaCatServletURL = (String)options.handleGetObject("MetaCatServletURL");
    }
    catch (Exception e) {System.out.println("Could not locate properties file!");}
		
	}

	//{{DECLARE_CONTROLS
	javax.swing.JPanel TopQueryPanel = new javax.swing.JPanel();
	javax.swing.JTabbedPane QueryChoiceTabs = new javax.swing.JTabbedPane();
	javax.swing.JPanel ThematicPanel = new javax.swing.JPanel();
	javax.swing.JPanel Query = new javax.swing.JPanel();
	javax.swing.JPanel RefineQueryPanel = new javax.swing.JPanel();
	javax.swing.JPanel QueryControls = new javax.swing.JPanel();
	javax.swing.JButton SearchButton = new javax.swing.JButton();
	javax.swing.JCheckBox LocalCheckBox = new javax.swing.JCheckBox();
	javax.swing.JPanel QueryChoicesPanel1 = new javax.swing.JPanel();
	javax.swing.JPanel ChoicesPanel2 = new javax.swing.JPanel();
	javax.swing.JPanel TextChoices11 = new javax.swing.JPanel();
	javax.swing.JLabel TextLabel11 = new javax.swing.JLabel();
	javax.swing.JTextField TextValue11 = new javax.swing.JTextField();
	javax.swing.JCheckBox TitleCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox AbstractCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox KeyWordsCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox AllCheckBox = new javax.swing.JCheckBox();
	javax.swing.JPanel TextChoices22 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel4 = new javax.swing.JLabel();
	javax.swing.JTextField TextValue22 = new javax.swing.JTextField();
	javax.swing.JLabel JLabel9 = new javax.swing.JLabel();
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JRadioButton AndButton2 = new javax.swing.JRadioButton();
	javax.swing.JRadioButton OrButton2 = new javax.swing.JRadioButton();
	javax.swing.JPanel RS_Panel = new javax.swing.JPanel();
	javax.swing.JPanel JPanel22 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel23 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel11 = new javax.swing.JLabel();
	javax.swing.JPanel JPanel24 = new javax.swing.JPanel();
	javax.swing.JScrollPane JScrollPane3 = new javax.swing.JScrollPane();
	javax.swing.JTextArea QueryStringTextArea = new javax.swing.JTextArea();
	javax.swing.JPanel JPanel25 = new javax.swing.JPanel();
	javax.swing.JCheckBox DetachCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox JCheckBox4 = new javax.swing.JCheckBox();
	javax.swing.JScrollPane RSScrollPane = new javax.swing.JScrollPane();
	javax.swing.JPanel AllTextPanel = new javax.swing.JPanel();
	javax.swing.JPanel Query1 = new javax.swing.JPanel();
	javax.swing.JPanel RefineQueryPanel1 = new javax.swing.JPanel();
	javax.swing.JPanel QueryControls1 = new javax.swing.JPanel();
	javax.swing.JButton SearchButton1 = new javax.swing.JButton();
	javax.swing.JCheckBox LocalCheckBox1 = new javax.swing.JCheckBox();
	javax.swing.JPanel QueryChoicesPanel11 = new javax.swing.JPanel();
	javax.swing.JScrollPane ChoicesScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JPanel JPanel7 = new javax.swing.JPanel();
	javax.swing.JPanel TextChoices1 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel8 = new javax.swing.JLabel();
	javax.swing.JComboBox TextMatch1 = new javax.swing.JComboBox();
	javax.swing.JTextField TextValue1 = new javax.swing.JTextField();
	javax.swing.JPanel TextChoices2 = new javax.swing.JPanel();
	javax.swing.JLabel TextLabel2 = new javax.swing.JLabel();
	javax.swing.JComboBox TextMatch2 = new javax.swing.JComboBox();
	javax.swing.JTextField TextValue2 = new javax.swing.JTextField();
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
	javax.swing.JPanel RS_Panel1 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel30 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel31 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel24 = new javax.swing.JLabel();
	javax.swing.JPanel JPanel32 = new javax.swing.JPanel();
	javax.swing.JScrollPane JScrollPane6 = new javax.swing.JScrollPane();
	javax.swing.JTextArea QueryStringTextArea1 = new javax.swing.JTextArea();
	javax.swing.JPanel JPanel33 = new javax.swing.JPanel();
	javax.swing.JCheckBox DetachCheckBox1 = new javax.swing.JCheckBox();
	javax.swing.JCheckBox JCheckBox11 = new javax.swing.JCheckBox();
	javax.swing.JScrollPane RSScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JPanel DocumentTypePanel = new javax.swing.JPanel();
	javax.swing.JPanel Query2 = new javax.swing.JPanel();
	edu.ucsb.nceas.querybean.DataGuideBean dataGuideBean1 = new edu.ucsb.nceas.querybean.DataGuideBean(this);
	javax.swing.JPanel RS_Panel2 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel34 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel35 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel25 = new javax.swing.JLabel();
	javax.swing.JPanel JPanel36 = new javax.swing.JPanel();
	javax.swing.JScrollPane JScrollPane8 = new javax.swing.JScrollPane();
	javax.swing.JTextArea QueryStringTextArea2 = new javax.swing.JTextArea();
	javax.swing.JPanel JPanel37 = new javax.swing.JPanel();
	javax.swing.JCheckBox JCheckBox12 = new javax.swing.JCheckBox();
	javax.swing.JCheckBox JCheckBox13 = new javax.swing.JCheckBox();
	javax.swing.JScrollPane RSScrollPane2 = new javax.swing.JScrollPane();
	javax.swing.JPanel JPanel11 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel12 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	com.symantec.itools.javax.swing.models.StringListModel DocTypeListModel = new com.symantec.itools.javax.swing.models.StringListModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel MatchTypesModel = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel MatchTypesModel2 = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel MatchTypesModel3 = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel MatchTypesModel4 = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel MatchTypesModel5 = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel MatchTypesModel6 = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	com.symantec.itools.javax.swing.models.StringTreeModel eml_dataset = new com.symantec.itools.javax.swing.models.StringTreeModel();
	com.symantec.itools.javax.swing.models.StringTreeModel eml_access = new com.symantec.itools.javax.swing.models.StringTreeModel();
	com.symantec.itools.javax.swing.borders.EtchedBorder etchedBorder1 = new com.symantec.itools.javax.swing.borders.EtchedBorder();
	//}}

	public static void main(String argv[])
	{
		class DriverFrame extends javax.swing.JFrame
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
				getContentPane().setLayout(new BorderLayout(0,0));
				setSize(600,400);
	            QueryBean queryBean1 = new QueryBean();
		        getContentPane().add(BorderLayout.CENTER, queryBean1);
			}
		}

		new DriverFrame().show();
	}


    public void setUserName(String name) {
        userName = name;
    }
    public void setPassWord(String ps) {
        passWord = ps;
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
			if (object == AllCheckBox)
				AllCheckBox_itemStateChanged(event);
			else if (object == DetachCheckBox)
				DetachCheckBox_itemStateChanged(event);
			if (object == AndButton2)
				AndButton2_itemStateChanged(event);
			else if (object == OrButton2)
				OrButton2_itemStateChanged(event);
			else if (object == LocalCheckBox)
				LocalCheckBox_itemStateChanged(event);
			else if (object == LocalCheckBox1)
				LocalCheckBox1_itemStateChanged(event);
			
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

	void AndButton2_itemStateChanged(java.awt.event.ItemEvent event)
	{
		if(AndButton2.isSelected()) OrButton2.setSelected(false);
	}

	void OrButton2_itemStateChanged(java.awt.event.ItemEvent event)
	{
		if(OrButton2.isSelected()) AndButton2.setSelected(false);
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
			if (object == SearchButton1)
				SearchButton1_actionPerformed(event);
			
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

	void SearchButton1_actionPerformed(java.awt.event.ActionEvent event)
	{
	    if (SearchButton1.getText().equalsIgnoreCase("Halt")) {
	        if (lq!=null) {
	            lq.setStopFlag();
	            lq = null;
	        }
	        SearchButton1.setText("Search");
	    }
	    else {
	    String path;
	    String root = "";
	    String[] paths = {"", "", "", "", "", ""};
	    String op = "and";
	    
	 
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
         
            if (paths[0].length()>0) paths[0] = "//*["+paths[0]+ "]";
            if (paths[1].length()>0) paths[1] = "//*["+paths[1]+ "]";
            if (paths[2].length()>0) paths[2] = "//*["+paths[2]+ "]";
            if (paths[3].length()>0) paths[3] = "//*["+paths[3]+ "]";
            if (paths[4].length()>0) paths[4] = "//*["+paths[4]+ "]";
            if (paths[5].length()>0) paths[5] = "//*["+paths[5]+ "]";
            QueryStringTextArea.setText(paths[0]+"\n"+paths[1]+"\n"+paths[2]+"\n"+paths[3]+"\n"+paths[4]+"\n"+paths[5]);
	        boolean op1 = true;
	        if (op.equalsIgnoreCase("or")) op1 = false;
		     lq = new LocalQuery(paths, op1, SearchButton1);
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
                        File file = new File(filename);
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
        
		     
		     
		     RSScrollPane1.getViewport().add(table);
//		     lq.queryAll();
            lq.start();
		} 
		if(!localonly) {
	        String temp = create_XMLQuery();
	        LogIn();
	        squery_submitToDatabase(temp);
	    }
		
//	LogOut();
    }

	void SearchButton_actionPerformed(java.awt.event.ActionEvent event)
	{
   //      simplequery_submitToDatabase("%NCEAS%");
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
	    
	    if (OrButton2.isSelected()) op = "or";
	    if (TitleCheckBox.isSelected()) {
	        paths[0] = "/eml-dataset/title[contains(text(),\""+TextValue11.getText()+"\")]";
	    }
	    if (AbstractCheckBox.isSelected()) {
	        paths[1] = "/eml-dataset/abstract/paragraph[contains(text(),\""+TextValue11.getText()+"\")]";
	    }
	    if (KeyWordsCheckBox.isSelected()) {
	        paths[2] = "/eml-dataset/keyword_info/keyword[contains(text(),\""+TextValue11.getText()+"\")]";
	    }
	    
	    if (AllCheckBox.isSelected()) {
	        paths[0] = "//*[(contains(text(),\""+TextValue11.getText()+"\"))]";
	        paths[1] = "";
	        paths[2] = "";
	    }
	    if (TextValue22.getText().length()>0) {
            paths[2] = "/eml-dataset/originator/party/party_individual/surname[(contains(text(),\""+TextValue22.getText()+"\"))]";
	    }
        
	    boolean op1 = true;
	    if (op.equalsIgnoreCase("or")) op1 = false;
	    lq = new LocalQuery(paths,op1, SearchButton);
	    
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
                        File file = new File(filename);
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
		if(!localonly) {
	        String temp = create_XMLQuery();
	        LogIn();
	        squery_submitToDatabase(temp);
	    }
		
//	LogOut();
	}
	
	
	String create_XMLQuery() {
	    String out = "";
	 	if(TextValue11.getText().length()>0) {
	    String op = "INTERSECT";
	    if (OrRadioButton.isSelected()) op = "UNION";
	    
		pathqueryXML pqx = new pathqueryXML();
	 if (QueryChoiceTabs.getSelectedIndex()==0) {
		if ((TextChoices2.isVisible())&&(TextValue2.getText().length()>0)) {
		    pqx.add_querygroup(op);
		    pqx.add_querygroup_asChild("UNION");
		}
		else {
		    pqx.add_querygroup("UNION");
		}
		if ((TextChoices11.isVisible())&&(TextValue11.getText().length()>0)) {
		    if (TitleCheckBox.isSelected()) {
		   //     pqx.add_queryterm(TextValue11.getText(),"/eml-dataset/title","contains",true);
		        pqx.add_queryterm(TextValue11.getText(),"title","contains",true);
		    }
		    if (AbstractCheckBox.isSelected()) {
		   //     pqx.add_queryterm(TextValue11.getText(),"/eml-dataset/abstract/paragraph","contains",true);
		        pqx.add_queryterm(TextValue11.getText(),"paragraph","contains",true);
		    }
		    if (KeyWordsCheckBox.isSelected()) {
		   //     pqx.add_queryterm(TextValue11.getText(),"/eml-dataset/keyword_info/keyword","contains",true);
		        pqx.add_queryterm(TextValue11.getText(),"keyword","contains",true);
		    }
		    if (AllCheckBox.isSelected()) {
		        pqx.add_queryterm(TextValue11.getText(),"//*","contains",true);		        
		    }
		}
//		pqx.end_querygroup();
		if ((TextChoices2.isVisible())&&(TextValue2.getText().length()>0)) {
		    pqx.add_querygroup(op);
		    pqx.add_queryterm(TextValue2.getText(),"/eml-dataset/originator/party/party_individual/surname","contains",true);
//		    pqx.end_querygroup();
		}
		pqx.end_query();
		
	 }
	 if (QueryChoiceTabs.getSelectedIndex()==1) {
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
		pqx.end_query();
	 }
		try{
		    out = pqx.get_XML();
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
        System.out.println(out);
		return out;
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
    QueryChoiceTabs.setSelectedIndex(0);
 //   TextMatch1.setSelectedIndex(0);
    TextValue1.setText(searchText);
    simplequery_submitToDatabase(searchText);
    SearchButton1_actionPerformed(null);
}
	
	void DetachCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
        if(DetachCheckBox.isSelected()) {
            RSFrame rs = new RSFrame("Results of Search");
            rs.setVisible(true);
            if (table!=null) {
                TableModel tm = table.getModel();
                rs.JTable1.setModel(tm);
                rs.pack();
            }
        DetachCheckBox.setSelected(false);
        }
    }
	
    public void getConfigData() {
		// Get the configuration file information
    try {
      options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");
      xmlcatalogfile = (String)options.handleGetObject("xmlcatalogfile");
      MetaCatServletURL = (String)options.handleGetObject("MetaCatServletURL");
    }
    catch (Exception e) {System.out.println("Could not locate properties file!");}
	}
	
	public void squery_submitToDatabase(String queryXML) {
	  Properties prop = new Properties();
        prop.put("action","squery");
        prop.put("query",queryXML);
        
        String respType = "xml";
		prop.put("qformat",respType);
      try {
        System.err.println("Trying: " + MetaCatServletURL);
        URL url = new URL(MetaCatServletURL);
        HttpMessage msg = new HttpMessage(url);
        InputStream in = msg.sendPostMessage(prop);

  /*      StringBuffer txt = new StringBuffer();
		    int x;
		    try {
		    while((x=in.read())!=-1) {
		        txt.append((char)x);
		    }
		    }
		    catch (Exception e) {}
		    String txt1 = txt.toString();
		    System.out.println(txt1);
*/
        
        ExternalQuery rq = new ExternalQuery(in);
        RSFrame rs = new RSFrame("Results of Search");
            rs.setVisible(true);
            rs.local=false;
                JTable ttt = rq.getTable();
                TableModel tm = ttt.getModel();
                rs.JTable1.setModel(tm);
                rs.pack();
 
		    in.close();

	  }
      catch (Exception w) {System.out.println("Error in submitting structured query");}
	}

	public void simplequery_submitToDatabase(String query) {
	  Properties prop = new Properties();
        prop.put("action","query");
        prop.put("anyfield",query);
        String respType = "xml";
		prop.put("qformat",respType);
      try {
        System.err.println("Trying: " + MetaCatServletURL);
        URL url = new URL(MetaCatServletURL);
        HttpMessage msg = new HttpMessage(url);
        InputStream in = msg.sendPostMessage(prop);
        ExternalQuery rq = new ExternalQuery(in);
        RSFrame rs = new RSFrame("Results of Search");
            rs.setVisible(true);
            rs.local=false;
                JTable ttt = rq.getTable();
                TableModel tm = ttt.getModel();
                rs.JTable1.setModel(tm);
                rs.pack();
 
		    in.close();

	  }
      catch (Exception w) {System.out.println("Error in submitting simple query");}
	}
	

public void LogIn() {
      Properties prop = new Properties();
       prop.put("action","Login Client");

      // Now try to write the document to the database
      try {
        PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
        String MetaCatServletURL =(String)options.handleGetObject("MetaCatServletURL");     // DFH
        System.err.println("Trying: " + MetaCatServletURL);
        URL url = new URL(MetaCatServletURL);
        HttpMessage msg = new HttpMessage(url);
            prop.put("username", userName);
            prop.put("password",passWord);
        InputStream returnStream = msg.sendPostMessage(prop);
	    StringWriter sw = new StringWriter();
	    int c;
	    while ((c = returnStream.read()) != -1) {
           sw.write(c);
        }
        returnStream.close();
        String res = sw.toString();
        sw.close();
        System.out.println(res);
			 
      } catch (Exception e) {
        System.out.println("Error logging into system");
      }
}

public void LogOut() {
      Properties prop = new Properties();
       prop.put("action","Logout");

      // Now try to write the document to the database
      try {
        PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
        String MetaCatServletURL =(String)options.handleGetObject("MetaCatServletURL");     // DFH
        System.err.println("Trying: " + MetaCatServletURL);
        URL url = new URL(MetaCatServletURL);
        HttpMessage msg = new HttpMessage(url);
        InputStream returnStream = msg.sendPostMessage(prop);
	    StringWriter sw = new StringWriter();
	    int c;
	    while ((c = returnStream.read()) != -1) {
           sw.write(c);
        }
        returnStream.close();
        String res = sw.toString();
        sw.close();
 //       System.out.println(res);
			 
      } catch (Exception e) {
        System.out.println("Error logging out of system");
      }
}



	void LocalCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
		localonly = LocalCheckBox.isSelected();
	}

	void LocalCheckBox1_itemStateChanged(java.awt.event.ItemEvent event)
	{
		localonly = LocalCheckBox1.isSelected();
	}
}