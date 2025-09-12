package com.ros.featuremanagement.featuremanager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The {@code FeatureManager} provides a central API for evaluating
 * whether application features are enabled or disabled for a given
 * {@link FeatureContext}.  
 * 
 * <p>It integrates with a {@link FeatureRepository} to fetch feature
 * definitions and applies {@link FeatureFilter} instances to determine
 * the activation status of each feature. The class supports default
 * filters such as "AlwaysOn", "Percentage", "RoleBased", "TimeBased",
 * and "Targeting", but custom filters can be provided via constructor.
 *
 * <p>Typical use cases include:
 * <ul>
 *     <li>Dynamic feature flag evaluation based on user roles, permissions, or groups.</li>
 *     <li>Time-based feature rollouts.</li>
 *     <li>Percentage-based feature experimentation or A/B testing.</li>
 *     <li>Custom targeting of features to specific users or user groups.</li>
 * </ul>
 *
 * <p>The manager can also refresh feature definitions from the repository
 * on demand.
 */
public class FeatureManager {

    /** The repository used to fetch feature definitions. */
    private final FeatureRepository repository;

    /** Map of feature filter names to filter implementations. */
    private final Map<String, FeatureFilter> filters;

    /** The default filter applied when a named filter is not found. */
    private final FeatureFilter defaultFilter;

    /**
     * Constructs a FeatureManager with a custom repository, filters, and default filter.
     *
     * @param repository the feature repository to use for fetching definitions
     * @param filters map of filter names to {@link FeatureFilter} implementations
     * @param defaultFilter a fallback filter applied when a named filter is missing
     */
    public FeatureManager(
            FeatureRepository repository,
            Map<String, FeatureFilter> filters,
            FeatureFilter defaultFilter) {
        this.repository = repository;
        this.filters = filters;
        this.defaultFilter = defaultFilter;
    }

    /**
     * Constructs a FeatureManager with a repository and a set of default filters.
     * The default filter returns {@code false} when a named filter is not found.
     *
     * <p>Default filters include:
     * <ul>
     *     <li>AlwaysOn - always returns true</li>
     *     <li>Percentage - enables feature for a given percentage of users</li>
     *     <li>RoleBased - enables feature for users with a specific role</li>
     *     <li>TimeBased - enables feature between start and end times</li>
     *     <li>Targeting - enables feature for specific users or groups</li>
     * </ul>
     *
     * @param repository the feature repository to use
     */
    public FeatureManager(
            FeatureRepository repository) {
        this.repository = repository;
        this.filters = getDefaultFilters();
        this.defaultFilter = (ctx, params) -> false;
    }    

    /**
     * Determines if a specific feature is enabled for the given {@link FeatureContext}.
     *
     * <p>The evaluation follows these rules:
     * <ul>
     *     <li>If the feature is not found or is explicitly disabled, returns false.</li>
     *     <li>Otherwise, iterates through the feature's configured filters and
     *     applies each filter in order. If any filter returns true, the feature
     *     is considered enabled (short-circuit evaluation).</li>
     * </ul>
     *
     * @param featureName the name of the feature to check
     * @param ctx the context containing user, roles, and permissions
     * @return true if the feature is enabled for the context; false otherwise
     */
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

    /**
     * Returns a map of all feature names to their enabled/disabled status
     * for the given {@link FeatureContext}.
     *
     * <p>This method evaluates each feature individually using {@link #isEnabled(String, FeatureContext)}.
     *
     * @param ctx the context containing user, roles, and permissions
     * @return a map where keys are feature names and values are booleans indicating
     *         whether the feature is enabled
     */
    public Map<String, Boolean> getAllFeatures(FeatureContext ctx) {
        return repository.getAllFeatures().keySet().stream()
                .collect(Collectors.toMap(
                        featureName -> featureName,
                        featureName -> isEnabled(featureName, ctx)
                ));
    }    

    /**
     * Returns the default set of filters used when none are provided externally.
     *
     * <p>Default filters:
     * <ul>
     *     <li>AlwaysOn - always enabled</li>
     *     <li>Percentage - enabled for a random percentage of users</li>
     *     <li>RoleBased - enabled for users with specified roles</li>
     *     <li>TimeBased - enabled between a start and end time</li>
     *     <li>Targeting - enabled for specific users or groups</li>
     * </ul>
     *
     * @return a map of default filter names to {@link FeatureFilter} implementations
     */
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

    /**
     * Refreshes all feature definitions from the underlying {@link FeatureRepository}.
     *
     * <p>This method can be called to ensure that the latest configuration
     * is applied, for example after an external update to feature flags.
     */
    public void refreshFeatures() {
        repository.refresh();
    }
}
