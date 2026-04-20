package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.model.dto.WebhookLeadRequest;
import com.turno.crm.model.dto.WebhookLeadResponse;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class WebhookService {

    private final DealRepository dealRepository;
    private final OperatorRepository operatorRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final DocumentChecklistItemRepository checklistItemRepository;
    private final DealDocumentRepository dealDocumentRepository;
    private final AuditService auditService;

    public WebhookService(DealRepository dealRepository,
                          OperatorRepository operatorRepository,
                          ContactRepository contactRepository,
                          UserRepository userRepository,
                          RegionRepository regionRepository,
                          DocumentChecklistItemRepository checklistItemRepository,
                          DealDocumentRepository dealDocumentRepository,
                          AuditService auditService) {
        this.dealRepository = dealRepository;
        this.operatorRepository = operatorRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.regionRepository = regionRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.dealDocumentRepository = dealDocumentRepository;
        this.auditService = auditService;
    }

    public WebhookLeadResponse processLead(WebhookLeadRequest request) {
        // Validate required fields
        if (!StringUtils.hasText(request.getContactPhone()) && !StringUtils.hasText(request.getContactEmail())) {
            throw new BusinessRuleViolationException("Either contactPhone or contactEmail is required");
        }

        // Idempotency check via sourceEventId
        if (StringUtils.hasText(request.getSourceEventId())) {
            Optional<Deal> existing = dealRepository.findBySourceEventId(request.getSourceEventId());
            if (existing.isPresent()) {
                Deal deal = existing.get();
                return new WebhookLeadResponse(deal.getId(), deal.getOperator().getId(), false);
            }
        }

        // Find a manager user to assign the deal to
        List<User> managers = userRepository.findByRoleAndStatus(UserRole.MANAGER, UserStatus.ACTIVE);
        if (managers.isEmpty()) {
            throw new BusinessRuleViolationException("No active manager found to assign the deal");
        }
        User assignee = managers.get(0);

        // Duplicate operator detection by name + phone
        boolean duplicateOperator = false;
        Operator operator;
        Optional<Operator> existingOp = operatorRepository.findByCompanyNameAndPhone(
                request.getOperatorName(), request.getContactPhone());

        if (existingOp.isPresent()) {
            operator = existingOp.get();
            duplicateOperator = true;
        } else {
            operator = new Operator();
            operator.setCompanyName(request.getOperatorName());
            operator.setPhone(request.getContactPhone());
            operator.setEmail(request.getContactEmail());
            operator.setCreatedBy(assignee);

            if (request.getFleetSize() != null) {
                operator.setFleetSize(request.getFleetSize());
            }

            if (StringUtils.hasText(request.getOperatorType())) {
                try {
                    operator.setOperatorType(OperatorType.valueOf(request.getOperatorType()));
                } catch (IllegalArgumentException ignored) {
                    // Skip invalid operator type
                }
            }

            if (StringUtils.hasText(request.getPrimaryUseCase())) {
                try {
                    operator.setPrimaryUseCase(PrimaryUseCase.valueOf(request.getPrimaryUseCase()));
                } catch (IllegalArgumentException ignored) {
                    // Skip invalid primary use case
                }
            }

            if (StringUtils.hasText(request.getRegion())) {
                regionRepository.findByActiveTrue().stream()
                        .filter(r -> r.getName().equalsIgnoreCase(request.getRegion()))
                        .findFirst()
                        .ifPresent(operator::setRegion);
            }

            operator = operatorRepository.save(operator);
        }

        // Create contact for the operator
        Contact contact = new Contact();
        contact.setOperator(operator);
        contact.setName(request.getContactName());
        contact.setRole(ContactRole.OWNER);
        contact.setMobile(request.getContactPhone());
        contact.setEmail(request.getContactEmail());
        contactRepository.save(contact);

        // Parse lead source
        LeadSource leadSource;
        try {
            leadSource = LeadSource.valueOf(request.getLeadSource());
        } catch (IllegalArgumentException e) {
            leadSource = LeadSource.INBOUND;
        }

        // Create the deal
        Deal deal = new Deal();
        deal.setName(request.getOperatorName() + " - " + System.currentTimeMillis());
        deal.setOperator(operator);
        deal.setAssignedAgent(assignee);
        deal.setLeadSource(leadSource);
        deal.setCurrentStage(DealStage.STAGE_1);
        deal.setStatus(DealStatus.ACTIVE);
        deal.setFleetSize(request.getFleetSize());

        if (StringUtils.hasText(request.getSourceEventId())) {
            deal.setSourceEventId(request.getSourceEventId());
        }

        deal = dealRepository.save(deal);

        // Auto-create deal_documents
        List<DocumentChecklistItem> activeItems = checklistItemRepository.findByActiveTrue();
        for (DocumentChecklistItem item : activeItems) {
            DealDocument doc = new DealDocument();
            doc.setDeal(deal);
            doc.setChecklistItem(item);
            doc.setStatus(DocStatus.NOT_STARTED);
            dealDocumentRepository.save(doc);
        }

        auditService.log(AuditEntityType.DEAL, deal.getId(), AuditAction.CREATE, assignee.getId(),
                Map.of("source", "webhook", "operatorName", request.getOperatorName(),
                        "duplicateOperator", duplicateOperator));

        return new WebhookLeadResponse(deal.getId(), operator.getId(), duplicateOperator);
    }
}
