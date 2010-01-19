/**
 *  '$RCSfile: IOUtil.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-12-16 22:13:22 $'
 * '$Revision: 1.5 $'
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
package edu.ucsb.nceas.morpho.util;

/**
 * This class represents a series of constants being used in incomplete eml documents
 * @author tao
 *
 */
public class IncompleteDocSettings 
{
   
   private final static String LESSTHAN  = "<";
   private final static String GREATERTHAN = ">";
   public final static String SLASH = "/";
   public final static String INCOMPLETE = "incomplete";
   public final static String INCOMPLETEOPENINGTAG = LESSTHAN+INCOMPLETE+GREATERTHAN;
   public final static String INCOMPLETECLOSINGTAG = LESSTHAN+SLASH+INCOMPLETE+GREATERTHAN;
   public final static String PACKAGEWIZARD = "packageWizard";
   public final static String ENTITYWIZARD = "enityWizard";
   public final static String CODEDEFINITIONWIZARD = "codeDefinitionWizard";
   public final static String CODEDEFINITIONWIZARDOPENINGTAG = LESSTHAN+CODEDEFINITIONWIZARD+GREATERTHAN;
   public final static String CODEDEFINITIONWIZARDCLOSINGTAG = LESSTHAN+SLASH+CODEDEFINITIONWIZARD+GREATERTHAN;
   public final static String EDITINGATTRIBUTE = "editingAttribute";
   public final static String EDITINGATTRIBUTEOPENINGTAG = LESSTHAN+EDITINGATTRIBUTE+GREATERTHAN;
   public final static String EDITINGATTRIBUTECLOSINGNGTAG = LESSTHAN+SLASH+EDITINGATTRIBUTE+GREATERTHAN;
   public final static String EDITINGENTITYINDEX = "editingEntityIndex";
   public final static String EDITINGENTITYINDEXOPENINGTAG = LESSTHAN+EDITINGENTITYINDEX+GREATERTHAN;
   public final static String EDITINGENTITYINDEXCLOSINGTAG = LESSTHAN+SLASH+EDITINGENTITYINDEX+GREATERTHAN;
   public final static String EDITINATTRIBUTEINDEX = "editingAttributeIndex";
   public final static String EDITINGATTRIBUTEINDEXOPENINGTAG = LESSTHAN+EDITINATTRIBUTEINDEX+GREATERTHAN;
   public final static String EDITINGATTRIBUTEINDEXCLOSINGTAG = LESSTHAN+SLASH+EDITINATTRIBUTEINDEX+GREATERTHAN;
   public final static String EDITINATTRIBUTEMAP = "editingAttributeMap";
   public final static String EDITINGATTRIBUTEMAPOPENINGTAG = LESSTHAN+EDITINATTRIBUTEMAP+GREATERTHAN;
   public final static String EDITINGATTRIBUTEMAPCLOSINGTAG = LESSTHAN+SLASH+EDITINATTRIBUTEMAP+GREATERTHAN;
   public final static String EDITINATTRIBUTEINSERTION = "editingAttributeInsertionBeforeSelection";
   public final static String EDITINGATTRIBUTEINSERTIONOPENINGTAG = LESSTHAN+EDITINATTRIBUTEINSERTION+GREATERTHAN;
   public final static String EDITINGATTRIBUTEINSERTIONCLOSINGTAG = LESSTHAN+SLASH+EDITINATTRIBUTEINSERTION+GREATERTHAN;
   
