package com.islamiccompanion.app.ui.names;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.GridLayoutManager;

import com.islamiccompanion.app.databinding.FragmentNamesBinding;

public class NamesFragment extends Fragment {

    private FragmentNamesBinding binding;
    private NamesViewModel viewModel;
    private NamesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNamesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NamesViewModel.class);
        adapter = new NamesAdapter();
        binding.rvNames.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvNames.setAdapter(adapter);
        viewModel.getNames().observe(getViewLifecycleOwner(), names -> {
            adapter.submitList(names);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
