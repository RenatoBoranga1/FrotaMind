package com.example.arlacontrole.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;

import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.model.AuthSession;
import com.example.arlacontrole.ui.BaseArlaViewModel;

public class LoginViewModel extends BaseArlaViewModel {

    public LoginViewModel(@NonNull Application application) {
        super(application);
    }

    public boolean hasValidSession() {
        return repository.hasValidSession();
    }

    public void login(String email, String password, RepositoryCallback<AuthSession> callback) {
        repository.login(email, password, callback);
    }
}
