package com.example.arlacontrole.ui.motoristas;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arlacontrole.R;
import com.example.arlacontrole.data.local.DriverEntity;
import com.example.arlacontrole.databinding.ItemDriverListBinding;

import java.util.ArrayList;
import java.util.List;

public class DriverListAdapter extends RecyclerView.Adapter<DriverListAdapter.DriverViewHolder> {

    private final List<DriverEntity> items = new ArrayList<>();

    public void submitList(List<DriverEntity> drivers) {
        items.clear();
        if (drivers != null) {
            items.addAll(drivers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DriverViewHolder(ItemDriverListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DriverViewHolder extends RecyclerView.ViewHolder {

        private final ItemDriverListBinding binding;

        DriverViewHolder(ItemDriverListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DriverEntity driver) {
            binding.textDriverName.setText(driver.name);
            binding.textDriverStatus.setText(driver.active
                ? binding.getRoot().getContext().getString(R.string.driver_status_active)
                : binding.getRoot().getContext().getString(R.string.driver_status_inactive));
        }
    }
}
