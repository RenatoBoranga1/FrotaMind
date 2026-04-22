package com.example.arlacontrole.ui.afericao;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.arlacontrole.data.local.OdometerCalibrationEntity;
import com.example.arlacontrole.data.local.OdometerCalibrationMediaEntity;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.util.List;

public class CalibrationDetailViewModel extends BaseArlaViewModel {

    private final MutableLiveData<Long> selectedId = new MutableLiveData<>();
    private final LiveData<OdometerCalibrationEntity> calibration =
        Transformations.switchMap(selectedId, repository::observeCalibration);
    private final LiveData<List<OdometerCalibrationMediaEntity>> media =
        Transformations.switchMap(selectedId, repository::observeCalibrationMedia);

    public CalibrationDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public void setCalibrationId(long localId) {
        selectedId.setValue(localId);
    }

    public LiveData<OdometerCalibrationEntity> getCalibration() {
        return calibration;
    }

    public LiveData<List<OdometerCalibrationMediaEntity>> getMedia() {
        return media;
    }
}
