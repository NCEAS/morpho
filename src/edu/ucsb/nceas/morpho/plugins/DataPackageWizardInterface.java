/**
 *  '$RCSfile: DataPackageWizardInterface.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-04-01 02:37:16 $'
 * '$Revision: 1.19 $'
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

package edu.ucsb.nceas.morpho.plugins;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JFrame;
/**
 *  Interface for Data Package Wizard Plugin
 */

public interface DataPackageWizardInterface {


  /**	Page IDs of the pages in the DataPackageWizard
    * 	We can request a particular page in the wizard through
    *	the getPage() method by passing the appropriate page ID.
  **/

 // NOTE TO SELF - these are strings rather than ints/shorts because they
 //are used as Map keys in WizardPageLibrary

  public static final String INTRODUCTION       = "INTRODUCTION";
  public static final String PROJECT            = "PROJECT";
  public static final String GENERAL            = "GENERAL";
  public static final String KEYWORDS           = "KEYWORDS";
  public static final String KEYWORDS_PAGE      = "KEYWORDS_PAGE";

  public static final String PARTY_INTRO        = "PARTY_INTRO";

  //used to denote listings pages that launch dialogs
  public static final String PARTY_CREATOR_PAGE    = "PARTY_CREATOR_PAGE";
  public static final String PARTY_CONTACT_PAGE    = "PARTY_CONTACT_PAGE";
  public static final String PARTY_ASSOCIATED_PAGE = "PARTY_ASSOCIATED_PAGE";

  //used to denote dialogs and roles
  public static final String PARTY_CREATOR      = "PARTY_CREATOR";
  public static final String PARTY_CONTACT      = "PARTY_CONTACT";
  public static final String PARTY_ASSOCIATED   = "PARTY_ASSOCIATED";
  public static final String PARTY_PERSONNEL    = "PARTY_PERSONNEL";

  public static final String GEOGRAPHIC         = "GEOGRAPHIC";
  public static final String GEOGRAPHIC_PAGE    = "GEOGRAPHIC_PAGE";
  public static final String TAXONOMIC          = "TAXONOMIC";
  public static final String TEMPORAL           = "TEMPORAL";
  public static final String TEMPORAL_PAGE      = "TEMPORAL_PAGE";
  public static final String USAGE_RIGHTS       = "USAGE_RIGHTS";
  public static final String DATA_LOCATION      = "DATA_LOCATION";
  public static final String TEXT_IMPORT_WIZARD = "TEXT_IMPORT_WIZARD";
  public static final String DATA_FORMAT        = "DATA_FORMAT";
  public static final String ENTITY             = "ENTITY";
  public static final String ATTRIBUTE_PAGE     = "ATTRIBUTE_PAGE";
  public static final String CODE_IMPORT_PAGE   = "CODE_IMPORT_PAGE";
  public static final String METHODS            = "METHODS";
  public static final String METHODS_PAGE       = "METHODS_PAGE";
  public static final String ACCESS             = "ACCESS";
  public static final String ACCESS_PAGE        = "ACCESS_PAGE";
  public static final String SUMMARY            = "SUMMARY";
  public static final String CODE_IMPORT_SUMMARY= "CODE_IMPORT_SUMMARY";
  public static final String CODE_DEFINITION   	= "CODE_DEFINITION";
  public static final String CITATION_PAGE	   	= "CITATION";


  /**
   *  method to start the Package wizard
   *
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Wizard has finished
   */
  public void startPackageWizard(DataPackageWizardListener listener);


  /**
   *  method to start the Entity wizard
   *
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Wizard has finished
   */
  public void startEntityWizard(DataPackageWizardListener listener);


  /**
   *  method to start the Code Definitions Import wizard
   *
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Wizard has finished
   */
  public void startCodeDefImportWizard(DataPackageWizardListener listener);


  /**
   *  returns the WizardPage with the corresponding pageID provided
   *
   *  @param pageID the String pageID for the WizardPage to be returned
   *
   *  @return  the corresponding WizardPage with this ID
   */
  public AbstractUIPage getPage(String pageID);
}
