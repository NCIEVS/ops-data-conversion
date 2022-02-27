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
package util

import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.dom4j.io.OutputFormat
import org.dom4j.io.XMLWriter
import org.dom4j.io.SAXReader
import org.dom4j.Namespace
import org.dom4j.QName
import org.dom4j.Attribute

import java.io.File
import java.io.FileWriter
import java.util.Date

import scala.collection.mutable.HashMap

import model._

class ODMWriter {
  
  val CDISC_NS  = "http://www.cdisc.org/ns/odm/v1.3"
  val NCI_ODM   = "http://ncicb.nci.nih.gov/xml/odm/EVS/CDISC"
  val XML_NS    = "http://www.w3.org/XML/1998/namespace"
  val XML_XSI   = "http://www.w3.org/2001/XMLSchema-instance"
  
  val SCHEMA_LOCATION = "http://www.nci.nih.gov/EVS/CDISC ../schema/controlledterminology1-0-0.xsd"
  
  var odm : ODM = null
  
  def write(file: File) {
    printf("Writing %s ... ", file)
    
    if (odm.creationDateTime == null) 
        odm.creationDateTime = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())

    val document = DocumentHelper.createDocument()
    
    // document.addProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"xsl/controlledterminology1-0-0.xsl\"")
    
    val odmElem = document.addElement("ODM", CDISC_NS)
    
    odmElem.addNamespace("",       CDISC_NS)
    odmElem.addNamespace("xs",     XML_XSI)
    odmElem.addNamespace("nciodm", NCI_ODM)
    
    //addAttribute(odmElem, "xs:schemaLocation", SCHEMA_LOCATION)
    addAttribute(odmElem, "FileType", "Snapshot")
    addAttribute(odmElem, "FileOID",    odm.fileOID)
    addAttribute(odmElem, "Granularity", "Metadata")
    addAttribute(odmElem, "CreationDateTime", odm.creationDateTime)
    addAttribute(odmElem, "AsOfDateTime", odm.asOfDateTime)
    addAttribute(odmElem, "ODMVersion", "1.3.2")
    addAttribute(odmElem, "Originator", odm.originator) 
    addAttribute(odmElem, "SourceSystem", odm.sourceSystem)
    addAttribute(odmElem, "SourceSystemVersion", odm.sourceSystemVersion)
    addAttribute(odmElem, "Description", odm.description)
    addAttribute(odmElem, "Originator", odm.originator)

    addAttribute(odmElem, "nciodm:ControlledTerminologyVersion", "1.2.0")

    if (odm.fileOID.indexOf("Protocol") != -1 || odm.fileOID.indexOf("Glossary") != -1) { 
        addAttribute(odmElem, "nciodm:Context", "Other")
    } else {
        addAttribute(odmElem, "nciodm:Context", "Submission")
    }
       
    val studyElem = odmElem.addElement("Study")
    addAttribute(studyElem, "OID", odm.study.oid)
    
    val globalVariablesElem = studyElem.addElement("GlobalVariables")
    addTextElement(globalVariablesElem, "StudyName", odm.study.globalVarsStudyName)
    addTextElement(globalVariablesElem, "StudyDescription", odm.study.globalVarsStudyDescription)
    addTextElement(globalVariablesElem, "ProtocolName", odm.study.globalVarsStudyProtocolName)
    
    if (!odm.study.munits.isEmpty) {
      val basicDefsElem = studyElem.addElement("BasicDefinitions")
      
      odm.study.munits.foreach { mu =>
        val muElem = basicDefsElem.addElement("MeasurementUnit")
        addAttribute(muElem, "OID",  mu.oid)
        addAttribute(muElem, "Name", mu.name)
        addTText(muElem, "Symbol", mu.symbol)
        addExtensions(muElem, mu)
      }
    }
    
    val mdv = odm.study.mdvs(0)
    
    val mdvElem = studyElem.addElement("MetaDataVersion")
    addAttribute(mdvElem, "OID",  mdv.oid)
    addAttribute(mdvElem, "Name", mdv.name)
    addAttribute(mdvElem, "Description", mdv.description)
             
    if (!mdv.eventDefs.isEmpty) {
      val protocolElem = mdvElem.addElement("Protocol")
                
      mdv.eventDefs.foreach { eventDef =>
        protocolElem.addElement("StudyEventRef")
                      .addAttribute("StudyEventOID", eventDef.oid)
                      .addAttribute("Mandatory",     "Yes")
      }
    }
             
    mdv.eventDefs.foreach { eventDef =>
      val eventDefElem = mdvElem.addElement("StudyEventDef")
      addAttribute(eventDefElem, "OID",       eventDef.oid)
      addAttribute(eventDefElem, "Name",      eventDef.name)
      addAttribute(eventDefElem, "Repeating", eventDef.repeating)
      addAttribute(eventDefElem, "Type",      eventDef.eventType)
                
      eventDef.references.foreach { ref =>
        val refElem = eventDefElem.addElement("FormRef")
        addAttribute(refElem, "FormOID",     ref.element.oid)
        addAttribute(refElem, "Mandatory",   "Yes")
      }
    }
                    
