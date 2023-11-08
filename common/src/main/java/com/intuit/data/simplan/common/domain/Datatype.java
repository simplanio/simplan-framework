package com.intuit.data.simplan.common.domain;

import java.io.Serializable;

public enum Datatype implements Serializable {
    STRING("String"),
    INTEGER("Integer"),
    FLOAT("Float"),
    LONG("Long"),
    DOUBLE("Double"),
    BOOLEAN("Boolean");

    private final String identifier;

    Datatype(String identifier) {
        this.identifier = identifier;
    }

    public String identifier() {
        return identifier;
    }

}