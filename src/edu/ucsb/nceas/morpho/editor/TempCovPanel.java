/**
 *       Name: PartyPanel.java
 *    Purpose: Example dynamic editor class for XMLPanel
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-01-21 22:20:31 $'
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
package edu.ucsb.nceas.morpho.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.toedter.calendar.JCalendar;

import java.text.DateFormat;

import java.util.Calendar;
import java.util.Enumeration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;


/**
 * TempCovPanel is an example of a special panel editor for
 * use with the DocFrame class.
 *
 * @author sgarg
 */

public class TempCovPanel extends JPanel
{

  private static final String EMPTY_STRING = "";
  private static final String CALENDAR_DATE = "calendarDate";
  private static final String SINGLE_DATE = "singleDateTime";
  private static final String BEGIN_DATE = "beginDate";
  private static final String END_DATE = "endDate";
  private static final int    YYYYMMDD = 8;
  private static final int    ALL = 4;
  private static final int    MONTH_YEAR = 2;
  private static final int    YEAR_ONLY = 1;

  private static final Dimension PANEL_DIMS = new Dimension(325,220);

  private JPanel singlePanel;
  private JPanel beginPanel;
  private JPanel endPanel;

  private JTextField singleTimeTF;
  private JTextField beginTimeTF;
  private JTextField endTimeTF;

  private JCalendar singleTimeCalendar;
  private JCalendar beginTimeCalendar;
  private JCalendar endTimeCalendar;

  private JScrollPane scrollPane;

  private DefaultMutableTreeNode nd;

  private final String[] timeText = new String[] {
    "Enter Year 0nly",
    "Enter Month and Year",
    "Enter Day, Month and Year"
  };

  private final String[] Months = new String[] {
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  };



  /**
   * Constructor used to display the Temporal Coverage Panel.
   *
   * @param node DefaultMutableTreeNode
   */
  public TempCovPanel(DefaultMutableTreeNode node) {
    nd = node;
    init(this);
  }


  /**
   * Function assigns appropriate panel to the scrollpane based on
   * whether the node is a singlePoint or range of Dates.
   *
   * @param panel JPanel
   */
  private void init(JPanel panel) {
    panel.setLayout(new BorderLayout());
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    panel.setMaximumSize(new Dimension(500,750));
    panel.setVisible(true);

    scrollPane = new javax.swing.JScrollPane();
    panel.add(BorderLayout.CENTER,scrollPane);

    if (isSingle(nd)) {      // check whether single point or range of dates
      scrollPane.getViewport().add( getSinglePointPanel() );
    } else {
      scrollPane.getViewport().add( getRangeTimePanel() );
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
     singleTimeTF.setBackground(Color.WHITE);

     singleTimeCalendar = new JCalendar();
     singleTimeCalendar.setVisible(true);

     if (isSingle(nd)) {
       setCalendarValue(nd, SINGLE_DATE, singleTimeCalendar);
     }

     singlePanel = getDateTimePanel("Enter date:", singleTimeTF,
                                    singleTimeCalendar, SINGLE_DATE);

     panel.add(singlePanel);
     panel.setBorder(new javax.swing.border.EmptyBorder(0,
         8*WizardSettings.PADDING,0,8*WizardSettings.PADDING));

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

     beginTimeTF = new JTextField();
     beginTimeTF.setEditable(false);
     beginTimeTF.setBackground(Color.WHITE);

     beginTimeCalendar = new JCalendar();
     beginTimeCalendar.setVisible(true);

     if (!isSingle(nd)) {
       setCalendarValue(nd, BEGIN_DATE, beginTimeCalendar);
     }

     beginPanel = getDateTimePanel("Enter beginning date:", beginTimeTF,
                                   beginTimeCalendar, BEGIN_DATE);
     panel.add(beginPanel);

     endTimeTF = new JTextField();
     endTimeTF.setEditable(false);
     endTimeTF.setBackground(Color.WHITE);

     endTimeCalendar = new JCalendar();
     endTimeCalendar.setVisible(true);

     if (!isSingle(nd)) {
       setCalendarValue(nd, END_DATE, endTimeCalendar);
     }

     endPanel = getDateTimePanel("Enter ending date:", endTimeTF,
                                 endTimeCalendar, END_DATE);
     panel.add(endPanel);

     panel.setBorder(new javax.swing.border.EmptyBorder(0,
         8*WizardSettings.PADDING,0,8*WizardSettings.PADDING));
     return panel;
   }


    /**
     *  Function returns a JPanel for selecting date
     *
     *  @return JPanel to select a date.
     */
    public JPanel getDateTimePanel(String panelHeading,
                                   JTextField timeTextField,
                                   JCalendar timeCalendar, final String DATE) {

      JPanel outerPanel = new JPanel();
      outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));

      WidgetFactory.addTitledBorder(outerPanel, panelHeading);

      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      panel.setBorder(new javax.swing.border.EmptyBorder(
          2*WizardSettings.PADDING,WizardSettings.PADDING,0,
          WizardSettings.PADDING));

