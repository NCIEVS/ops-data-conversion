package gov.nih.nci.evs.cdisc;
import gov.nih.nci.evs.bean.*;
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
public class CDISCScanner {
    String owlfile = null;

    Vector focusedCodes = new Vector();
    HashMap preferredNameMap = null;
    HashMap synonymMap = null;
    HashMap cdiscDefinitionMap = null;
    HashMap cdiscGlossDefinitionMap = null;
    HashMap extensibleListMap = null;
    HashSet retired_concepts = new HashSet();
    HashMap code2LabelMap = null;//getCode2LabelMap()

    OWLScanner owlscanner = null;
    Vector owl_vec = null;
    Vector parent_child_vec = null;
    Vector a8_vec = null;
    HashMap parent_child_hmap = null;
    HashMap subset_hmap = null;
    String version = null;

    private String CDISC_SOURCE = "CDISC";

    public CDISCScanner(String owlfile) {
		this.owlfile = owlfile;
		initialize();
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

	public void set_CDISC_SOURCE(String source) {
		CDISC_SOURCE = source;
	}

	public void setFocusedCodes(Vector focusedCodes) {
		this.focusedCodes = focusedCodes;
	}

	public String getVersion() {
		return this.version;
	}

	public String getLabel(String code) {
		return (String) code2LabelMap.get(code);
	}

	public Vector getSubclassCodes(String parentCode) {
		return (Vector) parent_child_hmap.get(parentCode);
	}

	public HashMap create_parent_child_hmap() {
		HashMap hmap = new HashMap();
		for (int i=0; i<parent_child_vec.size(); i++) {
			String line = (String) parent_child_vec.elementAt(i);
			Vector u = parseData(line, '|');
			String parent_code = (String) u.elementAt(1);
			String child_code = (String) u.elementAt(3);
			Vector w = new Vector();
			if (hmap.containsKey(parent_code)) {
				w = (Vector) hmap.get(parent_code);
			}
			w.add(child_code);
			hmap.put(parent_code, w);
		}
		return hmap;
	}

	public HashMap create_subset_hmap() {
		HashMap hmap = new HashMap();
		for (int i=0; i<a8_vec.size(); i++) {
			String line = (String) a8_vec.elementAt(i);
			Vector u = parseData(line, '|');
			String member_code = (String) u.elementAt(0);
			String subset_code = (String) u.elementAt(2);
			Vector w = new Vector();
			if (hmap.containsKey(subset_code)) {
				w = (Vector) hmap.get(subset_code);
			}
			w.add(member_code);
			hmap.put(subset_code, w);
		}
		return hmap;
	}

	public HashSet getRetiredConcepts() {
		return retired_concepts;
	}

	public Vector getFocusedCodes() {
		return focusedCodes;
	}

	public HashMap getPreferredNameMap() {
		return preferredNameMap;
	}

	public String getPreferredName(String code) {
		return (String) preferredNameMap.get(code);
	}

	public HashMap getSynonymMap() {
		return synonymMap;
	}

	public HashMap getCdiscDefinitionMap() {
		return cdiscDefinitionMap;
	}

	public HashMap getCdiscGlossDefinitionMap() {
		return cdiscGlossDefinitionMap;
	}

	public HashMap getExtensibleListMap() {
		return extensibleListMap;
	}


	public void initialize() {
		System.out.println("Initialization in progress ...");
		owlscanner = new OWLScanner(owlfile);
		owl_vec = owlscanner.get_owl_vec();
		parent_child_vec = owlscanner.extractHierarchicalRelationships(owl_vec);
		String associationCode = "A8";
		a8_vec = owlscanner.extractAssociations(owl_vec, associationCode);
		parent_child_hmap = create_parent_child_hmap();
		subset_hmap = create_subset_hmap();

		version = owlscanner.extractVersion();
		code2LabelMap = owlscanner.getCode2LabelMap();
		//focusedCodes = owlscanner.extract_value_set(owl_vec, subset_code);
		// preferred name
		//C100122|P108|Pepsinogen Measurement
		preferredNameMap = new HashMap();
		String propertyCode = "P108";
		Vector preferredNames = owlscanner.extractProperties(owl_vec, propertyCode);
		//Utils.saveToFile(propertyCode + ".txt", preferredNames);
        for (int i=0; i<preferredNames.size(); i++) {
			String line = (String) preferredNames.elementAt(i);
			Vector u = parseData(line, '|');
			String code = (String) u.elementAt(0);
			//if (focusedCodes.contains(code)) {
				preferredNameMap.put((String) u.elementAt(0), decodeSpecialChar((String) u.elementAt(2)));
			//}
		}

		retired_concepts = new HashSet();
		propertyCode = "P310";
		Vector w3 = owlscanner.extractProperties(owl_vec, propertyCode);
		//Utils.saveToFile(propertyCode + ".txt", preferredNames);
        for (int i=0; i<w3.size(); i++) {
			String line = (String) w3.elementAt(i);
			Vector u = parseData(line, '|');
			String code = (String) u.elementAt(0);
			//if (focusedCodes.contains(code)) {
				String status = (String) u.elementAt(2);
				if (status.compareTo("Retired_Concept") == 0) {
					retired_concepts.add(code);
			    }
			//}
		}

		propertyCode = "P325";
		Vector defs = owlscanner.extractAxiomData(propertyCode);

		//Utils.saveToFile(propertyCode + ".txt", defs);
		//A procedure to evaluate the health of the an individual after receiving a heart transplant.|C100005|P325|A procedure to evaluate the health of the an individual after receiving a heart transplant.|P378$CDISC
        //Vector w = owlscanner.extractPropertiesWithQualifiers(defs);
        cdiscDefinitionMap = new HashMap();
        cdiscGlossDefinitionMap = new HashMap();
		//PropertyValue|code|propertyCode|Description|P378$CDISC
        for (int i=0; i<defs.size(); i++) {
			String line = (String) defs.elementAt(i);
			if (line.indexOf("P378$CDISC") != -1 || line.indexOf("P378$CDISC-GLOSS") != -1) {
				Vector u = parseData(line, '|');
				String code = (String) u.elementAt(1);
				String alt_def = (String) u.elementAt(3);
				alt_def = decodeSpecialChar(alt_def);
				String source_str = (String) u.elementAt(4);
				Vector u2 = parseData(source_str, '$');
				String src = (String) u2.elementAt(1);
				if (src.compareTo("CDISC") == 0) {
					cdiscDefinitionMap.put(code, alt_def);
				} else {
					cdiscGlossDefinitionMap.put(code, alt_def);
				}
			}
		}
		propertyCode = "P90";
		Vector syns = owlscanner.extractAxiomData(propertyCode);
		//Utils.saveToFile(propertyCode + ".txt", syns);
		synonymMap = new HashMap();
		for (int i=0; i<syns.size(); i++) {
			String line = (String) syns.elementAt(i);
			//CSS1002C|C113894|P90|CSS1002C|P383$PT|P384$CDISC|P385$QS-C-SSRS Pediatric/Cognitively Impaired Lifetime/Recent TESTCD
			Vector u = parseData(line, '|');
			String code = (String) u.elementAt(1);
			//if (focusedCodes.contains(code)) {
				Synonym syn = string2Synonym(line);
				Vector w = new Vector();
				if (synonymMap.containsKey(code)) {
					w = (Vector) synonymMap.get(code);
				}
				w.add(syn);
				synonymMap.put(code, w);
			//}
		}

		propertyCode = "P361";
		Vector extensibleList = owlscanner.extractProperties(owl_vec, propertyCode);
		//Utils.saveToFile(propertyCode + ".txt", extensibleList);
		extensibleListMap = new HashMap();
		for (int i=0; i<extensibleList.size(); i++) {
			String line = (String) extensibleList.elementAt(i);
			Vector u = parseData(line, '|');
			extensibleListMap.put((String) u.elementAt(0), (String) u.elementAt(2));
		}
	}

	public String getExtensibleList(String code) {
		return  (String) extensibleListMap.get(code);
	}

	public String getCDISCPreferredName(String code) {
		return  (String) preferredNameMap.get(code);
	}

	public String getCDISCDefinition(String code) {
		return  (String) cdiscDefinitionMap.get(code);
	}

	public String getCDISCGlossDefinition(String code) {
		return  (String) cdiscGlossDefinitionMap.get(code);
	}

	public String getSynonyms(String code) {
		Vector w = new Vector();
		Vector syn_vec = (Vector) synonymMap.get(code);
		for (int i=0; i<syn_vec.size(); i++) {
			Synonym syn = (Synonym) syn_vec.elementAt(i);
			String termName = syn.getTermName();
			if (!w.contains(termName)) {
				w.add(termName);
			}
		}
		w = new SortUtils().quickSort(w);
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<w.size(); i++) {
			String t = (String) w.elementAt(i);
			buf.append(t).append(";");
		}
		String s = buf.toString();
		s = s.substring(0, s.length()-1);
		return s;
	}

