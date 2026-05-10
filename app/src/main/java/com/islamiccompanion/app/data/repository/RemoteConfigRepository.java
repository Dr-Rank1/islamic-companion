package com.islamiccompanion.app.data.repository;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.islamiccompanion.app.data.prefs.AppPreferences;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Fetches a lightweight JSON config from a hosted URL once every 24 hours.
 * Uses the cached value on subsequent launches within the same day.
 *
 * Config schema (host at https://islamiccompanion.app/config.json):
 * {
 *   "min_supported_version":  10000,  // versionCode — show "please update" below this
 *   "force_update_version":   10500,  // versionCode — block app below this
 *   "ad_interstitial_cooldown_seconds": 240,
 *   "messages": [
 *     { "id": "ramadan_2026", "title": "Ramadan Mubarak!", "body": "...", "expires_at": "2026-04-15" }
 *   ]
 * }
 *
 * Privacy: the request is a plain GET with no identifying headers — no user tracking.
 *
 * TODO: host config.json on GitHub Pages or similar before launch.
 *       URL: https://islamiccompanion.app/config.json
 */
public class RemoteConfigRepository {

    private static final String TAG        = "RemoteConfig";
    private static final String CONFIG_URL =
            "https://islamiccompanion.app/config.json"; // TODO: set up this URL before launch
    private static final long   CACHE_TTL  = TimeUnit.HOURS.toMillis(24);

    private static final String PREF_CONFIG_JSON      = "remote_config_json";
    private static final String PREF_CONFIG_FETCHED_AT = "remote_config_fetched_at";

    private final Context context;
    private final OkHttpClient http;
    private final AppPreferences prefs;

    private JsonObject cachedConfig;

    public RemoteConfigRepository(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.prefs   = new AppPreferences(ctx);
        this.http    = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /** Fetch config in background if cache is stale. Safe to call on every app open. */
    public void fetchIfStale() {
        long lastFetch = prefs.getLong(PREF_CONFIG_FETCHED_AT, 0L);
        if (System.currentTimeMillis() - lastFetch < CACHE_TTL) {
            Log.d(TAG, "Config is fresh — skipping fetch");
            return;
        }
        Executors.newSingleThreadExecutor().execute(this::fetch);
    }

    private void fetch() {
        try {
            Request req = new Request.Builder()
                    .url(CONFIG_URL)
                    .header("User-Agent", "IslamicCompanion/" + android.os.Build.VERSION.SDK_INT)
                    .get()
                    .build();
            try (Response resp = http.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) return;
                String json = resp.body().string();
                // Validate it's parseable JSON before caching
                new Gson().fromJson(json, JsonObject.class);
                prefs.putString(PREF_CONFIG_JSON, json);
                prefs.putLong(PREF_CONFIG_FETCHED_AT, System.currentTimeMillis());
                Log.d(TAG, "Remote config updated");
            }
        } catch (Exception e) {
            Log.w(TAG, "Remote config fetch failed: " + e.getMessage());
        }
    }

    /** Get the cached config. Returns null if no config has been fetched yet. */
    public JsonObject getConfig() {
        if (cachedConfig != null) return cachedConfig;
        String json = prefs.getString(PREF_CONFIG_JSON, null);
        if (json == null) return null;
        try {
            cachedConfig = new Gson().fromJson(json, JsonObject.class);
            return cachedConfig;
        } catch (Exception e) {
            return null;
        }
    }

    public int getMinSupportedVersion() {
        JsonObject c = getConfig();
        if (c == null || !c.has("min_supported_version")) return 0;
        return c.get("min_supported_version").getAsInt();
    }

    public int getForceUpdateVersion() {
        JsonObject c = getConfig();
        if (c == null || !c.has("force_update_version")) return 0;
        return c.get("force_update_version").getAsInt();
    }

    public int getInterstitialCooldownSeconds() {
        JsonObject c = getConfig();
        if (c == null || !c.has("ad_interstitial_cooldown_seconds")) return 240;
        return c.get("ad_interstitial_cooldown_seconds").getAsInt();
    }
}