   //public final static String TEXTIMPORTWIZARD = "textImportWizard";
   public final static String PACKAGEWIZARDOPENINGTAG = LESSTHAN+PACKAGEWIZARD+GREATERTHAN;
   public final static String PACKAGEWIZARDCLOSINGTAG = LESSTHAN+SLASH+PACKAGEWIZARD+GREATERTHAN;
   public final static String TRUE = "true";
   public final static String EMLPATH = "/eml:eml/";
   public final static String EML = "eml:eml";
   public final static String EMLCLOSINGTAG = "</eml:eml>";
   public final static String ADDITIONALMETADATA = "additionalMetadata";
   public final static String ADDITIONALMETADATAOPENINGTAG = LESSTHAN+ADDITIONALMETADATA+GREATERTHAN;
   public final static String ADDITIONALMETADATACLOSINGTAG = LESSTHAN+SLASH+ADDITIONALMETADATA+GREATERTHAN;
   public final static String METADATA= "metadata";
   public final static String METADATAOPENINGTAG = LESSTHAN+METADATA+GREATERTHAN;
   public final static String METADATACLOSINGTAG = LESSTHAN+SLASH+METADATA+GREATERTHAN;
   public final static String CLASS = "class";
   public final static String CLASSOPENINGTAG = LESSTHAN+CLASS+GREATERTHAN;
   public final static String CLASSCLOSINGTAG =  LESSTHAN+SLASH+CLASS+GREATERTHAN;
   public final static String NAME = "name";
   public final static String NAMEOPENINGTAG = LESSTHAN+NAME+GREATERTHAN;
   public final static String NAMECLOSINGTAG =  LESSTHAN+SLASH+NAME+GREATERTHAN;
   public final static String PARAMETER = "parameter";
   public final static String CLASSPARAMETEROPENINGTAG = LESSTHAN+PARAMETER+GREATERTHAN;
   public final static String CLASSPARAMETERCLOSINGTAG =  LESSTHAN+SLASH+PARAMETER+GREATERTHAN;
   public final static String INDEX = "index";
   public final static String ENTITYWIZARDOPENINGTAG = LESSTHAN+ENTITYWIZARD+GREATERTHAN;
   public final static String ENTITYWIZARDCLOSINGTAG = LESSTHAN+SLASH+ENTITYWIZARD+GREATERTHAN;
   public final static String INDEXOPENINGTAG = LESSTHAN+INDEX+GREATERTHAN;
   public final static String INDEXCLOSINGTAG = LESSTHAN+SLASH+INDEX+GREATERTHAN;
   public  static final String INCOMPLETE_PACKAGE_WIZARD = "incomplete-pakcage-wizard";
   public  static final String INCOMPLETE_ENTITY_WIZARD  = "incomplete-entity-wizard";
   public  static final String INCOMPLETE_CODE_DEFINITION_WIZARD  = "incomplete-code-definition-wizard";
   public  static final String VARIABLE = "variable";
   public  static final String VARIABLEOPENINGTAG = LESSTHAN+VARIABLE+GREATERTHAN;
   public  static final String VARIABLECLOSINGTAG  = LESSTHAN+SLASH+VARIABLE+GREATERTHAN;
   public  static final String KEY = "key";
   public  static final String KEYOPENINGTAG = LESSTHAN+KEY+GREATERTHAN;
   public  static final String KEYCLOSINGTAG  = LESSTHAN+SLASH+KEY+GREATERTHAN;
   public  static final String VALUE = "value";
   public  static final String VALUEOPENINGTAG = LESSTHAN+VALUE+GREATERTHAN;
   public  static final String VALUECLOSINGTAG = LESSTHAN+SLASH+VALUE+GREATERTHAN;
   public  static final String ADDITIONALINFO = "additionInfo";
   public  static final String ADDITIONALINFOOPENINGTAG = LESSTHAN+ADDITIONALINFO+GREATERTHAN;
   public  static final String ADDITIONALINFOCLOSINGTAG = LESSTHAN+SLASH+ADDITIONALINFO+GREATERTHAN;
   public  static final String REMOVEDIMPORTATTRIBUTE = "removedImortAttribute";
   public  static final String REMOVEDIMPORTATTRIBUTEOPENINGTAG = LESSTHAN+REMOVEDIMPORTATTRIBUTE+GREATERTHAN;
   public  static final String REMOVEDIMPORTATTRIBUTECLOSINGTAG = LESSTHAN+SLASH+REMOVEDIMPORTATTRIBUTE+GREATERTHAN;
   public  static final String TRACINGCHANGE = "tracingChange";
   public  static final String TRACINGCHANGEOPENINGTAG = LESSTHAN+TRACINGCHANGE+GREATERTHAN;
   public  static final String TRACINGCHANGECLOSINGTAG = LESSTHAN+SLASH+TRACINGCHANGE+GREATERTHAN;
   public  static final String TRACINGCHANGEPATH = EMLPATH+ADDITIONALMETADATA+SLASH+METADATA+SLASH+
                                       PACKAGEWIZARD+SLASH+TRACINGCHANGE+" | "+ 
                                       EMLPATH+ADDITIONALMETADATA+SLASH+METADATA+SLASH+ENTITYWIZARD+SLASH+TRACINGCHANGE+" | "+
                                       EMLPATH+ADDITIONALMETADATA+SLASH+METADATA+SLASH+CODEDEFINITIONWIZARD+SLASH+TRACINGCHANGE;
}

