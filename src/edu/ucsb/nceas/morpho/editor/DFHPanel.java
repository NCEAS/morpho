/**
 *       Name: DFHPanel.java
 *    Purpose: Example dynamic editor class for XMLPanel
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-07-13 17:28:59 $'
 * '$Revision: 1.4.2.1 $'
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
package edu.ucsb.nceas.morpho.editor;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;


/**
 * DFHPanel is an example of a class that can be dynamically
 * loaded at run time to edit a specific part of an XML
 * document tree. In particular, this class displays a panel
 * with a custom layout of User information (i.e. name,
 * address, e-mail, etc. It can be loaded at run time by
 * including the class name as an attibute in the XML file.
 * 
 * @author higgins
 */
public class DFHPanel extends JPanel
{
    
	//{{DECLARE_CONTROLS
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JPanel userPanel = new javax.swing.JPanel();
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
	javax.swing.JTextField userName = new javax.swing.JTextField();
	javax.swing.JPanel userInfoPanel = new javax.swing.JPanel();
	javax.swing.JPanel IndividualPanel = new javax.swing.JPanel();
	javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
	javax.swing.JTextField salutation = new javax.swing.JTextField();
	javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
	javax.swing.JTextField givenName = new javax.swing.JTextField();
	javax.swing.JLabel JLabel4 = new javax.swing.JLabel();
	javax.swing.JTextField surName = new javax.swing.JTextField();
	javax.swing.JPanel organizationPanel = new javax.swing.JPanel();
	javax.swing.JLabel JLabel5 = new javax.swing.JLabel();
	javax.swing.JTextField organizationName = new javax.swing.JTextField();
	javax.swing.JPanel positionPanel = new javax.swing.JPanel();
	javax.swing.JLabel JLabel6 = new javax.swing.JLabel();
	javax.swing.JTextField positionName = new javax.swing.JTextField();
	javax.swing.JPanel addressPanel = new javax.swing.JPanel();
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel7 = new javax.swing.JLabel();
	javax.swing.JTextField deliveryPoint = new javax.swing.JTextField();
	javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel8 = new javax.swing.JLabel();
	javax.swing.JTextField city = new javax.swing.JTextField();
	javax.swing.JLabel JLabel9 = new javax.swing.JLabel();
	javax.swing.JTextField administrativeArea = new javax.swing.JTextField();
	javax.swing.JLabel JLabel10 = new javax.swing.JLabel();
	javax.swing.JTextField postalCode = new javax.swing.JTextField();
	javax.swing.JLabel JLabel11 = new javax.swing.JLabel();
	javax.swing.JTextField country = new javax.swing.JTextField();
	javax.swing.JPanel JPanel4 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel12 = new javax.swing.JLabel();
	javax.swing.JTextField phone = new javax.swing.JTextField();
	javax.swing.JPanel JPanel5 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel13 = new javax.swing.JLabel();
	javax.swing.JTextField electronicMailAddress = new javax.swing.JTextField();
    
    public DefaultMutableTreeNode node;
    public Hashtable ht;
    public Hashtable htnodes;
    
 // nodeMap will store the tree node associated with each textfield
    public Hashtable nodeMap;
   
    /** Default constructor. */
    public DFHPanel() {
        this(null);
        }
    
    /**
     * The primary constructor used to dynamically load the class.
     * 
     * @param nd the treenode passed to the object. This node and its children contain
     * all the data that this editor displays in a panel. To pass the edited
     * data back, it must be put into the node objects as editing is done.
     */
    public DFHPanel(DefaultMutableTreeNode nd) {
        this.node = nd;
        nodeMap = new Hashtable();  // textfield key mapped to node
        init();
    }
    
