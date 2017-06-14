package gov.nih.nci.evs;

import gov.nih.nci.bopcheck.Wget;
import gov.nih.nci.evs.logger.MyLogger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BerkeleyBopDownload {

	private final static Logger LOGGER = Logger
			.getLogger(BerkeleyBopDownload.class.getName());
	Properties vocabList = new Properties();
	Vector<Vocabulary> vocabs = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Arguments should say whether to download, download and load, or
		// just load
				
			BerkeleyBopDownload bbd = new BerkeleyBopDownload();
	}
	
	public BerkeleyBopDownload(){
		String bopURL = "http://www.berkeleybop.org/ontologies/";
		try {
			
			MyLogger.setup();
			LOGGER.setLevel(Level.INFO);			
			
			vocabList.load(new FileInputStream(
					"./conf/VocabsToCheck.properties"));

			
			if(!createConceptArray()){
				LOGGER.severe("Unable to load concepts from properties file");
				System.exit(0);
			}
			
			Wget get = new Wget();
			get.get(bopURL,vocabs);
			
		} catch (FileNotFoundException e) {
			LOGGER.severe("Unable to find config file with names of vocabs to retrieve");
			System.exit(0);
		} catch (IOException e) {
			LOGGER.severe("Unable to read config file with names of vocabs to retrieve");
			System.exit(0);
		}

	}
	
	
	private boolean createConceptArray(){
		Set<Object> vocabNames= vocabList.keySet();
		vocabs = new Vector<Vocabulary>();
		try{
		for(Object name: vocabNames){
			String sName = (String) name;
			String date = vocabList.getProperty(sName);
			Vocabulary vocab = new Vocabulary(sName,date);
			vocabs.add(vocab);
		}} catch(Exception e){
			return false;
		}
		return true;
	}

	private void updatePropertiesFile() {
		// TODO if we do download a new file, update the properties file with
		// the new date
	}
}
