package gov.nih.nci.evs.canmed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Extends CanmedConcept to deal with NDC specific information
 */
class NdcConcept extends gov.nih.nci.evs.canmed.CanmedConcept {
    public static String ndc_minor = "Minor Class";
    public static String ndc_major = "Major Class";
    public static String ndc_seer = "SEER*Rx_Category";
    public static String ndc_node = "NDC";
    private List<String> adminRoutes = new ArrayList<String>();


    public NdcConcept(HashMap<String, String> propertyValueList) throws Exception {
        super(propertyValueList);
        String code = propertyValueList.get("NDC-11 (Package)");

        setMinor(ndc_minor);
        setMajor(ndc_major);
        setSeer(ndc_seer);
        setNode(ndc_node);
        setMinorClass(propertyValueList.get(this.getMinor()));
        setMajorClass(propertyValueList.get(this.getMajor()));
        setSeerClass(propertyValueList.get(this.getSeer()));


        if (code == null || code.length() < 1) {
            System.out.println("Code is empty");
            throw new Exception("Code is empty");
        }


        setCode(code);
        setParent();
        parseAdministrationRoute();
        buildSynonyms();
    }

    /**
     * NDC will sometimes put multiple admin routes in a single data field
     * These need to be separated into individual properties
     */
    private void parseAdministrationRoute() {
        String rawAdminRoute = getProperties().get("Administration_Route");
        removeProperty("Administration_Route");
        String[] routes = rawAdminRoute.split(",");
        if (routes.length > 1) {

            for (String routeValue : routes) {
                //				properties.put("Administration_Route", toCamel(routeValue));
                adminRoutes.add(formatAdminRoute(routeValue));
            }
        } else {
            if (rawAdminRoute.length() > 0) {
                adminRoutes.add(formatAdminRoute(rawAdminRoute));
            }
        }
    }

    /**
     * Return the set of admin routes as a list
     *
     * @return
     */
    public List<String> getAdminRoutes() {
        return adminRoutes;
    }

    /**
     * Clean up the admin route data to make it more consistant
     * trim leading and trailing spaces and commas
     *
     * @param route
     * @return
     */
    private String formatAdminRoute(String route) {
        route = route.trim();
        route = route.toLowerCase();
        route = toCamel(route);
        route = validateProperty(route);
        return route;
    }

    /**
     * Manually build several synonyms for review by CanMED
     * Hardcoded for now, can be externalized when they decide which ones they want
     */
    private void buildSynonyms() {
        List<String> syns = new ArrayList<String>();
        String syn1 = this.getName() + " " + this.getProperty("Strength");
        String syn2 = this.getName() + " (" + this.getProperty("NDC-9_Product") + ")";
        String syn3 = this.getName() + " (" + this.getProperty("NDC-11_Package") + ")";

        addProperty("Preferred_Name", this.getName() + " " + this.getProperty("Strength") + " (" + this.getProperty("NDC-11_Package") + ")");
        syns.add(syn1);
        syns.add(syn2);
        syns.add(syn3);

        this.setSynonyms(syns);

    }
}
