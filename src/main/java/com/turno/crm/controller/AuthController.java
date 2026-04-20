package com.turno.crm.controller;

import com.turno.crm.model.dto.DevLoginRequest;
import com.turno.crm.model.dto.LoginResponse;
import com.turno.crm.model.dto.RegionResponse;
import com.turno.crm.model.dto.UserResponse;
import com.turno.crm.model.entity.Region;
import com.turno.crm.model.entity.User;
import com.turno.crm.model.enums.UserStatus;
import com.turno.crm.repository.UserRepository;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.security.JwtTokenProvider;
import com.turno.crm.security.TokenBlacklist;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CurrentUserProvider currentUserProvider;
    private final TokenBlacklist tokenBlacklist;

    public AuthController(UserRepository userRepository,
                          JwtTokenProvider jwtTokenProvider,
                          CurrentUserProvider currentUserProvider,
                          TokenBlacklist tokenBlacklist) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.currentUserProvider = currentUserProvider;
        this.tokenBlacklist = tokenBlacklist;
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> listUsersForLogin() {
        List<User> users = userRepository.findByStatus(UserStatus.ACTIVE);
        List<Map<String, Object>> result = users.stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "name", u.getName(),
                        "role", u.getRole().name()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Transactional(readOnly = true)
    @PostMapping("/dev-login")
    public ResponseEntity<LoginResponse> devLogin(@Valid @RequestBody DevLoginRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getUserId()));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("User is not active");
        }

        String token = jwtTokenProvider.generateToken(user);
        UserResponse userResponse = toUserResponse(user);

        return ResponseEntity.ok(new LoginResponse(token, userResponse));
    }

    @Transactional(readOnly = true)
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        // Reload from repository to ensure entity is attached to current session
        // (needed for lazy-loaded regions after EAGER->LAZY migration)
        Long userId = currentUserProvider.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            long expiryMs = jwtTokenProvider.getExpirationMsFromToken(token);
            tokenBlacklist.blacklist(token, expiryMs);
        }
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    private UserResponse toUserResponse(User user) {
        List<RegionResponse> regions = user.getRegions().stream()
                .map(this::toRegionResponse)
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getStatus(),
                regions
        );
    }

    private RegionResponse toRegionResponse(Region region) {
        return new RegionResponse(region.getId(), region.getName());
    }
}
