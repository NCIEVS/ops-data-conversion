package gov.nih.nci.evs.canmed;

import java.io.File;
import java.net.URI;

import org.semanticweb.owlapi.model.IRI;


public class CanmedToOwl {
	private File csvFile = null;
	private URI saveURI = null;

	public static void main(String[] args) {
		
		CanmedToOwl cto = new CanmedToOwl();
		if(args.length==2)
		{
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
	
	private void configure(String[] args) {
		try {
			String inputFile = args[0];
			csvFile = readCsvFile(inputFile);
			
			String target = args[1];
			if (target != null) {
				saveURI = new URI(target);
			} 
		} catch (Exception e) {
			System.exit(0);
		}
	}
	
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
	
	public static File checkValidPath(String fileLoc) {
		try {
			File file = new File(fileLoc);
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	
	public static File checkValidURI(String fileLoc) {
		try {
			URI uri = new URI(fileLoc);
			return new File(uri);
		} catch (Exception e) {
			return null;
		}
	}
	
	private void processCanMED() {
		try {
			CanmedCsvParser parser = new CanmedCsvParser(csvFile);
			CanmedOntology ontology = new CanmedOntology(parser);
			CanmedOwlWriter writer = new CanmedOwlWriter(ontology,saveURI);
		} catch (Exception e) {
			System.out.println("Error reading in CSV file.  Program ending");
			e.printStackTrace();
			System.exit(0);
		} 
	}
	/**
	 * Prints the help.
	 */
	public static void printHelp() {


		System.out.println("");
		System.out.println("Usage: CanMEDToOwl source target ");
		System.out.println("  source The path to the raw text downloaded from CanMED");
		System.out.println("  target The path and name to store the owl file");
		System.out.println("");
		System.exit(1);
	}
}
