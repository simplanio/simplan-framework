package com.intuit.data.simplan.logging.domain.v2.fiedsets;

import com.intuit.data.simplan.logging.domain.JacksonAnyProperty;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 26-May-2022 at 4:42 PM
 */
public class MetaOpsEvent extends JacksonAnyProperty {
    String asset;
    String opsOwner;
    String businessOwner;

    public String getAsset() {
        return asset;
    }

    public MetaOpsEvent setAsset(String asset) {
        this.asset = asset;
        return this;
    }

    public String getOpsOwner() {
        return opsOwner;
    }

    public MetaOpsEvent setOpsOwner(String opsOwner) {
        this.opsOwner = opsOwner;
        return this;
    }

    public String getBusinessOwner() {
        return businessOwner;
    }

    public MetaOpsEvent setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
        return this;
    }
}
