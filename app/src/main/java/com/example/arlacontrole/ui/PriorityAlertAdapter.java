package com.example.arlacontrole.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arlacontrole.R;
import com.example.arlacontrole.databinding.ItemPriorityAlertBinding;
import com.example.arlacontrole.model.PriorityAlertItem;
import com.example.arlacontrole.model.RefuelStatus;

import java.util.ArrayList;
import java.util.List;

public class PriorityAlertAdapter extends RecyclerView.Adapter<PriorityAlertAdapter.PriorityAlertViewHolder> {

    private final List<PriorityAlertItem> items = new ArrayList<>();

    public void submitList(List<PriorityAlertItem> alerts) {
        items.clear();
        if (alerts != null) {
            items.addAll(alerts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PriorityAlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPriorityAlertBinding binding = ItemPriorityAlertBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PriorityAlertViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PriorityAlertViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PriorityAlertViewHolder extends RecyclerView.ViewHolder {

        private final ItemPriorityAlertBinding binding;

        PriorityAlertViewHolder(ItemPriorityAlertBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PriorityAlertItem item) {
            binding.chipAlertSource.setText(item.source == null || item.source.trim().isEmpty() ? "OPERACAO" : item.source);
            binding.chipAlertLevel.setText(item.level == null || item.level.trim().isEmpty() ? RefuelStatus.NORMAL : item.level);
            binding.chipAlertLevel.setChipBackgroundColorResource(resolveLevelColor(item.level));
            binding.textAlertTitle.setText(item.title);
            binding.textAlertDescription.setText(item.description);
        }

        private int resolveLevelColor(String level) {
            if (RefuelStatus.ALERT.equals(level)) {
                return R.color.danger_container;
            }
            if (RefuelStatus.ATTENTION.equals(level)) {
                return R.color.warning_container;
            }
            return R.color.success_container;
        }
    }
}
