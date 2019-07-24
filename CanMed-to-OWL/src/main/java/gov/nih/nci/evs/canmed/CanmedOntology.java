package gov.nih.nci.evs.canmed;

import java.util.HashMap;
import java.util.List;


class CanmedOntology {


    private HashMap<String, CanmedConcept> concepts = new HashMap<String, CanmedConcept>();

    /**
     * Takes both NDC and HCPCS parsers and reads the data into CanmedOntology object
     *
     * @param NDCparser
     * @param HCPCSparser
     */
    CanmedOntology(CanmedCsvParser NDCparser, gov.nih.nci.evs.canmed.CanmedCsvParser HCPCSparser) {
        createNDCconcepts(NDCparser);
        createHCPCSconcepts(HCPCSparser);
    }

    /**
     * Concept codes must be able to make valid URI fragments
     * This converts spaces, slashes and the like to neutral characters
     *
     * @param rawCode
     * @return valid string fragment for URI
     */
    public static String parseConceptCode(String rawCode) {
        if (rawCode != null) {
            rawCode = rawCode.replace("\"", "");
            rawCode = rawCode.replace(" ", "_");
            rawCode = rawCode.replace(",", "_");
            rawCode = rawCode.replace("__", "_");
            rawCode = rawCode.replace("/", "-");
            rawCode = rawCode.replace("&", "and");
        }
        return rawCode;
    }

    /**
     *
     * @return  all loaded concepts
     */
    HashMap<String, CanmedConcept> getConcepts() {
        return concepts;
    }

    /**
     * Send the NDC parser to be processed into concepts
     *
     * @param parser
     */
    private void createNDCconcepts(gov.nih.nci.evs.canmed.CanmedCsvParser parser) {
        List<String> header = parser.getHeader();
        for (List<String> dataLine : parser.getLineByLineData()) {
            HashMap<String, String> propertyValueList = getPropertyValues(header, dataLine);

            try {
                CanmedConcept concept = new NdcConcept(propertyValueList);
                createMajorHierarchy(concept);
                concepts.put(concept.getCode(), concept);
            } catch (Exception e) {
                System.out.println("No code found for NDC data line: " + dataLine);
            }
        }
    }

    /**
     * Send the HCPCS parser to be processed into concepts
     *
     * @param parser
     */
    private void createHCPCSconcepts(gov.nih.nci.evs.canmed.CanmedCsvParser parser) {

        List<String> header = parser.getHeader();
        for (List<String> dataLine : parser.getLineByLineData()) {
            HashMap<String, String> propertyValueList = getPropertyValues(header, dataLine);


            try {
                CanmedConcept concept = new HcpcsConcept(propertyValueList);
                createMajorHierarchy(concept);
                concepts.put(concept.getCode(), concept);
            } catch (Exception e) {
                System.out.println("No code found for HCPCS data line: " + dataLine);
            }
        }
    }

    /**
     * Matches the file header to the csv values for each line of data
     *
     * @param header
     * @param dataLine
     * @return
     */
    private HashMap<String, String> getPropertyValues(List<String> header, List<String> dataLine) {

        HashMap<String, String> propertyValueList = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            if (i < dataLine.size()) {
                propertyValueList.put(header.get(i), dataLine.get(i));
            } else {
                propertyValueList.put(header.get(i), null);
            }


        }
        return propertyValueList;
    }

    /**
     * Gather the major, minor and seer columns from a class
     * Turn those into a categorization hierarchy
     *
     * @param concept
     * @throws Exception
     */
    private void createMajorHierarchy(CanmedConcept concept) throws Exception {
        try {
            if ((concept.getMinorClass().length() > 0) && concept.getMajorClass().length() > 0) {
                findParents(concept.getMinorClass(), concept.getMajorClass(), concept.getNode());
            }
            if (concept.getMajorClass().length() > 0 && concept.getSeerClass().length() > 0) {
                findParents(concept.getMajorClass(), concept.getSeerClass(), concept.getNode());
            }
            if (concept.getSeerClass().length() > 0 && concept.getNode().length() > 0) {
                findParents(concept.getSeerClass(), concept.getNode(), concept.getNode());
            }
        } catch (Exception e) {
            System.out.println("Issue creating hierarchy using " + concept.getCode());
            throw e;
        }
    }

    /**
     * take the parent of a given concept and create a new concept for that parent, if it doesn't already exist
     * @param child
     * @param parent
     * @param node
     */
    private void findParents(String child, String parent, String node) {
        if (!concepts.containsKey(child)) {
            CanmedConcept concept = new CanmedConcept(child, child, node);
            concept.addParent(parent);
            concept.addProperty("description", "Hierarchy concept");
            concept.addProperty("Preferred_Name", child);
            concepts.put(concept.getCode(), concept);
        } else {
            CanmedConcept concept = concepts.get(child);
            if (!concept.getParents().containsKey(parent)) {
                concept.addParent(parent);
                concepts.put(concept.getCode(), concept);
            }
        }
    }

}
