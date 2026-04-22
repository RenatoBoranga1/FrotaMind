package com.example.arlacontrole.ui.seguranca;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.arlacontrole.data.local.DriverEntity;
import com.example.arlacontrole.data.local.VehicleEntity;
import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.model.NewSafetyEventInput;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.util.List;

public class NewSafetyEventViewModel extends BaseArlaViewModel {

    public NewSafetyEventViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<DriverEntity>> getDrivers() {
        return repository.observeDrivers();
    }

    public LiveData<List<VehicleEntity>> getVehicles() {
        return repository.observeVehicles();
    }

    public void saveSafetyEvent(NewSafetyEventInput input, RepositoryCallback<Long> callback) {
        repository.createSafetyEvent(input, callback);
    }
}
