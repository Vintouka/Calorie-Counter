package com.example.caloriecounter.ui.Overview;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecounter.R;
import com.example.caloriecounter.data.models.CalorieEntry;
import com.example.caloriecounter.databinding.FragmentOverviewBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class OverviewFragment extends Fragment {
    private FragmentOverviewBinding binding;
    private OverviewViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOverviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(OverviewViewModel.class);

        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.rvEntries);
        EntriesAdapter adapter = new EntriesAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Observe entries
        viewModel.getEntries().observe(getViewLifecycleOwner(), adapter::submitList);

        // Observe total calories
        TextView tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        viewModel.getTotalCalories().observe(getViewLifecycleOwner(), total ->
                tvTotalCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", total))
        );

        // Setup FAB
        FloatingActionButton fab = view.findViewById(R.id.fabAddEntry);
        fab.setOnClickListener(v -> showAddEntryDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload goals when returning to this fragment
        loadCalorieGoals();
    }

    private void showAddEntryDialog() {
        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_entry, null);

        // Get references to views
        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        TextInputEditText etCaloriesPerUnit = dialogView.findViewById(R.id.etCaloriesPerUnit);
        TextView tvItemTotal = dialogView.findViewById(R.id.tvItemTotal);

        // TextWatcher for real-time total calorie preview
        TextWatcher previewWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateItemTotal();
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateItemTotal();
            }

            private void updateItemTotal() {
                double quantity = parseDoubleOrZero(etQuantity.getText() != null ? etQuantity.getText().toString() : "");
                double caloriesPerUnit = parseDoubleOrZero(etCaloriesPerUnit.getText() != null ? etCaloriesPerUnit.getText().toString() : "");
                double total = quantity * caloriesPerUnit;
                tvItemTotal.setText(getString(
                        R.string.label_item_total_placeholder,
                        String.format(Locale.getDefault(), "%.2f", total)
                ));
            }
        };

        etQuantity.addTextChangedListener(previewWatcher);
        etCaloriesPerUnit.addTextChangedListener(previewWatcher);

        // Build the Material dialog
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_entry_title)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.add_entry_confirm, (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    double quantity = parseDoubleOrZero(etQuantity.getText() != null ? etQuantity.getText().toString() : "");
                    double caloriesPerUnit = parseDoubleOrZero(etCaloriesPerUnit.getText() != null ? etCaloriesPerUnit.getText().toString() : "");

                    // Basic validation
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), R.string.error_name_required, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (quantity <= 0 || caloriesPerUnit <= 0) {
                        Toast.makeText(getContext(), R.string.error_invalid_numbers, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create and add the new entry
                    CalorieEntry newEntry = new CalorieEntry(name, quantity, caloriesPerUnit);
                    viewModel.addEntry(newEntry);
                })
                .show();
    }

    private double parseDoubleOrZero(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    private void loadCalorieGoals() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("CalorieGoals", Context.MODE_PRIVATE);
        int minGoal = prefs.getInt("min_goal", 0);
        int maxGoal = prefs.getInt("max_goal", 0);

        if (minGoal > 0 && maxGoal > 0) {
            binding.tvCalorieGoal.setText(String.format(getString(R.string.daily_calorie_goal_display), minGoal, maxGoal));
            binding.tvCalorieGoal.setVisibility(View.VISIBLE);
        } else {
            binding.tvCalorieGoal.setText(R.string.no_goal_set);
            binding.tvCalorieGoal.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}