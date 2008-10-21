<?xml version="1.0"?>
<!-- In tree editor eml-2.1.0.xml, there is an issue in sequence element: even though all children is optional, but the sequence itself is required.
This stylesheet tries to modify this xml file. Another file - printSequenceInTreeEditor will print the sequences out.-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
  <xsl:output method="xml" indent="yes"/>
  <xsl:strip-space elements="*"/>
 
  <xsl:template match="/">
 	<xsl:apply-templates mode="copy-no-ns" select="*"/>
  </xsl:template>

  <xsl:template mode="copy-no-ns" match="*">
    <xsl:choose>
    	<xsl:when test="contains(name(), 'SEQUENCE') and @minOccurs != '0'">
      		<xsl:variable name="countRequiredChildren" select="count(./*[@minOccurs='1'])"/>
      		<xsl:variable name="countRequiredChildren2" select="count(./*[@minOccurs='3'])"/>
		<xsl:choose>
        		<xsl:when test="$countRequiredChildren=0 and $countRequiredChildren2=0 ">
                  		<xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">
                                    <xsl:copy-of select="@*"/>
				    <xsl:attribute name="minOccurs">0</xsl:attribute>
				    <xsl:apply-templates mode="copy-no-ns"/>
                   		</xsl:element>
        		</xsl:when>
			<xsl:otherwise>
				<xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">
		                   <xsl:copy-of select="@*"/>
                		   <xsl:apply-templates mode="copy-no-ns"/>
                		</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
    	</xsl:when>
	<xsl:otherwise>
		 <xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">
	           <xsl:copy-of select="@*"/>
        	   <xsl:apply-templates mode="copy-no-ns"/>
        	</xsl:element>
	</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
 
