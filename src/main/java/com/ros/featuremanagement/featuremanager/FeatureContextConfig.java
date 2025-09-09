package com.ros.featuremanagement.featuremanager;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeatureContextConfig {

    @Bean
    @RequestScope
    public FeatureContext featureContext(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        List<String> roles = List.of(request.getHeader("X-Roles").split(","));
        List<String> permissions = List.of(request.getHeader("X-Permissions").split(","));
        return new FeatureContext(userId, roles, permissions);
    }
}
