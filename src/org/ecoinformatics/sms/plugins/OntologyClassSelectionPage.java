/**
 *  '$RCSfile: GenericVocabularyPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-21 21:11:40 $'
 * '$Revision: 1.2 $'
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

package org.ecoinformatics.sms.plugins;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ecoinformatics.sms.ontology.OntologyClass;
import org.ecoinformatics.sms.renderer.OntologyClassSelectionPanel;

import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.vocabulary.AbstractUIVocabularyPage;
import edu.ucsb.nceas.utilities.OrderedMap;

public class OntologyClassSelectionPage extends AbstractUIVocabularyPage {

	private final String pageID = DataPackageWizardInterface.GENERIC_VOCABULARY;
	private final String nextPageID = "";
	private final String pageNumber = "";
	private final String title = "Class Selection";
	private final String subtitle = "";
		
	private ModalDialog modalDialog = null;
	
	private OntologyClassSelectionPanel selectionPanel;

	private OrderedMap returnMap = new OrderedMap();

	public OntologyClassSelectionPage() {
		init();
	}

	
	/**
	 * initialize method does frame-specific design - i.e. adding the widgets that
	 are displayed only in this frame (doesn't include prev/next buttons etc)
	 */
	private void init() {

		this.setLayout(new BorderLayout());
		
		JPanel middlePanel = WidgetFactory.makePanel();
		
		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
		middlePanel.add(WidgetFactory.makeDefaultSpacer());

		JLabel desc = 
			WidgetFactory.makeHTMLLabel("<b>Class Selection</b>", 1);
		middlePanel.add(desc);

		middlePanel.add(WidgetFactory.makeDefaultSpacer());
		middlePanel.add(WidgetFactory.makeDefaultSpacer());

		//the selected term
		MouseListener doubleClickListener =
			new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() > 1) {
						getModalDialog().okAction();
					}				
				}
			};

			selectionPanel = new OntologyClassSelectionPanel();
			
		//put the pieces together
		middlePanel.add(selectionPanel);

		middlePanel.setBorder(new javax.swing.border.EmptyBorder(0,
				4 * WizardSettings.PADDING, 7 * WizardSettings.PADDING,
				8 * WizardSettings.PADDING));

		this.add(middlePanel, BorderLayout.CENTER);
	}
	
	public void setVocabulary(String vocab) {
		//TODO: restrict to a certain ontology?
	}

	/**
	 * Users of this page can access the value[s] that have been chosen
	 * @return list of selected terms (classes) from the vocab (ontology)
	 */
	public List<String> getSelectedTerms() {
		List<String> terms = new ArrayList<String>();
		List<OntologyClass> classes = selectionPanel.getOntologyClasses();
		for (OntologyClass ontologyClass: classes) {
			terms.add(ontologyClass.getURI());
		}
		return terms;
	}

	/**
	 *  The action to be executed when the "OK" button is pressed. If no onAdvance
	 *  processing is required, implementation must return boolean true.
	 *
	 *  @return boolean true if dialog should close and return to wizard, false
	 *          if not (e.g. if a required field hasn't been filled in)
	 */
	public boolean onAdvanceAction() {
		return true;
	}

	/**
	 * NOT IMPLEMENTED
	 */
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
