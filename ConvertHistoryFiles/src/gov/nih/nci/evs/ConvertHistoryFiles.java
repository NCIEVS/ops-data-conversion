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
	
	public static void main (String[] args) {
		ConvertHistoryFiles convert = new ConvertHistoryFiles();
		if( args.length == 2 ) {
			convert.run(args);
		}
		else {
			System.exit(0);
		}
	}
	
	public void run(String[] args) {
		configPrintWriter("concept_history.txt");
		Vector<String> records = readFile(args[0]);
		String theDate = args[1];
		
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = null;
		try {
			d = sdf.parse(theDate);
            sdf.applyLocalizedPattern("dd-MMM-yy");
            theDate = sdf.format(d);				
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		for(String record : records) {
			String[] recordArray = record.split("\t");
			String conceptCode = recordArray[0];
			String action = recordArray[1];
			String date = recordArray[2];
			String refCode;
			if(recordArray.length>3){
			    refCode = processReferenceCode(recordArray[3]);
			} else {
				refCode = "null";
			}

            
            pw.println(conceptCode + "||" + action.toLowerCase() + "|" + theDate + "|" + refCode + "|");
            pw.flush();
		}
		
		pw.close();
		
	}
	
	private String processReferenceCode(String refCodeTemp){
		
		if(refCodeTemp.contains("null"))
		{
			return "null";
		}
		
		return refCodeTemp;
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
		}
		finally {
			// Closing the streams
			try {
				buff.close();
				configFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!v.isEmpty()) return v;
		else
			return null;
	}	

}
