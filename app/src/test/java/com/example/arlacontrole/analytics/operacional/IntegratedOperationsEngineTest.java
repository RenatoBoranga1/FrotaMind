package com.example.arlacontrole.analytics.operacional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.model.DashboardSnapshot;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.IndicatorSnapshot;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SafetyAnalysisStatus;
import com.example.arlacontrole.model.SafetyEventType;
import com.example.arlacontrole.model.SafetySeverity;
import com.example.arlacontrole.model.SyncState;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class IntegratedOperationsEngineTest {

    @Test
    public void buildDashboard_shouldIntegrateRefuelAndSafetySignals() {
        IntegratedOperationsEngine engine = new IntegratedOperationsEngine();
        List<RefuelEntity> refuels = Arrays.asList(
            refuel(FuelType.ARLA, "AAA1B11", "Carlos", 32d, RefuelStatus.NORMAL, 0),
            refuel(FuelType.DIESEL, "AAA1B11", "Carlos", 420d, RefuelStatus.ATTENTION, 0),
            refuel(FuelType.DIESEL, "AAA1B11", "Carlos", 390d, RefuelStatus.NORMAL, 0)
        );
        List<SafetyEventEntity> events = Arrays.asList(
            event("S1", SafetyEventType.INCIDENT, SafetySeverity.HIGH, SafetyAnalysisStatus.OPEN, "AAA1B11", "Carlos", 2),
            event("S2", SafetyEventType.NEAR_MISS, SafetySeverity.MODERATE, SafetyAnalysisStatus.OPEN, "AAA1B11", "Carlos", 6)
        );

        DashboardSnapshot snapshot = engine.buildDashboard(refuels, events, 2);

        assertEquals(3, snapshot.totalRefuelsToday);
        assertEquals(2, snapshot.totalSafetyEventsPeriod);
        assertEquals(2, snapshot.pendingSync);
        assertEquals(842d, snapshot.totalVolumePeriod, 0.001d);
        assertTrue(snapshot.activeAlerts >= 2);
        assertFalse(snapshot.priorityAlerts.isEmpty());
        assertFalse(snapshot.integratedInsights.isEmpty());
    }

    @Test
    public void buildIndicators_shouldExposeSafetyAndOperationalRankings() {
        IntegratedOperationsEngine engine = new IntegratedOperationsEngine();
        List<RefuelEntity> refuels = Arrays.asList(
            refuel(FuelType.ARLA, "AAA1B11", "Carlos", 32d, RefuelStatus.NORMAL, 1),
            refuel(FuelType.DIESEL, "AAA1B11", "Carlos", 420d, RefuelStatus.ALERT, 2),
            refuel(FuelType.DIESEL, "BBB2C22", "Marina", 360d, RefuelStatus.NORMAL, 3)
        );
        List<SafetyEventEntity> events = Arrays.asList(
            event("S1", SafetyEventType.ACCIDENT, SafetySeverity.CRITICAL, SafetyAnalysisStatus.OPEN, "AAA1B11", "Carlos", 1),
            event("S2", SafetyEventType.INCIDENT, SafetySeverity.HIGH, SafetyAnalysisStatus.RESOLVED, "BBB2C22", "Marina", 2)
        );

        IndicatorSnapshot snapshot = engine.buildIndicators(refuels, events);

        assertEquals(2, snapshot.totalSafetyEvents);
        assertEquals(2, snapshot.monitoredVehicles);
        assertEquals(812d, snapshot.totalMonthlyVolume, 0.001d);
        assertEquals(1, snapshot.refuelAlertCount);
        assertEquals(1, snapshot.unresolvedSafetyEvents);
        assertTrue(snapshot.priorityAlertCount >= 1);
        assertFalse(snapshot.dieselRanking.isEmpty());
        assertFalse(snapshot.vehicleSafetyRanking.isEmpty());
        assertFalse(snapshot.driverSafetyRanking.isEmpty());
        assertFalse(snapshot.priorityAlerts.isEmpty());
        assertTrue(snapshot.safetyRiskIndex >= 0d);
    }

    private RefuelEntity refuel(String fuelType, String plate, String driver, double liters, String status, int daysAgo) {
        long now = System.currentTimeMillis();
        return new RefuelEntity(
            fuelType,
            "R-" + plate + "-" + fuelType + "-" + daysAgo,
            null,
            plate,
            "FROTA 1",
            "Modelo",
            1L,
            driver,
            liters,
            120000,
            LocalDateTime.now().minusDays(daysAgo).withSecond(0).withNano(0).toString(),
            "Base",
            null,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            status,
            "",
            100,
            25d,
            2.5d,
            SyncState.PENDING,
            "",
            now,
            null
        );
    }

    private SafetyEventEntity event(
        String id,
        String type,
        String severity,
        String status,
        String plate,
        String driver,
        int daysAgo
    ) {
        long now = System.currentTimeMillis();
        return new SafetyEventEntity(
            id,
            null,
            type,
            LocalDateTime.now().minusDays(daysAgo).withSecond(0).withNano(0).toString(),
            plate,
            "FROTA 1",
            "Modelo",
            1L,
            driver,
            "Base",
            "Descricao",
            severity,
            "Causa",
            "",
            "",
            "SAFETY_OCCURRENCE",
            status,
            1,
            false,
            "",
            now,
            now
        );
    }
}
