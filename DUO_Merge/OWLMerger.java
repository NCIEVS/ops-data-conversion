import java.util.*;
import java.io.*;
import java.text.*;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2008,2020 MSC. This software was developed in conjunction
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
 *      "This product includes software developed by MSC and the National
 *      Cancer Institute."   If no such end-user documentation is to be
 *      included, this acknowledgment shall appear in the software itself,
 *      wherever such third-party acknowledgments normally appear.
 *   3. The names "The National Cancer Institute", "NCI" and "MSC" must
 *      not be used to endorse or promote products derived from this software.
 *   4. This license does not authorize the incorporation of this software
 *      into any third party proprietary programs. This license does not
 *      authorize the recipient to use any trademarks owned by either NCI
 *      or MSC
 *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
 *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
 *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 *      MSC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
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
 *      Modification history Initial implementation kim.ong@nih.gov
 *                           Last modified: 02/12/2012
 *
 */

public class OWLMerger {
	 private String owl_source = null;
	 private String owl_target = null;
	 public static boolean qa_flag = true;

	 public OWLMerger() {

	 }

	 public static Vector readFile(String filename) {
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

	 public static void saveToFile(String outputfile, Vector v) {
		if (outputfile == null) return;
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputfile, "UTF-8");
			if (pw != null) {
				if (v != null && v.size() > 0) {
					for (int i=0; i<v.size(); i++) {
						String t = (String) v.elementAt(i);
						pw.println(t);
					}
				}
			}
		} catch (Exception ex) {

		} finally {
			try {
				if (pw != null) pw.close();
				System.out.println("Output file " + outputfile + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	 }

	 public static String getToday() {
		 return getToday("MM/dd/yyyy");
	 }

	 public static String getToday(String format) {
		 java.util.Date date = Calendar.getInstance().getTime();
		 SimpleDateFormat sdf = new SimpleDateFormat(format);
		 return sdf.format(date);
	 }

	 public String generate_outputfile_name(String filename) {
		 int n = filename.lastIndexOf(".");
		 return filename.substring(0, n) + "_" + getToday("MMddyyyy") + ".owl";
	 }

 	 public String extract_version_info(String filename) {
		 Vector v = readFile(filename);
		 Vector w = new Vector();
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf("<owl:versionIRI") != -1) {
				 return t;
			 }
		 }
		 return null;
	 }

 	 public String extract_version(String filename) {
		 String version_info = extract_version_info(filename);
		 String t = version_info.substring(0, version_info.length()-3);
		 int n = t.lastIndexOf("/");
		 t = version_info.substring(0, n);
		 n = t.lastIndexOf("/");
		 t = version_info.substring(n+1, t.length());
		 return t;
	 }

