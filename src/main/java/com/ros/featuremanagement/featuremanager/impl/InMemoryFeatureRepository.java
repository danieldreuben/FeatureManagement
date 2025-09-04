package com.ros.featuremanagement.featuremanager.impl;

import com.ros.featuremanagement.featuremanager.FeatureDefinition;
import com.ros.featuremanagement.featuremanager.FeatureRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryFeatureRepository implements FeatureRepository {
    private final Map<String, FeatureDefinition> cache = new HashMap<>();

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
}
