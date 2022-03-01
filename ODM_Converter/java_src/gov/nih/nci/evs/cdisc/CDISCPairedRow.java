package gov.nih.nci.evs.cdisc;

import java.io.*;
import java.util.*;
import java.net.*;

public class CDISCPairedRow
{

// Variable declaration
	private String code;
	private String testcdParmcdCodelistCode;
	private String testcdParmcdCodelistName;
	private String testcdParmcdCDISCSubmissionValueCode;
	private String testParmCodelistCode;
	private String testParmCodelistName;
	private String testParmCDISCSubmissionValueName;
	private String cDISCSynonyms;
	private String cDISCDefinition;
	private String nCIPreferredTerm;

// Default constructor
	public CDISCPairedRow() {
	}

// Constructor
	public CDISCPairedRow(
		String code,
		String testcdParmcdCodelistCode,
		String testcdParmcdCodelistName,
		String testcdParmcdCDISCSubmissionValueCode,
		String testParmCodelistCode,
		String testParmCodelistName,
		String testParmCDISCSubmissionValueName,
		String cDISCSynonyms,
		String cDISCDefinition,
		String nCIPreferredTerm) {

		this.code = code;
		this.testcdParmcdCodelistCode = testcdParmcdCodelistCode;
		this.testcdParmcdCodelistName = testcdParmcdCodelistName;
		this.testcdParmcdCDISCSubmissionValueCode = testcdParmcdCDISCSubmissionValueCode;
		this.testParmCodelistCode = testParmCodelistCode;
		this.testParmCodelistName = testParmCodelistName;
		this.testParmCDISCSubmissionValueName = testParmCDISCSubmissionValueName;
		this.cDISCSynonyms = cDISCSynonyms;
		this.cDISCDefinition = cDISCDefinition;
		this.nCIPreferredTerm = nCIPreferredTerm;
	}

// Set methods
	public void setCode(String code) {
		this.code = code;
	}

	public void setTestcdParmcdCodelistCode(String testcdParmcdCodelistCode) {
		this.testcdParmcdCodelistCode = testcdParmcdCodelistCode;
	}

	public void setTestcdParmcdCodelistName(String testcdParmcdCodelistName) {
		this.testcdParmcdCodelistName = testcdParmcdCodelistName;
	}

	public void setTestcdParmcdCDISCSubmissionValueCode(String testcdParmcdCDISCSubmissionValueCode) {
		this.testcdParmcdCDISCSubmissionValueCode = testcdParmcdCDISCSubmissionValueCode;
	}

	public void setTestParmCodelistCode(String testParmCodelistCode) {
		this.testParmCodelistCode = testParmCodelistCode;
	}

	public void setTestParmCodelistName(String testParmCodelistName) {
		this.testParmCodelistName = testParmCodelistName;
	}

	public void setTestParmCDISCSubmissionValueName(String testParmCDISCSubmissionValueName) {
		this.testParmCDISCSubmissionValueName = testParmCDISCSubmissionValueName;
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

	public String getTestcdParmcdCodelistCode() {
		return this.testcdParmcdCodelistCode;
	}

	public String getTestcdParmcdCodelistName() {
		return this.testcdParmcdCodelistName;
	}

	public String getTestcdParmcdCDISCSubmissionValueCode() {
		return this.testcdParmcdCDISCSubmissionValueCode;
	}

	public String getTestParmCodelistCode() {
		return this.testParmCodelistCode;
	}

	public String getTestParmCodelistName() {
		return this.testParmCodelistName;
	}

	public String getTestParmCDISCSubmissionValueName() {
		return this.testParmCDISCSubmissionValueName;
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
