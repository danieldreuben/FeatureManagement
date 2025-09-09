package com.ros.featuremanagement.featuremanager;

import java.util.Map;

public interface FeatureRepository {
    FeatureDefinition getFeature(String name);
    Map<String, FeatureDefinition> getAllFeatures();
    void refresh();
    public void addFeature(FeatureDefinition def);    
}