	 public Vector extract_namespaces(String filename) {
		 Vector v = readFile(filename);
		 Vector w = new Vector();
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf("<owl:Ontology rdf:about") != -1) {
				 break;
			 }
			 w.add(t);
		 }
		 return w;
	 }

	 public Vector merge_namespaces(String duo_owl, String iao_owl) {
		 Vector v1 = extract_namespaces(duo_owl);
		 Vector v2 = extract_namespaces(iao_owl);
		 Vector w = new Vector();
		 for (int i=0; i<v2.size(); i++) {
			 String t = (String) v2.elementAt(i);
			 if (t.indexOf("rdf:RDF") == -1 && t.indexOf("xml:base") == -1) {
				 if (!v1.contains(t)) {
					 w.add(t);
				 }
			 }
		 }
		 v1.addAll(w);
		 return v1;
	 }

	 public Vector extract_source_lines(String filename, String target) {
		 Vector v = readFile(filename);
		 Vector w = new Vector();
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf(target) != -1) {
				 w.add(t);
		     }
		 }
		 return w;
	 }

	 public Vector extract_source_segment(String filename, String from, String to) {
		 Vector v = readFile(filename);
		 Vector w = new Vector();
		 boolean start = false;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 //if (t.indexOf("&quot;") == -1) {
				 if (t.indexOf(from) != -1) {
					 start = true;
				 }
				 if (start) {
					 w.add(t);
				 }
				 if (start && t.indexOf(to) != -1) {
					 break;
				 }
		     //}
		 }
		 return w;
	 }

	 public Vector extract_OWLOntology(String filename) {
		 return extract_source_segment(filename, "<owl:Ontology rdf:about", "</owl:Ontology>");
	 }

	 public Vector extract_annotation_properties(String filename) {
		 return extract_source_segment(filename, "Annotation properties", "Object Properties");
	 }

     public Vector addLines() {
		 Vector w = new Vector();
		 w.add("\n\n");
		 w.add("    <!--");
		 w.add("    ///////////////////////////////////////////////////////////////////////////////////////");
		 w.add("    //");
		 return w;
	 }


     public Vector removeLines(Vector v, int n) {
		 try {
			 for (int i=0; i<n; i++) {
				 v.remove(v.size()-1);
			 }
		 } catch (Exception ex) {

		 }
		 return v;
	 }

	 public Vector extract_object_properties(String filename, String from, String to) {
		 Vector v = readFile(filename);
		 Vector w = new Vector();
		 boolean start = false;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf(from) != -1 && t.indexOf(":") != -1) {
				 start = true;
			 }
			 if (start) {
				 w.add(t);
			 }
			 if (start && t.indexOf(to) != -1) {
				 break;
			 }
		 }
		 return w;
	 }

	 public Vector get_simple_class_ids(String filename) {
		 Vector v = readFile(filename);
		 Vector w = new Vector();
		 boolean start = false;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf("<owl:Class rdf:about") != -1 && !w.contains(t)) {
				 w.add(t);
		     }
		 }
		 return w;
	 }

	 public Vector get_simple_class_ids(Vector v) {
		 Vector w = new Vector();
		 boolean start = false;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf("<owl:Class rdf:about") != -1 && !w.contains(t)) {
				 w.add(t);
		     }
		 }
		 return w;
	 }

	 public Vector extract_simple_class(String filename, String from, String to) {
		 Vector v = readFile(filename);
		 Vector w = new Vector();
		 boolean start = false;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf(from) != -1) {
				 start = true;
				 w.add("\n");
				 String s = (String) v.elementAt(i-2);
				 w.add(s);
				 s = (String) v.elementAt(i-1);
				 w.add(s);
			 }
			 if (start) {
				 w.add(t);
			 }
			 if (start && t.indexOf(to) != -1) {
				 break;
			 }
		 }
		 return w;
	 }


	 public Vector extract_complex_class(String filename, String id, String from, String to) {
		 Vector v = readFile(filename);
		 Vector w = new Vector();
		 boolean start = false;
		 int knt = 0;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf(id) != -1) {
				 start = true;
				 w.add("\n");
				 String s = (String) v.elementAt(i-2);
				 w.add(s);
				 s = (String) v.elementAt(i-1);
				 w.add(s);
				 knt++;
			 }
			 if (start) {
				 w.add(t);
			 }
			 if (start && t.indexOf(from) != -1) {
				 knt++;
			 } else if (start && t.indexOf(to) != -1) {
				 knt--;
				 if (knt == 0) {
				 	break;
				}
			 }
		 }
		 return w;
	 }

	 public Vector extract_complex_class(Vector v, String id, String from, String to) {
		 Vector w = new Vector();
		 boolean start = false;
		 int knt = 0;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf(id) != -1) {
				 start = true;
				 w.add("\n");
				 String s = (String) v.elementAt(i-2);
				 w.add(s);
				 s = (String) v.elementAt(i-1);
				 w.add(s);
				 knt++;
			 }
			 if (start) {
				 w.add(t);
			 }
			 if (start && t.indexOf(from) != -1) {
				 knt++;
			 } else if (start && t.indexOf(to) != -1) {
				 knt--;
				 if (knt == 0) {
				 	break;
				}
			 }
		 }
		 return w;
	 }

	 public Vector extract_classes(String filename) {
		 Vector v = new Vector();
         String from = "<owl:Class";
         String to = "</owl:Class>";
		 Vector id_vec = get_simple_class_ids(filename);
		 dumpVector("id_vec", id_vec);
		 for (int k=0; k<id_vec.size(); k++) {
			 String id = (String) id_vec.elementAt(k);
			 Vector w = extract_complex_class(filename, id, from, to);
			 v.addAll(w);
		 }
		 return v;
	 }

	 public void dumpVector(String label, Vector v) {
		 boolean with_idx = true;
		 dumpVector(label, v, with_idx);
	 }

	 public void dumpVector(String label, Vector v, boolean with_idx) {
		 System.out.println(label);
		 int lcv = 0;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (with_idx) {
				 lcv++;
				 System.out.println("(" + lcv + ") " + t);
			 } else {
				  System.out.println(t);
			 }
		 }
		 if (v == null || v.size() == 0) {
			 System.out.println("\tNone");
		 }
	 }

	 public void dumpVector(PrintWriter pw, String label, Vector v) {
		 boolean with_idx = true;
		 dumpVector(pw, label, v, with_idx);
	 }

	 public void dumpVector(PrintWriter pw, String label, Vector v, boolean with_idx) {
		 pw.println(label);
		 int lcv = 0;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (with_idx) {
				 lcv++;
				 pw.println("(" + lcv + ") " + t);
			 } else {
				 pw.println(t);
			 }
		 }
		 if (v == null || v.size() == 0) {
			 pw.println("\tNone");
		 }
	 }


     public Vector addPartition(String title) {
		 Vector w = new Vector();
		 w.add("\n");
		 w.add("    <!--");
		 w.add("    ///////////////////////////////////////////////////////////////////////////////////////");
		 w.add("    //");
		 w.add("    // " + title);
		 w.add("    //");
		 w.add("    ///////////////////////////////////////////////////////////////////////////////////////");
		 w.add("     -->");
		 w.add("\n");
		 return w;
	 }

	 public void run_QA(PrintWriter pw, String target, String duo_owl, String iao_owl, String outputfile) {
		 pw.println("\nChecking " + target + " ...");
		 Vector v1 = extract_source_lines(duo_owl, target);
		 Vector v2 = extract_source_lines(iao_owl, target);
		 Vector v3 = extract_source_lines(outputfile, target);
		 dumpVector(pw, duo_owl, v1);
		 dumpVector(pw, iao_owl, v2);
		 dumpVector(pw, outputfile, v3);
		 pw.println("\t" + duo_owl + " count: " + v1.size());
         pw.println("\t" + iao_owl + " count: " + v2.size());
         pw.println("\t" + outputfile + " count: " + v3.size());
         if (v1.size() + v2.size() != v3.size()) {
			 Vector w = new Vector();
			 w.addAll(v1);
			 w.addAll(v2);
         	 compare(pw, "input/source files", w, "output/merged file", v3);
		 }
	 }

	public void run_QA(String duo_owl, String iao_owl, String outputfile) {
		PrintWriter pw = null;
		int n = outputfile.lastIndexOf(".");
		String qa_file = outputfile.substring(0, n) + "_qa" + ".owl";

		try {
			pw = new PrintWriter(qa_file, "UTF-8");
			String target = "<owl:AnnotationProperty";
			run_QA(pw, target, duo_owl, iao_owl, outputfile);
			pw.println("\n");
			target = "<owl:ObjectProperty";
			run_QA(pw, target, duo_owl, iao_owl, outputfile);
			pw.println("\n");
			target = "<owl:DatatypeProperty";
			run_QA(pw, target, duo_owl, iao_owl, outputfile);
			pw.println("\n");
			target = "<owl:Class ";
			run_QA(pw, target, duo_owl, iao_owl, outputfile);

		} catch (Exception ex) {

		} finally {
			try {
				if (pw != null) pw.close();
				System.out.println("Output file " + qa_file + " generated.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	 public void compare(PrintWriter pw, String label1, Vector v1, String label2, Vector v2) {
         Vector u1 = new Vector();
         for (int i=0; i<v1.size(); i++) {
			 String t = (String) v1.elementAt(i);
			 t = t.trim();
			 u1.add(t);
		 }
         Vector u2 = new Vector();
         for (int i=0; i<v2.size(); i++) {
			 String t = (String) v2.elementAt(i);
			 t = t.trim();
			 u2.add(t);
		 }
		 pw.println("Items in " + label1 + " but not in " + label2);
		 int lcv = 0;
		 for (int i=0; i<u1.size(); i++) {
			 String t = (String) u1.elementAt(i);
			 if (!u2.contains(t)) {
				 lcv++;
				 pw.println("(" + lcv + ") " + t);
			 }
		 }
		 pw.println("Items in " + label2 + " but not in " + label1);
		 lcv = 0;
		 for (int i=0; i<u2.size(); i++) {
			 String t = (String) u2.elementAt(i);
			 if (!u1.contains(t)) {
				 lcv++;
				 pw.println("(" + lcv + ") " + t);
			 }
		 }
	 }

	 public Vector set_difference(String label1, Vector v1, String label2, Vector v2) {
		 Vector diff_vec = new Vector();
		 HashMap hmap1 = new HashMap();
		 HashMap hmap2 = new HashMap();
         Vector u1 = new Vector();
         for (int i=0; i<v1.size(); i++) {
			 String t = (String) v1.elementAt(i);
			 String s = t;
			 t = t.trim();
			 hmap1.put(t, s);
			 u1.add(t);
		 }
         Vector u2 = new Vector();
         for (int i=0; i<v2.size(); i++) {
			 String t = (String) v2.elementAt(i);
			 String s = t;
			 t = t.trim();
			 hmap2.put(t, s);
			 u2.add(t);
		 }
		 int lcv = 0;
		 for (int i=0; i<u1.size(); i++) {
			 String t = (String) u1.elementAt(i);
			 if (!u2.contains(t)) {
				 diff_vec.add((String) hmap1.get(t));

			 }
		 }
		 return diff_vec;
	 }

	 public boolean is_comment_line(String t) {
		 String s = t;
		 s = s.trim();
		 if (s.startsWith("<!--") && s.endsWith("-->")) {
			 return true;
		 }
		 return false;
	 }

	 public Vector extract_element_definition(Vector v, String comment_line_id) {
		 Vector w = new Vector();
		 boolean start = false;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf(comment_line_id) != -1) {
				 start = true;
			 }
			 if (start && is_comment_line(t) && t.compareTo(comment_line_id) != 0) {
				 break;
			 } else {
				 if (start) {
					 w.add(t);
				 }
			 }
		 }
		 return w;
	 }

	 public Vector removeAnnotationPropertyDeclaration(Vector v, String target) {
		 String close_tag = "</owl:AnnotationProperty>";
		 Vector w = new Vector();
		 boolean flag = false;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf(target) != -1) {
				 //System.out.println("target found: " + t);
				 flag = true;
			 }
			 if (!flag) {
				 w.add(t);
			 }
			 if (flag) {
				 //System.out.println(t);
				 if (t.indexOf(close_tag) != -1) {
					 flag = false;
				 }
			 }
		 }
		 return w;
	 }

	 public Vector remove_annotation_property_from_classses(Vector v, String target) {
		 Vector w = new Vector();
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf(target) == -1) {
				 w.add(t);
			 }
		 }
		 return w;
	 }

     public boolean is_axiom_id_in_axiom_lines(Vector axiom_lines, String axiom_id) {
		 for (int i=0;i<axiom_lines.size(); i++) {
			 String t = (String) axiom_lines.elementAt(i);
			 if (t.indexOf(axiom_id) != -1) {
				 return true;
			 }
		 }
		 return false;
	 }

     public boolean is_target_in_block_lines(Vector block_lines, String target_id) {
		 for (int i=0;i<block_lines.size(); i++) {
			 String t = (String) block_lines.elementAt(i);
			 if (t.indexOf(target_id) != -1) {
				 return true;
			 }
		 }
		 return false;
	 }


