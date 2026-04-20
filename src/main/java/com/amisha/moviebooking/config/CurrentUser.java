package com.amisha.moviebooking.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class CurrentUser {

    // Hardcoded userId mapping for demo — in prod users would be in DB with auto-increment IDs
    public static Long getId() {
        String username = getUsername();
        return switch (username) {
            case "customer1" -> 1001L;
            case "admin" -> 1L;
            default -> -1L;
        };
    }

    public static String getUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    public static String getEmail() {
        String username = getUsername();
        return switch (username) {
            case "customer1" -> "customer1@example.com";
            case "admin" -> "admin@example.com";
            default -> username + "@example.com";
        };
    }
}
