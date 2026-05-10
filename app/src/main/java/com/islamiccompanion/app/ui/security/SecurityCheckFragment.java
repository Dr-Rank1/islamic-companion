package com.islamiccompanion.app.ui.security;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.islamiccompanion.app.BuildConfig;
import com.islamiccompanion.app.R;
import com.islamiccompanion.app.databinding.FragmentSecurityCheckBinding;
import com.islamiccompanion.app.util.SecurityHelper;

/** Read-only security diagnostic screen. Accessible via side drawer → Security Check. */
public class SecurityCheckFragment extends Fragment {

    private FragmentSecurityCheckBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSecurityCheckBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.toolbar.setNavigationOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
        populateChecks();
    }

    private void populateChecks() {
        setCheck(binding.rowPrefsEncrypted, binding.icPrefsEncrypted,
                SecurityHelper.isPrefsEncrypted(requireContext()),
                "Preferences encrypted (AES-256-GCM)",
                "Preferences in plaintext — check upgrade");

        setCheck(binding.rowTls, binding.icTls,
                SecurityHelper.isTlsEnforced(),
                "Network uses TLS only",
                "Cleartext traffic may be permitted");

        setCheck(binding.rowDebugBuild, binding.icDebugBuild,
                !SecurityHelper.isDebugBuild(),
                "Release build",
                "Running a debug build — do not publish");

        setCheck(binding.rowDebugger, binding.icDebugger,
                !SecurityHelper.isDebuggerAttached(),
                "No debugger attached",
                "Debugger is currently connected");

        setCheck(binding.rowRooted, binding.icRooted,
                !SecurityHelper.isLikelyRooted(),
                "Device does not appear rooted",
                "Device may be rooted — data at elevated risk");

        setCheck(binding.rowSignature, binding.icSignature,
                SecurityHelper.isSignatureValid(requireContext()),
                "App signature verified",
                "Signature check not configured — set before publishing");

        binding.tvBuildInfo.setText(
                "Version: " + BuildConfig.VERSION_NAME
                + " (" + BuildConfig.VERSION_CODE + ")"
                + "\nBuild type: " + BuildConfig.BUILD_TYPE
                + "\nSecurity patch: " + SecurityHelper.getSecurityPatchLevel());
    }

    private void setCheck(View row, ImageView icon, boolean pass,
                          String passMsg, String failMsg) {
        icon.setImageResource(pass
                ? android.R.drawable.checkbox_on_background
                : android.R.drawable.ic_delete);
        icon.setColorFilter(ContextCompat.getColor(requireContext(),
                pass ? R.color.stat_on_time : R.color.stat_missed));
        TextView label = row.findViewWithTag("label");
        if (label != null) label.setText(pass ? passMsg : failMsg);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
