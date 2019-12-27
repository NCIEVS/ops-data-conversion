package gov.nih.nci.evs.canmed.entity.concept;

import java.util.Collection;
import java.util.Date;

public class NdcPackage extends NdcProduct {

    String strength;
    String ndcPackageCode;
    String packageEffectiveDate;
    String packageDiscontinueDate;
    String status;
    boolean active=true;

    public NdcPackage(String code, String name) {
        super(code, name);
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getNdcPackageCode() {
        return ndcPackageCode;
    }

    public void setNdcPackageCode(String ndcPackageCode) {
        this.ndcPackageCode = ndcPackageCode;
    }

    public String getPackageEffectiveDate() {
        return packageEffectiveDate;
    }

    public void setPackageEffectiveDate(String packageEffectiveDate) {
        this.packageEffectiveDate = packageEffectiveDate;
    }

    public String getPackageDiscontinueDate() {
        return packageDiscontinueDate;
    }

    public void setPackageDiscontinueDate(String packageDiscontinueDate) {
        this.packageDiscontinueDate = packageDiscontinueDate;
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
