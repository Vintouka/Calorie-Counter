package com.example.caloriecounter.data.models;

public class CalorieEntry {
    private long id;
    private final String name;
    private final double quantity;
    private final double caloriesPerUnit;

    public CalorieEntry(String name, double quantity, double caloriesPerUnit) {
        this.name = name;
        this.quantity = quantity;
        this.caloriesPerUnit = caloriesPerUnit;
    }

    public double getTotalCalories() {
        return quantity * caloriesPerUnit;
    }
    public double getQuantity() {
        return quantity;
    }
    public double getCaloriesPerUnit() {
        return caloriesPerUnit;
    }
    public String getName() {
        return name;
    }
}
