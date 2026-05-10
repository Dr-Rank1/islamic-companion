package com.islamiccompanion.app.model;

import com.google.gson.annotations.SerializedName;

/** A supplication (Dua) from the bundled collection. */
public class Dua {

    @SerializedName("id")
    private int id;

    @SerializedName("category")
    private String category;

    @SerializedName("title_en")
    private String titleEn;

    @SerializedName("text_arabic")
    private String textArabic;

    @SerializedName("transliteration")
    private String transliteration;

    @SerializedName("translation_en")
    private String translationEn;

    @SerializedName("source")
    private String source;

    public Dua() {}

    public int getId() { return id; }
    public String getCategory() { return category; }
    public String getTitleEn() { return titleEn; }
    public String getTextArabic() { return textArabic; }
    public String getTransliteration() { return transliteration; }
    public String getTranslationEn() { return translationEn; }
    public String getSource() { return source; }
}
