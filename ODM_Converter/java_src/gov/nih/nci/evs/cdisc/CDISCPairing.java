package gov.nih.nci.evs.cdisc;
import com.opencsv.CSVReader;
import gov.nih.nci.evs.restapi.bean.*;
import gov.nih.nci.evs.restapi.util.*;
import java.awt.Color;
import java.io.*;
import java.io.*;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class CDISCPairing {
	public static int DATA_SOURCE_NCIT_OWL = 1;
	public static int DATA_SOURCE_SPARQL = 2;
	static String METADATA_HEADER = "Code|Codelist Code|Codelist Extensible (Yes/No)|Codelist Name|CDISC Submission Value|CDISC Synonym(s)|CDISC Definition|NCI Preferred Term";
    static String PAIRED_TERMS_HEADER = "Code|TESTCD/PARMCD Codelist Code|TESTCD/PARMCD Codelist Name|TESTCD/PARMCD CDISC Submission Value Code|TEST/PARM Codelist Code|TEST/PARM Codelist Name|TEST/PARM CDISC Submission Value Name|CDISC Synonym(s)|CDISC Definition|NCI Preferred Term";

    //CDISCPairingSPARQL cdiscPairingSPARQL = null;
    String serviceUrl = null;
    String namedGraph = null;
    String username = null;
    String password = null;
    String version  = null;
    int data_source = 0;

    String xlsfile = null;
    String owlfile = null;
    Vector tableData = null;
    Vector codes = null;
    Vector codelists = null;
    Vector<Synonym> codeSynonyms = null;
    Vector<Synonym> codelistSynonyms = null;
    HashMap paringConceptHashMap = null;
    HashMap code2TableDataMap = new HashMap();
    Vector focusedCodes = new Vector();
    HashMap focusedCode2CDISCPTSynMap = new HashMap();
    HashMap focusedCode2CDISCSYSynMap = new HashMap();
    HashMap focusedCode2CDISCSynMap = new HashMap();
    Vector codesWith2CDISCPTs = new Vector();
    HashMap codesWith2CDISCPT2SourceCodeMap = new HashMap();
    HashMap NCIABMap = new HashMap();
    HashMap pairingSourceMap = new HashMap();
    HashMap CDISCSYMap = new HashMap();
    HashMap pairingTargetMap = new HashMap();

    //**************************************************
    HashMap code2CDISCPTSourceCodeMap = null;
    HashMap pairedSourceTermData = null;
    Vector conceptsWithNCIAB = null;
    HashMap cdiscSY2CodeMap = null;
    Vector pairedTermData = null;

    CDISCScanner cdiscScanner = null;
    //
    //CDISCPairingSPARQL cdiscCPairingSPARQL = null;
    HashMap preferredNameMap = null;
    HashMap synonymMap = null;
    HashMap cdiscDefinitionMap = null;
    HashMap extensibleListMap = null;

    String xlsxfile = null;
    String root = null;
    HashSet retired_concepts = new HashSet();
    String terminology = null;


    public CDISCPairing(String owlfile) {
		this.owlfile = owlfile;
		this.cdiscScanner = new CDISCScanner(owlfile);
		this.data_source = 1;
	}

	public void dumpStringValuedHashMap(HashMap hmap) {
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String value = (String) hmap.get(key);
			System.out.println(key + " --> " + value);
		}
	}

	public void saveStringValuedHashMap(String outputfile, HashMap hmap) {
		Vector w = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String value = (String) hmap.get(key);
			System.out.println(key + " --> " + value);
			w.add(key + " --> " + value);
		}
		Utils.saveToFile(outputfile, w);
	}

	public HashMap createCode2CDISCPTSourceCodeMap() {
		HashMap hmap = new HashMap();
		for (int i=0; i<focusedCodes.size(); i++) {
			String code = (String) focusedCodes.elementAt(i);
			Vector syn_vec = (Vector) synonymMap.get(code);
			StringBuffer buf = new StringBuffer();
			if (syn_vec != null && syn_vec.size() > 0) {
				for (int j = 0; j<syn_vec.size(); j++) {
					Synonym syn = (Synonym) syn_vec.elementAt(j);
					if (syn.getTermGroup().compareTo("PT") == 0 &&
						syn.getTermSource().compareTo("CDISC") == 0) {
						if (syn.getSourceCode() != null && syn.getSourceCode().length() > 0) {
							buf.append(syn.getSourceCode()).append("|");
						}
					}
					String s = buf.toString();
					if (s.length() > 0) {
						s = s.substring(0, s.length()-1);
						hmap.put(code, s);
					}
				}
			}
		}
		return hmap;
	}

	public Vector getCoceptsWithNCIAB() {
		HashMap hmap = new HashMap();
		Vector w = new Vector();
		Iterator it = synonymMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
			for (int i=0; i<syn_vec.size(); i++) {
				Synonym syn = (Synonym) syn_vec.elementAt(i);
				if (syn.getTermGroup().compareTo("AB") == 0 &&
					syn.getTermSource().compareTo("NCI") == 0) {
					w.add(syn.getCode());
				}
			}
		}
		return w;
	}

    public String searchPairedSourceTerm(String member_code, String cd_code) {
		for (int i=0; i<conceptsWithNCIAB.size(); i++) {
			String code = (String) conceptsWithNCIAB.elementAt(i);
			Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
			String nci_ab = null;
			String cdisc_sy = null;
			boolean nci_ab_matched = false;
			String termName = null;
			for (int j=0; j<syn_vec.size(); j++) {
				Synonym syn = (Synonym) syn_vec.elementAt(j);
				termName = syn.getTermName();

				if (syn.getTermName().compareTo(cd_code) == 0 &&
					syn.getTermGroup().compareTo("AB") == 0 &&
					syn.getTermSource().compareTo("NCI") == 0) {
					nci_ab_matched = true;
				}
				if (syn.getTermGroup().compareTo("SY") == 0 &&
					syn.getTermSource().compareTo("CDISC") == 0) {
					cdisc_sy = syn.getTermName();
				}
			}
			if (nci_ab_matched && cdisc_sy != null) {
				// find CDISC PT matching source code: cd_code
				String cdisc_pt = findTermNameMatchingCDISCPTSourceCode(member_code, cd_code);
				return code + "|" + cdisc_sy + "|" + cdisc_pt + "|" + cd_code;
			}
		}
		return null;
	}


	public Vector getPairedSourceTermData(String code) {
    	Vector w = new Vector();
    	Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
    	Vector submissionValueCodes = getSubmissionValueCodes(syn_vec);
    	for (int i=0; i<submissionValueCodes.size(); i++) {
			String s = (String) submissionValueCodes.elementAt(i);
			String t = searchPairedSourceTerm(code, s);
			w.add(code + "|" + t);
		}
		return w;
	}

    public HashMap generatePairedSourceTermData() {
		HashMap hmap = new HashMap();
	    Iterator it = code2CDISCPTSourceCodeMap.keySet().iterator();
	    while (it.hasNext()) {
		    String code = (String) it.next();
		    Vector w = getPairedSourceTermData(code);
			if (w.size() > 0) {
				hmap.put(code, w);
			}
		}
		return hmap;
	}

	public HashMap createCDISCSY2CodeMap() {
		HashMap hmap = new HashMap();
		Iterator it = synonymMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
		    Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
			for (int i=0; i<syn_vec.size(); i++) {
				Synonym syn = (Synonym) syn_vec.elementAt(i);
				if (syn.getTermGroup().compareTo("SY") == 0 &&
					syn.getTermSource().compareTo("CDISC") == 0) {
				    String termName = syn.getTermName();
				    Vector v = new Vector();
				    if (hmap.containsKey(termName)) {
						v = (Vector) hmap.get(termName);
					}
					if (!v.contains(code)) {
						v.add(code);
					}
					hmap.put(termName, v);
				}
			}
		}
		return hmap;
	}

	public HashMap createFocusedCode2CDISCPTSynMap() {
		HashMap hmap = new HashMap();
		Iterator it = synonymMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			if (focusedCodes.contains(code)) {
				Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
				Vector w = new Vector();
				if (hmap.containsKey(code)) {
					w = (Vector) hmap.get(code);
				}
				for (int i=0; i<syn_vec.size(); i++) {
					Synonym syn = (Synonym) syn_vec.elementAt(i);
					if (syn.getTermGroup().compareTo("PT") == 0 &&
						syn.getTermSource().compareTo("CDISC") == 0) {
						w.add(syn);
					}
				}
				hmap.put(code, w);
			}
		}
		return hmap;

	}

	public HashMap createFocusedCode2CDISCSYSynMap() {
		HashMap hmap = new HashMap();
		Iterator it = synonymMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			if (focusedCodes.contains(code)) {
				Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
				Vector w = new Vector();
				if (hmap.containsKey(code)) {
					w = (Vector) hmap.get(code);
				}
				for (int i=0; i<syn_vec.size(); i++) {
					Synonym syn = (Synonym) syn_vec.elementAt(i);
					if (syn.getTermGroup().compareTo("SY") == 0 &&
						syn.getTermSource().compareTo("CDISC") == 0) {
						w.add(syn);
					}
				}
				hmap.put(code, w);
			}
		}
		return hmap;

	}

	public HashMap createFocusedCode2CDISCSynMap() {
		HashMap hmap = new HashMap();
		Iterator it = synonymMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			if (focusedCodes.contains(code)) {
				Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
				hmap.put(code, syn_vec);
			}
		}
		return hmap;
	}

	public void dumpHashMap(HashMap hmap) {
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector v = (Vector) hmap.get(key);
			Utils.dumpVector(key, v);
		}
	}

	public void saveHashMap(String outputfile, HashMap hmap) {
		Vector w = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Vector v = (Vector) hmap.get(key);
			Utils.dumpVector(key, v);

			w.add(key);
			for (int i=0; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				int j = i+1;
				w.add("\t(" + j + ") " + t);
			}
		}
		Utils.saveToFile(outputfile, w);
	}

   public String searchPairedTargetTerm(String member_code, String cdis_sy_term_name, String nci_ab) {
		for (int i=0; i<conceptsWithNCIAB.size(); i++) {
			String code = (String) conceptsWithNCIAB.elementAt(i);
			Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
			String cdisc_sy = null;
			boolean nci_ab_matched = false;
			boolean cdisc_sy_matched = false;
			String termName = null;
			for (int j=0; j<syn_vec.size(); j++) {
				Synonym syn = (Synonym) syn_vec.elementAt(j);
				termName = syn.getTermName();

				if (syn.getTermName().compareTo(nci_ab) == 0 &&
					syn.getTermGroup().compareTo("AB") == 0 &&
					syn.getTermSource().compareTo("NCI") == 0) {
					nci_ab_matched = true;
				}
				if (syn.getTermGroup().compareTo("SY") == 0 &&
					syn.getTermSource().compareTo("CDISC") == 0) {
					cdisc_sy = syn.getTermName();
					if (cdisc_sy.compareTo(cdis_sy_term_name) == 0) {
						cdisc_sy_matched = true;
					}
				}
			}
			if (nci_ab_matched && cdisc_sy_matched) {
				String cdisc_pt = findTermNameMatchingCDISCPTSourceCode(member_code, nci_ab);
				return code + "|" + cdisc_sy + "|" + cdisc_pt;
			}
		}
		return null;
	}

	public String findTermNameMatchingCDISCPTSourceCode(String code, String sourceCode) {
		Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
		for (int j=0; j<syn_vec.size(); j++) {
			Synonym syn = (Synonym) syn_vec.elementAt(j);
			String termName = syn.getTermName();
            if (syn.getSourceCode() != null && syn.getSourceCode().length() > 0) {
				if (syn.getTermGroup().compareTo("PT") == 0 &&
					syn.getTermSource().compareTo("CDISC") == 0 &&
					syn.getSourceCode().compareTo(sourceCode) == 0) {
					return termName;
				}
			}
		}
		return null;
	}

	public boolean isRetired(String code) {
		return retired_concepts.contains(code);
	}

	public Vector generatePairedTermData() {
		Vector pairedTermData = new Vector();
		pairedTermData.add(PAIRED_TERMS_HEADER);
		try {
			Iterator it = pairedSourceTermData.keySet().iterator();
			while (it.hasNext()) {
				String code = (String) it.next();
				Vector v = (Vector) pairedSourceTermData.get(code);
				for (int i=0; i<v.size(); i++) {
					String t = (String) v.elementAt(i);
					try {
						Vector u = StringUtils.parseData(t, '|');
						String member_code = (String) u.elementAt(0); //C065047
						String src_code = (String) u.elementAt(1); //C065047

						if (src_code != null && src_code.compareTo("null") != 0) {
							String name0 = (String) u.elementAt(2); //Laboratory Test Code
							String cdisc_pt = (String) u.elementAt(3); //cdisc pt matching submission code: CD code

							String value = (String) u.elementAt(4); // CD code
							String name = name0.replace("Parameter Code", "Parameter Long Name");
							name = name.replace("Test Code", "Test Name");
							name = name.replace("Short Name", "Long Name");
							name = name.replace("Parameters Code", "Parameters");

							String submissionValueCode = value;
							if (submissionValueCode.indexOf("CD") != -1) {
								submissionValueCode = submissionValueCode.replace("CD", "");
							}
							String syns = getSynonyms(code);
							syns = decodeSpecialChar(syns);
							String def = getCDISCDefinition(code);
							String pt = getPreferredName(code);

							String targetdata = searchPairedTargetTerm(member_code, name, submissionValueCode);
							if (targetdata != null) {
								String s  = member_code + "|" + src_code + "|" + name0 + "|" + cdisc_pt + "|" +  decodeSpecialChar(targetdata)
								+ "|" + syns + "|" + def + "|" + pt;
								if (!isRetired(member_code)) {
									pairedTermData.add(s);
								}
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						System.out.println("Error processing " + t);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return pairedTermData;
	}

    public String getSubmissionValueName(String code, String submissionValueCode) {
		Vector syn_vec = (Vector) focusedCode2CDISCSYSynMap.get(code);
		if (syn_vec.size() == 1) {
			Synonym syn = (Synonym) syn_vec.elementAt(0);
			return syn.getTermName();
		}

		if (submissionValueCode.endsWith("CD")) {
			submissionValueCode = submissionValueCode.substring(0, submissionValueCode.length()-2);
		}

		Synonym syn = (Synonym) syn_vec.elementAt(0);
		return syn.getTermName();
	}

    public Vector removeDuplicates(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String t = (String) v.elementAt(i);
			if (!w.contains(t)) {
				w.add(t);
			}
		}
		w = new SortUtils().quickSort(w);
		return w;
	}


    public void saveCdiscDefinitionMap() {
		Vector w = new Vector();
		Iterator it = cdiscDefinitionMap.keySet().iterator();
		int knt = 0;
		while (it.hasNext()) {
			knt++;
			String key = (String) it.next();
			String value = (String) cdiscDefinitionMap.get(key);
			w.add(key + "|" + value);
		}
		Utils.saveToFile("CdiscDefinition.txt", w);
	}

	public void run(String root, int dataSource) { //CDISC SEND Terminology (Code C77526)
	    if (dataSource == DATA_SOURCE_NCIT_OWL) {
			focusedCodes = cdiscScanner.createFocusedCodes(root);
			HashSet codes = cdiscScanner.vector2HashSet(focusedCodes);
			Vector syn_vec = cdiscScanner.extractFULLSyns(codes);
			synonymMap = cdiscScanner.createSynonymMap(syn_vec);
			preferredNameMap = cdiscScanner.getPreferredNameMap();
			terminology = (String) preferredNameMap.get(root);
			Vector u = StringUtils.parseData(terminology, ' ');
			terminology = (String) u.elementAt(1);
			if (root.compareTo("C67497") == 0) { //C67497 CDISC-GLOSS > "Glossary Terminology.log"
				cdiscDefinitionMap = cdiscScanner.getCdiscGlossDefinitionMap();
			} else {
				cdiscDefinitionMap = cdiscScanner.getCdiscDefinitionMap();
			}
            //saveCdiscDefinitionMap();

			extensibleListMap = cdiscScanner.getExtensibleListMap();
			retired_concepts = cdiscScanner.getRetiredConcepts();
	    }
		try {
			focusedCode2CDISCPTSynMap = createFocusedCode2CDISCPTSynMap();
			focusedCode2CDISCSYSynMap = createFocusedCode2CDISCSYSynMap();
			focusedCode2CDISCSynMap   = createFocusedCode2CDISCSynMap();
			code2CDISCPTSourceCodeMap = createCode2CDISCPTSourceCodeMap();
			conceptsWithNCIAB = getCoceptsWithNCIAB();
			pairedSourceTermData = generatePairedSourceTermData();
			cdiscSY2CodeMap = createCDISCSY2CodeMap();
			pairedTermData = generatePairedTermData();
			pairedTermData = sortPairedTermData(pairedTermData);

			String outputfile = "pairedTermData_" + data_source + ".txt";
			Utils.saveToFile(outputfile, pairedTermData);
            String metadatafile = generateMetadata();

            String excelFileName = terminology + "_paired_view_" + StringUtils.getToday("yyyy_MM_dd") + ".xlsx";

            Vector<String> textfiles = new Vector();
            Vector blanks = new Vector();
            String blankfile = "readme.txt";
            Utils.saveToFile(blankfile, blanks);
            textfiles.add(blankfile);
            textfiles.add(metadatafile);
            textfiles.add(outputfile);

            Vector<String> sheetNames = new Vector();
            sheetNames.add("ReadMe");
            sheetNames.add(terminology + " " + "Paired Codelist Metadata");
            sheetNames.add(terminology + " " + "Paired Terms");
            try {
				System.out.println("Generating Excel report. Please wait ...");
            	ExcelReadWriteUtils.writeXLSXFile(excelFileName, textfiles, sheetNames, '|');
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			//ExcelFormatter.reformat(excelFileName, excelFileName);
			XLSXFormatter.reformat(excelFileName, excelFileName);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void dumpParingConceptHashMap() {
		Iterator it = paringConceptHashMap.keySet().iterator();
		int knt = 0;
		while (it.hasNext()) {
			knt++;
			String code = (String) it.next();
			System.out.println("(" + knt + ") " + code);
			Vector<Synonym> v = (Vector) paringConceptHashMap.get(code);
			for (int i=0; i<v.size(); i++) {
				Synonym syn = (Synonym) v.elementAt(i);
				System.out.println(syn.toJson());
			}
		}
	}

    public Vector getParingConceptMetadata() {
		Vector w = new Vector();
		Iterator it = paringConceptHashMap.keySet().iterator();
		int knt = 0;
		while (it.hasNext()) {
			knt++;
			String code = (String) it.next();
			Vector v = (Vector) code2TableDataMap.get(code);
			if (v != null) {
				w.addAll(v);
			}
		}
        return w;
	}

	public Synonym searchsynonymMap(String termName) {
		return searchsynonymMap(termName, "AB", "NCI");
	}


	public Synonym searchsynonymMap(String termName, String termGroup, String termSource) {
		Iterator it = synonymMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
			for (int i=0; i<syn_vec.size(); i++) {
				Synonym syn = (Synonym) syn_vec.elementAt(i);

				if (syn.getTermName().compareTo(termName) == 0 &&
					syn.getTermGroup().compareTo(termGroup) == 0 &&
					syn.getTermSource().compareTo(termSource) == 0) {
					return(syn);
				}
			}
		}
		return null;
	}

	public Synonym searchsynonymMapByLabel(String label, String termGroup, String termSource) {
		Iterator it = synonymMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
			for (int i=0; i<codelistSynonyms.size(); i++) {
				Synonym syn = (Synonym) codelistSynonyms.elementAt(i);

				if (syn.getLabel().compareTo(label) == 0 &&
					syn.getTermGroup().compareTo("AB") == 0 &&
					syn.getTermSource().compareTo("NCI") == 0) {
					return(syn);
				}
			}
		}
		return null;
	}

	public Synonym searchsynonymMapBySourceCode(String sourceCode, String termGroup, String termSource) {
		Iterator it = synonymMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
			for (int i=0; i<codelistSynonyms.size(); i++) {
				Synonym syn = (Synonym) codelistSynonyms.elementAt(i);

				if (syn.getSourceCode().compareTo(sourceCode) == 0 &&
				    syn.getTermGroup().compareTo("AB") == 0 &&
					syn.getTermSource().compareTo("NCI") == 0) {
					return(syn);
				}
			}
		}
		return null;
	}

	public Synonym searchsynonymMapByTermName(String termName, String termGroup, String termSource) {
		Iterator it = synonymMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
			for (int i=0; i<codelistSynonyms.size(); i++) {
				Synonym syn = (Synonym) codelistSynonyms.elementAt(i);

				if (syn.getTermName().compareTo(termName) == 0 &&
				    syn.getTermGroup().compareTo("AB") == 0 &&
					syn.getTermSource().compareTo("NCI") == 0) {
					return(syn);
				}
			}
		}
		return null;
	}

	public Vector<Synonym> searchPairedTerms(String code) {
		Vector syn_vec = (Vector) paringConceptHashMap.get(code);
		Synonym syn_1 = (Synonym) syn_vec.elementAt(0);
		Synonym syn_2 = (Synonym) syn_vec.elementAt(1);
		String sourceCode_1 = syn_1.getSourceCode();
		String sourceCode_2 = syn_2.getSourceCode();
		Synonym term1 = null;
		Synonym term2 = null;
		Vector w = new Vector();
		if (sourceCode_1.endsWith("CD")) {
			String t = syn_1.getTermName();
			term1 = searchsynonymMapByTermName(t, "AB", "NCI");
			if (term1 != null) {
				String label = term1.getLabel();
				label = label.replace("Test Code", "Test Name");
				label = label.replace("Short Name", "Long Name");
				term2 = searchsynonymMapByLabel(label, "SY", "CDISC");
				w.add(term1);
				w.add(term2);
			} else {
				System.out.println(t + " NCI AB not found.");
			}
		} else if (sourceCode_2.endsWith("CD")) {
			String t = syn_2.getTermName();
			term1 = searchsynonymMapByTermName(t, "AB", "NCI");
			if (term1 != null) {
				String label = term1.getLabel();
				label = label.replace("Test Code", "Test Name");
				label = label.replace("Short Name", "Long Name");
				term2 = searchsynonymMapByLabel(label, "SY", "CDISC");
				w.add(term1);
				w.add(term2);
			} else {
				System.out.println(t + " NCI AB not found.");
			}
		}
		return w;
	}

	public HashMap searchPairedTerms() {
		HashMap hmap = new HashMap();
		int knt = 0;
		int count = 0;
		int count_0 = 0;
		Iterator it = paringConceptHashMap.keySet().iterator();
		while (it.hasNext()) {
			knt++;
			String code = (String) it.next();
			Vector<Synonym> v = searchPairedTerms(code);
			if (v != null) {
				hmap.put(code, v);
			}
		}
		return hmap;
	}

	public void dumpPairedTerms(HashMap hmap) {
		int knt = 0;
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			knt++;
			String code = (String) it.next();
			Vector syn_vec = (Vector) hmap.get(code);
			Synonym syn_1 = (Synonym) syn_vec.elementAt(0);
			Synonym syn_2 = (Synonym) syn_vec.elementAt(1);
			System.out.println("\n" + code);
			System.out.println(syn_1.toJson());
			System.out.println(syn_2.toJson());
		}
		System.out.println("Number of paired terms: " + knt);
	}

	public Vector getTableData() {
		return this.tableData;
	}

	public Vector extractColumnData(Vector tableData, int colNum) {
        return extractColumnData(tableData, colNum, '\t', true);
    }

	public Vector extractColumnData(Vector tableData, int colNum, char delim, boolean excludeHeading) {
		Vector w = new Vector();
		int istart = 0;
		if (excludeHeading) {
			istart = 1;
		}
		for (int i=istart; i<tableData.size(); i++) {
			String t = (String) tableData.elementAt(i);
			Vector u = StringUtils.parseData(t, delim);
			String value = (String) u.elementAt(colNum);
			if (!w.contains(value)) {
				w.add(value);
			}
		}
		return w;
	}

	public Vector getSynonymData(Vector v) {
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			String code = (String) v.elementAt(i);
			Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
			if (syn_vec != null && syn_vec.size() > 0) {
				w.addAll(syn_vec);
			}
		}
		return w;
	}

	public void dumpSynonymData(Vector v) {
		for (int i=0; i<v.size(); i++) {
			Synonym syn = (Synonym) v.elementAt(i);
			if (syn != null) {
				System.out.println(syn.toJson());
			}
		}
	}

	public int compareMetadata(Vector w, Vector w2) {
		int knt = 0;
		for (int i=0; i<w2.size(); i++) {
			String t = (String) w2.elementAt(i);
			if (!w.contains(t)) {
				System.out.println(t);
				knt++;
			}
		}
		return knt;
	}

	public Vector<CDISCRow> getPairedSources(Vector<CDISCRow> v) {
		int knt = 0;
		Vector w = new Vector();
		for (int i=0; i<v.size(); i++) {
			CDISCRow cDISCRow = (CDISCRow) v.elementAt(i);
			String codelistCode = cDISCRow.getCodelistCode();
			String codelistName = cDISCRow.getCodelistName();
			if (codelistCode != null && codelistCode.compareTo("") != 0) {
				if (codelistName.endsWith("Test Code") ||
					codelistName.endsWith("Short Name")) {
					knt++;
					w.add(cDISCRow);
				}
			}
		}
		return w;
	}

	public HashMap<String, CDISCRow> createCodelistNameHashMap(Vector<CDISCRow> v) {
		int knt = 0;
		HashMap hmap = new HashMap();
		for (int i=0; i<v.size(); i++) {
			CDISCRow cDISCRow = (CDISCRow) v.elementAt(i);
			String codelistName = cDISCRow.getCodelistName();
			Vector w = new Vector();
			if (hmap.containsKey(codelistName)) {
				w = (Vector) hmap.get(codelistName);
			}
			w.add(cDISCRow);
			hmap.put(codelistName, w);
		}
		return hmap;
	}

	public CDISCRow row2DISCRow(String t) {
		Vector data = StringUtils.parseData(t, '\t');
		String code = (String) data.elementAt(0);
		String codelistCode = (String) data.elementAt(1);
		String extensibleList =(String) data.elementAt(2);
		String codelistName =(String) data.elementAt(3);
		String cDISCSubmissionValue =(String) data.elementAt(4);
		String cDISCSynonyms =(String) data.elementAt(5);
		String cDISCDefinition =(String) data.elementAt(6);
		String nCIPreferredTerm =(String) data.elementAt(7);

		return new CDISCRow(
			code,
			codelistCode,
			extensibleList,
			codelistName,
			cDISCSubmissionValue,
			cDISCSynonyms,
			cDISCDefinition,
			nCIPreferredTerm);
	}

	public CDISCPairedRow row2DISCPairedRow(String t) {
		Vector data = StringUtils.parseData(t, '\t');
		String code = (String) data.elementAt(0);
		String testcdParmcdCodelistCode = (String) data.elementAt(1);
		String testcdParmcdCodelistName = (String) data.elementAt(2);
		String testcdParmcdCDISCSubmissionValueCode = (String) data.elementAt(3);
		String testParmCodelistCode = (String) data.elementAt(4);
		String testParmCodelistName = (String) data.elementAt(5);
		String testParmCDISCSubmissionValueName = (String) data.elementAt(6);
		String cDISCSynonyms = (String) data.elementAt(7);
		String cDISCDefinition = (String) data.elementAt(8);
		String nCIPreferredTerm = (String) data.elementAt(9);

		return new CDISCPairedRow(
			code,
			testcdParmcdCodelistCode,
			testcdParmcdCodelistName,
			testcdParmcdCDISCSubmissionValueCode,
			testParmCodelistCode,
			testParmCodelistName,
			testParmCDISCSubmissionValueName,
			cDISCSynonyms,
			cDISCDefinition,
			nCIPreferredTerm);
	}

	private static boolean isAllUpper(String s) {
		for(char c : s.toCharArray()) {
		   if(Character.isLetter(c) && Character.isLowerCase(c)) {
			   return false;
			}
		}
		return true;
	}

	public boolean validateCodeListName(String t) {
		if (t.length() > 8) return false;
		if (!isAllUpper(t)) return false;
		return true;
	}

    public void saveFULLSYNData(List list) {
		Vector w = new Vector();
        for (int i=0; i<list.size(); i++) {
			Synonym syn = (Synonym) list.get(i);
			w.add(syn.toString());
		}
		Utils.saveToFile("full_syn.txt", w);
	}


	 public static void saveToFile(String outputfile, String t) {
		 Vector v = new Vector();
		 v.add(t);
		 saveToFile(outputfile, v);
	 }

	 public static void saveToFile(String outputfile, Vector v) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			if (v != null && v.size() > 0) {
				for (int i=0; i<v.size(); i++) {
					String t = (String) v.elementAt(i);
					pw.println(t);
				}
		    }
		} catch (Exception ex) {

		} finally {
			try {
				pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	 }

	public static void saveToFile(PrintWriter pw, Vector v) {
		if (v != null && v.size() > 0) {
			for (int i=0; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				pw.println(t);
			}
		}
	}

	public static Vector readFile(String filename)
	{
		Vector v = new Vector();
		try {
			BufferedReader in = new BufferedReader(
			   new InputStreamReader(
						  new FileInputStream(filename), "UTF8"));
			String str;
			while ((str = in.readLine()) != null) {
				v.add(str);
			}
            in.close();
		} catch (Exception ex) {
            ex.printStackTrace();
		}
		return v;
	}

    public Vector getValueSetCodes(String owlfile, String subset_code) {
		OWLScanner scanner = new OWLScanner(owlfile);
		Vector focusedCodes = scanner.extract_value_set(scanner.get_owl_vec(), subset_code);
		scanner.get_owl_vec().clear();
		Utils.saveToFile("v1.txt", focusedCodes);
		return focusedCodes;
	}


    public HashMap createsynonymMap(String owlfile) {
		HashMap hmap = new HashMap();
		String fullsynfile = "full_syn.txt";
		File file = new File(fullsynfile);
		boolean exists = file.exists();
		List list = new ArrayList();
		if (!exists) {
			OWLScanner scanner = new OWLScanner(owlfile);
			list = scanner.extractFULLSyns();
			saveFULLSYNData(list);
			scanner.get_owl_vec().clear();
		} else {
			System.out.println(fullsynfile + " exists.");
			Vector v = readFile(fullsynfile);
			for (int i=0; i<v.size(); i++) {
				String t = (String) v.elementAt(i);
				Vector u = StringUtils.parseData(t, '|');
				String code = (String) u.elementAt(0);
				String label = (String) u.elementAt(1);
				String termName = (String) u.elementAt(3);
				String termGroup = (String) u.elementAt(5);
				String termSource = (String) u.elementAt(7);
				String sourceCode = (String) u.elementAt(9);
				if (sourceCode.compareTo("null") == 0) sourceCode = "";
				String subSourceName = (String) u.elementAt(11);
				if (subSourceName.compareTo("null") == 0) subSourceName = "";
				Synonym syn = new Synonym(
					code,
					label,
					termName,
					termGroup,
					termSource,
					sourceCode,
					subSourceName,
					null);
				list.add(syn);
			}
		}
		try {

			for (int i=0; i<list.size(); i++) {
				Synonym syn = (Synonym) list.get(i);
				String code = syn.getCode();
				Vector w = new Vector();
				if (hmap.containsKey(code)) {
					w = (Vector) hmap.get(code);
				}
				w.add(syn);
				hmap.put(code, w);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return hmap;
	}

	public Vector getSubmissionValueCodes(Vector syn_vec) {
		Vector w = new Vector();
		Vector sourceCodes = new Vector();
		for (int i=0; i<syn_vec.size(); i++) {
			Synonym syn = (Synonym) syn_vec.elementAt(i);
			if (syn.getSourceCode() != null && syn.getSourceCode().length() > 0) {
				if (syn.getTermGroup().compareTo("PT") == 0 &&
				    syn.getTermSource().compareTo("CDISC") == 0) {
                    sourceCodes.add(syn.getSourceCode());
				}
			}
		}

		for (int i=0; i<sourceCodes.size(); i++) {
			String t = (String) sourceCodes.elementAt(i);
			if (t.indexOf("CD") != -1) {
				String s = t.replace("CD", "");
				if (sourceCodes.contains(s)) {
					w.add(t);
				}
			}
		}
		return w;
	}


	public String decodeSpecialChar(String line) {
		line = line.replaceAll("&apos;", "'");
		line = line.replaceAll("&amp;", "&");
		line = line.replaceAll("&lt;", "<");
		line = line.replaceAll("&gt;", ">");
		line = line.replaceAll("&quot;", "\"");
		return line;
	}

	public String getExtensibleList(String code) {
		return  (String) extensibleListMap.get(code);
	}

	public String getPreferredName(String code) {
		return  (String) preferredNameMap.get(code);
	}

	public String getCDISCDefinition(String code) {
		return  (String) cdiscDefinitionMap.get(code);
	}

	public String getSynonyms(String code) {
		Vector w = new Vector();
		Vector syn_vec = (Vector) synonymMap.get(code);
		if (syn_vec != null && syn_vec.size() > 0) {
			for (int i=0; i<syn_vec.size(); i++) {
				Synonym syn = (Synonym) syn_vec.elementAt(i);

				if (syn.getTermGroup().compareTo("SY") == 0 &&
					syn.getTermSource().compareTo("CDISC") == 0) {
					String termName = syn.getTermName();
					if (!w.contains(termName)) {
						w.add(termName);
					}
				}
			}
			w = new SortUtils().quickSort(w);
			StringBuffer buf = new StringBuffer();
			for (int i=0; i<w.size(); i++) {
				String t = (String) w.elementAt(i);
				buf.append(t).append("; ");
			}
			String s = buf.toString();
			s = s.substring(0, s.length()-2);
			return s;
		} else {
			return "";
		}
	}

	public Vector sortPairedTermData(Vector pairedTermData) {
		HashMap hmap = new HashMap();
		Vector keys = new Vector();
		for (int i=1; i<pairedTermData.size(); i++) {
			String line = (String) pairedTermData.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String codelistcode = (String) u.elementAt(1);
			String key = codelistcode + "|" + code;
			hmap.put(key, line);
			keys.add(key);
		}
		keys = new SortUtils().quickSort(keys);
		Vector w = new Vector();
		w.add((String) pairedTermData.elementAt(0));
		for (int i=0; i<keys.size(); i++) {
			String key = (String) keys.elementAt(i);
			String value = (String) hmap.get(key);
			w.add(value);
		}
		return w;
	}

	public String generateMetadata() {
		Vector w = new Vector();
		w.add(METADATA_HEADER);
		for (int i=1; i<pairedTermData.size(); i++) {
			String line = (String) pairedTermData.elementAt(i);
			Vector u = StringUtils.parseData(line, '|');
			String code = (String) u.elementAt(0);
			String codelistcode = (String) u.elementAt(1);
			String yesorno = getExtensibleList(codelistcode);
			String codelistname = (String) u.elementAt(2);
			String submissionvalue = (String) u.elementAt(3);
			String syns = getSynonyms(code);
			syns = decodeSpecialChar(syns);
			String def = getCDISCDefinition(code);
			String pt = getPreferredName(code);
			String t = code
				+ "|" + codelistcode
				+ "|" + yesorno
				+ "|" + codelistname
				+ "|" + submissionvalue
				+ "|" + syns
				+ "|" + def
				+ "|" + pt;
			w.add(decodeSpecialChar(t));

			codelistcode = (String) u.elementAt(4);
			yesorno = getExtensibleList(codelistcode);
			codelistname = (String) u.elementAt(5);
			submissionvalue = (String) u.elementAt(6);
			syns = getSynonyms(code);
			syns = decodeSpecialChar(syns);
			def = getCDISCDefinition(code);
			pt = getPreferredName(code);
			t = code
				+ "|" + codelistcode
				+ "|" + yesorno
				+ "|" + codelistname
				+ "|" + submissionvalue
				+ "|" + syns
				+ "|" + def
				+ "|" + pt;
			w.add(decodeSpecialChar(t));
		}
		String outputfile = "metadata_" + data_source + ".txt";
		Utils.saveToFile(outputfile, w);
		return outputfile;
	}


    public static void main(String[] args) {
		long ms = System.currentTimeMillis();
		String owlfile = args[0];
		String root = args[1];
		CDISCPairing CDISCPairing = new CDISCPairing(owlfile);
		CDISCPairing.run(root, DATA_SOURCE_NCIT_OWL);
		System.out.println("Total run time (ms): " + (System.currentTimeMillis() - ms));
	}
}

