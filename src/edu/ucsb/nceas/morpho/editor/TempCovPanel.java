/**
 *       Name: PartyPanel.java
 *    Purpose: Example dynamic editor class for XMLPanel
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-01-21 04:41:15 $'
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
package edu.ucsb.nceas.morpho.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Enumeration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import com.toedter.calendar.JCalendar;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * PartyPanel is an example of a special panel editor for
 * use with the DocFrame class. It is designed to
 *
 * @author higgins
 */
public class TempCovPanel extends JPanel
{

  private static final Dimension PARTY_2COL_LABEL_DIMS = new Dimension(70,20);

  private JPanel topPanel;
  private JPanel currentPanel;
  private JPanel singlePointPanel;
  private JPanel rangeTimePanel;

  private JTextField singleTimeTF;
  private JTextField startTimeTF;
  private JTextField endTimeTF;

  private JCalendar singleTimeCalendar;
  private JCalendar startTimeCalendar;
  private JCalendar endTimeCalendar;

  private JPanel singlePanel;
  private JPanel startPanel;
  private JPanel endPanel;

  private final String EMPTY_STRING = "";
  private final String CALENDAR_DATE = "calendarDate";
  private final String SINGLE_DATE = "singleDateTime";
  private final String START_DATE = "startDate";
  private final String END_DATE = "endDate";

  private JPanel typeRadioPanel;
  private JScrollPane  jsp;

  private static final int YYYYMMDD = 8;
  private static final int ALL = 4;
  private static final int MONTH_YEAR = 2;
  private static final int YEAR_ONLY = 1;
  private static final Dimension PANEL_DIMS = new Dimension(325,220);

  private DefaultMutableTreeNode nd = null;
  private DefaultMutableTreeNode nd1 = null;

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

  public TempCovPanel(DefaultMutableTreeNode node) {
    nd = node;
    JPanel jp = this;
    jp.setLayout(new BorderLayout());
    jp.setAlignmentX(Component.LEFT_ALIGNMENT);

    jsp = new javax.swing.JScrollPane();
    jp.add(BorderLayout.CENTER,jsp);

    NodeInfo info = (NodeInfo)(nd.getUserObject());
    jp.setMaximumSize(new Dimension(500,750));
    init(jp, jsp);
    jp.setVisible(true);
  }

