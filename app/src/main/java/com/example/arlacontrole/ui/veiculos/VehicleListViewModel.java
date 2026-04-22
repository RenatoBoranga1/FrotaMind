package com.example.arlacontrole.ui.veiculos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.arlacontrole.data.local.VehicleEntity;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.util.List;

public class VehicleListViewModel extends BaseArlaViewModel {

    public VehicleListViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<VehicleEntity>> getVehicles() {
        return repository.observeVehicles();
    }
}
