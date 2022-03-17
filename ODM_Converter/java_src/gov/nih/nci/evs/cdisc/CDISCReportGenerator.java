package gov.nih.nci.evs.cdisc;
import gov.nih.nci.evs.bean.*;
import gov.nih.nci.evs.reportwriter.formatter.*;
import gov.nih.nci.evs.util.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.text.SimpleDateFormat;
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
public class CDISCReportGenerator {
    public static String HEADING = "Code	Codelist Code	Codelist Extensible (Yes/No)	Codelist Name	CDISC Submission Value	CDISC Synonym(s)	CDISC Definition	NCI Preferred Term";

    Vector focusedCodes = null;
    Vector codeListCodes = null;
    HashMap subsetMemberHashMap = null;


    HashMap preferredNameMap = null;
    HashMap synonymMap = null;
    HashMap cdiscDefinitionMap = null;
    HashMap cdiscGlossDefinitionMap = null;
    HashMap extensibleListMap = null;
    HashSet retired_concepts = null;

    String owlfile = null;
    String version = null;

    CDISCScanner cdiscScanner = null;

    static String CDISC = "CDISC";
    static String CDISC_GLOSS = "CDISC-GLOSS";

    String SOURCE_NAME = CDISC;

    public CDISCReportGenerator(String owlfile) {
        cdiscScanner = new CDISCScanner(owlfile);
		version = cdiscScanner.getVersion();
/*
	    extensibleListMap = cdiscScanner.getExtensibleListMap();
	    cdiscDefinitionMap = cdiscScanner.getCdiscDefinitionMap();
	    cdiscGlossDefinitionMap = cdiscScanner.getCdiscGlossDefinitionMap();
	    preferredNameMap = cdiscScanner.getPreferredNameMap();
	    retired_concepts = cdiscScanner.getRetiredConcepts();

	    //Vector w = new Vector();
	    //Iterator it = retired_concepts.iterator();
	    //while (it.hasNext()) {
			//w.add(it.next());
		//}
*/
	}

