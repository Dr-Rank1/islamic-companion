package com.islamiccompanion.app.data.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Room entity for a bookmarked Quran ayah. */
@Entity(tableName = "bookmarks")
public class Bookmark {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int surahNumber;
    private int ayahNumber;
    private String surahNameEn;
    private long createdAt;

    public Bookmark() {}

    public Bookmark(int surahNumber, int ayahNumber, String surahNameEn) {
        this.surahNumber = surahNumber;
        this.ayahNumber = ayahNumber;
        this.surahNameEn = surahNameEn;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSurahNumber() { return surahNumber; }
    public void setSurahNumber(int surahNumber) { this.surahNumber = surahNumber; }
    public int getAyahNumber() { return ayahNumber; }
    public void setAyahNumber(int ayahNumber) { this.ayahNumber = ayahNumber; }
    public String getSurahNameEn() { return surahNameEn; }
    public void setSurahNameEn(String surahNameEn) { this.surahNameEn = surahNameEn; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
