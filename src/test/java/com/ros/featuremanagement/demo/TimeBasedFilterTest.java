package com.ros.featuremanagement.demo;

import com.ros.featuremanagement.featuremanager.FeatureManager;
import com.ros.featuremanagement.featuremanager.FeatureFilter;
import com.ros.featuremanagement.featuremanager.FeatureDefinition;
import com.ros.featuremanagement.featuremanager.FilterConfig;
import com.ros.featuremanagement.featuremanager.FeatureContext;


import com.ros.featuremanagement.featuremanager.impl.InMemoryFeatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TimeBasedFilterTest {

    private InMemoryFeatureRepository repo;
    private FeatureManager featureManager;

    @BeforeEach
    void setup() {
        repo = new InMemoryFeatureRepository();

        Map<String, FeatureFilter> filters = Map.of(
                "TimeBased", (FeatureFilter) (ctx, params) -> {
                    String startStr = (String) params.get("start");
                    String endStr   = (String) params.get("end");

                    if (startStr == null || endStr == null) {
                        return false;
                    }

                    LocalDateTime now   = LocalDateTime.now();
                    LocalDateTime start = LocalDateTime.parse(startStr);
                    LocalDateTime end   = LocalDateTime.parse(endStr);

                    return (now.isEqual(start) || now.isAfter(start)) &&
                           (now.isEqual(end)   || now.isBefore(end));
                }
        );

        FeatureFilter defaultFilter = (ctx, params) -> false;

        featureManager = new FeatureManager(repo, filters, defaultFilter);
    }

    @Test
    void testTimeBasedWithinWindow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusMinutes(1);
        LocalDateTime end   = now.plusMinutes(1);

        FeatureDefinition def = new FeatureDefinition("FeatureTime",
                List.of(new FilterConfig("TimeBased", Map.of(
                        "start", start.toString(),
                        "end", end.toString()
                ))));
        repo.addFeature(def);

        boolean enabled = featureManager.isEnabled("FeatureTime", new FeatureContext("user1", Set.of("admin")));
        assertTrue(enabled, "Feature should be enabled when current time is inside the window");
    }

    @Test
    void testTimeBasedOutsideWindow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(2);
        LocalDateTime end   = now.minusHours(1);

        FeatureDefinition def = new FeatureDefinition("FeatureTimeExpired",
                List.of(new FilterConfig("TimeBased", Map.of(
                        "start", start.toString(),
                        "end", end.toString()
                ))));
        repo.addFeature(def);

        boolean enabled = featureManager.isEnabled("FeatureTimeExpired", new FeatureContext("user2", Set.of("user")));
        assertFalse(enabled, "Feature should be disabled when current time is outside the window");
    }
}
