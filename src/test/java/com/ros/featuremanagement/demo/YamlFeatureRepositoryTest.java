package com.ros.featuremanagement.demo;

import com.ros.featuremanagement.featuremanager.FeatureContext;
import com.ros.featuremanagement.featuremanager.FeatureDefinition;
import com.ros.featuremanagement.featuremanager.FeatureManager;
import com.ros.featuremanagement.featuremanager.FeatureRepository;
import com.ros.featuremanagement.featuremanager.impl.YamlFeatureRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlFeatureRepositoryTest {
        private FeatureManager fm;

        @BeforeEach
        void setUp() {
        FeatureRepository yamlRepo = new YamlFeatureRepository("feature.yaml");

        fm = new FeatureManager(
                yamlRepo,
                Map.of(

                        "Targeting", (ctx, params) -> {
                                @SuppressWarnings("unchecked")
                                List<String> users = (List<String>) params.getOrDefault("users", List.of());
                                @SuppressWarnings("unchecked")
                                List<String> groups = (List<String>) params.getOrDefault("groups", List.of());

                                if (users.contains(ctx.getUserId())) {
                                return true;
                                }
                                return ctx.getRoles().stream().anyMatch(groups::contains);
                        },
                        "RoleAndPermission", (ctx, params) -> {
                                @SuppressWarnings("unchecked")
                                List<String> roles = (List<String>) params.getOrDefault("roles", List.of());
                                @SuppressWarnings("unchecked")
                                List<String> permissions = (List<String>) params.getOrDefault("permissions", List.of());

                                boolean hasRole = roles.isEmpty() || ctx.getRoles().stream().anyMatch(roles::contains);
                                boolean hasPermission = permissions.isEmpty() || ctx.getPermissions().stream().anyMatch(permissions::contains);

                                return hasRole && hasPermission;
                        }

                ),
                (ctx, params) -> false // default filter
        );
        }

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

