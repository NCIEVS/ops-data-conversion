package gov.nih.nci.evs;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * @author EVS Team
 * @version 1.0
 *
 * Modification history:
 *     Initial implementation kim.ong@nic.gov
 *
 */


public class Utils {

	public static void main(String[] args) {
		try {
			new Utils().reload(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void reload(String inputfile, String outputfile) {
		Vector v = readFile(inputfile);
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
}
