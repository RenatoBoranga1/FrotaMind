package com.example.arlacontrole.model;

import java.util.ArrayList;
import java.util.List;

public class FinancialDashboardSnapshot {
    public double totalCostToday;
    public double totalCostPeriod;
    public double totalArlaCostPeriod;
    public double totalDieselCostPeriod;
    public double averageCostPerKm;
    public double previousPeriodTotalCost;
    public double costVariationPercent;
    public Double averageArlaPricePerLiter;
    public Double averageDieselPricePerLiter;
    public String summary = "";
    public final List<FinancialRankingItem> vehicleRanking = new ArrayList<>();
    public final List<FinancialRankingItem> driverRanking = new ArrayList<>();
    public final List<PriorityAlertItem> alerts = new ArrayList<>();
}
