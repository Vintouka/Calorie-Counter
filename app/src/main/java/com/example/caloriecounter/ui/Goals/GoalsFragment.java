package com.example.caloriecounter.ui.Goals;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.caloriecounter.databinding.FragmentGoalsBinding;

import java.util.ArrayList;
import java.util.List;

public class GoalsFragment extends Fragment {
    private FragmentGoalsBinding binding;
    private List<CaloriePreset> presets;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePresets();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        GoalsViewModel dashboardViewModel =
                new ViewModelProvider(this).get(GoalsViewModel.class);

        binding = FragmentGoalsBinding.inflate(inflater, container, false);

        // Access the CalendarView via binding
        CalendarView calendarView = binding.calendarView;

        // Handle date selection
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                Toast.makeText(getContext(), "Selected Date: " + date, Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load saved goals
        loadSavedGoals();

        // Set up Presets button
        binding.btnPresets.setOnClickListener(v -> showPresetsDialog());

        // Set up Set Goal button
        binding.btnSetGoal.setOnClickListener(v -> saveGoals());
    }

    private void initializePresets() {
        presets = new ArrayList<>();
        presets.add(new CaloriePreset("Sedentary Adult Male", 2000, 2500));
        presets.add(new CaloriePreset("Sedentary Adult Female", 1600, 2000));
        presets.add(new CaloriePreset("Active Adult Male", 2400, 3000));
        presets.add(new CaloriePreset("Active Adult Female", 2000, 2400));
        presets.add(new CaloriePreset("Teen Male (14-18)", 2400, 2800));
        presets.add(new CaloriePreset("Teen Female (14-18)", 1800, 2400));
        presets.add(new CaloriePreset("Child (9-13)", 1600, 2200));
        presets.add(new CaloriePreset("Very Active Male", 2800, 3500));
        presets.add(new CaloriePreset("Very Active Female", 2200, 2800));
        presets.add(new CaloriePreset("Weight Loss Male", 1500, 1800));
        presets.add(new CaloriePreset("Weight Loss Female", 1200, 1500));
        presets.add(new CaloriePreset("Weight Gain Male", 2500, 3200));
        presets.add(new CaloriePreset("Weight Gain Female", 2000, 2600));
    }

    private void loadSavedGoals() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("CalorieGoals", Context.MODE_PRIVATE);
        int minGoal = prefs.getInt("min_goal", 0);
        int maxGoal = prefs.getInt("max_goal", 0);

        if (minGoal > 0) {
            binding.etMinCalories.setText(String.valueOf(minGoal));
        }
        if (maxGoal > 0) {
            binding.etMaxCalories.setText(String.valueOf(maxGoal));
        }
    }

    private void showPresetsDialog() {
        String[] presetNames = new String[presets.size()];
        for (int i = 0; i < presets.size(); i++) {
            presetNames[i] = presets.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Calorie Goal Preset")
                .setItems(presetNames, (dialog, which) -> {
                    CaloriePreset selectedPreset = presets.get(which);
                    binding.etMinCalories.setText(String.valueOf(selectedPreset.getMinCalories()));
                    binding.etMaxCalories.setText(String.valueOf(selectedPreset.getMaxCalories()));
                    Toast.makeText(
                            requireContext(),
                            "Applied: " + selectedPreset.getName(),
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveGoals() {
        String minText = binding.etMinCalories.getText().toString().trim();
        String maxText = binding.etMaxCalories.getText().toString().trim();

        if (minText.isEmpty() || maxText.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Please enter both minimum and maximum calorie goals",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int minGoal;
        int maxGoal;

        try {
            minGoal = Integer.parseInt(minText);
            maxGoal = Integer.parseInt(maxText);
        } catch (NumberFormatException e) {
            Toast.makeText(
                    requireContext(),
                    "Please enter valid numbers",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (minGoal >= maxGoal) {
            Toast.makeText(
                    requireContext(),
                    "Minimum goal must be less than maximum goal",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("CalorieGoals", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("min_goal", minGoal);
        editor.putInt("max_goal", maxGoal);
        editor.apply();

        Toast.makeText(
                requireContext(),
                "Goals saved successfully!",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Inner class for CaloriePreset
    public static class CaloriePreset {
        private final String name;
        private final int minCalories;
        private final int maxCalories;

        public CaloriePreset(String name, int minCalories, int maxCalories) {
            this.name = name;
            this.minCalories = minCalories;
            this.maxCalories = maxCalories;
        }

        public String getName() {
            return name;
        }

        public int getMinCalories() {
            return minCalories;
        }

        public int getMaxCalories() {
            return maxCalories;
        }
    }
}
