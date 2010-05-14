/**
 *       Name: SaveDialog.java
 *    Purpose: Visual display for Export Choices
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-11-13 00:26:50 $'
 * '$Revision: 1.31 $'
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
package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.SaveEvent;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Vector;


import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.SwingConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xalan.templates.OutputProperties;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.ucsb.nceas.morpho.util.XMLUtil;

/**
 * A dialog box for user choice of export options
 */
public class SaveDialog extends JDialog
{

  /** Control button */
  private JButton executeButton = null;
  private JButton cancelButton = null;

  private boolean showPackageFlag = true;


  /** Radio button */
  private JCheckBox localLoc = new JCheckBox("Save Locally");
  private JCheckBox networkLoc = new JCheckBox("Save to Network.");
  private JCheckBox upgradeEml = new JCheckBox("Upgrade to latest EML (" +
                                       EML200DataPackage.LATEST_EML_VER + ")");

  private static final int PADDINGWIDTH = 8;
  private static String WARNING =
      "Please choose where you would like to save the data package.";

  /** A reference to morpho frame */
  MorphoFrame morphoFrame = null;

  /** A string indicating the morpho frame's type*/
  String morphoFrameType = null;


  /** selected docid  */
  String selectDocId = null;

  /** flag to indicate selected data package has local copy */
  private boolean inLocal = false;

  /** flag to indicate selected data package has local copy */
  private boolean inNetwork = false;


  /** the AbstractDataPackage object to be saved  */
  AbstractDataPackage adp = null;


  /**
   * Construct a new instance of the dialog where parent is morphoframe
   *
   */
  public SaveDialog(AbstractDataPackage adp)
  {
	  super(UIController.getInstance().getCurrentActiveWindow());
    setModal(true);
    this.adp = adp;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    initialize(morphoFrame);

  }

  public SaveDialog(AbstractDataPackage adp, boolean showPackageFlag)
  {
    this(adp);
    this.showPackageFlag = showPackageFlag;
  }

  /** Method to initialize save dialog */
  private void initialize(Window parent)
  {
     // Set OpenDialog size depent on parent size
    int parentWidth = parent.getWidth();
    int parentHeight = parent.getHeight();
    int dialogWidth = 400;
    int dialogHeight = 270;
    setSize(dialogWidth, dialogHeight);
    setResizable(false);

    // Set location of dialog, it shared same center of parent
    double parentX = parent.getLocation().getX();
    double parentY = parent.getLocation().getY();
    double centerX = parentX + 0.5 * parentWidth;
    double centerY = parentY + 0.5 * parentHeight;
    int dialogX = (new Double(centerX - 0.5 * dialogWidth)).intValue();
    int dialogY = (new Double(centerY - 0.5 * dialogHeight)).intValue();
    setLocation(dialogX, dialogY);

    setTitle("Save Current DataPackage");
    // Set the default close operation is dispose
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    // Set the border layout as layout
    getContentPane().setLayout(new BorderLayout(0, 0));
     // Add padding for left and right
    getContentPane().add(BorderLayout.EAST,
                                      Box.createHorizontalStrut(PADDINGWIDTH));
    getContentPane().add(BorderLayout.WEST,
                                      Box.createHorizontalStrut(PADDINGWIDTH));



    // Create JPanel and set it border layout
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(0, 0));

    // Create note box and add it to the north of mainPanel
    Box noteBox = Box.createVerticalBox();
    noteBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    JLabel note = WidgetFactory.makeHTMLLabel(WARNING, 2);
/*    JTextArea note = new JTextArea(WARNING);
    note.setEditable(false);
    note.setLineWrap(true);
    note.setWrapStyleWord(true);
    note.setOpaque(false);
*/
    noteBox.add(note);
    noteBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    mainPanel.add(BorderLayout.NORTH, noteBox);

    // Create a radio box
    Box radioBox = Box.createVerticalBox();
    radioBox.add(localLoc);
    radioBox.add(networkLoc);
    radioBox.add(upgradeEml);

    // create another center box which will put radion box in the center
    // and it will be add into center of mainPanel
    Box centerBox = Box.createHorizontalBox();
    centerBox.add(Box.createHorizontalGlue());
    centerBox.add(radioBox);
    centerBox.add(Box.createHorizontalGlue());
    mainPanel.add(BorderLayout.CENTER, centerBox);

    // Finish mainPanel and add it the certer of contentpanel
    getContentPane().add(BorderLayout.CENTER, mainPanel);

