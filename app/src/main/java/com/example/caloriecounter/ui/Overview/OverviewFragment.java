package com.example.caloriecounter.ui.Overview;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.caloriecounter.R;
import com.example.caloriecounter.databinding.FragmentOverviewBinding;

public class OverviewFragment extends Fragment {
    private FragmentOverviewBinding binding;

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
        loadCalorieGoals();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload goals when returning to this fragment
        loadCalorieGoals();
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