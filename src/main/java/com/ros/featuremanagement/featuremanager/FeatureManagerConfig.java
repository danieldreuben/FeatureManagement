package com.ros.featuremanagement.featuremanager;

import com.ros.featuremanagement.featuremanager.impl.AzureAppConfigFeatureRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class FeatureManagerConfig {

    @Bean
    public FeatureRepository featureRepository() {
        return new AzureAppConfigFeatureRepository();
    }

    @Bean
    public FeatureManager featureManager(FeatureRepository repo) {
        Map<String, FeatureFilter> filters = new HashMap<>();

        filters.put("AlwaysOn", (ctx, params) -> true);
        filters.put("Percentage", (ctx, params) ->
                Math.random() < (double) params.getOrDefault("percentage", 0.0));
        filters.put("RoleBased", (ctx, params) -> {
            String requiredRole = (String) params.get("role");
            return ctx.getRoles().contains(requiredRole);
        });

        FeatureFilter defaultFilter = (ctx, params) -> false;

        return new FeatureManager(repo, filters, defaultFilter);
    }

    // optional scheduled refresh (every 5 minutes)
    @Bean
    public TaskScheduler taskScheduler(FeatureManager featureManager) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        scheduler.scheduleAtFixedRate(featureManager::refreshFeatures, 300_000);
        return scheduler;
    }
}