    public static void saveToFile(String outputfile, Vector v) {
        try {
            FileOutputStream output = new FileOutputStream(outputfile);
            for (int i=0; i<v.size(); i++) {
				String data = (String) v.elementAt(i);
				if (i < v.size()) {
					data = data + "\n";
				}
				byte[] array = data.getBytes();
				output.write(array);
			}
            output.close();
        } catch(Exception e) {
            e.getStackTrace();
        }
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


    public Vector getAllDescendants(String code) {
		Vector w = new Vector();
		Vector v = cdiscScanner.getSubclassCodes(code);
		if (v != null && v.size()> 0) {
			for (int i=0; i<v.size(); i++) {
				String sub = (String) v.elementAt(i);
				Vector u = getAllDescendants(sub);
				w.addAll(u);
			}
		}
		return w;
	}

	public Vector getFocusedCodes(String root) {
		Vector v = cdiscScanner.getSubclassCodes(root);
		for (int i=0; i<v.size(); i++) {
			String subsetCode = (String) v.elementAt(i);
			Vector w = cdiscScanner.getSubsetMemberCodes(subsetCode);
			v.addAll(w);
		}
		return v;
	}

    public HashMap createSynonymMap(Vector syn_vec) {
		HashMap hmap = new HashMap();
		for (int i=0; i<syn_vec.size(); i++) {
			Synonym syn = (Synonym) syn_vec.elementAt(i);
			String code = syn.getCode();
			Vector w = new Vector();
			if (hmap.containsKey(code)) {
				w = (Vector) hmap.get(code);
			}
			w.add(syn);
			hmap.put(code, w);
		}
		return hmap;
	}

//KLO, 10132021
	public String findTermNameMatchingCDISCPTSourceCode(String code, String sourceCode) {
		Vector<Synonym> syn_vec = (Vector) synonymMap.get(code);
		for (int j=0; j<syn_vec.size(); j++) {
			Synonym syn = (Synonym) syn_vec.elementAt(j);
			String termName = syn.getTermName();
            if (syn.getSourceCode() != null && syn.getSourceCode().length() > 0) {
				if (syn.getTermGroup().compareTo("PT") == 0 &&
					syn.getTermSource().compareTo(SOURCE_NAME) == 0 &&
					syn.getSourceCode().compareTo(sourceCode) == 0) {
					return termName;
				}
			}
		}
		return null;
	}


    public void updateSynonymMap(Vector syn_vec) {
		if (syn_vec == null) return;
		for (int i=0; i<syn_vec.size(); i++) {
			Synonym syn = (Synonym) syn_vec.elementAt(i);
			String code = syn.getCode();
			Vector w = new Vector();
			if (synonymMap.containsKey(code)) {
				w = (Vector) synonymMap.get(code);
			}
			w.add(syn);
			synonymMap.put(code, w);
		}
	}

	public String getCodeListName(String code) {
		if (!synonymMap.containsKey(code)) return null;
		Vector syn_vec = (Vector) synonymMap.get(code);
		for (int i=0; i<syn_vec.size(); i++) {
			Synonym syn = (Synonym) syn_vec.elementAt(i);
			if (syn.getTermSource() != null && syn.getTermGroup()!= null ) {
				if (syn.getTermSource().compareTo(SOURCE_NAME) == 0 && syn.getTermGroup().compareTo("SY") == 0) {
					return syn.getTermName();
				}
			}
		}

		/*
		//find NCI AB
		String nci_ab = null;
		for (int i=0; i<syn_vec.size(); i++) {
			Synonym syn = (Synonym) syn_vec.elementAt(i);
			if (syn.getTermSource() != null && syn.getTermGroup()!= null ) {
				if (syn.getTermSource().compareTo("NCI") == 0 && syn.getTermGroup().compareTo("AB") == 0) {
					nci_ab = syn.getTermName();
					break;
				}
			}
		}

		if (nci_ab != null && nci_ab.indexOf("CD") == -1) {
			for (int i=0; i<syn_vec.size(); i++) {
				Synonym syn = (Synonym) syn_vec.elementAt(i);
				if (syn.getTermSource() != null && syn.getTermGroup()!= null ) {
					if (syn.getTermSource().compareTo(SOURCE_NAME) == 0 && syn.getTermGroup().compareTo("PT") == 0) {
						return syn.getTermName();
					}
				}
			}
		} else if (nci_ab != null && nci_ab.indexOf("CD") != -1) {
			for (int i=0; i<syn_vec.size(); i++) {
				Synonym syn = (Synonym) syn_vec.elementAt(i);
				if (syn.getTermSource() != null && syn.getTermGroup()!= null ) {
					if (syn.getTermSource().compareTo(SOURCE_NAME) == 0 && syn.getTermGroup().compareTo("SY") == 0) {
						return syn.getTermName();
					}
				}
			}
		} else {
			for (int i=0; i<syn_vec.size(); i++) {
				Synonym syn = (Synonym) syn_vec.elementAt(i);
				if (syn.getTermSource() != null && syn.getTermGroup()!= null ) {
					if (syn.getTermSource().compareTo(SOURCE_NAME) == 0 && syn.getTermGroup().compareTo("SY") == 0) {
						return syn.getTermName();
					}
				}
			}
		}
		*/

		return null;
	}

/*
	public String getSubmissionValue(String code) {
		if (!synonymMap.containsKey(code)) {
			return null;
		}
		Vector syn_vec = (Vector) synonymMap.get(code);
		Synonym cdiscPT = null;
		for (int i=0; i<syn_vec.size(); i++) {
			Synonym syn = (Synonym) syn_vec.elementAt(i);
			if (syn.getTermSource() != null && syn.getTermGroup()!= null ) {
				if (syn.getTermSource().compareTo(SOURCE_NAME) == 0 && syn.getTermGroup().compareTo("PT") == 0) {
					cdiscPT = syn;
					//return syn.getTermName();
					String sourceCode = syn.getSourceCode();
					if (sourceCode != null && sourceCode.indexOf("CD") != -1) {
						return syn.getTermName();
					}
				}
			}
		}
		if (cdiscPT != null) {
			return cdiscPT.getTermName();
		}
		return null;
	}
*/

	public String getSubmissionValue(String code) {
		if (!synonymMap.containsKey(code)) {
			return null;
		}
		Vector syn_vec = (Vector) synonymMap.get(code);
		for (int i=0; i<syn_vec.size(); i++) {
			Synonym syn = (Synonym) syn_vec.elementAt(i);
			if (syn.getTermSource() != null && syn.getTermGroup()!= null ) {
				if (syn.getTermSource().compareTo(SOURCE_NAME) == 0 && syn.getTermGroup().compareTo("PT") == 0) {
					return syn.getTermName();
				}
			}
		}
		return null;
	}

	public int getSourcePTCount(String code) {
		int knt = 0;
		Vector syn_vec = (Vector) synonymMap.get(code);
		for (int i=0; i<syn_vec.size(); i++) {
			Synonym syn = (Synonym) syn_vec.elementAt(i);
			if (syn.getTermSource() != null && syn.getTermGroup()!= null ) {
				if (syn.getTermSource().compareTo(SOURCE_NAME) == 0 && syn.getTermGroup().compareTo("PT") == 0) {
				    knt++;
				}
			}
		}
		return knt;
	}

	public String getSubmissionValue(String code, String codeListCode) {
		if (!synonymMap.containsKey(code) || !synonymMap.containsKey(codeListCode)) {
			return null;
		}
		int knt = getSourcePTCount(code);
		if (knt == 1) {
			return getSubmissionValue(code);
		}

		// find NCI AB of codeListCode:
		String termName = null;
		Vector syn_vec = (Vector) synonymMap.get(codeListCode);
		for (int i=0; i<syn_vec.size(); i++) {
			Synonym syn = (Synonym) syn_vec.elementAt(i);
			if (syn.getTermSource() != null && syn.getTermGroup()!= null ) {
				if (syn.getTermSource().compareTo("NCI") == 0 && syn.getTermGroup().compareTo("AB") == 0) {
					termName = syn.getTermName();
					break;
				}
			}
		}

		if (termName == null) {
			System.out.println("No NCI AB found --> termName == null, code: " + code + " codeListCode: " + codeListCode);
			return getSubmissionValue(code);
		}

		syn_vec = (Vector) synonymMap.get(code);
		for (int i=0; i<syn_vec.size(); i++) {
			Synonym syn = (Synonym) syn_vec.elementAt(i);
			if (syn.getTermSource() != null && syn.getTermGroup()!= null ) {
				if (syn.getTermSource().compareTo(SOURCE_NAME) == 0 && syn.getTermGroup().compareTo("PT") == 0) {
					if (syn.getSourceCode() != null && syn.getSourceCode().compareTo(termName) == 0) {
						termName = syn.getTermName();
						break;
				    }
				}
			}
		}
		return termName;
	}


	public String getCDISCSynonyms(String code) {
		if (!synonymMap.containsKey(code)) return "";
		StringBuffer buf = new StringBuffer();
		Vector syn_vec = (Vector) synonymMap.get(code);
		HashSet names = new HashSet();
		Vector name_vec = new Vector();
		for (int i=0; i<syn_vec.size(); i++) {
			Synonym syn = (Synonym) syn_vec.elementAt(i);
			if (syn.getTermSource() != null && syn.getTermGroup()!= null ) {
				if (syn.getTermSource().compareTo(SOURCE_NAME) == 0 && syn.getTermGroup().compareTo("SY") == 0) {
					if (!names.contains(syn.getTermName())) {
						name_vec.add(syn.getTermName());
						/*
						names.add(syn.getTermName());
						buf.append(syn.getTermName()).append(" ;");
						*/
					}
				}
			}
		}
		name_vec = new SortUtils().quickSort(name_vec);
		for (int i=0; i<name_vec.size(); i++) {
			String name = (String) name_vec.elementAt(i);
            buf.append(name).append("; ");
		}
		String t = buf.toString();
		if (t.length() == 0) {
			return "";
		}
		return t.substring(0, t.length()-2);
	}

	public String getCdiscDefinition(String code) {
		if (cdiscDefinitionMap == null) {
			return null;
		}
		if (!cdiscDefinitionMap.containsKey(code)) return null;
		return (String) cdiscDefinitionMap.get(code);
	}

	public String getPreferredName(String code) {
		if (!preferredNameMap.containsKey(code)) return null;
		return (String) preferredNameMap.get(code);
	}


    public void saveSynonymMap(String filename) {
		Vector w = new Vector();
		Iterator it = synonymMap.keySet().iterator();
		while (it.hasNext()) {
			String code = (String) it.next();
			Vector syn_vec = (Vector) synonymMap.get(code);
			for (int i=0; i<syn_vec.size(); i++) {
				Synonym syn = (Synonym) syn_vec.elementAt(i);
				w.add(syn.toJson());
			}
		}
		saveToFile(filename, w);
	}

    public boolean isRetired(String code) {
		if (retired_concepts == null) return false;
		return retired_concepts.contains(code);
	}

    public Vector createFocusedCodes(HashMap subsetMemberHashMap) {
		Vector w = new Vector();
		Iterator it = subsetMemberHashMap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			w.add(key);
			Vector v = (Vector) subsetMemberHashMap.get(key);
			if (v.size() > 0) {
				w.addAll(v);
			}
		}
        return w;
	}

