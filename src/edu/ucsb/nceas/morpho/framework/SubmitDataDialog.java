/**
 *        Name: SubmitDataDialog.java
 *     Purpose: A Class that is a dialog for submitting DATA to the server
 *		application (searchs local collection of XML files
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: SubmitDataDialog.java,v 1.2 2000-12-22 20:49:35 higgins Exp $'
 */

package edu.ucsb.nceas.dtclient;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

public class SubmitDataDialog extends javax.swing.JDialog implements ContentHandler
{
    private Stack elementStack = null;
    private String successMessage = "";
    private String errorMessage = "";
    ClientFramework container = null;
    String userName = "public";
    String passWord = "none";
    Socket echoSocket = null;
    OutputStream out = null;
    InputStream in = null;
    private boolean writeSucceeded = false;
    String documentID;
    String parserName = "org.apache.xerces.parsers.SAXParser";
    String shortFileName = "";
	public SubmitDataDialog(Frame parent)
	{
		super(parent);
		
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setTitle("Data Storage on Server");
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(568,308);
		setVisible(false);
		GetFilePanel.setLayout(new GridLayout(4,1,0,0));
		getContentPane().add(BorderLayout.NORTH,GetFilePanel);
		GetFilePanel.setBounds(0,0,568,140);
		JPanel3.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		GetFilePanel.add(JPanel3);
		JPanel3.setBounds(0,0,568,35);
		JLabel1.setText("File");
		JPanel3.add(JLabel1);
		JLabel1.setBounds(20,10,19,15);
		FileNameTextField.setColumns(30);
		FileNameTextField.setText("FileName of data to be sent to server should appear here");
		JPanel3.add(FileNameTextField);
		FileNameTextField.setBounds(44,8,330,19);
		FileMetadataTextField.setColumns(30);
		FileMetadataTextField.setText("./file_template.xml");
		JPanel3.add(FileMetadataTextField);
		FileMetadataTextField.setBounds(159,5,330,19);
		SelectFile.setText("Select File...");
		SelectFile.setActionCommand("Select File...");
		JPanel3.add(SelectFile);
		SelectFile.setBounds(379,5,101,25);
		VirtualFileCheckBox.setToolTipText("If set, only a reference to the data file is inserted.");
		VirtualFileCheckBox.setText("Virtual");
		JPanel3.add(VirtualFileCheckBox);
		VirtualFileCheckBox.setBounds(485,6,62,23);
		JPanel4.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		GetFilePanel.add(JPanel4);
		JPanel4.setBounds(0,35,568,35);
		JLabel2.setText("File Metadata");
		JPanel4.add(JLabel2);
		JLabel2.setBounds(79,7,75,15);
		JPanel4.add(FileMetadataTextField);
		JPanel5.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		GetFilePanel.add(JPanel5);
		JPanel5.setBounds(0,70,568,35);
		JLabel3.setText("Resource Metadata");
		JPanel5.add(JLabel3);
		JLabel3.setBounds(61,7,111,15);
		ResourceMetadataTextField.setColumns(30);
		ResourceMetadataTextField.setText("./resource_template.xml");
		JPanel5.add(ResourceMetadataTextField);
		ResourceMetadataTextField.setBounds(177,5,330,19);
		JPanel6.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		GetFilePanel.add(JPanel6);
		JPanel6.setBounds(0,105,568,35);
		JLabel4.setText("Package Name");
		JPanel6.add(JLabel4);
		JLabel4.setBounds(73,7,86,15);
		PackageNameTextField.setColumns(30);
		PackageNameTextField.setText("packageExample");
		JPanel6.add(PackageNameTextField);
		PackageNameTextField.setBounds(164,5,330,19);
		JPanel1.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		getContentPane().add(BorderLayout.SOUTH,JPanel1);
		JPanel1.setBounds(0,273,568,35);
		SubmitDataButton.setText("Submit");
		SubmitDataButton.setActionCommand("Submit");
		JPanel1.add(SubmitDataButton);
		SubmitDataButton.setBounds(207,5,75,25);
		CancelButton.setText("Cancel");
		CancelButton.setActionCommand("Cancel");
		JPanel1.add(CancelButton);
		CancelButton.setBounds(287,5,73,25);
		JPanel2.setAlignmentY(0.466667F);
		JPanel2.setLayout(new BoxLayout(JPanel2,BoxLayout.X_AXIS));
		getContentPane().add(BorderLayout.CENTER,JPanel2);
		JPanel2.setBounds(0,140,568,133);
		SubmitDataTextArea.setEditable(false);
		SubmitDataTextArea.setWrapStyleWord(true);
		SubmitDataTextArea.setText("Select File to be sent to server for centralized storage using \"Select File...\" button and then submit using the \"Submit\" button");
		SubmitDataTextArea.setLineWrap(true);
		JPanel2.add(SubmitDataTextArea);
		SubmitDataTextArea.setBounds(0,0,568,133);
		//}}
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		SelectFile.addActionListener(lSymAction);
		CancelButton.addActionListener(lSymAction);
		SubmitDataButton.addActionListener(lSymAction);
		//}}
	}

