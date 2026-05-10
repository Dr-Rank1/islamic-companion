package com.islamiccompanion.app.ui.quran;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.islamiccompanion.app.R;
import com.islamiccompanion.app.databinding.BottomSheetAyahOptionsBinding;
import com.islamiccompanion.app.databinding.FragmentQuranReaderBinding;
import com.islamiccompanion.app.model.Ayah;
import com.islamiccompanion.app.model.Surah;

import java.util.Locale;

/**
 * Displays the Arabic text and translation of a single surah.
 * No ads are shown on this screen.
 *
 * Audio streams from EveryAyah.com (free, public):
 *   https://everyayah.com/data/{reciter}/{surah:03d}{ayah:03d}.mp3
 */
public class QuranReaderFragment extends Fragment {

    public static final String ARG_SURAH_NUMBER = "surah_number";

    private FragmentQuranReaderBinding binding;
    private QuranReaderViewModel viewModel;
    private AyahAdapter ayahAdapter;
    private ExoPlayer player;
    private int currentSurahNum;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentQuranReaderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(QuranReaderViewModel.class);

        currentSurahNum = getArguments() != null
                ? getArguments().getInt(ARG_SURAH_NUMBER, 1)
                : 1;

        ayahAdapter = new AyahAdapter(this::showAyahOptions); // tap opens options sheet
        ayahAdapter.setShowTranslation(viewModel.isShowTranslation());
        ayahAdapter.setArabicTextSize(viewModel.getArabicTextSizeSp());
        binding.rvAyahs.setAdapter(ayahAdapter);

        viewModel.loadSurah(currentSurahNum);

        viewModel.getSurah().observe(getViewLifecycleOwner(), surah -> {
            if (surah == null) return;
            binding.collapsingToolbar.setTitle(surah.getNameEnglish());
            ayahAdapter.submitList(surah.getAyahs());
            viewModel.saveProgress(currentSurahNum, 1, surah.getNameEnglish());
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err ->
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show());

        binding.fabPlayAll.setOnClickListener(v -> playFromAyah(1));
        binding.fabStopAudio.setOnClickListener(v -> stopAudio());
    }

    private void showAyahOptions(Ayah ayah) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        BottomSheetAyahOptionsBinding b = BottomSheetAyahOptionsBinding
                .inflate(LayoutInflater.from(requireContext()));
        dialog.setContentView(b.getRoot());

        b.btnPlayAyah.setOnClickListener(v -> {
            dialog.dismiss();
            playFromAyah(ayah.getNumber());
        });

        b.btnCopyAyah.setOnClickListener(v -> {
            dialog.dismiss();
            android.content.ClipboardManager cm =
                    (android.content.ClipboardManager) requireContext()
                    .getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(android.content.ClipData.newPlainText(
                        "Ayah", ayah.getTextArabic() + "\n\n" + ayah.getTranslationEn()));
                Toast.makeText(requireContext(), R.string.copied, Toast.LENGTH_SHORT).show();
            }
        });

        b.btnShareAyah.setOnClickListener(v -> {
            dialog.dismiss();
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT,
                    ayah.getTextArabic() + "\n\n" + ayah.getTranslationEn()
                    + "\n\n— Surah " + currentSurahNum + ":" + ayah.getNumber());
            startActivity(Intent.createChooser(share, getString(R.string.share_ayah)));
        });

        b.btnBookmarkAyah.setOnClickListener(v -> {
            dialog.dismiss();
            Surah surah = viewModel.getSurah().getValue();
            String name = surah != null ? surah.getNameEnglish() : "";
            viewModel.toggleBookmark(currentSurahNum, ayah.getNumber(), name);
            Toast.makeText(requireContext(), R.string.bookmarked, Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    // ---- Audio playback via ExoPlayer ----

    private void playFromAyah(int startAyah) {
        Surah surah = viewModel.getSurah().getValue();
        if (surah == null || surah.getAyahs() == null) return;

        releasePlayer();
        player = new ExoPlayer.Builder(requireContext()).build();

        // Queue all ayahs from startAyah onwards
        for (Ayah a : surah.getAyahs()) {
            if (a.getNumber() < startAyah) continue;
            String url = buildAyahAudioUrl(currentSurahNum, a.getNumber(), viewModel.getReciter());
            player.addMediaItem(MediaItem.fromUri(Uri.parse(url)));
        }

        player.prepare();
        player.play();
        binding.fabStopAudio.setVisibility(View.VISIBLE);
    }

    /**
     * Constructs the EveryAyah.com streaming URL.
     * Example: https://everyayah.com/data/Alafasy_128kbps/001001.mp3
     */
    private String buildAyahAudioUrl(int surah, int ayah, String reciter) {
        return String.format(Locale.US,
                "https://everyayah.com/data/%s/%03d%03d.mp3",
                reciter, surah, ayah);
    }

    private void stopAudio() {
        releasePlayer();
        binding.fabStopAudio.setVisibility(View.GONE);
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onDestroyView() {
        releasePlayer();
        super.onDestroyView();
        binding = null;
    }
}
