package com.islamiccompanion.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Records whether the user prayed each of the five prayers on a given day.
 * Status: "on_time" | "late" | "missed" | "unknown"
 */
@Entity(
    tableName = "prayer_log",
    indices = {@Index(value = {"date", "prayer"}, unique = true)}
)
public class PrayerLogEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    /** yyyy-MM-dd */
    private String date;

    /** Fajr | Dhuhr | Asr | Maghrib | Isha */
    private String prayer;

    /** on_time | late | missed */
    private String status;

    private long loggedAt;

    public PrayerLogEntry() {}

    public PrayerLogEntry(String date, String prayer, String status) {
        this.date = date;
        this.prayer = prayer;
        this.status = status;
        this.loggedAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String d) { this.date = d; }
    public String getPrayer() { return prayer; }
    public void setPrayer(String p) { this.prayer = p; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public long getLoggedAt() { return loggedAt; }
    public void setLoggedAt(long t) { this.loggedAt = t; }

    public static final String ON_TIME = "on_time";
    public static final String LATE    = "late";
    public static final String MISSED  = "missed";
}
