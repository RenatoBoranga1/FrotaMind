package com.example.arlacontrole.model;

public class SafetyRankingItem {
    public String key = "";
    public String title = "";
    public String subtitle = "";
    public int eventCount;
    public int unresolvedCount;
    public int riskScore;
    public String topSeverity = SafetySeverity.LOW;
}
