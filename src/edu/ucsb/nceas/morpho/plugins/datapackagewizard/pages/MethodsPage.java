/**
 *  '$RCSfile: MethodsPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-03-25 01:31:30 $'
 * '$Revision: 1.7 $'
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

import java.util.ArrayList;
import java.util.List;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import java.util.StringTokenizer;

public class MethodsPage
    extends AbstractUIPage {

  private final String pageID = DataPackageWizardInterface.METHODS_PAGE;
  private final String nextPageID = "";
  private final String pageNumber = "";
  private final String title = "Methods Page";
  private final String subtitle = "";

  private final String EMPTY_STRING = "";
  private JPanel middlePanel;
  private JLabel titleLabel;
  private JLabel descLabel;
  private JTextArea descField;
  private JLabel instLabel;
  private JTextArea instField;
  private JTextField titleField;
  private final String xPathRoot =
      "/eml:eml/dataset/methods/methodStep/description/section";

  public MethodsPage() {
    init();
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
        "<font size=\"4\"><b>Enter Method Step Information:</b></font>", 1);
    middlePanel.add(desc);

    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    JLabel titleDesc = WidgetFactory.makeHTMLLabel(
        "<b>Enter title</b> ", 1);
    middlePanel.add(titleDesc);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    JPanel titlePanel = WidgetFactory.makePanel(1);

    titleLabel = WidgetFactory.makeLabel(" Title", false);
    titlePanel.add(titleLabel);

    titleField = WidgetFactory.makeOneLineTextField();
    titlePanel.add(titleField);

    titlePanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
        WizardSettings.PADDING));

    middlePanel.add(titlePanel);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    JLabel descTitle = WidgetFactory.makeHTMLLabel(
        "<b>Enter description</b> ", 1);
    middlePanel.add(descTitle);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    JPanel descPanel = WidgetFactory.makePanel(25);
    descLabel = WidgetFactory.makeLabel("Description:", true);
    descPanel.add(descLabel);

    descField = WidgetFactory.makeTextArea("", 7, true);
    JScrollPane jSampleScrl = new JScrollPane(descField);
    descPanel.add(jSampleScrl);

    descPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
        WizardSettings.PADDING));
    middlePanel.add(descPanel);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    JLabel instrumentationTitle = WidgetFactory.makeHTMLLabel(
        "<b>Enter Instrumentation Details</b> ", 1);
    middlePanel.add(instrumentationTitle);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    JPanel instPanel = WidgetFactory.makePanel(25);
    instLabel = WidgetFactory.makeLabel("Instrumentation:", false);
    instPanel.add(instLabel);

    instField = WidgetFactory.makeTextArea("", 7, true);
    JScrollPane jInstruPane = new JScrollPane(instField);
    instPanel.add(jInstruPane);

    instPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0,
        WizardSettings.PADDING));
    middlePanel.add(instPanel);

    middlePanel.setBorder(new javax.swing.border.EmptyBorder(0,
        4 * WizardSettings.PADDING,
        7 * WizardSettings.PADDING, 8 * WizardSettings.PADDING));

    this.add(middlePanel, BorderLayout.CENTER);
  }

  /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    String desc = descField.getText().trim();

    if (desc.compareTo(EMPTY_STRING) == 0) {
      WidgetFactory.hiliteComponent(descLabel);
      return false;
    }
    WidgetFactory.unhiliteComponent(descLabel);
    return true;
  }

  /**
   *  @return a List contaiing 2 String elements - one for each column of the
   *  2-col list in which this surrogate is displayed
   *
   */
  public List getSurrogate() {
    List surrogate = new ArrayList();

    String title = titleField.getText().trim();

    if (title == null) {
      title = EMPTY_STRING;
    }
    surrogate.add(title);

    String desc = descField.getText().trim();

    if (desc == null) {
      desc = EMPTY_STRING;
    }
    surrogate.add(desc);

    String inst = instField.getText().trim();

    if (inst == null) {
      inst = EMPTY_STRING;
    }
    surrogate.add(inst);

    return surrogate;
  }

  /**
   *  gets the Map object that contains all the key/value paired
   *
   *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
   *            appended when making name/value pairs.  For example, in the
   *            xpath: /eml:eml/dataset/methods/methodStep/description/setion,
   *            the root would be
   *            /eml:eml/dataset/methods/methodStep/description/setion
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

    String title = titleField.getText().trim();

    if (title!=null && !title.equals(EMPTY_STRING)) {
      returnMap.put(xPathRoot + "/description/section/title", title);
    }

    String desc = descField.getText().trim();
    if (desc != null && !desc.equals(EMPTY_STRING)) {

      StringTokenizer st = new StringTokenizer(desc, "\n");
      int count = 0;
      while(st.hasMoreTokens()){
        count++;
        returnMap.put(xPathRoot + "/description/section/para[" + count + "]", st.nextToken());
      }
    }

    String inst = instField.getText().trim();
    if (inst != null && !inst.equals(EMPTY_STRING)) {

      StringTokenizer st = new StringTokenizer(inst, "\n");
      int count = 0;
      while(st.hasMoreTokens()){
        count++;
        returnMap.put(xPathRoot + "/instrumentation[" + count + "]", st.nextToken());
      }
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

    public boolean setPageData(OrderedMap data, String xPathRoot) { return false; }
}
