/**
 *  '$RCSfile: Introduction.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2003-11-26 17:54:20 $'
 * '$Revision: 1.10 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

import java.util.Map;

import javax.swing.JLabel;

import java.awt.BorderLayout;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.utilities.OrderedMap;

public class Introduction extends AbstractWizardPage {

  public final String pageID     = DataPackageWizardInterface.INTRODUCTION;
  public final String nextPageID = DataPackageWizardInterface.PROJECT;
//////////////////////////////////////////////////////////

  public final String title      = "Welcome to the Data Package Wizard";
  public final String subtitle   = " ";


  public Introduction() {

    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    JLabel desc = WidgetFactory.makeHTMLLabel(
    "<p>If you do not have a basic understanding of metadat and "
    +"related concepts you should start by reading the <a>Ecological Metadata "
    +"Language(EML) Guide</a>. It provides background information on metadata "
    +"and contains metadata creation examples. this wizard creates a <i>Data "
    +"Package</i> that consists of metadata that describes your data and the "
    +"data itself. The wizard uses a subset of EML to describe your data. If "
    +"additional metadata are needed to adequately document your data use "
    +"<i>Morpho's EML Editor</i>. Before beginning you should have your data "
    +"(electronic or hardcopy format), and the following types of metadata "
    +"information in front of you: </p><br></br>"
    +"<li><b>General Information</b> <br></br>Information such as title, abstract, "
    +"keywords, people and/or organizations responsible for the data and "
    +"data usage rights.<br></br></li>"
    +"<li><b>Geographic and Temporal Information</b> <br></br>Data location descriptions, "
    +"geographic coordinates and temporal information that describes when the data were "
    +"collected.<br></br></li>"
    +"<li><b>Taxonomic Information:</b> <br></br>Information such as species names and "
    +"taxonomic level.<br></br></li>"
    +"<li><b>Methods:</b> <br></br>A description of the steps taken to collect the data, "
    +"sampling designs, electronic and computer equipment used, etc. <br></br></li>"
    +"<li><b>Data Table Information</b> <br></br>The data table is the data file that "
    +"contains your data. The wizard only allows you to document one table initially. "
    +"More tables can be added later. You should have information regarding the "
    +"structure of your data file, e.g. measurement units, definitions for codes "
    +"and abbreviations, values used for \"No Data\", etc. This wizard is not "
    +"meant for RDBMS files.<br></br></li></ul>", 10);

    this.setLayout(new BorderLayout());
    this.add(desc, BorderLayout.CENTER);
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
  public boolean onAdvanceAction() { return true; }


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

  public void setPageData(OrderedMap data) { }
}