    public Vector getAllDescendantCodes(String root) {
		Vector w = new Vector();
		Vector subs = cdiscScanner.getSubclassCodes(root);
		if (subs != null && subs.size() > 0) {
			w.addAll(subs);
			for (int i=0; i<subs.size(); i++) {
				String sub = (String) subs.elementAt(i);
				w.addAll(getAllDescendantCodes(sub));
			}
		}
		return new SortUtils().quickSort(w);
	}

	public Vector getCodeListCodes(String root) {
		Vector w = new Vector();
		w.addAll(getAllDescendantCodes(root));
		return w;
	}

	public HashMap createSubsetMemberHashMap(Vector codeListCodes) {
		HashMap hmap = new HashMap();
		for (int i=0; i<codeListCodes.size(); i++) {
			String subset_code = (String) codeListCodes.elementAt(i);
			Vector subsets = cdiscScanner.getSubsetMemberCodes(subset_code);
			if (subsets != null && subsets.size() > 0) {
				hmap.put(subset_code, subsets);
			} else {
				hmap.put(subset_code, new Vector());
			}
		}
		return hmap;
	}

	public String decodeSpecialChar(String line) {
		line = line.replaceAll("&apos;", "'");
		line = line.replaceAll("&amp;", "&");
		line = line.replaceAll("&lt;", "<");
		line = line.replaceAll("&gt;", ">");
		line = line.replaceAll("&quot;", "\"");
		return line;
	}

/*
Code	Codelist Code	Codelist Extensible (Yes/No)	Codelist Name	            CDISC Submission Value	CDISC Synonym(s)	CDISC Definition	NCI Preferred Term
C100129		            Yes	                            Category of Questionnaire	QSCAT	                Category of Questionnaire	A grouping of observations within the Questionnaires domain.	CDISC Questionnaire Category Terminology
C100759	C100129		                                    Category of Questionnaire	BPI	BPI1	Brief Pain Inventory (BPI) (copyright 1991 Charles S. Cleeland, PhD, Pain Research Group, All Right Reserved).	Brief Pain Inventory Questionnaire
*/

