package com.islamiccompanion.app.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.islamiccompanion.app.R;
import com.islamiccompanion.app.databinding.FragmentStatsBinding;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        viewModel.getCurrentStreak().observe(getViewLifecycleOwner(), streak ->
                binding.tvCurrentStreak.setText(
                        getString(R.string.stats_current_streak, streak != null ? streak : 0)));

        viewModel.getBestStreak().observe(getViewLifecycleOwner(), best ->
                binding.tvBestStreak.setText(
                        getString(R.string.stats_best_streak, best != null ? best : 0)));

        viewModel.getOnTimePct().observe(getViewLifecycleOwner(), pct ->
                binding.tvOnTimePct.setText(
                        getString(R.string.stats_on_time_pct, pct != null ? pct : 0)));

        viewModel.getTotalLogged().observe(getViewLifecycleOwner(), total ->
                binding.tvTotalPrayers.setText(
                        getString(R.string.stats_total_prayers, total != null ? total : 0)));

        viewModel.getHardestPrayer().observe(getViewLifecycleOwner(), p -> {
            if (p != null && !p.isEmpty()) {
                binding.tvHardestPrayer.setText(getString(R.string.stats_hardest_prayer, p));
                binding.tvHardestPrayer.setVisibility(View.VISIBLE);
            } else {
                binding.tvHardestPrayer.setVisibility(View.GONE);
            }
        });

        viewModel.getWeeklyData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                binding.barChartView.setData(data);
            }
        });

        binding.btnResetStats.setOnClickListener(v -> showResetConfirmDialog());
    }

    private void showResetConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.stats_reset)
                .setMessage(R.string.stats_reset_confirm)
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    viewModel.resetAllStats();
                    com.google.android.material.snackbar.Snackbar.make(
                            binding.getRoot(),
                            R.string.stats_reset_done,
                            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    ).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
