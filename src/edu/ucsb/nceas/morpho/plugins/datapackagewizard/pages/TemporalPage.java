/**
 *  '$RCSfile: TemporalPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-13 03:57:28 $'
 * '$Revision: 1.23 $'
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
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import com.toedter.calendar.JCalendar;

/**
 * <p>This is the actual temporal dialog page, where the user enters temporal
 * range limits</p>
 */
public class TemporalPage extends AbstractUIPage {

  private final String pageID     = DataPackageWizardInterface.TEMPORAL_PAGE;
  private final String pageNumber = "";
  private final String title      = "Access Page";
  private final String subtitle   = "";

  private int selDateTypeSingle = 1;
  private int selDateTypeStart = 1;
  private int selDateTypeEnd = 1;

  private JPanel topPanel;
  private JLabel descLabel;

  private JPanel currentPanel;
  private JPanel singlePointPanel;
  private JPanel rangeTimePanel;
  private JPanel dateTypeRadioPanel;

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
    "Enter Year Only",
    //"Enter Month and Year",
    "Enter Day, Month and Year"
  };

  private final String[] Months = new String[] {
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  };

  private String xPathRoot  = "/eml:eml/dataset/coverage/temporalCoverage";

  private static final Dimension PANEL_DIMS = new Dimension(325,350);
  private static final int YYYYMMDD = 8;
  private static final int ALL = 4;
  //private static final int MONTH_YEAR = 2;
  private static final int YEAR_ONLY = 1;

  public TemporalPage() {
	      nextPageID = "";
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
			enableSingleDatePanel();
        } else if (e.getActionCommand().equals(timeTypeText[1])) {
			enableDateRangePanel();
        }
        instance.validate();
        instance.repaint();
      }
    };

    JPanel dateTypeRadioOuterPanel = WidgetFactory.makePanel(2);
    dateTypeRadioPanel = WidgetFactory.makeRadioPanel(timeTypeText, 0, accessTypeListener);

    dateTypeRadioOuterPanel.add(WidgetFactory.makeLabel("", false));
    dateTypeRadioOuterPanel.add(dateTypeRadioPanel);

    topPanel.add(WidgetFactory.makeDefaultSpacer());

    descLabel = WidgetFactory.makeHTMLLabel(
        "<p><b>Choose date type:</b>", 1);
    topPanel.add(descLabel);
    topPanel.add(dateTypeRadioOuterPanel);

    this.add(topPanel, BorderLayout.NORTH);

    singlePointPanel = getSinglePointPanel();
    rangeTimePanel  = getRangeTimePanel();
    if (currentPanel==null) {
      currentPanel = singlePointPanel;
    }
    remove(currentPanel);
    if (currentPanel==singlePointPanel) {
      this.add(singlePointPanel, BorderLayout.CENTER);
    }
    else {
      this.add(rangeTimePanel, BorderLayout.CENTER);      
    }
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

	if (singleTimeCalendar == null) {
		singleTimeCalendar = new JCalendar();
	}
    singleTimeCalendar.setVisible(true);
    singleTimeCalendar.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    singleTimeCalendar.getMonthChooser().setFont(WizardSettings.WIZARD_CONTENT_FONT);

    JPanel singlePanel = getDateTimePanel("Enter date:", singleTimeTF, 
			singleTimeCalendar, selDateTypeSingle);

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

	if (startTimeCalendar == null) {
    	startTimeCalendar = new JCalendar();
	}
    startTimeCalendar.setVisible(true);

    JPanel startingPanel = getDateTimePanel("Enter starting date:",
                                            startTimeTF,
                                            startTimeCalendar, selDateTypeStart);
    panel.add(Box.createGlue());

    panel.add(startingPanel);

    endTimeTF = new JTextField();
    endTimeTF.setEditable(false);

	if (endTimeCalendar == null) {
    	endTimeCalendar = new JCalendar();
	}
    endTimeCalendar.setVisible(true);

    JPanel endingPanel = getDateTimePanel("Enter ending date:",
                                          endTimeTF, endTimeCalendar, selDateTypeEnd);
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
  public JPanel getDateTimePanel(String panelHeading, JTextField timeTextField, 
				JCalendar timeCalendar, int selDateType) {

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

	// sets the text field value based on date type radio
    PropertyChangeListener propertyListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        Log.debug(45, "got radiobutton command: "+e.getPropertyName());
        if (e.getPropertyName().equals("calendar")) {
          if(finalTimeCalendar.getDayChooser().isEnabled()){
            finalTimeTextField.setText(calendarToString(finalTimeCalendar, ALL));
			/*
          } else if(finalTimeCalendar.getMonthChooser().isEnabled()){
            finalTimeTextField.setText(calendarToString(finalTimeCalendar, MONTH_YEAR));
		  */
          } else {
            finalTimeTextField.setText(calendarToString(finalTimeCalendar, YEAR_ONLY));
          }
        }
      }
    };


	// handles change to date type radio
    ActionListener dayTypeListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Log.debug(45, "got radiobutton command: "+e.getActionCommand());
        onLoadAction();
        if (e.getActionCommand().equals(timeText[0])) {
			enableYearOnlyFields(finalTimeCalendar, finalTimeTextField);
			/*
        } else if (e.getActionCommand().equals(timeText[1])) {
			enableMonthYearFields(finalTimeCalendar, finalTimeTextField);
		*/
        } else if (e.getActionCommand().equals(timeText[1])) {
			enableDayMonthYearFields(finalTimeCalendar, finalTimeTextField);
        }
      }
    };

    JPanel typeRadioContainer = WidgetFactory.makeVerticalPanel(3);

    JPanel dateFieldsRadioPanel
        = WidgetFactory.makeRadioPanel(timeText, selDateType, dayTypeListener);

	switch (selDateType) {
	case 0:
		enableYearOnlyFields(timeCalendar, timeTextField);
		break;
		/*
	case 1:
		enableMonthYearFields(timeCalendar, timeTextField);
		break;
		*/
	default:
		enableDayMonthYearFields(timeCalendar, timeTextField);
		break;
	}
	//Log.debug(1, "set date text field: " + selDateType + ": " + timeTextField.getText());

    typeRadioContainer.add(dateFieldsRadioPanel);
    typeRadioContainer.add(Box.createGlue());

    panel.add(typeRadioContainer, BorderLayout.NORTH);

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
	/*
    if(returnType == MONTH_YEAR){
      return Months[calendar.get(Calendar.MONTH)] + "," +
          calendar.get(Calendar.YEAR);
    }
	*/
    if(returnType == YYYYMMDD){
      String dateString = calendar.get(Calendar.YEAR) + "-";

      int month = calendar.get(Calendar.MONTH) + 1;
      int day = calendar.get(Calendar.DAY_OF_MONTH);

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
		// single date
		String singleDate;
		if(singleTimeCalendar.getDayChooser().isEnabled()){
			singleDate = calendarToString(singleTimeCalendar, YYYYMMDD);
			/*
		} else if(singleTimeCalendar.getMonthChooser().isEnabled()){
			singleDate = calendarToString(singleTimeCalendar, MONTH_YEAR);
			*/
		} else {
			singleDate = calendarToString(singleTimeCalendar, YEAR_ONLY);
		}

		returnMap.put(xPathRoot + "/singleDateTime/calendarDate", singleDate);

    } else {
		// date range
		String startDate, endDate;

		if(startTimeCalendar.getDayChooser().isEnabled()){
			startDate = calendarToString(startTimeCalendar, YYYYMMDD);
			/*
		} else if(startTimeCalendar.getMonthChooser().isEnabled()){
			startDate = calendarToString(startTimeCalendar, MONTH_YEAR);
			*/
		} else {
			startDate = calendarToString(startTimeCalendar, YEAR_ONLY);
		}

		if(endTimeCalendar.getDayChooser().isEnabled()){
			endDate = calendarToString(endTimeCalendar, YYYYMMDD);
			/*
		} else if(endTimeCalendar.getMonthChooser().isEnabled()){
			endDate = calendarToString(endTimeCalendar, MONTH_YEAR);
			*/
		} else {
			endDate = calendarToString(endTimeCalendar, YEAR_ONLY);
		}

		returnMap.put(xPathRoot + "/rangeOfDates/beginDate/calendarDate", startDate);
		returnMap.put(xPathRoot + "/rangeOfDates/endDate/calendarDate", endDate);
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

  public boolean setPageData(OrderedMap map, String _xPathRoot) { 

    if (_xPathRoot!=null && _xPathRoot.trim().length() > 0) this.xPathRoot = _xPathRoot;

    String dateString = (String)map.get(xPathRoot + "/singleDateTime[1]/calendarDate[1]");
    if (dateString!=null) {
		// single date
		map.remove(xPathRoot + "/singleDateTime[1]/calendarDate[1]");

		Calendar singleCalendar = createCalendarFromDateString(dateString);
		singleTimeCalendar.setCalendar(singleCalendar);

		switch (dateString.length()) {
			case 4: selDateTypeSingle = 0; break;
			//case 7: selDateTypeSingle = 1; break;
			default: selDateTypeSingle = 1;
		}
		//Log.debug(1, "Setting single date radio button values: " + selDateTypeSingle + ": " + dateString);

		activateSingleDateMode();

    }
    else {
			// date range
      String startString = (String)map.get(xPathRoot + "/rangeOfDates[1]/beginDate[1]/calendarDate[1]");
      String endString = (String)map.get(xPathRoot + "/rangeOfDates[1]/endDate[1]/calendarDate[1]");

      if ((startString!=null)&&(endString!=null)) {
        map.remove(xPathRoot + "/rangeOfDates[1]/beginDate[1]/calendarDate[1]");
        map.remove(xPathRoot + "/rangeOfDates[1]/endDate[1]/calendarDate[1]");

		Calendar startCalendar = createCalendarFromDateString(startString);
		Calendar endCalendar = createCalendarFromDateString(endString);

        startTimeCalendar.setCalendar(startCalendar);
        endTimeCalendar.setCalendar(endCalendar);

		//Log.debug(45,"Setting date range radio button values.");
		switch (startString.length()) {
			case 4: selDateTypeStart = 0; break;
			//case 7: selDateTypeStart = 1; break;
			default: selDateTypeStart = 1;
		}
		switch (endString.length()) {
			case 4: selDateTypeEnd = 0; break;
			//case 7: selDateTypeEnd = 1; break;
			default: selDateTypeEnd = 1;
		}

		activateDateRangeMode();
	
		// TODO: get the date range calendar panel and set the proper date field radios
		// based on start/endString
				
      }

    }

    //if anything left in map, then it included stuff we can't handle...
     boolean returnVal = map.isEmpty();

     if (!returnVal) {

       Log.debug(20, "TemporalPage.setPageData returning FALSE! Map still contains:" + map);
     }
     return returnVal;
  }



	/**
	 * Given a date in YYYY-MM-DD format or YYYY,
	 * returns a Calendar with those values set.
	 */
	private Calendar createCalendarFromDateString(String dateString) {
		if (dateString == null || dateString.length() == 0) { return null; }
		
		Calendar cal = Calendar.getInstance();
		try {
			Log.debug(30, "creating cal: " + dateString);
			String monthS, dayS, yearS;
			int month, day, year;

			yearS = dateString.substring(0,4);
			year = (new Integer(yearS)).intValue();
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DATE, 1);

			if (dateString.length() > 7) {
				monthS = dateString.substring(5,7);
				month = (new Integer(monthS)).intValue();
				cal.set(Calendar.MONTH, month-1);

				dayS = dateString.substring(8,10);
				day = (new Integer(dayS)).intValue();
				cal.set(Calendar.DATE, day);
			}

		} catch (Exception ex) {
			cal = Calendar.getInstance();
			Log.debug(1, "Problem creating calendar from: " + dateString);
		}

		return cal;
	}


	private void enableYearOnlyFields(JCalendar cal, JTextField f) {
		cal.getDayChooser().setEnabled(false);
		/*
		cal.getMonthChooser().setEnabled(false);
		*/
		cal.getYearChooser().setEnabled(true);
		f.setText(calendarToString(cal, YEAR_ONLY));
	}

		/*
	private void enableMonthYearFields(JCalendar cal, JTextField f) {
		cal.getDayChooser().setEnabled(false);
		cal.getMonthChooser().setEnabled(true);
		cal.getYearChooser().setEnabled(true);
		f.setText(calendarToString(cal, MONTH_YEAR));
	}
		*/

	private void enableDayMonthYearFields(JCalendar cal, JTextField f) {
		cal.getDayChooser().setEnabled(true);
		/*
		cal.getMonthChooser().setEnabled(true);
		*/
		cal.getYearChooser().setEnabled(true);
		f.setText(calendarToString(cal, ALL));
	}

	private void enableSingleDatePanel() {
		this.remove(currentPanel);
		// rebuild the panel
		singlePointPanel = getSinglePointPanel();
		currentPanel = singlePointPanel;
		//this.add(singlePointPanel);
		this.add(singlePointPanel, BorderLayout.CENTER);
	}

	private void enableDateRangePanel() {
		this.remove(currentPanel);
		// rebuild the panels
		rangeTimePanel = getRangeTimePanel();
		currentPanel = rangeTimePanel;
		//this.add(rangeTimePanel);
		this.add(rangeTimePanel, BorderLayout.CENTER);
	}

	/**
	 * Sets the single date radio and enables calendar panel.
	 */
	private void activateSingleDateMode() {
		JPanel radioPanel = (JPanel)dateTypeRadioPanel.getComponent(1);
		// first radio button
		((JRadioButton)radioPanel.getComponent(0)).setSelected(true);
		enableSingleDatePanel();
	}

	/**
	 * Sets the date range radio and enables calendar panels.
	 */
	private void activateDateRangeMode() {
		JPanel radioPanel = (JPanel)dateTypeRadioPanel.getComponent(1);
		// second radio button
		((JRadioButton)radioPanel.getComponent(1)).setSelected(true);
		enableDateRangePanel();
	}

}
