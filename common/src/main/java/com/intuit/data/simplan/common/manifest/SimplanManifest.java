package com.intuit.data.simplan.common.manifest;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 05-Aug-2022 at 2:30 PM
 */
public class SimplanManifest {
    String parent;
    String appName;
    String asset;
    String sqlScript;
    String businessOwner;
    String opsOwner;
    String sha;
    String environment;
    String source;

    public String getBusinessOwner() {
        return businessOwner;
    }

    public SimplanManifest setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
        return this;
    }

    public String getOpsOwner() {
        return opsOwner;
    }

    public SimplanManifest setOpsOwner(String opsOwner) {
        this.opsOwner = opsOwner;
        return this;
    }

    public String getParent() {
        return parent;
    }

    public SimplanManifest setParent(String parent) {
        this.parent = parent;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public SimplanManifest setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getAsset() {
        return asset;
    }

    public SimplanManifest setAsset(String asset) {
        this.asset = asset;
        return this;
    }

    public String getSqlScript() {
        return sqlScript;
    }

    public SimplanManifest setSqlScript(String sqlScript) {
        this.sqlScript = sqlScript;
        return this;
    }

    public String getSha() {
        return sha;
    }

    public SimplanManifest setSha(String sha) {
        this.sha = sha;
        return this;
    }

    public String getEnvironment() {
        return environment;
    }

    public SimplanManifest setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public String getSource() {
        return source;
    }

    public SimplanManifest setSource(String source) {
        this.source = source;
        return this;
    }
}