    public Vector sort2(Vector data) {
		Vector w = new Vector();
		for (int i=0; i<data.size(); i++) {
			String line = (String) data.elementAt(i);
			Vector u = parseData(line, '\t');
			String codelist = (String) u.elementAt(1);
			if (codelist.length() == 0) {
				w.add(line);
				data.remove(i);
			}
		}
		HashMap hmap = new HashMap();
		for (int i=0; i<data.size(); i++) {
			String line = (String) data.elementAt(i);
			Vector u = parseData(line, '\t');
			String code = (String) u.elementAt(1);
			String codelistName = (String) u.elementAt(3);
			String submissionValue = (String) u.elementAt(4);
			String key = codelistName + "$" + submissionValue + "$" + code;
			hmap.put(key, line);
		}
		Vector key_vec = new Vector();
		Iterator it = hmap.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			key_vec.add(key);
		}
		key_vec = new SortUtils().quickSort(key_vec);
		for (int i=0; i<key_vec.size(); i++) {
			String key = (String) key_vec.elementAt(i);
			String t = (String) hmap.get(key);
			w.add(t);
		}
		return w;
	}

	public Vector sort(Vector v) {
		// The content needs to be sorted alphabetically by CDISC submission value within a codelist
		// and also alphabetically by codelist long name.
		Vector w = new Vector();
		w.add(HEADING);
		String prev_key = "";
		HashMap hmap = new HashMap();
		for (int i=1; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			Vector u = parseData(line, '\t');
			String code = (String) u.elementAt(1);
			String codelistName = (String) u.elementAt(3);
			String submissionValue = (String) u.elementAt(4);
			String key = codelistName;
			Vector lines = new Vector();
			if (hmap.containsKey(key)) {
				lines = (Vector) hmap.get(key);
			}
			lines.add(line);
			hmap.put(key, lines);
		}

		Iterator it = hmap.keySet().iterator();
		Vector key_vec = new Vector();
		while (it.hasNext()) {
			String key = (String) it.next();
			key_vec.add(key);
		}
		key_vec = new SortUtils().quickSort(key_vec);
		for (int k=0; k<key_vec.size(); k++) {
			String codelistName = (String) key_vec.elementAt(k);
			Vector lines = (Vector) hmap.get(codelistName);
			lines = sort2(lines);
			w.addAll(lines);
		}
		return w;
	}

	public String run(String root) {
		System.out.println("Subset root concept code: " + root);
		synonymMap = new HashMap();
		String label = cdiscScanner.getPreferredName(root);
		String textfile = label + ".txt";
		System.out.println("Generating " + textfile + " -- please wait.");

		if (label.indexOf("Glossary") != -1) {
			SOURCE_NAME = CDISC_GLOSS;
			cdiscDefinitionMap = cdiscScanner.getCdiscGlossDefinitionMap();
		} else {
			cdiscDefinitionMap = cdiscScanner.getCdiscDefinitionMap();
		}

	    extensibleListMap = cdiscScanner.getExtensibleListMap();
	    preferredNameMap = cdiscScanner.getPreferredNameMap();
	    retired_concepts = cdiscScanner.getRetiredConcepts();

		Vector v = new Vector();
		v.add(HEADING);

        codeListCodes = getCodeListCodes(root);
        if (codeListCodes == null || codeListCodes.size() == 0) {
			codeListCodes = new Vector();
			codeListCodes.add(root);
		}
		subsetMemberHashMap = createSubsetMemberHashMap(codeListCodes);
		focusedCodes = createFocusedCodes(subsetMemberHashMap);

        HashSet hset = cdiscScanner.vector2HashSet(focusedCodes);
        if (!hset.contains(root)) {
        	hset.add(root);
		}
        Vector syn_vec = cdiscScanner.extractFULLSyns(hset);
        synonymMap = createSynonymMap(syn_vec);
        Vector subset_codes = cdiscScanner.getSubclassCodes(root);

        for (int i=0; i<codeListCodes.size(); i++) {
			String code = (String) codeListCodes.elementAt(i);
			String codelistName = getCodeListName(code);
			String submissionValue = getSubmissionValue(code);
			if (submissionValue != null && submissionValue.compareTo("null") != 0) {
				String yetOrNo = (String) extensibleListMap.get(code);
				//KLO, 12/08/2021
				if (yetOrNo == null) {
					yetOrNo = "";
				}
				String s = getCDISCSynonyms(code);
				s = decodeSpecialChar(s);
				String def = getCdiscDefinition(code);
				String pref_name = getPreferredName(code);
				String line = code + "\t" + "" + "\t" + yetOrNo + "\t" + codelistName + "\t" + submissionValue + "\t" + s + "\t" + def + "\t" + pref_name;
				v.add(decodeSpecialChar(line));
				//Vector members = cdiscScanner.getSubsetMemberCodes(code);
				Vector members = (Vector) subsetMemberHashMap.get(code);
				for (int j=0; j<members.size(); j++) {
					String member = (String) members.elementAt(j);
					if (!isRetired(member)) {
						submissionValue = getSubmissionValue(member, code);
						s = getCDISCSynonyms(member);
						s = decodeSpecialChar(s);
						def = getCdiscDefinition(member);
						pref_name = getPreferredName(member);
						line = member + "\t" + code + "\t" + "\t" + codelistName + "\t" + submissionValue + "\t" + s + "\t" + def + "\t" + pref_name;
						v.add(decodeSpecialChar(line));
					}
				}
			}
		}
        v = sort(v);
		saveToFile(textfile, v);
		generateExcel(textfile, label);
		return textfile;
    }

    public void generateExcel(String textfile, String pref_name) {
		try {
			new AsciiToExcelFormatter().convert(textfile, "\t", textfile.replace(".txt", ".xls"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

/*
		Vector v = new Vector();
		String readmefile = "readme.txt";
		v.add("To be replaced.");
		saveToFile(readmefile, v);
		String excelfile = pref_name + ".xlsx";
    	char delim = '\t';
		String headerColor = ExcelWriter.RED;

		Vector datafile_vec = new Vector();
		datafile_vec.add(readmefile);
		datafile_vec.add(textfile);

		Vector sheetLabel_vec = new Vector();
		sheetLabel_vec.add("ReadMe");
		sheetLabel_vec.add(pref_name);

		new ExcelWriter().writeToXSSF(datafile_vec, excelfile, delim, sheetLabel_vec, headerColor);
		System.out.println(excelfile + " generated.");
	}
*/

    public static void main(String args[]) {
        if (args == null || args.length != 2) {
			System.out.println("Command line parameters: (1) ThesaurusInferred_forTS.owl  (2): Root concept code (e.g., C77526)");
			System.exit(1);
		}
		String owlfile = args[0];
		String root = args[1];
        new CDISCReportGenerator(owlfile).run(root);
    }

}
