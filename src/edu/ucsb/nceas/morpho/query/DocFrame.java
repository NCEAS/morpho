/**
 *        Name: DocFrame.java
 *     Purpose: A Class for creating Displaying a specific document
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: DocFrame.java,v 1.3 2000-08-07 23:43:23 higgins Exp $'
 */


package edu.ucsb.nceas.querybean;

import java.awt.*;
import javax.swing.*;
import java.net.URL;
import java.io.*;
import com.arbortext.catalog.*;
import org.xml.sax.SAXException;
import org.apache.xalan.xslt.XSLTProcessorFactory;
import org.apache.xalan.xslt.XSLTInputSource;
import org.apache.xalan.xslt.XSLTResultTarget;
import org.apache.xalan.xslt.XSLTProcessor;
import org.apache.xalan.xpath.xml.*;

public class DocFrame extends javax.swing.JFrame
{
    File file;
	public DocFrame()
	{
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(405,305);
		DocControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		getContentPane().add(BorderLayout.NORTH,DocControlPanel);
		DocControlPanel.setBounds(0,0,20,40);
		TransformButton.setText("Transform");
		TransformButton.setActionCommand("Transform");
		DocControlPanel.add(TransformButton);
		TransformButton.setBounds(0,0,35,40);
		JTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
		getContentPane().add(BorderLayout.CENTER,JTabbedPane1);
		JTabbedPane1.setBounds(0,0,405,305);
		JScrollPane1.setOpaque(true);
		JTabbedPane1.add(JScrollPane1);
		JScrollPane1.setBounds(2,2,400,275);
		JScrollPane1.setVisible(false);
		JScrollPane1.getViewport().add(XMLText);
		XMLText.setBounds(0,0,397,272);
		JScrollPane2.setOpaque(true);
		JTabbedPane1.add(JScrollPane2);
		JScrollPane2.setBounds(0,0,20,40);
		JScrollPane2.setVisible(false);
		JScrollPane2.getViewport().add(HTMLPane);
		HTMLPane.setBounds(0,0,20,40);
		JPanel1.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JTabbedPane1.add(JPanel1);
		JPanel1.setBounds(2,2,400,275);
		JPanel1.setVisible(false);
		JTabbedPane1.setSelectedComponent(JScrollPane1);
		JTabbedPane1.setSelectedIndex(0);
		JTabbedPane1.setTitleAt(0,"Text");
		JTabbedPane1.setTitleAt(1,"HTML");
		JTabbedPane1.setTitleAt(2,"Tree");
		//}}

		//{{INIT_MENUS
		//}}
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		TransformButton.addActionListener(lSymAction);
		//}}
	}

	public DocFrame(String sTitle)
	{
		this();
		setTitle(sTitle);
	}

	public DocFrame(String sTitle, String doctext)
	{
		this();
		setTitle(sTitle);
		XMLText.setText(doctext);
	}
	
	public DocFrame(File file)
	{
	    this();
	    this.file = file;
	}
	
	public void setFile(File f) {
	    file = f;
	}

	public void setVisible(boolean b)
	{
		if (b)
			setLocation(50, 50);
		super.setVisible(b);
	}

	static public void main(String args[])
	{
		(new DocFrame()).setVisible(true);
	}

	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets and menu bar
		Insets insets = getInsets();
		javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
		int menuBarHeight = 0;
		if (menuBar != null)
			menuBarHeight = menuBar.getPreferredSize().height;
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JPanel DocControlPanel = new javax.swing.JPanel();
	javax.swing.JButton TransformButton = new javax.swing.JButton();
	javax.swing.JTabbedPane JTabbedPane1 = new javax.swing.JTabbedPane();
	javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JTextArea XMLText = new javax.swing.JTextArea();
	javax.swing.JScrollPane JScrollPane2 = new javax.swing.JScrollPane();
	javax.swing.JEditorPane HTMLPane = new javax.swing.JEditorPane();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	//}}

	//{{DECLARE_MENUS
	//}}
	
	
public void writeInfo() {
    try{
    FileReader in = new FileReader(file);
    StringWriter out = new StringWriter();
    int c;
    while ((c = in.read()) != -1) {
        out.write(c);
        }
        in.close();
        out.close();
    XMLText.setText(out.toString());
    }
	catch (Exception e) {;}
    
    }

	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == TransformButton)
				TransformButton_actionPerformed(event);
		}
	}

	void TransformButton_actionPerformed(java.awt.event.ActionEvent event)
	{
        CatalogEntityResolver cer = new CatalogEntityResolver();
        String xmlcatalogfile = "./catalog/catalog"; 
        
        try {
            Catalog myCatalog = new Catalog();
            myCatalog.loadSystemCatalogs();
            myCatalog.parseCatalog(xmlcatalogfile);
            cer.setCatalog(myCatalog);
        }
        catch (Exception e) {System.out.println("Problem creating Catalog!");}
	    
	try{
    // Have the XSLTProcessorFactory obtain a interface to a
    // new XSLTProcessor object.
    XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
    XMLParserLiaison pl = processor.getXMLProcessorLiaison();
    pl.setEntityResolver(cer);

    // Have the XSLTProcessor processor object transform "foo.xml" to
    // System.out, using the XSLT instructions found in "foo.xsl".
    processor.process(new XSLTInputSource(file.getAbsolutePath()),
                      new XSLTInputSource("eml-dataset-display.xsl"),
                      new XSLTResultTarget("html.out"));
    File html = new File("html.out");
    HTMLPane.setPage("file:///"+html.getAbsolutePath());
    JTabbedPane1.setSelectedIndex(1);
    }
    catch (Exception w) {}
	}
		
			 
	
}