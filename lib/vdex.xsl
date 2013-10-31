<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		
		<html>
			<head>
				<title>vocab</title>
			</head>
			<body>
				<div>
					<table width="100%">
						<tr>
							<td>
								<b><xsl:value-of select="vdex/vocabName/langstring" /></b>
								<br />
								<xsl:value-of select="vdex/vocabIdentifier" />
							</td>
						</tr>
						<tr>
							<td>
								<xsl:for-each select="vdex/term">
									<xsl:call-template name="term" />
								</xsl:for-each>
							</td>
						</tr>
					</table>
				</div>
			</body>
		</html>
	</xsl:template>


	<!--********************************************************
		term part
		********************************************************-->
	<xsl:template name="term">
		<ul>
			<li>
				<b><xsl:value-of select="termIdentifier" /></b>
				<xsl:if test="description/langstring != ''">
					-
					<xsl:value-of select="description/langstring" />
				</xsl:if>
				<xsl:for-each select="term">
					<xsl:call-template name="term" />
				</xsl:for-each>
			</li>
		</ul>

	</xsl:template>

</xsl:stylesheet>
