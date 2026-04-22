package com.example.arlacontrole.analytics.seguranca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.model.SafetyAnalysisStatus;
import com.example.arlacontrole.model.SafetyDashboardSnapshot;
import com.example.arlacontrole.model.SafetyEventType;
import com.example.arlacontrole.model.SafetySeverity;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class SafetyAnalyticsEngineTest {

    @Test
    public void buildDashboard_shouldCalculateCountsCoverageAndAlerts() {
        SafetyAnalyticsEngine engine = new SafetyAnalyticsEngine();
        List<SafetyEventEntity> events = Arrays.asList(
            event("A1", SafetyEventType.ACCIDENT, SafetySeverity.CRITICAL, SafetyAnalysisStatus.OPEN, "ABC1D23", "Carlos", true, 2),
            event("A2", SafetyEventType.INCIDENT, SafetySeverity.HIGH, SafetyAnalysisStatus.IN_REVIEW, "ABC1D23", "Carlos", false, 5),
            event("A3", SafetyEventType.NEAR_MISS, SafetySeverity.MODERATE, SafetyAnalysisStatus.RESOLVED, "XYZ9K88", "Marina", true, 9),
            event("A4", SafetyEventType.UNSAFE_CONDITION, SafetySeverity.LOW, SafetyAnalysisStatus.OPEN, "ABC1D23", "Carlos", false, 12),
            event("A5", SafetyEventType.UNSAFE_BEHAVIOR, SafetySeverity.MODERATE, SafetyAnalysisStatus.RESOLVED, "XYZ9K88", "Marina", true, 40)
        );

        SafetyDashboardSnapshot snapshot = engine.buildDashboard(events);

        assertEquals(4, snapshot.totalEvents);
        assertEquals(1, snapshot.accidents);
        assertEquals(1, snapshot.incidents);
        assertEquals(1, snapshot.nearMisses);
        assertEquals(1, snapshot.unsafeConditions);
        assertEquals(0, snapshot.unsafeBehaviors);
        assertEquals(3, snapshot.unresolvedEvents);
        assertEquals(50d, snapshot.evidenceCoveragePercent, 0.01d);
        assertEquals(25d, snapshot.resolutionRatePercent, 0.01d);
        assertEquals("controlado", snapshot.riskLevel);
        assertFalse(snapshot.priorityAlerts.isEmpty());
        assertFalse(snapshot.vehicleRanking.isEmpty());
        assertEquals("ABC1D23", snapshot.vehicleRanking.get(0).title);
    }

    @Test
    public void buildDashboard_withoutEvents_shouldReturnControlledSummary() {
        SafetyDashboardSnapshot snapshot = new SafetyAnalyticsEngine().buildDashboard(java.util.Collections.emptyList());

        assertEquals(0, snapshot.totalEvents);
        assertEquals("controlado", snapshot.riskLevel);
        assertTrue(snapshot.executiveSummary.contains("Sem eventos"));
    }

    @Test
    public void buildDashboard_shouldRespectImportedOccurrenceCount() {
        SafetyEventEntity weightedEvent = event("A6", SafetyEventType.UNSAFE_BEHAVIOR, SafetySeverity.HIGH, SafetyAnalysisStatus.OPEN, "IMP4T21", "Importador", true, 1);
        weightedEvent.occurrenceCount = 7;

        SafetyDashboardSnapshot snapshot = new SafetyAnalyticsEngine().buildDashboard(java.util.Collections.singletonList(weightedEvent));

        assertEquals(7, snapshot.totalEvents);
        assertEquals(7, snapshot.unresolvedEvents);
        assertEquals(100d, snapshot.evidenceCoveragePercent, 0.01d);
    }

    private SafetyEventEntity event(
        String id,
        String type,
        String severity,
        String status,
        String plate,
        String driver,
        boolean evidence,
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
            "Patio central",
            "Descricao do evento",
            severity,
            "Causa provavel",
            "Observacao",
            evidence ? "C:/tmp/evidence.jpg" : "",
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
