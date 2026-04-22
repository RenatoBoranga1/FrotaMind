package com.example.arlacontrole.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OdometerCalibrationDao {

    @Insert
    long insert(OdometerCalibrationEntity entity);

    @Query("SELECT * FROM odometer_calibrations ORDER BY calibration_at_iso DESC, local_id DESC LIMIT 1")
    LiveData<OdometerCalibrationEntity> observeLatest();

    @Query("SELECT * FROM odometer_calibrations ORDER BY calibration_at_iso DESC, local_id DESC LIMIT 1")
    OdometerCalibrationEntity getLatestSync();

    @Query("SELECT * FROM odometer_calibrations WHERE local_id = :localId LIMIT 1")
    LiveData<OdometerCalibrationEntity> observeById(long localId);

    @Query("SELECT * FROM odometer_calibrations WHERE local_id = :localId LIMIT 1")
    OdometerCalibrationEntity findByIdSync(long localId);

    @Query("SELECT * FROM odometer_calibrations WHERE vehicle_plate = :plate ORDER BY calibration_at_iso DESC, local_id DESC")
    LiveData<List<OdometerCalibrationEntity>> observeHistoryByVehicle(String plate);
}
