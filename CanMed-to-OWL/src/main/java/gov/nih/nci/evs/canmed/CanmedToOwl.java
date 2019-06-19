/**
 * Main class for CanmedToOwl
 * Accepts path for NDC and HCPCS files
 * Outputs to single OWL file
 */
package gov.nih.nci.evs.canmed;

import java.io.File;
import java.net.URI;


public class CanmedToOwl {
    private File NDCcsvFile = null;
    private File HCPCScsvFile = null;
    private URI saveURI = null;


    public static void main(String[] args) {

        CanmedToOwl cto = new CanmedToOwl();
        if (args.length == 3) {
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
        System.out.println("  source The path to the raw NDC text downloaded from CanMED");
        System.out.println("  source The path to the raw HCPCS text downloaded from CanMED");
        System.out.println("  target The path and name to store the owl file");
        System.out.println();
        System.exit(1);
    }

    /**
     * Reads the two CSV files and sets up the output
     * @param args
     */
    private void configure(String[] args) {
        try {

            NDCcsvFile = readCsvFile(args[0]);
            HCPCScsvFile = readCsvFile(args[1]);


            String target = args[2];
            if (target != null) {
                saveURI = new URI(target);
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
            CanmedOntology ontology = new CanmedOntology(NDCparser, HCPCSparser);
            CanmedOwlWriter writer = new CanmedOwlWriter(ontology, saveURI);
        } catch (Exception e) {
            System.out.println("Error reading in CSV file.  Program ending");
            e.printStackTrace();
            System.exit(0);
        }
    }
}
