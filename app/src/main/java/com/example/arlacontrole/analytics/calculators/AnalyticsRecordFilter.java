package com.example.arlacontrole.analytics.calculators;

import com.example.arlacontrole.analytics.AnalyticsFilter;
import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SyncState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class AnalyticsRecordFilter {

    private AnalyticsRecordFilter() {
    }

    public static List<RefuelEntity> apply(List<RefuelEntity> source, AnalyticsFilter filter) {
        List<RefuelEntity> result = new ArrayList<>();
        if (source == null) {
            return result;
        }

        LocalDate referenceDate = LocalDate.now();
        for (RefuelEntity entity : source) {
            if (matches(entity, filter, referenceDate)) {
                result.add(entity);
            }
        }
        return result;
    }

    public static boolean matches(RefuelEntity entity, AnalyticsFilter filter, LocalDate referenceDate) {
        if (entity == null) {
            return false;
        }
        if (filter == null) {
            return true;
        }

        String fuelType = normalize(filter.fuelType).toUpperCase();
        String plate = normalize(filter.vehiclePlate).toUpperCase();
        String driver = normalize(filter.driverName).toLowerCase();
        String status = normalizeStatus(filter.statusLevel);
        String syncFilter = normalizeSync(filter.syncFilter);

        if (!fuelType.isEmpty() && !fuelType.equalsIgnoreCase(entity.fuelType)) {
            return false;
        }
        if (!plate.isEmpty() && !entity.vehiclePlate.toUpperCase().contains(plate)) {
            return false;
        }
        if (!driver.isEmpty() && !entity.driverName.toLowerCase().contains(driver)) {
            return false;
        }
        if (!status.isEmpty() && !status.equals(entity.statusLevel)) {
            return false;
        }
        if (AnalyticsFilter.SYNC_SYNCED.equals(syncFilter) && !SyncState.SYNCED.equals(entity.syncStatus)) {
            return false;
        }
        if (AnalyticsFilter.SYNC_PENDING.equals(syncFilter) && SyncState.SYNCED.equals(entity.syncStatus)) {
            return false;
        }

        LocalDate date = parseDate(entity.suppliedAtIso);
        LocalDate startDate = filter.resolveStartDate(referenceDate);
        LocalDate endDate = filter.resolveEndDate(referenceDate);

        if (startDate != null && date.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && date.isAfter(endDate)) {
            return false;
        }
        return true;
    }

    public static LocalDate parseDate(String suppliedAtIso) {
        try {
            return LocalDateTime.parse(suppliedAtIso).toLocalDate();
        } catch (Exception ignored) {
            return LocalDate.now();
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeStatus(String value) {
        if (RefuelStatus.ALERT.equals(value) || RefuelStatus.ATTENTION.equals(value) || RefuelStatus.NORMAL.equals(value)) {
            return value;
        }
        return "";
    }

    private static String normalizeSync(String value) {
        if (AnalyticsFilter.SYNC_SYNCED.equals(value) || AnalyticsFilter.SYNC_PENDING.equals(value)) {
            return value;
        }
        return AnalyticsFilter.SYNC_ALL;
    }
}
