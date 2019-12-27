package gov.nih.nci.evs.canmed.entity.concept;

public class HcpcsHierarchicalConcept extends HierarchicalConcept {
    String vocabularyType="HCPCS";

    public HcpcsHierarchicalConcept(String code, String name) {
        super(code, name);
    }

    public String getVocabularyType(){
        return this.vocabularyType;
    }
}
