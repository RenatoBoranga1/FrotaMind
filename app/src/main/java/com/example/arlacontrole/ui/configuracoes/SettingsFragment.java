package com.example.arlacontrole.ui.configuracoes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.arlacontrole.R;
import com.example.arlacontrole.databinding.FragmentSettingsBinding;
import com.example.arlacontrole.utils.AppPreferences;
import com.example.arlacontrole.utils.FormatUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        binding.inputApiUrl.setText(viewModel.getApiUrl());
        binding.textVersionInfo.setText(getString(R.string.version_value, resolveVersionName()));
        refreshSyncInfo();

        viewModel.getPendingSyncCount().observe(getViewLifecycleOwner(), count -> {
            binding.textPendingSyncSettings.setText(getString(R.string.pending_sync) + ": " + (count == null ? 0 : count));
        });

        binding.buttonTestConnection.setOnClickListener(v -> {
            String baseUrl = binding.inputApiUrl.getText() == null ? "" : binding.inputApiUrl.getText().toString();
            if (!AppPreferences.isValidBaseUrl(baseUrl)) {
                binding.inputApiUrl.setError(getString(R.string.invalid_url));
                return;
            }
            binding.inputApiUrl.setError(null);
            viewModel.saveApiUrl(baseUrl);
            binding.progressSettings.setVisibility(View.VISIBLE);
            viewModel.testConnection(new com.example.arlacontrole.data.repository.RepositoryCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    binding.progressSettings.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), R.string.connection_ok, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    binding.progressSettings.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), R.string.connection_error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.buttonSyncNow.setOnClickListener(v -> {
            String baseUrl = binding.inputApiUrl.getText() == null ? "" : binding.inputApiUrl.getText().toString();
            if (!AppPreferences.isValidBaseUrl(baseUrl)) {
                binding.inputApiUrl.setError(getString(R.string.invalid_url));
                return;
            }
            binding.inputApiUrl.setError(null);
            viewModel.saveApiUrl(baseUrl);
            viewModel.syncNow();
            Toast.makeText(requireContext(), R.string.sync_in_progress, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            binding.textVersionInfo.setText(getString(R.string.version_value, resolveVersionName()));
        }
        refreshSyncInfo();
    }

    private void refreshSyncInfo() {
        if (binding == null || viewModel == null) {
            return;
        }
        long lastSyncAt = viewModel.getLastSyncAt();
        if (lastSyncAt > 0L) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSyncAt), ZoneId.systemDefault());
            binding.textSyncLast.setText(getString(R.string.sync_last) + ": " + FormatUtils.formatDateTime(dateTime.toString()));
        } else {
            binding.textSyncLast.setText(getString(R.string.sync_last) + ": --");
        }
        String message = viewModel.getLastSyncMessage();
        binding.textSyncMessage.setText(message == null || message.isEmpty() ? getString(R.string.sync_manual_description) : message);
    }

    private String resolveVersionName() {
        try {
            return requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName;
        } catch (Exception ignored) {
            return "2.0.0";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
