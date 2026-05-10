package com.islamiccompanion.app.ui.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.islamiccompanion.app.R;
import com.islamiccompanion.app.databinding.FragmentMoreBinding;

/** Hub screen linking to all secondary features. */
public class MoreFragment extends Fragment {

    private FragmentMoreBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.cardTasbih.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_more_to_tasbih));

        binding.cardHadith.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_more_to_hadith));

        binding.cardDua.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_more_to_duaList));

        binding.cardNames.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_more_to_names));

        binding.cardCalendar.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_more_to_calendar));

        binding.cardStats.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_more_to_stats));

        binding.cardMosque.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_more_to_mosque));

        binding.cardSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_more_to_settings));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
