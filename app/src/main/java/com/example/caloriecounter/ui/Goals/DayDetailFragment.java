package com.example.caloriecounter.ui.Goals;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.data.database.AppDatabase;
import com.example.caloriecounter.data.database.CalorieEntryDao;
import com.example.caloriecounter.data.database.CalorieEntryEntity;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DayDetailFragment extends Fragment {
    private static final String ARG_DATE = "date";
    private String selectedDate;
    private CalorieEntryDao dao;
    private ExecutorService executorService;
    private LinearLayout entriesContainer;
    private TextView tvDateTitle, tvGoalStatus, tvGoalRange, tvTotal;

    public static DayDetailFragment newInstance(String date) {
        DayDetailFragment fragment = new DayDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedDate = getArguments().getString(ARG_DATE);
        }
        AppDatabase database = AppDatabase.getInstance(requireContext());
        dao = database.calorieEntryDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day_detail, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        tvDateTitle = view.findViewById(R.id.tvDateTitle);
        tvGoalStatus = view.findViewById(R.id.tvGoalStatus);
        tvGoalRange = view.findViewById(R.id.tvGoalRange);
        tvTotal = view.findViewById(R.id.tvTotal);
        entriesContainer = view.findViewById(R.id.entriesContainer);

        tvDateTitle.setText(selectedDate);

        loadDayData();

        return view;
    }

    private void loadDayData() {
        executorService.execute(() -> {
            List<CalorieEntryEntity> entries = dao.getEntriesForDateSync(selectedDate);
            double totalCalories = dao.getTotalCaloriesForDate(selectedDate);

            SharedPreferences prefs = requireActivity()
                    .getSharedPreferences("CalorieGoals", Context.MODE_PRIVATE);
            int minGoal = prefs.getInt("min_goal", 0);
            int maxGoal = prefs.getInt("max_goal", 0);

            requireActivity().runOnUiThread(() -> {
                // Show goal status
                if (minGoal > 0 && maxGoal > 0) {
                    boolean metGoal = totalCalories >= minGoal && totalCalories <= maxGoal;
                    tvGoalStatus.setText(metGoal ? "✓ Goal Met" : "✗ Goal Not Met");
                    tvGoalStatus.setTextColor(metGoal ? Color.GREEN : Color.RED);
                    tvGoalStatus.setVisibility(View.VISIBLE);

                    tvGoalRange.setText(String.format(Locale.getDefault(),
                            "Goal: %d - %d kcal", minGoal, maxGoal));
                    tvGoalRange.setVisibility(View.VISIBLE);
                } else {
                    tvGoalStatus.setVisibility(View.GONE);
                    tvGoalRange.setVisibility(View.GONE);
                }

                // Show total
                tvTotal.setText(String.format(Locale.getDefault(),
                        "Total: %.0f kcal", totalCalories));

                // Show entries
                entriesContainer.removeAllViews();
                for (CalorieEntryEntity entry : entries) {
                    TextView entryView = new TextView(requireContext());
                    entryView.setText(String.format(Locale.getDefault(),
                            "%s: %.1f × %.0f = %.0f kcal",
                            entry.getName(),
                            entry.getQuantity(),
                            entry.getCaloriesPerUnit(),
                            entry.getTotalCalories()));
                    entryView.setTextSize(16);
                    entryView.setPadding(0, 12, 0, 12);
                    entriesContainer.addView(entryView);
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}