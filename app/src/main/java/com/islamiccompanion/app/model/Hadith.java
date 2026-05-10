package com.islamiccompanion.app.model;

import com.google.gson.annotations.SerializedName;

/** A single Hadith from the bundled collection. */
public class Hadith {

    @SerializedName("id")
    private int id;

    @SerializedName("collection")
    private String collection;

    @SerializedName("number")
    private String number;

    @SerializedName("narrator")
    private String narrator;

    @SerializedName("text_en")
    private String textEn;

    @SerializedName("text_arabic")
    private String textArabic;

    public Hadith() {}

    public int getId() { return id; }
    public String getCollection() { return collection; }
    public String getNumber() { return number; }
    public String getNarrator() { return narrator; }
    public String getTextEn() { return textEn; }
    public String getTextArabic() { return textArabic; }
}
