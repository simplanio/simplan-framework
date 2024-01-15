package com.intuit.data.simplan.logging.domain.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intuit.data.simplan.logging.domain.JacksonAnyProperty;
import com.intuit.data.simplan.logging.domain.v2.fiedsets.*;
import com.intuit.data.simplan.logging.utils.JacksonJsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 26-May-2022 at 12:25 PM
 */
public class SimplanOpsEvent extends JacksonAnyProperty {
    public final Long metricVersion = 2L;
    public String message;
    public String detailedMessage;
    @JsonProperty("@timestamp")
    public Instant timestamp;
    public Map<String, String> labels;
    public List<String> tags;
    public ErrorOpsEvent error;
    public TaskOpsEvent task;
    public ProcessOpsEvent process;
    public Object configDefinition;
    public Object eventData;
    public MetaOpsEvent meta;
    public ContextOpsEvent context;

    public SimplanOpsEvent() {
        this.timestamp = Instant.now();
        this.setTags(null);
        this.setLabels(null);
    }

    public static SimplanOpsEvent fromJson(String json) {
        return JacksonJsonMapper.fromJson(json, SimplanOpsEvent.class);
    }

    public String toJson() {
        return JacksonJsonMapper.toJson(this);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public SimplanOpsEvent setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public SimplanOpsEvent setLabels(Map<String, String> labels) {
        this.labels = labels;
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public SimplanOpsEvent setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public ErrorOpsEvent getError() {
        return error;
    }

    public SimplanOpsEvent setError(ErrorOpsEvent error) {
        this.error = error;
        return this;
    }

    public TaskOpsEvent getTask() {
        return task;
    }

    public SimplanOpsEvent setTask(TaskOpsEvent task) {
        this.task = task;
        return this;
    }

    public ProcessOpsEvent getProcess() {
        return process;
    }

    public SimplanOpsEvent setProcess(ProcessOpsEvent process) {
        this.process = process;
        return this;
    }

    public MetaOpsEvent getMeta() {
        return meta;
    }

    public SimplanOpsEvent setMeta(MetaOpsEvent meta) {
        this.meta = meta;
        return this;
    }

    public ContextOpsEvent getContext() {
        return context;
    }

    public SimplanOpsEvent setContext(ContextOpsEvent context) {
        this.context = context;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public SimplanOpsEvent setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public SimplanOpsEvent setDetailedMessage(String detailedMessage) {
        this.detailedMessage = detailedMessage;
        return this;
    }

    public Long getMetricVersion() {
        return metricVersion;
    }

    public Object getConfigDefinition() {
        return configDefinition;
    }

    public SimplanOpsEvent setConfigDefinition(Object configDefinition) {
        this.configDefinition = configDefinition;
        return this;
    }

    public Object getEventData() {
        return eventData;
    }

    public SimplanOpsEvent setEventData(Object eventData) {
        this.eventData = eventData;
        return this;
    }
}
