package com.example.arlacontrole.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "odometer_calibration_media",
    indices = {
        @Index(value = {"calibration_local_id"})
    }
)
public class OdometerCalibrationMediaEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    public long localId;

    @ColumnInfo(name = "calibration_local_id")
    public long calibrationLocalId;

    @NonNull
    @ColumnInfo(name = "media_type")
    public String mediaType;

    @NonNull
    @ColumnInfo(name = "file_path")
    public String filePath;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public OdometerCalibrationMediaEntity(long calibrationLocalId, @NonNull String mediaType, @NonNull String filePath, long createdAt) {
        this.calibrationLocalId = calibrationLocalId;
        this.mediaType = mediaType;
        this.filePath = filePath;
        this.createdAt = createdAt;
    }
}
