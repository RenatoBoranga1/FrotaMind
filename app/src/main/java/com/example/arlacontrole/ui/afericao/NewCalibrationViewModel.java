package com.example.arlacontrole.ui.afericao;

import android.app.Application;

import androidx.annotation.NonNull;

import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.model.NewCalibrationInput;
import com.example.arlacontrole.ui.BaseArlaViewModel;

public class NewCalibrationViewModel extends BaseArlaViewModel {

    public NewCalibrationViewModel(@NonNull Application application) {
        super(application);
    }

    public void saveCalibration(NewCalibrationInput input, RepositoryCallback<Long> callback) {
        repository.createCalibration(input, callback);
    }
}
