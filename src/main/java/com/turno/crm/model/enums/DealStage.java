package com.turno.crm.model.enums;

public enum DealStage {
    STAGE_1,
    STAGE_2,
    STAGE_3,
    STAGE_4,
    STAGE_5,
    STAGE_6,
    STAGE_7,
    STAGE_8;

    private static final String[] DISPLAY_NAMES = {
        "Lead Captured",
        "First Contact",
        "Qualified Lead",
        "Closure of Commercials",
        "Token Received",
        "Documentation",
        "Lease Signing",
        "Vehicle Delivery"
    };

    public enum Phase { EARLY, COMMERCIAL, CLOSURE }

    public int getNumber() {
        return Integer.parseInt(name().substring(6));
    }

    public String getDisplayName() {
        return DISPLAY_NAMES[ordinal()];
    }

    public Phase getPhase() {
        int n = getNumber();
        if (n <= 2) return Phase.EARLY;
        if (n <= 4) return Phase.COMMERCIAL;
        return Phase.CLOSURE;
    }

    public DealStage next() {
        int n = getNumber();
        if (n >= 8) return null;
        return values()[ordinal() + 1];
    }

    public DealStage previous() {
        int n = getNumber();
        if (n <= 1) return null;
        return values()[ordinal() - 1];
    }
}
