package com.ros.featuremanagement.featuremanager;

import java.util.List;

public class FeatureDefinition {
    private final String name;
    private final boolean enabled;
    private final List<FilterConfig> filters;

    public FeatureDefinition(String name, List<FilterConfig> filters) {
        this.name = name;
        this.enabled = true;
        this.filters = filters;
    }

    public FeatureDefinition(String name, boolean enabled, List<FilterConfig> filters) {
        this.name = name;
        this.enabled = enabled;
        this.filters = filters;
    }

    public String getName() {
        return name;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public List<FilterConfig> getFilters() {
        return filters;
    }
}
