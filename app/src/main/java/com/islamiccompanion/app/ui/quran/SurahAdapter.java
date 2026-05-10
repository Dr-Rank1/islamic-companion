package com.islamiccompanion.app.ui.quran;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.islamiccompanion.app.databinding.ItemSurahBinding;
import com.islamiccompanion.app.model.Surah;

import java.util.Locale;

public class SurahAdapter extends ListAdapter<Surah, SurahAdapter.ViewHolder> {

    public interface OnSurahClickListener {
        void onSurahClick(Surah surah);
    }

    private final OnSurahClickListener listener;

    public SurahAdapter(OnSurahClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSurahBinding b = ItemSurahBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSurahBinding b;

        ViewHolder(ItemSurahBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(Surah surah, OnSurahClickListener listener) {
            b.tvSurahNumber.setText(String.valueOf(surah.getNumber()));
            b.tvSurahNameArabic.setText(surah.getNameArabic());
            b.tvSurahNameEnglish.setText(surah.getNameEnglish());
            b.tvSurahMeta.setText(String.format(Locale.getDefault(),
                    "%s · %d ayahs", surah.getRevelationType(), surah.getAyahCount()));
            b.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onSurahClick(surah);
            });
        }
    }

    private static final DiffUtil.ItemCallback<Surah> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Surah>() {
                @Override
                public boolean areItemsTheSame(@NonNull Surah a, @NonNull Surah b) {
                    return a.getNumber() == b.getNumber();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Surah a, @NonNull Surah b) {
                    return a.getNumber() == b.getNumber();
                }
            };
}
