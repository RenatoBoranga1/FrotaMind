package com.example.arlacontrole;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import com.example.arlacontrole.data.local.ArlaDatabase;
import com.example.arlacontrole.data.repository.ArlaRepository;
import com.example.arlacontrole.sync.SyncScheduler;

public class ArlaControleApplication extends Application implements Configuration.Provider {

    private ArlaRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        ArlaDatabase database = ArlaDatabase.getInstance(this);
        database.seedDefaults();
        repository = new ArlaRepository(this, database);
        SyncScheduler.schedulePeriodic(this);
    }

    public ArlaRepository getRepository() {
        return repository;
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build();
    }
}
