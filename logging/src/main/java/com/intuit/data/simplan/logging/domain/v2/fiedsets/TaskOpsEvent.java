package com.intuit.data.simplan.logging.domain.v2.fiedsets;

import com.intuit.data.simplan.logging.domain.JacksonAnyProperty;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 14-Apr-2022 at 2:45 PM
 */
public class TaskOpsEvent extends JacksonAnyProperty {
    String name;
    Long index;
    String operatorType;
    String operator;

    public String getName() {
        return name;
    }

    public TaskOpsEvent setName(String name) {
        this.name = name;
        return this;
    }

    public Long getIndex() {
        return index;
    }

    public TaskOpsEvent setIndex(Long index) {
        this.index = index;
        return this;
    }

    public String getOperatorType() {
        return operatorType;
    }

    public TaskOpsEvent setOperatorType(String operatorType) {
        this.operatorType = operatorType;
        return this;
    }

    public String getOperator() {
        return operator;
    }

    public TaskOpsEvent setOperator(String operator) {
        this.operator = operator;
        return this;
    }
}
