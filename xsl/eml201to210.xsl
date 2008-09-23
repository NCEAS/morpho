<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0" >
<xsl:output method="xml" indent="yes"/>
<xsl:strip-space elements="*"/>
<xsl:template match="/ ">
 <eml:eml xmlns:eml="eml://ecoinformatics.org/eml-2.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" packageId="tao.12340.1" system="knb" xsi:schemaLocation="eml://ecoinformatics.org/eml-2.1.0 eml.xsd">       
    	<xsl:copy-of select="//dataset">
    	</xsl:copy-of>
    	 <xsl:for-each select="//additionalMetadata">
    	       <addtionalMetadata>
    	             <xsl:for-each select="*">
    	           	           <xsl:choose>
    	           	               <xsl:when test="name()='describes'">
    	           	                      <xsl:copy-of select=".">
    	                                  </xsl:copy-of>
    	           	               </xsl:when>
    	           	               <xsl:otherwise>
    	           	                   <metadata>
    	           	                       <xsl:copy-of select=".">
    	                                    </xsl:copy-of>
    	           	                    </metadata>
    	           	                </xsl:otherwise>
    	           	           </xsl:choose>
    	           	  </xsl:for-each>
    	        </addtionalMetadata>
    	 </xsl:for-each>
  </eml:eml>
  </xsl:template>
   <xsl:template match="describes">
          <xsl:copy-of select=".">
    	  </xsl:copy-of>
   </xsl:template>
</xsl:stylesheet>