package com.yk.bookviewer.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yk.bookviewer.R;
import com.yk.bookviewer.databinding.FragmentSettingsBinding;

/**
 * Settings fragment.
 * It constants all configuration that are necessary for application
 */

public class SettingsFragment extends Fragment {

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

}