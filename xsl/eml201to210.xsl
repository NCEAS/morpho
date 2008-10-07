<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0" >
<xsl:output method="xml" indent="yes"/>
<xsl:strip-space elements="*"/>
<xsl:variable name="packageId" select="/*/@packageId"/>
<xsl:template match="/ ">
 <eml:eml xmlns:eml="eml://ecoinformatics.org/eml-2.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" packageId="{$packageId}" system="knb" xsi:schemaLocation="eml://ecoinformatics.org/eml-2.1.0 eml.xsd">       
     <xsl:apply-templates mode="copy-top-access-tree" select="/*/dataset/access"/>
     <xsl:apply-templates mode="copy-top-access-tree" select="/*/citation/access"/>
     <xsl:apply-templates mode="copy-top-access-tree" select="/*/software/access"/>
     <xsl:apply-templates mode="copy-top-access-tree" select="/*/protocol/access"/>	
     
      <xsl:for-each select="/*/*">
    	  <xsl:choose>
    	     <xsl:when test="name()='dataset'">
                  <xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="handle-elements-under-main-module" select="."/>
    	           </xsl:element>
			 </xsl:when>

    	     <xsl:when test="name()='citation'">
    	         <xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="handle-elements-under-main-module" select="."/>
    	           </xsl:element>
    	     </xsl:when>

    	     <xsl:when test="name()='software'">
    	         <xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="handle-elements-under-main-module" select="."/>
    	           </xsl:element>
    	     </xsl:when>

    	     <xsl:when test="name()='protocol'">
    	         <xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="handle-elements-under-main-module" select="."/>
    	           </xsl:element>
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

	<!-- handle make changes under main module (dataset, citation, protocol and software) -->
	<xsl:template mode="handle-elements-under-main-module" match="*">  		
		<xsl:for-each select="./*">
                  	<xsl:choose>
    	            	<xsl:when test="name()='access'">
    	               		<xsl:apply-templates mode="do-nothing" select="."/>
    	            	</xsl:when>
                     	<xsl:otherwise>
   	                        <xsl:apply-templates select="."/>							
                     	 </xsl:otherwise>
                  	</xsl:choose>
   		</xsl:for-each>     
	</xsl:template>
   
    <!-- main template which will copy nodes recursively-->
    <xsl:template match="*">  
        <xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">  
             <xsl:copy-of select="@*"/>
             <xsl:apply-templates/>
        </xsl:element>
	</xsl:template>

	<!-- fixing dataset/dataTable/attributeList/attribute/measurementScale/datetime -> .../dateTime -->
	<xsl:template match="dataTable/attributeList/attribute/measurementScale/datetime">  
        <xsl:element name="dateTime" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="copy-no-ns" select="./*"/>
    	 </xsl:element>
	</xsl:template>
	
	<xsl:template match="dataTable/method">  
        <xsl:element name="methods" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="copy-no-ns" select="./*"/>
    	 </xsl:element>
	</xsl:template>

    <xsl:template match="spatialRaster/method">  
        <xsl:element name="methods" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="copy-no-ns" select="./*"/>
    	 </xsl:element>
	</xsl:template>

	<xsl:template match="spatialVector/method">  
        <xsl:element name="methods" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="copy-no-ns" select="./*"/>
    	 </xsl:element>
	</xsl:template>

	<xsl:template match="view/method">  
        <xsl:element name="methods" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="copy-no-ns" select="./*"/>
    	 </xsl:element>
	</xsl:template>

	<xsl:template match="storedProcedure/method">  
        <xsl:element name="methods" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="copy-no-ns" select="./*"/>
    	 </xsl:element>
	</xsl:template>

	<!-- copy access tree under dataset(or protocol, software and citation) to the top level -->
	<xsl:template mode="copy-top-access-tree" match="*">
         <xsl:apply-templates mode="copy-no-ns" select="."/>
 	</xsl:template>

	<!-- do nothing for this element (removing it)-->
 	<xsl:template mode="do-nothing" match="*">  
 	</xsl:template>

     <!-- copy node and children without namespace -->
	<xsl:template mode="copy-no-ns" match="*">  
        <xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">  
           <xsl:copy-of select="@*"/> 
           <xsl:apply-templates mode="copy-no-ns"/>  
        </xsl:element> 
	</xsl:template>

  
</xsl:stylesheet>
