package com.example.arlacontrole.model;

public class FuelCostSnapshot {
    public Double liters;
    public Double totalAmount;
    public Double pricePerLiter;
    public Double costPerKm;

    public boolean hasTotalAmount() {
        return totalAmount != null && totalAmount > 0d;
    }

    public boolean hasPricePerLiter() {
        return pricePerLiter != null && pricePerLiter > 0d;
    }

    public boolean hasCostPerKm() {
        return costPerKm != null && costPerKm > 0d;
    }
}
