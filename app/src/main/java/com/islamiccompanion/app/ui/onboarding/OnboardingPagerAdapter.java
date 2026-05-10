package com.islamiccompanion.app.ui.onboarding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.islamiccompanion.app.R;

/** Simple ViewPager2 adapter for the 3 onboarding screens. */
public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.PageHolder> {

    private static final int[] ILLUSTRATIONS = {
            R.drawable.ic_splash_crescent,   // screen 1 — welcome
            R.drawable.ic_qibla,             // screen 2 — location
            R.drawable.ic_notification       // screen 3 — notifications
    };

    private static final int[] TITLES = {
            R.string.onboarding_1_title,
            R.string.onboarding_2_title,
            R.string.onboarding_3_title
    };

    private static final int[] SUBTITLES = {
            R.string.onboarding_1_subtitle,
            R.string.onboarding_2_subtitle,
            R.string.onboarding_3_subtitle
    };

    private final AppCompatActivity activity;

    public OnboardingPagerAdapter(AppCompatActivity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_onboarding_page, parent, false);
        return new PageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder holder, int position) {
        holder.illustration.setImageResource(ILLUSTRATIONS[position]);
        holder.title.setText(TITLES[position]);
        holder.subtitle.setText(SUBTITLES[position]);
    }

    @Override
    public int getItemCount() { return 3; }

    static class PageHolder extends RecyclerView.ViewHolder {
        ImageView illustration;
        TextView title;
        TextView subtitle;

        PageHolder(View v) {
            super(v);
            illustration = v.findViewById(R.id.ivOnboardingIllustration);
            title        = v.findViewById(R.id.tvOnboardingTitle);
            subtitle     = v.findViewById(R.id.tvOnboardingSubtitle);
        }
    }
}
