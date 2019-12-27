package gov.nih.nci.evs.canmed;

import gov.nih.nci.evs.canmed.entity.Ontology;
import gov.nih.nci.evs.canmed.entity.concept.HcpcsPackage;
import gov.nih.nci.evs.canmed.entity.concept.HierarchicalConcept;
import gov.nih.nci.evs.canmed.entity.concept.NdcPackage;
import gov.nih.nci.evs.canmed.entity.concept.NdcProduct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.io.WriterDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLStorer;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * Takes the CanmedOntology, parses and writes it out into OWL
 */
class OwlWriter {
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
    public OwlWriter(Ontology canmedOntology, URI saveURI) {
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
    private void createConcepts(Ontology cmOntology) {
        HashMap<String, HierarchicalConcept> conceptMap = cmOntology.getConcepts();

        for (String conceptCode : conceptMap.keySet()) {
            if(conceptCode.equals("HCPCS_")){
                String debug="Stop";
            }
            // Assume hierarchy is unknown

            try {
                HierarchicalConcept concept = conceptMap.get(conceptCode);
                OWLClass clz = factory.getOWLClass(createIRI(concept.getCode().replace(":", "_")));
                OWLClass parent = factory.getOWLThing();

                if (concept.getParentConcepts() != null && concept.getParentConcepts().size() > 0) {
                    for (HierarchicalConcept parentConcept : concept.getParentConcepts()) {
                        parent = factory.getOWLClass(createIRI(parentConcept.getCode()));
                        commitConcept(clz, parent);
                    }
                } else {
                    commitConcept(clz, parent);
                }



                if(concept instanceof NdcPackage){
                    loadNDCpackage((NdcPackage)concept,clz);
                    setRdfsLabel(concept.getName()+" "+ ((NdcPackage) concept).getNdcPackageCode(), clz);
                } else if (concept instanceof NdcProduct) {
                    loadNDCproduct((NdcProduct)concept,clz);
                    setRdfsLabel(concept.getName()+" "+ ((NdcProduct) concept).getNdcProductCode(), clz);
                } else if (concept instanceof HcpcsPackage) {
                    loadHCPCSpackage((HcpcsPackage)concept,clz);
                    if(((HcpcsPackage) concept).getStrength()!=null) {
                        setRdfsLabel(concept.getName() + " " + ((HcpcsPackage) concept).getStrength(), clz);
                    } else {
                        setRdfsLabel(concept.getName(), clz);
                    }
                } else {
                    //add rdfs:Label for concept.getName
                    setRdfsLabel(concept.getName(), clz);
                }

            } catch (Exception e) {
                System.out.println("Error adding axiom: " + conceptCode);
            }
        }
    }

    private void setRdfsLabel(String name, OWLClass clz){
        OWLLiteral lbl = factory.getOWLLiteral(name);
        OWLAnnotation label = factory.getOWLAnnotation(factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), lbl);
        final OWLAxiom rdfsLabel = factory.getOWLAnnotationAssertionAxiom(clz.getIRI(), label);
        manager.applyChange(new AddAxiom(ontology, rdfsLabel));
    }

    private void setDeprecated(OWLClass clz){
        OWLRDFVocabulary.OWL_DEPRECATED_CLASS.getIRI();
        OWLAnnotationAssertionAxiom axiom = factory.getDeprecatedOWLAnnotationAssertionAxiom(clz.getIRI());
        manager.applyChange(new AddAxiom(ontology, axiom));
    }

    private void loadNDCpackage(NdcPackage concept, OWLClass clz){
        loadNDCproduct((NdcProduct)concept, clz);
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        OWLAxiom ax1 = buildAxiom(clz, "NDC_Package_Code",concept.getNdcPackageCode());
        changes.add(new AddAxiom(ontology, ax1));
//        concept.getNdcProductCode();
//        concept.getBrandNames();
//        concept.getRoutesOfAdministration();
        if(concept.getStatus()!=null) {
            ax1 = buildAxiom(clz, "Status", concept.getStatus());
            changes.add(new AddAxiom(ontology, ax1));
        }

        if(concept.getStrength()!=null) {
            ax1 = buildAxiom(clz, "Strength", concept.getStrength());
            changes.add(new AddAxiom(ontology, ax1));
        }

        if (concept.getPackageDiscontinueDate()!=null) {
            ax1 = buildAxiom(clz, "Discontinue_Date", concept.getPackageDiscontinueDate());
            changes.add(new AddAxiom(ontology, ax1));
        }

        if(concept.getPackageEffectiveDate()!=null) {
            ax1 = buildAxiom(clz, "Effective_Date", concept.getPackageEffectiveDate());
            changes.add(new AddAxiom(ontology, ax1));
        }

        manager.applyChanges(changes);


        if(!concept.isActive()){
            setDeprecated(clz);
        }
    }

