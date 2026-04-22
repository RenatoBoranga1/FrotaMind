package com.example.arlacontrole.model;

public class FinancialRankingItem {
    public String key = "";
    public String title = "";
    public String subtitle = "";
    public double totalCost;
    public Double costPerKm;
    public Double averagePricePerLiter;
    public int refuelCount;
    public String topStatus = RefuelStatus.NORMAL;
}
