package com.example.arlacontrole.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.arlacontrole.data.local.OdometerCalibrationEntity;
import com.example.arlacontrole.analytics.operacional.IntegratedOperationsEngine;
import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.model.DashboardSnapshot;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends BaseArlaViewModel {

    private final LiveData<List<RefuelEntity>> recentRefuels;
    private final LiveData<List<SafetyEventEntity>> recentSafetyEvents;
    private final MediatorLiveData<DashboardSnapshot> snapshot = new MediatorLiveData<>();
    private final IntegratedOperationsEngine analyticsEngine = new IntegratedOperationsEngine();
    private List<RefuelEntity> allRefuels = new ArrayList<>();
    private List<SafetyEventEntity> allSafetyEvents = new ArrayList<>();
    private int pendingCount;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        recentRefuels = repository.observeRecentRefuels(5);
        recentSafetyEvents = repository.observeRecentSafetyEvents(4);

        snapshot.addSource(repository.observeAllRefuels(), refuels -> {
            allRefuels = refuels == null ? new ArrayList<>() : refuels;
            computeSnapshot();
        });
        snapshot.addSource(repository.observeAllSafetyEvents(), events -> {
            allSafetyEvents = events == null ? new ArrayList<>() : events;
            computeSnapshot();
        });
        snapshot.addSource(repository.observePendingSyncCount(), count -> {
            pendingCount = count == null ? 0 : count;
            computeSnapshot();
        });
    }

    public LiveData<List<RefuelEntity>> getRecentRefuels() {
        return recentRefuels;
    }

    public LiveData<List<SafetyEventEntity>> getRecentSafetyEvents() {
        return recentSafetyEvents;
    }

    public LiveData<OdometerCalibrationEntity> getLatestCalibration() {
        return repository.observeLatestCalibration();
    }

    public LiveData<DashboardSnapshot> getSnapshot() {
        return snapshot;
    }

    public void syncNow() {
        repository.enqueueManualSync();
    }

    private void computeSnapshot() {
        snapshot.setValue(analyticsEngine.buildDashboard(allRefuels, allSafetyEvents, pendingCount));
    }
}
