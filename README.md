# Calorie Counter

## Description of Application 
This project is an Android application created in Android Studio built using Java, with the intended purpose to provide users with a method to track and maintain their caloric goals through input of various food items, in which the user will store and record the item's name, caloric value, and quantity of item, to meet a set caloric goal throughout the day. The application will also have access to a calendar feature which will allow the user to view previous entries on previous days and keep a tab on whether their goals have or have not been met. Furthermore, the application also provides the use of a notification feature which the user could use, enabling them to set reminders throughout the day and allow custom messages to be sent to remind them of any specific needs that need to be attended to.

## File Structure 

    app/
        └── java/com.example.caloriecounter/
            ├── ui/
            │   ├── Goals/
            │   │   └── CustomCalendarView.java 
            |   |   ├── GoalsViewModel.java
            │   │   └── GoalsFragment.java
            |   |   |___DayDetailFragment.java
            |   | OverView/
            │   │   ├── EntreiesAdapter.java
            │   │   └── OverviewFragmeent.java
            |   |   |__ OverviewViewModel.java
            │   ├── Reminders/
            │   │   ├── ReminderFragment.java
            │   │   ├── ReminderManager.java
            │   │   ├── ReminderReceiver.java
            │   │   ├── ReminderService.java
            │   │   └── NotificationHelper.java 
            |   |   |___RemidersViewModel.java

These files contain the codes implemented throughout the development process, disregarding other files where the User Interface was created, with each file corresponding to the 3 main pages of the application, that being the Goals Page, Overview Page, and Reminders Page. With the Goals page, the user can set their caloric intake goal for the day by manually inputting the goal or they may set a goal with some hard-coded presets that the user may use if they are uncertain about what goal they should follow while also implementing a calendar feature which shows the days where the user goal has or has not been met, allowing the user to see previous entries from prior days.

#Caloric Presets Code 

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
        
The Overview Page allows the user to see the progress that they have towards their daily caloric intake goal as well as provides them with the option to input any foods they have eaten which updates the progress bar and items are saved upon entry which allows the user to be able to enter the same item again and view it on a previous day.

## Overview Code Save Data 
The application saves the user data even if the application is suspended, allowing the user to view previous entries in the calendar and the entries of that same day.

     private void loadCalorieGoals() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("CalorieGoals", Context.MODE_PRIVATE);
        int minGoal = prefs.getInt("min_goal", 0);
        int maxGoal = prefs.getInt("max_goal", 0);

        if (minGoal > 0 && maxGoal > 0) {
            binding.tvCalorieGoal.setText(String.format(getString(R.string.daily_calorie_goal_display), minGoal, maxGoal));
            binding.tvCalorieGoal.setVisibility(View.VISIBLE);
            binding.progressBar2.setMax(maxGoal);
            Double totalCalories = viewModel.getTotalCalories().getValue();
            int progress = totalCalories != null ? (int) (double) totalCalories : 0;
            binding.progressBar2.setProgress(progress);
        } else {
            binding.tvCalorieGoal.setText(R.string.no_goal_set);
            binding.tvCalorieGoal.setVisibility(View.VISIBLE);
        }
    }

The Reminders or notification portion of the application allows the app to send notifications to the user's phone, given by reminders the user can implement where the user can choose a preset message or by entering a custom message. The user can then choose a time using the clock feature added to set a specific time they would like the notification to be sent, allowing them to set up multiple reminders and have the app remind them to enter their breakfast, lunch, or dinner entries or any other personalized message.

## Reminders Scheduling Code 
        private void scheduleAlarm(int hour, int minute, String message, int requestCode) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

