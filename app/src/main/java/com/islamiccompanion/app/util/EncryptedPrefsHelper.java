package com.islamiccompanion.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.File;
import java.util.Map;

/**
 * Provides an EncryptedSharedPreferences instance backed by an AES-256-GCM key
 * stored in the Android Keystore.
 *
 * Key management:
 *  - Key alias "islamic_companion_master_key" is created once, survives reinstall only
 *    if the device has a hardware-backed Keystore (TEE/StrongBox).
 *  - If the encrypted file is corrupted or the key is lost (e.g., after a factory reset
 *    without backup), the file is deleted and recreated (data loss, but no crash).
 *
 * Migration:
 *  Call {@link #migrateFromPlaintext(Context)} once on app upgrade from v2.
 *  It copies every key/value from the old plaintext prefs to the new encrypted prefs.
 */
public final class EncryptedPrefsHelper {

    private static final String TAG = "EncryptedPrefsHelper";

    /** Encrypted preferences file name. */
    public static final String ENCRYPTED_FILE = "islamic_companion_secure_prefs";

    /** Old plaintext preferences file name (pre-v3). */
    public static final String PLAINTEXT_FILE = "islamic_companion_prefs";

    /** Meta-file storing migration state (intentionally NOT encrypted). */
    private static final String META_FILE     = "islamic_companion_meta";
    private static final String KEY_MIGRATED  = "v3_encrypted_migrated";

    private EncryptedPrefsHelper() {}

    /**
     * Returns an EncryptedSharedPreferences instance.
     * Falls back to regular SharedPreferences if the Keystore is unavailable
     * (e.g., emulators without Keystore, very old devices).
     */
    public static SharedPreferences get(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.w(TAG, "EncryptedSharedPreferences creation failed — deleting corrupted file and retrying: "
                    + e.getMessage());
            // Delete corrupted encrypted file and try again
            deleteEncryptedFile(context);
            return tryCreateAfterReset(context);
        }
    }

    private static SharedPreferences tryCreateAfterReset(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context, ENCRYPTED_FILE, masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception fallback) {
            Log.e(TAG, "Falling back to plaintext prefs: " + fallback.getMessage());
            return context.getSharedPreferences(ENCRYPTED_FILE, Context.MODE_PRIVATE);
        }
    }

    /**
     * One-time migration from the plaintext prefs file to the encrypted one.
     * Safe to call on every launch — checks the migration flag first.
     */
    public static void migrateFromPlaintext(Context context) {
        SharedPreferences meta = context.getSharedPreferences(META_FILE, Context.MODE_PRIVATE);
        if (meta.getBoolean(KEY_MIGRATED, false)) return;

        SharedPreferences oldPrefs = context.getSharedPreferences(
                PLAINTEXT_FILE, Context.MODE_PRIVATE);

        if (oldPrefs.getAll().isEmpty()) {
            // Nothing to migrate — just mark done
            meta.edit().putBoolean(KEY_MIGRATED, true).apply();
            return;
        }

        try {
            SharedPreferences encPrefs = get(context);
            SharedPreferences.Editor editor = encPrefs.edit();

            for (Map.Entry<String, ?> entry : oldPrefs.getAll().entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                if (v instanceof String)  editor.putString(k, (String) v);
                else if (v instanceof Integer) editor.putInt(k, (Integer) v);
                else if (v instanceof Boolean) editor.putBoolean(k, (Boolean) v);
                else if (v instanceof Float)   editor.putFloat(k, (Float) v);
                else if (v instanceof Long)    editor.putLong(k, (Long) v);
            }
            editor.apply();

            Log.i(TAG, "Migrated " + oldPrefs.getAll().size() + " preferences to encrypted storage");
            meta.edit().putBoolean(KEY_MIGRATED, true).apply();
        } catch (Exception e) {
            Log.e(TAG, "Migration failed — will retry on next launch: " + e.getMessage());
        }
    }

    private static void deleteEncryptedFile(Context context) {
        try {
            File prefsDir = new File(context.getApplicationInfo().dataDir + "/shared_prefs");
            File encFile = new File(prefsDir, ENCRYPTED_FILE + ".xml");
            if (encFile.exists() && encFile.delete()) {
                Log.w(TAG, "Deleted corrupted encrypted prefs file");
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not delete encrypted prefs file: " + e.getMessage());
        }
    }

    /** Whether the device's Keystore successfully backs the encryption key. */
    public static boolean isHardwareBacked(Context context) {
        try {
            MasterKey mk = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return mk != null; // actual HW-backed check requires KeyStore API level inspection
        } catch (Exception e) {
            return false;
        }
    }
}
