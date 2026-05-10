package com.islamiccompanion.app.data.repository;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.islamiccompanion.app.model.Hadith;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Provides Hadith data from the bundled JSON asset.
 *
 * TODO (RELIGIOUS ACCURACY): Source ~200 verified hadith from sunnah.com or a
 *   peer-reviewed collection before release. Current asset contains samples only.
 */
public class HadithRepository {

    private final Context context;
    private List<Hadith> cachedHadiths;

    public HadithRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Load all hadiths from the asset. Cached in memory after first load. */
    public List<Hadith> getAllHadiths() throws IOException {
        if (cachedHadiths != null) return cachedHadiths;

        InputStream is = context.getAssets().open("hadith.json");
        InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(reader, JsonObject.class);
        Type type = new TypeToken<List<Hadith>>() {}.getType();
        cachedHadiths = gson.fromJson(root.get("hadiths"), type);
        if (cachedHadiths == null) cachedHadiths = new ArrayList<>();
        return cachedHadiths;
    }

    /**
     * Return a stable "Hadith of the Day" based on day-of-year, so it doesn't
     * change during the day but rotates daily.
     */
    public Hadith getHadithOfTheDay() throws IOException {
        List<Hadith> all = getAllHadiths();
        if (all.isEmpty()) return null;
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        return all.get(dayOfYear % all.size());
    }
}
