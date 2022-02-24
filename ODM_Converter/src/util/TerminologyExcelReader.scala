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
package util;

import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

import java.io.FileInputStream
import java.io.File

import model._

class TerminologyExcelReader extends ExcelReader {
  
  val COLS = Map(
    "code"               -> new Column(0,  "Code"),
    "codelist_code"      -> new Column(1,  "Codelist Code"),
    "extensible"         -> new Column(2,  "Codelist Extensible (Yes/No)"),
    "name"               -> new Column(3,  "Codelist Name"),
    "submission_value"   -> new Column(4,  "CDISC Submission Value"),
    //"preferred_term"     -> new Column(5,  "CDISC Preferred Term"),
    "synonyms"           -> new Column(5,  "CDISC Synonym(s)"),
    "definition"         -> new Column(6,  "CDISC Definition"),
    "nci_preferred_term" -> new Column(7,  "NCI Preferred Term")
  )
 
  var terminologyModel: String = null
  var terminologyShortModel: String = null
  var terminologyType: String = null
  var terminologyDate: String = null
  
  var codelists = new ArrayBuffer[CodeList]()
  
  def read(file : File) {
    printf("Reading %s ... ", file)
    
    val is = new FileInputStream(file)
    val fs = new POIFSFileSystem(is)
    
    val workbook = new HSSFWorkbook(fs)
    
    val sheetCount = workbook.getNumberOfSheets()
    
    for (i <- List.range(0, sheetCount)) {
      val sheet = workbook.getSheetAt(i)
      
      val sheetName = workbook.getSheetName(i)
 
      if (sheetName.contains("Terminology")) {
        printf("found sheet '%s' ... ", sheetName);
        
        val parts = sheetName.split(" ")
          
        if (parts.length != 3) {
          throw new RuntimeException("Expected sheet name in form 'Clinical <type> Glossary <date>' but found '" + sheetName + "' instead")
        }
    
        terminologyModel = parts(0).trim()
        terminologyShortModel = parts(0).trim()
        terminologyType = "Controlled Terminology"
        terminologyDate = parts(2).trim()
      
        readCodelists(sheet)
      } 
    }
    
    is.close()
    
    printf("Done reading %s ... ", file)
  }      
      

  def readCodelists(sheet : HSSFSheet) {
    val firstRow = sheet.getFirstRowNum()
    val lastRow  = sheet.getLastRowNum()
    
    var isFirstRow = true
    
    var codelist : CodeList = null;
    var xmlUtil = new XmlUtil()
    //var test_encoded_str = xmlUtil.xmlEscapeText("<>&")
    
    for (i <- firstRow to lastRow) {
      var row = sheet.getRow(i)
        
      if (isFirstRow) {
        checkHeaders(row, COLS)
        isFirstRow = false
        
      } else {
        val code               = getCellValue(row, COLS("code"              ), false)
        val codelist_code      = getCellValue(row, COLS("codelist_code"     ), true)
        val extensible         = getCellValue(row, COLS("extensible"        ), false)
        val name               = getCellValue(row, COLS("name"              ), false)
        val submission_value   = getCellValue(row, COLS("submission_value"  ), false)
        //val preferred_term     = getCellValue(row, COLS("preferred_term"    ), false)
        val synonyms           = getCellValue(row, COLS("synonyms"          ), false)
        val definition         = getCellValue(row, COLS("definition"        ), false)
        val nci_preferred_term = getCellValue(row, COLS("nci_preferred_term"), false)
        
        if (code != null && codelist_code == null) {
          // New codelist
          codelist = new CodeList("[NEW]", "[NEW]", "text")
          codelist.addExtra("terminology:code", code)

          //if (terminologyModel.indexOf("Glossary") != -1 || terminologyModel.indexOf("Protocol") != -1) {
          	//codelist.addExtra("terminology:extensible", extensible)
          //} else {
          if (extensible != null && extensible.length > 0) {
               codelist.addExtra("terminology:extensible", extensible)
          }

          codelist.addExtra("terminology:name", xmlUtil.xmlEscapeText(name))
          codelist.addExtra("terminology:submission_value", xmlUtil.xmlEscapeText(submission_value))
          // codelist.addExtra("terminology:preferred_term", xmlUtil.xmlEscapeText(preferred_term))
          codelist.addExtra("terminology:synonyms", xmlUtil.xmlEscapeText(synonyms))
          codelist.addExtra("terminology:definition", xmlUtil.xmlEscapeText(definition))
          codelist.addExtra("terminology:nci_preferred_term", xmlUtil.xmlEscapeText(nci_preferred_term))
          codelists += codelist
          
        } else if (code != null && codelist_code != null) {
          if (codelist_code != codelist.extras("terminology:code")) {
            sys.error("ERROR: Incorrect codelist code.  Expected '" + codelist.extras("terminology:code") + "' but found '" + codelist_code + "' at line " + i + ".")
            println("ERROR: Incorrect codelist code.  Expected '" + codelist.extras("terminology:code") + "' but found '" + codelist_code + "' at line " + i + ".")
          } 
          
          val cli = new CodeListItem(submission_value, "[NEW]")
          
          cli.addExtra("terminology:code", code)
          // cli.extras("terminology:extensible", extensible)
          cli.addExtra("terminology:name", name)
          cli.addExtra("terminology:submission_value", submission_value)
          // codelist.addExtra("terminology:preferred_term", preferred_term)
          cli.addExtra("terminology:synonyms", synonyms)
          cli.addExtra("terminology:definition", definition)
          cli.addExtra("terminology:nci_preferred_term", nci_preferred_term)
          codelist.codelistItems += cli
        }
      }
    }
  }
}

