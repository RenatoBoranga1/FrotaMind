package com.example.arlacontrole.rules;

import static org.junit.Assert.assertEquals;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SyncState;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class HistoryFilterEngineTest {

    @Test
    public void apply_filtersByFuelTypePlateDriverAndPeriod() {
        List<RefuelEntity> source = Arrays.asList(
            buildEntity(FuelType.ARLA, "BRA2E19", "Carlos Mendes", "2026-04-10T08:00:00"),
            buildEntity(FuelType.DIESEL, "BRA2E19", "Carlos Mendes", "2026-04-12T08:00:00"),
            buildEntity(FuelType.DIESEL, "QTM8A44", "Joao Pereira", "2026-04-13T08:00:00")
        );

        List<RefuelEntity> filtered = HistoryFilterEngine.apply(
            source,
            FuelType.DIESEL,
            "BRA2",
            "carlos",
            LocalDate.of(2026, 4, 11),
            LocalDate.of(2026, 4, 12)
        );

        assertEquals(1, filtered.size());
        assertEquals(FuelType.DIESEL, filtered.get(0).fuelType);
        assertEquals("BRA2E19", filtered.get(0).vehiclePlate);
    }

    @Test
    public void normalizeFilter_returnsBlankForUnknownType() {
        assertEquals("", HistoryFilterEngine.normalizeFilter("GASOLINA"));
    }

    private RefuelEntity buildEntity(String fuelType, String plate, String driver, String suppliedAt) {
        return new RefuelEntity(
            fuelType,
            fuelType + "-" + plate + "-" + suppliedAt,
            null,
            plate,
            "FROTA",
            "Modelo",
            1L,
            driver,
            20.0,
            100000,
            suppliedAt,
            "Base",
            null,
            "",
            "",
            "",
            "[]",
            "",
            "MANUAL",
            "",
            "",
            "{}",
            RefuelStatus.NORMAL,
            "Registro dentro do padrao esperado para o veiculo.",
            null,
            null,
            null,
            SyncState.PENDING,
            "",
            1L,
            null
        );
    }
}
