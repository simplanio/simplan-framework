package com.intuit.data.simplan.common;

public class PullRequestModel {
    public String number;
    public PullRequestHead base;

    public PullRequestHead getBase() {
        return base;
    }

    public void setBase(PullRequestHead base) {
        this.base = base;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
