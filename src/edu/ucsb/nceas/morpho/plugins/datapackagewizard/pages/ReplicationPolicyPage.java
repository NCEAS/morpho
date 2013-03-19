/**
 *  '$RCSfile: General.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author$'
 *     '$Date$'
 * '$Revision$'
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dataone.service.types.v1.NodeReference;
import org.dataone.service.types.v1.ReplicationPolicy;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.utilities.OrderedMap;

public class ReplicationPolicyPage extends AbstractUIPage{

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String pageID     = DataPackageWizardInterface.REPLICATION_POLICY;
  private final String title      = Language.getInstance().getMessage("ReplicationPolicy");
  private final String subtitle   = "";
  public  final String pageNumber = "0";

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private JLabel replicationAllowedLabel;
  private JCheckBox replicationAllowed;
  
  private JLabel numberReplicasLabel;
  private JTextField numberReplicas;
  
  private JLabel preferredMemberNodeLabel;
  private CustomList preferredMemberNodeList;
  
  private JLabel blockedMemberNodeLabel;
  private CustomList blockedMemberNodeList;
  
  private JCheckBox applyToAll = null;
  private boolean policyMatch = true;
  
  private boolean isEntity = false;

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public ReplicationPolicyPage(boolean isEntity) 
  {
	  nextPageID = DataPackageWizardInterface.SUMMARY;
	  this.isEntity = isEntity;
	  init(); 
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel vbox = this;

    vbox.add(WidgetFactory.makeHalfSpacer());

    JLabel title = WidgetFactory.makeHTMLLabel("<p><b>" + Language.getInstance().getMessage("ReplicationPolicy") + "</b></p>", 1);
    vbox.add(title);
    
    JLabel titleDesc = WidgetFactory.makeHTMLLabel(
    		"<p>" +
    		Language.getInstance().getMessage("ReplicationPolicy.Desc1") +
    		"</p>" /* +
    		"<br/>" +
    		"<p>" +
    		Language.getInstance().getMessage("ReplicationPolicy.Desc2") +
    		"</p>" +
    		"<br/>" +
    		"<p>" +
    		Language.getInstance().getMessage("ReplicationPolicy.Desc3") +
    		"</p>"
    		*/
    		, 4);
    vbox.add(titleDesc);

    // should this apply to the entire datapackage
 	applyToAll = WidgetFactory.makeCheckBox(Language.getInstance().getMessage("Access.applyToAll"), false);
 	if (!isEntity) {
 		
 		JLabel applyDesc = WidgetFactory.makeHTMLLabel("<p><b>" + Language.getInstance().getMessage("Access.applyToAll.desc") + "</b></p>", 2);
 	    vbox.add(applyDesc);
 		        
 	    JPanel applyToAllPanel = WidgetFactory.makePanel(2);
 	    applyToAllPanel.add(applyToAll);
 	    vbox.add(applyToAllPanel);
 		
 		// check only if the policies in the package match each other
 		if (policyMatch) {
 			applyToAll.setSelected(true);
 		}
 		
 		//  warning message when selecting the box
 		applyToAll.addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {				
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					JOptionPane.showMessageDialog(
 							null, 
 							Language.getInstance().getMessage("ReplicationPolicy.applyToAll.warning"), //"All data replication policies will be overwritten by those specified here", 
 							Language.getInstance().getMessage("ReplicationPolicy"), 
 							JOptionPane.INFORMATION_MESSAGE);
 				}
 			}
 		});
 	}
    
 	JLabel details = WidgetFactory.makeHTMLLabel("<p><b>" + Language.getInstance().getMessage("Details") + "</b></p>", 2);
	vbox.add(details);
 	
    JPanel replicationPanel = WidgetFactory.makePanel();

    replicationAllowedLabel = WidgetFactory.makeLabel(Language.getInstance().getMessage("ReplicationAllowed"), true, null);
    replicationPanel.add(replicationAllowedLabel);
    
    replicationAllowed = WidgetFactory.makeCheckBox(null, false);
    replicationPanel.add(replicationAllowed);
    
    numberReplicasLabel = WidgetFactory.makeLabel(Language.getInstance().getMessage("NumberOfReplicas"), true, null);
    replicationPanel.add(numberReplicasLabel);

    numberReplicas = WidgetFactory.makeOneLineShortTextField();
    replicationPanel.add(numberReplicas);
        
    replicationPanel.add(Box.createHorizontalGlue());

    replicationPanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));
    vbox.add(replicationPanel);

    vbox.add(WidgetFactory.makeDefaultSpacer());

    ////////////////////////////////////////////////////////////////////////////

    JPanel nodesPanel = WidgetFactory.makePanel(8);
   
    preferredMemberNodeLabel = WidgetFactory.makeLabel(Language.getInstance().getMessage("PreferredNodes"), true, null);
    nodesPanel.add(preferredMemberNodeLabel);
    String[] colNames = {Language.getInstance().getMessage("NodeId")};
	preferredMemberNodeList = WidgetFactory.makeList(
    		colNames , //colNames 
    		new Object[]{ new JTextField()}, //colTemplates, 
    		3, //displayRows, 
    		true, //showAddButton, 
    		false, //showEditButton, 
    		false, //showDuplicateButton, 
    		true, //showDeleteButton, 
    		true, //showMoveUpButton, 
    		true); //showMoveDownButton);
    nodesPanel.add(preferredMemberNodeList);
    
    blockedMemberNodeLabel = WidgetFactory.makeLabel(Language.getInstance().getMessage("BlockedNodes"), true, null);
    nodesPanel.add(blockedMemberNodeLabel);
    blockedMemberNodeList = WidgetFactory.makeList(
    		colNames , //colNames 
    		new Object[]{ new JTextField()}, //colTemplates, 
    		3, //displayRows, 
    		true, //showAddButton, 
    		false, //showEditButton, 
    		false, //showDuplicateButton, 
    		true, //showDeleteButton, 
    		true, //showMoveUpButton, 
    		true); //showMoveDownButton);
    nodesPanel.add(blockedMemberNodeList);

    nodesPanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));
    vbox.add(nodesPanel);

    vbox.add(WidgetFactory.makeDefaultSpacer());
    vbox.add(WidgetFactory.makeDefaultSpacer());

  }

  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


	public ReplicationPolicy getReplicationPolicy() {
		// construct the policy from the GUI
		ReplicationPolicy replicationPolicy = new ReplicationPolicy();
		replicationPolicy.setReplicationAllowed(replicationAllowed.isSelected());
		replicationPolicy.setNumberReplicas(Integer.valueOf(numberReplicas.getText()));
		List<List<String>> rows = preferredMemberNodeList.getListOfRowLists();
		if (rows != null) {
			for (List<String> row: rows) {
				String nodeId = row.get(0);
				NodeReference nodeRef = new NodeReference();
				nodeRef.setValue(nodeId);
				replicationPolicy.addPreferredMemberNode(nodeRef );
			}
		}
		rows = blockedMemberNodeList.getListOfRowLists();
		if (rows != null) {
			for (List<String> row: rows) {
				String nodeId = row.get(0);
				NodeReference nodeRef = new NodeReference();
				nodeRef.setValue(nodeId);
				replicationPolicy.addBlockedMemberNode(nodeRef );
			}
		}
		
		return replicationPolicy;
	}
	
	public void setReplicationPolicy(ReplicationPolicy replicationPolicy) {
		
		// set the fields
		if (replicationPolicy != null) {
			replicationAllowed.setSelected(replicationPolicy.getReplicationAllowed());
			String numReplicas = null;
			if (replicationPolicy.getNumberReplicas() != null) {
				numReplicas = replicationPolicy.getNumberReplicas().toString();
			}
			numberReplicas.setText(numReplicas);
			preferredMemberNodeList.removeAllRows();
			if (replicationPolicy.getPreferredMemberNodeList() != null) {
				for (NodeReference nodeRef: replicationPolicy.getPreferredMemberNodeList()) {
					List<String> rowList = new ArrayList<String>();
					rowList.add(nodeRef.getValue());
					preferredMemberNodeList.addRow(rowList);
				}
			}
			blockedMemberNodeList.removeAllRows();
			if (replicationPolicy.getBlockedMemberNodeList() != null) {
				for (NodeReference nodeRef: replicationPolicy.getBlockedMemberNodeList()) {
					List<String> rowList = new ArrayList<String>();
					rowList.add(nodeRef.getValue());
					blockedMemberNodeList.addRow(rowList);
				}
			}
		}
	}

/**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
    
  }


  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {
  }

  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty, but if so, must
   *  return true
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    return true;
  }

  /**
   *  gets the OrderedMap object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the OrderedMap object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap returnMap = new OrderedMap();

  public OrderedMap getPageData() {

    returnMap.clear();

    return returnMap;
  }

  /**
   * gets the Map object that contains all the key/value paired settings for
   * this particular wizard page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular wizard page
   */
  public OrderedMap getPageData(String rootXPath) {

    returnMap.clear();

    return returnMap;

  }

  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return pageID; }

  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public String getTitle() { return title; }

  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public String getSubtitle() { return subtitle; }

  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID() { return nextPageID; }

  /**
    *  Returns the serial number of the page
    *
    *  @return the serial number of the page
    */
  public String getPageNumber() { return pageNumber; }

  public boolean setPageData(OrderedMap map, String _xPathRoot) {
    return true;
  }

	public boolean isApplyToAll() {
		return applyToAll.isSelected();
	}
	
	public void setPolicyMatch(boolean policyMatch) {
		this.policyMatch = policyMatch;
		this.applyToAll.setSelected(policyMatch);
	}
}
