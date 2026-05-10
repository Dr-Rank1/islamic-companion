package com.islamiccompanion.app.ui.quran;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.islamiccompanion.app.R;
import com.islamiccompanion.app.databinding.FragmentQuranListBinding;
import com.islamiccompanion.app.model.Surah;
import com.islamiccompanion.app.util.AdHelper;

public class QuranListFragment extends Fragment {

    private FragmentQuranListBinding binding;
    private QuranListViewModel viewModel;
    private SurahAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentQuranListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(QuranListViewModel.class);

        adapter = new SurahAdapter(this::onSurahSelected);
        binding.rvSurahs.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSurahs.setAdapter(adapter);

        viewModel.getSurahs().observe(getViewLifecycleOwner(), surahs -> {
            binding.progressBar.setVisibility(View.GONE);
            adapter.submitList(surahs);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.tvError.setVisibility(View.VISIBLE);
            binding.tvError.setText(err);
        });
    }

    private void onSurahSelected(Surah surah) {
        // Show interstitial before opening reader (3-minute cooldown enforced in AdHelper)
        if (getActivity() != null) {
            AdHelper.showInterstitialIfReady(requireActivity());
        }
        Bundle args = new Bundle();
        args.putInt(QuranReaderFragment.ARG_SURAH_NUMBER, surah.getNumber());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_quranList_to_quranReader, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
