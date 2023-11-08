package com.intuit.data.simplan.core.domain;

public enum QueryType {
    DATA_DEFINITION("DataDefinition"),
    DESCRIBE("Describe"),
    EXPLAIN("Explain"),
    ANALYZE("Analyze"),
    INSERT("Insert"),
    SELECT("Select");

    private final String value;

    QueryType(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
}
