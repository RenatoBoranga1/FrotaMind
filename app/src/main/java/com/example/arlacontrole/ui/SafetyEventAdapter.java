package com.example.arlacontrole.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arlacontrole.R;
import com.example.arlacontrole.data.local.SafetyEventEntity;
import com.example.arlacontrole.databinding.ItemSafetyEventBinding;
import com.example.arlacontrole.model.SafetyAnalysisStatus;
import com.example.arlacontrole.model.SafetySeverity;
import com.example.arlacontrole.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class SafetyEventAdapter extends RecyclerView.Adapter<SafetyEventAdapter.SafetyEventViewHolder> {

    public interface OnSafetyEventClickListener {
        void onSafetyEventClick(SafetyEventEntity entity);
    }

    private final List<SafetyEventEntity> items = new ArrayList<>();
    private final OnSafetyEventClickListener listener;

    public SafetyEventAdapter(OnSafetyEventClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SafetyEventEntity> events) {
        items.clear();
        if (events != null) {
            items.addAll(events);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SafetyEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSafetyEventBinding binding = ItemSafetyEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SafetyEventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SafetyEventViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SafetyEventViewHolder extends RecyclerView.ViewHolder {

        private final ItemSafetyEventBinding binding;

        SafetyEventViewHolder(ItemSafetyEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SafetyEventEntity entity, OnSafetyEventClickListener listener) {
            binding.textSafetyTitle.setText(entity.vehiclePlate + " | " + entity.vehicleFleetCode);
            String subtitle = entity.driverName + " | " + entity.locationName;
            if (entity.occurrenceCount > 1) {
                subtitle = subtitle + " | " + binding.getRoot().getContext().getString(R.string.safety_occurrence_count_label, entity.occurrenceCount);
            }
            binding.textSafetySubtitle.setText(subtitle);
            binding.textSafetyDescription.setText(entity.description);
            binding.textSafetyDate.setText(FormatUtils.formatDateTime(entity.occurredAtIso));
            binding.chipSafetyType.setText(FormatUtils.formatSafetyEventType(binding.getRoot().getContext(), entity.eventType));
            binding.chipSafetyStatus.setText(FormatUtils.formatSafetyAnalysisStatus(binding.getRoot().getContext(), entity.analysisStatus));
            binding.chipSafetySeverity.setText(FormatUtils.formatSafetySeverity(binding.getRoot().getContext(), entity.severity));
            binding.chipSafetySeverity.setChipBackgroundColorResource(resolveSeverityColor(entity.severity));
            binding.chipSafetyStatus.setChipBackgroundColorResource(resolveStatusColor(entity.analysisStatus));
            binding.chipSafetyEvidence.setVisibility(entity.hasEvidence() ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.getRoot().setOnClickListener(view -> listener.onSafetyEventClick(entity));
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

        private int resolveStatusColor(String status) {
            if (SafetyAnalysisStatus.RESOLVED.equals(status)) {
                return R.color.success_container;
            }
            if (SafetyAnalysisStatus.IN_REVIEW.equals(status)) {
                return R.color.info_container;
            }
            return R.color.warning_container;
        }
    }
}
