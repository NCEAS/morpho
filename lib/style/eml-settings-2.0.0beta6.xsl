<?xml version="1.0"?>
<!--
  *  '$RCSfile: eml-settings-2.0.0beta6.xsl,v $'
  *      Authors: Matthew Brooke
  *    Copyright: 2000 Regents of the University of California and the
  *               National Center for Ecological Analysis and Synthesis
  *  For Details: http://www.nceas.ucsb.edu/
  *
  *   '$Author: brooke $'
  *     '$Date: 2002-12-19 01:00:15 $'
  * '$Revision: 1.9 $'
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
  *
  *****************************************************************************
  *
  * This is an XSLT (http://www.w3.org/TR/xslt) stylesheet that provides a 
  * single, central location for setting all installation-specific paths for 
  * XSLT stylesheets.  It is intended to be imported (using the 
  * <xsl:import href="..." /> element) into other XSLT stylesheets used in the
  * transformation of xml files that are valid with respect to the 
  * applicable dtd of the Ecological Metadata Language (EML).
  
  * Some of these paths incorporate values of the form: @token-name@; these are 
  * intended to allow an Ant (http://jakarta.apache.org/ant/index.html) build 
  * script to replace the tokens automatically with the correct values at build/
  * install time.  If Ant is not used, the tokens may simply be edited by hand 
  * to point to the correct resources.   
  * Note that the values given below may be overridden by passing parameters to 
  * the XSLT processor programatically, although the procedure for doing so is 
  * vendor-specific.  Note also that these parameter definitions will be overridden 
  * by any identical parameter names declared within xsl stylesheets that import 
  * this stylesheet.
  * 
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- 
    /**
    *   The filename of the default css stylesheet to be used
    *   (filename only - not the whole path, and no ".css" extension.  The 
    *   example below would look for a file named "default.css" in the same 
    *   directory as the stylesheets
    */
-->
    
  <xsl:param name="qformat">default</xsl:param>
  
<!-- 
    /**
    *   The filename of the default css stylesheet to be used
    *   for styling teh entity and attribute data in the right-hand metaview
    *   (filename only - not the whole path, and no ".css" extension.  The 
    *   example below would look for a file named "entity.css" in the same 
    *   directory as the stylesheets
    */
-->
    
  <xsl:param name="entitystyle">entity</xsl:param>
  
  
<!-- 
    /**
    *   the path of the directory where the XSL and CSS files reside - .  
    *   Typically in a jar file for Morpho. NOTE - this will be overridden by 
    *   Morpho at runtime, so don't need to fill in.  
    *   Just for reference, value will look like this:
    *
    *   EXAMPLE:
    *       <xsl:param name="stylePath">jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/morpho-config.jar!/style</xsl:param>  
    */
 -->

<xsl:param name="stylePath"></xsl:param>

  
<!-- 
    /**
    *   The base URI to be used for the href link to each document in a 
    *   "subject-relationaship-object" triple
    *
    *   EXAMPLE:
    *       <xsl:param name="tripleURI">
    *         <![CDATA[/brooke/servlet/metacat?action=read&qformat=knb&docid=]]>
    *       </xsl:param>
    *
    *   (Note in the above case the "qformat=knb" parameter in the url; a system 
    *   could pass this parameter to the XSLT engine to override the local 
    *   <xsl:param name="qformat"> tags defined earlier in this document.)  
    */
-->

    <xsl:param name="tripleURI"></xsl:param>
    
<!-- 
    /**
    *   The file extension (including ".") to be appended to href paths 
    *   (eg ".html")  Should typically be default blank value for viewing in 
    *   metaviewer, and should be .htm or .html for export files
    */ 
-->
    
  <xsl:param name="href_path_extension"></xsl:param>
  
<!-- 
    /**
    *   (primarily beta 6)
    *   When exporting html to files on disk, Morpho renames the DataSet file 
    *   with a "real" name (instead of just the package ID), so the user can 
    *   easily determine which is the "main" file that they shoudl open (the one 
    *   which contains the links to the others). 
    *   When doing exports, therefore, the stylesheet needs to know which package 
    *   ID number refers to the DataSet file, and what is the "real" filename it 
    *   should substitute for this package i.d. number when constructing the 
    *   href links. 
    *
    *   * * * NOTE * * *  - "real" filename should not include the "." or the 
    *   extension - this is taken care of by the "href_path_extension" parameter 
    *   described above
    */ 
-->
  
<!--    which package ID number refers to the DataSet file? (see note above) -->
  <xsl:param name="package_id"></xsl:param>
  
<!--    what name will be given to the DataSet file on disk?(see note above) -->
  <xsl:param name="package_index_name"></xsl:param>
  
<!-- 
    /**
    *   This is the parameter which tells the Attribute stylesheet which 
    *   attribute(s) to display, suppressing the remainder. The default value is 
    *   to display them all.  The type is a string representation of an integer
    */ 
-->
  
  <xsl:param name="selected_attribute">-1</xsl:param>

<!-- 
    /**
    *   These are the parameters that tell the Resource stylesheet which triple 
    *   subjects/objects need to be suppressed in the datapackage metaview.  
    *   Export function uses default value (blank) so they are displayed in the 
    *   exported HTML file
    */ 
-->
  
  <xsl:param name="suppress_subjects_identifier"></xsl:param>
  <xsl:param name="suppress_objects_identifier"></xsl:param>

  
<!-- 
    /**
    *   Most of the html pages are currently laid out as a 2-column table, with 
    *   highlights for more-major rows containing subsection titles etc.  
    *   The following parameters are used within the 
    *           <td width="whateverWidth" class="whateverClass">  
    *   tags to define the column widths and (css) styles.
    *
    *   The values of the "xxxColWidth" parameters can be percentages (need to 
    *   include % sign) or pixels (number only). Note that if a width is defined 
    *   in the CSS stylesheet (see next paragraph), it will override this local
    *   width setting in browsers newer than NN4
    *
    *   The values of the "xxxColStyle" parameters refer to style definitions 
    *   listed in the *.css stylesheet that is defined in this xsl document,
    *   above (in the <xsl:param name="qformat"> tag).
    *
    *   (Note that if the "qformat" is changed from the default by passing a 
    *   value in the url (see notes for <xsl:param name="qformat"> tag, above), 
    *   then the params below must match style names in the "new" CSS stylesheet
    */
-->

<!--    the style for major rows containing subsection titles etc. -->
  <xsl:param name="subHeaderStyle" select="'tablehead'"/>
  
<!--    the width for the first column (but see note above) -->
  <xsl:param name="firstColWidth" select="'20%'"/>
  
<!--    the width for the first column (but see note above) -->
  <xsl:param name="entityFirstColWidth" select="'45%'"/>
  
<!-- the style for the first column -->
  <xsl:param name="firstColStyle" select="'highlight'"/>
  
<!--    the width for the second column (but see note above) -->
  <xsl:param name="secondColWidth" select="'80%'"/>
   
<!--    the width for the first column (but see note above) -->
  <xsl:param name="entitySecondColWidth" select="'55%'"/>
  
<!-- the style for the second column -->
  <xsl:param name="secondColStyle" select="''"/>  
  
<!-- Some html pages use a nested table in the second column.  
     Some of these nested tables set their first column to 
     the following width: -->
  <xsl:param name="secondColIndent" select="'10%'"/> 

</xsl:stylesheet>
