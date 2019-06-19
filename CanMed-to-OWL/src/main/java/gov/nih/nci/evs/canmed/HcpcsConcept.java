package gov.nih.nci.evs.canmed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Extends CanmedConcept to deal with HCPCS specific information
 */
class HcpcsConcept extends gov.nih.nci.evs.canmed.CanmedConcept {
    public static String hcpcs_minor = "Minor Drug Class";
    public static String hcpcs_major = "Major Drug Class";
    public static String hcpcs_seer = "SEER*Rx Category";
    public static String hcpcs_node = "HCPCS";

    public HcpcsConcept(HashMap<String, String> propertyValueList) throws Exception {
        super(propertyValueList);
        String code = propertyValueList.get("HCPCS");

        setMinor(hcpcs_minor);
        setMajor(hcpcs_major);
        setSeer(hcpcs_seer);
        setNode(hcpcs_node);
        setMinorClass(propertyValueList.get(this.getMinor()));
        setMajorClass(propertyValueList.get(this.getMajor()));
        setSeerClass(propertyValueList.get(this.getSeer()));


        if (code == null || code.length() < 1) {
            System.out.println("Code is empty");
            throw new Exception("Code is empty");
        }
        if (code.equals("NA") || code.startsWith("Not")) {
            System.out.println("Code not assigned");
            throw new Exception("Code not assigned");
        }
        setCode(code);
        setParent();
        buildSynonyms();
    }

    /**
     * Manually build several synonyms for review by CanMED
     * Hardcoded for now, can be externalized when they decide which ones they want
     */
    private void buildSynonyms() {
        List<String> syns = new ArrayList<String>();
        String syn1 = this.getName() + " " + this.getProperty("Strength");
        String syn2 = this.getName() + " (" + this.getProperty("HCPCS") + ")";
        addProperty("Preferred_Name", this.getName() + " " + this.getProperty("Strength") + " (" + this.getProperty("HCPCS") + ")");
        syns.add(syn1);
        syns.add(syn2);
        this.setSynonyms(syns);

    }
}
