<?xml version="1.0"?>
<!--
  * resource.xsl
  *
  *      Authors: Chad Berkley, Matt Jones
  *    Copyright: 2000 Regents of the University of California and the 
  *               National Center for Ecological Analysis and Synthesis
  *  For Details: http://www.nceas.ucsb.edu/
  *      Created: 2000 July 20
  *    File Info: '$Id: resource.xsl,v 1.2 2000-12-19 23:44:57 higgins Exp $'  
  *
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" />
  <xsl:template match="/"> 
    <html>
    <head>
      <link rel="stylesheet" type="text/css" 
       href="default.css" />
      <title>
        <xsl:value-of select="//title" />
      </title>
    </head>
    <body bgcolor="white">
      <h1><xsl:value-of select="//title" /></h1>
      <xsl:if test="count(//alternateTitle) &gt; 0">
        <h2><xsl:value-of select="//alternateTitle" /></h2>
      </xsl:if>
      
      <!-- display the literature specific fields if they are present -->
      <xsl:if test="count(resource/literature/ISBN) &gt; 0">
        <h2>ISBN: <xsl:value-of select="resource/literature/ISBN" /></h2>
      </xsl:if>
      <xsl:if test="count(resource/literature/editor) &gt; 0">
        <xsl:for-each select="resource/literature/editor">
          <h2>editor: <xsl:value-of select="resource/literature/editor" /></h2>
        </xsl:for-each>
      </xsl:if>
      <xsl:if test="count(resource/literature/edition) &gt; 0">
        <h2>edition: <xsl:value-of select="resource/literature/edition" /></h2>
      </xsl:if>
      <xsl:if test="count(resource/literature/volume) &gt; 0">
        <h2>volume: <xsl:value-of select="resource/literature/volume" /></h2>
      </xsl:if>
      <xsl:if test="count(resource/literature/ISBN) &gt; 0">
        <h2>ISBN: <xsl:value-of select="resource/literature/ISBN" /></h2>
      </xsl:if>
      
      <!-- display the contact information -->
      <h3 class="highlight">Contacts</h3>
      
      <xsl:for-each select="resource/*/originator">
        <u>
        <xsl:value-of select="./individualName/salutation" /> 
        <xsl:value-of select="./individualName/givenName" /> 
        <xsl:value-of select="./individualName/surName" /> 
        </u>
        <br />
        <dir>
        <xsl:text>Role: </xsl:text>
        <xsl:value-of select="./roleCode" /><br/>
        <xsl:if test="count(resource/*/organizationName) &gt; 0">
          <xsl:value-of select="./organizationName" />
          <br />
        </xsl:if>
        
        <xsl:if test="count(./contactInfo/phone/voice) &gt; 0">
          Phone: 
          <xsl:for-each select="./contactInfo/phone/voice">
           <xsl:value-of select="." />
            <br />
          </xsl:for-each>
        </xsl:if>
        
        <xsl:if test="count(./contactInfo/phone/facsimile) &gt; 0">
          Fax: 
          <xsl:for-each select="./contactInfo/phone/facsimile">
            <xsl:value-of select="." />
            <br />
          </xsl:for-each>
        </xsl:if>
        
        <xsl:if test="count(./contactInfo/phone/other) &gt; 0">
          Other phone: 
          <xsl:for-each select="./contactInfo/phone/other">
            <xsl:value-of select="." />
              
            <xsl:text>Number type:</xsl:text>
            <xsl:value-of select="/resource/*/*/contactInfo/phone/otherType" />
            <br />
          </xsl:for-each>
        </xsl:if>
        
        <xsl:if test="count(./contactInfo/address) &gt; 0">
          <xsl:value-of select="./contactInfo/address/deliveryPoint" /><br/>
          <xsl:value-of select="./contactInfo/address/city" />, 
          <xsl:value-of select="./contactInfo/address/administrativeArea" /> 
          <xsl:value-of select="./contactInfo/address/postalCode" /> <br/>
          <xsl:value-of select="./contactInfo/address/country" /><br/>
          <xsl:value-of select="./contactInfo/address/electronicMailAddress" />
          <br/>
        </xsl:if>
          
        <xsl:if test="count(./onlineResource) &gt; 0">
          <xsl:value-of select="./onlineResource/linkage/URL" /><br/>
          <xsl:value-of select="./onlineResource/protocol" /><br/>
          <xsl:value-of select="./onlineResource/applicationProtocol" /><br/>
          <xsl:value-of select="./onlineResource/linkage/name" /><br/>
          <xsl:value-of select="./onlineResource/linkage/description" /><br/>
          <xsl:value-of select="./onlineResource/linkage/functionCode" /><br/>
          <br />
        </xsl:if>
        
        <xsl:if test="count(./contactInfo/hoursOfService) &gt; 0">
          Office hours: 
          <xsl:value-of select="./contactInfo/hoursOfService" />
          <br/>
        </xsl:if>
        
        <xsl:if test="count(./contactInfo/contactInstructions) &gt; 0">
          Contact instructions: 
          <xsl:value-of select="./contactInfo/contactInstructions" />
          <br/>
        </xsl:if>
        
        <br/>
        </dir>
      </xsl:for-each>
      
      <xsl:if test="count(resource/*/publisher) &gt; 0">
        <h3 class="highlight">Publishing Information</h3>
        Publisher:
        
        <xsl:value-of select="resource/*/publisher/individualName/salutation" />
         
        <xsl:value-of select="resource/*/publisher/individualName/givenName" /> 
        <xsl:value-of select="resource/*/publisher/individualName/surName" /> 
        <br />
        <xsl:if test="count(resource/*/publisher/organizationName) &gt; 0">
          <xsl:value-of select="resource/*/publisher/organizationName" />
          <br />
        </xsl:if>
        
        <xsl:if test="count(resource/*/publisher/contactInfo/phone/voice) &gt; 0">
          Phone: 
          <xsl:for-each select="resource/*/publisher/contactInfo/phone/voice">
           <xsl:value-of select="." />
            <br />
          </xsl:for-each>
        </xsl:if>
        
        <xsl:if test="count(resource/*/publisher/contactInfo/phone/facsimile) &gt; 0">
          Fax: 
          <xsl:for-each select="resource/*/publisher/contactInfo/phone/facsimile">
            <xsl:value-of select="." />
            <br />
          </xsl:for-each>
        </xsl:if>
        
        <xsl:if test="count(resource/*/publisher/contactInfo/phone/other) &gt; 0">
          Other phone: 
          <xsl:for-each select="resource/*/publisher/contactInfo/phone/other">
            <xsl:value-of select="." />
              
            <xsl:text>Number type:</xsl:text>
            <xsl:value-of select="/resource/*/*/contactInfo/phone/otherType" />
            <br />
          </xsl:for-each>
        </xsl:if>
        
        <xsl:if test="count(resource/*/publisher/contactInfo/address) &gt; 0">
          <xsl:value-of 
           select="resource/*/publisher/contactInfo/address/deliveryPoint" />
           <br/>
          <xsl:value-of 
           select="resource/*/publisher/contactInfo/address/city" />, 
          <xsl:value-of 
           select="resource/*/publisher/contactInfo/address/administrativeArea"/>
            
          <xsl:value-of 
           select="resource/*/publisher/contactInfo/address/postalCode" />
            <br/>
          <xsl:value-of 
           select="resource/*/publisher/contactInfo/address/country" /><br/>
          <xsl:value-of 
           select="resource/*/publisher/contactInfo/address/electronicMailAddress" />
          <br/>
        </xsl:if>
          
        <xsl:if test="count(resource/*/publisher/onlineResource) &gt; 0">
          <xsl:value-of 
           select="resource/*/publisher/onlineResource/linkage/URL" /><br/>
          <xsl:value-of 
           select="resource/*/publisher/onlineResource/protocol" /><br/>
          <xsl:value-of 
           select="resource/*/publisher/onlineResource/applicationProtocol" />
           <br/>
          <xsl:value-of 
           select="resource/*/publisher/onlineResource/linkage/name" /><br/>
          <xsl:value-of 
           select="resource/*/publisher/onlineResource/linkage/description" />
           <br/>
          <xsl:value-of 
           select="resource/*/publisher/onlineResource/linkage/functionCode" />
           <br/>
          <br />
        </xsl:if>
        
        <xsl:if 
         test="count(resource/*/publisher/contactInfo/hoursOfService) &gt; 0">
          Office hours: 
          <xsl:value-of 
           select="resource/*/publisher/contactInfo/hoursOfService" />
          <br/>
        </xsl:if>
        
        <xsl:if 
         test="count(resource/*/publisher/contactInfo/contactInstructions) &gt; 0">
          Contact instructions: 
          <xsl:value-of 
           select="resource/*/publisher/contactInfo/contactInstructions" />
          <br/>
        </xsl:if>
      </xsl:if>
      
      <xsl:if test="count(resource/*/pubdate) &gt; 0">
        Publishing Date:
          <xsl:value-of select="resource/*/pubdate" /><br/>
      </xsl:if>
      
      <xsl:if test="count(resource/*/pubplace) &gt; 0">
        Publishing Place: 
          <xsl:value-of select="resource/*/pubplace" />
        <br/>
      </xsl:if>
      
      <xsl:if test="count(resource/*/series) &gt; 0">
        Series:  <xsl:value-of select="resource/*/series" /><br/>
      </xsl:if>
      
      <xsl:if test="count(resource/*/additionalInfo) &gt; 0">
        <h3 class="highlight">Other Information</h3>
        Additional Information:
          <xsl:value-of select="resource/*/additionalInfo" /><br/>
      </xsl:if>
      
      <!-- display the dataset specific field if present -->
      <xsl:if test="count(resource/dataset/geoForm) &gt; 0">
        <h3 class="highlight">Data Format:</h3>
        <xsl:value-of select="resource/dataset/geoForm" />
      </xsl:if>
      
      <xsl:if test="count(resource/*/abstract) &gt; 0">
        <h3 class="highlight">Abstract</h3>
        <xsl:value-of select="resource/*/abstract" /><br/>
      </xsl:if>
      
      <xsl:for-each select="resource/*/url">
        URL:  
        <a>
        <xsl:attribute name="href">
          <xsl:value-of select="./URL" />
        </xsl:attribute>
        <xsl:value-of select="./URL" /><br/>
        </a>
      </xsl:for-each>
      <br/>
      
      <xsl:if test="count(resource/*/keywordInfo) &gt; 0">
        <h3 class="highlight">Keywords</h3>
        <table width="50%" border="0">
        <tr class="tablehead">
          <th>Keyword</th><th>Keyword Type</th><th>Keyword Thesauri</th>
        </tr>
       
        <xsl:for-each select="resource/*/keywordInfo">
          <tr valign="top">
            <xsl:attribute name="class">
              <xsl:choose>
                <xsl:when test="position() mod 2 = 1">rowodd</xsl:when>
                <xsl:when test="position() mod 2 = 0">roweven</xsl:when>
              </xsl:choose>
            </xsl:attribute>
            <td>
              <xsl:value-of select="./keyword"/>
            </td>
            <td>
              <xsl:value-of select="./keywordType"/>
            </td>
            <td>
              <xsl:for-each select="./keywordThesaurus">
                <xsl:value-of select="."/><br/>
              </xsl:for-each>
            </td>
          </tr>
        </xsl:for-each>

        </table>
      </xsl:if>
      
    </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
