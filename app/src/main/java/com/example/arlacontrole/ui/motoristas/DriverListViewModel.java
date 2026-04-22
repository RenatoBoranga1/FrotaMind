package com.example.arlacontrole.ui.motoristas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.arlacontrole.data.local.DriverEntity;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.util.List;

public class DriverListViewModel extends BaseArlaViewModel {

    public DriverListViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<DriverEntity>> getDrivers() {
        return repository.observeDrivers();
    }
}
