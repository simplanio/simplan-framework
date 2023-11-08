package com.intuit.data.simplan.logging.domain.v2.fiedsets;

import com.intuit.data.simplan.logging.domain.JacksonAnyProperty;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 14-Apr-2022 at 1:44 PM
 */
public class ErrorOpsEvent extends JacksonAnyProperty {
    /**
     * Message of the Exception
     */
    String message;
    /**
     * Stack Trace ofthe message
     */
    String stackTrace;
    /**
     * Exception Class Canonical name
     */
    String type;

    /**
     * Cause of exception, if available
     */
    String cause;

    /**
     * Trace of cause of available.
     */
    String causeTrace;

    public ErrorOpsEvent() {
    }

    public ErrorOpsEvent(Throwable t) {
        this.message = ExceptionUtils.getMessage(t);
        this.stackTrace = ExceptionUtils.getStackTrace(t);
        this.type = t.getClass().getCanonicalName();
        this.cause = ExceptionUtils.getRootCauseMessage(t);
        try {
            this.causeTrace = ExceptionUtils.getStackTrace(t.getCause());
        } catch (Exception ignored) {
        }
    }

    public String getMessage() {
        return message;
    }

    public ErrorOpsEvent setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public ErrorOpsEvent setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }

    public String getType() {
        return type;
    }

    public ErrorOpsEvent setType(String type) {
        this.type = type;
        return this;
    }

    public String getCause() {
        return cause;
    }

    public ErrorOpsEvent setCause(String cause) {
        this.cause = cause;
        return this;
    }

    public String getCauseTrace() {
        return causeTrace;
    }

    public ErrorOpsEvent setCauseTrace(String causeTrace) {
        this.causeTrace = causeTrace;
        return this;
    }
}
