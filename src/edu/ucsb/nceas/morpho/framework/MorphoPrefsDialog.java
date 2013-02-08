/**
 *  '$RCSfile: MorphoPrefsDialog.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-02-27 23:10:50 $'
 * '$Revision: 1.3 $'
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

package edu.ucsb.nceas.morpho.framework;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import org.dataone.service.types.v1.Node;
import org.dataone.service.types.v1.NodeType;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * A simple Morpho Preferences Dialog
 */
public class MorphoPrefsDialog extends javax.swing.JDialog
{

  Morpho morpho;
  ConfigXML config;
  
	public MorphoPrefsDialog(Frame parentFrame, Morpho morpho) {
		super(parentFrame);
		this.morpho = morpho;
		setTitle(Language.getInstance().getMessage("MorphoPreferences"));
		setModal(true);
		getContentPane().setLayout(new BorderLayout(0, 0));
		setSize(600, 400);
		setVisible(false);
		CenterPanel.setLayout(new GridLayout(8, 1, 0, 0));
		
		// tabbed center panel
		JTabbedPane tabbedCenterPane = new JTabbedPane();
		tabbedCenterPane.addTab(Language.getInstance().getMessage("General"), CenterPanel);
		tabbedCenterPane.addTab(Language.getInstance().getMessage("Advanced"), cnPanel);

		getContentPane().add(BorderLayout.CENTER, tabbedCenterPane);
		
		aboutLabel.setHorizontalAlignment(SwingConstants.CENTER);
		aboutLabel.setText(Language.getInstance().getMessage("MorphoPreferences"));
		aboutLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		CenterPanel.add(aboutLabel);
		
		// the CN
		cnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		coordinatingNodeURLLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		coordinatingNodeURLLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		coordinatingNodeURLLabel.setText(Language.getInstance().getMessage("CoordinatingNodeURL"));
		coordinatingNodeURLLabel.setForeground(java.awt.Color.black);
		coordinatingNodeURLLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		cnPanel.add(coordinatingNodeURLLabel);
		coordinatingNodeURLTextField.setColumns(35);
		coordinatingNodeURLTextField.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent event) {
				// don't do anything
			}

