package com.example.arlacontrole.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arlacontrole.R;
import com.example.arlacontrole.databinding.ItemFinancialRankingBinding;
import com.example.arlacontrole.model.FinancialRankingItem;
import com.example.arlacontrole.model.RefuelStatus;
import com.example.arlacontrole.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class FinancialRankingAdapter extends RecyclerView.Adapter<FinancialRankingAdapter.FinancialRankingViewHolder> {

    public interface OnFinancialRankingClickListener {
        void onFinancialRankingClick(String key);
    }

    private final List<FinancialRankingItem> items = new ArrayList<>();
    private final OnFinancialRankingClickListener listener;

    public FinancialRankingAdapter(OnFinancialRankingClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FinancialRankingItem> ranking) {
        items.clear();
        if (ranking != null) {
            items.addAll(ranking);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FinancialRankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFinancialRankingBinding binding = ItemFinancialRankingBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new FinancialRankingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FinancialRankingViewHolder holder, int position) {
        holder.bind(items.get(position), position + 1, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FinancialRankingViewHolder extends RecyclerView.ViewHolder {

        private final ItemFinancialRankingBinding binding;

        FinancialRankingViewHolder(ItemFinancialRankingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FinancialRankingItem item, int position, OnFinancialRankingClickListener listener) {
            binding.textFinancialRank.setText(String.valueOf(position));
            binding.textFinancialTitle.setText(item.title);
            binding.textFinancialSubtitle.setText(item.subtitle);
            binding.textFinancialTotal.setText(FormatUtils.formatCurrency(item.totalCost));
            binding.textFinancialMetrics.setText(
                binding.getRoot().getContext().getString(
                    R.string.financial_ranking_metrics,
                    item.averagePricePerLiter == null ? binding.getRoot().getContext().getString(R.string.metric_not_available) : FormatUtils.formatCurrency(item.averagePricePerLiter),
                    item.costPerKm == null ? binding.getRoot().getContext().getString(R.string.metric_not_available) : FormatUtils.formatCurrency(item.costPerKm)
                )
            );
            binding.chipFinancialStatus.setText(FormatUtils.formatStatus(binding.getRoot().getContext(), item.topStatus));
            binding.chipFinancialStatus.setChipBackgroundColorResource(resolveStatusColor(item.topStatus));
            binding.getRoot().setOnClickListener(view -> listener.onFinancialRankingClick(item.key));
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
