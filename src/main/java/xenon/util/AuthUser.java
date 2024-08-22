package xenon.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import xenon.service.security.UserDetailsImpl;

public class AuthUser {
    public static Integer getUserId() {
        Integer userId;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (authentication != null && authentication.isAuthenticated()) {
            userId = userDetails.getId();
        } else {
            userId = null;
        }
        return userId;
    }

    public static String getState() {
        String companyId;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (authentication != null && authentication.isAuthenticated()) {
            companyId = userDetails.getState();
        } else {
            companyId = "null";
        }
        return companyId;
    }

    public static String getRole() {
        String role;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (authentication != null && authentication.isAuthenticated()) {
            role = userDetails.getRole();
        } else {
            role = "null";
        }
        return role;
    }

    public static String getEmail() {
        String email;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (authentication != null && authentication.isAuthenticated()) {
            email = userDetails.getEmail();
        } else {
            email = "null";
        }
        return email;
    }

}
