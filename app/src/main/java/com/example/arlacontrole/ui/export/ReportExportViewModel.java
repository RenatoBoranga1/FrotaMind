package com.example.arlacontrole.ui.export;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.arlacontrole.data.local.ArlaDatabase;
import com.example.arlacontrole.data.local.DriverEntity;
import com.example.arlacontrole.data.local.VehicleEntity;
import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.export.ExportManager;
import com.example.arlacontrole.export.ExportedReport;
import com.example.arlacontrole.export.ReportFilter;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.ui.BaseArlaViewModel;

import java.util.List;

public class ReportExportViewModel extends BaseArlaViewModel {

    private final ExportManager exportManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ReportExportViewModel(@NonNull Application application) {
        super(application);
        exportManager = new ExportManager(application);
    }

    public LiveData<List<VehicleEntity>> getVehicles() {
        return repository.observeVehicles();
    }

    public LiveData<List<DriverEntity>> getDrivers() {
        return repository.observeDrivers();
    }

    public boolean canExportReports() {
        return UserRole.canExportReports(repository.getCurrentSession().role);
    }

    public void exportReport(@NonNull ReportFilter filter, @NonNull RepositoryCallback<ExportedReport> callback) {
        ArlaDatabase.databaseWriteExecutor.execute(() -> {
            try {
                ExportedReport report = exportManager.generateReport(filter);
                mainHandler.post(() -> callback.onSuccess(report));
            } catch (Exception exception) {
                String message = exception.getMessage() == null ? "Nao foi possivel exportar o relatorio." : exception.getMessage();
                mainHandler.post(() -> callback.onError(message));
            }
        });
    }
}
