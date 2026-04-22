package com.example.arlacontrole.rules;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.model.FuelType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class HistoryFilterEngine {

    private HistoryFilterEngine() {
    }

    public static List<RefuelEntity> apply(
        List<RefuelEntity> sourceRefuels,
        String fuelTypeFilter,
        String plateFilter,
        String driverFilter,
        LocalDate startDate,
        LocalDate endDate
    ) {
        List<RefuelEntity> result = new ArrayList<>();
        String safeFuelType = fuelTypeFilter == null ? "" : fuelTypeFilter.trim();
        String safePlate = plateFilter == null ? "" : plateFilter.trim().toUpperCase();
        String safeDriver = driverFilter == null ? "" : driverFilter.trim().toLowerCase();

        for (RefuelEntity entity : sourceRefuels) {
            if (!safeFuelType.isEmpty() && !entity.fuelType.equals(safeFuelType)) {
                continue;
            }
            if (!safePlate.isEmpty() && !entity.vehiclePlate.contains(safePlate)) {
                continue;
            }
            if (!safeDriver.isEmpty() && !entity.driverName.toLowerCase().contains(safeDriver)) {
                continue;
            }
            LocalDate date = LocalDateTime.parse(entity.suppliedAtIso).toLocalDate();
            if (startDate != null && date.isBefore(startDate)) {
                continue;
            }
            if (endDate != null && date.isAfter(endDate)) {
                continue;
            }
            result.add(entity);
        }
        return result;
    }

    public static String normalizeFilter(String fuelTypeFilter) {
        if (FuelType.ARLA.equals(fuelTypeFilter) || FuelType.DIESEL.equals(fuelTypeFilter)) {
            return fuelTypeFilter;
        }
        return "";
    }
}
