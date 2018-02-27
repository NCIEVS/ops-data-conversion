/* Rob Wynne, MSC
 * 
 * Convert the ncitconcept_history file to the
 * format consumed by LexEVS.  The output is
 * appended to cumulative_history.txt 
 */

package gov.nih.nci.evs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class ConvertHistoryFiles {

	private PrintWriter pw;

	public static void main(String[] args) {
		ConvertHistoryFiles convert = new ConvertHistoryFiles();
		if (args.length == 2) {
			convert.run(args);
		} else {
			System.exit(0);
		}
	}

	public void run(String[] args) {
		configPrintWriter(args[1]);
		Vector<String> records = readFile(args[0]);

		for (String record : records) {
			String[] recordArray = record.split("\t");
			
				String conceptCode = recordArray[0];
				String action = recordArray[1];
				String date = recordArray[2];
				String refCode = "null";
				if (recordArray.length == 4) {
				    refCode = recordArray[3];
				    if(refCode.equals("(null)")){
				    	refCode = "null";
				    }
				}

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				SimpleDateFormat sdf_short = new SimpleDateFormat("yyyy-MM-dd");
				boolean shortdate = false;
				Date d = null;

				try {
					d = sdf.parse(date);
					sdf.applyLocalizedPattern("dd-MMM-yy");
					date = sdf.format(d);
				} catch (ParseException e) {
//					e.printStackTrace();
					shortdate = true;
				}
				
				if(shortdate){
					try {
						d = sdf_short.parse(date);
						sdf.applyLocalizedPattern("dd-MMM-yy");
						date = sdf.format(d);
					} catch (ParseException e) {
						e.printStackTrace();
						shortdate = false;
					}
				}


			pw.println(conceptCode + "||" + action.toLowerCase() + "|" + date + "|" + refCode + "|");
			pw.flush();

		}

		pw.close();

	}

	private void configPrintWriter(String outputfile) {
		try {
			File file = new File(outputfile);
			pw = new PrintWriter(file);
		} catch (Exception e) {
			System.out.println("Error in PrintWriter");
		}
	}

	public Vector<String> readFile(String filename) {
		Vector<String> v = new Vector<String>();
		FileReader configFile = null;
		BufferedReader buff = null;
		try {
			configFile = new FileReader(filename);
			buff = new BufferedReader(configFile);
			boolean eof = false;
			while (!eof) {
				String line = buff.readLine();
				if (line == null) {
					eof = true;
				} else {
					v.add(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Closing the streams
			try {
				buff.close();
				configFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!v.isEmpty())
			return v;
		else
			return null;
	}

}