	public SubmitDataDialog()
	{
		this((Frame)null);
	}

	public SubmitDataDialog(String sTitle)
	{
		this();
		setTitle(sTitle);
	}
	
	public SubmitDataDialog(ClientFramework cf) {
		this((Frame)null);
	    container = cf;
	    userName = cf.userName;
	    passWord = cf.passWord;
	    this.setModal(true);
	    
	}

	public void setVisible(boolean b)
	{
		if (b)
			setLocation(50, 50);
		super.setVisible(b);
	}

	static public void main(String args[])
	{
		(new SubmitDataDialog()).setVisible(true);
	}

	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets
		Insets insets = getInsets();
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JPanel GetFilePanel = new javax.swing.JPanel();
	javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
	javax.swing.JTextField FileNameTextField = new javax.swing.JTextField();
	javax.swing.JButton SelectFile = new javax.swing.JButton();
	javax.swing.JCheckBox VirtualFileCheckBox = new javax.swing.JCheckBox();
	javax.swing.JPanel JPanel4 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
	javax.swing.JTextField FileMetadataTextField = new javax.swing.JTextField();
	javax.swing.JPanel JPanel5 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
	javax.swing.JTextField ResourceMetadataTextField = new javax.swing.JTextField();
	javax.swing.JPanel JPanel6 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel4 = new javax.swing.JLabel();
	javax.swing.JTextField PackageNameTextField = new javax.swing.JTextField();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JButton SubmitDataButton = new javax.swing.JButton();
	javax.swing.JButton CancelButton = new javax.swing.JButton();
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JTextArea SubmitDataTextArea = new javax.swing.JTextArea();
	//}}


	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == SelectFile)
				SelectFile_actionPerformed(event);
			else if (object == CancelButton)
				CancelButton_actionPerformed(event);
			else if (object == SubmitDataButton)
				SubmitDataButton_actionPerformed(event);
		}
	}

	void SelectFile_actionPerformed(java.awt.event.ActionEvent event)
	{
		try {
			// saveFileDialog Show the FileDialog
			container.openFileDialog.setVisible(true);
		} catch (Exception e) {}
		String file = container.openFileDialog.getFile();
		if (file!=null) {
		    shortFileName = file;
		    file = container.openFileDialog.getDirectory() + file;
		    FileNameTextField.setText(file);
		}
	}

	void CancelButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		dispose();
	}
	
	void SubmitDataButton_actionPerformed(java.awt.event.ActionEvent event)
	{
	    if (container.userName.equals("public")) {
	        JOptionPane.showMessageDialog(this,"You must be logged in as a registered user to insert data into the system catalog!");
	    }
	    else if (FileNameTextField.getText().startsWith("FileName of data to be sent to server"))
	        {JOptionPane.showMessageDialog(this,"Please enter a file name.");}
	         
	    else {
	  String dataURL = FileNameTextField.getText();      
	  if (!VirtualFileCheckBox.isSelected()) {     
	    // need to change 'hardwired' SendFile parameters
	    dataURL = SendFile(FileNameTextField.getText(),"dev.nceas.ucsb.edu",4444);
	  }
		// now insert the dataURL into the resourcetemplate file
		String resourcemetadatafile = "./resource_template.xml";
		if (!ResourceMetadataTextField.getText().equals("")) {
		   resourcemetadatafile = ResourceMetadataTextField.getText();} 
		ReplaceFile(resourcemetadatafile, "datasetid", dataURL);
		ReplaceFile(resourcemetadatafile, "title", shortFileName);
		String resourceID = insertIntoMetacat(resourcemetadatafile);
	//	System.out.println("ID of resource_template file is :"+resourceID);
	    String filemetadatafile = "./file_template.xml";
		if (!FileMetadataTextField.getText().equals("")) {
		   resourcemetadatafile = FileMetadataTextField.getText();} 
		ReplaceFile(filemetadatafile, "file_name", shortFileName);
		String fileID = insertIntoMetacat(filemetadatafile);
	//	System.out.println("ID of file_template file is :"+fileID);
		String pack = buildPackage(resourceID, "ismetadatafor", dataURL, fileID, "ismetadatafor", dataURL);
		String packID = insertStringIntoMetacat(pack);
	//	System.out.println("ID of packagefile is :"+packID);
		String ids = dataURL+"\nfileID = "+fileID+"\nresourceID = "+resourceID+"\npackage ID "+packID;
		SubmitDataTextArea.setText(ids);
		
		}
	}
	
	String buildPackage(String subj1, String rel1, String obj1, String subj2, String rel2, String obj2) {
	    StringBuffer pack = new StringBuffer();
	    pack.append("<?xml version=\"1.0\"?>\n");
	    pack.append("<!DOCTYPE package PUBLIC \"-//NCEAS//package//EN\" \"http://dev.nceas.ucsb.edu/berkley/dtd/package.dtd\">\n");
        pack.append("<package>\n");
        String packagename = "packageExample";
        if (!PackageNameTextField.getText().equals("")) {
            packagename = PackageNameTextField.getText(); }
        pack.append("<name>"+packagename+"</name>\n");
        pack.append("<relation>\n");
        pack.append("<subject>"+subj1+"</subject>\n");
        pack.append("<relationship>"+rel1+"</relationship>\n");
        pack.append("<object>"+obj1+"</object>\n");
        pack.append("</relation>\n");
        pack.append("<relation>\n");
        pack.append("<subject>"+subj2+"</subject>\n");
        pack.append("<relationship>"+rel2+"</relationship>\n");
        pack.append("<object>"+obj2+"</object>\n");
        pack.append("</relation>\n");
        pack.append("</package>\n");
        
        return (pack.toString());
	}
	
	/**
	 * Actual method for sending file to server using TCP socket
	 * protocol. First sends file name, terminated by a '0'; then
	 * sends file in 1024 size buffers so large files never need to
	 * be completely in memory
	 * 
	 * @param filename file name to be sent
	 * @param host host name where server is running
	 * @param port port number used by server (4444 by default)
	 */
	
	String SendFile(String filename, String host, int port) {
		String res = "return";
        try {
            echoSocket = new Socket(host, port);
            out = echoSocket.getOutputStream();
            in = echoSocket.getInputStream();
        } catch (UnknownHostException e) {
                      System.err.println("Don't know about host: "+host);
                      System.exit(1);
         } catch (IOException e) {
                      System.err.println("Couldn't get I/O for "
                                         + "the connection to: "+host);
                      System.exit(1);
         }
	    
	    
	  try{  
        File file = new File(filename);
        DataOutputStream dsout = new DataOutputStream(out);
        FileInputStream fs = new FileInputStream(file);
        // first convert the filename to a byte array
        String fn = file.getName();
        byte[] str = fn.getBytes();
        // now write the string bytes followed by a '0'
        for (int i=0;i<str.length;i++) {
            dsout.write(str[i]);    
        }
        dsout.write(0);  // marks end of name info
        
        // now read response from server
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader rin = new BufferedReader(isr);
        res = rin.readLine();
	String res1 = res.substring(0,4);  // should be 'http'
        if (res1.equalsIgnoreCase("http")) {
            // now send the file data
            byte[] buf = new byte[1024];
            int cnt = 0;
            int i = 0;
            while (cnt!=-1) {
                cnt = fs.read(buf);
                System.out.println("i = "+i+" Bytes read = "+cnt);
                if (cnt!=-1) {
                    dsout.write(buf, 0, cnt);
                }
                i++;
        }
        }
            fs.close();
            dsout.flush();
            dsout.close();
	  }
	  catch (Exception w) {}

	return res;
	}
	
	
