package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.exception.ResourceNotFoundException;
import com.turno.crm.model.dto.DocumentChecklistResponse;
import com.turno.crm.model.dto.DocumentCompletionResponse;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.DealDocumentRepository;
import com.turno.crm.repository.DealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentChecklistServiceTest {

    @Mock private DealDocumentRepository dealDocumentRepository;
    @Mock private DealRepository dealRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private AuditService auditService;

    @InjectMocks
    private DocumentChecklistService service;

    private Deal deal;
    private DealDocument document;
    private DocumentChecklistItem checklistItem;

    @BeforeEach
    void setUp() {
        deal = new Deal();
        deal.setId(1L);
        deal.setName("Test Deal");

        checklistItem = new DocumentChecklistItem();
        checklistItem.setId(1L);
        checklistItem.setDocumentName("Registration Certificate");
        checklistItem.setRequirement(DocRequirement.MANDATORY);
        checklistItem.setRequiredByStage(DealStage.STAGE_7);

        document = new DealDocument();
        document.setId(10L);
        document.setDeal(deal);
        document.setChecklistItem(checklistItem);
        document.setStatus(DocStatus.NOT_STARTED);
    }

    // === getDocuments ===

    @Test
    void getDocuments_returnsItemsWithStatus() {
        when(dealRepository.existsById(1L)).thenReturn(true);
        when(dealDocumentRepository.findByDealId(1L)).thenReturn(List.of(document));
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(1L);
        when(dealDocumentRepository.countMandatoryComplete(1L)).thenReturn(0L);

        DocumentCompletionResponse resp = service.getDocuments(1L);

        assertNotNull(resp);
        assertEquals(1, resp.getItems().size());
        assertEquals("NOT_STARTED", resp.getItems().get(0).getStatus());
    }

    @Test
    void getDocuments_dealNotFound_throws() {
        when(dealRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                service.getDocuments(999L));
    }

    // === Completion metrics ===

    @Test
    void getCompletionMetrics_calculatesPercentageCorrectly() {
        when(dealRepository.existsById(1L)).thenReturn(true);
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(4L);
        when(dealDocumentRepository.countMandatoryComplete(1L)).thenReturn(2L);

        DocumentCompletionResponse resp = service.getCompletionMetrics(1L);

        assertEquals(2, resp.getMandatoryComplete());
        assertEquals(4, resp.getMandatoryTotal());
        assertEquals(50.0, resp.getPercentage());
    }

    @Test
    void getCompletionMetrics_noMandatoryDocs_returns100Percent() {
        when(dealRepository.existsById(1L)).thenReturn(true);
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(0L);
        when(dealDocumentRepository.countMandatoryComplete(1L)).thenReturn(0L);

        DocumentCompletionResponse resp = service.getCompletionMetrics(1L);

        assertEquals(100.0, resp.getPercentage());
    }

    @Test
    void getCompletionMetrics_allComplete_returns100Percent() {
        when(dealRepository.existsById(1L)).thenReturn(true);
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(5L);
        when(dealDocumentRepository.countMandatoryComplete(1L)).thenReturn(5L);

        DocumentCompletionResponse resp = service.getCompletionMetrics(1L);

        assertEquals(100.0, resp.getPercentage());
    }

    // === Status update ===

    @Test
    void updateStatus_validTransition_succeeds() {
        when(dealDocumentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(dealDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DocumentChecklistResponse resp = service.updateStatus(1L, 10L, "REQUESTED", 100L);

        assertEquals("REQUESTED", resp.getStatus());
    }

    @Test
    void updateStatus_invalidStatus_throws() {
        when(dealDocumentRepository.findById(10L)).thenReturn(Optional.of(document));

        assertThrows(BusinessRuleViolationException.class, () ->
                service.updateStatus(1L, 10L, "INVALID_STATUS", 100L));
    }

    @Test
    void updateStatus_documentNotFound_throws() {
        when(dealDocumentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.updateStatus(1L, 999L, "RECEIVED", 100L));
    }

    @Test
    void updateStatus_wrongDealId_throws() {
        Deal otherDeal = new Deal();
        otherDeal.setId(999L);
        document.setDeal(otherDeal);

        when(dealDocumentRepository.findById(10L)).thenReturn(Optional.of(document));

        assertThrows(ResourceNotFoundException.class, () ->
                service.updateStatus(1L, 10L, "RECEIVED", 100L));
    }

    @Test
    void updateStatus_toReceivedStatus() {
        when(dealDocumentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(dealDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DocumentChecklistResponse resp = service.updateStatus(1L, 10L, "RECEIVED", 100L);

        assertEquals("RECEIVED", resp.getStatus());
    }

    @Test
    void updateStatus_toVerifiedStatus() {
        document.setStatus(DocStatus.RECEIVED);
        when(dealDocumentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(dealDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DocumentChecklistResponse resp = service.updateStatus(1L, 10L, "VERIFIED", 100L);

        assertEquals("VERIFIED", resp.getStatus());
    }

    // === Upload ===

    @Test
    void uploadFile_setsFileKeyAndStatusReceived() {
        when(dealDocumentRepository.findById(10L)).thenReturn(Optional.of(document));
        when(fileStorageService.store(any(), anyString())).thenReturn("deals/1/documents/test.pdf");
        when(dealDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MultipartFile file = mock(MultipartFile.class);

        DocumentChecklistResponse resp = service.uploadFile(1L, 10L, file, 100L);

        assertEquals("deals/1/documents/test.pdf", resp.getFileKey());
        assertTrue(resp.isHasFile());
        assertEquals("RECEIVED", resp.getStatus());
    }

    // === Completion data for response ===

    @Test
    void getDocuments_includesRequirementAndRequiredByStage() {
        when(dealRepository.existsById(1L)).thenReturn(true);
        when(dealDocumentRepository.findByDealId(1L)).thenReturn(List.of(document));
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(1L);
        when(dealDocumentRepository.countMandatoryComplete(1L)).thenReturn(0L);

        DocumentCompletionResponse resp = service.getDocuments(1L);

        assertEquals("MANDATORY", resp.getItems().get(0).getRequirement());
        assertEquals("STAGE_7", resp.getItems().get(0).getRequiredByStage());
    }

    @Test
    void getDocuments_fileKeyNull_hasFileFalse() {
        document.setFileKey(null);
        when(dealRepository.existsById(1L)).thenReturn(true);
        when(dealDocumentRepository.findByDealId(1L)).thenReturn(List.of(document));
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(1L);
        when(dealDocumentRepository.countMandatoryComplete(1L)).thenReturn(0L);

        DocumentCompletionResponse resp = service.getDocuments(1L);

        assertFalse(resp.getItems().get(0).isHasFile());
    }

    @Test
    void getDocuments_fileKeyPresent_hasFileTrue() {
        document.setFileKey("deals/1/documents/file.pdf");
        when(dealRepository.existsById(1L)).thenReturn(true);
        when(dealDocumentRepository.findByDealId(1L)).thenReturn(List.of(document));
        when(dealDocumentRepository.countMandatoryTotal(1L)).thenReturn(1L);
        when(dealDocumentRepository.countMandatoryComplete(1L)).thenReturn(1L);

        DocumentCompletionResponse resp = service.getDocuments(1L);

        assertTrue(resp.getItems().get(0).isHasFile());
    }
}
