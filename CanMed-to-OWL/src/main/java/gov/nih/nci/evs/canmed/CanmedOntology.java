package gov.nih.nci.evs.canmed;

import java.util.HashMap;
import java.util.List;



public class CanmedOntology {
	
	private CanmedCsvParser parser = null;

	public HashMap<String, CanmedConcept> getConcepts() {
		return concepts;
	}

	private HashMap<String, CanmedConcept> concepts = new HashMap<String, CanmedConcept>();

	public CanmedOntology(CanmedCsvParser parser) {
		this.parser= parser;
		createConcepts();
	}
	
	private void createConcepts() {
		List<String> header = parser.getHeader();
		for(List<String> dataLine : parser.getLineByLineData()) {
			HashMap<String, String> propertyValueList = new HashMap<>();
			for (int i = 0; i < header.size(); i++) {
				if (i < dataLine.size()) {
					propertyValueList.put(header.get(i), dataLine.get(i));
				} else {
					propertyValueList.put(header.get(i), null);
				}
			}
			
			CanmedConcept concept = new CanmedConcept(propertyValueList);
			createMajorHierarchy(concept);
			concepts.put(concept.getCode(),concept);
		}
		
	}
	
	private void createMajorHierarchy(CanmedConcept concept) {
		//Check the 3 hierarchy columns
		//SEER*Rx Category,	Major Class, Minor Class
		//If it doesn't exist, create concept for it
		
		//TODO handle multiple treeing of these.
		try {
			if(concept.getCode().equals("00078-0923-61")) {
				String debug="true";
			}
			if(concept.getCode().equals("Cycline_dependent_kinase_inhibiter")) {
				String debug="true";
				
			}
		String testParent = parseConceptCode(concept.getProperty("Minor_Class"));

		if (concepts.get(testParent)==null && testParent!=null) {
			//add as new concept			
			CanmedConcept hierConcept = new CanmedConcept(testParent, concept.getProperty("Minor_Class"),parseConceptCode( concept.getProperty("Major_Class")));
			concepts.put(testParent, hierConcept);
		} 
		testParent = parseConceptCode(concept.getProperty("Major_Class"));
		if (concepts.get(testParent)==null && testParent!=null) {
			//add as new concept
			CanmedConcept hierConcept = new CanmedConcept(testParent, concept.getProperty("Major_Class"),parseConceptCode(concept.getProperty("SEER*Rx_Category")));
			concepts.put(testParent, hierConcept);
		}
		testParent = parseConceptCode(concept.getProperty("SEER*Rx_Category"));
		if(testParent.equals("Chemotherapy_Hormonal_Therapy")) {
			String debug = "true";
		}
		if (concepts.get(testParent)==null && testParent!=null && !testParent.equals("")) {
			//add as new concept
			CanmedConcept hierConcept = new CanmedConcept(testParent, concept.getProperty("SEER*Rx_Category"),null);
			concepts.put(testParent, hierConcept);
		}
		} catch(Exception e) {
			System.out.println("Problem with "+ concept.getCode());
		}
		
		
	}
	
	public static String parseConceptCode(String rawCode) {
		if(rawCode !=null) {
			rawCode = rawCode.replace("\"", "");
		rawCode = rawCode.replace(" ", "_");
		rawCode =rawCode.replace(",", "_");
		rawCode = rawCode.replace("__", "_");
		rawCode = rawCode.replace("/", "-");
		}
		return rawCode;
	}

}
