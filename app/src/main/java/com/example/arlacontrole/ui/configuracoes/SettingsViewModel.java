package com.example.arlacontrole.ui.configuracoes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.ui.BaseArlaViewModel;

public class SettingsViewModel extends BaseArlaViewModel {

    public SettingsViewModel(@NonNull Application application) {
        super(application);
    }

    public String getApiUrl() {
        return repository.getApiBaseUrl();
    }

    public void saveApiUrl(String url) {
        repository.saveApiBaseUrl(url);
    }

    public long getLastSyncAt() {
        return repository.getLastSyncAt();
    }

    public String getLastSyncMessage() {
        return repository.getLastSyncMessage();
    }

    public LiveData<Integer> getPendingSyncCount() {
        return repository.observePendingSyncCount();
    }

    public void testConnection(RepositoryCallback<String> callback) {
        repository.testConnection(callback);
    }

    public void syncNow() {
        repository.enqueueManualSync();
    }
}