/*
// routine to reproduce an XML input stream with the value of a single
// tag changed to a new value. To be used to replace id tags
*/
public static void replaceXMLText(InputStream is, OutputStream os, String tagname, String replacement) {
    BufferedReader in = new BufferedReader(new InputStreamReader(is));
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));

    try {
        String line = in.readLine();
        while (line!=null) {
            if (line.indexOf(tagname)>=0) {   // tag name found!
                int iii = line.indexOf(tagname)+tagname.length();
                int jjj = line.indexOf(">",iii); // end of start tag
                while (jjj<0) {   // end of start tag not on this line
                    line = line + in.readLine();
                    jjj = line.indexOf(">",iii);
                }
                String beginString = line.substring(0,jjj+1);
                
                int kkk = line.indexOf("</",jjj+1);
                while (kkk<0) {   // beginning of tag not on this line
                    line = line + in.readLine();
                    kkk = line.indexOf("</",jjj+1);
                }
                String endString = line.substring(kkk, line.length());
                String outputString = beginString+replacement+endString;
                out.write(outputString,0,outputString.length());
                out.newLine();
            }
            else {
                out.write(line, 0, line.length());
                out.newLine();
            }
            line = in.readLine();  // next line
        }
        
        out.flush();
        out.close();
        in.close();
    }
    catch (Exception e) {System.out.println("Error replacing XMLText!");}
}

public void ReplaceFile(String file_in, String tag, String newid) {
    File infile = new File(file_in);
    String file_out = file_in+"tmp";
    File outfile = new File(file_out);
    try{
    FileInputStream instream = new FileInputStream(infile);
    FileOutputStream outstream = new FileOutputStream(outfile);
    replaceXMLText(instream,outstream,tag,newid);
    }
    catch (Exception e) {}
    infile.delete();
    outfile.renameTo(infile);
}	

