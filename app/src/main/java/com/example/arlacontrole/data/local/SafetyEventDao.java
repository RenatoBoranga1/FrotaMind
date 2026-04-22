package com.example.arlacontrole.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SafetyEventDao {

    @Insert
    long insert(SafetyEventEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllReplace(List<SafetyEventEntity> entities);

    @Update
    void update(SafetyEventEntity entity);

    @Query("SELECT * FROM safety_events ORDER BY occurred_at_iso DESC, local_id DESC")
    LiveData<List<SafetyEventEntity>> observeAll();

    @Query("SELECT * FROM safety_events ORDER BY occurred_at_iso DESC, local_id DESC")
    List<SafetyEventEntity> getAllSync();

    @Query("SELECT * FROM safety_events ORDER BY occurred_at_iso DESC, local_id DESC LIMIT :limit")
    LiveData<List<SafetyEventEntity>> observeRecent(int limit);

    @Query("SELECT * FROM safety_events WHERE local_id = :localId LIMIT 1")
    LiveData<SafetyEventEntity> observeById(long localId);

    @Query("SELECT * FROM safety_events WHERE local_id = :localId LIMIT 1")
    SafetyEventEntity findByLocalIdSync(long localId);

    @Query("SELECT COUNT(*) FROM safety_events WHERE analysis_status != 'RESOLVIDO'")
    LiveData<Integer> observeOpenCount();

    @Query("DELETE FROM safety_events")
    void clearAll();
}
