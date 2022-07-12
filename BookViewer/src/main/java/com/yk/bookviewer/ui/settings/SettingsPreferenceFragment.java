package com.yk.bookviewer.ui.settings;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.yk.bookviewer.R;
import com.yk.common.learning.GenericUniqueJobScheduler;
import com.yk.common.learning.NotificationWorker;
import com.yk.common.utils.ApplicationContext;
import com.yk.common.utils.PreferenceHelper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@RequiresApi(api = Build.VERSION_CODES.S)
public class SettingsPreferenceFragment extends PreferenceFragmentCompat {

    private PreferenceHelper preferenceHelper;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        preferenceHelper = new PreferenceHelper();
        setPreferencesFromResource(R.xml.settoings_preferences, rootKey);
        SwitchPreferenceCompat learningSwitch = findPreference("learning");
        if (learningSwitch != null) {
            learningSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((boolean) newValue)
                    new GenericUniqueJobScheduler(ApplicationContext.getContext(),
                            NotificationWorker.class, 0)
                            .schedule("LearningNotification");
                preferenceHelper.setLearningEnabled((Boolean) newValue);
                return true;
            });
            learningSwitch.setChecked(preferenceHelper.isLearningEnabled());
        }
        ListPreference learningIntervalList = findPreference("learning_delay");
        if (learningIntervalList != null) {
            learningIntervalList.setOnPreferenceChangeListener((preference, newValue) -> {
                preferenceHelper.setLearningInterval(Integer.parseInt((String) newValue));
                return true;
            });
        }
    }
}
