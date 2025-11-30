package com.example.caloriecounter.ui.Goals;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecounter.R;
import com.example.caloriecounter.data.database.AppDatabase;
import com.example.caloriecounter.data.database.CalorieEntryDao;
import com.example.caloriecounter.data.database.CalorieEntryEntity;
import com.example.caloriecounter.data.models.CalorieEntry;
import com.example.caloriecounter.ui.Overview.EntriesAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DayDetailFragment extends Fragment {
    private static final String ARG_DATE = "date";
    private String selectedDate;
    private CalorieEntryDao dao;
    private ExecutorService executorService;
    private RecyclerView recyclerView;
    private EntriesAdapter adapter;
    private TextView tvDateTitle, tvGoalStatus, tvGoalRange, tvTotal;
    private ProgressBar progressBar;

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
        toolbar.setNavigationOnClickListener(v -> {
            // Pop the child fragment manager back stack
            if (getParentFragment() != null) {
                getParentFragment().getChildFragmentManager().popBackStack();
                // Show main content again
                if (getParentFragment().getView() != null) {
                    getParentFragment().getView().findViewById(R.id.goalsMainContent).setVisibility(View.VISIBLE);
                    getParentFragment().getView().findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
                }
            }
        });

        tvDateTitle = view.findViewById(R.id.tvDateTitle);
        tvGoalStatus = view.findViewById(R.id.tvGoalStatus);
        tvGoalRange = view.findViewById(R.id.tvGoalRange);
        tvTotal = view.findViewById(R.id.tvTotal);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView = view.findViewById(R.id.rvEntries);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Create adapter with null listeners (read-only)
        adapter = new EntriesAdapter(null, null);
        recyclerView.setAdapter(adapter);

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

                    // Set progress bar
                    progressBar.setMax(maxGoal);
                    progressBar.setProgress((int) totalCalories, true);
                } else {
                    tvGoalStatus.setVisibility(View.GONE);
                    tvGoalRange.setText("No goal set");
                    progressBar.setVisibility(View.GONE);
                }

                // Show total
                tvTotal.setText(String.format(Locale.getDefault(),
                        "%.0f kcal", totalCalories));

                // Convert entities to CalorieEntry list for adapter
                List<CalorieEntry> calorieEntries = new ArrayList<>();
                for (CalorieEntryEntity entity : entries) {
                    calorieEntries.add(new CalorieEntry(
                            entity.getName(),
                            entity.getQuantity(),
                            entity.getCaloriesPerUnit()
                    ));
                }
                adapter.submitList(calorieEntries);
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