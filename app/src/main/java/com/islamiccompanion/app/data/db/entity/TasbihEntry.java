package com.islamiccompanion.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Room entity for daily Tasbih (dhikr) session totals. */
@Entity(tableName = "tasbih_entries")
public class TasbihEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String date;      // "yyyy-MM-dd"
    private String dhikr;     // e.g. "SubhanAllah"
    private int count;
    private long updatedAt;

    public TasbihEntry() {}

    public TasbihEntry(String date, String dhikr, int count) {
        this.date = date;
        this.dhikr = dhikr;
        this.count = count;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDhikr() { return dhikr; }
    public void setDhikr(String dhikr) { this.dhikr = dhikr; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
