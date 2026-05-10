package com.islamiccompanion.app.ui.quran;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.islamiccompanion.app.data.prefs.AppPreferences;
import com.islamiccompanion.app.data.repository.QuranRepository;
import com.islamiccompanion.app.model.Ayah;
import com.islamiccompanion.app.model.Surah;

import java.util.List;
import java.util.concurrent.Executors;

public class QuranReaderViewModel extends AndroidViewModel {

    private final QuranRepository repo;
    private final AppPreferences prefs;
    private final MutableLiveData<Surah> surahLive = new MutableLiveData<>();
    private final MutableLiveData<String> errorLive = new MutableLiveData<>();

    public QuranReaderViewModel(@NonNull Application application) {
        super(application);
        repo = new QuranRepository(application);
        prefs = new AppPreferences(application);
    }

    public void loadSurah(int surahNumber) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Surah surah = repo.getSurahByNumber(surahNumber);
                if (surah != null && surah.getAyahs() != null) {
                    for (Ayah a : surah.getAyahs()) {
                        a.setSurahNumber(surahNumber);
                    }
                }
                surahLive.postValue(surah);
            } catch (Exception e) {
                errorLive.postValue(e.getMessage());
            }
        });
    }

    public void saveProgress(int surahNum, int ayahNum, String surahName) {
        repo.saveReadingProgress(surahNum, ayahNum, surahName);
    }

    public boolean isBookmarked(int surahNum, int ayahNum) {
        return repo.isBookmarked(surahNum, ayahNum);
    }

    public void toggleBookmark(int surahNum, int ayahNum, String surahName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (repo.isBookmarked(surahNum, ayahNum)) {
                repo.removeBookmarkByAyah(surahNum, ayahNum);
            } else {
                repo.addBookmark(surahNum, ayahNum, surahName);
            }
        });
    }

    public boolean isShowTranslation() { return prefs.isShowTranslation(); }

    public float getArabicTextSizeSp() {
        switch (prefs.getQuranTextSize()) {
            case "small":   return 16f;
            case "medium":  return 20f;
            case "xlarge":  return 28f;
            case "large":
            default:        return 24f;
        }
    }

    public String getReciter() { return prefs.getReciter(); }

    public LiveData<Surah> getSurah() { return surahLive; }
    public LiveData<String> getError() { return errorLive; }
}
