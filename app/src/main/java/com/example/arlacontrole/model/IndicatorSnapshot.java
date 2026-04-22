package com.example.arlacontrole.model;

import com.example.arlacontrole.data.local.RefuelEntity;

import java.util.ArrayList;
import java.util.List;

public class IndicatorSnapshot {
    public double arlaMonthlyTotal;
    public double dieselMonthlyTotal;
    public double totalMonthlyVolume;
    public double totalCostPeriod;
    public double totalArlaCostPeriod;
    public double totalDieselCostPeriod;
    public double averageCostPerKm;
    public double costVariationPercent;
    public double averagePerVehicle;
    public int totalRecords;
    public int monitoredVehicles;
    public int refuelAlertCount;
    public int totalSafetyEvents;
    public int totalAccidents;
    public int totalIncidents;
    public int totalNearMisses;
    public int unresolvedSafetyEvents;
    public int priorityAlertCount;
    public double evidenceCoveragePercent;
    public double resolutionRatePercent;
    public double safetyRiskIndex;
    public String safetyRiskLevel = "controlado";
    public final List<VehicleRankingItem> dieselRanking = new ArrayList<>();
    public final List<VehicleRankingItem> arlaRanking = new ArrayList<>();
    public final List<FinancialRankingItem> vehicleCostRanking = new ArrayList<>();
    public final List<FinancialRankingItem> driverCostRanking = new ArrayList<>();
    public final List<RefuelEntity> alerts = new ArrayList<>();
    public final List<SafetyRankingItem> vehicleSafetyRanking = new ArrayList<>();
    public final List<SafetyRankingItem> driverSafetyRanking = new ArrayList<>();
    public final List<PriorityAlertItem> priorityAlerts = new ArrayList<>();
    public final List<String> integratedInsights = new ArrayList<>();
}
