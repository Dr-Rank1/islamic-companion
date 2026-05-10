package com.islamiccompanion.app.ui.onboarding;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.islamiccompanion.app.MainActivity;
import com.islamiccompanion.app.R;
import com.islamiccompanion.app.data.prefs.AppPreferences;
import com.islamiccompanion.app.databinding.ActivityOnboardingBinding;
import com.islamiccompanion.app.service.AdhanWorker;
import com.islamiccompanion.app.util.LocationHelper;

/**
 * 3-screen onboarding flow shown on first launch.
 *  Screen 1: Welcome
 *  Screen 2: Location permission — requests ACCESS_FINE_LOCATION, then fetches coordinates
 *  Screen 3: Notification permission
 *
 * If the user denies location, we still advance and the app falls back to the
 * bundled city list / manual selection. Prayer times will work — just less precise
 * until the user grants location or picks a city in Settings.
 */
public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private OnboardingPagerAdapter adapter;

    private final ActivityResultLauncher<String[]> locationLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {
                boolean granted = Boolean.TRUE.equals(r.get(Manifest.permission.ACCESS_FINE_LOCATION));
                if (granted) {
                    // Permission granted — immediately fetch and save the GPS fix
                    fetchAndSaveLocation();
                    goToNextPage();
                } else {
                    // Denied — show a gentle note, then let the user proceed
                    Toast.makeText(this,
                            "Location denied. You can pick a city in Settings.",
                            Toast.LENGTH_LONG).show();
                    goToNextPage();
                }
            });

    private final ActivityResultLauncher<String> notifLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> finishOnboarding());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new OnboardingPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setUserInputEnabled(false);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
                updateButtons(position);
            }
        });

        binding.btnSkip.setOnClickListener(v -> finishOnboarding());
        binding.btnNext.setOnClickListener(v -> handleNextButton());

        updateIndicators(0);
        updateButtons(0);
    }

    private void handleNextButton() {
        int page = binding.viewPager.getCurrentItem();
        switch (page) {
            case 0:
                goToNextPage();
                break;

            case 1:
                // Request location permission
                locationLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
                break;

            case 2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    finishOnboarding();
                }
                break;
        }
    }

    private void goToNextPage() {
        int next = binding.viewPager.getCurrentItem() + 1;
        if (next < adapter.getItemCount()) {
            binding.viewPager.setCurrentItem(next, true);
        }
    }

    private void fetchAndSaveLocation() {
        LocationHelper helper = new LocationHelper(this);
        helper.getLocation(this, new LocationHelper.LocationCallback2() {
            @Override
            public void onLocationReceived(double lat, double lng) {
                new AppPreferences(OnboardingActivity.this).saveLastLocation(lat, lng);
            }
            @Override
            public void onLocationFailed(String reason) {
                // Silently ignore — app will use city selection or defaults
            }
        });
    }

    private void finishOnboarding() {
        new AppPreferences(this).setFirstLaunchDone();
        AdhanWorker.scheduleTodaysPrayers(this);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void updateIndicators(int position) {
        binding.indicator0.setSelected(position == 0);
        binding.indicator1.setSelected(position == 1);
        binding.indicator2.setSelected(position == 2);
    }

    private void updateButtons(int position) {
        boolean isLast = position == adapter.getItemCount() - 1;
        binding.btnSkip.setVisibility(isLast ? View.GONE : View.VISIBLE);
        binding.btnNext.setText(isLast
                ? getString(R.string.onboarding_get_started)
                : getString(R.string.onboarding_next));
    }
}
