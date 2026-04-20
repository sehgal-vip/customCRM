package com.turno.crm.model.dto;

import java.util.List;

public class DashboardAlertsResponse {

    private List<DealAlertItem> staleDeals;
    private List<DealAlertItem> incompleteDocsApproachingLease;
    private List<DealAlertItem> highValueAtRisk;

    public List<DealAlertItem> getStaleDeals() { return staleDeals; }
    public void setStaleDeals(List<DealAlertItem> staleDeals) { this.staleDeals = staleDeals; }

    public List<DealAlertItem> getIncompleteDocsApproachingLease() { return incompleteDocsApproachingLease; }
    public void setIncompleteDocsApproachingLease(List<DealAlertItem> incompleteDocsApproachingLease) { this.incompleteDocsApproachingLease = incompleteDocsApproachingLease; }

    public List<DealAlertItem> getHighValueAtRisk() { return highValueAtRisk; }
    public void setHighValueAtRisk(List<DealAlertItem> highValueAtRisk) { this.highValueAtRisk = highValueAtRisk; }
}
