package com.example.arlacontrole.ui.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arlacontrole.R;
import com.example.arlacontrole.databinding.ActivityCameraCaptureBinding;
import com.example.arlacontrole.evidence.RefuelEvidenceManager;
import com.example.arlacontrole.model.FuelType;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class CameraCaptureActivity extends AppCompatActivity {

    public static final String EXTRA_FUEL_TYPE = "extra_fuel_type";
    public static final String EXTRA_PHOTO_PATH = "extra_photo_path";
    public static final String EXTRA_EVIDENCE_CATEGORY = "extra_evidence_category";
    public static final String EXTRA_CAPTURE_TITLE = "extra_capture_title";
    public static final String EXTRA_CAPTURE_INSTRUCTION = "extra_capture_instruction";

    private ActivityCameraCaptureBinding binding;
    private final CameraCaptureManager cameraCaptureManager = new CameraCaptureManager();
    private String fuelType = FuelType.ARLA;
    private String evidenceCategory = "";
    private File currentOutputFile;
    private boolean captureCompleted;

    private final ActivityResultLauncher<String> permissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                startCameraPreview();
            } else {
                Snackbar.make(binding.getRoot(), R.string.camera_permission_required, Snackbar.LENGTH_LONG).show();
                finish();
            }
        });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraCaptureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        fuelType = getIntent().getStringExtra(EXTRA_FUEL_TYPE);
        evidenceCategory = getIntent().getStringExtra(EXTRA_EVIDENCE_CATEGORY);
        if (!FuelType.DIESEL.equals(fuelType)) {
            fuelType = FuelType.ARLA;
        }

        String customTitle = getIntent().getStringExtra(EXTRA_CAPTURE_TITLE);
        String customInstruction = getIntent().getStringExtra(EXTRA_CAPTURE_INSTRUCTION);
        if (customTitle != null && !customTitle.trim().isEmpty()) {
            binding.toolbar.setTitle(customTitle);
        }
        binding.textCameraInstruction.setText(
            customInstruction != null && !customInstruction.trim().isEmpty()
                ? customInstruction
                : (FuelType.DIESEL.equals(fuelType)
                    ? getString(R.string.camera_instruction_diesel)
                    : getString(R.string.camera_instruction_arla))
        );

        binding.buttonCapture.setOnClickListener(v -> capturePhoto());
        ensureCameraPermission();
    }

    private void ensureCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraPreview();
            return;
        }
        permissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void startCameraPreview() {
        binding.progressCamera.setVisibility(View.VISIBLE);
        cameraCaptureManager.bind(this, binding.previewView, message -> {
            binding.progressCamera.setVisibility(View.GONE);
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
        });
        binding.progressCamera.setVisibility(View.GONE);
    }

    private void capturePhoto() {
        try {
            currentOutputFile = RefuelEvidenceManager.CATEGORY_SAFETY_OCCURRENCE.equals(evidenceCategory)
                ? RefuelEvidenceManager.createSafetyEvidenceFile(this)
                : RefuelEvidenceManager.createEvidenceFile(this, fuelType);
            binding.buttonCapture.setEnabled(false);
            binding.progressCapture.show();
            cameraCaptureManager.capture(this, currentOutputFile, new CameraCaptureManager.CaptureCallback() {
                @Override
                public void onSuccess(String filePath) {
                    binding.progressCapture.hide();
                    binding.buttonCapture.setEnabled(true);
                    captureCompleted = true;
                    Intent result = new Intent();
                    result.putExtra(EXTRA_PHOTO_PATH, filePath);
                    setResult(RESULT_OK, result);
                    finish();
                }

                @Override
                public void onError(String message) {
                    binding.progressCapture.hide();
                    binding.buttonCapture.setEnabled(true);
                    if (currentOutputFile != null) {
                        RefuelEvidenceManager.deleteQuietly(currentOutputFile.getAbsolutePath());
                    }
                    Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
                }
            });
        } catch (Exception exception) {
            Snackbar.make(binding.getRoot(), R.string.camera_capture_error, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraCaptureManager.unbind();
        if (isFinishing() && !captureCompleted && currentOutputFile != null) {
            RefuelEvidenceManager.deleteQuietly(currentOutputFile.getAbsolutePath());
        }
    }
}