  private void init(JPanel panel, javax.swing.JScrollPane scrollPane) {

    topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel typeRadioOuterPanel = WidgetFactory.makePanel(2);

    singlePointPanel = getSinglePointPanel();
    rangeTimePanel = getRangeTimePanel();

    if (isSingle(nd)) {
      typeRadioPanel = WidgetFactory.makeRadioPanel(timeTypeText, 0,
                                                    new dateActionListener());
      currentPanel = singlePointPanel;
      scrollPane.getViewport().add(singlePointPanel);
    }
    else {
      typeRadioPanel = WidgetFactory.makeRadioPanel(timeTypeText, 1,
                                                    new dateActionListener());
      currentPanel = rangeTimePanel;
      scrollPane.getViewport().add(rangeTimePanel);
    }

    typeRadioPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        4 * WizardSettings.PADDING, 0, 0));
    typeRadioOuterPanel.add(typeRadioPanel);

    topPanel.add(typeRadioOuterPanel);
    panel.add(topPanel, BorderLayout.NORTH);

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

     if (isSingle(nd)) {
       setCalendarValue(nd, SINGLE_DATE, singleTimeCalendar);
     }

     singlePanel = getDateTimePanel("Enter date:", "Time", singleTimeTF,
                                    singleTimeCalendar, SINGLE_DATE);
     panel.add(singlePanel);

     panel.setBorder(new javax.swing.border.EmptyBorder(0,8*WizardSettings.PADDING,
         0,8*WizardSettings.PADDING));
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

     startTimeTF = new JTextField();
     startTimeTF.setEditable(false);
     startTimeTF.setBackground(Color.WHITE);

     startTimeCalendar = new JCalendar();
     startTimeCalendar.setVisible(true);

     if (!isSingle(nd)) {
       setCalendarValue(nd, START_DATE, startTimeCalendar);
     }

     startPanel = getDateTimePanel("Enter starting date:", "Start Time",
                                   startTimeTF, startTimeCalendar, START_DATE);
     panel.add(startPanel);

     endTimeTF = new JTextField();
     endTimeTF.setEditable(false);
     endTimeTF.setBackground(Color.WHITE);

     endTimeCalendar = new JCalendar();
     endTimeCalendar.setVisible(true);

     if (!isSingle(nd)) {
       setCalendarValue(nd, END_DATE, endTimeCalendar);
     }

     endPanel = getDateTimePanel("Enter ending date:", "End Time", endTimeTF,
                                 endTimeCalendar, END_DATE);
     panel.add(endPanel);

     panel.setBorder(new javax.swing.border.EmptyBorder(0,8*WizardSettings.PADDING,
         0,8*WizardSettings.PADDING));
     return panel;
   }

   /**
     *  Function returns a JPanel for selecting date
     *
     *  @return JPanel to select a date.
     */

    public JPanel getDateTimePanel(String panelHeading, String buttonText,
                                   JTextField timeTextField,
                                   JCalendar timeCalendar, final String DATE) {

      JPanel outerPanel = new JPanel();
      outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));

      WidgetFactory.addTitledBorder(outerPanel, panelHeading);

      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      panel.setBorder(new javax.swing.border.EmptyBorder(2*WizardSettings.PADDING,
          WizardSettings.PADDING,0,
         WizardSettings.PADDING));

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
            String value = calendarToString(singleTimeCalendar, YYYYMMDD);
            setNodeValue(nd, DATE, value);
            Log.debug(10, nd + DATE + value);
          }
        }
      };

      timeCalendar.addPropertyChangeListener(propertyListener);
      panel.add(timeCalendar, BorderLayout.CENTER);

      ActionListener dayTypeListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Log.debug(45, "got radiobutton command: "+e.getActionCommand());
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
         return calendar.get(Calendar.YEAR) + EMPTY_STRING;
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
   *  This method searches for a descendent of the specified node
   *  with the specified name and returns boolean
   */
  private boolean isSingle(DefaultMutableTreeNode node) {

    Enumeration enum = node.breadthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();
      if (nodeName.equals(SINGLE_DATE)) {
        DefaultMutableTreeNode tnode = (DefaultMutableTreeNode)nd.getFirstChild();
        if(getValue(tnode, CALENDAR_DATE).trim().compareTo(EMPTY_STRING) > 0){
          return true;
        }
      }
    }
    return false;
  }


  private void setCalendarValue(DefaultMutableTreeNode node, String name, JCalendar calendar) {

    Enumeration enum = node.breadthFirstEnumeration();
    while (enum.hasMoreElements()) {

      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();

      if (nodeName.equals(name)) {
        DefaultMutableTreeNode tnode = (DefaultMutableTreeNode)nd.getFirstChild();
        String value = getValue(tnode, CALENDAR_DATE).trim();

        if(value.compareTo(EMPTY_STRING) > 0){
          java.util.StringTokenizer tokens = new java.util.StringTokenizer(value, "-");
          calendar.getYearChooser().setYear(Integer.valueOf(tokens.nextToken()).intValue());
          calendar.getMonthChooser().setMonth(Integer.valueOf(tokens.nextToken()).intValue() - 1);
          calendar.getDayChooser().setDay(Integer.valueOf(tokens.nextToken()).intValue() - 1);
        }
      }
    }
  }


  private void setNodeValue(DefaultMutableTreeNode node, String name, String value) {

    Enumeration enum = node.breadthFirstEnumeration();
    while (enum.hasMoreElements()) {

      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();

      if (nodeName.equals(name)) {
        DefaultMutableTreeNode tnode = (DefaultMutableTreeNode)nd.getFirstChild();
        setValue(tnode, CALENDAR_DATE, value);
        break;
      }
    }
  }

  /**
   *  This method searches for a descendent of the specified node
   *  with the specified name and returns the text value.
   */
  private String getValue(DefaultMutableTreeNode node, String name) {
    String ret = null;
    Enumeration enum = node.breadthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();
     if (nodeName.equals(name)) {
        DefaultMutableTreeNode tnode = (DefaultMutableTreeNode)nd.getFirstChild();
        NodeInfo tni = (NodeInfo)tnode.getUserObject();
        ret = tni.getPCValue();
        return ret;
      }
    }
    return ret;
  }

    /**
   *  This method searches for a descendent of the specified node
   *  with the specified name, and the specified attribute with the given name
   *  and returns the text value.
   */
  private String getValue(DefaultMutableTreeNode node, String name, String attrName, String attrVal) {
    String ret = null;
    Enumeration enum = node.breadthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();
     if (nodeName.equals(name)) {
        if ((ni.attr.containsKey(attrName))&&(((String)(ni.attr.get(attrName))).equals(attrVal))) {
          DefaultMutableTreeNode tnode = (DefaultMutableTreeNode)nd.getFirstChild();
          NodeInfo tni = (NodeInfo)tnode.getUserObject();
          ret = tni.getPCValue();
          return ret;
        }
      }
    }
    return ret;
  }


  /**
   *  This method searches for a descendent of the specified node
   *  with the specified name and sets the text value.
   */
  private void setValue(DefaultMutableTreeNode node, String name, String val) {
    String ret = null;
    Enumeration enum = node.breadthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();
     if (nodeName.equals(name)) {
        DefaultMutableTreeNode tnode = (DefaultMutableTreeNode)nd.getFirstChild();
        NodeInfo tni = (NodeInfo)tnode.getUserObject();
        tni.setPCValue(val);
        break;
      }
    }
  }

  /**
   *  This method searches for a descendent of the specified node
   *  with the specified name, specified attribute name & value
   *  and and sets the text value.
   */
  private void setValue(DefaultMutableTreeNode node, String name, String val, String attrName, String attrVal) {
    String ret = null;
    Enumeration enum = node.breadthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();
     if (nodeName.equals(name)) {
       if ((ni.attr.containsKey(attrName))&&(((String)(ni.attr.get(attrName))).equals(attrVal))) {
         DefaultMutableTreeNode tnode = (DefaultMutableTreeNode)nd.getFirstChild();
         NodeInfo tni = (NodeInfo)tnode.getUserObject();
         tni.setPCValue(val);
        break;
       }
      }
    }
  }



  class dateActionListener implements java.awt.event.ActionListener
  {
    public void actionPerformed(java.awt.event.ActionEvent event)
    {

      Log.debug(45, "got radiobutton command: " + event.getActionCommand());

      if (event.getActionCommand().equals(timeTypeText[0])) {

        jsp.remove(currentPanel);
        currentPanel = singlePointPanel;

        jsp.getViewport().add(singlePointPanel);

  //      startTimeTF.setText(EMPTY_STRING);
  //      endTimeTF.setText(EMPTY_STRING);

        setValue(nd, START_DATE, EMPTY_STRING);
        setValue(nd, END_DATE, EMPTY_STRING);
      }
      else if (event.getActionCommand().equals(timeTypeText[1])) {

        jsp.remove(currentPanel);
        currentPanel = rangeTimePanel;

        jsp.getViewport().add(rangeTimePanel);

//        startTimeTF.setText(EMPTY_STRING);
        setValue(nd, SINGLE_DATE, EMPTY_STRING);
      }

      jsp.validate();
      jsp.repaint();
    }
  }


  class dateFocusListener extends java.awt.event.FocusAdapter {
    public void focusLost(java.awt.event.FocusEvent event)
    {
      Object object = event.getSource();

      Log.debug(10, object.toString());

      if (object == singlePanel) {
        Log.debug(10, "single");
        String value = calendarToString(singleTimeCalendar, YYYYMMDD);
        setNodeValue(nd, SINGLE_DATE, value);
      }

      if (object == startPanel) {
        Log.debug(10, "start");
        String value = calendarToString(startTimeCalendar, YYYYMMDD);
        setNodeValue(nd, START_DATE, value);
      }

      if (object == endPanel) {
        Log.debug(10, "end");
        String value = calendarToString(endTimeCalendar, YYYYMMDD);
        setNodeValue(nd, END_DATE, value);
      }
    }

    public void focusGained(java.awt.event.FocusEvent event)
    {
      Object object = event.getSource();
      Log.debug(10, object.toString());

      if (object instanceof JTextArea) {
      }
    }
  }

}
