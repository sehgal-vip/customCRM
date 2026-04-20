package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.DuplicateResourceException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.*;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private TaxonomyItemRepository taxonomyItemRepository;
    @Mock private AdminSettingRepository adminSettingRepository;
    @Mock private DocumentChecklistItemRepository checklistItemRepository;
    @Mock private DealDocumentRepository dealDocumentRepository;
    @Mock private DealRepository dealRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private WebhookApiKeyRepository webhookApiKeyRepository;
    @Mock private NotificationPreferenceRepository notificationPreferenceRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private AdminService service;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setName("Admin");
    }

    // === Taxonomy tests ===

    @Test
    void addTaxonomy_succeeds() {
        when(taxonomyItemRepository.findByTaxonomyTypeAndValue(TaxonomyType.OBJECTION, "Price"))
                .thenReturn(Optional.empty());
        when(taxonomyItemRepository.save(any())).thenAnswer(inv -> {
            TaxonomyItem item = inv.getArgument(0);
            item.setId(1L);
            return item;
        });

        CreateTaxonomyRequest req = new CreateTaxonomyRequest();
        req.setValue("Price");

        TaxonomyItemResponse resp = service.addTaxonomy(TaxonomyType.OBJECTION, req);

        assertEquals("Price", resp.getValue());
        assertTrue(resp.isActive());
    }

    @Test
    void addTaxonomy_duplicateValueRejected() {
        TaxonomyItem existing = new TaxonomyItem();
        existing.setId(1L);
        when(taxonomyItemRepository.findByTaxonomyTypeAndValue(TaxonomyType.OBJECTION, "Price"))
                .thenReturn(Optional.of(existing));

        CreateTaxonomyRequest req = new CreateTaxonomyRequest();
        req.setValue("Price");

        assertThrows(DuplicateResourceException.class, () ->
                service.addTaxonomy(TaxonomyType.OBJECTION, req));
    }

    @Test
    void updateTaxonomy_rename_succeeds() {
        TaxonomyItem item = new TaxonomyItem();
        item.setId(1L);
        item.setTaxonomyType(TaxonomyType.OBJECTION);
        item.setValue("Price");
        item.setActive(true);

        when(taxonomyItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(taxonomyItemRepository.findByTaxonomyTypeAndValue(TaxonomyType.OBJECTION, "Cost"))
                .thenReturn(Optional.empty());
        when(taxonomyItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateTaxonomyRequest req = new UpdateTaxonomyRequest();
        req.setValue("Cost");

        TaxonomyItemResponse resp = service.updateTaxonomy(TaxonomyType.OBJECTION, 1L, req);

        assertEquals("Cost", resp.getValue());
    }

    @Test
    void updateTaxonomy_archive_succeeds() {
        TaxonomyItem item = new TaxonomyItem();
        item.setId(1L);
        item.setTaxonomyType(TaxonomyType.OBJECTION);
        item.setValue("Price");
        item.setActive(true);

        when(taxonomyItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(taxonomyItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateTaxonomyRequest req = new UpdateTaxonomyRequest();
        req.setActive(false);

        TaxonomyItemResponse resp = service.updateTaxonomy(TaxonomyType.OBJECTION, 1L, req);

        assertFalse(resp.isActive());
    }

    @Test
    void updateTaxonomy_duplicateRenameRejected() {
        TaxonomyItem item = new TaxonomyItem();
        item.setId(1L);
        item.setTaxonomyType(TaxonomyType.OBJECTION);
        item.setValue("Price");

        TaxonomyItem other = new TaxonomyItem();
        other.setId(2L);

        when(taxonomyItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(taxonomyItemRepository.findByTaxonomyTypeAndValue(TaxonomyType.OBJECTION, "Existing"))
                .thenReturn(Optional.of(other));

        UpdateTaxonomyRequest req = new UpdateTaxonomyRequest();
        req.setValue("Existing");

        assertThrows(DuplicateResourceException.class, () ->
                service.updateTaxonomy(TaxonomyType.OBJECTION, 1L, req));
    }

    @Test
    void updateTaxonomy_wrongType_throws() {
        TaxonomyItem item = new TaxonomyItem();
        item.setId(1L);
        item.setTaxonomyType(TaxonomyType.BUYING_SIGNAL);

        when(taxonomyItemRepository.findById(1L)).thenReturn(Optional.of(item));

        UpdateTaxonomyRequest req = new UpdateTaxonomyRequest();
        req.setValue("test");

        assertThrows(BusinessRuleViolationException.class, () ->
                service.updateTaxonomy(TaxonomyType.OBJECTION, 1L, req));
    }

    @Test
    void listTaxonomy_returnsItems() {
        TaxonomyItem item1 = new TaxonomyItem();
        item1.setId(1L);
        item1.setTaxonomyType(TaxonomyType.OBJECTION);
        item1.setValue("Alpha");
        item1.setActive(true);

        TaxonomyItem item2 = new TaxonomyItem();
        item2.setId(2L);
        item2.setTaxonomyType(TaxonomyType.OBJECTION);
        item2.setValue("Beta");
        item2.setActive(false);

        when(taxonomyItemRepository.findByTaxonomyType(TaxonomyType.OBJECTION))
                .thenReturn(new ArrayList<>(List.of(item1, item2)));

        List<TaxonomyItemResponse> result = service.listTaxonomy(TaxonomyType.OBJECTION);

        assertEquals(2, result.size());
        // Active items first
        assertTrue(result.get(0).isActive());
    }

    // === Stale thresholds ===

    @Test
    void getStaleThresholds_returnsAllStages() {
        when(adminSettingRepository.findBySettingType(AdminSettingType.STALE_THRESHOLD))
                .thenReturn(Collections.emptyList());

        List<StaleThresholdResponse> result = service.getStaleThresholds();

        assertEquals(DealStage.values().length, result.size());
    }

    @Test
    void updateStaleThresholds_updatesValues() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(adminSettingRepository.findBySettingTypeAndSettingKey(eq(AdminSettingType.STALE_THRESHOLD), anyString()))
                .thenReturn(Optional.empty());
        when(adminSettingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(adminSettingRepository.findBySettingType(AdminSettingType.STALE_THRESHOLD))
                .thenReturn(Collections.emptyList());

        UpdateStaleThresholdsRequest req = new UpdateStaleThresholdsRequest();
        req.setThresholds(Map.of("STAGE_1", 7, "STAGE_2", 10));

        List<StaleThresholdResponse> result = service.updateStaleThresholds(req, 1L);

        verify(adminSettingRepository, times(2)).save(any(AdminSetting.class));
        assertNotNull(result);
    }

    @Test
    void updateStaleThresholds_invalidStageName_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        UpdateStaleThresholdsRequest req = new UpdateStaleThresholdsRequest();
        req.setThresholds(Map.of("INVALID_STAGE", 7));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.updateStaleThresholds(req, 1L));
    }

    // === Checklist ===

    @Test
    void addChecklistItem_autoCreatesDealDocuments() {
        when(checklistItemRepository.save(any())).thenAnswer(inv -> {
            DocumentChecklistItem item = inv.getArgument(0);
            item.setId(1L);
            return item;
        });

        Deal activeDeal = new Deal();
        activeDeal.setId(1L);
        activeDeal.setCurrentStage(DealStage.STAGE_3);
        when(dealRepository.findByStatus(eq(DealStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activeDeal)));
        when(dealDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateChecklistItemRequest req = new CreateChecklistItemRequest();
        req.setDocumentName("ID Copy");
        req.setRequirement("MANDATORY");
        req.setRequiredByStage("STAGE_6");

        service.addChecklistItem(req, 1L);

        verify(dealDocumentRepository).save(any(DealDocument.class));
    }

    // === Region ===

    @Test
    void addRegion_succeeds() {
        when(regionRepository.save(any())).thenAnswer(inv -> {
            Region r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        CreateRegionRequest req = new CreateRegionRequest();
        req.setName("Gauteng");

        RegionResponse resp = service.addRegion(req);

        assertEquals("Gauteng", resp.getName());
        assertTrue(resp.isActive());
    }

    @Test
    void updateRegion_archive() {
        Region region = new Region();
        region.setId(1L);
        region.setName("Gauteng");
        region.setActive(true);

        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(regionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateRegionRequest req = new UpdateRegionRequest();
        req.setActive(false);

        RegionResponse resp = service.updateRegion(1L, req);

        assertFalse(resp.isActive());
    }

    // === Webhook key ===

    @Test
    void generateKey_returnsPlaintextAndStoresHash() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(webhookApiKeyRepository.save(any())).thenAnswer(inv -> {
            WebhookApiKey key = inv.getArgument(0);
            key.setId(1L);
            return key;
        });

        GenerateKeyRequest req = new GenerateKeyRequest();
        req.setDescription("Partner integration");

        GenerateKeyResponse resp = service.generateKey(req, 1L);

        assertNotNull(resp.getApiKey());
        assertTrue(resp.getApiKey().startsWith("tcrm_"));
        assertEquals("Partner integration", resp.getDescription());
        // Verify hash is stored, not plaintext
        verify(webhookApiKeyRepository).save(argThat(key ->
                key.getKeyHash() != null && !key.getKeyHash().equals(resp.getApiKey())));
    }

    // === Notification preferences ===

    @Test
    void updatePreferences_updatesPerRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(notificationPreferenceRepository.findByRoleAndEventType(any(), any()))
                .thenReturn(Optional.empty());
        when(notificationPreferenceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(notificationPreferenceRepository.findAll()).thenReturn(Collections.emptyList());

        UpdateNotificationPrefsRequest req = new UpdateNotificationPrefsRequest();
        UpdateNotificationPrefsRequest.PrefItem item = new UpdateNotificationPrefsRequest.PrefItem();
        item.setRole(UserRole.AGENT);
        item.setEventType(NotificationEventType.FOLLOW_UP_DUE_TODAY);
        item.setEnabled(false);
        req.setPreferences(List.of(item));

        List<NotificationPrefResponse> result = service.updatePreferences(req, 1L);

        verify(notificationPreferenceRepository).save(any(NotificationPreference.class));
    }
}
