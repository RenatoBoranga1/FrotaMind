package com.example.arlacontrole.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "safety_events",
    indices = {
        @Index(value = {"client_record_id"}, unique = true),
        @Index(value = {"vehicle_plate"}),
        @Index(value = {"driver_id"}),
        @Index(value = {"analysis_status"})
    }
)
public class SafetyEventEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    public long localId;

    @NonNull
    @ColumnInfo(name = "client_record_id")
    public String clientRecordId;

    @ColumnInfo(name = "remote_id")
    public Long remoteId;

    @NonNull
    @ColumnInfo(name = "event_type")
    public String eventType;

    @NonNull
    @ColumnInfo(name = "occurred_at_iso")
    public String occurredAtIso;

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

    @NonNull
    @ColumnInfo(name = "location_name")
    public String locationName;

    @NonNull
    public String description;

    @NonNull
    public String severity;

    @NonNull
    @ColumnInfo(name = "probable_cause")
    public String probableCause;

    @NonNull
    public String notes;

    @NonNull
    @ColumnInfo(name = "evidence_photo_path")
    public String evidencePhotoPath;

    @NonNull
    @ColumnInfo(name = "evidence_category")
    public String evidenceCategory;

    @NonNull
    @ColumnInfo(name = "analysis_status")
    public String analysisStatus;

    @ColumnInfo(name = "occurrence_count")
    public int occurrenceCount;

    @ColumnInfo(name = "synced")
    public boolean synced;

    @NonNull
    @ColumnInfo(name = "sync_error")
    public String syncError;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public SafetyEventEntity(
        @NonNull String clientRecordId,
        Long remoteId,
        @NonNull String eventType,
        @NonNull String occurredAtIso,
        @NonNull String vehiclePlate,
        @NonNull String vehicleFleetCode,
        @NonNull String vehicleModel,
        long driverId,
        @NonNull String driverName,
        @NonNull String locationName,
        @NonNull String description,
        @NonNull String severity,
        @NonNull String probableCause,
        @NonNull String notes,
        @NonNull String evidencePhotoPath,
        @NonNull String evidenceCategory,
        @NonNull String analysisStatus,
        int occurrenceCount,
        boolean synced,
        @NonNull String syncError,
        long createdAt,
        long updatedAt
    ) {
        this.clientRecordId = clientRecordId;
        this.remoteId = remoteId;
        this.eventType = eventType;
        this.occurredAtIso = occurredAtIso;
        this.vehiclePlate = vehiclePlate;
        this.vehicleFleetCode = vehicleFleetCode;
        this.vehicleModel = vehicleModel;
        this.driverId = driverId;
        this.driverName = driverName;
        this.locationName = locationName;
        this.description = description;
        this.severity = severity;
        this.probableCause = probableCause;
        this.notes = notes;
        this.evidencePhotoPath = evidencePhotoPath;
        this.evidenceCategory = evidenceCategory;
        this.analysisStatus = analysisStatus;
        this.occurrenceCount = occurrenceCount;
        this.synced = synced;
        this.syncError = syncError;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean hasEvidence() {
        return evidencePhotoPath != null && !evidencePhotoPath.trim().isEmpty();
    }
}
