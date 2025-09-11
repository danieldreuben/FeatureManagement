package com.ros.featuremanagement.featuremanager.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "feature.appconfig")
public class FeatureAppConfigProperties {

    private String endpoint;
    private List<String> keys;
    private String label;
    private Duration refreshInterval;

    // --- getters ---
    public String getEndpoint() {
        return endpoint;
    }

    public List<String> getKeys() {
        return keys;
    }

    public String getLabel() {
        return label;
    }

    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    // --- setters ---
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}
