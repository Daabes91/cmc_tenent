package com.clinic.modules.saas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * DTO representing the resource object within a PayPal webhook event.
 * Contains the actual subscription or payment data.
 */
public class PayPalWebhookResource {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("custom_id")
    private String customId;

    @JsonProperty("billing_info")
    private Map<String, Object> billingInfo;

    @JsonProperty("amount")
    private Map<String, Object> amount;

    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("update_time")
    private String updateTime;

    // Constructors

    public PayPalWebhookResource() {
    }

    public PayPalWebhookResource(String id, String status, String customId) {
        this.id = id;
        this.status = status;
        this.customId = customId;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public Map<String, Object> getBillingInfo() {
        return billingInfo;
    }

    public void setBillingInfo(Map<String, Object> billingInfo) {
        this.billingInfo = billingInfo;
    }

    public Map<String, Object> getAmount() {
        return amount;
    }

    public void setAmount(Map<String, Object> amount) {
        this.amount = amount;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