    public Synonym string2Synonym(String line) {
		Vector u = parseData(line, '|');
		String label = (String) u.elementAt(0);
		String code = (String) u.elementAt(1);
		String termName = (String) u.elementAt(3);
		//String qualifiers = (String) u.elementAt(4);

		String termGroup = null;
		String termSource = null;
		String sourceCode = null;
		String subSourceName = null;
		String subSourceCode = null;

		if (u.size() > 4) {
			String group = (String) u.elementAt(4);
			Vector v = parseData(group, '$');
			termGroup = (String) v.elementAt(1);
		}
		if (u.size() > 5) {
			String source = (String) u.elementAt(5);
			Vector v = parseData(source, '$');
			termSource = (String) v.elementAt(1);
		}
		if (u.size() > 6) {
			String sourcecode = (String) u.elementAt(6);
			Vector v = parseData(sourcecode, '$');
			sourceCode = (String) v.elementAt(1);
		}
		if (u.size() > 7) {
			String sourcesourcename = (String) u.elementAt(7);
			Vector v = parseData(sourcesourcename, '$');
			subSourceName = (String) v.elementAt(1);
		}
		return new Synonym(
			code,
			label,
			termName,
			termGroup,
			termSource,
			sourceCode,
			subSourceName,
			subSourceCode);
	}

