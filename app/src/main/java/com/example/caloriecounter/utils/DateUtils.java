package com.example.caloriecounter.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    /**
     * Get today's date in yyyy-MM-dd format
     */
    public static String getTodayDate() {
        return dateFormatter.format(new Date());
    }

    /**
     * Format a date from year, month, and day
     * @param year The year
     * @param month The month (0-11, where 0 is January)
     * @param dayOfMonth The day of the month
     * @return Formatted date string in yyyy-MM-dd format
     */
    public static String formatDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        return dateFormatter.format(calendar.getTime());
    }

    /**
     * Check if a given date string is in the future
     * @param dateString Date in yyyy-MM-dd format
     * @return true if the date is in the future, false otherwise
     */
    public static boolean isFutureDate(String dateString) {
        try {
            Date date = dateFormatter.parse(dateString);
            Date today = dateFormatter.parse(getTodayDate());
            return date != null && today != null && date.after(today);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Parse a date string to Date object
     * @param dateString Date in yyyy-MM-dd format
     * @return Date object or null if parsing fails
     */
    public static Date parseDate(String dateString) {
        try {
            return dateFormatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Format a Date object to string
     * @param date Date object
     * @return Formatted date string in yyyy-MM-dd format
     */
    public static String formatDate(Date date) {
        return dateFormatter.format(date);
    }

    /**
     * Get a date string for a specific number of days offset from today
     * @param daysOffset Number of days to offset (negative for past, positive for future)
     * @return Formatted date string in yyyy-MM-dd format
     */
    public static String getDateWithOffset(int daysOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, daysOffset);
        return dateFormatter.format(calendar.getTime());
    }
}