package com.ros.featuremanagement.demo;

import com.ros.featuremanagement.featuremanager.impl.FeatureAppConfigProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static org.junit.jupiter.api.Assertions.*;

// TODO: Build out units (not covered by inmemory..)

@SpringBootTest(classes = AzureAppConfigFeatureRepositorySpringTest.TestConfig.class)
class AzureAppConfigFeatureRepositorySpringTest {

    @Autowired
    private FeatureAppConfigProperties props;

    @Test
    void testPropertiesAreLoadedFromYaml() {
        assertNotNull(props);
        assertEquals("https://fake-appconfig.azconfig.io", props.getEndpoint());
        assertEquals("dev", props.getLabel());
        assertTrue(props.getKeys().contains("Feature:SearchUI"));
        assertTrue(props.getKeys().contains("Feature:BetaMode"));
    }

    // Minimal configuration class for test
    @Configuration
    @EnableConfigurationProperties(FeatureAppConfigProperties.class)
    static class TestConfig {}
}



