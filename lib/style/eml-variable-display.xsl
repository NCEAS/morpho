<?xml version="1.0"?>
<!--
  * eml-variable-display2.xsl
  *
  *      Authors: Matt Jones
  *    Copyright: 2000 Regents of the University of California and the 
  *               National Center for Ecological Analysis and Synthesis
  *  For Details: http://www.nceas.ucsb.edu/
  *      Created: 2000 April 5
  *    File Info: '$Id: eml-variable-display.xsl,v 1.1 2000-07-12 19:47:44 higgins Exp $'
  *
  * This is an XSLT (http://www.w3.org/TR/xslt) stylesheet designed to
  * convert an XML file that is valid with respect to the eml-variable.dtd
  * module of the Ecological Metadata Language (EML) into an HTML format 
  * suitable for rendering with modern web browsers.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="html" encoding="iso-8859-1"/>

  <xsl:template match="/">
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/xmltodb/xsqltest/rowcol.css" />
      </head>
      <body class="emlbody">
        <center>
          <h1>Attribute structure description</h1>
          <h3>Ecological Metadata Language</h3>
        </center>
        
        <xsl:apply-templates select="eml-variable/meta_file_id"/>

        <h3>Attributes in the Data Set:</h3>
        <table width="100%">
        <tr class="rowodd">
        <th><xsl:text>Attribute Name</xsl:text></th>
        <th><xsl:text>Attribute Definition</xsl:text></th>
        <th><xsl:text>Unit</xsl:text></th>
        <th><xsl:text>Type</xsl:text></th>
        <th><xsl:text>Codes</xsl:text></th>
        <th><xsl:text>Range</xsl:text></th>
        <th><xsl:text>Missing</xsl:text></th>
        <th><xsl:text>Precision</xsl:text></th>
        <th><xsl:text>Format</xsl:text></th>
        </tr>

        <xsl:for-each select="eml-variable/variable">
          <tr valign="top">
            <xsl:attribute name="class">
              <xsl:choose>
                <xsl:when test="position() mod 2 = 1">rowwhite</xsl:when>
                <xsl:when test="position() mod 2 = 0">rowlight</xsl:when>
              </xsl:choose>
            </xsl:attribute>

          <td><b><xsl:value-of select="variable_name"/></b>
              <xsl:text>&nbsp;</xsl:text></td>
          <td><xsl:value-of select="variable_definition"/>
              <xsl:text>&nbsp;</xsl:text></td>
          <td><xsl:value-of select="unit"/>
              <xsl:text>&nbsp;</xsl:text></td>
          <td><xsl:value-of select="storage_type"/>
              <xsl:text>&nbsp;</xsl:text></td>
          <td><ul>
              <xsl:for-each select="code_definition">
                <li><xsl:value-of select="code"/>
                    <xsl:text> - </xsl:text>
                    <xsl:value-of select="definition"/>
                </li>
              </xsl:for-each>
              </ul>
              <xsl:text>&nbsp;</xsl:text></td>
          <td><ul>
              <xsl:for-each select="numeric_range">
                <li><xsl:value-of select="minimum"/>
                    <xsl:text> - </xsl:text>
                    <xsl:value-of select="maximum"/>
                </li>
              </xsl:for-each>
              </ul>
              <xsl:text>&nbsp;</xsl:text></td>
          <td><xsl:for-each select="missing_value_code">
                <xsl:value-of select="."/><br />
              </xsl:for-each>
              <xsl:text>&nbsp;</xsl:text></td>
          <td><xsl:value-of select="precision"/>
              <xsl:text>&nbsp;</xsl:text></td>
          <td><xsl:apply-templates select="field_format"/>
              <xsl:text>&nbsp;</xsl:text></td>
          </tr>
        </xsl:for-each>
        </table>

      </body>
    </html>
  </xsl:template>

  <xsl:template match="meta_file_id">
    <table>
      <tr>
        <td class="shaded">
           <b><xsl:text>Metadata File ID:</xsl:text></b>
        </td>
        <td>
            <xsl:value-of select="."/>
        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template match="field_format">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="variable_format">
    <xsl:text>Variable: </xsl:text>
  </xsl:template>

  <xsl:template match="delimiter">
    <xsl:text>Variable </xsl:text>
    <xsl:text>(</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="fixed_format">
    <xsl:text>Fixed</xsl:text>
  </xsl:template>

  <xsl:template match="field_width">
    <xsl:text>Fixed </xsl:text>
    <xsl:text>(</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>)</xsl:text>
  </xsl:template>

</xsl:stylesheet>
