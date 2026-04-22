package com.example.arlacontrole.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OdometerCalibrationMediaDao {

    @Insert
    void insertAll(List<OdometerCalibrationMediaEntity> media);

    @Query("SELECT * FROM odometer_calibration_media WHERE calibration_local_id = :calibrationLocalId ORDER BY local_id ASC")
    LiveData<List<OdometerCalibrationMediaEntity>> observeByCalibration(long calibrationLocalId);

    @Query("SELECT * FROM odometer_calibration_media WHERE calibration_local_id = :calibrationLocalId ORDER BY local_id ASC")
    List<OdometerCalibrationMediaEntity> getByCalibrationSync(long calibrationLocalId);
}
