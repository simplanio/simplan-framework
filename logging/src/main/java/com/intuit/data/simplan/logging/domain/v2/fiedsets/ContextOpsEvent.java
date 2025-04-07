package com.intuit.data.simplan.logging.domain.v2.fiedsets;

import com.intuit.data.simplan.logging.domain.JacksonAnyProperty;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 14-Apr-2022 at 1:44 PM
 */
public class ContextOpsEvent extends JacksonAnyProperty {
    /**
     * Simplan application name, This is usually the job Name
     */
    String appName;
    /**
     * Simplan parrent applciation Name - In most cases pipeline name
     */
    String parentName;

    /**
     * Simplan namespace of the application. This is used for classifying the application. If it needs to be classified in a hierarchical way then it can be done using dot notation.
     */
    String namespace;

    /**
     * Environment in which this is running. Like dev/Prod/E2e etc
     */
    String environment;
    /**
     * Run id of the specific instance
     */
    String runId;

    /**
     * Run id of the specific instance
     */
    String instanceId;
    /**
     * This gives context for this specific event, Eg: taskName, applicationName Etc
     */
    String subject;
    /**
     * Type of event like audit, metadata, process
     */
    String type;
    /**
     * Typeof action performed like applicationExecution, taskExecution etc
     */
    String action;
    /**
     * Criticality of the meric, Like info, warn, error etc
     */
    EventLevel level;
    /**
     * The source which the event is coming from! Example: QuickETL, Simplan, Superglue
     */
    String source;

    /**
     * The Orchestrator used to run the job
     */
    String orchestrator;
    /**
     * Id received from the orchestrator to identify the job
     */
    String orchestratorId;

    /**
     * Application Execution Id - Eg: Spark Application Id
     */
    String applicationId;

    public String getAppName() {
        return appName;
    }

    public ContextOpsEvent setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getParentName() {
        return parentName;
    }

    public ContextOpsEvent setParentName(String parentName) {
        this.parentName = parentName;
        return this;
    }

    public String getEnvironment() {
        return environment;
    }

    public ContextOpsEvent setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public String getRunId() {
        return runId;
    }

    public ContextOpsEvent setRunId(String runId) {
        this.runId = runId;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public ContextOpsEvent setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getType() {
        return type;
    }

    public ContextOpsEvent setType(String type) {
        this.type = type;
        return this;
    }

    public String getAction() {
        return action;
    }

    public ContextOpsEvent setAction(String action) {
        this.action = action;
        return this;
    }

    public EventLevel getLevel() {
        return level;
    }

    public ContextOpsEvent setLevel(EventLevel level) {
        this.level = level;
        return this;
    }

    public String getSource() {
        return source;
    }

    public ContextOpsEvent setSource(String source) {
        this.source = source;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public ContextOpsEvent setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getOrchestrator() {
        return orchestrator;
    }

    public ContextOpsEvent setOrchestrator(String orchestrator) {
        this.orchestrator = orchestrator;
        return this;
    }

    public String getOrchestratorId() {
        return orchestratorId;
    }

    public ContextOpsEvent setOrchestratorId(String orchestratorId) {
        this.orchestratorId = orchestratorId;
        return this;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public ContextOpsEvent setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public ContextOpsEvent setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }
}
