package com.ros.featuremanagement.featuremanager.impl;

import com.ros.featuremanagement.featuremanager.FeatureDefinition;
import com.ros.featuremanagement.featuremanager.FeatureRepository;
import com.ros.featuremanagement.featuremanager.FilterConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryFeatureRepository implements FeatureRepository {
    private final Map<String, FeatureDefinition> cache = new HashMap<>();

    public InMemoryFeatureRepository() {
        getDefaultFeatures(this);
    }

    public void addFeature(FeatureDefinition def) {
        cache.put(def.getName(), def);
    }

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
        // no-op for in-memory
    }

    private void getDefaultFeatures(FeatureRepository repo) {
        repo.addFeature(new FeatureDefinition("AlwaysOnFeature", true, List.of(new FilterConfig("AlwaysOn", Map.of()))));
        repo.addFeature(new FeatureDefinition("DisabledFeature", false, List.of(new FilterConfig("AlwaysOn", Map.of()))));
        repo.addFeature(new FeatureDefinition("AdminFeature", true, List.of(new FilterConfig("RoleBased", Map.of("role", "admin")))));
    }    
}
