/**
 *  '$RCSfile: Geographic.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-01-14 23:58:00 $'
 * '$Revision: 1.5 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.morpho.query.LiveMapPanel;
import edu.ucsb.nceas.morpho.util.Log;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import edu.ucsb.nceas.morpho.framework.ConfigXML;


public class Geographic extends AbstractWizardPage {

  public final String pageID     = DataPackageWizardInterface.GEOGRAPHIC;
  public final String nextPageID = null;
  public final String pageNumber = "1";
//////////////////////////////////////////////////////////

  public final String title      = "Geographic Information";
  public final String subtitle   = "Unintentionally left blank";
  
  private JTextArea   covDescField;
  private JLabel regionSelectionLabel;
  private JList regionList;
  private LiveMapPanel lmp;

  private ConfigXML locationsXML = null;
  
  public Geographic() {

    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel vbox = this;

    vbox.add(WidgetFactory.makeHalfSpacer());

    JLabel coverageDesc = WidgetFactory.makeHTMLLabel(
        "<b>Enter a description of the geographic coverage.</b> This provides a "
       +"textual description about the area relevent to the data. "
       +"It can used to provide further detail concerning the geographic area of concern.", 2);
    vbox.add(coverageDesc);

    JPanel covDescPanel = WidgetFactory.makePanel();

    JLabel covDescLabel = WidgetFactory.makeLabel(" Description:", true);
    covDescLabel.setVerticalAlignment(SwingConstants.TOP);
    covDescLabel.setAlignmentY(SwingConstants.TOP);
    covDescPanel.add(covDescLabel);

    covDescField = WidgetFactory.makeTextArea("", 6, true);
    JScrollPane jscrl = new JScrollPane(covDescField);
    covDescPanel.add(jscrl);

    covDescPanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));
    vbox.add(covDescPanel);

    vbox.add(WidgetFactory.makeDefaultSpacer());

    JLabel bbDesc = WidgetFactory.makeHTMLLabel(
        "<b>Set the geographic coordinates which bound the coverage</b> Latitude and longitude"
       +"values are used to create a 'bounding box' containing the region of interest. "
       +"Drag or click on the map. Then edit the text boxes if necessary. "
       +"[Default entries are in fractional degrees. To enter in degreea/minutes/seconds, simply "
       +"type a space between the degrees, minutes, and seconds values]", 4);
    vbox.add(bbDesc);


    JPanel bboxPanel = WidgetFactory.makePanel();
    
    JLabel bboxLabel = WidgetFactory.makeLabel(" Bounding Box:", true);
    bboxLabel.setVerticalAlignment(SwingConstants.TOP);
    bboxLabel.setAlignmentY(SwingConstants.TOP);
    bboxPanel.add(bboxLabel);
    
    lmp = new LiveMapPanel();
    bboxPanel.add(lmp);

    bboxPanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));
    vbox.add(bboxPanel);

  ////////////////////////////////////////////////////////////////////////////

    JPanel regionPanel = new JPanel();
    regionPanel.setLayout(new GridLayout(1,2));
    JPanel regionSelectionPanel = WidgetFactory.makePanel(4);

    regionSelectionLabel = WidgetFactory.makeLabel(" Named Regions:", false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    regionSelectionLabel.setVerticalAlignment(SwingConstants.TOP);
    regionSelectionLabel.setAlignmentY(SwingConstants.TOP);
    regionSelectionPanel.add(regionSelectionLabel);

    
    Vector names = getLocationNames();
    regionList = new JList(names);
    regionList.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    regionList.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    regionList.setSelectedIndex(0);
    JScrollPane jscr2 = new JScrollPane(regionList);
    regionSelectionPanel.add(jscr2);
    JLabel regionHelpLabel = getLabel("Click button to display region.");
    JButton selectButton = new JButton("Select");
    selectButton.setPreferredSize(new Dimension(60,24));
    selectButton.setMaximumSize(new Dimension(60,24));
    selectButton.setMargin(new Insets(0, 2, 1, 2));
    selectButton.setEnabled(true);
    selectButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    selectButton.setFocusPainted(false);
    selectButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        String selection = (String)regionList.getSelectedValue();
//        Log.debug(1,"Selection: "+selection);
        double n = (new Double(getNorth(selection))).doubleValue();
        double w = (new Double(getWest(selection))).doubleValue();
        double s = (new Double(getSouth(selection))).doubleValue();
        double e = (new Double(getEast(selection))).doubleValue();
        lmp.setBoundingBox(n, w, s, e);
      }
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    buttonPanel.add(selectButton);
    buttonPanel.add(regionHelpLabel);
    
    
    regionPanel.add(regionSelectionPanel);
    regionPanel.add(buttonPanel);

    vbox.add(regionPanel);

    vbox.add(WidgetFactory.makeDefaultSpacer());
    
  }

  
  /**
   *  gets info for location list from file
   */
  private Vector getLocationNames() {
    try{
      locationsXML = new ConfigXML("./lib/locations.xml");
      Vector vec = locationsXML.getValuesForPath("name");
      return vec;
    } catch (Exception w) {
      Log.debug(5, "problem reading locations file!");      
    }
    return null;
  }
  
  private String getNorth(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath("location/name[.='"+locname+"']/../north");
     res = (String)vec.firstElement();
    }
    return res;
  }
  private String getWest(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath("location/name[.='"+locname+"']/../west");
     res = (String)vec.firstElement();
    }
    return res;
  }
  private String getSouth(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath("location/name[.='"+locname+"']/../south");
     res = (String)vec.firstElement();
    }
    return res;
  }
  private String getEast(String locname) {
    String res = "";
    if (locationsXML!=null) {
     Vector vec = locationsXML.getValuesForPath("location/name[.='"+locname+"']/../east");
     res = (String)vec.firstElement();
    }
    return res;
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
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  public OrderedMap getPageData() {

    return null;
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

  public void setPageData(OrderedMap data) { }
  
  private JLabel getLabel(String text) {
    if (text==null) text="";
    JLabel label = new JLabel(text);

    label.setAlignmentX(1.0f);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    label.setBorder(BorderFactory.createMatteBorder(1,10,1,3, (Color)null));

    return label;
  }
  
    /**
   *  This is a static main method configured to test the class 
   */
  static public void main(String args[]) {
    JFrame frame = new JFrame("Demo/Test");
    frame.setSize(800, 600);
    frame.getContentPane().setLayout(new BorderLayout());
    Geographic geo = new Geographic();
    
    frame.getContentPane().add(geo, BorderLayout.CENTER);
    frame.setVisible(true);

  }

}
