package com.example.arlacontrole.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arlacontrole.R;
import com.example.arlacontrole.data.local.RefuelEntity;
import com.example.arlacontrole.databinding.ItemRefuelBinding;
import com.example.arlacontrole.evidence.RefuelEvidenceManager;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.SyncState;
import com.example.arlacontrole.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class RefuelAdapter extends RecyclerView.Adapter<RefuelAdapter.RefuelViewHolder> {

    public interface OnRefuelClickListener {
        void onRefuelClick(RefuelEntity entity);
    }

    private final List<RefuelEntity> items = new ArrayList<>();
    private final OnRefuelClickListener listener;

    public RefuelAdapter(OnRefuelClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<RefuelEntity> entities) {
        items.clear();
        if (entities != null) {
            items.addAll(entities);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RefuelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRefuelBinding binding = ItemRefuelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RefuelViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RefuelViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RefuelViewHolder extends RecyclerView.ViewHolder {

        private final ItemRefuelBinding binding;

        RefuelViewHolder(ItemRefuelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RefuelEntity entity, OnRefuelClickListener listener) {
            Context context = binding.getRoot().getContext();
            binding.textPlate.setText(entity.vehiclePlate);
            binding.textModel.setText(entity.vehicleModel);
            binding.textFleet.setText(context.getString(R.string.label_fleet_code, entity.vehicleFleetCode));
            binding.textDriver.setText(entity.driverName);
            binding.textLocation.setText(entity.locationName);
            binding.textLiters.setText(FormatUtils.formatLiters(entity.liters));
            binding.textDate.setText(FormatUtils.formatDateTime(entity.suppliedAtIso));
            binding.chipFuelType.setText(FormatUtils.formatFuelType(context, entity.fuelType));
            binding.chipStatus.setText(FormatUtils.formatStatus(context, entity.statusLevel));
            binding.chipSync.setText(FormatUtils.formatSyncStatus(context, entity.syncStatus));
            binding.chipFuelType.setChipBackgroundColorResource(resolveFuelColor(entity.fuelType));
            binding.chipStatus.setChipBackgroundColorResource(resolveStatusColor(entity.statusLevel));
            binding.chipSync.setChipBackgroundColorResource(resolveSyncColor(entity.syncStatus));
            binding.chipEvidence.setVisibility(RefuelEvidenceManager.hasEvidence(entity.evidencePhotoPath) ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.chipChecklist.setVisibility(entity.hasChecklist() ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.getRoot().setOnClickListener(view -> listener.onRefuelClick(entity));
        }

        private int resolveFuelColor(String fuelType) {
            if (FuelType.DIESEL.equals(fuelType)) {
                return R.color.warning_container;
            }
            return R.color.info_container;
        }

        private int resolveStatusColor(String status) {
            if (RefuelStatus.ALERT.equals(status)) {
                return R.color.danger_container;
            }
            if (RefuelStatus.ATTENTION.equals(status)) {
                return R.color.warning_container;
            }
            return R.color.success_container;
        }

        private int resolveSyncColor(String syncState) {
            if (SyncState.SYNCED.equals(syncState)) {
                return R.color.success_container;
            }
            if (SyncState.FAILED.equals(syncState)) {
                return R.color.danger_container;
            }
            return R.color.info_container;
        }
    }
}
