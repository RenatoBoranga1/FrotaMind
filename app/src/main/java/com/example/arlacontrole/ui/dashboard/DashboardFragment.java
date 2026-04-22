package com.example.arlacontrole.ui.dashboard;

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
import com.example.arlacontrole.databinding.FragmentDashboardBinding;
import com.example.arlacontrole.model.CalibrationDeadlineStatus;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.rules.CalibrationDeadlineRules;
import com.example.arlacontrole.ui.PriorityAlertAdapter;
import com.example.arlacontrole.ui.RefuelAdapter;
import com.example.arlacontrole.ui.SafetyEventAdapter;
import com.example.arlacontrole.ui.afericao.NewCalibrationActivity;
import com.example.arlacontrole.ui.abastecimento.NewRefuelActivity;
import com.example.arlacontrole.ui.export.ReportExportBottomSheet;
import com.example.arlacontrole.ui.historico.RefuelDetailActivity;
import com.example.arlacontrole.ui.seguranca.SafetyEventDetailActivity;
import com.example.arlacontrole.utils.AppPreferences;
import com.example.arlacontrole.utils.FormatUtils;
import com.example.arlacontrole.utils.NetworkUtils;
import com.google.android.material.snackbar.Snackbar;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private RefuelAdapter refuelAdapter;
    private SafetyEventAdapter safetyEventAdapter;
    private PriorityAlertAdapter alertAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

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
        alertAdapter = new PriorityAlertAdapter();

        binding.recyclerRecentRefuels.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerRecentRefuels.setAdapter(refuelAdapter);
        binding.recyclerRecentSafetyEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerRecentSafetyEvents.setAdapter(safetyEventAdapter);
        binding.recyclerDashboardAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerDashboardAlerts.setAdapter(alertAdapter);

        binding.buttonNewRefuel.setOnClickListener(v -> startActivity(new Intent(requireContext(), NewRefuelActivity.class)));
        binding.buttonDashboardCalibration.setOnClickListener(v -> openCalibrationScreen());

        boolean canExport = UserRole.canExportReports(new AppPreferences(requireContext()).getSession().role);
        binding.buttonExportDashboard.setVisibility(canExport ? View.VISIBLE : View.GONE);
        binding.buttonExportDashboard.setOnClickListener(v -> ReportExportBottomSheet.newInstance(null, null, null, null, null)
            .show(getParentFragmentManager(), "report_export_dashboard"));
        refreshConnectionBanner();

        viewModel.getSnapshot().observe(getViewLifecycleOwner(), snapshot -> {
            if (snapshot == null) {
                return;
            }
            binding.textTotalToday.setText(FormatUtils.formatCurrency(snapshot.totalCostToday));
            binding.textArlaMonth.setText(FormatUtils.formatCurrency(snapshot.totalCostPeriod));
            binding.textDieselMonth.setText(
                snapshot.averageCostPerKm > 0d
                    ? getString(R.string.financial_cost_km_metric, FormatUtils.formatCurrency(snapshot.averageCostPerKm))
                    : getString(R.string.metric_not_available)
            );
            binding.textSafetyMonth.setText(String.valueOf(snapshot.totalSafetyEventsPeriod));
            binding.textCriticalCount.setText(String.valueOf(snapshot.activeAlerts));
            binding.textExecutiveSummary.setText(snapshot.executiveSummary);
            binding.textDashboardInsights.setText(joinInsights(snapshot.integratedInsights));
            alertAdapter.submitList(snapshot.priorityAlerts);
            binding.textDashboardAlertsEmpty.setVisibility(snapshot.priorityAlerts.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getLatestCalibration().observe(getViewLifecycleOwner(), this::renderCalibrationCard);

        viewModel.getRecentRefuels().observe(getViewLifecycleOwner(), refuels -> {
            refuelAdapter.submitList(refuels);
            binding.textRecentEmpty.setVisibility(refuels == null || refuels.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getRecentSafetyEvents().observe(getViewLifecycleOwner(), events -> {
            safetyEventAdapter.submitList(events);
            binding.textRecentSafetyEmpty.setVisibility(events == null || events.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshConnectionBanner();
    }

    private void refreshConnectionBanner() {
        if (binding == null) {
            return;
        }
        binding.textOfflineBanner.setText(NetworkUtils.isOnline(requireContext()) ? getString(R.string.sync_ready) : getString(R.string.offline_banner));
        binding.textOfflineBanner.setVisibility(View.VISIBLE);
    }

    private void renderCalibrationCard(OdometerCalibrationEntity calibration) {
        if (binding == null) {
            return;
        }
        if (calibration == null) {
            binding.textDashboardCalibrationDate.setText(getString(R.string.calibration_home_empty_date));
            binding.textDashboardCalibrationOdometer.setText(getString(R.string.calibration_home_empty_odometer));
            binding.chipDashboardCalibrationStatus.setText(getString(R.string.calibration_home_missing));
            binding.chipDashboardCalibrationStatus.setChipBackgroundColorResource(R.color.danger_container);
            return;
        }
        CalibrationDeadlineStatus status = CalibrationDeadlineRules.evaluate(calibration.calibrationAtIso);
        binding.textDashboardCalibrationDate.setText(
            getString(R.string.calibration_last_date_label, FormatUtils.formatDate(calibration.calibrationAtIso))
        );
        binding.textDashboardCalibrationOdometer.setText(
            getString(R.string.calibration_last_odometer_label, FormatUtils.formatKilometers(calibration.odometerKm))
        );
        binding.chipDashboardCalibrationStatus.setText(status.message);
        binding.chipDashboardCalibrationStatus.setChipBackgroundColorResource(resolveDeadlineColor(status.level));
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

    private String joinInsights(java.util.List<String> insights) {
        if (insights == null || insights.isEmpty()) {
            return getString(R.string.dashboard_integrated_fallback);
        }
        StringBuilder builder = new StringBuilder();
        for (String insight : insights) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append("- ").append(insight);
        }
        return builder.toString();
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
