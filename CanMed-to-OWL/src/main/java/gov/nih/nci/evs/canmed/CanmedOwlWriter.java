package gov.nih.nci.evs.canmed;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLStorer;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * Takes the CanmedOntology, parses and writes it out into OWL
 */
class CanmedOwlWriter {
    static String typeConstantString = "http://www.w3.org/2001/XMLSchema#string";
    private static IRI ontologyIRI = IRI.create("http://seer.nci.nih.gov/CanMED.owl");

    /**
     * The manager.
     */
    private OWLOntologyManager manager;

    /**
     * The ontology.
     */
    private OWLOntology ontology;
    private OWLDataFactory factory;

    /**
     * Input the CanmedOntology object and a URI location for saving the final file
     * @param canmedOntology
     * @param saveURI
     */
    public CanmedOwlWriter(CanmedOntology canmedOntology, URI saveURI) {
        try {
            this.manager = OWLManager.createOWLOntologyManager();

            this.ontology = manager.createOntology(ontologyIRI);
            factory = manager.getOWLDataFactory();
            createConcepts(canmedOntology);
            saveOntology(saveURI);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Problem creating OWL output.");
            System.exit(0);
        }
    }

    /**
     * Create a valid IRI from the concept code
     *
     * @param className
     * @return
     */
    public static IRI createIRI(String className) {
        try {
            if (!(className == null)) {
                String urlCompliantClassName = underscoredString(className);
                return IRI.create(ontologyIRI + "#" + urlCompliantClassName);
            } else return null;
        } catch (Exception e) {
            System.out.println("Problem with " + className);
            throw e;
        }
    }

    /**
     * Return a string with spaces and parenthesis replaced to form valid OWL concept ids
     *
     * @param input
     * @return
     */
    public static String underscoredString(String input) {

        return input.trim().replace(" ", "_").replace("(", "_").replace(")", "_").replace(",", "").replace(":", "_");
    }

    /**
     * Parse CanmedOntology and turn each concept into an annotated OWLClass
     *
     * @param cmOntology
     */
    private void createConcepts(CanmedOntology cmOntology) {
        HashMap<String, CanmedConcept> conceptMap = cmOntology.getConcepts();

        for (String conceptCode : conceptMap.keySet()) {

            // Assume hierarchy is unknown

            try {
                CanmedConcept concept = conceptMap.get(conceptCode);
                OWLClass clz = factory.getOWLClass(createIRI(concept.getCode().replace(":", "_")));

                if (concept.getParents() != null && concept.getParents().size() > 0) {
                    for (String parentString : concept.getParents().keySet()) {
                        OWLClass parent = factory.getOWLClass(createIRI(parentString));
                        AddAxiom addAxiom = commitConcept(clz, parent);
                        addProperties(concept, addAxiom);
                        addSynonyms(concept, addAxiom);
                    }
                } else {
                    OWLClass parent = factory.getOWLThing();
                    AddAxiom addAxiom = commitConcept(clz, parent);
                    addProperties(concept, addAxiom);
                    addSynonyms(concept, addAxiom);
                }

            } catch (Exception e) {
                System.out.println("Error adding axiom: " + conceptCode);
            }
        }
    }

    /**
     * Create the concept axiom and assign a parent
     *
     * @param clz
     * @param parent
     * @return
     */
    private AddAxiom commitConcept(OWLClass clz, OWLClass parent) {
        OWLAxiom axiom = factory.getOWLSubClassOfAxiom(clz, parent);
        AddAxiom addAxiom = new AddAxiom(ontology, axiom);
        manager.applyChange(addAxiom);
        return addAxiom;
    }

    /**
     * Annotate the OWLClass with the properties read from the CanmedConcept
     *
     * @param concept
     * @param ax
     */
    private void addProperties(CanmedConcept concept, AddAxiom ax) {

        HashMap<String, String> properties = concept.getProperties();
        Set<OWLOntologyChange> changes = new HashSet<>();


        OWLSubClassOfAxiom subAx = (OWLSubClassOfAxiom) ax.getAxiom();

        OWLEntity ent = subAx.getSubClass().asOWLClass();

        //set rdfs:Label
        OWLLiteral lbl = factory.getOWLLiteral(concept.getName());
        OWLAnnotation label = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), lbl);
        final OWLAxiom rdfsLabel = factory.getOWLAnnotationAssertionAxiom(ent.getIRI(), label);
        changes.add(new AddAxiom(ontology, rdfsLabel));


        Set<String> propNames = properties.keySet();
        for (String propName : propNames) {
            try {

                String test = properties.get(propName);
                if (test != null && !test.isEmpty()) {
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
        if (concept instanceof NdcConcept) {
            for (String route : ((NdcConcept) concept).getAdminRoutes()) {
                OWLAnnotationProperty aProp = this.manager.getOWLDataFactory()
                        .getOWLAnnotationProperty(createIRI("Administration_Route"));
                OWLAnnotation anno = factory.getOWLAnnotation(aProp, factory.getOWLLiteral(route));
                final OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(ent.getIRI(), anno);
                changes.add(new AddAxiom(ontology, ax1));
            }
        }

        if (!changes.isEmpty()) {
            List<OWLOntologyChange> list = new ArrayList<>(changes);
            manager.applyChanges(list);
        }
    }

    /**
     * Annotate the OWLClass with a set of trial synonyms for review by Canmed
     *
     *
     * @param concept
     * @param axiom
     * @throws Exception
     */
    private void addSynonyms(CanmedConcept concept, AddAxiom axiom) throws Exception {
        List<String> synonyms = concept.getSynonyms();


        Set<OWLOntologyChange> changes = new HashSet<>();

        OWLSubClassOfAxiom subAx = (OWLSubClassOfAxiom) axiom.getAxiom();
        OWLEntity ent = subAx.getSubClass().asOWLClass();
//		OWLDatatype odt = factory.getOWLDatatype(IRI.create(typeConstantString));
        for (String synonym : synonyms) {
            try {

                OWLAnnotationProperty aProp = this.manager.getOWLDataFactory()
                        .getOWLAnnotationProperty(createIRI("synonym"));
                OWLAnnotation anno = factory.getOWLAnnotation(aProp, factory.getOWLLiteral(synonym));
                final OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(ent.getIRI(), anno);

                changes.add(new AddAxiom(ontology, ax1));

            } catch (Exception e) {
                System.out.println("Error with " + concept.getCode() + " property " + synonym);
                throw e;
            }
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
                    new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
            OWLXMLDocumentFormat format = new OWLXMLDocumentFormat();
            storer.storeOntology(ontology, target, format);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
