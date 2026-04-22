package com.example.arlacontrole.data.local;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_queue", indices = {@Index(value = {"refuel_local_id"}, unique = true)})
public class SyncQueueEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "refuel_local_id")
    public long refuelLocalId;

    @ColumnInfo(name = "operation_type")
    public String operationType;

    @ColumnInfo(name = "attempt_count")
    public int attemptCount;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "last_attempt_at")
    public long lastAttemptAt;

    public SyncQueueEntity(long refuelLocalId, String operationType, int attemptCount, long createdAt, long lastAttemptAt) {
        this.refuelLocalId = refuelLocalId;
        this.operationType = operationType;
        this.attemptCount = attemptCount;
        this.createdAt = createdAt;
        this.lastAttemptAt = lastAttemptAt;
    }
}
