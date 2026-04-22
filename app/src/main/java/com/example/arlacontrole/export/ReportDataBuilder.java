package com.example.arlacontrole.export;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SyncState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReportDataBuilder {

    public ReportDataset build(
        String systemName,
        String generatedAtIso,
        ReportFilter filter,
        List<RefuelEntity> sourceRecords
    ) {
        ReportDataset dataset = new ReportDataset();
        dataset.systemName = systemName;
        dataset.generatedAtIso = generatedAtIso;
        dataset.filter = filter;
        dataset.summary = new ReportSummary();

        if (sourceRecords == null) {
            return dataset;
        }

        Set<String> vehicles = new HashSet<>();
        List<RefuelEntity> filtered = new ArrayList<>();
        for (RefuelEntity entity : sourceRecords) {
            if (!matches(entity, filter)) {
                continue;
            }
            filtered.add(entity);
            vehicles.add(entity.vehiclePlate);
            dataset.summary.totalRecords++;
            if (RefuelStatus.ALERT.equals(entity.statusLevel)) {
                dataset.summary.alertCount++;
            } else if (RefuelStatus.ATTENTION.equals(entity.statusLevel)) {
                dataset.summary.attentionCount++;
            }
            if ("DIESEL".equals(entity.fuelType)) {
                dataset.summary.totalDieselLiters += entity.liters;
            } else {
                dataset.summary.totalArlaLiters += entity.liters;
            }
        }

        dataset.summary.distinctVehicles = vehicles.size();
        dataset.summary.averageLitersPerVehicle =
            vehicles.isEmpty() ? 0d : (dataset.summary.totalArlaLiters + dataset.summary.totalDieselLiters) / vehicles.size();
        dataset.records = filtered;
        return dataset;
    }

    private boolean matches(RefuelEntity entity, ReportFilter filter) {
        if (filter == null) {
            return true;
        }
        String fuelType = filter.fuelType == null ? "" : filter.fuelType.trim();
        String plate = filter.vehiclePlate == null ? "" : filter.vehiclePlate.trim().toUpperCase();
        String driver = filter.driverName == null ? "" : filter.driverName.trim().toLowerCase();
        String status = filter.statusLevel == null ? "" : filter.statusLevel.trim();
        String sync = filter.syncFilter == null ? ReportFilter.SYNC_ALL : filter.syncFilter.trim();

        if (!fuelType.isEmpty() && !fuelType.equals(entity.fuelType)) {
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
        if (ReportFilter.SYNC_SYNCED.equals(sync) && !SyncState.SYNCED.equals(entity.syncStatus)) {
            return false;
        }
        if (ReportFilter.SYNC_PENDING.equals(sync) && SyncState.SYNCED.equals(entity.syncStatus)) {
            return false;
        }
        LocalDate date = LocalDateTime.parse(entity.suppliedAtIso).toLocalDate();
        if (filter.startDate != null && date.isBefore(filter.startDate)) {
            return false;
        }
        if (filter.endDate != null && date.isAfter(filter.endDate)) {
            return false;
        }
        return true;
    }
}
