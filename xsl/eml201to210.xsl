<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0" >
<xsl:output method="xml" indent="yes"/>
<xsl:strip-space elements="*"/>
<xsl:variable name="packageId" select="/*/@packageId"/>
<xsl:template match="/ ">
 <eml:eml xmlns:eml="eml://ecoinformatics.org/eml-2.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" packageId="{$packageId}" system="knb" xsi:schemaLocation="eml://ecoinformatics.org/eml-2.1.0 eml.xsd">       
    	<xsl:for-each select="/*/*">
    	  <xsl:choose>
    	     <xsl:when test="name()='dataset'">
    	         <xsl:apply-templates mode="copy-no-ns" select="."/>
    	     </xsl:when>
    	     <xsl:when test="name()='citation'">
    	         <xsl:apply-templates mode="copy-no-ns" select="."/>
    	     </xsl:when>
    	     <xsl:when test="name()='software'">
    	         <xsl:apply-templates mode="copy-no-ns" select="."/>
    	     </xsl:when>
    	     <xsl:when test="name()='protocol'">
    	         <xsl:apply-templates mode="copy-no-ns" select="."/>
    	     </xsl:when>
    	     <xsl:when test="name()='additionalMetadata'">
    	       <additionalMetadata>
    	             <xsl:for-each select="*">
    	           	           <xsl:choose>
    	           	               <xsl:when test="name()='describes'">
    	           	                    <xsl:apply-templates mode="copy-no-ns" select="."/>
    	           	               </xsl:when>
    	           	               <xsl:otherwise>
    	           	                   <metadata>
    	           	                      <xsl:apply-templates mode="copy-no-ns" select="."/>
    	           	                    </metadata>
    	           	                </xsl:otherwise>
    	           	           </xsl:choose>
    	           	  </xsl:for-each>
    	        </additionalMetadata>
    	     </xsl:when>
    	   </xsl:choose>
    	 </xsl:for-each>
  </eml:eml>
  </xsl:template>
  <!-- copy node and children without namespace -->
   <xsl:template mode="copy-no-ns" match="*">  
        <xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">  
           <xsl:copy-of select="@*"/> 
           <xsl:apply-templates mode="copy-no-ns"/>  
        </xsl:element> 
   </xsl:template>
</xsl:stylesheet>
