package com.turno.crm.service;

import com.turno.crm.exception.DuplicateResourceException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.*;
import com.turno.crm.model.entity.Operator;
import com.turno.crm.model.entity.Region;
import com.turno.crm.model.entity.User;
import com.turno.crm.model.enums.AuditAction;
import com.turno.crm.model.enums.AuditEntityType;
import com.turno.crm.repository.DealRepository;
import com.turno.crm.repository.OperatorRepository;
import com.turno.crm.repository.RegionRepository;
import com.turno.crm.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OperatorService {

    private final OperatorRepository operatorRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;
    private final DealRepository dealRepository;
    private final AuditService auditService;

    public OperatorService(OperatorRepository operatorRepository, RegionRepository regionRepository,
                           UserRepository userRepository, DealRepository dealRepository,
                           AuditService auditService) {
        this.operatorRepository = operatorRepository;
        this.regionRepository = regionRepository;
        this.userRepository = userRepository;
        this.dealRepository = dealRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<OperatorResponse> list(String search, Long regionId, Pageable pageable) {
        Specification<Operator> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("companyName")), pattern),
                    cb.like(cb.lower(root.get("phone")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            ));
        }

        if (regionId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("region").get("id"), regionId));
        }

        return operatorRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OperatorResponse getById(Long id) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));
        return toResponse(operator);
    }

    public OperatorResponse create(CreateOperatorRequest request, Long actorId) {
        operatorRepository.findByCompanyNameAndPhone(request.getCompanyName(), request.getPhone())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Operator with company name '" + request.getCompanyName() + "' and phone '" + request.getPhone() + "' already exists");
                });

        Operator operator = new Operator();
        operator.setCompanyName(request.getCompanyName());
        operator.setPhone(request.getPhone());
        operator.setEmail(request.getEmail());
        operator.setOperatorType(request.getOperatorType());
        operator.setReferralSource(request.getReferralSource());
        operator.setFleetSize(request.getFleetSize());
        operator.setNumRoutes(request.getNumRoutes());
        operator.setPrimaryUseCase(request.getPrimaryUseCase());

        if (request.getRegionId() != null) {
            Region region = regionRepository.findById(request.getRegionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Region", request.getRegionId()));
            operator.setRegion(region);
        }

        if (request.getReferredByOperatorId() != null) {
            Operator referrer = operatorRepository.findById(request.getReferredByOperatorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Operator", request.getReferredByOperatorId()));
            operator.setReferredByOperator(referrer);
        }

        User creator = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));
        operator.setCreatedBy(creator);

        operator = operatorRepository.save(operator);

        auditService.log(AuditEntityType.OPERATOR, operator.getId(), AuditAction.CREATE, actorId,
                Map.of("companyName", operator.getCompanyName()));

        return toResponse(operator);
    }

    public OperatorResponse update(Long id, UpdateOperatorRequest request, Long actorId) {
        Operator operator = operatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", id));

        Map<String, Object> changes = new HashMap<>();

        if (request.getCompanyName() != null) {
            changes.put("companyName", Map.of("from", operator.getCompanyName(), "to", request.getCompanyName()));
            operator.setCompanyName(request.getCompanyName());
        }
        if (request.getPhone() != null) {
            changes.put("phone", Map.of("from", String.valueOf(operator.getPhone()), "to", request.getPhone()));
            operator.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            changes.put("email", Map.of("from", String.valueOf(operator.getEmail()), "to", request.getEmail()));
            operator.setEmail(request.getEmail());
        }
        if (request.getOperatorType() != null) {
            operator.setOperatorType(request.getOperatorType());
            changes.put("operatorType", request.getOperatorType().name());
        }
        if (request.getReferralSource() != null) {
            operator.setReferralSource(request.getReferralSource());
            changes.put("referralSource", request.getReferralSource());
        }
        if (request.getFleetSize() != null) {
            operator.setFleetSize(request.getFleetSize());
            changes.put("fleetSize", request.getFleetSize());
        }
        if (request.getNumRoutes() != null) {
            operator.setNumRoutes(request.getNumRoutes());
            changes.put("numRoutes", request.getNumRoutes());
        }
        if (request.getPrimaryUseCase() != null) {
            operator.setPrimaryUseCase(request.getPrimaryUseCase());
            changes.put("primaryUseCase", request.getPrimaryUseCase().name());
        }
        if (request.getRegionId() != null) {
            Region region = regionRepository.findById(request.getRegionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Region", request.getRegionId()));
            operator.setRegion(region);
            changes.put("regionId", request.getRegionId());
        }
        if (request.getReferredByOperatorId() != null) {
            Operator referrer = operatorRepository.findById(request.getReferredByOperatorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Operator", request.getReferredByOperatorId()));
            operator.setReferredByOperator(referrer);
            changes.put("referredByOperatorId", request.getReferredByOperatorId());
        }

        operator = operatorRepository.save(operator);

        auditService.log(AuditEntityType.OPERATOR, operator.getId(), AuditAction.UPDATE, actorId, changes);

        return toResponse(operator);
    }

    @Transactional(readOnly = true)
    public List<OperatorResponse> search(String query) {
        return operatorRepository.search(query).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private OperatorResponse toResponse(Operator operator) {
        OperatorResponse resp = new OperatorResponse();
        resp.setId(operator.getId());
        resp.setCompanyName(operator.getCompanyName());
        resp.setPhone(operator.getPhone());
        resp.setEmail(operator.getEmail());
        resp.setOperatorType(operator.getOperatorType());
        resp.setReferralSource(operator.getReferralSource());
        resp.setFleetSize(operator.getFleetSize());
        resp.setNumRoutes(operator.getNumRoutes());
        resp.setPrimaryUseCase(operator.getPrimaryUseCase());
        resp.setCreatedAt(operator.getCreatedAt());

        if (operator.getRegion() != null) {
            resp.setRegion(new RegionResponse(
                    operator.getRegion().getId(),
                    operator.getRegion().getName(),
                    operator.getRegion().getActive()));
        }

        resp.setContacts(operator.getContacts().stream().map(c -> {
            ContactResponse cr = new ContactResponse();
            cr.setId(c.getId());
            cr.setName(c.getName());
            cr.setRole(c.getRole());
            cr.setMobile(c.getMobile());
            cr.setEmail(c.getEmail());
            cr.setIncomplete((c.getMobile() == null || c.getMobile().isBlank())
                    || (c.getEmail() == null || c.getEmail().isBlank()));
            return cr;
        }).collect(Collectors.toList()));

        // Count deals for this operator using specification
        Specification<com.turno.crm.model.entity.Deal> dealSpec =
                (root, query, cb) -> cb.equal(root.get("operator").get("id"), operator.getId());
        resp.setDealCount((int) dealRepository.count(dealSpec));

        return resp;
    }
}
