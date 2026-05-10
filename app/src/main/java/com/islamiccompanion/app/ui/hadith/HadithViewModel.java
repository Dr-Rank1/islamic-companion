package com.islamiccompanion.app.ui.hadith;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.islamiccompanion.app.data.repository.HadithRepository;
import com.islamiccompanion.app.model.Hadith;

import java.util.concurrent.Executors;

public class HadithViewModel extends AndroidViewModel {

    private final HadithRepository repo;
    private final MutableLiveData<Hadith> hadithLive = new MutableLiveData<>();
    private final MutableLiveData<String> errorLive = new MutableLiveData<>();

    public HadithViewModel(@NonNull Application application) {
        super(application);
        repo = new HadithRepository(application);
        load();
    }

    private void load() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                hadithLive.postValue(repo.getHadithOfTheDay());
            } catch (Exception e) {
                errorLive.postValue(e.getMessage());
            }
        });
    }

    public LiveData<Hadith> getHadith() { return hadithLive; }
    public LiveData<String> getError() { return errorLive; }
}
