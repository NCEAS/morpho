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
 *     '$Date: 2004-01-13 21:59:31 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;


import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.awt.Color;
import java.text.DateFormat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.border.EmptyBorder;
import javax.swing.JComponent;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.toedter.calendar.JCalendar;

public class TemporalPage extends AbstractWizardPage {

  private final String pageID     = DataPackageWizardInterface.TEMPORAL_PAGE;
  private final String nextPageID = "";
  private final String pageNumber = "";
  private final String title      = "Access Page";
  private final String subtitle   = "";

  private final String EMPTY_STRING = "";
  private JPanel topPanel;
  private JLabel descLabel;

  private JPanel currentPanel;
  private JPanel singlePointPanel;
  private JPanel rangeTimePanel;

  private JTextField singleTimeTF;
  private JTextField startTimeTF;
  private JTextField endTimeTF;
  private JCalendar singleTimeCalendar;
  private JCalendar startTimeCalendar;
  private JCalendar endTimeCalendar;

  private final String[] timeTypeText = new String[] {
    "Single Point in Time",
    "Range of Date/Time"
  };

  private final String[] timeText = new String[] {
    "Enter Year 0nly",
    "Enter Month and Year",
    "Enter Day, Month and Year"
  };

  private final String[] Months = new String[] {
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  };

  private final String xPathRoot  = "/eml:eml/dataset/coverage/temporalCoverage";

  private static final Dimension PANEL_DIMS = new Dimension(325,220);
  private static final int YYYYMMDD = 8;
  private static final int ALL = 4;
  private static final int MONTH_YEAR = 2;
  private static final int YEAR_ONLY = 1;

  public TemporalPage() {
          init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BorderLayout());
 //   this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

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
    typeRadioOuterPanel.add(typeRadioPanel);

    topPanel.add(WidgetFactory.makeDefaultSpacer());
    topPanel.add(WidgetFactory.makeDefaultSpacer());

    descLabel = WidgetFactory.makeHTMLLabel(
        "<p><b>Choose access type:</b> Choose to allow or deny the user the "
        +"below defined permission.</p>", 1);
    topPanel.add(descLabel);
    topPanel.add(typeRadioOuterPanel);

    topPanel.setBorder(new javax.swing.border.EmptyBorder(0,4*WizardSettings.PADDING,
        0,0));
    this.add(topPanel, BorderLayout.NORTH);

    singlePointPanel = getSinglePointPanel();
    rangeTimePanel  = getRangeTimePanel();

