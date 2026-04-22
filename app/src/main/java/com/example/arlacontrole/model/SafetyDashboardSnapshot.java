package com.example.arlacontrole.model;

import java.util.ArrayList;
import java.util.List;

public class SafetyDashboardSnapshot {
    public int totalEvents;
    public int accidents;
    public int incidents;
    public int nearMisses;
    public int unsafeConditions;
    public int unsafeBehaviors;
    public int unresolvedEvents;
    public double evidenceCoveragePercent;
    public double resolutionRatePercent;
    public double riskIndex;
    public String riskLevel = "controlado";
    public String executiveSummary = "";
    public final List<PriorityAlertItem> priorityAlerts = new ArrayList<>();
    public final List<SafetyRankingItem> vehicleRanking = new ArrayList<>();
    public final List<SafetyRankingItem> driverRanking = new ArrayList<>();
}
