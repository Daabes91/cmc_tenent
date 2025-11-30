package com.clinic.modules.saas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing a PayPal webhook event.
 * Maps to the structure of webhook notifications sent by PayPal.
 */
public class PayPalWebhookEvent {

    @JsonProperty("id")
    private String id;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("resource")
    private PayPalWebhookResource resource;

    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("resource_type")
    private String resourceType;

    @JsonProperty("event_version")
    private String eventVersion;

    @JsonProperty("summary")
    private String summary;

    // Constructors

    public PayPalWebhookEvent() {
    }

    public PayPalWebhookEvent(String id, String eventType, PayPalWebhookResource resource) {
        this.id = id;
        this.eventType = eventType;
        this.resource = resource;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public PayPalWebhookResource getResource() {
        return resource;
    }

    public void setResource(PayPalWebhookResource resource) {
        this.resource = resource;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getEventVersion() {
        return eventVersion;
    }

    public void setEventVersion(String eventVersion) {
        this.eventVersion = eventVersion;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
