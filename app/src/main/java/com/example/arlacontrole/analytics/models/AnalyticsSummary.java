package com.example.arlacontrole.analytics.models;

public class AnalyticsSummary {
    public int totalRecords;
    public double totalArlaLiters;
    public double totalDieselLiters;
    public double totalAmount;
    public int alertCount;
    public int attentionCount;
    public int activeAlertCount;
    public int pendingCount;
    public int syncedCount;
    public int failedSyncCount;
    public int evidenceCount;
    public int ocrAutoCount;
    public int ocrReviewedCount;
    public int ocrReviewRequiredCount;
    public int divergentCount;
    public int distinctVehicles;
    public int distinctDrivers;
    public int outOfPatternVehicles;
    public double averageLitersPerVehicle;
    public double averageLitersPerDriver;
    public double averageArlaMetric;
    public double averageDieselMetric;
    public double evidenceRate;
    public double ocrCoverageRate;
    public double syncSuccessRate;
    public String topLocation = "";
    public int topLocationCount;
    public double topLocationLiters;

    public double totalLiters() {
        return totalArlaLiters + totalDieselLiters;
    }
}
