package com.example.arlacontrole.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "odometer_calibrations",
    indices = {
        @Index(value = {"vehicle_plate"}),
        @Index(value = {"calibration_at_iso"})
    }
)
public class OdometerCalibrationEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    public long localId;

    @NonNull
    @ColumnInfo(name = "vehicle_plate")
    public String vehiclePlate;

    @NonNull
    @ColumnInfo(name = "vehicle_fleet_code")
    public String vehicleFleetCode;

    @NonNull
    @ColumnInfo(name = "vehicle_model")
    public String vehicleModel;

    @NonNull
    @ColumnInfo(name = "calibration_at_iso")
    public String calibrationAtIso;

    @ColumnInfo(name = "odometer_km")
    public int odometerKm;

    @NonNull
    public String notes;

    @NonNull
    @ColumnInfo(name = "registered_by_name")
    public String registeredByName;

    @NonNull
    @ColumnInfo(name = "registered_at_iso")
    public String registeredAtIso;

    @NonNull
    @ColumnInfo(name = "sync_status")
    public String syncStatus;

    @NonNull
    @ColumnInfo(name = "sync_error")
    public String syncError;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "synced_at")
    public Long syncedAt;

    public OdometerCalibrationEntity(
        @NonNull String vehiclePlate,
        @NonNull String vehicleFleetCode,
        @NonNull String vehicleModel,
        @NonNull String calibrationAtIso,
        int odometerKm,
        @NonNull String notes,
        @NonNull String registeredByName,
        @NonNull String registeredAtIso,
        @NonNull String syncStatus,
        @NonNull String syncError,
        long createdAt,
        Long syncedAt
    ) {
        this.vehiclePlate = vehiclePlate;
        this.vehicleFleetCode = vehicleFleetCode;
        this.vehicleModel = vehicleModel;
        this.calibrationAtIso = calibrationAtIso;
        this.odometerKm = odometerKm;
        this.notes = notes;
        this.registeredByName = registeredByName;
        this.registeredAtIso = registeredAtIso;
        this.syncStatus = syncStatus;
        this.syncError = syncError;
        this.createdAt = createdAt;
        this.syncedAt = syncedAt;
    }
}
