package com.islamiccompanion.app.ui.hadith;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.islamiccompanion.app.R;
import com.islamiccompanion.app.databinding.FragmentHadithBinding;
import com.islamiccompanion.app.model.Hadith;

public class HadithFragment extends Fragment {

    private FragmentHadithBinding binding;
    private HadithViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHadithBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HadithViewModel.class);

        viewModel.getHadith().observe(getViewLifecycleOwner(), hadith -> {
            if (hadith == null) return;
            binding.tvHadithCollection.setText(
                    hadith.getCollection() + " — Hadith #" + hadith.getNumber());
            binding.tvHadithNarrator.setText(
                    getString(R.string.hadith_narrator, hadith.getNarrator()));
            binding.tvHadithArabic.setText(hadith.getTextArabic());
            binding.tvHadithEnglish.setText(hadith.getTextEn());

            binding.btnShareHadith.setOnClickListener(v -> shareHadith(hadith));
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            binding.tvHadithEnglish.setText(err);
        });
    }

    private void shareHadith(Hadith hadith) {
        String text = hadith.getTextArabic() + "\n\n"
                + hadith.getTextEn() + "\n\n"
                + "— " + hadith.getCollection() + " #" + hadith.getNumber();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(share, getString(R.string.share_hadith)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
