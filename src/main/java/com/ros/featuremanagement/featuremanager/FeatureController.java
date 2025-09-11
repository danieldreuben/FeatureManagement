package com.ros.featuremanagement.featuremanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ros.featuremanagement.featuremanager.impl.InMemoryFeatureRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/features")
public class FeatureController {

    private  FeatureManager featureManager;
    @Autowired
    FeatureContext fc;

    public FeatureController() {
        InMemoryFeatureRepository repo = new InMemoryFeatureRepository();
        featureManager = new FeatureManagerConfig().getDefaultFeatures(repo);
    }

    /**
     * GET /features/{name}
     * Check if a single feature is enabled for the current user context.
     * note: user context is injected from FeatureContextConfig.
     */
    @GetMapping("/{name}")
    public boolean isFeatureEnabled(
            @PathVariable String name) {
        //System.out.println("user: " + fc.getUserId());
        return featureManager.isEnabled(name, fc);
    }

    /**
     * GET /features/{name}
     * Check if a single feature is enabled for the current user context.
     * TODO: RequestHeaders or request context bean (see: FeatureContextConfig)?
     */
    /*@GetMapping("/{name}")
    public boolean isFeatureEnabled(
            @PathVariable String name,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-Roles", required = false) String rolesHeader,
            @RequestHeader(value = "X-Permissions", required = false) String permissionsHeader) {

        List<String> roles = parseHeader(rolesHeader);
        List<String> permissions = parseHeader(permissionsHeader);
        FeatureContext ctx = new FeatureContext(userId, roles, permissions);
        return featureManager.isEnabled(name, ctx);
    }*/

    /**
     * GET /features
     * Return all features and their enabled state for the current user context.
     */
    @GetMapping
    public Map<String, Boolean> getAllFeatures(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-Roles", required = false) String rolesHeader,
            @RequestHeader(value = "X-Permissions", required = false) String permissionsHeader) {

              
        List<String> roles = parseHeader(rolesHeader);
        List<String> permissions = parseHeader(permissionsHeader);

        System.out.println("getAllFeatures() " + roles + " permissions " + permissions);  

        FeatureContext ctx = new FeatureContext(userId, roles, permissions);

        return featureManager.getAllFeatures(ctx).keySet().stream()
                .collect(Collectors.toMap(
                        featureName -> featureName,
                        featureName -> featureManager.isEnabled(featureName, ctx)
                ));
    }

    private List<String> parseHeader(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return List.of();
        }
        return Arrays.stream(headerValue.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

