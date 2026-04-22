package com.example.arlacontrole.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface VehicleDao {

    @Query("SELECT * FROM vehicles ORDER BY plate")
    LiveData<List<VehicleEntity>> observeVehicles();

    @Query("SELECT * FROM vehicles ORDER BY plate")
    List<VehicleEntity> getVehiclesSync();

    @Query("SELECT * FROM vehicles WHERE plate = :plate LIMIT 1")
    VehicleEntity findByPlateSync(String plate);

    @Query("SELECT * FROM vehicles WHERE plate = :plate LIMIT 1")
    LiveData<VehicleEntity> observeByPlate(String plate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<VehicleEntity> vehicles);

    @Query("SELECT COUNT(*) FROM vehicles")
    int countVehicles();
}
