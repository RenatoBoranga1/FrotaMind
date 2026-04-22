package com.example.arlacontrole.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "import_errors",
    indices = {
        @Index(value = {"import_log_id"}),
        @Index(value = {"issue_type"}),
        @Index(value = {"status"})
    }
)
public class ImportErrorEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    public long localId;

    @ColumnInfo(name = "import_log_id")
    public long importLogId;

    @ColumnInfo(name = "row_number")
    public int rowNumber;

    @NonNull
    @ColumnInfo(name = "driver_name")
    public String driverName;

    @NonNull
    @ColumnInfo(name = "vehicle_plate")
    public String vehiclePlate;

    @NonNull
    @ColumnInfo(name = "event_type_raw")
    public String eventTypeRaw;

    @NonNull
    @ColumnInfo(name = "issue_type")
    public String issueType;

    @NonNull
    @ColumnInfo(name = "issue_message")
    public String issueMessage;

    @NonNull
    @ColumnInfo(name = "resolution_hint")
    public String resolutionHint;

    @NonNull
    @ColumnInfo(name = "status")
    public String status;

    @NonNull
    @ColumnInfo(name = "raw_payload")
    public String rawPayload;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public ImportErrorEntity(
        long importLogId,
        int rowNumber,
        @NonNull String driverName,
        @NonNull String vehiclePlate,
        @NonNull String eventTypeRaw,
        @NonNull String issueType,
        @NonNull String issueMessage,
        @NonNull String resolutionHint,
        @NonNull String status,
        @NonNull String rawPayload,
        long createdAt
    ) {
        this.importLogId = importLogId;
        this.rowNumber = rowNumber;
        this.driverName = driverName;
        this.vehiclePlate = vehiclePlate;
        this.eventTypeRaw = eventTypeRaw;
        this.issueType = issueType;
        this.issueMessage = issueMessage;
        this.resolutionHint = resolutionHint;
        this.status = status;
        this.rawPayload = rawPayload;
        this.createdAt = createdAt;
    }
}
