package com.turno.crm.model.dto;

import com.turno.crm.model.enums.OperatorType;
import com.turno.crm.model.enums.PrimaryUseCase;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateOperatorRequest {

    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Email(message = "Must be a valid email")
    private String email;

    private Long regionId;
    private OperatorType operatorType;
    private String referralSource;
    private Long referredByOperatorId;
    private Integer fleetSize;
    private Integer numRoutes;
    private PrimaryUseCase primaryUseCase;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getRegionId() { return regionId; }
    public void setRegionId(Long regionId) { this.regionId = regionId; }

    public OperatorType getOperatorType() { return operatorType; }
    public void setOperatorType(OperatorType operatorType) { this.operatorType = operatorType; }

    public String getReferralSource() { return referralSource; }
    public void setReferralSource(String referralSource) { this.referralSource = referralSource; }

    public Long getReferredByOperatorId() { return referredByOperatorId; }
    public void setReferredByOperatorId(Long referredByOperatorId) { this.referredByOperatorId = referredByOperatorId; }

    public Integer getFleetSize() { return fleetSize; }
    public void setFleetSize(Integer fleetSize) { this.fleetSize = fleetSize; }

    public Integer getNumRoutes() { return numRoutes; }
    public void setNumRoutes(Integer numRoutes) { this.numRoutes = numRoutes; }

    public PrimaryUseCase getPrimaryUseCase() { return primaryUseCase; }
    public void setPrimaryUseCase(PrimaryUseCase primaryUseCase) { this.primaryUseCase = primaryUseCase; }
}
