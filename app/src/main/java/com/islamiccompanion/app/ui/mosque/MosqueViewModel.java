package com.islamiccompanion.app.ui.mosque;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.islamiccompanion.app.data.prefs.AppPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Fetches nearby mosques from the OpenStreetMap Overpass API.
 * Free, no API key required.
 *
 * LiveData design — no null initial values, so observers never fire blank states:
 *  - mosquesLive  → populated only when results arrive
 *  - isLoadingLive → true while fetching
 *  - errorLive    → non-null only on failure
 *  - noResultsLive → true when query returned 0 results
 */
public class MosqueViewModel extends AndroidViewModel {

    private static final int RADIUS_M = 5000;
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    // No initial values so observers don't fire immediately with blank states
    private final MutableLiveData<List<Mosque>> mosquesLive   = new MutableLiveData<>();
    private final MutableLiveData<Boolean>      isLoadingLive = new MutableLiveData<>(false);
    private final MutableLiveData<String>       errorLive     = new MutableLiveData<>();
    private final MutableLiveData<Boolean>      noResultsLive = new MutableLiveData<>();

    private final AppPreferences prefs;
    private final OkHttpClient http;

    public MosqueViewModel(@NonNull Application app) {
        super(app);
        prefs = new AppPreferences(app);
        http = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void searchMosques() {
        double lat = prefs.getLastLatitude();
        double lng = prefs.getLastLongitude();

        // Default Mecca coordinates means location has never been set
        boolean isDefaultLocation =
                Math.abs(lat - 21.3891) < 0.001 && Math.abs(lng - 39.8579) < 0.001;

        if (isDefaultLocation) {
            isLoadingLive.setValue(false);
            errorLive.setValue(
                    "Location not set.\n\nGrant location permission, or choose a city in Settings, then tap Refresh.");
            return;
        }

        isLoadingLive.setValue(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String query =
                        "[out:json][timeout:25];" +
                        "node[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"]" +
                        "(around:" + RADIUS_M + "," + lat + "," + lng + ");" +
                        "out;";

                String url = OVERPASS_URL + "?data=" +
                        URLEncoder.encode(query, "UTF-8");

                Request req = new Request.Builder()
                        .url(url)
                        .header("User-Agent", "IslamicCompanionApp/2.0")
                        .build();

                try (Response resp = http.newCall(req).execute()) {
                    if (!resp.isSuccessful()) {
                        isLoadingLive.postValue(false);
                        errorLive.postValue("Server error " + resp.code() + ". Try again later.");
                        return;
                    }
                    String body = resp.body() != null ? resp.body().string() : "{}";
                    List<Mosque> mosques = parseResponse(body, lat, lng);
                    isLoadingLive.postValue(false);
                    if (mosques.isEmpty()) {
                        noResultsLive.postValue(true);
                    } else {
                        mosquesLive.postValue(mosques);
                    }
                }
            } catch (Exception e) {
                isLoadingLive.postValue(false);
                String msg = e.getMessage();
                if (msg == null || msg.isEmpty()) msg = e.getClass().getSimpleName();
                errorLive.postValue("Could not load mosques: " + msg
                        + "\n\nCheck your internet connection and tap Refresh.");
            }
        });
    }

    private List<Mosque> parseResponse(String json, double userLat, double userLng)
            throws Exception {
        List<Mosque> results = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        JSONArray elements = root.optJSONArray("elements");
        if (elements == null) return results;

        for (int i = 0; i < elements.length(); i++) {
            JSONObject el = elements.getJSONObject(i);
            double elLat = el.optDouble("lat", 0);
            double elLng = el.optDouble("lon", 0);
            if (elLat == 0 && elLng == 0) continue;

            JSONObject tags = el.optJSONObject("tags");
            String name = "";
            if (tags != null) {
                name = tags.optString("name:en", "");
                if (name.isEmpty()) name = tags.optString("name", "");
            }

            Mosque m = new Mosque();
            m.id         = el.optString("id", String.valueOf(i));
            m.name       = name;
            m.lat        = elLat;
            m.lng        = elLng;
            m.distanceKm = haversineKm(userLat, userLng, elLat, elLng);
            results.add(m);
        }

        results.sort((a, b) -> Double.compare(a.distanceKm, b.distanceKm));
        return results.subList(0, Math.min(results.size(), 25));
    }

    private static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public LiveData<List<Mosque>> getMosques()  { return mosquesLive; }
    public LiveData<Boolean>     getIsLoading() { return isLoadingLive; }
    public LiveData<String>      getError()     { return errorLive; }
    public LiveData<Boolean>     getNoResults() { return noResultsLive; }

    public static class Mosque {
        public String id = "";
        public String name = "";
        public double lat;
        public double lng;
        public double distanceKm;
    }
}
