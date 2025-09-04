package com.ros.featuremanagement.demo;

import com.ros.featuremanagement.featuremanager.FeatureDefinition;
import com.ros.featuremanagement.featuremanager.FilterConfig;
import com.ros.featuremanagement.featuremanager.impl.InMemoryFeatureRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryFeatureRepositoryTest {

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

        repo.addFeature(new FeatureDefinition("F1", List.of()));
        repo.addFeature(new FeatureDefinition("F2", List.of()));

        Map<String, FeatureDefinition> all = repo.getAllFeatures();
        assertEquals(2, all.size());
        assertTrue(all.containsKey("F1"));
        assertTrue(all.containsKey("F2"));
    }

    @Test
    void testRefreshIsNoOp() {
        InMemoryFeatureRepository repo = new InMemoryFeatureRepository();
        repo.refresh(); // should not throw
    }
}

