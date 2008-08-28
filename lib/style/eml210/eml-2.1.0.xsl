<?xml version="1.0"?>
<!--
  *  '$RCSfile: eml-2.1.0.xsl,v $'
  *      Authors: Matt Jones
  *    Copyright: 2000 Regents of the University of California and the
  *               National Center for Ecological Analysis and Synthesis
  *  For Details: http://www.nceas.ucsb.edu/
  *
  *   '$Author: tao $'
  *     '$Date: 2008-08-28 23:00:02 $'
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
  * convert an XML file that is valid with respect to the eml-dataset.dtd
  * module of the Ecological Metadata Language (EML) into an HTML format
  * suitable for rendering with modern web browsers.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/02/xpath-function" xmlns:eml="eml://ecoinformatics.org/eml-2.0.1" version="1.0">
  <xsl:import href="emlroot-2.1.0.xsl"/>

  <xsl:output method="html" encoding="iso-8859-1"
              doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
              doctype-system="http://www.w3.org/TR/html4/loose.dtd"
              indent="yes" />

  <xsl:template match="/">
    <html>
      <head>
        <title><xsl:value-of select="./eml:eml/dataset/title"/></title>
        <link rel="stylesheet" type="text/css"
              href="{$contextURL}/style/skins/{$qformat}/{$qformat}.css"></link>
        <script language="Javascript" type="text/JavaScript"
                src="{$contextURL}/style/skins/{$qformat}/{$qformat}.js"></script>
        <script language="Javascript"
                type="text/JavaScript"
                src="{$contextURL}/style/common/branding.js"></script>
      </head>
      <body>

        <div id="{$mainTableAligmentStyle}">
          <script language="JavaScript" type="text/JavaScript">
	     <xsl:if test="$insertTemplate='0'">
                 <xsl:comment>insertTemplateOpening('<xsl:value-of select="$contextURL" />');//</xsl:comment>
             </xsl:if>
	     <xsl:if test="$insertTemplate='1'">
                 insertTemplateOpening('<xsl:value-of select="$contextURL" />');
             </xsl:if>
          </script>

          <table xsl:use-attribute-sets="cellspacing" width="100%"
                                        class="{$mainContainerTableStyle}">
          <xsl:apply-templates select="*[local-name()='eml']"/>
          </table>

	  <script language="JavaScript" type="text/JavaScript">
	     <xsl:if test="$insertTemplate='0'">
               <xsl:comment>insertTemplateClosing('<xsl:value-of select="$contextURL" />');//</xsl:comment>
             </xsl:if>
	     <xsl:if test="$insertTemplate='1'">
                 insertTemplateClosing('<xsl:value-of select="$contextURL" />');
             </xsl:if>
          </script>
        </div>
      </body>
    </html>
   </xsl:template>

</xsl:stylesheet>
