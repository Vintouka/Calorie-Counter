package com.example.caloriecounter.ui.Overview;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.Objects;

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

        viewModel = new ViewModelProvider(this).get(OverviewViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.rvEntries);
        EntriesAdapter adapter = new EntriesAdapter(
                position -> showEntryDialog(Objects.requireNonNull(viewModel.getEntries().getValue()).get(position), position),
                this::confirmDelete
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Observe entries
        viewModel.getEntries().observe(getViewLifecycleOwner(), adapter::submitList);

        // Observe total calories
        TextView tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        viewModel.getTotalCalories().observe(getViewLifecycleOwner(), total ->
                tvTotalCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", total))
        );

        FloatingActionButton fab = view.findViewById(R.id.fabAddEntry);
        fab.setOnClickListener(v -> showEntryDialog(null, -1));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload goals when returning to this fragment
        loadCalorieGoals();
    }

    private void showEntryDialog(@Nullable CalorieEntry existingEntry, int position) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_entry, null);

        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        TextInputEditText etCaloriesPerUnit = dialogView.findViewById(R.id.etCaloriesPerUnit);
        TextView tvItemTotal = dialogView.findViewById(R.id.tvItemTotal);
        ImageButton btnInc = dialogView.findViewById(R.id.btnIncreaseQty);
        ImageButton btnDec = dialogView.findViewById(R.id.btnDecreaseQty);

        if (existingEntry != null) {
            etName.setText(existingEntry.getName());
            etQuantity.setText(String.format(Locale.getDefault(), "%.2f", existingEntry.getQuantity()));
            etCaloriesPerUnit.setText(String.format(Locale.getDefault(), "%.2f", existingEntry.getCaloriesPerUnit()));
        } else {
            etQuantity.setText("1"); // default quantity
        }

        // Update total live
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotal();
            }
            @Override public void afterTextChanged(Editable s) {
                updateTotal();
            }
            private void updateTotal() {
                double qty = parseDoubleOrZero(etQuantity.getText() != null ? etQuantity.getText().toString() : "");
                double per = parseDoubleOrZero(etCaloriesPerUnit.getText() != null ? etCaloriesPerUnit.getText().toString() : "");
                double total = qty * per;
                tvItemTotal.setText(String.format(Locale.getDefault(), getString(R.string.label_item_total_placeholder), total));
            }
        };
        etQuantity.addTextChangedListener(watcher);
        etCaloriesPerUnit.addTextChangedListener(watcher);

        // Quantity increment/decrement buttons
        btnInc.setOnClickListener(v -> {
            double qty = parseDoubleOrZero(Objects.requireNonNull(etQuantity.getText()).toString());
            etQuantity.setText(String.format(Locale.getDefault(), "%.2f", qty + 1));
        });

        btnDec.setOnClickListener(v -> {
            double qty = parseDoubleOrZero(Objects.requireNonNull(etQuantity.getText()).toString());
            if (qty > 1) {
                etQuantity.setText(String.format(Locale.getDefault(), "%.2f", qty - 1));
            }
        });

        // Build dialog
        boolean isEditing = existingEntry != null;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isEditing ? R.string.edit_entry_title : R.string.add_entry_title)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(isEditing ? R.string.save_changes : R.string.add_entry_confirm, (dialog, which) -> {
                    String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                    double qty = parseDoubleOrZero(etQuantity.getText() != null ? etQuantity.getText().toString() : "");
                    double per = parseDoubleOrZero(etCaloriesPerUnit.getText() != null ? etCaloriesPerUnit.getText().toString() : "");

                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), R.string.error_name_required, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (qty <= 0 || per <= 0) {
                        Toast.makeText(getContext(), R.string.error_invalid_numbers, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    CalorieEntry entry = new CalorieEntry(name, qty, per);
                    if (isEditing) {
                        viewModel.updateEntry(position, entry);
                    } else {
                        viewModel.addEntry(entry);
                    }
                })
                .show();
    }

    private void confirmDelete(int position) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_entry_title)
                .setMessage(R.string.delete_entry_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete_entry_confirm, (dialog, which) ->
                        viewModel.deleteEntry(position))
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