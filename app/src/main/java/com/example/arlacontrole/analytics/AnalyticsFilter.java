package com.example.arlacontrole.analytics;

import java.time.LocalDate;

public class AnalyticsFilter {

    public static final String PERIOD_TODAY = "TODAY";
    public static final String PERIOD_LAST_7_DAYS = "LAST_7_DAYS";
    public static final String PERIOD_CURRENT_MONTH = "CURRENT_MONTH";
    public static final String PERIOD_CUSTOM = "CUSTOM";

    public static final String SYNC_ALL = "ALL";
    public static final String SYNC_SYNCED = "SYNCED";
    public static final String SYNC_PENDING = "PENDING";

    public String periodPreset = PERIOD_LAST_7_DAYS;
    public String fuelType = "";
    public String vehiclePlate = "";
    public String driverName = "";
    public String statusLevel = "";
    public String syncFilter = SYNC_ALL;
    public LocalDate startDate;
    public LocalDate endDate;

    public static AnalyticsFilter defaultDashboard() {
        AnalyticsFilter filter = new AnalyticsFilter();
        filter.periodPreset = PERIOD_LAST_7_DAYS;
        return filter;
    }

    public static AnalyticsFilter defaultIndicators() {
        AnalyticsFilter filter = new AnalyticsFilter();
        filter.periodPreset = PERIOD_CURRENT_MONTH;
        return filter;
    }

    public static AnalyticsFilter defaultHistory() {
        AnalyticsFilter filter = new AnalyticsFilter();
        filter.periodPreset = PERIOD_CURRENT_MONTH;
        return filter;
    }

    public AnalyticsFilter copy() {
        AnalyticsFilter copy = new AnalyticsFilter();
        copy.periodPreset = periodPreset;
        copy.fuelType = fuelType;
        copy.vehiclePlate = vehiclePlate;
        copy.driverName = driverName;
        copy.statusLevel = statusLevel;
        copy.syncFilter = syncFilter;
        copy.startDate = startDate;
        copy.endDate = endDate;
        return copy;
    }

    public LocalDate resolveStartDate(LocalDate referenceDate) {
        LocalDate safeReference = referenceDate == null ? LocalDate.now() : referenceDate;
        if (PERIOD_TODAY.equals(periodPreset)) {
            return safeReference;
        }
        if (PERIOD_CURRENT_MONTH.equals(periodPreset)) {
            return safeReference.withDayOfMonth(1);
        }
        if (PERIOD_CUSTOM.equals(periodPreset)) {
            return startDate;
        }
        return safeReference.minusDays(6);
    }

    public LocalDate resolveEndDate(LocalDate referenceDate) {
        LocalDate safeReference = referenceDate == null ? LocalDate.now() : referenceDate;
        if (PERIOD_CUSTOM.equals(periodPreset)) {
            return endDate;
        }
        return safeReference;
    }

    public AnalyticsFilter copyWithPeriod(LocalDate startDate, LocalDate endDate) {
        AnalyticsFilter copy = copy();
        copy.periodPreset = PERIOD_CUSTOM;
        copy.startDate = startDate;
        copy.endDate = endDate;
        return copy;
    }

    public void resetAdvancedFilters() {
        fuelType = "";
        vehiclePlate = "";
        driverName = "";
        statusLevel = "";
        syncFilter = SYNC_ALL;
        if (PERIOD_CUSTOM.equals(periodPreset)) {
            startDate = null;
            endDate = null;
        }
    }
}
