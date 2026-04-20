package com.turno.crm.exception;

import java.util.List;

public class ExitCriteriaNotMetException extends RuntimeException {

    private final List<CriteriaResult> results;

    public record CriteriaResult(String rule, boolean met, boolean softBlock, String message) {}

    public ExitCriteriaNotMetException(List<CriteriaResult> results) {
        super("Exit criteria not met");
        this.results = results;
    }

    public List<CriteriaResult> getResults() {
        return results;
    }
}
