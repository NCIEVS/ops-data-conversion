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
package model

import scala.collection.mutable.ArrayBuffer

class ItemDef(val order: Int, val name : String, val question: String, val cdashAlias: String, val sdtmAlias: String, val description: String) extends Sourced with OIDEntity with Extras  {
  var datatype: String = null
  var length: Int = -1
  
  val munitRefs = new ArrayBuffer[Reference]()
  var codelistRef : Reference = null
  
  override def toString(): String = {
    "ItemDef[" + oid +
      ", cdashAlias=" +  cdashAlias +
      ", datatype=" +    datatype +
      ", length=" +      length +
      ", source=" +      source +
      "]"
  }
}
