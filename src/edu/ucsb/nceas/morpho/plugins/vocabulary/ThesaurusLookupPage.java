/**
 *  '$RCSfile: ThesaurusLookupPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2008-11-22 01:28:10 $'
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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3.www._2001.sw.Europe.skos.namespace.ConceptResult;

import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

public class ThesaurusLookupPage extends AbstractUIVocabularyPage {

	private final String pageID = DataPackageWizardInterface.NBII_THESAURUS_LOOKUP;
	private final String nextPageID = "";
	private final String pageNumber = "";
	private final String title = "Thesaurus Page";
	private final String subtitle = "";
	
	private ModalDialog modalDialog = null;

	private final String EMPTY_STRING = "";

	private JTextField searchTerm;
	private JLabel searchTermLabel;
	private JButton searchButton;
	//private CustomList selectedTerms;
	private JLabel selectedTerms;
	private JLabel selectedTermLabel;

	private JComboBox relativeConcepts;

	private JList narrowerTerms;
	private JList broaderTerms;
	private JList relatedTerms;

	private JPanel middlePanel;

	private String xPathRoot = "/edml:edml/dataset/keywordSet[1]";
	private final String KEYWORD_REL_XPATH = "keyword[";
	private final String THESAURUS_REL_XPATH = "keywordThesaurus[1]";

	private OrderedMap returnMap = new OrderedMap();
	private final OrderedMap listResultsMap = new OrderedMap();
	private final StringBuffer listResultsBuff = new StringBuffer();

	public ThesaurusLookupPage() {
		init();
		comeBack();
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
				"<font size=\"4\"><b>Thesaurus Lookup (NBII):</b></font>", 1);
		middlePanel.add(desc);

		middlePanel.add(WidgetFactory.makeDefaultSpacer());
		middlePanel.add(WidgetFactory.makeDefaultSpacer());

		//the search action
		final ActionListener searchAction = 
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					performSearch();
				}
			};
		
		////the category search area
		JPanel kwPanel = WidgetFactory.makePanel(2);
		searchTermLabel = WidgetFactory.makeLabel("Search term:", true);
		kwPanel.add(searchTermLabel);
		searchTerm = WidgetFactory.makeOneLineTextField();
		searchTerm.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchAction.actionPerformed(null);
				}
			}
			
		});
		kwPanel.add(searchTerm);
		searchButton = WidgetFactory.makeJButton("Search", searchAction);
		kwPanel.add(searchButton);
		
		JPanel conceptsPanel = WidgetFactory.makePanel(2);
		conceptsPanel.add(WidgetFactory.makeLabel("Concept:", true));
		relativeConcepts = WidgetFactory.makePickList(new String[0], false, 0,
				new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						showTerms((String) e.getItem());
					}
				});
		conceptsPanel.add(relativeConcepts);

		//the selected term
		JPanel selectedPanel = WidgetFactory.makePanel(7);
		selectedTermLabel = WidgetFactory.makeLabel("Selected term:", true);
		selectedPanel.add(selectedTermLabel);
		String[] columnName= {"Term"};
//		selectedTerms = 
//			WidgetFactory.makeList(
//					columnName, null, 3, true, false, false, true, true, true);
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
		
		//for the various narrowing/broadening menus
		JPanel termsPanel = WidgetFactory.makePanel(8);

		narrowerTerms = WidgetFactory.makeSimpleList(new String[0]);
		narrowerTerms.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList ls = (JList) e.getSource();
				setSelectedTermValue((String) ls.getSelectedValue());
			}
		});
		narrowerTerms.addMouseListener(doubleClickListener);
		termsPanel.add(new JScrollPane(narrowerTerms));

		termsPanel.add(WidgetFactory.makeHalfSpacer());

		broaderTerms = WidgetFactory.makeSimpleList(new String[0]);
		broaderTerms.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList ls = (JList) e.getSource();
				setSelectedTermValue((String) ls.getSelectedValue());
			}
		});
		broaderTerms.addMouseListener(doubleClickListener);
		termsPanel.add(new JScrollPane(broaderTerms));

		termsPanel.add(WidgetFactory.makeHalfSpacer());

		relatedTerms = WidgetFactory.makeSimpleList(new String[0]);
		relatedTerms.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				JList ls = (JList) e.getSource();
				setSelectedTermValue((String) ls.getSelectedValue());
			}
		});
		relatedTerms.addMouseListener(doubleClickListener);
		termsPanel.add(new JScrollPane(relatedTerms));

		//put the search pieces together
		middlePanel.add(kwPanel);
		middlePanel.add(conceptsPanel);
		middlePanel.add(termsPanel);
		
		middlePanel.add(selectedPanel);


		middlePanel.setBorder(new javax.swing.border.EmptyBorder(0,
				4 * WizardSettings.PADDING, 7 * WizardSettings.PADDING,
				8 * WizardSettings.PADDING));

		this.add(middlePanel, BorderLayout.CENTER);
	}

	private void comeBack() {
		ConceptResult[] concepts = ThesaurusUtil.getInstace().getConceptResults();
		if (concepts != null) {
			relativeConcepts.removeAllItems();
			for (int i = 0; i < concepts.length; i++) {
				relativeConcepts.addItem(concepts[i].getConcept()
						.getPreferredLabel());
			}
			this.showTerms(concepts[0].getConcept().getPreferredLabel());
		}
	}
	
	private void performSearch() {
		//only perform search if there is text
		String term = searchTerm.getText();
		if (term == null || term.length() == 0) {
			WidgetFactory.hiliteComponent(searchTermLabel);
			return;
		}
		WidgetFactory.unhiliteComponent(searchTermLabel);

		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			ConceptResult[] concepts = 
				ThesaurusUtil.getInstace().search(term);
			relativeConcepts.removeAllItems();
			for (int i = 0; i < concepts.length; i++) {
				relativeConcepts.addItem(concepts[i].getConcept()
						.getPreferredLabel());
			}
		}
		catch (Exception e) {
			Log.debug(5, "Could not perform search: " + e.getMessage());
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	private void showTerms(String selectedConcept) {
		DefaultListModel listModel = null;
		String[] temp = null;

		listModel = new DefaultListModel();
		listModel.addElement("---Narrower terms---");
		temp = 
			ThesaurusUtil.getInstace().getTerms(
					selectedConcept,
					ThesaurusUtil.NARROWER);
		if (temp != null) {
			for (int i = 0; i < temp.length; i++) {
				listModel.addElement(temp[i]);
			}
		}
		narrowerTerms.setModel(listModel);

		//broader
		listModel = new DefaultListModel();
		listModel.addElement("---Broader terms---");
		temp = null;
		temp = 
			ThesaurusUtil.getInstace().getTerms(
					selectedConcept,
					ThesaurusUtil.BROADER);
		if (temp != null) {
			for (int i = 0; i < temp.length; i++) {
				listModel.addElement(temp[i]);
			}
		}
		broaderTerms.setModel(listModel);

		listModel = new DefaultListModel();
		listModel.addElement("---Related terms---");
		temp = null;
		temp = 
			ThesaurusUtil.getInstace().getTerms(
					selectedConcept,
					ThesaurusUtil.RELATED);
		if (temp != null) {
			for (int i = 0; i < temp.length; i++) {
				listModel.addElement(temp[i]);
			}
		}
		relatedTerms.setModel(listModel);

		//set the concept to the relative result just selected
		setSelectedTermValue(selectedConcept);
	}

	private void setSelectedTermValue(String value) {
		List row = new ArrayList();
		row.add(value);
		//selectedTerms.addRow(row);
		selectedTerms.setText(value);
	}

	public List getSelectedTerms() {
		List retValues = new ArrayList();
		retValues.add(selectedTerms.getText());

		return retValues;
	}

	public String getSearchTermValue() {
		return searchTerm.getText();
	}

	public void setSearchTermValue(String value) {
		searchTerm.setText(value);
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
		return getPageData(xPathRoot);
	}

	public OrderedMap getPageData(String xPathRoot) {

		returnMap.clear();

		returnMap.putAll(getKWListAsNVP(xPathRoot));

		return returnMap;
	}

	//
	private OrderedMap getKWListAsNVP(String xPathRoot) {

		listResultsMap.clear();

		int rowNumber = -1;
		int predicateIndex = 0;
		List rowLists = new ArrayList();
		List singleRow = new ArrayList();
		//String nextKWType   = null;

		//just one entry from the 
		singleRow.add(selectedTerms.getText());
		//rowLists = selectedTerms.getListOfRowLists();

		for (Iterator it = rowLists.iterator(); it.hasNext();) {

			rowNumber++;
			// CHECK FOR AND ELIMINATE EMPTY ROWS...
			Object nextRowObj = it.next();
			if (nextRowObj == null) {
				continue;
			}
			List nextRow = (List) nextRowObj;
			if (nextRow.size() < 1) {
				continue;
			}
			listResultsBuff.delete(0, listResultsBuff.length());
			listResultsBuff.append(xPathRoot);
			listResultsBuff.append("/keyword[");
			listResultsBuff.append(++predicateIndex);
			listResultsBuff.append("]");
			listResultsMap.put(
					listResultsBuff.toString(), 
					((String) (nextRow.get(0))).trim());

			//      if (nextRow.get(1)==null) continue;
			//      nextKWType = ((String)(nextRow.get(1))).trim();
			//
			//      if (nextKWType.equals(EMPTY_STRING)) continue;
			//
			//      listResultsBuff.delete(0,listResultsBuff.length());
			//      listResultsBuff.append(xPathRoot);
			//      listResultsBuff.append("/keyword[");
			//      listResultsBuff.append(predicateIndex);
			//      listResultsBuff.append("]/@keywordType");
			//      listResultsMap.put(listResultsBuff.toString(), nextKWType);
		}

		return listResultsMap;
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

	// resets all fields to blank
	private void resetBlankData() {
		searchTerm.setText("");
	}

	public boolean setPageData(OrderedMap map, String _xPathRoot) {

		if (_xPathRoot != null && _xPathRoot.trim().length() > 0) {
			this.xPathRoot = _xPathRoot;
		}

		if (map == null || map.isEmpty()) {
			resetBlankData();
			return true;
		}

		List toDeleteList = new ArrayList();
		Iterator keyIt = map.keySet().iterator();
		Object nextXPathObj = null;
		String nextXPath = null;
		Object nextValObj = null;
		String nextVal = null;

		while (keyIt.hasNext()) {

			nextXPathObj = keyIt.next();
			if (nextXPathObj == null) {
				continue;
			}
			nextXPath = (String) nextXPathObj;

			nextValObj = map.get(nextXPathObj);
			nextVal = (nextValObj == null) ? "" : ((String) nextValObj).trim();

			Log.debug(45, "Keyword:  nextXPath = " + nextXPath
					+ "\n nextVal   = " + nextVal);

			if (nextXPath.indexOf(KEYWORD_REL_XPATH) > -1) {
				Log.debug(45, ">>>>>>>>>> adding to kwList: nextXPathObj="
						+ nextXPathObj + "; nextValObj=" + nextValObj);
				List newRow = new ArrayList();
				newRow.add(nextVal);

				//just set the search term
				searchTerm.setText(nextVal);
				toDeleteList.add(nextXPathObj);
			} else if (nextXPath.indexOf(THESAURUS_REL_XPATH) > -1) {

				Log.debug(45, ">>>>>>>>>> adding to thesaurus: nextXPathObj="
						+ nextXPathObj + "; nextValObj=" + nextValObj);

				toDeleteList.add(nextXPathObj);
			}

		}

		//remove entries we have used from map:
		Iterator dlIt = toDeleteList.iterator();
		while (dlIt.hasNext()) {
			map.remove(dlIt.next());
			//if anything left in map, then it included stuff we can't handle...
		}
		
		boolean returnVal = map.isEmpty();
		if (!returnVal) {
			Log.debug(20,
					"Keyword.setPageData returning FALSE! Map still contains:" + map);
		}
		return (returnVal);
	}
	
	public ModalDialog getModalDialog() {
		  return modalDialog;
	  }
	  public void setModalDialog(ModalDialog md) {
		  this.modalDialog = md;
	  }

	public void setVocabulary(String vocab) {
		// don't need to do anything with this
		
	}
}
