package com.islamiccompanion.app.model;

import com.google.gson.annotations.SerializedName;

/** One of the 99 Names of Allah. */
public class DivineName {

    @SerializedName("number")
    private int number;

    @SerializedName("arabic")
    private String arabic;

    @SerializedName("transliteration")
    private String transliteration;

    @SerializedName("meaning_en")
    private String meaningEn;

    public DivineName() {}

    public int getNumber() { return number; }
    public String getArabic() { return arabic; }
    public String getTransliteration() { return transliteration; }
    public String getMeaningEn() { return meaningEn; }
}
