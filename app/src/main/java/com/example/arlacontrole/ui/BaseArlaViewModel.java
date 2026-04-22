package com.example.arlacontrole.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.arlacontrole.ArlaControleApplication;
import com.example.arlacontrole.data.repository.ArlaRepository;

public abstract class BaseArlaViewModel extends AndroidViewModel {

    protected final ArlaRepository repository;

    public BaseArlaViewModel(@NonNull Application application) {
        super(application);
        repository = ((ArlaControleApplication) application).getRepository();
    }
}