    // Create bottom box
    Box bottomBox = Box.createVerticalBox();
    getContentPane().add(BorderLayout.SOUTH, bottomBox);
    //Create padding between result panel and Contorl button box
    bottomBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    // Create a controlbuttionBox
    Box controlButtonsBox = Box.createHorizontalBox();
    controlButtonsBox.add(Box.createHorizontalGlue());

    // Save button
    executeButton = new JButton("Save");
    controlButtonsBox.add(executeButton);
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));

    //Cancel button
    cancelButton = new JButton("Cancel");
    controlButtonsBox.add(cancelButton);
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));

    // Add controlButtonsBox to bottomBox
    bottomBox.add(controlButtonsBox);
    // Add the margin between controlButtonPanel to the bottom line
    bottomBox.add(Box.createVerticalStrut(10));

		SymAction lSymAction = new SymAction();
		executeButton.addActionListener(lSymAction);
		cancelButton.addActionListener(lSymAction);

    String location = adp.getLocation();
    if (location.equals("")) {  // never been saved
      localLoc.setEnabled(true);
      networkLoc.setEnabled(true);
      localLoc.setSelected(true);
      networkLoc.setSelected(false);
    }
    else if (location.equals(AbstractDataPackage.LOCAL)) {
      localLoc.setEnabled(false);
      networkLoc.setEnabled(true);
      localLoc.setSelected(false);
      networkLoc.setSelected(true);
    }
    else if (location.equals(AbstractDataPackage.METACAT)) {
      localLoc.setEnabled(true);
      networkLoc.setEnabled(false);
      localLoc.setSelected(true);
      networkLoc.setSelected(false);
    }
    else if (location.equals(AbstractDataPackage.BOTH)) {
      localLoc.setEnabled(false);
      networkLoc.setEnabled(false);
      localLoc.setSelected(false);
      networkLoc.setSelected(false);
    }

    try {
        /*String emlVersion = ((EML200DataPackage)adp).getEMLVersion();
        Log.debug(10, "\n\n**********Got the EML version: " + emlVersion);
        boolean askUpgrade = (emlVersion.toLowerCase()
                             .indexOf(EML200DataPackage.LATEST_EML_VER) == -1);*/
    	boolean askUpgrade = false;
    	if (!((EML200DataPackage)adp).isLatestEMLVersion())
    	{
    		askUpgrade = true;
    	}
        // show and check the checkbox for upgrading EML if not latest version
        upgradeEml.setSelected(askUpgrade);
        upgradeEml.setVisible(askUpgrade);

    } catch (Exception e) {
        Log.debug(30, "Couldn't get EML version: " + e.getMessage());
    }

    setVisible(true);

  }

    /** Method to enable executeButton and assign command */
   private void enableExecuteButton(Object object, JDialog dialog)
   {
   }//enableExecuteButton


	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == executeButton) {
				executeButton_actionPerformed(event);
      }
      else if (object == cancelButton) {
        cancelButton_actionPerformed(event);
      }
		}
	}

  void cancelButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		this.setVisible(false);
		this.dispose();
	}

  void executeButton_actionPerformed(java.awt.event.ActionEvent event)
	{
    Component comp = morphoFrame.getContentComponent();
    if (comp instanceof DataViewContainerPanel) {
      ((DataViewContainerPanel)comp).saveDataChanges();
    }

    boolean problem = false;
    Morpho morpho = Morpho.thisStaticInstance;
    String location = adp.getLocation();
    // track the save event
    SaveEvent saveEvent = new SaveEvent(this, StateChangeEvent.SAVE_DATAPACKAGE);
    String id = adp.getAccessionNumber();
    // initial id
    saveEvent.setInitialId(id);
    if (location.equals("")) { // only update version if new
      try {
          if (upgradeEml.isSelected()) {
              // change the XML
             //((EML200DataPackage)adp).setEMLVersion(EML200DataPackage.LATEST_EML_VER);
        	  /*String newString = doTransform("./xsl/eml201to210.xsl", XMLUtil.getDOMTreeAsString(
                      adp.getMetadataNode().getOwnerDocument())) ;*/
        	  String newString = ((EML200DataPackage)adp).transformToLastestEML();
              if (newString != null)
              {
	              /*Log.debug(15, "Reloading ADP with its own XML:\n" +
	                        XMLUtil.getDOMTreeAsString(
	                                newNode.getOwnerDocument())
	                                .substring(0, 512) + ".............");*/
	              
	
	              // create a new data package instance with the altered XML
	              adp = (EML200DataPackage)DataPackageFactory.getDataPackage(
	                      new java.io.StringReader(newString), false, true);
                  ((EML200DataPackage)adp).setEMLVersion(EML200DataPackage.LATEST_EML_VER);
              }
              else
              {
            	  JOptionPane.showMessageDialog(morphoFrame, "Morpho couldn't transform it to the newest version of EML.", "Information",
                          JOptionPane.INFORMATION_MESSAGE);
              }

          }
      } catch (Exception ex) {
    	  ex.printStackTrace();
          Log.debug(30, "Problem setting new EML version: " + ex.toString());
      }

      try{
        if (id.indexOf(AccessionNumber.TEMP)>-1) {
          AccessionNumber an = new AccessionNumber(morpho);
          String nextid = an.getNextId();
          adp.setAccessionNumber(nextid);
        } else {
          AccessionNumber an = new AccessionNumber(morpho);
          String newid = an.incRev(id);
          adp.setAccessionNumber(newid);
        }
      }
      catch (Exception www) {
        // no valid accession number; thus create one
        AccessionNumber an = new AccessionNumber(morpho);
        String nextid = an.getNextId();
        adp.setAccessionNumber(nextid);
      }
    }

    try{
      if ((localLoc.isSelected())&&(localLoc.isEnabled())
          &&(networkLoc.isSelected())&&(networkLoc.isEnabled())) {
    	adp.serializeData(AbstractDataPackage.BOTH);
        adp.serialize(AbstractDataPackage.BOTH);
        if (adp.getSerializeLocalSuccess() && adp.getSerializeMetacatSuccess())
        {
        	adp.setLocation(AbstractDataPackage.BOTH);
        }
        else if(adp.getSerializeLocalSuccess())
        {
        	adp.setLocation(AbstractDataPackage.LOCAL);
        }
        else if(adp.getSerializeMetacatSuccess())
        {
        	adp.setLocation(AbstractDataPackage.METACAT);
        }
        else
        {
        	adp.setLocation("");
        }
        
      }
      else if ((localLoc.isSelected())&&(localLoc.isEnabled())) {
    	adp.serializeData(AbstractDataPackage.LOCAL);
        adp.serialize(AbstractDataPackage.LOCAL);
        if (adp.getSerializeLocalSuccess())
        {
        	adp.setLocation(AbstractDataPackage.LOCAL);
        }
        else
        {
        	adp.setLocation("");
        }
        
      }
      else if ((networkLoc.isSelected())&&(networkLoc.isEnabled())) {
        adp.serializeData(AbstractDataPackage.METACAT);        
        adp.serialize(AbstractDataPackage.METACAT);
        if(adp.getSerializeMetacatSuccess())
        {
        	adp.setLocation(AbstractDataPackage.METACAT);
        }
        else if(adp.getLocation() != null && adp.getLocation().equals(AbstractDataPackage.LOCAL)
        		&& !adp.getPackageIDChanged() && !adp.getDataIDChanged())
        {
        	adp.setLocation(AbstractDataPackage.LOCAL);
        }
        else
        {
        	adp.setLocation("");
        }
      }
      else {
        Log.debug(1, "No location for saving is selected!");
		  }
		} catch (MetacatUploadException mue) {
			  String errormsg = mue.getMessage();
				if (errormsg.indexOf("ERROR SAVING DATA TO METACAT")>-1) {
					// error in saving data file
			  Log.debug(5, "Problem Saving Data to Metacat");
				}
				else if (errormsg.indexOf("is already in use")>-1) {
					// metadata insert error
			  Log.debug(5, "Problem Saving Data: Id already in use");
				}
				else if (errormsg.indexOf("Document not found for Accession number")>-1) {
					// error in updating data file
			  Log.debug(5, "Problem Saving Data: Document not found for Accession number");
				}
        else if (errormsg.indexOf("Invalid content")>-1) {
          // validation error
			  Log.debug(5, "Problem Saving Data due to invalid content");
        }

			  Log.debug(20, "Problem Saving\n"+mue.getMessage());
				problem = true;
		}

		saveEvent.setFinalId(adp.getAccessionNumber());
		saveEvent.setLocation(adp.getLocation());
	  this.setVisible(false);
	  this.dispose();
	  UIController.getInstance().removeDocidFromIdleWizardRecorder(adp.getAutoSavedD());
	  //delete the incomplete file
	  Util.deleteAutoSavedFile(adp);
    
    

    if (!problem) {
    	 //alert listeners
        StateChangeMonitor.getInstance().notifyStateChange(saveEvent);
      if (showPackageFlag) {
        UIController.showNewPackageNoLocChange(adp);
      }
      else {
        MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
        morphoFrame.setVisible(false);
        UIController controller = UIController.getInstance();
        controller.removeWindow(morphoFrame);
        morphoFrame.dispose();
      }
	  }

	}
  
 


}//ExportDialog
