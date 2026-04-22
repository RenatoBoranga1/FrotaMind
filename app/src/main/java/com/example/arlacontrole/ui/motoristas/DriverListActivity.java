package com.example.arlacontrole.ui.motoristas;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.arlacontrole.databinding.ActivityDriverListBinding;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.utils.AppPreferences;

public class DriverListActivity extends AppCompatActivity {

    private ActivityDriverListBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!UserRole.canAccessManagement(new AppPreferences(this).getSession().role)) {
            finish();
            return;
        }

        binding = ActivityDriverListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        DriverListAdapter adapter = new DriverListAdapter();
        binding.recyclerDrivers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDrivers.setAdapter(adapter);

        DriverListViewModel viewModel = new ViewModelProvider(this).get(DriverListViewModel.class);
        viewModel.getDrivers().observe(this, drivers -> {
            adapter.submitList(drivers);
            binding.textDriversEmpty.setVisibility(drivers == null || drivers.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
