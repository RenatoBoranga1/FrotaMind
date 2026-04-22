package com.example.arlacontrole.ui.management;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.arlacontrole.databinding.FragmentManagementHubBinding;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.ui.alertas.AlertListActivity;
import com.example.arlacontrole.ui.configuracoes.SettingsActivity;
import com.example.arlacontrole.ui.export.ReportExportBottomSheet;
import com.example.arlacontrole.ui.motoristas.DriverListActivity;
import com.example.arlacontrole.ui.veiculos.VehicleListActivity;
import com.example.arlacontrole.utils.AppPreferences;

public class ManagementHubFragment extends Fragment {

    private FragmentManagementHubBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentManagementHubBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppPreferences preferences = new AppPreferences(requireContext());
        String role = preferences.getSession().role;

        binding.buttonOpenVehicles.setOnClickListener(v -> startActivity(new Intent(requireContext(), VehicleListActivity.class)));
        binding.buttonOpenDrivers.setOnClickListener(v -> startActivity(new Intent(requireContext(), DriverListActivity.class)));
        binding.buttonOpenAlerts.setOnClickListener(v -> startActivity(new Intent(requireContext(), AlertListActivity.class)));
        binding.buttonOpenManagementHelp.setOnClickListener(v -> startActivity(new Intent(requireContext(), ManagementHelpActivity.class)));
        binding.buttonOpenSettings.setOnClickListener(v -> startActivity(new Intent(requireContext(), SettingsActivity.class)));
        binding.buttonOpenExport.setOnClickListener(v -> ReportExportBottomSheet.newInstance(null, null, null, null, null)
            .show(getParentFragmentManager(), "report_export_manage"));
        binding.cardSettings.setVisibility(UserRole.canAccessSettings(role) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
