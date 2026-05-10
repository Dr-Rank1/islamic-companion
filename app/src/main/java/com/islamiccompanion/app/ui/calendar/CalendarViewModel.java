package com.islamiccompanion.app.ui.calendar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.islamiccompanion.app.data.prefs.AppPreferences;
import com.islamiccompanion.app.util.HijriDateUtil;

import java.util.Calendar;

public class CalendarViewModel extends AndroidViewModel {

    private final MutableLiveData<int[]> currentHijriLive = new MutableLiveData<>();
    private final MutableLiveData<String> todayStringLive = new MutableLiveData<>();
    private int displayYear;
    private int displayMonth;
    private final AppPreferences prefs;

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        prefs = new AppPreferences(application);
        Calendar now = Calendar.getInstance();
        displayYear  = now.get(Calendar.YEAR);
        displayMonth = now.get(Calendar.MONTH) + 1; // 1-based
        loadCurrentMonth();
    }

    private void loadCurrentMonth() {
        int[] hijri = HijriDateUtil.todayHijri(prefs.getHijriOffset());
        currentHijriLive.postValue(hijri);
        todayStringLive.postValue(HijriDateUtil.formatHijri(hijri));
    }

    public void prevMonth() {
        displayMonth--;
        if (displayMonth < 1) { displayMonth = 12; displayYear--; }
        loadCurrentMonth();
    }

    public void nextMonth() {
        displayMonth++;
        if (displayMonth > 12) { displayMonth = 1; displayYear++; }
        loadCurrentMonth();
    }

    public int getDisplayYear()  { return displayYear; }
    public int getDisplayMonth() { return displayMonth; }

    public LiveData<int[]> getCurrentHijri() { return currentHijriLive; }
    public LiveData<String> getTodayString()  { return todayStringLive; }
}
