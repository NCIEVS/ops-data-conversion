package gov.nih.nci.evs.appl;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.*;
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
public class TerminologyExcel2ODM {

	String dateStamp = null;
	String terminologyType = null;
	String terminologyModel = null;
	String terminologyShortModel = null;

	String odm_creationDateTime = null;
	String odm_asOfDateTime = null;
	String odm_originator = null;
	String odm_sourceSystem = null;
	String odm_sourceSystemVersion = null;
	String odm_fileOID = null;

	String odm_context = null;
	String odm_controlledTerminologyVersion = null;

	String study_oid = null;
	String study_globalVarsStudyName = null;
	String study_globalVarsStudyDescription = null;
	String study_globalVarsStudyProtocolName = null;

	String mdv_oid = null;
	String mdv_name = null;
	String mdv_description = null;

	String excelfile = null;
	String odm_xml_file = null;
	Vector xml_data = null;
	int codeListCount = 0;

	XMLData curr_xmldata = null;

	TerminologyExcelReader terminologyReader = null;

	static String XSD_FILENAME = "controlledterminology1-2-0.xsd";
	static String ODM_VERSION = "1.3.2";

	public TerminologyExcel2ODM(String excelfile) {
		int n = excelfile.lastIndexOf(".");
		odm_xml_file = excelfile.substring(0, n) + ".odm.xml";
	}




	public TerminologyExcel2ODM(String excelfile, String odmxmlfile) {
		odm_xml_file = odmxmlfile;
		terminologyReader = new TerminologyExcelReader();
		File excel_file = new File(excelfile);
		terminologyReader.read(excel_file);

		dateStamp = terminologyReader.terminologyDate;
		terminologyType = terminologyReader.terminologyType;
		terminologyModel = terminologyReader.terminologyModel;
		terminologyShortModel = terminologyReader.terminologyShortModel;

		odm_creationDateTime = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
		odm_asOfDateTime = dateStamp + "T00:00:00";
		odm_originator = "CDISC XML Technologies Team (Terminology2ODM converter)";
		odm_sourceSystem = "NCI Thesaurus";
		odm_sourceSystemVersion = dateStamp;
		odm_fileOID     = "CDISC_CT." + terminologyModel + "." + dateStamp;
		odm_controlledTerminologyVersion = "1.2.0";

		if (odm_xml_file.indexOf("Glossary") != -1 || odm_xml_file.indexOf("DDF") != -1 || odm_xml_file.indexOf("Protocol") != -1) {
			odm_context = "Other";
		} else {
			odm_context = "Submission";
		}

		study_oid = "CDISC_CT." + terminologyModel + "." + dateStamp;
		study_globalVarsStudyName = "CDISC " + terminologyModel + " " + terminologyType;
		study_globalVarsStudyDescription = "CDISC " + terminologyModel + " " + terminologyType + ", " + dateStamp;
		study_globalVarsStudyProtocolName = "CDISC " + terminologyModel + " " + terminologyType;

		mdv_oid = "CDISC_CT_MetaDataVersion." + terminologyShortModel + "." + dateStamp;
		mdv_name = "CDISC " + terminologyModel + " " + terminologyType;
		mdv_description = "CDISC " + terminologyModel + " " + terminologyType + ", " + dateStamp;
		char delim = '|';
		int sheetIndex = TerminologyExcelReader.findTerminologySheetName(excelfile);
		xml_data = ExcelReader.toDelimited(excelfile, sheetIndex, delim);
	}

