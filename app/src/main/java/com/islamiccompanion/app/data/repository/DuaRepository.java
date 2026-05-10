package com.islamiccompanion.app.data.repository;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.islamiccompanion.app.model.DivineName;
import com.islamiccompanion.app.model.Dua;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides Dua and 99 Names data from bundled JSON assets.
 *
 * TODO (RELIGIOUS ACCURACY): Verify all duas against authenticated source books
 *   (Hisnul Muslim / Fortress of the Muslim) before publishing.
 */
public class DuaRepository {

    private final Context context;
    private List<Dua> cachedDuas;
    private List<DivineName> cachedNames;

    public DuaRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<Dua> getAllDuas() throws IOException {
        if (cachedDuas != null) return cachedDuas;
        InputStream is = context.getAssets().open("duas.json");
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
        Type type = new TypeToken<List<Dua>>() {}.getType();
        cachedDuas = gson.fromJson(root.get("duas"), type);
        if (cachedDuas == null) cachedDuas = new ArrayList<>();
        return cachedDuas;
    }

    /** Return duas grouped by category. */
    public Map<String, List<Dua>> getDuasByCategory() throws IOException {
        Map<String, List<Dua>> grouped = new LinkedHashMap<>();
        for (Dua dua : getAllDuas()) {
            String cat = dua.getCategory() != null ? dua.getCategory() : "General";
            grouped.computeIfAbsent(cat, k -> new ArrayList<>()).add(dua);
        }
        return grouped;
    }

    public List<DivineName> getDivineNames() throws IOException {
        if (cachedNames != null) return cachedNames;
        InputStream is = context.getAssets().open("99names.json");
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
        Type type = new TypeToken<List<DivineName>>() {}.getType();
        cachedNames = gson.fromJson(root.get("names"), type);
        if (cachedNames == null) cachedNames = new ArrayList<>();
        return cachedNames;
    }
}
