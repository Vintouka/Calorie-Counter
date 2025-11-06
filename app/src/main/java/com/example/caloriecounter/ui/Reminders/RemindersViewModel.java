package com.example.caloriecounter.ui.Reminders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RemindersViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public RemindersViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Set daily reminders to meet your calorie goals");
    }

    public LiveData<String> getText() {
        return mText;
    }
}