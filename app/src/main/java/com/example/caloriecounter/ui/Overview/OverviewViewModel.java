package com.example.caloriecounter.ui.Overview;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.caloriecounter.data.models.CalorieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OverviewViewModel extends ViewModel {
    private final MutableLiveData<List<CalorieEntry>> entries = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<Double> totalCalories = new MediatorLiveData<>();

    public OverviewViewModel() {
        totalCalories.addSource(entries, list -> {
            double sum = 0;
            for (CalorieEntry e : list) sum += e.getTotalCalories();
            totalCalories.setValue(sum);
        });
    }

    public LiveData<List<CalorieEntry>> getEntries() { return entries; }
    public LiveData<Double> getTotalCalories() { return totalCalories; }

    public void addEntry(CalorieEntry e) {
        List<CalorieEntry> current = new ArrayList<>(Objects.requireNonNull(entries.getValue()));
        current.add(0, e);
        entries.setValue(current);
    }

    public void updateEntry(int index, CalorieEntry newEntry) {
        List<CalorieEntry> current = new ArrayList<>(Objects.requireNonNull(entries.getValue()));
        if (index >= 0 && index < current.size()) {
            current.set(index, newEntry);
            entries.setValue(current);
        }
    }

    public void deleteEntry(int index) {
        List<CalorieEntry> current = new ArrayList<>(Objects.requireNonNull(entries.getValue()));
        if (index >= 0 && index < current.size()) {
            current.remove(index);
            entries.setValue(current);
        }
    }
}