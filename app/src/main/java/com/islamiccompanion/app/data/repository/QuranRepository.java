package com.islamiccompanion.app.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.islamiccompanion.app.data.db.AppDatabase;
import com.islamiccompanion.app.data.db.dao.BookmarkDao;
import com.islamiccompanion.app.data.db.entity.Bookmark;
import com.islamiccompanion.app.data.db.entity.ReadingProgress;
import com.islamiccompanion.app.model.Surah;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Provides Quran data from the bundled JSON asset.
 *
 * TODO (RELIGIOUS ACCURACY): Replace placeholder JSON in assets/quran/quran.json with
 *   the full, verified Quran text.
 *   - Arabic (Uthmani script): https://tanzil.net/download/
 *   - English (Sahih International): public domain
 *   - Additional translation: Yusuf Ali or Pickthall (public domain)
 */
public class QuranRepository {

    private final Context context;
    private final BookmarkDao bookmarkDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private List<Surah> cachedSurahs;

    public QuranRepository(Context context) {
        this.context = context.getApplicationContext();
        this.bookmarkDao = AppDatabase.getInstance(this.context).bookmarkDao();
    }

    /**
     * Load all 114 surahs from the JSON asset. Results are cached in memory.
     */
    public List<Surah> getSurahs() throws IOException {
        if (cachedSurahs != null) return cachedSurahs;

        InputStream is = context.getAssets().open("quran/quran.json");
        InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(reader, JsonObject.class);
        Type listType = new TypeToken<List<Surah>>() {}.getType();
        cachedSurahs = gson.fromJson(root.get("surahs"), listType);
        if (cachedSurahs == null) cachedSurahs = new ArrayList<>();
        return cachedSurahs;
    }

    /** Return a single surah by its 1-based number. */
    public Surah getSurahByNumber(int number) throws IOException {
        for (Surah s : getSurahs()) {
            if (s.getNumber() == number) return s;
        }
        return null;
    }

    // ---- Bookmarks ----

    public LiveData<List<Bookmark>> getAllBookmarks() {
        return bookmarkDao.getAllBookmarks();
    }

    public void addBookmark(int surahNum, int ayahNum, String surahName) {
        executor.execute(() -> bookmarkDao.insertBookmark(
                new Bookmark(surahNum, ayahNum, surahName)));
    }

    public void removeBookmark(Bookmark b) {
        executor.execute(() -> bookmarkDao.deleteBookmark(b));
    }

    /** Remove a bookmark by surah + ayah number (no need to fetch the entity first). */
    public void removeBookmarkByAyah(int surahNum, int ayahNum) {
        executor.execute(() -> bookmarkDao.deleteByAyah(surahNum, ayahNum));
    }

    public boolean isBookmarked(int surahNum, int ayahNum) {
        return bookmarkDao.getBookmark(surahNum, ayahNum) != null;
    }

    // ---- Reading progress ----

    public ReadingProgress getReadingProgress() {
        return bookmarkDao.getReadingProgress();
    }

    public void saveReadingProgress(int surahNum, int ayahNum, String surahName) {
        executor.execute(() -> {
            ReadingProgress progress = new ReadingProgress(surahNum, ayahNum, surahName);
            bookmarkDao.saveReadingProgress(progress);
        });
    }
}
