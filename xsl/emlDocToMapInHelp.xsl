<?xml version="1.0"?>
<!-- this file will transfer eml index (only part, see eml-index.xml) page to a mapID in help map file -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" indent="yes"/>
<xsl:output encoding="ISO-8859-1"/>
 <xsl:param name="filename"/>
   <xsl:template match="/">
     <xsl:element name="map">
       <xsl:for-each select="//a">
         <!--<xsl:if test="starts-with(./@href, '#')">-->
           <xsl:call-template name="mapping"/>
        <!-- </xsl:if>-->
      </xsl:for-each>
     </xsl:element >
   </xsl:template>
   <xsl:template name="mapping">
        <xsl:element name="mapID">
          <xsl:variable name="href" select="./@href" />
          <xsl:variable name="url" select="substring-after($href, '/')"/>
          <xsl:variable name="filename" select="substring-before($url, '.')"/>

          <xsl:attribute name="target"><xsl:value-of select="./"/>_<xsl:value-of select="$filename"/></xsl:attribute>
          <xsl:attribute name="url">morphohelp/<xsl:value-of select="$url"/></xsl:attribute>
       </xsl:element>
   </xsl:template>

</xsl:stylesheet>
