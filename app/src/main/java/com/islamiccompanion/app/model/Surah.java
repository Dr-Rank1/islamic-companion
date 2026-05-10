package com.islamiccompanion.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** A Quranic chapter (surah) with its ayahs. */
public class Surah {

    @SerializedName("number")
    private int number;

    @SerializedName("name_arabic")
    private String nameArabic;

    @SerializedName("name_english")
    private String nameEnglish;

    @SerializedName("name_transliteration")
    private String nameTransliteration;

    @SerializedName("revelation_type")
    private String revelationType; // "Meccan" or "Medinan"

    @SerializedName("ayah_count")
    private int ayahCount;

    @SerializedName("ayahs")
    private List<Ayah> ayahs;

    public Surah() {}

    public int getNumber() { return number; }
    public String getNameArabic() { return nameArabic; }
    public String getNameEnglish() { return nameEnglish; }
    public String getNameTransliteration() { return nameTransliteration; }
    public String getRevelationType() { return revelationType; }
    public int getAyahCount() { return ayahCount; }
    public List<Ayah> getAyahs() { return ayahs; }
}
