package com.islamiccompanion.app.ui.quran;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.islamiccompanion.app.data.repository.QuranRepository;
import com.islamiccompanion.app.model.Surah;

import java.util.List;
import java.util.concurrent.Executors;

public class QuranListViewModel extends AndroidViewModel {

    private final QuranRepository repo;
    private final MutableLiveData<List<Surah>> surahsLive = new MutableLiveData<>();
    private final MutableLiveData<String> errorLive = new MutableLiveData<>();

    public QuranListViewModel(@NonNull Application application) {
        super(application);
        repo = new QuranRepository(application);
        loadSurahs();
    }

    private void loadSurahs() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                surahsLive.postValue(repo.getSurahs());
            } catch (Exception e) {
                errorLive.postValue(e.getMessage());
            }
        });
    }

    public LiveData<List<Surah>> getSurahs() { return surahsLive; }
    public LiveData<String> getError() { return errorLive; }
    public QuranRepository getRepo() { return repo; }
}
