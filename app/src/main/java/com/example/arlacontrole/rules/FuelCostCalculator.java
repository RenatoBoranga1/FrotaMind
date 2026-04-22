package com.example.arlacontrole.rules;

import com.example.arlacontrole.model.FuelCostSnapshot;

public final class FuelCostCalculator {

    private FuelCostCalculator() {
    }

    public static Double calculatePricePerLiter(Double totalAmount, Double liters) {
        if (totalAmount == null || liters == null || totalAmount <= 0d || liters <= 0d) {
            return null;
        }
        return totalAmount / liters;
    }

    public static Double calculateTotalAmount(Double liters, Double pricePerLiter) {
        if (liters == null || pricePerLiter == null || liters <= 0d || pricePerLiter <= 0d) {
            return null;
        }
        return liters * pricePerLiter;
    }

    public static Double calculateCostPerKm(Double totalAmount, Integer odometerInitialKm, Integer odometerFinalKm) {
        if (totalAmount == null || totalAmount <= 0d || odometerInitialKm == null || odometerFinalKm == null) {
            return null;
        }
        int distanceKm = odometerFinalKm - odometerInitialKm;
        if (distanceKm <= 0) {
            return null;
        }
        return totalAmount / distanceKm;
    }

    public static FuelCostSnapshot normalize(
        Double liters,
        Double totalAmount,
        Double pricePerLiter,
        Integer odometerInitialKm,
        Integer odometerFinalKm
    ) {
        FuelCostSnapshot snapshot = new FuelCostSnapshot();
        snapshot.liters = liters;
        snapshot.totalAmount = totalAmount;
        snapshot.pricePerLiter = pricePerLiter;

        if (snapshot.liters != null && snapshot.liters > 0d) {
            if ((snapshot.pricePerLiter == null || snapshot.pricePerLiter <= 0d) && snapshot.totalAmount != null && snapshot.totalAmount > 0d) {
                snapshot.pricePerLiter = calculatePricePerLiter(snapshot.totalAmount, snapshot.liters);
            }
            if ((snapshot.totalAmount == null || snapshot.totalAmount <= 0d) && snapshot.pricePerLiter != null && snapshot.pricePerLiter > 0d) {
                snapshot.totalAmount = calculateTotalAmount(snapshot.liters, snapshot.pricePerLiter);
            }
        }

        snapshot.costPerKm = calculateCostPerKm(snapshot.totalAmount, odometerInitialKm, odometerFinalKm);
        return snapshot;
    }
}
