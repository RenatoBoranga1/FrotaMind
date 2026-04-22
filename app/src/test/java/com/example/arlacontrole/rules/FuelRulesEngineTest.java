package com.example.arlacontrole.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.data.local.VehicleEntity;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SyncState;

import org.junit.Test;

public class FuelRulesEngineTest {

    @Test
    public void evaluateArla_returnsNormalWithinExpectedRange() {
        FuelEvaluation evaluation = FuelRulesEngine.evaluate(FuelType.ARLA, sampleVehicle(), 35.0, 120000, null);

        assertEquals(RefuelStatus.NORMAL, evaluation.status);
        assertEquals("Registro dentro do padrao esperado para o veiculo.", evaluation.reason);
        assertNull(evaluation.kmPerLiter);
    }

    @Test
    public void evaluateArla_calculatesLitersPer1000Km() {
        FuelEvaluation evaluation = FuelRulesEngine.evaluate(FuelType.ARLA, sampleVehicle(), 36.0, 130000, samplePrevious(FuelType.ARLA, 120000));

        assertEquals(Integer.valueOf(10_000), evaluation.kmSinceLastSupply);
        assertNotNull(evaluation.litersPer1000Km);
        assertEquals(3.6, evaluation.litersPer1000Km, 0.01);
    }

    @Test
    public void evaluateDiesel_calculatesKmPerLiter() {
        FuelEvaluation evaluation = FuelRulesEngine.evaluate(FuelType.DIESEL, sampleVehicle(), 200.0, 120500, samplePrevious(FuelType.DIESEL, 120000));

        assertEquals(Integer.valueOf(500), evaluation.kmSinceLastSupply);
        assertNotNull(evaluation.kmPerLiter);
        assertEquals(2.5, evaluation.kmPerLiter, 0.01);
    }

    @Test
    public void evaluateDiesel_returnsAlertWhenEfficiencyBelowExpected() {
        FuelEvaluation evaluation = FuelRulesEngine.evaluate(FuelType.DIESEL, sampleVehicle(), 300.0, 120300, samplePrevious(FuelType.DIESEL, 120000));

        assertEquals(RefuelStatus.ALERT, evaluation.status);
        assertEquals("Consumo de diesel fora da faixa esperada para o veiculo.", evaluation.reason);
    }

    @Test
    public void evaluate_returnsAttentionWhenOdometerDoesNotIncrease() {
        FuelEvaluation evaluation = FuelRulesEngine.evaluate(FuelType.ARLA, sampleVehicle(), 30.0, 119999, samplePrevious(FuelType.ARLA, 120000));

        assertEquals(RefuelStatus.ATTENTION, evaluation.status);
        assertEquals("Odometro menor ou igual ao ultimo registro do veiculo.", evaluation.reason);
    }

    private VehicleEntity sampleVehicle() {
        return new VehicleEntity(
            "BRA2E19",
            "FROTA 201",
            "VW Meteor 29.530",
            "Longa distancia",
            32.0,
            50.0,
            2.8,
            4.1,
            220.0,
            480.0,
            1.9,
            2.8,
            1L
        );
    }

    private RefuelEntity samplePrevious(String fuelType, int odometerKm) {
        return new RefuelEntity(
            fuelType,
            "client-" + fuelType + "-1",
            null,
            "BRA2E19",
            "FROTA 201",
            "VW Meteor 29.530",
            1L,
            "Carlos Mendes",
            30.0,
            odometerKm,
            "2026-04-13T07:00:00",
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
            SyncState.SYNCED,
            "",
            1L,
            1L
        );
    }
}
