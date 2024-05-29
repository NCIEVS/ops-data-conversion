package gov.nih.nci.evs.canmed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import jdk.nashorn.internal.ir.IfNode;

public class CanmedCsvParser {
    private final List<List<String>> lineByLineData = new Vector<>();
    private List<String> header = null;

    /**
     * Read in the file from the input location
     *
     * @param file
     * @throws Exception
     */
    public CanmedCsvParser(File file) throws Exception {
        try {
            readFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Return the header line from the input file, tokenized into a list
     *
     * @return
     */
    public List<String> getHeader() {
        return header;
    }

    /**
     * Return the full set of tokenized data lines
     *
     * @return
     */
    public List<List<String>> getLineByLineData() {
        return lineByLineData;
    }

    /**
     * Parse the input file into a headerline and a set of data lines
     *
     * @param file
     * @throws Exception
     */
    private void readFile(File file) throws Exception {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            header = tokenizeString(line);

            while ((line = reader.readLine()) != null) {
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

    /**
     * Tokenize each line of csv data into a list of strings, being careful not to break
     * strings on commas that are within quotations.
     *
     * @param input
     * @return
     */
    public static List<String> tokenizeString(String input) {
        //https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
        //String input = "foo,bar,c;qual=\"baz,blurb\",d;junk=\"quux,syzygy\"";
        List<String> result = new ArrayList<String>();
        int start = 1;
        boolean inQuotes = false;
        try{
        for (int current = 0; current < input.length(); current++) {
            if (input.charAt(current) == '\"') inQuotes = !inQuotes; // toggle state
            //Are we at the end of the input string yet?
            boolean atLastChar = (current == input.length() - 1);
            //if so, the add the last token
            if (atLastChar) {
                String token = input.substring(start);
                if(token.startsWith("\"") ){
                    token = token.substring(1, token.length()-1);
                }
                result.add(token.trim());
            }
            //If we are at a comma and not within quotes then we have found a delimiter
            else if (input.charAt(current) == ',' && !inQuotes) {
                String token = input.substring(start, current-1);
                if(!token.isEmpty()){
                if(token.startsWith("\"")){
                    token = token.substring(1, token.length());
                }
                if(token.endsWith("\"")){
                    token = token.substring(0, token.length()-1);
                }}
                result.add(token.trim());
//                result.add(input.substring(start, current));
                start = current + 1;
            }
        }
        } catch (Exception e){
            System.out.println("Error in tokenizeString " + input);
            return null;
        }
        return result;
    }

    /**
     * Checks each line to make sure it has the same number of tokens as the
     * header If not, then the tokenizing has gone wrong.
     *
     * Caveat: NDC has description as the last column and some data lines will leave this empty.
     * You will see header=x+1 and line=x warnings for this case. This can be ignored
     *
     * @return isValid
     */
    private boolean validateAgainstHeader() {
        for (List<String> line : lineByLineData) {
            if (line.size() > header.size()) {
                System.out.println("Invalid data at " + line.get(0) + ". More data than there are columns");
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
                return false;
            }
            if (line.get(0).equals("")) {
                System.out.println("The code is empty for " +line.get(1));
            }


        }

        return true;
    }
}
