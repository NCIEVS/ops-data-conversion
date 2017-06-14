
/**
 * This class processes the NCI Thesaurus to have it show up better in BioPortal. 
 * Specifically, we perform the following clean-up actions:
 *  - remove mark-up from instances
 *   
 * @author Natasha Noy
 * 
 */

//package org.bioontology.ncitProcessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.impl.AbstractOWLModel;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.util.FileUtils;

public class NCITProcessor {
	private static Project _sourceProject = null;
//	private static AbstractOWLModel _sourceKB = null;
	
	private static JenaOWLModel _sourceKB = null;
	
	private static RDFSNamedClass _owlClass = null;
//	private static RDFProperty _synonymProperty = null;
//	private static RDFProperty _definitionProperty = null;
//	private static Vector<TermObject> terms = new Vector<TermObject>();
	private static HashMap<String, TermObject> termMap = new HashMap<String,TermObject>();
	
	// NCI Thesaurus properties	
//	private static final String SYNONYM_PROPERTY = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#FULL_SYN";
//	private static final String DEFINITION_PROPERTY = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#DEFINITION";
	
	// NCI Thesaurus byCode properties	
//	private static final String SYNONYM_PROPERTY = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90";
//	private static final String DEFINITION_PROPERTY = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P97";

	// NPO properties
//	private static final String SYNONYM_PROPERTY = "http://purl.bioontology.org/ontology/npo#FULL_SYN";
//	private static final String DEFINITION_PROPERTY = "http://purl.bioontology.org/ontology/npo#definition";
	
