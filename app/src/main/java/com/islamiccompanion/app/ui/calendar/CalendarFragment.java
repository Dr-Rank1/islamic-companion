package com.islamiccompanion.app.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.islamiccompanion.app.databinding.FragmentCalendarBinding;
import com.islamiccompanion.app.util.HijriDateUtil;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private CalendarViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        viewModel.getTodayString().observe(getViewLifecycleOwner(), s ->
                binding.tvHijriToday.setText(s));

        viewModel.getCurrentHijri().observe(getViewLifecycleOwner(), hijri -> {
            if (hijri == null || hijri.length < 3) return;
            String monthName = HijriDateUtil.getMonthNameEn(hijri[1]);
            binding.tvMonthHeader.setText(monthName + " " + hijri[0]);

            // Check for Islamic events this day
            String event = HijriDateUtil.getIslamicEvent(hijri[1], hijri[2]);
            if (event != null) {
                binding.tvIslamicEvent.setVisibility(View.VISIBLE);
                binding.tvIslamicEvent.setText(event);
            } else {
                binding.tvIslamicEvent.setVisibility(View.GONE);
            }
        });

        binding.btnPrevMonth.setOnClickListener(v -> viewModel.prevMonth());
        binding.btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