	public void clear() {
	    if (owlscanner.get_owl_vec() != null) {
			owlscanner.get_owl_vec().clear();
		}
	}

	public static Vector hset2Vector(HashSet hset) {
		Vector w = new Vector();
		Iterator it = hset.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			w.add(key);
		}
		return w;
	}

	public static HashSet vector2HashSet(Vector w) {
		HashSet hset = new HashSet();
		for (int i=0; i<w.size(); i++) {
            String t = (String) w.elementAt(i);
            hset.add(t);
		}
		return hset;
	}

	public Vector removeDuplicates(Vector codes) {
		HashSet hset = new HashSet();
		for (int i=0; i<codes.size(); i++) {
			String code = (String) codes.elementAt(i);
			if (!hset.contains(code)) {
				hset.add(code);
			}
		}
		return hset2Vector(hset);
	}

	public Vector getSubsetMemberCodes(String subsetCode) {
        return (Vector) subset_hmap.get(subsetCode);
	}


	public Vector getSubsetMembersByRecurrsion(String root, int level, int maxLevel) {
		Vector v = new Vector();
		if (level < maxLevel) {
			Vector subsets = (Vector) subset_hmap.get(root);
			if (subsets != null && subsets.size() > 0) {
				v.addAll(subsets);
			}
			Vector w = (Vector) parent_child_hmap.get(root);
			if (w != null && w.size() > 0) {
				for (int j=0; j<w.size(); j++) {
					String code2 = (String) w.elementAt(j);
					Vector subsets_sub = getSubsetMembersByRecurrsion(code2, level+1, maxLevel);
					if (subsets_sub != null && subsets_sub.size() > 0) {
						v.addAll(subsets_sub);
					}
				}
			}
		}
		v = removeDuplicates(v);
		v = new SortUtils().quickSort(v);
		return v;
	}

    public Vector createFocusedCodes(String root) {
		Vector w = new Vector();
		Vector subset_codes = getSubclassCodes(root);
		w.addAll(subset_codes);
		for (int i=0; i<subset_codes.size(); i++) {
			String sub = (String) subset_codes.elementAt(i);
			int j = i+1;
			Vector subsets = getSubsetMemberCodes(sub);
			w.addAll(subsets);
		}
		return w;
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

	public Vector extractFULLSyns(HashSet hset) {
		List list = owlscanner.extractFULLSyns();
		Vector w = new Vector();
		for (int i=0; i<list.size(); i++) {
			Synonym syn = (Synonym) list.get(i);
			if (hset.contains(syn.getCode())) {
				w.add(syn);
			}
		}
		return w;
	}

	public String decodeSpecialChar(String line) {
		line = line.replace("&apos;", "'");
		line = line.replace("&amp;", "&");
		line = line.replace("&lt;", "<");
		line = line.replace("&gt;", ">");
		line = line.replace("&quot;", "\"");
		return line;
	}

	public static void main(String[] args) {
		String owlfile = args[0];
		CDISCScanner cdiscScanner = new CDISCScanner(owlfile);
	}
}


