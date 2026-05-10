package com.islamiccompanion.app.ui.dua;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.islamiccompanion.app.databinding.ItemDuaBinding;
import com.islamiccompanion.app.model.Dua;

public class DuaAdapter extends ListAdapter<Dua, DuaAdapter.ViewHolder> {

    public interface OnDuaClickListener {
        void onClick(Dua dua);
    }

    private final OnDuaClickListener listener;

    public DuaAdapter(OnDuaClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemDuaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemDuaBinding b;
        ViewHolder(ItemDuaBinding b) { super(b.getRoot()); this.b = b; }

        void bind(Dua dua, OnDuaClickListener listener) {
            b.tvDuaTitle.setText(dua.getTitleEn());
            b.tvDuaArabic.setText(dua.getTextArabic());
            b.tvDuaTranslation.setText(dua.getTranslationEn());
            b.getRoot().setOnClickListener(v -> { if (listener != null) listener.onClick(dua); });
        }
    }

    private static final DiffUtil.ItemCallback<Dua> DIFF = new DiffUtil.ItemCallback<Dua>() {
        @Override public boolean areItemsTheSame(@NonNull Dua a, @NonNull Dua b) { return a.getId() == b.getId(); }
        @Override public boolean areContentsTheSame(@NonNull Dua a, @NonNull Dua b) { return a.getId() == b.getId(); }
    };
}
