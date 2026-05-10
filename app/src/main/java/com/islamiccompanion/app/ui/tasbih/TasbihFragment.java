package com.islamiccompanion.app.ui.tasbih;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.islamiccompanion.app.R;
import com.islamiccompanion.app.databinding.FragmentTasbihBinding;
import com.islamiccompanion.app.util.AdHelper;

public class TasbihFragment extends Fragment {

    private FragmentTasbihBinding binding;
    private TasbihViewModel viewModel;
    private Vibrator vibrator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTasbihBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TasbihViewModel.class);

        initVibrator();

        viewModel.getCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvCount.setText(String.valueOf(count));
            Integer target = viewModel.getTarget().getValue();
            if (target != null && target > 0) {
                binding.beadView.setFilledBeads(count % target == 0 && count > 0 ? target : count % target);
            } else {
                binding.beadView.setFilledBeads(count);
            }
        });

        viewModel.getTarget().observe(getViewLifecycleOwner(), target -> {
            binding.tvTarget.setText(getString(R.string.tasbih_target, target));
            binding.beadView.setTotalBeads(Math.min(target, 99));
        });

        viewModel.getDhikrName().observe(getViewLifecycleOwner(),
                name -> binding.tvDhikrName.setText(name));

        viewModel.getDhikrArabic().observe(getViewLifecycleOwner(),
                ar -> binding.tvDhikrArabic.setText(ar));

        viewModel.getDailyTotal().observe(getViewLifecycleOwner(), total ->
                binding.tvDailyTotal.setText(getString(R.string.tasbih_today_total, total)));

        viewModel.isTargetReached().observe(getViewLifecycleOwner(), reached -> {
            if (Boolean.TRUE.equals(reached)) {
                vibrateCompletion();
                // Interstitial after cycle (cooldown enforced)
                if (getActivity() != null) AdHelper.showInterstitialIfReady(requireActivity());
                com.google.android.material.snackbar.Snackbar.make(
                        binding.getRoot(),
                        getString(R.string.tasbih_target_reached),
                        com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show();
            }
        });

        // Tap button with bounce animation
        binding.btnTap.setOnClickListener(v -> {
            viewModel.tap();
            vibrateClick();
            animateCounterBounce();
        });

        binding.btnReset.setOnClickListener(v -> viewModel.reset());
        binding.btnNextDhikr.setOnClickListener(v -> viewModel.nextPreset());
    }

    private void animateCounterBounce() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.tvCount, "scaleX", 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.tvCount, "scaleY", 1f, 1.15f, 1f);
        scaleX.setDuration(150);
        scaleY.setDuration(150);
        scaleX.setInterpolator(new OvershootInterpolator(3f));
        scaleY.setInterpolator(new OvershootInterpolator(3f));
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.start();
    }

    private void initVibrator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager)
                    requireContext().getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE);
            if (vm != null) vibrator = vm.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) requireContext()
                    .getSystemService(android.content.Context.VIBRATOR_SERVICE);
        }
    }

    private void vibrateClick() {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(20);
        }
    }

    private void vibrateCompletion() {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Spec: 50ms-pause-100ms-pause-50ms celebration pattern
            vibrator.vibrate(VibrationEffect.createWaveform(
                    new long[]{0, 50, 50, 100, 50, 50}, -1));
        } else {
            vibrator.vibrate(new long[]{0, 50, 50, 100, 50, 50}, -1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
