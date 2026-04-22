package com.example.arlacontrole.analytics.models;

import java.util.ArrayList;
import java.util.List;

public class VehicleAnalyticsSnapshot {
    public String plate = "";
    public String fleetCode = "";
    public String model = "";
    public String operation = "";
    public double expectedArlaMin;
    public double expectedArlaMax;
    public double expectedDieselKmPerLiterMin;
    public double expectedDieselKmPerLiterMax;
    public double totalArla;
    public double totalDiesel;
    public double totalAmount;
    public double averageArlaConsumption;
    public double averageDieselConsumption;
    public double evidenceRate;
    public double syncSuccessRate;
    public String currentStatus = "";
    public int totalRecords;
    public int alertCount;
    public int pendingSyncCount;
    public final List<TrendPoint> trendPoints = new ArrayList<>();
    public final List<OperationalAlertItem> alerts = new ArrayList<>();
}
