package com.islamiccompanion.app.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.islamiccompanion.app.data.db.dao.BookmarkDao;
import com.islamiccompanion.app.data.db.dao.PrayerLogDao;
import com.islamiccompanion.app.data.db.dao.TasbihDao;
import com.islamiccompanion.app.data.db.entity.Bookmark;
import com.islamiccompanion.app.data.db.entity.PrayerLogEntry;
import com.islamiccompanion.app.data.db.entity.ReadingProgress;
import com.islamiccompanion.app.data.db.entity.TasbihEntry;

/** Singleton Room database — v2 adds the prayer_log table. */
@Database(
        entities = {
                Bookmark.class,
                TasbihEntry.class,
                ReadingProgress.class,
                PrayerLogEntry.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract BookmarkDao bookmarkDao();
    public abstract TasbihDao tasbihDao();
    public abstract PrayerLogDao prayerLogDao();

    /** v1 → v2: adds the prayer_log table. */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `prayer_log` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`date` TEXT, " +
                "`prayer` TEXT, " +
                "`status` TEXT, " +
                "`loggedAt` INTEGER NOT NULL, " +
                "UNIQUE(`date`, `prayer`)" +
                ")"
            );
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "islamic_companion.db"
                            )
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
