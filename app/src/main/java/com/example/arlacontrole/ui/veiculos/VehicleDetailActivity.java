package com.example.arlacontrole.ui.veiculos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.arlacontrole.R;
import com.example.arlacontrole.databinding.ActivityVehicleDetailBinding;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.ui.RefuelAdapter;
import com.example.arlacontrole.ui.historico.RefuelDetailActivity;
import com.example.arlacontrole.utils.FormatUtils;

public class VehicleDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PLATE = "extra_plate";

    private ActivityVehicleDetailBinding binding;
    private VehicleDetailViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        String plate = getIntent().getStringExtra(EXTRA_PLATE);
        if (plate == null || plate.trim().isEmpty()) {
            finish();
            return;
        }

        RefuelAdapter adapter = new RefuelAdapter(entity -> {
            Intent intent = new Intent(this, RefuelDetailActivity.class);
            intent.putExtra(RefuelDetailActivity.EXTRA_REFUEL_ID, entity.localId);
            startActivity(intent);
        });
        binding.recyclerVehicleHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerVehicleHistory.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(VehicleDetailViewModel.class);
        viewModel.setVehiclePlate(plate);
        viewModel.getSnapshot().observe(this, snapshot -> {
            if (snapshot == null) {
                return;
            }
            binding.textVehiclePlate.setText(snapshot.plate + " | " + snapshot.fleetCode);
            binding.textVehicleModel.setText(snapshot.model);
            binding.textVehicleOperation.setText(snapshot.operation);
            binding.textExpectedAverage.setText(
                getString(R.string.expected_arla_range, snapshot.expectedArlaMin, snapshot.expectedArlaMax)
                    + "\n"
                    + getString(R.string.expected_diesel_range, snapshot.expectedDieselKmPerLiterMin, snapshot.expectedDieselKmPerLiterMax)
            );
            binding.textAverageConsumption.setText(
                getString(
                    R.string.average_arla_vehicle,
                    FormatUtils.formatFuelMetric(this, FuelType.ARLA, snapshot.averageArlaConsumption <= 0d ? null : snapshot.averageArlaConsumption)
                ) + "\n" +
                getString(
                    R.string.average_diesel_vehicle,
                    FormatUtils.formatFuelMetric(this, FuelType.DIESEL, snapshot.averageDieselConsumption <= 0d ? null : snapshot.averageDieselConsumption)
                )
            );
            binding.textTotalArla.setText(getString(R.string.total_arla, FormatUtils.formatLiters(snapshot.totalArla)));
            binding.textTotalDiesel.setText(getString(R.string.total_diesel, FormatUtils.formatLiters(snapshot.totalDiesel)));
            binding.textAlertCount.setText(
                getString(R.string.vehicle_alert_count, snapshot.alertCount)
                    + " | "
                    + getString(R.string.vehicle_records_summary, snapshot.totalRecords)
            );
            binding.textCurrentStatus.setText(getString(R.string.current_status) + ": " + FormatUtils.formatStatus(this, snapshot.currentStatus));
        });
        viewModel.getHistory().observe(this, history -> {
            adapter.submitList(history);
            binding.textVehicleHistoryEmpty.setVisibility(history == null || history.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
