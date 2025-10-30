package com.example.caloriecounter.data.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "calorie_entries")
public class CalorieEntryEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String date; // Format: "yyyy-MM-dd"
    private String name;
    private double quantity;
    private double caloriesPerUnit;
    private long timestamp; // For ordering within a day

    public CalorieEntryEntity(String date, String name, double quantity, double caloriesPerUnit, long timestamp) {
        this.date = date;
        this.name = name;
        this.quantity = quantity;
        this.caloriesPerUnit = caloriesPerUnit;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getCaloriesPerUnit() {
        return caloriesPerUnit;
    }

    public void setCaloriesPerUnit(double caloriesPerUnit) {
        this.caloriesPerUnit = caloriesPerUnit;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getTotalCalories() {
        return quantity * caloriesPerUnit;
    }
}