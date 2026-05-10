package com.islamiccompanion.app.ui.names;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.islamiccompanion.app.databinding.ItemNameBinding;
import com.islamiccompanion.app.model.DivineName;

public class NamesAdapter extends ListAdapter<DivineName, NamesAdapter.ViewHolder> {

    public NamesAdapter() { super(DIFF); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemNameBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNameBinding b;
        private boolean expanded = false;

        ViewHolder(ItemNameBinding b) { super(b.getRoot()); this.b = b; }

        void bind(DivineName name) {
            b.tvNumber.setText(String.valueOf(name.getNumber()));
            b.tvArabic.setText(name.getArabic());
            b.tvTranslit.setText(name.getTransliteration());
            b.tvMeaning.setText(name.getMeaningEn());
            b.tvMeaning.setVisibility(expanded ? View.VISIBLE : View.GONE);
            b.getRoot().setOnClickListener(v -> {
                expanded = !expanded;
                b.tvMeaning.setVisibility(expanded ? View.VISIBLE : View.GONE);
            });
        }
    }

    private static final DiffUtil.ItemCallback<DivineName> DIFF =
            new DiffUtil.ItemCallback<DivineName>() {
                @Override public boolean areItemsTheSame(@NonNull DivineName a, @NonNull DivineName b) { return a.getNumber() == b.getNumber(); }
                @Override public boolean areContentsTheSame(@NonNull DivineName a, @NonNull DivineName b) { return a.getNumber() == b.getNumber(); }
            };
}
