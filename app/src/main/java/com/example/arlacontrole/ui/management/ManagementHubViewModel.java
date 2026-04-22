package com.example.arlacontrole.ui.management;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.ui.BaseArlaViewModel;

public class ManagementHubViewModel extends BaseArlaViewModel {

    public ManagementHubViewModel(@NonNull Application application) {
        super(application);
    }

    public void exportOperationalReport(@NonNull Uri destinationUri, RepositoryCallback<String> callback) {
        repository.exportOperationalReport(destinationUri, callback);
    }

    public void exportOperationalReportPdf(@NonNull Uri destinationUri, RepositoryCallback<String> callback) {
        repository.exportOperationalReportPdf(destinationUri, callback);
    }
}
