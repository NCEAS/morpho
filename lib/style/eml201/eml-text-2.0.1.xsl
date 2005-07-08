<?xml version="1.0"?>
<!--
  *  '$RCSfile: eml-text-2.0.1.xsl,v $'
  *      Authors: Matthew Brooke
  *    Copyright: 2000 Regents of the University of California and the
  *               National Center for Ecological Analysis and Synthesis
  *  For Details: http://www.nceas.ucsb.edu/
  *
  *   '$Author: sgarg $'
  *     '$Date: 2005-07-08 19:50:05 $'
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
  *
  * This is an XSLT (http://www.w3.org/TR/xslt) stylesheet designed to
  * convert an XML file that is valid with respect to the eml-variable.dtd
  * module of the Ecological Metadata Language (EML) into an HTML format
  * suitable for rendering with modern web browsers.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="html" encoding="iso-8859-1"
              doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
              doctype-system="http://www.w3.org/TR/html4/loose.dtd"
              indent="yes" />  


<!-- This module is for text module in eml2 document. It is a table and self contained-->

  <xsl:template name="text">
        <xsl:param name="textfirstColStyle" />
        <xsl:if test="(section and normalize-space(section)!='') or (para and normalize-space(para)!='')">
        <table xsl:use-attribute-sets="cellspacing" class="{$tabledefaultStyle}" width="100%">
          <xsl:apply-templates  select="." mode="text"/>
        </table>
      </xsl:if>
  </xsl:template>


  <!-- *********************************************************************** -->
  <!-- Template for section-->
   <xsl:template match="section" mode="text">
      <xsl:param name="textfirstColStyle" />
      <xsl:if test="normalize-space(.)!=''">
        <xsl:if test="title and normalize-space(title)!=''">
          <tr>
            <td width="100%" align="left" class="{$secondColStyle}" >
              <b><xsl:value-of select="title"/></b>
            </td>
          </tr>
        </xsl:if>
        <xsl:if test="para and normalize-space(para)!=''">
          <tr>
            <td width="100%" class="{$secondColStyle}">
              <xsl:apply-templates select="para" mode="lowlevel"/>
            </td>
           </tr>
         </xsl:if>
         <xsl:if test="section and normalize-space(section)!=''">
          <tr>
            <td width="100%" class="{$secondColStyle}">
              <xsl:apply-templates select="section" mode="lowlevel"/>
            </td>
         </tr>
        </xsl:if>
      </xsl:if>
  </xsl:template>

  <!-- Section template for low level. Cteate a nested table and second column -->
  <xsl:template match="section" mode="lowlevel">
     <table xsl:use-attribute-sets="cellspacing" class="{$tabledefaultStyle}" width="100%">
      <xsl:if test="title and normalize-space(title)!=''">
        <tr>
          <td width="10%" class="{$secondColStyle}">
            &#160;
          </td>
          <td class="{$secondColStyle}" width="90%" align="left">
            <xsl:value-of select="title"/>
          </td>
        </tr>
      </xsl:if>
      <xsl:if test="para and normalize-space(para)!=''">
        <tr>
          <td width="10%"  class="{$secondColStyle}">
           &#160;
          </td>
          <td width="90%" class="{$secondColStyle}">
            <xsl:apply-templates select="para" mode="lowlevel"/>
          </td>
        </tr>
       </xsl:if>
       <xsl:if test="section and normalize-space(section)!=''">
           <tr>
          <td width="10%"  class="{$secondColStyle}">
           &#160;
          </td>
          <td width="90%" class="{$secondColStyle}">
            <xsl:apply-templates select="section" mode="lowlevel"/>
          </td>
        </tr>
       </xsl:if>
     </table>
  </xsl:template>

  <!-- para template for text mode-->
   <xsl:template match="para" mode="text">
    <xsl:param name="textfirstColStyle"/>
    <tr>
      <td width="100%" class="{$secondColStyle}">
         <xsl:apply-templates mode="lowlevel"/>
      </td>
    </tr>
  </xsl:template>

  <!-- para template without table structure. It does actually transfer.
       Currently, only get the text and it need more revision-->
  <xsl:template match="para" mode="lowlevel">
       <xsl:value-of select="."/><br/>
  </xsl:template>

</xsl:stylesheet>
