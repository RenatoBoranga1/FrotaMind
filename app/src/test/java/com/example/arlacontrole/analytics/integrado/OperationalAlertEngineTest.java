package com.example.arlacontrole.analytics.integrado;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.model.FinancialDashboardSnapshot;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.PriorityAlertItem;
import com.example.arlacontrole.model.RefuelEntryMode;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SafetyAnalysisStatus;
import com.example.arlacontrole.model.SafetyEventType;
import com.example.arlacontrole.model.SafetySeverity;
import com.example.arlacontrole.model.SyncState;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class OperationalAlertEngineTest {

    @Test
    public void buildAlerts_shouldFlagVehicleAndDriverWhenRiskAndCostRiseTogether() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        RefuelEntity highRiskVehicle = refuel("op-1", "AAA1B11", "Carlos", 1000d, now.minusDays(2), 100000, 100400);
        RefuelEntity baselineVehicle = refuel("op-2", "BBB2C22", "Marina", 200d, now.minusDays(4), 90000, 90400);
        SafetyEventEntity firstEvent = event("evt-1", "AAA1B11", "Carlos", now.minusDays(1), 2);
        SafetyEventEntity secondEvent = event("evt-2", "AAA1B11", "Carlos", now.minusDays(3), 1);

        FinancialDashboardSnapshot financialSnapshot = new FinancialDashboardSnapshot();
        financialSnapshot.averageCostPerKm = 1d;

        OperationalAlertEngine engine = new OperationalAlertEngine();
        List<PriorityAlertItem> alerts = engine.buildAlerts(
            Arrays.asList(highRiskVehicle, baselineVehicle),
            Arrays.asList(firstEvent, secondEvent),
            financialSnapshot
        );
        List<String> insights = engine.buildInsights(
            Arrays.asList(highRiskVehicle, baselineVehicle),
            Arrays.asList(firstEvent, secondEvent),
            financialSnapshot
        );

        assertFalse(alerts.isEmpty());
        assertTrue(alerts.stream().anyMatch(item -> item.title.contains("Veiculo com custo alto")));
        assertTrue(alerts.stream().anyMatch(item -> item.description.contains("AAA1B11")));
        assertTrue(insights.stream().anyMatch(item -> item.contains("AAA1B11")));
        assertTrue(insights.stream().anyMatch(item -> item.contains("Carlos")));
    }

    private RefuelEntity refuel(
        String id,
        String plate,
        String driver,
        double totalAmount,
        LocalDateTime suppliedAt,
        int odometerInitial,
        int odometerFinal
    ) {
        RefuelEntity entity = new RefuelEntity(
            FuelType.DIESEL,
            id,
            null,
            plate,
            "FROTA 9",
            "Cavalo",
            1L,
            driver,
            100d,
            odometerFinal,
            suppliedAt.toString(),
            "Rodovia",
            totalAmount,
            "",
            "",
            "",
            "",
            "",
            RefuelEntryMode.MANUAL,
            "",
            "",
            "",
            RefuelStatus.NORMAL,
            "",
            odometerFinal - odometerInitial,
            null,
            2.8d,
            SyncState.PENDING,
            "",
            System.currentTimeMillis(),
            null
        );
        entity.odometerInitialKm = odometerInitial;
        entity.odometerFinalKm = odometerFinal;
        entity.costPerKm = totalAmount / (odometerFinal - odometerInitial);
        entity.pricePerLiter = totalAmount / 100d;
        return entity;
    }

    private SafetyEventEntity event(String id, String plate, String driver, LocalDateTime occurredAt, int quantity) {
        SafetyEventEntity entity = new SafetyEventEntity(
            id,
            null,
            SafetyEventType.UNSAFE_BEHAVIOR,
            occurredAt.toString(),
            plate,
            "FROTA 9",
            "Cavalo",
            1L,
            driver,
            "Rodovia",
            "Comportamento fora do padrao",
            SafetySeverity.HIGH,
            "",
            "",
            "",
            "MAXTRACK",
            SafetyAnalysisStatus.OPEN,
            quantity,
            false,
            "",
            System.currentTimeMillis(),
            System.currentTimeMillis()
        );
        entity.occurrenceCount = quantity;
        return entity;
    }
}
