package com.islamiccompanion.app.ui.dua;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.islamiccompanion.app.databinding.FragmentDuaDetailBinding;
import com.islamiccompanion.app.model.Dua;

/**
 * Full-detail view of a single Dua.
 * No ads shown here — per spec: "Do not put any ads on Dua reading screen while a dua is open."
 */
public class DuaDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_DUA_ID       = "dua_id";
    private static final String ARG_DUA_TITLE    = "dua_title";
    private static final String ARG_DUA_ARABIC   = "dua_arabic";
    private static final String ARG_DUA_TRANSLIT = "dua_translit";
    private static final String ARG_DUA_TRANS    = "dua_translation";
    private static final String ARG_DUA_SOURCE   = "dua_source";

    private FragmentDuaDetailBinding binding;

    public static DuaDetailBottomSheet newInstance(Dua dua) {
        DuaDetailBottomSheet f = new DuaDetailBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_DUA_ID,       dua.getId());
        args.putString(ARG_DUA_TITLE,    dua.getTitleEn());
        args.putString(ARG_DUA_ARABIC,   dua.getTextArabic());
        args.putString(ARG_DUA_TRANSLIT, dua.getTransliteration());
        args.putString(ARG_DUA_TRANS,    dua.getTranslationEn());
        args.putString(ARG_DUA_SOURCE,   dua.getSource());
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDuaDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle a = getArguments();
        if (a == null) return;
        binding.tvDuaDetailTitle.setText(a.getString(ARG_DUA_TITLE));
        binding.tvDuaDetailArabic.setText(a.getString(ARG_DUA_ARABIC));
        binding.tvDuaDetailTranslit.setText(a.getString(ARG_DUA_TRANSLIT));
        binding.tvDuaDetailTranslation.setText(a.getString(ARG_DUA_TRANS));
        binding.tvDuaDetailSource.setText(a.getString(ARG_DUA_SOURCE));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
