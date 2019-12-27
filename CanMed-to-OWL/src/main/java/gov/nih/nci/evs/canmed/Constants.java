package gov.nih.nci.evs.canmed;

import com.sun.tools.javac.code.Attribute;

/**
 * This is where to map the downloaded data files to the hierarchy.
 * The name of the constant is the hierarchy tag, the value is the column name in the downloaded data
 */
public class Constants {
    //NDC Constants
    public static final String NDC_PACKAGE = "NDC-11 (Package)";
    public static final String NDC_PRODUCT = "NDC-9 (Product)";
    public static final String NDC_DRUG_NAME = "Generic Name";
    public static final String NDC_BRAND_NAME = "Brand Name";
    public static final String NDC_STRENGTH = "Strength";
    public static final String NDC_SEER = "SEER*Rx Category";
    public static final String NDC_MAJOR_CLASS = "Major Class";
    public static final String NDC_MINOR_CLASS = "Minor Class";
    public static final String NDC_ROUTE_OF_ADMINISTRATION = "Administration Route";
    public static final String NDC_EFFECTIVE_DATE = "Package Effective Date";
    public static final String NDC_DISCONTINUATION_DATE = "Package Discontinuation Date";
    public static final String NDC_STATUS = "Status";


    //HCPCS Constants
    public static final String HCPCS_CODE = "HCPCS";
    public static final String HCPCS_DRUG_NAME = "Generic Name";
    public static final String HCPCS_BRAND_NAME = "Brand Name";
    public static final String HCPCS_STRENGTH = "Strength";
    public static final String HCPCS_SEER = "SEER*Rx Category";
    public static final String HCPCS_MAJOR_CLASS = "Major Drug Class";
    public static final String HCPCS_MINOR_CLASS = "Minor Drug Class";
    public static final String HCPCS_ORAL = "Oral (Y/N)";
    public static final String HCPCS_FDA_APPROVAL_YEAR = "FDA Approval Year";
    public static final String HCPCS_FDA_DISCONTINUATION_YEAR = "FDA Discontinuation Year";
    public static final String HCPCS_CMS_EFFECTIVE_DATE = "CMS Effective Date";
    public static final String HCPCS_CMS_DISCONTINUATION_DATE = "CMS Discontinuation Date";
    public static final String HCPCS_STATUS = "Status";
}
