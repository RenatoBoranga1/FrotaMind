package com.example.arlacontrole.ui.historico;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.arlacontrole.R;
import com.example.arlacontrole.checklist.ChecklistItemState;
import com.example.arlacontrole.checklist.RefuelChecklistManager;
import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.databinding.ActivityRefuelDetailBinding;
import com.example.arlacontrole.evidence.RefuelEvidenceManager;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.utils.FormatUtils;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.List;

public class RefuelDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REFUEL_ID = "extra_refuel_id";

    private ActivityRefuelDetailBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRefuelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        long refuelId = getIntent().getLongExtra(EXTRA_REFUEL_ID, -1L);
        if (refuelId <= 0L) {
            finish();
            return;
        }

        RefuelDetailViewModel viewModel = new ViewModelProvider(this).get(RefuelDetailViewModel.class);
        viewModel.setRefuelId(refuelId);
        viewModel.getRefuel().observe(this, refuel -> {
            if (refuel == null) {
                return;
            }
            binding.textDetailPlate.setText(refuel.vehiclePlate + " | " + refuel.vehicleModel);
            binding.textDetailFleet.setText(getString(R.string.label_fleet_code, refuel.vehicleFleetCode));
            binding.textDetailFuelType.setText(getString(R.string.label_fuel_type, FormatUtils.formatFuelType(this, refuel.fuelType)));
            binding.textDetailDriver.setText(getString(R.string.detail_driver, refuel.driverName));
            binding.textDetailLiters.setText(FormatUtils.formatLiters(refuel.liters));
            binding.textDetailTotalAmount.setVisibility(refuel.totalAmount != null && refuel.totalAmount > 0d ? View.VISIBLE : View.GONE);
            if (refuel.totalAmount != null && refuel.totalAmount > 0d) {
                binding.textDetailTotalAmount.setText(getString(R.string.detail_total_amount, FormatUtils.formatCurrency(refuel.totalAmount)));
            }
            binding.textDetailOdometer.setText(getString(R.string.detail_odometer, FormatUtils.formatKilometers(refuel.odometerKm)));
            binding.textDetailDate.setText(getString(R.string.detail_date, FormatUtils.formatDateTime(refuel.suppliedAtIso)));
            binding.textDetailLocation.setText(getString(R.string.detail_location, refuel.locationName));
            binding.textDetailNotes.setText(refuel.notes == null || refuel.notes.isEmpty() ? getString(R.string.detail_notes_empty) : refuel.notes);
            binding.textDetailStatus.setText(getString(R.string.detail_status, FormatUtils.formatStatus(this, refuel.statusLevel)));
            binding.textDetailReason.setText(buildAnalysis(refuel));
            binding.textDetailSync.setText(getString(R.string.detail_sync, FormatUtils.formatSyncStatus(this, refuel.syncStatus)));
            binding.textDetailEntryMode.setText(getString(R.string.detail_entry_mode, FormatUtils.formatEntryMode(this, refuel.dataEntryMode)));
            binding.textDetailExtractionStatus.setText(getString(R.string.detail_extraction_status, FormatUtils.formatExtractionStatus(this, refuel.ocrStatus)));
            bindEvidence(refuel);
            bindChecklist(refuel);
        });
    }

    private String buildAnalysis(RefuelEntity refuel) {
        String analysis = refuel.statusReason;
        Double metric = FuelType.DIESEL.equals(refuel.fuelType) ? refuel.kmPerLiter : refuel.litersPer1000Km;
        if (metric != null && metric > 0d) {
            analysis = analysis + "\n" + getString(
                R.string.metric_average_label,
                FormatUtils.formatFuelMetric(this, refuel.fuelType, metric)
            );
        }
        return analysis;
    }

    private void bindEvidence(RefuelEntity refuel) {
        if (!RefuelEvidenceManager.hasEvidence(refuel.evidencePhotoPath)) {
            binding.cardEvidence.setVisibility(android.view.View.GONE);
            return;
        }
        binding.cardEvidence.setVisibility(android.view.View.VISIBLE);
        binding.textEvidenceTitle.setText(RefuelEvidenceManager.buildTitle(this, refuel.evidenceCategory));
        Bitmap bitmap = RefuelEvidenceManager.loadPreview(refuel.evidencePhotoPath, 1400, 1400);
        binding.imageEvidenceDetail.setImageBitmap(bitmap);
        binding.buttonOpenEvidence.setOnClickListener(v -> openEvidence(refuel));
        binding.imageEvidenceDetail.setOnClickListener(v -> openEvidence(refuel));
    }

    private void openEvidence(RefuelEntity refuel) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(
                RefuelEvidenceManager.buildContentUri(this, new File(refuel.evidencePhotoPath)),
                "image/jpeg"
            );
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Snackbar.make(binding.getRoot(), R.string.evidence_open_error, Snackbar.LENGTH_LONG).show();
        }
    }

    private void bindChecklist(RefuelEntity refuel) {
        List<ChecklistItemState> items = RefuelChecklistManager.deserialize(refuel.checklistPayload);
        if (items.isEmpty()) {
            binding.cardChecklist.setVisibility(View.GONE);
            return;
        }

        binding.cardChecklist.setVisibility(View.VISIBLE);
        binding.textChecklistCompletedAt.setText(
            refuel.checklistCompletedAtIso == null || refuel.checklistCompletedAtIso.trim().isEmpty()
                ? getString(R.string.checklist_pending)
                : getString(R.string.checklist_completed_at, FormatUtils.formatDateTime(refuel.checklistCompletedAtIso))
        );
        LayoutInflater inflater = LayoutInflater.from(this);
        binding.layoutChecklistDetailItems.removeAllViews();
        for (ChecklistItemState item : items) {
            MaterialCheckBox checkBox = (MaterialCheckBox) inflater.inflate(
                R.layout.item_checklist_option,
                binding.layoutChecklistDetailItems,
                false
            );
            checkBox.setText(item.label);
            checkBox.setChecked(item.checked);
            checkBox.setEnabled(false);
            checkBox.setAlpha(item.checked ? 1f : 0.72f);
            binding.layoutChecklistDetailItems.addView(checkBox);
        }
    }
}
