/**
 *  '$RCSfile: AccessPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-01-09 05:51:54 $'
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

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AccessPage extends AbstractWizardPage {

  private final String pageID     = DataPackageWizardInterface.ACCESS_PAGE;
  private final String nextPageID = "";
  private final String pageNumber = "";
  private final String title      = "Access Page";
  private final String subtitle   = "";

  private final String EMPTY_STRING = "";
  private JPanel middlePanel;
  private JTextField dnField;
  private JLabel dnLabel;
  private JLabel descLabel;
  private String userAccessType   = new String("Allow");
  private String userAccess       = new String("Read");

  private final String[] accessTypeText = new String[] {
    "Allow",
    "Deny"
  };

  private final String[] accessText = new String[] {
    "Read",
    "Write",
    "All"
  };

  public boolean accessIsAllow = true;
  private final String xPathRoot  = "/eml:eml/dataset/access";

  public AccessPage() {
          init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    middlePanel = new JPanel();
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc = WidgetFactory.makeHTMLLabel(
                      "<font size=\"4\"><b>Define Access:</b></font>", 1);
    middlePanel.add(desc);

    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    JPanel dnPanel = WidgetFactory.makePanel(1);
    dnLabel = WidgetFactory.makeLabel("Enter User DN:", false);
    dnPanel.add(dnLabel);
    dnField = WidgetFactory.makeOneLineTextField();
    dnPanel.add(dnField);
    dnPanel.setBorder(new javax.swing.border.EmptyBorder(0,WizardSettings.PADDING,0,
        WizardSettings.PADDING));
    middlePanel.add(WidgetFactory.makeHalfSpacer());
    middlePanel.add(dnPanel);

    ActionListener accessTypeListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Log.debug(45, "got radiobutton command: "+e.getActionCommand());

        if (e.getActionCommand().equals(accessTypeText[0])) {
          userAccessType = "Allow";
          accessIsAllow = true;
        } else if (e.getActionCommand().equals(accessTypeText[1])) {
          userAccessType = "Deny";
          accessIsAllow = false;
        }
      }
    };

    JPanel typeRadioOuterPanel = WidgetFactory.makePanel(2);
    JPanel typeRadioPanel = WidgetFactory.makeRadioPanel(accessTypeText, 0, accessTypeListener);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    typeRadioOuterPanel.add(typeRadioPanel);
    descLabel = WidgetFactory.makeHTMLLabel(
        "<p><b>Choose access type:</b> Choose to allow or deny the user the "
        +"below defined permission.</p>", 1);
    middlePanel.add(descLabel);
    middlePanel.add(typeRadioOuterPanel);

    ActionListener accessListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Log.debug(45, "got radiobutton command: "+e.getActionCommand());

        if (e.getActionCommand().equals(accessText[0])) {
          userAccess = "Read";
        } else if (e.getActionCommand().equals(accessText[1])) {
          userAccess = "Write";
        } else if (e.getActionCommand().equals(accessText[2])) {
          userAccess = "All";
        }
      }
    };

    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    JPanel accessRadioOuterPanel = WidgetFactory.makePanel(3);
    JPanel accessRadioPanel = WidgetFactory.makeRadioPanel(accessText, 0, accessListener);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    accessRadioOuterPanel.add(accessRadioPanel);
    descLabel = WidgetFactory.makeHTMLLabel(
      "<p><b>Choose access:</b></p>", 1);
    middlePanel.add(descLabel);
    middlePanel.add(accessRadioOuterPanel);

    middlePanel.setBorder(new javax.swing.border.EmptyBorder(0,4*WizardSettings.PADDING,
        37*WizardSettings.PADDING,8*WizardSettings.PADDING));

    this.add(middlePanel);
  }


  /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {
    if(dnField.getText().trim().equals(EMPTY_STRING)){
      WidgetFactory.hiliteComponent(dnLabel);
      return false;
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
