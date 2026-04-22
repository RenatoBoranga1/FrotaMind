package com.example.arlacontrole.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SyncQueueEntity entity);

    @Query("SELECT * FROM sync_queue ORDER BY created_at ASC")
    List<SyncQueueEntity> getAllSync();

    @Query("DELETE FROM sync_queue WHERE refuel_local_id = :refuelLocalId")
    void deleteByRefuelLocalId(long refuelLocalId);

    @Query("UPDATE sync_queue SET attempt_count = attempt_count + 1, last_attempt_at = :attemptAt WHERE refuel_local_id = :refuelLocalId")
    void registerAttempt(long refuelLocalId, long attemptAt);

    @Query("DELETE FROM sync_queue")
    void clearAll();
}
