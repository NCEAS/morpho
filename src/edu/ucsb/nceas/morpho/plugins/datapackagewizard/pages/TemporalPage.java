/**
 *  '$RCSfile: TemporalPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-01-09 23:03:50 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;


import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TemporalPage extends AbstractWizardPage {

  private final String pageID     = DataPackageWizardInterface.ACCESS_PAGE;
  private final String nextPageID = "";
  private final String pageNumber = "";
  private final String title      = "Access Page";
  private final String subtitle   = "";

  private final String EMPTY_STRING = "";
  private JPanel topPanel;
  private JTextField dnField;
  private JLabel dnLabel;
  private JLabel descLabel;
  private String userAccessType   = new String("Allow");
  private String userAccess       = new String("Read");

  private JPanel currentPanel;
  private JPanel singlePointPanel;
  private JPanel rangeTimePanel;

  private final String[] timeTypeText = new String[] {
    "Single Point in Time",
    "Range of Date/Time"
  };

  private final String xPathRoot  = "/eml:eml/dataset/access";

  private static final Dimension PARTY_COL_LABEL_DIMS = new Dimension(60,20);

  public TemporalPage() {
          init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

//    this.setLayout(new BorderLayout());
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    topPanel = new JPanel();

    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc = WidgetFactory.makeHTMLLabel(
                      "<font size=\"4\"><b>Define Temporal Coverage:</b></font>", 1);
    topPanel.add(desc);

    final JPanel instance = this;

    ActionListener accessTypeListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Log.debug(45, "got radiobutton command: "+e.getActionCommand());
        onLoadAction();
        if (e.getActionCommand().equals(timeTypeText[0])) {
          instance.remove(currentPanel);
          currentPanel = singlePointPanel;
          instance.add(singlePointPanel);
        } else if (e.getActionCommand().equals(timeTypeText[1])) {
          instance.remove(currentPanel);
          currentPanel = rangeTimePanel;
          instance.add(rangeTimePanel);
        }

        instance.validate();
        instance.repaint();
      }
    };

    JPanel typeRadioOuterPanel = WidgetFactory.makePanel(2);
    JPanel typeRadioPanel = WidgetFactory.makeRadioPanel(timeTypeText, 0, accessTypeListener);
    typeRadioPanel.setBorder(new javax.swing.border.EmptyBorder(0,4*WizardSettings.PADDING,0,0));

    topPanel.add(WidgetFactory.makeDefaultSpacer());
    topPanel.add(WidgetFactory.makeDefaultSpacer());
    typeRadioOuterPanel.add(typeRadioPanel);
    descLabel = WidgetFactory.makeHTMLLabel(
        "<p><b>Choose access type:</b> Choose to allow or deny the user the "
        +"below defined permission.</p>", 1);
    topPanel.add(descLabel);
    topPanel.add(typeRadioOuterPanel);
    topPanel.add(WidgetFactory.makeDefaultSpacer());
    topPanel.add(WidgetFactory.makeDefaultSpacer());

    topPanel.setBorder(new javax.swing.border.EmptyBorder(0,4*WizardSettings.PADDING,
        5*WizardSettings.PADDING,8*WizardSettings.PADDING));

    this.add(topPanel);

    singlePointPanel = getSinglePointPanel();
    rangeTimePanel  = getRangeTimePanel();
    currentPanel = singlePointPanel;
    this.add(singlePointPanel);
  }

  /**
   *  Function returns a JPanel for selecting a single point of time
   *
   *  @return JPanel to select a single point of time.
   */

  public JPanel getSinglePointPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JLabel desc = WidgetFactory.makeHTMLLabel(
                      "<b>Enter date and time:</b>", 1);
    panel.add(desc);
    desc.setBorder(new javax.swing.border.EmptyBorder(0,4*WizardSettings.PADDING,
                          0,8*WizardSettings.PADDING));

    JPanel singlePanel = getDateTimePanel();
    panel.add(singlePanel);

    return panel;
  }


  /**
   *  Function returns a JPanel for selecting range of time
   *
   *  @return JPanel to select a single point of time.
   */

  public JPanel getRangeTimePanel() {
    JPanel panel = new JPanel();

    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JLabel desc = WidgetFactory.makeHTMLLabel(
                      "<b>Enter starting date:</b>", 1);
    desc.setBorder(new javax.swing.border.EmptyBorder(0,4*WizardSettings.PADDING,
                      0,8*WizardSettings.PADDING));

    panel.add(desc);

    JPanel startingPanel = getDateTimePanel();
    panel.add(startingPanel);
    panel.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc1 = WidgetFactory.makeHTMLLabel(
                      "<b>Enter ending date:</b>", 1);
    desc1.setBorder(new javax.swing.border.EmptyBorder(0,4*WizardSettings.PADDING,
                       0,8*WizardSettings.PADDING));
    panel.add(desc1);

    JPanel endingPanel = getDateTimePanel();
    panel.add(endingPanel);

    return panel;
  }


  /**
   *  Function returns a JPanel for selecting date
   *
   *  @return JPanel to select a date.
   */
  private String[] valueList = new String[] {""};
  public JPanel getDateTimePanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JPanel timePanel = WidgetFactory.makePanel(1);

    JLabel yearLabel = WidgetFactory.makeLabel("Year:", false);
    setPrefMinMaxSizes(yearLabel, PARTY_COL_LABEL_DIMS);
    timePanel.add(yearLabel);
    JComboBox yearBox = WidgetFactory.makePickList(valueList, true, 0, null);
    for (int count=1960; count < 2006; count++){
      yearBox.addItem("" + count);
    }
    timePanel.add(yearBox);

    JLabel monthLabel = WidgetFactory.makeLabel("   Month:", false);
    setPrefMinMaxSizes(monthLabel, PARTY_COL_LABEL_DIMS);
    timePanel.add(monthLabel);
    JComboBox monthBox = WidgetFactory.makePickList(valueList, false, 0, null);
    for (int count=1; count <= 12; count++){
      monthBox.addItem("" + count);
    }
    timePanel.add(monthBox);

    JLabel dayLabel = WidgetFactory.makeLabel("      Day:", false);
    setPrefMinMaxSizes(dayLabel, PARTY_COL_LABEL_DIMS);
    timePanel.add(dayLabel);
    JComboBox dayBox = WidgetFactory.makePickList(valueList, false, 0, null);
    for (int count=1; count <= 31; count++){
      dayBox.addItem("" + count);
    }
    timePanel.add(dayBox);

    timePanel.setBorder(new javax.swing.border.EmptyBorder(0,8*WizardSettings.PADDING,
        0,8*WizardSettings.PADDING));
    panel.add(timePanel);

    return panel;
  }


  private void setPrefMinMaxSizes(JComponent component, Dimension dims) {
    WidgetFactory.setPrefMaxSizes(component, dims);
    component.setMinimumSize(dims);
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
   *  @return a List contaiing 2 String elements - one for each column of the
   *  2-col list in which this surrogate is displayed
   *
   */
  private final StringBuffer surrogateBuff = new StringBuffer();
  //
  public List getSurrogate() {

    List surrogate = new ArrayList();

    // Get the value of the DN
    surrogate.add(" " + dnField.getText().trim());

    // Get access given to the user
    surrogate.add(" " + userAccessType + "   " + userAccess);

    return surrogate;
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
  private OrderedMap returnMap = new OrderedMap();
  //
  public OrderedMap getPageData() {
      return getPageData(xPathRoot);
  }

  public OrderedMap getPageData(String xPathRoot) {

    returnMap.clear();

    returnMap.put(xPathRoot + "/principal", dnField.getText().trim());

    returnMap.put(xPathRoot + "/permission", userAccess.toLowerCase());

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
  public String getPageID() { return this.pageID;}

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
  public String getNextPageID() { return this.nextPageID; }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  public void setPageData(OrderedMap data) {}
}
