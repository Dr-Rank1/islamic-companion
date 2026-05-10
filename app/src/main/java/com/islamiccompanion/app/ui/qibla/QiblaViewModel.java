package com.islamiccompanion.app.ui.qibla;

import android.app.Application;
import android.hardware.GeomagneticField;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.islamiccompanion.app.data.prefs.AppPreferences;
import com.islamiccompanion.app.util.LocationHelper;

import java.util.Locale;

/**
 * Provides:
 *  - Qibla bearing from the user's location (with GeomagneticField declination correction)
 *  - Distance to Mecca in km
 *  - Calibration status string
 */
public class QiblaViewModel extends AndroidViewModel {

    // Kaaba coordinates
    private static final double KAABA_LAT = 21.4225;
    private static final double KAABA_LNG = 39.8262;

    private final MutableLiveData<Float>  qiblaBearingLive = new MutableLiveData<>(0f);
    private final MutableLiveData<String> distanceLive     = new MutableLiveData<>("");
    private final MutableLiveData<String> statusLive       = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> alignedLive     = new MutableLiveData<>(false);

    public QiblaViewModel(@NonNull Application application) {
        super(application);
        compute(application);
    }

    private void compute(Application app) {
        AppPreferences prefs = new AppPreferences(app);
        double lat = prefs.getLastLatitude();
        double lng = prefs.getLastLongitude();

        // True bearing to Qibla (already uses true north by default in LocationHelper.bearingTo)
        float bearing = LocationHelper.bearingTo(lat, lng, KAABA_LAT, KAABA_LNG);

        // Apply magnetic declination so compass sensor (magnetic north) → true north
        GeomagneticField geoField = new GeomagneticField(
                (float) lat, (float) lng, 0f,
                System.currentTimeMillis());
        float declination = geoField.getDeclination();
        // The bearing already targets true north via great-circle.
        // Device sensor gives magnetic north, so we need to add declination to the
        // bearing we show (or equivalently subtract it from device reading).
        // We store the adjusted Qibla bearing so the compass math stays simple.
        float adjustedBearing = (bearing + declination + 360) % 360;
        qiblaBearingLive.postValue(adjustedBearing);

        // Great-circle distance to Mecca
        double distKm = haversineKm(lat, lng, KAABA_LAT, KAABA_LNG);
        distanceLive.postValue(String.format(Locale.getDefault(), "%.0f", distKm));
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public MutableLiveData<Float>  getQiblaBearing() { return qiblaBearingLive; }
    public MutableLiveData<String> getDistance()     { return distanceLive; }
    public MutableLiveData<String> getStatus()       { return statusLive; }
    public MutableLiveData<Boolean> getAligned()     { return alignedLive; }

    public void setStatus(String msg) { statusLive.postValue(msg); }
    public void setAligned(boolean a) { alignedLive.postValue(a); }
}
