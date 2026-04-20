package com.turno.crm.controller;

import com.turno.crm.model.dto.SearchResponse;
import com.turno.crm.security.CurrentUserProvider;
import com.turno.crm.service.SearchService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;
    private final CurrentUserProvider currentUserProvider;

    public SearchController(SearchService searchService, CurrentUserProvider currentUserProvider) {
        this.searchService = searchService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam("q") @NotBlank @Size(min = 2, max = 100) String query) {
        SearchResponse response = searchService.search(
                query,
                currentUserProvider.getCurrentUserId(),
                currentUserProvider.getCurrentUserRole());
        return ResponseEntity.ok(response);
    }
}
