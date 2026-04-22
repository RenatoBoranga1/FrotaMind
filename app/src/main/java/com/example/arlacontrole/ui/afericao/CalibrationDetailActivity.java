package com.example.arlacontrole.ui.afericao;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.arlacontrole.R;
import com.example.arlacontrole.data.local.OdometerCalibrationEntity;
import com.example.arlacontrole.data.local.OdometerCalibrationMediaEntity;
import com.example.arlacontrole.databinding.ActivityCalibrationDetailBinding;
import com.example.arlacontrole.media.CalibrationMediaManager;
import com.example.arlacontrole.model.CalibrationDeadlineStatus;
import com.example.arlacontrole.model.CalibrationMediaType;
import com.example.arlacontrole.rules.CalibrationDeadlineRules;
import com.example.arlacontrole.utils.FormatUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class CalibrationDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CALIBRATION_ID = "extra_calibration_id";
    private static final String TAG = "CalibrationDetailAct";

    private ActivityCalibrationDetailBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalibrationDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        long calibrationId = getIntent() == null ? -1L : getIntent().getLongExtra(EXTRA_CALIBRATION_ID, -1L);
        if (calibrationId <= 0L) {
            Snackbar.make(binding.getRoot(), R.string.calibration_loading_error, Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        CalibrationDetailViewModel viewModel = new ViewModelProvider(this).get(CalibrationDetailViewModel.class);
        viewModel.setCalibrationId(calibrationId);
        viewModel.getCalibration().observe(this, this::bindCalibration);
        viewModel.getMedia().observe(this, this::bindMedia);
    }

    private void bindCalibration(OdometerCalibrationEntity calibration) {
        if (calibration == null) {
            return;
        }
        boolean hasVehicleLink = calibration.vehiclePlate != null && !calibration.vehiclePlate.trim().isEmpty();
        binding.textCalibrationVehicle.setText(
            hasVehicleLink
                ? calibration.vehiclePlate + " | " + calibration.vehicleModel
                : getString(R.string.calibration_general_record)
        );
        binding.textCalibrationFleet.setText(
            hasVehicleLink
                ? getString(R.string.label_fleet_code, calibration.vehicleFleetCode)
                : getString(R.string.calibration_without_vehicle)
        );
        binding.textCalibrationDate.setText(getString(R.string.calibration_last_date_label, FormatUtils.formatDateTime(calibration.calibrationAtIso)));
        binding.textCalibrationOdometer.setText(getString(R.string.calibration_last_odometer_label, FormatUtils.formatKilometers(calibration.odometerKm)));
        binding.textCalibrationNotes.setText(
            calibration.notes == null || calibration.notes.trim().isEmpty()
                ? getString(R.string.detail_notes_empty)
                : calibration.notes
        );
        binding.textCalibrationRegisteredBy.setText(getString(R.string.calibration_responsible_value, calibration.registeredByName));
        CalibrationDeadlineStatus status = CalibrationDeadlineRules.evaluate(calibration.calibrationAtIso);
        binding.chipCalibrationDeadline.setText(status.message);
        binding.chipCalibrationDeadline.setChipBackgroundColorResource(resolveDeadlineColor(status.level));
    }

    private void bindMedia(List<OdometerCalibrationMediaEntity> mediaItems) {
        binding.layoutCalibrationMedia.removeAllViews();
        if (mediaItems == null || mediaItems.isEmpty()) {
            binding.textCalibrationMediaEmpty.setVisibility(View.VISIBLE);
            return;
        }
        binding.textCalibrationMediaEmpty.setVisibility(View.GONE);
        for (OdometerCalibrationMediaEntity media : mediaItems) {
            TextView item = new TextView(this);
            item.setText(
                CalibrationMediaType.VIDEO.equals(media.mediaType)
                    ? getString(R.string.calibration_video_open)
                    : getString(R.string.calibration_photo_open)
            );
            item.setTextAppearance(this, com.google.android.material.R.style.TextAppearance_MaterialComponents_Body1);
            item.setPadding(0, 0, 0, 24);
            item.setOnClickListener(v -> openMedia(media));
            binding.layoutCalibrationMedia.addView(item);
        }
    }

    private void openMedia(OdometerCalibrationMediaEntity media) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(
                CalibrationMediaManager.buildUri(this, media.filePath),
                CalibrationMediaType.VIDEO.equals(media.mediaType) ? "video/*" : "image/*"
            );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Log.w(TAG, "Nenhum app compativel para abrir a evidencia da afericao.", exception);
            Snackbar.make(binding.getRoot(), R.string.evidence_open_error, Snackbar.LENGTH_LONG).show();
        } catch (Exception exception) {
            Log.e(TAG, "Falha ao abrir evidencia da afericao.", exception);
            Snackbar.make(binding.getRoot(), R.string.evidence_open_error, Snackbar.LENGTH_LONG).show();
        }
    }

    private int resolveDeadlineColor(String level) {
        if (CalibrationDeadlineRules.LEVEL_OK.equals(level)) {
            return R.color.success_container;
        }
        if (CalibrationDeadlineRules.LEVEL_WARNING.equals(level)) {
            return R.color.warning_container;
        }
        return R.color.danger_container;
    }
}
