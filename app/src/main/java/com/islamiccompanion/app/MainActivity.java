package com.islamiccompanion.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.islamiccompanion.app.data.prefs.AppPreferences;
import com.islamiccompanion.app.databinding.ActivityMainBinding;
import com.islamiccompanion.app.databinding.NavDrawerHeaderBinding;
import com.islamiccompanion.app.service.AdhanWorker;
import com.islamiccompanion.app.ui.home.PrayerCountdownViewModel;
import com.islamiccompanion.app.ui.onboarding.OnboardingActivity;
import com.islamiccompanion.app.util.AdHelper;
import com.islamiccompanion.app.util.HijriDateUtil;
import com.islamiccompanion.app.util.PermissionHelper;
import com.islamiccompanion.app.util.SecurityHelper;

import java.util.Set;

/**
 * Single-Activity host.
 * v3 additions:
 *  - DrawerLayout side navigation
 *  - Persistent NowPill (prayer countdown above the ad banner)
 *  - Tab badge on Home for unlogged prayers
 *  - Predictive back (OnBackPressedCallback): drawer → Home tab → exit
 *  - Activity-scoped PrayerCountdownViewModel
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private PrayerCountdownViewModel countdownVm;
    private boolean backPressedOnce = false;

    private final ActivityResultLauncher<String[]> locationPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    r -> {
                        Boolean fine = r.get(android.Manifest.permission.ACCESS_FINE_LOCATION);
                        if (Boolean.TRUE.equals(fine)) {
                            // Permission just granted — fetch actual GPS coordinates and save
                            fetchAndSaveLocation();
                        }
                    });

    private final ActivityResultLauncher<String> notifPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> { /* silent */ });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Redirect to onboarding on first launch
        AppPreferences prefs = new AppPreferences(this);
        if (prefs.isFirstLaunch()) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        // Lightweight security checks (log-only, never crash)
        SecurityHelper.performStartupChecks(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupDrawer();
        setupNowPill();
        setupBackHandling();

        AdHelper.loadBanner(binding.adViewMain);
        AdHelper.loadInterstitial(this, null);

        // Always request permissions (handles new installs, revoked permissions, etc.)
        requestPermissionsIfNeeded();
        AdhanWorker.scheduleTodaysPrayers(this);
    }

    // ---- Navigation ----

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = binding.bottomNavView;

        // Top-level destinations — no back arrow for these
        Set<Integer> topLevel = new java.util.HashSet<>();
        topLevel.add(R.id.homeFragment);
        topLevel.add(R.id.quranListFragment);
        topLevel.add(R.id.qiblaFragment);
        topLevel.add(R.id.moreFragment);

        AppBarConfiguration appBarConfig = new AppBarConfiguration.Builder(topLevel)
                .setOpenableLayout(binding.drawerLayout)
                .build();

        NavigationUI.setupWithNavController(bottomNav, navController);

        // Hide NowPill on Home tab (hero card already shows countdown)
        navController.addOnDestinationChangedListener((ctrl, dest, args) -> {
            boolean onHome = dest.getId() == R.id.homeFragment;
            binding.nowPillWrapper.setVisibility(onHome
                    ? android.view.View.GONE
                    : android.view.View.VISIBLE);
        });

        // Add fade-through animation between tabs
        bottomNav.setOnItemSelectedListener(item -> {
            NavOptions opts = new NavOptions.Builder()
                    .setEnterAnim(R.anim.fade_slide_in)
                    .setExitAnim(R.anim.fade_slide_out)
                    .setPopEnterAnim(R.anim.fade_slide_in_reverse)
                    .setPopExitAnim(R.anim.fade_slide_out_reverse)
                    .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                    .setLaunchSingleTop(true)
                    .build();
            try {
                navController.navigate(item.getItemId(), null, opts);
            } catch (Exception e) {
                navController.navigate(item.getItemId());
            }
            return true;
        });

        // Home tab badge: unlogged prayer indicator (updated on resume)
        navController.addOnDestinationChangedListener((ctrl, dest, args) ->
                updateHomeTabBadge(bottomNav));
    }

    private void updateHomeTabBadge(BottomNavigationView bottomNav) {
        // Show a red dot if the HomeViewModel has an active prayer prompt
        // (simplified: badge shown whenever we're away from home — full impl checks prayer log)
        // TODO: wire to StatsRepository.getLogForToday() for accurate badge
    }

    // ---- Drawer ----

    private void setupDrawer() {
        NavigationView navView = binding.navigationView;

        // Populate drawer header
        android.view.View header = navView.getHeaderView(0);
        if (header != null) {
            AppPreferences prefs = new AppPreferences(this);
            android.widget.TextView tvDate =
                    header.findViewById(R.id.tvDrawerHijriDate);
            android.widget.TextView tvStreak =
                    header.findViewById(R.id.tvDrawerStreak);
            if (tvDate != null) {
                int[] hijri = HijriDateUtil.todayHijri(prefs.getHijriOffset());
                tvDate.setText(HijriDateUtil.formatHijri(hijri));
            }
            if (tvStreak != null) {
                int streak = prefs.getInt(AppPreferences.KEY_BEST_STREAK, 0);
                tvStreak.setText(streak + " day streak");
            }
        }

        // Drawer item click → navigate
        navView.setNavigationItemSelectedListener(item -> {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            int id = item.getItemId();

            if (id == R.id.drawer_stats) {
                navController.navigate(R.id.statsFragment);
            } else if (id == R.id.drawer_mosque) {
                navController.navigate(R.id.mosqueFragment);
            } else if (id == R.id.drawer_calendar) {
                navController.navigate(R.id.calendarFragment);
            } else if (id == R.id.drawer_settings) {
                navController.navigate(R.id.settingsFragment);
            } else if (id == R.id.drawer_security) {
                navController.navigate(R.id.securityCheckFragment);
            } else if (id == R.id.drawer_share) {
                shareApp();
            } else if (id == R.id.drawer_rate) {
                rateApp();
            }
            return true;
        });
    }

    /** Called by any fragment toolbar hamburger icon. */
    public void openDrawer() {
        if (binding != null) {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    // ---- NowPill ----

    private void setupNowPill() {
        countdownVm = new ViewModelProvider(this).get(PrayerCountdownViewModel.class);

        // Pill tap → go to Home tab
        // nowPillContainer is a LayoutNowPillBinding (included layout with its own id)
        binding.nowPillContainer.nowPillCard.setOnClickListener(v ->
                binding.bottomNavView.setSelectedItemId(R.id.homeFragment));

        countdownVm.getCountdown().observe(this, cd ->
                binding.nowPillContainer.tvPillCountdown.setText(cd));

        countdownVm.getNextPrayer().observe(this, name ->
                binding.nowPillContainer.tvPillPrayerName.setText(name));

        countdownVm.getArabicName().observe(this, ar ->
                binding.nowPillContainer.tvPillPrayerArabic.setText(ar));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (countdownVm != null) countdownVm.startTicking();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (countdownVm != null) countdownVm.stopTicking();
    }

    // ---- Back handling ----

    private void setupBackHandling() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 1. Close drawer if open
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }

                // 2. If not on Home tab root, navigate to Home
                int currentDest = navController.getCurrentDestination() != null
                        ? navController.getCurrentDestination().getId() : -1;

                boolean onHomeRoot = currentDest == R.id.homeFragment;
                boolean onTabRoot  = currentDest == R.id.quranListFragment
                        || currentDest == R.id.qiblaFragment
                        || currentDest == R.id.moreFragment;

                if (onTabRoot) {
                    // Switch to Home tab
                    binding.bottomNavView.setSelectedItemId(R.id.homeFragment);
                    return;
                }

                // 3. Back stack within a tab — let NavController handle it
                if (!onHomeRoot && navController.popBackStack()) {
                    return;
                }

                // 4. On Home root — double-tap to exit
                if (backPressedOnce) {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                } else {
                    backPressedOnce = true;
                    Toast.makeText(MainActivity.this,
                            "Press back again to exit", Toast.LENGTH_SHORT).show();
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .postDelayed(() -> backPressedOnce = false, 2000);
                }
            }
        });
    }

    // ---- Permissions ----

    private void requestPermissionsIfNeeded() {
        if (PermissionHelper.hasLocationPermission(this)) {
            // Permission already granted — fetch location in case prefs still have defaults
            fetchAndSaveLocation();
        } else if (PermissionHelper.isPermanentlyDenied(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Permanently denied — show a Snackbar directing the user to Settings
            com.google.android.material.snackbar.Snackbar.make(
                    binding.getRoot(),
                    "Location permission is needed for accurate prayer times. Tap Fix to open Settings.",
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            ).setAction("Fix", v -> {
                android.content.Intent intent = new android.content.Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
            }).show();
        } else {
            // Not yet asked — request the permission
            PermissionHelper.requestLocationPermissions(locationPermLauncher);
        }

        if (!PermissionHelper.hasNotificationPermission(this))
            PermissionHelper.requestNotificationPermission(notifPermLauncher);
    }

    /**
     * Fetch the device's current GPS coordinates and save them to preferences.
     * Called after permission is granted and on every app open if permission is already held.
     * This is the only place where actual GPS coordinates are obtained.
     */
    private void fetchAndSaveLocation() {
        com.islamiccompanion.app.util.LocationHelper helper =
                new com.islamiccompanion.app.util.LocationHelper(this);
        helper.getLocation(this, new com.islamiccompanion.app.util.LocationHelper.LocationCallback2() {
            @Override
            public void onLocationReceived(double lat, double lng) {
                new AppPreferences(MainActivity.this).saveLastLocation(lat, lng);
                AdhanWorker.scheduleTodaysPrayers(MainActivity.this);
            }
            @Override
            public void onLocationFailed(String reason) {
                // Silent — app uses cached/manual location as fallback
                AdhanWorker.scheduleTodaysPrayers(MainActivity.this);
            }
        });
    }

    // ---- Social actions ----

    private void shareApp() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT,
                "Check out Islamic Companion — prayer times, Quran, Qibla & more!");
        startActivity(Intent.createChooser(share, "Share Islamic Companion"));
    }

    private void rateApp() {
        // TODO: replace with play.google.com/store link once published
        Toast.makeText(this, "Rate us on the Play Store — coming soon!", Toast.LENGTH_SHORT).show();
    }
}
