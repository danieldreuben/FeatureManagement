package com.ros.featuremanagement.featuremanager;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import jakarta.servlet.http.HttpServletRequest;
/**
 * Spring configuration class that provides request-scoped beans
 * for feature management and authorization context.
 *
 * <p>This configuration extracts user-specific information
 * (user ID, roles, and permissions) from HTTP request headers and
 * builds a {@link FeatureContext} object. The resulting context
 * can be injected into downstream services, controllers, or filters
 * to support feature flag evaluation, authorization checks, or
 * role-based logic.
 *
 * <p>The bean is declared with {@link RequestScope}, ensuring that
 * a unique {@link FeatureContext} instance is created for each
 * HTTP request and disposed of when the request completes.
 *
 * <p>Expected request headers:
 * <ul>
 *   <li><b>X-User-Id</b> - unique identifier of the user</li>
 *   <li><b>X-Roles</b> - comma-separated list of user roles</li>
 *   <li><b>X-Permissions</b> - comma-separated list of user permissions</li>
 * </ul>
 */
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
