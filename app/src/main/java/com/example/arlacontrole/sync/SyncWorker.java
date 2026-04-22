package com.example.arlacontrole.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.arlacontrole.ArlaControleApplication;
import com.example.arlacontrole.data.repository.ArlaRepository;
import com.example.arlacontrole.model.SyncExecutionResult;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        ArlaRepository repository = ((ArlaControleApplication) getApplicationContext()).getRepository();
        SyncExecutionResult result = repository.synchronizeBlocking();
        if (result.shouldRetry) {
            return Result.retry();
        }
        return Result.success();
    }
}
