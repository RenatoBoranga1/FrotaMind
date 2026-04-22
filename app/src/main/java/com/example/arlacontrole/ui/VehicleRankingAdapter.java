package com.example.arlacontrole.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arlacontrole.R;
import com.example.arlacontrole.databinding.ItemVehicleRankingBinding;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.model.VehicleRankingItem;
import com.example.arlacontrole.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class VehicleRankingAdapter extends RecyclerView.Adapter<VehicleRankingAdapter.RankingViewHolder> {

    public interface OnVehicleClickListener {
        void onVehicleClick(String plate);
    }

    private final List<VehicleRankingItem> items = new ArrayList<>();
    private final OnVehicleClickListener listener;

    public VehicleRankingAdapter(OnVehicleClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<VehicleRankingItem> ranking) {
        items.clear();
        if (ranking != null) {
            items.addAll(ranking);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVehicleRankingBinding binding = ItemVehicleRankingBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new RankingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        holder.bind(items.get(position), position + 1, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RankingViewHolder extends RecyclerView.ViewHolder {

        private final ItemVehicleRankingBinding binding;

        RankingViewHolder(ItemVehicleRankingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VehicleRankingItem item, int position, OnVehicleClickListener listener) {
            binding.textRank.setText(String.valueOf(position));
            binding.textVehicle.setText(item.plate + " | " + item.fleetCode + " | " + item.model);
            binding.textAverage.setText(
                binding.getRoot().getContext().getString(
                    R.string.metric_average_label,
                    FormatUtils.formatFuelMetric(
                        binding.getRoot().getContext(),
                        item.fuelType,
                        item.averageConsumptionValue <= 0d ? null : item.averageConsumptionValue
                    )
                )
            );
            binding.textTotalLiters.setText(FormatUtils.formatLiters(item.totalLiters));
            binding.chipRankingStatus.setText(FormatUtils.formatStatus(binding.getRoot().getContext(), item.topStatus));
            binding.chipRankingStatus.setChipBackgroundColorResource(resolveStatusColor(item.topStatus));
            binding.getRoot().setOnClickListener(view -> listener.onVehicleClick(item.plate));
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
    }
}
