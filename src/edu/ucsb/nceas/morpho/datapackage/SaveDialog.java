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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dataone.service.types.v1.SystemMetadata;
import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Access;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.SaveEvent;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;

/**
 * A dialog box for user choice of export options
 */
public class SaveDialog extends JDialog implements DataPackageWizardListener {

	/** Control button */
	private JButton executeButton = null;
	private JButton cancelButton = null;

	private boolean showPackageFlag = true;

 /** Radio button */
  private JCheckBox localLoc = new JCheckBox(/*"Save Locally"*/ Language.getInstance().getMessage("SaveLocally"));
  private JCheckBox networkLoc = new JCheckBox(/*"Save to Network."*/ Language.getInstance().getMessage("SaveToNetwork"));
  private JCheckBox upgradeEml = new JCheckBox(/*"Upgrade to latest EML "*/ Language.getInstance().getMessage("UpgradeToLatestEML") 
  												+"(" 
  												+ EML200DataPackage.LATEST_EML_VER + ")");

	private static final int PADDINGWIDTH = 8;
	  private static String WARNING =
      /*"Please choose where you would like to save the data package."*/
	  Language.getInstance().getMessage("ChooseWhereToSave")
	  ;

	/** A reference to morpho frame */
	MorphoFrame morphoFrame = null;

	/** A string indicating the morpho frame's type */
	String morphoFrameType = null;

	/** selected docid */
	String selectDocId = null;

	/** the MorphoDataPackage object to be saved */
	MorphoDataPackage mdp = null;
	
	SaveEvent saveEvent = null;
	//private JComboBox identifierSchemeComboBox = null;
	private JCheckBox DOICheckBox = null;
	private Box schemeBox = null;
	private String identifierScheme = null;
	
	private static final String BOTHFAILMESSAGEINSAVINGBOTH = Language.getInstance().getMessage("FailureSavingTo") + " " +  DataPackageInterface.BOTH;
	private static final String NETWORKFAILMESSAGEINSAVINGBOTH = Language.getInstance().getMessage("SuccessSavingTo") + " " + DataPackageInterface.LOCAL + ". " + Language.getInstance().getMessage("FailureSavingTo") + " " + DataPackageInterface.NETWORK;
	private static final String LOCALFAILMESSAGEINSAVINGBOTH = Language.getInstance().getMessage("SuccessSavingTo") + " "  + DataPackageInterface.NETWORK + ". " + Language.getInstance().getMessage("FailureSavingTo") + " " + DataPackageInterface.LOCAL;
    //private static final String IDENTIFIERSCHEME = Language.getInstance().getMessage("SaveDialog.IdentifierScheme")+":";
	private static final String DOICHECKBOXLABEL = Language.getInstance().getMessage("SaveDialog.DOICheckBox");
	private static final String DOITOOLTIP1 = Language.getInstance().getMessage("SaveDialog.DOIToolTip1");
	private static final String DOITOOLTIP2 = Language.getInstance().getMessage("SaveDialog.DOIToolTip2");
	private static final String DOITOOLTIP =  "<html>"+DOITOOLTIP1+"<br>"+DOITOOLTIP2+"</html>";
    /**
	 * Construct a new instance of the dialog where parent is morphoframe
	 * 
	 */
	public SaveDialog(MorphoDataPackage mdp) {
		super(UIController.getInstance().getCurrentActiveWindow());
		setModal(true);
		this.mdp = mdp;
		morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		initialize(morphoFrame);

	}

	public SaveDialog(MorphoDataPackage mdp, boolean showPackageFlag) {
		this(mdp);
		this.showPackageFlag = showPackageFlag;
	}