	private static String namespace = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#";
	private static String xmlBase = "xml:base=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl\"";

	
	public static void main(String[] args) {
		Collection<String> errors = new ArrayList<String>();
		
		Log.getLogger().info("Start conversion at " + new Date ());
		
		String sourceFileURI = args[0];
		String configFileAddress = args[1];
		String saveFileURI = sourceFileURI + "-NCBO.owl";
		Log.getLogger().info("Test print args: " + args[0]);
		Log.getLogger().info("Test config args: " + args[1]);
		
		try{
			InputStream configFile = new FileInputStream(configFileAddress);
			readConfigFile(configFile);
		}
		catch (Exception e){
			Log.getLogger().info("No readable config file supplied.  Exiting");
			System.exit(0);
		}

		
		try {
			File owlFile = new File(sourceFileURI);
			FileInputStream owlStream = new FileInputStream(owlFile);
			_sourceKB = ProtegeOWL.createJenaOWLModelFromInputStream(owlStream); 
	//		_sourceKB = ProtegeOWL.createJenaOWLModelFromURI(sourceFileURI); 
	//	} catch (OntologyLoadException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		processKnowledgeBase ();

		String tmpFileName = saveFileURI+"_temp";
		
		try {
//			((JenaOWLModel) _sourceKB).save(new File(saveFileURI).toURI());
			_sourceKB.save(new File(tmpFileName).toURI());
			
			//TODO add xml:base
			addBase(tmpFileName,saveFileURI);
			File file = new File(tmpFileName);
			file.delete();
//			_sourceKB.save(new File(saveFileURI).toURI(),FileUtils.langXMLAbbrev, errors);
//			JenaOWLModel.save(new File(saveFileURI), _sourceKB.getOntModel(), FileUtils.langXMLAbbrev,namespace,xmlBase);
//			_sourceKB.save(new File(saveFileURI),  _sourceKB.getOntModel(), FileUtils.langXML,namespace,xmlBase);
	//		((JenaOWLModel) _sourceKB).save(new URI(saveFileURI));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Log.getLogger().info("DONE: " + new Date());
	}
	
	
	private static void addBase(String inputFilePath, String exportFilePath) throws IOException{
		//This will remove the HTML header and footer information.
		/*grep "<tr><td class=\"aws\">" temp-$myserver-$ldate.htm > temp2-$myserver-$ldate.htm
     sed \
	-e 's/<tr><td class=\"aws\">/	/' \
	-e 's/<\/td><td>/	/g' \
	-e 's/<span style=\"color: #666688\">/	/g' \
	-e 's/<\/span>/	/g' \
	-e 's/<\/td><\/tr>/	/' < temp2-$myserver-$ldate.htm > $1*/
		String line = "";
		String regLine = "xmlns=\"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#\"";
		FileReader fir=null;
		BufferedReader reader=null;
		FileWriter fw=null;
		BufferedWriter fileWriter=null;
		
		try {
			fir = new FileReader(inputFilePath);
			reader = new BufferedReader(fir);
			fw = new FileWriter(exportFilePath);
			fileWriter = new BufferedWriter(fw);
			
		while ((line = reader.readLine()) !=null){
//			System.out.println(line);

	            Pattern p = Pattern.compile(regLine);
	            // get a matcher object
	            Matcher m = p.matcher(line);
	            line = m.replaceAll(line+"\n"+xmlBase);	            
			
	            

//			System.out.println(line);
//			if (line.length()>4){
			fileWriter.write(line+"\n");
			}
		reader.close();
		fileWriter.close();
//		}

		}
		catch(FileNotFoundException e){
			System.out.println("File not found");
		}
		catch(IOException e){
			System.out.println("IOException");
		}

	}
	
	private static void processKnowledgeBase () {
		Collection<RDFSClass> roots = _sourceKB.getRootClses();
		
		
		Vector<RDFSClass> classesInQueue = new Vector<RDFSClass> ();
		classesInQueue.addAll(roots);

		HashSet<RDFSClass> classesProcessed = new HashSet<RDFSClass> (_sourceKB.getClsCount());
		
		_owlClass = _sourceKB.getSystemFrames().getOwlNamedClassClass();
		
//		_synonymProperty = (RDFProperty)_sourceKB.getOWLProperty(SYNONYM_PROPERTY);
//		_definitionProperty = (RDFProperty)_sourceKB.getOWLProperty(DEFINITION_PROPERTY);
		
		//check if the ontology has any instances of the property we want to process
		boolean propertyFound = checkExistanceInKB();
		if (!propertyFound) return;
		
		int counter = 0;

		while (!classesInQueue.isEmpty()) {
			RDFSClass next = (RDFSClass)classesInQueue.firstElement();
    		classesInQueue.remove(0);
    		
    		if (classesProcessed.contains(next)) continue;
    		classesProcessed.add(next);
    	
       		if (!(next instanceof RDFSNamedClass)) continue;
       	 
       		if (counter % 100 == 0)
    			Log.getLogger().info("Processing: " + counter + ":" + next);

     	
    		processClass ((RDFSNamedClass)next);

    		Collection<RDFSClass> nextSubs = next.getSubclasses(false);
    		if (nextSubs != null)
    			classesInQueue.addAll(0,  nextSubs);
    		
     		counter++;
		}
				
	}

	private static boolean checkExistanceInKB() {
		boolean found = false;
		Set<String> testTerms = termMap.keySet();
		Iterator iter = testTerms.iterator();
		while(iter.hasNext()){
			RDFProperty testProp = (RDFProperty)_sourceKB.getOWLProperty((String) iter.next());
			if (testProp != null){
				found = true;
			}
		}
		return found;
	}

	/**
	 * Read and parse the config file:
	 * 	- read the config file in and load to vector
	 *  - parse each vector line to determine terms and their start and end tags
	 *  - load the terms and tags into a vector of TermObject 
	 * 
	 * @param configFile 
	 */
	private static void readConfigFile(InputStream configFile){
		try{
			InputStreamReader isr = new InputStreamReader(configFile, "8859_1");
			BufferedReader in = new BufferedReader(isr);
			String lineItem = new String();
			while((lineItem = in.readLine()) !=null){
				TermObject to = parse(lineItem);
				termMap.put(to.getName(), to);
			}
			
			
		}catch (Exception e){
			Log.getLogger().info("Config file could not be parsed. Exiting");
			System.exit(0);
		}
				
	}
	
	/**
	 * Take the input line and load it into a TermObject:
	 *  - return the TermObject 
	 * 
	 * @param line
	 */	
	private static TermObject parse(String line){
		String[] lineTokens = line.split("\t");
		try{
			TermObject ob = new TermObject(lineTokens[0], lineTokens[1],lineTokens[2]);
			return ob;
		}
		catch (Exception e){
			Log.getLogger().info("Config file must have term \\t startTag \\t endTag");
			System.exit(0);
			return null;
		}
	}
	
	/**
	 * Process a single class:
	 * 	- change the value for FULL_SYN
	 *  - remove duplicate values from FULL_SYN and remove those values  that are identical to the class name
	 *   - change the value for Definition 
	 * 
	 * @param cls
	 */
//	private static final String TERMNAME_OPEN = "<ncicp:term-name>";
//	private static final String TERMNAME_CLOSE = "</ncicp:term-name>";
//	private static final String DEFINITION_OPEN = "<ncicp:def-definition>";
//	private static final String DEFINITION_CLOSE = "</ncicp:def-definition>";
	
//	private static final String TERMNAME_OPEN = "&lt;ncicp:term-name&gt;";
//	private static final String TERMNAME_CLOSE = "&lt;/ncicp:term-name&gt;&lt;";
//	private static final String DEFINITION_OPEN = "&lt;ncicp:def-definition&gt;";
//	private static final String DEFINITION_CLOSE = "&lt;/ncicp:def-definition";

	
	private static void processClass(RDFSNamedClass cls) {

		if (cls.isSystem()) return;

		String mCName = (String) cls.getBrowserText();
		try {
			Set<String> testTerms = termMap.keySet();
			Iterator iter = testTerms.iterator();
			while(iter.hasNext()){
	//			TermObject term = termMap.get(iter);
	//			RDFProperty testProp = (RDFProperty)_sourceKB.getOWLProperty( iter.next().toString());
				TermObject term = termMap.get(iter.next());
				RDFProperty testProp = (RDFProperty)_sourceKB.getOWLProperty( term.getName() );
				if(testProp != null){
					processPropertyForClass(cls, testProp,term.getStartTag(),term.getEndTag());}
					}
			} catch (Exception e) {
				System.out.println ("Cannot process property in class " + mCName );

			}
		
//		processPropertyForClass (cls, _synonymProperty, TERMNAME_OPEN, TERMNAME_CLOSE);
//		processPropertyForClass (cls, _definitionProperty, DEFINITION_OPEN, DEFINITION_CLOSE);
	}
	
	private static void processPropertyForClass (RDFSNamedClass cls, RDFProperty property, String start, String end) {
		if (cls.isSystem()) return;
		Collection<DefaultRDFSLiteral> oldValues = cls.getPropertyValues(property);
		Collection<String> newValues = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		String label = (String) CollectionUtilities.getFirstItem(cls.getLabels());

		for (Object value : oldValues) {
			String stringValue;
			// deal with NPO, where definition can be a String or an instance if RDFSLiteral
			if (value instanceof DefaultRDFSLiteral)
				stringValue = ((DefaultRDFSLiteral)value).getRawValue();
			else
				stringValue = (String) value;
			stringValue = removeMarkup (stringValue, start, end);
			if (stringValue.equalsIgnoreCase(label)) continue;
			newValues.add(stringValue);
		}
		
		cls.setPropertyValues(property, newValues);
		
	}

	private static String removeMarkup (String str, String start, String end) {
		int startIndex = str.indexOf(start) + start.length();
		int endIndex = str.indexOf(end);
		
		if (endIndex > 0 && startIndex < endIndex && startIndex >= start.length()) 
			str = str.substring(startIndex, endIndex);
		
		return str;
	}

	private static void displayErrors(Collection errors) {
		Iterator i = errors.iterator();
		while (i.hasNext()) {
			System.out.println("Error: " + i.next());
		}
	}
	}
