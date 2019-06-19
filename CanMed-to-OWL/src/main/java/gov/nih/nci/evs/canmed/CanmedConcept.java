package gov.nih.nci.evs.canmed;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class CanmedConcept {

    private HashMap<String, String> parents = new HashMap<String, String>();

    private String code = null;
    private String name = null;
    private String major;
    private String majorClass;
    private String minor;
    private String minorClass;
    private String seer;
    private String seerClass;
    private String node;
    private HashMap<String, String> properties = new HashMap<String, String>();
    private List<String> synonyms = new ArrayList<String>();

    /**
     * Create a Canmed Concept by passing in a full property list
     *
     * @param propertyValueList
     */
    public CanmedConcept(HashMap<String, String> propertyValueList) {

        try {

            name = propertyValueList.get("Generic Name");

            setProperties(propertyValueList);


        } catch (Exception e) {
            System.out.println("Problem parsing " + code);
        }
    }

    /**
     * Create a Canmed Concept by passing in the minimal information
     * This is mainly used to generate concepts for the major, minor and seer data
     *
     * @param code
     * @param name
     * @param node
     */
    public CanmedConcept(String code, String name, String node) {

        this.name = name;
        this.node = node;
        this.code = gov.nih.nci.evs.canmed.CanmedOntology.parseConceptCode(tag(code));

    }

    /**
     * Convert a given string to camelCase
     *
     * @param rawString
     * @return
     */
    public static String toCamel(String rawString) {
        return rawString.substring(0, 1).toUpperCase() +
                rawString.substring(1).toLowerCase();
    }

    /**
     * trim starting or ending slashes or commas from property values
     *
     * @param propValue
     * @return
     */
    public static String validateProperty(String propValue) {
        //trim empty space
        propValue = propValue.trim();
        //remove unnecessary quotes
        if (propValue.startsWith("\"")) {
            propValue = propValue.substring(1);
        }
        if (propValue.endsWith("\"")) {
            propValue = propValue.substring(0, propValue.length() - 1);
        }
        if (propValue.endsWith(",")) {
            propValue = propValue.substring(0, propValue.length() - 1);
        }

        return propValue;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public String getMajorClass() {
        return majorClass;
    }

    public void setMajorClass(String majorClass) {
        this.majorClass = majorClass;
    }

    public String getMinorClass() {
        return minorClass;
    }

    public void setMinorClass(String minorClass) {
        this.minorClass = minorClass;
    }

    public String getSeerClass() {
        return seerClass;
    }

    public void setSeerClass(String seerClass) {
        this.seerClass = seerClass;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public String getSeer() {
        return seer;
    }
//	public HashMap<String,String>  getGrandParents() { return grandParents;}

    public void setSeer(String seer) {
        this.seer = seer;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public HashMap<String, String> getParents() {
        return parents;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String inCode) {
        code = inCode;
    }


    public HashMap<String, String> getProperties() {

        return this.properties;
    }

    /**
     * Take the raw propertyValueList, examine and "clean" each value and add it to the properties map
     *
     * @param propertyValueList
     * @throws Exception
     */
    private void setProperties(HashMap<String, String> propertyValueList) throws Exception {
        //some of the column names won't work as property IDs
        for (String propName : propertyValueList.keySet()) {
            String propertyCode = parsePropertyId(propName);
            String propValue = propertyValueList.get(propName);
            if (!(propValue == null) && propValue.length() > 0)
                propValue = validateProperty(propValue);
            properties.put(propertyCode, propValue);
        }
    }

    /**
     * Remove a property by property Name
     *
     * @param propertyName
     */
    public void removeProperty(String propertyName) {
        properties.remove(propertyName);
    }

    /**
     * Return the name of this concept.  Usually generated from the Generic Name
     * Except for the major, minor and seer concepts
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Add a parent code and parent concept to a map of parents for this concept
     *
     * @param parent
     */
    public void addParent(String parent) {
        String parentCode = CanmedOntology.parseConceptCode(tag(parent));
        this.parents.put(parentCode, parent);

    }

    /**
     * Check the property codes and make sure they can be valid URI fragments
     * @param rawId
     * @return
     */
    private String parsePropertyId(String rawId) {
        String id = rawId.replace("(", "");
        id = id.replace(")", "");
        id = id.replace(" ", "_");
        id = id.replace("\\", "-");
        return id;
    }

    /**
     * Add a property by passing in a code and value
     * If that code already exists, this will overwrite
     *
     * @param code
     * @param value
     */
    public void addProperty(String code, String value) {
        if (!this.properties.containsKey(code)) {
            this.properties.put(code, value);
        }
    }

    /**
     * Get a property by property code
     * @param propertyCode
     * @return
     */
    public String getProperty(String propertyCode) {
        return properties.get(propertyCode);
    }

    /**
     * Examine the major, minor and seer properties to determine the immediate parent of this class
     *
     */
    public void setParent() {
        if (this.getMinorClass().length() > 0) {
            addParent(this.getMinorClass());
        } else if (this.getMajorClass().length() > 0) {
            addParent(this.getMajorClass());
        } else if (this.getSeerClass().length() > 0) {
            addParent(this.getSeerClass());
        } else {
            addParent(this.getNode());
        }
    }

    /**
     * Add a suffix to the string that indicates whether the concept is from NDC or HCPCS
     * @param raw
     * @return
     */
    private String tag(String raw) {
        if (!raw.equals(node)) {
            return raw + " " + node;
        }
        return raw;
    }

}
