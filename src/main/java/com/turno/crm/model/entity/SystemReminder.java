package com.turno.crm.model.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "system_reminders")
public class SystemReminder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id", nullable = false)
    private Deal deal;

    @Column(name = "reminder_type", nullable = false, length = 50)
    private String reminderType;

    @Column(name = "dismissed", nullable = false)
    private Boolean dismissed = false;

    @Column(name = "dismissed_at")
    private OffsetDateTime dismissedAt;

    public Deal getDeal() { return deal; }
    public void setDeal(Deal deal) { this.deal = deal; }

    public String getReminderType() { return reminderType; }
    public void setReminderType(String reminderType) { this.reminderType = reminderType; }

    public Boolean getDismissed() { return dismissed; }
    public void setDismissed(Boolean dismissed) { this.dismissed = dismissed; }

    public OffsetDateTime getDismissedAt() { return dismissedAt; }
    public void setDismissedAt(OffsetDateTime dismissedAt) { this.dismissedAt = dismissedAt; }
}
