package gov.nih.nci.evs.canmed.entity;

import com.sun.javafx.sg.prism.NGDefaultCamera;
import gov.nih.nci.evs.canmed.CanmedCsvParser;
import gov.nih.nci.evs.canmed.CanmedToOwl;
import gov.nih.nci.evs.canmed.Constants;
import gov.nih.nci.evs.canmed.entity.concept.HcpcsHierarchicalConcept;
import gov.nih.nci.evs.canmed.entity.concept.HcpcsPackage;
import gov.nih.nci.evs.canmed.entity.concept.HierarchicalConcept;
import gov.nih.nci.evs.canmed.entity.concept.NdcHierarchicalConcept;
import gov.nih.nci.evs.canmed.entity.concept.NdcPackage;
import gov.nih.nci.evs.canmed.entity.concept.NdcProduct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Ontology {

    private HashMap<String, HierarchicalConcept> concepts = new HashMap<String, HierarchicalConcept>();

    public Ontology(CanmedCsvParser NDCparser, gov.nih.nci.evs.canmed.CanmedCsvParser HCPCSparser) {
        createNDCconcepts(NDCparser);
        createHCPCSconcepts(HCPCSparser);
    }

    public HierarchicalConcept getConcept(String code){
        return concepts.get(code);
    }

    private void createNDCconcepts(gov.nih.nci.evs.canmed.CanmedCsvParser parser) {
        HierarchicalConcept root = new NdcHierarchicalConcept("NDC","NDC");
        List<String> header = parser.getHeader();
        for (List<String> dataLine : parser.getLineByLineData()) {
            HashMap<String, String> propertyValueList = getPropertyValues(header, dataLine);
            HierarchicalConcept SEERconcept = null;
            HierarchicalConcept MajorConcept = null;
            HierarchicalConcept MinorConcept = null;
            HierarchicalConcept DrugNameConcept;
            NdcProduct ProductConcept;
            NdcPackage PackageConcept;
            try {
                //Create SEER Concept
                String SEER = propertyValueList.get(Constants.NDC_SEER);
                String SEERcode = "NDC_" + CanmedToOwl.parseConceptCode(SEER);
                if (concepts.containsKey(SEERcode)) {
                    SEERconcept = concepts.get(SEERcode);
                } else {
                    SEERconcept = new NdcHierarchicalConcept(SEERcode, SEER);
                }
                SEERconcept.addParentConcept(root);

                //Create Major Class Concept
                String major = propertyValueList.get(Constants.NDC_MAJOR_CLASS);
                String majorCode = "NDC_" + CanmedToOwl.parseConceptCode(major);
                if(major.length()>0) {

                        if (concepts.containsKey(majorCode)) {
                            MajorConcept = concepts.get(majorCode);
                        } else {
                            MajorConcept = new NdcHierarchicalConcept(majorCode, major);

                        }
                        if (SEERconcept != null) {
                            MajorConcept.addParentConcept(SEERconcept);
                            SEERconcept.addChildConcept(MajorConcept);
                        }

                }
                //Create Minor Class Concept
                String minor = propertyValueList.get(Constants.NDC_MINOR_CLASS);
                String minorCode = "NDC_" + CanmedToOwl.parseConceptCode(minor);
                if(minor.length()>0) {
                        if (concepts.containsKey(minorCode)) {
                            MinorConcept = concepts.get(minorCode);
                        } else {
                            MinorConcept = new NdcHierarchicalConcept(minorCode, minor);

                        }
                        if (MajorConcept != null) {
                            MinorConcept.addParentConcept(MajorConcept);
                            MajorConcept.addChildConcept(MinorConcept);
                        } else if (SEERconcept != null) {
                            MinorConcept.addParentConcept(SEERconcept);
                            SEERconcept.addChildConcept(MinorConcept);
                        }
                }

                //Create Drug Name Concept
                String drugName = propertyValueList.get(Constants.NDC_DRUG_NAME);
                String drugNameCode = "NDC_" + CanmedToOwl.parseConceptCode(drugName);
                if (concepts.containsKey(drugNameCode)) {
                    DrugNameConcept = concepts.get(drugNameCode);
                } else {
                    DrugNameConcept = new NdcHierarchicalConcept(drugNameCode, drugName);

                }
                if (MinorConcept != null) {
                    DrugNameConcept.addParentConcept(MinorConcept);
                    MinorConcept.addChildConcept(DrugNameConcept);
                } else if (MajorConcept != null) {
                    DrugNameConcept.addParentConcept(MajorConcept);
                    MajorConcept.addChildConcept(DrugNameConcept);
                } else if (SEERconcept !=null){
                    DrugNameConcept.addParentConcept(SEERconcept);
                    SEERconcept.addChildConcept(DrugNameConcept);
                }


                //Create Product Concept
                String productCode = "NDC_" + CanmedToOwl.parseConceptCode(propertyValueList.get(Constants.NDC_PRODUCT));
                String routeString = propertyValueList.get(Constants.NDC_ROUTE_OF_ADMINISTRATION);
                HashSet<String> routes = new HashSet<String>(parser.tokenizeString(routeString));
                String brandNameString = propertyValueList.get(Constants.NDC_BRAND_NAME);
                HashSet<String> brandNames = new HashSet<String>(parser.tokenizeString(brandNameString));
                if (concepts.containsKey(productCode)) {
                    ProductConcept = (NdcProduct) concepts.get(productCode);
                    for (String brandName : brandNames) {
                        ProductConcept.addBrandName(brandName);
                    }
                    for (String route : routes) {
                        ProductConcept.addRouteOfAdministration(route);
                    }
                } else {
                    ProductConcept = new NdcProduct(productCode, drugName);
                    ProductConcept.setBrandNames(brandNames);
                    ProductConcept.setRoutesOfAdministration(routes);
                    ProductConcept.setNdcProductCode(propertyValueList.get(Constants.NDC_PRODUCT));
                }
                ProductConcept.addParentConcept(DrugNameConcept);
                DrugNameConcept.addChildConcept(ProductConcept);


                //Create Package Concept
                String packageCode = "NDC_" + CanmedToOwl.parseConceptCode(propertyValueList.get(Constants.NDC_PACKAGE));
                String strength = propertyValueList.get(Constants.NDC_STRENGTH);
                String status = propertyValueList.get(Constants.NDC_STATUS);
                String effectiveDate = propertyValueList.get(Constants.NDC_EFFECTIVE_DATE);
                String discontinue = propertyValueList.get(Constants.NDC_DISCONTINUATION_DATE);
                if (concepts.containsKey(packageCode)) {
                    System.out.println("Duplicate package found " + packageCode);
                    PackageConcept = (NdcPackage) concepts.get(packageCode);
                    for (String brandName : brandNames) {
                        ProductConcept.addBrandName(brandName);
                    }
                    for (String route : routes) {
                        ProductConcept.addRouteOfAdministration(route);
                    }
                } else {
                    PackageConcept = new NdcPackage(packageCode, drugName);
                    PackageConcept.setRoutesOfAdministration(routes);
                    PackageConcept.setBrandNames(brandNames);
                    PackageConcept.setNdcPackageCode(propertyValueList.get(Constants.NDC_PACKAGE));
                }
                if (strength.length() > 0) {
                    PackageConcept.setStrength(strength);
                }
                if (status.length() > 0){
                    PackageConcept.setStatus(status);
                }
                if(effectiveDate.length()>0) {
                    PackageConcept.setPackageEffectiveDate(effectiveDate);
                }
                if(discontinue.length()>0) {
                    PackageConcept.setPackageDiscontinueDate(discontinue);
                }
                PackageConcept.setNdcProductCode(propertyValueList.get(Constants.NDC_PRODUCT));
                PackageConcept.addParentConcept(ProductConcept);
                ProductConcept.addChildConcept(PackageConcept);


                if (SEERconcept != null) {
                    concepts.put(SEERcode, SEERconcept);
                }
                if (MajorConcept != null) {
                    concepts.put(majorCode, MajorConcept);
                }
                if (MinorConcept != null) {
                    concepts.put(minorCode, MinorConcept);
                }
                if (DrugNameConcept != null) {
                    concepts.put(drugNameCode, DrugNameConcept);
                }
                if (ProductConcept != null) {
                    concepts.put(productCode, ProductConcept);
                }
                if (PackageConcept != null) {
                    concepts.put(packageCode, PackageConcept);
                }

            } catch (Exception e) {
                System.out.println("No code found for NDC data line: " + dataLine);
            }
        }
    }

    private void createHCPCSconcepts(CanmedCsvParser parser){
        HierarchicalConcept root = new HcpcsHierarchicalConcept("HCPCS", "HCPCS");
        List<String> header = parser.getHeader();
        for (List<String> dataLine : parser.getLineByLineData()) {
            HashMap<String, String> propertyValueList = getPropertyValues(header, dataLine);
            HierarchicalConcept SEERconcept = null;
            HierarchicalConcept MajorConcept = null;
            HierarchicalConcept MinorConcept = null;
            HierarchicalConcept DrugNameConcept;
            HcpcsPackage HcpcsPackageConcept;
            try {
        //Create SEER Concept
                String SEER = propertyValueList.get(Constants.HCPCS_SEER);
                String SEERcode = "HCPCS_"+ CanmedToOwl.parseConceptCode(SEER);
                if(SEERcode.equals("HCPCS_")){
                    String debug="Stop";
                }
                if(concepts.containsKey(SEERcode)){
                    SEERconcept = concepts.get(SEERcode);
                } else {
                    SEERconcept = new HcpcsHierarchicalConcept(SEERcode, SEER);
                }
                SEERconcept.addParentConcept(root);

        //Create Major Class Concept
                String major = propertyValueList.get(Constants.HCPCS_MAJOR_CLASS);
                String majorCode = "HCPCS_"+ CanmedToOwl.parseConceptCode(major);
                if(majorCode.equals("HCPCS_")){
                    String debug = "stop";
                }
                if (majorCode!=null && majorCode.length()>6){
                    if(concepts.containsKey(majorCode)){
                        MajorConcept = concepts.get(majorCode);
                    } else {
                        MajorConcept = new HcpcsHierarchicalConcept(majorCode, major);

                    }
                    if(SEERconcept != null){
                        MajorConcept.addParentConcept(SEERconcept);
                        SEERconcept.addChildConcept(MajorConcept);
                    }
                }

        //Create Minor Class Concept
                String minor = propertyValueList.get(Constants.HCPCS_MINOR_CLASS);
                String minorCode = "HCPCS_"+ CanmedToOwl.parseConceptCode(minor);
                if (minorCode.equals("HCPCS_")){
                    String debug = "stop";
                }
                if(minorCode!=null && minorCode.length()>6){
                    if(concepts.containsKey(minorCode)){
                        MinorConcept = concepts.get(minorCode);
                    } else {
                        MinorConcept = new HcpcsHierarchicalConcept(minorCode, minor);

                    }
                    if(MajorConcept!=null){
                        MinorConcept.addParentConcept(MajorConcept);
                        MajorConcept.addChildConcept(MinorConcept);
                    } else if (SEERconcept!=null){
                        MinorConcept.addParentConcept(SEERconcept);
                        SEERconcept.addChildConcept(MinorConcept);
                    }
                }

        //Create Drug Name Concept
                String drugName = propertyValueList.get(Constants.HCPCS_DRUG_NAME);
                String drugNameCode = "HCPCS_"+ CanmedToOwl.parseConceptCode(drugName);
                if(drugNameCode.equals("HCPCS_")){
                    String debug = "stop";
                }
                if(concepts.containsKey(drugNameCode)){
                    DrugNameConcept = concepts.get(drugNameCode);
                } else {
                    DrugNameConcept = new HcpcsHierarchicalConcept(drugNameCode, drugName);

                }
                if(MinorConcept!=null){
                    DrugNameConcept.addParentConcept(MinorConcept);
                    MinorConcept.addChildConcept(DrugNameConcept);
                } else if (MajorConcept!=null){
                    DrugNameConcept.addParentConcept(MajorConcept);
                    MajorConcept.addChildConcept(DrugNameConcept);
                } else if (SEERconcept!=null){
                    DrugNameConcept.addParentConcept(SEERconcept);
                    SEERconcept.addChildConcept(DrugNameConcept);
                }

        //Create Package Concept
                String packageCode = generateHCPCSpackageCode(propertyValueList);
               if(packageCode.toUpperCase().startsWith("NOT")|| packageCode.endsWith("_")){
                    String debug = "stop";
                }
                String brandNameString = propertyValueList.get(Constants.HCPCS_BRAND_NAME);
                HashSet<String> brandNames = new HashSet<String>(parser.tokenizeString(brandNameString));
                if(concepts.containsKey(packageCode)){
                    System.out.println("Duplicated HCPCS package "+ packageCode);
                    HcpcsPackageConcept = (HcpcsPackage) concepts.get(packageCode);
                    for(String brandName:brandNames){
                        HcpcsPackageConcept.addBrandName(brandName);
                    }
                } else {
                    HcpcsPackageConcept = new HcpcsPackage(packageCode, drugName);
                    HcpcsPackageConcept.setBrandNames(brandNames);
                }
                String strength = propertyValueList.get(Constants.HCPCS_STRENGTH);
                String oral = propertyValueList.get(Constants.HCPCS_ORAL);
                String status = propertyValueList.get(Constants.HCPCS_STATUS);
                String fdaApproval = propertyValueList.get(Constants.HCPCS_FDA_APPROVAL_YEAR);
                String fdaDiscontinue = propertyValueList.get(Constants.HCPCS_FDA_DISCONTINUATION_YEAR);
                String cmsEffective = propertyValueList.get(Constants.HCPCS_CMS_EFFECTIVE_DATE);
                String cmsDiscontinue = propertyValueList.get(Constants.HCPCS_CMS_DISCONTINUATION_DATE);
                String hcpcsCode = propertyValueList.get(Constants.HCPCS_CODE);
                if(strength.length()>0) {
                    HcpcsPackageConcept.setStrength(strength);
                }
                if(oral.length()>0) {
                    HcpcsPackageConcept.setOral(oral);
                }
                if(status.length()>0) {
                    HcpcsPackageConcept.setStatus(status);
                }
                if(fdaApproval.length()>0) {
                    HcpcsPackageConcept.setFdaApproval(fdaApproval);
                }
                if(fdaDiscontinue.length()>0) {
                    HcpcsPackageConcept.setFdaDiscontinue(fdaDiscontinue);
                }
                if(cmsEffective.length()>0) {
                    HcpcsPackageConcept.setCmsEffective(cmsEffective);
                }
                if(cmsDiscontinue.length()>0) {
                    HcpcsPackageConcept.setCmsDiscontinue(cmsDiscontinue);
                }
                if(hcpcsCode.length()>2) {
                    HcpcsPackageConcept.setHcpcsCode(hcpcsCode);
                }
                HcpcsPackageConcept.addParentConcept(DrugNameConcept);
                DrugNameConcept.addChildConcept(HcpcsPackageConcept);

                if (SEERconcept != null) {
                    concepts.put(SEERcode, SEERconcept);
                }
                if (MajorConcept != null) {
                    concepts.put(majorCode, MajorConcept);
                }
                if (MinorConcept != null) {
                    concepts.put(minorCode, MinorConcept);
                }
                if (DrugNameConcept != null) {
                    concepts.put(drugNameCode, DrugNameConcept);
                }
                if (HcpcsPackageConcept != null) {
                    concepts.put(packageCode, HcpcsPackageConcept);
                }

            } catch (Exception e) {
                System.out.println("No code found for NDC data line: " + dataLine);
            }
        }
    }

    private String generateHCPCSpackageCode(HashMap<String,String> propertyValues){
        String packageCode = "HCPCS_";

        String hcpcsCode = propertyValues.get(Constants.HCPCS_CODE);
        //multiple rows are labeled "Not yet assigned".
        if(hcpcsCode.toUpperCase().startsWith("NOT")){
            hcpcsCode="";
        }
        //The default code is "NA". We only want real codes.
        if(hcpcsCode.length()>2){
            return hcpcsCode;
        }
        String code = packageCode + propertyValues.get(Constants.HCPCS_DRUG_NAME);
        if(propertyValues.get(Constants.HCPCS_STRENGTH) !=null && propertyValues.get(Constants.HCPCS_STRENGTH).length()>0 ){
            code = code +"_"+ propertyValues.get(Constants.HCPCS_STRENGTH);
        }
        return CanmedToOwl.parseConceptCode(code);
    }

    public HashMap<String, HierarchicalConcept> getConcepts() {
        return this.concepts;
    }

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
}
