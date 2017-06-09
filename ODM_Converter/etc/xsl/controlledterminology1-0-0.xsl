<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:odm="http://www.cdisc.org/ns/odm/v1.3"
    xmlns:nciodm="http://ncicb.nci.nih.gov/xml/odm/EVS/CDISC"
    xmlns:xalan="http://xml.apache.org/xalan"
    exclude-result-prefixes="odm nciodm xalan">

    <xsl:output method="html" indent="yes" xalan:indent-amount="2" 
                encoding="ISO-8859-1" 
                doctype-public="-//W3C//DTD HTML 4.01//EN" doctype-system="http://www.w3.org/TR/html4/strict.dtd"/>
    
    <xsl:key name="codelist" match="odm:CodeList" use="@OID"/>
    
    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="/odm:ODM/odm:Study/odm:GlobalVariables/odm:StudyName"/></title>
                <style type="text/css">
body {
    font-family: Arial, Helvetica, sans-serif;
    font-style:  normal;
    font-weight: normal;
    font-size:   14px;
    margin: 0;
    padding: 10px;
}

h1, #study_description {
    text-align: center;
}

table {
    border-collapse: collapse;
    margin: 10px 25px;
    border: 2px solid black;
}

td {
    vertical-align: top;
    padding: 3px 4px;
    border:  1px solid #dddddd;
}

thead tr th {
    vertical-align: top;
    padding: 3px 4px;
    border:  2px solid black;
}

.header {
    font-weight:bold;
    white-space: nowrap;
}

.header td {
    text-align: center;
    vertical-align: middle;
}

tr.item_last td {
    border-bottom: solid 1px black;
}

.cl {
    background: #ccffff;
}

.cli {
    background: white;
}

.nci {
    background: #ffffcc;
}

.pre {
    white-space: pre-wrap;
}

#legend {
    position: absolute;
    top: 10px;
    right: 10px;
}

#toc {
float:left;
left:5px;
width:300px;
}
#contents {
position: absolute;
left:300px;
margin-right:10px;
}


tr.codelist td {
    border-top: solid 1px black;
}
                </style>
            </head>
            <body>
                <a id="top" />
                <h1><xsl:value-of select="/odm:ODM/odm:Study/odm:GlobalVariables/odm:StudyName"/></h1>
                <div id="study_description"><xsl:value-of select="/odm:ODM/odm:Study/odm:GlobalVariables/odm:StudyDescription"/></div>
                
                <table id="legend">
                    <caption>Legend:</caption>
                    <tr><td class="cl">CodeList</td></tr>
                    <tr><td class="cli">CodeListItem</td></tr>
                    <tr><td class="nci">NCI attributes</td></tr>
                </table>

              <div id="toc">
                <table>
                  <xsl:for-each select="//odm:CodeList">
                    <tr>
                      <td><xsl:value-of select="@nciodm:ExtCodeID"/></td>
                      <td><a href="#{@OID}"><xsl:value-of select="@Name"/></a></td>
                    </tr>
                  </xsl:for-each> 
                </table>
              </div>

              <div id="contents">
                <h2>Codelist Definitions</h2>

                <table><thead>
                    <tr class="header cl">
                        <th>OID</th>
                        <th>Name<br />(CDISC Submission Value)</th>
                        <th>DataType<br />Extensible</th>
                        <th rowspan="2" class="nci">NCI Code</th>
                        <th rowspan="2" class="nci">CDISC Synonym</th>
                        <th rowspan="2" class="nci">CDISC Definition</th>
                        <th rowspan="2" class="nci">Preferred Term</th>
                    </tr>
                    <tr class="header cli">
                        <th/>
                        <th colspan="2">CDISC Submission Value [ODM:CodedValue]</th>
                        <!--  <th>Decode</th> -->
                    </tr>
                    </thead>
                    <tbody>
                    <xsl:apply-templates select="//odm:CodeList"/>
                    </tbody>
                </table>
              </div>  
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match="odm:CodeList">
        <tr class="cl">
            <td>
                <a name="{@OID}"><xsl:value-of select="@OID"/></a>
            </td>
            <td><xsl:value-of select="@Name"/><br />(<xsl:value-of select="nciodm:CDISCSubmissionValue"/>)</td>
            <td nowrap="nowrap"><xsl:value-of select="@DataType"/><br />Extensible: <xsl:value-of select="@nciodm:CodeListExtensible"/></td>
            <td><xsl:value-of select="@nciodm:ExtCodeID"/></td>
            <td>
            <xsl:for-each select="nciodm:CDISCSynonym">            
                <xsl:value-of select="text()"/>
                <xsl:if test="position() != last()">;</xsl:if>
            </xsl:for-each>                
            </td>
            
            <td><xsl:value-of select="odm:Description/odm:TranslatedText[@xml:lang='en']"/></td>
            <td><xsl:value-of select="nciodm:PreferredTerm"/></td>
        </tr>
        
        <xsl:for-each select="odm:CodeListItem|odm:EnumeratedItem">
            <tr class="cli">
                <td/>
                <td colspan="2"><xsl:value-of select="@CodedValue"/></td>
                <!--  <td><xsl:value-of select="odm:Decode/odm:TranslatedText[@xml:lang='en']"/></td> -->
                <td class="nci"><xsl:value-of select="@nciodm:ExtCodeID"/></td>
                <td class="nci">
                <xsl:for-each select="nciodm:CDISCSynonym">            
                    <xsl:value-of select="text()"/>
                    <xsl:if test="position() != last()">;</xsl:if>
                </xsl:for-each>                
                </td>
                <td class="nci"><xsl:value-of select="nciodm:CDISCDefinition"/></td>
                <td class="nci"><xsl:value-of select="nciodm:PreferredTerm"/></td>
            </tr>
        </xsl:for-each>
        
        <tr><td colspan="7"><a href="#top">Back to top</a></td></tr>

    </xsl:template>
</xsl:stylesheet>

