package com.yk.bookviewer.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yk.bookviewer.R;
import com.yk.bookviewer.databinding.FragmentSettingsBinding;
import com.yk.bookviewer.utils.ButtonHider;

import java.util.Objects;

/**
 * Settings fragment.
 * It constants all configuration that are necessary for application
 */

public final class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // replace placeholder with preference
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsPlaceholder,
                        new SettingsPreferenceFragment()).commit();
    }

    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButtonHider.hide(this);
        var libraryActionButton = (FloatingActionButton) requireView().getRootView().findViewById(R.id.library);
        libraryActionButton.setOnClickListener(v -> {
            if (!Objects.equals(Objects.requireNonNull(NavHostFragment.findNavController(this).getCurrentDestination()).getId(), R.id.navigation_home))
                NavHostFragment.findNavController(this).navigate(R.id.navigation_home);
        });
        libraryActionButton.setImageResource(R.drawable.ic_library_foreground);
    }
}