package com.example.arlacontrole.ui.seguranca;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.model.SafetyImportResult;
import com.example.arlacontrole.ui.BaseArlaViewModel;

public class SafetySpreadsheetImportViewModel extends BaseArlaViewModel {

    public SafetySpreadsheetImportViewModel(@NonNull Application application) {
        super(application);
    }

    public void importSpreadsheet(Uri uri, String fileName, RepositoryCallback<SafetyImportResult> callback) {
        repository.importSafetyEventsSpreadsheet(uri, fileName, callback);
    }
}
