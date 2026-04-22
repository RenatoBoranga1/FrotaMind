package com.example.arlacontrole.ui.indicadores;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.arlacontrole.analytics.operacional.IntegratedOperationsEngine;
import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.model.IndicatorSnapshot;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.util.ArrayList;
import java.util.List;

public class IndicatorsViewModel extends BaseArlaViewModel {

    private final MediatorLiveData<IndicatorSnapshot> snapshot = new MediatorLiveData<>();
    private final IntegratedOperationsEngine analyticsEngine = new IntegratedOperationsEngine();
    private List<RefuelEntity> allRefuels = new ArrayList<>();
    private List<SafetyEventEntity> allSafetyEvents = new ArrayList<>();

    public IndicatorsViewModel(@NonNull Application application) {
        super(application);
        snapshot.addSource(repository.observeAllRefuels(), refuels -> {
            allRefuels = refuels == null ? new ArrayList<>() : refuels;
            refresh();
        });
        snapshot.addSource(repository.observeAllSafetyEvents(), events -> {
            allSafetyEvents = events == null ? new ArrayList<>() : events;
            refresh();
        });
    }

    public LiveData<IndicatorSnapshot> getSnapshot() {
        return snapshot;
    }

    private void refresh() {
        snapshot.setValue(analyticsEngine.buildIndicators(allRefuels, allSafetyEvents));
    }
}
