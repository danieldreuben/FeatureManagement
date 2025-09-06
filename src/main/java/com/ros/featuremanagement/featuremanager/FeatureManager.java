package com.ros.featuremanagement.featuremanager;

import java.util.Map;

public class FeatureManager {
    private final FeatureRepository repository;
    private final Map<String, FeatureFilter> filters;
    private final FeatureFilter defaultFilter;

    public FeatureManager(
            FeatureRepository repository,
            Map<String, FeatureFilter> filters,
            FeatureFilter defaultFilter) {
        this.repository = repository;
        this.filters = filters;
        this.defaultFilter = defaultFilter;
    }

    public boolean isEnabled(String featureName, FeatureContext ctx) {
        FeatureDefinition def = repository.getFeature(featureName);

        if (def == null || !def.getEnabled()) { return false; }

        for (FilterConfig filterConfig : def.getFilters()) {
      
            FeatureFilter filter = filters.getOrDefault(filterConfig.getName(), defaultFilter);
            if (filter.evaluate(ctx, filterConfig.getParameters())) {
                return true; // short-circuit success
            }
        }
        return false;
    }

    public void refreshFeatures() {
        repository.refresh();
    }
}