    public void init() {
        ht = getElements(node);
        
		setAlignmentY(0.0F);
		setAlignmentX(0.0F);
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
//		userPanel.setBorder(userBorder);
        userPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("user name"),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
		
		
		userPanel.setAlignmentY(0.0F);
		userPanel.setAlignmentX(0.0F);
		userPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		add(userPanel);
		userPanel.setBounds(0,0,537,61);
		JLabel1.setText("Morpho User Name: ");
		userPanel.add(JLabel1);
		JLabel1.setBounds(10,27,115,15);
		userName.setColumns(20);
		userName.setText(getValue("userName"));
		nodeMap.put(userName,getNode("userName"));
		userName.addFocusListener(new dfhFocus1());
		
		userPanel.add(userName);
		userName.setBounds(130,25,220,19);
        userInfoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("User Information"),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
		
		
		userInfoPanel.setAlignmentY(0.0F);
		userInfoPanel.setAlignmentX(0.0F);
		userInfoPanel.setLayout(new BoxLayout(userInfoPanel,BoxLayout.Y_AXIS));
		add(userInfoPanel);
		userInfoPanel.setBounds(0,61,537,135);
		IndividualPanel.setAlignmentY(0.0F);
		IndividualPanel.setAlignmentX(0.0F);
		IndividualPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		userInfoPanel.add(IndividualPanel);
		IndividualPanel.setBounds(5,20,527,36);
		JLabel2.setText("Salutation: ");
		IndividualPanel.add(JLabel2);
		JLabel2.setBounds(5,7,63,15);
		salutation.setColumns(3);
		salutation.setText(getValue("salutation"));
		nodeMap.put(salutation,getNode("salutation"));
		salutation.addFocusListener(new dfhFocus1());
		
		IndividualPanel.add(salutation);
		salutation.setBounds(73,5,33,19);
		JLabel3.setText("Given Name: ");
		IndividualPanel.add(JLabel3);
		JLabel3.setBounds(111,7,73,15);
		givenName.setColumns(10);
		givenName.setText(getValue("givenName"));
		nodeMap.put(givenName,getNode("givenName"));
		givenName.addFocusListener(new dfhFocus1());
		
		IndividualPanel.add(givenName);
		givenName.setBounds(189,5,110,19);
		JLabel4.setText("surName: ");
		IndividualPanel.add(JLabel4);
		JLabel4.setBounds(304,7,58,15);
		surName.setColumns(10);
		surName.setText(getValue("surName"));
		nodeMap.put(surName,getNode("surName"));
		surName.addFocusListener(new dfhFocus1());
		
		IndividualPanel.add(surName);
		surName.setBounds(367,5,110,19);
		organizationPanel.setAlignmentY(0.0F);
		organizationPanel.setAlignmentX(0.0F);
		organizationPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		userInfoPanel.add(organizationPanel);
		organizationPanel.setBounds(5,56,527,36);
		JLabel5.setText("Organization Name: ");
		organizationPanel.add(JLabel5);
		JLabel5.setBounds(5,7,114,15);
		organizationName.setColumns(30);
		organizationName.setText(getValue("organizationName"));
		nodeMap.put(organizationName,getNode("organizationName"));
		organizationName.addFocusListener(new dfhFocus1());
		
		organizationPanel.add(organizationName);
		organizationName.setBounds(124,5,330,19);
		positionPanel.setAlignmentY(0.0F);
		positionPanel.setAlignmentX(0.0F);
		positionPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		userInfoPanel.add(positionPanel);
		positionPanel.setBounds(5,92,527,36);
		JLabel6.setText("Position Name: ");
		positionPanel.add(JLabel6);
		JLabel6.setBounds(5,7,88,15);
		positionName.setColumns(30);
		positionName.setText(getValue("positionName"));
		nodeMap.put(positionName,getNode("positionName"));
		positionName.addFocusListener(new dfhFocus1());
		
		positionPanel.add(positionName);
		positionName.setBounds(98,5,330,19);
        addressPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Address"),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
		
		
		
		addressPanel.setAlignmentY(0.0F);
		addressPanel.setAlignmentX(0.0F);
		addressPanel.setLayout(new BoxLayout(addressPanel,BoxLayout.Y_AXIS));
		add(addressPanel);
		addressPanel.setBounds(0,196,537,172);
		JPanel2.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		addressPanel.add(JPanel2);
		JPanel2.setBounds(5,20,527,36);
		JLabel7.setText("Street Address:");
		JPanel2.add(JLabel7);
		JLabel7.setBounds(5,7,89,15);
		deliveryPoint.setColumns(30);
		deliveryPoint.setText(getValue("deliveryPoint"));
		nodeMap.put(deliveryPoint,getNode("deliveryPoint"));
		deliveryPoint.addFocusListener(new dfhFocus1());
		
		JPanel2.add(deliveryPoint);
		deliveryPoint.setBounds(99,5,330,19);
		JPanel3.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		addressPanel.add(JPanel3);
		JPanel3.setBounds(5,56,527,36);
		JLabel8.setText("City: ");
		JPanel3.add(JLabel8);
		JLabel8.setBounds(5,7,27,15);
		city.setColumns(12);
		city.setText(getValue("city"));
		
		JPanel3.add(city);
		city.setBounds(37,5,132,19);
		JLabel9.setText("State: ");
		JPanel3.add(JLabel9);
		JLabel9.setBounds(174,7,36,15);
		administrativeArea.setColumns(3);
		administrativeArea.setText(getValue("administrativeArea"));
		
		JPanel3.add(administrativeArea);
		administrativeArea.setBounds(215,5,33,19);
		JLabel10.setText("ZIP: ");
		JPanel3.add(JLabel10);
		JLabel10.setBounds(253,7,24,15);
		postalCode.setColumns(6);
		postalCode.setText(getValue("postalCode"));
		
		JPanel3.add(postalCode);
		postalCode.setBounds(282,5,66,19);
		JLabel11.setText("Country: ");
		JPanel3.add(JLabel11);
		JLabel11.setBounds(353,7,50,15);
		country.setColumns(6);
		country.setText(getValue("country"));
		
		JPanel3.add(country);
		country.setBounds(408,5,66,19);
		JPanel4.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		addressPanel.add(JPanel4);
		JPanel4.setBounds(5,92,527,36);
		JLabel12.setText("Telephone Number: ");
		JPanel4.add(JLabel12);
		JLabel12.setBounds(5,7,113,15);
		phone.setColumns(30);
		phone.setText(getValue("phone"));
		
		JPanel4.add(phone);
		phone.setBounds(123,5,330,19);
		JPanel5.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		addressPanel.add(JPanel5);
		JPanel5.setBounds(5,128,527,36);
		JLabel13.setText("e-mail Address: ");
		JPanel5.add(JLabel13);
		JLabel13.setBounds(5,7,92,15);
		electronicMailAddress.setColumns(30);
		electronicMailAddress.setText(getValue("electronicMailAddress"));
		
		JPanel5.add(electronicMailAddress);
		electronicMailAddress.setBounds(102,5,330,19);
    }
    
/**
 *  given a DefaultMutableTreeNode, build a Hashtable of all elements
 *  that have text i.e. #PCDATA children
 */
 
