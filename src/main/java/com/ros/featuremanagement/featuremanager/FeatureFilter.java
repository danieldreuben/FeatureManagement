package com.ros.featuremanagement.featuremanager;

import java.util.Map;

@FunctionalInterface
public interface FeatureFilter {
    boolean evaluate(FeatureContext context, Map<String, Object> parameters);
}
