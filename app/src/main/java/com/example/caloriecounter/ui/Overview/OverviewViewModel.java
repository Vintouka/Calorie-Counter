package com.example.caloriecounter.ui.Overview;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.example.caloriecounter.data.database.AppDatabase;
import com.example.caloriecounter.data.database.CalorieEntryDao;
import com.example.caloriecounter.data.database.CalorieEntryEntity;
import com.example.caloriecounter.data.models.CalorieEntry;
import com.example.caloriecounter.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverviewViewModel extends AndroidViewModel {
    private final CalorieEntryDao dao;
    private final ExecutorService executorService;
    private final LiveData<List<CalorieEntryEntity>> todayEntitiesLive;
    private final LiveData<List<CalorieEntry>> entries;
    private final MediatorLiveData<Double> totalCalories = new MediatorLiveData<>(0.0);

    public OverviewViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        dao = database.calorieEntryDao();
        executorService = Executors.newSingleThreadExecutor();

        // Get today's entries from database
        todayEntitiesLive = dao.getEntriesForDate(DateUtils.getTodayDate());

        // Convert entities to CalorieEntry objects
        entries = Transformations.map(todayEntitiesLive, entities -> {
            List<CalorieEntry> list = new ArrayList<>();
            for (CalorieEntryEntity entity : entities) {
                CalorieEntry entry = new CalorieEntry(
                        entity.getName(),
                        entity.getQuantity(),
                        entity.getCaloriesPerUnit()
                );
                list.add(entry);
            }
            return list;
        });

        // Calculate total calories
        totalCalories.addSource(todayEntitiesLive, list -> {
            double sum = 0;
            for (CalorieEntryEntity e : list) {
                sum += e.getTotalCalories();
            }
            totalCalories.setValue(sum);
        });
    }

    public LiveData<List<CalorieEntry>> getEntries() {
        return entries;
    }

    public LiveData<Double> getTotalCalories() {
        return totalCalories;
    }

    public void addEntry(CalorieEntry entry) {
        executorService.execute(() -> {
            CalorieEntryEntity entity = new CalorieEntryEntity(
                    DateUtils.getTodayDate(),
                    entry.getName(),
                    entry.getQuantity(),
                    entry.getCaloriesPerUnit(),
                    System.currentTimeMillis()
            );
            dao.insert(entity);
        });
    }

    public void updateEntry(int index, CalorieEntry newEntry) {
        executorService.execute(() -> {
            List<CalorieEntryEntity> entities = dao.getEntriesForDateSync(DateUtils.getTodayDate());
            if (index >= 0 && index < entities.size()) {
                CalorieEntryEntity entity = entities.get(index);
                entity.setName(newEntry.getName());
                entity.setQuantity(newEntry.getQuantity());
                entity.setCaloriesPerUnit(newEntry.getCaloriesPerUnit());
                dao.update(entity);
            }
        });
    }

    public void deleteEntry(int index) {
        executorService.execute(() -> {
            List<CalorieEntryEntity> entities = dao.getEntriesForDateSync(DateUtils.getTodayDate());
            if (index >= 0 && index < entities.size()) {
                dao.delete(entities.get(index));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}