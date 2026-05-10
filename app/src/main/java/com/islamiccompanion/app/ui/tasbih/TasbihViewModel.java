package com.islamiccompanion.app.ui.tasbih;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.islamiccompanion.app.data.db.AppDatabase;
import com.islamiccompanion.app.data.db.entity.TasbihEntry;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class TasbihViewModel extends AndroidViewModel {

    /** Bundled dhikr presets: [name, arabicText, target]. */
    public static final List<String[]> DHIKR_PRESETS = Arrays.asList(
            new String[]{"SubhanAllah",      "سُبْحَانَ اللَّهِ",    "33"},
            new String[]{"Alhamdulillah",    "الْحَمْدُ لِلَّهِ",   "33"},
            new String[]{"Allahu Akbar",     "اللَّهُ أَكْبَرُ",    "34"},
            new String[]{"La ilaha ill Allah","لَا إِلَٰهَ إِلَّا اللَّهُ", "100"},
            new String[]{"Astaghfirullah",   "أَسْتَغْفِرُ اللَّهَ", "100"},
            new String[]{"Salawat",          "اللَّهُمَّ صَلِّ عَلَى مُحَمَّد", "100"}
    );

    private final MutableLiveData<Integer> countLive = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> targetLive = new MutableLiveData<>(33);
    private final MutableLiveData<String> dhikrNameLive = new MutableLiveData<>("SubhanAllah");
    private final MutableLiveData<String> dhikrArabicLive = new MutableLiveData<>("سُبْحَانَ اللَّهِ");
    private final MutableLiveData<Boolean> targetReachedLive = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> dailyTotalLive = new MutableLiveData<>(0);

    private int presetIndex = 0;
    private final AppDatabase db;

    public TasbihViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        loadPreset(0);
    }

    public void tap() {
        int current = countLive.getValue() != null ? countLive.getValue() : 0;
        int target = targetLive.getValue() != null ? targetLive.getValue() : 33;
        current++;
        countLive.setValue(current);
        if (current >= target) {
            targetReachedLive.setValue(true);
            saveToDb(target);
        }
    }

    public void reset() {
        countLive.setValue(0);
        targetReachedLive.setValue(false);
    }

    public void nextPreset() {
        presetIndex = (presetIndex + 1) % DHIKR_PRESETS.size();
        loadPreset(presetIndex);
        reset();
    }

    public void setCustomDhikr(String name, int target) {
        dhikrNameLive.setValue(name);
        dhikrArabicLive.setValue(name);
        targetLive.setValue(target);
        reset();
    }

    private void loadPreset(int index) {
        String[] preset = DHIKR_PRESETS.get(index);
        dhikrNameLive.setValue(preset[0]);
        dhikrArabicLive.setValue(preset[1]);
        targetLive.setValue(Integer.parseInt(preset[2]));
    }

    private void saveToDb(int count) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String name = dhikrNameLive.getValue() != null ? dhikrNameLive.getValue() : "";
        TasbihEntry entry = new TasbihEntry(today, name, count);
        Executors.newSingleThreadExecutor().execute(() -> db.tasbihDao().insertOrUpdate(entry));
    }

    public LiveData<Integer> getCount()       { return countLive; }
    public LiveData<Integer> getTarget()      { return targetLive; }
    public LiveData<String>  getDhikrName()   { return dhikrNameLive; }
    public LiveData<String>  getDhikrArabic() { return dhikrArabicLive; }
    public LiveData<Boolean> isTargetReached(){ return targetReachedLive; }
    public LiveData<Integer> getDailyTotal()  { return dailyTotalLive; }
}
