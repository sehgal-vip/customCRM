package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.DuplicateResourceException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.*;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final TaxonomyItemRepository taxonomyItemRepository;
    private final AdminSettingRepository adminSettingRepository;
    private final DocumentChecklistItemRepository checklistItemRepository;
    private final DealDocumentRepository dealDocumentRepository;
    private final DealRepository dealRepository;
    private final RegionRepository regionRepository;
    private final WebhookApiKeyRepository webhookApiKeyRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AdminService(TaxonomyItemRepository taxonomyItemRepository,
                        AdminSettingRepository adminSettingRepository,
                        DocumentChecklistItemRepository checklistItemRepository,
                        DealDocumentRepository dealDocumentRepository,
                        DealRepository dealRepository,
                        RegionRepository regionRepository,
                        WebhookApiKeyRepository webhookApiKeyRepository,
                        NotificationPreferenceRepository notificationPreferenceRepository,
                        UserRepository userRepository,
                        AuditService auditService) {
        this.taxonomyItemRepository = taxonomyItemRepository;
        this.adminSettingRepository = adminSettingRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.dealDocumentRepository = dealDocumentRepository;
        this.dealRepository = dealRepository;
        this.regionRepository = regionRepository;
        this.webhookApiKeyRepository = webhookApiKeyRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    // ─── Taxonomy CRUD ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TaxonomyItemResponse> listTaxonomy(TaxonomyType type) {
        List<TaxonomyItem> items = taxonomyItemRepository.findByTaxonomyType(type);
        // Active items first, then alphabetical
        items.sort(Comparator.comparing((TaxonomyItem i) -> !i.getActive())
                .thenComparing(TaxonomyItem::getValue));
        return items.stream().map(this::toTaxonomyResponse).collect(Collectors.toList());
    }

    public TaxonomyItemResponse addTaxonomy(TaxonomyType type, CreateTaxonomyRequest request) {
        taxonomyItemRepository.findByTaxonomyTypeAndValue(type, request.getValue()).ifPresent(existing -> {
            throw new DuplicateResourceException("Taxonomy item '" + request.getValue() + "' already exists for type " + type);
        });

        TaxonomyItem item = new TaxonomyItem();
        item.setTaxonomyType(type);
        item.setValue(request.getValue());
        item.setActive(true);
        item = taxonomyItemRepository.save(item);
        return toTaxonomyResponse(item);
    }

    public TaxonomyItemResponse updateTaxonomy(TaxonomyType type, Long id, UpdateTaxonomyRequest request) {
        TaxonomyItem item = taxonomyItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaxonomyItem", id));

        if (!item.getTaxonomyType().equals(type)) {
            throw new BusinessRuleViolationException("Taxonomy item does not belong to type " + type);
        }

        if (request.getValue() != null) {
            taxonomyItemRepository.findByTaxonomyTypeAndValue(type, request.getValue()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("Taxonomy item '" + request.getValue() + "' already exists for type " + type);
                }
            });
            item.setValue(request.getValue());
        }

        if (request.getActive() != null) {
            item.setActive(request.getActive());
        }

        item = taxonomyItemRepository.save(item);
        return toTaxonomyResponse(item);
    }

    private TaxonomyItemResponse toTaxonomyResponse(TaxonomyItem item) {
        return new TaxonomyItemResponse(item.getId(), item.getTaxonomyType(), item.getValue(),
                item.getActive() != null && item.getActive());
    }

    // ─── Stale Thresholds ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StaleThresholdResponse> getStaleThresholds() {
        List<AdminSetting> settings = adminSettingRepository.findBySettingType(AdminSettingType.STALE_THRESHOLD);
        List<StaleThresholdResponse> result = new ArrayList<>();

        for (DealStage stage : DealStage.values()) {
            int defaultDays = 14;
            for (AdminSetting setting : settings) {
                if (setting.getSettingKey().equals(stage.name())) {
                    Object val = setting.getSettingValue().get("days");
                    if (val instanceof Number) {
                        defaultDays = ((Number) val).intValue();
                    }
                    break;
                }
            }
            result.add(new StaleThresholdResponse(stage.name(), defaultDays));
        }
        return result;
    }

    public List<StaleThresholdResponse> updateStaleThresholds(UpdateStaleThresholdsRequest request, Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));

        for (Map.Entry<String, Integer> entry : request.getThresholds().entrySet()) {
            String stageName = entry.getKey();
            int days = entry.getValue();

            // Validate stage name
            try {
                DealStage.valueOf(stageName);
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleViolationException("Invalid stage name: " + stageName);
            }

            AdminSetting setting = adminSettingRepository
                    .findBySettingTypeAndSettingKey(AdminSettingType.STALE_THRESHOLD, stageName)
                    .orElseGet(() -> {
                        AdminSetting s = new AdminSetting();
                        s.setSettingType(AdminSettingType.STALE_THRESHOLD);
                        s.setSettingKey(stageName);
                        return s;
                    });

            setting.setSettingValue(Map.of("days", days));
            setting.setUpdatedBy(actor);
            adminSettingRepository.save(setting);
        }

        auditService.log(AuditEntityType.DEAL, actorId, AuditAction.UPDATE, actorId,
                Map.of("action", "updateStaleThresholds", "thresholds", request.getThresholds()));

        return getStaleThresholds();
    }

    // ─── Exit Criteria ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ExitCriteriaResponse> getExitCriteria() {
        List<AdminSetting> settings = adminSettingRepository.findBySettingType(AdminSettingType.EXIT_CRITERIA);
        List<ExitCriteriaResponse> result = new ArrayList<>();

        for (DealStage stage : DealStage.values()) {
            boolean activityRequired = true; // default
            for (AdminSetting setting : settings) {
                if (setting.getSettingKey().equals(stage.name())) {
                    Object val = setting.getSettingValue().get("activityRequired");
                    if (val instanceof Boolean) {
                        activityRequired = (Boolean) val;
                    }
                    break;
                }
            }
            result.add(new ExitCriteriaResponse(stage.name(), activityRequired));
        }
        return result;
    }

    public List<ExitCriteriaResponse> updateExitCriteria(UpdateExitCriteriaRequest request, Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));

        for (Map.Entry<String, Boolean> entry : request.getCriteria().entrySet()) {
            String stageName = entry.getKey();
            boolean activityRequired = entry.getValue();

            try {
                DealStage.valueOf(stageName);
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleViolationException("Invalid stage name: " + stageName);
            }

            AdminSetting setting = adminSettingRepository
                    .findBySettingTypeAndSettingKey(AdminSettingType.EXIT_CRITERIA, stageName)
                    .orElseGet(() -> {
                        AdminSetting s = new AdminSetting();
                        s.setSettingType(AdminSettingType.EXIT_CRITERIA);
                        s.setSettingKey(stageName);
                        return s;
                    });

            setting.setSettingValue(Map.of("activityRequired", activityRequired));
            setting.setUpdatedBy(actor);
            adminSettingRepository.save(setting);
        }

        auditService.log(AuditEntityType.DEAL, actorId, AuditAction.UPDATE, actorId,
                Map.of("action", "updateExitCriteria", "criteria", request.getCriteria()));

        return getExitCriteria();
    }

    // ─── Document Checklist Config ──────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ChecklistItemResponse> listChecklistItems() {
        return checklistItemRepository.findAll().stream()
                .map(this::toChecklistResponse)
                .collect(Collectors.toList());
    }

    public ChecklistItemResponse addChecklistItem(CreateChecklistItemRequest request, Long actorId) {
        DocumentChecklistItem item = new DocumentChecklistItem();
        item.setDocumentName(request.getDocumentName());
        item.setRequirement(DocRequirement.valueOf(request.getRequirement()));
        item.setRequiredByStage(DealStage.valueOf(request.getRequiredByStage()));
        item.setActive(true);
        item = checklistItemRepository.save(item);

        // Auto-create deal_documents for all active deals at or before that stage
        DealStage requiredByStage = item.getRequiredByStage();
        List<Deal> activeDeals = dealRepository.findByStatus(DealStatus.ACTIVE, org.springframework.data.domain.Pageable.unpaged()).getContent();
        for (Deal deal : activeDeals) {
            if (deal.getCurrentStage().ordinal() <= requiredByStage.ordinal()) {
                DealDocument doc = new DealDocument();
                doc.setDeal(deal);
                doc.setChecklistItem(item);
                doc.setStatus(DocStatus.NOT_STARTED);
                dealDocumentRepository.save(doc);
            }
        }

        auditService.log(AuditEntityType.DEAL, item.getId(), AuditAction.CREATE, actorId,
                Map.of("action", "addChecklistItem", "documentName", item.getDocumentName()));

        return toChecklistResponse(item);
    }

    public ChecklistItemResponse updateChecklistItem(Long id, UpdateChecklistItemRequest request, Long actorId) {
        DocumentChecklistItem item = checklistItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentChecklistItem", id));

        if (request.getDocumentName() != null) {
            item.setDocumentName(request.getDocumentName());
        }
        if (request.getRequirement() != null) {
            item.setRequirement(DocRequirement.valueOf(request.getRequirement()));
        }
        if (request.getRequiredByStage() != null) {
            item.setRequiredByStage(DealStage.valueOf(request.getRequiredByStage()));
        }
        if (request.getActive() != null) {
            item.setActive(request.getActive());
        }

        item = checklistItemRepository.save(item);

        auditService.log(AuditEntityType.DEAL, id, AuditAction.UPDATE, actorId,
                Map.of("action", "updateChecklistItem", "id", id));

        return toChecklistResponse(item);
    }

    private ChecklistItemResponse toChecklistResponse(DocumentChecklistItem item) {
        ChecklistItemResponse resp = new ChecklistItemResponse();
        resp.setId(item.getId());
        resp.setDocumentName(item.getDocumentName());
        resp.setRequirement(item.getRequirement().name());
        resp.setRequiredByStage(item.getRequiredByStage().name());
        resp.setActive(item.getActive() != null && item.getActive());
        return resp;
    }

    // ─── Region Management ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RegionResponse> listRegions() {
        return regionRepository.findAll().stream()
                .map(r -> new RegionResponse(r.getId(), r.getName(), r.getActive() != null && r.getActive()))
                .collect(Collectors.toList());
    }

    public RegionResponse addRegion(CreateRegionRequest request) {
        Region region = new Region();
        region.setName(request.getName());
        region.setActive(true);
        region = regionRepository.save(region);
        return new RegionResponse(region.getId(), region.getName(), true);
    }

    public RegionResponse updateRegion(Long id, UpdateRegionRequest request) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Region", id));

        if (request.getName() != null) {
            region.setName(request.getName());
        }
        if (request.getActive() != null) {
            region.setActive(request.getActive());
        }
        region = regionRepository.save(region);
        return new RegionResponse(region.getId(), region.getName(), region.getActive() != null && region.getActive());
    }

    // ─── Webhook API Keys ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<WebhookKeyResponse> listKeys() {
        return webhookApiKeyRepository.findAll().stream().map(key -> {
            WebhookKeyResponse resp = new WebhookKeyResponse();
            resp.setId(key.getId());
            // Show only first 8 chars of hash as prefix indicator
            String hash = key.getKeyHash();
            resp.setKeyPrefix(hash != null && hash.length() >= 8 ? hash.substring(0, 8) + "..." : "");
            resp.setDescription(key.getDescription());
            resp.setActive(key.getActive() != null && key.getActive());
            resp.setCreatedAt(key.getCreatedAt());
            resp.setRevokedAt(key.getRevokedAt());
            return resp;
        }).collect(Collectors.toList());
    }

    public GenerateKeyResponse generateKey(GenerateKeyRequest request, Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));

        // Generate random API key
        String plainKey = generateRandomKey();
        String keyHash = sha256(plainKey);

        WebhookApiKey apiKey = new WebhookApiKey();
        apiKey.setKeyHash(keyHash);
        apiKey.setDescription(request.getDescription());
        apiKey.setCreatedBy(actor);
        apiKey.setActive(true);
        apiKey = webhookApiKeyRepository.save(apiKey);

        auditService.log(AuditEntityType.ADMIN_SETTING, apiKey.getId(), AuditAction.CREATE, actorId,
                Map.of("action", "generateWebhookKey", "description", request.getDescription()));

        return new GenerateKeyResponse(apiKey.getId(), plainKey, apiKey.getDescription());
    }

    public void revokeKey(Long id, Long actorId) {
        WebhookApiKey key = webhookApiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WebhookApiKey", id));

        key.setActive(false);
        key.setRevokedAt(OffsetDateTime.now());
        webhookApiKeyRepository.save(key);

        auditService.log(AuditEntityType.DEAL, id, AuditAction.DEACTIVATE, actorId,
                Map.of("action", "revokeWebhookKey", "id", id));
    }

    private String generateRandomKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder("tcrm_");
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ─── Notification Preferences ───────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NotificationPrefResponse> getPreferences() {
        List<NotificationPreference> all = notificationPreferenceRepository.findAll();
        return all.stream()
                .map(p -> new NotificationPrefResponse(p.getRole(), p.getEventType(),
                        p.getEnabled() != null && p.getEnabled()))
                .collect(Collectors.toList());
    }

    public List<NotificationPrefResponse> updatePreferences(UpdateNotificationPrefsRequest request, Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));

        for (UpdateNotificationPrefsRequest.PrefItem item : request.getPreferences()) {
            NotificationPreference pref = notificationPreferenceRepository
                    .findByRoleAndEventType(item.getRole(), item.getEventType())
                    .orElseGet(() -> {
                        NotificationPreference np = new NotificationPreference();
                        np.setRole(item.getRole());
                        np.setEventType(item.getEventType());
                        return np;
                    });

            pref.setEnabled(item.isEnabled());
            pref.setUpdatedBy(actor);
            notificationPreferenceRepository.save(pref);
        }

        return getPreferences();
    }
}
