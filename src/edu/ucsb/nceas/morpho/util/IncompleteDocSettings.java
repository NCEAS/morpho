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
   private final static String SLASH = "/";
   public final static String INCOMPLETE = "incomplete";
   public final static String INCOMPLETEOPENINGTAG = LESSTHAN+INCOMPLETE+GREATERTHAN;
   public final static String INCOMPLETECLOSINGTAG = LESSTHAN+SLASH+INCOMPLETE+GREATERTHAN;
   public final static String PACKAGEWIZARD = "packageWizard";
   public final static String TEXTIMPORTWIZARD = "textImportWizard";
   public final static String PACKAGEWIZARDOPENINGTAG = LESSTHAN+PACKAGEWIZARD+GREATERTHAN;
   public final static String PACKAGEWIZARDCLOSINGTAG = LESSTHAN+SLASH+PACKAGEWIZARD+GREATERTHAN;
   public final static String TRUE = "true";
   public final static String EMLCLOSINGTAG = "</eml:eml>";
   public final static String ADDITIONALMETADATA = "additionalMetadata";
   public final static String ADDITIONALMETADATAOPENINGTAG = LESSTHAN+ADDITIONALMETADATA+GREATERTHAN;
   public final static String ADDITIONALMETADATACLOSINGTAG = LESSTHAN+SLASH+ADDITIONALMETADATA+GREATERTHAN;
   public final static String METADATA= "metadata";
   public final static String METADATAOPENINGTAG = LESSTHAN+METADATA+GREATERTHAN;
   public final static String METADATACLOSINGTAG = LESSTHAN+SLASH+METADATA+GREATERTHAN;
   
}
