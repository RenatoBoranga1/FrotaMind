package com.example.arlacontrole.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "refuels",
    indices = {
        @Index(value = {"client_record_id"}, unique = true),
        @Index(value = {"vehicle_plate"}),
        @Index(value = {"driver_id"}),
        @Index(value = {"sync_status"})
    }
)
public class RefuelEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    public long localId;

    @NonNull
    @ColumnInfo(name = "fuel_type")
    public String fuelType;

    @NonNull
    @ColumnInfo(name = "client_record_id")
    public String clientRecordId;

    @ColumnInfo(name = "remote_id")
    public Long remoteId;

    @NonNull
    @ColumnInfo(name = "vehicle_plate")
    public String vehiclePlate;

    @NonNull
    @ColumnInfo(name = "vehicle_fleet_code")
    public String vehicleFleetCode;

    @NonNull
    @ColumnInfo(name = "vehicle_model")
    public String vehicleModel;

    @ColumnInfo(name = "driver_id")
    public long driverId;

    @NonNull
    @ColumnInfo(name = "driver_name")
    public String driverName;

    @ColumnInfo(name = "liters")
    public double liters;

    @ColumnInfo(name = "odometer_km")
    public int odometerKm;

    @ColumnInfo(name = "odometer_initial_km")
    public int odometerInitialKm;

    @ColumnInfo(name = "odometer_final_km")
    public int odometerFinalKm;

    @ColumnInfo(name = "calculated_arla_control_quantity")
    public double calculatedArlaControlQuantity;

    @ColumnInfo(name = "expected_initial_odometer_km")
    public Integer expectedInitialOdometerKm;

    @ColumnInfo(name = "odometer_divergence_km")
    public Integer odometerDivergenceKm;

    @NonNull
    @ColumnInfo(name = "supplied_at_iso")
    public String suppliedAtIso;

    @NonNull
    @ColumnInfo(name = "location_name")
    public String locationName;

    @ColumnInfo(name = "total_amount")
    public Double totalAmount;

    @ColumnInfo(name = "price_per_liter")
    public double pricePerLiter;

    @ColumnInfo(name = "cost_per_km")
    public double costPerKm;

    @NonNull
    @ColumnInfo(name = "notes")
    public String notes;

    @NonNull
    @ColumnInfo(name = "evidence_photo_path")
    public String evidencePhotoPath;

    @NonNull
    @ColumnInfo(name = "evidence_category")
    public String evidenceCategory;

    @NonNull
    @ColumnInfo(name = "checklist_payload")
    public String checklistPayload;

    @NonNull
    @ColumnInfo(name = "checklist_completed_at_iso")
    public String checklistCompletedAtIso;

    @NonNull
    @ColumnInfo(name = "data_entry_mode")
    public String dataEntryMode;

    @NonNull
    @ColumnInfo(name = "ocr_status")
    public String ocrStatus;

    @NonNull
    @ColumnInfo(name = "ocr_raw_text")
    public String ocrRawText;

    @NonNull
    @ColumnInfo(name = "ocr_metadata_json")
    public String ocrMetadataJson;

    @NonNull
    @ColumnInfo(name = "status_level")
    public String statusLevel;

    @NonNull
    @ColumnInfo(name = "status_reason")
    public String statusReason;

    @ColumnInfo(name = "km_since_last_supply")
    public Integer kmSinceLastSupply;

    @ColumnInfo(name = "liters_per_1000_km")
    public Double litersPer1000Km;

    @ColumnInfo(name = "km_per_liter")
    public Double kmPerLiter;

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

    public RefuelEntity(
        @NonNull String fuelType,
        @NonNull String clientRecordId,
        Long remoteId,
        @NonNull String vehiclePlate,
        @NonNull String vehicleFleetCode,
        @NonNull String vehicleModel,
        long driverId,
        @NonNull String driverName,
        double liters,
        int odometerKm,
        @NonNull String suppliedAtIso,
        @NonNull String locationName,
        Double totalAmount,
        @NonNull String notes,
        @NonNull String evidencePhotoPath,
        @NonNull String evidenceCategory,
        @NonNull String checklistPayload,
        @NonNull String checklistCompletedAtIso,
        @NonNull String dataEntryMode,
        @NonNull String ocrStatus,
        @NonNull String ocrRawText,
        @NonNull String ocrMetadataJson,
        @NonNull String statusLevel,
        @NonNull String statusReason,
        Integer kmSinceLastSupply,
        Double litersPer1000Km,
        Double kmPerLiter,
        @NonNull String syncStatus,
        @NonNull String syncError,
        long createdAt,
        Long syncedAt
    ) {
        this.fuelType = fuelType;
        this.clientRecordId = clientRecordId;
        this.remoteId = remoteId;
        this.vehiclePlate = vehiclePlate;
        this.vehicleFleetCode = vehicleFleetCode;
        this.vehicleModel = vehicleModel;
        this.driverId = driverId;
        this.driverName = driverName;
        this.liters = liters;
        this.odometerKm = odometerKm;
        this.suppliedAtIso = suppliedAtIso;
        this.locationName = locationName;
        this.totalAmount = totalAmount;
        this.notes = notes;
        this.evidencePhotoPath = evidencePhotoPath;
        this.evidenceCategory = evidenceCategory;
        this.checklistPayload = checklistPayload;
        this.checklistCompletedAtIso = checklistCompletedAtIso;
        this.dataEntryMode = dataEntryMode;
        this.ocrStatus = ocrStatus;
        this.ocrRawText = ocrRawText;
        this.ocrMetadataJson = ocrMetadataJson;
        this.statusLevel = statusLevel;
        this.statusReason = statusReason;
        this.kmSinceLastSupply = kmSinceLastSupply;
        this.litersPer1000Km = litersPer1000Km;
        this.kmPerLiter = kmPerLiter;
        this.syncStatus = syncStatus;
        this.syncError = syncError;
        this.createdAt = createdAt;
        this.syncedAt = syncedAt;
    }

    public boolean hasEvidence() {
        return evidencePhotoPath != null && !evidencePhotoPath.trim().isEmpty();
    }

    public boolean hasChecklist() {
        return checklistPayload != null && !checklistPayload.trim().isEmpty();
    }
}
