package com.example.arlacontrole.ui.campo;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.arlacontrole.analytics.operacional.IntegratedOperationsEngine;
import com.example.arlacontrole.data.local.OdometerCalibrationEntity;
import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.model.DashboardSnapshot;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.util.ArrayList;
import java.util.List;

public class FieldHomeViewModel extends BaseArlaViewModel {

    private final MediatorLiveData<DashboardSnapshot> snapshot = new MediatorLiveData<>();
    private final IntegratedOperationsEngine analyticsEngine = new IntegratedOperationsEngine();
    private List<RefuelEntity> allRefuels = new ArrayList<>();
    private List<SafetyEventEntity> allSafetyEvents = new ArrayList<>();
    private int pendingCount;

    public FieldHomeViewModel(@NonNull Application application) {
        super(application);
        snapshot.addSource(repository.observeAllRefuels(), refuels -> {
            allRefuels = refuels == null ? new ArrayList<>() : refuels;
            refresh();
        });
        snapshot.addSource(repository.observeAllSafetyEvents(), events -> {
            allSafetyEvents = events == null ? new ArrayList<>() : events;
            refresh();
        });
        snapshot.addSource(repository.observePendingSyncCount(), count -> {
            pendingCount = count == null ? 0 : count;
            refresh();
        });
    }

    public LiveData<List<RefuelEntity>> getRecentRefuels() {
        return repository.observeRecentRefuels(5);
    }

    public LiveData<List<SafetyEventEntity>> getRecentSafetyEvents() {
        return repository.observeRecentSafetyEvents(3);
    }

    public LiveData<Integer> getPendingSyncCount() {
        return repository.observePendingSyncCount();
    }

    public long getLastSyncAt() {
        return repository.getLastSyncAt();
    }

    public LiveData<DashboardSnapshot> getSnapshot() {
        return snapshot;
    }

    public LiveData<OdometerCalibrationEntity> getLatestCalibration() {
        return repository.observeLatestCalibration();
    }

    private void refresh() {
        snapshot.setValue(analyticsEngine.buildDashboard(allRefuels, allSafetyEvents, pendingCount));
    }
}
