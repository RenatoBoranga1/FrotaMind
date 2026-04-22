package com.example.arlacontrole.ui.veiculos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arlacontrole.data.local.VehicleEntity;
import com.example.arlacontrole.databinding.ItemVehicleListBinding;

import java.util.ArrayList;
import java.util.List;

public class VehicleListAdapter extends RecyclerView.Adapter<VehicleListAdapter.VehicleViewHolder> {

    public interface OnVehicleClickListener {
        void onVehicleClick(VehicleEntity vehicle);
    }

    private final List<VehicleEntity> items = new ArrayList<>();
    private final OnVehicleClickListener listener;

    public VehicleListAdapter(OnVehicleClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<VehicleEntity> vehicles) {
        items.clear();
        if (vehicles != null) {
            items.addAll(vehicles);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VehicleViewHolder(ItemVehicleListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {

        private final ItemVehicleListBinding binding;

        VehicleViewHolder(ItemVehicleListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VehicleEntity vehicle, OnVehicleClickListener listener) {
            binding.textVehiclePrimary.setText(vehicle.plate + " | " + vehicle.fleetCode);
            binding.textVehicleSecondary.setText(vehicle.model);
            binding.textVehicleOperationInfo.setText(vehicle.operation);
            binding.getRoot().setOnClickListener(v -> listener.onVehicleClick(vehicle));
        }
    }
}
