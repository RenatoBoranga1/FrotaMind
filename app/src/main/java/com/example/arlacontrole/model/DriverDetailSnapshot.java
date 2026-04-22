package com.example.arlacontrole.model;

import java.util.ArrayList;
import java.util.List;

public class DriverDetailSnapshot {
    public String driverName = "";
    public int totalRefuels;
    public int totalEvents;
    public int criticalEvents;
    public double totalCost;
    public double totalDiesel;
    public double totalArla;
    public double averageCostPerKm;
    public double eventsPer100Km;
    public double riskScore;
    public String topRiskType = "";
    public final List<String> insights = new ArrayList<>();
}
