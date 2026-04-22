package com.example.arlacontrole.model;

import java.util.ArrayList;
import java.util.List;

public class DashboardSnapshot {
    public int totalRefuelsToday;
    public double arlaLitersPeriod;
    public double dieselLitersPeriod;
    public double totalVolumePeriod;
    public double totalCostToday;
    public double totalCostPeriod;
    public double totalArlaCostPeriod;
    public double totalDieselCostPeriod;
    public double averageCostPerKm;
    public double costVariationPercent;
    public int pendingSync;
    public int activeAlerts;
    public int totalSafetyEventsPeriod;
    public int unresolvedSafetyEvents;
    public int highRiskAssets;
    public double evidenceCoveragePercent;
    public double resolutionRatePercent;
    public String safetyRiskLevel = "controlado";
    public String executiveSummary = "";
    public final List<PriorityAlertItem> priorityAlerts = new ArrayList<>();
    public final List<String> integratedInsights = new ArrayList<>();
}
