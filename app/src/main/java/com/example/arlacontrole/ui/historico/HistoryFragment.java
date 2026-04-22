package com.example.arlacontrole.ui.historico;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.arlacontrole.R;
import com.example.arlacontrole.databinding.FragmentHistoryBinding;
import com.example.arlacontrole.model.FuelType;
import com.example.arlacontrole.model.UserRole;
import com.example.arlacontrole.ui.RefuelAdapter;
import com.example.arlacontrole.ui.export.ReportExportBottomSheet;
import com.example.arlacontrole.utils.AppPreferences;
import com.example.arlacontrole.utils.FormatUtils;
import com.example.arlacontrole.utils.SimpleTextWatcher;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private RefuelAdapter adapter;
    private LocalDate startDate;
    private LocalDate endDate;
    private String selectedFuelType = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        adapter = new RefuelAdapter(entity -> {
            Intent intent = new Intent(requireContext(), RefuelDetailActivity.class);
            intent.putExtra(RefuelDetailActivity.EXTRA_REFUEL_ID, entity.localId);
            startActivity(intent);
        });

        binding.recyclerHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerHistory.setAdapter(adapter);
        binding.toggleFilterType.check(R.id.buttonFilterAll);

        binding.inputFilterPlate.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(android.text.Editable editable) {
                applyFilters();
            }
        });
        binding.inputFilterDriver.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(android.text.Editable editable) {
                applyFilters();
            }
        });
        binding.toggleFilterType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.buttonFilterArla) {
                selectedFuelType = FuelType.ARLA;
            } else if (checkedId == R.id.buttonFilterDiesel) {
                selectedFuelType = FuelType.DIESEL;
            } else {
                selectedFuelType = "";
            }
            applyFilters();
        });

        binding.buttonStartDate.setOnClickListener(v -> openDatePicker(true));
        binding.buttonEndDate.setOnClickListener(v -> openDatePicker(false));
        binding.buttonClearFilters.setOnClickListener(v -> clearFilters());
        boolean canExport = UserRole.canExportReports(new AppPreferences(requireContext()).getSession().role);
        binding.buttonExportHistory.setVisibility(canExport ? View.VISIBLE : View.GONE);
        binding.buttonExportHistory.setOnClickListener(v -> ReportExportBottomSheet.newInstance(
            selectedFuelType,
            binding.inputFilterPlate.getText() == null ? "" : binding.inputFilterPlate.getText().toString(),
            binding.inputFilterDriver.getText() == null ? "" : binding.inputFilterDriver.getText().toString(),
            startDate,
            endDate
        ).show(getParentFragmentManager(), "report_export_history"));
        binding.swipeHistory.setOnRefreshListener(() -> {
            viewModel.syncNow();
            binding.swipeHistory.setRefreshing(false);
            Snackbar.make(binding.getRoot(), R.string.sync_in_progress, Snackbar.LENGTH_SHORT).show();
        });

        viewModel.getFilteredRefuels().observe(getViewLifecycleOwner(), refuels -> {
            adapter.submitList(refuels);
            binding.textHistoryEmpty.setVisibility(refuels == null || refuels.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void openDatePicker(boolean isStart) {
        LocalDate initial = isStart ? (startDate == null ? LocalDate.now() : startDate) : (endDate == null ? LocalDate.now() : endDate);
        DatePickerDialog dialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                if (isStart) {
                    startDate = selectedDate;
                    binding.buttonStartDate.setText(
                        getString(R.string.history_start_date_selected, FormatUtils.formatDate(selectedDate.atStartOfDay().toString()))
                    );
                } else {
                    endDate = selectedDate;
                    binding.buttonEndDate.setText(
                        getString(R.string.history_end_date_selected, FormatUtils.formatDate(selectedDate.atStartOfDay().toString()))
                    );
                }
                applyFilters();
            },
            initial.getYear(),
            initial.getMonthValue() - 1,
            initial.getDayOfMonth()
        );
        dialog.show();
    }

    private void applyFilters() {
        String plate = binding.inputFilterPlate.getText() == null ? "" : binding.inputFilterPlate.getText().toString();
        String driver = binding.inputFilterDriver.getText() == null ? "" : binding.inputFilterDriver.getText().toString();
        viewModel.setFilters(selectedFuelType, plate, driver, startDate, endDate);
    }

    private void clearFilters() {
        selectedFuelType = "";
        startDate = null;
        endDate = null;
        binding.inputFilterPlate.setText(null);
        binding.inputFilterDriver.setText(null);
        binding.toggleFilterType.check(R.id.buttonFilterAll);
        binding.buttonStartDate.setText(R.string.history_start_date);
        binding.buttonEndDate.setText(R.string.history_end_date);
        viewModel.clearFilters();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
