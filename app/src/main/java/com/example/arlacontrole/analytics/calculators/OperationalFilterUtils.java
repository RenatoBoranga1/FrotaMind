package com.example.arlacontrole.analytics.calculators;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.model.OperationalFilter;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OperationalFilterUtils {

    private OperationalFilterUtils() {
    }

    public static List<RefuelEntity> filterRefuels(List<RefuelEntity> source, OperationalFilter filter) {
        List<RefuelEntity> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (RefuelEntity entity : source) {
            if (matchesRefuel(entity, filter)) {
                result.add(entity);
            }
        }
        return result;
    }

    public static List<SafetyEventEntity> filterSafetyEvents(List<SafetyEventEntity> source, OperationalFilter filter) {
        List<SafetyEventEntity> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (SafetyEventEntity entity : source) {
            if (matchesSafetyEvent(entity, filter)) {
                result.add(entity);
            }
        }
        return result;
    }

    public static boolean matchesRefuel(RefuelEntity entity, OperationalFilter filter) {
        if (entity == null || filter == null) {
            return entity != null;
        }
        if (!safe(filter.vehiclePlate).isEmpty() && !safe(entity.vehiclePlate).toUpperCase(Locale.ROOT).contains(safe(filter.vehiclePlate).toUpperCase(Locale.ROOT))) {
            return false;
        }
        if (!normalize(filter.driverName).isEmpty() && !normalize(entity.driverName).contains(normalize(filter.driverName))) {
            return false;
        }
        return matchesDate(entity.suppliedAtIso, filter);
    }

    public static boolean matchesSafetyEvent(SafetyEventEntity entity, OperationalFilter filter) {
        if (entity == null || filter == null) {
            return entity != null;
        }
        if (!safe(filter.vehiclePlate).isEmpty() && !safe(entity.vehiclePlate).toUpperCase(Locale.ROOT).contains(safe(filter.vehiclePlate).toUpperCase(Locale.ROOT))) {
            return false;
        }
        if (!normalize(filter.driverName).isEmpty() && !normalize(entity.driverName).contains(normalize(filter.driverName))) {
            return false;
        }
        if (!normalize(filter.eventType).isEmpty()
            && !normalize(entity.description + " " + entity.eventType).contains(normalize(filter.eventType))) {
            return false;
        }
        return matchesDate(entity.occurredAtIso, filter);
    }

    private static boolean matchesDate(String iso, OperationalFilter filter) {
        LocalDate date = parseDate(iso);
        if (date == null) {
            return false;
        }
        LocalDate start = filter.resolveStartDate();
        LocalDate end = filter.resolveEndDate();
        if (start != null && date.isBefore(start)) {
            return false;
        }
        if (end != null && date.isAfter(end)) {
            return false;
        }
        return true;
    }

    private static LocalDate parseDate(String iso) {
        try {
            return LocalDateTime.parse(iso).toLocalDate();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalize(String value) {
        String safe = safe(value);
        if (safe.isEmpty()) {
            return "";
        }
        return Normalizer.normalize(safe, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT);
    }
}