	/** Method to initialize save dialog */
	private void initialize(Window parent) {
	    
	    // add an item listener to the the network check box.
	    //if the network check box is selected, the DOI will be added to the scheme selection list.
	    //if the network check box is unselected, the DOI will be removed from the scheme selection list.
	    networkLoc.addItemListener( new ItemListener() {
	            public void itemStateChanged (ItemEvent e) {
	                if(e.getStateChange() == ItemEvent.SELECTED) {
	                    //identifierSchemeComboBox.addItem(DataStoreServiceController.DOI);
	                    DOICheckBox.setEnabled(true);
	                    schemeBox.revalidate();
	                    schemeBox.repaint();
	                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
	                    //identifierSchemeComboBox.removeItem(DataStoreServiceController.DOI);
	                    DOICheckBox.setSelected(false);
	                    DOICheckBox.setEnabled(false);
	                    schemeBox.revalidate();
                        schemeBox.repaint();
	                }
	        }
	    });
		// Set OpenDialog size depent on parent size
		int parentWidth = parent.getWidth();
		int parentHeight = parent.getHeight();
		int dialogWidth = 500;
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

		setTitle(/*"Save Current DataPackage"*/ Language.getInstance().getMessage("SaveCurrentDataPackage"));
    	// Set the default close operation is dispose
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Set the border layout as layout
		getContentPane().setLayout(new BorderLayout(0, 0));
		// Add padding for left and right
		getContentPane().add(BorderLayout.EAST,
				Box.createHorizontalStrut(PADDINGWIDTH));
		getContentPane().add(BorderLayout.WEST,
				Box.createHorizontalStrut(PADDINGWIDTH));
		
		// Create note box and add it to the north of mainPanel
        Box noteBox = Box.createVerticalBox();
        noteBox.add(Box.createVerticalStrut(PADDINGWIDTH));
        JLabel note = WidgetFactory.makeHTMLLabel(WARNING, 2);
        /*
         * JTextArea note = new JTextArea(WARNING); note.setEditable(false);
         * note.setLineWrap(true); note.setWrapStyleWord(true);
         * note.setOpaque(false);
         */
        noteBox.add(note);
        noteBox.add(Box.createVerticalStrut(PADDINGWIDTH));
        getContentPane().add(BorderLayout.NORTH, noteBox);

		// Create JPanel and set it border layout
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(Box.createVerticalStrut(25));
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
		mainPanel.add(centerBox);
		mainPanel.add(WidgetFactory.makeDefaultSpacer());
		schemeBox = Box.createHorizontalBox();
        //identifierSchemeComboBox = new JComboBox(DataStoreServiceController.INITIAL_IDENTIFIER_SCHEMES);
        //identifierSchemeComboBox.setFont(WizardSettings.WIZARD_CONTENT_FONT);
		DOICheckBox = new JCheckBox(DOICHECKBOXLABEL);
		DOICheckBox.setFont(WizardSettings.WIZARD_CONTENT_FONT);
		DOICheckBox.setToolTipText(DOITOOLTIP);
		if(!networkLoc.isSelected()) {
		    DOICheckBox.setEnabled(false);
		}
        //schemeBox.add(Box.createHorizontalStrut(LEFTSPACE));
        //JLabel schemes = new JLabel(IDENTIFIERSCHEME);
        //schemes.setFont(WizardSettings.WIZARD_CONTENT_FONT);
        //schemeBox.add(schemes);
        //schemeBox.add(identifierSchemeComboBox);
        schemeBox.add(DOICheckBox);
        //schemeBox.add(Box.createHorizontalGlue());
        mainPanel.add(schemeBox);

		// Finish mainPanel and add it the certer of contentpanel
		getContentPane().add(BorderLayout.CENTER, mainPanel);
		
		
		

		// Create bottom box
		Box bottomBox = Box.createVerticalBox();
		getContentPane().add(BorderLayout.SOUTH, bottomBox);
		// Create padding between result panel and Contorl button box
		bottomBox.add(Box.createVerticalStrut(PADDINGWIDTH));
		// Create a controlbuttionBox
		Box controlButtonsBox = Box.createHorizontalBox();
		controlButtonsBox.add(Box.createHorizontalGlue());

		// Save button
		executeButton = new JButton(/*"Save"*/ Language.getInstance().getMessage("Save"));
		controlButtonsBox.add(executeButton);
		controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));

		// Cancel button
		cancelButton = new JButton(/*"Cancel"*/ Language.getInstance().getMessage("Cancel"));
		controlButtonsBox.add(cancelButton);
		controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));

		// Add controlButtonsBox to bottomBox
		bottomBox.add(controlButtonsBox);
		// Add the margin between controlButtonPanel to the bottom line
		bottomBox.add(Box.createVerticalStrut(10));

		SymAction lSymAction = new SymAction();
		executeButton.addActionListener(lSymAction);
		cancelButton.addActionListener(lSymAction);

		AbstractDataPackage adp = mdp.getAbstractDataPackage();
		String location = adp.getLocation();
		if (location.equals("")) { // never been saved
			localLoc.setEnabled(true);
			networkLoc.setEnabled(true);
			localLoc.setSelected(true);
			networkLoc.setSelected(false);
		} else if (location.equals(DataPackageInterface.LOCAL)) {
			localLoc.setEnabled(false);
			networkLoc.setEnabled(true);
			localLoc.setSelected(false);
			networkLoc.setSelected(true);
		} else if (location.equals(DataPackageInterface.NETWORK)) {
			localLoc.setEnabled(true);
			networkLoc.setEnabled(false);
			localLoc.setSelected(true);
			networkLoc.setSelected(false);
			//schemeBox.setVisible(false);
		} else if (location.equals(DataPackageInterface.BOTH)) {
			localLoc.setEnabled(false);
			networkLoc.setEnabled(false);
			localLoc.setSelected(false);
			networkLoc.setSelected(false);
			//schemeBox.setVisible(false);
		}

		try {

			boolean askUpgrade = false;
			if (!((EML200DataPackage) adp).isLatestEMLVersion()) {
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

	class SymAction implements java.awt.event.ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent event) {
			Object object = event.getSource();
			if (object == executeButton) {
				executeButton_actionPerformed(event);
			} else if (object == cancelButton) {
				cancelButton_actionPerformed(event);
			}
		}
	}

	void cancelButton_actionPerformed(java.awt.event.ActionEvent event) {
		this.setVisible(false);
		this.dispose();
	}

	void executeButton_actionPerformed(java.awt.event.ActionEvent event) {
	    //identifierScheme = (String)identifierSchemeComboBox.getSelectedItem();
	    if(DOICheckBox.isSelected()) {
	        identifierScheme = DataStoreServiceController.DOI;
	    } else {
	        identifierScheme = DataStoreServiceController.UUID;
	    }
	    
	    if(identifierScheme == null || identifierScheme.trim().equals("")) {
	        identifierScheme = DataStoreServiceController.UUID;
	    }
	    //System.out.println("the selected scheme is ====== "+identifierScheme);
		Component comp = morphoFrame.getContentComponent();
		if (comp instanceof DataViewContainerPanel) {
			((DataViewContainerPanel) comp).saveDataChanges();
		}

		AbstractDataPackage adp = mdp.getAbstractDataPackage();
		boolean runCorrectionWizard = false;
		//String location = adp.getLocation();
		// track the save event
		saveEvent = new SaveEvent(this, StateChangeEvent.SAVE_DATAPACKAGE);
		String id = adp.getAccessionNumber();
		// initial id
		saveEvent.setInitialId(id);
		//if (location.equals("")) { // only update version if new
		
		if (upgradeEml.isSelected()) {
				    String newString = null;
				    boolean hasWarn = false;
				    boolean success = true;
				    SystemMetadata sysmeta = adp.getSystemMetadata();
				    EML200DataPackage eml200Package = (EML200DataPackage) adp;
					// change the XML
				    try
	                {
	                    newString = eml200Package.transformToLastestEML();
	                }
	                catch(EMLVersionTransformationException e)
	                {
	                    hasWarn = true;
	                    newString = e.getNewEMLOutput();//this part of exception is eml output.
	                } catch(Exception e) {
	                    success = false;
	                }
				    //we need correction the error
				    if(success) {
				        eml200Package = (EML200DataPackage) DataPackageFactory.getDataPackage(new java.io.StringReader(newString));
				        eml200Package.setEMLVersion(EML200DataPackage.LATEST_EML_VER);
				        if(hasWarn) {
				            try {
	                            Reader xml = new StringReader(newString);
	                            EML210Validate validate = new EML210Validate();
	                            validate.parse(xml);
	                            xml.close();
	                            Vector errorPathList = validate.getInvalidPathList();
	                            ServiceController services = ServiceController.getInstance();
	                            ServiceProvider provider =
	                                services.getServiceProvider(DataPackageWizardInterface.class);
	                            DataPackageWizardInterface wizard = (DataPackageWizardInterface)provider;
	                            //we pass the listener to correction wizard and let it handle the listener.
	                            adp.setMetadataNode(eml200Package.getMetadataNode());
	                            //System.out.println("\n\n********** after setting to mdp");
	                            this.dispose();
	                            //System.out.println(XMLUtil.getDOMTreeAsString(mdp.getAbstractDataPackage().getMetadataNode()));
	                            
	                            boolean isSaveProcess = true;
	                            runCorrectionWizard = true;
	                            wizard.startCorrectionWizard(mdp, errorPathList, morphoFrame, this, isSaveProcess);
	                            
	                        } catch (Exception e) {
	                            success = false;
	                        }
				        } else {
                            try {
                                adp.setMetadataNode(eml200Package.getMetadataNode());
                            } catch (Exception e){
                                Log.debug(30, "Can't set the system metadata "+e.getMessage());
                            }
                            
				        }
				    }
                        
					if(!success) {
					    JOptionPane.showMessageDialog(
										morphoFrame,
										Language.getInstance().getMessage("SaveDialog.couldNotUpgrade"),
										Language.getInstance().getMessage("Information"),
										JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
				    
		}
		

		//If there is no correction wizard involved, save the package.
        //If there is a correction wizard involved, the save method will be called during the DataPackageWizardListener.wizardComplete method. So
        //we skip here.
        if(!runCorrectionWizard) {
            save();
        }

		

	}
	
	/*
	 * Save the package
	 */
	private void save() {
	    boolean problem = false;
	    AbstractDataPackage adp = mdp.getAbstractDataPackage();
	    try {
	        
            // BOTH
          //System.out.println("the location is ======================="+adp.getLocation());
            if ((localLoc.isSelected()) && (localLoc.isEnabled())
                    && (networkLoc.isSelected()) && (networkLoc.isEnabled())) {
                try {
                    DataStoreServiceController.getInstance().save(mdp, DataPackageInterface.BOTH, identifierScheme);
                } catch (Exception mue) {
                    // TODO: More informative?
                    String errormsg = mue.getMessage();
                    Log.debug(5, "Problem Saving package: \n" + errormsg);
                    mue.printStackTrace();
                }
                
                if (adp.getSerializeLocalSuccess()
                        && adp.getSerializeMetacatSuccess()) {
                    adp.setLocation(DataPackageInterface.BOTH);//success
                } else if (adp.getSerializeLocalSuccess()) {
                    Log.debug(8, NETWORKFAILMESSAGEINSAVINGBOTH);
                    adp.setLocation(DataPackageInterface.LOCAL);//partial success
                } else if (adp.getSerializeMetacatSuccess()) {
                    Log.debug(8, LOCALFAILMESSAGEINSAVINGBOTH);
                    adp.setLocation(DataPackageInterface.NETWORK);//partial success
                } else {
                    Log.debug(8, BOTHFAILMESSAGEINSAVINGBOTH);
                    adp.setLocation(adp.getLocation());//failed
                    problem = true;
                }
            // LOCAL
            } else if ((localLoc.isSelected()) && (localLoc.isEnabled())) {
                DataStoreServiceController.getInstance().save(mdp, DataPackageInterface.LOCAL, identifierScheme);
                if (adp.getSerializeLocalSuccess()) {
                    if (adp.getLocation() != null
              && adp.getLocation().equals(DataPackageInterface.NETWORK)
              && !adp.getPackageIDChanged()
              && !adp.getDataIDChanged()) {
            adp.setLocation(DataPackageInterface.BOTH);
          } else {
            adp.setLocation(DataPackageInterface.LOCAL);
          }
                } else {
                    adp.setLocation(adp.getLocation());
                }
            // METACAT
            } else if ((networkLoc.isSelected()) && (networkLoc.isEnabled())) {
                DataStoreServiceController.getInstance().save(mdp, DataPackageInterface.NETWORK, identifierScheme);
                if (adp.getSerializeMetacatSuccess()) {           
                  if (adp.getLocation() != null
                && adp.getLocation().equals(DataPackageInterface.LOCAL)
                && !adp.getPackageIDChanged()
                && !adp.getDataIDChanged()) {
              adp.setLocation(DataPackageInterface.BOTH);
                  } else {
                    adp.setLocation(DataPackageInterface.NETWORK);
                  }
                    
                } else {
                    adp.setLocation(adp.getLocation());
                }
            // NOTHING  
            } else {
                Log.debug(1, "No location for saving is selected!");
                // don't refresh the screen - nothing has been done
                problem = true;
            }
            adp.setPackageIDChanged(false);//reset the value.
        } catch (Exception mue) {
            // TODO: More informative?
            String errormsg = mue.getMessage();
            Log.debug(5, "Problem Saving package: \n" + errormsg);
            mue.printStackTrace();
            problem = true;
        }
        
        this.setVisible(false);
        this.dispose();

        if (!problem) {
            UIController.getInstance().removeDocidFromIdleWizardRecorder(adp.getAutoSavedD());
            // delete the incomplete file
            Morpho.thisStaticInstance.getLocalDataStoreService().deleteAutoSavedFile(adp);
            
            // alert listeners
            if(saveEvent != null) {
                saveEvent.setFinalId(adp.getAccessionNumber());
                saveEvent.setLocation(adp.getLocation());
                StateChangeMonitor.getInstance().notifyStateChange(saveEvent);
            }
           
            
            // refresh
            if (showPackageFlag) {
                UIController.showNewPackageNoLocChange(mdp);
            } else {
                MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
                morphoFrame.setVisible(false);
                UIController controller = UIController.getInstance();
                controller.removeWindow(morphoFrame);
                morphoFrame.dispose();
            }
        }
	}
	
	    @Override
	    public void wizardComplete(Node newDOM, String autoSavedID) {
	        if(newDOM != null) {
	            AbstractDataPackage adp = mdp.getAbstractDataPackage();
	            adp.setMetadataNode(newDOM);
	        }
	       
	        save();
	       
	    }

	    @Override
	    public void wizardCanceled() {
	    // TODO Auto-generated method stub
	    
	    }

	    @Override
	    public void wizardSavedForLater() {
	    // TODO Auto-generated method stub
	    
	    }
	   

}// ExportDialog
