package com.example.arlacontrole.analytics.operacional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.model.FinancialDashboardSnapshot;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.RefuelEntryMode;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SyncState;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

public class FinancialIndicatorCalculatorTest {

    @Test
    public void build_shouldSummarizeCostsAverageAndRanking() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        RefuelEntity diesel = refuel("rf-1", FuelType.DIESEL, "ABC1D23", "Carlos", 100d, 650d, 6.5d, now.minusDays(2), 100000, 100500);
        RefuelEntity arla = refuel("rf-2", FuelType.ARLA, "ABC1D23", "Carlos", 20d, 100d, 5d, now.minusDays(1), 100500, 101000);
        RefuelEntity previous = refuel("rf-3", FuelType.DIESEL, "XYZ9K88", "Marina", 80d, 500d, 6.25d, now.minusMonths(1).minusDays(1), 90000, 90500);

        FinancialDashboardSnapshot snapshot = new FinancialIndicatorCalculator().build(Arrays.asList(diesel, arla, previous));

        assertEquals(750d, snapshot.totalCostPeriod, 0.01d);
        assertEquals(100d, snapshot.totalArlaCostPeriod, 0.01d);
        assertEquals(650d, snapshot.totalDieselCostPeriod, 0.01d);
        assertEquals(0.75d, snapshot.averageCostPerKm, 0.01d);
        assertEquals(50d, snapshot.costVariationPercent, 0.01d);
        assertFalse(snapshot.vehicleRanking.isEmpty());
        assertEquals("ABC1D23 | FROTA 1", snapshot.vehicleRanking.get(0).title);
    }

    private RefuelEntity refuel(
        String id,
        String fuelType,
        String plate,
        String driver,
        double liters,
        double totalAmount,
        double pricePerLiter,
        LocalDateTime suppliedAt,
        int odometerInitial,
        int odometerFinal
    ) {
        RefuelEntity entity = new RefuelEntity(
            fuelType,
            id,
            null,
            plate,
            "FROTA 1",
            "Cavalo",
            1L,
            driver,
            liters,
            odometerFinal,
            suppliedAt.toString(),
            "Patio",
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
            fuelType.equals(FuelType.ARLA) ? liters : null,
            fuelType.equals(FuelType.DIESEL) ? 3.5d : null,
            SyncState.PENDING,
            "",
            System.currentTimeMillis(),
            null
        );
        entity.odometerInitialKm = odometerInitial;
        entity.odometerFinalKm = odometerFinal;
        entity.pricePerLiter = pricePerLiter;
        entity.costPerKm = totalAmount / (odometerFinal - odometerInitial);
        return entity;
    }
}
