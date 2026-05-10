package com.islamiccompanion.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.islamiccompanion.app.R;
import com.islamiccompanion.app.data.prefs.AppPreferences;
import com.islamiccompanion.app.util.LocationHelper;
import com.islamiccompanion.app.util.PermissionHelper;
import com.islamiccompanion.app.data.db.entity.PrayerLogEntry;
import com.islamiccompanion.app.databinding.FragmentHomeBinding;
import com.islamiccompanion.app.databinding.ItemPrayerPillBinding;
import com.islamiccompanion.app.util.AdHelper;

import java.util.Map;

/**
 * Home screen — rich dashboard with hero prayer card, prayer pills, quick actions,
 * hadith preview, streak, and contextual prayer-tracking prompt.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding.swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.primary));
        binding.swipeRefresh.setOnRefreshListener(() -> {
            // If location permission is held, refresh the GPS fix first, then reload prayer times
            if (PermissionHelper.hasLocationPermission(requireContext())) {
                refreshLocationThenPrayerTimes();
            } else {
                viewModel.refresh();
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        setupPrayerPillNames();
        observeViewModel();
        setupQuickActions();
        setupPrayerPrompt();
    }

    private void setupPrayerPillNames() {
        binding.pillFajr.tvPillName.setText(R.string.home_fajr);
        binding.pillFajr.tvPillNameAr.setText(R.string.home_fajr_ar);
        binding.pillSunrise.tvPillName.setText(R.string.home_sunrise);
        binding.pillSunrise.tvPillNameAr.setText(R.string.home_sunrise_ar);
        binding.pillDhuhr.tvPillName.setText(R.string.home_dhuhr);
        binding.pillDhuhr.tvPillNameAr.setText(R.string.home_dhuhr_ar);
        binding.pillAsr.tvPillName.setText(R.string.home_asr);
        binding.pillAsr.tvPillNameAr.setText(R.string.home_asr_ar);
        binding.pillMaghrib.tvPillName.setText(R.string.home_maghrib);
        binding.pillMaghrib.tvPillNameAr.setText(R.string.home_maghrib_ar);
        binding.pillIsha.tvPillName.setText(R.string.home_isha);
        binding.pillIsha.tvPillNameAr.setText(R.string.home_isha_ar);
    }

    private void observeViewModel() {
        viewModel.getPrayerTimes().observe(getViewLifecycleOwner(), times -> {
            if (times == null) return;
            binding.pillFajr.tvPillTime.setText(times.getOrDefault("Fajr", "--:--"));
            binding.pillSunrise.tvPillTime.setText(times.getOrDefault("Sunrise", "--:--"));
            binding.pillDhuhr.tvPillTime.setText(times.getOrDefault("Dhuhr", "--:--"));
            binding.pillAsr.tvPillTime.setText(times.getOrDefault("Asr", "--:--"));
            binding.pillMaghrib.tvPillTime.setText(times.getOrDefault("Maghrib", "--:--"));
            binding.pillIsha.tvPillTime.setText(times.getOrDefault("Isha", "--:--"));
        });

        viewModel.getNextPrayerName().observe(getViewLifecycleOwner(), name -> {
            if (name == null) return;
            binding.tvNextPrayerName.setText(name);
            binding.tvNextPrayerArabic.setText(getArabicNameFor(name));
            highlightPill(name);
        });

        viewModel.getCountdown().observe(getViewLifecycleOwner(), cd ->
                binding.tvCountdown.setText(cd));

        viewModel.getNextPrayerTime().observe(getViewLifecycleOwner(), t ->
                binding.tvNextPrayerTime.setText(
                        t != null ? getString(R.string.home_at_time, t) : ""));

        viewModel.getHijriDate().observe(getViewLifecycleOwner(), d ->
                binding.tvHijriDate.setText(d));

        viewModel.getLocationName().observe(getViewLifecycleOwner(), loc ->
                binding.chipLocation.setText(
                        loc != null ? "📍 " + loc : getString(R.string.home_location_unknown)));

        viewModel.getHadithPreview().observe(getViewLifecycleOwner(), preview ->
                binding.tvHadithPreview.setText(preview));

        viewModel.getStreakCount().observe(getViewLifecycleOwner(), streak -> {
            if (streak != null && streak > 0) {
                binding.cardStreak.setVisibility(View.VISIBLE);
                binding.tvStreakCount.setText(getString(R.string.streak_days, streak));
            } else {
                binding.cardStreak.setVisibility(View.GONE);
            }
        });

        viewModel.getBestStreak().observe(getViewLifecycleOwner(), best -> {
            if (best != null && best > 0) {
                binding.tvStreakBest.setText(getString(R.string.streak_best, best));
            }
        });

        viewModel.getPrayerPrompt().observe(getViewLifecycleOwner(), prompt -> {
            if (prompt != null && !prompt.isEmpty()) {
                binding.cardPrayerPrompt.setVisibility(View.VISIBLE);
                binding.tvPrayerPromptText.setText(
                        getString(R.string.prayer_log_prompt, prompt));
                setupPrayerPromptButtons(prompt);
            } else {
                binding.cardPrayerPrompt.setVisibility(View.GONE);
            }
        });
    }

    private void setupPrayerPromptButtons(String prayer) {
        binding.btnPrayedOnTime.setOnClickListener(v ->
                logAndDismissPrompt(prayer, PrayerLogEntry.ON_TIME));
        binding.btnPrayedLate.setOnClickListener(v ->
                logAndDismissPrompt(prayer, PrayerLogEntry.LATE));
        binding.btnPrayedMissed.setOnClickListener(v ->
                logAndDismissPrompt(prayer, PrayerLogEntry.MISSED));
    }

    private void logAndDismissPrompt(String prayer, String status) {
        viewModel.logPrayer(prayer, status);
        binding.cardPrayerPrompt.setVisibility(View.GONE);
        Toast.makeText(requireContext(), R.string.prayer_log_saved, Toast.LENGTH_SHORT).show();
    }

    private void setupPrayerPrompt() {
        // Prompt is managed by ViewModel based on the last passed prayer
    }

    private void setupQuickActions() {
        binding.cardQuickQuran.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_quranList));

        binding.cardQuickQibla.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_qibla));

        binding.cardQuickTasbih.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_tasbih));

        binding.cardQuickDua.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_duaList));

        binding.cardHadith.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_hadith));

        binding.cardStreak.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_stats));
    }

    /** Highlight the pill for the upcoming prayer and dim passed ones. */
    private void highlightPill(String nextName) {
        String[] order = {"Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"};
        ItemPrayerPillBinding[] pills = {
                binding.pillFajr, binding.pillSunrise, binding.pillDhuhr,
                binding.pillAsr, binding.pillMaghrib, binding.pillIsha
        };

        boolean found = false;
        for (int i = 0; i < order.length; i++) {
            boolean isNext = order[i].equals(nextName);
            boolean isPassed = !found && !isNext;
            if (isNext) found = true;

            int bgColor = isNext
                    ? ContextCompat.getColor(requireContext(), R.color.prayer_pill_next)
                    : ContextCompat.getColor(requireContext(), R.color.prayer_pill_default);

            pills[i].getRoot().setCardBackgroundColor(bgColor);
            float alpha = isPassed ? 0.45f : 1.0f;
            pills[i].tvPillName.setAlpha(alpha);
            pills[i].tvPillNameAr.setAlpha(alpha);
            pills[i].tvPillTime.setAlpha(alpha);

            int nameColor = isNext
                    ? ContextCompat.getColor(requireContext(), R.color.prayer_next_text)
                    : ContextCompat.getColor(requireContext(), R.color.text_secondary);
            pills[i].tvPillName.setTextColor(nameColor);
            pills[i].tvPillTime.setTextColor(isNext
                    ? ContextCompat.getColor(requireContext(), R.color.prayer_next_text)
                    : ContextCompat.getColor(requireContext(), R.color.text_primary));
        }
    }

    private String getArabicNameFor(String name) {
        switch (name) {
            case "Fajr":    return getString(R.string.home_fajr_ar);
            case "Sunrise": return getString(R.string.home_sunrise_ar);
            case "Dhuhr":   return getString(R.string.home_dhuhr_ar);
            case "Asr":     return getString(R.string.home_asr_ar);
            case "Maghrib": return getString(R.string.home_maghrib_ar);
            case "Isha":    return getString(R.string.home_isha_ar);
            default:        return "";
        }
    }

    /**
     * Fetch a fresh GPS fix, save it, then reload prayer times.
     * Called from pull-to-refresh and from onResume if no location was ever saved.
     */
    private void refreshLocationThenPrayerTimes() {
        LocationHelper helper = new LocationHelper(requireContext());
        helper.getLocation(requireContext(), new LocationHelper.LocationCallback2() {
            @Override
            public void onLocationReceived(double lat, double lng) {
                new AppPreferences(requireContext()).saveLastLocation(lat, lng);
                viewModel.refresh();
                if (binding != null) binding.swipeRefresh.setRefreshing(false);
            }
            @Override
            public void onLocationFailed(String reason) {
                viewModel.refresh();
                if (binding != null) binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
