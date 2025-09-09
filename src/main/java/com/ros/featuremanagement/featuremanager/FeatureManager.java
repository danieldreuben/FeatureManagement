package com.ros.featuremanagement.featuremanager;

import java.util.Map;
import java.util.stream.Collectors;

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

    /** ✅ New method: return all features with their enabled/disabled status */
    public Map<String, Boolean> getAllFeatures(FeatureContext ctx) {
        return repository.getAllFeatures().keySet().stream()
                .collect(Collectors.toMap(
                        featureName -> featureName,
                        featureName -> isEnabled(featureName, ctx)
                ));
    }    

    public void refreshFeatures() {
        repository.refresh();
    }
}
