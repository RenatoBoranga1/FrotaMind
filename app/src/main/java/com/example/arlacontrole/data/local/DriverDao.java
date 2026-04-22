package com.example.arlacontrole.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DriverDao {

    @Query("SELECT * FROM drivers WHERE active = 1 ORDER BY name")
    LiveData<List<DriverEntity>> observeActiveDrivers();

    @Query("SELECT * FROM drivers WHERE active = 1 ORDER BY name")
    List<DriverEntity> getActiveDriversSync();

    @Query("SELECT * FROM drivers WHERE id = :driverId LIMIT 1")
    DriverEntity findByIdSync(long driverId);

    @Query("SELECT * FROM drivers WHERE name = :driverName LIMIT 1")
    DriverEntity findByNameSync(String driverName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DriverEntity driver);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DriverEntity> drivers);

    @Query("SELECT COUNT(*) FROM drivers")
    int countDrivers();
}
