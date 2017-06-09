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
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import java.io.File

import model._

import java.io.FileInputStream

abstract class ExcelReader {

  def checkHeaders(row : HSSFRow, columns : Map[String,Column]) {
    for ((key, col) <- columns) {
      var value : String = getCellValue(row, col, false)
    
      if (value != col.header) {
        println("ERROR: Incorrect header field.  Expected '" + col.header + "' but found '" + value + "'")
        error("ERROR: Incorrect header field.  Expected '" + col.header + "' but found '" + value + "'")
        
        System.exit(1)
      }
    }
  }
  
  def getCellValue(row: HSSFRow , column: Column , scrapNewline: Boolean) : String = {
    val cell = row.getCell(column.index)
    
    if (cell == null) return null
      
    var value : String = cell.getCellType() match {
      case HSSFCell.CELL_TYPE_BLANK => return null
      case HSSFCell.CELL_TYPE_STRING => cell.getRichStringCellValue().getString().trim()
      case HSSFCell.CELL_TYPE_NUMERIC => new String("" + cell.getNumericCellValue().intValue())
        
      case _ 
        => {
          println("Don't know what to do with cell " + row.getRowNum() + ", " + column.index + " - unknown datatype " + cell.getCellType())
          return null
      }
    }
    
    value = value.toString()
        
    if (scrapNewline) {
      value = value.replace('\n', ' ')
    }
    
    if (value == "")
      value = null
    
    return value
  }
}

