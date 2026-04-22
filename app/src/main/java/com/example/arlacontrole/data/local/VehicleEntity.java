package com.example.arlacontrole.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vehicles")
public class VehicleEntity {

    @PrimaryKey
    @NonNull
    public String plate;

    @NonNull
    @ColumnInfo(name = "fleet_code")
    public String fleetCode;

    @NonNull
    public String model;

    @NonNull
    public String operation;

    @ColumnInfo(name = "expected_fill_min_liters")
    public double expectedFillMinLiters;

    @ColumnInfo(name = "expected_fill_max_liters")
    public double expectedFillMaxLiters;

    @ColumnInfo(name = "expected_per_1000_km_min")
    public double expectedPer1000KmMin;

    @ColumnInfo(name = "expected_per_1000_km_max")
    public double expectedPer1000KmMax;

    @ColumnInfo(name = "expected_diesel_fill_min_liters")
    public double expectedDieselFillMinLiters;

    @ColumnInfo(name = "expected_diesel_fill_max_liters")
    public double expectedDieselFillMaxLiters;

    @ColumnInfo(name = "expected_diesel_km_per_liter_min")
    public double expectedDieselKmPerLiterMin;

    @ColumnInfo(name = "expected_diesel_km_per_liter_max")
    public double expectedDieselKmPerLiterMax;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public VehicleEntity(
        @NonNull String plate,
        @NonNull String fleetCode,
        @NonNull String model,
        @NonNull String operation,
        double expectedFillMinLiters,
        double expectedFillMaxLiters,
        double expectedPer1000KmMin,
        double expectedPer1000KmMax,
        double expectedDieselFillMinLiters,
        double expectedDieselFillMaxLiters,
        double expectedDieselKmPerLiterMin,
        double expectedDieselKmPerLiterMax,
        long updatedAt
    ) {
        this.plate = plate;
        this.fleetCode = fleetCode;
        this.model = model;
        this.operation = operation;
        this.expectedFillMinLiters = expectedFillMinLiters;
        this.expectedFillMaxLiters = expectedFillMaxLiters;
        this.expectedPer1000KmMin = expectedPer1000KmMin;
        this.expectedPer1000KmMax = expectedPer1000KmMax;
        this.expectedDieselFillMinLiters = expectedDieselFillMinLiters;
        this.expectedDieselFillMaxLiters = expectedDieselFillMaxLiters;
        this.expectedDieselKmPerLiterMin = expectedDieselKmPerLiterMin;
        this.expectedDieselKmPerLiterMax = expectedDieselKmPerLiterMax;
        this.updatedAt = updatedAt;
    }
}
