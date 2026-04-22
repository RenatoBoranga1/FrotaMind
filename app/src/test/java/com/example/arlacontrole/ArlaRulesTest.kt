package com.example.arlacontrole

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArlaRulesTest {
    private val vehicle = VehicleProfile(
        plate = "BRA2E19",
        model = "VW Meteor 29.530",
        operation = "Longa distancia",
        expectedFillMinLiters = 32.0,
        expectedFillMaxLiters = 50.0,
        expectedPer1000KmMin = 2.8,
        expectedPer1000KmMax = 4.1,
    )

    @Test
    fun returnsNormalWhenConsumptionIsWithinExpectedRange() {
        val previousRecord = ArlaRecord(
            id = 1,
            plate = vehicle.plate,
            driverName = "Carlos",
            liters = 40.0,
            odometerKm = 100000,
            recordedAtIso = "2026-04-08T09:00:00",
            note = "",
            status = ArlaStatus.NORMAL,
            analysisReason = "",
            kmSinceLastSupply = null,
            litersPer1000Km = null,
        )

        val result = ArlaRules.evaluate(
            profile = vehicle,
            liters = 38.0,
            odometerKm = 110000,
            previousRecord = previousRecord
        )

        assertEquals(ArlaStatus.NORMAL, result.status)
        assertTrue(result.reason.contains("dentro do padrao"))
    }

    @Test
    fun returnsAlertWhenFillIsFarAboveExpectedRange() {
        val result = ArlaRules.evaluate(
            profile = vehicle,
            liters = 60.0,
            odometerKm = 120000,
            previousRecord = null
        )

        assertEquals(ArlaStatus.ALERTA, result.status)
    }

    @Test
    fun returnsAttentionWhenOdometerGoesBackwards() {
        val previousRecord = ArlaRecord(
            id = 2,
            plate = vehicle.plate,
            driverName = "Carlos",
            liters = 40.0,
            odometerKm = 150000,
            recordedAtIso = "2026-04-08T09:00:00",
            note = "",
            status = ArlaStatus.NORMAL,
            analysisReason = "",
            kmSinceLastSupply = null,
            litersPer1000Km = null,
        )

        val result = ArlaRules.evaluate(
            profile = vehicle,
            liters = 35.0,
            odometerKm = 149000,
            previousRecord = previousRecord
        )

        assertEquals(ArlaStatus.ATENCAO, result.status)
    }
}
