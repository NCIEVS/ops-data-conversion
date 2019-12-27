package gov.nih.nci.evs.canmed.entity.concept;

import com.sun.org.apache.bcel.internal.generic.ARETURN;
import com.sun.org.apache.bcel.internal.generic.DRETURN;
import com.sun.org.apache.bcel.internal.generic.FRETURN;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class HcpcsPackage extends HcpcsHierarchicalConcept {
    HashSet<String> brandNames;
    String hcpcsCode;
    String strength;
    String oral;
    String fdaApproval;
    String fdaDiscontinue;
    String cmsEffective;
    String cmsDiscontinue;
    String status;
    boolean active = true;

    public HcpcsPackage(String code, String name) {
        super(code, name);
    }

    public HashSet<String> getBrandNames() {
        return brandNames;
    }


    public void setBrandNames(HashSet<String> brandNames) {
        this.brandNames = brandNames;
    }

    public void addBrandName(String brandName) {
        this.brandNames.add(brandName);
    }

    public String getHcpcsCode() {
        return hcpcsCode;
    }

    public void setHcpcsCode(String hcpcsCode) {
        this.hcpcsCode = hcpcsCode;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getOral() {
        return oral;
    }

    public void setOral(String oral) {
        this.oral = oral;
    }

    public String getFdaApproval() {
        return fdaApproval;
    }

    public void setFdaApproval(String fdaApproval) {
        this.fdaApproval = fdaApproval;
    }

    public String getFdaDiscontinue() {
        return fdaDiscontinue;
    }

    public void setFdaDiscontinue(String fdaDiscontinue) {
        this.fdaDiscontinue = fdaDiscontinue;
    }

    public String getCmsEffective() {
        return cmsEffective;
    }

    public void setCmsEffective(String cmsEffective) {
        this.cmsEffective = cmsEffective;
    }

    public String getCmsDiscontinue() {
        return cmsDiscontinue;
    }

    public void setCmsDiscontinue(String cmsDiscontinue) {
        this.cmsDiscontinue = cmsDiscontinue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if (status.toUpperCase().startsWith("NO")) {
            active = false;
        }
    }

    public boolean isActive() { return this.active; }
}