//************************************************************************************************

     public Vector remove_target_from_block_lines(Vector v, String target_id) {
		 Vector w = new Vector();
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf(target_id) == -1) {
				 w.add(t);
			 }
		 }
		 return w;
	 }

	 public Vector remove_annotation_property_from_all_classes(Vector v, String target_id) {
         Vector class_id_vec = get_simple_class_ids(v);
         for (int i=0; i<class_id_vec.size(); i++) {
			 String class_id = (String) class_id_vec.elementAt(i);
			 v = remove_annotation_property_from_class(v, class_id, target_id);
		 }
		 return v;
	 }

	 public Vector remove_annotation_property_from_class(Vector v, String class_id, String target_id) {
		 Vector w = new Vector();
		 boolean flag = false;
		 boolean start = false;
		 int knt = 0;
		 String open_tag = "<owl:Class";
		 String close_tag = "</owl:Class>";
		 Vector block_lines = new Vector();
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (!start && t.indexOf(class_id) != -1) {
				 start = true;
			 }
			 if (start) {
				 block_lines.add(t);
				 if (t.indexOf(open_tag) != -1) {
					 knt++;
				 }
				 if (t.indexOf(close_tag) != -1) {
					 knt--;
				 }
				 if (knt == 0) {
					 Vector w2 = remove_target_from_block_lines(block_lines, target_id);
					 w.addAll(w2);
					 start = false;
				 }

			 } else if (!start) {
				 w.add(t);
			 }
		 }
		 return w;
	 }

	 public Vector remove_axiom_from_classses(Vector v, String axiom_id) {
		 Vector w = new Vector();
		 boolean flag = false;
		 String open_axiom = "<owl:Axiom>";
		 String close_axiom = "</owl:Axiom>";
		 Vector axiom_lines = new Vector();
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (!flag && t.indexOf(open_axiom) != -1) {
				 flag = true;
			 }
			 if (flag) {
				 axiom_lines.add(t);
			 } else {
				 w.add(t);
			 }

			 if (t.indexOf(close_axiom) != -1) {
				 //check if the axiom_lines contains target
				 boolean found = is_axiom_id_in_axiom_lines(axiom_lines, axiom_id);
				 if (!found) {
					 w.addAll(axiom_lines);
				 }
				 flag = false;
				 axiom_lines = new Vector();
			 }
		 }
		 return w;
	 }

	 public Vector get_id_comment_lines(Vector v) {
		 Vector id_vec =new Vector();
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 String s = t;
			 s = s.trim();
			 if (s.startsWith("<!--") && s.endsWith("-->")) {
				 if (s.indexOf("http:") != -1) {
				 	id_vec.add(t);
				 }
		     }
		 }
		 return id_vec;
	 }

     // Find content in file1 but not in file2
     public Vector extract_content_difference(String file1, String file2) {
        Vector u1 = get_id_comment_lines(readFile(file1));
        Vector u2 = get_id_comment_lines(readFile(file2));
        Vector diff_vec = set_difference(file1, u1, file2, u2);
        Vector all_vec = new Vector();
        Vector file1_content = readFile(file1);
        for (int i=0; i<diff_vec.size(); i++) {
			String comment_line_id = (String) diff_vec.elementAt(i);
            Vector u = extract_element_definition(file1_content, comment_line_id);
            all_vec.addAll(u);
		}
		return all_vec;
	}

     public Vector extract_owl_content(Vector v) {
		 Vector w = new Vector();
		 boolean start = false;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf("</owl:Ontology>") != -1) {
				 start = true;
			 } else if (t.indexOf("</rdf:RDF>") != -1) {
				 start = false;
				 break;
			 }

			 if (start && t.indexOf("</owl:Ontology>") == -1) {
				 w.add(t);
			 }
		 }
		 return w;
	 }

	 public Vector remove_annotation_property(Vector v, String from_tag, String target_id) {
		 Vector w = new Vector();
		 boolean flag = false;
		 String open_axiom = "<" + from_tag;
		 String close_axiom = "</" + from_tag + ">";
		 Vector block_lines = new Vector();
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (!flag && t.indexOf(open_axiom) != -1) {
				 flag = true;
			 }
			 if (flag) {
				 block_lines.add(t);
			 } else {
				 w.add(t);
			 }

			 if (t.indexOf(close_axiom) != -1) {
				 boolean found = is_target_in_block_lines(block_lines, target_id);
				 if (!found) {
					 w.addAll(block_lines);
				 }
				 flag = false;
				 block_lines = new Vector();
			 }
		 }
		 return w;
	 }

	 public Vector search_property(Vector v, String target) {
		 Vector w = new Vector();
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 if (t.indexOf(target) != -1) {
				 w.add(t);
			 }
		 }
		 return w;
	 }


	 public Vector removeSimpleProperty(Vector v, String target) {
		 String open_tag = "<" + target;// <obo:IAO_0000116
		 String close_tag = "</" + target + ">";
		 Vector w = new Vector();
		 boolean inclusion_flag = true;
		 for (int i=0; i<v.size(); i++) {
			 String t = (String) v.elementAt(i);
			 String s = t.trim();
			 if (s.startsWith(open_tag)) {
				 inclusion_flag = false;
			 }
			 if (inclusion_flag) {
				 w.add(t);
			 }
			 if (!inclusion_flag && s.endsWith(close_tag)) {
				 inclusion_flag = true;
			 }
		 }
		 return w;
	 }

     public static void main(String[] args) {
        String duo_owl = args[0];
        String iao_owl = args[1];
        OWLMerger merger = new OWLMerger();
        String outputfile = merger.generate_outputfile_name(duo_owl);
        if (args.length > 2) {
			outputfile = args[2];
		}

        String duo_owl_version = merger.extract_version(duo_owl);
        String iao_owl_version = merger.extract_version(iao_owl);

        Vector v = merger.merge_namespaces(duo_owl, iao_owl);
        v.addAll(merger.extract_OWLOntology(duo_owl));

        v.addAll(merger.extract_owl_content(merger.readFile(iao_owl)));
        v.addAll(merger.extract_content_difference(duo_owl, iao_owl));

        String from_tag = null;
        String target_id = null;

        String[] properties = new String[] {"oboInOwl:created_by",
                                            "oboInOwl:creation_date",
                                            "obo:IAO_0000232",
                                            "obo:IAO_0000117",
                                            "obo:IAO_0000116"};

        for (int m=0; m<properties.length; m++) {
			target_id = (String) properties[m];
			v = merger.removeSimpleProperty(v, target_id);
		}

        String axiom_id = "<owl:annotatedProperty rdf:resource=\"http://purl.obolibrary.org/obo/IAO_0000116\"/>";
        v = merger.remove_axiom_from_classses(v, axiom_id);

        v.add("</rdf:RDF>");
        v.add("\n");
        v.add("<!-- Generated by merging " + duo_owl + " (version: " + duo_owl_version + ") with " + iao_owl + " (version: " + iao_owl_version + ") on " + getToday() + " -->");
        merger.saveToFile(outputfile, v);

	    if (qa_flag) {
	    	merger.run_QA(duo_owl, iao_owl, outputfile);
		}

        for (int m=0; m<properties.length; m++) {
			target_id = (String) properties[m];
			Vector u = merger.search_property(v, target_id);
			merger.dumpVector(target_id, u);
		}
	}
}
// online owl validator: http://visualdataweb.de/validator/
