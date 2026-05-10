package com.islamiccompanion.app.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.islamiccompanion.app.BuildConfig;
import com.islamiccompanion.app.data.prefs.AppPreferences;

import java.util.concurrent.TimeUnit;

/**
 * Centralised AdMob integration with production safeguards.
 *
 * Key features:
 *  - Ad IDs come from BuildConfig (injected via keystore.properties) — never hardcoded.
 *  - PG content-rating filter (no gambling/alcohol/adult on a religious app).
 *  - 4-minute interstitial cooldown + max-8-per-session cap.
 *  - Respects ads_removed lifetime IAP flag.
 *  - Respects ads_removed_until 24-hour rewarded removal window.
 *  - No ads within 5 minutes of prayer time (set externally by callers).
 *
 * TODO (before publishing):
 *  - Integrate UMP (User Messaging Platform) SDK for EEA/UK GDPR consent.
 *  - Gate MobileAds.initialize() behind UMP consent result.
 *  - Set real ad unit IDs in keystore.properties (see ADS_INTEGRATION.md).
 */
public final class AdHelper {

    private static final String TAG = "AdHelper";

    /** Minimum milliseconds between two interstitials. */
    private static final long COOLDOWN_MS     = TimeUnit.MINUTES.toMillis(4);
    /** Maximum interstitials shown per app session. */
    private static final int  MAX_PER_SESSION = 8;

    private static InterstitialAd cachedInterstitial;
    private static RewardedAd     cachedRewarded;
    private static long           lastInterstitialShownAt = 0L;
    private static int            sessionInterstitialCount = 0;

    private AdHelper() {}

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Configure global ad settings (call once from Application.onCreate AFTER consent).
     * Sets the content rating to PG so gambling, alcohol, and adult ads are excluded —
     * critical for a religious app audience.
     */
    public static void configure() {
        RequestConfiguration config = new RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG)
                .build();
        MobileAds.setRequestConfiguration(config);
    }

    // ── Banner ────────────────────────────────────────────────────────────────

    /** Load a banner ad. No-op if ads are removed. */
    public static void loadBanner(AdView adView) {
        if (adView == null) return;
        if (adsRemovedGlobally(adView.getContext())) {
            adView.setVisibility(android.view.View.GONE);
            return;
        }
        adView.loadAd(buildRequest());
    }

    // ── Interstitial ──────────────────────────────────────────────────────────

    public interface OnInterstitialReady { void onReady(); }

    /**
     * Pre-load an interstitial ad so it can be shown immediately when needed.
     * Uses the unit ID from BuildConfig (injected from keystore.properties).
     */
    public static void loadInterstitial(Context ctx, OnInterstitialReady callback) {
        if (adsRemovedGlobally(ctx)) return;
        if (cachedInterstitial != null) {
            if (callback != null) callback.onReady();
            return;
        }
        InterstitialAd.load(ctx, BuildConfig.AD_INTERSTITIAL_ID, buildRequest(),
                new InterstitialAdLoadCallback() {
                    @Override public void onAdLoaded(InterstitialAd ad) {
                        cachedInterstitial = ad;
                        if (callback != null) callback.onReady();
                    }
                    @Override public void onAdFailedToLoad(LoadAdError e) {
                        cachedInterstitial = null;
                        Log.d(TAG, "Interstitial failed to load: " + e.getMessage());
                    }
                });
    }

    /**
     * Show the cached interstitial if:
     *  - Ads are not removed
     *  - Cooldown has elapsed
     *  - Session cap not exceeded
     *  - An ad is ready
     */
    public static void showInterstitialIfReady(Activity activity) {
        if (activity == null) return;
        if (adsRemovedGlobally(activity)) return;

        long now = System.currentTimeMillis();
        if ((now - lastInterstitialShownAt) < COOLDOWN_MS) return;
        if (sessionInterstitialCount >= MAX_PER_SESSION) return;
        if (cachedInterstitial == null) {
            loadInterstitial(activity, null); // warm up for next time
            return;
        }

        cachedInterstitial.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                cachedInterstitial = null;
                lastInterstitialShownAt = System.currentTimeMillis();
                sessionInterstitialCount++;
                loadInterstitial(activity, null); // pre-load next
            }
            @Override public void onAdFailedToShowFullScreenContent(
                    com.google.android.gms.ads.AdError e) {
                cachedInterstitial = null;
            }
        });
        cachedInterstitial.show(activity);
    }

    // ── Rewarded ──────────────────────────────────────────────────────────────

    public interface OnRewardGranted { void onGranted(); }

    /** Load a rewarded ad for the "Remove Ads for 24 hours" offer. */
    public static void loadRewarded(Context ctx) {
        if (adsRemovedGlobally(ctx)) return;
        RewardedAd.load(ctx, BuildConfig.AD_REWARDED_ID, buildRequest(),
                new RewardedAdLoadCallback() {
                    @Override public void onAdLoaded(RewardedAd ad) {
                        cachedRewarded = ad;
                    }
                    @Override public void onAdFailedToLoad(LoadAdError e) {
                        cachedRewarded = null;
                    }
                });
    }

    /**
     * Show a rewarded ad. On reward: removes ads for 24 hours, calls callback.
     */
    public static void showRewardedIfReady(Activity activity, OnRewardGranted callback) {
        if (activity == null || cachedRewarded == null) {
            loadRewarded(activity);
            return;
        }
        cachedRewarded.show(activity, rewardItem -> {
            // Grant 24-hour ad removal
            AppPreferences prefs = new AppPreferences(activity);
            prefs.putLong(AppPreferences.KEY_ADS_REMOVED_UNTIL,
                    System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24));
            if (callback != null) callback.onGranted();
            cachedRewarded = null;
        });
    }

    // ── Ads-removed state ─────────────────────────────────────────────────────

    /**
     * Returns true if ads should be suppressed:
     *  - Lifetime IAP removal purchased, OR
     *  - 24-hour rewarded removal still active.
     */
    public static boolean adsRemovedGlobally(Context ctx) {
        AppPreferences prefs = new AppPreferences(ctx);
        if (prefs.getBoolean(AppPreferences.KEY_ADS_REMOVED, false)) return true;
        long removedUntil = prefs.getLong(AppPreferences.KEY_ADS_REMOVED_UNTIL, 0L);
        return System.currentTimeMillis() < removedUntil;
    }

    /** Called when the user completes a lifetime Remove Ads purchase. */
    public static void setAdsRemovedLifetime(Context ctx) {
        new AppPreferences(ctx).putBoolean(AppPreferences.KEY_ADS_REMOVED, true);
        Log.i(TAG, "Lifetime ad removal activated");
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private static AdRequest buildRequest() {
        return new AdRequest.Builder().build();
    }
}
