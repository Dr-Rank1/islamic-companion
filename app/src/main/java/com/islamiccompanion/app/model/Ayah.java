package com.islamiccompanion.app.model;

import com.google.gson.annotations.SerializedName;

/** A single verse of the Quran. */
public class Ayah {

    @SerializedName("number")
    private int number;

    @SerializedName("text_arabic")
    private String textArabic;

    @SerializedName("translation_en")
    private String translationEn;

    @SerializedName("translation_extra")
    private String translationExtra;

    // Runtime-only fields (not in JSON)
    private int surahNumber;
    private boolean isBookmarked;

    public Ayah() {}

    public int getNumber() { return number; }
    public String getTextArabic() { return textArabic; }
    public String getTranslationEn() { return translationEn; }
    public String getTranslationExtra() { return translationExtra; }
    public int getSurahNumber() { return surahNumber; }
    public boolean isBookmarked() { return isBookmarked; }

    public void setSurahNumber(int surahNumber) { this.surahNumber = surahNumber; }
    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }
}
