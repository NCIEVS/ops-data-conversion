package gov.nih.nci.evs.canmed.entity.concept;

import java.util.Collection;
import java.util.HashSet;

public class NdcProduct extends gov.nih.nci.evs.canmed.entity.concept.NdcHierarchicalConcept {
    HashSet<String> brandNames;
    String genericName;
    String ndcProductCode;
    HashSet<String> routesOfAdministration;

    public NdcProduct(String code, String name) {
        super(code, name);
    }

    public String getGenericName(){ return genericName;}

    public void setGenericName(String genericName){
        this.genericName = genericName;
    }

    public HashSet<String> getBrandNames() {
        return brandNames;
    }

    public void setBrandNames(HashSet<String> brandNames) {
        this.brandNames = brandNames;
    }

    public void addBrandName(String brandName){
        brandNames.add(brandName);
    }

    public String getNdcProductCode() {
        return ndcProductCode;
    }

    public void setNdcProductCode(String ndcProductCode) {
        this.ndcProductCode = ndcProductCode;
    }

    public HashSet<String> getRoutesOfAdministration() {
        return routesOfAdministration;
    }

    public void setRoutesOfAdministration(HashSet<String> routeOfAdministration) {
        this.routesOfAdministration = routeOfAdministration;
    }

    public void addRouteOfAdministration(String routeOfAdministration){
        this.routesOfAdministration.add(routeOfAdministration);
    }


}
