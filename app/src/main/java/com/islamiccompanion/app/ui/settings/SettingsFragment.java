package com.islamiccompanion.app.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.islamiccompanion.app.R;
import com.islamiccompanion.app.data.prefs.AppPreferences;
import com.islamiccompanion.app.databinding.FragmentSettingsBinding;
import com.islamiccompanion.app.model.City;
import com.islamiccompanion.app.service.AdhanWorker;
import com.islamiccompanion.app.service.PrayerTimeCalculator;
import com.islamiccompanion.app.util.AdHelper;
import com.islamiccompanion.app.util.ThemeHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private AppPreferences prefs;
    private List<City> cities = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = new AppPreferences(requireContext());

        loadCitiesAsync();
        populateCalculationMethodSpinner();
        populateAdhanVoiceSpinner();
        loadCurrentValues();
        wireListeners();
    }

    /** Load the bundled cities.json asset on a background thread, then populate the spinner. */
    private void loadCitiesAsync() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream is = requireContext().getAssets().open("cities.json");
                Gson gson = new Gson();
                JsonObject root = gson.fromJson(
                        new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
                Type listType = new TypeToken<List<City>>() {}.getType();
                cities = gson.fromJson(root.get("cities"), listType);
                if (cities == null) cities = new ArrayList<>();
            } catch (IOException e) {
                cities = new ArrayList<>();
            }

            requireActivity().runOnUiThread(() -> {
                List<String> cityNames = new ArrayList<>();
                cityNames.add("Auto (GPS)");
                for (City c : cities) {
                    cityNames.add(c.getName() + ", " + c.getCountry());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_spinner_item, cityNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerCity.setAdapter(adapter);

                // Restore selected city
                String saved = prefs.getString(AppPreferences.KEY_MANUAL_CITY, "");
                for (int i = 0; i < cities.size(); i++) {
                    City c = cities.get(i);
                    if ((c.getName() + ", " + c.getCountry()).equals(saved)) {
                        binding.spinnerCity.setSelection(i + 1); // +1 for "Auto" at index 0
                        break;
                    }
                }
            });
        });
    }

    private void populateCalculationMethodSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, PrayerTimeCalculator.METHOD_NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCalcMethod.setAdapter(adapter);
    }

    private void populateAdhanVoiceSpinner() {
        String[] voices = {"makkah", "madinah", "aqsa", "beep"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, voices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAdhanVoice.setAdapter(adapter);
    }

    private void loadCurrentValues() {
        // Theme
        String theme = prefs.getTheme();
        switch (theme) {
            case "light":  binding.rbThemeLight.setChecked(true);  break;
            case "dark":   binding.rbThemeDark.setChecked(true);   break;
            default:       binding.rbThemeSystem.setChecked(true); break;
        }

        // Madhab
        if ("hanafi".equals(prefs.getMadhab())) {
            binding.rbMadhabHanafi.setChecked(true);
        } else {
            binding.rbMadhabShafi.setChecked(true);
        }

        // Hijri offset
        binding.sliderHijriOffset.setValue(prefs.getHijriOffset());

        // Prayer adjustments
        binding.sliderAdjFajr.setValue(prefs.getInt(AppPreferences.KEY_ADJ_FAJR, 0));
        binding.sliderAdjDhuhr.setValue(prefs.getInt(AppPreferences.KEY_ADJ_DHUHR, 0));
        binding.sliderAdjAsr.setValue(prefs.getInt(AppPreferences.KEY_ADJ_ASR, 0));
        binding.sliderAdjMaghrib.setValue(prefs.getInt(AppPreferences.KEY_ADJ_MAGHRIB, 0));
        binding.sliderAdjIsha.setValue(prefs.getInt(AppPreferences.KEY_ADJ_ISHA, 0));

        // Calculation method spinner pre-selection
        String method = prefs.getCalculationMethod();
        for (int i = 0; i < PrayerTimeCalculator.METHOD_NAMES.length; i++) {
            if (PrayerTimeCalculator.METHOD_NAMES[i].equals(method)) {
                binding.spinnerCalcMethod.setSelection(i);
                break;
            }
        }

        // Adhan voice spinner pre-selection
        String voice = prefs.getAdhanVoice();
        String[] voices = {"makkah", "madinah", "aqsa", "beep"};
        for (int i = 0; i < voices.length; i++) {
            if (voices[i].equals(voice)) {
                binding.spinnerAdhanVoice.setSelection(i);
                break;
            }
        }

    }

    private void wireListeners() {
        // Theme
        binding.rgTheme.setOnCheckedChangeListener((group, id) -> {
            String theme;
            if (id == R.id.rbThemeLight)      theme = "light";
            else if (id == R.id.rbThemeDark)  theme = "dark";
            else                               theme = "system";
            prefs.putString(AppPreferences.KEY_THEME, theme);
            ThemeHelper.applyTheme(theme);
        });

        // Madhab
        binding.rgMadhab.setOnCheckedChangeListener((group, id) ->
                prefs.putString(AppPreferences.KEY_MADHAB,
                        id == R.id.rbMadhabHanafi ? "hanafi" : "shafi"));

        // Hijri offset
        binding.sliderHijriOffset.addOnChangeListener((s, val, fromUser) ->
                prefs.putInt(AppPreferences.KEY_HIJRI_OFFSET, (int) val));

        // Prayer time adjustments
        binding.sliderAdjFajr.addOnChangeListener((s, v, u)    -> prefs.putInt(AppPreferences.KEY_ADJ_FAJR, (int) v));
        binding.sliderAdjDhuhr.addOnChangeListener((s, v, u)   -> prefs.putInt(AppPreferences.KEY_ADJ_DHUHR, (int) v));
        binding.sliderAdjAsr.addOnChangeListener((s, v, u)     -> prefs.putInt(AppPreferences.KEY_ADJ_ASR, (int) v));
        binding.sliderAdjMaghrib.addOnChangeListener((s, v, u) -> prefs.putInt(AppPreferences.KEY_ADJ_MAGHRIB, (int) v));
        binding.sliderAdjIsha.addOnChangeListener((s, v, u)    -> prefs.putInt(AppPreferences.KEY_ADJ_ISHA, (int) v));

        // Save button
        binding.btnSaveSettings.setOnClickListener(v -> {
            // Persist spinner selections
            prefs.putString(AppPreferences.KEY_CALC_METHOD,
                    (String) binding.spinnerCalcMethod.getSelectedItem());
            prefs.putString(AppPreferences.KEY_ADHAN_VOICE,
                    (String) binding.spinnerAdhanVoice.getSelectedItem());

            // Persist manual city selection
            int cityPos = binding.spinnerCity.getSelectedItemPosition();
            if (cityPos == 0) {
                // "Auto (GPS)" selected — clear manual city
                prefs.putString(AppPreferences.KEY_MANUAL_CITY, "");
                prefs.putString(AppPreferences.KEY_LOCATION_MODE, "auto");
            } else if (!cities.isEmpty() && cityPos - 1 < cities.size()) {
                City selectedCity = cities.get(cityPos - 1);
                prefs.putString(AppPreferences.KEY_MANUAL_CITY,
                        selectedCity.getName() + ", " + selectedCity.getCountry());
                prefs.putString(AppPreferences.KEY_LOCATION_MODE, "manual");
                prefs.saveLastLocation(selectedCity.getLatitude(), selectedCity.getLongitude());
            }

            // Re-schedule prayers with updated settings
            AdhanWorker.scheduleTodaysPrayers(requireContext());

            // Interstitial after leaving Settings (3-minute cooldown enforced in AdHelper)
            if (getActivity() != null) {
                AdHelper.showInterstitialIfReady(requireActivity());
            }
            Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
