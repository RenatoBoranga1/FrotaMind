package com.example.arlacontrole.ui.historico;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.rules.HistoryFilterEngine;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistoryViewModel extends BaseArlaViewModel {

    private final MediatorLiveData<List<RefuelEntity>> filteredRefuels = new MediatorLiveData<>();
    private List<RefuelEntity> sourceRefuels = new ArrayList<>();
    private String fuelTypeFilter = "";
    private String plateFilter = "";
    private String driverFilter = "";
    private LocalDate startDate;
    private LocalDate endDate;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        filteredRefuels.addSource(repository.observeAllRefuels(), refuels -> {
            sourceRefuels = refuels == null ? new ArrayList<>() : refuels;
            applyFilters();
        });
    }

    public LiveData<List<RefuelEntity>> getFilteredRefuels() {
        return filteredRefuels;
    }

    public void setFilters(String fuelType, String plate, String driver, LocalDate startDate, LocalDate endDate) {
        fuelTypeFilter = HistoryFilterEngine.normalizeFilter(fuelType);
        plateFilter = plate == null ? "" : plate.trim().toUpperCase();
        driverFilter = driver == null ? "" : driver.trim().toLowerCase();
        this.startDate = startDate;
        this.endDate = endDate;
        applyFilters();
    }

    public void clearFilters() {
        fuelTypeFilter = "";
        plateFilter = "";
        driverFilter = "";
        startDate = null;
        endDate = null;
        applyFilters();
    }

    public void syncNow() {
        repository.enqueueManualSync();
    }

    private void applyFilters() {
        filteredRefuels.setValue(
            HistoryFilterEngine.apply(sourceRefuels, fuelTypeFilter, plateFilter, driverFilter, startDate, endDate)
        );
    }
}
