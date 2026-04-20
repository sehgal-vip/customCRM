package com.turno.crm.service;

import com.turno.crm.model.dto.SearchResponse;
import com.turno.crm.model.dto.SearchResponse.*;
import com.turno.crm.model.entity.*;
import com.turno.crm.model.enums.UserRole;
import com.turno.crm.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchService {

    private static final int MAX_RESULTS_PER_TYPE = 10;

    private final DealRepository dealRepository;
    private final OperatorRepository operatorRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;

    public SearchService(DealRepository dealRepository, OperatorRepository operatorRepository,
                          TaskRepository taskRepository, UserRepository userRepository,
                          ContactRepository contactRepository) {
        this.dealRepository = dealRepository;
        this.operatorRepository = operatorRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
    }

    public SearchResponse search(String query, Long userId, UserRole userRole) {
        if (query == null || query.isBlank() || query.length() < 2) {
            return new SearchResponse(List.of(), List.of(), List.of(), List.of(), List.of());
        }

        String q = query.trim();

        return new SearchResponse(
                searchDeals(q, userId, userRole),
                searchOperators(q),
                searchTasks(q, userId, userRole),
                searchUsers(q),
                searchContacts(q)
        );
    }

    private List<DealSearchResult> searchDeals(String q, Long userId, UserRole role) {
        Specification<Deal> spec = (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("operator").get("companyName")), "%" + q.toLowerCase() + "%")
        );

        if (role == UserRole.AGENT) {
            spec = spec.and((root, cq, cb) ->
                    cb.equal(root.get("assignedAgent").get("id"), userId));
        }

        return dealRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS_PER_TYPE))
                .getContent()
                .stream()
                .map(d -> new DealSearchResult(
                        d.getId(),
                        d.getName(),
                        d.getOperator().getCompanyName(),
                        d.getCurrentStage().getDisplayName(),
                        d.getStatus().name()))
                .toList();
    }

    private List<OperatorSearchResult> searchOperators(String q) {
        return operatorRepository.search(q).stream()
                .limit(MAX_RESULTS_PER_TYPE)
                .map(o -> new OperatorSearchResult(
                        o.getId(),
                        o.getCompanyName(),
                        o.getPhone(),
                        o.getEmail()))
                .toList();
    }

    private List<TaskSearchResult> searchTasks(String q, Long userId, UserRole role) {
        Specification<Task> spec = (root, cq, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + q.toLowerCase() + "%");

        if (role == UserRole.AGENT) {
            spec = spec.and((root, cq, cb) ->
                    cb.equal(root.get("assignedTo").get("id"), userId));
        }

        return taskRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS_PER_TYPE))
                .getContent()
                .stream()
                .map(t -> new TaskSearchResult(
                        t.getId(),
                        t.getTitle(),
                        t.getAssignedTo().getName(),
                        t.getDeal() != null ? t.getDeal().getName() : null,
                        t.getStatus().name(),
                        t.getDueDate() != null ? t.getDueDate().toString() : null))
                .toList();
    }

    private List<UserSearchResult> searchUsers(String q) {
        return userRepository.search(q).stream()
                .limit(MAX_RESULTS_PER_TYPE)
                .map(u -> new UserSearchResult(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        u.getRole().name()))
                .toList();
    }

    private List<ContactSearchResult> searchContacts(String q) {
        return contactRepository.search(q).stream()
                .limit(MAX_RESULTS_PER_TYPE)
                .map(c -> new ContactSearchResult(
                        c.getId(),
                        c.getName(),
                        c.getRole() != null ? c.getRole().name() : null,
                        c.getMobile(),
                        c.getEmail(),
                        c.getOperator().getId(),
                        c.getOperator().getCompanyName()))
                .toList();
    }
}
