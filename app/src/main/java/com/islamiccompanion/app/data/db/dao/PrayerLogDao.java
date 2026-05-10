package com.islamiccompanion.app.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.islamiccompanion.app.data.db.entity.PrayerLogEntry;

import java.util.List;

/** DAO for the prayer log (tracking whether each prayer was prayed). */
@Dao
public interface PrayerLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void log(PrayerLogEntry entry);

    @Query("SELECT * FROM prayer_log WHERE date = :date ORDER BY id ASC")
    List<PrayerLogEntry> getEntriesForDate(String date);

    @Query("SELECT * FROM prayer_log WHERE date = :date AND prayer = :prayer LIMIT 1")
    PrayerLogEntry getEntry(String date, String prayer);

    /** Last 7 days entries for weekly stats. */
    @Query("SELECT * FROM prayer_log WHERE date >= :fromDate ORDER BY date ASC")
    LiveData<List<PrayerLogEntry>> getEntriesSince(String fromDate);

    @Query("SELECT * FROM prayer_log ORDER BY date DESC")
    LiveData<List<PrayerLogEntry>> getAllEntries();

    @Query("SELECT COUNT(*) FROM prayer_log WHERE status != 'missed'")
    int getTotalPrayed();

    @Query("SELECT COUNT(*) FROM prayer_log WHERE status = 'on_time'")
    int getTotalOnTime();

    @Query("SELECT COUNT(*) FROM prayer_log")
    int getTotalLogged();

    @Query("DELETE FROM prayer_log")
    void deleteAll();

    /** Count distinct days with at least one non-missed prayer (for streak calculation). */
    @Query("SELECT DISTINCT date FROM prayer_log WHERE status != 'missed' ORDER BY date DESC")
    List<String> getDatesWithPrayerPrayed();
}
