package com.example.arlacontrole.ui.indicadores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.arlacontrole.R;
import com.example.arlacontrole.databinding.FragmentIndicatorsBinding;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.ui.FinancialRankingAdapter;
import com.example.arlacontrole.ui.PriorityAlertAdapter;
import com.example.arlacontrole.ui.SafetyRankingAdapter;
import com.example.arlacontrole.ui.export.ReportExportBottomSheet;
import com.example.arlacontrole.ui.veiculos.VehicleDetailActivity;
import com.example.arlacontrole.utils.AppPreferences;
import com.example.arlacontrole.utils.FormatUtils;

import java.util.Locale;

public class IndicatorsFragment extends Fragment {

    private FragmentIndicatorsBinding binding;
    private IndicatorsViewModel viewModel;
    private FinancialRankingAdapter vehicleCostRankingAdapter;
    private FinancialRankingAdapter driverCostRankingAdapter;
    private SafetyRankingAdapter vehicleSafetyRankingAdapter;
    private SafetyRankingAdapter driverSafetyRankingAdapter;
    private PriorityAlertAdapter alertAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentIndicatorsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(IndicatorsViewModel.class);

        vehicleCostRankingAdapter = new FinancialRankingAdapter(this::openVehicle);
        driverCostRankingAdapter = new FinancialRankingAdapter(key -> { });
        vehicleSafetyRankingAdapter = new SafetyRankingAdapter(key -> { });
        driverSafetyRankingAdapter = new SafetyRankingAdapter(key -> { });
        alertAdapter = new PriorityAlertAdapter();

        binding.recyclerDieselRanking.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerDieselRanking.setAdapter(vehicleCostRankingAdapter);
        binding.recyclerArlaRanking.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerArlaRanking.setAdapter(driverCostRankingAdapter);
        binding.recyclerVehicleSafetyRanking.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerVehicleSafetyRanking.setAdapter(vehicleSafetyRankingAdapter);
        binding.recyclerDriverSafetyRanking.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerDriverSafetyRanking.setAdapter(driverSafetyRankingAdapter);
        binding.recyclerIndicatorAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerIndicatorAlerts.setAdapter(alertAdapter);

        boolean canExport = UserRole.canExportReports(new AppPreferences(requireContext()).getSession().role);
        binding.buttonExportIndicators.setVisibility(canExport ? View.VISIBLE : View.GONE);
        binding.buttonExportIndicators.setOnClickListener(v -> ReportExportBottomSheet.newInstance(null, null, null, null, null)
            .show(getParentFragmentManager(), "report_export_indicators"));

        viewModel.getSnapshot().observe(getViewLifecycleOwner(), snapshot -> {
            if (snapshot == null) {
                return;
            }
            binding.textArlaMonthlyTotal.setText(FormatUtils.formatCurrency(snapshot.totalArlaCostPeriod));
            binding.textDieselMonthlyTotal.setText(FormatUtils.formatCurrency(snapshot.totalDieselCostPeriod));
            binding.textSafetyMonthlyTotal.setText(String.valueOf(snapshot.totalSafetyEvents));
            binding.textEvidenceCoverage.setText(
                getString(R.string.financial_total_cost_period) + ": " + FormatUtils.formatCurrency(snapshot.totalCostPeriod)
            );
            String averageCostPerKm = snapshot.averageCostPerKm > 0d
                ? getString(R.string.financial_cost_km_metric, FormatUtils.formatCurrency(snapshot.averageCostPerKm))
                : getString(R.string.metric_not_available);
            String variation = String.format(Locale.US, "%.0f%%", snapshot.costVariationPercent);
            binding.textResolutionRate.setText(
                getString(R.string.financial_variation_label, variation)
                    + " | " + getString(R.string.financial_average_cost_km) + ": " + averageCostPerKm
            );
            binding.textIndicatorsRisk.setText(getString(R.string.security_risk_indicator, snapshot.safetyRiskLevel.toUpperCase(Locale.ROOT)));
            binding.textIndicatorInsights.setText(joinInsights(snapshot.integratedInsights));
            vehicleCostRankingAdapter.submitList(snapshot.vehicleCostRanking);
            driverCostRankingAdapter.submitList(snapshot.driverCostRanking);
            vehicleSafetyRankingAdapter.submitList(snapshot.vehicleSafetyRanking);
            driverSafetyRankingAdapter.submitList(snapshot.driverSafetyRanking);
            alertAdapter.submitList(snapshot.priorityAlerts);
            binding.textDieselRankingEmpty.setVisibility(snapshot.vehicleCostRanking.isEmpty() ? View.VISIBLE : View.GONE);
            binding.textArlaRankingEmpty.setVisibility(snapshot.driverCostRanking.isEmpty() ? View.VISIBLE : View.GONE);
            binding.textVehicleSafetyRankingEmpty.setVisibility(snapshot.vehicleSafetyRanking.isEmpty() ? View.VISIBLE : View.GONE);
            binding.textDriverSafetyRankingEmpty.setVisibility(snapshot.driverSafetyRanking.isEmpty() ? View.VISIBLE : View.GONE);
            binding.textIndicatorAlertsEmpty.setVisibility(snapshot.priorityAlerts.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void openVehicle(String plate) {
        android.content.Intent intent = new android.content.Intent(requireContext(), VehicleDetailActivity.class);
        intent.putExtra(VehicleDetailActivity.EXTRA_PLATE, plate);
        startActivity(intent);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
