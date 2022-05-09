package gov.nih.nci.evs.cdisc;
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
public class CDISCRow
{

// Variable declaration
	private String code;
	private String codelistCode;
	private String extensibleList;
	private String codelistName;
	private String cDISCSubmissionValue;
	private String cDISCSynonyms;
	private String cDISCDefinition;
	private String nCIPreferredTerm;

// Default constructor
	public CDISCRow() {
	}

// Constructor
	public CDISCRow(
		String code,
		String codelistCode,
		String extensibleList,
		String codelistName,
		String cDISCSubmissionValue,
		String cDISCSynonyms,
		String cDISCDefinition,
		String nCIPreferredTerm) {

		this.code = code;
		this.codelistCode = codelistCode;
		this.extensibleList = extensibleList;
		this.codelistName = codelistName;
		this.cDISCSubmissionValue = cDISCSubmissionValue;
		this.cDISCSynonyms = cDISCSynonyms;
		this.cDISCDefinition = cDISCDefinition;
		this.nCIPreferredTerm = nCIPreferredTerm;
	}

// Set methods
	public void setCode(String code) {
		this.code = code;
	}

	public void setCodelistCode(String codelistCode) {
		this.codelistCode = codelistCode;
	}

	public void setExtensibleList(String extensibleList) {
		this.extensibleList = extensibleList;
	}

	public void setCodelistName(String codelistName) {
		this.codelistName = codelistName;
	}

	public void setCDISCSubmissionValue(String cDISCSubmissionValue) {
		this.cDISCSubmissionValue = cDISCSubmissionValue;
	}

	public void setCDISCSynonyms(String cDISCSynonyms) {
		this.cDISCSynonyms = cDISCSynonyms;
	}

	public void setCDISCDefinition(String cDISCDefinition) {
		this.cDISCDefinition = cDISCDefinition;
	}

	public void setNCIPreferredTerm(String nCIPreferredTerm) {
		this.nCIPreferredTerm = nCIPreferredTerm;
	}


// Get methods
	public String getCode() {
		return this.code;
	}

	public String getCodelistCode() {
		return this.codelistCode;
	}

	public String getExtensibleList() {
		return this.extensibleList;
	}

	public String getCodelistName() {
		return this.codelistName;
	}

	public String getCDISCSubmissionValue() {
		return this.cDISCSubmissionValue;
	}

	public String getCDISCSynonyms() {
		return this.cDISCSynonyms;
	}

	public String getCDISCDefinition() {
		return this.cDISCDefinition;
	}

	public String getNCIPreferredTerm() {
		return this.nCIPreferredTerm;
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
}
