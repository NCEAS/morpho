/**
 *  '$RCSfile: Introduction.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-13 03:57:28 $'
 * '$Revision: 1.29 $'
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

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.HelpMetadataIntroCommand;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.HyperlinkButton;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

public class Introduction extends AbstractUIPage {

  public final String pageID     = DataPackageWizardInterface.INTRODUCTION;
  public final String pageNumber = "1";

//////////////////////////////////////////////////////////
  private JComponent metadataIntroLink;
  public final String title      = /*"Welcome to the New Data Package Wizard"*/ 
	  								Language.getInstance().getMessage("Introduction.title");
  public final String subtitle   = " ";


  public Introduction() {
	nextPageID = DataPackageWizardInterface.GENERAL;
    init();

    }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.add(WidgetFactory.makeHalfSpacer());

    JLabel desc1 = WidgetFactory.makeHTMLLabel(
    /*"<p>This wizard creates a"
    +" <i>Data Package</i>, consisting of the structured documentation that "
    +"describes your data (i.e., metadata), and the "
    +"data themselves. <br></br></p>"
    */
    "<p>"
    +Language.getInstance().getMessage("Introduction.desc1_1")
    +"<br></br></p>"
    /*
    +"If you wish to improve your understanding of metadata "
    +"and related concepts, you should start by reading "
    */ 
    +Language.getInstance().getMessage("Introduction.desc1_2")
    ,3);

    JLabel desc2 = WidgetFactory.makeHTMLLabel(
    /*
    "which provides background information and examples of metadata. "
    +"The wizard uses a subset of EML to describe your data. If "
    +"additional documentation is needed to adequately document your data, use "
    +"<i>Morpho Editor</i> (after you finish this wizard, choose \"Add/"
    +"Edit Documentation\" from the \"Documentation\" menu on the main Morpho screen).<br></br></p>"
    */
    Language.getInstance().getMessage("Introduction.desc2_1")
    +"<br></br></p>"

    /*
    +"<p>Before beginning you should have your data "
    +"(electronic or hardcopy format) available. You can provide the following "
    +"types of "
    +"information using this wizard: </p>"
    */
    +"<br><p>"
    +Language.getInstance().getMessage("Introduction.desc2_2")
    +"</p>"
    /*
    +"<li><b>Title and abstract</b><br></br></li>"
    +"<li><b>Keywords</b><br></br></li>"
    +"<li><b>People and Organizations</b><br></br></li>"
    +"<li><b>Usage Rights</b><br></br></li>"
    +"<li><b>Research Project Information</b><br></br></li>"
    +"<li><b>Coverage Details</b><br></br></li>"
    +"<li><b>Methods and Sampling</b><br></br></li>"
    +"<li><b>Access Information</b></li></ul>"
    +"<p><b>Note:</b> Required information includes the title and personnel "
    +"information for your data set.  The rest of the information collected here "
    +"is optional, however it is highly recommended that you fill in as much as "
    +"possible.</p>"
	*/

    +"<li><b>"+ Language.getInstance().getMessage("TitleAndAbstract") +"</b><br></br></li>"
    +"<li><b>"+ Language.getInstance().getMessage("Keywords") +"</b><br></br></li>"
    +"<li><b>"+ Language.getInstance().getMessage("PeopleAndOrganizations") +"</b><br></br></li>"
    +"<li><b>"+ Language.getInstance().getMessage("UsageRights") +"</b><br></br></li>"
    +"<li><b>"+ Language.getInstance().getMessage("ResearchProjectInformation") +"</b><br></br></li>"
    +"<li><b>"+ Language.getInstance().getMessage("CoverageDetails") +"</b><br></br></li>"
    +"<li><b>"+ Language.getInstance().getMessage("MethodsAndSampling") +"</b><br></br></li>"
    +"<li><b>"+ Language.getInstance().getMessage("AccessInformation") +"</b></li></ul>"

    +"<p><b>"+ Language.getInstance().getMessage("Note")+":</b>"
    +Language.getInstance().getMessage("Introduction.desc2_3")
    +"</p>"
    , 19);

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.add(desc1);
    this.add(getMetadataIntroLink());
    this.add(desc2);

    this.add(Box.createVerticalGlue());

    desc1.setAlignmentX(-1f);
    desc2.setAlignmentX(-1f);
  }

  private JComponent getMetadataIntroLink() {

    if (metadataIntroLink==null) {

      GUIAction newDataTableAction
        = new GUIAction(/*"An Introduction to Ecological Metadata Language (EML) "*/ Language.getInstance().getMessage("EML_Introduction"),
                        null,
                        new Command() {

        public void execute(ActionEvent ae) {

            (new HelpMetadataIntroCommand()).execute(ae);

        }
      });

      metadataIntroLink = new HyperlinkButton(newDataTableAction);
      final Dimension DIM = new Dimension(400,15);
      metadataIntroLink.setMinimumSize(DIM);
      metadataIntroLink.setPreferredSize(DIM);
      metadataIntroLink.setMaximumSize(DIM);
    }
    return metadataIntroLink;
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
   * gets the Map object that contains all the key/value paired settings for
   * this particular wizard page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular wizard page
   */
  public OrderedMap getPageData(String rootXPath) {

    throw new UnsupportedOperationException(
      "getPageData(String rootXPath) Method Not Implemented");
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

    public boolean setPageData(OrderedMap data, String xPathRoot) { return false; }


}
