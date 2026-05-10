package com.islamiccompanion.app.ui.names;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.islamiccompanion.app.data.repository.DuaRepository;
import com.islamiccompanion.app.model.DivineName;

import java.util.List;
import java.util.concurrent.Executors;

public class NamesViewModel extends AndroidViewModel {

    private final DuaRepository repo;
    private final MutableLiveData<List<DivineName>> namesLive = new MutableLiveData<>();
    private final MutableLiveData<String> errorLive = new MutableLiveData<>();

    public NamesViewModel(@NonNull Application application) {
        super(application);
        repo = new DuaRepository(application);
        load();
    }

    private void load() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                namesLive.postValue(repo.getDivineNames());
            } catch (Exception e) {
                errorLive.postValue(e.getMessage());
            }
        });
    }

    public LiveData<List<DivineName>> getNames() { return namesLive; }
    public LiveData<String> getError() { return errorLive; }
}
