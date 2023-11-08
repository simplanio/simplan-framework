package com.intuit.data.simplan.core.domain;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 16-Nov-2021 at 12:09 PM
 */
public enum OperatorType {
    TRIGGER("trigger"),
    ACTION("action"),
    VALIDATION("validation");

    private final String name;

    OperatorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
