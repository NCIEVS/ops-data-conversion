package gov.nih.nci.evs.canmed;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLStorer;



public class CanmedOwlWriter {
	String typeConstantString = "http://www.w3.org/2001/XMLSchema#string";
	
	/** The manager. */
	private OWLOntologyManager manager;

	/** The ontology. */
	private OWLOntology ontology;
	private OWLDataFactory factory;
	private IRI ontologyIRI = IRI.create("http://seer.nci.nih.gov/CanMED/NGC.owl");

	public CanmedOwlWriter(CanmedOntology canmedOntology, URI saveURI) {
		try {
		this.manager = OWLManager.createOWLOntologyManager();
		
		this.ontology = manager.createOntology(ontologyIRI);
		factory = manager.getOWLDataFactory();
		createConcepts(canmedOntology);
		saveOntology(saveURI);
//		URI owl2 = URI.create(saveURI + "2");
//		System.out.print(owl2.toString());
//
//		manager.saveOntology(ontology, IRI.create(owl2));
		
		
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Problem creating OWL output.");
			System.exit(0);
		}
	}
	
	
	private void createConcepts(CanmedOntology cmOntology) {
		HashMap<String, CanmedConcept> conceptMap = cmOntology.getConcepts();
		
		for (String conceptCode : conceptMap.keySet()) {
			if(conceptCode.equals("Chemotherapy_Hormonal_Therapy")) {
				String debug="true";
				
			}
			// Assume hierarchy is unknown
			// OWLClass clz = factory.getOWLClass(URI.create(ontologyURI
			// + concept.code));
			try {
			CanmedConcept concept = conceptMap.get(conceptCode);
			OWLClass clz = factory.getOWLClass(createIRI(concept.getCode().replace(":", "_")));

			if(concept.getParents() != null && concept.getParents().size()>0) {
				for(String parentString: concept.getParents()) {
					OWLClass parent = factory.getOWLClass(createIRI(parentString));
					AddAxiom addAxiom = commitConcept(clz, parent, concept);
//					OWLAxiom axiom = factory.getOWLSubClassOfAxiom(clz, parent);
//					AddAxiom addAxiom = new AddAxiom(ontology, axiom);
//					manager.applyChange(addAxiom);
					addProperties(concept, addAxiom);
				}
			} else {
				OWLClass parent =  factory.getOWLThing();
//				OWLAxiom axiom = factory.getOWLSubClassOfAxiom(clz, parent);
//				AddAxiom addAxiom = new AddAxiom(ontology, axiom);
//				manager.applyChange(addAxiom);
				AddAxiom addAxiom = commitConcept(clz, parent, concept);
				addProperties(concept, addAxiom);
			}
			// OWLAxiom axiom = factory.getOWLSubClassAxiom(clz, factory
			// .getOWLThing());
			
			} catch (Exception e) {
				System.out.println("Error adding axiom :" + conceptCode);
			}			
		}
	}
	
	private AddAxiom commitConcept(OWLClass clz, OWLClass parent, CanmedConcept concept ) {
		OWLAxiom axiom = factory.getOWLSubClassOfAxiom(clz , parent);
		AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		manager.applyChange(addAxiom);
		return addAxiom;
	}
	
	private void addProperties(CanmedConcept concept, AddAxiom ax) {

		//TODO rdfs:Label?
		HashMap<String,String> properties = concept.getProperties();
		Set<OWLOntologyChange> changes = new HashSet<>();
		OWLEntity ent = null;
		OWLSubClassOfAxiom subAx = (OWLSubClassOfAxiom) ax.getAxiom();

		ent = subAx.getSubClass().asOWLClass();
		OWLDatatype odt = factory.getOWLDatatype(IRI.create(typeConstantString));
		Set<String> propNames = properties.keySet();
		for (String propName : propNames) {
			try {

				String test = properties.get(propName);
			if(test != null  && !test.isEmpty()) {
			OWLAnnotationProperty aProp = this.manager.getOWLDataFactory()
					.getOWLAnnotationProperty(createIRI(propName));
			OWLAnnotation anno = factory.getOWLAnnotation(aProp, factory.getOWLLiteral(properties.get(propName)));
			final OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(ent.getIRI(), anno);

			changes.add(new AddAxiom(ontology, ax1));
			}
			} catch (Exception e) {
				System.out.println("Error with " + concept.getCode() + " property " + propName);
				throw e;
			}
		}
		for(String route:concept.getAdminRoutes()) {
			OWLAnnotationProperty aProp = this.manager.getOWLDataFactory()
					.getOWLAnnotationProperty(createIRI("Administration_Route"));
			OWLAnnotation anno = factory.getOWLAnnotation(aProp, factory.getOWLLiteral(route));
			final OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(ent.getIRI(), anno);
			changes.add(new AddAxiom(ontology, ax1));
		}

		if (!changes.isEmpty()) {
			List<OWLOntologyChange> list = new ArrayList<>(changes);
			manager.applyChanges(list);
		}
	}
	
	/**
	 * Save ontology to the file specified in the properties By default encodes
	 * to utf-8
	 */
	private void saveOntology(URI saveURI) {
		try {
			RDFXMLStorer storer = new RDFXMLStorer();
			File newFile = new File(saveURI);
			FileOutputStream out = new FileOutputStream(newFile);
			WriterDocumentTarget target = new WriterDocumentTarget(
					new BufferedWriter(new OutputStreamWriter(out, "UTF8")));
			OWLXMLDocumentFormat format = new OWLXMLDocumentFormat();
			storer.storeOntology(ontology, target, format);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public IRI createIRI(String className) {
		try {
			if(!(className ==null)) {
		String urlCompliantClassName = underscoredString(className);
		return IRI.create(ontologyIRI + "#" + urlCompliantClassName);
			} else return null;
		} catch (Exception e){
			System.out.println("Problem with " + className);
			throw e;
		}
	}
	
	public static String underscoredString(String input) {

		return input.trim().replace(" ", "_").replace("(", "_").replace(")", "_").replace(",", "").replace(":", "_");
	}
}
