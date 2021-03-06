/**
 *  '$RCSfile: DataPackageWizardInterface.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-02 21:48:38 $'
 * '$Revision: 1.28 $'
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

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.IncompleteDocumentLoader;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.TextImportAttribute;
import edu.ucsb.nceas.morpho.plugins.IncompleteDocInfo;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

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

  public static final String REPLICATION_POLICY = "REPLICATION_POLICY";
  
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
	public static final String PARTY_CITATION_AUTHOR = "PARTY_CITATION_AUTHOR";

  public static final String GEOGRAPHIC         = "GEOGRAPHIC";
  public static final String GEOGRAPHIC_PAGE    = "GEOGRAPHIC_PAGE";
  public static final String TAXONOMIC          = "TAXONOMIC";
  public static final String TEMPORAL           = "TEMPORAL";
  public static final String TEMPORAL_PAGE      = "TEMPORAL_PAGE";
  public static final String USAGE_RIGHTS       = "USAGE_RIGHTS";
  public static final String DATA_LOCATION      = "DATA_LOCATION";
  public static final String REPLACE_DATA_LOCATION      = "REPLACE_DATA_LOCATION";
  public static final String OTHER_ENTITY             = "OTHER_ENTITY";
  //public static final String TEXT_IMPORT_WIZARD = "TEXT_IMPORT_WIZARD";
  public static final String DATA_FORMAT        = "DATA_FORMAT";
  public static final String ENTITY             = "ENTITY";
  public static final String ATTRIBUTE_PAGE     = "ATTRIBUTE_PAGE";
  public static final String CODE_IMPORT_PAGE   = "CODE_IMPORT_PAGE";
  public static final String METHODS            = "METHODS";
  public static final String METHODS_PAGE       = "METHODS_PAGE";
  public static final String ACCESS             = "ACCESS";
  public static final String ENTITY_ACCESS      = "ENTITY_ACCESS";
  public static final String ACCESS_PAGE        = "ACCESS_PAGE";
  public static final String SUMMARY            = "SUMMARY";
  public static final String CORRECTION_SUMMARY    ="CORRECTION_SUMMARY";
  public static final String CODE_IMPORT_SUMMARY= "CODE_IMPORT_SUMMARY";
  public static final String CODE_DEFINITION   	= "CODE_DEFINITION";
  public static final String CITATION_PAGE	   	= "CITATION";
  public static final String CUSTOM_UNIT_PAGE	  = "CUSTOM_UNIT_PAGE";
  public static final String GENERIC_VOCABULARY	  = "GENERIC_VOCABULARY";
  public static final String NBII_THESAURUS_LOOKUP	  = "NBII_THESAURUS_LOOKUP";
  public static final String CORRECTION_DATA_LOCATION = "CORRECTION_DATA_LOCATION";
  public static final String TEXT_IMPORT_ENTITY = "TEXT_IMPORT_ENTITY";
  public static final String TEXT_IMPORT_DELIMITERS = "TEXT_IMPORT_DELIMITERS";
  public static final String TEXT_IMPORT_ATTRIBUTE = "TEXT_IMPORT_ATTRIBUTE_";
  public static final String LOAD_INCOMPLETED_ATTRIBUTE = "LOAD_INCOMPLETED_ATTRIBUTE";
  public static final String TEXT_IMPORT_FIRST_ATTRIBUTE = TEXT_IMPORT_ATTRIBUTE+TextImportAttribute.FIRSTINDEX;
  
  public static final String NEWPACKAGEWIZARDFRAMETITLE = Language.getInstance().getMessage("NewDataPackageWizard");
  public static final String NEWTABLEEWIZARDFRAMETITLE = Language.getInstance().getMessage("NewDataTableWizard");
  public static final String NEWCODEDEFINITIONWIZARDFRAMETITLE = Language.getInstance().getMessage("ImportCodesAndDefinitions");


  /**
   *  method to start the Package wizard
   *
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Wizard has finished
   */
  public void startPackageWizard(DataPackageWizardListener listener);


  /**
   *  method to start the Entity wizard
   
   *  @param originatingMorphoFrame the frame which started the wizard.
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Wizard has finished
   *  @param entityIndex the index of the new entity in this package
   */ 
  public void startEntityWizard(MorphoFrame originatingMorphoFrame, DataPackageWizardListener listener, int entityIndex);


  /**
   *  method to start the Code Definitions Import wizard
   *
   *  @param originatingMorphoFrame the frame which started the wizard.
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Wizard has finished
   *  @param entityIndex the index of the entity which wizard will use (next entity index)
   *  @param attributeMap  the ordered map for the editing attribute
   *  @param editingEntityIndex  the index of the entity which is editing
   *  @param editingAttributeIndex the index of the attribute which is editing
   *  @param beforeFlag if the new column is before the select column. If it is null, it means editing rather than inserting
   */
  public void startCodeDefImportWizard(MorphoFrame originatingMorphoFrame, DataPackageWizardListener listener, int entityIndex,  Boolean beforeFlag, int editingEntityIndex, int editingAttributeIndex);


  /**
   *  returns the WizardPage with the corresponding pageID provided
   *
   *  @param pageID the String pageID for the WizardPage to be returned
   *
   *  @return  the corresponding WizardPage with this ID
   */
  public AbstractUIPage getPage(String pageID);
  
  /**
   * 
   * start a correction invalid eml document wizard. This wizard always be used to
   * correct in valid eml document which was transformed from old eml version.
   *
   * @param dataPackage  the datapackage will be corrected
   * @param errorPathes    the list of path which has valid value
   * @param frame            the old frame which need be disposed after correction is done
   * @param listener          the listener will handle some another action  after the wizard is done, e.g .AddAccessCommand
   */
  public void startCorrectionWizard(MorphoDataPackage mdp, Vector errorPathes, MorphoFrame frame, DataPackageWizardListener listener);
  
  
  /**
   * 
   * start a correction invalid eml document wizard. This wizard always be used to
   * correct in valid eml document which was transformed from old eml version.
   *
   * @param dataPackage  the datapackage will be corrected
   * @param errorPathes    the list of path which has valid value
   * @param frame            the old frame which need be disposed after correction is done
   * @param listener          the listener will handle some another action  after the wizard is done, e.g .AddAccessCommand
   * @param isSaveProcess    if the correction happens during the save process.  
   */
  public void startCorrectionWizard(MorphoDataPackage mdp, Vector errorPathes, MorphoFrame frame, DataPackageWizardListener listener, boolean isSaveProcess);
  
  /**
   * Load (open) an incomplete document into new package wizard /text import wizard
   * @param dataPackage the incomplete data package
   */
  public void loadIncompleteDocument(MorphoDataPackage mdp);
}