// retrieves the current value of a named tag (the id tag)
public String getXMLID(InputStream is, String tagname) {
    BufferedReader in = new BufferedReader(new InputStreamReader(is));
    try {
        String line = in.readLine();
        while (line!=null) {
            if (line.indexOf(tagname)>=0) {   // tag name found!
                int iii = line.indexOf(tagname)+tagname.length();
                int jjj = line.indexOf(">",iii); // end of start tag
                while (jjj<0) {   // end of start tag not on this line
                    line = line + in.readLine();
                    jjj = line.indexOf(">",iii);
                }
                
                int kkk = line.indexOf("</",jjj+1);
                while (kkk<0) {   // beginning of tag not on this line
                    line = line + in.readLine();
                    kkk = line.indexOf("</",jjj+1);
                }
                String outputString = line.substring(jjj+1,kkk);
                outputString.trim();
                in.close();
                return outputString;
            }
            line = in.readLine();  // next line
        }
        
        in.close();
    }
    catch (Exception e) {System.out.println("Error replacing XMLText!");}
    return null;
    
}

public String getIDFromFile(String file_in, String tag) {
    File infile = new File(file_in);
    try{
        FileInputStream instream = new FileInputStream(infile);
        if (instream==null) System.out.println("instream is null!!");
        String ID = getXMLID(instream, tag);
        return ID;
    }
    catch (Exception e) {
        return null;
        }
}

public String insertStringIntoMetacat(String xmlstring) {
        String res = "error";
        try {
		    URL url = new URL(container.MetaCatServletURL);
		    HttpMessage msg = new HttpMessage(url);
		    Properties prop = new Properties();
		    prop.put("action","insert");
		    prop.put("doctext",xmlstring);
		    InputStream inn = msg.sendPostMessage(prop);
	
        // Determine the assigned docid if insert successful
        XMLReader parser = null;

        // Set up the SAX document handlers for parsing
  //      try {
          // Get an instance of the parser
          parser = XMLReaderFactory.createXMLReader(parserName);
          // Set the ContentHandler to this instance
          parser.setContentHandler(this);
          parser.parse(new InputSource(inn));
        } catch (Exception e) {
           System.err.println(e.toString());
        }

        if (writeSucceeded) {
            res = documentID;     
        }
  return res;
    
}
	
	
public String insertIntoMetacat(String filename) {
        String res = "error";
        StringBuffer txt = new StringBuffer();
      try{  
		FileReader fr = new FileReader(filename);
		int x;
		while((x=fr.read())!=-1) {
		    txt.append((char)x);
		 }
		 fr.close();
		    URL url = new URL(container.MetaCatServletURL);
		    HttpMessage msg = new HttpMessage(url);
		    Properties prop = new Properties();
		    prop.put("action","insert");
		    prop.put("doctext",txt.toString());
		    InputStream inn = msg.sendPostMessage(prop);
	
        // Determine the assigned docid if insert successful
        XMLReader parser = null;

        // Set up the SAX document handlers for parsing
  //      try {
          // Get an instance of the parser
          parser = XMLReaderFactory.createXMLReader(parserName);
          // Set the ContentHandler to this instance
          parser.setContentHandler(this);
          parser.parse(new InputSource(inn));
        } catch (Exception e) {
           System.err.println(e.toString());
        }

        if (writeSucceeded) {
            res = documentID;     
        }
  return res;
}



    public void startElement (String uri, String localName,
                              String qName, Attributes atts)
           throws SAXException {
      elementStack.push(localName);
      if (localName.equals("success")) {
        writeSucceeded = true;
      }
    }
  
    public void endElement (String uri, String localName,
                            String qName) throws SAXException {
      String leaving = (String)elementStack.pop();
    }
  
    public void characters(char ch[], int start, int length) {
  
      String inputString = new String(ch, start, length);
      String currentTag = (String)elementStack.peek();
      if (currentTag.equals("docid")) {
        documentID = inputString;
      }
      if (currentTag.equals("success")) {
        successMessage = inputString;
      }
      if (currentTag.equals("error")) {
        errorMessage = inputString;
      }
    }

   public void startDocument() throws SAXException { 
     elementStack = new Stack();
   }

   public void endDocument() throws SAXException { }
   public void ignorableWhitespace(char[] cbuf, int start, int len) { }
   public void skippedEntity(String name) throws SAXException { }
   public void processingInstruction(String target, String data) throws SAXException { }
   public void startPrefixMapping(String prefix, String uri) throws SAXException { }
   public void endPrefixMapping(String prefix) throws SAXException { }
   public void setDocumentLocator (Locator locator) { }

}