package com.example.arlacontrole.export;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SyncState;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;

public class ReportDataBuilderTest {

    @Test
    public void build_filtersByFuelStatusAndSync() {
        ReportFilter filter = new ReportFilter();
        filter.fuelType = "DIESEL";
        filter.statusLevel = RefuelStatus.ALERT;
        filter.syncFilter = ReportFilter.SYNC_PENDING;

        ReportDataset dataset = new ReportDataBuilder().build(
            "Ritmo Abastecimento",
            "2026-04-14T10:00:00",
            filter,
            Arrays.asList(
                createRefuel("ARLA", "BRA2E19", "Carlos Mendes", RefuelStatus.NORMAL, SyncState.SYNCED, 30, "2026-04-10T08:00:00"),
                createRefuel("DIESEL", "BRA2E19", "Carlos Mendes", RefuelStatus.ALERT, SyncState.FAILED, 220, "2026-04-11T09:00:00"),
                createRefuel("DIESEL", "FLE7K21", "Marina Souza", RefuelStatus.NORMAL, SyncState.PENDING, 210, "2026-04-12T09:00:00")
            )
        );

        Assert.assertEquals(1, dataset.records.size());
        Assert.assertEquals("DIESEL", dataset.records.get(0).fuelType);
        Assert.assertEquals(220d, dataset.summary.totalDieselLiters, 0.001d);
        Assert.assertEquals(0d, dataset.summary.totalArlaLiters, 0.001d);
        Assert.assertEquals(1, dataset.summary.alertCount);
    }

    @Test
    public void build_calculatesSummaryWithDateRange() {
        ReportFilter filter = new ReportFilter();
        filter.startDate = LocalDate.of(2026, 4, 10);
        filter.endDate = LocalDate.of(2026, 4, 12);

        ReportDataset dataset = new ReportDataBuilder().build(
            "Ritmo Abastecimento",
            "2026-04-14T10:00:00",
            filter,
            Arrays.asList(
                createRefuel("ARLA", "BRA2E19", "Carlos Mendes", RefuelStatus.NORMAL, SyncState.SYNCED, 35, "2026-04-10T08:00:00"),
                createRefuel("DIESEL", "BRA2E19", "Carlos Mendes", RefuelStatus.ATTENTION, SyncState.SYNCED, 200, "2026-04-11T09:00:00"),
                createRefuel("DIESEL", "FLE7K21", "Marina Souza", RefuelStatus.ALERT, SyncState.SYNCED, 210, "2026-04-12T09:00:00"),
                createRefuel("ARLA", "QTM8A44", "Joao Pereira", RefuelStatus.NORMAL, SyncState.SYNCED, 28, "2026-04-15T09:00:00")
            )
        );

        Assert.assertEquals(3, dataset.records.size());
        Assert.assertEquals(35d, dataset.summary.totalArlaLiters, 0.001d);
        Assert.assertEquals(410d, dataset.summary.totalDieselLiters, 0.001d);
        Assert.assertEquals(2, dataset.summary.distinctVehicles);
        Assert.assertEquals(1, dataset.summary.attentionCount);
        Assert.assertEquals(1, dataset.summary.alertCount);
        Assert.assertEquals(222.5d, dataset.summary.averageLitersPerVehicle, 0.001d);
    }

    private RefuelEntity createRefuel(
        String fuelType,
        String plate,
        String driverName,
        String status,
        String syncStatus,
        double liters,
        String suppliedAtIso
    ) {
        return new RefuelEntity(
            fuelType,
            plate + suppliedAtIso,
            null,
            plate,
            "FROTA 100",
            "Modelo teste",
            1L,
            driverName,
            liters,
            100000,
            suppliedAtIso,
            "Base",
            null,
            "Observacao",
            "",
            "",
            "[]",
            "",
            "MANUAL",
            "",
            "",
            "{}",
            status,
            "Motivo",
            120,
            3.5,
            2.4,
            syncStatus,
            "",
            System.currentTimeMillis(),
            null
        );
    }
}
