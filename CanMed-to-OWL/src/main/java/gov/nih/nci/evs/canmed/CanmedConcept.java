package gov.nih.nci.evs.canmed;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CanmedConcept {
	
	private List<String> parents = new ArrayList<String>();
	private String code = null;
	private String name = null;
	private HashMap<String, String> properties = new HashMap<String,String>();
	private List<String> adminRoutes = new ArrayList<String>();

	public List<String> getParents() {
		return parents;
	}


	public String getCode() {
		return code;
	}


	public String getName() {
		return name;
	}
	
	public List<String> getAdminRoutes(){
		return adminRoutes;
	}

	public CanmedConcept(HashMap<String, String> propertyValueList) {
		// TODO Auto-generated constructor stub
		try {
		code = propertyValueList.get("NDC-11 (Package)");
		if(code==null || code.length()<1 || code.equals("")) {
			System.out.println("Code is empty");
		}
		name = propertyValueList.get("Generic Name");
		setParent(propertyValueList);
		setProperties(propertyValueList);
		
		parseAdministrationRoute();
		} catch (Exception e) {
			System.out.println("Problem parsing "+ code);
		}
	}
	
	public CanmedConcept(String code, String name, String parent) {
		this.code=CanmedOntology.parseConceptCode(code);
		this.name= name;
		parent = CanmedOntology.parseConceptCode(parent);
		if(parent!=null && parent.length()>0) {
		this.parents.add(parent);
		}
	}
	
	private void setProperties(HashMap<String, String> propertyValueList) throws Exception{
		//some of the column names won't work as property IDs
		for(String propName:propertyValueList.keySet()) {
			String propertyCode = parsePropertyId(propName);
			String propValue = propertyValueList.get(propName);
			if(!(propValue==null) && propValue.length()>0)
				propValue = validateProperty(propValue);
			properties.put(propertyCode, propValue);
		}
	}
	
	private String parsePropertyId(String rawId) {
		String id = rawId.replace("(", "");
		id = id.replace(")","");
		id = id.replace(" ", "_");
		return id;
	}
	
	private void parseAdministrationRoute(){
		String rawAdminRoute = properties.get("Administration_Route");
		properties.remove("Administration_Route");
		String[] routes = rawAdminRoute.split(",");
		if(routes.length>1) {
			
			for(int i=0; i<routes.length; i++) {
				String routeValue = routes[i];
//				properties.put("Administration_Route", toCamel(routeValue));
				adminRoutes.add(formatAdminRoute(routeValue));
			}
		} else {
			if(rawAdminRoute.length()>0) {
			adminRoutes.add(formatAdminRoute(rawAdminRoute));
			}
		}
	}
	
	private String formatAdminRoute(String route) {
		route = route.trim();
		route = route.toLowerCase();
		route = toCamel(route);
		route = validateProperty(route);
		return route;
	}

	
	private String toCamel(String rawString) {
	    return rawString.substring(0, 1).toUpperCase() +
	    		rawString.substring(1).toLowerCase();
	}
	
	private void setParent(HashMap<String, String>propertyValueList) {
		//check minor category
		//if empty, check major category

		if (propertyValueList.get("Minor Class").length()>0) {
			parents.add(CanmedOntology.parseConceptCode(propertyValueList.get("Minor Class")));
		} else if (propertyValueList.get("Major Class").length()>0){
			parents.add(CanmedOntology.parseConceptCode(propertyValueList.get("Major Class")));
		} else {
			parents.add(CanmedOntology.parseConceptCode(propertyValueList.get("SEER*Rx_Category")));
		}
	}
	
	public String getProperty(String propertyName) {
		return properties.get(propertyName);
	}
	
	public HashMap<String,String> getProperties(){
		return this.properties;
	}
	
	private String validateProperty(String propValue) {
		//trim empty space
		propValue = propValue.trim();
		//remove unnecessary quotes
		if(propValue.startsWith("\"")) {
			propValue = propValue.substring(1);
		}
		if(propValue.endsWith("\"")) {
			propValue = propValue.substring(0,propValue.length()-1);
		}
		if(propValue.endsWith(",")) {
			propValue = propValue.substring(0,propValue.length()-1); 
		}
		
		return propValue;
	}
	

}
