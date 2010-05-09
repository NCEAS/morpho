package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

public class FileChooserWidget extends JPanel {

	  private final       String EMPTY_STRING = "";
	  private JLabel     fileNameLabel;
	  private JLabel     descLabel;
	  private JTextField fileNameField;
	  private JButton    fileNameButton;
	  private String     importFileURL;

	  static private File lastDataDir = null;

	  public FileChooserWidget(String label, String descText, String initialText) {

	    super();
	    init();
	    if (initialText==null) initialText = EMPTY_STRING;
	    fileNameField.setText(initialText);
	    setDescription(descText);
	    setLabelText(label);
	  }

	  private void init() {

	    this.setLayout(new GridLayout(3,1));

	    descLabel = WidgetFactory.makeHTMLLabel(EMPTY_STRING, 1);
	    this.add(descLabel);

	    ////
	    JPanel fileNamePanel = WidgetFactory.makePanel();
	    fileNamePanel.setLayout(new BorderLayout());
	    fileNamePanel.setMaximumSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);

	    fileNameLabel = WidgetFactory.makeLabel(EMPTY_STRING, true);

	    fileNameLabel.setBackground(Color.lightGray);
	    fileNamePanel.add(fileNameLabel, BorderLayout.WEST);

	    fileNameField = WidgetFactory.makeOneLineTextField();
	    fileNameField.setEnabled(false);
	    fileNameField.setEditable(false);
	    fileNameField.setDisabledTextColor(
	                                      WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
	    fileNamePanel.add(fileNameField, BorderLayout.CENTER);

	    fileNameButton = WidgetFactory.makeJButton("locate...",

	            new ActionListener() {

	              public void actionPerformed(ActionEvent e) {

	                final JFileChooser fc = new JFileChooser();
	                fc.setDialogTitle("Select a data file to import...");
	                String userdir = System.getProperty("user.dir");
	                String homedir = System.getProperty("user.home");
	                String osname = System.getProperty("os.name");
	                if (lastDataDir==null) {
	                  fc.setCurrentDirectory(new File(userdir));
	                  if (osname.indexOf("Window")>-1) {  // a windows os
	                    File mydocs = new File(homedir+File.separator+"My Documents");
	                    if (mydocs.exists()) {
	                      fc.setCurrentDirectory(mydocs);
	                    }
	                  }
	                } else {  // use previous dataDirectory
	                  fc.setCurrentDirectory(lastDataDir);
	                }
	                int returnVal = fc.showOpenDialog(null);
	                File file = null;
	                if (returnVal == JFileChooser.APPROVE_OPTION) {

	                  file = fc.getSelectedFile();

	                  if (file!=null) {
	                    lastDataDir = new File(file.getParent());
	                    setImportFileURL(file.getAbsolutePath());
	                    fileNameField.setText(getImportFileURL());
	                  }
	                }
	              }
	            });

	    fileNamePanel.add(fileNameButton, BorderLayout.EAST);

	    this.add(fileNamePanel);
	  }

	  public JLabel  getLabel()  { return this.fileNameLabel; }

	  public void setLabelText(String text) { this.fileNameLabel.setText(text); }

	  public JButton getButton() { return this.fileNameButton; }

	  public JTextField getTextArea() { return this.fileNameField; }

	  public String  getImportFileURL() { return this.importFileURL; }

	  public void setDescription(String desc) {

	    if (desc==null) desc = EMPTY_STRING;
	    descLabel.setText(desc);
	  }

	  public void setImportFileURL(String filePath) {

	    this.importFileURL = filePath;
	  }
	  
	  /**
	   * Sets the chosen file name
	   * @param fileName
	   */
	  public void setChosenFileName(String fileName)
	  {
		  if(fileName != null)
		  {
			  fileNameField.setText(fileName);
			  setImportFileURL(fileName);
		  }
	  }
	}