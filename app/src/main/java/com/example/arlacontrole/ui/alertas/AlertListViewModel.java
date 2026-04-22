package com.example.arlacontrole.ui.alertas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.arlacontrole.analytics.operacional.IntegratedOperationsEngine;
import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.model.PriorityAlertItem;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.util.ArrayList;
import java.util.List;

public class AlertListViewModel extends BaseArlaViewModel {

    private final MediatorLiveData<List<PriorityAlertItem>> alerts = new MediatorLiveData<>();
    private final IntegratedOperationsEngine analyticsEngine = new IntegratedOperationsEngine();
    private List<RefuelEntity> allRefuels = new ArrayList<>();
    private List<SafetyEventEntity> allSafetyEvents = new ArrayList<>();

    public AlertListViewModel(@NonNull Application application) {
        super(application);
        alerts.addSource(repository.observeAllRefuels(), refuels -> {
            allRefuels = refuels == null ? new ArrayList<>() : refuels;
            refresh();
        });
        alerts.addSource(repository.observeAllSafetyEvents(), events -> {
            allSafetyEvents = events == null ? new ArrayList<>() : events;
            refresh();
        });
    }

    public LiveData<List<PriorityAlertItem>> getAlerts() {
        return alerts;
    }

    private void refresh() {
        List<PriorityAlertItem> result = new ArrayList<>(analyticsEngine.buildDashboard(allRefuels, allSafetyEvents, 0).priorityAlerts);
        int refuelAlerts = 0;
        for (RefuelEntity entity : allRefuels) {
            if (!RefuelStatus.NORMAL.equals(entity.statusLevel)) {
                refuelAlerts++;
            }
        }
        if (refuelAlerts > 0) {
            PriorityAlertItem item = new PriorityAlertItem();
            item.source = "ABASTECIMENTO";
            item.level = RefuelStatus.ATTENTION;
            item.title = "Anomalias operacionais de abastecimento";
            item.description = refuelAlerts + " registro(s) exigem revisao.";
            result.add(0, item);
        }
        alerts.setValue(result);
    }
}
