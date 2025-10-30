package com.example.caloriecounter.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CalorieEntryDao {

    @Insert
    void insert(CalorieEntryEntity entry);

    @Update
    void update(CalorieEntryEntity entry);

    @Delete
    void delete(CalorieEntryEntity entry);

    @Query("SELECT * FROM calorie_entries WHERE date = :date ORDER BY timestamp ASC")
    LiveData<List<CalorieEntryEntity>> getEntriesForDate(String date);

    @Query("SELECT * FROM calorie_entries WHERE date = :date ORDER BY timestamp ASC")
    List<CalorieEntryEntity> getEntriesForDateSync(String date);

    @Query("SELECT SUM(quantity * caloriesPerUnit) FROM calorie_entries WHERE date = :date")
    double getTotalCaloriesForDate(String date);

    @Query("SELECT DISTINCT date FROM calorie_entries ORDER BY date DESC")
    LiveData<List<String>> getAllDatesWithEntries();

    @Query("DELETE FROM calorie_entries WHERE date = :date")
    void deleteEntriesForDate(String date);

    @Query("DELETE FROM calorie_entries")
    void deleteAllEntries();
}