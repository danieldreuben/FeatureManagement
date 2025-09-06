package com.ros.featuremanagement.demo;

import com.ros.featuremanagement.featuremanager.*;
import com.ros.featuremanagement.featuremanager.impl.InMemoryFeatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FeatureEnabledTest {

    private FeatureManager fm;
    private InMemoryFeatureRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryFeatureRepository();

        // Feature is globally disabled
        repo.addFeature(new FeatureDefinition(
                "DisabledFeature",
                false, // globally disabled
                List.of(new FilterConfig("AlwaysOn", Map.of()))
        ));

        fm = new FeatureManager(
                repo,
                Map.of(
                        "AlwaysOn", (ctx, params) -> true
                ),
                (ctx, params) -> false // default filter
        );
    }

    @Test
    void testDisabledFeatureAlwaysReturnsFalse() {
        boolean result = fm.isEnabled("DisabledFeature", new FeatureContext("u1", List.of(), List.of()));
        assertFalse(result, "Disabled feature should always return false");
    }

    @Test
    void testUnknownFeatureReturnsFalse() {
        boolean result = fm.isEnabled("NoSuchFeature", new FeatureContext("u1", List.of(), List.of()));
        assertFalse(result, "Unknown feature should return false");
    }
}
