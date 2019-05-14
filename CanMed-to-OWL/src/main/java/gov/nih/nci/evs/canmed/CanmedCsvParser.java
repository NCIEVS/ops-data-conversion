package gov.nih.nci.evs.canmed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

public class CanmedCsvParser {
	public CanmedCsvParser(File file) throws Exception{
		try {
			readFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private List<String> header = null;
	public List<String> getHeader() {
		return header;
	}


	public List<List<String>> getLineByLineData() {
		return lineByLineData;
	}

	private final List<List<String>> lineByLineData = new Vector<>();

	private void readFile(File file) throws Exception {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			// Scanner reader = new Scanner(file, "UTF-8");
			Pattern p = Pattern.compile("\t");
			// String line = reader.nextLine(); // first line is a header
			String line = reader.readLine();
			header = tokenizeString(line);
			List<String> data = new ArrayList<String>();
			// while (reader.hasNextLine()) {
			// line = reader.nextLine();
			// data.add(line);
			// lineByLineData.add(tokenizeString(line, p));
			// }

			while ((line = reader.readLine()) != null) {
				// line = reader.nextLine();
				data.add(line);
				lineByLineData.add(tokenizeString(line));
			}
			if (lineByLineData.size() < 1) {
				reader.close();
				throw new FileNotFoundException();
			}
			boolean isValidData = validateAgainstHeader();
			if (!isValidData) {
				reader.close();
				throw new Exception();
			}
			reader.close();
		} catch (Exception e) {

			System.out.println("Error in readFile");
			throw e;
		}
	}
	
	
	private List<String> tokenizeString(String input){
		//https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
		//String input = "foo,bar,c;qual=\"baz,blurb\",d;junk=\"quux,syzygy\"";
		List<String> result = new ArrayList<String>();
		int start = 0;
		boolean inQuotes = false;
		for (int current = 0; current < input.length(); current++) {
		    if (input.charAt(current) == '\"') inQuotes = !inQuotes; // toggle state
		    boolean atLastChar = (current == input.length() - 1);
		    if(atLastChar) result.add(input.substring(start));
		    else if (input.charAt(current) == ',' && !inQuotes) {
		        result.add(input.substring(start, current));
		        start = current + 1;
		    }
		}
		return result;
	}
	
	/**
	 * Checks each line to make sure it has the same number of tokens as the
	 * header If not, then the tokenizing has gone wrong.
	 *
	 * @return
	 */
	private boolean validateAgainstHeader() {
		for (List<String> line : lineByLineData) {
			if (line.size() > header.size()) {
				System.out.println("Invalid data at " + line.get(0)  + ". More data than there are columns");
				return false;
			}
			if (line.size() < 1) {
				System.out.println("Invalid data.  Empty line");
				return false;
			}
			if (line.size() < (header.size() / 2)) {
				System.out.println("Too few data fields at " + line.get(0) + ".  Data is " + line.size());
				return false;
			}
			if (line.size() != header.size()) {
				System.out.println("Warning: Data is " + line.size() + " fields long.  Header is " + header.size()
						+ " long at " + line.get(0));
			}
			if (header.size() - line.size() > 4) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("ERROR: There is something seriously wrong with " + line.get(0));
			}
			if(line.get(0).equals("")) {
				return false;
			}



		}

		return true;
	}
}
