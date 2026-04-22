package com.example.arlacontrole.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.arlacontrole.model.FuelCostSnapshot;

import org.junit.Test;

public class FuelCostCalculatorTest {

    @Test
    public void normalize_shouldCalculatePricePerLiterAndCostPerKm() {
        FuelCostSnapshot snapshot = FuelCostCalculator.normalize(40d, 260d, null, 120000, 120400);

        assertEquals(260d, snapshot.totalAmount, 0.01d);
        assertEquals(6.5d, snapshot.pricePerLiter, 0.01d);
        assertEquals(0.65d, snapshot.costPerKm, 0.01d);
    }

    @Test
    public void normalize_shouldCalculateTotalAmountFromPricePerLiter() {
        FuelCostSnapshot snapshot = FuelCostCalculator.normalize(32d, null, 5.75d, 98000, 98400);

        assertEquals(184d, snapshot.totalAmount, 0.01d);
        assertEquals(5.75d, snapshot.pricePerLiter, 0.01d);
        assertEquals(0.46d, snapshot.costPerKm, 0.01d);
    }

    @Test
    public void calculateCostPerKm_shouldReturnNullWhenDistanceIsInvalid() {
        assertNull(FuelCostCalculator.calculateCostPerKm(150d, 1000, 1000));
    }
}
