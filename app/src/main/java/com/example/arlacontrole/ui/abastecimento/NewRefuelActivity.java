package com.example.arlacontrole.ui.abastecimento;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.arlacontrole.R;
import com.example.arlacontrole.checklist.ChecklistItemState;
import com.example.arlacontrole.checklist.RefuelChecklistManager;
import com.example.arlacontrole.data.local.DriverEntity;
import com.example.arlacontrole.data.local.VehicleEntity;
import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.model.CostValidationResult;
import com.example.arlacontrole.model.FuelCostContext;
import com.example.arlacontrole.model.FuelCostSnapshot;
import com.example.arlacontrole.databinding.ActivityNewRefuelBinding;
import com.example.arlacontrole.evidence.RefuelEvidenceManager;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.NewRefuelInput;
import com.example.arlacontrole.model.RefuelOdometerContext;
import com.example.arlacontrole.model.RefuelOdometerValidation;
import com.example.arlacontrole.model.RefuelEntryMode;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.rules.CostValidator;
import com.example.arlacontrole.rules.FuelCostCalculator;
import com.example.arlacontrole.rules.RefuelOdometerRules;
import com.example.arlacontrole.ui.camera.CameraCaptureActivity;
import com.example.arlacontrole.utils.AppPreferences;
import com.example.arlacontrole.utils.FormatUtils;
import com.example.arlacontrole.vision.ExtractionResult;
import com.example.arlacontrole.vision.ExtractionStatus;
import com.example.arlacontrole.vision.RefuelExtractionEngine;
import com.example.arlacontrole.vision.ocr.MlKitOcrProcessor;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewRefuelActivity extends AppCompatActivity {

    private static final String KEY_SUPPLIED_AT = "supplied_at";
    private static final String KEY_FUEL_TYPE = "fuel_type";
    private static final String KEY_EVIDENCE_PATH = "evidence_path";
    private static final String KEY_EVIDENCE_CATEGORY = "evidence_category";
    private static final String KEY_CHECKLIST_PAYLOAD = "checklist_payload";
    private static final String KEY_CHECKLIST_COMPLETED_AT = "checklist_completed_at";

    private ActivityNewRefuelBinding binding;
    private NewRefuelViewModel viewModel;
    private final Map<String, VehicleEntity> vehicleOptions = new LinkedHashMap<>();
    private final Map<String, DriverEntity> driverOptions = new LinkedHashMap<>();
    private final List<String> locationOptions = new ArrayList<>();
    private final List<ChecklistItemState> checklistItems = new ArrayList<>();
    private final RefuelExtractionEngine extractionEngine = new RefuelExtractionEngine(new MlKitOcrProcessor());
    private LocalDateTime suppliedAt = LocalDateTime.now();
    private String selectedFuelType = FuelType.ARLA;
    private boolean driverLocked;
    private String linkedDriverName = "";
    private String evidencePhotoPath = "";
    private String evidenceCategory = "";
    private String checklistCompletedAtIso = "";
    private boolean refuelSaved;
    private ExtractionResult lastExtractionResult;
    private RefuelOdometerContext lastOdometerContext = new RefuelOdometerContext();
    private RefuelOdometerValidation lastOdometerValidation = new RefuelOdometerValidation();
    private FuelCostContext lastFuelCostContext = new FuelCostContext();
    private FuelCostSnapshot lastFuelCostSnapshot = new FuelCostSnapshot();
    private CostValidationResult lastCostValidation = new CostValidationResult();
    private boolean applyingSuggestedInitialOdometer;
    private boolean updatingFinancialFields;
    private Integer lastAppliedSuggestedInitialOdometerKm;
    private final ActivityResultLauncher<Intent> cameraCaptureLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                return;
            }
            String capturedPath = result.getData().getStringExtra(CameraCaptureActivity.EXTRA_PHOTO_PATH);
            if (!RefuelEvidenceManager.hasEvidence(capturedPath)) {
                Snackbar.make(binding.getRoot(), R.string.camera_capture_error, Snackbar.LENGTH_LONG).show();
                return;
            }
            if (RefuelEvidenceManager.hasEvidence(evidencePhotoPath) && !evidencePhotoPath.equals(capturedPath)) {
                RefuelEvidenceManager.deleteQuietly(evidencePhotoPath);
            }
            evidencePhotoPath = capturedPath;
            evidenceCategory = RefuelEvidenceManager.resolveCategory(selectedFuelType);
            updateFuelLabels();
            updateEvidencePreview();
            analyzeCapturedEvidence();
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewRefuelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        AppPreferences preferences = new AppPreferences(this);
        if (!preferences.hasValidSession()) {
            finish();
            return;
        }
        driverLocked = UserRole.isDriver(preferences.getSession().role);
        linkedDriverName = preferences.getSession().linkedDriverName == null ? "" : preferences.getSession().linkedDriverName;
        restoreState(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NewRefuelViewModel.class);
        updateDateTimeField();
        binding.textSelectedVehicleInfo.setText(getString(R.string.choose_vehicle));
        configureLocationOptions();

        binding.toggleFuelType.check(FuelType.DIESEL.equals(selectedFuelType) ? R.id.buttonFuelDiesel : R.id.buttonFuelArla);
        binding.toggleFuelType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            String previousFuelType = selectedFuelType;
            selectedFuelType = checkedId == R.id.buttonFuelDiesel ? FuelType.DIESEL : FuelType.ARLA;
            if (!previousFuelType.equals(selectedFuelType)) {
                clearExtractionUi();
                resetEvidenceForFuelChange();
                checklistItems.clear();
                checklistCompletedAtIso = "";
            }
            updateFuelLabels();
            rebuildChecklist(true);
            requestFuelCostContext(resolveVehicle());
            updateFinancialState();
        });

        binding.inputDateTime.setOnClickListener(v -> openDateTimePicker());
        binding.buttonCaptureEvidence.setOnClickListener(v -> openCameraCapture());
        binding.buttonSaveRefuel.setOnClickListener(v -> saveRefuel());
        binding.autoVehicle.setOnItemClickListener((parent, view, position, id) -> {
            VehicleEntity vehicle = resolveVehicle();
            updateSelectedVehicleInfo(vehicle);
            requestOdometerContext(vehicle);
            requestFuelCostContext(vehicle);
        });
        configureOdometerInputs();
        configureFinancialInputs();
        binding.inputLayoutPricePerLiter.setHelperText(getString(R.string.fuel_cost_price_auto_helper));
        binding.inputLayoutTotalAmount.setHelperText(getString(R.string.fuel_cost_total_auto_helper));
        binding.inputTotalAmount.setFocusable(false);
        binding.inputTotalAmount.setClickable(false);
        binding.inputTotalAmount.setLongClickable(false);
        binding.inputTotalAmount.setCursorVisible(false);

        updateFuelLabels();
        rebuildChecklist(true);
        updateEvidencePreview();
        renderFuelCostContext();
        updateFinancialState();
        viewModel.getVehicles().observe(this, this::bindVehicles);
        viewModel.getDrivers().observe(this, this::bindDrivers);
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
        binding.autoVehicle.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels));
        VehicleEntity vehicle = resolveVehicle();
        updateSelectedVehicleInfo(vehicle);
        requestOdometerContext(vehicle);
        requestFuelCostContext(vehicle);
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
        binding.autoDriver.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels));
        if (driverLocked && !linkedDriverName.trim().isEmpty()) {
            binding.autoDriver.setText(linkedDriverName, false);
            binding.autoDriver.setEnabled(false);
            binding.autoDriver.setFocusable(false);
        }
    }

    private void configureLocationOptions() {
        locationOptions.clear();
        locationOptions.addAll(Arrays.asList(
            getString(R.string.refuel_location_posto),
            getString(R.string.refuel_location_patio)
        ));
        binding.inputLocation.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locationOptions));
    }

    private void openDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                suppliedAt = suppliedAt.withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth);
                openTimePicker();
            },
            suppliedAt.getYear(),
            suppliedAt.getMonthValue() - 1,
            suppliedAt.getDayOfMonth()
        );
        datePickerDialog.show();
    }

    private void openTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                suppliedAt = suppliedAt.withHour(hourOfDay).withMinute(minute).withSecond(0).withNano(0);
                updateDateTimeField();
            },
            suppliedAt.getHour(),
            suppliedAt.getMinute(),
            true
        );
        timePickerDialog.show();
    }

    private void updateDateTimeField() {
        binding.inputDateTime.setText(FormatUtils.formatDateTime(suppliedAt.toString()));
    }

    private void updateFuelLabels() {
        binding.buttonSaveRefuel.setText(getString(R.string.save));
        binding.inputLayoutLiters.setHint(
            FuelType.DIESEL.equals(selectedFuelType)
                ? getString(R.string.input_liters_diesel)
                : getString(R.string.input_liters_arla)
        );
        binding.inputLayoutTotalAmount.setVisibility(View.VISIBLE);
        binding.inputLayoutPricePerLiter.setVisibility(View.VISIBLE);
        binding.cardFuelCostSummary.setVisibility(View.VISIBLE);
        binding.cardArlaCalculatedControl.setVisibility(FuelType.DIESEL.equals(selectedFuelType) ? View.GONE : View.VISIBLE);
        evidenceCategory = RefuelEvidenceManager.resolveCategory(selectedFuelType);
        binding.textEvidenceInstruction.setText(RefuelEvidenceManager.buildInstruction(this, selectedFuelType));
        binding.buttonCaptureEvidence.setText(
            RefuelEvidenceManager.buildButtonLabel(this, selectedFuelType, RefuelEvidenceManager.hasEvidence(evidencePhotoPath))
        );
        binding.textEvidenceStatus.setText(
            RefuelEvidenceManager.hasEvidence(evidencePhotoPath)
                ? getString(R.string.evidence_attached)
                : getString(R.string.evidence_required_message)
        );
        binding.textChecklistInstruction.setText(
            FuelType.DIESEL.equals(selectedFuelType)
                ? getString(R.string.checklist_instruction_diesel)
                : getString(R.string.checklist_instruction_arla)
        );
    }

    private void saveRefuel() {
        if (selectedFuelType == null || selectedFuelType.trim().isEmpty()) {
            Snackbar.make(binding.getRoot(), R.string.fuel_type_required, Snackbar.LENGTH_LONG).show();
            return;
        }

        VehicleEntity vehicle = resolveVehicle();
        if (vehicle == null) {
            binding.autoVehicle.setError(getString(R.string.invalid_vehicle));
            return;
        }
        binding.autoVehicle.setError(null);

        DriverEntity driver = resolveDriver();
        if (driver == null) {
            binding.autoDriver.setError(getString(R.string.invalid_driver));
            return;
        }
        binding.autoDriver.setError(null);

        Double liters = FormatUtils.parseFlexibleDecimal(getText(binding.inputLiters));
        if (liters == null || liters <= 0d) {
            binding.inputLiters.setError(getString(R.string.invalid_liters));
            return;
        }
        binding.inputLiters.setError(null);

        Integer odometerInitial = parseInteger(binding.inputOdometerInitial);
        Integer odometerFinal = parseInteger(binding.inputOdometerFinal);
        if (odometerInitial == null || odometerInitial <= 0) {
            binding.inputLayoutOdometerInitial.setError(getString(R.string.invalid_odometer_initial));
            return;
        }
        binding.inputLayoutOdometerInitial.setError(null);
        if (odometerFinal == null || odometerFinal <= 0) {
            binding.inputLayoutOdometerFinal.setError(getString(R.string.invalid_odometer_final));
            return;
        }
        binding.inputLayoutOdometerFinal.setError(null);
        RefuelOdometerValidation odometerValidation = RefuelOdometerRules.validate(
            lastOdometerContext.expectedInitialOdometerKm,
            odometerInitial,
            odometerFinal
        );
        if (!odometerValidation.valid) {
            binding.inputLayoutOdometerFinal.setError(odometerValidation.message);
            return;
        }
        binding.inputLayoutOdometerFinal.setError(null);

        Double pricePerLiter = parseDouble(binding.inputPricePerLiter);
        FuelCostSnapshot financialSnapshot = FuelCostCalculator.normalize(
            liters,
            null,
            pricePerLiter,
            odometerInitial,
            odometerFinal
        );
        CostValidationResult financialValidation = CostValidator.validate(selectedFuelType, financialSnapshot, null);
        if (!financialValidation.valid) {
            binding.inputLayoutTotalAmount.setError(null);
            binding.inputLayoutPricePerLiter.setError(getString(R.string.invalid_price_per_liter));
            binding.textFuelCostValidation.setText(financialValidation.message);
            binding.textFuelCostValidation.setTextColor(getColor(R.color.danger));
            return;
        }
        binding.inputLayoutTotalAmount.setError(null);
        binding.inputLayoutPricePerLiter.setError(null);

        String location = getText(binding.inputLocation);
        if (location.trim().isEmpty() || !locationOptions.contains(location)) {
            binding.inputLocation.setError(getString(R.string.invalid_location));
            return;
        }
        binding.inputLocation.setError(null);
        if (!RefuelEvidenceManager.hasEvidence(evidencePhotoPath)) {
            Snackbar.make(binding.getRoot(), R.string.evidence_missing_error, Snackbar.LENGTH_LONG).show();
            return;
        }
        if (!RefuelChecklistManager.isComplete(checklistItems)) {
            Snackbar.make(binding.getRoot(), R.string.checklist_missing_error, Snackbar.LENGTH_LONG).show();
            return;
        }

        NewRefuelInput input = new NewRefuelInput();
        input.fuelType = selectedFuelType;
        input.vehiclePlate = vehicle.plate;
        input.driverId = driver.id;
        input.liters = liters;
        input.totalAmount = financialSnapshot.totalAmount;
        input.pricePerLiter = financialSnapshot.pricePerLiter;
        input.costPerKm = financialSnapshot.costPerKm;
        input.odometerInitialKm = odometerInitial;
        input.odometerFinalKm = odometerFinal;
        input.calculatedArlaControlQuantity = odometerValidation.calculatedDistanceKm;
        input.expectedInitialOdometerKm = lastOdometerContext.expectedInitialOdometerKm;
        input.odometerDivergenceKm = odometerValidation.hasDivergenceWarning ? odometerValidation.divergenceKm : null;
        input.odometerKm = odometerFinal;
        input.suppliedAtIso = suppliedAt.withSecond(0).withNano(0).toString();
        input.locationName = location;
        input.notes = getText(binding.inputNotes);
        input.evidencePhotoPath = evidencePhotoPath;
        input.evidenceCategory = evidenceCategory;
        input.checklistPayload = RefuelChecklistManager.serialize(checklistItems);
        input.checklistCompletedAtIso = checklistCompletedAtIso;
        input.dataEntryMode = resolveEntryMode();
        input.ocrStatus = lastExtractionResult == null ? "" : lastExtractionResult.status;
        input.ocrRawText = lastExtractionResult == null ? "" : lastExtractionResult.rawText;
        input.ocrMetadataJson = lastExtractionResult == null ? "" : lastExtractionResult.toMetadataJson();

        binding.progressSave.show();
        binding.buttonSaveRefuel.setEnabled(false);
        viewModel.saveRefuel(input, new RepositoryCallback<Long>() {
            @Override
            public void onSuccess(Long result) {
                refuelSaved = true;
                binding.progressSave.hide();
                binding.buttonSaveRefuel.setEnabled(true);
                Toast.makeText(NewRefuelActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String message) {
                binding.progressSave.hide();
                binding.buttonSaveRefuel.setEnabled(true);
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private VehicleEntity resolveVehicle() {
        String selected = binding.autoVehicle.getText() == null ? "" : binding.autoVehicle.getText().toString();
        if (vehicleOptions.containsKey(selected)) {
            return vehicleOptions.get(selected);
        }
        for (VehicleEntity entity : vehicleOptions.values()) {
            if (entity.plate.equalsIgnoreCase(selected.trim())) {
                return entity;
            }
        }
        return null;
    }

    private void updateSelectedVehicleInfo(VehicleEntity vehicle) {
        if (vehicle == null) {
            binding.textSelectedVehicleInfo.setText(getString(R.string.choose_vehicle));
            return;
        }
        binding.textSelectedVehicleInfo.setText(getString(R.string.selected_vehicle_info, vehicle.plate, vehicle.fleetCode));
    }

    private void configureOdometerInputs() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateOdometerState();
                updateFinancialState();
            }
        };
        binding.inputOdometerInitial.addTextChangedListener(watcher);
        binding.inputOdometerFinal.addTextChangedListener(watcher);
    }

    private void configureFinancialInputs() {
        binding.inputLiters.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateFinancialState();
            }
        });
        binding.inputPricePerLiter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateFinancialState();
            }
        });
    }

    private void requestOdometerContext(VehicleEntity vehicle) {
        if (vehicle == null) {
            lastOdometerContext = new RefuelOdometerContext();
            if (shouldReplaceSuggestedInitialOdometer()) {
                applyingSuggestedInitialOdometer = true;
                binding.inputOdometerInitial.setText("");
                applyingSuggestedInitialOdometer = false;
            }
            lastAppliedSuggestedInitialOdometerKm = null;
            renderOdometerContext();
            updateOdometerState();
            return;
        }
        viewModel.loadRefuelOdometerContext(vehicle.plate, new RepositoryCallback<RefuelOdometerContext>() {
            @Override
            public void onSuccess(RefuelOdometerContext result) {
                lastOdometerContext = result == null ? new RefuelOdometerContext() : result;
                if (lastOdometerContext.expectedInitialOdometerKm != null && shouldReplaceSuggestedInitialOdometer()) {
                    applyingSuggestedInitialOdometer = true;
                    binding.inputOdometerInitial.setText(String.valueOf(lastOdometerContext.expectedInitialOdometerKm));
                    applyingSuggestedInitialOdometer = false;
                    lastAppliedSuggestedInitialOdometerKm = lastOdometerContext.expectedInitialOdometerKm;
                } else if (lastOdometerContext.expectedInitialOdometerKm == null && shouldReplaceSuggestedInitialOdometer()) {
                    applyingSuggestedInitialOdometer = true;
                    binding.inputOdometerInitial.setText("");
                    applyingSuggestedInitialOdometer = false;
                    lastAppliedSuggestedInitialOdometerKm = null;
                }
                renderOdometerContext();
                updateOdometerState();
            }

            @Override
            public void onError(String message) {
                lastOdometerContext = new RefuelOdometerContext();
                renderOdometerContext();
                updateOdometerState();
            }
        });
    }

    private void requestFuelCostContext(VehicleEntity vehicle) {
        if (vehicle == null) {
            lastFuelCostContext = new FuelCostContext();
            renderFuelCostContext();
            updateFinancialState();
            return;
        }
        viewModel.loadFuelCostContext(selectedFuelType, vehicle.plate, new RepositoryCallback<FuelCostContext>() {
            @Override
            public void onSuccess(FuelCostContext result) {
                lastFuelCostContext = result == null ? new FuelCostContext() : result;
                renderFuelCostContext();
                updateFinancialState();
            }

            @Override
            public void onError(String message) {
                lastFuelCostContext = new FuelCostContext();
                renderFuelCostContext();
                updateFinancialState();
            }
        });
    }

    private void renderOdometerContext() {
        if (lastOdometerContext == null || !lastOdometerContext.hasPreviousRecord || lastOdometerContext.expectedInitialOdometerKm == null) {
            binding.cardOdometerSource.setVisibility(View.GONE);
            binding.inputLayoutOdometerInitial.setHelperText(getString(R.string.refuel_odometer_initial_manual_helper));
            return;
        }
        binding.cardOdometerSource.setVisibility(View.VISIBLE);
        binding.textOdometerSourceTitle.setText(
            getString(R.string.refuel_odometer_source_title, FormatUtils.formatKilometers(lastOdometerContext.expectedInitialOdometerKm))
        );
        binding.textOdometerSourceSubtitle.setText(
            getString(R.string.refuel_odometer_source_subtitle, FormatUtils.formatDateTime(lastOdometerContext.sourceSuppliedAtIso))
        );
        binding.inputLayoutOdometerInitial.setHelperText(getString(R.string.refuel_odometer_initial_helper));
    }

    private void renderFuelCostContext() {
        List<String> suggestions = new ArrayList<>();
        if (lastFuelCostContext != null) {
            if (lastFuelCostContext.hasVehicleSuggestion && lastFuelCostContext.suggestedPricePerLiter != null) {
                suggestions.add(
                    getString(
                        R.string.fuel_cost_suggestion_vehicle,
                        FormatUtils.formatCurrency(lastFuelCostContext.suggestedPricePerLiter),
                        FormatUtils.formatDate(lastFuelCostContext.lastReferenceAtIso)
                    )
                );
            }
            if (lastFuelCostContext.averagePricePerLiter != null) {
                suggestions.add(
                    getString(
                        R.string.fuel_cost_suggestion_average,
                        FormatUtils.formatFuelType(this, selectedFuelType),
                        FormatUtils.formatCurrency(lastFuelCostContext.averagePricePerLiter)
                    )
                );
            }
        }
        binding.textFuelCostSuggestion.setText(
            suggestions.isEmpty() ? getString(R.string.fuel_cost_suggestion_empty) : android.text.TextUtils.join("\n", suggestions)
        );
    }

    private void updateOdometerState() {
        Integer odometerInitial = parseInteger(binding.inputOdometerInitial);
        Integer odometerFinal = parseInteger(binding.inputOdometerFinal);
        if (odometerInitial == null || odometerFinal == null) {
            binding.textArlaCalculatedControl.setText(getString(R.string.refuel_arla_control_empty));
            binding.textOdometerValidation.setVisibility(View.VISIBLE);
            binding.textOdometerValidation.setText(
                lastOdometerContext != null && lastOdometerContext.hasPreviousRecord
                    ? getString(R.string.refuel_odometer_source_hint)
                    : getString(R.string.refuel_odometer_fill_hint)
            );
            binding.textOdometerValidation.setTextColor(getColor(R.color.text_secondary));
            return;
        }
        lastOdometerValidation = RefuelOdometerRules.validate(
            lastOdometerContext.expectedInitialOdometerKm,
            odometerInitial,
            odometerFinal
        );
        if (lastOdometerValidation.valid) {
            binding.textArlaCalculatedControl.setText(
                getString(R.string.refuel_arla_control_value, FormatUtils.formatKilometers(lastOdometerValidation.calculatedDistanceKm))
            );
        } else {
            binding.textArlaCalculatedControl.setText(getString(R.string.refuel_arla_control_empty));
        }
        binding.textOdometerValidation.setVisibility(View.VISIBLE);
        binding.textOdometerValidation.setText(lastOdometerValidation.message);
        int color = lastOdometerValidation.hasDivergenceWarning
            ? R.color.warning
            : (lastOdometerValidation.valid ? R.color.success : R.color.danger);
        binding.textOdometerValidation.setTextColor(getColor(color));
        if (applyingSuggestedInitialOdometer) {
            binding.textOdometerValidation.setTextColor(getColor(R.color.text_secondary));
        }
    }

    private void updateFinancialState() {
        Double liters = parseDouble(binding.inputLiters);
        Double pricePerLiter = parseDouble(binding.inputPricePerLiter);
        Integer odometerInitial = parseInteger(binding.inputOdometerInitial);
        Integer odometerFinal = parseInteger(binding.inputOdometerFinal);

        lastFuelCostSnapshot = FuelCostCalculator.normalize(
            liters,
            null,
            pricePerLiter,
            odometerInitial,
            odometerFinal
        );

        if (!updatingFinancialFields) {
            updatingFinancialFields = true;
            if (liters != null && liters > 0d && pricePerLiter != null && pricePerLiter > 0d && lastFuelCostSnapshot.totalAmount != null) {
                setDecimalValue(binding.inputTotalAmount, lastFuelCostSnapshot.totalAmount);
                binding.inputLayoutTotalAmount.setHelperText(getString(R.string.fuel_cost_auto_calculated));
            } else {
                binding.inputTotalAmount.setText("");
                binding.inputLayoutTotalAmount.setHelperText(getString(R.string.fuel_cost_total_auto_helper));
            }
            updatingFinancialFields = false;
        }

        binding.textFuelCostPerKm.setText(
            lastFuelCostSnapshot.hasCostPerKm()
                ? getString(R.string.fuel_cost_per_km, FormatUtils.formatCurrency(lastFuelCostSnapshot.costPerKm))
                : getString(R.string.fuel_cost_per_km_empty)
        );

        if (liters == null || liters <= 0d || (!lastFuelCostSnapshot.hasTotalAmount() && !lastFuelCostSnapshot.hasPricePerLiter())) {
            binding.textFuelCostValidation.setText(getString(R.string.fuel_cost_validation_pending));
            binding.textFuelCostValidation.setTextColor(getColor(R.color.text_secondary));
            return;
        }

        lastCostValidation = CostValidator.validate(selectedFuelType, lastFuelCostSnapshot, null);
        binding.textFuelCostValidation.setText(lastCostValidation.message);
        binding.textFuelCostValidation.setTextColor(getColor(resolveFinancialValidationColor(lastCostValidation.level)));
    }

    private DriverEntity resolveDriver() {
        String selected = binding.autoDriver.getText() == null ? "" : binding.autoDriver.getText().toString();
        return driverOptions.get(selected);
    }

    @NonNull
    private String getText(TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }

    private Integer parseInteger(TextView view) {
        String value = getText(view);
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Double parseDouble(TextView view) {
        return FormatUtils.parseFlexibleDecimal(getText(view));
    }

    private void setDecimalValue(TextView view, Double value) {
        String formatted = FormatUtils.formatEditableDecimal(value);
        if (!formatted.equals(getText(view))) {
            view.setText(formatted);
        }
    }

    private void openCameraCapture() {
        Intent intent = new Intent(this, CameraCaptureActivity.class);
        intent.putExtra(CameraCaptureActivity.EXTRA_FUEL_TYPE, selectedFuelType);
        cameraCaptureLauncher.launch(intent);
    }

    private void analyzeCapturedEvidence() {
        showExtractionLoading();
        extractionEngine.analyze(this, selectedFuelType, evidencePhotoPath, new RefuelExtractionEngine.Callback() {
            @Override
            public void onSuccess(ExtractionResult result) {
                lastExtractionResult = result;
                applyExtractionSuggestions(result);
                showExtractionResult(result);
                if (ExtractionStatus.INSUFFICIENT.equals(result.status)) {
                    Snackbar.make(binding.getRoot(), R.string.extraction_retry_message, Snackbar.LENGTH_LONG).show();
                } else if (!ExtractionStatus.CONFIDENT.equals(result.status)) {
                    Snackbar.make(binding.getRoot(), R.string.extraction_review_message, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String message) {
                lastExtractionResult = null;
                showExtractionFailure(message);
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void showExtractionLoading() {
        binding.cardExtractionResult.setVisibility(View.VISIBLE);
        binding.layoutExtractionLoading.setVisibility(View.VISIBLE);
        binding.chipExtractionStatus.setVisibility(View.GONE);
        binding.textExtractionSummary.setText(getString(R.string.extraction_processing));
        binding.textExtractionFields.setText("");
        binding.buttonSaveRefuel.setEnabled(false);
    }

    private void showExtractionResult(ExtractionResult result) {
        binding.cardExtractionResult.setVisibility(View.VISIBLE);
        binding.layoutExtractionLoading.setVisibility(View.GONE);
        binding.chipExtractionStatus.setVisibility(View.VISIBLE);
        binding.chipExtractionStatus.setText(FormatUtils.formatExtractionStatus(this, result.status));
        binding.chipExtractionStatus.setChipBackgroundColorResource(resolveExtractionColor(result.status));
        binding.textExtractionSummary.setText(
            result.summaryMessage == null || result.summaryMessage.trim().isEmpty()
                ? getString(R.string.extraction_review_message)
                : result.summaryMessage
        );
        binding.textExtractionFields.setText(buildExtractionDetails(result));
        binding.buttonSaveRefuel.setEnabled(true);
    }

    private void showExtractionFailure(String message) {
        binding.cardExtractionResult.setVisibility(View.VISIBLE);
        binding.layoutExtractionLoading.setVisibility(View.GONE);
        binding.chipExtractionStatus.setVisibility(View.VISIBLE);
        binding.chipExtractionStatus.setText(getString(R.string.ocr_status_insufficient));
        binding.chipExtractionStatus.setChipBackgroundColorResource(R.color.danger_container);
        binding.textExtractionSummary.setText(
            message == null || message.trim().isEmpty()
                ? getString(R.string.extraction_failed_message)
                : message
        );
        binding.textExtractionFields.setText(getString(R.string.extraction_retry_message));
        binding.buttonSaveRefuel.setEnabled(true);
    }

    private void clearExtractionUi() {
        lastExtractionResult = null;
        binding.cardExtractionResult.setVisibility(View.GONE);
        binding.layoutExtractionLoading.setVisibility(View.GONE);
        binding.chipExtractionStatus.setVisibility(View.GONE);
        binding.textExtractionSummary.setText("");
        binding.textExtractionFields.setText("");
        binding.inputLayoutLiters.setHelperText(null);
        binding.inputLayoutTotalAmount.setHelperText(null);
        binding.inputLayoutDateTime.setHelperText(null);
        binding.inputLayoutLocation.setHelperText(null);
        binding.buttonSaveRefuel.setEnabled(true);
    }

    private void applyExtractionSuggestions(ExtractionResult result) {
        boolean litersFilled = false;
        boolean amountFilled = false;
        boolean dateFilled = false;
        boolean locationFilled = false;

        if (result.liters != null) {
            binding.inputLiters.setText(FormatUtils.formatEditableDecimal(result.liters));
            litersFilled = true;
        }
        if (result.totalAmount != null && result.liters != null && result.liters > 0d) {
            Double suggestedPrice = FuelCostCalculator.calculatePricePerLiter(result.totalAmount, result.liters);
            if (suggestedPrice != null) {
                binding.inputPricePerLiter.setText(FormatUtils.formatEditableDecimal(suggestedPrice));
                amountFilled = true;
            }
        }
        if (result.suppliedAtIso != null && !result.suppliedAtIso.trim().isEmpty()) {
            try {
                suppliedAt = LocalDateTime.parse(result.suppliedAtIso);
                updateDateTimeField();
                dateFilled = true;
            } catch (Exception ignored) {
            }
        }
        if (result.locationName != null && !result.locationName.trim().isEmpty()) {
            String normalizedLocation = normalizeLocationOption(result.locationName);
            if (!normalizedLocation.isEmpty()) {
                binding.inputLocation.setText(normalizedLocation, false);
                locationFilled = true;
            }
        }

        binding.inputLayoutLiters.setHelperText(litersFilled ? getString(R.string.field_autofilled) : null);
        binding.inputLayoutPricePerLiter.setHelperText(amountFilled ? getString(R.string.field_autofilled) : getString(R.string.fuel_cost_price_auto_helper));
        binding.inputLayoutDateTime.setHelperText(dateFilled ? getString(R.string.field_autofilled) : null);
        binding.inputLayoutLocation.setHelperText(locationFilled ? getString(R.string.field_autofilled) : null);
        updateFinancialState();
    }

    private String buildExtractionDetails(ExtractionResult result) {
        List<String> details = new ArrayList<>();
        if (result.liters != null) {
            details.add(getString(R.string.extraction_detail_liters, FormatUtils.formatLiters(result.liters)));
        }
        if (result.totalAmount != null) {
            details.add(getString(R.string.extraction_detail_total, FormatUtils.formatCurrency(result.totalAmount)));
        }
        if (result.suppliedAtIso != null && !result.suppliedAtIso.trim().isEmpty()) {
            details.add(getString(R.string.extraction_detail_date, FormatUtils.formatDateTime(result.suppliedAtIso)));
        }
        if (result.locationName != null && !result.locationName.trim().isEmpty()) {
            details.add(getString(R.string.extraction_detail_location, result.locationName));
        }
        if (result.pumpNumber != null && !result.pumpNumber.trim().isEmpty()) {
            details.add(getString(R.string.extraction_detail_pump, result.pumpNumber));
        }
        if (result.cnpj != null && !result.cnpj.trim().isEmpty()) {
            details.add(getString(R.string.extraction_detail_cnpj, result.cnpj));
        }
        if (result.documentNumber != null && !result.documentNumber.trim().isEmpty()) {
            details.add(getString(R.string.extraction_detail_document, result.documentNumber));
        }
        if (result.paymentMethod != null && !result.paymentMethod.trim().isEmpty()) {
            details.add(getString(R.string.extraction_detail_payment, result.paymentMethod));
        }
        if (details.isEmpty()) {
            return getString(R.string.extraction_no_data_message);
        }
        StringBuilder builder = new StringBuilder();
        for (String detail : details) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(detail);
        }
        return builder.toString();
    }

    private void updateEvidencePreview() {
        boolean hasEvidence = RefuelEvidenceManager.hasEvidence(evidencePhotoPath);
        binding.imageEvidencePreview.setVisibility(hasEvidence ? View.VISIBLE : View.GONE);
        if (!hasEvidence) {
            binding.imageEvidencePreview.setImageDrawable(null);
            updateChecklistState();
            return;
        }
        Bitmap bitmap = RefuelEvidenceManager.loadPreview(evidencePhotoPath, 1200, 1200);
        binding.imageEvidencePreview.setImageBitmap(bitmap);
        updateChecklistState();
    }

    private void resetEvidenceForFuelChange() {
        if (!RefuelEvidenceManager.hasEvidence(evidencePhotoPath)) {
            evidenceCategory = RefuelEvidenceManager.resolveCategory(selectedFuelType);
            return;
        }
        RefuelEvidenceManager.deleteQuietly(evidencePhotoPath);
        evidencePhotoPath = "";
        evidenceCategory = RefuelEvidenceManager.resolveCategory(selectedFuelType);
        updateEvidencePreview();
        Snackbar.make(binding.getRoot(), R.string.evidence_reset_on_fuel_change, Snackbar.LENGTH_SHORT).show();
    }

    private void rebuildChecklist(boolean preserveExistingState) {
        Map<String, Boolean> checkedByKey = new HashMap<>();
        if (preserveExistingState) {
            for (ChecklistItemState item : checklistItems) {
                checkedByKey.put(item.key, item.checked);
            }
        }
        List<ChecklistItemState> builtItems = RefuelChecklistManager.build(
            this,
            selectedFuelType,
            RefuelEvidenceManager.hasEvidence(evidencePhotoPath)
        );
        if (preserveExistingState) {
            for (ChecklistItemState item : builtItems) {
                Boolean previousChecked = checkedByKey.get(item.key);
                if (previousChecked != null && !item.autoManaged) {
                    item.checked = previousChecked;
                }
            }
        }
        checklistItems.clear();
        checklistItems.addAll(builtItems);
        updateChecklistState();
    }

    private void updateChecklistState() {
        RefuelChecklistManager.applyEvidenceState(checklistItems, RefuelEvidenceManager.hasEvidence(evidencePhotoPath));
        boolean completed = RefuelChecklistManager.isComplete(checklistItems);
        if (completed) {
            if (checklistCompletedAtIso == null || checklistCompletedAtIso.trim().isEmpty()) {
                checklistCompletedAtIso = LocalDateTime.now().withSecond(0).withNano(0).toString();
            }
            binding.textChecklistTimestamp.setText(
                getString(R.string.checklist_completed_at, FormatUtils.formatDateTime(checklistCompletedAtIso))
            );
        } else {
            checklistCompletedAtIso = "";
            binding.textChecklistTimestamp.setText(getString(R.string.checklist_pending));
        }
        renderChecklistItems();
    }

    private void renderChecklistItems() {
        LayoutInflater inflater = LayoutInflater.from(this);
        binding.layoutChecklistItems.removeAllViews();
        for (ChecklistItemState item : checklistItems) {
            MaterialCheckBox checkBox = (MaterialCheckBox) inflater.inflate(
                R.layout.item_checklist_option,
                binding.layoutChecklistItems,
                false
            );
            checkBox.setText(item.label);
            checkBox.setChecked(item.checked);
            checkBox.setEnabled(!item.autoManaged);
            checkBox.setAlpha(item.autoManaged ? 0.72f : 1f);
            if (!item.autoManaged) {
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    item.checked = isChecked;
                    updateChecklistState();
                });
            }
            binding.layoutChecklistItems.addView(checkBox);
        }
    }

    private String resolveEntryMode() {
        if (lastExtractionResult == null || !lastExtractionResult.hasAnySuggestion()) {
            return RefuelEntryMode.MANUAL;
        }
        boolean reviewed = false;
        Double currentLiters = FormatUtils.parseFlexibleDecimal(getText(binding.inputLiters));
        Double currentAmount = FormatUtils.parseFlexibleDecimal(getText(binding.inputTotalAmount));
        if (!approximatelyEqual(lastExtractionResult.liters, currentLiters)) {
            reviewed = true;
        }
        if (lastExtractionResult.totalAmount != null && !approximatelyEqual(lastExtractionResult.totalAmount, currentAmount)) {
            reviewed = true;
        }
        if (!sameText(lastExtractionResult.locationName, getText(binding.inputLocation))) {
            reviewed = true;
        }
        String suggestedDate = lastExtractionResult.suppliedAtIso == null ? "" : lastExtractionResult.suppliedAtIso;
        if (!suggestedDate.isEmpty() && !sameText(suggestedDate, suppliedAt.toString())) {
            reviewed = true;
        }
        return reviewed ? RefuelEntryMode.OCR_REVIEWED : RefuelEntryMode.OCR_AUTO;
    }

    private boolean approximatelyEqual(Double first, Double second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return Math.abs(first - second) < 0.01d;
    }

    private boolean shouldReplaceSuggestedInitialOdometer() {
        String currentValue = getText(binding.inputOdometerInitial);
        if (currentValue.isEmpty()) {
            return true;
        }
        if (lastAppliedSuggestedInitialOdometerKm == null) {
            return false;
        }
        try {
            return Integer.parseInt(currentValue) == lastAppliedSuggestedInitialOdometerKm;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private int resolveFinancialValidationColor(String level) {
        if (com.example.arlacontrole.model.RefuelStatus.ALERT.equals(level)) {
            return R.color.danger;
        }
        if (com.example.arlacontrole.model.RefuelStatus.ATTENTION.equals(level)) {
            return R.color.warning;
        }
        return R.color.success;
    }

    private boolean sameText(String first, String second) {
        String normalizedFirst = first == null ? "" : first.trim();
        String normalizedSecond = second == null ? "" : second.trim();
        return normalizedFirst.equalsIgnoreCase(normalizedSecond);
    }

    @NonNull
    private String normalizeLocationOption(String rawValue) {
        String normalized = rawValue == null ? "" : rawValue.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("patio")) {
            return getString(R.string.refuel_location_patio);
        }
        if (normalized.contains("posto")) {
            return getString(R.string.refuel_location_posto);
        }
        return "";
    }

    private int resolveExtractionColor(String status) {
        if (ExtractionStatus.CONFIDENT.equals(status)) {
            return R.color.success_container;
        }
        if (ExtractionStatus.PARTIAL.equals(status) || ExtractionStatus.REVIEW_REQUIRED.equals(status)) {
            return R.color.warning_container;
        }
        return R.color.danger_container;
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            evidenceCategory = RefuelEvidenceManager.resolveCategory(selectedFuelType);
            return;
        }
        String restoredDate = savedInstanceState.getString(KEY_SUPPLIED_AT, "");
        if (!restoredDate.isEmpty()) {
            suppliedAt = LocalDateTime.parse(restoredDate);
        }
        selectedFuelType = savedInstanceState.getString(KEY_FUEL_TYPE, selectedFuelType);
        evidencePhotoPath = savedInstanceState.getString(KEY_EVIDENCE_PATH, "");
        evidenceCategory = savedInstanceState.getString(KEY_EVIDENCE_CATEGORY, RefuelEvidenceManager.resolveCategory(selectedFuelType));
        checklistCompletedAtIso = savedInstanceState.getString(KEY_CHECKLIST_COMPLETED_AT, "");
        checklistItems.clear();
        checklistItems.addAll(RefuelChecklistManager.deserialize(savedInstanceState.getString(KEY_CHECKLIST_PAYLOAD, "")));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SUPPLIED_AT, suppliedAt.toString());
        outState.putString(KEY_FUEL_TYPE, selectedFuelType);
        outState.putString(KEY_EVIDENCE_PATH, evidencePhotoPath);
        outState.putString(KEY_EVIDENCE_CATEGORY, evidenceCategory);
        outState.putString(KEY_CHECKLIST_PAYLOAD, RefuelChecklistManager.serialize(checklistItems));
        outState.putString(KEY_CHECKLIST_COMPLETED_AT, checklistCompletedAtIso);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && !refuelSaved) {
            RefuelEvidenceManager.deleteQuietly(evidencePhotoPath);
        }
    }
}
