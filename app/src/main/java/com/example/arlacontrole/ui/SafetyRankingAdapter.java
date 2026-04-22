package com.example.arlacontrole.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arlacontrole.R;
import com.example.arlacontrole.databinding.ItemSafetyRankingBinding;
import com.example.arlacontrole.model.SafetyRankingItem;
import com.example.arlacontrole.model.SafetySeverity;
import com.example.arlacontrole.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class SafetyRankingAdapter extends RecyclerView.Adapter<SafetyRankingAdapter.SafetyRankingViewHolder> {

    public interface OnSafetyRankingClickListener {
        void onSafetyRankingClick(String key);
    }

    private final List<SafetyRankingItem> items = new ArrayList<>();
    private final OnSafetyRankingClickListener listener;

    public SafetyRankingAdapter(OnSafetyRankingClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SafetyRankingItem> ranking) {
        items.clear();
        if (ranking != null) {
            items.addAll(ranking);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SafetyRankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSafetyRankingBinding binding = ItemSafetyRankingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SafetyRankingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SafetyRankingViewHolder holder, int position) {
        holder.bind(items.get(position), position + 1, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SafetyRankingViewHolder extends RecyclerView.ViewHolder {

        private final ItemSafetyRankingBinding binding;

        SafetyRankingViewHolder(ItemSafetyRankingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SafetyRankingItem item, int position, OnSafetyRankingClickListener listener) {
            binding.textSafetyRank.setText(String.valueOf(position));
            binding.textSafetyRankingTitle.setText(item.title);
            binding.textSafetyRankingSubtitle.setText(item.subtitle);
            binding.textSafetyRankingMetrics.setText(
                binding.getRoot().getContext().getString(
                    R.string.security_ranking_metrics,
                    item.eventCount,
                    item.riskScore
                )
            );
            binding.chipSafetyRankingSeverity.setText(FormatUtils.formatSafetySeverity(binding.getRoot().getContext(), item.topSeverity));
            binding.chipSafetyRankingSeverity.setChipBackgroundColorResource(resolveSeverityColor(item.topSeverity));
            binding.chipSafetyRankingUnresolved.setText(
                binding.getRoot().getContext().getString(R.string.security_unresolved_short, item.unresolvedCount)
            );
            binding.getRoot().setOnClickListener(view -> listener.onSafetyRankingClick(item.key));
        }

        private int resolveSeverityColor(String severity) {
            if (SafetySeverity.CRITICAL.equals(severity)) {
                return R.color.danger_container;
            }
            if (SafetySeverity.HIGH.equals(severity)) {
                return R.color.warning_container;
            }
            if (SafetySeverity.MODERATE.equals(severity)) {
                return R.color.info_container;
            }
            return R.color.success_container;
        }
    }
}
