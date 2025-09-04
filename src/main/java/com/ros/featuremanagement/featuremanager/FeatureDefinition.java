package com.ros.featuremanagement.featuremanager;

import java.util.List;

public class FeatureDefinition {
    private final String name;
    private final List<FilterConfig> filters;

    public FeatureDefinition(String name, List<FilterConfig> filters) {
        this.name = name;
        this.filters = filters;
    }

    public String getName() {
        return name;
    }

    public List<FilterConfig> getFilters() {
        return filters;
    }
}
