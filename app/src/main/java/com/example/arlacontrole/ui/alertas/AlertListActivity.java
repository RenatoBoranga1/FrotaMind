package com.example.arlacontrole.ui.alertas;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.arlacontrole.databinding.ActivityAlertListBinding;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.ui.PriorityAlertAdapter;
import com.example.arlacontrole.utils.AppPreferences;

public class AlertListActivity extends AppCompatActivity {

    private ActivityAlertListBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!UserRole.canAccessManagement(new AppPreferences(this).getSession().role)) {
            finish();
            return;
        }

        binding = ActivityAlertListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        PriorityAlertAdapter adapter = new PriorityAlertAdapter();
        binding.recyclerAlerts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerAlerts.setAdapter(adapter);

        AlertListViewModel viewModel = new ViewModelProvider(this).get(AlertListViewModel.class);
        viewModel.getAlerts().observe(this, alerts -> {
            adapter.submitList(alerts);
            binding.textAlertsListEmpty.setVisibility(alerts == null || alerts.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
