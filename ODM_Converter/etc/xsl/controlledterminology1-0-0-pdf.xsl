<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:xlink="http://www.w3.org/1999/xlink" xml:lang="en"
  xmlns:nciodm="http://ncicb.nci.nih.gov/xml/odm/EVS/CDISC"
  exclude-result-prefixes="xlink odm xsi nciodm">
  <xsl:output method="xml" indent="yes" encoding="UTF-8"
    omit-xml-declaration="yes"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" 
    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
    version="4.0" 
    />
    
  <!--
			Global Variables
		-->
  <xsl:variable name="g_xndMetaDataVersion" select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion[1]"/>
  <xsl:variable name="g_seqItemGroupDefs" select="$g_xndMetaDataVersion/odm:ItemGroupDef"/>
  <xsl:variable name="g_seqItemDefs" select="$g_xndMetaDataVersion/odm:ItemDef"/>
  <xsl:variable name="g_seqCodeLists" select="$g_xndMetaDataVersion/odm:CodeList"/>

    <xsl:key name="codelist" match="odm:CodeList" use="@OID"/>
    
    <xsl:template match="/">
        <html>
          <head>
            <meta http-equiv="Content-Script-Type" content="text/javascript" />
            <meta http-equiv="Content-Style-Type" content="text/css" />
            <title><xsl:value-of select="/odm:ODM/odm:Study/odm:GlobalVariables/odm:StudyName"/></title>
            <style type="text/css">
body {
    font-family:Verdana, Arial, Helvetica, sans-serif; 
    font-style:  normal;
    font-weight: normal;
    font-size:12px;
    margin: 0;
    padding: 10px;
}

a {
  text-decoration:none;
}

h1 {
    text-align: left;
    font-size:14px;
}
h2 {
		text-align: left;
		font-size:12px;
}

.h3 {
		text-align: left;
		font-size:10px;
}

table {
    border-collapse: collapse;
    margin-top: 10px;
    border: 0.5px black;
    repeat-header:yes;
    repeat-footer:yes;
    width:100%;
}

th {
    vertical-align: top;
    background: #dddddd;
    font-size:9px;
    padding-left:5px;
}

td {
    vertical-align: top;
    border:  0px;
    font-size:9px;
    padding-left:5px;
}

#contents {
position: absolute;
left:5px;
margin-right:5px;
}

</style>
  </head>
  <body>
      <h1><xsl:value-of select="/odm:ODM/odm:Study/odm:GlobalVariables/odm:StudyDescription"/></h1>
      <div class="h3">Source: NCI EVS Terminology Resources website: http://www.cancer.gov/cancertopics/cancerlibrary/terminologyresources/cdisc</div>
      
    <table style="page-break-after: always; repeat-header:yes">
        <thead>
          <tr>
            <th style="width:0.55in;">NCI Code</th>
            <th style="width:1.40in;">CDISC Submission Value</th>
            <th style="width:1.20in;">Codelist Name</th>
            <th>CDISC Definition</th>
            <th style="width:0.65in;">Codelist Extensible</th>
          </tr>
        </thead>
        <tbody>
    <xsl:for-each select="$g_seqCodeLists">
        <xsl:sort order="ascending" select="nciodm:CDISCSubmissionValue"/>
        <xsl:call-template name="CodeLists"/>
    </xsl:for-each>
    </tbody>
    </table>

    <xsl:for-each select="$g_seqCodeLists">
        <xsl:sort order="ascending" select="nciodm:CDISCSubmissionValue"/>
        <xsl:call-template name="CodeListItems"/>
    </xsl:for-each>
    <!-- <xsl:apply-templates select="//odm:CodeList"/> -->
  </body>
        </html>
    </xsl:template>
    
    <xsl:template name="CodeListItems">

      <a name="{@OID}" />
      <h2 name="{@OID}"><xsl:value-of select="nciodm:CDISCSubmissionValue"/> (<xsl:value-of select="@Name"/>)</h2>
    	<div class="h3">NCI Code: <xsl:value-of select="@nciodm:ExtCodeID"/>, Codelist extensible: <xsl:value-of select="@nciodm:CodeListExtensible"/></div>
    	
      <table style="page-break-after: always; repeat-header:yes;">
        <thead>
        	<tr style="background-color:white; border:0;">
          	<th style="background-color:white; border:0;"><xsl:value-of select="@nciodm:ExtCodeID"/></th>
          	<th style="background-color:white; border:0;"><xsl:value-of select="nciodm:CDISCSubmissionValue"/></th>
        		<th style="background-color:white; border:0;"></th>
        		<th style="background-color:white; border:0;"></th>
        		<th style="background-color:white; border:0;"></th>
          </tr>
          <tr>
              <th style="width:0.55in;">NCI Code</th>
              <th style="width:1.40in;">CDISC Submission Value</th>
              <th style="width:1.40in;">CDISC Synonym</th>
              <th>CDISC Definition</th>
              <th style="width:1.40in;">NCI Preferred Term</th>
          </tr>
        </thead>
        <tbody>

        <xsl:for-each select="odm:CodeListItem|odm:EnumeratedItem">
          <tr>
            <td><xsl:value-of select="@nciodm:ExtCodeID"/></td>
            <td><xsl:value-of select="@CodedValue"/></td>
            <td>
              <xsl:for-each select="nciodm:CDISCSynonym">            
                  <xsl:value-of select="text()"/>
                  <xsl:if test="position() != last()">;</xsl:if>
              </xsl:for-each>                
            </td>
            <td><xsl:value-of select="nciodm:CDISCDefinition"/></td>
            <td><xsl:value-of select="nciodm:PreferredTerm"/></td>
          </tr>
        </xsl:for-each>
        
                    </tbody>
                </table>
    </xsl:template>


  <!-- ***************************************** -->
  <!-- Code List Items                           -->
  <!-- ***************************************** -->
  <xsl:template name="CodeLists">

    <tr>  
      <td><xsl:value-of select="@nciodm:ExtCodeID"/></td>
      <td><a class="codelist" href="#{@OID}"><xsl:value-of select="nciodm:CDISCSubmissionValue"/></a></td>
      <td><xsl:value-of select="@Name"/></td>
      <td><xsl:value-of select="odm:Description/odm:TranslatedText[@xml:lang='en']"/></td>
      <td><xsl:value-of select="@nciodm:CodeListExtensible"/></td>                    
    </tr>

  </xsl:template>

</xsl:stylesheet>

