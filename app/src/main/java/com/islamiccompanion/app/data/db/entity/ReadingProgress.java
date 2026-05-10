package com.islamiccompanion.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Room entity tracking where the user left off in the Quran. */
@Entity(tableName = "reading_progress")
public class ReadingProgress {

    @PrimaryKey
    private int id = 1; // singleton row

    private int surahNumber;
    private int ayahNumber;
    private String surahNameEn;
    private long updatedAt;

    public ReadingProgress() {}

    public ReadingProgress(int surahNumber, int ayahNumber, String surahNameEn) {
        this.surahNumber = surahNumber;
        this.ayahNumber = ayahNumber;
        this.surahNameEn = surahNameEn;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSurahNumber() { return surahNumber; }
    public void setSurahNumber(int surahNumber) { this.surahNumber = surahNumber; }
    public int getAyahNumber() { return ayahNumber; }
    public void setAyahNumber(int ayahNumber) { this.ayahNumber = ayahNumber; }
    public String getSurahNameEn() { return surahNameEn; }
    public void setSurahNameEn(String surahNameEn) { this.surahNameEn = surahNameEn; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
