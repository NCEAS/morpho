<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0" xmlns:eml="eml://ecoinformatics.org/eml-2.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" >
<xsl:output method="xml" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/* ">
    <!--handle top level element-->
   <xsl:element name="eml:eml"> 
      <xsl:copy-of select="@*"/> 
      <xsl:attribute name="xsi:schemaLocation">eml://ecoinformatics.org/eml-2.1.0 eml.xsd</xsl:attribute>

     <!-- move the access sub tree to top level-->
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
  </xsl:element>
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

	<!-- fixing attributeList/attribute/measurementScale/datetime -> .../dateTime -->
	<xsl:template match="attributeList/attribute/measurementScale/datetime">  
        <xsl:element name="dateTime" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="copy-no-ns" select="./*"/>
    	 </xsl:element>
	</xsl:template>
	
    <!-- change the name of element form method to methods -->
	<xsl:template match="dataTable/method">  
        <xsl:element name="methods" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="copy-no-ns" select="./*"/>
    	 </xsl:element>
	</xsl:template>

	<!-- change the name of element form method to methods -->
    <xsl:template match="spatialRaster/method">  
        <xsl:element name="methods" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="copy-no-ns" select="./*"/>
    	 </xsl:element>
	</xsl:template>

	<!-- change the name of element form method to methods -->
	<xsl:template match="spatialVector/method">  
        <xsl:element name="methods" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="copy-no-ns" select="./*"/>
    	 </xsl:element>
	</xsl:template>

	<!-- change the name of element form method to methods -->
	<xsl:template match="view/method">  
        <xsl:element name="methods" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="copy-no-ns" select="./*"/>
    	 </xsl:element>
	</xsl:template>

	<!-- change the name of element form method to methods -->
	<xsl:template match="storedProcedure/method">  
        <xsl:element name="methods" namespace="{namespace-uri(.)}">  
           			    <xsl:copy-of select="@*"/> 
                    	<xsl:apply-templates mode="copy-no-ns" select="./*"/>
    	 </xsl:element>
	</xsl:template>


     <!-- Move the access tree of data file level from additionalMetadata part to physical/distribution part.
           If we find the id of physical/distribution is in aditionalMetadata/describe and it 
             has sibling of access subtree, copy the subtree to physical/distribution -->
     <xsl:template match="physical/distribution">
        <xsl:element name="distribution" namespace="{namespace-uri(.)}">
          <xsl:copy-of select="@*"/> 
          <xsl:apply-templates mode="copy-no-access" select="./*"/>
          <!--Check if access arleady exist-->
		  <xsl:choose> 
			<xsl:when test="access/*">
				<!--distribution does have any access node. This can happen that we already moved an access tree 
                      from additionalMetadata to this part. This means document have two or more addtionalMetadata
					   with access tree describing same distribution.
                       <additionalMetadata>
                          <describes>100</describe>
                          <access>...</access>
                        </additionalMetadata>
						 <additionalMetadata>
                          <describes>100</describe>
                          <access>...</access>
                        </additionalMetadata>
                  -->
				<xsl:variable name="id" select="@id"/>
				<xsl:for-each select="access">
				<xsl:param name="accessOrder">
					<xsl:value-of select="@order"/>
                 </xsl:param>	
          		<xsl:for-each select="/*/additionalMetadata/describes">
                	<xsl:variable name="describesId" select="."/>
                	<xsl:if test="$id=$describesId">				
                     	 <xsl:variable name="secondOrder" select="../access/@order"/>
						 	<xsl:choose>
										<!-- two access trees have same order (denyFirst or allowFirst), merge it-->
										<xsl:when test="$accessOrder=$secondOrder">	
												<xsl:element name="access" namespace="{namespace-uri(.)}">
          											<xsl:copy-of select="./access/@*"/> 								
											    	<xsl:apply-templates mode="copy-no-ns" select="../access/*"/>
												</xsl:element>											
										</xsl:when>
										<xsl:otherwise>
											<xsl:message terminate="no">EML 2.0.1 document has more than one access subtree in addtionalMetadata block describing same entity.
                                                                                         However, the access subtree has different order. It is illegitimate. Please fix the EML 2.0.1 document first.
                                             </xsl:message>
										</xsl:otherwise>
									</xsl:choose>
                	</xsl:if>
          		</xsl:for-each>
				</xsl:for-each>	
            </xsl:when>
			<xsl:otherwise>
				<!--distribution doesn't have any access node yet. Move the subtree from addtionalMetadata to distribution-->
				<!--find the id in addtionalMetacat/describes-->
          		<xsl:variable name="id" select="@id"/>
          		<xsl:for-each select="/*/additionalMetadata/describes">
                	<xsl:variable name="describesId" select="."/>
                	<xsl:if test="$id=$describesId">				
                     	 <xsl:apply-templates mode="copy-no-ns" select="../access"/>
                	</xsl:if>
          		</xsl:for-each>			
			</xsl:otherwise>         
		  </xsl:choose>		  
		</xsl:element>
     </xsl:template>

      <!-- Move the access tree of data file level from additionalMetadata part to software/implementation/distribution part.
           If we find the id of physical/distribution is in aditionalMetadata/describe and it 
             has sibling of access subtree, copy the subtree to software/implementation/distribution -->
     <xsl:template match="software/implementation/distribution">
        <xsl:element name="distribution" namespace="{namespace-uri(.)}">
          <xsl:copy-of select="@*"/> 
          <xsl:apply-templates mode="copy-no-ns" select="./*"/>
 		  <!--find the id in addtionalMetacat/describes-->
          <xsl:variable name="id" select="@id"/>
          <xsl:for-each select="/*/additionalMetadata/describes">
                <xsl:variable name="describesId" select="."/>
                <xsl:if test="$id=$describesId">				
                     	 <xsl:apply-templates mode="copy-no-ns" select="../access"/>
                </xsl:if>
          </xsl:for-each>
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

 	<!-- copy node and children without namespace and without access tree -->
	<xsl:template mode="copy-no-access" match="*">
       <xsl:if test="name()!='access'">  
        <xsl:element name="{name(.)}" namespace="{namespace-uri(.)}">  
           <xsl:copy-of select="@*"/> 
           <xsl:apply-templates mode="copy-no-ns"/>  
        </xsl:element>
       </xsl:if> 
	</xsl:template>

  
</xsl:stylesheet>
