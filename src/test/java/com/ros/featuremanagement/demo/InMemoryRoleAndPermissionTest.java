package com.ros.featuremanagement.demo;

import com.ros.featuremanagement.featuremanager.*;
import com.ros.featuremanagement.featuremanager.impl.InMemoryFeatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRoleAndPermissionTest {

    private FeatureManager fm;
    private InMemoryFeatureRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryFeatureRepository();

        // Feature requiring 1 role AND 1 permission
        repo.addFeature(new FeatureDefinition(
                "RoleAndPermissionFeature",
                true,
                List.of(new FilterConfig(
                        "RoleAndPermission",
                        Map.of(
                                "roles", List.of("admin", "manager"),
                                "permissions", List.of("invoice.read", "invoice.write")
                        )
                ))
        ));

        fm = new FeatureManager(
                repo,
                Map.of(
                        "RoleAndPermission", (ctx, params) -> {
                            @SuppressWarnings("unchecked")
                            List<String> roles = (List<String>) params.getOrDefault("roles", List.of());
                            @SuppressWarnings("unchecked")
                            List<String> permissions = (List<String>) params.getOrDefault("permissions", List.of());

                            boolean hasRole = roles.stream().anyMatch(ctx.getRoles()::contains);
                            boolean hasPermission = permissions.stream().anyMatch(ctx.getPermissions()::contains);

                            return hasRole && hasPermission;
                        }
                ),
                (ctx, params) -> false // default filter
        );
    }

    @Test
    void testRoleAndPermission_Positive() {
        FeatureContext ctx = new FeatureContext(
                "alice",
                List.of("admin"),               // has one required role
                List.of("invoice.write")        // has one required permission
        );

        boolean result = fm.isEnabled("RoleAndPermissionFeature", ctx);
        assertTrue(result, "User with 1 role and 1 permission should pass filter");
    }

    @Test
    void testRoleAndPermission_Negative_MissingPermission() {
        FeatureContext ctx = new FeatureContext(
                "bob",
                List.of("admin"),               // has a role
                List.of("invoice.delete")       // missing all required permissions
        );

        boolean result = fm.isEnabled("RoleAndPermissionFeature", ctx);
        assertFalse(result, "User missing required permission should fail filter");
    }

    @Test
    void testRoleAndPermission_Negative_MissingRole() {
        FeatureContext ctx = new FeatureContext(
                "charlie",
                List.of("user"),                // missing all required roles
                List.of("invoice.read")         // has a permission
        );

        boolean result = fm.isEnabled("RoleAndPermissionFeature", ctx);
        assertFalse(result, "User missing required role should fail filter");
    }

    @Test
    void testRoleAndPermission_Negative_MissingBoth() {
        FeatureContext ctx = new FeatureContext(
                "dave",
                List.of("guest"),               // missing required role
                List.of("invoice.delete")       // missing required permission
        );

        boolean result = fm.isEnabled("RoleAndPermissionFeature", ctx);
        assertFalse(result, "User missing both role and permission should fail filter");
    }

    @Test
    void testUnknownFeatureReturnsFalse() {
        FeatureContext ctx = new FeatureContext(
                "eve",
                List.of("admin"),
                List.of("invoice.read")
        );

        boolean result = fm.isEnabled("NoSuchFeature", ctx);
        assertFalse(result, "Unknown feature should return false");
    }
}
