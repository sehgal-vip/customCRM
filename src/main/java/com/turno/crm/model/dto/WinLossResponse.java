package com.turno.crm.model.dto;

import java.util.List;

public class WinLossResponse {

    private double winRate;
    private List<LostReasonCount> lostReasons;

    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }

    public List<LostReasonCount> getLostReasons() { return lostReasons; }
    public void setLostReasons(List<LostReasonCount> lostReasons) { this.lostReasons = lostReasons; }
}
