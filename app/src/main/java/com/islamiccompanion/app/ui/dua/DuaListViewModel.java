package com.islamiccompanion.app.ui.dua;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.islamiccompanion.app.data.repository.DuaRepository;
import com.islamiccompanion.app.model.Dua;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class DuaListViewModel extends AndroidViewModel {

    private final DuaRepository repo;
    private final MutableLiveData<Map<String, List<Dua>>> duasByCategory = new MutableLiveData<>();
    private final MutableLiveData<String> errorLive = new MutableLiveData<>();

    public DuaListViewModel(@NonNull Application application) {
        super(application);
        repo = new DuaRepository(application);
        load();
    }

    private void load() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                duasByCategory.postValue(repo.getDuasByCategory());
            } catch (Exception e) {
                errorLive.postValue(e.getMessage());
            }
        });
    }

    public LiveData<Map<String, List<Dua>>> getDuasByCategory() { return duasByCategory; }
    public LiveData<String> getError() { return errorLive; }
}
