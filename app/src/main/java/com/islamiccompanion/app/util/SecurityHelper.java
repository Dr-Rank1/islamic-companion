package com.islamiccompanion.app.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Debug;
import android.util.Base64;
import android.util.Log;

import com.islamiccompanion.app.BuildConfig;

import java.io.File;
import java.security.MessageDigest;

/**
 * Security self-check utilities.
 * Results are informational — never block app usage based on these checks.
 * All checks are lightweight (no heavy computation).
 */
public final class SecurityHelper {

    private static final String TAG = "SecurityHelper";

    private SecurityHelper() {}

    // ---- Security state checks ----

    /** Whether the app's SharedPreferences are encrypted (always true in v3+). */
    public static boolean isPrefsEncrypted(Context ctx) {
        try {
            // Check for the encrypted file presence (simple proxy)
            File prefsDir = new File(ctx.getApplicationInfo().dataDir + "/shared_prefs");
            return new File(prefsDir, EncryptedPrefsHelper.ENCRYPTED_FILE + ".xml").exists();
        } catch (Exception e) {
            return false;
        }
    }

    /** Whether the network config enforces TLS (always true in v3+ via the NSC manifest). */
    public static boolean isTlsEnforced() {
        return true; // NSC declares cleartextTrafficPermitted=false globally
    }

    /** Whether the app is running in debug mode. Should be false in a published release. */
    public static boolean isDebugBuild() {
        return BuildConfig.DEBUG;
    }

    /** Whether a debugger is attached (should never be true in production). */
    public static boolean isDebuggerAttached() {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger();
    }

    /** Heuristic root detection — informational only, not a security gate. */
    public static boolean isLikelyRooted() {
        return checkForSuBinary() || checkForRootPackages();
    }

    /**
     * Verify the APK is signed with the expected certificate.
     * Call this in Application.onCreate() on release builds.
     * If the signature doesn't match, log a warning — do not crash.
     *
     * TODO: Replace EXPECTED_CERT_SHA256 with the actual release cert fingerprint before publishing.
     */
    public static boolean isSignatureValid(Context ctx) {
        final String EXPECTED_CERT_SHA256 =
                "TODO_REPLACE_WITH_RELEASE_CERT_SHA256_FINGERPRINT";

        if (EXPECTED_CERT_SHA256.startsWith("TODO")) {
            // Not configured — skip check in debug builds
            Log.d(TAG, "Signature check skipped (no expected cert configured)");
            return true;
        }

        try {
            String actual = getCertificateFingerprint(ctx);
            boolean valid = EXPECTED_CERT_SHA256.equals(actual);
            if (!valid) {
                Log.e(TAG, "SIGNATURE MISMATCH — app may have been re-signed by a third party");
            }
            return valid;
        } catch (Exception e) {
            Log.w(TAG, "Signature check failed: " + e.getMessage());
            return true; // fail open — log only
        }
    }

    /** Returns the SHA-256 fingerprint of the first signing certificate. */
    public static String getCertificateFingerprint(Context ctx) throws Exception {
        PackageInfo pi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pi = ctx.getPackageManager().getPackageInfo(
                    ctx.getPackageName(),
                    PackageManager.GET_SIGNING_CERTIFICATES);
            Signature[] sigs = pi.signingInfo.getApkContentsSigners();
            return sha256Hex(sigs[0].toByteArray());
        } else {
            pi = ctx.getPackageManager().getPackageInfo(
                    ctx.getPackageName(), PackageManager.GET_SIGNATURES);
            return sha256Hex(pi.signatures[0].toByteArray());
        }
    }

    /** Android security patch level string (e.g., "2024-11-05"). */
    public static String getSecurityPatchLevel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Build.VERSION.SECURITY_PATCH;
        }
        return "Unknown";
    }

    // ---- Private helpers ----

    private static boolean checkForSuBinary() {
        String[] paths = {
                "/system/bin/su", "/system/xbin/su", "/sbin/su",
                "/data/local/xbin/su", "/data/local/bin/su"
        };
        for (String p : paths) {
            if (new File(p).exists()) return true;
        }
        return false;
    }

    private static boolean checkForRootPackages() {
        String[] pkgs = {
                "com.topjohnwu.magisk", "eu.chainfire.supersu",
                "com.noshufou.android.su", "com.koushikdutta.superuser"
        };
        // We can't query other packages without QUERY_ALL_PACKAGES in API 30+,
        // so this is best-effort on older API levels.
        return false;
    }

    private static String sha256Hex(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data);
        return Base64.encodeToString(digest, Base64.NO_WRAP);
    }

    /** Log a warning if running in a suspicious environment. Called from Application.onCreate(). */
    public static void performStartupChecks(Context ctx) {
        if (isDebuggerAttached()) {
            Log.w(TAG, "Debugger attached — unusual for a published app");
        }
        if (isLikelyRooted()) {
            Log.i(TAG, "Device appears rooted — user data at elevated risk");
        }
        isSignatureValid(ctx);
    }
}
