package com.intuit.data.simplan.logging.domain.v2.fiedsets;

import com.intuit.data.simplan.logging.domain.JacksonAnyProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 26-May-2022 at 4:50 PM
 */
public class ConfigDefinitionOpsEvent extends JacksonAnyProperty {
    Map<String, Object> config;
    Map<String, Object> options;

    public ConfigDefinitionOpsEvent() {
        this.config = new HashMap<>();
        this.options = new HashMap<>();
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public ConfigDefinitionOpsEvent setConfig(Map<String, Object> config) {
        this.config = config;
        return this;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public ConfigDefinitionOpsEvent setOptions(Map<String, Object> options) {
        this.options = options;
        return this;
    }
}
