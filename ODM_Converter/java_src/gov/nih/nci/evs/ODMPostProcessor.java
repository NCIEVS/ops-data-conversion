package gov.nih.nci.evs;

import java.io.*;
import java.util.*;
import java.text.*;

/**
 * <!-- LICENSE_TEXT_START -->
 * Copyright 2011, MSC. This software was developed in conjunction
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
 *      or MSC.
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
 * Modification history:
 *     Initial implementation ongki@nih.gov
 *
 */

public class ODMPostProcessor {

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

	public static String getTruncatedTimeStamp(Vector w) {
		//<Study OID="CDISC_CT.Define-XML.2021-10-">
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			line = line.trim();
			if (line.startsWith("<Study OID=")) {
				int n1 = line.lastIndexOf(".");
				int n2 = line.lastIndexOf("-");
				return line.substring(n1+1, n2+1);
			}
		}
		return null;
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

	public static String getToday() {
		return getToday("yyyy-MM-dd");
	}

	public static String getToday(String format) {
		java.util.Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static String getCreationDateTime(Vector w) {
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			if (line.indexOf("CreationDateTime=") != -1) {
				int n1 = line.indexOf("CreationDateTime=");
				String t = line.substring(n1 + "CreationDateTime=".length()+1, line.length());
				int n2 = t.indexOf("\"");
				t = t.substring(0, n2);
				return t;
			}
		}
		return null;
	}

	public static void run(String odmxml) {
		run(odmxml, odmxml);
	}

/*
	public static void run(String odmxml, String outputfile) {
		Vector w = readFile(odmxml);
		String timeStamp = getToday();
		Vector v = new Vector();
        String creationDataTime1 = getCreationDateTime(w);
		String truncatedTimeStamp = getTruncatedTimeStamp(w);
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			line = line.replace(truncatedTimeStamp, timeStamp);
			v.add(line);
		}
		String creationDataTime2 = getCreationDateTime(v);
		Vector v2 = new Vector();
		for (int i=0; i<v.size(); i++) {
			String line = (String) v.elementAt(i);
			line = line.replace(creationDataTime2, creationDataTime1);
			v2.add(line);
		}
		saveToFile(outputfile, v2);
	}
*/

	public static void run(String odmxml, String outputfile) {
		Vector w = readFile(odmxml);
		Vector v = new Vector();
		for (int i=0; i<w.size(); i++) {
			String line = (String) w.elementAt(i);
			line = line.replace("&amp;#", "&#");
			v.add(line);
		}
		saveToFile(outputfile, v);
	}


	public static void main(String[] args) {
		String odmxml = args[0];
		run(odmxml, odmxml);
	}
}
