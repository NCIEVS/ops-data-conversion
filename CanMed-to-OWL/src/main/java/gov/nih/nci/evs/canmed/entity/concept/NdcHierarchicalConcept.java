package gov.nih.nci.evs.canmed.entity.concept;

public class NdcHierarchicalConcept extends HierarchicalConcept {
    String vocabularyType="NDC";

    public NdcHierarchicalConcept(String code, String name) {
        super(code, name);
    }


    public String getVocabularyType() {
        return this.vocabularyType;
    }

}
