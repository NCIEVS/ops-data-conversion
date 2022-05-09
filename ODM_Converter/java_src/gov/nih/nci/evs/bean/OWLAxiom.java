package gov.nih.nci.evs.bean;
import com.google.gson.*;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.XStream;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2022 Guidehouse. This software was developed in conjunction
 * with the National Cancer Institute, and so to the extent government
 * employees are co-authors, any rights in such works shall be subject
 * to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the disclaimer of Article 3,
 *      below. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   2. The end-user documentation included with the redistribution,
 *      if any, must include the following acknowledgment:
 *      "This product includes software developed by Guidehouse and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "Guidehouse" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or GUIDEHOUSE
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      GUIDEHOUSE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
 *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *      POSSIBILITY OF SUCH DAMAGE.
 * <!-- LICENSE_TEXT_END -->
 */

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nih.gov
 *
 */
public class OWLAxiom
{

// Variable declaration
	private int axiomId;
	private String label;
	private String annotatedSource;
	private String annotatedProperty;
	private String annotatedTarget;
	private String qualifierName;
	private String qualifierValue;

// Default constructor
	public OWLAxiom() {
	}

// Constructor
	public OWLAxiom(
		int axiomId,
		String label,
		String annotatedSource,
		String annotatedProperty,
		String annotatedTarget,
		String qualifierName,
		String qualifierValue) {

		this.axiomId = axiomId;
		this.label = label;
		this.annotatedSource = annotatedSource;
		this.annotatedProperty = annotatedProperty;
		this.annotatedTarget = annotatedTarget;
		this.qualifierName = qualifierName;
		this.qualifierValue = qualifierValue;
	}

// Set methods
	public void setAxiomId(int axiomId) {
		this.axiomId = axiomId;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setAnnotatedSource(String annotatedSource) {
		this.annotatedSource = annotatedSource;
	}

	public void setAnnotatedProperty(String annotatedProperty) {
		this.annotatedProperty = annotatedProperty;
	}

	public void setAnnotatedTarget(String annotatedTarget) {
		this.annotatedTarget = annotatedTarget;
	}

	public void setQualifierName(String qualifierName) {
		this.qualifierName = qualifierName;
	}

	public void setQualifierValue(String qualifierValue) {
		this.qualifierValue = qualifierValue;
	}


// Get methods
	public int getAxiomId() {
		return this.axiomId;
	}

	public String getLabel() {
		return this.label;
	}

	public String getAnnotatedSource() {
		return this.annotatedSource;
	}

	public String getAnnotatedProperty() {
		return this.annotatedProperty;
	}

	public String getAnnotatedTarget() {
		return this.annotatedTarget;
	}

	public String getQualifierName() {
		return this.qualifierName;
	}

	public String getQualifierValue() {
		return this.qualifierValue;
	}

	public String toXML() {
		XStream xstream_xml = new XStream(new DomDriver());
		String xml = xstream_xml.toXML(this);
		xml = escapeDoubleQuotes(xml);
		StringBuffer buf = new StringBuffer();
		String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		buf.append(XML_DECLARATION).append("\n").append(xml);
		xml = buf.toString();
		return xml;
	}

	public String toJson() {
		JsonParser parser = new JsonParser();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
	}

	public String escapeDoubleQuotes(String inputStr) {
		char doubleQ = '"';
		StringBuffer buf = new StringBuffer();
		for (int i=0;  i<inputStr.length(); i++) {
			char c = inputStr.charAt(i);
			if (c == doubleQ) {
				buf.append(doubleQ).append(doubleQ);
			}
			buf.append(c);
		}
		return buf.toString();
	}

	public String toString() {
		return toString('|');
	}

	public String toString(char delim) {
		return "" + this.axiomId + delim
		     + this.label + delim
		     + this.annotatedSource + delim
		     + this.annotatedProperty + delim
		     + this.annotatedTarget + delim
		     + this.qualifierName + delim
		     + this.qualifierValue;
	}

}
