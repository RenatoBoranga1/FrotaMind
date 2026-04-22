package com.example.arlacontrole.analytics.models;

import com.example.arlacontrole.analytics.AnalyticsFilter;
import com.example.arlacontrole.data.local.RefuelEntity;

import java.util.ArrayList;
import java.util.List;

public class DashboardUiState {
    public AnalyticsFilter filter;
    public String periodLabel = "";
    public String filterSummary = "";
    public AnalyticsSummary summary = new AnalyticsSummary();
    public PeriodComparison todayComparison = new PeriodComparison();
    public PeriodComparison weekComparison = new PeriodComparison();
    public PeriodComparison monthComparison = new PeriodComparison();
    public final List<TrendPoint> trendPoints = new ArrayList<>();
    public final List<OperationalAlertItem> alerts = new ArrayList<>();
    public final List<InsightRankingItem> topVehicles = new ArrayList<>();
    public final List<InsightRankingItem> topDrivers = new ArrayList<>();
    public final List<RefuelEntity> recentRecords = new ArrayList<>();
    public boolean empty;
}
