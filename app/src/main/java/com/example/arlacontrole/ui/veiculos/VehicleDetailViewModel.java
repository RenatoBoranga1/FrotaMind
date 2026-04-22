package com.example.arlacontrole.ui.veiculos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.data.local.VehicleEntity;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.VehicleDetailSnapshot;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.util.ArrayList;
import java.util.List;

public class VehicleDetailViewModel extends BaseArlaViewModel {

    private final MutableLiveData<String> selectedPlate = new MutableLiveData<>();
    private final LiveData<VehicleEntity> vehicle = Transformations.switchMap(selectedPlate, repository::observeVehicle);
    private final LiveData<List<RefuelEntity>> history = Transformations.switchMap(selectedPlate, repository::observeVehicleHistory);
    private final MediatorLiveData<VehicleDetailSnapshot> snapshot = new MediatorLiveData<>();
    private VehicleEntity currentVehicle;
    private List<RefuelEntity> currentHistory = new ArrayList<>();

    public VehicleDetailViewModel(@NonNull Application application) {
        super(application);
        snapshot.addSource(vehicle, item -> {
            currentVehicle = item;
            computeSnapshot();
        });
        snapshot.addSource(history, items -> {
            currentHistory = items == null ? new ArrayList<>() : items;
            computeSnapshot();
        });
    }

    public void setVehiclePlate(String plate) {
        selectedPlate.setValue(plate);
    }

    public LiveData<List<RefuelEntity>> getHistory() {
        return history;
    }

    public LiveData<VehicleDetailSnapshot> getSnapshot() {
        return snapshot;
    }

    private void computeSnapshot() {
        if (currentVehicle == null) {
            return;
        }

        VehicleDetailSnapshot item = new VehicleDetailSnapshot();
        item.plate = currentVehicle.plate;
        item.fleetCode = currentVehicle.fleetCode;
        item.model = currentVehicle.model;
        item.operation = currentVehicle.operation;
        item.expectedArlaMin = currentVehicle.expectedPer1000KmMin;
        item.expectedArlaMax = currentVehicle.expectedPer1000KmMax;
        item.expectedDieselKmPerLiterMin = currentVehicle.expectedDieselKmPerLiterMin;
        item.expectedDieselKmPerLiterMax = currentVehicle.expectedDieselKmPerLiterMax;
        item.totalRecords = currentHistory.size();
        item.currentStatus = RefuelStatus.NORMAL;

        double totalArlaMetric = 0d;
        double totalDieselMetric = 0d;
        int arlaCounter = 0;
        int dieselCounter = 0;

        for (RefuelEntity entity : currentHistory) {
            if (FuelType.DIESEL.equals(entity.fuelType)) {
                item.totalDiesel += entity.liters;
                if (entity.kmPerLiter != null) {
                    totalDieselMetric += entity.kmPerLiter;
                    dieselCounter++;
                }
            } else {
                item.totalArla += entity.liters;
                if (entity.litersPer1000Km != null) {
                    totalArlaMetric += entity.litersPer1000Km;
                    arlaCounter++;
                }
            }

            if (!RefuelStatus.NORMAL.equals(entity.statusLevel)) {
                item.alertCount++;
            }
            if (RefuelStatus.priority(entity.statusLevel) > RefuelStatus.priority(item.currentStatus)) {
                item.currentStatus = entity.statusLevel;
            }
        }

        item.averageArlaConsumption = arlaCounter == 0 ? 0d : totalArlaMetric / arlaCounter;
        item.averageDieselConsumption = dieselCounter == 0 ? 0d : totalDieselMetric / dieselCounter;
        snapshot.setValue(item);
    }
}
