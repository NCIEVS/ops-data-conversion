package gov.nih.nci.evs.cdisc;

import java.io.*;
import java.util.*;
import java.net.*;


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
