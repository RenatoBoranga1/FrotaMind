package com.example.arlacontrole.analytics.models;

public class PeriodComparison {
    public static final String TREND_UP = "UP";
    public static final String TREND_DOWN = "DOWN";
    public static final String TREND_STABLE = "STABLE";

    public String label;
    public double totalLiters;
    public double totalAmount;
    public int totalRecords;
    public double changePercentage;
    public String trendDirection = TREND_STABLE;
}