    private void loadNDCproduct(NdcProduct concept, OWLClass clz){
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        OWLAxiom ax1 = buildAxiom(clz, "NDC_Product_Code",concept.getNdcProductCode());
        changes.add(new AddAxiom(ontology, ax1));

        ax1 = buildAxiom(clz, "Generic_Name", concept.getName());
        changes.add(new AddAxiom(ontology, ax1));

        for(String brandName: concept.getBrandNames()){
            ax1 = buildAxiom(clz, "Brand_Name",brandName);
            changes.add(new AddAxiom(ontology, ax1));
        }

        for(String route:concept.getRoutesOfAdministration()){
            ax1 = buildAxiom(clz, "Route_of_Administration",route);
            changes.add(new AddAxiom(ontology, ax1));
        }
        manager.applyChanges(changes);
    }

    private void loadHCPCSpackage(HcpcsPackage concept, OWLClass clz){
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        OWLAxiom ax1;
        ax1 = buildAxiom(clz, "Generic_Name", concept.getName());
        changes.add(new AddAxiom(ontology, ax1));

        if(concept.getHcpcsCode()!=null) {
            ax1 = buildAxiom(clz, "HCPCS_Code", concept.getHcpcsCode());
            changes.add(new AddAxiom(ontology, ax1));
        }

        for(String brandName: concept.getBrandNames()){
            ax1 = buildAxiom(clz, "Brand_Name",brandName);
            changes.add(new AddAxiom(ontology, ax1));
        }

        if(concept.getStatus()!=null) {
            ax1 = buildAxiom(clz, "Status", concept.getStatus());
            changes.add(new AddAxiom(ontology, ax1));
        }

        if(concept.getStrength()!=null) {
            ax1 = buildAxiom(clz, "Strength", concept.getStrength());
            changes.add(new AddAxiom(ontology, ax1));
        }

        if(concept.getOral()!=null) {
            ax1 = buildAxiom(clz, "Oral", concept.getOral());
            changes.add(new AddAxiom(ontology, ax1));
        }

        if(concept.getFdaApproval()!=null) {
            ax1 = buildAxiom(clz, "FDA Approval Date", concept.getFdaApproval());
            changes.add(new AddAxiom(ontology, ax1));
        }

        if(concept.getFdaDiscontinue()!=null) {
            ax1 = buildAxiom(clz, "FDA Discontinue Date", concept.getFdaDiscontinue());
            changes.add(new AddAxiom(ontology, ax1));
        }

        if(concept.getCmsEffective()!=null) {
            ax1 = buildAxiom(clz, "CMS Effective Date", concept.getCmsEffective());
            changes.add(new AddAxiom(ontology, ax1));
        }

        if(concept.getCmsDiscontinue()!=null) {
            ax1 = buildAxiom(clz, "CMS Discontinue Date", concept.getCmsDiscontinue());
            changes.add(new AddAxiom(ontology, ax1));
        }

        manager.applyChanges(changes);

        if(!concept.isActive()){
            setDeprecated(clz);
        }
    }

    OWLAxiom buildAxiom(OWLClass clz, String propName, String propValue){
        OWLAnnotationProperty aProp = this.manager.getOWLDataFactory()
                .getOWLAnnotationProperty(createIRI(propName));
        OWLAnnotation anno = factory.getOWLAnnotation(aProp, factory.getOWLLiteral(propValue));
        final OWLAxiom ax1 = factory.getOWLAnnotationAssertionAxiom(clz.getIRI(), anno);
        return ax1;
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