      timeTextField.setText(calendarToString(timeCalendar, ALL));
      panel.add(timeTextField, BorderLayout.NORTH);

      final JCalendar finalTimeCalendar = timeCalendar;
      final JTextField finalTimeTextField = timeTextField;

      PropertyChangeListener propertyListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
          Log.debug(45, "got property change from JCalender bean: "+e.getPropertyName());

          if (e.getPropertyName().equals("calendar")) {
            // Set date in the text field on top of JCalender
            if(finalTimeCalendar.getDayChooser().isEnabled()){

              finalTimeTextField.setText(calendarToString(finalTimeCalendar, ALL));
            } else if(finalTimeCalendar.getMonthChooser().isEnabled()){

              finalTimeTextField.setText(calendarToString(finalTimeCalendar, MONTH_YEAR));
            } else {

              finalTimeTextField.setText(calendarToString(finalTimeCalendar, YEAR_ONLY));
            }

            // Set date in the editor
            String value = calendarToString(finalTimeCalendar, YYYYMMDD);
            setNodeValue(nd, DATE, value);
          }
        }
      };

      timeCalendar.addPropertyChangeListener(propertyListener);
      panel.add(timeCalendar, BorderLayout.CENTER);

      // When options change in typeRadioPanel, accordingly make changes in
      // timeCalendar and text field.
      ActionListener dayTypeListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Log.debug(45, "got radiobutton command: "+e.getActionCommand());

          if (e.getActionCommand().equals(timeText[0])) {
            finalTimeCalendar.getDayChooser().setEnabled(false);
            finalTimeCalendar.getMonthChooser().setEnabled(false);
            finalTimeCalendar.getYearChooser().setEnabled(true);

            finalTimeTextField.setText(calendarToString(finalTimeCalendar, YEAR_ONLY));
            setNodeValue(nd, DATE, calendarToString(finalTimeCalendar, YYYYMMDD));

          } else if (e.getActionCommand().equals(timeText[1])) {
            finalTimeCalendar.getDayChooser().setEnabled(false);
            finalTimeCalendar.getMonthChooser().setEnabled(true);
            finalTimeCalendar.getYearChooser().setEnabled(true);

            finalTimeTextField.setText(calendarToString(finalTimeCalendar, MONTH_YEAR));
            setNodeValue(nd, DATE, calendarToString(finalTimeCalendar, YYYYMMDD));

          } else if (e.getActionCommand().equals(timeText[2])) {
            finalTimeCalendar.getDayChooser().setEnabled(true);
            finalTimeCalendar.getMonthChooser().setEnabled(true);
            finalTimeCalendar.getYearChooser().setEnabled(true);

            finalTimeTextField.setText(calendarToString(finalTimeCalendar, ALL));
            setNodeValue(nd, DATE, calendarToString(finalTimeCalendar, YYYYMMDD));
          }
        }
      };

      JPanel typeRadioPanel = WidgetFactory.makeRadioPanel(timeText, 2, dayTypeListener);
      panel.add(typeRadioPanel, BorderLayout.SOUTH);

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
         return calendar.get(Calendar.YEAR) + EMPTY_STRING;
       }

       if(returnType == MONTH_YEAR){
         return Months[calendar.get(Calendar.MONTH)] + "," +
             calendar.get(Calendar.YEAR);
       }

       if(returnType == YYYYMMDD){
         String dateString = calendar.get(Calendar.YEAR) + "-";

         int month = calendar.get(Calendar.MONTH) + 1;
         int day = calendar.get(Calendar.DAY_OF_MONTH);

         if(c.getMonthChooser().isEnabled()){
           if (month < 10) {
             dateString = dateString + "0" + month + "-";

           } else {
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

           } else {
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
   *  Function finds out whether node has SINGLE_DATE or not.
   *
   *  @param node DefaultMutableTreeNode
   *  @return boolean
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


  /**
   *  Function sets value of JCalendar calendar based on the value that is
   *  set in the DefaultMutableTreeNode node.  String name is used to
   *  distinguish parent of the CALENDAR_DATE node.
   *
   *  @param node DefaultMutableTreeNode
   *  @param name String
   *  @param calendar JCalendar
   */
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

          calendar.getYearChooser().setYear(
                        Integer.valueOf(tokens.nextToken()).intValue() );
          calendar.getMonthChooser().setMonth(
                        Integer.valueOf(tokens.nextToken()).intValue() - 1 );
          calendar.getDayChooser().setDay(
                        Integer.valueOf(tokens.nextToken()).intValue() );
        }
      }
    }
  }


  /**
   *  Function sets value of CALENDAR_DATE in DefaultMutableTreeNode node
   *  based on the value that is passed in String value. String name is used to
   *  distinguish parent of the CALENDAR_DATE node.
   *
   *  @param node DefaultMutableTreeNode
   *  @param name String
   *  @param calendar JCalendar
   */
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
}
