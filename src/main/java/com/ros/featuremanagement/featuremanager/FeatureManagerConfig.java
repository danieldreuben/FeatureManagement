package com.ros.featuremanagement.featuremanager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.ros.featuremanagement.featuremanager.impl.InMemoryFeatureRepository;

@Configuration
public class FeatureManagerConfig {

    @Bean
    public FeatureRepository featureRepository() {
        FeatureRepository repo = new InMemoryFeatureRepository();
        return repo;
    }

    @Bean
    public FeatureManager featureManager(FeatureRepository repo) {
        FeatureManager fm = new FeatureManager(repo);
        return fm;
    }

    @Bean
    public TaskScheduler taskScheduler(FeatureManager featureManager) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        scheduler.setThreadNamePrefix("feature-refresh-");
        scheduler.initialize();
        scheduler.scheduleAtFixedRate(featureManager::refreshFeatures, java.time.Duration.ofMinutes(1));
        return scheduler;
    }
}

