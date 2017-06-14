package gov.nih.nci.bopcheck;

import gov.nih.nci.evs.Vocabulary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

public class Wget {
	private final static Logger LOGGER = Logger.getLogger(Wget.class.getName());
	private HashMap<String, String> vocabsFound = new HashMap<String, String>();
	Vector<Vocabulary> vocabs = null;

	public Wget() {

	}

	public boolean get(String url, Vector<Vocabulary> vocabs) {
		try {

			this.vocabs = vocabs;
			URL location = new URL(url);

			URLConnection connection = location.openConnection();

			connection.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String inputLine;
			String data = null;
			Vector<String> vocabsToExamine = new Vector<String>();
			while ((inputLine = in.readLine()) != null) {
				data += inputLine + "\n";
				if (compareLine(inputLine)) {
					vocabsToExamine.add(inputLine);
				}
			}
			in.close();

			return compareDataToWantedVocabs(vocabsToExamine);
		} catch (MalformedURLException e) {
			LOGGER.severe("Unable to contact berkeleybop due to malformed URL");
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.severe("Unable to read from berkeleybop web site");
			e.printStackTrace();
		}
		return false;
	}

	private boolean compareDataToWantedVocabs(Vector<String> data) {
		boolean newFilesFound = false;
		// parse the data to something reasonable.
		// compare the list to the vocabs of interest
		// check the dates are newer than ours
		// if they are, download the file then update our properties file.
		HashMap<String, String> tempMap = new HashMap<String, String>();
		for (String dataLine : data) {
			String coreData = dataLine.substring(
					dataLine.lastIndexOf("a href"),
					dataLine.lastIndexOf(":") + 5);
			coreData = coreData.substring(coreData.indexOf(">") + 1);
			coreData = coreData.replace("</a></td><td align=\"right\">", "\t");
			String[] temp = coreData.split("\t");
			tempMap.put(temp[0], temp[1].trim());
		}

		return compareDates(tempMap);

	}

	private boolean compareDates(HashMap<String, String> tempData) {

		for (Vocabulary vocab : this.vocabs) {
			String searchString = vocab.getName();
			String tempDate = tempData.get(vocab).trim();
			if (tempDate == null) {
				LOGGER.info("No true match for " + searchString);
			}
			try {
				Date realDate = Vocabulary.convertStringDateToDate(tempDate);
				if (vocab.isAfterDate(realDate)) {
					vocab.setNeedsUpdate(true);
				}
			} catch (ParseException e) {
				LOGGER.info("Unable to parse date for " + vocab);
				e.printStackTrace();
			}

		}
		// TODO put more error checking in to compare the dates.
		return true;
	}

	private boolean compareLine(String inputLine) {

		// Check the stream from the web and snag any lines that match our vocab
		// name.

		for (Vocabulary vocab : this.vocabs) {
			String searchString = vocab.getName();
			if (inputLine.contains(searchString)) return true;
		}
		return false;

	}

}