			@Override
			public void focusLost(FocusEvent event) {
				// refresh the MN list
				refreshMemberNodeList();
			}
			
		});
		cnPanel.add(coordinatingNodeURLTextField);
		//CenterPanel.add(cnPanel);
		
		// the MN selection / edit box
		JPanel mnSelectionPanel = new JPanel();
		mnSelectionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		// load the MNs initially
		refreshMemberNodeList();
		
		memberNodeComboBox.setEditable(true);
		//memberNodeComboBox.setRenderer(new NodeListCellRenderer());
		memberNodeURLLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		memberNodeURLLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		memberNodeURLLabel.setText(Language.getInstance().getMessage("MemberNodeURL"));
		memberNodeURLLabel.setForeground(java.awt.Color.black);
		memberNodeURLLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		mnSelectionPanel.add(memberNodeURLLabel);
		mnSelectionPanel.add(memberNodeComboBox);
		CenterPanel.add(mnSelectionPanel);

		JPanel3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		CenterPanel.add(JPanel3);
		loggingLabel.setText(Language.getInstance().getMessage("LogMessages"));
		JPanel3.add(loggingLabel);
		loggingLabel.setForeground(java.awt.Color.black);
		loggingLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		logYes.setText("yes");
		JPanel3.add(logYes);
		logNo.setText("no");
		logNo.setSelected(true);
		JPanel3.add(logNo);
		JPanel4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		CenterPanel.add(JPanel4);
		debugLevelLabel.setText(Language.getInstance().getMessage("DebugLevel") + " (1-100)");
		JPanel4.add(debugLevelLabel);
		debugLevelLabel.setForeground(java.awt.Color.black);
		debugLevelLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		debugLevelTextField.setColumns(5);
		JPanel4.add(debugLevelTextField);

		JPanel5.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		CenterPanel.add(JPanel5);
		LFLabel.setText(Language.getInstance().getMessage("LookandFeel"));
		JPanel5.add(LFLabel);
		LFLabel.setForeground(java.awt.Color.black);
		LFLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		JPanel5.add(LFCombo);
		LFCombo.addItem("system L&F");
		LFCombo.addItem("kunststoff L&F");
		LFCombo.addItem("metal L&F");
		LFCombo.addItem("windows L&F");
		LFCombo.addItem("motif L&F");
		LFCombo.setSelectedIndex(0);

		clearButton.setText(Language.getInstance().getMessage("ClearTemporaryStorage"));
		JPanel6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JPanel6.add(clearButton);
		CenterPanel.add(JPanel6);

		JPanel1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		CenterPanel.add(JPanel1);

		ControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		getContentPane().add(BorderLayout.SOUTH, ControlPanel);
		setButton.setText(/* "Set" */Language.getInstance().getMessage("Set"));
		setButton.setActionCommand("OK");
		setButton.setOpaque(false);
		setButton.setMnemonic((int) 'O');
		ControlPanel.add(setButton);
		cancelButton.setText(/* "Cancel" */Language.getInstance().getMessage("Cancel"));
		ControlPanel.add(cancelButton);
		// }}

		// {{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		setButton.addActionListener(lSymAction);
		cancelButton.addActionListener(lSymAction);
		clearButton.addActionListener(lSymAction);

		SymItem lSymItem = new SymItem();
		logYes.addItemListener(lSymItem);
		logNo.addItemListener(lSymItem);
		// }}
		config = Morpho.getConfiguration();
		coordinatingNodeURLTextField.setText(morpho.getDataONEDataStoreService().getCNodeURL());
		
		// set the selected MN
		setSelectedMemberNode();
		
		if (config.get("log_file", 0).equals("true")) {
			logYes.setSelected(true);
			logNo.setSelected(false);
		} else {
			logYes.setSelected(false);
			logNo.setSelected(true);
		}
		debugLevelTextField.setText(config.get("debug_level", 0));

		String lnf = config.get("lookAndFeel", 0);
		if (lnf == null)
			LFCombo.setSelectedIndex(0);
		else if (lnf.equalsIgnoreCase("kunststoff"))
			LFCombo.setSelectedIndex(1);
		else if (lnf.equalsIgnoreCase("metal"))
			LFCombo.setSelectedIndex(2);
		else if (lnf.equalsIgnoreCase("windows"))
			LFCombo.setSelectedIndex(3);
		else if (lnf.equalsIgnoreCase("motif"))
			LFCombo.setSelectedIndex(4);
	}

	public void setVisible(boolean b) {
		if (b) {
			Rectangle bounds = (getParent()).getBounds();
			Dimension size = getSize();
			setLocation(bounds.x + (bounds.width - size.width) / 2, bounds.y
					+ (bounds.height - size.height) / 2);
		}

		super.setVisible(b);
	}
	
	private void setSelectedMemberNode() {
		// match the URLs if we have it
		String configuredMnURL = morpho.getDataONEDataStoreService().getMNodeURL();
		Object selectedItem = configuredMnURL;
		// check the list
		for (int i = 0; i < memberNodeComboBox.getItemCount(); i++) {
			Object mnItemObject = memberNodeComboBox.getItemAt(i);
			String mnURL = null;
			if (mnItemObject instanceof NodeItem) {
				mnURL = ((NodeItem) mnItemObject).node.getBaseURL();
			} else {
				mnURL = mnItemObject.toString();
			}
			// do the URLs match for this list item?
			if (configuredMnURL.equals(mnURL)) {
				selectedItem = mnItemObject;
				break;
			}
		}
		// set whatever we ended up with
		memberNodeComboBox.setSelectedItem(selectedItem);
	}
	
	private void refreshMemberNodeList() {
		Object selectedItem = memberNodeComboBox.getSelectedItem();
		memberNodeComboBox.removeAllItems();
		Vector options = null;
		try {
			String cnURL = coordinatingNodeURLTextField.getText();
			List<Node> nodes = morpho.getDataONEDataStoreService().getNodes(cnURL);
			if (nodes != null) {
				//options = nodes.toArray();
				options = new Vector();
				for (Node node: nodes) {
					if (node.getType().equals(NodeType.MN)) {
						NodeItem item = new NodeItem(node);
						options.add(item);
						memberNodeComboBox.addItem(item);
					}
				}
			}
		} catch (Exception e) {
			Log.debug(10, "Could not look up available Member Nodes from the CN: " + e.getMessage());
			e.printStackTrace();
		}
		if (selectedItem != null) {
			memberNodeComboBox.setSelectedItem(selectedItem);
		}
	}

	// {{DECLARE_CONTROLS
	JPanel CenterPanel = new JPanel();
	JLabel aboutLabel = new JLabel();
	JPanel JPanel1 = new JPanel();
	JPanel JPanel2 = new JPanel();
	JPanel cnPanel = new JPanel();
	JLabel coordinatingNodeURLLabel = new JLabel();
	JTextField coordinatingNodeURLTextField = new JTextField();
	JLabel memberNodeURLLabel = new JLabel();
	JComboBox memberNodeComboBox = new JComboBox();
	JPanel JPanel3 = new JPanel();
	JLabel loggingLabel = new JLabel();
	JRadioButton logYes = new JRadioButton();
	JRadioButton logNo = new JRadioButton();
	JPanel JPanel4 = new JPanel();
	JLabel debugLevelLabel = new JLabel();
	JTextField debugLevelTextField = new JTextField();
	JPanel JPanel5 = new JPanel();
	JPanel JPanel6 = new JPanel();
	JLabel LFLabel = new JLabel();
	JComboBox LFCombo = new JComboBox();

	JPanel ControlPanel = new JPanel();
	JButton setButton = new JButton();
	JButton cancelButton = new JButton();
	JButton clearButton = new JButton();

	// }}

	class SymWindow extends java.awt.event.WindowAdapter {
		public void windowClosing(WindowEvent event) {
			Object object = event.getSource();
			if (object == MorphoPrefsDialog.this)
				morphoPrefsDialog_windowClosing(event);
		}
	}

	void morphoPrefsDialog_windowClosing(WindowEvent event) {
		// to do: code goes here.

		morphoPrefsDialog_windowClosing_Interaction1(event);
	}

	void morphoPrefsDialog_windowClosing_Interaction1(
			WindowEvent event) {
		try {
			// morphoPrefsDialog Hide the JAboutDialog
			this.setVisible(false);
		} catch (Exception e) {
		}
	}
	
	class SymAction implements java.awt.event.ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent event) {
			Object object = event.getSource();
			if (object == setButton) {
				setButton_actionPerformed(event);
			} else if (object == cancelButton) {
				cancelButton_actionPerformed(event);
			} else if (object == clearButton) {
				int opt = JOptionPane.showConfirmDialog(UIController
						.getInstance().getCurrentActiveWindow(), Language
						.getInstance().getMessage("MorphoPrefs.clear.confirm1") // "Are you sure you want to delete the temporary files?"
						+ "\n "
						+ Language.getInstance().getMessage("MorphoPrefs.clear.confirm2"), // "Before deleting them, you should make sure there is no crashed wizard and no wizard is running in Morpho."
						Language.getInstance().getMessage("MorphoPrefs.clear.title"), // "DO YOU WANT TO CONTINUE?"
						JOptionPane.YES_NO_OPTION);
				if (opt == JOptionPane.NO_OPTION) {
					return;
				}
				Morpho.thisStaticInstance.cleanCache();
				Morpho.thisStaticInstance.cleanTemp();
				JOptionPane.showMessageDialog(UIController.getInstance().getCurrentActiveWindow(), 
						Language.getInstance().getMessage("MorphoPrefs.clear.emptied"), // "Temporary Storage Areas Emptied!"// ,
						null, JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	void setButton_actionPerformed(java.awt.event.ActionEvent event) {
		
		if (logYes.isSelected()) {
			config.set("log_file", 0, "true");
		} else {
			config.set("log_file", 0, "false");
		}
		String debugLevelS = debugLevelTextField.getText();
		try {
			Integer iii = new Integer(debugLevelS);
			config.set("debug_level", 0, debugLevelS);
		} catch (Exception e) {
			config.set("debug_level", 0, "20");
			Log.debug(20,
					"Debug Level is NOT an integer! Reset to a value of 20.");
		}

		String lnf = "";
		if (LFCombo.getSelectedIndex() == 0) {
			lnf = "";
		} else if (LFCombo.getSelectedIndex() == 1) {
			lnf = "kunststoff";
		} else if (LFCombo.getSelectedIndex() == 2) {
			lnf = "metal";
		} else if (LFCombo.getSelectedIndex() == 3) {
			lnf = "windows";
		} else if (LFCombo.getSelectedIndex() == 4) {
			lnf = "motif";
		}
		config.set("lookAndFeel", 0, lnf);
		
		config.save();

		// set the active MN URL
		String mnURL = null;
		Object mnItemObject = memberNodeComboBox.getSelectedItem();
		if (mnItemObject instanceof NodeItem) {
			mnURL = ((NodeItem) mnItemObject).node.getBaseURL();
		} else {
			mnURL = mnItemObject.toString();
		}
		morpho.getDataONEDataStoreService().setMNodeURL(mnURL);

		// set the CN URL
		morpho.getDataONEDataStoreService().setCNodeURL(coordinatingNodeURLTextField.getText());

		Morpho.initializeLogging(config);
		// need to add Look and Feel support
		Morpho.setLookAndFeel(config.get("lookAndFeel", 0));

		setVisible(false);
		this.dispose();
	}
  
	void cancelButton_actionPerformed(java.awt.event.ActionEvent event) {
		setVisible(false);
		this.dispose();

	}

  	class SymItem implements java.awt.event.ItemListener {
		public void itemStateChanged(java.awt.event.ItemEvent event) {
			Object object = event.getSource();
			if (object == logYes) {
				if (logYes.isSelected())
					logNo.setSelected(false);
			}
			if (object == logNo) {
				if (logNo.isSelected())
					logYes.setSelected(false);
			}
		}
	}
  	
  	/**
  	 * Custom object for rendering a Node in the combobox
  	 * Overrides the toString method to display something meaningful
  	 * without loosing information about the node (e.g., nodeId)
  	 * @author leinfelder
  	 *
  	 */
  	class NodeItem {
  		
  		private Node node;
  		
  		public NodeItem(Node node) {
  			this.node = node;
  		}
  		
  		@Override
  		public String toString() {
  			String retVal = null;
  			if (node != null) {
  				retVal = node.getName();
  				//retVal = node.getIdentifier().getValue() + " (" + node.getName() +  ")";
  				//retVal = node.getBaseURL();
  			}
  			return retVal;
  		}
  	}
  	
  	/**
  	 * Experimental cell renderer for Node objects. Not nearly as clean as
  	 * a toString() override (UI looks sluggish) but does allow for more information
  	 * per entry to be displayed.
  	 * Does not work well with an editable combobox
  	 * @author leinfelder
  	 *
  	 */
  	class NodeListCellRenderer extends JLabel implements ListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			// default is blank
			setText(null);
			if (value != null) {
				if (value instanceof Node) {
					Node node = (Node) value;
					setText(node.getName() + "(" + node.getBaseURL() + ")" );
					setToolTipText(node.getDescription());
				} else {
					setText(value.toString());
				}
			}
			return this;
		}	
  	}
  	
}
