package com.ros.featuremanagement.featuremanager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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

    public FeatureManager(
            FeatureRepository repository) {
        this.repository = repository;
        this.filters = getDefaultFilters();
        this.defaultFilter = (ctx, params) -> false;
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

    private Map<String, FeatureFilter> getDefaultFilters() {
        Map<String, FeatureFilter> filters = new HashMap<>();
        filters.put("AlwaysOn", (ctx, params) -> true);
        filters.put("Percentage", (ctx, params) ->
            Math.random() < (double) params.getOrDefault("percentage", 0.0));
        filters.put("RoleBased", (ctx, params) -> ctx.getRoles().contains(params.get("role")));
        filters.put("TimeBased", (ctx, params) -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = params.get("start") != null ? LocalDateTime.parse((String) params.get("start")) : LocalDateTime.MIN;
            LocalDateTime end = params.get("end") != null ? LocalDateTime.parse((String) params.get("end")) : LocalDateTime.MAX;
            return !now.isBefore(start) && !now.isAfter(end);
        });
        filters.put("Targeting", (ctx, params) -> {
            List<String> users = (List<String>) params.getOrDefault("users", List.of());
            List<String> groups = (List<String>) params.getOrDefault("groups", List.of());
            return users.contains(ctx.getUserId()) || ctx.getRoles().stream().anyMatch(groups::contains);
        });

        return filters;
    }

    public void refreshFeatures() {
        repository.refresh();
    }
}
