package com.ros.featuremanagement.featuremanager.impl;

import com.ros.featuremanagement.featuremanager.FeatureDefinition;
import com.ros.featuremanagement.featuremanager.FeatureRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AzureAppConfigFeatureRepository implements FeatureRepository {
    private Map<String, FeatureDefinition> cache = new HashMap<>();

    @Override
    public FeatureDefinition getFeature(String name) {
        return cache.get(name);
    }

    @Override
    public Map<String, FeatureDefinition> getAllFeatures() {
        return Collections.unmodifiableMap(cache);
    }

    @Override
    public void refresh() {
        // TODO: Replace with Azure App Configuration SDK/REST call
        // Stub example:
        System.out.println("Refreshing features from Azure App Config...");
        // cache = loadFromAzure();
    }

    public void addFeature(FeatureDefinition def) {
        cache.put(def.getName(), def);
    }    
}
