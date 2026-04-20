package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.DuplicateResourceException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.*;
import com.turno.crm.model.entity.Region;
import com.turno.crm.model.entity.User;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.DealRepository;
import com.turno.crm.repository.RegionRepository;
import com.turno.crm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final DealRepository dealRepository;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, RegionRepository regionRepository,
                       DealRepository dealRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.dealRepository = dealRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(UserRole role, UserStatus status) {
        List<User> users;
        if (role != null && status != null) {
            users = userRepository.findByRoleAndStatus(role, status);
        } else if (role != null) {
            users = userRepository.findByRole(role);
        } else if (status != null) {
            users = userRepository.findByStatus(status);
        } else {
            users = userRepository.findAll();
        }
        return users.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toResponse(user);
    }

    public UserResponse create(CreateUserRequest request, Long actorId) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        });

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);

        if (request.getRegionIds() != null && !request.getRegionIds().isEmpty()) {
            Set<Region> regions = new HashSet<>(regionRepository.findAllById(request.getRegionIds()));
            user.setRegions(regions);
        }

        user = userRepository.save(user);

        auditService.log(AuditEntityType.USER, user.getId(), AuditAction.CREATE, actorId,
                Map.of("email", user.getEmail(), "role", user.getRole().name()));

        return toResponse(user);
    }

    public UserResponse update(Long id, UpdateUserRequest request, Long actorId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        Map<String, Object> changes = new HashMap<>();

        if (request.getName() != null) {
            changes.put("name", Map.of("from", user.getName(), "to", request.getName()));
            user.setName(request.getName());
        }
        if (request.getRole() != null) {
            changes.put("role", Map.of("from", user.getRole().name(), "to", request.getRole().name()));
            user.setRole(request.getRole());
        }
        if (request.getRegionIds() != null) {
            Set<Region> regions = new HashSet<>(regionRepository.findAllById(request.getRegionIds()));
            user.setRegions(regions);
            changes.put("regionIds", request.getRegionIds());
        }

        user = userRepository.save(user);

        auditService.log(AuditEntityType.USER, user.getId(), AuditAction.UPDATE, actorId, changes);

        return toResponse(user);
    }

    public UserResponse deactivate(Long id, Long actorId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        long activeDeals = dealRepository.countByAssignedAgentIdAndStatus(id, DealStatus.ACTIVE);
        if (activeDeals > 0) {
            throw new BusinessRuleViolationException(
                    "Cannot deactivate user with " + activeDeals + " active deals. Reassign deals first.");
        }

        user.setStatus(UserStatus.DEACTIVATED);
        user = userRepository.save(user);

        auditService.log(AuditEntityType.USER, user.getId(), AuditAction.DEACTIVATE, actorId, Map.of());

        return toResponse(user);
    }

    public UserResponse reactivate(Long id, Long actorId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        user.setStatus(UserStatus.ACTIVE);
        user = userRepository.save(user);

        auditService.log(AuditEntityType.USER, user.getId(), AuditAction.REACTIVATE, actorId, Map.of());

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setEmail(user.getEmail());
        resp.setName(user.getName());
        resp.setRole(user.getRole());
        resp.setStatus(user.getStatus());
        resp.setRegions(user.getRegions().stream()
                .map(r -> new RegionResponse(r.getId(), r.getName(), r.getActive()))
                .collect(Collectors.toList()));
        resp.setActiveDeals(dealRepository.countByAssignedAgentIdAndStatus(user.getId(), DealStatus.ACTIVE));
        return resp;
    }
}
