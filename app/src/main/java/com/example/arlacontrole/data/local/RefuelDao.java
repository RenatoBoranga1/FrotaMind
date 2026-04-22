package com.example.arlacontrole.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RefuelDao {

    @Insert
    long insert(RefuelEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllReplace(List<RefuelEntity> entities);

    @Update
    void update(RefuelEntity entity);

    @Query("SELECT * FROM refuels ORDER BY supplied_at_iso DESC, local_id DESC")
    LiveData<List<RefuelEntity>> observeAll();

    @Query("SELECT * FROM refuels ORDER BY supplied_at_iso DESC, local_id DESC")
    List<RefuelEntity> getAllSync();

    @Query("SELECT * FROM refuels ORDER BY supplied_at_iso DESC, local_id DESC LIMIT :limit")
    LiveData<List<RefuelEntity>> observeRecent(int limit);

    @Query("SELECT * FROM refuels WHERE local_id = :localId LIMIT 1")
    LiveData<RefuelEntity> observeById(long localId);

    @Query("SELECT * FROM refuels WHERE local_id = :localId LIMIT 1")
    RefuelEntity findByLocalIdSync(long localId);

    @Query("SELECT * FROM refuels WHERE client_record_id = :clientRecordId LIMIT 1")
    RefuelEntity findByClientRecordIdSync(String clientRecordId);

    @Query("SELECT * FROM refuels WHERE vehicle_plate = :plate ORDER BY supplied_at_iso DESC, local_id DESC")
    LiveData<List<RefuelEntity>> observeByVehicle(String plate);

    @Query("SELECT * FROM refuels WHERE vehicle_plate = :plate ORDER BY supplied_at_iso DESC, local_id DESC")
    List<RefuelEntity> getByVehicleSync(String plate);

    @Query("SELECT * FROM refuels WHERE vehicle_plate = :plate ORDER BY supplied_at_iso DESC, local_id DESC LIMIT 1")
    RefuelEntity getLatestForVehicleSync(String plate);

    @Query("SELECT * FROM refuels WHERE vehicle_plate = :plate AND fuel_type = :fuelType ORDER BY supplied_at_iso DESC, local_id DESC LIMIT 1")
    RefuelEntity getLatestForVehicleAndFuelSync(String plate, String fuelType);

    @Query("SELECT * FROM refuels WHERE fuel_type = :fuelType ORDER BY supplied_at_iso DESC, local_id DESC")
    List<RefuelEntity> getByFuelTypeSync(String fuelType);

    @Query("SELECT COUNT(*) FROM refuels WHERE sync_status != 'SYNCED'")
    LiveData<Integer> observePendingCount();

    @Query("SELECT COUNT(*) FROM refuels WHERE sync_status != 'SYNCED'")
    int getPendingCountSync();

    @Query("DELETE FROM refuels WHERE sync_status = 'SYNCED'")
    void deleteSyncedRecords();

    @Query("DELETE FROM refuels")
    void clearAll();
}
