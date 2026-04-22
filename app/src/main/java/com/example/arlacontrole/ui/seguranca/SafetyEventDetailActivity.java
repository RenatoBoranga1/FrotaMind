package com.example.arlacontrole.ui.seguranca;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.arlacontrole.R;
import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.data.repository.RepositoryCallback;
import com.example.arlacontrole.databinding.ActivitySafetyEventDetailBinding;
import com.example.arlacontrole.evidence.RefuelEvidenceManager;
import com.example.arlacontrole.model.SafetyAnalysisStatus;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.utils.AppPreferences;
import com.example.arlacontrole.utils.FormatUtils;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class SafetyEventDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SAFETY_EVENT_ID = "extra_safety_event_id";

    private ActivitySafetyEventDetailBinding binding;
    private SafetyEventDetailViewModel viewModel;
    private long localId;
    private SafetyEventEntity currentEvent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySafetyEventDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        localId = getIntent().getLongExtra(EXTRA_SAFETY_EVENT_ID, -1L);
        if (localId <= 0L) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(SafetyEventDetailViewModel.class);
        binding.buttonOpenSafetyEvidence.setOnClickListener(v -> openEvidence());
        binding.buttonResolveSafetyEvent.setOnClickListener(v -> markResolved());

        viewModel.getEvent(localId).observe(this, this::bindEvent);
    }

    private void bindEvent(SafetyEventEntity event) {
        currentEvent = event;
        if (event == null) {
            finish();
            return;
        }

        binding.textSafetyDetailSummary.setText(event.vehiclePlate + " | " + event.driverName);
        binding.chipSafetyDetailType.setText(FormatUtils.formatSafetyEventType(this, event.eventType));
        binding.chipSafetyDetailSeverity.setText(FormatUtils.formatSafetySeverity(this, event.severity));
        binding.chipSafetyDetailStatus.setText(FormatUtils.formatSafetyAnalysisStatus(this, event.analysisStatus));
        binding.textSafetyDetailMeta.setText(
            getString(
                R.string.safety_detail_meta,
                FormatUtils.formatDateTime(event.occurredAtIso),
                event.locationName,
                event.vehicleFleetCode,
                event.vehicleModel
            )
        );
        binding.textSafetyDetailDescription.setText(event.description);
        binding.textSafetyDetailCause.setText(getString(R.string.safety_detail_cause, emptyFallback(event.probableCause)));
        binding.textSafetyDetailNotes.setText(getString(R.string.safety_detail_notes, emptyFallback(event.notes)));

        Bitmap preview = RefuelEvidenceManager.loadPreview(event.evidencePhotoPath, 1000, 700);
        binding.imageSafetyDetail.setImageBitmap(preview);
        boolean hasLocalEvidence = event.hasEvidence() && new File(event.evidencePhotoPath).exists();
        binding.buttonOpenSafetyEvidence.setVisibility(hasLocalEvidence ? View.VISIBLE : View.GONE);
        boolean canResolve = UserRole.canAccessManagement(new AppPreferences(this).getSession().role)
            && !SafetyAnalysisStatus.isResolved(event.analysisStatus);
        binding.buttonResolveSafetyEvent.setVisibility(canResolve ? View.VISIBLE : View.GONE);
    }

    private void openEvidence() {
        if (currentEvent == null || !currentEvent.hasEvidence()) {
            Snackbar.make(binding.getRoot(), R.string.evidence_open_error, Snackbar.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = RefuelEvidenceManager.buildContentUri(this, new File(currentEvent.evidencePhotoPath));
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Snackbar.make(binding.getRoot(), R.string.evidence_open_error, Snackbar.LENGTH_LONG).show();
        }
    }

    private void markResolved() {
        viewModel.updateStatus(localId, SafetyAnalysisStatus.RESOLVED, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Snackbar.make(binding.getRoot(), R.string.safety_status_updated, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(String message) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private String emptyFallback(String value) {
        return value == null || value.trim().isEmpty() ? getString(R.string.detail_notes_empty) : value.trim();
    }
}
