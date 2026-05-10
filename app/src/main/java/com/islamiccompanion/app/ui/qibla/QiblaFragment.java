package com.islamiccompanion.app.ui.qibla;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.islamiccompanion.app.R;
import com.islamiccompanion.app.databinding.FragmentQiblaBinding;
import com.islamiccompanion.app.util.AdHelper;

/**
 * Premium Qibla compass with:
 * - TYPE_ROTATION_VECTOR (preferred) or accel+mag fallback
 * - Low-pass filter applied in QiblaCompassView
 * - Magnetic declination corrected in QiblaViewModel
 * - Haptic feedback when aligned (within 5°)
 * - Distance to Mecca displayed
 */
public class QiblaFragment extends Fragment implements SensorEventListener {

    private FragmentQiblaBinding binding;
    private QiblaViewModel viewModel;
    private SensorManager sensorManager;
    private Sensor rotationSensor, accelerometerSensor, magnetometerSensor;

    private final float[] rotationMatrix    = new float[9];
    private final float[] orientationAngles = new float[3];
    private float[] gravity;
    private float[] geomagnetic;

    private Vibrator vibrator;
    private boolean wasAligned = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentQiblaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(QiblaViewModel.class);

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        initVibrator();

        viewModel.getQiblaBearing().observe(getViewLifecycleOwner(), bearing -> {
            binding.compassView.setQiblaBearing(bearing);
            binding.tvQiblaBearing.setText(
                    getString(R.string.qibla_bearing,
                            String.format("%.1f°", bearing)));
        });

        viewModel.getDistance().observe(getViewLifecycleOwner(), dist ->
                binding.tvQiblaDistance.setText(
                        getString(R.string.qibla_distance, dist)));

        viewModel.getStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null && !status.isEmpty()) {
                binding.tvCalibrationWarning.setText(status);
                binding.tvCalibrationWarning.setVisibility(View.VISIBLE);
            } else {
                binding.tvCalibrationWarning.setVisibility(View.GONE);
            }
        });

        viewModel.getAligned().observe(getViewLifecycleOwner(), aligned -> {
            if (Boolean.TRUE.equals(aligned)) {
                binding.tvAligned.setVisibility(View.VISIBLE);
            } else {
                binding.tvAligned.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setupSensors();
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        // Show interstitial when leaving (cooldown enforced in AdHelper)
        if (getActivity() != null) AdHelper.showInterstitialIfReady(requireActivity());
    }

    private void setupSensors() {
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometerSensor  = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (accelerometerSensor != null)
                sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
            if (magnetometerSensor != null)
                sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float azimuth;

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);
            azimuth = (float) Math.toDegrees(orientationAngles[0]);
            azimuth = (azimuth + 360) % 360;
        } else {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) gravity = event.values.clone();
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) geomagnetic = event.values.clone();
            if (gravity == null || geomagnetic == null) return;
            float[] R = new float[9], I = new float[9];
            if (!SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) return;
            SensorManager.getOrientation(R, orientationAngles);
            azimuth = (float) Math.toDegrees(orientationAngles[0]);
            azimuth = (azimuth + 360) % 360;
        }

        binding.compassView.setBearing(azimuth);

        // Qibla alignment haptic
        boolean nowAligned = binding.compassView.isAligned();
        viewModel.setAligned(nowAligned);
        if (nowAligned && !wasAligned) {
            vibrateAligned();
        }
        wasAligned = nowAligned;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor != null && sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                && accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            viewModel.setStatus(getString(R.string.qibla_calibrate_warning));
        } else {
            viewModel.setStatus(null);
        }
    }

    private void initVibrator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager)
                    requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            if (vm != null) vibrator = vm.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    private void vibrateAligned() {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(
                    new long[]{0, 60, 40, 60}, -1));
        } else {
            vibrator.vibrate(new long[]{0, 60, 40, 60}, -1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