    currentPanel = singlePointPanel;
    this.add(singlePointPanel, BorderLayout.CENTER);
  }

  /**
   *  Function returns a JPanel for selecting a single point of time
   *
   *  @return JPanel to select a single point of time.
   */

  public JPanel getSinglePointPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    singleTimeTF = new JTextField();
    singleTimeTF.setEditable(false);
    singleTimeTF.setBackground(Color.WHITE);

    singleTimeCalendar = new JCalendar();
    singleTimeCalendar.setVisible(true);

    JPanel singlePanel = getDateTimePanel("Enter date:", "Time",
                                          singleTimeTF, singleTimeCalendar);
    panel.add(singlePanel);
    panel.setBorder(new javax.swing.border.EmptyBorder(0,40*WizardSettings.PADDING,
        0,4*WizardSettings.PADDING));
    return panel;
  }


  /**
   *  Function returns a JPanel for selecting range of time
   *
   *  @return JPanel to select a single point of time.
   */

  public JPanel getRangeTimePanel() {
    JPanel panel = new JPanel();

    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    startTimeTF = new JTextField();
    startTimeTF.setEditable(false);
    startTimeTF.setBackground(Color.WHITE);

    startTimeCalendar = new JCalendar();
    startTimeCalendar.setVisible(true);

    JPanel startingPanel = getDateTimePanel("Enter starting date:",
                                            "Start Time", startTimeTF,
                                            startTimeCalendar);
    panel.add(startingPanel);

    endTimeTF = new JTextField();
    endTimeTF.setEditable(false);
    endTimeTF.setBackground(Color.WHITE);

    endTimeCalendar = new JCalendar();
    endTimeCalendar.setVisible(true);

    JPanel endingPanel = getDateTimePanel("Enter ending date:", "End Time",
                                          endTimeTF, endTimeCalendar);
    panel.add(endingPanel);

    panel.setBorder(new javax.swing.border.EmptyBorder(0,8*WizardSettings.PADDING,
        0,4*WizardSettings.PADDING));
    return panel;
  }


  /**
   *  Function returns a JPanel for selecting date
   *
   *  @return JPanel to select a date.
   */

  public JPanel getDateTimePanel(String panelHeading, String buttonText,
                                 JTextField timeTextField, JCalendar timeCalendar) {

    JPanel outerPanel = new JPanel();
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));

    WidgetFactory.addTitledBorder(outerPanel, panelHeading);

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(new javax.swing.border.EmptyBorder(2*WizardSettings.PADDING,
        4*WizardSettings.PADDING,0,
        4*WizardSettings.PADDING));

    timeTextField.setText(calendarToString(timeCalendar, ALL));
    panel.add(timeTextField, BorderLayout.NORTH);

    final JCalendar finalTimeCalendar = timeCalendar;
    final JTextField finalTimeTextField = timeTextField;

    PropertyChangeListener propertyListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        Log.debug(45, "got radiobutton command: "+e.getPropertyName());
        if (e.getPropertyName().equals("calendar")) {
          if(finalTimeCalendar.getDayChooser().isEnabled()){
            finalTimeTextField.setText(calendarToString(finalTimeCalendar, ALL));
          } else if(finalTimeCalendar.getMonthChooser().isEnabled()){
            finalTimeTextField.setText(calendarToString(finalTimeCalendar, MONTH_YEAR));
          } else {
            finalTimeTextField.setText(calendarToString(finalTimeCalendar, YEAR_ONLY));
          }
        }
      }
    };

    timeCalendar.addPropertyChangeListener(propertyListener);
    panel.add(timeCalendar, BorderLayout.CENTER);

    ActionListener dayTypeListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Log.debug(45, "got radiobutton command: "+e.getActionCommand());
        onLoadAction();
        if (e.getActionCommand().equals(timeText[0])) {
          finalTimeCalendar.getDayChooser().setEnabled(false);
          finalTimeCalendar.getMonthChooser().setEnabled(false);
          finalTimeCalendar.getYearChooser().setEnabled(true);
          finalTimeTextField.setText(calendarToString(finalTimeCalendar, YEAR_ONLY));
        } else if (e.getActionCommand().equals(timeText[1])) {
          finalTimeCalendar.getDayChooser().setEnabled(false);
          finalTimeCalendar.getMonthChooser().setEnabled(true);
          finalTimeCalendar.getYearChooser().setEnabled(true);
          finalTimeTextField.setText(calendarToString(finalTimeCalendar, MONTH_YEAR));
        } else if (e.getActionCommand().equals(timeText[2])) {
          finalTimeCalendar.getDayChooser().setEnabled(true);
          finalTimeCalendar.getMonthChooser().setEnabled(true);
          finalTimeCalendar.getYearChooser().setEnabled(true);
          finalTimeTextField.setText(calendarToString(finalTimeCalendar, ALL));
        }
      }
    };

    JPanel typeRadioPanel = WidgetFactory.makeRadioPanel(timeText, 2, dayTypeListener);
    panel.add(typeRadioPanel, BorderLayout.SOUTH);

    setPrefMinMaxSizes(panel, PANEL_DIMS);
    outerPanel.add(panel);

    return outerPanel;
  }

  private String calendarToString(JCalendar c, int returnType){
    Calendar calendar = c.getCalendar();
    DateFormat df = DateFormat.getDateInstance(DateFormat.LONG,
        c.getLocale());

    if(returnType == YEAR_ONLY){
      return calendar.get(Calendar.YEAR) + "";
    }
    if(returnType == MONTH_YEAR){
      return Months[calendar.get(Calendar.MONTH)] + "," +
          calendar.get(Calendar.YEAR);
    }
    if(returnType == YYYYMMDD){
      String dateString = calendar.get(Calendar.YEAR) + "-";

      int month = calendar.get(Calendar.MONTH) + 1;
      int day = calendar.get(Calendar.DAY_OF_MONTH) + 1;

      if(returnType != YEAR_ONLY){
        if (month < 10) {
          dateString = dateString + "0" + month + "-";
        }
        else {
          dateString = dateString + month + "-";
        }
      } else {
        dateString = dateString + "01" + "-";
      }

      if(returnType != ALL){
        dateString = dateString + "01";
      } else {
        if (day < 10) {
          dateString = dateString + "0" + day;
        }
        else {
          dateString = dateString + day;
        }
      }

      return dateString;
    }
    return df.format(calendar.getTime());
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

    if(currentPanel == singlePointPanel){
      surrogate.add(" " + singleTimeTF.getText().trim());
    } else {
      surrogate.add(startTimeTF.getText().trim() + "-" +
                    endTimeTF.getText().trim());
    }
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
    if(currentPanel == singlePointPanel){
      returnMap.put(xPathRoot + "/singleDateTime/calendarDate",
                    calendarToString(singleTimeCalendar, YYYYMMDD));

    } else {
      returnMap.put(xPathRoot + "/startDateTime/calendarDate",
                    calendarToString(startTimeCalendar, YYYYMMDD));

      returnMap.put(xPathRoot + "/endDateTime/calendarDate",
                    calendarToString(endTimeCalendar, YYYYMMDD));
    }

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