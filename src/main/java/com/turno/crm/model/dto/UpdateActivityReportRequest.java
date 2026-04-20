package com.turno.crm.model.dto;

import java.time.LocalDate;

public class UpdateActivityReportRequest {

    private LocalDate nextActionEta;

    public LocalDate getNextActionEta() { return nextActionEta; }
    public void setNextActionEta(LocalDate nextActionEta) { this.nextActionEta = nextActionEta; }
}
