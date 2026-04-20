package com.turno.crm.model.dto;

import com.turno.crm.model.enums.OperatorType;
import com.turno.crm.model.enums.PrimaryUseCase;

import java.time.OffsetDateTime;
import java.util.List;

public class OperatorResponse {

    private Long id;
    private String companyName;
    private String phone;
    private String email;
    private RegionResponse region;
    private OperatorType operatorType;
    private String referralSource;
    private Integer fleetSize;
    private Integer numRoutes;
    private PrimaryUseCase primaryUseCase;
    private List<ContactResponse> contacts;
    private int dealCount;
    private OffsetDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public RegionResponse getRegion() { return region; }
    public void setRegion(RegionResponse region) { this.region = region; }

    public OperatorType getOperatorType() { return operatorType; }
    public void setOperatorType(OperatorType operatorType) { this.operatorType = operatorType; }

    public String getReferralSource() { return referralSource; }
    public void setReferralSource(String referralSource) { this.referralSource = referralSource; }

    public Integer getFleetSize() { return fleetSize; }
    public void setFleetSize(Integer fleetSize) { this.fleetSize = fleetSize; }

    public Integer getNumRoutes() { return numRoutes; }
    public void setNumRoutes(Integer numRoutes) { this.numRoutes = numRoutes; }

    public PrimaryUseCase getPrimaryUseCase() { return primaryUseCase; }
    public void setPrimaryUseCase(PrimaryUseCase primaryUseCase) { this.primaryUseCase = primaryUseCase; }

    public List<ContactResponse> getContacts() { return contacts; }
    public void setContacts(List<ContactResponse> contacts) { this.contacts = contacts; }

    public int getDealCount() { return dealCount; }
    public void setDealCount(int dealCount) { this.dealCount = dealCount; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
