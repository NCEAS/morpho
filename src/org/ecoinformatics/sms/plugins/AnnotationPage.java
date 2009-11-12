/**
 *  '$RCSfile: UsageRights.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-13 03:57:28 $'
 * '$Revision: 1.18 $'
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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ecoinformatics.sms.annotation.Annotation;
import org.ecoinformatics.sms.annotation.Characteristic;
import org.ecoinformatics.sms.annotation.Entity;
import org.ecoinformatics.sms.annotation.Measurement;
import org.ecoinformatics.sms.annotation.Observation;
import org.ecoinformatics.sms.annotation.Standard;

import com.sun.tools.doclets.formats.html.resources.standard;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;



public class AnnotationPage extends AbstractUIPage{

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String pageID     = DataPackageWizardInterface.USAGE_RIGHTS;
  private final String pageNumber = "9";
  private final String title      = "Attribute Annotation Editor";
  private final String subtitle   = "";

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JTextField observationEntity;
  private JTextField observationCharacteristic;
  private JTextField observationStandard;

  private Annotation annotation = null;
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public AnnotationPage() 
  {
	  nextPageID = DataPackageWizardInterface.GEOGRAPHIC;
	  init(); 
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    JLabel desc = WidgetFactory.makeHTMLLabel(
      "<b>Select Observation Entity, Characteristic and Standard for the selected attribute.</b> "
      +"The Ontology Browser can be used to navigate specific ontologies.", 2);
    this.add(desc);

    this.add(WidgetFactory.makeDefaultSpacer());

    MouseListener mListener = 
    	new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				JTextField source = (JTextField) e.getSource();
				showDialog(source);
			}
    	
    };
    JPanel classesPanel = WidgetFactory.makePanel();
    classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));
    
    // Entity
    JPanel entityPanel = WidgetFactory.makePanel(1);
    entityPanel.add(WidgetFactory.makeLabel("Entity:", false));
    observationEntity = WidgetFactory.makeOneLineTextField("<entity>");
    observationEntity.addMouseListener(mListener);
    entityPanel.add(observationEntity);
    entityPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 8 * WizardSettings.PADDING));
    
    // Characteristic
    JPanel characteristicPanel = WidgetFactory.makePanel(1);
    characteristicPanel.add(WidgetFactory.makeLabel("Characteristic:", false));
    observationCharacteristic = WidgetFactory.makeOneLineTextField("<characteristic>");
    observationCharacteristic.addMouseListener(mListener);

    characteristicPanel.add(observationCharacteristic);
    characteristicPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 8 * WizardSettings.PADDING));
    
    // Standard
    JPanel standardPanel = WidgetFactory.makePanel(1);
    standardPanel.add(WidgetFactory.makeLabel("Standard:", false));
    observationStandard = WidgetFactory.makeOneLineTextField("<standard>");
    observationStandard.addMouseListener(mListener);

    standardPanel.add(observationStandard);
    standardPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 8 * WizardSettings.PADDING));
    
    //put them together
    classesPanel.add(entityPanel);
    classesPanel.add(WidgetFactory.makeDefaultSpacer());
    classesPanel.add(characteristicPanel);
    classesPanel.add(WidgetFactory.makeDefaultSpacer());
    classesPanel.add(standardPanel);

    this.add(classesPanel);

    this.add(WidgetFactory.makeHalfSpacer());
    this.add(WidgetFactory.makeDefaultSpacer());

  }

  private void showDialog(JTextField source) {
		OntologyClassSelectionPage page = new OntologyClassSelectionPage();
		
		// show the dialog
		ModalDialog dialog = 
			new ModalDialog(
					page,
					UIController.getInstance().getCurrentActiveWindow(),
					UISettings.POPUPDIALOG_WIDTH,
					UISettings.POPUPDIALOG_HEIGHT);
		
		//get the response back
		if (dialog.USER_RESPONSE == ModalDialog.OK_OPTION) {
			source.setText(page.getSelectedTerms().get(0));
		}
		page = null;
	}
  
  public void setAnnotation(Annotation a) {
	  this.annotation = a;
	  
	  //try to set the text field values
	  try {
		  String entity = annotation.getObservations().get(0).getEntity().getURI();
		  this.observationEntity.setText(entity);
	  }
	  catch (Exception e) {}
	  try {
		  String characteristic = annotation.getObservations().get(0).getMeasurements().get(0).getCharacteristics().get(0).getURI();
		  this.observationCharacteristic.setText(characteristic);
	  }
	  catch (Exception e) {}
	  try {
		  String standard = annotation.getObservations().get(0).getMeasurements().get(0).getStandard().getURI();
		  this.observationStandard.setText(standard);
	  }
	  catch (Exception e) {}
  }

  public Annotation getAnnotation() {
	  return annotation;
  }
  
  public Observation getObservation() {
	  // set up the annotation objects
	  Observation observation = new Observation();	  
	  Entity entity = new Entity(observationEntity.getText());
	  observation.setEntity(entity);
	  
	  Measurement measurement = new Measurement();
	  Characteristic characteristic = new Characteristic(observationCharacteristic.getText());
	  measurement.addCharacteristic(characteristic);
	  Standard standard = new Standard(observationStandard.getText());
	  measurement.setStandard(standard);
	  observation.addMeasurement(measurement);
	  
	  //add the observation to the annotation
	  return observation;
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

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

    return getPageData(null);
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



  public boolean setPageData(OrderedMap data, String _xPathRoot) {

    return true;
  }
}
