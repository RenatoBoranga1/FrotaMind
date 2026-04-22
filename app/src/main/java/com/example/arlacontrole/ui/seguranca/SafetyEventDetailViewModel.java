package com.example.arlacontrole.ui.seguranca;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.ui.BaseArlaViewModel;

public class SafetyEventDetailViewModel extends BaseArlaViewModel {

    public SafetyEventDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<SafetyEventEntity> getEvent(long localId) {
        return repository.observeSafetyEvent(localId);
    }

    public void updateStatus(long localId, String status, RepositoryCallback<Void> callback) {
        repository.updateSafetyEventStatus(localId, status, callback);
    }
}
