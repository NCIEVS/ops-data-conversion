/*
	Copyright 2011 Andrew Fowler <andrew.fowler@devframe.com>
	
	This file is part of Terinology2ODM Terminology2ODMConverter.
	
	Terminology2ODMConverter is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	
	Terminology2ODMConverter is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with Terminology2ODMConverter.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.SAXReader
import org.dom4j.Namespace
import org.dom4j.QName

import util.TerminologyExcelReader
import util.ODMWriter

import model._

object TerminologyExcel2ODM {
  
  def main(args: Array[String]) {
    try {
      run(args)
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
  
  def run(args: Array[String]) {
    val infile = new File(args(0))
    val outfile = new File(args(1))
  
    val terminologyReader = new TerminologyExcelReader()
    terminologyReader.read(infile)
    
    val odm = new ODM()
    val study = new Study()
    val mdv = new MDV()
    
    odm.study = study 
    odm.study.mdvs += mdv
    
    val dateStamp = terminologyReader.terminologyDate
    val terminologyType = terminologyReader.terminologyType
    val terminologyModel = terminologyReader.terminologyModel
    val terminologyShortModel = terminologyReader.terminologyShortModel
    
    odm.creationDateTime = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
    odm.asOfDateTime = dateStamp + "T00:00:00"
    odm.originator = "CDISC XML Technologies Team (Terminology2ODM converter)" 
    odm.sourceSystem = "NCI Thesaurus"
    odm.sourceSystemVersion = dateStamp
    odm.fileOID     = "CDISC_CT." + terminologyModel + "." + dateStamp
    
    study.oid = "CDISC_CT." + terminologyModel + "." + dateStamp
    study.globalVarsStudyName = "CDISC " + terminologyModel + " " + terminologyType
    study.globalVarsStudyDescription = "CDISC " + terminologyModel + " " + terminologyType + ", " + dateStamp
    study.globalVarsStudyProtocolName = "CDISC " + terminologyModel + " " + terminologyType
    
    mdv.oid = "CDISC_CT_MetaDataVersion." + terminologyShortModel + "." + dateStamp
    mdv.name = "CDISC " + terminologyModel + " " + terminologyType
    mdv.description = "CDISC " + terminologyModel + " " + terminologyType + ", " + dateStamp
    
    var codelists = terminologyReader.codelists
    
    /*
    // SORTING??? codelists
    codelists = codelists.sortWith((cl1, cl2) => 
      cl1.extras("terminology:submission_value") + "." + cl1.extras("terminology:code") <
      cl2.extras("terminology:submission_value") + "." + cl2.extras("terminology:code")
    )
    */

    // Convert loaded codelists to chosen odm form (enumerated codelists), and convert extensions
    codelists.foreach { termCodelist =>
      
        val oid = "CL." + termCodelist.extras("terminology:code") + "." + termCodelist.extras("terminology:submission_value")
        val name = termCodelist.extras("terminology:name")
        val datatype = "text"
        
        val odmCodelist = new CodeList(oid, name, datatype)
        
        odmCodelist.description = termCodelist.extras("terminology:definition")
        
        odmCodelist.extendedAttributes += new Extension("nciodm:ExtCodeID", termCodelist.extras("terminology:code"))
        try {
        odmCodelist.extendedAttributes += new Extension("nciodm:CodeListExtensible", termCodelist.extras("terminology:extensible"))
        } catch {
        case e: Exception => e.printStackTrace()
        }
        
        odmCodelist.extendedElements += new Extension("nciodm:CDISCSubmissionValue", termCodelist.extras("terminology:submission_value"))
        
        convertSynonyms(odmCodelist, termCodelist)
        
        if (termCodelist.extras.contains("terminology:nci_preferred_term")) 
            odmCodelist.extendedElements += new Extension("nciodm:PreferredTerm", termCodelist.extras("terminology:nci_preferred_term"))
        
        mdv.codelists += odmCodelist
        
        
        var codelistItems = termCodelist.codelistItems
        
        /*
        // SORTING??? codelists items
        codelistItems = codelistItems.sortWith((cli1, cli2) => 
          cli1.extras("terminology:submission_value") < cli2.extras("terminology:submission_value")
        )
        */
        
        codelistItems.foreach { termCli =>
        
            val odmEnumi = new EnumeratedItem(termCli.extras("terminology:submission_value"))
            odmEnumi.extendedAttributes += new Extension("nciodm:ExtCodeID", termCli.extras("terminology:code"))
            
            convertSynonyms(odmEnumi, termCli)
            
            odmEnumi.extendedElements += new Extension("nciodm:CDISCDefinition", termCli.extras("terminology:definition"))
            odmEnumi.extendedElements += new Extension("nciodm:PreferredTerm", termCli.extras("terminology:nci_preferred_term"))
            
            odmCodelist.enumeratedItems += odmEnumi
        }
    }
    
    checkDuplicateOids(study.munits.asInstanceOf[ArrayBuffer[OIDEntity]])
    checkDuplicateOids(mdv.eventDefs.asInstanceOf[ArrayBuffer[OIDEntity]])
    checkDuplicateOids(mdv.formDefs.asInstanceOf[ArrayBuffer[OIDEntity]])
    checkDuplicateOids(mdv.igroupDefs.asInstanceOf[ArrayBuffer[OIDEntity]])
    checkDuplicateOids(mdv.itemDefs.asInstanceOf[ArrayBuffer[OIDEntity]])
    checkDuplicateOids(mdv.codelists.asInstanceOf[ArrayBuffer[OIDEntity]])
    
    val writer = new ODMWriter()
    writer.odm = odm
    writer.write(outfile)
  }
  
  def convertSynonyms(target: Extras, source: Extras) {
    if (source.extras.contains("terminology:synonyms")) {
        source.extras("terminology:synonyms").split(';').foreach { synonym =>
            target.extendedElements += new Extension("nciodm:CDISCSynonym", synonym.trim())
        }
    }
  }

  def checkDuplicateOids(definitions : ArrayBuffer[OIDEntity]) {
    val oids = new HashSet[String]()
    
    for (definition <- definitions) {
      if (oids.contains(definition.oid)) {
        sys.error("Duplicate oid '" + definition.oid + "'")
      }
      oids.add(definition.oid)
    }
  }
}

