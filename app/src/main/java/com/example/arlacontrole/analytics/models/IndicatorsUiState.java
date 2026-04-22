package com.example.arlacontrole.analytics.models;

import com.example.arlacontrole.analytics.AnalyticsFilter;

import java.util.ArrayList;
import java.util.List;

public class IndicatorsUiState {
    public AnalyticsFilter filter;
    public String periodLabel = "";
    public String filterSummary = "";
    public AnalyticsSummary summary = new AnalyticsSummary();
    public final List<TrendPoint> trendPoints = new ArrayList<>();
    public final List<OperationalAlertItem> alerts = new ArrayList<>();
    public final List<InsightRankingItem> dieselRanking = new ArrayList<>();
    public final List<InsightRankingItem> arlaRanking = new ArrayList<>();
    public final List<InsightRankingItem> driverRanking = new ArrayList<>();
    public final List<InsightRankingItem> locationRanking = new ArrayList<>();
    public boolean empty;
}
