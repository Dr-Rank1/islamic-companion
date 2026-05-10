package com.islamiccompanion.app.ui.mosque;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.islamiccompanion.app.R;
import com.islamiccompanion.app.databinding.FragmentMosqueBinding;
import com.islamiccompanion.app.databinding.ItemMosqueBinding;

/**
 * Mosque finder using OpenStreetMap Overpass API (free, no API key).
 * Shows nearby mosques in a list with distance and directions.
 */
public class MosqueFragment extends Fragment {

    private FragmentMosqueBinding binding;
    private MosqueViewModel viewModel;
    private MosqueAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMosqueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MosqueViewModel.class);

        adapter = new MosqueAdapter(this::openDirections);
        binding.rvMosques.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMosques.setAdapter(adapter);

        // Initial UI state: show loading spinner, hide everything else
        setLoading(true);
        hideEmpty();

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (Boolean.TRUE.equals(loading)) {
                setLoading(true);
                hideEmpty();
            } else {
                setLoading(false);
            }
        });

        viewModel.getMosques().observe(getViewLifecycleOwner(), mosques -> {
            setLoading(false);
            if (mosques != null && !mosques.isEmpty()) {
                hideEmpty();
                adapter.submitList(mosques);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null && !err.isEmpty()) {
                setLoading(false);
                showEmpty(err);
            }
        });

        viewModel.getNoResults().observe(getViewLifecycleOwner(), noResults -> {
            if (Boolean.TRUE.equals(noResults)) {
                setLoading(false);
                showEmpty(getString(R.string.mosque_empty));
            }
        });

        binding.btnRefresh.setOnClickListener(v -> {
            hideEmpty();
            setLoading(true);
            viewModel.searchMosques();
        });

        viewModel.searchMosques();
    }

    private void setLoading(boolean loading) {
        if (binding == null) return;
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(String message) {
        if (binding == null) return;
        binding.tvEmpty.setText(message);
        binding.tvEmpty.setVisibility(View.VISIBLE);
    }

    private void hideEmpty() {
        if (binding == null) return;
        binding.tvEmpty.setVisibility(View.GONE);
    }

    private void openDirections(MosqueViewModel.Mosque mosque) {
        String label = mosque.name.isEmpty() ? "Mosque" : mosque.name;
        String uri = "geo:" + mosque.lat + "," + mosque.lng
                + "?q=" + mosque.lat + "," + mosque.lng
                + "(" + Uri.encode(label) + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Fallback: open browser maps
            String mapsUrl = "https://www.google.com/maps/search/?api=1&query="
                    + mosque.lat + "," + mosque.lng;
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ---- Adapter ----

    static class MosqueAdapter extends ListAdapter<MosqueViewModel.Mosque, MosqueAdapter.VH> {

        interface OnClick { void onClick(MosqueViewModel.Mosque m); }

        private final OnClick callback;

        MosqueAdapter(OnClick cb) {
            super(new DiffUtil.ItemCallback<MosqueViewModel.Mosque>() {
                @Override public boolean areItemsTheSame(@NonNull MosqueViewModel.Mosque a,
                                                         @NonNull MosqueViewModel.Mosque b) {
                    return a.id.equals(b.id);
                }
                @Override public boolean areContentsTheSame(@NonNull MosqueViewModel.Mosque a,
                                                             @NonNull MosqueViewModel.Mosque b) {
                    return a.id.equals(b.id);
                }
            });
            this.callback = cb;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemMosqueBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            MosqueViewModel.Mosque m = getItem(position);
            String name = m.name.isEmpty()
                    ? holder.itemView.getContext().getString(R.string.mosque_title)
                    : m.name;
            holder.binding.tvMosqueName.setText(name);
            holder.binding.tvMosqueDistance.setText(
                    holder.itemView.getContext()
                            .getString(R.string.mosque_km_away, m.distanceKm));
            holder.binding.btnDirections.setOnClickListener(v -> callback.onClick(m));
        }

        static class VH extends RecyclerView.ViewHolder {
            final ItemMosqueBinding binding;
            VH(ItemMosqueBinding b) { super(b.getRoot()); binding = b; }
        }
    }
}
