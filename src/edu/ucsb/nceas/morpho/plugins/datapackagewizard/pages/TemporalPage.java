/**
 *  '$RCSfile: TemporalPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-24 02:14:18 $'
 * '$Revision: 1.16 $'
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
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.toedter.calendar.JCalendar;

/**
 * <p>This is the actual temporal dialog page, where the user enters temporal
 * range limits</p>
 */
public class TemporalPage extends AbstractUIPage {

  private final String pageID     = DataPackageWizardInterface.TEMPORAL_PAGE;
  private final String nextPageID = "";
  private final String pageNumber = "";
  private final String title      = "Access Page";
  private final String subtitle   = "";

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

  private static final Dimension PANEL_DIMS = new Dimension(325,350);
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

    typeRadioOuterPanel.add(WidgetFactory.makeLabel("", false));
    typeRadioOuterPanel.add(typeRadioPanel);

    topPanel.add(WidgetFactory.makeDefaultSpacer());

    descLabel = WidgetFactory.makeHTMLLabel(
        "<p><b>Choose date type:</b>", 1);
    topPanel.add(descLabel);
    topPanel.add(typeRadioOuterPanel);

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

    singleTimeCalendar = new JCalendar();
    singleTimeCalendar.setVisible(true);
    singleTimeCalendar.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    singleTimeCalendar.getMonthChooser().setFont(WizardSettings.WIZARD_CONTENT_FONT);

    JPanel singlePanel = getDateTimePanel("Enter date:",
                                          singleTimeTF, singleTimeCalendar);

    panel.add(Box.createGlue());

    panel.add(singlePanel);

    panel.add(Box.createGlue());

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

    startTimeCalendar = new JCalendar();
    startTimeCalendar.setVisible(true);

    JPanel startingPanel = getDateTimePanel("Enter starting date:",
                                            startTimeTF,
                                            startTimeCalendar);
    panel.add(Box.createGlue());

    panel.add(startingPanel);

    endTimeTF = new JTextField();
    endTimeTF.setEditable(false);

    endTimeCalendar = new JCalendar();
    endTimeCalendar.setVisible(true);

    JPanel endingPanel = getDateTimePanel("Enter ending date:",
                                          endTimeTF, endTimeCalendar);
    panel.add(endingPanel);

    panel.add(Box.createGlue());

    return panel;
  }


  /**
   * Function returns a JPanel for selecting date
   *
   * @return JPanel to select a date.
   * @param panelHeading String
   * @param buttonText String
   * @param timeTextField JTextField
   * @param timeCalendar JCalendar
   */
  public JPanel getDateTimePanel(String panelHeading,
                                 JTextField timeTextField, JCalendar timeCalendar) {

    JPanel outerPanel = new JPanel();
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));

    WidgetFactory.addTitledBorder(outerPanel, panelHeading);

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(new EmptyBorder(2*WizardSettings.PADDING,
        4*WizardSettings.PADDING,0,
        4*WizardSettings.PADDING));


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

    JPanel typeRadioContainer = WidgetFactory.makeVerticalPanel(4);

    JPanel typeRadioPanel
        = WidgetFactory.makeRadioPanel(timeText, 2, dayTypeListener);

    typeRadioContainer.add(typeRadioPanel);
    typeRadioContainer.add(Box.createGlue());

    panel.add(typeRadioContainer, BorderLayout.NORTH);

    timeTextField.setText(calendarToString(timeCalendar, ALL));

    JPanel calendarPanel = new JPanel();
    calendarPanel.setLayout(new BoxLayout(calendarPanel, BoxLayout.Y_AXIS));

    WidgetFactory.setPrefMaxSizes(timeTextField,
                                  WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);
    calendarPanel.add(Box.createVerticalStrut(5));
    calendarPanel.add(timeTextField);
    calendarPanel.add(Box.createVerticalStrut(5));

    timeCalendar.addPropertyChangeListener(propertyListener);
    calendarPanel.add(timeCalendar);

    calendarPanel.add(Box.createGlue());
    panel.add(calendarPanel, BorderLayout.CENTER);

    setPrefMinMaxSizes(panel, PANEL_DIMS);
    outerPanel.add(panel);

    return outerPanel;
  }


  /**
   *  Function returns a string which specifies the date which
   *  JCalendar c has. These can be returned in four formats based on
   *  returntype specified. YEAR_ONLY format takes the year from c and
   *  set month to January and Date to 01. MONTH_YEAR format takes the
   *  year and month from JCalendar c and the date is set to 01. ALL takes
   *  all three values from  JCalendar c. YYYYMMDD returns a string which
   *  specifies the date from JCalendar c as YYYY-MM-DD
   *
   *  @param c JCalendar
   *  @param returnType int
   *  @return String
   */
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

      if(c.getMonthChooser().isEnabled()){
        if (month < 10) {
          dateString = dateString + "0" + month + "-";
        }
        else {
          dateString = dateString + month + "-";
        }
      } else {
        dateString = dateString + "01" + "-";
      }

      if(!c.getDayChooser().isEnabled()){
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
    if(currentPanel == rangeTimePanel){
      Calendar sCalendar = startTimeCalendar.getCalendar();
      Calendar eCalendar = endTimeCalendar.getCalendar();
      if(sCalendar.after(eCalendar)){
        Log.debug(1,"Starting date should be before ending date.");
        return false;
      }
    }
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
      surrogate.add(" " + startTimeTF.getText().trim() + "-" +
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

      returnMap.put(xPathRoot + "/rangeOfDates/beginDate/calendarDate",
                    calendarToString(startTimeCalendar, YYYYMMDD));

      returnMap.put(xPathRoot + "/rangeOfDates/endDate/calendarDate",
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

    public boolean setPageData(OrderedMap data, String xPathRoot) { return false; }
}
