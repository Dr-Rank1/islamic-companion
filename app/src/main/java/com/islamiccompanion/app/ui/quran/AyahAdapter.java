package com.islamiccompanion.app.ui.quran;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.islamiccompanion.app.databinding.ItemAyahBinding;
import com.islamiccompanion.app.model.Ayah;

public class AyahAdapter extends ListAdapter<Ayah, AyahAdapter.ViewHolder> {

    public interface OnAyahClickListener {
        void onAyahClick(Ayah ayah);
    }

    private final OnAyahClickListener clickListener;
    private boolean showTranslation = true;
    private float arabicTextSizeSp = 22f;

    public AyahAdapter(OnAyahClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    public void setShowTranslation(boolean show) {
        this.showTranslation = show;
        notifyDataSetChanged();
    }

    public void setArabicTextSize(float sp) {
        this.arabicTextSizeSp = sp;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAyahBinding b = ItemAyahBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), clickListener, showTranslation, arabicTextSizeSp);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAyahBinding b;

        ViewHolder(ItemAyahBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Ayah ayah, OnAyahClickListener listener,
                  boolean showTranslation, float arabicSizeSp) {
            b.tvAyahNumber.setText(String.valueOf(ayah.getNumber()));
            b.tvArabicText.setText(ayah.getTextArabic());
            b.tvArabicText.setTextSize(arabicSizeSp);
            b.tvTranslation.setVisibility(showTranslation ? View.VISIBLE : View.GONE);
            b.tvTranslation.setText(ayah.getTranslationEn());

            // Tap to open ayah options (play, copy, share, bookmark)
            b.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onAyahClick(ayah);
            });
        }
    }

    private static final DiffUtil.ItemCallback<Ayah> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Ayah>() {
                @Override
                public boolean areItemsTheSame(@NonNull Ayah a, @NonNull Ayah b) {
                    return a.getSurahNumber() == b.getSurahNumber()
                            && a.getNumber() == b.getNumber();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Ayah a, @NonNull Ayah b) {
                    return areItemsTheSame(a, b);
                }
            };
}
