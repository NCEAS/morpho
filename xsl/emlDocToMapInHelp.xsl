<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" indent="yes"/>
<xsl:output encoding="ISO-8859-1"/>
 <xsl:param name="filename"/>
   <xsl:template match="/">
       <xsl:for-each select="//a">
         <xsl:if test="starts-with(./@href, '#')">
           <xsl:call-template name="mapping"/>
         </xsl:if>
      </xsl:for-each>
   </xsl:template>
   <xsl:template name="mapping">
        <xsl:element name="mapping">
          <xsl:attribute name="target"><xsl:value-of select="./"/>_<xsl:value-of select="$filename"/></xsl:attribute>
          <xsl:attribute name="url">morphohelp/<xsl:value-of select="$filename"/><xsl:value-of select="./@href"/></xsl:attribute>
       </xsl:element>
   </xsl:template>

</xsl:stylesheet>
