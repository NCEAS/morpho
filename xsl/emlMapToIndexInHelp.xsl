<?xml version="1.0"?>
<!-- this file will transfer eml map page to a index.xml in help file -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" indent="yes"/>
<xsl:output encoding="ISO-8859-1"/>
 <xsl:param name="filename"/>
   <xsl:template match="/">
       <xsl:for-each select="//mapID">
          <xsl:call-template name="index"/>
        </xsl:for-each>
   </xsl:template>
   <xsl:template name="index">
        <xsl:element name="indexitem">
          <xsl:variable name="targetname" select="./@target" />
          <xsl:variable name="textname" select="substring-before($targetname, '_')"/>
          <xsl:attribute name="text"><xsl:value-of select="$textname"/></xsl:attribute>
          <xsl:attribute name="target"><xsl:value-of select="$targetname"/></xsl:attribute>
        </xsl:element>
   </xsl:template>

</xsl:stylesheet>
