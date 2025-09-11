
package com.ros.featuremanagement.demo;

import com.ros.featuremanagement.featuremanager.FeatureManager;
import com.ros.featuremanagement.featuremanager.FeatureDefinition;
import com.ros.featuremanagement.featuremanager.FilterConfig;
import com.ros.featuremanagement.featuremanager.FeatureContext;

import com.ros.featuremanagement.featuremanager.impl.InMemoryFeatureRepository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryFeatureRepositoryTest {

    private InMemoryFeatureRepository repo;
    private FeatureManager featureManager;

	@BeforeEach
	void setup() {
		repo = new InMemoryFeatureRepository();
		featureManager = new FeatureManager(repo);
	}

    @Test
    void testAlwaysOnFilter() {
        FeatureDefinition def = new FeatureDefinition("FeatureA", true,
                List.of(new FilterConfig("AlwaysOn", Collections.emptyMap())));
        repo.addFeature(def);

        boolean enabled = featureManager.isEnabled("FeatureA", new FeatureContext("user1", Set.of("admin")));
        assertTrue(enabled, "AlwaysOn filter should always return true");
    }

    @Test
    void testRoleBasedFilterSuccess() {
        FeatureDefinition def = new FeatureDefinition("FeatureB", true,
                List.of(new FilterConfig("RoleBased", Map.of("role", "admin"))));
        repo.addFeature(def);

        FeatureContext ctx = new FeatureContext("user1", Set.of("admin"));
        assertTrue(featureManager.isEnabled("FeatureB", ctx));
    }

    @Test
    void testRoleBasedFilterFailure() {
        FeatureDefinition def = new FeatureDefinition("FeatureC", true,
                List.of(new FilterConfig("RoleBased", Map.of("role", "manager"))));
        repo.addFeature(def);

        FeatureContext ctx = new FeatureContext("user1", Set.of("admin"));
        assertFalse(featureManager.isEnabled("FeatureC", ctx));
    }

    @Test
    void testUnknownFilterFallsBackToDefault() {
        FeatureDefinition def = new FeatureDefinition("FeatureD", true,
                List.of(new FilterConfig("UnknownFilter", Collections.emptyMap())));
        repo.addFeature(def);

        FeatureContext ctx = new FeatureContext("user1", Set.of("admin"));
        assertFalse(featureManager.isEnabled("FeatureD", ctx),
                "Unknown filter should fall back to default filter (false)");
    }

    @Test
    void testMultipleFiltersShortCircuit() {
        FeatureDefinition def = new FeatureDefinition("FeatureE", List.of(
                new FilterConfig("RoleBased", Map.of("role", "admin")),
                new FilterConfig("AlwaysOn", Collections.emptyMap())
        ));
        repo.addFeature(def);

        FeatureContext ctx = new FeatureContext("user1", Set.of("admin"));
        assertTrue(featureManager.isEnabled("FeatureE", ctx),
                "Should return true after first matching filter");
    }
    @Test
    void testAddAndRetrieveFeature() {
        InMemoryFeatureRepository repo = new InMemoryFeatureRepository();

        FeatureDefinition def = new FeatureDefinition("TestFeature",
                List.of(new FilterConfig("AlwaysOn", Map.of())));
        repo.addFeature(def);

        FeatureDefinition retrieved = repo.getFeature("TestFeature");
        assertNotNull(retrieved);
        assertEquals("TestFeature", retrieved.getName());
        assertEquals(1, retrieved.getFilters().size());
    }

    @Test
    void testGetAllFeatures() {
        InMemoryFeatureRepository repo = new InMemoryFeatureRepository();
        int initial = repo.getAllFeatures().size();
        repo.addFeature(new FeatureDefinition("F1", true, List.of()));
        repo.addFeature(new FeatureDefinition("F2", true, List.of()));

        Map<String, FeatureDefinition> all = repo.getAllFeatures();
        assertEquals(initial + 2, all.size());
        assertTrue(all.containsKey("F1"));
        assertTrue(all.containsKey("F2"));
    }

    @Test
    void testRefreshIsNoOp() {
        InMemoryFeatureRepository repo = new InMemoryFeatureRepository();
        repo.refresh(); // should not throw
    }

    @Test
    void testFeatureNotDefined() {
        FeatureContext ctx = new FeatureContext("user1", Set.of("admin"));
        assertFalse(featureManager.isEnabled("NonExistent", ctx));
    }

    @Test
    void testDisabledFeatureAlwaysReturnsFalse() {
        // Feature is globally disabled
        repo.addFeature(new FeatureDefinition(
                "DisabledFeature",
                false, // globally disabled
                List.of(new FilterConfig("AlwaysOn", Map.of()))
        ));        
        boolean result = featureManager.isEnabled("DisabledFeature", new FeatureContext("u1", List.of(), List.of()));
        assertFalse(result, "Disabled feature should always return false");
    }

    @Test
    void testUnknownFeatureReturnsFalse() {
        boolean result = featureManager.isEnabled("NoSuchFeature", new FeatureContext("u1", List.of(), List.of()));
        assertFalse(result, "Unknown feature should return false");
    }    
}
