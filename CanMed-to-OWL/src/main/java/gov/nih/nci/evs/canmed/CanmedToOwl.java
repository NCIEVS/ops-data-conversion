/**
 * Main class for CanmedToOwl
 * Accepts path for NDC and HCPCS files
 * Outputs to single OWL file
 */
package gov.nih.nci.evs.canmed;

import gov.nih.nci.evs.canmed.entity.Ontology;
import java.io.File;
import java.net.URI;
import java.time.LocalDate;


public class CanmedToOwl {
    private File NDCcsvFile = null;
    private File HCPCScsvFile = null;
    private URI saveURI = null;
    private String version = "";


    public static void main(String[] args) {

        CanmedToOwl cto = new CanmedToOwl();
        if (args.length >0) {
            cto.version=LocalDate.now().getMonth().toString()+LocalDate.now().getYear();
            cto.configure(args);
            cto.processCanMED();
        } else {
            printHelp();
        }
        // ReadCSV file
        //parse each row into a CanMED concept object.
        //harvest property list
        //build SEER / Major class/ Minor class hierarchy
        //write OWL
        //header
        //properties
        //iterated concepts
        //footer
    }

    /**
     *  Checks validity of the file locations.
     *  Determines if it is a URL or file path
     *  Calls checkValidPath and checkValidURI
     *
     * @param fileLocation
     * @return
     */
    public static File readCsvFile(String fileLocation) {
        File file = checkValidURI(fileLocation);
        if (file == null) {
            file = checkValidPath(fileLocation);
        }
        if (file == null) {
            System.out.println("Not a valid file location");
            System.exit(0);
        }
        return file;
    }

    /**
     * Check if they entered a valid path for the file
     *
     * @param fileLoc
     * @return File object
     */
    public static File checkValidPath(String fileLoc) {
        try {
            File file = new File(fileLoc);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if they entered a URI for the file path and if it is valid
     *
     * @param fileLoc
     * @return File object
     */
    public static File checkValidURI(String fileLoc) {
        try {
            URI uri = new URI(fileLoc);
            return new File(uri);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Prints the help.
     */
    public static void printHelp() {


        System.out.println();
        System.out.println("Usage: CanMEDToOwl NDCsource HCPCSsource target ");
        System.out.println("-n  ndc source. The path to the raw NDC text downloaded from CanMED");
        System.out.println("-h  hcpcs source. The path to the raw HCPCS text downloaded from CanMED");
        System.out.println("-t  target output. The path and name to store the owl file");
        System.out.println("-v version. The version to assign to the final vocabulary");
        System.out.println();
        System.exit(1);
    }

    /**
     * Reads the two CSV files and sets up the output
     * @param args
     */
    private void configure(String[] args) {
        try {
            if(args.length>0){
                for (int i = 0; i < args.length; i++) {
                    if (args[i].equalsIgnoreCase("-n")){
                        NDCcsvFile = readCsvFile(args[++i]);
                    }
                    else if (args[i].equalsIgnoreCase("-h")){
                        HCPCScsvFile = readCsvFile(args[++i]);
                    }
                    else if (args[i].equalsIgnoreCase("-t")){
                        saveURI = new URI(args[++i]);
                    }
                    else if (args[i].equalsIgnoreCase("-v")){
                        version = args[++i];
                    }else {
                        printHelp();
                    }
                }
            }else {
                printHelp();
            }

        } catch (Exception e) {
            System.exit(0);
        }
    }

    /**
     * Sends the csv files to the NDC and HCPCS parsers
     * Retrieves the results and merges that to Cannmed Ontology
     * Sends Canmed ontology to OWL Writer for output
     */
    private void processCanMED() {
        try {
            CanmedCsvParser NDCparser = new CanmedCsvParser(NDCcsvFile);
            CanmedCsvParser HCPCSparser = new CanmedCsvParser(HCPCScsvFile);
            Ontology ontology = new Ontology(NDCparser, HCPCSparser);
            OwlWriter owlWriter = new OwlWriter(ontology, saveURI, version);
//            CanmedOntology canmedOntology = new CanmedOntology(NDCparser, HCPCSparser);
//            CanmedOwlWriter writer = new CanmedOwlWriter(canmedOntology, saveURI);
        } catch (Exception e) {
            System.out.println("Error reading in CSV file.  Program ending");
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Concept codes must be able to make valid URI fragments
     * This converts spaces, slashes and the like to neutral characters
     *
     * @param rawCode
     * @return valid string fragment for URI
     */
    public static String parseConceptCode(String rawCode) {
        String origRawCode = rawCode;
        if (rawCode != null && rawCode.length()>0) {
            rawCode = rawCode.toUpperCase();
            rawCode = rawCode.replace("\"", "");
            rawCode = rawCode.replace(" ", "_");
            rawCode = rawCode.replace(",", "_");
            rawCode = rawCode.replace("__", "_");
            rawCode = rawCode.replace("/", "-");
            rawCode = rawCode.replace("&", "and");
            rawCode = rawCode.replace("'", "");
            rawCode = rawCode.replace("%", "");
        }
        if(rawCode.length()<1){
            System.out.println("Code is empty "+ origRawCode);
        }
        return rawCode;
    }
}
