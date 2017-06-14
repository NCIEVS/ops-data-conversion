package gov.nih.nci.evs;

import gov.nih.nci.evs.dda.DataDownloadApp;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger {

//TODO Make logger append
    static private FileHandler fileTxt;

	static private SimpleFormatter formatterTxt;

	static public void setup() throws IOException {

		// Get the global logger to configure it

		Logger logger = Logger.getLogger(DataDownloadApp.class.getName());

		logger.setLevel(Level.INFO);

		fileTxt = new FileHandler("Logging.txt");

		// Create txt Formatter

		formatterTxt = new SimpleFormatter();

		fileTxt.setFormatter(formatterTxt);

		logger.addHandler(fileTxt);
		logger.info("Log created");
	}
	


}
