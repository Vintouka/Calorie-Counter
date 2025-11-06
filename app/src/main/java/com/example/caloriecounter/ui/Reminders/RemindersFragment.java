package com.example.caloriecounter.ui.Reminders;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.app.TimePickerDialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.caloriecounter.databinding.FragmentRemindersBinding;

import java.util.Calendar;

public class RemindersFragment extends Fragment {

    private FragmentRemindersBinding binding;
    private ReminderManager reminderManager;
    private TextView tvCurrentReminder;
    private Button btnSetReminder;
    private Button btnCancelReminder;

    // ActivityResultLauncher for the permission request
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Show the time picker dialog.
                    showTimePickerDialog();
                } else {
                    // Permission denied. Inform the user.
                    Toast.makeText(getContext(), "Notification permission is required to set reminders.", Toast.LENGTH_LONG).show();
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        RemindersViewModel remindersViewModel =
                new ViewModelProvider(this).get(RemindersViewModel.class);

        binding = FragmentRemindersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        tvCurrentReminder = binding.textNotifications;
        btnSetReminder = binding.btnSetReminder;
        btnCancelReminder = binding.btnCancelReminder;

        reminderManager = new ReminderManager(requireContext());

        btnSetReminder.setOnClickListener(v -> handleSetReminderClick());
        btnCancelReminder.setOnClickListener(v -> cancelReminder());

        updateReminderStatus();

        remindersViewModel.getText().observe(getViewLifecycleOwner(), tvCurrentReminder::setText);
        return root;
    }

    private void handleSetReminderClick() {
        // Check for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Permission is already granted
                showTimePickerDialog();
            } else {
                // Permission is not granted, request it
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // No runtime permission needed for older Android versions
            showTimePickerDialog();
        }
    }

    private void showTimePickerDialog() {
        final Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, selectedHour, selectedMinute) -> setReminder(selectedHour, selectedMinute),
                hour, minute, false);
        timePickerDialog.setTitle("Set Reminder Time");
        timePickerDialog.show();
    }

    private void setReminder(int hour, int minute) {
        reminderManager.setDailyReminder(hour, minute);
        updateReminderStatus();

        String timeString = String.format("%02d:%02d", hour, minute);
        Toast.makeText(getContext(), "Reminder set for " + timeString + " daily", Toast.LENGTH_SHORT).show();
    }

    private void cancelReminder() {
        reminderManager.cancelReminder();
        updateReminderStatus();
        Toast.makeText(getContext(), "Reminder cancelled", Toast.LENGTH_SHORT).show();
    }

    private void updateReminderStatus() {
        Pair<Integer, Integer> reminderTime = reminderManager.getReminderTime();
        if (reminderTime != null) {
            int hour = reminderTime.first;
            int minute = reminderTime.second;
            String timeString = String.format("%02d:%02d", hour, minute);
            tvCurrentReminder.setText("Daily reminder set for: " + timeString);
            btnCancelReminder.setEnabled(true);
        } else {
            tvCurrentReminder.setText("No reminder set");
            btnCancelReminder.setEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
