package com.example.arlacontrole.ui.sync;

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
import com.example.arlacontrole.databinding.FragmentSyncStatusBinding;
import com.example.arlacontrole.model.AuthSession;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.utils.FormatUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SyncStatusFragment extends Fragment {

    private FragmentSyncStatusBinding binding;
    private SyncStatusViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSyncStatusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SyncStatusViewModel.class);

        AuthSession session = viewModel.getSession();
        binding.textSyncUser.setText(getString(R.string.current_user_label, session.fullName));
        binding.textSyncRole.setText(getString(R.string.current_profile_label, formatRole(session.role)));

        viewModel.getPendingSyncCount().observe(getViewLifecycleOwner(), count -> {
            binding.textSyncPending.setText(getString(R.string.pending_sync) + ": " + (count == null ? 0 : count));
        });

        binding.buttonSyncStatusNow.setOnClickListener(v -> {
            binding.progressSyncStatus.setVisibility(View.VISIBLE);
            viewModel.syncNow();
            Toast.makeText(requireContext(), R.string.sync_in_progress, Toast.LENGTH_SHORT).show();
            binding.getRoot().postDelayed(() -> {
                if (binding != null) {
                    binding.progressSyncStatus.setVisibility(View.GONE);
                    refreshTexts();
                }
            }, 1200L);
        });

        refreshTexts();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshTexts();
    }

    private void refreshTexts() {
        if (binding == null || viewModel == null) {
            return;
        }
        long lastSyncAt = viewModel.getLastSyncAt();
        if (lastSyncAt > 0L) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSyncAt), ZoneId.systemDefault());
            binding.textSyncLastStatus.setText(getString(R.string.sync_last) + ": " + FormatUtils.formatDateTime(dateTime.toString()));
        } else {
            binding.textSyncLastStatus.setText(getString(R.string.sync_last) + ": --");
        }
        binding.textSyncMessageStatus.setText(viewModel.getLastSyncMessage());
    }

    private String formatRole(String role) {
        if (UserRole.ADMIN.equals(role)) {
            return getString(R.string.role_admin);
        }
        if (UserRole.OPERACIONAL.equals(role)) {
            return getString(R.string.role_operational);
        }
        return getString(R.string.role_driver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
