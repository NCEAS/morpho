<?xml version="1.0"?>
<!--
  * eml-dataset-display.xsl
  *
  *      Authors: Matt Jones
  *    Copyright: 2000 Regents of the University of California and the 
  *               National Center for Ecological Analysis and Synthesis
  *  For Details: http://www.nceas.ucsb.edu/
  *      Created: 2000 April 5
  *      Version: 0.01
  *    File Info: '$Id: eml-dataset-display.xsl,v 1.1 2000-07-28 21:46:05 higgins Exp $'
  *
  * This is an XSLT (http://www.w3.org/TR/xslt) stylesheet designed to
  * convert an XML file that is valid with respect to the eml-dataset.dtd
  * module of the Ecological Metadata Language (EML) into an HTML format 
  * suitable for rendering with modern web browsers.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="html" encoding="iso-8859-1"/>

  <xsl:template match="/">
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="rowcol.css" />
      </head>
      <body class="emlbody">
        <center>
          <h1>Data set description</h1>
          <h3>Ecological Metadata Language</h3>
        </center>
        <xsl:apply-templates/>      

        <table width="100%">
        <tr><td class="rowodd">
        <b><xsl:text>Keywords:</xsl:text></b>
        </td></tr>
        <tr><td>
          <ul>
            <xsl:for-each select="//keyword_info/keyword">
              <li><xsl:value-of select="."/></li>
            </xsl:for-each>
          </ul>
        </td></tr>
        </table>
         

        <table width="100%">
        <tr><td class="rowodd">
        <b><xsl:text>Related Metadata and Data Files:</xsl:text></b>
        </td></tr>
        <tr><td>
        <ul>
          <xsl:for-each select="//relations">
            <li>
             <xsl:value-of select="@object"/>
             <xsl:text> </xsl:text>
             <xsl:value-of select="@relation"/>
             <xsl:text> </xsl:text>
             <xsl:value-of select="@target"/>
            </li>
          </xsl:for-each>
        </ul>
        </td></tr>
        </table>

      </body>
    </html>
  </xsl:template>

  <xsl:template match="meta_file_id">
    <table width="100%">
    <tr>
    <td class="rowodd"><b><xsl:text>Metadata File ID:</xsl:text></b></td>
    <td><xsl:value-of select="."/></td>
    </tr>
      <xsl:for-each select="//dataset_id|//title">
        <tr>
        <xsl:if test="name(.)='dataset_id'">
          <td class="rowodd"><b><xsl:text>Data set ID:</xsl:text></b></td>
        </xsl:if>
        <xsl:if test="name(.)='title'">
          <td class="rowodd"><b><xsl:text>Title:</xsl:text></b></td>
        </xsl:if>
        <td><xsl:value-of select="."/></td>
        </tr>
      </xsl:for-each>
    </table>

  </xsl:template>

  <xsl:template match="dataset_id"/>
  <xsl:template match="title"/>

  <xsl:template match="originator[1]">
      <table width="100%">
      <tr><td class="rowodd">
      <b><xsl:text>Data Set Owner(s):</xsl:text></b>
      </td></tr>
      </table>
  </xsl:template>

  <xsl:template match="party_org">
    <br><b><xsl:value-of select="."/></b></br>
  </xsl:template>

  <xsl:template match="party_individual">
    <br>
    <b>
       <xsl:value-of select="./salutation"/>
       <xsl:text> </xsl:text>
       <xsl:value-of select="./given_name"/>
       <xsl:text> </xsl:text>
       <xsl:value-of select="./surname"/>
    </b></br>
    <br><xsl:value-of select="./title"/></br>
  </xsl:template>

  <xsl:template match="meta_address">
    <table>
    <xsl:for-each select="./address">
      <tr>
      <td><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <tr>
    <td><xsl:value-of select="./city"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="./admin_area"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="./postal_code"/>
    </td>
    </tr>
    <xsl:if test="./work_phone">
      <tr><td><xsl:text>Work: </xsl:text><xsl:value-of select="./work_phone"/>
       </td></tr>
    </xsl:if>
    <xsl:if test="./home_phone">
      <tr><td><xsl:text>Home: </xsl:text><xsl:value-of select="./home_phone"/>
       </td></tr>
    </xsl:if>
    <xsl:if test="./fax">
      <tr><td><xsl:text>Fax: </xsl:text><xsl:value-of select="./fax"/></td></tr>
    </xsl:if>
    <xsl:if test="./email">
      <tr><td><xsl:value-of select="./email"/></td></tr>
    </xsl:if>
    <xsl:if test="./resource_url">
      <tr><td><xsl:value-of select="./resource_url"/></td></tr>
    </xsl:if>
    </table>
  </xsl:template>

  <xsl:template match="abstract">
    <table width="100%">
    <tr>
    <td class="rowodd"><b><xsl:text>Abstract:</xsl:text></b></td>
    </tr>
    <tr>
    <td><xsl:value-of select="."/></td>
    </tr></table>
  </xsl:template>

  <xsl:template match="keyword_info"/>
  <xsl:template match="relations"/>

</xsl:stylesheet>
