package com.example.arlacontrole.ui.sync;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.arlacontrole.model.AuthSession;
import com.example.arlacontrole.ui.BaseArlaViewModel;

public class SyncStatusViewModel extends BaseArlaViewModel {

    public SyncStatusViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Integer> getPendingSyncCount() {
        return repository.observePendingSyncCount();
    }

    public long getLastSyncAt() {
        return repository.getLastSyncAt();
    }

    public String getLastSyncMessage() {
        return repository.getLastSyncMessage();
    }

    public AuthSession getSession() {
        return repository.getCurrentSession();
    }

    public void syncNow() {
        repository.enqueueManualSync();
    }
}
