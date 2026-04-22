package com.example.arlacontrole.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImportErrorDao {

    @Insert
    void insertAll(List<ImportErrorEntity> entities);

    @Query("SELECT * FROM import_errors WHERE import_log_id = :logId ORDER BY row_number ASC, local_id ASC")
    LiveData<List<ImportErrorEntity>> observeByImportLog(long logId);

    @Query("SELECT * FROM import_errors WHERE status = 'PENDING' ORDER BY created_at DESC, local_id DESC")
    LiveData<List<ImportErrorEntity>> observePending();

    @Query("SELECT COUNT(*) FROM import_errors WHERE status = 'PENDING'")
    LiveData<Integer> observePendingCount();

    @Query("SELECT * FROM import_errors WHERE local_id = :errorId LIMIT 1")
    ImportErrorEntity findByIdSync(long errorId);

    @Query("UPDATE import_errors SET status = :status WHERE local_id = :errorId")
    void updateStatus(long errorId, String status);
}
