package com.quickbite.restaurant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "restaurant-service",
                "message", "Restaurant Service is running on Self-Hosted Runner infrastructure"
        ));
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getRestaurants() {
        return ResponseEntity.ok(List.of(
                Map.of("id", 1, "name", "Pho Ha Noi", "cuisine", "Vietnamese", "rating", 4.8),
                Map.of("id", 2, "name", "Tokyo Sushi Bar", "cuisine", "Japanese", "rating", 4.9),
                Map.of("id", 3, "name", "Burger King", "cuisine", "Fast Food", "rating", 4.5)
        ));
    }
}
