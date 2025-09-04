package com.ros.featuremanagement.featuremanager;

import java.util.Map;

public class FilterConfig {
    private final String name;
    private final Map<String, Object> parameters;

    public FilterConfig(String name, Map<String, Object> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
