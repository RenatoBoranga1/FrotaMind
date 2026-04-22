package com.example.arlacontrole.ui.afericao;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.arlacontrole.R;
import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.databinding.ActivityNewCalibrationBinding;
import com.example.arlacontrole.evidence.RefuelEvidenceManager;
import com.example.arlacontrole.media.CalibrationMediaManager;
import com.example.arlacontrole.model.NewCalibrationInput;
import com.example.arlacontrole.utils.FormatUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NewCalibrationActivity extends AppCompatActivity {

    private static final String TAG = "NewCalibrationActivity";
    private static final List<String> RESPONSIBLE_OPTIONS = Arrays.asList(
        "Carlos Mendes",
        "Marina Souza",
        "Rafael Lima",
        "Patricia Costa",
        "Juliana Ferreira"
    );

    private ActivityNewCalibrationBinding binding;
    private NewCalibrationViewModel viewModel;
    private final List<String> photoPaths = new ArrayList<>();
    private final List<String> videoPaths = new ArrayList<>();
    private LocalDateTime calibrationAt = LocalDateTime.now().withSecond(0).withNano(0);
    private boolean calibrationSaved;

    private final ActivityResultLauncher<String> pickPhotoLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> importPhoto(uri));

    private final ActivityResultLauncher<String> pickVideoLauncher =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> importVideo(uri));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityNewCalibrationBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            setSupportActionBar(binding.toolbar);
            binding.toolbar.setNavigationOnClickListener(v -> finish());

            viewModel = new ViewModelProvider(this).get(NewCalibrationViewModel.class);
            updateCalibrationDate();
            renderMediaState();
            binding.inputLayoutCalibrationOdometer.setHelperText(getString(R.string.calibration_subtitle));
            bindResponsibles();

            binding.inputCalibrationDate.setOnClickListener(v -> openDatePicker());
            binding.buttonAddCalibrationPhoto.setOnClickListener(v -> pickPhotoLauncher.launch("image/*"));
            binding.buttonAddCalibrationVideo.setOnClickListener(v -> pickVideoLauncher.launch("video/*"));
            binding.buttonSaveCalibration.setOnClickListener(v -> saveCalibration());
        } catch (Exception exception) {
            Log.e(TAG, "Falha ao inicializar a tela de afericao.", exception);
            Toast.makeText(this, R.string.calibration_loading_error, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void bindResponsibles() {
        binding.autoCalibrationResponsible.setAdapter(
            new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RESPONSIBLE_OPTIONS)
        );
    }

    private void openDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calibrationAt = calibrationAt.withYear(year).withMonth(month + 1).withDayOfMonth(dayOfMonth);
                updateCalibrationDate();
            },
            calibrationAt.getYear(),
            calibrationAt.getMonthValue() - 1,
            calibrationAt.getDayOfMonth()
        );
        dialog.show();
    }

    private void importPhoto(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            photoPaths.add(CalibrationMediaManager.copyPhotoToLocal(this, uri));
            renderMediaState();
        } catch (Exception exception) {
            Log.e(TAG, "Falha ao importar foto da afericao.", exception);
            Snackbar.make(
                binding.getRoot(),
                exception.getMessage() == null ? getString(R.string.calibration_loading_error) : exception.getMessage(),
                Snackbar.LENGTH_LONG
            ).show();
        }
    }

    private void importVideo(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            videoPaths.add(CalibrationMediaManager.copyVideoToLocal(this, uri));
            renderMediaState();
        } catch (Exception exception) {
            Log.e(TAG, "Falha ao importar video da afericao.", exception);
            Snackbar.make(
                binding.getRoot(),
                exception.getMessage() == null ? getString(R.string.calibration_loading_error) : exception.getMessage(),
                Snackbar.LENGTH_LONG
            ).show();
        }
    }

    private void renderMediaState() {
        binding.textCalibrationPhotoCount.setText(getString(R.string.calibration_photo_count, photoPaths.size()));
        binding.textCalibrationVideoCount.setText(getString(R.string.calibration_video_count, videoPaths.size()));
        if (photoPaths.isEmpty()) {
            binding.imageCalibrationPreview.setVisibility(View.GONE);
        } else {
            try {
                binding.imageCalibrationPreview.setVisibility(View.VISIBLE);
                binding.imageCalibrationPreview.setImageBitmap(
                    RefuelEvidenceManager.loadPreview(photoPaths.get(photoPaths.size() - 1), 1200, 1200)
                );
            } catch (Exception exception) {
                Log.e(TAG, "Falha ao renderizar preview da afericao.", exception);
                binding.imageCalibrationPreview.setVisibility(View.GONE);
                Snackbar.make(binding.getRoot(), R.string.calibration_media_preview_error, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void updateCalibrationDate() {
        binding.inputCalibrationDate.setText(FormatUtils.formatDateTime(calibrationAt.toString()));
    }

    private void saveCalibration() {
        try {
            String responsible = resolveResponsible();
            if (responsible.isEmpty()) {
                binding.autoCalibrationResponsible.setError(getString(R.string.invalid_calibration_responsible));
                return;
            }
            binding.autoCalibrationResponsible.setError(null);

            Integer odometer = parseInteger();
            if (odometer == null || odometer <= 0) {
                binding.inputCalibrationOdometer.setError(getString(R.string.invalid_odometer));
                return;
            }
            binding.inputCalibrationOdometer.setError(null);

            NewCalibrationInput input = new NewCalibrationInput();
            input.vehiclePlate = "";
            input.calibrationAtIso = calibrationAt.toString();
            input.odometerKm = odometer;
            input.notes = textValue(binding.inputCalibrationNotes);
            input.registeredByName = responsible;
            input.photoPaths.addAll(photoPaths);
            input.videoPaths.addAll(videoPaths);

            binding.progressSaveCalibration.show();
            binding.buttonSaveCalibration.setEnabled(false);
            viewModel.saveCalibration(input, new RepositoryCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    calibrationSaved = true;
                    binding.progressSaveCalibration.hide();
                    binding.buttonSaveCalibration.setEnabled(true);
                    Snackbar.make(binding.getRoot(), R.string.calibration_save_success, Snackbar.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onError(String message) {
                    Log.w(TAG, "Falha validada ao salvar afericao: " + message);
                    binding.progressSaveCalibration.hide();
                    binding.buttonSaveCalibration.setEnabled(true);
                    Snackbar.make(
                        binding.getRoot(),
                        message == null || message.trim().isEmpty() ? getString(R.string.calibration_loading_error) : message,
                        Snackbar.LENGTH_LONG
                    ).show();
                }
            });
        } catch (Exception exception) {
            Log.e(TAG, "Falha inesperada ao salvar afericao.", exception);
            binding.progressSaveCalibration.hide();
            binding.buttonSaveCalibration.setEnabled(true);
            Snackbar.make(binding.getRoot(), R.string.calibration_loading_error, Snackbar.LENGTH_LONG).show();
        }
    }

    private String resolveResponsible() {
        String selected = textValue(binding.autoCalibrationResponsible);
        for (String option : RESPONSIBLE_OPTIONS) {
            if (option.equalsIgnoreCase(selected)) {
                return option;
            }
        }
        return "";
    }

    private Integer parseInteger() {
        try {
            return Integer.parseInt(textValue(binding.inputCalibrationOdometer));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String textValue(android.widget.TextView textView) {
        return textView.getText() == null ? "" : textView.getText().toString().trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && !calibrationSaved) {
            for (String photoPath : photoPaths) {
                CalibrationMediaManager.deleteQuietly(photoPath);
            }
            for (String videoPath : videoPaths) {
                CalibrationMediaManager.deleteQuietly(videoPath);
            }
        }
    }
}
