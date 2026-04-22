package com.example.arlacontrole.ui.seguranca;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.arlacontrole.analytics.seguranca.SafetyAnalyticsEngine;
import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.model.SafetyDashboardSnapshot;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.util.ArrayList;
import java.util.List;

public class SafetyHubViewModel extends BaseArlaViewModel {

    private final SafetyAnalyticsEngine analyticsEngine = new SafetyAnalyticsEngine();
    private final MediatorLiveData<SafetyDashboardSnapshot> snapshot = new MediatorLiveData<>();
    private final LiveData<List<SafetyEventEntity>> recentEvents;
    private List<SafetyEventEntity> allEvents = new ArrayList<>();

    public SafetyHubViewModel(@NonNull Application application) {
        super(application);
        recentEvents = repository.observeRecentSafetyEvents(5);
        snapshot.addSource(repository.observeAllSafetyEvents(), events -> {
            allEvents = events == null ? new ArrayList<>() : events;
            snapshot.setValue(analyticsEngine.buildDashboard(allEvents));
        });
    }

    public LiveData<SafetyDashboardSnapshot> getSnapshot() {
        return snapshot;
    }

    public LiveData<List<SafetyEventEntity>> getRecentEvents() {
        return recentEvents;
    }
}
