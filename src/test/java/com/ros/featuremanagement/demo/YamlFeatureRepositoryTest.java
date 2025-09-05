package com.ros.featuremanagement.demo;

import com.ros.featuremanagement.featuremanager.FeatureContext;
import com.ros.featuremanagement.featuremanager.FeatureDefinition;
import com.ros.featuremanagement.featuremanager.FeatureManager;
import com.ros.featuremanagement.featuremanager.FeatureRepository;
import com.ros.featuremanagement.featuremanager.FilterConfig;
import com.ros.featuremanagement.featuremanager.impl.YamlFeatureRepository;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlFeatureRepositoryTest {
        @Test
        void testYamlFeatureLoading() {
                FeatureRepository repo = new YamlFeatureRepository("feature.yaml");

                FeatureDefinition alwaysOn = repo.getFeature("AlwaysOnFeature");
                assertNotNull(alwaysOn);
                assertEquals("AlwaysOnFeature", alwaysOn.getName());
                assertEquals(1, alwaysOn.getFilters().size());

                FeatureDefinition roleBased = repo.getFeature("AdminOnlyFeature");
                assertNotNull(roleBased);
                assertEquals("admin", roleBased.getFilters().get(0).getParameters().get("role"));
        }

        @Test
        void testTargetingFeatureForUserAndGroup() {
        FeatureRepository yamlRepo = new YamlFeatureRepository("feature.yaml");
        FeatureManager fm = new FeatureManager(
                yamlRepo,
                Map.of(
                        "AlwaysOn", (ctx, params) -> true,
                        "RoleBased", (ctx, params) -> ctx.getRoles().contains(params.get("role")),
                        "Percentage", (ctx, params) -> Math.random() < (double) params.getOrDefault("percentage", 0.0),
                        "TimeBased", (ctx, params) -> true, // skip for now
                        "Targeting", (ctx, params) -> {
                                @SuppressWarnings("unchecked")
                                List<String> users = (List<String>) params.getOrDefault("users", List.of());
                                @SuppressWarnings("unchecked")
                                List<String> groups = (List<String>) params.getOrDefault("groups", List.of());

                                if (users.contains(ctx.getUserId())) {
                                return true;
                                }
                                return ctx.getRoles().stream().anyMatch(groups::contains);
                        }
                ),
                (ctx, params) -> false // default filter
        );

        FeatureContext userAlice = new FeatureContext("alice", List.of());
        FeatureContext userBob = new FeatureContext("bob", List.of("qa"));
        FeatureContext userEve = new FeatureContext("eve", List.of("guest"));

        assertTrue(fm.isEnabled("TargetingFeature", userAlice),
                "alice should match via users list");
        assertTrue(fm.isEnabled("TargetingFeature", userBob),
                "bob should match via group 'qa'");
        assertFalse(fm.isEnabled("TargetingFeature", userEve),
                "eve should not match any targeting rule");
        }

}

