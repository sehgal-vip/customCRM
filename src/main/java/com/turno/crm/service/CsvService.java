package com.turno.crm.service;

import com.turno.crm.exception.BusinessRuleViolationException;
import com.turno.crm.model.dto.*;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.*;
import com.turno.crm.repository.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CsvService {

    private static final int MAX_ROWS = 200;

    private static final String[] CSV_HEADERS = {
            "deal_name", "operator_name", "contact_name", "contact_phone",
            "contact_email", "contact_role", "fleet_size", "lead_source",
            "operator_type", "region"
    };

    private final DealRepository dealRepository;
    private final OperatorRepository operatorRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final DocumentChecklistItemRepository checklistItemRepository;
    private final DealDocumentRepository dealDocumentRepository;
    private final AuditService auditService;

    public CsvService(DealRepository dealRepository,
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

    // ─── Import: Parse & Validate ───────────────────────────────────────

    public CsvPreviewResponse parseAndValidate(InputStream inputStream) {
        List<CsvRowResult> rows = new ArrayList<>();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setHeader(CSV_HEADERS)
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            int rowNum = 0;
            for (CSVRecord record : parser) {
                rowNum++;
                if (rowNum > MAX_ROWS) {
                    break;
                }

                CsvRowResult result = new CsvRowResult();
                result.setRowNumber(rowNum);

                Map<String, String> data = new LinkedHashMap<>();
                for (String header : CSV_HEADERS) {
                    String val = record.isMapped(header) ? record.get(header) : "";
                    data.put(header, val);
                }
                result.setData(data);

                List<String> errors = validateRow(data);
                result.setErrors(errors);
                result.setValid(errors.isEmpty());

                rows.add(result);
            }
        } catch (IOException e) {
            throw new BusinessRuleViolationException("Failed to parse CSV file: " + e.getMessage());
        }

        int valid = (int) rows.stream().filter(CsvRowResult::isValid).count();
        int invalid = rows.size() - valid;

        CsvPreviewResponse response = new CsvPreviewResponse();
        response.setTotalRows(rows.size());
        response.setValidRows(valid);
        response.setInvalidRows(invalid);
        response.setRows(rows);
        return response;
    }

    private List<String> validateRow(Map<String, String> data) {
        List<String> errors = new ArrayList<>();

        if (!StringUtils.hasText(data.get("deal_name"))) {
            errors.add("deal_name is required");
        }
        if (!StringUtils.hasText(data.get("operator_name"))) {
            errors.add("operator_name is required");
        }
        if (!StringUtils.hasText(data.get("contact_name"))) {
            errors.add("contact_name is required");
        }
        if (!StringUtils.hasText(data.get("contact_phone")) && !StringUtils.hasText(data.get("contact_email"))) {
            errors.add("Either contact_phone or contact_email is required");
        }
        if (!StringUtils.hasText(data.get("lead_source"))) {
            errors.add("lead_source is required");
        } else {
            try {
                LeadSource.valueOf(data.get("lead_source"));
            } catch (IllegalArgumentException e) {
                errors.add("Invalid lead_source: " + data.get("lead_source"));
            }
        }

        String fleetSize = data.get("fleet_size");
        if (StringUtils.hasText(fleetSize)) {
            try {
                Integer.parseInt(fleetSize);
            } catch (NumberFormatException e) {
                errors.add("fleet_size must be a number");
            }
        }

        String operatorType = data.get("operator_type");
        if (StringUtils.hasText(operatorType)) {
            try {
                OperatorType.valueOf(operatorType);
            } catch (IllegalArgumentException e) {
                errors.add("Invalid operator_type: " + operatorType);
            }
        }

        return errors;
    }

    // ─── Import: Execute ────────────────────────────────────────────────

    public CsvImportResponse executeImport(CsvPreviewResponse preview, CsvImportRequest importRequest, Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new BusinessRuleViolationException("User not found"));

        // Find a manager to assign deals to
        List<User> managers = userRepository.findByRoleAndStatus(UserRole.MANAGER, UserStatus.ACTIVE);
        User assignee = managers.isEmpty() ? actor : managers.get(0);

        Set<Integer> selectedSet = new HashSet<>(importRequest.getSelectedRows());
        List<DocumentChecklistItem> activeChecklistItems = checklistItemRepository.findByActiveTrue();

        int imported = 0;
        int skipped = 0;

        for (CsvRowResult row : preview.getRows()) {
            if (!selectedSet.contains(row.getRowNumber())) {
                continue;
            }
            if (!row.isValid()) {
                skipped++;
                continue;
            }

            Map<String, String> data = row.getData();

            // Check deal name uniqueness
            if (dealRepository.existsByName(data.get("deal_name"))) {
                skipped++;
                continue;
            }

            // Create or find operator
            Operator operator;
            Optional<Operator> existingOp = operatorRepository.findByCompanyNameAndPhone(
                    data.get("operator_name"), data.get("contact_phone"));

            if (existingOp.isPresent()) {
                operator = existingOp.get();
            } else {
                operator = new Operator();
                operator.setCompanyName(data.get("operator_name"));
                operator.setPhone(data.get("contact_phone"));
                operator.setEmail(data.get("contact_email"));
                operator.setCreatedBy(actor);

                if (StringUtils.hasText(data.get("fleet_size"))) {
                    operator.setFleetSize(Integer.parseInt(data.get("fleet_size")));
                }
                if (StringUtils.hasText(data.get("operator_type"))) {
                    try {
                        operator.setOperatorType(OperatorType.valueOf(data.get("operator_type")));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (StringUtils.hasText(data.get("region"))) {
                    regionRepository.findByActiveTrue().stream()
                            .filter(r -> r.getName().equalsIgnoreCase(data.get("region")))
                            .findFirst()
                            .ifPresent(operator::setRegion);
                }
                operator = operatorRepository.save(operator);
            }

            // Create contact
            Contact contact = new Contact();
            contact.setOperator(operator);
            contact.setName(data.get("contact_name"));
            if (StringUtils.hasText(data.get("contact_role"))) {
                try {
                    contact.setRole(ContactRole.valueOf(data.get("contact_role")));
                } catch (IllegalArgumentException e) {
                    contact.setRole(ContactRole.OWNER);
                }
            } else {
                contact.setRole(ContactRole.OWNER);
            }
            contact.setMobile(data.get("contact_phone"));
            contact.setEmail(data.get("contact_email"));
            contactRepository.save(contact);

            // Create deal
            Deal deal = new Deal();
            deal.setName(data.get("deal_name"));
            deal.setOperator(operator);
            deal.setAssignedAgent(assignee);
            deal.setLeadSource(LeadSource.valueOf(data.get("lead_source")));
            deal.setCurrentStage(DealStage.STAGE_1);
            deal.setStatus(DealStatus.ACTIVE);

            if (StringUtils.hasText(data.get("fleet_size"))) {
                deal.setFleetSize(Integer.parseInt(data.get("fleet_size")));
            }

            deal = dealRepository.save(deal);

            // Auto-create deal documents
            for (DocumentChecklistItem item : activeChecklistItems) {
                DealDocument doc = new DealDocument();
                doc.setDeal(deal);
                doc.setChecklistItem(item);
                doc.setStatus(DocStatus.NOT_STARTED);
                dealDocumentRepository.save(doc);
            }

            auditService.log(AuditEntityType.DEAL, deal.getId(), AuditAction.CREATE, actorId,
                    Map.of("source", "csv_import", "dealName", deal.getName()));

            imported++;
        }

        return new CsvImportResponse(imported, skipped);
    }

    // ─── Export ─────────────────────────────────────────────────────────

    public byte[] exportDeals(String format) {
        List<Deal> deals = dealRepository.findAll();

        if ("xlsx".equalsIgnoreCase(format)) {
            return exportDealsXlsx(deals);
        }
        return exportDealsCsv(deals);
    }

    private byte[] exportDealsCsv(List<Deal> deals) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8),
                     CSVFormat.DEFAULT.builder().setHeader(
                             "id", "deal_name", "operator_name", "agent_name", "fleet_size",
                             "estimated_monthly_value", "lead_source", "current_stage",
                             "status", "created_at"
                     ).build())) {

            for (Deal deal : deals) {
                printer.printRecord(
                        deal.getId(),
                        deal.getName(),
                        deal.getOperator().getCompanyName(),
                        deal.getAssignedAgent().getName(),
                        deal.getFleetSize(),
                        deal.getEstimatedMonthlyValue(),
                        deal.getLeadSource(),
                        deal.getCurrentStage(),
                        deal.getStatus(),
                        deal.getCreatedAt()
                );
            }
            printer.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessRuleViolationException("Failed to export CSV: " + e.getMessage());
        }
    }

    private byte[] exportDealsXlsx(List<Deal> deals) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Deals");

            // Header row
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "Deal Name", "Operator Name", "Agent Name", "Fleet Size",
                    "Est. Monthly Value", "Lead Source", "Current Stage", "Status", "Created At"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (Deal deal : deals) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(deal.getId());
                row.createCell(1).setCellValue(deal.getName());
                row.createCell(2).setCellValue(deal.getOperator().getCompanyName());
                row.createCell(3).setCellValue(deal.getAssignedAgent().getName());
                row.createCell(4).setCellValue(deal.getFleetSize() != null ? deal.getFleetSize() : 0);
                row.createCell(5).setCellValue(deal.getEstimatedMonthlyValue() != null ?
                        deal.getEstimatedMonthlyValue().doubleValue() : 0);
                row.createCell(6).setCellValue(deal.getLeadSource() != null ? deal.getLeadSource().name() : "");
                row.createCell(7).setCellValue(deal.getCurrentStage() != null ? deal.getCurrentStage().name() : "");
                row.createCell(8).setCellValue(deal.getStatus() != null ? deal.getStatus().name() : "");
                row.createCell(9).setCellValue(deal.getCreatedAt() != null ? deal.getCreatedAt().toString() : "");
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessRuleViolationException("Failed to export XLSX: " + e.getMessage());
        }
    }
}
