/**
 *  '$RCSfile: GenericTreeVocabularyPage.java,v $'
 *    Purpose: Tree view selection UI of XML-based vocabulary
 *             Uses VDEX schema
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Ben Leinfelder
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2008-11-24 02:10:54 $'
 * '$Revision: 1.1 $'
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

package edu.ucsb.nceas.morpho.plugins.vocabulary;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

public class GenericTreeVocabularyPage extends AbstractUIVocabularyPage {

	private final String pageID = DataPackageWizardInterface.GENERIC_VOCABULARY;
	private final String nextPageID = "";
	private final String pageNumber = "";
	private final String title = "Vocabulary Page";
	private final String subtitle = "";
	
	private DefaultTreeModel vocabulary = new DefaultTreeModel(new DefaultMutableTreeNode());
	
	private ModalDialog modalDialog = null;

	private JLabel selectedTerms;
	private JLabel selectedTermLabel;

	private JTree relatedTerms;

	private JPanel middlePanel;

	private OrderedMap returnMap = new OrderedMap();

	public GenericTreeVocabularyPage() {
		init();
	}
	
	private static DefaultMutableTreeNode DOM2TreeNode(Node xmlNode, boolean isRoot) throws TransformerException {
		
		Node valueNode = null;
		NodeList children = null;
		
		if (isRoot) {
			valueNode = XMLUtilities.getNodeWithXPath(xmlNode, "./vocabIdentifier");
		}
		else {
			valueNode = XMLUtilities.getNodeWithXPath(xmlNode, "./termIdentifier") ;
		}
		
		DefaultMutableTreeNode thisNode = new DefaultMutableTreeNode(valueNode.getFirstChild().getTextContent());		

		children = XMLUtilities.getNodeListWithXPath(xmlNode, "./term");
		if (children != null) {
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child != null) {
					thisNode.add(DOM2TreeNode(child, false));
				}		
			}
		}
		return thisNode;
	}

	private void initVocab(String vocab) {
		try {
			String vocabFilePath = 
				File.separator + vocab + ".xml";
			ConfigXML vocabConfig = new ConfigXML(this.getClass().getResourceAsStream(vocabFilePath));
			
			Node xmlRoot = vocabConfig.getRoot();
			DefaultMutableTreeNode rootNode = DOM2TreeNode(xmlRoot, true);
			vocabulary.setRoot(rootNode);

			
		} catch (Exception e) {
			Log.debug(5, "Could not load selected vocabulary!");
			e.printStackTrace();
		}
		
	}
	
	public void setVocabulary(String vocab) {
		initVocab(vocab);
		relatedTerms.setModel(vocabulary);
	}	
	
	/**
	 * initialize method does frame-specific design - i.e. adding the widgets that
	 are displayed only in this frame (doesn't include prev/next buttons etc)
	 */
	private void init() {

		middlePanel = new JPanel();
		this.setLayout(new BorderLayout());

		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
		middlePanel.add(WidgetFactory.makeDefaultSpacer());

		JLabel desc = WidgetFactory.makeHTMLLabel(
				"<font size=\"4\"><b>Vocabulary Lookup</b></font>", 1);
		middlePanel.add(desc);

		middlePanel.add(WidgetFactory.makeDefaultSpacer());
		middlePanel.add(WidgetFactory.makeDefaultSpacer());

		//the selected term
		JPanel selectedPanel = WidgetFactory.makePanel(7);
		selectedTermLabel = WidgetFactory.makeLabel("Selected term:", true);
		selectedPanel.add(selectedTermLabel);

		selectedTerms = 
			WidgetFactory.makeLabel("", false);
		
		selectedPanel.add(selectedTerms);

		MouseListener doubleClickListener =
			new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() > 1) {
						getModalDialog().okAction();
					}				
				}
			};
		
		//for the list of terms
		JPanel termsPanel = WidgetFactory.makePanel(8);
		relatedTerms = new JTree();
		relatedTerms.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				
				Object node =
					e.getPath().getLastPathComponent();

				if (node == null) {
					//Nothing is selected.	
					return;
				}
				selectedTerms.setText(node.toString());
				
			}
		});
		relatedTerms.addMouseListener(doubleClickListener);
		termsPanel.add(new JScrollPane(relatedTerms));

		termsPanel.add(WidgetFactory.makeDefaultSpacer());

		//put the pieces together
		middlePanel.add(termsPanel);
		middlePanel.add(selectedPanel);

		middlePanel.setBorder(new javax.swing.border.EmptyBorder(0,
				4 * WizardSettings.PADDING, 7 * WizardSettings.PADDING,
				8 * WizardSettings.PADDING));

		this.add(middlePanel, BorderLayout.CENTER);
	}

	private void setSelectedTermValue(String value) {
		selectedTerms.setText(value);
	}

	/**
	 * TODO: introduce multi-selection so that returning a list makes sense
	 * Users of this page can access the value[s] that have been chosen
	 * @return list of selected terms from the vocab
	 */
	public List getSelectedTerms() {
		List retValues = new ArrayList();
		retValues.add(selectedTerms.getText());
		return retValues;
	}

	/**
	 *  The action to be executed when the "OK" button is pressed. If no onAdvance
	 *  processing is required, implementation must return boolean true.
	 *
	 *  @return boolean true if dialog should close and return to wizard, false
	 *          if not (e.g. if a required field hasn't been filled in)
	 */
	public boolean onAdvanceAction() {

		if (selectedTerms.getText().length() == 0) {
			WidgetFactory.hiliteComponent(selectedTermLabel);
			return false;
		}
		return true;
	}

	/**
	 *  gets the Map object that contains all the key/value paired
	 *
	 *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
	 *            appended when making name/value pairs.  For example, in the
	 *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the
	 *            root would be /eml:eml/dataset/keywordSet[2]
	 *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
	 *            SQUARE BRACKETS []
	 *
	 *  @return   data the Map object that contains all the
	 *            key/value paired settings for this particular wizard page
	 */

	//
	public OrderedMap getPageData() {
		return getPageData(null);
	}

	public OrderedMap getPageData(String xPathRoot) {

		returnMap.clear();

		return returnMap;
	}


	/**
	 *  The action to be executed when the "Prev" button is pressed. May be empty
	 *  Here, it does nothing because this is just a Panel and not the outer container
	 */

	public void onRewindAction() {
	}

	/**
	 *  The action to be executed when the page is loaded
	 *  Here, it does nothing because this is just a Panel and not the outer container
	 */

	public void onLoadAction() {
	}

	/**
	 *  gets the unique ID for this wizard page
	 *
	 *  @return   the unique ID String for this wizard page
	 */
	public String getPageID() {
		return this.pageID;
	}

	/**
	 *  gets the title for this wizard page
	 *
	 *  @return   the String title for this wizard page
	 */
	public String getTitle() {
		return title;
	}

	/**
	 *  gets the subtitle for this wizard page
	 *
	 *  @return   the String subtitle for this wizard page
	 */
	public String getSubtitle() {
		return subtitle;
	}

	/**
	 *  Returns the ID of the page that the user will see next, after the "Next"
	 *  button is pressed. If this is the last page, return value must be null
	 *
	 *  @return the String ID of the page that the user will see next, or null if
	 *  this is te last page
	 */
	public String getNextPageID() {
		return this.nextPageID;
	}

	/**
	 *  Returns the serial number of the page
	 *
	 *  @return the serial number of the page
	 */
	public String getPageNumber() {
		return pageNumber;
	}

	public boolean setPageData(OrderedMap map, String _xPathRoot) {
		return true;
	}
	
	public ModalDialog getModalDialog() {
		  return modalDialog;
	  }
	  public void setModalDialog(ModalDialog md) {
		  this.modalDialog = md;
	  }
}
