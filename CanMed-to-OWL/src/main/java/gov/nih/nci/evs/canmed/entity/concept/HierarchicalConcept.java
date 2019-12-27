package gov.nih.nci.evs.canmed.entity.concept;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public abstract class HierarchicalConcept  {
    HashSet<HierarchicalConcept> childConcepts = new HashSet<HierarchicalConcept>();
    HashSet<HierarchicalConcept> parentConcepts= new HashSet<HierarchicalConcept>();
    String code;
    String name;

    public HierarchicalConcept(String code, String name){
        this.code = code;
        this.name = name;
    }


    public HashSet<HierarchicalConcept> getChildConcepts() {
        return this.childConcepts;
    }


    public void setChildConcepts(HashSet<HierarchicalConcept> concepts) {
        this.childConcepts=concepts;
    }


    public void addChildConcept(HierarchicalConcept concept) {
        this.childConcepts.add(concept);
    }


    public HashSet<HierarchicalConcept> getParentConcepts() {
        return parentConcepts;
    }


    public void setParentConcepts(HashSet<HierarchicalConcept> parentConcepts) {
        this.parentConcepts = parentConcepts;
    }


    public void addParentConcept(HierarchicalConcept concept) {
        this.parentConcepts.add(concept);
    }


    public void setCode(String code) {
        this.code = code;
    }


    public String getCode() {
        return this.code;
    }


    public void setName(String name) {
        this.name=name;
    }


    public String getName() {
        return this.name;
    }
}
