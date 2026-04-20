package com.turno.crm.security;

import com.turno.crm.model.entity.User;
import com.turno.crm.model.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public UserRole getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    public boolean isManager() {
        return getCurrentUserRole() == UserRole.MANAGER;
    }
}
