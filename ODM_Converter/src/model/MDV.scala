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

class MDV extends OIDEntity {
  var name: String = null
  var description: String = null
  val eventDefs  = new ArrayBuffer[EventDef]()
  val formDefs   = new ArrayBuffer[FormDef]()
  val igroupDefs = new ArrayBuffer[IGroupDef]()
  val itemDefs   = new ArrayBuffer[ItemDef]()
  val codelists  = new ArrayBuffer[CodeList]()

  def findEventDefByOID(oid : String) : EventDef = {
    val results = eventDefs.filter { elem => elem.oid == oid }
    if (results.size == 1) results(0) else null
  }
  
  def findFormDefByOID(oid : String) : FormDef = {
    val results = formDefs.filter { elem => elem.oid == oid }
    if (results.size == 1) results(0) else null
  }
  
  def findIGroupDefByOID(oid : String) : IGroupDef = {
    val results = igroupDefs.filter { elem => elem.oid == oid }
    if (results.size == 1) results(0) else null
  }
  
  def findItemDefByAlias(alias : String) : ItemDef = {
    val results = itemDefs.filter { elem => elem.cdashAlias == alias }
    if (results.size == 1) results(0) else null
  }
  
}