    mdv.formDefs.foreach { formDef =>
      val formDefElem = mdvElem.addElement("FormDef")
      addAttribute(formDefElem, "OID",       formDef.oid)
      addAttribute(formDefElem, "Name",      formDef.name)
      addAttribute(formDefElem, "Repeating", formDef.repeating)
                
      formDef.references.foreach { ref =>
        val refElem = formDefElem.addElement("ItemGroupRef")
        addAttribute(refElem, "ItemGroupOID",  ref.element.oid)
        addAttribute(refElem, "Mandatory",     "Yes")
      }
    }
    
    mdv.igroupDefs.foreach { igroupDef =>
      val igroupDefElem = mdvElem.addElement("ItemGroupDef")
      addAttribute(igroupDefElem, "OID",       igroupDef.oid)
      addAttribute(igroupDefElem, "Name",      igroupDef.name)
      addAttribute(igroupDefElem, "Repeating", igroupDef.repeating)
                
      igroupDef.references.foreach { ref =>
        val refElem = igroupDefElem.addElement("ItemRef")
        addAttribute(refElem, "ItemOID",       ref.element.oid)
        addAttribute(refElem, "Mandatory",     "Yes")
      }
    }
    
    mdv.itemDefs.foreach { itemDef =>
      val itemDefElem = mdvElem.addElement("ItemDef")
      addAttribute(itemDefElem, "OID",          itemDef.oid)
      addAttribute(itemDefElem, "Name",         itemDef.name)
      addAttribute(itemDefElem, "DataType",     itemDef.datatype)
                 
      if (itemDef.length > 0) itemDefElem.addAttribute("Length", itemDef.length.toString())
      
      addTText(itemDefElem, "Description", itemDef.description)
      addTText(itemDefElem, "Question", itemDef.question)
      
      if (!itemDef.munitRefs.isEmpty) {
        itemDef.munitRefs.foreach { muRef =>
          itemDefElem.addElement("MeasurementUnitRef").addAttribute("MeasurementUnitOID", muRef.getOID())
        }
      }
      
      if (itemDef.codelistRef != null) {
        itemDefElem.addElement("CodeListRef").addAttribute("CodeListOID", itemDef.codelistRef.getOID())
      }
      
      if (itemDef.cdashAlias != null) {
        itemDefElem.addElement("Alias")
            .addAttribute("Name", itemDef.cdashAlias)
            .addAttribute("Context", "CDASH")
      }
                     
      if (itemDef.sdtmAlias != null) {
        itemDefElem.addElement("Alias")
            .addAttribute("Name", itemDef.sdtmAlias)
            .addAttribute("Context", "CDASH/SDTM")
      }
                     
      addExtensions(itemDefElem, itemDef)
    }
    
    mdv.codelists.foreach { cl =>
      val codelistElem = mdvElem.addElement("CodeList")
      addAttribute(codelistElem, "OID",          cl.oid)
      addAttribute(codelistElem, "Name",         cl.name)
      addAttribute(codelistElem, "DataType",     cl.datatype)
                 
      addTText(codelistElem, "Description", cl.description)
                 
      cl.codelistItems.foreach { cli =>
        val cliElem = codelistElem.addElement("CodeListItem")
        addAttribute(cliElem, "CodedValue", cli.codedValue)
        addTText(cliElem, "Decode", cli.decode)

        addExtensions(cliElem, cli)
      }
      
      cl.enumeratedItems.foreach { enumi =>
        val enumiElem = codelistElem.addElement("EnumeratedItem")
        addAttribute(enumiElem, "CodedValue", enumi.codedValue)
        addExtensions(enumiElem, enumi)
      }
      
      addExtensions(codelistElem, cl)
    }
    
    var format = new OutputFormat("    ", true);
    val writer = new XMLWriter(new FileWriter(file), format)
    writer.write(document);
    writer.close();
    
    println("OK")
  }
  
  def addAttribute(element: Element, name: String, value: String) {
    if (value != null) {
      element.addAttribute(name, value)
    }
  }
  
  def addTextElement(element: Element, name: String, text: String) {
    if (text != null) {
      element.addElement(name).setText(text.replace("&amp;#", "&#"))
    }
  }
  
  def addTText(element: Element, name: String, text: String) {
    if (text != null) {
      element.addElement(name)
        .addElement("TranslatedText")
        .addAttribute("xml:lang", "en")
        .setText(text.replace("&amp;#", "&#"))
    }
  }
      
  def addExtensions(parentElem: Element, extended: Extras) {
    extended.extendedAttributes.foreach { extension =>
        parentElem.addAttribute(extension.name, extension.value);
    }
    extended.extendedElements.foreach { extension =>
        parentElem.addElement(extension.name).setText(extension.value.replace("&amp;#", "&#"))
    }
  }
}

