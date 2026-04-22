package com.example.arlacontrole.ui.export;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.arlacontrole.R;
import com.example.arlacontrole.data.local.DriverEntity;
import com.example.arlacontrole.data.local.VehicleEntity;
import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.databinding.BottomSheetReportExportBinding;
import com.example.arlacontrole.export.ExportedReport;
import com.example.arlacontrole.export.ReportFilter;
import com.example.arlacontrole.export.ReportFormat;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.utils.FormatUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportExportBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_FUEL_TYPE = "fuel_type";
    private static final String ARG_PLATE = "plate";
    private static final String ARG_DRIVER = "driver";
    private static final String ARG_START_DATE = "start_date";
    private static final String ARG_END_DATE = "end_date";

    private BottomSheetReportExportBinding binding;
    private ReportExportViewModel viewModel;
    private final Map<String, String> vehicleOptions = new LinkedHashMap<>();
    private String selectedFormat = ReportFormat.PDF;
    private String selectedFuelType = "";
    private String selectedStatus = "";
    private String selectedSync = ReportFilter.SYNC_ALL;
    private LocalDate startDate;
    private LocalDate endDate;

    public static ReportExportBottomSheet newInstance(
        @Nullable String fuelType,
        @Nullable String plate,
        @Nullable String driver,
        @Nullable LocalDate startDate,
        @Nullable LocalDate endDate
    ) {
        Bundle args = new Bundle();
        args.putString(ARG_FUEL_TYPE, fuelType);
        args.putString(ARG_PLATE, plate);
        args.putString(ARG_DRIVER, driver);
        args.putString(ARG_START_DATE, startDate == null ? "" : startDate.toString());
        args.putString(ARG_END_DATE, endDate == null ? "" : endDate.toString());
        ReportExportBottomSheet sheet = new ReportExportBottomSheet();
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetReportExportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReportExportViewModel.class);
        if (!viewModel.canExportReports()) {
            dismissAllowingStateLoss();
            return;
        }

        binding.toggleExportFormat.check(R.id.buttonExportFormatPdf);
        binding.toggleExportFuel.check(R.id.buttonExportFuelAll);
        binding.toggleExportStatus.check(R.id.buttonExportStatusAll);
        binding.toggleExportSync.check(R.id.buttonExportSyncAll);

        binding.toggleExportFormat.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                selectedFormat = checkedId == R.id.buttonExportFormatXlsx ? ReportFormat.XLSX : ReportFormat.PDF;
            }
        });
        binding.toggleExportFuel.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.buttonExportFuelArla) {
                selectedFuelType = FuelType.ARLA;
            } else if (checkedId == R.id.buttonExportFuelDiesel) {
                selectedFuelType = FuelType.DIESEL;
            } else {
                selectedFuelType = "";
            }
        });
        binding.toggleExportStatus.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.buttonExportStatusNormal) {
                selectedStatus = RefuelStatus.NORMAL;
            } else if (checkedId == R.id.buttonExportStatusAttention) {
                selectedStatus = RefuelStatus.ATTENTION;
            } else if (checkedId == R.id.buttonExportStatusAlert) {
                selectedStatus = RefuelStatus.ALERT;
            } else {
                selectedStatus = "";
            }
        });
        binding.toggleExportSync.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.buttonExportSyncSynced) {
                selectedSync = ReportFilter.SYNC_SYNCED;
            } else if (checkedId == R.id.buttonExportSyncPending) {
                selectedSync = ReportFilter.SYNC_PENDING;
            } else {
                selectedSync = ReportFilter.SYNC_ALL;
            }
        });

        binding.buttonExportStartDate.setOnClickListener(v -> openDatePicker(true));
        binding.buttonExportEndDate.setOnClickListener(v -> openDatePicker(false));
        binding.buttonGenerateExport.setOnClickListener(v -> generateReport());

        viewModel.getVehicles().observe(getViewLifecycleOwner(), this::bindVehicles);
        viewModel.getDrivers().observe(getViewLifecycleOwner(), this::bindDrivers);
        applyArguments();
    }

    private void bindVehicles(List<VehicleEntity> vehicles) {
        vehicleOptions.clear();
        java.util.ArrayList<String> labels = new java.util.ArrayList<>();
        if (vehicles != null) {
            for (VehicleEntity vehicle : vehicles) {
                String label = vehicle.plate + " - " + vehicle.fleetCode + " - " + vehicle.model;
                vehicleOptions.put(label, vehicle.plate);
                labels.add(label);
            }
        }
        binding.inputExportVehicle.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, labels));
    }

    private void bindDrivers(List<DriverEntity> drivers) {
        java.util.ArrayList<String> labels = new java.util.ArrayList<>();
        if (drivers != null) {
            for (DriverEntity driver : drivers) {
                labels.add(driver.name);
            }
        }
        binding.inputExportDriver.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, labels));
    }

    private void applyArguments() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        String fuel = args.getString(ARG_FUEL_TYPE, "");
        if (FuelType.ARLA.equals(fuel)) {
            selectedFuelType = FuelType.ARLA;
            binding.toggleExportFuel.check(R.id.buttonExportFuelArla);
        } else if (FuelType.DIESEL.equals(fuel)) {
            selectedFuelType = FuelType.DIESEL;
            binding.toggleExportFuel.check(R.id.buttonExportFuelDiesel);
        }
        String plate = args.getString(ARG_PLATE, "");
        if (!plate.trim().isEmpty()) {
            binding.inputExportVehicle.setText(plate.trim().toUpperCase(), false);
        }
        String driver = args.getString(ARG_DRIVER, "");
        if (!driver.trim().isEmpty()) {
            binding.inputExportDriver.setText(driver.trim(), false);
        }
        String start = args.getString(ARG_START_DATE, "");
        if (!start.isEmpty()) {
            startDate = LocalDate.parse(start);
            binding.buttonExportStartDate.setText(FormatUtils.formatDate(startDate.atStartOfDay().toString()));
        }
        String end = args.getString(ARG_END_DATE, "");
        if (!end.isEmpty()) {
            endDate = LocalDate.parse(end);
            binding.buttonExportEndDate.setText(FormatUtils.formatDate(endDate.atStartOfDay().toString()));
        }
    }

    private void openDatePicker(boolean isStart) {
        LocalDate baseDate = isStart ? (startDate == null ? LocalDate.now() : startDate) : (endDate == null ? LocalDate.now() : endDate);
        DatePickerDialog dialog = new DatePickerDialog(
            requireContext(),
            (picker, year, month, dayOfMonth) -> {
                LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                if (isStart) {
                    startDate = selectedDate;
                    binding.buttonExportStartDate.setText(FormatUtils.formatDate(selectedDate.atStartOfDay().toString()));
                } else {
                    endDate = selectedDate;
                    binding.buttonExportEndDate.setText(FormatUtils.formatDate(selectedDate.atStartOfDay().toString()));
                }
            },
            baseDate.getYear(),
            baseDate.getMonthValue() - 1,
            baseDate.getDayOfMonth()
        );
        dialog.show();
    }

    private void generateReport() {
        ReportFilter filter = new ReportFilter();
        filter.format = selectedFormat;
        filter.fuelType = selectedFuelType;
        filter.vehiclePlate = resolveVehiclePlate();
        filter.driverName = getText(binding.inputExportDriver);
        filter.statusLevel = selectedStatus;
        filter.syncFilter = selectedSync;
        filter.startDate = startDate;
        filter.endDate = endDate;

        setLoading(true);
        viewModel.exportReport(filter, new RepositoryCallback<ExportedReport>() {
            @Override
            public void onSuccess(ExportedReport result) {
                setLoading(false);
                showSuccessDialog(result);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessDialog(ExportedReport report) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.export_success_title)
            .setMessage(getString(R.string.export_success_message, report.fileName))
            .setPositiveButton(R.string.export_share, (dialog, which) -> shareReport(report))
            .setNeutralButton(R.string.export_open, (dialog, which) -> openReport(report))
            .setNegativeButton(R.string.export_close, null)
            .show();
    }

    private void shareReport(ExportedReport report) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(report.mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, report.uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, getString(R.string.export_share)));
    }

    private void openReport(ExportedReport report) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(report.uri, report.mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Snackbar.make(binding.getRoot(), R.string.export_open_error, Snackbar.LENGTH_LONG).show();
        }
    }

    private void setLoading(boolean loading) {
        binding.progressExport.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonGenerateExport.setEnabled(!loading);
    }

    private String resolveVehiclePlate() {
        String raw = getText(binding.inputExportVehicle).trim();
        if (raw.isEmpty()) {
            return "";
        }
        if (vehicleOptions.containsKey(raw)) {
            return vehicleOptions.get(raw);
        }
        int separator = raw.indexOf(' ');
        String value = separator > 0 ? raw.substring(0, separator) : raw;
        return value.trim().toUpperCase();
    }

    private String getText(android.widget.TextView textView) {
        return textView.getText() == null ? "" : textView.getText().toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
