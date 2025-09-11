package com.ros.featuremanagement.featuremanager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootApplication
public class FeatureApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeatureApplication.class, args);
    }

   @Bean
    public CommandLineRunner demoRestCall() {
        return args -> {
            RestTemplate rest = new RestTemplate();

            // Build headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", "bob");
            headers.set("X-Roles", String.join(",", List.of("admin")));
            headers.set("X-Permissions", String.join(",", List.of("invoice.read","admin")));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Call single feature
            ResponseEntity<Boolean> response = rest.exchange(
                "http://localhost:8080/features/AdminRoleFeature",
                HttpMethod.GET,
                entity,
                Boolean.class
            );

            System.out.println("RoleAndPermissionFeature enabled? " + response.getBody());

            // Call all features
            ResponseEntity<String> allResponse = rest.exchange(
                "http://localhost:8080/features",
                HttpMethod.GET,
                entity,
                String.class
            );

            System.out.println("All features: " + allResponse.getBody());
        };
    }

}
