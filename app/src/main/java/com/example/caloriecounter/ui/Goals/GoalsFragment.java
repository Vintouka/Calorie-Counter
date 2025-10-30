package com.example.caloriecounter.ui.Goals;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.data.database.AppDatabase;
import com.example.caloriecounter.data.database.CalorieEntryDao;
import com.example.caloriecounter.data.database.CalorieEntryEntity;
import com.example.caloriecounter.databinding.FragmentGoalsBinding;
import com.example.caloriecounter.utils.DateUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoalsFragment extends Fragment {
    private FragmentGoalsBinding binding;
    private List<CaloriePreset> presets;
    private CalorieEntryDao dao;
    private ExecutorService executorService;
    private Set<String> datesWithEntries = new HashSet<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePresets();
        AppDatabase database = AppDatabase.getInstance(requireContext());
        dao = database.calorieEntryDao();
        executorService = Executors.newSingleThreadExecutor();

        // Load all dates with entries
        dao.getAllDatesWithEntries().observe(this, dates -> {
            if (dates != null) {
                datesWithEntries.clear();
                datesWithEntries.addAll(dates);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGoalsBinding.inflate(inflater, container, false);

        CalendarView calendarView = binding.calendarView;

        // Handle date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = DateUtils.formatDate(year, month, dayOfMonth);

            // Check if date is in the future
            if (DateUtils.isFutureDate(selectedDate)) {
                Toast.makeText(getContext(), "No data for future dates", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if date has entries
            if (!datesWithEntries.contains(selectedDate)) {
                Toast.makeText(getContext(), "No entries for this date", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show entries for selected date
            showEntriesForDate(selectedDate);
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadSavedGoals();
        binding.btnPresets.setOnClickListener(v -> showPresetsDialog());
        binding.btnSetGoal.setOnClickListener(v -> saveGoals());
    }

    private void showEntriesForDate(String date) {
        executorService.execute(() -> {
            List<CalorieEntryEntity> entries = dao.getEntriesForDateSync(date);
            double totalCalories = dao.getTotalCaloriesForDate(date);

            SharedPreferences prefs = requireActivity()
                    .getSharedPreferences("CalorieGoals", Context.MODE_PRIVATE);
            int minGoal = prefs.getInt("min_goal", 0);
            int maxGoal = prefs.getInt("max_goal", 0);

            requireActivity().runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Entries for " + date);

                // Create custom view for dialog
                LinearLayout layout = new LinearLayout(requireContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(50, 20, 50, 20);

                // Show goal status
                if (minGoal > 0 && maxGoal > 0) {
                    TextView goalStatus = new TextView(requireContext());
                    boolean metGoal = totalCalories >= minGoal && totalCalories <= maxGoal;
                    goalStatus.setText(metGoal ? "✓ Goal Met" : "✗ Goal Not Met");
                    goalStatus.setTextColor(metGoal ? Color.GREEN : Color.RED);
                    goalStatus.setTextSize(18);
                    goalStatus.setPadding(0, 0, 0, 20);
                    layout.addView(goalStatus);

                    TextView goalRange = new TextView(requireContext());
                    goalRange.setText(String.format(Locale.getDefault(),
                            "Goal: %d - %d kcal", minGoal, maxGoal));
                    goalRange.setTextSize(14);
                    goalRange.setPadding(0, 0, 0, 10);
                    layout.addView(goalRange);
                }

                // Show total
                TextView totalView = new TextView(requireContext());
                totalView.setText(String.format(Locale.getDefault(),
                        "Total: %.0f kcal", totalCalories));
                totalView.setTextSize(16);
                totalView.setTypeface(null, android.graphics.Typeface.BOLD);
                totalView.setPadding(0, 0, 0, 20);
                layout.addView(totalView);

                // Show entries
                for (CalorieEntryEntity entry : entries) {
                    TextView entryView = new TextView(requireContext());
                    entryView.setText(String.format(Locale.getDefault(),
                            "%s: %.1f × %.0f = %.0f kcal",
                            entry.getName(),
                            entry.getQuantity(),
                            entry.getCaloriesPerUnit(),
                            entry.getTotalCalories()));
                    entryView.setTextSize(14);
                    entryView.setPadding(0, 5, 0, 5);
                    layout.addView(entryView);
                }

                builder.setView(layout);
                builder.setPositiveButton("Close", null);
                builder.show();
            });
        });
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
        String minText = Objects.requireNonNull(binding.etMinCalories.getText()).toString().trim();
        String maxText = Objects.requireNonNull(binding.etMaxCalories.getText()).toString().trim();

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

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
