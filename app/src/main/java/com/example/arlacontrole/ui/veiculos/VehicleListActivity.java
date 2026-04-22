package com.example.arlacontrole.ui.veiculos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.arlacontrole.databinding.ActivityVehicleListBinding;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.utils.AppPreferences;

public class VehicleListActivity extends AppCompatActivity {

    private ActivityVehicleListBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!UserRole.canAccessManagement(new AppPreferences(this).getSession().role)) {
            finish();
            return;
        }

        binding = ActivityVehicleListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        VehicleListAdapter adapter = new VehicleListAdapter(vehicle -> {
            Intent intent = new Intent(this, VehicleDetailActivity.class);
            intent.putExtra(VehicleDetailActivity.EXTRA_PLATE, vehicle.plate);
            startActivity(intent);
        });
        binding.recyclerVehicles.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerVehicles.setAdapter(adapter);

        VehicleListViewModel viewModel = new ViewModelProvider(this).get(VehicleListViewModel.class);
        viewModel.getVehicles().observe(this, vehicles -> {
            adapter.submitList(vehicles);
            binding.textVehiclesEmpty.setVisibility(vehicles == null || vehicles.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
