package com.ros.featuremanagement.demo;

import com.ros.featuremanagement.featuremanager.FeatureContext;
import com.ros.featuremanagement.featuremanager.FeatureDefinition;
import com.ros.featuremanagement.featuremanager.FeatureManager;
import com.ros.featuremanagement.featuremanager.FeatureRepository;
import com.ros.featuremanagement.featuremanager.impl.YamlFeatureRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class YamlFeatureRepositoryTest {
        private FeatureManager fm;
        FeatureRepository yamlRepo;
        
        @BeforeEach
        void setUp() {
                yamlRepo = new YamlFeatureRepository("feature.yaml");
                fm = new FeatureManager(yamlRepo);
        }

        @Test
        void testYamlFeatureLoading() {

                FeatureDefinition alwaysOn = yamlRepo.getFeature("AlwaysOnFeature");
                assertNotNull(alwaysOn);
                assertEquals("AlwaysOnFeature", alwaysOn.getName());
                assertEquals(1, alwaysOn.getFilters().size());

                FeatureDefinition roleBased = yamlRepo.getFeature("AdminOnlyFeature");
                assertNotNull(roleBased);
                assertEquals("admin", roleBased.getFilters().get(0).getParameters().get("role"));
        }

        @Test
        void testTargetingFeatureForUser() {
                FeatureContext userAlice = new FeatureContext("alice", List.of(), List.of());
                FeatureContext userEve = new FeatureContext("eve", List.of("guest"), List.of());

                assertTrue(fm.isEnabled("TargetingFeature", userAlice),
                        "alice should match via users list");
                assertFalse(fm.isEnabled("TargetingFeature", userEve),
                        "eve should not match any targeting rule");
        }

        @Test
        void testTargetingFeatureForGroup() {
                FeatureContext userBob = new FeatureContext("bob", List.of("qa"), List.of());
                FeatureContext userEve = new FeatureContext("eve", List.of("guest"), List.of());

                assertTrue(fm.isEnabled("TargetingFeature", userBob),
                        "bob should match via group 'qa'");
                assertFalse(fm.isEnabled("TargetingFeature", userEve),
                        "eve should not match any targeting rule");
        }
}

