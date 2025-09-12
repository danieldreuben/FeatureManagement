package com.ros.featuremanagement.featuremanager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.ros.featuremanagement.featuremanager.impl.InMemoryFeatureRepository;

/**
 * Spring configuration for the Feature Management system.
 *
 * <p>This configuration class provides the default beans for:
 * <ul>
 *     <li>{@link FeatureRepository} — by default, an {@link InMemoryFeatureRepository} is used.</li>
 *     <li>{@link FeatureManager} — configured with the default repository and default filters.</li>
 *     <li>{@link TaskScheduler} — periodically refreshes feature definitions from the repository.</li>
 * </ul>
 *
 * <p>The default in-memory repository is suitable for testing or lightweight
 * deployments. For production, a persistent repository implementation may
 * be substituted.
 *
 * <p>The refresh task scheduler ensures that any updates to the repository
 * (e.g., from external configuration changes) are automatically applied
 * to the FeatureManager at a fixed interval (currently every 1 minute).
 */
@Configuration
public class FeatureManagerConfig {

    /**
     * Provides the default {@link FeatureRepository} bean.
     *
     * <p>Uses {@link InMemoryFeatureRepository} as the default implementation.
     * This repository stores feature definitions in memory and does not
     * persist changes across application restarts.
     *
     * @return the in-memory feature repository
     */
    @Bean
    public FeatureRepository featureRepository() {
        FeatureRepository repo = new InMemoryFeatureRepository();
        return repo;
    }

    /**
     * Provides the {@link FeatureManager} bean configured with the repository.
     *
     * <p>The FeatureManager is responsible for evaluating feature flags
     * based on the configured repository and filters.
     *
     * @param repo the repository to be used by the FeatureManager
     * @return a configured FeatureManager instance
     */
    @Bean
    public FeatureManager featureManager(FeatureRepository repo) {
        FeatureManager fm = new FeatureManager(repo);
        return fm;
    }

    /**
     * Configures a {@link TaskScheduler} to periodically refresh features.
     *
     * <p>Uses a {@link ThreadPoolTaskScheduler} with a single thread,
     * which executes {@link FeatureManager#refreshFeatures()} every minute.
     * The scheduler thread is named with the prefix "feature-refresh-".
     *
     * <p>This ensures that changes in the repository (for example, updated
     * feature definitions) are automatically applied without restarting
     * the application.
     *
     * @param featureManager the FeatureManager whose features will be refreshed
     * @return a configured TaskScheduler
     */
    @Bean
    public TaskScheduler taskScheduler(FeatureManager featureManager) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("feature-refresh-");
        scheduler.initialize();
        scheduler.scheduleAtFixedRate(featureManager::refreshFeatures, java.time.Duration.ofMinutes(1));
        return scheduler;
    }
}
