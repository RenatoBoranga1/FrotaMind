package com.example.arlacontrole.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImportLogDao {

    @Insert
    long insert(ImportLogEntity entity);

    @Query("SELECT * FROM import_logs ORDER BY imported_at DESC, local_id DESC")
    LiveData<List<ImportLogEntity>> observeAll();

    @Query("SELECT * FROM import_logs WHERE local_id = :logId LIMIT 1")
    LiveData<ImportLogEntity> observeById(long logId);

    @Query("SELECT * FROM import_logs WHERE local_id = :logId LIMIT 1")
    ImportLogEntity findByIdSync(long logId);
}
