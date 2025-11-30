package com.example.caloriecounter.ui.Goals;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.data.database.AppDatabase;
import com.example.caloriecounter.data.database.CalorieEntryDao;
import com.example.caloriecounter.databinding.FragmentGoalsBinding;
import com.example.caloriecounter.utils.DateUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private Map<String, Boolean> goalStatusMap = new HashMap<>();
    private CustomCalendarView customCalendarView;
    private View goalsMainContent;
    private FrameLayout fragmentContainer;

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

                // Calculate goal status for each date
                executorService.execute(() -> {
                    SharedPreferences prefs = requireActivity()
                            .getSharedPreferences("CalorieGoals", Context.MODE_PRIVATE);
                    int minGoal = prefs.getInt("min_goal", 0);
                    int maxGoal = prefs.getInt("max_goal", 0);

                    goalStatusMap.clear();
                    for (String date : dates) {
                        double total = dao.getTotalCaloriesForDate(date);
                        boolean metGoal = minGoal > 0 && maxGoal > 0 &&
                                total >= minGoal && total <= maxGoal;
                        goalStatusMap.put(date, metGoal);
                    }

                    requireActivity().runOnUiThread(() -> {
                        if (customCalendarView != null) {
                            customCalendarView.setDatesWithEntries(datesWithEntries);
                            customCalendarView.setGoalStatusMap(goalStatusMap);
                        }
                    });
                });
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGoalsBinding.inflate(inflater, container, false);

        goalsMainContent = binding.goalsMainContent;
        fragmentContainer = binding.fragmentContainer;

        // Setup custom calendar
        setupCustomCalendar();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadSavedGoals();
        binding.btnPresets.setOnClickListener(v -> showPresetsDialog());
        binding.btnSetGoal.setOnClickListener(v -> saveGoals());

        // Handle Goals tab reselection to dismiss DayDetail
        setupNavigationReselectionListener();
    }

    private void setupNavigationReselectionListener() {
        // Get the bottom navigation view from the activity
        if (getActivity() != null) {
            View bottomNav = getActivity().findViewById(R.id.nav_view);
            if (bottomNav instanceof BottomNavigationView) {
                BottomNavigationView navView =
                        (BottomNavigationView) bottomNav;

                navView.setOnItemReselectedListener(item -> {
                    if (item.getItemId() == R.id.navigation_dashboard) { // Assuming Goals is navigation_dashboard
                        // If DayDetail is showing, hide it
                        if (fragmentContainer != null && fragmentContainer.getVisibility() == View.VISIBLE) {
                            while (getChildFragmentManager().getBackStackEntryCount() > 0) {
                                getChildFragmentManager().popBackStackImmediate();
                            }
                            goalsMainContent.setVisibility(View.VISIBLE);
                            fragmentContainer.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Listen for back stack changes
        getChildFragmentManager().addOnBackStackChangedListener(() -> {
            if (getChildFragmentManager().getBackStackEntryCount() == 0) {
                goalsMainContent.setVisibility(View.VISIBLE);
                fragmentContainer.setVisibility(View.GONE);
            }
        });
    }

    private void setupCustomCalendar() {
        customCalendarView = new CustomCalendarView(requireContext());
        customCalendarView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        binding.customCalendarContainer.addView(customCalendarView);

        // Set initial data if already loaded
        customCalendarView.setDatesWithEntries(datesWithEntries);
        customCalendarView.setGoalStatusMap(goalStatusMap);

        customCalendarView.setOnDateClickListener((year, month, day) -> {
            String selectedDate = DateUtils.formatDate(year, month, day);

            if (DateUtils.isFutureDate(selectedDate)) {
                Toast.makeText(getContext(), "No data for future dates", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!datesWithEntries.contains(selectedDate)) {
                Toast.makeText(getContext(), "No entries for this date", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show DayDetailFragment
            showDayDetail(selectedDate);
        });

        // Setup navigation buttons
        binding.btnPrevMonth.setOnClickListener(v -> customCalendarView.previousMonth());
        binding.btnNextMonth.setOnClickListener(v -> customCalendarView.nextMonth());
    }

    private void showDayDetail(String date) {
        goalsMainContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        DayDetailFragment fragment = DayDetailFragment.newInstance(date);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
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

        // Recalculate goal status for all dates with new goals
        executorService.execute(() -> {
            goalStatusMap.clear();
            for (String date : datesWithEntries) {
                double total = dao.getTotalCaloriesForDate(date);
                boolean metGoal = minGoal > 0 && maxGoal > 0 &&
                        total >= minGoal && total <= maxGoal;
                goalStatusMap.put(date, metGoal);
            }

            requireActivity().runOnUiThread(() -> {
                if (customCalendarView != null) {
                    customCalendarView.setGoalStatusMap(goalStatusMap);
                }
            });
        });
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