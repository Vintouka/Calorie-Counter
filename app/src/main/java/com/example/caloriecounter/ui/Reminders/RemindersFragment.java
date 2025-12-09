package com.example.caloriecounter.ui.Reminders;

import com.example.caloriecounter.R;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.TimePickerDialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecounter.databinding.FragmentRemindersBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RemindersFragment extends Fragment {

    private FragmentRemindersBinding binding;
    private ReminderManager reminderManager;
    private Button btnSetReminder;

    private ReminderViewModel reminderViewModel;
    private RemindersAdapter remindersAdapter;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) chooseReminderThenTime();
                else Toast.makeText(getContext(), "Notification permission is required.", Toast.LENGTH_LONG).show();
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRemindersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnSetReminder = binding.btnSetReminder;

        reminderManager = new ReminderManager(requireContext());
        reminderViewModel = new ViewModelProvider(this).get(ReminderViewModel.class);

        btnSetReminder.setOnClickListener(v -> requestNotiPermissionThenChoose());

        return root;
    }

    private void requestNotiPermissionThenChoose() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }

        chooseReminderThenTime();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = binding.rvReminders;
        remindersAdapter = new RemindersAdapter(position -> confirmDelete(position));
        rv.setAdapter(remindersAdapter);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        reminderViewModel.getReminders().observe(
                getViewLifecycleOwner(),
                remindersAdapter::submitList
        );
    }



    private void chooseReminderThenTime() {
        String[] options = {
                "Breakfast Entry",
                "Lunch Entry",
                "Dinner Entry",
                "Custom Reminder"
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Reminder Type")
                .setItems(options, (dialog, which) -> {

                    if (which == 3) {
                        showCustomReminderDialog();
                    } else {
                        String message = "Please enter your " + options[which].toLowerCase() + "!";
                        pickTimeForReminder(message);
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    private void showCustomReminderDialog() {
        final androidx.appcompat.widget.AppCompatEditText input =
                new androidx.appcompat.widget.AppCompatEditText(requireContext());
        input.setHint("Enter reminder message");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Custom Reminder")
                .setView(input)
                .setPositiveButton("Next", (dialog, which) -> {
                    String message = input.getText().toString().trim();

                    if (!message.isEmpty()) {
                        pickTimeForReminder(message);
                    } else {
                        Toast.makeText(getContext(),
                                "Reminder message cannot be empty.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    private void pickTimeForReminder(String message) {
        Calendar current = Calendar.getInstance();
        int hour = current.get(Calendar.HOUR_OF_DAY);
        int minute = current.get(Calendar.MINUTE);

        new TimePickerDialog(requireContext(), (tp, h, m) -> {

            String fullReminder = message + " at " + String.format("%02d:%02d", h, m);

            // save to list display
            reminderViewModel.addReminder(fullReminder);

            // schedule alarm WITH MESSAGE
            // Add a new reminder (multiple reminders supported)
            reminderManager.addReminder(h, m, message);


            Toast.makeText(getContext(),
                    "Reminder set: " + fullReminder,
                    Toast.LENGTH_SHORT).show();

        }, hour, minute, false).show();
    }



    private void confirmDelete(int position) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Reminder")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (d, w) -> {
                    // Get the reminder message from ViewModel
                    String fullReminder = reminderViewModel.getReminders().getValue().get(position);

                    // Cancel the corresponding alarm
                    // NOTE: ReminderManager stores requestCode based on hour, minute, and message
                    // We'll need to parse hour & minute from the displayed string
                    String[] parts = fullReminder.split(" at ");
                    if (parts.length == 2) {
                        String message = parts[0];
                        String[] timeParts = parts[1].split(":");
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = Integer.parseInt(timeParts[1]);

                        // Cancel specific reminder
                        int requestCode = (hour * 60 + minute) + message.hashCode();
                        reminderManager.cancelReminder(requestCode);
                    }

                    // Remove from UI list
                    reminderViewModel.deleteReminder(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }




    public static class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.ViewHolder> {

        public interface OnDeleteClickListener {
            void onDelete(int position);
        }

        private List<String> reminders = new ArrayList<>();
        private final OnDeleteClickListener listener;

        public RemindersAdapter(OnDeleteClickListener listener) {
            this.listener = listener;
        }

        public void submitList(List<String> list) {
            reminders = list;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() { return reminders.size(); }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reminder, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tv.setText(reminders.get(position));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(position));
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            ImageButton btnDelete;

            ViewHolder(View item) {
                super(item);
                tv = item.findViewById(R.id.tvReminderItem);
                btnDelete = item.findViewById(R.id.btnDeleteReminder);
            }
        }
    }



    public static class ReminderViewModel extends ViewModel {
        private final MutableLiveData<List<String>> reminders =
                new MutableLiveData<>(new ArrayList<>());

        LiveData<List<String>> getReminders() { return reminders; }

        void addReminder(String r) {
            List<String> list = new ArrayList<>(reminders.getValue());
            list.add(r);
            reminders.setValue(list);
        }

        void deleteReminder(int index) {
            List<String> list = new ArrayList<>(reminders.getValue());
            list.remove(index);
            reminders.setValue(list);
        }
    }
}
