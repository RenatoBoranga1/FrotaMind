package com.example.arlacontrole.ui.campo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.arlacontrole.R;
import com.example.arlacontrole.data.local.OdometerCalibrationEntity;
import com.example.arlacontrole.databinding.FragmentFieldHomeBinding;
import com.example.arlacontrole.model.CalibrationDeadlineStatus;
import com.example.arlacontrole.ui.RefuelAdapter;
import com.example.arlacontrole.ui.SafetyEventAdapter;
import com.example.arlacontrole.rules.CalibrationDeadlineRules;
import com.example.arlacontrole.ui.afericao.NewCalibrationActivity;
import com.example.arlacontrole.ui.abastecimento.NewRefuelActivity;
import com.example.arlacontrole.ui.historico.RefuelDetailActivity;
import com.example.arlacontrole.ui.seguranca.SafetyEventDetailActivity;
import com.example.arlacontrole.utils.FormatUtils;
import com.google.android.material.snackbar.Snackbar;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FieldHomeFragment extends Fragment {

    private static final String TAG = "FieldHomeFragment";

    private FragmentFieldHomeBinding binding;
    private FieldHomeViewModel viewModel;
    private RefuelAdapter refuelAdapter;
    private SafetyEventAdapter safetyEventAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFieldHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FieldHomeViewModel.class);
        refuelAdapter = new RefuelAdapter(entity -> {
            Intent intent = new Intent(requireContext(), RefuelDetailActivity.class);
            intent.putExtra(RefuelDetailActivity.EXTRA_REFUEL_ID, entity.localId);
            startActivity(intent);
        });
        safetyEventAdapter = new SafetyEventAdapter(entity -> {
            Intent intent = new Intent(requireContext(), SafetyEventDetailActivity.class);
            intent.putExtra(SafetyEventDetailActivity.EXTRA_SAFETY_EVENT_ID, entity.localId);
            startActivity(intent);
        });

        binding.recyclerFieldRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerFieldRecent.setAdapter(refuelAdapter);
        binding.recyclerFieldSafetyRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerFieldSafetyRecent.setAdapter(safetyEventAdapter);
        binding.buttonNewFieldRefuel.setOnClickListener(v -> startActivity(new Intent(requireContext(), NewRefuelActivity.class)));
        binding.buttonFieldCalibration.setOnClickListener(v -> openCalibrationScreen());

        viewModel.getPendingSyncCount().observe(getViewLifecycleOwner(), count -> {
            binding.textFieldPending.setText(getString(R.string.pending_sync) + ": " + (count == null ? 0 : count));
        });
        viewModel.getRecentRefuels().observe(getViewLifecycleOwner(), items -> {
            refuelAdapter.submitList(items);
            binding.textFieldEmpty.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getRecentSafetyEvents().observe(getViewLifecycleOwner(), items -> {
            safetyEventAdapter.submitList(items);
            binding.textFieldSafetyEmpty.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getLatestCalibration().observe(getViewLifecycleOwner(), this::renderCalibrationCard);
        refreshLastSync();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLastSync();
    }

    private void refreshLastSync() {
        if (binding == null || viewModel == null) {
            return;
        }
        long lastSyncAt = viewModel.getLastSyncAt();
        if (lastSyncAt > 0L) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSyncAt), ZoneId.systemDefault());
            binding.textFieldLastSync.setText(getString(R.string.sync_last) + ": " + FormatUtils.formatDateTime(dateTime.toString()));
        } else {
            binding.textFieldLastSync.setText(getString(R.string.sync_last) + ": --");
        }
    }

    private void renderCalibrationCard(OdometerCalibrationEntity calibration) {
        if (binding == null) {
            return;
        }
        if (calibration == null) {
            binding.textFieldCalibrationDate.setText(getString(R.string.calibration_home_empty_date));
            binding.textFieldCalibrationOdometer.setText(getString(R.string.calibration_home_empty_odometer));
            binding.chipFieldCalibrationStatus.setText(getString(R.string.calibration_home_missing));
            binding.chipFieldCalibrationStatus.setChipBackgroundColorResource(R.color.danger_container);
            return;
        }
        CalibrationDeadlineStatus status = CalibrationDeadlineRules.evaluate(calibration.calibrationAtIso);
        binding.textFieldCalibrationDate.setText(
            getString(R.string.calibration_last_date_label, FormatUtils.formatDate(calibration.calibrationAtIso))
        );
        binding.textFieldCalibrationOdometer.setText(
            getString(R.string.calibration_last_odometer_label, FormatUtils.formatKilometers(calibration.odometerKm))
        );
        binding.chipFieldCalibrationStatus.setText(status.message);
        binding.chipFieldCalibrationStatus.setChipBackgroundColorResource(resolveDeadlineColor(status.level));
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

    private void openCalibrationScreen() {
        try {
            startActivity(new Intent(requireContext(), NewCalibrationActivity.class));
        } catch (ActivityNotFoundException exception) {
            Log.e(TAG, "Tela de afericao nao encontrada.", exception);
            Snackbar.make(binding.getRoot(), R.string.calibration_open_error, Snackbar.LENGTH_LONG).show();
        } catch (Exception exception) {
            Log.e(TAG, "Falha ao abrir tela de afericao.", exception);
            Snackbar.make(binding.getRoot(), R.string.calibration_open_error, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
