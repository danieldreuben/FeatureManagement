package com.ros.featuremanagement.featuremanager;

import com.ros.featuremanagement.featuremanager.impl.AzureAppConfigFeatureRepository;
import com.ros.featuremanagement.featuremanager.impl.InMemoryFeatureRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class FeatureManagerConfig {

    @Bean
    public FeatureRepository featureRepository() {
        FeatureRepository repo = new InMemoryFeatureRepository();
        getDefaultFeatures(repo);
        return repo;
    }

    @Bean
    public FeatureManager getDefaultFeatures(FeatureRepository repo) {
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

		FeatureManager featureManager = new FeatureManager(repo, filters, defaultFilter);

        FeatureDefinition def = new FeatureDefinition("AdminFeature", true,
                List.of(new FilterConfig("RoleBased", Map.of("role", "admin"))));
        repo.addFeature(def);

        // Always on feature
        FeatureDefinition alwaysOn = new FeatureDefinition(
            "AlwaysOnFeature",    
            true,              
            List.of(new FilterConfig("AlwaysOn", Map.of())) 
        ); 
        repo.addFeature(alwaysOn);       

        // Always on feature
        FeatureDefinition disabledAlwaysOn = new FeatureDefinition(
            "DisabledFeature",    
            false,              
            List.of(new FilterConfig("AlwaysOn", Map.of())) 
        ); 
        repo.addFeature(disabledAlwaysOn);        

        System.out.println("setup " + featureManager);

        return featureManager;

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

