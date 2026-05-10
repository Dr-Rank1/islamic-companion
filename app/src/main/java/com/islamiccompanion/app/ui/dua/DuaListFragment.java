package com.islamiccompanion.app.ui.dua;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.islamiccompanion.app.databinding.FragmentDuaListBinding;
import com.islamiccompanion.app.model.Dua;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shows Duas grouped by category. No ads while a Dua is open
 * (the detail view is a BottomSheet; the list itself may carry a banner).
 */
public class DuaListFragment extends Fragment {

    private FragmentDuaListBinding binding;
    private DuaListViewModel viewModel;
    private DuaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDuaListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DuaListViewModel.class);

        adapter = new DuaAdapter(this::showDuaDetail);
        binding.rvDuas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDuas.setAdapter(adapter);

        viewModel.getDuasByCategory().observe(getViewLifecycleOwner(), map -> {
            List<Dua> flat = new ArrayList<>();
            for (Map.Entry<String, List<Dua>> e : map.entrySet()) {
                flat.addAll(e.getValue());
            }
            adapter.submitList(flat);
        });
    }

    private void showDuaDetail(Dua dua) {
        DuaDetailBottomSheet sheet = DuaDetailBottomSheet.newInstance(dua);
        sheet.show(getChildFragmentManager(), "dua_detail");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
