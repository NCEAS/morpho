<?xml version="1.0"?>
<!--
  *  '$RCSfile: emlroot-2.1.0.xsl,v $'
  *      Authors: Matt Jones
  *    Copyright: 2000 Regents of the University of California and the
  *               National Center for Ecological Analysis and Synthesis
  *  For Details: http://www.nceas.ucsb.edu/
  *
  *   '$Author: tao $'
  *     '$Date: 2008-08-28 23:00:02 $'
  * '$Revision: 1.1 $'
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * This is an XSLT (http://www.w3.org/TR/xslt) stylesheet designed to
  * convert an XML file that is valid with respect to the eml-dataset.dtd
  * module of the Ecological Metadata Language (EML) into an HTML format
  * suitable for rendering with modern web browsers.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/02/xpath-function" version="1.0">
  <xsl:import href="eml-access-2.1.0.xsl"/>
  <xsl:import href="eml-additionalmetadata-2.1.0.xsl"/>
  <xsl:import href="eml-attribute-2.1.0.xsl"/>
  <xsl:import href="eml-attribute-enumeratedDomain-2.1.0.xsl"/>
  <xsl:import href="eml-constraint-2.1.0.xsl"/>
  <xsl:import href="eml-coverage-2.1.0.xsl"/>
  <xsl:import href="eml-dataset-2.1.0.xsl"/>
  <xsl:import href="eml-datatable-2.1.0.xsl"/>
  <xsl:import href="eml-distribution-2.1.0.xsl"/>
  <xsl:import href="eml-entity-2.1.0.xsl"/>
  <xsl:import href="eml-identifier-2.1.0.xsl"/>
  <xsl:import href="eml-literature-2.1.0.xsl"/>
  <xsl:import href="eml-method-2.1.0.xsl"/>
  <xsl:import href="eml-otherentity-2.1.0.xsl"/>
  <xsl:import href="eml-party-2.1.0.xsl"/>
  <xsl:import href="eml-physical-2.1.0.xsl"/>
  <xsl:import href="eml-project-2.1.0.xsl"/>
  <xsl:import href="eml-protocol-2.1.0.xsl"/>
  <xsl:import href="eml-resource-2.1.0.xsl"/>
  <xsl:import href="eml-settings-2.1.0.xsl"/>
  <xsl:import href="eml-software-2.1.0.xsl"/>
  <xsl:import href="eml-spatialraster-2.1.0.xsl"/>
  <xsl:import href="eml-spatialvector-2.1.0.xsl"/>
  <xsl:import href="eml-storedprocedure-2.1.0.xsl"/>
  <xsl:import href="eml-text-2.1.0.xsl"/>
  <xsl:import href="eml-view-2.1.0.xsl"/>

 <!-- global variables to store id node set in case to be referenced-->
 <xsl:variable name="ids" select="//*[@id!='']"/>
  
 <xsl:template match="*[local-name()='eml']">
     <tr><td>
       <xsl:for-each select="dataset">
         <xsl:call-template name="emldataset"/>
       </xsl:for-each>
        <!-- Additinal metadata-->
       <xsl:choose>
               <xsl:when test="$displaymodule='additionalmetadata' or $displaymodule='printall'">
                   <xsl:for-each select="additionalMetadata">
                     <xsl:if test="$displaymodule='additionalmetadata' and $additionalmetadataindex=position()">
                        <tr><td>
                            <xsl:call-template name="additionalmetadata">
                               <xsl:with-param name="additionalmetadataindex" select="position()"/>
                            </xsl:call-template>
                         </td></tr>
                     </xsl:if>
                     <xsl:if test="$displaymodule='printall'">
                        <tr><td>
                            <xsl:call-template name="additionalmetadata">
                               <xsl:with-param name="additionalmetadataindex" select="position()"/>
                            </xsl:call-template>
                         </td></tr>
                     </xsl:if>
                  </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:if test="$displaymodule='dataset'">
                   <xsl:if test="$withAdditionalMetadataLink='1'">
                     <xsl:for-each select="additionalMetadata">
                         <xsl:call-template name="additionalmetadataURL">
                             <xsl:with-param name="additionalmetadataindex" select="position()"/>
                          </xsl:call-template>
                     </xsl:for-each>
                   </xsl:if>
                 </xsl:if>
              </xsl:otherwise>
     </xsl:choose>
       <xsl:for-each select="citation">
         <xsl:call-template name="emlcitation"/>
       </xsl:for-each>
       <xsl:for-each select="software">
         <xsl:call-template name="emlsoftware"/>
       </xsl:for-each>
       <xsl:for-each select="protocol">
         <xsl:call-template name="emlprotocol"/>
       </xsl:for-each>
      </td></tr>

     <!-- xml format
     <xsl:if test="$displaymodule='dataset'">
       <xsl:if test="$withOriginalXMLLink='1'">
         <tr><td>
            <xsl:call-template name="xml"/>
         </td></tr>
       </xsl:if>
     </xsl:if-->
   </xsl:template>

   <!--********************************************************
                             dataset part
       ********************************************************-->

   <xsl:template name="emldataset">
      <table xsl:use-attribute-sets="cellspacing"  class="{$tabledefaultStyle}" width="100%">
          <xsl:if test="$displaymodule='dataset'">
             <xsl:call-template name="datasetpart"/>
          </xsl:if>
          <xsl:if test="$displaymodule='entity'">
             <xsl:call-template name="entitypart"/>
          </xsl:if>
          <xsl:if test="$displaymodule='attribute'">
             <xsl:call-template name="attributepart"/>
          </xsl:if>
          <xsl:if test="$displaymodule='attributedomain'">
             <xsl:call-template name="datasetattributedomain"/>
          </xsl:if>
          <xsl:if test="$displaymodule='attributecoverage'">
             <xsl:call-template name="datasetattributecoverage"/>
          </xsl:if>
          <xsl:if test="$displaymodule='attributemethod'">
             <xsl:call-template name="datasetattributemethod"/>
          </xsl:if>
          <xsl:if test="$displaymodule='inlinedata'">
             <xsl:call-template name="emlinlinedata"/>
          </xsl:if>
          <xsl:if test="$displaymodule='attributedetail'">
             <xsl:call-template name="entityparam"/>
          </xsl:if>
          <xsl:if test="$displaymodule='printall'">
             <xsl:call-template name="printalltemplate"/>
          </xsl:if>
      </table>
   </xsl:template>

   <!--*************** Data set diaplay *************-->
   <xsl:template name="datasetpart">
			<xsl:call-template name="identifier"/>
     <tr>
           <td colspan="2">
              <xsl:apply-templates select="." mode="dataset"/>
           </td>
      </tr>
   </xsl:template>
   

   <!--************ Entity diplay *****************-->
   <xsl:template name="entitypart">
       <xsl:choose>
               <xsl:when test="references!=''">
                  <xsl:variable name="ref_id" select="references"/>
                  <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                  <xsl:for-each select="$references">
                     <xsl:call-template name="entitypartcommon"/>
                  </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:call-template name="entitypartcommon"/>
              </xsl:otherwise>
         </xsl:choose>
    </xsl:template>


    <xsl:template name="entitypartcommon">
      <tr><td colspan="2">
            <h3>Entity Description</h3>
      </td></tr>
      <xsl:call-template name="identifier">
                <xsl:with-param name="packageID" select="../@packageId"/>
                <xsl:with-param name="system" select="../@system"/>
      </xsl:call-template>
      <tr>
           <td colspan="2">
              <!-- find the subtree to process -->
             <xsl:call-template name="entityparam"/>
           </td>
      </tr>
   </xsl:template>

   <!--************ Attribute display *****************-->
   <xsl:template name="attributedetailpart">
   </xsl:template>

    <xsl:template name="attributepart">
      <tr><td width="100%">

            <h3>Attributes Description</h3>

      </td></tr>
      <tr>
           <td width="100%">
              <!-- find the subtree to process -->
            <xsl:if test="$entitytype='dataTable'">
              <xsl:for-each select="dataTable">
                  <xsl:if test="position()=$entityindex">
                      <xsl:for-each select="attributeList">
                         <xsl:call-template name="attributelist">
                            <xsl:with-param name="docid" select="$docid"/>
                            <xsl:with-param name="entitytype" select="$entitytype"/>
                            <xsl:with-param name="entityindex" select="$entityindex"/>
                         </xsl:call-template>
                      </xsl:for-each>
                  </xsl:if>
              </xsl:for-each>
            </xsl:if>
          </td>
      </tr>
   </xsl:template>

   <!--************************Attribute Domain display module************************-->
   <xsl:template name="datasetattributedomain">
      <tr><td>
            <h3>Attribute Domain</h3>
      </td></tr>
      <tr>
           <td width="100%">
             <!-- find the subtree to process -->
             <xsl:call-template name="entityparam"/>
          </td>
      </tr>
   </xsl:template>


   <!--************************Attribute Method display module************************-->
   <xsl:template name="datasetattributemethod">
      <tr><td>
            <h3>Attribute Method</h3>
      </td></tr>
      <tr>
           <td width="100%">
             <!-- find the subtree to process -->
             <xsl:call-template name="entityparam"/>
          </td>
      </tr>
   </xsl:template>


   <!--************************Attribute Coverage display module************************-->
   <xsl:template name="datasetattributecoverage">
     <tr><td>
            <h3>Attribute Coverage</h3>
      </td></tr>
      <tr>
           <td width="100%">
             <!-- find the subtree to process -->
             <xsl:call-template name="entityparam"/>
          </td>
      </tr>
   </xsl:template>


   <!--************************Print all display module************************-->
   <xsl:template name="printalltemplate">
 	<!-- find the subtree to process -->
             <xsl:call-template name="datasetpart"/>
   </xsl:template>


   <xsl:template name="entityparam">
     <xsl:choose>
      <xsl:when test="$entitytype=''">
	<xsl:variable name="dataTableCount" select="0"/>
        <xsl:variable name="spatialRasterCount" select="0"/>
        <xsl:variable name="spatialVectorCount" select="0"/>
        <xsl:variable name="storedProcedureCount" select="0"/>
        <xsl:variable name="viewCount" select="0"/>
        <xsl:variable name="otherEntityCount" select="0"/>
        <xsl:for-each select="dataTable|spatialRaster|spatialVector|storedProcedure|view|otherEntity">

        <xsl:if test="'dataTable' = name()">
           <xsl:variable name="currentNode" select="."/>
           <xsl:variable name="dataTableCount">
            <xsl:for-each select="../dataTable">
    	      <xsl:if test=". = $currentNode">
                <xsl:value-of select="position()"/>
              </xsl:if>
            </xsl:for-each>
	   </xsl:variable>
           <xsl:if test="position() = $entityindex">
             <xsl:choose>
               <xsl:when test="$displaymodule='attributedetail'">
                 <xsl:for-each select="attributeList">
                   <xsl:call-template name="singleattribute">
                    <xsl:with-param name="attributeindex" select="$attributeindex"/>
                    <xsl:with-param name="docid" select="$docid"/>
                    <xsl:with-param name="entitytype" select="'dataTable'"/>
                    <xsl:with-param name="entityindex" select="$dataTableCount"/>
                   </xsl:call-template>
                 </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:for-each select="../.">
                   <xsl:call-template name="chooseentity">
                    <xsl:with-param name="entitytype" select="'dataTable'"/>
                    <xsl:with-param name="entityindex" select="$dataTableCount"/>
                   </xsl:call-template>
                  </xsl:for-each>
                  </xsl:otherwise>
 		</xsl:choose>
           </xsl:if>
        </xsl:if>

        <xsl:if test="'spatialRaster' = name()">
          <xsl:variable name="currentNode" select="."/>
           <xsl:variable name="spatialRasterCount">
            <xsl:for-each select="../spatialRaster">
    	      <xsl:if test=". = $currentNode">
                <xsl:value-of select="position()"/>
              </xsl:if>
            </xsl:for-each>
	   </xsl:variable>
            <xsl:if test="position() = $entityindex">
           <xsl:choose>
               <xsl:when test="$displaymodule='attributedetail'">
                 <xsl:for-each select="attributeList">
                   <xsl:call-template name="singleattribute">
                    <xsl:with-param name="attributeindex" select="$attributeindex"/>
                    <xsl:with-param name="docid" select="$docid"/>
                    <xsl:with-param name="entitytype" select="'spatialRaster'"/>
                    <xsl:with-param name="entityindex" select="$spatialRasterCount"/>
                   </xsl:call-template>
                 </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:for-each select="../.">
                   <xsl:call-template name="chooseentity">
                    <xsl:with-param name="entitytype" select="'spatialRaster'"/>
                    <xsl:with-param name="entityindex" select="$spatialRasterCount"/>
                   </xsl:call-template>
                  </xsl:for-each>
                  </xsl:otherwise>
 		</xsl:choose>
            </xsl:if>
        </xsl:if>

        <xsl:if test="'spatialVector' = name()">
          <xsl:variable name="currentNode" select="."/>
           <xsl:variable name="spatialVectorCount">
            <xsl:for-each select="../spatialVector">
    	      <xsl:if test=". = $currentNode">
                <xsl:value-of select="position()"/>
              </xsl:if>
            </xsl:for-each>
	   </xsl:variable>
           <xsl:if test="position() = $entityindex">
             <xsl:choose>
               <xsl:when test="$displaymodule='attributedetail'">
                 <xsl:for-each select="attributeList">
                   <xsl:call-template name="singleattribute">
                    <xsl:with-param name="attributeindex" select="$attributeindex"/>
                    <xsl:with-param name="docid" select="$docid"/>
                    <xsl:with-param name="entitytype" select="'spatialVector'"/>
                    <xsl:with-param name="entityindex" select="$spatialVectorCount"/>
                   </xsl:call-template>
                 </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:for-each select="../.">
                   <xsl:call-template name="chooseentity">
                    <xsl:with-param name="entitytype" select="'spatialVector'"/>
                    <xsl:with-param name="entityindex" select="$spatialVectorCount"/>
                   </xsl:call-template>
                  </xsl:for-each>
                  </xsl:otherwise>
 		</xsl:choose>
           </xsl:if>
        </xsl:if>

        <xsl:if test="'storedProcedure' = name()">
          <xsl:variable name="currentNode" select="."/>
           <xsl:variable name="storedProcedureCount">
            <xsl:for-each select="../storedProcedure">
    	      <xsl:if test=". = $currentNode">
                <xsl:value-of select="position()"/>
              </xsl:if>
            </xsl:for-each>
	   </xsl:variable>
           <xsl:if test="position() = $entityindex">
             <xsl:choose>
               <xsl:when test="$displaymodule='attributedetail'">
                 <xsl:for-each select="attributeList">
                   <xsl:call-template name="singleattribute">
                    <xsl:with-param name="attributeindex" select="$attributeindex"/>
                    <xsl:with-param name="docid" select="$docid"/>
                    <xsl:with-param name="entitytype" select="'storedProcedure'"/>
                    <xsl:with-param name="entityindex" select="$storedProcedureCount"/>
                   </xsl:call-template>
                 </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:for-each select="../.">
                   <xsl:call-template name="chooseentity">
                    <xsl:with-param name="entitytype" select="'storedProcedure'"/>
                    <xsl:with-param name="entityindex" select="$storedProcedureCount"/>
                   </xsl:call-template>
                  </xsl:for-each>
                  </xsl:otherwise>
              </xsl:choose>
           </xsl:if>
        </xsl:if>

        <xsl:if test="'view' = name()">
          <xsl:variable name="currentNode" select="."/>
           <xsl:variable name="viewCount">
            <xsl:for-each select="../view">
    	      <xsl:if test=". = $currentNode">
                <xsl:value-of select="position()"/>
              </xsl:if>
            </xsl:for-each>
	   </xsl:variable>
           <xsl:if test="position() = $entityindex">
            <xsl:choose>
               <xsl:when test="$displaymodule='attributedetail'">
                 <xsl:for-each select="attributeList">
                   <xsl:call-template name="singleattribute">
                    <xsl:with-param name="attributeindex" select="$attributeindex"/>
                    <xsl:with-param name="docid" select="$docid"/>
                    <xsl:with-param name="entitytype" select="'view'"/>
                    <xsl:with-param name="entityindex" select="$viewCount"/>
                   </xsl:call-template>
                 </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:for-each select="../.">
                   <xsl:call-template name="chooseentity">
                    <xsl:with-param name="entitytype" select="'view'"/>
                    <xsl:with-param name="entityindex" select="$viewCount"/>
                   </xsl:call-template>
                  </xsl:for-each>
                  </xsl:otherwise>
 		</xsl:choose>
            </xsl:if>
        </xsl:if>

        <xsl:if test="'otherEntityTable' = name()">
          <xsl:variable name="currentNode" select="."/>
           <xsl:variable name="otherEntityCount">
            <xsl:for-each select="../otherEntity">
    	      <xsl:if test=". = $currentNode">
                <xsl:value-of select="position()"/>
              </xsl:if>
            </xsl:for-each>
	   </xsl:variable>
           <xsl:if test="position() = $entityindex">
            <xsl:choose>
               <xsl:when test="$displaymodule='attributedetail'">
                 <xsl:for-each select="attributeList">
                   <xsl:call-template name="singleattribute">
                    <xsl:with-param name="attributeindex" select="$attributeindex"/>
                    <xsl:with-param name="docid" select="$docid"/>
                    <xsl:with-param name="entitytype" select="'otherEntity'"/>
                    <xsl:with-param name="entityindex" select="$otherEntityCount"/>
                   </xsl:call-template>
                 </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:for-each select="../.">
                   <xsl:call-template name="chooseentity">
                    <xsl:with-param name="entitytype" select="'otherEntity'"/>
                    <xsl:with-param name="entityindex" select="$otherEntityCount"/>
                   </xsl:call-template>
                  </xsl:for-each>
                  </xsl:otherwise>
 		</xsl:choose>
             </xsl:if>
        </xsl:if>
       </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
	<xsl:choose>
           <xsl:when test="$displaymodule='attributedetail'">
            <xsl:for-each select="attributeList">
             <xsl:call-template name="singleattribute">
               <xsl:with-param name="attributeindex" select="$attributeindex"/>
               <xsl:with-param name="docid" select="$docid"/>
               <xsl:with-param name="entitytype" select="$entitytype"/>
               <xsl:with-param name="entityindex" select="$entityindex"/>
             </xsl:call-template>
            </xsl:for-each>
           </xsl:when>
           <xsl:otherwise>
             <xsl:call-template name="chooseentity">
               <xsl:with-param name="entitytype" select="$entitytype"/>
               <xsl:with-param name="entityindex" select="$entityindex"/>
             </xsl:call-template>
           </xsl:otherwise>
 	 </xsl:choose>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>


   <xsl:template name="chooseentity" match='dataset'>
      <xsl:param name="entityindex"/>
      <xsl:param name="entitytype"/>
           <xsl:if test="$entitytype='dataTable'">
              <xsl:for-each select="dataTable">
                  <xsl:if test="position()=$entityindex">
                         <xsl:choose>
                           <xsl:when test="references!=''">
                              <xsl:variable name="ref_id" select="references"/>
                              <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                                <xsl:for-each select="$references">
                                    <xsl:choose>
                                       <xsl:when test="$displaymodule='entity' or $displaymodule='printall'">
                                          <xsl:call-template name="dataTable">
                                              <xsl:with-param name="datatablefirstColStyle" select="$firstColStyle"/>
                                              <xsl:with-param name="datatablesubHeaderStyle" select="$subHeaderStyle"/>
                                              <xsl:with-param name="docid" select="$docid"/>
                                              <xsl:with-param name="entitytype" select="$entitytype"/>
                                              <xsl:with-param name="entityindex" select="$entityindex"/>
                                          </xsl:call-template>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:call-template name="chooseattributelist"/>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                           </xsl:when>
                           <xsl:otherwise>
                             <xsl:choose>
                                       <xsl:when test="$displaymodule='entity' or $displaymodule='printall'">
                                          <xsl:call-template name="dataTable">
                                              <xsl:with-param name="datatablefirstColStyle" select="$firstColStyle"/>
                                              <xsl:with-param name="datatablesubHeaderStyle" select="$subHeaderStyle"/>
                                              <xsl:with-param name="docid" select="$docid"/>
                                              <xsl:with-param name="entitytype" select="$entitytype"/>
                                              <xsl:with-param name="entityindex" select="$entityindex"/>
                                          </xsl:call-template>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:call-template name="chooseattributelist"/>
                                       </xsl:otherwise>
                             </xsl:choose>
                         </xsl:otherwise>
                      </xsl:choose>
                  </xsl:if>
              </xsl:for-each>
            </xsl:if>
            <xsl:if test="$entitytype='spatialRaster'">
              <xsl:for-each select="spatialRaster">
                  <xsl:if test="position()=$entityindex">
                         <xsl:choose>
                           <xsl:when test="references!=''">
                              <xsl:variable name="ref_id" select="references"/>
                              <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                                <xsl:for-each select="$references">
                                    <xsl:choose>
                                       <xsl:when test="$displaymodule='entity' or $displaymodule='printall'">
                                          <xsl:call-template name="spatialRaster">
                                              <xsl:with-param name="spatialrasterfirstColStyle" select="$firstColStyle"/>
                                              <xsl:with-param name="spatialrastersubHeaderStyle" select="$subHeaderStyle"/>
                                              <xsl:with-param name="docid" select="$docid"/>
                                              <xsl:with-param name="entitytype" select="$entitytype"/>
                                              <xsl:with-param name="entityindex" select="$entityindex"/>
                                          </xsl:call-template>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:call-template name="chooseattributelist"/>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                           </xsl:when>
                           <xsl:otherwise>
                             <xsl:choose>
                                       <xsl:when test="$displaymodule='entity' or $displaymodule='printall'">
                                          <xsl:call-template name="spatialRaster">
                                              <xsl:with-param name="spatialrasterfirstColStyle" select="$firstColStyle"/>
                                              <xsl:with-param name="spatialrastersubHeaderStyle" select="$subHeaderStyle"/>
                                              <xsl:with-param name="docid" select="$docid"/>
                                              <xsl:with-param name="entitytype" select="$entitytype"/>
                                              <xsl:with-param name="entityindex" select="$entityindex"/>
                                          </xsl:call-template>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:call-template name="chooseattributelist"/>
                                       </xsl:otherwise>
                             </xsl:choose>
                         </xsl:otherwise>
                      </xsl:choose>
                  </xsl:if>
              </xsl:for-each>
            </xsl:if>
            <xsl:if test="$entitytype='spatialVector'">
              <xsl:for-each select="spatialVector">
                  <xsl:if test="position()=$entityindex">
                         <xsl:choose>
                           <xsl:when test="references!=''">
                              <xsl:variable name="ref_id" select="references"/>
                              <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                                <xsl:for-each select="$references">
                                    <xsl:choose>
                                       <xsl:when test="$displaymodule='entity' or $displaymodule='printall'">
                                          <xsl:call-template name="spatialVector">
                                             <xsl:with-param name="spatialvectorfirstColStyle" select="$firstColStyle"/>
                                              <xsl:with-param name="spatialvectorsubHeaderStyle" select="$subHeaderStyle"/>
                                              <xsl:with-param name="docid" select="$docid"/>
                                              <xsl:with-param name="entitytype" select="$entitytype"/>
                                              <xsl:with-param name="entityindex" select="$entityindex"/>
                                          </xsl:call-template>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:call-template name="chooseattributelist"/>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                           </xsl:when>
                           <xsl:otherwise>
                             <xsl:choose>
                                       <xsl:when test="$displaymodule='entity' or $displaymodule='printall'">
                                          <xsl:call-template name="spatialVector">
                                              <xsl:with-param name="spatialvectorfirstColStyle" select="$firstColStyle"/>
                                              <xsl:with-param name="spatialvectorsubHeaderStyle" select="$subHeaderStyle"/>
                                              <xsl:with-param name="docid" select="$docid"/>
                                              <xsl:with-param name="entitytype" select="$entitytype"/>
                                              <xsl:with-param name="entityindex" select="$entityindex"/>
                                          </xsl:call-template>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:call-template name="chooseattributelist"/>
                                       </xsl:otherwise>
                             </xsl:choose>
                         </xsl:otherwise>
                      </xsl:choose>
                  </xsl:if>
              </xsl:for-each>
            </xsl:if>
            <xsl:if test="$entitytype='storedProcedure'">
              <xsl:for-each select="storedProcedure">
                  <xsl:if test="position()=$entityindex">
                         <xsl:choose>
                           <xsl:when test="references!=''">
                              <xsl:variable name="ref_id" select="references"/>
                              <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                                <xsl:for-each select="$references">
                                    <xsl:choose>
                                       <xsl:when test="$displaymodule='entity' or $displaymodule='printall'">
                                          <xsl:call-template name="storedProcedure">
                                             <xsl:with-param name="storedprocedurefirstColStyle" select="$firstColStyle"/>
                                             <xsl:with-param name="storedproceduresubHeaderStyle" select="$subHeaderStyle"/>
                                             <xsl:with-param name="docid" select="$docid"/>
                                             <xsl:with-param name="entitytype" select="$entitytype"/>
                                             <xsl:with-param name="entityindex" select="$entityindex"/>
                                          </xsl:call-template>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:call-template name="chooseattributelist"/>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                           </xsl:when>
                           <xsl:otherwise>
                             <xsl:choose>
                                       <xsl:when test="$displaymodule='entity' or $displaymodule='printall'">
                                          <xsl:call-template name="storedProcedure">
                                             <xsl:with-param name="storedprocedurefirstColStyle" select="$firstColStyle"/>
                                             <xsl:with-param name="storedproceduresubHeaderStyle" select="$subHeaderStyle"/>
                                             <xsl:with-param name="docid" select="$docid"/>
                                             <xsl:with-param name="entitytype" select="$entitytype"/>
                                             <xsl:with-param name="entityindex" select="$entityindex"/>
                                          </xsl:call-template>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:call-template name="chooseattributelist"/>
                                       </xsl:otherwise>
                             </xsl:choose>
                         </xsl:otherwise>
                      </xsl:choose>
                  </xsl:if>
              </xsl:for-each>
            </xsl:if>
            <xsl:if test="$entitytype='view'">
              <xsl:for-each select="view">
                  <xsl:if test="position()=$entityindex">
                         <xsl:choose>
                           <xsl:when test="references!=''">
                              <xsl:variable name="ref_id" select="references"/>
                              <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                                <xsl:for-each select="$references">
                                    <xsl:choose>
                                       <xsl:when test="$displaymodule='entity' or $displaymodule='printall'">
                                          <xsl:call-template name="view">
                                             <xsl:with-param name="viewfirstColStyle" select="$firstColStyle"/>
                                             <xsl:with-param name="viewsubHeaderStyle" select="$subHeaderStyle"/>
                                             <xsl:with-param name="docid" select="$docid"/>
                                             <xsl:with-param name="entitytype" select="$entitytype"/>
                                             <xsl:with-param name="entityindex" select="$entityindex"/>
                                          </xsl:call-template>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:call-template name="chooseattributelist"/>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                           </xsl:when>
                           <xsl:otherwise>
                             <xsl:choose>
                                       <xsl:when test="$displaymodule='entity' or $displaymodule='printall'">
                                          <xsl:call-template name="view">
                                             <xsl:with-param name="viewfirstColStyle" select="$firstColStyle"/>
                                             <xsl:with-param name="viewsubHeaderStyle" select="$subHeaderStyle"/>
                                             <xsl:with-param name="docid" select="$docid"/>
                                             <xsl:with-param name="entitytype" select="$entitytype"/>
                                             <xsl:with-param name="entityindex" select="$entityindex"/>
                                          </xsl:call-template>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:call-template name="chooseattributelist"/>
                                       </xsl:otherwise>
                             </xsl:choose>
                         </xsl:otherwise>
                      </xsl:choose>
                  </xsl:if>
              </xsl:for-each>
            </xsl:if>
            <xsl:if test="$entitytype='otherEntity'">
              <xsl:for-each select="otherEntity">
                  <xsl:if test="position()=$entityindex">
                         <xsl:choose>
                           <xsl:when test="references!=''">
                              <xsl:variable name="ref_id" select="references"/>
                              <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                                <xsl:for-each select="$references">
                                    <xsl:choose>
                                       <xsl:when test="$displaymodule='entity' or $displaymodule='printall'">
                                          <xsl:call-template name="otherEntity">
                                             <xsl:with-param name="otherentityfirstColStyle" select="$firstColStyle"/>
                                             <xsl:with-param name="otherentitysubHeaderStyle" select="$subHeaderStyle"/>
                                             <xsl:with-param name="docid" select="$docid"/>
                                             <xsl:with-param name="entitytype" select="$entitytype"/>
                                             <xsl:with-param name="entityindex" select="$entityindex"/>
                                          </xsl:call-template>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:call-template name="chooseattributelist"/>
                                       </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                           </xsl:when>
                           <xsl:otherwise>
                             <xsl:choose>
                                       <xsl:when test="$displaymodule='entity' or $displaymodule='printall'">
                                          <xsl:call-template name="otherEntity">
                                             <xsl:with-param name="otherentityfirstColStyle" select="$firstColStyle"/>
                                             <xsl:with-param name="otherentitysubHeaderStyle" select="$subHeaderStyle"/>
                                             <xsl:with-param name="docid" select="$docid"/>
                                             <xsl:with-param name="entitytype" select="$entitytype"/>
                                             <xsl:with-param name="entityindex" select="$entityindex"/>
                                          </xsl:call-template>
                                       </xsl:when>
                                       <xsl:otherwise>
                                          <xsl:call-template name="chooseattributelist"/>
                                       </xsl:otherwise>
                             </xsl:choose>
                         </xsl:otherwise>
                      </xsl:choose>
                  </xsl:if>
              </xsl:for-each>
            </xsl:if>
   </xsl:template>

   <xsl:template name="chooseattributelist">
       <xsl:for-each select="attributeList">
          <xsl:choose>
               <xsl:when test="references!=''">
                  <xsl:variable name="ref_id" select="references"/>
                  <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                  <xsl:for-each select="$references">
                     <xsl:call-template name="chooseattribute"/>
                 </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                   <xsl:call-template name="chooseattribute"/>
              </xsl:otherwise>
         </xsl:choose>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="chooseattribute">
       <xsl:for-each select="attribute">
          <xsl:if test="position()=$attributeindex">
            <xsl:if test="$displaymodule='attributedomain'">
              <xsl:for-each select="measurementScale/*/*">
                <xsl:call-template name="nonNumericDomain">
                    <xsl:with-param name="displaymodule" select="$displaymodule"/>
                    <xsl:with-param name="entitytype" select="$entitytype"/>
                    <xsl:with-param name="entityindex" select="$entityindex"/>
                    <xsl:with-param name="attributeindex" select="$attributeindex"/>
                    <xsl:with-param name="nondomainfirstColStyle" select="$firstColStyle"/>
                 </xsl:call-template>
              </xsl:for-each>
           </xsl:if>
           <xsl:if test="$displaymodule='attributecoverage'">
              <xsl:for-each select="coverage">
                <xsl:call-template name="coverage">
                </xsl:call-template>
              </xsl:for-each>
           </xsl:if>
           <xsl:if test="$displaymodule='attributemethod'">
              <xsl:for-each select="method">
                <xsl:call-template name="method">
                    <xsl:with-param name="methodfirstColStyle" select="$firstColStyle"/>
                    <xsl:with-param name="methodsubHeaderStyle" select="$firstColStyle"/>
                 </xsl:call-template>
              </xsl:for-each>
           </xsl:if>
         </xsl:if>
       </xsl:for-each>
   </xsl:template>



   <!--*************************Distribution Inline Data display module*****************-->
   <xsl:template name="emlinlinedata">
      <tr><td>
            <h3>Inline Data</h3>
      </td></tr>
      <tr>
           <td width="100%">
            <xsl:if test="$distributionlevel='toplevel'">
               <xsl:for-each select="distribution">
                  <xsl:if test="position()=$distributionindex">
                     <xsl:choose>
                       <xsl:when test="references!=''">
                          <xsl:variable name="ref_id1" select="references"/>
                          <xsl:variable name="references1" select="$ids[@id=$ref_id1]" />
                          <xsl:for-each select="$references1">
                              <xsl:for-each select="inline">
				   <pre><xsl:value-of select="."/></pre>
                              </xsl:for-each>
                          </xsl:for-each>
                       </xsl:when>
                       <xsl:otherwise>
                           <xsl:for-each select="inline">
				   <pre><xsl:value-of select="."/></pre>
                           </xsl:for-each>
                       </xsl:otherwise>
                     </xsl:choose>
                  </xsl:if>
               </xsl:for-each>
            </xsl:if>
            <xsl:if test="$distributionlevel='entitylevel'">
              <xsl:if test="$entitytype='dataTable'">
                <xsl:for-each select="dataTable">
                  <xsl:if test="position()=$entityindex">
                      <xsl:choose>
                       <xsl:when test="references!=''">
                          <xsl:variable name="ref_id2" select="references"/>
                          <xsl:variable name="references2" select="$ids[@id=$ref_id2]" />
                          <xsl:for-each select="$references2">
                             <xsl:call-template name="choosephysical"/>
                          </xsl:for-each>
                       </xsl:when>
                       <xsl:otherwise>
                           <xsl:call-template name="choosephysical"/>
                       </xsl:otherwise>
                     </xsl:choose>
                  </xsl:if>
                </xsl:for-each>
              </xsl:if>
              <xsl:if test="$entitytype='spatialRaster'">
                <xsl:for-each select="spatialRaster">
                  <xsl:if test="position()=$entityindex">
                      <xsl:choose>
                       <xsl:when test="references!=''">
                          <xsl:variable name="ref_id2" select="references"/>
                          <xsl:variable name="references2" select="$ids[@id=$ref_id2]" />
                          <xsl:for-each select="$references2">
                             <xsl:call-template name="choosephysical"/>
                          </xsl:for-each>
                       </xsl:when>
                       <xsl:otherwise>
                           <xsl:call-template name="choosephysical"/>
                       </xsl:otherwise>
                     </xsl:choose>
                  </xsl:if>
                </xsl:for-each>
              </xsl:if>
              <xsl:if test="$entitytype='spatialVector'">
                <xsl:for-each select="spatialVector">
                  <xsl:if test="position()=$entityindex">
                      <xsl:choose>
                       <xsl:when test="references!=''">
                          <xsl:variable name="ref_id2" select="references"/>
                          <xsl:variable name="references2" select="$ids[@id=$ref_id2]" />
                          <xsl:for-each select="$references2">
                             <xsl:call-template name="choosephysical"/>
                          </xsl:for-each>
                       </xsl:when>
                       <xsl:otherwise>
                           <xsl:call-template name="choosephysical"/>
                       </xsl:otherwise>
                     </xsl:choose>
                  </xsl:if>
                </xsl:for-each>
              </xsl:if>
              <xsl:if test="$entitytype='storedProcedure'">
                <xsl:for-each select="storedProcedure">
                  <xsl:if test="position()=$entityindex">
                      <xsl:choose>
                       <xsl:when test="references!=''">
                          <xsl:variable name="ref_id2" select="references"/>
                          <xsl:variable name="references2" select="$ids[@id=$ref_id2]" />
                          <xsl:for-each select="$references2">
                             <xsl:call-template name="choosephysical"/>
                          </xsl:for-each>
                       </xsl:when>
                       <xsl:otherwise>
                           <xsl:call-template name="choosephysical"/>
                       </xsl:otherwise>
                     </xsl:choose>
                  </xsl:if>
                </xsl:for-each>
              </xsl:if>
              <xsl:if test="$entitytype='view'">
                <xsl:for-each select="view">
                  <xsl:if test="position()=$entityindex">
                      <xsl:choose>
                       <xsl:when test="references!=''">
                          <xsl:variable name="ref_id2" select="references"/>
                          <xsl:variable name="references2" select="$ids[@id=$ref_id2]" />
                          <xsl:for-each select="$references2">
                             <xsl:call-template name="choosephysical"/>
                          </xsl:for-each>
                       </xsl:when>
                       <xsl:otherwise>
                           <xsl:call-template name="choosephysical"/>
                       </xsl:otherwise>
                     </xsl:choose>
                  </xsl:if>
                </xsl:for-each>
              </xsl:if>
              <xsl:if test="$entitytype='otherEntity'">
                <xsl:for-each select="otherEntity">
                  <xsl:if test="position()=$entityindex">
                      <xsl:choose>
                       <xsl:when test="references!=''">
                          <xsl:variable name="ref_id2" select="references"/>
                          <xsl:variable name="references2" select="$ids[@id=$ref_id2]" />
                          <xsl:for-each select="$references2">
                             <xsl:call-template name="choosephysical"/>
                          </xsl:for-each>
                       </xsl:when>
                       <xsl:otherwise>
                           <xsl:call-template name="choosephysical"/>
                       </xsl:otherwise>
                     </xsl:choose>
                  </xsl:if>
                </xsl:for-each>
              </xsl:if>
            </xsl:if>
          </td>
      </tr>
   </xsl:template>

   <xsl:template name="choosephysical">
      <xsl:for-each select="physical">
         <xsl:if test="position()=$physicalindex">
            <xsl:choose>
               <xsl:when test="references!=''">
                  <xsl:variable name="ref_id" select="references"/>
                  <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                  <xsl:for-each select="$references">
                     <xsl:call-template name="choosedistribution"/>
                  </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:call-template name="choosedistribution"/>
              </xsl:otherwise>
           </xsl:choose>
        </xsl:if>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="choosedistribution">
      <xsl:for-each select="distribution">
         <xsl:if test="$distributionindex=position()">
            <xsl:choose>
               <xsl:when test="references!=''">
                  <xsl:variable name="ref_id" select="references"/>
                  <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                  <xsl:for-each select="$references">
                     <xsl:for-each select="inline">
                        <pre><xsl:value-of select="."/></pre>
                      </xsl:for-each>
                  </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:for-each select="inline">
                    <pre><xsl:value-of select="."/></pre>
                  </xsl:for-each>
              </xsl:otherwise>
           </xsl:choose>
        </xsl:if>
      </xsl:for-each>
   </xsl:template>


     <!--********************************************************
                     Citation part
       ********************************************************-->
   <xsl:template name="emlcitation">
     <xsl:choose>
       <xsl:when test="$displaymodule='inlinedata'">
          <xsl:call-template name="emlinlinedata"/>
       </xsl:when>
       <xsl:otherwise>
        <table xsl:use-attribute-sets="cellspacing" class="{$tabledefaultStyle}" width="100%">
        <tr>
          <td colspan="2">
               <h3>Citation Description</h3>
          </td>
        </tr>
        <xsl:call-template name="identifier">
              <xsl:with-param name="packageID" select="../@packageId"/>
              <xsl:with-param name="system" select="../@system"/>
        </xsl:call-template>
        <tr>
          <td colspan="2">
            <xsl:call-template name="citation">
               <xsl:with-param name="citationfirstColStyle" select="$firstColStyle"/>
               <xsl:with-param name="citationsubHeaderStyle" select="$subHeaderStyle"/>
           </xsl:call-template>
          </td>
        </tr>
      </table>
     </xsl:otherwise>
    </xsl:choose>
   </xsl:template>



     <!--********************************************************
                    Software part
       ********************************************************-->

   <xsl:template name="emlsoftware">
     <xsl:choose>
       <xsl:when test="$displaymodule='inlinedata'">
          <xsl:call-template name="emlinlinedata"/>
       </xsl:when>
       <xsl:otherwise>
          <table xsl:use-attribute-sets="cellspacing" class="{$tabledefaultStyle}" width="100%">
          <tr>
            <td colspan="2">
               <h3>Software Description</h3>
           </td>
          </tr>
          <xsl:call-template name="identifier">
              <xsl:with-param name="packageID" select="../@packageId"/>
              <xsl:with-param name="system" select="../@system"/>
          </xsl:call-template>
          <tr>
           <td colspan="2">
            <xsl:call-template name="software">
               <xsl:with-param name="softwarefirstColStyle" select="$firstColStyle"/>
               <xsl:with-param name="softwaresubHeaderStyle" select="$subHeaderStyle"/>
           </xsl:call-template>
           </td>
         </tr>
       </table>
      </xsl:otherwise>
     </xsl:choose>
   </xsl:template>


     <!--********************************************************
                    Protocal part
       ********************************************************-->

   <xsl:template name="emlprotocol">
    <xsl:choose>
       <xsl:when test="$displaymodule='inlinedata'">
          <xsl:call-template name="emlinlinedata"/>
       </xsl:when>
       <xsl:otherwise>
        <table xsl:use-attribute-sets="cellspacing" class="{$tabledefaultStyle}" width="100%">
         <tr>
          <td colspan="2">
               <h3>Protocal Description</h3>
          </td>
         </tr>
         <xsl:call-template name="identifier">
              <xsl:with-param name="packageID" select="../@packageId"/>
              <xsl:with-param name="system" select="../@system"/>
         </xsl:call-template>
         <tr>
          <td colspan="2">
            <xsl:call-template name="protocol">
               <xsl:with-param name="protocolfirstColStyle" select="$firstColStyle"/>
               <xsl:with-param name="protocolsubHeaderStyle" select="$subHeaderStyle"/>
           </xsl:call-template>
          </td>
         </tr>
       </table>
      </xsl:otherwise>
    </xsl:choose>
   </xsl:template>

      <!--********************************************************
                   additionalmetadata part
       ********************************************************-->
   <xsl:template name="additionalmetadataURL">
     <xsl:param name="additionalmetadataindex"/>
<table  class="defaulttable" width="100%"
 ><xsl:if test="$additionalmetadataindex=1">
 <tr><td  class="tablehead" colspan="2">Additional Metadata</td></tr></xsl:if>
<tr><td>
<table xsl:use-attribute-sets="cellspacing" class="{$tabledefaultStyle}" width="100%">
     <tr><td width="{$firstColWidth}" class="highlight">Additional Metadata:</td><td width="{$secondColWidth}" class="secondCol">
                     <xsl:text>Item </xsl:text> <xsl:value-of select="$additionalmetadataindex"/><xsl:text>&#160;</xsl:text> <a><xsl:attribute name="href"><xsl:value-of select="$tripleURI"/><xsl:value-of select="$docid"/>&amp;displaymodule=additionalmetadata&amp;additionalmetadataindex=<xsl:value-of select="$additionalmetadataindex"/></xsl:attribute>
              <b>(View)</b></a>
           </td>
       </tr>
</table>
</td></tr>
</table>
   </xsl:template>

     <!--********************************************************
                   download xml part
       ********************************************************-->
        <xsl:template name="xml">
     <xsl:param name="index"/>
<table   width="100%">
        <tr>
           <td  width="{$firstColWidth}" class="highlight" >Metadata download:</td>
           <td  width="{$secondColWidth}" class="secondCol"><a><xsl:attribute name="href"><xsl:value-of select="$xmlURI"/><xsl:value-of select="$docid"/></xsl:attribute>
              <b>Ecological Metadata Language (EML) File</b></a>
           </td>
       </tr>
       </table>
   </xsl:template>
</xsl:stylesheet>
