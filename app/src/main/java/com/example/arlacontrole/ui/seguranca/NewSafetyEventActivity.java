package com.example.arlacontrole.ui.seguranca;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.arlacontrole.R;
import com.example.arlacontrole.data.local.DriverEntity;
import com.example.arlacontrole.data.local.VehicleEntity;
import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.databinding.ActivityNewSafetyEventBinding;
import com.example.arlacontrole.evidence.RefuelEvidenceManager;
import com.example.arlacontrole.model.NewSafetyEventInput;
import com.example.arlacontrole.model.SafetyAnalysisStatus;
import com.example.arlacontrole.model.SafetyEventType;
import com.example.arlacontrole.model.SafetySeverity;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.ui.camera.CameraCaptureActivity;
import com.example.arlacontrole.utils.AppPreferences;
import com.example.arlacontrole.utils.FormatUtils;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NewSafetyEventActivity extends AppCompatActivity {

    private ActivityNewSafetyEventBinding binding;
    private NewSafetyEventViewModel viewModel;
    private final Map<String, String> eventTypeOptions = new LinkedHashMap<>();
    private final Map<String, String> analysisStatusOptions = new LinkedHashMap<>();
    private final Map<String, VehicleEntity> vehicleOptions = new LinkedHashMap<>();
    private final Map<String, DriverEntity> driverOptions = new LinkedHashMap<>();
    private LocalDateTime occurredAt = LocalDateTime.now().withSecond(0).withNano(0);
    private String selectedSeverity = SafetySeverity.MODERATE;
    private String evidencePhotoPath = "";
    private boolean driverLocked;
    private String linkedDriverName = "";

    private final ActivityResultLauncher<Intent> cameraLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                return;
            }
            evidencePhotoPath = result.getData().getStringExtra(CameraCaptureActivity.EXTRA_PHOTO_PATH);
            updateEvidencePreview();
        });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewSafetyEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        AppPreferences preferences = new AppPreferences(this);
        if (!preferences.hasValidSession()) {
            finish();
            return;
        }

        driverLocked = UserRole.isDriver(preferences.getSession().role);
        linkedDriverName = preferences.getSession().linkedDriverName == null ? "" : preferences.getSession().linkedDriverName.trim();

        viewModel = new ViewModelProvider(this).get(NewSafetyEventViewModel.class);
        configureStaticOptions();
        updateDateTimeField();
        updateEvidencePreview();

        binding.toggleSafetySeverity.check(R.id.buttonSeverityModerate);
        binding.toggleSafetySeverity.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.buttonSeverityCritical) {
                selectedSeverity = SafetySeverity.CRITICAL;
            } else if (checkedId == R.id.buttonSeverityHigh) {
                selectedSeverity = SafetySeverity.HIGH;
            } else if (checkedId == R.id.buttonSeverityLow) {
                selectedSeverity = SafetySeverity.LOW;
            } else {
                selectedSeverity = SafetySeverity.MODERATE;
            }
        });

        binding.inputSafetyDateTime.setOnClickListener(v -> openDateTimePicker());
        binding.buttonCaptureSafetyEvidence.setOnClickListener(v -> openCameraCapture());
        binding.buttonSaveSafetyEvent.setOnClickListener(v -> saveEvent());
        binding.layoutSafetyAnalysisStatus.setVisibility(driverLocked ? View.GONE : View.VISIBLE);

        viewModel.getVehicles().observe(this, this::bindVehicles);
        viewModel.getDrivers().observe(this, this::bindDrivers);
    }

    private void configureStaticOptions() {
        eventTypeOptions.clear();
        eventTypeOptions.put(getString(R.string.safety_type_accident), SafetyEventType.ACCIDENT);
        eventTypeOptions.put(getString(R.string.safety_type_incident), SafetyEventType.INCIDENT);
        eventTypeOptions.put(getString(R.string.safety_type_near_miss), SafetyEventType.NEAR_MISS);
        eventTypeOptions.put(getString(R.string.safety_type_unsafe_condition), SafetyEventType.UNSAFE_CONDITION);
        eventTypeOptions.put(getString(R.string.safety_type_unsafe_behavior), SafetyEventType.UNSAFE_BEHAVIOR);
        binding.autoEventType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(eventTypeOptions.keySet())));
        binding.autoEventType.setText(getString(R.string.safety_type_incident), false);

        analysisStatusOptions.clear();
        analysisStatusOptions.put(getString(R.string.safety_status_open), SafetyAnalysisStatus.OPEN);
        analysisStatusOptions.put(getString(R.string.safety_status_in_review), SafetyAnalysisStatus.IN_REVIEW);
        analysisStatusOptions.put(getString(R.string.safety_status_action_pending), SafetyAnalysisStatus.ACTION_PENDING);
        analysisStatusOptions.put(getString(R.string.safety_status_resolved), SafetyAnalysisStatus.RESOLVED);
        binding.autoSafetyAnalysisStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(analysisStatusOptions.keySet())));
        binding.autoSafetyAnalysisStatus.setText(getString(R.string.safety_status_open), false);
    }

    private void bindVehicles(List<VehicleEntity> vehicles) {
        vehicleOptions.clear();
        List<String> labels = new ArrayList<>();
        if (vehicles != null) {
            for (VehicleEntity entity : vehicles) {
                String label = entity.plate + " - " + entity.fleetCode + " - " + entity.model;
                vehicleOptions.put(label, entity);
                labels.add(label);
            }
        }
        binding.autoSafetyVehicle.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels));
    }

    private void bindDrivers(List<DriverEntity> drivers) {
        driverOptions.clear();
        List<String> labels = new ArrayList<>();
        if (drivers != null) {
            for (DriverEntity entity : drivers) {
                driverOptions.put(entity.name, entity);
                labels.add(entity.name);
            }
        }
        binding.autoSafetyDriver.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels));
        if (driverLocked && !linkedDriverName.isEmpty()) {
            binding.autoSafetyDriver.setText(linkedDriverName, false);
            binding.autoSafetyDriver.setEnabled(false);
            binding.autoSafetyDriver.setFocusable(false);
        }
    }

    private void openDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                occurredAt = occurredAt.withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth);
                openTimePicker();
            },
            occurredAt.getYear(),
            occurredAt.getMonthValue() - 1,
            occurredAt.getDayOfMonth()
        );
        datePickerDialog.show();
    }

    private void openTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                occurredAt = occurredAt.withHour(hourOfDay).withMinute(minute).withSecond(0).withNano(0);
                updateDateTimeField();
            },
            occurredAt.getHour(),
            occurredAt.getMinute(),
            true
        );
        timePickerDialog.show();
    }

    private void updateDateTimeField() {
        binding.inputSafetyDateTime.setText(FormatUtils.formatDateTime(occurredAt.toString()));
    }

    private void openCameraCapture() {
        Intent intent = new Intent(this, CameraCaptureActivity.class);
        intent.putExtra(CameraCaptureActivity.EXTRA_EVIDENCE_CATEGORY, RefuelEvidenceManager.CATEGORY_SAFETY_OCCURRENCE);
        intent.putExtra(CameraCaptureActivity.EXTRA_CAPTURE_TITLE, getString(R.string.safety_capture_title));
        intent.putExtra(CameraCaptureActivity.EXTRA_CAPTURE_INSTRUCTION, getString(R.string.safety_capture_instruction));
        cameraLauncher.launch(intent);
    }

    private void updateEvidencePreview() {
        Bitmap preview = RefuelEvidenceManager.loadPreview(evidencePhotoPath, 900, 600);
        if (preview != null) {
            binding.imageSafetyEvidencePreview.setImageBitmap(preview);
            binding.textSafetyEvidenceHint.setText(R.string.evidence_attached);
            binding.buttonCaptureSafetyEvidence.setText(R.string.safety_retake_evidence);
        } else {
            binding.imageSafetyEvidencePreview.setImageDrawable(null);
            binding.textSafetyEvidenceHint.setText(R.string.safety_evidence_hint);
            binding.buttonCaptureSafetyEvidence.setText(R.string.safety_capture_evidence);
        }
    }

    private void saveEvent() {
        VehicleEntity vehicle = resolveVehicle();
        DriverEntity driver = resolveDriver();
        String location = getText(binding.inputSafetyLocation);
        String description = getText(binding.inputSafetyDescription);

        if (vehicle == null) {
            Snackbar.make(binding.getRoot(), R.string.invalid_vehicle, Snackbar.LENGTH_LONG).show();
            return;
        }
        if (driver == null) {
            Snackbar.make(binding.getRoot(), R.string.invalid_driver, Snackbar.LENGTH_LONG).show();
            return;
        }
        if (location.trim().isEmpty()) {
            Snackbar.make(binding.getRoot(), R.string.invalid_location, Snackbar.LENGTH_LONG).show();
            return;
        }
        if (description.trim().isEmpty()) {
            Snackbar.make(binding.getRoot(), R.string.safety_invalid_description, Snackbar.LENGTH_LONG).show();
            return;
        }

        NewSafetyEventInput input = new NewSafetyEventInput();
        input.type = resolveEventType();
        input.occurredAtIso = occurredAt.toString();
        input.vehiclePlate = vehicle.plate;
        input.driverId = driver.id;
        input.locationName = location.trim();
        input.description = description.trim();
        input.severity = selectedSeverity;
        input.probableCause = getText(binding.inputSafetyProbableCause).trim();
        input.notes = getText(binding.inputSafetyNotes).trim();
        input.evidencePhotoPath = evidencePhotoPath;
        input.evidenceCategory = RefuelEvidenceManager.CATEGORY_SAFETY_OCCURRENCE;
        input.analysisStatus = driverLocked ? SafetyAnalysisStatus.OPEN : resolveAnalysisStatus();

        setLoading(true);
        viewModel.saveSafetyEvent(input, new RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                setLoading(false);
                Snackbar.make(binding.getRoot(), R.string.safety_save_success, Snackbar.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private VehicleEntity resolveVehicle() {
        String raw = getText(binding.autoSafetyVehicle).trim();
        if (vehicleOptions.containsKey(raw)) {
            return vehicleOptions.get(raw);
        }
        int separator = raw.indexOf(' ');
        String plate = separator > 0 ? raw.substring(0, separator) : raw;
        for (VehicleEntity entity : vehicleOptions.values()) {
            if (entity.plate.equalsIgnoreCase(plate.trim())) {
                return entity;
            }
        }
        return null;
    }

    private DriverEntity resolveDriver() {
        String raw = driverLocked ? linkedDriverName : getText(binding.autoSafetyDriver).trim();
        return driverOptions.get(raw);
    }

    private String resolveEventType() {
        String raw = getText(binding.autoEventType).trim();
        return eventTypeOptions.containsKey(raw) ? eventTypeOptions.get(raw) : SafetyEventType.INCIDENT;
    }

    private String resolveAnalysisStatus() {
        String raw = getText(binding.autoSafetyAnalysisStatus).trim();
        return analysisStatusOptions.containsKey(raw) ? analysisStatusOptions.get(raw) : SafetyAnalysisStatus.OPEN;
    }

    private void setLoading(boolean loading) {
        binding.buttonSaveSafetyEvent.setEnabled(!loading);
        binding.buttonCaptureSafetyEvidence.setEnabled(!loading);
    }

    private String getText(android.widget.TextView view) {
        return view.getText() == null ? "" : view.getText().toString();
    }
}
