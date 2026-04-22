package com.example.arlacontrole.ui.abastecimento;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.arlacontrole.data.local.DriverEntity;
import com.example.arlacontrole.data.local.VehicleEntity;
import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.model.FuelCostContext;
import com.example.arlacontrole.model.NewRefuelInput;
import com.example.arlacontrole.model.RefuelOdometerContext;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.util.List;

public class NewRefuelViewModel extends BaseArlaViewModel {

    public NewRefuelViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<DriverEntity>> getDrivers() {
        return repository.observeDrivers();
    }

    public LiveData<List<VehicleEntity>> getVehicles() {
        return repository.observeVehicles();
    }

    public void saveRefuel(NewRefuelInput input, RepositoryCallback<Long> callback) {
        repository.createRefuel(input, callback);
    }

    public void loadRefuelOdometerContext(String vehiclePlate, RepositoryCallback<RefuelOdometerContext> callback) {
        repository.loadRefuelOdometerContext(vehiclePlate, callback);
    }

    public void loadFuelCostContext(String fuelType, String vehiclePlate, RepositoryCallback<FuelCostContext> callback) {
        repository.loadFuelCostContext(fuelType, vehiclePlate, callback);
    }
}
