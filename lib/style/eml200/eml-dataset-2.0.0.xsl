<?xml version="1.0"?>
<!--
  *  '$RCSfile: eml-dataset-2.0.0.xsl,v $'
  *      Authors: Matt Jones
  *    Copyright: 2000 Regents of the University of California and the
  *               National Center for Ecological Analysis and Synthesis
  *  For Details: http://www.nceas.ucsb.edu/
  *
  *   '$Author: brooke $'
  *     '$Date: 2003-10-11 04:52:12 $'
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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:import href="eml-resource-2.0.0.xsl"/>
  
  <xsl:output method="html" encoding="iso-8859-1"/>
  
  <xsl:template match="dataset" mode="dataset">
    <table class="tabledefault" width="100%">
     <tr>
        <td rowspan="2" width="100%">
          <xsl:call-template name="resource">
            <xsl:with-param name="resfirstColStyle" select="$firstColStyle"/>
            <xsl:with-param name="ressubHeaderStyle" select="$subHeaderStyle"/>
          </xsl:call-template>
       </td>
     </tr>
    </table>
    
  </xsl:template>
  
  
  <!--<xsl:template match="//dataTable/physical/distribution" mode="resource" />-->
  <xsl:template match="text()" mode="dataset" />
  <xsl:template match="text()" mode="resource" />

</xsl:stylesheet>
