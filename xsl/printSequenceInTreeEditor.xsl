<?xml version="1.0"?>
<!-- In tree editor eml-2.1.0.xml, there is an issue in sequence element: even though all children is optional, but the sequence itself is required.
This stylesheet tries to print out those sequences, then we will manully change the sequence from requirement to option-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
  <xsl:output method="xml" indent="yes"/>
  <xsl:strip-space elements="*"/>
  <xsl:template match="*">
    <xsl:if test="contains(name(), 'SEQUENCE') and @minOccurs != '0'">
      <xsl:variable name="countRequiredChildren" select="count(./*[@minOccurs='1'])"/>
      <xsl:variable name="countRequiredChildren2" select="count(./*[@minOccurs='3'])"/>
        <xsl:if test="$countRequiredChildren=0 and $countRequiredChildren2=0 ">
                  <xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">
                                    <xsl:copy-of select="@*"/>
                   </xsl:element>
        </xsl:if>
    </xsl:if>
    <xsl:apply-templates select="*"/>
  </xsl:template>
</xsl:stylesheet>
 