    public static Vector parseData(String line, char delimiter) {
		if(line == null) return null;
		Vector w = new Vector();
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<line.length(); i++) {
			char c = line.charAt(i);
			if (c == delimiter) {
				w.add(buf.toString());
				buf = new StringBuffer();
			} else {
				buf.append(c);
			}
		}
		w.add(buf.toString());
		return w;
	}

	public void writeHeader(PrintWriter out) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("");
		out.println("<ODM xmlns=\"http://www.cdisc.org/ns/odm/v1.3\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:nciodm=\"http://ncicb.nci.nih.gov/xml/odm/EVS/CDISC\"" + " FileType=\"Snapshot\" FileOID=\"" + odm_fileOID + "\" Granularity=\"Metadata\" CreationDateTime=\"" + odm_creationDateTime + "\" AsOfDateTime=\"" + odm_asOfDateTime + "\" ODMVersion=\"" + ODM_VERSION + "\" Originator=\"" + odm_originator + "\" SourceSystem=\"" + odm_sourceSystem + "\" SourceSystemVersion=\"" + odm_sourceSystemVersion
		    + "\" nciodm:Context=\"" + odm_context + "\" nciodm:ControlledTerminologyVersion=\"" + odm_controlledTerminologyVersion + "\">");
		out.println("    <Study OID=\"" + study_oid + "\">");
		out.println("        <GlobalVariables>");
		out.println("            <StudyName>" + study_globalVarsStudyName + "</StudyName>");
		out.println("            <StudyDescription>" + xmlEscapeText(study_globalVarsStudyDescription) + "</StudyDescription>");
		out.println("            <ProtocolName>" + study_globalVarsStudyProtocolName + "</ProtocolName>");
		out.println("        </GlobalVariables>");
		out.println("        <MetaDataVersion OID=\"" + mdv_oid + "\" Name=\"" + mdv_name + "\" Description=\"" + xmlEscapeText(mdv_description) + "\">");
	}

	public void writeCodeList(PrintWriter out, XMLData xmldata) {
		if (curr_xmldata != null) {
			String submissionValue = curr_xmldata.getSubmissionValue();
			out.println("                <nciodm:CDISCSubmissionValue>" + xmlEscapeText(submissionValue) + "</nciodm:CDISCSubmissionValue>");

			String syns = curr_xmldata.getSynonyms();
			syns = syns.replace("; ", ";");
			Vector sync_vec = parseData(syns, ';');

			for (int i=0; i<sync_vec.size(); i++) {
				String syn = (String) sync_vec.elementAt(i);
				if (syn.length() > 0) {
					out.println("                <nciodm:CDISCSynonym>" + xmlEscapeText(syn) + "</nciodm:CDISCSynonym>");
				}
			}

			String nciPreferredTerm = curr_xmldata.getNciPreferredTerm();
			out.println("                <nciodm:PreferredTerm>" + xmlEscapeText(nciPreferredTerm) + "</nciodm:PreferredTerm>");
		}
		curr_xmldata = xmldata;

		String code = xmldata.getCode();
		String submissionValue = xmldata.getSubmissionValue();
		String codeListName = xmldata.getCodeListName();
		String codelistExtensible = xmldata.getCodelistExtensible();
		String cdiscDefinition = xmldata.getCdiscDefinition();

		if (codeListCount > 0) {
			out.println("            </CodeList>");
	    }
	    codeListCount++;
/*
        if (odm_context.compareTo("Submission") == 0) {
			out.println("            <CodeList OID=\"CL." + code + "." + xmlEscapeText(submissionValue) + "\" Name=\"" + xmlEscapeText(codeListName) + "\" DataType=\"text\" nciodm:ExtCodeID=\"" + code + "\" nciodm:CodeListExtensible=\"" + codelistExtensible + "\">");
	    } else {
			out.println("            <CodeList OID=\"CL." + code + "." + xmlEscapeText(submissionValue) + "\" Name=\"" + xmlEscapeText(codeListName) + "\" DataType=\"text\" nciodm:ExtCodeID=\"" + code + "\">");
		}
*/
        if (codelistExtensible != null && codelistExtensible.length() > 0) {
		    out.println("            <CodeList OID=\"CL." + code + "." + xmlEscapeText(submissionValue) + "\" Name=\"" + xmlEscapeText(codeListName) + "\" DataType=\"text\" nciodm:ExtCodeID=\"" + code + "\" nciodm:CodeListExtensible=\"" + codelistExtensible + "\">");
	    } else {
			out.println("            <CodeList OID=\"CL." + code + "." + xmlEscapeText(submissionValue) + "\" Name=\"" + xmlEscapeText(codeListName) + "\" DataType=\"text\" nciodm:ExtCodeID=\"" + code + "\">");
		}
		out.println("                <Description>");
		out.println("                    <TranslatedText xml:lang=\"en\">" + xmlEscapeText(cdiscDefinition) + "</TranslatedText>");
		out.println("                </Description>");
	}


	public void writeCodeListEnumeratedItem(PrintWriter out, XMLData xmldata) {
		String code = xmldata.getCode();
		String submissionValue = xmldata.getSubmissionValue();
		String cdiscDefinition = xmldata.getCdiscDefinition();
		String nciPreferredTerm = xmldata.getNciPreferredTerm();

		String syns = xmldata.getSynonyms();
		syns = syns.replace("; ", ";");
		Vector sync_vec = parseData(syns, ';');
		out.println("                <EnumeratedItem CodedValue=\"" + xmlEscapeText(submissionValue) + "\" nciodm:ExtCodeID=\"" + code + "\">");

		for (int i=0; i<sync_vec.size(); i++) {
			String syn = (String) sync_vec.elementAt(i);
			if (syn.length() > 0) {
				out.println("                    <nciodm:CDISCSynonym>" + xmlEscapeText(syn) + "</nciodm:CDISCSynonym>");
			}
		}
		out.println("                    <nciodm:CDISCDefinition>" + xmlEscapeText(cdiscDefinition) + "</nciodm:CDISCDefinition>");
		out.println("                    <nciodm:PreferredTerm>" + xmlEscapeText(nciPreferredTerm) + "</nciodm:PreferredTerm>");
		out.println("                </EnumeratedItem>");
	}

    public void writeFooter(PrintWriter out) {

		if (curr_xmldata != null) {
			String submissionValue = curr_xmldata.getSubmissionValue();
			out.println("                <nciodm:CDISCSubmissionValue>" + xmlEscapeText(submissionValue) + "</nciodm:CDISCSubmissionValue>");

			String syns = curr_xmldata.getSynonyms();
			syns = syns.replace("; ", ";");
			Vector sync_vec = parseData(syns, ';');

			for (int i=0; i<sync_vec.size(); i++) {
				String syn = (String) sync_vec.elementAt(i);
				if (syn.length() > 0) {
					out.println("                <nciodm:CDISCSynonym>" + xmlEscapeText(syn) + "</nciodm:CDISCSynonym>");
				}
			}

			String nciPreferredTerm = curr_xmldata.getNciPreferredTerm();
			out.println("                <nciodm:PreferredTerm>" + xmlEscapeText(nciPreferredTerm) + "</nciodm:PreferredTerm>");
		}
		out.println("            </CodeList>");
		out.println("        </MetaDataVersion>");
		out.println("    </Study>");
		out.println("</ODM>");
    }

    public void generate_odm_xml() {
        long ms = System.currentTimeMillis();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(odm_xml_file, "UTF-8");
            writeHeader(pw);
            for (int i=1; i<xml_data.size(); i++) {
				String line = (String) xml_data.elementAt(i);
				XMLData xmldata = line2XMLData(line);
				if (isCodeList(xmldata)) {
					writeCodeList(pw, xmldata);
				} else {
                    writeCodeListEnumeratedItem(pw, xmldata);
				}
			}
			writeFooter(pw);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				pw.close();
				System.out.println("Output file " + odm_xml_file + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}

	public void dumpData() {
        System.out.println("dateStamp: " + dateStamp);
        System.out.println("terminologyType: " + terminologyType);
        System.out.println("terminologyModel: " + terminologyModel);
        System.out.println("terminologyShortModel: " + terminologyShortModel);

        System.out.println("odm_creationDateTime: " + odm_creationDateTime);
        System.out.println("odm_asOfDateTime: " + odm_asOfDateTime);
        System.out.println("odm_originator: " + odm_originator);
        System.out.println("odm_sourceSystem: " + odm_sourceSystem);
        System.out.println("odm_sourceSystemVersion: " + odm_sourceSystemVersion);
        System.out.println("odm_fileOID: " + odm_fileOID);

        System.out.println("study_oid: " + study_oid);
        System.out.println("study_globalVarsStudyName: " + study_globalVarsStudyName);
        System.out.println("study_globalVarsStudyDescription: " + study_globalVarsStudyDescription);
        System.out.println("study_globalVarsStudyProtocolName: " + study_globalVarsStudyProtocolName);

        System.out.println("mdv_oid: " + mdv_oid);
        System.out.println("mdv_name: " + mdv_name);
        System.out.println("mdv_description: " + mdv_description);
	}

	public boolean isCodeList(XMLData xmldata) {
		if (xmldata.getCodeListCode().compareTo("") == 0) {
			return true;
		}
		return false;
	}

	public XMLData line2XMLData(String line) {
		Vector u = parseData(line, '|');
		String code = (String) u.elementAt(0);
		String codeListCode = (String) u.elementAt(1);
		String codelistExtensible = (String) u.elementAt(2);
		String codeListName = (String) u.elementAt(3);
		String submissionValue = (String) u.elementAt(4);
		String synonyms = (String) u.elementAt(5);
		String cdiscDefinition = (String) u.elementAt(6);
		String nciPreferredTerm = (String) u.elementAt(7);

        XMLData xmldata = new XMLData(
			code,
			codeListCode,
			codelistExtensible,
			codeListName,
			submissionValue,
			synonyms,
			cdiscDefinition,
			nciPreferredTerm);
		xmldata.setData(line);
		return xmldata;
	}

	public String xmlEscapeText(String t) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < t.length(); i++){
			char c = t.charAt(i);
			switch(c){
				case '<': sb.append("&lt;"); break;
				case '>': sb.append("&gt;"); break;
				case '\"': sb.append("&quot;"); break;
				case '&': sb.append("&amp;"); break;
				case '\'': sb.append("&apos;"); break;
				default:
					if (c>0x7e) {
						sb.append("&#"+((int)c)+";");
					} else {
						sb.append(c);
					}
			}
		}
		return sb.toString();
	}

    public static boolean fileExists(String filename) {
		File f = new File(filename);
		if(f.exists() && !f.isDirectory()) {
			return true;
		} else {
			return false;
		}
	}

	public static void main(String[] args) {
		String excelfile = args[0];
		String odmxmlfile = null;
		if (fileExists(excelfile)) {
			if (args.length == 1) {
				int n = excelfile.lastIndexOf(".");
				odmxmlfile = excelfile.substring(0, n) + ".odm.xml";
			} else {
				odmxmlfile = args[1];
			}
			System.out.println("\nConverting " + excelfile + " to " + odmxmlfile + " ...");
			TerminologyExcel2ODM test = new TerminologyExcel2ODM(excelfile, odmxmlfile);
			test.generate_odm_xml();
	    } else {
			System.out.println("File " + excelfile + " does not exist.");
		}
	}
}

