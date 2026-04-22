package com.example.arlacontrole.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "import_logs",
    indices = {
        @Index(value = {"imported_at"}),
        @Index(value = {"status"})
    }
)
public class ImportLogEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    public long localId;

    @ColumnInfo(name = "imported_at")
    public long importedAt;

    @NonNull
    @ColumnInfo(name = "user_name")
    public String userName;

    @NonNull
    @ColumnInfo(name = "file_name")
    public String fileName;

    @ColumnInfo(name = "total_records")
    public int totalRecords;

    @ColumnInfo(name = "valid_records")
    public int validRecords;

    @ColumnInfo(name = "invalid_records")
    public int invalidRecords;

    @NonNull
    public String status;

    @NonNull
    @ColumnInfo(name = "period_start_iso")
    public String periodStartIso;

    @NonNull
    @ColumnInfo(name = "period_end_iso")
    public String periodEndIso;

    @NonNull
    public String notes;

    public ImportLogEntity(
        long importedAt,
        @NonNull String userName,
        @NonNull String fileName,
        int totalRecords,
        int validRecords,
        int invalidRecords,
        @NonNull String status,
        @NonNull String periodStartIso,
        @NonNull String periodEndIso,
        @NonNull String notes
    ) {
        this.importedAt = importedAt;
        this.userName = userName;
        this.fileName = fileName;
        this.totalRecords = totalRecords;
        this.validRecords = validRecords;
        this.invalidRecords = invalidRecords;
        this.status = status;
        this.periodStartIso = periodStartIso;
        this.periodEndIso = periodEndIso;
        this.notes = notes;
    }
}
