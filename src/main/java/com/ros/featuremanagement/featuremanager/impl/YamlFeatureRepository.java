package com.ros.featuremanagement.featuremanager.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ros.featuremanagement.featuremanager.FeatureDefinition;
import com.ros.featuremanagement.featuremanager.FeatureRepository;
import com.ros.featuremanagement.featuremanager.FilterConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class YamlFeatureRepository implements FeatureRepository {

    private final String yamlPath;
    private Map<String, FeatureDefinition> cache = new HashMap<>();

    public YamlFeatureRepository(String yamlPath) {
        this.yamlPath = yamlPath;
        refresh();
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
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(yamlPath)) {
            if (in == null) {
                throw new IllegalStateException("Feature YAML not found: " + yamlPath);
            }

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            FeatureYamlRoot root = mapper.readValue(in, FeatureYamlRoot.class);

            cache = root.getFeatures().stream()
                    .collect(Collectors.toMap(
                            FeatureYaml::getName,
                            fy -> new FeatureDefinition(
                                    fy.getName(),
                                    fy.getEnabled(),
                                    fy.getFilters().stream()
                                            .map(f -> new FilterConfig(f.getName(), f.getParameters()))
                                            .collect(Collectors.toList())
                            )
                    ));

        } catch (IOException e) {
            throw new RuntimeException("Failed to load features from YAML", e);
        }
    }

    // DTOs for YAML parsing
    public static class FeatureYamlRoot {
        private List<FeatureYaml> features;

        public List<FeatureYaml> getFeatures() {
            return features != null ? features : new ArrayList<>();
        }

        public void setFeatures(List<FeatureYaml> features) {
            this.features = features;
        }
    }

    public static class FeatureYaml {
        private String name;
        private boolean enabled;
        private List<FilterYaml> filters;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public boolean getEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public List<FilterYaml> getFilters() {
            return filters != null ? filters : new ArrayList<>();
        }
        public void setFilters(List<FilterYaml> filters) {
            this.filters = filters;
        }
    }

    public static class FilterYaml {
        private String name;
        private Map<String, Object> parameters = new HashMap<>();

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @JsonProperty("parameters")
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }

    public void addFeature(FeatureDefinition def) {
        cache.put(def.getName(), def);
    }    

}
