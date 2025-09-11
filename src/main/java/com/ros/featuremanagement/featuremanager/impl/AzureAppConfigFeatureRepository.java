package com.ros.featuremanagement.featuremanager.impl;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.ros.featuremanagement.featuremanager.FeatureDefinition;
import com.ros.featuremanagement.featuremanager.FeatureRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AzureAppConfigFeatureRepository implements FeatureRepository {
    private Map<String, FeatureDefinition> cache = new HashMap<>();
    private final String endpoint;
    private final String label;
    private final List<String> keys;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

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
        // TODO: Replace with Azure App Configuration SDK/REST call
        // Stub example:
        System.out.println("Refreshing features from Azure App Config...");
        // cache = loadFromAzure();
        this.cache = getAllFeatures(this.keys);
    }

    public void addFeature(FeatureDefinition def) {
        cache.put(def.getName(), def);
    }    

    public AzureAppConfigFeatureRepository(FeatureAppConfigProperties props) {
        this.endpoint = props.getEndpoint();
        this.label = props.getLabel();
        this.keys = props.getKeys();
        System.out.println(">>>>>>>" + props.getKeys());
    }

    //@Override
    public Map<String, FeatureDefinition> getAllFeatures(List<String> keys) {
        Map<String, FeatureDefinition> features = new HashMap<>();

        for (String key : keys) {
            try {
                String url = String.format("%s/kv/%s?label=%s&api-version=1.0", endpoint, key, label);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", getAccessToken())
                        .header("Content-Type", "application/json")
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String yamlValue = extractValueFromResponse(response.body());

                    FeatureDefinition def = yamlMapper.readValue(yamlValue, FeatureDefinition.class);
                    features.put(key, def);

                } else {
                    System.err.println("Failed to fetch " + key + ": " + response.statusCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return features;
    }

    private String getAccessToken() {
        // Use Azure Managed Identity (DefaultAzureCredential)
        var credential = new DefaultAzureCredentialBuilder().build();
        var token = credential.getToken(
                new com.azure.core.credential.TokenRequestContext()
                        .addScopes("https://azconfig.io/.default"))
                .block();
        return "Bearer " + token.getToken();
    }

    private String extractValueFromResponse(String jsonResponse) throws Exception {
        // App Config returns JSON; extract the "value" field which holds YAML
        var mapper = new ObjectMapper();
        return mapper.readTree(jsonResponse).get("value").asText();
    }    
}
