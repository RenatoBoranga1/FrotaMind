package com.example.arlacontrole.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.arlacontrole.model.CostValidationResult;
import com.example.arlacontrole.model.FuelCostSnapshot;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.RefuelStatus;

import org.junit.Test;

public class CostValidatorTest {

    @Test
    public void validate_shouldRejectWhenTotalAndPriceAreMissing() {
        FuelCostSnapshot snapshot = new FuelCostSnapshot();
        snapshot.liters = 25d;

        CostValidationResult result = CostValidator.validate(FuelType.DIESEL, snapshot, null);

        assertFalse(result.valid);
        assertEquals(RefuelStatus.ALERT, result.level);
    }

    @Test
    public void validate_shouldWarnWhenPricePerLiterIsOutsideExpectedRange() {
        FuelCostSnapshot snapshot = FuelCostCalculator.normalize(20d, 500d, null, 1000, 1400);

        CostValidationResult result = CostValidator.validate(FuelType.DIESEL, snapshot, null);

        assertTrue(result.valid);
        assertEquals(RefuelStatus.ATTENTION, result.level);
        assertTrue(result.message.contains("fora da faixa"));
    }

    @Test
    public void validate_shouldAlertWhenCostPerKmIsTooHigh() {
        FuelCostSnapshot snapshot = FuelCostCalculator.normalize(20d, 300d, null, 1000, 1010);

        CostValidationResult result = CostValidator.validate(FuelType.ARLA, snapshot, null);

        assertTrue(result.valid);
        assertEquals(RefuelStatus.ALERT, result.level);
        assertTrue(result.message.contains("Custo por km"));
    }
}