 public Hashtable getElements(DefaultMutableTreeNode topnode) {
    Hashtable ht = new Hashtable();
    htnodes = new Hashtable(); 
    getElementInfo(topnode,ht, htnodes);
    // debug
    Enumeration eee = ht.keys();
    while(eee.hasMoreElements()) {
        String key = (String)eee.nextElement();
        Vector vvv = (Vector)ht.get(key);
        String obj = "";
        for (Enumeration q=vvv.elements();q.hasMoreElements();) {
            obj = obj+(String)q.nextElement();
        }
//        System.out.println(key+" = "+obj);   
    }
    
    return ht;
 }
 
 public void getElementInfo(DefaultMutableTreeNode node, Hashtable ht, Hashtable htnodes) {
    getData(node, ht, htnodes);
    Enumeration nodes = node.children();
    // loop over child node
    while(nodes.hasMoreElements()) {
        DefaultMutableTreeNode nd = (DefaultMutableTreeNode)(nodes.nextElement());
		NodeInfo info = (NodeInfo)(nd.getUserObject());
        if (!((info.name).equals("#PCDATA"))) {
            getElementInfo(nd,ht,htnodes);    
        }
    }
 }
 
 public void getData(DefaultMutableTreeNode node, Hashtable ht, Hashtable htnodes) {
    Enumeration nodes = node.children();
    // loop over child node
    String txt ="";
    while(nodes.hasMoreElements()) {
        DefaultMutableTreeNode nd = (DefaultMutableTreeNode)(nodes.nextElement());
		NodeInfo info1 = (NodeInfo)(nd.getUserObject());
		if ((info1.name).equals("#PCDATA")) {
		    txt = info1.getPCValue();
        }
        if (txt.length()>0) {
            NodeInfo info2 = (NodeInfo)(node.getUserObject());
            String key = info2.name;
            if (ht.containsKey(key)) {
                Vector ob = (Vector)ht.get(key);
                Vector nds = (Vector)htnodes.get(key);
                ob.addElement(txt);
                nds.addElement(nd);
            }
            else {
                Vector vec = new Vector();
                vec.addElement(txt);
                ht.put(key,vec);
                Vector nds = new Vector();
                nds.addElement(nd);
                htnodes.put(key,nds);
            }
        }
    }
 }

    
 public String getValue(String nodeName) {
    String ret = "";
    Vector v = (Vector)ht.get(nodeName);
    if (v!=null) {
        String tmp = (String)v.firstElement();
        if (tmp!=null) ret = tmp;
    }
    return ret;
 }

 public DefaultMutableTreeNode getNode(String nodeName) {
    DefaultMutableTreeNode ret = null;
    Vector v = (Vector)htnodes.get(nodeName);
    if (v!=null) {
        DefaultMutableTreeNode tmp = (DefaultMutableTreeNode)v.firstElement();
        if (tmp!=null) ret = tmp;
    }
    return ret;
 }
 

 
 
	class dfhFocus1 extends java.awt.event.FocusAdapter
	{
		public void focusLost(java.awt.event.FocusEvent event)
		{
			Object object = event.getSource();
			if (object instanceof JTextField)
				{
				    DefaultMutableTreeNode nd = (DefaultMutableTreeNode)nodeMap.get(object);
		            NodeInfo info = (NodeInfo)(nd.getUserObject());
                    info.setPCValue(((JTextField)object).getText());
//				    System.out.println(((JTextField)object).getText());
//				    if (treeModel!=null) {
//				        treeModel.reload();
//				    }
				}
		}
		
		public void focusGained(java.awt.event.FocusEvent event)
		{
			Object object = event.getSource();
			if (object instanceof JTextField)
				{
//				    int dist = pixelsFromTop((JComponent)object);
	//			    System.out.println("Distance = "+dist);
//				    topPanel.scrollRectToVisible(new Rectangle(0,dist,50,50));
				}
		}
	}
 
}