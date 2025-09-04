
package com.ros.featuremanagement.demo;

import com.ros.featuremanagement.featuremanager.FeatureManager;
import com.ros.featuremanagement.featuremanager.FeatureRepository;
import com.ros.featuremanagement.featuremanager.FeatureFilter;
import com.ros.featuremanagement.featuremanager.FeatureDefinition;
import com.ros.featuremanagement.featuremanager.FilterConfig;
import com.ros.featuremanagement.featuremanager.FeatureContext;



import com.ros.featuremanagement.featuremanager.impl.InMemoryFeatureRepository;
import com.ros.featuremanagement.featuremanager.impl.YamlFeatureRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FeatureManagerTest {

    private InMemoryFeatureRepository repo;
    private FeatureManager featureManager;

	@BeforeEach
	void setup() {
		repo = new InMemoryFeatureRepository();

		Map<String, FeatureFilter> filters = new HashMap<>();
		filters.put("AlwaysOn", (ctx, params) -> true);
		filters.put("Percentage", (ctx, params) ->
				Math.random() < (double) params.getOrDefault("percentage", 0.0));
		filters.put("RoleBased", (ctx, params) -> {
			String requiredRole = (String) params.get("role");
			return ctx.getRoles().contains(requiredRole);
		});
		filters.put("TimeBased", (ctx, params) -> {
			String start = (String) params.get("start");
			String end = (String) params.get("end");
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime startTime = start != null ? LocalDateTime.parse(start) : LocalDateTime.MIN;
			LocalDateTime endTime = end != null ? LocalDateTime.parse(end) : LocalDateTime.MAX;
			return !now.isBefore(startTime) && !now.isAfter(endTime);
		});
		filters.put("Targeting", (ctx, params) -> {
			@SuppressWarnings("unchecked")
			List<String> users = (List<String>) params.getOrDefault("users", List.of());
			@SuppressWarnings("unchecked")
			List<String> groups = (List<String>) params.getOrDefault("groups", List.of());

			if (users.contains(ctx.getUserId())) {
				return true;
			}
			return ctx.getRoles().stream().anyMatch(groups::contains);
		});

		FeatureFilter defaultFilter = (ctx, params) -> false;

		featureManager = new FeatureManager(repo, filters, defaultFilter);
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

    @Test
    void testAlwaysOnFilter() {
        FeatureDefinition def = new FeatureDefinition("FeatureA",
                List.of(new FilterConfig("AlwaysOn", Collections.emptyMap())));
        repo.addFeature(def);

        boolean enabled = featureManager.isEnabled("FeatureA", new FeatureContext("user1", Set.of("admin")));
        assertTrue(enabled, "AlwaysOn filter should always return true");
    }

    @Test
    void testRoleBasedFilterSuccess() {
        FeatureDefinition def = new FeatureDefinition("FeatureB",
                List.of(new FilterConfig("RoleBased", Map.of("role", "admin"))));
        repo.addFeature(def);

        FeatureContext ctx = new FeatureContext("user1", Set.of("admin"));
        assertTrue(featureManager.isEnabled("FeatureB", ctx));
    }

    @Test
    void testRoleBasedFilterFailure() {
        FeatureDefinition def = new FeatureDefinition("FeatureC",
                List.of(new FilterConfig("RoleBased", Map.of("role", "manager"))));
        repo.addFeature(def);

        FeatureContext ctx = new FeatureContext("user1", Set.of("admin"));
        assertFalse(featureManager.isEnabled("FeatureC", ctx));
    }

    @Test
    void testUnknownFilterFallsBackToDefault() {
        FeatureDefinition def = new FeatureDefinition("FeatureD",
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
    void testFeatureNotDefined() {
        FeatureContext ctx = new FeatureContext("user1", Set.of("admin"));
        assertFalse(featureManager.isEnabled("NonExistent", ctx));
    }
}
