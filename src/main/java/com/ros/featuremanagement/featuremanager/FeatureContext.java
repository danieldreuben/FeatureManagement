package com.ros.featuremanagement.featuremanager;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FeatureContext {
    private final String userId;
    private final List<String> roles;
    private final List<String> permissions;
    
    public FeatureContext(String userId, List<String> roles, List<String> permissions) {
        this.userId = userId;
        this.roles = roles != null ? roles : Collections.emptyList();
        this.permissions = permissions != null ? permissions : Collections.emptyList();
    }

    public FeatureContext(String userId, Set<String> roles) {
        this.userId = userId;
        this.roles = roles != null
                ? roles.stream().collect(Collectors.toList())
                : Collections.emptyList();
        this.permissions = Collections.emptyList();        
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getRoles() {
        return roles;
    }
    public List<String> getPermissions() {
        return permissions;
    }    
}
